package sailpoint.services.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sailpoint.xml.object.CertificationItemXML;

import com.biliruben.file.FileScanner;
import com.biliruben.tools.xml.BreadCrumbHandler;
import com.biliruben.tools.xml.XMLProcessor;
import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVCollection;
import com.biliruben.util.csv.CSVExporter;

/**
 * Tool to analyze an XML files containing Certification object(s) and extract specific data to a CSV file.<br>
 * <br>
 * Required API:<br>
 * &nbsp;&nbsp;&nbsp;GetOpts 1.2<br>
 * &nbsp;&nbsp;&nbsp;ThreadRunner 1.0<br>
 * &nbsp;&nbsp;&nbsp;FileMaint 1.1<br>
 * &nbsp;&nbsp;&nbsp;XmlAPI 1.1<br>
 * &nbsp;&nbsp;&nbsp;CSVSource 1.2<br>
 * <br>
 * TODO: Expand functionality to take in command line params that specify attributes to read in and column
 * names to export them under.
 * @author trey.kirk
 *
 */
public class CertToCSV {

	private static final String ARG_XML_FILE = "xmlFile";
	private static final String COL_CERT_ID = "Certification ID";
	private static final String COL_CERT_NAME = "Certification Name";
	private static final String COL_CERT_ENTITY_ID = "Entity ID";
	private static final String COL_CERT_ENTITY_IDENTITY = "Entity Identity";
	private static final String COL_ITEM_APPLICATION = "Item Application";
	private static final String COL_ITEM_ATTR_NAME = "Item Attribute Name";
	private static final String COL_ITEM_ATTR_VALUE = "Item Attribute Value";
	private static final String COL_TARGET_ID = "Target ID";
	private static final String COL_ITEM_ID = "Item ID";
	private static final String COL_ITEM_NATIVE_ID = "Item Native Identity";
	private static GetOpts _opts;
	private static Stack<CertificationItemXML> items = new Stack<CertificationItemXML>();
	private static CSVCollection _csvCollection;
	private static boolean _firstPrint = true;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException, SAXException {
		init(args);


		FileScanner scanner = FileScanner.createScannerFromPattern(_opts.getStr(ARG_XML_FILE));
		Iterator<File> files = scanner.getIterator(false, false);
		DefaultHandler handler = new CertificationHandler(items);
		while (files.hasNext()) {
			XMLProcessor processor = new XMLProcessor(files.next().getAbsolutePath(), handler);
			processor.parse();
			_csvCollection = new CSVCollection();
			while (!items.isEmpty()) {
				CertificationItemXML nextItem = items.pop();
				addCertToCsv(nextItem);
			}
			if (!_csvCollection.isEmpty()) {
				if (_firstPrint) {
					CSVExporter.exportOnlyCSVHeader(System.out, _csvCollection);
					_firstPrint = false;
				} 
					CSVExporter.exportCSV(System.out, _csvCollection, false);
			
			}
		}
		System.out.println();

	}

	private static void addCertToCsv(CertificationItemXML certItem) {
		Map<String, String> csvLine = new HashMap<String, String>();
		csvLine.put(COL_CERT_ID, certItem.getCertificationId());
		csvLine.put(COL_CERT_NAME, certItem.getCertificationName());
		csvLine.put(COL_CERT_ENTITY_ID, certItem.getEntityId());
		csvLine.put(COL_CERT_ENTITY_IDENTITY, certItem.getEntityIdentity());
		csvLine.put(COL_ITEM_APPLICATION, certItem.getItemApplication());
		csvLine.put(COL_ITEM_ATTR_NAME, certItem.getItemAttributeName());
		csvLine.put(COL_ITEM_ATTR_VALUE, certItem.getItemAttributeValue());
		csvLine.put(COL_TARGET_ID, certItem.getEntityTargetId());
		csvLine.put(COL_ITEM_ID, certItem.getItemId());
		csvLine.put(COL_ITEM_NATIVE_ID, certItem.getItemNativeIdentity());
		_csvCollection.add(csvLine);

	}

	public static void init(String[] args) {
		_opts = new GetOpts(CertToCSV.class);

		OptionLegend legend = new OptionLegend(ARG_XML_FILE);
		legend.setDescription("File name or pattern of xml file(s) to analyze");
		legend.setRequired(true);
		legend.setExampleValue("file.xml");
		_opts.addLegend(legend);

		_opts.parseOpts(args);
	}

	// 
	private static String CRUMB_CERTIFICATION = "Certification";
	private static String CRUMB_ENTITY = CRUMB_CERTIFICATION + ".CertificationEntity";
	private static String CRUMB_ITEM = CRUMB_ENTITY + ".CertificationItem";
	private static String CRUMB_ENT_SNAPSHOT = CRUMB_ITEM + ".ExceptionEntitlements.EntitlementSnapshot";
	private static String CRUMB_ENT_ATT_MAP_ENTRY = CRUMB_ENT_SNAPSHOT + ".Attributes.Map.entry";

	private static class CertificationHandler extends BreadCrumbHandler {

		private List<CertificationItemXML> _allItems;
		private CertificationItemXML _refItem;

		CertificationHandler (List<CertificationItemXML> allItems) {
			_allItems = allItems;
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);

			if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_CERTIFICATION)) {
				// entered Certification tag, 
				_refItem = new CertificationItemXML();
				_refItem.setCertificationId(attributes.getValue("id"));
				_refItem.setCertificationName(attributes.getValue("name"));
			} else if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_ENTITY)) {
				// entered CertificationEntity tag
				_refItem.setEntityId(attributes.getValue("id"));
				_refItem.setEntityIdentity(attributes.getValue("identity"));
				_refItem.setEntityTargetId(attributes.getValue("targetId"));
			} else if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_ITEM)) {
				// entered CertificationItem tag
				_refItem.setItemId(attributes.getValue("id"));
				_refItem.setItemApplication(attributes.getValue("exceptionApplication"));
				_refItem.setItemAttributeName(attributes.getValue("exceptionAttributeName"));
				_refItem.setItemAttributeValue(attributes.getValue("exceptionAttributeValue"));
			} else if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_ENT_SNAPSHOT)) {
				_refItem.setItemNativeIdentity(attributes.getValue("nativeIdentity"));
				_allItems.add(new CertificationItemXML(_refItem));
			}
		}
	}
}

