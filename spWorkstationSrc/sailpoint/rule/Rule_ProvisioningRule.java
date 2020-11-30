package sailpoint.rule;

import java.util.List;

import sailpoint.object.Application;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningResult;
import sailpoint.object.ResourceObject;

public class Rule_ProvisioningRule extends GenericRule {

    ProvisioningPlan plan = (ProvisioningPlan) new Object();
    Application application = (Application) new Object();
    @Override
    public Object execute() throws Throwable {
        
        // ProvisioningDelimitedFileRule
        
        // Create a feux ResourceObject and toss it into the provisioning result
        ProvisioningResult result = new ProvisioningResult();
        
        ResourceObject object = new ResourceObject("ralph", "account");
        result.setObject(object);
        result.setStatus(ProvisioningResult.STATUS_COMMITTED);
        object.setAttribute("employeeId", "zzzzzz");
        object.setAttribute("dbId", "zzzzzz");
        object.setAttribute("userName", "Ralphie Boy");
        List acctRequests = plan.getAccountRequests();
        for (Object request : acctRequests) {
            AccountRequest acctRequest = (AccountRequest)request;
            String app = acctRequest.getApplication();
            if (app.equals(application.getName())) {
                List attrRequests = acctRequest.getAttributeRequests();
                for (Object oAttrRequest : attrRequests) {
                    AttributeRequest attrRequest = (AttributeRequest)oAttrRequest;
                    String attribute = attrRequest.getName();
                    Object value = attrRequest.getValue();
                    object.setAttribute(attribute, value);
                }
            }
        }
        System.out.println("Returning plan: " + plan.toXml());
        return result;
    }

}
