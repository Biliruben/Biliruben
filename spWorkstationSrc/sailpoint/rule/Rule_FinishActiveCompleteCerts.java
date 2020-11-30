package sailpoint.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import sailpoint.api.BasicMessageRepository;
import sailpoint.api.CertificationPhaser;
import sailpoint.api.Certificationer;
import sailpoint.object.Certification;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;

public class Rule_FinishActiveCompleteCerts extends GenericRule {

	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object execute() throws Throwable {
		
		log = LogFactory.getLog("sailpoint.rule.FinishActiveCompleteCerts");
		
		int CLEAR_EVERY = 100;
		boolean DEBUG_ONLY = true;
		
		Certificationer certificationer = new Certificationer(context);
		CertificationPhaser scotty = new CertificationPhaser(context, new BasicMessageRepository());
		int counter = 0;
		
		QueryOptions certOpts = new QueryOptions();
		List filters = new ArrayList();
		filters.add(Filter.eq("phase", Certification.Phase.Active));  // certs can't be active..
		filters.add(Filter.eq("complete", true));					  // and complete at the same time
		certOpts.setFilters(filters);
		List properties = new ArrayList();
		properties.add("id");
		
		Iterator results = context.search(Certification.class, certOpts, properties);
		while (results.hasNext()) {
			counter++;
			Object[] resultObj = (Object[]) results.next();
			String certId = (String)resultObj[0];
			log.debug("Fetching certification: " + certId);
			Certification cert = context.getObjectById(Certification.class, certId);
			// finish him
			
			if (cert != null) { // could it?
				if (!DEBUG_ONLY) {
					log.warn("Finishing certification: " + counter + " : " + cert);
					certificationer.finish(cert);
					scotty.advancePhase(cert);
				}
				if (counter % CLEAR_EVERY == 0) {
					context.commitTransaction();
					context.decache();
				}
			} else {
				log.warn("No certification found for id: " + certId);
			}
		}
		context.commitTransaction();
		context.decache();
		
		return null;
	}

}
