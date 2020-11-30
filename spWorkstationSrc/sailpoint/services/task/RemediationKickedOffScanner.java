package sailpoint.services.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.CertificationAction;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;

/**
 * Locate remediation actions that are not set to be kicked off when they should be.  Correct the flag.
 * SQL query:
 * 
 * 
select spt_certification_item.id as certificationItemId,
spt_certification_entity.id as certificationEntityId,
spt_certification.id as certificationId,
spt_certification_action.id
from spt_certification_item 
join spt_certification_entity on spt_certification_item.certification_entity_id = spt_certification_entity.id
join spt_certification_action on spt_certification_item.action = spt_certification_action.id
join spt_certification on spt_certification.id = spt_certification_entity.certification_id
where spt_certification_action.status = 'Remediated'
and spt_certification_action.remediation_kicked_off <> true
and spt_certification.phase = 'End'
and spt_certification_action.remediation_action = 'OpenWorkItem'


 * @author trey.kirk
 *
 */
public class RemediationKickedOffScanner extends AbstractTaskExecutor {

	private boolean _terminated = false;
	
	private Log _log = LogFactory.getLog(RemediationKickedOffScanner.class);
	private String file;


	// Get cert, entity, item, and action id where action != true (false or null), cert phase is end, and action == 'open work item'
	private static final String QUERY = "select cert.id, entity.id, item.id, action.id " +
		"from Certification as cert " +
		"join cert.entities as entity " +
		"join entity.items as item " +
		"join item.action as action " +
		"where action.remediationKickedOff <> true " +
		"and cert.phase = 'End' " +
		"and action.remediationAction = 'OpenWorkItem'";
	
	// Index holders in case I want to pull up the cer, entity, or item
	private static final int INDEX_CERT = 0;
	private static final int INDEX_CERT_ENTITY = 1;
	private static final int INDEX_CERT_ITEM = 2;
	private static final int INDEX_CERT_ACTION = 3;

	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
	throws Exception {
		// find our remediation items
		Iterator hqlResults = findRemediations(context, result);
		int counter = 0;
		// correct them
		while (hqlResults != null && hqlResults.hasNext() && !_terminated) {
			String actionId = ((Object[]) hqlResults.next())[INDEX_CERT_ACTION].toString();
			_log.debug("Processing action: " + actionId);
			markActionAsStarted (context, actionId, result);
			counter++;
		}
		
		if (_terminated) {
			_log.warn("Task terminated by user.");
			result.addMessage(new Message(Message.Type.Info, "Task terminated by user", (Object[])null));
		}
		result.setAttribute("Revocations Corrected", counter);
	}
	
	/*
	 * Take the provided action id and mark it as kicked off.
	 */
	private void markActionAsStarted(SailPointContext context, String actionId, TaskResult result) {
		CertificationAction action = null;
		try {
			action = context.getObjectById(CertificationAction.class, actionId);
			if (action != null) { // shouldn't happen, but whatev...
				action.setRemediationKickedOff(true);
				context.commitTransaction();
				context.decache(action);
				_log.debug("Committed " + actionId);
			} else {
				_log.warn("No CertificationAction found for id " + actionId);
			}

		} catch (GeneralException e) {
			_log.error(e.getMessage());
			e.printStackTrace();
			result.addException(e);
		}

	}

	/*
	 * Given the above query, locate the actions that are not marked as kicked off which should be.
	 * Return the query results or null
	 */
	private Iterator findRemediations(SailPointContext context, TaskResult result) {

		// I'm sure I probably want to define query options and / or arguments
		// but I dunno what that'd be right now
		Map<String, Object> args = new HashMap<String, Object>();
		QueryOptions opts = new QueryOptions();
		try {
			_log.debug("Query: " + QUERY);
			Iterator results = context.search(QUERY, args, opts);
			return results;
		} catch (GeneralException e) {
			// oopsie
			_log.error(e.getMessage());
			result.addException(e);
			return null;
		}
	}



	public boolean terminate() {
		_terminated  = true;
		return _terminated;
	}

}
