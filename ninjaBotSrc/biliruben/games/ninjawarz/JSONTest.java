package biliruben.games.ninjawarz;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import biliruben.games.ninjawarz.object.*;

import org.codehaus.jackson.map.ObjectMapper;

public class JSONTest {

    /**
     * @param args
     * @throws IOException 
     * @throws JSONException 
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub

        File jsonTest = new File("c:/temp/json.test4");
        FileReader reader = new FileReader(jsonTest);
        StringBuffer buff = new StringBuffer();
        char c = (char) reader.read();
        while (c != -1 && reader.ready()) {
            buff.append(c);
            c = (char) reader.read();
        }
        String jsonString = buff.toString();
        ObjectMapper mapper = new ObjectMapper();
        //Map<String, Object> mapped = mapper.readValue(jsonTest, Map.class);
        Battle mapped = mapper.readValue(jsonTest, Battle.class);
        System.out.println(mapped);
    }

}
