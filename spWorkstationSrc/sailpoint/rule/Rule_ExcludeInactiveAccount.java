package sailpoint.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.LogFactory;

import sailpoint.object.Application;
import sailpoint.object.Certifiable;
import sailpoint.object.EntitlementGroup;
import sailpoint.object.Identity;
import sailpoint.object.Link;

public class Rule_ExcludeInactiveAccount extends ExclusionRule {

	@Override
	public Object execute() throws Throwable {

		log = LogFactory.getLog("sailpoint.rule.Rule_ExcludeInactiveAccount");
		
		Set excludedNativeIdentities = new TreeSet();
		List iterationList = new ArrayList(items);
		for (Object obj : iterationList) {
			Certifiable item = (Certifiable)obj;
			log.debug("Processing item: " + item);
			if (item instanceof EntitlementGroup) {
				log.debug("...is EntitlementGroup");
				Identity identity = (Identity)entity;
				log.debug("Identity: " + identity);
				String nativeIdentity = ((EntitlementGroup) item).getNativeIdentity();
				String appName = ((EntitlementGroup) item).getApplicationName();
				String identityApp = nativeIdentity + ":" + appName ;
				log.debug("Using key: " + identityApp);
				if (!excludedNativeIdentities.contains(identityApp)) {
					log.debug("Not in set: " + excludedNativeIdentities);
					Application applicationObj = ((EntitlementGroup) item).getApplicationObject(context);
					List links = identity.getLinks(applicationObj);
					for (Object linkObj : links) {
						Link link = (Link)linkObj;
						log.debug("Searching link: " + link);
						if (link != null && nativeIdentity.equals(link.getNativeIdentity())) {
							// found link for given nativeIdentity
							log.debug("Disabled status: " + link.isDisabled());
							log.debug("Inactive status: " + link.getAttribute("inactive"));
							if (link.isDisabled() || "true".equalsIgnoreCase((String)link.getAttribute("inactive"))) {
								// inactive account, exclude
								log.debug("Excluding item: " + item);
								itemsToExclude.add(item);
								excludedNativeIdentities.add(identityApp);
							}
						}
					}
				} else {
					// inactive account, exclude
					itemsToExclude.add(item);
				}
			}
		}
		
		items.removeAll(itemsToExclude);
		
		return "Inactive account";
	}

}
