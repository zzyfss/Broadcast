/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import bc.beb.BEBroadcaster;
import chat.constant.ChatSystemConstants;
import chat.user.group.User;
import chat.user.group.UserGroup;

/**
 * Handler for the in-coming messages from client.
 */
public class MessageHandler implements Runnable{

	private UserGroup userGroup;

	private Socket client;

	private boolean isDebug;
	
	private void log(String message){
		if(isDebug){
			System.out.println(message);
		}
	}

	public MessageHandler(final Socket client, final UserGroup userGroup, final boolean isDebug){
		this.client = client;
		this.userGroup = userGroup;
		this.isDebug = isDebug;
	}
	
	public void run() {

		try{
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(client.getInputStream()));

			// Set writer to be auto-flushed!
			final PrintWriter out = new PrintWriter(client.getOutputStream(), true);

			String msg;

			while(null != (msg = in.readLine())) {

				if(msg.startsWith(ChatSystemConstants.MSG_REG)){

					log("Received registration request from " + client);

					final int port_sindex = msg.indexOf(':');
					final String name = msg.substring(ChatSystemConstants.MSG_REG.length(), port_sindex);
					final int client_port = Integer.parseInt(msg.substring(port_sindex+1));

					if(userGroup.contains(name)){
						out.println(ChatSystemConstants.MSG_REJ);
					}
					else{
						final User new_user = 
								new User(name, 
										client.getInetAddress().getHostAddress(),
										client_port);

						if(userGroup.add(new_user)){
							out.println(ChatSystemConstants.MSG_ACK);
							log("Added new user:" + new_user.toString());
							
							final String bc_msg = ChatSystemConstants.MSG_ADD + new_user.toString();
							
							synchronized(userGroup){
								new BEBroadcaster(userGroup.getUsers(), bc_msg).run();
							}
						}
						else {
							out.println(ChatSystemConstants.MSG_REJ);
							log("Failed to add user " + new_user);
						}
					}
				}
				else if(msg.startsWith(ChatSystemConstants.MSG_GET)){

					log("Received GET request from " + client);

					final StringBuilder sb = new StringBuilder();
					for(User usr : userGroup.getUsers()){
						sb.append(usr);
						sb.append('\n');
					}

					out.print(ChatSystemConstants.MSG_USG);
					out.print(sb);
					out.flush();

					break;
				}
				else if(msg.startsWith(ChatSystemConstants.MSG_HBT)){
					
					// Handle heartbeat 
					String name = msg.substring(ChatSystemConstants.MSG_HBT.length()).trim();

					log("Received a heartbeat from "+ name);

					final User alive_user = userGroup.get(name);

					if( null != alive_user){
						
						log("Refreshed the last heartbeat of " + name);
						alive_user.setLastHeartBeat(System.currentTimeMillis());
					}
					else{
						log("User " + name + " doesn't exist.");
						System.out.println(userGroup);
					}
					
					break;
				}
				else{
					log("Received invalid message ("+ msg + ") from " + client);
					break;
				}

			}// End while

			// Close the connection with client
			log("Close connection with " + client);
			client.close();


		}catch (IOException e){
			log(e.getMessage());
		}

	}
}
