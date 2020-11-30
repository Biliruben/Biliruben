package sailpoint.maintenance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.idx.AnalyzeSQL;
import sailpoint.idx.HibernateIndexRebuilder;
import sailpoint.object.Attributes;
import sailpoint.object.Custom;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.server.Environment;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;

/**
 * Custom task to iterate over SQL results of id values, parent ids, and idx values and determine
 * if a parent's child list is corrupt.  Corrupt lists would be idx values having gaps (0,1,2,4,5) or
 * overlaps (0,1,2,2,3).  Corruptions in these lists cause issues in IdentityIQ such as NPE when iterating
 * over lists or apparent data missing.<br>
 * <br>
 * This class will take only one argument: report only (boolean).  It will scan all known child / parent 
 * associations that have historically been associated to IDX corruption.  For configuration, this task
 * will read a custom object named:<br>
 * <br>
 * {@link #IDX_TABLE_CUSTOM_OBJECT}<br>
 * <br>
 * If the object is not found, a default version is created with a base known set of tables.  If it is found,
 * the tables noted in the object are used exclusively.<br>
 * <br>
 * This has a potential for harsh data corruption.  That is, what if somebody updates our Custom object and puts in a 
 * column that's not part of the child / parent relationship for this table.  So now I wonder if that was a good idea<br>
 * <br>
 * 
 * @author trey.kirk
 * @see AnalyzeSQL
 * @see HibernateIndexRebuilder
 *
 */
public class IDXSQLAnalyzerExecutor extends AbstractTaskExecutor {

	public static final String ARG_READ_ONLY = "readOnly";
	private static final String ATTR_TABLE_MAPS = "tableMaps";
	private static final String COL_ID = "ID";
	private static final String COL_IDX = "IDX";
	private static final String[][] DEFAULT_MAPS = {
		{"SPT_LINK", "IDENTITY_ID"},
		{"SPT_CERTIFICATION", "PARENT"},
		{"SPT_CERTIFICATION_ENTITY", "CERTIFICATION_ID"},
		{"SPT_CERTIFICATION_ITEM", "CERTIFICATION_ENTITY_ID"}
	};
	public static final boolean DEFAULT_READ_ONLY = false;
	private static final String ENTRY_CHILDREN_LIST = "children";
	public static final String IDX_TABLE_CUSTOM_OBJECT = "IDXTablesMappings";
	private static Log log = LogFactory.getLog(IDXSQLAnalyzerExecutor.class);
	private static final String MSG_RESULT = "msg";
	private static final String NO_ID = "NO_ID";

	private Connection _connection;
	private SailPointContext _context;
	private Custom _customMap;
	private boolean _halt = false;
	private boolean _readOnly;
	private TaskResult _result;
	private List<List<String>> _tables;
	private StringBuffer _msg;

	/*
	 * Builds the custom map, saves it
	 */
	private void buildCustomMap() {
		_customMap = new Custom();
		_customMap.setName(IDX_TABLE_CUSTOM_OBJECT);
		List<List<String>> defaultMaps = buildDefaultMaps();
		Attributes<String, Object> attrs = new Attributes<String, Object>();
		attrs.put(ATTR_TABLE_MAPS, defaultMaps);
		_customMap.setAttributes(attrs);
		try {
			_context.saveObject(_customMap);
			_context.commitTransaction();
		} catch (GeneralException e) {
			_result.addException(e);
			terminate();
		}
	}

	/*
	 * Builds a list of lists of strings, where:
	 * - the outter list is the container
	 * - each element list has a pair of String elements
	 * - element 0 is the table name, element 1 is the parent column name
	 */
	private static List<List<String>> buildDefaultMaps() {

		List<List<String>> tables = new ArrayList<List<String>>();
		for (String[] line : DEFAULT_MAPS) {
			List<String> elements = new ArrayList<String>(2);
			elements.add(line[0]);
			elements.add(line[1]);
			tables.add(elements);
		}
		return tables;
	}

