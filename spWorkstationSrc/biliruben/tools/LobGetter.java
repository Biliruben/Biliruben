package biliruben.tools;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Blob;
import oracle.sql.BLOB;
import java.sql.SQLException;

import java.sql.Clob;
import java.util.Properties;

import oracle.sql.CLOB;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.InputStreamReader;

public class LobGetter 
{
    final static int bBufLen = 4 * 8192;
    String _query;
    String _connectString;
    String _outFile;
    Connection _conn;
    String _user;
    String _password;

    public LobGetter(String connectString, String query, String outFile) {
        this._connectString = connectString;
        this._query = query;
        this._outFile = outFile;
        this._conn = null;
    }

    public static void main(String[] args)
      throws FileNotFoundException, IOException, SQLException {
        if (args.length < 5) usage();
        int ii = 0;
        String connString = args[ii++];
        String queryFile = null;
        String outputFile = null;

        boolean read = true;
        boolean isBinary = false;

        for (; ii < args.length; ii++) {
            if (args[ii].equals("-write"))
                read = false;

            if (args[ii].equals("-blob"))
                isBinary = true;

            if (args[ii].equals("-qf") && ii < args.length - 1)
                queryFile = args[++ii];

            if (args[ii].equals("-lf") && ii < args.length - 1)
                outputFile = args[++ii];

        }

        if (queryFile == null || outputFile == null) usage();

            // all set
        if (read) {
            BufferedReader freader = new BufferedReader(new FileReader(queryFile));
            StringWriter swriter = new StringWriter();
            int bufLen = 1024;
            char[] cbuf = new char[bufLen];
            int length = -1;
            while ((length = freader.read(cbuf, 0, bufLen)) != -1) {
                swriter.write(cbuf, 0, length);
            }
            freader.close();
            swriter.close();
            String query = swriter.toString();

            LobGetter lutils = new LobGetter(connString, query, outputFile);
            if (isBinary) {
                Blob blob = lutils.getBlob();
                long wrote = lutils.writeBlobToFile(blob);
                System.out.println("Wrote " + wrote + " bytes to file " + outputFile);
            } else {
                Clob clob = lutils.getClob();
                long wrote = lutils.writeClobToFile(clob);
                System.out.println("Wrote " + wrote + " bytes to file " + outputFile);
            }
        } else {
            BufferedReader freader = new BufferedReader(new FileReader(queryFile));
            StringWriter swriter = new StringWriter();
            int bufLen = 1024;
            char[] cbuf = new char[bufLen];
            int length = -1;
            while ((length = freader.read(cbuf, 0, bufLen)) != -1) {
                swriter.write(cbuf, 0, length);
            }
            freader.close();
            swriter.close();
            String query = swriter.toString();

            LobGetter lutils = new LobGetter(connString, query, outputFile);
            Clob clob = lutils.getClob();
            InputStream creader = new FileInputStream(outputFile);
            long wrote = lutils.writeToOraClob(clob, creader);
            System.out.println("Wrote " + wrote + " bytes from file " + outputFile);
        }
    }
    
    private Connection getConnection(String connectString) throws SQLException {
        // connectString ala 'jdbc:oracle:thin:@lazysmurf.test.sailpoint.com:1521:identityiq'
        // Driver ala oracle.jdbc.OracleDriver
        Properties info = new Properties();
        info.put("user", _user);
        info.put("password", _password);
        Connection conn= DriverManager.getConnection(connectString, info);
        return conn;
    }

    public Clob getClob()
      throws SQLException {
        _conn = getConnection(_connectString);
        _conn.setAutoCommit(false);
        Statement stmt = _conn.createStatement();
        ResultSet rs = stmt.executeQuery(_query);
        Clob clob = null;
        if (rs.next()) {
            clob = rs.getClob(1);
        }
        return clob;
    }

    public Blob getBlob()
      throws SQLException {
        _conn = getConnection(_connectString);
        _conn.setAutoCommit(false);
        Statement stmt = _conn.createStatement();
        ResultSet rs = stmt.executeQuery(_query);
        Blob blob = null;
        if (rs.next()) {
            blob = rs.getBlob(1);
        }
        return blob;
    }

    public long writeClobToFile(Clob clob)
      throws IOException, SQLException {
        long wrote = 0;
        BufferedWriter fwriter = new BufferedWriter(new FileWriter(_outFile));
        wrote = readFromClob(clob, fwriter);
        fwriter.close();
        _conn.commit();
        _conn.close();
        return wrote;
    }

    public long writeBlobToFile(Blob blob)
      throws IOException, SQLException {
        long wrote = 0;
        OutputStream fwriter = new FileOutputStream(_outFile);
        wrote = readFromBlob(blob, fwriter);
        fwriter.close();
        _conn.commit();
        _conn.close();
        return wrote;
    }

    private static void usage() {
        System.err.println("Usage: java LobGetter user/passwd@sid [-write] [-blob] -qf query_file -lf lob_file");
        System.exit(1);
    }

    public static long writeToOraBlob(Blob blob, InputStream in)
      throws SQLException, IOException {
        BLOB oblob = (BLOB)blob;
        OutputStream out = oblob.getBinaryOutputStream();
        int length = -1;
        long wrote = 0;
        int chunkSize = oblob.getChunkSize();
        byte[] buf = new byte[chunkSize];
        while ((length = in.read(buf)) != -1) {
            out.write(buf, 0, length);
            wrote += length;
        }
        out.close();
        return wrote;
    }

    public long writeToOraClob(Clob clob, InputStream in)
      throws SQLException, IOException {
        CLOB oclob = (CLOB)clob;
        OutputStream out = oclob.getAsciiOutputStream();
        int length = -1;
        long wrote = 0;
        int chunkSize = oclob.getChunkSize();
        byte[] buf = new byte[chunkSize];
        while ((length = in.read(buf)) != -1) {
            out.write(buf, 0, length);
            wrote += length;
        }
        out.close();
        _conn.commit();
        return wrote;
    }

    public static long readFromBlob(Blob blob, OutputStream out)
      throws SQLException, IOException {
        InputStream in = blob.getBinaryStream();
        int length = -1;
        long read = 0;
        byte[] buf = new byte[bBufLen];
        while ((length = in.read(buf)) != -1) {
            out.write(buf, 0, length);
            read += length;
        }
        in.close();
        return read;
    }

    public static long readFromClob(Clob clob, Writer out)
      throws SQLException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clob.getAsciiStream()));
        int length = -1;
        long read = 0;
        char[] buf = new char[bBufLen];
        while ((length = in.read(buf, 0, bBufLen)) != -1) {
            out.write(buf, 0, length);
            read += length;
        }
        in.close();
        return read;
    }
}