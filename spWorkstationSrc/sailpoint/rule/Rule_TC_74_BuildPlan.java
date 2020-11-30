package sailpoint.rule;

import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningPlan.Operation;
import sailpoint.object.ProvisioningProject;
import sailpoint.provisioning.PlanCompiler;

public class Rule_TC_74_BuildPlan extends GenericRule {

    public Rule_TC_74_BuildPlan() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object execute() throws Throwable {
        String IDENTITY = "Alice.Ford";
        String APPLICATION_NAME = "ADDirectDemodata";
        String NATIVE_IDENTITY = "CN=Mary Johnson,OU=Austin,OU=Americas,OU=DemoData,DC=test,DC=sailpoint,DC=com";
        String NEW_ATTRIBUTE_NAME = "department";
        String NEW_ATTRIBUTE_VALUE = "Scout Regiment";
        
        ProvisioningPlan plan = new ProvisioningPlan();
        AccountRequest acctReq = new AccountRequest();
        Identity identity = context.getObjectByName(Identity.class, IDENTITY);
        acctReq.setApplication(APPLICATION_NAME);
        acctReq.setNativeIdentity(NATIVE_IDENTITY);
        acctReq.setOperation(AccountRequest.Operation.Modify);
        acctReq.addArgument("args", "blargs");
        //acctReq.addArgument(NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE);
        AttributeRequest attrReq = new AttributeRequest();
        attrReq.setOp(Operation.Set);
        attrReq.setName(NEW_ATTRIBUTE_NAME);
        attrReq.setValue(NEW_ATTRIBUTE_VALUE);
        acctReq.add(attrReq);
        plan.setIdentity(identity);
        plan.add(acctReq);
        
        log.warn("Plan before compile:\n" + plan.toXml());
        
        PlanCompiler gomer = new PlanCompiler(context);
        ProvisioningProject project = gomer.compile(new Attributes(), plan, new Attributes());
        
        log.warn("New fangled project:\n" + project);
       
        return project;
    }

}
