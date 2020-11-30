package biliruben.apps.xml;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import biliruben.tools.xml.XMLProcessor;
import biliruben.tools.xml.tcx.SplitTcxHandler;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

/**
 * Splits an incoming TCX file into files that are more managable for
 * importing in connect.garmin.com
 * @author trey.kirk
 *
 */
public class SplitTcx {
    
    
    
    private static final String OPT_XML_FILE = "tcxFile";
    private static final String OPT_OUT_DIR = "outDir";
    private static final String OPT_ACTIVITY_LIMIT = "limit";
    private static final String OPT_BASE_OUTPUT_NAME = "baseName";

    private static GetOpts _opts;
    private static String _inputFileName;
    private static String _outputDirectoryPath;
    private static String _baseFileName;
    private static int _activityLimit;
    
    public static void main(String[] args) throws IOException, SAXException {
        // init
        init(args);
        
        // setup parser
        SplitTcxHandler handler = new SplitTcxHandler(_baseFileName, _outputDirectoryPath, _activityLimit);
        XMLProcessor proc = new XMLProcessor(_inputFileName, handler);
        
        // PARSE!
        proc.parse();
        
        // summary?
        for (String fileName : handler.getFilesWritten()) {
            System.out.println(fileName);
        }
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(SplitTcx.class);
        
        // args:
        // input XML (required)
        OptionLegend legend = new OptionLegend(OPT_XML_FILE);
        legend.setRequired(true);
        legend.setDescription("TCX format file to parse");
        _opts.addLegend(legend);
        
        // output directory (optional, $PWD inferred)
        legend = new OptionLegend(OPT_OUT_DIR);
        legend.setRequired(false);
        legend.setDescription("Directory to output resulting TCX files");
        legend.setDefaultValue(new File(".").getAbsolutePath());
        _opts.addLegend(legend);
        
        // base file name (optional)
        legend = new OptionLegend(OPT_BASE_OUTPUT_NAME);
        legend.setDescription("The base filename used for output TCX files.  Each file will be enumerated to maintain uniqueness.");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        // number of activiites per file (optional, provide default value)
        // 70 / file seems nice
        legend = new OptionLegend(OPT_ACTIVITY_LIMIT);
        legend.setDescription("Maximum number of Activities to put into each output file");
        legend.setRequired(false);
        legend.setDefaultValue("70");
        _opts.addLegend(legend);
        
        // parse options
        _opts.parseOpts(args);
        _inputFileName = _opts.getStr(OPT_XML_FILE);
        _outputDirectoryPath = _opts.getStr(OPT_OUT_DIR);
        _baseFileName = _opts.getStr(OPT_BASE_OUTPUT_NAME);
        deriveBaseFileName();
        _activityLimit = Integer.valueOf(_opts.getStr(OPT_ACTIVITY_LIMIT));
    }
    
    private static void deriveBaseFileName() {
        if (_baseFileName == null) {
            File f = new File(_inputFileName);
            CharSequence baseName = f.getName().subSequence(0, f.getName().indexOf("."));
            _baseFileName = baseName.toString();
        }
    }

}
