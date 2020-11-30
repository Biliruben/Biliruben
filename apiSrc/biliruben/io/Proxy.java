package biliruben.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Proxy {

    
    public static void main(String[] args) throws IOException {
        Proxy proxy = new Proxy (51234, "www.kongregate.com", 80);
        proxy.start();
    }
    
    private int _localPort;
    private String _remoteHost;
    private int _remotePort;
    
    public Proxy (int localPort, String remoteHost, int remotePort) {
        boolean error = false;
        String errorMessage = null;

        if(localPort <= 0){
            errorMessage = "Error: Invalid Local Port Specification";
            error = true;
        }
        if(remotePort <=0){
            errorMessage = "Error: Invalid Remote Port Specification";
            error = true;
        }
        if(remoteHost == null){
            errorMessage = "Error: Invalid Remote Host Specification";
            error = true;
        }

        if(error) {
            throw new IllegalArgumentException(errorMessage);
        }
        this._localPort = localPort;
        this._remoteHost = remoteHost;
        this._remotePort = remotePort;
    }
    
    public void start() throws IOException{



        //Test and create a listening socket at proxy

        ServerSocket server = null;
        server = new ServerSocket(_localPort);

        //Loop to listen for incoming connection, and accept if there is one

        while(true)
        {
                Socket incoming = server.accept();
                /*
                InputStream in = incoming.getInputStream();
                byte[] buffer = new byte[60];
                int len = 0;
                while (len != -1) {
                    len = in.read(buffer, 0, 50);
                    System.out.print(new String(buffer, 0, 50));
                }
                */

                //Create the 2 threads for the incoming and outgoing traffic of proxy server
                Socket outgoing = new Socket(_remoteHost, _remotePort); 

                ProxyThread incomingToOutgoing = new ProxyThread(incoming, outgoing);
                incomingToOutgoing.start();

                ProxyThread outgoingToIncoming = new ProxyThread(outgoing, incoming);
                outgoingToIncoming.start();

        }
    }


}














