package sailpoint.services.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Certification;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.services.api.idx.CertificationIDXRebuilder;
import sailpoint.services.api.idx.HibernateIndexRebuilder;
import sailpoint.task.AbstractTaskExecutor;

public class CertificationIDXScanner extends AbstractTaskExecutor {

	private static Log log = LogFactory.getLog(CertificationIDXScanner.class);
	private boolean _terminate = false;
	private HibernateIndexRebuilder _rebuilder = new CertificationIDXRebuilder();

	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
	throws Exception {
		// Scan all certifications, look for any parent with nulls in their children list
		QueryOptions ops = new QueryOptions();

		// Build the column list that will be fetched.
		List<String> idColsList = new ArrayList<String>();
		idColsList.add("id");
		idColsList.add("name");
		int idIndex = idColsList.indexOf("id");
		int nameIndex = idColsList.indexOf("name");

		// Returns just the ids
		Iterator<Object[]> it = context.search(Certification.class, ops, idColsList);

		// Iterate through list
		while (it.hasNext() && !_terminate) {
			Object[] current = it.next();
			String id = (String)current[idIndex];
			String name = (String)current[nameIndex];
			log.debug("Certification: " + id);
			Certification cert = context.getObjectById(Certification.class, id);

			boolean rebuildCert = false;
			try {			
				List<Certification> children = cert.getCertifications();
				// null IDX or gaps in IDX will be caught here
				if (children != null) {

					for (Certification child : children) {
						if (child == null) {
							rebuildCert = true;
							break;  // for loop
						}
					}

				}

				// Repeated IDX caught here
				if (children == null) {
					children = new ArrayList<Certification>();
				}

				QueryOptions qo = new QueryOptions();
				qo.add(Filter.eq("parent", cert));
				List<Certification> actualChildren = context.getObjects(Certification.class, qo);

				if (actualChildren != null && (actualChildren.size() != children.size())) {
					rebuildCert = true;
				}

			} catch (Throwable e) {
				// If we're here, clearly we have an issue
				rebuildCert = true;
			}

			if (rebuildCert) {
				List<String> processed = (List<String>) result.getAttribute("processed");
				if (processed == null) {
					processed = new ArrayList<String>();
					result.setAttribute("processed", processed);
				}
				processed.add("\n" + name);
				_rebuilder.reorderList(context, id, result);
			}	

		}
	}

	public boolean terminate() {
		// TODO Auto-generated method stub
		_terminate = true;
		return _terminate;
	}

}
