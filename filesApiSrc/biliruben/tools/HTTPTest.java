package biliruben.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;

import biliruben.games.ninjawarz.api.NewsParser;
import biliruben.io.HTTPConnection;

public class HTTPTest {

    /**
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // http://www.gmap-pedometer.com/?r=5594937 
        //String google = "http://kongregate.ninjawarz.brokenbulbstudios.com//ajax/fight";
        //HTTPConnection connection = new HTTPConnection(url, out);
        /*
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("Content-Length", "11");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Keep-Alive", "115");
        */
        System.out.println("Fighting...");
        String google = "http://kongregate.ninjawarz.brokenbulbstudios.com//ajax/fight";
        URL url = new URL(google);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HTTPConnection connection = new HTTPConnection(url, bytes);
        OutputStream out = new FileOutputStream(new File("c:/temp/testOut.zip"));

        /*
        buildFightConnection(connection, 98282);
        connection.connect();
        System.out.println("\n\nFought.  Waiting 10s");
        Thread.sleep(10000);
        */
        System.out.println("Healing...");
        google = "http://kongregate.ninjawarz.brokenbulbstudios.com/ajax/hospital";
        google = "http://kongregate.ninjawarz.brokenbulbstudios.com/ajax/get_flat_news";
        url = new URL(google);
        connection = new HTTPConnection(url, bytes);

        //buildHealPackage(connection);
        buildNewsPackage(connection);
        connection.connect();
        String message = bytes.toString();
        System.out.println(message);
        System.out.println(NewsParser.parseNews(message));
        //Thread.sleep(10000);
        //System.out.println("Connect 2");
        //connection.connect();
    }
    
    private static void init(String[] args) {
        // nothing
    }

    private static void buildNewsPackage(HTTPConnection connection) {
        String token = "PHPSESSID=khopmimmfiacbuslk0a1ei49ipoijltk";
        connection.setContentToken(token);
        
        /* Fight package */
        
        connection.setRequestProperty("Host", "kongregate.ninjawarz.brokenbulbstudios.com");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("DNT", "1");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", "__utma=50608691.42758719.1343580137.1344118242.1344136864.40; __utmz=50608691.1343580137.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); PHPSESSID=khopmimmfiacbuslk0a1ei49ipoijltk; __utmc=50608691");
        connection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/game.swf?880");
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-length", String.valueOf(token.length()));

    }

    private static void buildHealPackage(HTTPConnection connection) {
        String token = "PHPSESSID=khopmimmfiacbuslk0a1ei49ipoijltk";
        connection.setContentToken(token);
        
        /* Fight package */
        
        connection.setRequestProperty("Host", "kongregate.ninjawarz.brokenbulbstudios.com");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("DNT", "1");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", "__utma=50608691.42758719.1343580137.1344118242.1344136864.40; __utmz=50608691.1343580137.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none\"); PHPSESSID=khopmimmfiacbuslk0a1ei49ipoijltk; __utmc=50608691; __utmb=50608691.35.10.1344136864");
        connection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/game.swf?880");
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-length", String.valueOf(token.length()));

    }

    private static void buildFightConnection(HTTPConnection connection, int fighter) {
        String token = "PHPSESSID=khopmimmfiacbuslk0a1ei49ipoijltk&opponent=" + fighter;
        connection.setContentToken(token);
        
        /* Fight package */
        
        connection.setRequestProperty("Host", "kongregate.ninjawarz.brokenbulbstudios.com");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("DNT", "1");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", "__utma=50608691.42758719.1343580137.1344118242.1344136864.40; __utmz=50608691.1343580137.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none\"); PHPSESSID=khopmimmfiacbuslk0a1ei49ipoijltk; __utmc=50608691; __utmb=50608691.35.10.1344136864");
        connection.setRequestProperty("Referer", "http://kongregate.ninjacdn.brokenbulbstudios.com/swf/game.swf?880");
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-length", String.valueOf(token.length()));
        
    }
}
