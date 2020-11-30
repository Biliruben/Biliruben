package sailpoint.rule;

import java.util.Date;

import sailpoint.tools.xml.XMLObjectFactory;

public class Rule_ExcludeStateTest extends ExclusionRule {

    @Override
    public Object execute() throws Throwable {
        
        XMLObjectFactory xmlFactory = XMLObjectFactory.getInstance();
        Object alwaysHereKey = state.get("alwaysHereKey");
        if (alwaysHereKey == null) {
            log.warn("Adding a value that should always be there");
            state.put("alwaysHereKey", "This is always here!");
        }
        state.put("" + System.currentTimeMillis(), new Date());
        log.warn(xmlFactory.toXml(state));
        return null;
    }

}
