package sailpoint.rule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import sailpoint.object.Certification;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.Tag;
import sailpoint.tools.GeneralException;

public class Rule_ApplyMissedTags extends GenericRule {

	private Set iteratorToSet(Iterator results) {
		Set idSet = new HashSet();
		while (results != null && results.hasNext()) {
			Object[] nextArry  = (Object[]) results.next();
			idSet.add(nextArry[0]);
		}
		return idSet;
	}

	void tagCertification(String certificationId) throws GeneralException {
		// recursively tags the given cert.
		log.debug("Tagging cert: " + certificationId);
		incrementCount();
		Certification cert = context.getObjectById(Certification.class, certificationId);
		log.debug("Fetched cert: " + cert);
		cert.setTags(refreshTags());
		if (!DEBUG_MODE) {
			log.warn("Tagged cert: " + cert);
			context.saveObject(cert);
		}
		// we start from the top, so find this certs children and process also
		QueryOptions opts = new QueryOptions();
		properties.add("id");
		List filters = new ArrayList();
		filters.add(Filter.eq("parent", cert));
		opts.setFilters(filters);
		log.debug("Searching for child certs of " + certificationId + " using: " + opts);
		Iterator results = context.search(Certification.class, opts, properties);
		while (results != null && results.hasNext()) {
			Object[] nextObj = (Object[]) results.next();
			String nextId = (String) nextObj[0];
			tagCertification(nextId);
		}
	}

	void incrementCount() throws GeneralException {
		count++;
		if (!DEBUG_MODE) {
			if (BATCH_LIMIT % count == 0) {
				log.debug("Committing, decaching");
				context.commitTransaction();
				context.decache();
				tagList = null; // nulling out tag list forces us to refetch
			}
		}
	}

	List refreshTags() {
		if (tagList == null) {
			tagList = new ArrayList();
			for (String tagName : tagNames) {
				try {
					log.debug("Fetching tag: " + tagName);
					Tag t = context.getObjectByName(Tag.class, tagName);
					if (t != null) {
						log.debug("Adding tag to list: " + t);
						tagList.add(t);
					} else {
						log.error("Tag: " + tagName + " was not found.");
					}
				} catch (GeneralException e) {
					log.error("Error fetching tag: " + tagName, e);
				}
			}
		}
		log.debug("Tags: " + tagList);
		return tagList;
	}

	/* Initializers */
	boolean DEBUG_MODE = true;
	int BATCH_LIMIT = 100;

	String[] tagNames = {
			"yarp",
			"narp",
			"tarp"
	};

	int beginYear = 2011;
	int beginMonth = 3;
	int beginDay = 1;

	int endYear = 2011;
	int endMonth = 3;
	int endDay = 15;

	int count = 0;
	List tagList = null;
	List properties = new ArrayList();

	/* End Initializers */

	@SuppressWarnings("unchecked")
	@Override
	public Object execute() throws Throwable {

		/*
		 * Inputs:
		 * 	- create date range to find these certs
		 * 	- certification type
		 * 	- tags
		 * 
		 * Behavior:
		 * 	- Find the top level certs based on the date and type provided.  That is:
		 * 		- certs created during that date range of the noted type
		 * 		- certs that have no tags
		 * 		- certs that have no parent
		 * 
		 *  - Traverse the certification tree and apply the tags
		 */

		// Inputs via XML suck...  Go with in-rule variety

		log = LogFactory.getLog("sailpoint.rule.Rule_ApplyMissedTag");

		/*
		 * Paste initializers here
		 * 
		 * 
		 */

		Calendar c = new GregorianCalendar();
		c.set(beginYear, beginMonth - 1, beginDay, 0, 0, 0);
		Date beginDate = c.getTime();
		log.debug("Start date: " + beginDate);

		c.clear();
		c.set(endYear, endMonth - 1, endDay, 23, 59, 59);
		Date endDate = c.getTime();
		log.debug("End date: " + endDate);

		properties.add("id");

		// Find parent certs
		List filters = new ArrayList();
		filters.add(Filter.isnull("parent"));
		filters.add(Filter.ge("created", beginDate));
		filters.add(Filter.le("created", endDate));
		// Similar search, but this time join the Tags class.  This _should_ be a sub-set
		// of the previous result.  The objects of our desire are 
		QueryOptions matchesOpts = new QueryOptions();
		filters.add(Filter.isempty("tags"));
		matchesOpts.setFilters(filters);
		log.debug("Searching for tagged matches using: " + matchesOpts);
		Iterator allResults = context.search(Certification.class, matchesOpts, properties);

		log.debug("Searching complete");
		Set allCertMatches = iteratorToSet(allResults);

		for (Object objId : allCertMatches) {
			String certId = (String)objId;
			tagCertification(certId);
		}

		return null;
	}


}
