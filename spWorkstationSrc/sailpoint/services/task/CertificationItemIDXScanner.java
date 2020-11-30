package sailpoint.services.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Certification;
import sailpoint.object.CertificationEntity;
import sailpoint.object.CertificationItem;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.services.api.idx.CertificationItemIDXRebuilder;
import sailpoint.services.api.idx.HibernateIndexRebuilder;
import sailpoint.task.AbstractTaskExecutor;

public class CertificationItemIDXScanner extends AbstractTaskExecutor {

	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
			throws Exception {
		// Scan all certifications, look for any parent with nulls in their children list
		QueryOptions ops = new QueryOptions();

		// Build the column list that will be fetched.
		List<String> idColsList = new ArrayList<String>();
		idColsList.add("id");
		int idIndex = idColsList.indexOf("id");

		// Returns just the ids
		Iterator<Object[]> it = context.search(CertificationEntity.class, ops, idColsList);
		HibernateIndexRebuilder itemRebuilder = new CertificationItemIDXRebuilder();
		while (it.hasNext() && !_terminate) {
			String id = (String)it.next()[idIndex];
			CertificationEntity entity = context.getObjectById(CertificationEntity.class, id);
			
			boolean process = false;
			try {
				// grab the items
				List<CertificationItem> items = entity.getItems();
				
				// look for nulls (indicates gaps in idx numbering
				for (CertificationItem item : items) {
					if (item == null) {
						process = true;
						// no need to look further
						break;
					}
				}
				
				// Look for idxs that were repeated
				ops = new QueryOptions();
				ops.add(Filter.eq("parent", entity));
				Iterator<Object[]> realChildren = context.search(CertificationItem.class, ops, idColsList);
				// iterate through, count each one.  If the final count doesn't match our child list, bad
				int count = 0;
				while (realChildren.hasNext()) {
					realChildren.next();
					count++;
				}
				
				if (count != items.size()) {
					process = true;
				}
				
			} catch (Throwable e) {
				// clearly we've had an issue
				process = true;
			}
			
			
			if (process) {
				itemRebuilder.reorderList(context, id, result);
			}
		}
		




	}
	
	private boolean _terminate = false;

	public boolean terminate() {
		// TODO Auto-generated method stub
		_terminate = true;
		return _terminate;
	}

}
