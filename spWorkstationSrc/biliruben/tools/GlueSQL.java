package biliruben.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given trace data from SP's logging, 'glue' the parameter values to the '?' placeholders for a given prepared stmt
 * @author trey.kirk
 *
 */
public class GlueSQL {
    
    String SELECT_PATTERN = "select";
    String PARAM_PATTERN = "(.*?)\\?(.*)";

    public static void main(String[] args) throws IOException {
        // First line is the query, ala: select * from stuff where name like ? and ted <> ?
        // All following lines are 'bindings'
        // ALL lines are log data, so carries with it noise at the front
        String THE_FILE = "c:/temp/glueit.txt";
        
        // Read the file
        File f = new File (THE_FILE);
        FileReader fr = new FileReader(f);
        BufferedReader r = new BufferedReader(fr);
        // Nab the first line, pull out the query
        String queryLine = r.readLine();
        // it's messy:
        // ala 2019-06-02 16:16:24,558 DEBUG http-bio-8080-exec-8 org.hibernate.SQL:111 - select distinct count
        // cheat: find 'select' and get the substring
        int beginIndex = queryLine.indexOf("select");
        if (beginIndex < 0) {
            throw new IOException("Can't find SQL in " + THE_FILE);
        }
        // nothing trailing, so just that is fine
        String query = queryLine.substring(beginIndex);
        
        // Idea 1: for each new line, pull out the binding value
        // regex the first ? into that value
        // vomit on the first unmatched ? (too few ? for binding values);
        StringBuilder buff = new StringBuilder();
        boolean found = true;
        Pattern questionablePattern = Pattern.compile("(.*?)\\?(.*)");
        String bindingLine = r.readLine();
        while (found && bindingLine != null) {
            int firstQuoteIndex = bindingLine.indexOf('\'');
            int lastQuoteIndex = bindingLine.indexOf('\'', firstQuoteIndex + 1);
            String bindValue = bindingLine.substring(firstQuoteIndex, lastQuoteIndex+1);
            Matcher m = questionablePattern.matcher(query);
            if (m.matches()) {
                buff.append(m.group(1));
                buff.append(bindValue);
                buff.append(m.group(2));
                query = buff.toString();
                buff = new StringBuilder();
            }
            bindingLine = r.readLine();
        }
        
        r.close();
        System.out.println(query);
        
        // For each ? in the query, replace it with the next line's binding value
        // how do I replace each ? in succession but one at a time?
            // idea 1: perform a regex search/replace matching only the first ? (aka not global)
                // super easy
                // regex can be slow. w/ 500 matches, probably not slow enough to matter
            // idea 2: use String#indexOf() to find the first ?; build a StringBuilder that contains the previous segment, then the biding
                // complicated. Gotta keep up with a cursor along the string to identify the segments
                // no performance concerns
            // try idea 1 first
        
            // [looping] nab the next line; extract the binded value; don't forget the quotes
        // [end loop]
        
        // report final query

    }

}
