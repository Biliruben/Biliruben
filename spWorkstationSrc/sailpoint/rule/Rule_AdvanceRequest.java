package sailpoint.rule;

import java.util.Date;

import sailpoint.object.Request;

public class Rule_AdvanceRequest extends GenericRule {

    @Override
    public Object execute() throws Throwable {
        Request req = context.getObjectById(Request.class, "2c9090fd4e6422d9014e6429267b0009");
        req.setNextLaunch(new Date(0));
        context.saveObject(req);
        context.commitTransaction();
        
        return req;
    }

}
