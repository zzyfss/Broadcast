/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import chat.constant.ChatSystemConstants;

public class HeartbeatSender implements Runnable {
	
	private volatile boolean running = true;
	
	private final int serverPort;
	
	private final String serverAddr;
	
	private final String userName;
	
	public HeartbeatSender(final String serverAddr, final int serverPort, final String userName) throws SocketException{
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
		this.userName = userName;	
	}
	
	public void run(){
		while(running){
			try {
				
				// Consider latency
				Thread.sleep((long) (ChatSystemConstants.HEARTBEAT_RATE * 0.9));
				
				final Socket server;

				server = new Socket(serverAddr, serverPort);
					
				/**
					 * Set writer to be auto-flushed!
					 */
					final PrintWriter out = new PrintWriter(server.getOutputStream(), true);
				
				out.println(ChatSystemConstants.MSG_HBT + userName);
				
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			} catch (InterruptedException ie){
				ie.printStackTrace();
				running = false;
			}
		}
	}

}
