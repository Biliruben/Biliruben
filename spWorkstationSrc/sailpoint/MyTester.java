package sailpoint;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.services.task.TaskExecutorWrapper;

public class MyTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//Class taskExecutor = sailpoint.rule.Rule_TestAuthentication.class;
		Class taskExecutor = sailpoint.rule.Rule_DeleteBigCertification.class;
		
		Attributes<String, Object> attr = new Attributes();
		//attr.put(IDXSQLAnalyzerExecutor.ARG_READ_ONLY, true);
		
		TaskExecutorWrapper wrapper = new TaskExecutorWrapper();
		SailPointContext context = wrapper.initialize();
		wrapper.execute(context, taskExecutor, attr);
	}

}
