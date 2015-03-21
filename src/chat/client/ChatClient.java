/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import bc.rb.BroadcastReceiver;
import bc.rb.FIFORbImpl;
import bc.rb.Message;
import bc.rb.Broadcast;
import bc.rb.RbImpl;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;

public class ChatClient implements BroadcastReceiver {

	private Broadcast bcast;
	
	private String userName; 
	
	private final ServerSocket listener;

	private final String serverAddr;

	private final int serverPort;

	private final boolean isDebug;
	
	private int msgNumber;

	private void log(String message){
		if(isDebug){
			System.out.print("----");
			System.out.println(message);
		}
	}

	/**
	 * Constructor call when used by console mode.
	 * @param ip_address
	 * 		IP address of the server.
	 * @param port
	 * 		Server port.
	 * @param is_debug
	 * 		If it is debugging.
	 * @throws IOException
	 */
	public ChatClient(String ipAddress, int port, boolean isFifo, boolean isDebug) throws IOException{
		listener = new ServerSocket(0);
		this.isDebug = isDebug;
		serverAddr = ipAddress;
		serverPort = port;
		
		if(! isFifo){
			bcast = new RbImpl();
		}
		else {
			bcast = new FIFORbImpl();
		}
		
		log("Created " + listener);
	}

	/**
	 * Connect to server. 
	 * @param userName
	 * @throws IOException 
	 */
	public boolean register(String userName) {
		boolean succeeded = false;

		this.userName = userName;

		log("User name:" + userName);

		final Socket server;

		try {
			server = new Socket(serverAddr, serverPort);


			final BufferedReader in = new BufferedReader(
					new InputStreamReader(server.getInputStream()));

			/**
			 * Set writer to be auto-flushed!
			 */
			final PrintWriter out = new PrintWriter(server.getOutputStream(), true);


			String reg_request = ChatSystemConstants.MSG_REG + userName + ":" + listener.getLocalPort();

			out.println(reg_request);

			log("Sent registration request [" + reg_request + "] to " + server);

			// Get the first message returned by server.
			String msg = in.readLine();

			if (msg == null){
				// Server closes the connection.
				display("Server closed connection.");

			}		
			else if(msg.startsWith(ChatSystemConstants.MSG_REJ)){
				display("Server rejects registration request.");

			}
			else if(msg.startsWith(ChatSystemConstants.MSG_ACK)){
				
				display("Registration succeeded.");
				
				// Activate a heart-beat sender.
				new Thread(new HeartbeatSender(serverAddr, serverPort, userName)).start();

				User currentUser = new User(userName, listener.getInetAddress().getHostAddress(), listener.getLocalPort());
				
				bcast.init(currentUser, this);

				// Sent GET request to obtain active user list
				out.println(ChatSystemConstants.MSG_GET);

				msg = in.readLine();

				// Receive the list of active users
				if(msg.startsWith(ChatSystemConstants.MSG_USG)){

					this.display("Active Users List");
 
					String user_str = msg.substring(ChatSystemConstants.MSG_USG.length());
							
					bcast.addMember(new User(user_str));
					
					this.display(user_str);
					
					// Get and display the remaining lines.
					while( null != (msg = in.readLine())){
						
						bcast.addMember(new User(msg));
						this.display(msg);
					}
				}
				
				
				new Thread(new MessageReceiver(listener, bcast)).start();
				msgNumber = 0;

				succeeded =  true;			
			}
			else{
				// Invalid message from server
				log("Received invalid message (" + msg + ").");
				display("Failed to register.");
			}

			server.close();

		} catch (UnknownHostException e) {
			display("Unknown server.");
			log(e.getMessage());
		} catch (IOException e) {
			display("Failed to connect to server.");
			log(e.getMessage());
		}

		return succeeded;
	}
	
	public void send(String content){
		if(content.length() == 0){
			return;
		}
		
		// Craft a message that contains user name
		final Message msg = new Message(userName, content, msgNumber);
		
		// Broadcast the message
		bcast.broadcast(msg);
		
		// Increment message counter
		msgNumber++;
	}

	public void receive(Message m) {
		System.out.println(m.getSender() + ":" + m.getContent());
	}
	
	/**
	 * Display msg on the client side.
	 * @param msg
	 */
	public void display(String msg){
		System.out.println(msg);
	}

	public String getUserName(){
		return userName;
	}

	public ServerSocket getListener(){
		return listener;
	}

	/**
	 * program runs with commands -ip=(host name|ip address) -port=number [-debug] [-fifo]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		boolean is_debug = false;
		boolean is_fifo = false;
		int server_port = ChatSystemConstants.DEFAULT_PORT;
		String server_ip = "";

		/**
		 * Parse user commands.
		 */
		// Use set to handle commands entered without order.
		Set<String> commands = new HashSet<String>();
		for(int i=0; i<args.length; i++){
			commands.add(args[i]);
		}


		for(String command : commands){
			if(command.startsWith("-debug")){
				is_debug = true;
			}
			else if(command.startsWith("-port=")){
				server_port = Integer.parseInt(command.substring(6));
			}
			else if(command.startsWith("-ip=")){
				server_ip = command.substring(4);
			}
			else if(command.startsWith("-fifo")){
				is_fifo = true;
			}
		}

		/**
		 *  Initialize chat client.
		 */
		ChatClient chatClient = null; 

		try {
			chatClient = new ChatClient(server_ip, server_port, is_fifo, is_debug);

		} catch (IOException e) {
			System.out.println("Failed to initialize client.");
			System.exit(-1);
		}


		final Scanner in = new Scanner(System.in);

		/**
		 * Register a user.
		 */
		while(true) {
			System.out.print("Enter user name:");

			String name = in.next(ChatSystemConstants.NAME_PATTERN);

			// Consume white space
			in.nextLine();


			if(chatClient.register(name)){
				break;
			}

		}

		// Waiting for user input and broadcast message
		while(true){
			String msg = in.nextLine();
			chatClient.send(msg);
		}	


	}

}
