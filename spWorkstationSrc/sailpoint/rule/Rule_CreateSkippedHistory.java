package sailpoint.rule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.Bundle;
import sailpoint.object.CertifiableDescriptor;
import sailpoint.object.CertificationEntity;
import sailpoint.object.CertificationItem;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.IdentityHistoryItem;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;

public class Rule_CreateSkippedHistory extends GenericRule {

	/* User Input Section */
	/* Create date to incorporate into all Filters */
	String CREATED_SINCE_STR = "05/01/2011"; // This String MUST be in the form of MM/DD/YYYY

	/* How often to report per CertificationEntity */
	int ENTITY_REPORT_INCREMENT = 10;

	/* How often to report on history duplication analysis */
	int ITEM_HISTORY_REPORT_INCREMENT = 100;

	/* How often to decache per CertificationItem processed */
	int ITEM_DECACHE_INCREMENT = 20;

	/* End User Input Section -- do not modify further */

	String[] PROPERTIES = {  // properties are declared in the order I want it sorted
			"entitlements",          // not comparing
			"entryDate",
			"nativeIdentity",
			"application",
			"instance",
			"policy",
			"constraintName",
			"role",
			"attribute",
			"value",
			"actor",
			"created",               // not comparing
			"identityHistory.id",
			"id",                    // not comparing
			"certifiableDescriptor", // not sorting by
			"certificationLink"      // not sorting by
	};

	String[] EXCLUDED_FROM_EQUALITY = { // properties in the above array not tested for equality.  This needs to be in order for binary searching.
			"created",
			"entitlements",   // Bad: uses Object.equal().  May need to come back to this one.  Maybe not: CertifiableDescriptor includes a entSnap differencer
			"id"
	};

	String[] EXCLUDED_FROM_ORDERBY = { // properties in the above array not used for ordering.  Needs to be ordered for binary search.
			"certifiableDescriptor",
			"certificationLink"
	};

	Map columns = new HashMap();
	Date createdSince;
	Log log = LogFactory.getLog("sailpoint.rule.Rule_CreateSkippedHistory ");

	/*
	 * Builds the QueryOptions.  This rule uses no filters but does rely on ordering.
	 * The ordering will be defined by the properties list passed in.  A mapping of
	 * properties to column number will also be created.
	 */
	private QueryOptions buildOptions(String[] properties) throws IllegalArgumentException, ParseException {
		QueryOptions opts = new QueryOptions();
		columns = new HashMap();
		int i = 0;

		for (String prop : properties) {
			if (Arrays.binarySearch(EXCLUDED_FROM_ORDERBY, prop) < 0) {
				opts.addOrdering(prop, true);
			}
			columns.put(prop, i);
			i++;
		}

		// don't go all the way back in history, just since our provided create date
		Filter createFilter = Filter.ge("created", parseDateString(CREATED_SINCE_STR));
		opts.addFilter(createFilter);

		return opts;
	}

	private boolean isArrayEqual(Object[] arry1, Object[] arry2) {

		if (log.isDebugEnabled()) {
			// expensive tracing
			List list1 = Arrays.asList(arry1); // for printability
			List list2 = Arrays.asList(arry2);
			log.debug("isArrayEqual:\n\tarry1: " + list1 + "\n\tarry2: " + list2);
		}
		// Arrays are equal when:
		// - equal number of elements
		// - each element is represented in both arrays in the same order  (guess just iterate and test)
		// Hmm, this is a great general array test, but my arrays don't need to be completely equal to represent duplicated
		// history items.  Define elements that are excluded from the test (done above)

		if (arry1.length != arry2.length) { // in this rule, it shouldn't happen.  All results derive from the same properties
			return false;
		}

		for (int i = 0; i < arry1.length; i++) {
			if (Arrays.binarySearch(EXCLUDED_FROM_EQUALITY, PROPERTIES[i]) >= 0) {
				continue;
			}
			Object obj1 = arry1[i];
			Object obj2 = arry2[i];
			if ((obj1 == null) != (obj2 == null)) { // XOR -- is just one null?  Well that's wrong.
				return false;
			} else if (!(obj1 == null && obj2 == null)) { // and if both aren't null, test for equality.
				if (!(obj1.equals(obj2))) {
					return false;
				}
			} // the remaining situation is both are null, which is logically equal
		}

		// got here?  guess they're equal
		return true;
	}

	private void purgeHistoryItem(Object[] current) throws GeneralException {
		int col = (Integer) columns.get("id");
		String id = (String)current[col];
		IdentityHistoryItem item = context.getObjectById(IdentityHistoryItem.class, id);
		log.debug("Purging IdentityHistoryItem: " + item);
		if (item != null) { // would it?
			context.removeObject(item);
			context.commitTransaction();
			context.decache();
		}
	}

