package biliruben.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class ProxyThread extends Thread {
    Socket incoming, outgoing;
    
    //Thread constructor
    
    ProxyThread(Socket in, Socket out){
        incoming = in;
        outgoing = out;
    }
    
    //Overwritten run() method of thread -- does the data transfers
    
    public void run(){
        byte[] buffer = new byte[60];
      int numberRead = 0;
      OutputStream ToClient;
      InputStream FromClient;
      
      try{
          ToClient = outgoing.getOutputStream();      
          FromClient = incoming.getInputStream();
         while( true){
           numberRead = FromClient.read(buffer, 0, 50);
           System.out.println(this.getName() + ": " + new String(buffer, 0, 50));
           //buffer[numberRead] = buffer[0] = (byte)'+';
           
	    if(numberRead == -1){
	      incoming.close();
	      outgoing.close();
	    }
	   
	   ToClient.write(buffer, 0, numberRead);
	   
	  
	 }
 
      }
      catch(IOException e) {}
      catch(ArrayIndexOutOfBoundsException e) {}
      
    }
    
}
