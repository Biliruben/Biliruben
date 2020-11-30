package sailpoint.rule;

import sailpoint.object.Attributes;
import sailpoint.object.Identity;

public abstract class PreRefreshRule extends GenericRule {
    
    protected Identity identity;
    protected Attributes environment;
    
    @Override
    public void preExecute(Attributes<String, Object> attrs) {
        super.preExecute(attrs);
        identity = (Identity)attrs.get("identity");
        environment = (Attributes)attrs.get("environment");
    }

}
