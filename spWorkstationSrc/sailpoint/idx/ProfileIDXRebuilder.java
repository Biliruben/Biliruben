package sailpoint.services.api.idx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.MessageAccumulator;
import sailpoint.api.SailPointContext;
import sailpoint.idx.HibernateIndexRebuilder;
import sailpoint.idx.ProfileIDXRebuilder;
import sailpoint.object.Bundle;
import sailpoint.object.Filter;
import sailpoint.object.Bundle;
import sailpoint.object.Profile;
import sailpoint.object.Profile;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;
import sailpoint.tools.Message.Type;

public class ProfileIDXRebuilder implements HibernateIndexRebuilder {
	
	private static Log log = LogFactory.getLog(ProfileIDXRebuilder.class);

	public void reorderList(SailPointContext context, String id,
			MessageAccumulator errHandler) throws GeneralException {
		// TODO Auto-generated method stub
		
		Bundle role = context.getObjectById(Bundle.class, id);
		
		List<Profile> newProfiles = new ArrayList<Profile>();
		
		QueryOptions qo = new QueryOptions();
		qo.add(Filter.eq("bundle", role));
		List<Profile> allProfiles = context.getObjects(Profile.class, qo);
		
		if (allProfiles != null) {
			for (Profile profile : allProfiles) {
				// not sure if an item can be null.  But hey, the assumption is stuff is already messed up.  We
				// should double check
				if (profile != null) {
					log.debug("Examining Profile: " + profile.getId());

					Bundle parent = profile.getBundle();
					// Same clause as before: dont' expect we could get a null entity, but let's assume stuff ain't right
					// to begin with
					if (parent != null) {
						log.debug(profile.getId() + " Bundle: " + parent.getId());
						if (parent.getId().equalsIgnoreCase(parent.getId())) {
							// matching parents, put it in our new list
							log.info(parent.getId() + " matches profile's bundle.");
							newProfiles.add(profile);
						}
					} else {
						// Should note that it's horked
						log.warn(profile.getId() + " is an orphan.");
						// Suppose we should tell the error handler too
						errHandler.addMessage(new Message(Type.Warn, profile.getId() + " is an orphan.", (Object[])null));
					}
				}

			}

			// Remove old links
			if (role.getProfiles() != null) {
				List<Profile> oldProfiles = new ArrayList<Profile>(role.getProfiles());
				Collections.copy(oldProfiles, role.getProfiles());
				for (Profile profile : oldProfiles) {
					if (profile != null) {
						role.remove(profile);
					}

				}
			}
			context.commitTransaction();
			context.decache(role);
			Bundle freshRole = null;
			
			try {

				freshRole = context.getObjectById(Bundle.class, id);

				// Replace the old list with the new one
				for (Profile newProfile : allProfiles) {
					freshRole.add(newProfile);
				}
				
				context.commitTransaction();
				
			} catch (Throwable t) {
				StringBuffer errMsg = new StringBuffer("Error when comitting Role " + id + " back in.  Confirm the following profiles are not orphaned:\n");
				if (freshRole != null) {
					errMsg.append("bundle_id: " + freshRole.getId() + "\n");
					for (Profile newProfile : allProfiles) {
						if (newProfile != null) {
							errMsg.append("\tid: " + newProfile.getId() + "\n");
						}
					}
				}
				log.error(errMsg.toString());
				errHandler.addMessage(new Message(Type.Error, errMsg.toString(), (Object[])null));
			}
		}

	}

}
