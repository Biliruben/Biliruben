package biliruben.apps.xml;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import biliruben.api.DataTokenBuilder;

public class BundleGenerator {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // v1.0 - flat bundles only
        // get a data token generator
        DataTokenBuilder builder = new DataTokenBuilder(new File("C:/scripts/data"), "[Place] // [Street] [Job]", false);
        //DataTokenBuilder builder = new DataTokenBuilder(new File("C:/scripts/data"), "[Title] [FirstName] [FirstName] [LastName], [Job]", false);
        
        // for n, create a bundle XML
        int total = 200;
        Iterator<String> it = builder.iterator();
        while (total > 0 && it.hasNext()) {
            String token = it.next();
            System.out.println(token);
            total--;
        }
        
    }

}
