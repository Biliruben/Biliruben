package biliruben.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biliruben.files.FileScanner;
import biliruben.files.Scanner;
import biliruben.tools.CSVPrint;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionParseException;
import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVUtil;

public class ExtractBrickLinkDescriptions {

    private static GetOpts _opts;
    private static List<String> _fileMasks;

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        //0. setup - require a directory to read and a file mask
        init(args);
        
        //1. iterate over list of files (part number is in the file name)
        ExtractBrickListDescriptionHandler handler = new ExtractBrickListDescriptionHandler();
        Scanner scanner = new FileScanner(false);
        scanner.addHandler(handler);
        for (String mask : _fileMasks) {
            scanner.scan(new File(mask));
        }

        CSVRecord record = new CSVRecord();
        
        Map<String, String> descriptions = handler.getDescriptions();
        String partNumber = "partNumber";
        String description = "description";
        for (String key : descriptions.keySet()) {
            Map<String, String> line = new HashMap<String, String>();
            line.put(partNumber, key);
            line.put(description, descriptions.get(key));
            record.addLine(line);
        }
        FileOutputStream out = new FileOutputStream("c:\\temp\\partDescriptions.txt");
        CSVUtil.exportToCsv(record, out);
        out.flush();
        out.close();
        
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(ExtractBrickLinkDescriptions.class);
        
        _opts.setUsageTail("fileMask");
        _opts.setDescriptionTail("\tfileMask: Files to read in");
        _opts.parseOpts(args);
        _fileMasks = _opts.getUnswitchedOptions();
        if (_fileMasks == null || _fileMasks.isEmpty()) {
            throw new OptionParseException("fileMask is required", _opts, true);
        }
    }

}
