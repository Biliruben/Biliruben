/**
 * 
 */
package sailpoint.services.task;

import java.util.Date;

import sailpoint.api.DatabaseVersionException;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Attributes;
import sailpoint.object.TaskDefinition;
import sailpoint.object.TaskDefinition.ResultAction;
import sailpoint.object.TaskItemDefinition;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.spring.SpringStarter;
import sailpoint.task.AbstractTaskExecutor;


/**
 * @author trey.kirk
 * A convenience class that allows me to run a TaskExecutor class stand-alone without having
 * to setup Tomcat or a console.  Basically a re-usable set of code that will
 * get CIQ / IIQ running and get me a context to pass to the task.
 */
public class TaskExecutorWrapper {
	SpringStarter ss;

	public SailPointContext initialize() {
		SailPointContext ctx = null;
		String dflt = "iiqBeans";
		//String dflt = "ciqBeans";
		ss = new SpringStarter(dflt, null);

		String configFile = ss.getConfigFile();
		if (!configFile.startsWith(dflt))
			System.out.println("Reading spring config from: " + configFile);

		try {
			// suppress the background schedulers
			ss.setSuppressTaskScheduler(true);
			ss.setSuppressRequestScheduler(true);
			ss.start();

			ctx = SailPointFactory.createContext();

		}
		catch (DatabaseVersionException dve) {
			// format this more better  
			System.out.println(dve.getMessage());
		}
		catch (Throwable t) {
			System.out.println(t);
		}

		return ctx;


	}
	
	private TaskDefinition _taskDef;
	
	public void setTaskDefinition (TaskDefinition task) {
		_taskDef = task;
	}
	
	public TaskDefinition getTaskDefinition() {
		return _taskDef;
	}
	
	/**
	 * @param args
	 */
	public void execute(SailPointContext ctx, Class taskExecutor, Attributes<String, Object> attr) {

		try {
			AbstractTaskExecutor task = (AbstractTaskExecutor)taskExecutor.newInstance();
			String resultName = "Task Executor Wrapper";
			TaskResult result;
			TaskDefinition definition = null;
			if (getTaskDefinition() != null) {
				definition = getTaskDefinition();
				attr = definition.getArguments();
				
			} else {
				definition = ctx.getObject(TaskDefinition.class, resultName);
				if (definition == null) {
					definition = new TaskDefinition();
					definition.setExecutor(taskExecutor);
					definition.setName(resultName);
					definition.setResultAction(ResultAction.RenameNew);
					definition.setType(TaskItemDefinition.Type.Generic);
					ctx.saveObject(definition);
				}				
			}

			result = ctx.getObject(TaskResult.class, resultName);
			if (result == null) {
				result = new TaskResult();
			} else {
				// clear the current set of results
				result.setAttributes(new Attributes());
				//result.clear();
			}

			result.setName(resultName);
			result.setType(definition.getType());
			result.setLauncher("Admin");
			result.setLaunched(new Date());

			result.setDefinition(definition);
			
			TaskSchedule sched = new TaskSchedule();
			sched.setNextExecution(new Date());
			sched.setTaskDefinition(definition);
			//sched.setDefinition(definition);

			task.execute(ctx, sched, result, attr);
			result.setCompleted(new Date());
			System.out.println(result.toXml());
			result.setAttributes(null);
			/*
			ctx.saveObject(definition);
			ctx.saveObject(result);
			ctx.commitTransaction();
*/
		} catch (Throwable t) {
			throw new RuntimeException (t);
		} finally {
			ss.close();
		}
	}
}