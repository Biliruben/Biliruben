package biliruben.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class IIQRESTCommands {
    
    
    static String BASE_IIQ_URL = "http://localhost:8080/iiq2";
    static String REST_URL = BASE_IIQ_URL + "/rest";
    //static String LIST_CERT_URL = REST_URL + "/certificationGroups";
    static String LIST_CERT_URL = REST_URL + "/certifications/search";
    
    static String USER = "james.smith";
    static String PASSWORD = "xyzzy";
    static String AUTH_STRING = "amFtZXMuc21pdGg6eHl6enk="; // 'james.smith:xyzzy'
    
    static String DATA_STRING = "certGroupId=2c90904c67657e1c0167658243b8000a&colKey=certificationAccessReviewsTableColumns&limit=25&page=1&start=0";



    public static void main(String[] args) throws Throwable {
        
        
        URL url;
        HttpURLConnection urlConn;
        DataOutputStream printout;
        String urlSrc = LIST_CERT_URL;
        url = new URL(urlSrc);
        urlConn = (HttpURLConnection)url.openConnection();
        urlConn.setDoInput(true);
        urlConn.setDoOutput(true);
        urlConn.setUseCaches(false);
        urlConn.setRequestProperty("User-Agent", "javaBullShit");
        urlConn.setRequestProperty("Accept", "*/*");
        urlConn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        /*
        urlConn.setRequestProperty("Content-Length", "11");
        urlConn.setRequestProperty("Connection", "keep-alive");
        urlConn.setRequestProperty("Keep-Alive", "115");
        */
        
        urlConn.setRequestMethod("POST");
        
        
        
        
        
        
            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.writeBytes(DATA_STRING);
            dos.flush();
            dos.close();

        urlConn.connect();
        BufferedInputStream bis = new BufferedInputStream(new DataInputStream(urlConn.getInputStream()));
        BufferedReader input = new BufferedReader(new InputStreamReader(bis));
        String str = null;
        while (null != ((str = input.readLine()))) {
            System.out.write(str.getBytes());
        }
        fetchResponseHeaders(urlConn);
        input.close();
        System.out.flush();
 
        
        
        
        
        /*
        int code = urlConn.getResponseCode();
        System.out.println("code: " + code);
        printout = new DataOutputStream(urlConn.getOutputStream());
        String content="rId=" + URLEncoder.encode("5280019", "UTF-8");
        printout.writeBytes(content);
        printout.flush();
        printout.close();
        input = new DataInputStream(urlConn.getInputStream());
        String str;
        while (null != ((str = input.readLine()))) {
            System.out.println(str);
        }
        input.close();
        */

        
        
        
        

    }
    
    static private Map<String, String> fetchResponseHeaders(HttpURLConnection urlConn) {

        HashMap<String, String> responseHeaders = new HashMap<String, String>();
        // List all the response headers from the server.
        // Note: The first call to getHeaderFieldKey() will implicit send
        // the HTTP request to the server.
        for (int i=0; ; i++) {
            String headerName = urlConn.getHeaderFieldKey(i);
            String headerValue = urlConn.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if (headerName == null) {
                // The header value contains the server's HTTP version
                headerName = "version";
            }
            responseHeaders.put(headerName, headerValue);
        }
        
        return responseHeaders;
    }


}
