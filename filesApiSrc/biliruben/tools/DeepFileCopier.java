package biliruben.tools;

import java.io.File;
import java.io.IOException;

import biliruben.files.FileCopyHandler;
import biliruben.files.FileScanner;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;
import com.biliruben.util.OptionParseException;

public class DeepFileCopier {

    private static final String OPT_SRC_DIRECTORY = "src";
    private static final String OPT_SRC_FILE_PATTERN = "pattern";
    private static final String OPT_TARGET_DIRECTORY = "dest";
    private static GetOpts _opts;
    private static String _sourceDirectory;
    private static String _targetDirectory;

    /**
     * @param args
     */
    public static void main(String[] args) {
        init(args);

        try {
            FileCopyHandler handler = new FileCopyHandler(_sourceDirectory, _targetDirectory, true);
            File sourceFile = new File(_sourceDirectory + File.separator + _opts.getStr(OPT_SRC_FILE_PATTERN));
            FileScanner scanner = new FileScanner(true);
            scanner.addHandler(handler);
            scanner.scan(sourceFile);
            handler.shutdown();  // just tells the copier to kill its threads when its done with the work
        } catch (IOException e) {
            System.err.println("An error occured!\n" + e.getMessage());
        }

    }

    private static void init(String[] args) {
        _opts = new GetOpts(DeepFileCopier.class);

        OptionLegend legend = new OptionLegend(OPT_SRC_DIRECTORY);
        legend.setRequired(false);
        legend.setDefaultValue(System.getProperty("user.dir"));
        _opts.addLegend(legend);

        legend = new OptionLegend(OPT_SRC_FILE_PATTERN);
        legend.setRequired(false);
        legend.setDefaultValue("*");
        _opts.addLegend(legend);

        legend = new OptionLegend(OPT_TARGET_DIRECTORY);
        legend.setRequired(false);
        legend.setDefaultValue(System.getProperty("user.dir"));
        _opts.addLegend(legend);

        // So there are no actual required options, but we do have a requirement
        // for the source to not be the same as the target
        _opts.parseOpts(args);

        _sourceDirectory = _opts.getStr(OPT_SRC_DIRECTORY);
        _targetDirectory = _opts.getStr(OPT_TARGET_DIRECTORY);
        if (_sourceDirectory.equals(_targetDirectory)) {
            throw new OptionParseException("Source directory and target must be different!  Source: " + _sourceDirectory + ", Target: " + _targetDirectory, _opts, true);
        }
    }

}
