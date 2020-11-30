package sailpoint.xml;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.biliruben.tools.xml.BreadCrumbHandler.MatchMode;
import com.biliruben.tools.xml.XMLProcessor;
import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

/**
 * Tool that examines an input xml file and reports attribute information at the desired xml element level.  The level is basically
 * the "depth" of an element where the root element is level 1, the immediate children elements to root are one level deeper putting
 * them at level 2, etc.
 * 
 * Required SailPoint Services APIs:
 * 	GetOpts 1.2
 *  XmlAPI 1.1
 * @author trey.kirk
 * @version 1.0
 */
public class XMLAnalyzerApp {

	private static GetOpts _opts;
	private static final String OPT_INCLUDE_FILTER = "filter";
	private static final String OPT_OUTPUT_FORMAT = "format";
	private static String OPT_XML_FILE = "xmlFile";
	private static String OPT_TARGET_LEVEL = "startLevel";
	private static String OPT_STOP_LEVEL = "stopLevel";
	private static String OPT_ADDITIONAL_DATE = "dateAttribute";
	private static final String FORMAT_ARCHIVE = "archive";
	private static final String FORMAT_CSV = "csv";
	private static final String OPT_FILTER_SCOPE = "filterScope";
	private static final String SCOPE_SAME_LEVEL = "sameLevel";
	private static final String SCOPE_LEVEL_WITH_CHILDREN = "subLevel";
	private static final String OPT_NO_DATES = "noDates";
	
	/**
	 * @param args
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SAXException, IOException {
		// Parse an xml and report the attributes.  Convert timestamp based attributes to local date format

		init(args);
		
		PrintStream out = System.out;

		String startLevelStr = _opts.getStr(OPT_TARGET_LEVEL);
		int startLevel = Integer.valueOf(startLevelStr);
		
		String endLevelStr = _opts.getStr(OPT_STOP_LEVEL);
		int stopLevel = Integer.valueOf(endLevelStr);
		
		//String elementFilter = _opts.getStr(OPT_INCLUDE_FILTER);
		ArrayList<String> elementFilter = _opts.getList(OPT_INCLUDE_FILTER);
		
		String format = _opts.getStr(OPT_OUTPUT_FORMAT);
		
		String scope = _opts.getStr(OPT_FILTER_SCOPE);
		
		String noDatesStr = _opts.getStr(OPT_NO_DATES);
		boolean noDates = new Boolean(noDatesStr);
		
		MatchMode mode = null;
		if (scope.equals(SCOPE_LEVEL_WITH_CHILDREN)) {
			mode = MatchMode.BEGINS_WITH;
		} else if (scope.equals(SCOPE_SAME_LEVEL)) {
			mode = MatchMode.EQUALS;
		}
		
		FilteringXmlTransformer analyzer = null;
		if (elementFilter != null) {
			if (format.equals(FORMAT_CSV)) {
				analyzer = new CSVXMLTransformer(elementFilter, mode, out);
			} else if (format.equals(FORMAT_ARCHIVE)) {
				analyzer = new ArchiveXMLTransformer(elementFilter, mode, out);
			}
		} else {
			if (format.equals(FORMAT_CSV)) {
				analyzer = new CSVXMLTransformer(startLevel, stopLevel, out);
			} else if (format.equals(FORMAT_ARCHIVE)) {
				analyzer = new ArchiveXMLTransformer(startLevel, stopLevel, out);
			}
		}
		
		if (noDates) {
			analyzer.setProcessDates(false);
		}
		//AbstractXMLTransformer analyzer = new AbstractXMLTransformer(level);

		List<String> newAttrs = _opts.getList(OPT_ADDITIONAL_DATE);
		if (newAttrs != null && newAttrs.size() > 0) {
			analyzer.addDateAttribute(newAttrs.toArray(new String[1]));
		}
		XMLProcessor processor = new XMLProcessor(_opts.getStr(OPT_XML_FILE), analyzer);
		processor.parse();
	}

	public static void init(String[] args) {
		_opts = new GetOpts(XMLAnalyzerApp.class);

		OptionLegend legend = new OptionLegend(OPT_XML_FILE);
		legend.setRequired(true);
		legend.setDescription("XML File of an IdentityIQ object to parse");
		_opts.addLegend(legend);

		legend = new OptionLegend(OPT_TARGET_LEVEL);
		legend.setRequired(false);
		legend.setDescription("Integer representing the XML element level to report on.  I.e. '2' would report on all Elements nested at the second level, or just below the root element");
		legend.setDefaultValue("2");
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_STOP_LEVEL);
		legend.setRequired(false);
		legend.setDescription("Integer representing the XML element level top stop reporting on.  I.e. '4' would report on all Elements nested up to the fourht level");
		legend.setDefaultValue("2");
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_INCLUDE_FILTER);
		legend.setRequired(false);
		legend.setDescription("Breadcrumb filter for elements to include.  Specifying a filter will exclude filtering based on element level");
		legend.setMulti(true);
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_ADDITIONAL_DATE);
		legend.setRequired(false);
		legend.setDescription("Denotes a date based attribute not part of the built-in set");
		legend.setMulti(true);
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_OUTPUT_FORMAT);
		legend.setRequired(false);
		legend.setDescription("Indicates format of output");
		String[] formats = {FORMAT_CSV, FORMAT_ARCHIVE};
		legend.setAllowedValues(formats);
		legend.setDefaultValue(FORMAT_CSV);
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_FILTER_SCOPE);
		legend.setRequired(false);
		legend.setDescription("Scope of search");
		String[] scopes = {SCOPE_SAME_LEVEL, SCOPE_LEVEL_WITH_CHILDREN};
		legend.setAllowedValues(scopes);
		legend.setDefaultValue(SCOPE_LEVEL_WITH_CHILDREN);
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_NO_DATES);
		legend.setFlag(true);
		legend.setDescription("When set, skips converting date values");
		_opts.addLegend(legend);

		_opts.parseOpts(args);
	}

}
