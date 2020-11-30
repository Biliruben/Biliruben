package sailpoint.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sailpoint.object.IdentityHistoryItem;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;


public class Rule_PurgeDuplicateHistory extends GenericRule {

	// global vars / constants
	boolean REPORT_ONLY = false; // When true, reports summary and detail.  No data is purged.
								 // When false, reports summary and detail and purges data.

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

	/*
	 * Builds the QueryOptions.  This rule uses no filters but does rely on ordering.
	 * The ordering will be defined by the properties list passed in.  A mapping of
	 * properties to column number will also be created.
	 */
	private QueryOptions buildOptions(String[] properties) {
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
		if (item != null) { // would it?
			context.removeObject(item);
			context.commitTransaction();
			context.decache();
		}
	}

	@Override
	public Object execute() throws Throwable {
		// build QueryOptions to get a list of items in order
		QueryOptions itemHistoryOpts = buildOptions(PROPERTIES);

		// get our stuff.
		Iterator results = context.search(IdentityHistoryItem.class, itemHistoryOpts, Arrays.asList(PROPERTIES));
		Object[] last = null;
		int analyzed = 0;
		int found = 0;
		int purged = 0; // mebe there will be a report-only version
		List processedIds = new ArrayList();
		
		while (results.hasNext()) {
			analyzed++;
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
				if (!REPORT_ONLY) {
					purged++;
					purgeHistoryItem(current);
				}
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
		StringBuffer summary = new StringBuffer("Of " + analyzed + " there were " + found + " duplicates.  Purged " + purged + " items.\n");
		for (Object id : processedIds) {
			summary.append("\t" + id + "\n");
		}
		return summary.toString();
	}



}
