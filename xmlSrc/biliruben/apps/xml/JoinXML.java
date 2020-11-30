package biliruben.apps.xml;

import java.util.List;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.OptionParseException;

public class JoinXML {

	private static final String DEFAULT_ROOT = "root";
	private static final String OPT_ROOT = "root";
	private static final String OPT_OUT = "out";
	private static final String OPT_FILTER = "filter";
	private static final String OPT_FILE = "file";
	private static GetOpts _opts;
	private static List<String> _files;
	private static String _filter;
	private static String _out;
	private static String _rootElement;

	/**
	 * Given a list of xml filenames or file filter, join the XML content into a single XML root and 
	 * output file.
	 * @param args
	 */
	public static void main(String[] args) {
		init(args);
		
		

	}
	
	private static void getFileList() {
		if (_filter != null) {
			
		}
	}

	private static void init(String[] args) {
		_opts = new GetOpts(JoinXML.class);
		
		OptionLegend legend = new OptionLegend(OPT_FILTER);
		legend.setRequired(false);
		legend.setDescription("File filter of XML files to join.  When specified with " + OPT_FILE + ", this option takes precedence while " + OPT_FILE + " is ignored.");
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_FILE);
		legend.setRequired(false);
		legend.setMulti(true);
		legend.setDescription("Specific XML files to join.  When specified with " + OPT_FILTER + ", this option is ignored.");
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_OUT);
		legend.setRequired(true);
		legend.setDescription("Output XML file");
		_opts.addLegend(legend);
		
		legend = new OptionLegend(OPT_ROOT);
		legend.setRequired(false);
		legend.setDefaultValue(DEFAULT_ROOT);
		legend.setDescription("Root element value");
		_opts.addLegend(legend);
		
		// Future: add name validation parameters (i.e. use child DTD, public/system names?
		
		_opts.parseOpts(args);

		String filter = _opts.getStr(OPT_FILTER);
		List<String> files = _opts.getList(OPT_FILE);
		if (filter == null && files == null) {
			// BZZT!
			throw new OptionParseException("Either " + OPT_FILTER + " or " + OPT_FILE + " must be specified!", _opts, true);
		}
		
		if (filter != null) { // filter is higher precedence than files
			_filter = filter;
		} else {
			_files = files;
		}
		
		_out = _opts.getStr(OPT_OUT); // output file
		
		_rootElement = _opts.getStr(OPT_ROOT);
	}

}
