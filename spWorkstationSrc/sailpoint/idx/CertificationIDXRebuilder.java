package sailpoint.services.api.idx;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.MessageAccumulator;
import sailpoint.api.SailPointContext;
import sailpoint.idx.CertificationIDXRebuilder;
import sailpoint.idx.HibernateIndexRebuilder;
import sailpoint.object.Certification;
import sailpoint.object.CertificationItem;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;

public class CertificationIDXRebuilder implements HibernateIndexRebuilder {

	private static Log log = LogFactory.getLog(CertificationIDXRebuilder.class);
	
	public void reorderList(SailPointContext context, String certificationId,
			MessageAccumulator errHandler) throws GeneralException {

		log.info("*** Processing Cert: " + certificationId);
		Certification cert = null;
		try {
			cert = context.getObjectById(Certification.class, certificationId);
		} catch (GeneralException e) {
			log.warn("Unable to retrieve Certification: " + certificationId);
		}
		
		QueryOptions qo = new QueryOptions();
		qo.add(Filter.eq("parent", cert));
		List<Certification> actualChildren = context.getObjects(Certification.class, qo);
		
		// Clear the current list
		
		cert.setCertifications(actualChildren);

		context.commitTransaction();
	}

}
