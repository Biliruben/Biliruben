package com.biliruben.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.biliruben.util.csv.CSVRecord;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVCoallator;
import com.biliruben.util.csv.CSVSourceImpl;
import com.biliruben.util.csv.CSVUtil;

public class CreateManagerHierarchy {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        File f = new File("C:\\etn_data\\1223 - was 21208 - Nonthread safe bullshit access my homie\\5kIdent.csv");
        CSVSourceImpl src = new CSVSourceImpl(f, CSVType.WithHeader);
        CSVCoallator collator = new CSVCoallator(src, "acctId", "manager", "OU");
        Map<String, String> collationMap = collator.getHierarchyMap();
        for (String key : collationMap.keySet()) {
            String ou = collationMap.get(key);
            System.out.println(key + ":\t" + ou);
        }
        
        CSVRecord r = new CSVRecord();
        for (String key : collationMap.keySet()) {
            Map<String, String> line = new HashMap<String, String>();
            line.put("acctId", key);
            line.put("OU", collationMap.get(key));
            r.addLine(line);
        }
        File outFile = new File("C:\\etn_data\\1223 - was 21208 - Nonthread safe bullshit access my homie\\userOUs.csv");
        CSVUtil.exportToCsv(r, new FileOutputStream(outFile));
        
        

    }

}
