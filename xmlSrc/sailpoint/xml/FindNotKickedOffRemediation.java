package sailpoint.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.biliruben.file.FileScanner;
import com.biliruben.tools.xml.BreadCrumbHandler;
import com.biliruben.tools.xml.XMLProcessor;
import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.csv.CSVCollection;
import com.biliruben.util.csv.CSVExporter;

public class FindNotKickedOffRemediation {

	private static final String OPT_XML_FILE = "xmlFile";
	private static GetOpts _opts;
	private static String _filePattern;
	private static PrintStream _output;
	private static Stack<Map<String, String>> _items;
	private static boolean _firstPrint = true;

	/*
	 * Find Certifications that have a Certification action with 'remediationKickedOff' != true

	 *          <CertificationAction actorName="1086648" bulkCertified="true" created="1253910735156" id="8a2fa0c223f2a65a0123f2e76934249c" modified="1253910736469" ownerName="1057401" remediationAction="OpenWorkItem" remediationKickedOff="true" reviewed="true" status="Remediated" workItem="8a2fa0c223f230b20123f29dc596026f">

	 */

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		init (args);

		_output = System.out;
		_items = new Stack<Map<String, String>>();
		DefaultHandler handler = new XmlHandler(_items);
		FileScanner scanner = FileScanner.createScannerFromPattern(_filePattern);
		Iterator<File> xmlIt = scanner.getIterator(false, false);
		while (xmlIt.hasNext()) {
			File nextFile = xmlIt.next();
			try {
				XMLProcessor proc = new XMLProcessor(nextFile.getAbsolutePath(), handler);
				proc.parse();
				CSVCollection csv = new CSVCollection();
				if (!_items.isEmpty()) {
					while (!_items.isEmpty()) {
						csv.add(_items.pop());
					}
					if (_firstPrint ) {
						CSVExporter.exportOnlyCSVHeader(_output, csv);
						_firstPrint = false;
					}
					CSVExporter.exportCSV(_output, csv, false);
				}

			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	static class XmlHandler extends BreadCrumbHandler {
		private static final String CRUMB_CERT_ACTION = "Certification.CertificationEntity.CertificationItem.CertificationAction";
		private static final String ATTRIBUTE_KICKED_OFF = "remediationKickedOff";
		private static final String CRUMB_CERTIFICATION = "Certification";
		private static final String ATTRIBUTE_ID = "id";
		private static final String ATTRIBUTE_NAME = "name";
		private static final String CERT_ID = "Certification ID";
		private static final String CERT_NAME = "Certification Name";
		private static final String CRUMB_CERTIFICATION_ENTITY = "Certification.CertificationEntity";
		private static final String ATTRIBUTE_IDENTITY = "identity";
		private static final String ENTITY_IDENTITY = "Identity";
		private static final String ENTITY_ID = "Entity ID";
		private static final String CRUMB_CERTIFICATION_ITEM = "Certification.CertificationEntity.CertificationItem";
		private static final String ITEM_ID = "Item ID";
		private static final String ACTION_ID = "Action ID";
		private static final String ATTRIBUTE_STATUS = "status";
		private static final String ATTRIBUTE_TYPE = "type";
		private static final String CERT_TYPE = "Certification Type";
		private static final String ATTRIBUTE_PHASE = "phase";
		private static final String CERT_PHASE = "Certification Phase";
		private Map<String, String> _cert;
		private List<Map<String, String>> _certs;


		public XmlHandler (List<Map<String, String>> items) {

			_certs = items;
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			// TODO Auto-generated method stub
			super.startElement(uri, localName, name, attributes);
			String id = attributes.getValue(ATTRIBUTE_ID);
			String elName = attributes.getValue(ATTRIBUTE_NAME);
			if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_CERT_ACTION)) {
				// Found a cert action, check the attributes
				String kickedOff = attributes.getValue(ATTRIBUTE_KICKED_OFF);
				String status = attributes.getValue(ATTRIBUTE_STATUS);
				if (!"true".equalsIgnoreCase(kickedOff) && !"Approved".equalsIgnoreCase(status)) {
					// Cert action with kicked off == false
					_cert.put(ACTION_ID, id);
					// add a copy
					_certs.add(new HashMap<String, String>(_cert));
					
				}
			} else if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_CERTIFICATION)) {
				// Beginning of a certification, log some information
				_cert = new HashMap<String, String>();
				_cert.put(CERT_ID, id);
				_cert.put(CERT_NAME, elName);
				_cert.put(CERT_TYPE, attributes.getValue(ATTRIBUTE_TYPE));
				_cert.put(CERT_PHASE, attributes.getValue(ATTRIBUTE_PHASE));
			} else if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_CERTIFICATION_ENTITY)) {
				// CertificationEntity
				_cert.put(ENTITY_IDENTITY, attributes.getValue(ATTRIBUTE_IDENTITY));
				_cert.put(ENTITY_ID, id);
			} else if (matchBreadCrumb(MatchMode.ENDS_WITH, CRUMB_CERTIFICATION_ITEM)) {
				// CertificationItem
				_cert.put(ITEM_ID, id);
			}
		}
	}

	private static void init(String[] args) {
		_opts = new GetOpts(FindNotKickedOffRemediation.class);

		OptionLegend legend = new OptionLegend(OPT_XML_FILE);
		legend.setRequired(true);
		legend.setExampleValue("any_file_filter");
		legend.setDescription("File name or filter of XML files to analyze");
		_opts.addLegend(legend);

		_opts.parseOpts(args);
		_filePattern = _opts.getStr(OPT_XML_FILE);

	}



}
