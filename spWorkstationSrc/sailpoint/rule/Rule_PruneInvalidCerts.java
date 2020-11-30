package sailpoint.rule;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import sailpoint.api.Certificationer;
import sailpoint.api.Terminator;
import sailpoint.object.Certification;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;

public class Rule_PruneInvalidCerts extends GenericRule {

	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean inspectCertification(Certification parentCert) {
		boolean found = false;
		List children = parentCert.getCertifications();
		if (children != null) {
			for (Object childObj : children) {
				Certification child = (Certification)childObj;
				if (inspectCertification(child)) {
					log.warn("Live child found: " + child);
					found = true;
				}
			}
		}

		return found;
	}

	@Override
	public Object execute() throws Throwable {

		// override the log
		log = LogFactory.getLog("sailpoint.rule.Rule_PruneInvalidCerts");
		
		
		int CLEAR_EVERY = 100;
		boolean DEBUG_ONLY = true;

		Terminator arnold = new Terminator(context);
		// Invalid certs, for the purpose of this rule, are those that have a null phaes
		// Given that a null phase is completely out of scope, we'll just look for that

		// Find the certs with null phases
		QueryOptions certOpts = new QueryOptions();
		List filters = new ArrayList();
		filters.add(Filter.isnull("phase"));		
		List properties = new ArrayList();
		properties.add("id");
		certOpts.setFilters(filters);

		Iterator results = context.search(Certification.class, certOpts, properties);
		int counter = 0;
		while (results.hasNext()) {
			counter++;
			Object[] nextObjArry = (Object[]) results.next();
			String certId = (String) nextObjArry[0];
			log.warn("Next cert: " + counter + " : " + certId);
			Certification cert = context.getObjectById(Certification.class, certId);
			if (cert == null) {
				// It left.  Possibility as while pruning, we may cascade prune invalid certs below?
				log.warn("Can't find certification: " + certId);
				continue;
			}

			// before we smoosh the cert, I'd like to follow the hierarchy and and ensure no lower cert is a valid one
			// although it should be noted that cert, being in a chain of invalid certs, is inherently invalid.
			if (inspectCertification(cert)) {
				log.error("Certification: " + cert + " has got a live kid!");
				// bail
				return cert;
			}

			log.warn("Deleting certification: " + cert);
			if (!DEBUG_ONLY) {
				arnold.deleteObject(cert);
				if (counter % CLEAR_EVERY == 0) {
					context.commitTransaction();
					context.decache();
				}
			}
		}

		return null;
	}


}
