package biliruben.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.alutam.ziputils.ZipDecryptInputStream;

public class ZipDecrypter {

    public static void main(String[] args) throws IOException {
        String fileName = "C:\\etn_data\\TC-66 - Orphaned cert items\\Cases\\77657\\certification.zip";
        //String fileName = "c://temp//newZip.zip";
        String password = "Vi0let55!";
        
        
        ZipFile f = new ZipFile(fileName);
    }

}