	private String purgeDuplicateHistory() throws GeneralException, IllegalArgumentException, ParseException {

		// build QueryOptions to get a list of items in order
		QueryOptions itemHistoryOpts = buildOptions(PROPERTIES);

		log.debug("itemHistoryOpts: " + itemHistoryOpts);
		// get our stuff.
		int totalHistoryItems = context.countObjects(IdentityHistoryItem.class, itemHistoryOpts);
		log.warn("Analyzing " + totalHistoryItems + " IdentityHistoryItems for duplicates");
		Iterator results = context.search(IdentityHistoryItem.class, itemHistoryOpts, Arrays.asList(PROPERTIES));
		Object[] last = null;
		int analyzed = 0;
		int found = 0;
		int purged = 0;
		List processedIds = new ArrayList();

		while (results.hasNext()) {
			analyzed++;
			if (analyzed % ITEM_HISTORY_REPORT_INCREMENT == 0 || analyzed == totalHistoryItems) {
				log.warn("Analyzing " + analyzed + " of " + totalHistoryItems);
			}
			if (last == null) {
				last = (Object[])results.next();
				continue;
			}

			// Arrays.equal is no good here, elements of String are != String of same value
			// Roll my own?
			Object[] current = (Object[]) results.next();
			if (isArrayEqual(current, last)) { // found a dupe.  Last remains as is for the next check.
				found++;
				processedIds.add(current[(Integer) columns.get("id")]);
				purged++;
				purgeHistoryItem(current);

			} else {  // When not equal, make this last.
				last = current;
			}
		}

		// Roll through the list, compare 'this' with 'last'.  If they match, purge 'this'
		/*
		 * 	- What are the comparision points?
		 * 	- Logging
		 */

		// Report summary, use logging for details
		StringBuffer summary = new StringBuffer("Of " + analyzed + " history items, there were " + found + " duplicates.  Purged " + purged + " items.\n");
		if (log.isDebugEnabled()) {
			for (Object id : processedIds) {
				summary.append("\t" + id + "\n");
			}
		}
		return summary.toString();
	}

	/**
	 * If the entity is of a type that is historical, add the given
	 * certification item to the Identity's certification decision history.
	 */
	private void updateDecisionHistory(Identity identity,
			CertificationItem item)
	throws GeneralException {

		if (!item.isHistorical())
			return;

		log.debug("Adding decision history for " + item);
		// IIQ 5.1p6
		// identity.addCertificationDecision(context,item);

		// IIQ 5.1p3
		identity.addCertificationDecision(item);


		// if the user has decided to revoke any roles which were required or
		// permitted by the primary role, add history for those roles.
		if (item.getAction().getAdditionalRemediations() != null){
			for(String revokedRoleId : item.getAction().getAdditionalRemediations()){
				Bundle role = context.getObjectByName(Bundle.class, revokedRoleId);
				if (role != null) {
					// IIQ 5.1p6
					// identity.addCertificationDecision(context, item, new CertifiableDescriptor(role));

					// IIQ 5.1p3
					identity.addCertificationDecision(item, new CertifiableDescriptor(role));
				}
			}
		}

		// For some reason, the decisions list is not getting saved if we have
		// just added a new item to a non-empty decision history.  Force a save
		// here to make sure it gets flushed.
		context.saveObject(identity.getHistory());
	}

	Date parseDateString(String dateString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); //please notice the capital M
		Date date = formatter.parse(CREATED_SINCE_STR);
		return date;
	}

	@Override
	public Object execute() throws Throwable {

		Date createdSince = parseDateString(CREATED_SINCE_STR);

		// Find Identities certified since some time
		log.warn("Locating certifications since " + createdSince);
		List properties = new ArrayList();
		properties.add("id");
		QueryOptions entityOpts = new QueryOptions();

		// The assumption is that more are skipped than duplicated, so
		// add all items' history and remove the duplicates later

		// find entities that for non-continuous certs
		List restrictions = new ArrayList();
		restrictions.add(Filter.ge("created", createdSince));
		restrictions.add(Filter.eq("certification.continuous", false));
		restrictions.add(Filter.ge("certification.signed", createdSince));

		entityOpts.setRestrictions(restrictions);

		log.debug("entityOpts: " + entityOpts);
		int total = context.countObjects(CertificationEntity.class, entityOpts);
		log.warn("Processing " + total + " CertificationEntities");

		Iterator entityResults = context.search(CertificationEntity.class, entityOpts, properties);

		// For each entity, Add all items as history
		int itemCount = 0;
		int entityCount = 0;
		while (entityResults != null && entityResults.hasNext()) {
			String entityId = (String)((Object[])entityResults.next())[0];
			CertificationEntity entity = context.getObjectById(CertificationEntity.class, entityId);
			entityCount++;

			Identity identity = entity.getIdentity(context);

			if (identity != null) { // no need to continue if there is no Identity
				log.debug("Processing entity: " + entity + " for " + identity);
				QueryOptions itemOptions = new QueryOptions();
				Filter itemFilter = Filter.eq("parent.id", entityId);
				itemOptions.addFilter(itemFilter);

				if (log.isDebugEnabled()) {
					int itemTotal = context.countObjects(CertificationItem.class, itemOptions);
					log.debug("Processing " + itemTotal + " items for entity " + entity);
				}
				Iterator itemResults = context.search(CertificationItem.class, itemOptions, properties);

				while (itemResults != null && itemResults.hasNext()) {
					itemCount++;
					String id = (String) ((Object[])itemResults.next())[0];
					CertificationItem item = context.getObjectById(CertificationItem.class, id);

					// Do what we all came here to see
					updateDecisionHistory(identity, item);
					if (itemCount % ITEM_DECACHE_INCREMENT == 0) {
						context.commitTransaction();
						context.decache();
						context.attach(identity);
					}
				}
			} else {
				log.warn("No identity found for CertificationEntity: " + entity);
			}

			if (entityCount % ENTITY_REPORT_INCREMENT == 0 || entityCount == total) {
				log.warn("Processed " + entityCount + " of " + total);
			}
		}

		context.commitTransaction();
		context.decache();
		// Finally, run the duplication pruner (for good measure)
		log.warn("Running duplicate history purge");
		String duplicatePurgeMsg = purgeDuplicateHistory();
		log.warn(duplicatePurgeMsg);

		return null;
	}

}
