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
import bc.rb.Message;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;

/**
 * A worker thread associated to a broadcasting session.
 * It receives messages over network and invokes specific methods corresponding to message type
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
						// Add a member to broadcast group
						final User newUser = new User(msg.substring(ChatSystemConstants.MSG_ADD.length()));
						bcast.addMember(newUser);
					}
					else if(msg.startsWith(ChatSystemConstants.MSG_RMU)){
						// Remove a dead member.
						final User dead = new User(msg.substring(ChatSystemConstants.MSG_RMU.length()));
						bcast.removeMember(dead);
					}
					else if(msg.startsWith(ChatSystemConstants.MSG_BEB)){
						final Message beb_msg =
							new Message(msg.substring(ChatSystemConstants.MSG_BEB.length()));
						bcast.deliver(beb_msg);
					}
					
				}	
				//System.out.println(bcast.getMembers());
				other.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
		}
	}
}
