package biliruben.json;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import biliruben.files.DosFileNameFilter;

public class FindQueryText {

    /**
     * @param args
     * @throws IOException 
     * @throws JsonParseException 
     */
    public static void main(String[] args) throws JsonParseException, IOException {
        // gimmie a directory
        String directoryPath = args[0];
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new RuntimeException ("Fuck you, I need a directory");
        }
        FilenameFilter filter = new DosFileNameFilter("*.json");
        String[] files = directory.list(filter);
        for (String file : files) {
            findElement(directory.getAbsolutePath() + File.separator + file, "query_text", "porn");
        }

    }
    
    
    public static void findElement(String jsonFile, String element, String endsWith) throws JsonParseException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nodeTree = mapper.readTree(new File(jsonFile));
        List<JsonNode> values = nodeTree.findValues(element);
        for (JsonNode valueNode : values) {
            String valueText = valueNode.asText();
            if (valueText.endsWith(endsWith)) {
                System.out.println(valueNode.asText());
            }
        }
    }

}
