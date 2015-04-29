package chat.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import bc.co.coImpl;
import bc.rb.Broadcast;
import bc.rb.BroadcastReceiver;
import bc.rb.FIFORbImpl;
import bc.rb.Message;
import bc.rb.rbImpl;

import chat.client.HeartbeatSender;
import chat.client.MessageReceiver;
import chat.constant.ChatSystemConstants;
import chat.user.group.User;

public class TestClient implements BroadcastReceiver{

	private Broadcast bcast;

	private final ServerSocket listener;

	private final String serverAddr;

	private final int serverPort;

	private final boolean isDebug;

	private static int nR=0;
	private static int n =10000;

	private static long start_t = System.currentTimeMillis();
	
	private void log(String message){
		if(isDebug){
			System.out.print("----");
			System.out.println(message);
		}
	}
	public TestClient(String ipAddress, int port, boolean isFifo, boolean isCo,boolean isDebug) throws IOException{
		listener = new ServerSocket(0);
		this.isDebug = isDebug;
		serverAddr = ipAddress;
		serverPort = port;
		if( isFifo){
			log("fifo rbcast");
			bcast = new FIFORbImpl();
		}
		else if (isCo) {
			log("co rbcast");
			bcast = new coImpl();
		}
		else{
			bcast = new rbImpl();
		}

		log("Created " + listener);
	}



	public boolean register(final String userName){
		boolean succeeded = false;


		log("User name:" + userName);
		Socket server;
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

				succeeded =  true;			

			}
			server.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return succeeded;		

	}

	/**
	 * Display msg on the client side.
	 * @param msg
	 */
	public void display(String msg){
		System.out.println(msg);
	}


	public void send(String userName,int i){

		String content =  userName + ": Boardcast_"+ i;
		// Craft a message that contains user name
		final Message msg = new Message(userName, content);

		// Broadcast the message
		bcast.broadcast(msg);

	}

	/**
	 * program runs with commands -n=number_of_user -ip=(host name|ip address) -port=number [-prefix=string] [-debug]
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws InterruptedException {
		boolean is_debug = false;
		int server_port = ChatSystemConstants.DEFAULT_PORT;
		
		String server_ip = "";
		String name_prefix = "";
		boolean is_fifo =false;
		boolean is_co = false;
		String userName = "";

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

			else if(command.startsWith("-name=")){
				userName = command.substring(6);
			}

			else if(command.startsWith("-fifo")){
				is_fifo = true;
			}
			else if(command.startsWith("-co")){
				is_co = true;
			}
		}

		/**
		 *  Initialize chat client.
		 */
		TestClient testClient = null; 

		try {
			testClient = new TestClient(server_ip, server_port, is_fifo,is_co,is_debug);

			// Activate a dummy heartbeat sender

			testClient.register(userName);
			new Thread(new DummyHeartbeatSender(server_ip, server_port, userName)).start();


		} catch (IOException e) {
			System.out.println("Failed to initialize client.");
			System.exit(-1);
		}

		/**
		 * Register users and measure through put and latency.
		 */
		double latency = 0;

		long n_response = 0;

		long n_success = 0;
		

		Thread.sleep(10000);

		
		for(int i=0; i < n; i++){
			long last_t = System.currentTimeMillis();

			testClient.send(userName,i);

			n_success++;
			n_response ++;


			long curr_t = System.currentTimeMillis();
			if(latency ==0){
				latency = curr_t - last_t;
			}
			else{
				latency = (latency + curr_t - last_t)/2;
			}
		}

		long total_t = System.currentTimeMillis() - start_t;

	/*	System.out.println("Latency=" + latency);
		System.out.println("Response=" + n_response);
		System.out.println("Success=" + n_success);
		System.out.println("Time Elapsed (in sec)=" + total_t/1000);
		System.out.println("Throughput=" + n_response * 1.0/(total_t/1000));

	*/
		while (true);
	}


	public void receive(Message m) {

		//log(m.toString());
		//System.out.println(m.getSender() + ":" + m.getContent());
		if(TestClient.nR<1){
		TestClient.start_t = System.currentTimeMillis();
		}
		System.out.println(m);
		TestClient.nR++;
			
		if(( TestClient.nR % n)==0){
			System.out.println(TestClient.nR);

		}
		
		if(TestClient.nR==(n*10)){
			long total_t = System.currentTimeMillis()-TestClient.start_t;
			System.out.println("The final time=" + total_t);
			System.out.println("Time Elapsed (in sec)=" + total_t/1000);
		
			System.out.println("Throughput=" + TestClient.nR * 1.0/(total_t/1000));
			
		
		}


	}
}

