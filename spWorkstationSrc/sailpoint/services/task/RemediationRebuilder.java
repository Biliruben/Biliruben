package sailpoint.services.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sailpoint.api.Certificationer;
import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.CertificationAction;
import sailpoint.object.CertificationEntity;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.tools.Message;

public class RemediationRebuilder extends AbstractTaskExecutor {

	private boolean _terminate;
	private boolean _reportOnly;
	private List<Message> _messages = new ArrayList<Message>();


	/**
	 * Custom task to locate certification items that should have a revocation but don't 
	 * (a situation that can occur if WorkItems are errantly deleted).  Revocations
	 * are recreated.
	 */
	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
	throws Exception {

		// In case you'd like to see what's broke before fixing it
		_reportOnly = args.getBoolean("reportOnly");

		// The thing that's gonna do stuff
		Certificationer certificationer = new Certificationer(context);

		// In English: Find CertificationEntities with items that have actions that are have: 
		//		remediationAction == "OpenWorkItem"
		//		remediationCompleted == false
		Filter f1 = Filter.compile("CertificationEntity.items.action.remediationAction == \"OpenWorkItem\"");
		Filter f2 = Filter.compile("CertificationEntity.items.action.remediationCompleted == false");

		Filter comp = Filter.and(f1, f2);
		QueryOptions opts = new QueryOptions(comp);

		// Fetch the CertificationEntity
		List<String> props = new ArrayList<String>();
		props.add("id");
		Set<String> allEntities = iteratorToSet (context.search(CertificationEntity.class, opts, props));

		// Now find Entities that have actions that reference work items that exist
		// We'll use this set as the items to exclude from our interesting set
		Filter f3 = Filter.join("items.action.workItem", "WorkItem.id");
		comp = Filter.and(f1, f2, f3);
		opts = new QueryOptions(comp);
		Set<String> withWorkItems = iteratorToSet (context.search(CertificationEntity.class, opts, props));

		// And exclude.  What's left is what's broke
		allEntities.removeAll(withWorkItems);

		// New filtering so reset our QueryOpts.  Now we want to look closer at the CertActions
		props = new ArrayList<String>();
		props.add("items.action.id");
		// found our CertificationEntities with missing remediations, on to the fixing
		for (String entityId : allEntities) {
			CertificationEntity entity = context.getObjectById(CertificationEntity.class, entityId);
			if (!_reportOnly) {
				if (!_terminate) { // Termination check
					// Add a new filter in the mix: where CertificationEntity.id == entityId
					// We need that to find our CertificationActions by way of the owning CertificationEntity
					Filter f4 = Filter.eq("id", entityId);
					opts = new QueryOptions(f1, f2, f4);

					if (entity != null && entity.getCertification() != null) { // Wouldn't imagine they would be null.. but
						// Get the actions that indirectly belong to this CertificationEntity.  Need to use a search since
						// it traverses the CertificationItem table
						Set<String> actions = iteratorToSet(context.search(CertificationEntity.class, opts, props));
						// Add a join to the work item table, again to get a list of ids to exclude
						// Conveniently, the same join as before
						opts.add(f3);
						Set<String> actionsWithWork = iteratorToSet(context.search(CertificationEntity.class, opts, props));
						// Exclude the "haves" so only to leave me with the "have nots".  We have to do this as the "have nots"
						// need to be reset, in a manner of speaking
						actions.removeAll(actionsWithWork);

						for (String actionId : actions) {
							// 

							CertificationAction action = context.getObjectById(CertificationAction.class, actionId);
							if (action != null) {
								// reset the remediation
								action.setRemediationKickedOff(false);
								action.setWorkItem(null);
								context.saveObject(action);
							}

						}

						// commit the actions
						context.commitTransaction();
						// Make with the new remediations
						certificationer.handleRemediations(entity.getCertification(), entity);
						// I think there's already been a commit by now, but just in case...
						context.commitTransaction();
						// detach the entity, action (we have just ids to play with, so should be safe)
						context.decache();
					}
				} // if !_terminate
			} else {
				// Report only
				String nameOrId = (entity != null ? entity.getName() : entityId);
				_messages.add(new Message(Message.Type.Info, nameOrId, (Object[])null));
			}
		}
		result.addMessages(_messages);

	}

	/*
	 * Convenience method to convert an Iterator<Object []> into a Set<String>
	 * This method assumes the first element of each Object [] is the desired String
	 * value to populate the Set<String> with.  Thi is equivalent to #iteratorToSet(search, 0)
	 */
	private Set<String> iteratorToSet(Iterator<Object []> search) {
		return iteratorToSet(search, 0);
	}

	/*
	 * Convenience method to convert an Iterator<Object []> into a Set<String>
	 * This method will use the element provided as the position of the value to
	 * put in the return Set<String>
	 */
	private Set<String> iteratorToSet(Iterator<Object []> search, int element) {
		Set<String> newSet = new HashSet<String>();
		while (search.hasNext()) {
			newSet.add((String)search.next()[element]);
		}
		return newSet;		
	}


	public boolean terminate() {
		_terminate = true;
		return _terminate;
	}

}
