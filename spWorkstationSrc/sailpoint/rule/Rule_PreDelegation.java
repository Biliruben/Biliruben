package sailpoint.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.Certificationer;
import sailpoint.api.SailPointContext;
import sailpoint.api.certification.CertificationActionDescriber;
import sailpoint.object.Certification;
import sailpoint.object.CertificationItem;
import sailpoint.object.Identity;
import sailpoint.tools.GeneralException;

public class Rule_PreDelegation extends GenericRule {

    /* No... this not a pre-delegation. it's a post process wanna be delegation like bullllllshit */
    public Rule_PreDelegation() {
        log = LogFactory.getLog(Rule_PreDelegation.class);
        // TODO Auto-generated constructor stub
    }
    
    private static class SailpointProcessTaskInfo {

        private String uid;
        private String certId;
        private String delegatedCertifier;
        private List<String> taskGroup;
        

        public SailpointProcessTaskInfo(String uid, String certId, String delegatedCertifier) {
            this.uid = uid;
            this.certId = certId;
            this.delegatedCertifier = delegatedCertifier;
        }

        public void addTaskGroup (String targetId) {
            if (this.taskGroup == null) {
                this.taskGroup = new ArrayList<String>();
            }
            this.taskGroup.add(targetId);
        }
        
    }

    public Map<String, SailpointProcessTaskInfo> delegateItems(Certification certification, SailPointContext context, Identity certifier,
            Map<String, Identity> targetIdMap, CertificationActionDescriber delegationDescriber, List<CertificationItem> list)
                    throws GeneralException {
        context.startTransaction();

        Map<String, SailpointProcessTaskInfo> spProcessItems = new HashMap<>();
        for (CertificationItem certificationItem : list) {
            // check if line item is already delegated or not.
            if (!certificationItem.isDelegatedOrWaitingReview()) {
                // Assumption :- each certification will have one certifier.
                // certItem.getTargetId returns null a lot
                Identity target = targetIdMap.get(certificationItem.getTargetId());
                if (target == null) {
                    //target = context.getObject(Identity.class, certificationItem.getTargetId());
                    target = context.getObject(Identity.class, "james.smith");
                    targetIdMap.put(certificationItem.getTargetId(), target);
                }
                /*>>>>>>>>>>>>>>>>>> need to validate if we can remove the code to create default delegation description <<<<<<<<<<<<<<<<<*/
                delegationDescriber.setItem(certificationItem);
                String description = delegationDescriber.getDefaultDelegationDescription(certificationItem.getCertificationEntity());
                String delegatedCertifier;
                String delComment;
                if (target.getManager() != null) {
                    delegatedCertifier = target.getManager().getName();
                    delComment = "delegating the line item to the manager";
                } else {
                    // if target i.e. line item user does not have a manager then delegate the line item to the default user.
                    delegatedCertifier = "spadmin";
                    delComment = "delegating line item to the default configured user";
                }
                certificationItem.delegate(certifier, null, delegatedCertifier, description, delComment);
                // Add task group to SailpointProcessTaskInfo
                SailpointProcessTaskInfo spProcessItem = spProcessItems.get(delegatedCertifier);
                if (spProcessItem == null) {
                    spProcessItem = new SailpointProcessTaskInfo(UUID.randomUUID().toString(), certification.getId(), delegatedCertifier);
                    spProcessItems.put(delegatedCertifier, spProcessItem);
                }
                spProcessItem.addTaskGroup(certificationItem.getTargetId());
                log.trace("ended preparing the SailpointProcessTaskInfo.");
                // save object, it is recommended to save object after making changes in it.
            }
            context.saveObject(certificationItem);
        }

        context.commitTransaction();
        log.debug("saving certification");
        context.saveObject(certification);
        log.debug("calling certificationer refresh now.");
        new Certificationer(context).refresh(certification);
        log.debug("certification refresh completed.");
        return spProcessItems;
    }

    @Override
    public Object execute () throws Throwable {
        return null;
    }

}