	/*
	 * We store the table : parentColumn mapping in a Custom object.  Fetch it. If it doesn't exist, build it.
	 */
	private void buildTableMapping() throws GeneralException {
		_customMap = _context.getObjectByName(Custom.class, IDX_TABLE_CUSTOM_OBJECT);
		if (_customMap == null) {
			log.warn(IDX_TABLE_CUSTOM_OBJECT + " not found, generating");
			buildCustomMap();
		}
		// parse it, build if unsuccessful
		Attributes<String, Object> attrs = _customMap.getAttributes();
		if (attrs != null) {
			// If it's poorly built, this will throw ClassCast or some other.  Catch and rebuild if that happens
			try {
				_tables = (List<List<String>>) attrs.get(ATTR_TABLE_MAPS);
			} catch (Throwable t) {
				// bad parse, ensure _tables is null and let the remaining code fix it
				log.warn("Custom obj doesn't contain correct data, rebuilding.");
				_tables = null;
			}
			if (_tables == null) {
				// bad value, try again.  I wonder if there's any scenario where I get stuck in an endless loop with this
				buildCustomMap();
				buildTableMapping();
			}
		}
	}

	/*
	 * Given a parent id, table, and the parent column in question, reorder the IDX values.  This ordering is a bit arbitrary and
	 * is not given much consideration to its previous ordering.
	 */
	private void correctParent(String id, String table, String parentCol) throws SQLException {
		String sql = "select " + COL_ID + ", " + COL_IDX + " from " + table + " where " + parentCol + " = '" + id + "' order by " + COL_IDX + " asc";
		ResultSet rs = (ResultSet) executeQuery(sql, false);

		// we're just going to reset the idx.
		logMessage("Correcting " + id + " on " + table);
		boolean done = false;
		int count = 0;
		int rows = 0;
		while (!done) {
			Map<String, String> nextRow = getNextRow(rs);
			if (nextRow == null) {
				done = true;
				continue;
			}
			String childId = nextRow.get(COL_ID);
			String currentIdx = nextRow.get(COL_IDX);
			String strCount = String.valueOf(count);
			if (!strCount.equals(currentIdx)) {
				String updateSql = "update " + table + " set " + COL_IDX + " = " + count + " where " + COL_ID +" = '" + childId + "'";
				rows = (Integer) executeQuery(updateSql, true);
				if (rows == 0) {
					log.warn("No Rows modified!");
				} else if (rows > 1) {
					log.warn("More than 1 row modified! (" + rows + ")");
				} else {
					log.debug("Rows modified: " + rows);
					count++;
				}				
			} else {
				count++;
			}
		}
	}

	/*
	 * Logger method.  Maybe use a Monitor at some point.
	 */
	private void logMessage(String msg) {
		_msg.append(msg + "\n");
		log.debug(msg);
	}

	/*
	 * We've build a map of attributes for the parent, now we scan the IDX values
	 */
	private boolean examineParent(Map<String, Object> parentObj) {

		List<Map<String, String>> children = (List<Map<String, String>>) parentObj.get(ENTRY_CHILDREN_LIST);
		Set<String> idxs = new TreeSet<String>(); //Why TreeSet now and HashSet earlier?
		int maxIdx = 0;
		for (Map<String, String> child : children) {
			String childIdx = child.get(COL_IDX);
			if (childIdx != null && !childIdx.equals("")) {
				int idxInt = Integer.valueOf(childIdx);
				if (idxInt > maxIdx) {
					maxIdx = idxInt;
				}
				idxs.add(childIdx);
			} else {
				//  null or empty child idx.  If the list is more than one, whoop whoop!
				if (children.size() > 1) {
					return true;
				}
			}
		}
		// validate idxs set
		// - Null values were already found by now, no need to check that
		// - overlaps are detected very easily: the size of the set won't match the size of the children
		// - Gaps are found if the smallest element isn't 0 and the largest element isn't size - 1
		if (children.size() != idxs.size() || maxIdx != idxs.size() - 1) {
			return true;
		} else {
			if (!idxs.contains("0")) { // this may be moo. previous logic seems to capture all possible scenarios
				return true;
			}
		}
		return false;
	}

	/**
	 * Entry point
	 */
	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
	throws Exception {
		_result = result;
		_context = context;
		_msg = new StringBuffer();
		if (_readOnly) {
			logMessage("Read only mode!");
		}
		fetchAttributes(args);
		buildTableMapping();
		if (!_halt) {
			_connection = getConnection(); // finally needs to close this
			try {
				for (List<String> line : _tables) {
					if (_halt) {
						_result.addMessage(Message.warn(MSG_RESULT, "Task halted by user"));
						_connection.close();
						return;
					}
					String table = line.get(0);
					String parentCol = line.get(1);
					if (table != null && parentCol != null) {
						Set<String> ids = findBadIdx(table, parentCol);
						Iterator<String> iter = ids.iterator();
						while (iter.hasNext()) {
							String id = (String) iter.next();
							correctParent(id, table, parentCol);
						}
					}
				}
			} finally {
				_connection.close();
			}
		}
		_result.setAttribute(MSG_RESULT, _msg.toString());
	}

