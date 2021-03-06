/**
 * 
 */
package sailpoint.services.task;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.services.api.idx.HibernateIndexRebuilder;
import sailpoint.task.AbstractTaskExecutor;

/**
 * @author trey.kirk
 *
 */
public class IDXRebuilderExecutor extends AbstractTaskExecutor {

	/**
	 * 
	 */
	public IDXRebuilderExecutor() {
		// This Class will take in an id, a class, a target id, and a target class.
		// It will provide those to a specialized class that deals with the given class.
		// The specialized class will fetch the list of sub-classes from provided object and
		// regenerate the list, removing null values.
	}

	/* (non-Javadoc)
	 * @see sailpoint.object.TaskExecutor#execute(sailpoint.api.SailPointContext, sailpoint.object.TaskSchedule, sailpoint.object.TaskResult, sailpoint.object.Attributes)
	 */
	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
	throws Exception {
		//HibernateIndexRebuilder rebuilder = new CertificationItemIDXRebuilder();
		String clazzStr = args.getString("rebuilderClass");
		Class clazz = Class.forName(clazzStr);
		HibernateIndexRebuilder rebuilder = (HibernateIndexRebuilder)clazz.newInstance();
		String objId = args.getString("id");
		rebuilder.reorderList(context, objId, result);
	}

	/* (non-Javadoc)
	 * @see sailpoint.object.TaskExecutor#terminate()
	 */
	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}

	public static void main(String[] args) {

	}
}
