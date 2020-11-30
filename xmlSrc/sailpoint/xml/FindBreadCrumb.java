package sailpoint.xml;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.biliruben.tools.xml.BreadCrumbHandler;
import com.biliruben.tools.xml.XMLProcessor;
import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class FindBreadCrumb {




	private static final String OPT_XML_FILE = "xmlFile";

	private static final String OPT_ELEMENT_NAME = "el";

	private static final String OPT_ATTR_NAME = "attr";

	private static final String DEFAULT_ATTR_DELIM = ":";

	private static final String OPT_ATTR_DELIM = "delim";

	private static final String OPT_SHOW_ATTRS = "details";

	private static GetOpts _opts;

	private static String _attrPair;

	private static boolean _locateAttr = false;

	private static String _attrDelim;

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SAXException, IOException {
		// TODO Auto-generated method stub

		init(args);
		_attrPair = _opts.getStr(OPT_ATTR_NAME);
		_attrDelim = _opts.getStr(OPT_ATTR_DELIM);
		if (_attrPair != null) {
			_locateAttr  = true;
		}
		Finder finder = new Finder(_opts.getStr(OPT_ELEMENT_NAME), _locateAttr, _attrPair, _attrDelim);
		finder.showDetails(Boolean.valueOf(_opts.getStr(OPT_SHOW_ATTRS)));
		finder.setFileName(_opts.getStr(OPT_XML_FILE));
		XMLProcessor proc = new XMLProcessor(_opts.getStr(OPT_XML_FILE), finder);
		proc.parse();
	}

	private static void init(String []args) {
		_opts = new GetOpts(FindBreadCrumb.class);

		OptionLegend legend = new OptionLegend (OPT_XML_FILE);
		legend.setDescription("XML file to examine");
		legend.setRequired(true);
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_ELEMENT_NAME);
		legend.setDescription("XML element name to locate in breadcrumb format");
		legend.setExampleValue("Element1.Element2");
		legend.setRequired(true);
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_ATTR_NAME);
		legend.setDescription("Attribute name value pair to locate in addition");
		legend.setRequired(false);
		legend.setExampleValue("name" + DEFAULT_ATTR_DELIM + "Ralph");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_ATTR_DELIM);
		legend.setDescription("Delimiter to use for attribute name value pair");
		legend.setRequired(false);
		legend.setDefaultValue(DEFAULT_ATTR_DELIM);
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_SHOW_ATTRS);
		legend.setDescription("Display attribute values");
		legend.setFlag(true);
		_opts.addLegend(legend);
		


		_opts.parseOpts(args);
	}

	static class Finder extends BreadCrumbHandler {
		private String _element;
		private boolean _locateAttr;
		private String _attrName;
		private String _attrValue;
		private boolean _showDetails;
		private String _fileName = "";

		// given an Element name, display the bread crumb
		Finder (String element, boolean locateAttr, String attrPair, String attrDelim) {
			_element = element;
			_locateAttr = locateAttr;
			if (locateAttr && (attrPair == null || attrDelim == null)) {
				throw new NullPointerException ("attrPair and attrDelim must not be null when locating attribute pair!");
			}
			if (locateAttr) {
				String[] tokens = attrPair.split(attrDelim);
				_attrName = tokens[0];
				_attrValue = tokens[1];
			}

		}
		
		public void setFileName(String fileName) {
			_fileName = fileName;
			
		}

		public void showDetails(boolean showDetails) {
			_showDetails = showDetails;
		}

		private Set <String> _crumbs = new HashSet<String>();
		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			boolean located = false;
			if (matchBreadCrumb(MatchMode.ENDS_WITH, _element)) {
				if (_locateAttr) {
					String value = attributes.getValue(_attrName);
					if (_attrValue.equals(value)) {
						located = true; 
					}
				} else {
					located = true;
				}
			}
			if (located) {
				StringBuffer results = new StringBuffer();
				if (_showDetails) {
					results.append(_fileName + ":");
				}
					results.append(getBreadCrumb());
				
				if (_showDetails) {
					for (int i = 0; i < attributes.getLength(); i++) {
						results.append(" " + attributes.getLocalName(i) + "=" + attributes.getValue(i));
					}
				}
				_crumbs.add(results.toString());
			}
		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.endDocument();
			for (String crumb : _crumbs) {
				System.out.println(crumb);
			}
		}
	}
}