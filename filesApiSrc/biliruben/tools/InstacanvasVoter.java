package biliruben.tools;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import biliruben.io.HTTPConnection;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class InstacanvasVoter {

    /**
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
        //http://instacanv.as/omgitspeeb/piece/294917652767145869_10312910?omgitspeeb/piece/294917652767145869_10312910
        URL url = new URL("http://instacanv.as/omgitspeeb/piece/294917652767145869_10312910");
        ByteOutputStream bos = new ByteOutputStream();
        HTTPConnection conn = new HTTPConnection(url, bos);
        conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
        conn.setRequestProperty("Accept-Language", "en-US");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; MALC)");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Host", "instacanv.as");
        conn.setContentToken("omgitspeeb/piece/294917652767145869_10312910");
        conn.setDoOutput(false); // get, no post
        conn.connect();
        Map<String, String> headers = conn.getResponseHeaders();
        System.out.println(headers);

        // output will embed the CSRF token: <meta content="r3MiqJZBHcUk7ZzDZGkSKgp8VGDseSD2CwAEFuJ5rPc=" name="csrf-token"
        // vote
        String rawCookieValue = headers.get("Set-Cookie");
        url = new URL("http://instacanv.as/contest-submissions/10453/vote");
        bos = new ByteOutputStream();
        conn = new HTTPConnection(url, bos);
        conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        conn.setRequestProperty("Accept-Language", "en-US");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; MALC)");
        conn.setRequestProperty("Host", "instacanv.as");
        conn.setRequestProperty("Referer", "http://instacanv.as/omgitspeeb/piece/294917652767145869_10312910#omgitspeeb/piece/294917652767145869_10312910");
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cookie", rawCookieValue);
        conn.setDoOutput(true);
        conn.connect();
        System.out.println(bos.toString());
        
        // rest
        Thread.sleep(250);
        }

    }
    
    

}
