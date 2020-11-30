package biliruben.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.xml.bind.DatatypeConverter;

public class TestDecode {

    private static final byte[] PNG_SIG = {(byte)137, 80, 78, 71, 13, 10, 26, 10};


    /**
     * @param args
     * @throws MessagingException 
     * @throws IOException 
     */
    public static void main(String[] args) throws MessagingException, IOException {


        /*
        BufferedReader in = new BufferedReader(new FileReader("c://temp//test.mime"));
        FileWriter fstream = new FileWriter("c://temp//test.mime.out");
        BufferedWriter out = new BufferedWriter(fstream);

        String line = in.readLine(); // <-- read whole line
        StringTokenizer tk = new StringTokenizer(line);
        int a = Integer.parseInt(tk.nextToken()); // <-- read single word on line and parse to int

        out.write(""+a);
        out.flush();
        */
        
        File f = new File("c:/temp/15min1.png");
        FileInputStream fis = new FileInputStream(f);

        List<byte[]> bytes = new ArrayList<byte[]>();
        byte[] buffer = new byte[1024];
        int totalBytesRead = 0;
        int bytesRead = 0;
        bytesRead = fis.read(buffer);
        while (bytesRead > 0) {
            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
            }
            bytes.add(buffer);
            buffer = new byte[1024];
            bytesRead = fis.read(buffer);
        }
        fis.close();
        byte[] fullFile = new byte[totalBytesRead];
        int i = 0;
        for (byte[] data : bytes) {
            int length = totalBytesRead > data.length ? data.length : totalBytesRead;
            System.arraycopy(data, 0, fullFile, i, length);
            totalBytesRead -= length;
            i += length;
        }
        
        FileWriter writer = new FileWriter("c:/temp/15min1.txt");
            String converted = DatatypeConverter.printBase64Binary(fullFile);
            writer.write(converted);
        writer.close();
        

        /*
        BufferedReader in = new BufferedReader(new FileReader("c://temp//test.mime"));
        FileOutputStream fostream = new FileOutputStream("c://temp//test.png.1");
        String encodedStr = in.readLine();
        while (encodedStr != null) {
            byte[] decodedSr = DatatypeConverter.parseBase64Binary(encodedStr);
            fostream.write(decodedSr);
            encodedStr = in.readLine();
        }
        fostream.flush();
        fostream.close();


        InputStream fis = new FileInputStream(new File("c://temp//test.mime"));
        // TODO Auto-generated method stub
        InputStream decoded = MimeUtility.decode(fis, "base64");
        List<byte[]> bytes = new ArrayList<byte[]>();
        byte[] buffer = new byte[1024];
        int totalBytesRead = 0;
        int bytesRead = 0;
        bytesRead = decoded.read(buffer);
        while (bytesRead > 0)
        {
            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
            }
            bytes.add(buffer);
            buffer = new byte[1024];
            bytesRead = decoded.read(buffer);
        }
        if (totalBytesRead > 0) {
            writePng(bytes, totalBytesRead);
        }
        */
    }
    
    private static void writePng(List<byte[]> bytes, int bytesRead) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File("c://temp//test.png"));

        // write png header
        fos.write(PNG_SIG);
        
        // write IHDR
        writeChunk(fos, "IHDR", new byte[] {0,0,0,127,0,0,0,127,16, 2, 0, 0, 0});
        
        // write IDAT
        byte[] allData = new byte[bytesRead];
        int bytesWritten = 0;
        for (byte[] byteArry : bytes) {
            int deltaBytes = bytesRead - bytesWritten;
            int nextBytes = deltaBytes > byteArry.length ? byteArry.length : deltaBytes;
            System.arraycopy(byteArry, 0, allData, bytesWritten, nextBytes);
            bytesWritten += byteArry.length;
        }
        writeChunk(fos, "IDAT", allData);
        
        // write IEND
        writeNullChunk(fos, "IEND");
        
        
        fos.flush();
        fos.close();
        
    }
    
    private static void writeNullChunk(OutputStream out, String chunkType) throws IOException {
        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        lenBuffer.putInt(0);
        out.write(lenBuffer.array());

        out.write(chunkType.getBytes());
        
        // data CRC (of type + data)
        long crc = getCRC(chunkType.getBytes());
        ByteBuffer crcBuffer = ByteBuffer.allocate(4);
        crcBuffer.putInt((int) crc);
        out.write(crcBuffer.array());

    }
    
    
    private static void writeChunk(OutputStream out, String chunkType, byte[] data) throws IOException {
        // 4 bits of length
        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        lenBuffer.putInt(data.length);
        out.write(lenBuffer.array());
        
        // 4 bits of type
        out.write(chunkType.getBytes());
        
        // chunk data
        out.write(data);
        
        // data CRC (of type + data)
        byte[] crcData = new byte[4 + data.length];
        System.arraycopy(chunkType.getBytes(), 0, crcData, 0, 4);
        System.arraycopy(data, 0, crcData, 4, data.length);
        long crc = getCRC(crcData);
        ByteBuffer crcBuffer = ByteBuffer.allocate(4);
        // CRCBuffer.putLong wants an 8 bit allocation
        crcBuffer.putInt((int)crc);
        out.write(crcBuffer.array());
    }
    
    /* Table of CRCs of all 8-bit messages. */
    static long[] crc_table = new long[256];
    
    /* Flag: has the table been computed? Initially false. */
    static boolean crc_table_computed = false;

    /* Make the table for a fast CRC. */
    static void make_crc_table()
    {
      long c;
      int n, k;
    
      for (n = 0; n < 256; n++) {
        c = (long) n;
        for (k = 0; k < 8; k++) {
          if ((c & 1) == 1)
            c = 0xedb88320L ^ (c >> 1);
          else
            c = c >> 1;
        }
        crc_table[n] = c;
      }
      crc_table_computed = true;
    }

    /* Update a running CRC with the bytes buf[0..len-1]--the CRC
    should be initialized to all 1's, and the transmitted value
    is the 1's complement of the final running CRC (see the
    crc() routine below). */

    private static long update_crc(long crc, byte[] buf,
            int len)
    {
        long c = crc;
        int n;

        if (!crc_table_computed)
            make_crc_table();
        for (n = 0; n < len; n++) {
            c = crc_table[(int)((c ^ buf[n]) & 0xff)] ^ (c >> 8);
        }
        return c;
    }

    
    private static long getCRC(byte[] data) {
        /*
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
        */
        
        
       

        
        /* Return the CRC of the bytes buf[0..len-1]. */
        return update_crc(0xffffffffL, data, data.length) ^ 0xffffffffL;

    }

}
