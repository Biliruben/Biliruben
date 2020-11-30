package biliruben.games.ninjawarz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class OutputConsole extends JFrame {
    
    public static final int MAX_LINES = 1000;
    
    private PipedInputStream piOut;
    private PipedOutputStream poOut;
    private JTextArea textArea = new JTextArea();
    private ReaderThread _out;

    public OutputConsole(String title) throws IOException {
        // Set up System.out
        piOut = new PipedInputStream();
        poOut = new PipedOutputStream(piOut);
        //System.setOut(new PrintStream(poOut, true));

        // Add a scrolling text area
        textArea.setEditable(false);
        textArea.setRows(20);
        textArea.setColumns(50);
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.cyan);
        textArea.setFont(new Font("Serif", Font.BOLD, 15));
        setTitle(title);
        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        pack();
        setVisible(true);

        // Create reader threads
        _out = new ReaderThread(piOut);
        _out.start();
    }
    
    public void setTextColor(Color color) {
        textArea.setForeground(color);
    }
    
    public void setBackgroundColor(Color color) {
        textArea.setBackground(color);
    }
    
    public void setFont(Font font) {
        textArea.setFont(font);
    }
    
    public PipedOutputStream getOutput() {
        return poOut;
    }

    public void exitConsole() {
        try {
            piOut.close();
            poOut.close();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            setVisible(false);
            dispose();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class ReaderThread extends Thread {
        PipedInputStream pi;

        boolean running = true;

        ReaderThread(PipedInputStream pi) {
            this.pi = pi;
        }

        public void exit() {
            running = false;
            try {
                pi.close();
                int i = pi.read();
                int j = i;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void run() {
            final byte[] buf = new byte[1024];
            try {
                while (running) {
                    final int len = pi.read(buf);
                    if (len == -1) {
                        break;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            textArea.append(new String(buf, 0, len));

                            // Make sure the last line is always visible
                            textArea.setCaretPosition(textArea.getDocument().getLength());

                            // Keep the text area down to a certain character size
                            int idealSize = 1000;
                            int maxExcess = 500;
                            int lines = textArea.getLineCount();
                            if (lines > MAX_LINES) {
                                textArea.replaceRange("", 0, (lines - MAX_LINES) * textArea.getColumns());
                            }
                            /*
                            if (excess >= maxExcess) {
                                textArea.replaceRange("", 0, excess);
                            }
                            */
                        }
                    });
                }
            } catch (IOException e) {
            }
        }
    }
}