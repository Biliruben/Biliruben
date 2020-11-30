package sailpoint.rule;

import sailpoint.object.Application;
import sailpoint.object.Identity;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;

public class Rule_LogicalProvisioning extends GenericRule {
    
    private Application application;
    private Identity identity;
    private ProvisioningPlan plan;

    @Override
    public Object execute() throws Throwable {
        
        for (AccountRequest req : plan.getAccountRequests()) {
            req.setApplication("Schooner Active Directory");
        }
        log.warn(plan.toXml());

        return plan;
    }

}
