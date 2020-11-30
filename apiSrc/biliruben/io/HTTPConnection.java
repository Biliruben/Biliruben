package biliruben.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HTTPConnection {

    public static final int DEFAULT_TIMEOUT = 60; // 60 seconds

    private Map<String, String> _properties;
    private URL _url;
    private OutputStream _out;
    private URLConnection _urlConn;
    private Set<String> _contentTokens;
    private int _timeout; //in seconds
    private Map<String, String> _responseHeaders;
    private boolean _doInput;
    private boolean _doOutput;

    public HTTPConnection(URL url) {
        this(url, null);
    }

    public HTTPConnection(URL url, OutputStream output) {
        setUrl(url);
        setOutputStream(output);
        _contentTokens = new HashSet<String>();
        _timeout = DEFAULT_TIMEOUT;
        _responseHeaders = new HashMap<String, String>();
        _doInput = true;
        _doOutput = true;
    }

    public void setTimeout(int seconds) { 
        _timeout = seconds;
    }

    public int getTimeout() {
        return _timeout;
    }

    public void setDoInput(boolean doInput) {
        _doInput = doInput;
    }

    public void setDoOutput(boolean doOutput) {
        _doOutput = doOutput;
    }

    public void setUrl(URL url) {
        _url = url;
    }

    public URL getUrl() {
        return _url;
    }

    public void setOutputStream (OutputStream output) {
        _out = output;
    }

    public OutputStream getOutputStream() {
        return _out;
    }

    public void setRequestProperties(Map<String, String> properties) {
        _properties = properties;
    }

    public void mergeRequestProperties(Map<String, String> properties) {
        getRequestProperties().entrySet().addAll(properties.entrySet());
    }

    public Map<String, String> getRequestProperties() {
        if (_properties == null) {
            _properties = new HashMap<String, String>();
            _properties.put("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
        }
        return _properties;
    }

    public void setRequestProperty(String property, String value) {
        getRequestProperties().put(property, value);
    }

    public void removeRequestProperti(String property) {
        getRequestProperties().remove(property);
    }

    public Set<String> getContentTokens() {
        return _contentTokens;
    }

    public void setContentToken(String token) {
        _contentTokens.add(token);
    }

    public void setContentTokens(Set<String> contentTokens) {
        _contentTokens = contentTokens;
    }

    public void connect() throws IOException {
        if (_out == null) {
            throw new IOException("OutputStream has not beend defined yet!");
        }
        if (_doOutput) {
            String token = getTokensString();
            setRequestProperty("Content-length", String.valueOf(token.length()));
        }
        _urlConn = _url.openConnection();
        _urlConn.setConnectTimeout(_timeout * 1000);
        _urlConn.setDoInput(_doInput);
        _urlConn.setDoOutput(_doOutput);
        applyProperties();
        if (_doOutput) {
            DataOutputStream dos = new DataOutputStream(_urlConn.getOutputStream());
            writeTokens(dos);
            dos.flush();
            dos.close();
        }
        _urlConn.connect();
        BufferedInputStream bis = new BufferedInputStream(new DataInputStream(_urlConn.getInputStream()));
        BufferedReader input = new BufferedReader(new InputStreamReader(bis));
        String str = null;
        while (null != ((str = input.readLine()))) {
            _out.write(str.getBytes());
        }
        fetchResponseHeaders();
        input.close();
        _out.flush();
    }

    private void fetchResponseHeaders() {

        // List all the response headers from the server.
        // Note: The first call to getHeaderFieldKey() will implicit send
        // the HTTP request to the server.
        for (int i=0; ; i++) {
            String headerName = _urlConn.getHeaderFieldKey(i);
            String headerValue = _urlConn.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if (headerName == null) {
                // The header value contains the server's HTTP version
                headerName = "version";
            }
            _responseHeaders.put(headerName, headerValue);
        }
    }

    private void writeTokens(DataOutputStream output) throws IOException {
        if (_contentTokens.size() > 0) {
            output.writeBytes(getTokensString());
        }
    }

    public Map<String, String> getResponseHeaders() {
        return _responseHeaders;
    }

    private String getTokensString() {
        StringBuffer contentBuffer = new StringBuffer();
        for (String token : _contentTokens) {
            if (contentBuffer.length() > 0) {
                // add content delim
                contentBuffer.append("&");
            }
            contentBuffer.append(token);
        }
        return contentBuffer.toString();
    }

    private void applyProperties() {
        for (Entry<String, String> entry :getRequestProperties().entrySet()) {
            _urlConn.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }
}
