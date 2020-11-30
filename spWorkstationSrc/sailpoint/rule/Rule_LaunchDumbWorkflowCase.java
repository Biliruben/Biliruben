package sailpoint.rule;

import java.util.List;

import sailpoint.api.Workflower;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskResult;
import sailpoint.object.WorkItem;
import sailpoint.object.WorkflowCase;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class Rule_LaunchDumbWorkflowCase extends GenericRule {

    public Rule_LaunchDumbWorkflowCase() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object execute() throws Throwable {
        /*
         * to launch a workflowcase, Workflower#assimilate is the jumping off point. However, this must done first
- a task result must exist. Not sure the content, but bootstrap one and set the wfcase taskResultId (Attribute) to it, or wfcase.setTaskResult(result);
- should have a WorkItem. Bootstrap that and attach wfc to the item. use the itme in the assimilate call.
         */
        
        // get the WFC
        final String WFC_ID = "derps";
        
        WorkflowCase wfc = context.getObject(WorkflowCase.class, WFC_ID);
        if (wfc == null) {
            throw new GeneralException ("naw");
        }
        
        // Wire up whatever needs wiring
        // taskresult
        TaskResult r = wfc.getTaskResult();
        if (r == null) {
            r = new TaskResult();
            r.setName(wfc.getName() + " - TaskResult");
            wfc.setTaskResult(r);
            context.saveObject(r);
            context.saveObject(wfc);
            context.commitTransaction();
        }
        
        // see if there's a WorkItem, create it if not
        QueryOptions opts = new QueryOptions();
        opts.add(Filter.eq("workflowCase", wfc.getId()));
        List<WorkItem> items = context.getObjects(WorkItem.class, opts);
        WorkItem item = null;
        if (Util.isEmpty(items)) {
            item = items.get(0);
        } else {
            // bootstrap one
            item = new WorkItem();
            item.setName(wfc.getName() + " - WorkItem");
            item.setWorkflowCase(wfc);
            context.saveObject(item);
            context.saveObject(wfc);
            context.commitTransaction();
        }
        
        
        // start it
        Workflower flower = new Workflower(context);
        // todo: check code to see what foreground is normally set to
        flower.handleWorkItem(context, item, true);
        return "Done!";
    }

}
