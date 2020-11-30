package biliruben.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBrickLinkScanner extends BrickLinkScanner {

    private String _jsonFile;

    public FileBrickLinkScanner(String jsonFile, String partsListFile) throws IOException {
        this(jsonFile, partsListFile, "part number", "quantity");
    }
    
    public FileBrickLinkScanner(String jsonFile, String partsListFile,
            String partNumberFieldName, String qtyFieldName) throws IOException {
        super(partsListFile, partNumberFieldName, qtyFieldName);
        _jsonFile = jsonFile;
    }
    
    @Override
    public Map<String, List<Map>> getAllPartsData() throws IOException {
        File jsonFile = new File(_jsonFile);
        FileReader reader = new FileReader(jsonFile);
        BufferedReader buffReader = new BufferedReader(reader);
        String jsonLine = buffReader.readLine();
        Map<String, List<Map>> partsData = new HashMap<String, List<Map>>();
        while (jsonLine != null) {
            String[] tokens = jsonLine.split(",", 2);
            String partNumber = tokens[0];
            String partJson = tokens[1];
            System.out.println("Parsing part: " + partNumber);
            partsData.put(partNumber, parseJson(partJson));
            jsonLine = buffReader.readLine();
        }
        return partsData;
    }

}
