package biliruben.html;

import java.io.File;

import biliruben.files.FileScanner;
import biliruben.files.Scanner;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class ExtractFidelityInvestments {

    private static GetOpts _opts;
    private static String _file;

    /**
     * @param args
     */
    public static void main(String[] args) {
        init(args);
        ExtractFidelityInvestmentsHandler handler = new ExtractFidelityInvestmentsHandler();
        Scanner scanner = new FileScanner(false);
        scanner.addHandler(handler);
        scanner.scan(new File(_file));

    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(ExtractFidelityInvestments.class);
        OptionLegend legend = new OptionLegend("file");
        legend.setRequired(true);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
        
        _file = _opts.getStr("file");
    }

}
