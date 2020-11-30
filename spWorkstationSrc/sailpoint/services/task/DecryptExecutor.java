package sailpoint.services.task;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;

public class DecryptExecutor extends AbstractTaskExecutor {

	public static final String ARG_ENCRYPTED_VALUE = "encrypted";

	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
			throws Exception {
		String encrypted = args.getString(ARG_ENCRYPTED_VALUE);
		String decrypted = "";
		if (encrypted != null) {
			decrypted = context.decrypt(encrypted);
		}
		result.setAttribute("DecryptedValue", decrypted);
	}

	public boolean terminate() {
		// Whatever
		return false;
	}

}
