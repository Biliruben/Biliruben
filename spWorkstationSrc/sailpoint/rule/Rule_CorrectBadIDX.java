package sailpoint.rule;

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

import org.apache.commons.logging.LogFactory;

import sailpoint.server.Environment;

public class Rule_CorrectBadIDX extends GenericRule {

	private static final String CHILDREN_LIST = "children";
	boolean DEBUG_ONLY = false;
	String TABLE = "SPT_LINK";
	String PARENT = "IDENTITY_ID";

	
	static final String IDX = "IDX";
	static final String ID = "ID";
	static final String NO_ID = "NO_ID";
	Connection connection;

	void setConnection() throws SQLException {
		Environment e = Environment.getEnvironment();
		DataSource src = e.getDataSource();
		log.debug("DataSource: " + src.toString());
		connection = src.getConnection();
		log.debug("Connection: " + connection);
	}

	Object executeQuery (String sql, boolean upsert) throws SQLException {
		log.debug("Entering executeQuery: sql=" + sql + " upsert=" + upsert);
		if (upsert && DEBUG_ONLY) {
			log.warn("Skipping upsert of sql: " + sql + ", debug mode enabled");
			return 0;
		} else {
			PreparedStatement statement = connection.prepareStatement(sql);
			return executeStatement (statement, upsert);			
		}
	}

	Object executeStatement (PreparedStatement statement, boolean upsert) throws SQLException {
		log.debug("Entering executeStatement: statement=" + statement + " upsert=" + upsert);
		ResultSet rs = null;
		Integer updates = 0;
		Object returnVal = null;
		if (!upsert) {
			returnVal = statement.executeQuery();
		} else {
			returnVal = statement.executeUpdate();
		}		
		log.debug("Exiting executeStatement (" + returnVal + ")");
		return returnVal;
	}

	Map getNextRow(ResultSet rs) throws SQLException {
		log.debug("Entering getNextRow: rs=" + rs);
		Map results = null;
		if (rs.next()) {
			if (results == null) {
				results = new HashMap();
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
		log.debug("Exiting getNextRow (" + results + ")");
		return results;
	}

	boolean examineParent(Map parentObj) {
		log.debug("Entering examineParent(" + parentObj + ")");
		List children = (List) parentObj.get(CHILDREN_LIST);
		Set idxs = new TreeSet();
		int maxIdx = 0;
		for (Object childObj : children) {
			Map child = (Map)childObj;
			String childIdx = (String) child.get(IDX);
			if (childIdx != null && !childIdx.equals("")) {
				int idxInt = Integer.valueOf(childIdx);
				if (idxInt > maxIdx) {
					maxIdx = idxInt;
				}
				idxs.add(childIdx);
			} else {
				//  null or empty child idx.  If hte list is more than one, whoop whoop!
				if (children.size() > 1) {
					log.debug("Exiting examineParent(true) -- null / empty IDX with multiple children");
					return true;
				}
			}

		}
		// validate idxs set
		// - Null values were already found by now, no need to check that
		// - overlaps are detected very easily: the size of the set won't match the size of the children
		// - Gaps are found if the smallest element isn't 0 and the largest element isn't size - 1
		if (children.size() != idxs.size() || maxIdx != idxs.size() - 1) {
			log.debug("Exiting examineParent(true) -- size diff");
			return true;
		} else {
			if (!idxs.contains("0")) { // this may be moo. previous logic seems to capture all possible scenarios
				log.debug("Exiting examineParent(true) -- doesn't contain 0");
				return true;
			}
		}
		log.debug("Exiting examineParent(false)");
		return false;
	}
	
	Set findBadIdx(String table) throws SQLException {
		log.debug("Entering findBadIdx: table=" + table);
		Set ids = new HashSet();
		String sql = "select id, " + PARENT + ", idx from " + TABLE + " order by " + PARENT + ", idx asc";
		ResultSet rs = (ResultSet) executeQuery(sql, false);
		boolean done = false;
		String lastParentId = NO_ID;
		Map parentObj = new HashMap();
		List children  = new ArrayList();
		parentObj.put(CHILDREN_LIST, children);
		while (!done) {
			Map nextRow = getNextRow(rs);
			if (nextRow != null) {
				String parentId = (String) nextRow.get(PARENT);
				if (parentId != null && !parentId.equals("")) {
					if (NO_ID.equals(lastParentId)) {
						lastParentId = parentId;
					}

					if (!lastParentId.equals(parentId)) {
						// got all our children for this parent, process before moving on
						if (examineParent(parentObj)) {
							ids.add(parentObj.get(PARENT));
						}
						lastParentId = parentId;
						parentObj = new HashMap();
						children = new ArrayList();
						parentObj.put(CHILDREN_LIST, children);
					}

					parentObj.put(PARENT, parentId);
					String childId = (String) nextRow.get(ID);
					String childIdx = (String) nextRow.get(IDX);
					Map childMap = new HashMap();
					childMap.put(ID, childId);
					childMap.put(IDX, childIdx);
					children.add(childMap);
				}
			} else {
				done = true;				
			}
		}
		
		log.debug("Exiting findBadIdx: ids=" + ids);
		return ids;
	}

	void correctParent(String id) throws SQLException {
		log.debug("Entering correctParent: id=" + id);
		String sql = "select id, idx from " + TABLE + " where " + PARENT + " = '" + id + "' order by idx asc";
		ResultSet rs = (ResultSet) executeQuery(sql, false);

		// we're just going to reset the idx.
		List idIdx = new ArrayList();
		boolean done = false;
		int count = 0;
		int rows = 0;
		while (!done) {
			Map nextRow = getNextRow(rs);
			//log.debug("nextRow: " + nextRow);
			if (nextRow == null) {
				done = true;
				continue;
			}
			String childId = (String) nextRow.get(ID);
			String currentIdx = (String) nextRow.get(IDX);
			String strCount = String.valueOf(count);
			if (!strCount.equals(currentIdx)) {
				String updateSql = "update " + TABLE + " set IDX = " + count + " where ID = '" + childId + "'";
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
		log.debug("Exiting correctParent: " + id);
	}

	@Override
	public Object execute() throws Throwable {


		/*
		 * New requirements: need to do a direct query to correct the IDX.  So...
		 * - Run a query to find Null IDX
		 * - New requirement: find IDX gaps, overlaps
		 * - Run a query to learn how to order it?
		 * - Run a query to reset the IDX
		 * 
		 * Simple, right?
		 */

		log = LogFactory.getLog("sailpoint.rule.Rule_CorrectBadIDX");
		setConnection();

		// Find null idx
		Set ids = findBadIdx(TABLE);
		log.warn("Found ids: " + ids);

		Iterator iter = ids.iterator();
		StringBuffer buff = new StringBuffer();
		while (iter.hasNext()) {
			String id = (String) iter.next();
			log.warn("Correcting parent: " + id);
			correctParent(id);
			buff.append("Corrected parent cert: " + id + "\n");
		}

		return buff.toString();

	}

}
