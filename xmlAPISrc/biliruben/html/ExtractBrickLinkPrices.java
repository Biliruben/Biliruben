package biliruben.html;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import biliruben.files.FileHandler;
import biliruben.files.FileScanner;
import biliruben.files.Scanner;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionParseException;

public class ExtractBrickLinkPrices {

    private static GetOpts _opts;
    private static List<String> _fileMasks;

    /**
     * @param args
     */
    public static void main(String[] args) {
        //0. setup - require a directory to read and a file mask
        init(args);
        
        //1. iterate over list of files (part number is in the file name)
        ExtractBrickListPriceHandler handler = new ExtractBrickListPriceHandler();
        Scanner scanner = new FileScanner(false);
        scanner.addHandler(handler);
        for (String mask : _fileMasks) {
            scanner.scan(new File(mask));
        }
        
        Map<String, Double> prices = handler.getPrices();
        DecimalFormat df = new DecimalFormat("####0.00");
        for (String part : prices.keySet()) {
            System.out.println(part + "," + df.format(prices.get(part)));
        }
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(ExtractBrickLinkPrices.class);
        
        _opts.setUsageTail("fileMask");
        _opts.setDescriptionTail("\tfileMask: Files to read in");
        _opts.parseOpts(args);
        _fileMasks = _opts.getUnswitchedOptions();
        if (_fileMasks == null || _fileMasks.isEmpty()) {
            throw new OptionParseException("fileMask is required", _opts, true);
        }
    }

}
