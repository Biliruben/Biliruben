package sailpoint.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sailpoint.api.Certificationer;
import sailpoint.object.AbstractCertificationItem;
import sailpoint.object.Certification;
import sailpoint.object.CertificationAction;
import sailpoint.object.CertificationItem;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;



public class Rule_ApproveAndSignOpenCerts extends GenericRule {

	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object execute() throws Throwable {
		Identity approver = context.getObjectByName(Identity.class, "spadmin");
		int SAVE_EVERY = 100;
		boolean revokeInstead = true;
		
		Certificationer certificationer = new Certificationer(context);
		// Find open items, approve them
		// Criteria: items that are open for certs that are not complete
		List filters = new ArrayList();
		filters.add(Filter.eq("parent.certification.complete", false));
		filters.add(Filter.eq("summaryStatus", AbstractCertificationItem.Status.Open));
		QueryOptions itemOpts = new QueryOptions();
		itemOpts.setFilters(filters);
		List properties = new ArrayList();
		properties.add("id");
		Iterator results = context.search(CertificationItem.class, itemOpts, properties);
		
		// For every item we approve, we'll need to refresh the owning certification.  Create
		// a Set for the cert ids and refresh them after approval
		Set certs = new HashSet();
		int counter = 0;
		log.warn("Processing certItem results: " + results);
		while (results.hasNext()) {
			counter++;
			Object[] objArray = (Object[]) results.next();
			String id = (String) objArray[0];
			log.warn("Approving item: " + id);
			CertificationItem item = context.getObjectById(CertificationItem.class, id);
			String certId = item.getCertification().getId();
			certs.add(certId);
			if (revokeInstead) {
                CertificationAction.RemediationAction remediationAction = CertificationAction.RemediationAction.OpenWorkItem;
                item.remediate(approver, null, remediationAction, approver.getName(), "Do EET!", null, null, null);
                
			} else {
				item.approve(approver, null);
			}
			
			if (counter % SAVE_EVERY == 0) {
				log.warn(counter + ": save & decaching");
				context.commitTransaction();
				context.decache();
			}
		}

		// save
		context.commitTransaction();
		context.decache();
		
		// Refresh certs
		log.warn("Refreshing certs...");
		for (Object objId : certs) {
			String certId = (String)objId;
			Certification cert = context.getObjectById(Certification.class, certId);
			log.warn("Refreshing cert: " + cert);
			if (cert != null) {
				certificationer.refresh(cert);
				// save
				context.commitTransaction();
				context.decache();
			}
		}
		
		
		// Find open certs, sign them
		filters = new ArrayList();
		filters.add(Filter.isnull("signed"));
		QueryOptions certOpts = new QueryOptions();
		certOpts.setFilters(filters);
		results = context.search(Certification.class, certOpts, properties);
		log.warn("Processing certification results: " + results);

		while (results.hasNext()) {
			Object[] objArray = (Object[]) results.next();
			String id = (String) objArray[0];
			log.warn("Signing cert: " + id);
			Certification cert = context.getObjectById(Certification.class, id);
			certificationer.sign(cert, approver);
			context.commitTransaction();
			context.decache();
		}
		
		return null;
	}

}
