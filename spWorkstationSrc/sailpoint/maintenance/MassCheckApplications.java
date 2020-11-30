package sailpoint.maintenance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sailpoint.api.SailPointContext;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.tools.GeneralException;

/**
 * Two basic functions are done with this:<br>
 * <li>Connection check for all applications</li>
 * <li>ConnectorDebug iteration for all applications</li>
 * <br>
 * The inputs will include:<br>
 * <li>Application filter</li>
 * <li>Flags dictating phases to run (i.e. just do test, or connectorDebug)</li>
 * <br>
 * The connection check will output a table of results to the TaskResult.  The connectorDebug results will store
 * details in something.  Summary results (time and total) will go to TaskResult
 * @author trey.kirk
 *
 */
public class MassCheckApplications extends AbstractTaskExecutor {

	private static final String ARG_APP_FILTER = "filter";
	private static final String ARG_DEBUG_TEST = null;
	private static final String ARG_CONNECTOR_TEST = null;
	private SailPointContext _context;
	private TaskResult _taskResult;
	private QueryOptions _opts;
	private boolean _doConnectorTest;
	private boolean _doDebugTest;
	private List<String> _ids;

	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
			throws Exception {
		_context = context;
		_taskResult = result;
		fetchAttributes(args);
		if (_doConnectorTest) {
			checkConnections();
		}
	}

	private void checkConnections() {
		// TODO Auto-generated method stub
		
	}

	private void fetchAttributes(Attributes<String, Object> args) throws GeneralException {
		String applicationFilter = args.getString(ARG_APP_FILTER);
		_opts = new QueryOptions();
		_opts.setOrderBy("name");
		if (applicationFilter != null) {
			Filter filter = Filter.compile(applicationFilter);
			_opts.add(filter);
		}
		_doConnectorTest = args.getBoolean(ARG_CONNECTOR_TEST, false);
		_doDebugTest = args.getBoolean(ARG_DEBUG_TEST, false);
		
		List<String> props = new ArrayList<String>();
		props.add("id");
		Iterator<Object[]> results = _context.search(Application.class, _opts, props);
		_ids = new ArrayList<String>();
		while (results.hasNext()) {
			String id = (String) results.next()[0];
			_ids.add(id);
		}
	}
	
	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}

}
