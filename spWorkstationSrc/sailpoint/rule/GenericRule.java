package sailpoint.rule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;


public abstract class GenericRule extends AbstractTaskExecutor implements AllRules {
	
	public SailPointContext context;
	public Log log;
	
	
	
	public void execute(SailPointContext ctx, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> attrs) throws Exception {
		this.context = ctx;
		log = LogFactory.getLog(GenericRule.class);
		this.preExecute(attrs);
		try {
			Object returnResult = execute();
			result.setAttribute("result", returnResult);
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}
	
	public abstract Object execute() throws Throwable;
	
	public void preExecute(Attributes<String, Object> attrs) {
		// no op
	}
	
	public boolean terminate() {
		// nothing doing here
		return false;
	}

}
