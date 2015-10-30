package proxyS;


import java.net.*;
import java.io.*;
import java.util.*;





public class proxyServer {
	 public static final int TIMEOUT = 500;
	 public static final int DNSTTL = 40;
	 public static HashMap<String, DNSRec> DNSTable = new HashMap<String, DNSRec>();
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;
        

        int port = 5515;	//default
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //ignore me
        }
        
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Started on: " + port);
        } catch (IOException e) {
            //System.err.println("Could not listen on port: " + args[0]);
            //System.exit(-1);
        }
        int threadNum=0;
        while (listening) {
            new serverThread(serverSocket.accept()).start();
            System.out.println(threadNum);
            threadNum++;
        }
        serverSocket.close();
    }
}



