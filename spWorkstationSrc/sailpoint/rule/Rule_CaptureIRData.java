package sailpoint.rule;

import java.util.Iterator;

import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.IdentityEntitlement;
import sailpoint.object.IdentityRequest;
import sailpoint.object.IdentityRequestItem;
import sailpoint.object.Link;
import sailpoint.object.QueryOptions;
import sailpoint.tools.Util;

public class Rule_CaptureIRData extends GenericRule {
    IdentityRequest identityRequest;

    @Override
    public Object execute() throws Throwable {
        log.warn("The identityEntitlement is: " + identityRequest.toXml());
        // iterate over the RequestItems in the IdentityRequest, find the associated
        // IdentityEntitlement, and update its attributes map with additional link data
        for (IdentityRequestItem item : identityRequest.getItems()) {
            String ieId = (String)item.getAttribute("identityEntitlementId");
            if (ieId != null) {
                IdentityEntitlement ie = context.getObjectById(IdentityEntitlement.class, "ieId");
                String nativeIdentity = item.getNativeIdentity();
                if (ie != null && Util.isNotNullOrEmpty(nativeIdentity)) {
                    log.warn("IdentityEntitlement before update: " + ie.toXml());
                    QueryOptions ops = new QueryOptions();
                    ops.addFilter(Filter.eq("nativeIdentity", nativeIdentity));
                    Iterator<Object[]> results = context.search(Link.class, ops, "attributes");
                    if (results.hasNext()) {
                        Attributes linkAttributes = (Attributes) results.next()[0];
                        if (linkAttributes != null) {
                            if (ie.getAttributes() != null) {
                                ie.getAttributes().putAll(linkAttributes);
                            } else {
                                Attributes newAttributes = new Attributes(linkAttributes);
                                ie.setAttributes(newAttributes);
                            }
                            context.saveObject(ie);
                            context.commitTransaction();
                            log.warn("IdentityEntitlement after update: " + ie.toXml());
                        }
                    }
                }
            }
            context.decache();
        }
        return null;
    }

}
