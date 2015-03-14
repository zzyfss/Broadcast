/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chat.constant.ChatSystemConstants;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.UserGroup;

public class MultithreadedChatServer {

	private final ServerSocket listener;

	private final UserGroup userGroup;

	private final ExecutorService pool;

	/**
	 * If it is debugging.
	 */
	private final  boolean isDebug;


	public MultithreadedChatServer(int port, int n_thread, boolean isDebug) 
			throws IOException{
		userGroup = new UGConcurrentHashMapImpl(n_thread);
		listener = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(n_thread);
		this.isDebug = isDebug;

		// Activate a dead collector
		new Thread(new DeadCollector(userGroup)).start();
		log("Socket created " + listener.toString());
	}

	private void log(String message){
		if(isDebug){
			System.out.println(message);
		}
	}

	public void run() throws IOException{
		try{
			while(true){
				Socket client = listener.accept();

				// Handle messages from client.
				pool.execute(new MessageHandler(client, userGroup, isDebug));		
			}
		}catch (IOException ex) {
			pool.shutdown();
		}finally {
			listener.close();
		}
	}

	/**
	 * program runs with optional commands [-port=number] [-debug] [-nthread=number] 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		boolean is_debug = false;
		int port = ChatSystemConstants.DEFAULT_PORT;
		int n_thread = ChatSystemConstants.NUM_THREAD;

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
				port = Integer.parseInt(command.substring(6));
			}
			else if(command.startsWith("-nthread=")){
				n_thread = Integer.parseInt(command.substring(9));
			}
		}

		MultithreadedChatServer m_server = new MultithreadedChatServer(port, n_thread, is_debug);
		m_server.run();

	}

}
