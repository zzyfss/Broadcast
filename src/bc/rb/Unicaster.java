/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import chat.user.group.User;

public class Unicaster implements Runnable {

	private final User user;

	private final String message;

	public Unicaster(final User user, String message){
		this.user = user;
		this.message = message;
	}

	public void run() {
		final Socket client;
		try {
			client = new Socket(user.getIpAddress(), user.getPort());
			final PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			out.println(message);
			client.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Client " + user + " is down.");
		}

	}
}
