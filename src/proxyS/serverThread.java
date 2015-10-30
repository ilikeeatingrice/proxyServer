package proxyS;

import java.net.*;
import java.io.*;
import java.util.*;

// create a class for DNS record
class DNSRec {
	String ip = null; // IP of target server	
	float bornTime; // time when this record is put into Hashmap
	String getIp  (String string) //function to get IP address by name.
	{	InetAddress tempAddress = null;
		try {
			 tempAddress=InetAddress.getByName(string);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String tempStringAddress = tempAddress.toString();
		
	    return tempStringAddress.substring(tempStringAddress.indexOf("/")+1);
		
	}
}

public class serverThread extends Thread {
	private Socket socketB = null;
	private Socket toServerSocket = null;
	String targetIp = "";
	// String inputB = "";
	OutputStream outputS = null;
	OutputStream outputB = null;
	InputStream inputB = null;
	InputStream inputS = null;
	DNSRec dnsRec = new DNSRec();

	
    // Open a socket to get data flow from browser
	public serverThread(Socket socket) {
		// super("serverThread");
		this.socketB = socket;
		try {
			socketB.setSoTimeout(proxyServer.TIMEOUT);
		} catch (SocketException e) {
			
			e.printStackTrace();
		}

	}
	
	// main processing block
	public void run() {
		try {

			inputB = socketB.getInputStream();

			int port = 80;
			boolean ifNewSocket = true;
			String reqHost = "";
			while (true) {
				//Keep reading data from socket
				while (true) {
					byte bufferS[] = new byte[5000];
					int b = inputB.read(bufferS);
					String reqLog = new String(bufferS, 0, b);
					System.out.println(reqLog);
					int first = reqLog.indexOf("Host: ") + 6;
					int last = reqLog.indexOf('\n', first);
					reqHost = reqLog.substring(first, last - 1);
					// System.out.println(reqHost);
					//socket only need to be created once in one connection
					if (ifNewSocket) {
						if (proxyServer.DNSTable.containsKey(reqHost)){
							/*
							 * If target host name is contained in hashmap, and that record
							 * is not expired, use the IP address in our DNS cache to make the socket
							 * to target server.
							 */
							if ((System.currentTimeMillis() - proxyServer.DNSTable.get(reqHost).bornTime) / 1000.0 <= proxyServer.DNSTTL)
							{	
								targetIp = proxyServer.DNSTable.get(reqHost).ip;
								toServerSocket = new Socket (targetIp, port);
								toServerSocket.setSoTimeout(proxyServer.TIMEOUT);
								System.out.println("This connection is made using DNS cache!!");
							}
							/*
							 * If record expired, make connection use normal way, and renew the record.
							 */
							else {
								toServerSocket = new Socket(reqHost, port);
								toServerSocket.setSoTimeout(proxyServer.TIMEOUT);
								proxyServer.DNSTable.remove(reqHost);
								dnsRec.bornTime = System.currentTimeMillis();
								dnsRec.ip = dnsRec.getIp(reqHost);
								proxyServer.DNSTable.put(reqHost, dnsRec); 
							}
						}
						else 
							// if target host name is not cached, create socket in normal way.
							
							{
								
								toServerSocket = new Socket(reqHost, port);
								toServerSocket.setSoTimeout(proxyServer.TIMEOUT);
								dnsRec.bornTime = System.currentTimeMillis();
								dnsRec.ip = dnsRec.getIp(reqHost);
								proxyServer.DNSTable.put(reqHost, dnsRec);
						    }
						ifNewSocket = false;
					}
					
					outputS = toServerSocket.getOutputStream();
					outputS.write(bufferS, 0, b);
					outputS.flush();
					String lastChar = reqLog.substring(reqLog.length() - 4);
					// System.out.println(lastChar);
					if (lastChar.equals("\r\n\r\n"))
						break;
				}

				inputS = toServerSocket.getInputStream();
				outputB = socketB.getOutputStream();

				// Getting response from Server to Browser

				int m = 0;
				while (m != -1) {
					byte bufferB[] = new byte[5000];
					m = inputS.read(bufferB);
					//String resLog = new String(bufferB, 0, m);
					// System.out.println("responselog");
					//System.out.println(resLog.substring(0,resLog.indexOf("\r\n\r\n")));
					outputB.write(bufferB, 0, m);
					outputB.flush();
				}

			}
		} 
		catch (Exception e) {
		}

		try {
			if (socketB != null)
			socketB.close();
			if (toServerSocket != null)
			toServerSocket.close();
		} catch (Exception e) {
			System.err.println("closing expection");

		}

	}
}
