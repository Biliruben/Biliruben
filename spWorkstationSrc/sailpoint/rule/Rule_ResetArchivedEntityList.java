package sailpoint.rule;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import sailpoint.object.ArchivedCertificationEntity;
import sailpoint.object.Certification;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;

public class Rule_ResetArchivedEntityList extends GenericRule {

	String certId = "29ddb1b62e4827ad012e57158b8c2d11";
	
	@Override
	public Object execute() throws Throwable {
	
		
		log = LogFactory.getLog("sailpoint.rule.Rule_ResetArchivedEntityList");
		// A given certification has archived entities with IDX gaps.  We need to reset that list
		
		log.debug("Fetching Certification: " + certId);
		// Get certification
		Certification cert = context.getObjectById(Certification.class, certId);
		
		// Get true list of archived entities
		QueryOptions opts = new QueryOptions();
		List filters = new ArrayList();
		filters.add(Filter.eq("certification", cert));
		opts.setRestrictions(filters);
		List props = new ArrayList();
		props.add("id");
		
		List newArchived = new ArrayList();
		
		Iterator results = context.search(ArchivedCertificationEntity.class, opts, props);
		while (results != null && results.hasNext()) {
			Object[] result = (Object[]) results.next();
			String arcEntityId = (String) result[0];
			newArchived.add(arcEntityId);
			
			// detach entity from certification.  This is how we effectively clear a list that is corrupt
			ArchivedCertificationEntity entity = context.getObjectById(ArchivedCertificationEntity.class, arcEntityId);
			entity.setCertification(null);
			context.saveObject(entity);
		}
		
		// commit changes
		context.commitTransaction();
		
		// drop current cert from memory and refetch
		context.decache();
		cert = context.getObjectById(Certification.class, certId);
		
		List newArchivedObjects = new ArrayList();
		// Reset list
		for (Object entityId : newArchived) {
			ArchivedCertificationEntity newEntity = context.getObjectById(ArchivedCertificationEntity.class, (String)entityId);
			newArchivedObjects.add(newEntity);
		}
		
		cert.setArchivedEntities(newArchivedObjects);
		
		context.commitTransaction();
		
		
		return null;
	}

}
