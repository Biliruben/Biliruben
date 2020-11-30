package sailpoint.rule;

import sailpoint.object.Bundle;
import sailpoint.object.EntitlementGroup;
import sailpoint.object.EntitlementSnapshot;
import sailpoint.tools.Util;

public class Rule_ExcludeItemsStartingWithA extends ExclusionRule {

    @Override
    public Object execute() throws Throwable {

        for (Object item : items) {
            if (item instanceof Bundle) {
                Bundle bundle = (Bundle)item;
                if (bundle.getName().toLowerCase().startsWith("a")) {
                    itemsToExclude.add(item);
                }
            } else if (item instanceof EntitlementSnapshot) {
                EntitlementSnapshot entitlement = (EntitlementSnapshot)item;
                if (entitlement.getAttributeValue() != null && 
                        entitlement.getAttributeValue().toString().toLowerCase().startsWith("a")) {
                    itemsToExclude.add(item);
                }
            }
        }
        
        if (!Util.isEmpty(itemsToExclude)) {
            for (Object item : itemsToExclude) {
                items.remove(item);
            }
        }
        
        return "I hate things that start with 'A'";
    }
    
    

}
