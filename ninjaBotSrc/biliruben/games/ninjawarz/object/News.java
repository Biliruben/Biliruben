package biliruben.games.ninjawarz.object;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class News extends JSONObject {

    private String occurred_at;
    private String text;
    private String icon;
    public String getOccurred_at() {
        return occurred_at;
    }
    public void setOccurred_at(String occurred_at) {
        this.occurred_at = occurred_at;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getClearText() {
        String newsXml = text;
        StringBuffer buffer = new StringBuffer();
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
        return buffer.toString();
    }
    

}
