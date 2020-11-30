package biliruben.games.ninjawarz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

public class NewsParser {
    
    public static void parseNews(String rawNews, OutputStream output) throws IOException {
        String news = parseNews(rawNews);
        output.write(news.getBytes());
        output.flush();
    }
    
    public static String parseNews(String rawNews) throws IOException {
        // Raw news is in the format of:
        // [{field1:field2:field3},{field1:field2:field3}...]
        //
        // field1 is static, "occured_at", ignore
        // field2 is a relative time stamp.  Easy to parse, report as our first field
        // field3 is escaped html, that's the real news
        
        StringBuffer newsBuffer = new StringBuffer();
        // Strip the surrounding [ ... ]
        if (rawNews == null || "".equals(rawNews.trim())) {
            return "(no news to parse)\n";
        }
        rawNews = rawNews.substring(1, rawNews.length() - 1);
        // each news line is {line1},{line2}...
        
        String[] nextLine = rawNews.split("\\},\\{");
        if (nextLine != null && nextLine.length > 0) {
            for (String newsLine : nextLine) {
                // each 'newsLine' is {field1:field2:field3}
                if (newsLine.startsWith("{")) {
                    newsLine = newsLine.substring(1);
                } else if (newsLine.endsWith("}")) {
                    newsLine = newsLine.substring(0, newsLine.length() - 1);
                }
                parseNewsFields(newsBuffer, newsLine);
            }
        }
        
        return newsBuffer.toString();
    }
    
    // we get the fields without the surrounding {...}, so now just parse
    // the fields from delimiting :
    private static void parseNewsFields(StringBuffer buffer, String newsFields) throws IOException {
        CSVSource delimitedFields = new CSVSourceImpl(newsFields, CSVType.WithOutHeader);
        delimitedFields.setDelim(':');
        String[] fields = delimitedFields.getNextLine(); 
        if (fields.length > 1) {
            String rawTimeStamp = fields[1];  // ex. "2 minutes ago","text", so parse that
            rawTimeStamp = "\"" + rawTimeStamp + "\""; // we stripped the surrounding " and we need those
            CSVSource timeStampCsv = new CSVSourceImpl(rawTimeStamp, CSVType.WithOutHeader);
            
            //_stdout.printf("%-20s", command.getName());
            //_stdout.println(command.getDescription());
            String formattedTimeStamp = String.format("%-20s", timeStampCsv.getNextLine()[0] + ":");
            buffer.append(formattedTimeStamp);
        }
        if (fields.length > 2) {
            // the complicated field: escaped html
            String rawNewsField = fields[2];
            rawNewsField = "\"" + rawNewsField +"\"";
            CSVSource rawNewsCsv = new CSVSourceImpl(rawNewsField, CSVType.WithOutHeader);
            String rawNewsXml = rawNewsCsv.getNextLine()[0];
            // we'll have quotes and / escaped, we want to de-escape the quotes
            rawNewsXml = rawNewsXml.replace("\\\"", "\"");

            try {
                parseNewsPart(rawNewsXml, buffer);
            } catch (SAXException e) {
                // convert it to an IOException
                throw new IOException(e);
            }
        }
        buffer.append("\n");
    }
    
    private static void parseNewsPart(String newsXml, StringBuffer buffer) throws SAXException, IOException {
        // bare text leading <
        Pattern p = Pattern.compile("^([^<]*)<");
        Matcher m = p.matcher(newsXml);
        if (m.matches()) {
            buffer.append(m.group(1));
        }
        
        // cdata text
        p = Pattern.compile(">([^<]*)<");
        m = p.matcher(newsXml);
        while (m.find()) {
            buffer.append(m.group(1));
        }
        
        // and any trailing >[^<]*$
        p = Pattern.compile(">([^<]*)$");
        m = p.matcher(newsXml);
        if (m.find()) {
            buffer.append(m.group(1));
        }
        
        // and what if the entire message has no feaux xml
        p = Pattern.compile("^.*<.*>.*$");
        m = p.matcher(newsXml);
        if (!m.matches()) {
            // no feaux xml, slide in the whole message
            buffer.append(newsXml);
        }

    }
}
