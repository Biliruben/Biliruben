package sailpoint.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sailpoint.api.Identitizer;
import sailpoint.api.IdentityService;
import sailpoint.connector.ADLDAPConnector;
import sailpoint.integration.Util;
import sailpoint.object.Application;
import sailpoint.object.AttributeAssignment;
import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.IdentityEntitlement;
import sailpoint.object.Link;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;

public class Rule_TwerkAttributeAssignment extends PreRefreshRule {
    
    @Override
    public Object execute() throws Throwable {
        boolean doProvisioning = environment.getBoolean(Identitizer.ARG_PROVISION);
        if (!doProvisioning) {
            // If the refresh isn't going to partition, don't mess with all the heavy
            // lifting
            return null;
        }
        
        List assignments = (List) identity.getPreference("attributeAssignments");

        for (Object assignment : assignments) {
            // see if assignment is still valid
            AttributeAssignment attrAssignment = (AttributeAssignment)assignment;
            IdentityService svc = new IdentityService(context);

            // Consider evaluating attrAssignment.getApplicationName against a list
            // of known Applications we want to test assignments against. If it's
            // not in the list, move on and avoid unnecessary activity. This example
            // skips such a check
            
            String applicationId = attrAssignment.getApplicationId();
            if (!Util.isNotNullOrEmpty(applicationId)) {
                // nothing to do here; move on
                continue;
            }
            Application application = context.getObjectById(Application.class, applicationId);
            // Another place to check if we're doing more work than we need to. This form
            // of AttributeAssignment verification only needs to happen with Applications
            // that change nativeIdentities during a move or rename. 
            // This example only cares about ActiveDirectory accounts
            if (application == null || !ADLDAPConnector.class.isAssignableFrom(Class.forName(application.getConnector()))) {
                // nothing to do here; move on
                continue;
            }
            Link link = svc.getLink(identity, application, attrAssignment.getInstance(), attrAssignment.getNativeIdentity());
            if (link == null) {
                // ok, got an assignment for a link that doesn't exist on an application
                // that we know renames nativeIdentities when an account is moved. So now
                // see if the account really was moved
                //
                // In AD, we can use the attribute 'sAMAccountName' to determine if an account was moved or
                // is for a uniquely different account. We can then compare the sAMAccountName with the original
                // link attributes our custom LCM Provisioning sub pull out and stuff into the related IdentityEntitlement
                
                List ieAttributes = getRelatedIdentityEntitlementAttributes(identity, attrAssignment);
                
                // If any of the found attributes has data that correlates to the attributeAssignment,
                // we need to see if these attributes correlate to an existing Link
                String movedLinkIdentity = findRelatedLinkIdentity(svc, "sAMAccountName", ieAttributes, identity, application, attrAssignment.getInstance());
                
                // last step: we found a link that correlates to our IdentityEntitlement attributes and otherwise
                // matches the original assignment. Update the assignment to reflect the new nativeIdentity
                if (Util.isNotNullOrEmpty(movedLinkIdentity)) {
                    attrAssignment.setNativeIdentity(movedLinkIdentity);
                    context.saveObject(identity);
                    context.commitTransaction();
                    // we're done with this assignment, move on
                    continue;
                }
            }
        }
        return null;
    }
    
    private String findRelatedLinkIdentity(IdentityService svc, String correlatingAttribute, List ieAttributesList, Identity identity, Application application, String instance) throws GeneralException {
        // we may have several IdentityEntitlement Attributes, only one has to have correlating data
        List links = svc.getLinks(identity, application, instance);
        if (Util.isEmpty(links)) {
            return null;
        }
        for (Object candidateLink : links) {
            Object linkAttribute = ((Link)candidateLink).getAttribute(correlatingAttribute);
            if (linkAttribute != null) {
                for (Object ieAttributes : ieAttributesList) {
                    Object ieAttribute = ((Attributes)ieAttributes).get(correlatingAttribute);
                    if (ieAttribute != null && ieAttribute.equals(linkAttribute)) {
                        // winner winner!
                        return ((Link)candidateLink).getNativeIdentity();
                    }
                }
            }
        }
        // not found
        return null;
    }

    /*
     * Finds the related IdentityEntitlements
     */
    private List getRelatedIdentityEntitlementAttributes(
            Identity identity, AttributeAssignment attrAssignment) throws GeneralException {
        QueryOptions ops = new QueryOptions();
        // index for a binary value; in the end may not be as helpful
        ops.setDistinct(true);
        ops.addFilter(Filter.eq("assigned", true));
        ops.addFilter(Filter.eq("identity", identity));
        ops.addFilter(Filter.eq("application.id", attrAssignment.getApplicationId()));
        ops.addFilter(Filter.eq("nativeIdentity", attrAssignment.getNativeIdentity()));
        // this example is narrow in scope and is only concerned with attributeAssignments, so
        // fetch Entitlement type only
        ops.addFilter(Filter.eq("type", ManagedAttribute.Type.Entitlement));
        if (Util.isNotNullOrEmpty(attrAssignment.getInstance())) {
            ops.addFilter(Filter.eq("instance", attrAssignment.getInstance()));
        }
        // We only care about the attributes bag for these objects, so reduce the amount
        // of potential data by asking for just that
        Iterator results = context.search(IdentityEntitlement.class, ops, "attributes");
        List attributes = new ArrayList();
        while (results.hasNext()) {
            Object[] result = (Object[]) results.next();
            attributes.add(result[0]);
        }
        return attributes;
    }

}
