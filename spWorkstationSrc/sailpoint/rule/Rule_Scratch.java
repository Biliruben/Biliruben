package sailpoint.rule;


import sailpoint.object.Attributes;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningPlan.Operation;

public class Rule_Scratch extends GenericRule {
    

    @Override
    public Object execute() throws Throwable {
        Object obj = null;
        
        ProvisioningPlan plan = new ProvisioningPlan();
        AccountRequest acct = new AccountRequest();
        AttributeRequest attr = new AttributeRequest();
        attr.setName("donkey");
        attr.setValue("church");
        acct.add(attr);
        
        acct.add(new AttributeRequest("donkey", Operation.Set, "church"));
        acct.add(new AttributeRequest ("slurry", Operation.Set, "Flarm"));
        acct.add(new AttributeRequest ("password", Operation.Set, "phallus"));
        AttributeRequest secret = new AttributeRequest ("seeeecret", Operation.Set, "wheeeee");
        Attributes attrs = new Attributes();
        attrs.put(ProvisioningPlan.ARG_SECRET, true);
        secret.setArgs(attrs);
        acct.add(secret);
        plan.add(acct);

        return ProvisioningPlan.getLoggingPlan(plan);
    }

}