	/*
	 * Executes a query from SQL string.
	 * This just converts the string into a PreparedStatement and passes it on to the executeStatement.
	 * Returns Integer for an upsert indicating rows updated.  Returns a ResultSet for a query
	 */
	private Object executeQuery (String sql, boolean upsert) throws SQLException {
		if (upsert && _readOnly) {
			log.warn("Skipping upsert of sql: " + sql + ", read-only mode enabled");
			return 0;
		} else {
			PreparedStatement statement = _connection.prepareStatement(sql);
			return executeStatement (statement, upsert);			
		}
	}

	/*
	 * Executes the PreparedStatement.  Set upsert to true to conver the query into an insert / update.
	 * Returns Integer for an upsert indicating rows updated.  Returns a ResultSet for a query
	 */
	private Object executeStatement (PreparedStatement statement, boolean upsert) throws SQLException {

		Object returnVal = null;
		if (!upsert) {
			returnVal = statement.executeQuery();
		} else {
			returnVal = statement.executeUpdate();
		}		
		return returnVal;
	}

	/*
	 * Parses the arguments.  Right now, the only attribute is the read only flag
	 */
	private void fetchAttributes(Attributes<String, Object> args) {
		_readOnly = args.getBoolean(ARG_READ_ONLY, DEFAULT_READ_ONLY);
	}

	/*
	 * Scans the table, groups the data by parent, and examines each parent (or rather group of children ids by parent)
	 */
	private Set<String> findBadIdx(String table, String parentCol) throws SQLException {

		logMessage("Scanning table: " + table);
		Set<String> ids = new HashSet<String>();
		String sql = "select " + COL_ID + ", " + parentCol + ", " + COL_IDX + " from " + table + " order by " + parentCol + ", idx asc";
		ResultSet rs = (ResultSet) executeQuery(sql, false);
		boolean done = false;
		String lastParentId = NO_ID;
		Map <String, Object> parentObj = new HashMap<String, Object>();
		List <Map<String, String>> children  = new ArrayList <Map<String, String>>();
		// put together map containing list of children ids
		parentObj.put(ENTRY_CHILDREN_LIST, children);
		while (!(done || _halt)) {
			Map<String, String> nextRow = getNextRow(rs);
			if (nextRow != null) {
				String parentId = nextRow.get(parentCol);
				if (parentId != null && !parentId.equals("")) {
					if (NO_ID.equals(lastParentId)) { // happens on the first iteration
						lastParentId = parentId;
					}

					// if last parent ID is different, assume this parent object is done
					// we can do this since the query sorts by parent
					if (!lastParentId.equals(parentId)) {
						// got all our children for this parent, process before moving on
						if (examineParent(parentObj)) {
							ids.add((String) parentObj.get(parentCol));
						}
						lastParentId = parentId;
						parentObj = new HashMap<String, Object>();
						children = new ArrayList <Map<String, String>>();
						parentObj.put(ENTRY_CHILDREN_LIST, children);
					}

					parentObj.put(parentCol, parentId);
					String childId = (String) nextRow.get(COL_ID);
					String childIdx = (String) nextRow.get(COL_IDX);
					Map<String, String> childMap = new HashMap<String, String>();
					childMap.put(COL_ID, childId);
					childMap.put(COL_IDX, childIdx);
					children.add(childMap);
				}
			} else {
				done = true;				
			}
		}

		return ids;
	}

	/*
	 * Converts the ResultSet to a map of known keys
	 */
	private Map<String, String> getNextRow(ResultSet rs) throws SQLException {
		Map<String, String> results = null;
		if (rs.next()) {
			if (results == null) {
				results = new HashMap<String, String>();
			}
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			log.debug("Columns: " + cols);
			for (int i = 1; i <= cols; i++) {
				String colName = md.getColumnLabel(i);
				String colValue = rs.getString(i);
				log.debug("column: " + colName + " : " + colValue);
				results.put(colName.toUpperCase(), colValue);
			}
		}
		return results;
	}

	/**
	 * Override to control what / how to connect
	 * @return
	 * @throws SQLException
	 */
	protected Connection getConnection() throws SQLException {
		Environment e = Environment.getEnvironment();
		DataSource src = e.getDataSource();
		log.debug("DataSource: " + src.toString());
		Connection connection = src.getConnection();
		log.debug("Connection: " + connection);
		return connection;
	}

	public boolean terminate() {
		_halt = true;
		return _halt;
	}

}