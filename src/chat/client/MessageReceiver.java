/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A worker thread associated to an active chat session.
 *
 */
public class MessageReceiver implements Runnable {

private ServerSocket listener;
	
	public MessageReceiver(ServerSocket listener){
		this.listener = listener;
		
	}
	
	public void run(){

		while(true){
			try {
				Socket other =  listener.accept();			
				
				final BufferedReader in = new BufferedReader(
						new InputStreamReader(other.getInputStream()));
	
				String msg;
				while( (msg = in.readLine()) != null) {
					System.out.println(msg);
				}	
				
				other.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			
		}
	}
}
