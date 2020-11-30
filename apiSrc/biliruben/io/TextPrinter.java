package biliruben.io;

import java.io.IOException;

/**
 * Given an input of text, printout the text with formatting
 * @author trey.kirk
 *
 */
public class TextPrinter {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        int r = 0;
        StringBuilder buff = new StringBuilder();
        while (r != -1) {
            r = System.in.read();
            if (r != -1) buff.append((char)r);
        }
        String formatted = buff.toString().replace("\\n", "\n").replace("\\t", "\t");
        System.out.println(formatted);
    }

}
