package sailpoint.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.OptionParseException;

/**
 * Iterates through a text file and purges duplicate lines only when they immediately following
 * a matching line. For example:
 * 
 * This is line 1
 * This is line 1 <-- will be purged
 * This is line 2
 * This is line 2 <-- will be purged
 * This is line 3
 * This is line 4
 * This is line 3 <-- will NOT be purged
 * 
 * @author trey.kirk
 *
 */
public class PruneDupes {

    private static final String OPT_OUTPUT_FILE = "outFile";
    private static GetOpts _opts;
    private static String _inFile;
    private static String _outFile;

    public static void main(String[] args) throws IOException {
        init(args);
        
        BufferedReader reader = null;
        if (_inFile != null) {
            File f = new File(_inFile);
            reader = new BufferedReader(new FileReader(f));
        } else {
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
        
        Writer writer = null;
        if (_outFile != null) {
            writer = new FileWriter(new File(_outFile));
        } else {
            writer = new OutputStreamWriter(System.out);
        }

        parseDupes(reader, writer);
    }
    
    private static void parseDupes(BufferedReader reader, Writer writer) throws IOException {
        String line = null;
        String lastLine = null;
        do {
            line = reader.readLine();
            if (line != null) {
                if (!line.equals(lastLine)) {
                    // no match; not a dupe
                    writer.write(line);
                    writer.write("\n");
                    lastLine = line;
                } // else it's an immediate match and therefore a duplicate; ignore it
            }
        } while (line != null && reader.ready());
        writer.flush();
        writer.close();
        reader.close();
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(PruneDupes.class);
        
        OptionLegend legend = new OptionLegend(OPT_OUTPUT_FILE);
        legend.setRequired(false);
        legend.setDescription("Output file. If none is specified, output will go to STDOUT");
        _opts.addLegend(legend);
        
        _opts.setUsageTail("[file]");
        _opts.setDescriptionTail("file:\tName of file to parse duplicated lines from. If none is specified, STDIN will be used for input");
        _opts.parseOpts(args);
        
        ArrayList<String> unswitched = _opts.getUnswitchedOptions();
        if (unswitched != null) {
            if (unswitched.size() == 1) {
                _inFile = unswitched.get(0);
            } else if (unswitched.size() > 0) {
                throw new OptionParseException("Too many parameters passed: " + unswitched, _opts);
            }
        }
        
        _outFile = _opts.getStr(OPT_OUTPUT_FILE);
    }

}
