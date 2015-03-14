/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;

import chat.user.group.User;

public class BEBroadcaster implements Runnable {

	private final Collection<User> users;
	
	private final String message;
	
	public BEBroadcaster(final Collection<User> users, String message){
		this.users = users;
		this.message = message;
	}
	
	public void run() {
		for(User user: users){
			final Socket client;
			try {
				client = new Socket(user.getIpAddress(), user.getPort());
				final PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				out.print(message);
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
