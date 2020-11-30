package sailpoint.services.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Attributes;
import sailpoint.object.Rule;
import sailpoint.object.TaskExecutor;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.TaskMonitor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;

public class RuleExecutor implements TaskExecutor {

	private static Log log = LogFactory.getLog(RuleExecutor.class);

	public static final String COMPONENT_NAME = "sailpoint.task.RuleExecutor";
	public static final String COMPONENT_VERSION = "1.0";
	public static final String COMPONENT_DATE = "11 August 2009 13:22 CST";

	public static final String ARG_RULE_NAME = "ruleName";
	public static final String ARG_RULE_CONFIG = "ruleConfig";

	private SailPointContext context;

	/*
	 * TaskMonitor
	 */
	TaskMonitor monitor;

	/*
	 * Termination flag
	 */
	boolean terminate;

	/*
	 * Holds all the errors
	 */
	List<Message> errors = new ArrayList<Message>();

	/*
	 * Holds all the warnings
	 */
	List<Message> warnings = new ArrayList<Message>();

	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
			throws Exception {

		log.debug("Running RuleExecutor Task...");

		log.info("Component Name [" + COMPONENT_NAME + "]");
		log.info("Component Version [" + COMPONENT_VERSION + "]");
		log.info("Component Date [" + COMPONENT_DATE + "]");
		log.info("-------------------------");

		this.context = context;

		/*
		 * Create a new TaskMonitor for the GUI.
		 */
		monitor = new TaskMonitor(context, result);

		/*
		 * Try to get the Context.
		 */
		try {

			log.debug("Getting Current Context.");
			context = SailPointFactory.getCurrentContext();

		} catch (GeneralException e) {

			throw new Exception(e);
		}

		/*
		 * Get the name of the rule from the TaskDefinition arguments.
		 */
		String ruleName = args.getString(ARG_RULE_NAME);
		log.debug(ARG_RULE_NAME + " [" + ruleName + "]");

		/*
		 * Get the configuration map from the TaskDefinition arguments.
		 */
		Map<String, Object> configMap = (Map) args.get(ARG_RULE_CONFIG);
		log.debug(ARG_RULE_CONFIG + " [" + configMap.toString() + "]");

		/*
		 * This is the input for the rule execution.
		 */
		Map<String, Object> inputMap = new HashMap<String, Object>();

		try {

			inputMap.put("context", this.context);
			inputMap.put("log", log);
			inputMap.put("config", configMap);

			Object o = runRule(ruleName, inputMap);

			if (null == o)
				throw new GeneralException("Rule [" + ruleName + "] did not return any status.");
			else if (o instanceof String)
				result.setAttribute("Result: ", o );
			else
				throw new GeneralException("Rule [" + ruleName
						+ "] did not return a String.");

		} catch (GeneralException e) {

			log.debug("Exception: " + e);
			throw new Exception(e);
		}

		log.debug("Process complete.");
		monitor.completed();

	}

	public boolean terminate() {
		return false;
	}

	private Object runRule(String ruleName, Map<String, Object> ruleContext)
			throws Exception {

		log.debug("Entering runRule.");

		Object o = null;

		log.debug("ruleName[" + ruleName + "]");
		log.debug("ruleContext[" + ruleContext + "]");

		try {

			if (null == ruleContext.get("context"))
				ruleContext.put("context", this.context);
			if (null == ruleContext.get("log"))
				ruleContext.put("log", log);

			Rule rule = context.getObject(Rule.class, ruleName);

			o = context.runRule(rule, ruleContext);

		} catch (Exception e) {

			log.debug("Exception: " + e);
			throw new GeneralException(e);

		}

		return o;
	}

}
