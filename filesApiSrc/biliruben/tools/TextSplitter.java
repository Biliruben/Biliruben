package biliruben.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class TextSplitter {

    private static final String OPT_FILE = "file";
    private static final String OPT_LINES = "lines";
    private static GetOpts opts;
    private static String _sourceFile;
    private static Integer _lines;


    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        init(args);
        _sourceFile = opts.getStr(OPT_FILE);
        _lines = Integer.valueOf(opts.getStr(OPT_LINES));
        splitFile();
    }
    
    private static void splitFile() throws IOException {
        int counter = 1;
        File file = new File(_sourceFile);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (br.ready() && line != null) {
            int lineCounter = 0;
            File outFile = new File(_sourceFile + "." + counter);
            FileWriter fw = new FileWriter(outFile);
            while (lineCounter < _lines && line != null) {
                fw.write(line + "\n");
                line = br.readLine();
                lineCounter++;
            }
            fw.flush();
            fw.close();
            counter++;
        }
    }
    
    
    private static void init(String[] args) {
        opts = new GetOpts(TextSplitter.class);
        OptionLegend legend = new OptionLegend(OPT_FILE);
        legend.setRequired(true);
        legend.setDescription("Text file to split up");
        opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_LINES);
        legend.setDefaultValue("10000");
        legend.setRequired(false);
        legend.setDescription("Number of lines each new file should be");
        opts.addLegend(legend);
        
        opts.parseOpts(args);
        
    }

}
