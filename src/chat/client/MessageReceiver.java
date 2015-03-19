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

import bc.rb.Broadcast;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;

/**
 * A worker thread associated to an active chat session.
 *
 */
public class MessageReceiver implements Runnable {

	private ServerSocket listener;
	private Broadcast bcast;
	
	public MessageReceiver(final ServerSocket listener, final Broadcast bcast){
		this.listener = listener;
		this.bcast = bcast;
	}
	
	public void run(){

		while(true){
			try {
				final Socket other =  listener.accept();			
				
				final BufferedReader in = new BufferedReader(
						new InputStreamReader(other.getInputStream()));
	
				String msg;
				while( (msg = in.readLine()) != null) {
					if(msg.startsWith(ChatSystemConstants.MSG_ADD)){
						final User newUser = new User(msg.substring(ChatSystemConstants.MSG_ADD.length()));
						bcast.addMember(newUser);
					}
					else if(msg.startsWith(ChatSystemConstants.MSG_RMU)){
						final User dead = new User(msg.substring(ChatSystemConstants.MSG_RMU.length()));
						bcast.removeMember(dead);
					}
					
				}	
				//System.out.println(bcast.getMembers());
				other.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			
		}
	}
}
