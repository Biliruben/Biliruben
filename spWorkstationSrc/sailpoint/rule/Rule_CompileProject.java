package sailpoint.rule;

import sailpoint.api.Provisioner;
import sailpoint.object.Attributes;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningProject;

public class Rule_CompileProject extends GenericRule {

    ProvisioningPlan plan = null;


    @Override
    public Object execute() throws Throwable {
        Provisioner p = new Provisioner(context);
        Attributes scriptArgs = new Attributes();
        scriptArgs.put("launcher","spadmin"); 
        scriptArgs.put("timezone","Antarctica/Casey"); 
        scriptArgs.put("refreshSource","Task");
        scriptArgs.put("locale","en_AU"); 
        scriptArgs.put("provision","true");
        scriptArgs.put("filter","name == \"KYBU680332\""); 
        ProvisioningProject compiled = p.compile(plan, scriptArgs);
        return compiled;
    }

}
