package biliruben.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class HTTPGet {
	private static final String URL_GMAPS_PEDO = "http://www.gmap-pedometer.com/gp/ajaxRoute/get";
	/**
	 * Start simple: Given a single argument, open an HTML connection and fetch the resource.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		postUrl2(args);
	}
	
	private static void postUrl(String[] args) throws IOException {
		URL url;
		URLConnection urlConn;
		DataOutputStream printout;
		DataInputStream input;
		String urlSrc = args[0];
		url = new URL(urlSrc);
		urlConn = url.openConnection();
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		urlConn.setRequestProperty("Content-Length", "11");
		urlConn.setRequestProperty("Connection", "keep-alive");
		urlConn.setRequestProperty("Keep-Alive", "115");
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
		
	}
	
	private static void postUrl2(String[] args) throws IOException {
		URL url;
		URLConnection urlConn;
		DataOutputStream printout;
		String urlSrc = URL_GMAPS_PEDO;
		url = new URL(urlSrc);
		urlConn = url.openConnection();
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		urlConn.setRequestProperty("Content-Length", "11");
		urlConn.setRequestProperty("Connection", "keep-alive");
		urlConn.setRequestProperty("Keep-Alive", "115");
		printout = new DataOutputStream(urlConn.getOutputStream());
		String content="rId=" + URLEncoder.encode("5280019", "UTF-8");
		printout.writeBytes(content);
		printout.flush();
		printout.close();

		String str;
		StringBuffer buff = new StringBuffer();
		DataInputStream dis = new DataInputStream(urlConn.getInputStream());		
	     BufferedReader input = new BufferedReader(new InputStreamReader(dis));

		while (null != ((str = input.readLine()))) {
			System.out.println(str);
		}
		input.close();

	}
	
	private void getFile(String[] args) throws IOException {
		if (args == null || args.length < 2) {
			// TODO: replace with real args, getOpts
			System.out.println("Two arguments required: source_URL and target_file");
			System.exit(1);
		}
		
		String urlSrc = args[0];
		String outputSrc = args[1];
		URL getUrl = new URL(urlSrc);
		InputStream in = getUrl.openStream();
		FileOutputStream fis = new FileOutputStream(outputSrc);

		int c;
		while ((c = in.read()) > -1) {
			fis.write(c);
		}
		in.close();
		fis.close();
		
		
	}

}
