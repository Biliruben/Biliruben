package sailpoint.rule;

import java.util.List;

import sailpoint.api.IdentityService;
import sailpoint.object.Application;
import sailpoint.object.AttributeAssignment;
import sailpoint.object.Link;
import sailpoint.tools.Util;

public class Rule_DiddleTheAttributeAssignments extends PreRefreshRule {

    @Override
    public Object execute() throws Throwable {

        // Sniff the AttributeAssignments and look for any pointing to a Link that doesn't exist. Set the nativeIdentity to null on said assignment
        // and let it fall away naturally
        IdentityService iSvc = new IdentityService(context);
        boolean dirty = false;
        List assignments = identity.getAttributeAssignments();
        if (!Util.isEmpty(assignments)) {
            for (Object assignmentObj : assignments) {
                AttributeAssignment assignment = (AttributeAssignment)assignmentObj;
                Link matched = null;
                Application assApp = null;
                if (assignment.getApplicationId() != null) {
                    assApp = context.getObject(Application.class, assignment.getApplicationId());
                }
                String instance = assignment.getInstance();
                String nativeIdentity = assignment.getNativeIdentity();
                if (nativeIdentity != null && !"".equals(nativeIdentity.trim()) && assApp != null) {
                    matched = iSvc.getLink(identity, assApp, instance, nativeIdentity);
                }
                if (matched == null) {
                    assignment.setNativeIdentity(null);
                    dirty = true;
                }
            }
        }
        
        if (dirty) {
            context.saveObject(identity);
            context.commitTransaction();
        }
        
        return null;
    }

}
