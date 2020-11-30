package sailpoint.rule;

import sailpoint.api.ObjectUtil;
import sailpoint.api.PersistenceManager;
import sailpoint.api.PersistenceManager.LockParameters;
import sailpoint.object.TaskResult;

public class Rule_LockObject extends GenericRule {

    private static final String TASK_RESULT_ID = "1";
    
    @Override
    public Object execute() throws Throwable {
        String TASK_RESULT_ID = "Scale Identity AA";

        TaskResult res = context.getObjectById(TaskResult.class, TASK_RESULT_ID);
        if (res != null) {
            System.out.println("Found TaskResult: " + res.getName());
            LockParameters params = new LockParameters(res);
            params.setLockType(PersistenceManager.LOCK_TYPE_PERSISTENT);
            res = ObjectUtil.lockObject(context, TaskResult.class, params);
        } else {
            System.out.println("I didn't find SHIT!");
        }

        
        return res;
    }

}
