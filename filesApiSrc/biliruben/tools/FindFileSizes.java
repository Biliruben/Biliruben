package biliruben.tools;

import java.io.File;
import java.util.ArrayList;

import biliruben.files.FileScanner;
import biliruben.files.FileSizeHandler;
import biliruben.threads.ThreadRunner;
import biliruben.util.Util;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class FindFileSizes {

    private static final String OPT_RECURSE = "recurse";
    private static final String OPT_MIN_SIZE = "minSize";
    private static final String RUNNER_PROPS_GROUP = "runner";
    private static GetOpts _opts;

    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        init(args);
        
        String startingPath = ".";
        String recurseStr = _opts.getStr(OPT_RECURSE);
        boolean recurse = Boolean.valueOf(recurseStr);
        String optsMinSize = _opts.getStr(OPT_MIN_SIZE);
        long minSize = 0L;
        if (!Util.isEmpty(optsMinSize)) {
            minSize = Long.valueOf(optsMinSize);
        }
        
        ArrayList<String> files = _opts.getUnswitchedOptions();
        if (files == null) {
            files = new ArrayList<String>();
        }
        
        if (files.isEmpty()) {
            files.add(startingPath);
        }
        
        String propertyFile = _opts.getStr(OptionLegend.OPT_PROPERTY_FILE);
        ThreadRunner runner = null;
        if (propertyFile != null) {
            runner = new ThreadRunner(propertyFile, RUNNER_PROPS_GROUP);
        }
        FileScanner scanner = new FileScanner(recurse, runner);
        FileSizeHandler handler = new FileSizeHandler(false);
        handler.setMinSize(minSize);
        scanner.addHandler(handler);
        for (String filePath : files) {
            scanner.scan(new File(filePath));
        }
        
        if (runner != null && runner.hasWork()) {
            String scanningMsg = "Scanning ";
            System.out.print(scanningMsg);
            int incr = 80;
            int count = scanningMsg.length();
            while (runner.hasWork()) {
                //sleep for 250ms and try again
                System.out.print(".");
                count++;
                if (count % incr == 0) {
                    System.out.print("\n");
                }
                Thread.sleep(250);
            }
            runner.shutDown();
        }
        System.out.println("\nCaculating file sizes...");

        System.out.println(handler.getFileSizes());
        
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(FindFileSizes.class);
        OptionLegend legend = new OptionLegend(OPT_RECURSE);
        legend.setFlag(true);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_MIN_SIZE);
        legend.setDescription("Minimum size to report");
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        legend = _opts.getLegend(OptionLegend.OPT_PROPERTY_FILE);
        legend.setDescription("Thread runner properties configuration file. Use property group '" + RUNNER_PROPS_GROUP + "'");
        
        legend = _opts.getLegend("propertyGroup");
        legend.setDescription("Unused");
        
        _opts.setUsageTail("One or more files or directories to scan. Dos file patterns may be used");
        _opts.parseOpts(args);
    }

}
