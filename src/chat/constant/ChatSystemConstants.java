package chat.constant;

/**
 * Define constants used across the chat system.
 */
public class ChatSystemConstants {
	
	public final static String NAME_PATTERN = "([0-9A-Za-z\n])+";
	/**
	 * Default port for server 
	 */
	public final static int DEFAULT_PORT = 8888;
	
	/**
	 * Heart beat rate in millisecond used to maintain user group.
	 */
	public final static int HEARTBEAT_RATE = 10*1000;
	
	/**
	 * The maximum length of heart beat message.
	 */
	public final static int HEARTBEAT_LEN = 32;
	
	/**
	 * Default size of thread pool.
	 */
	public final static int NUM_THREAD = 16;
	
	/**
	 * Default initial capacity of user group.
	 */
	public final static int INIT_CAP = 128;
	
	/**
	 * Default load factor.
	 */
	public final static float LOAD_FACTOR = 0.75f;
	
	/**
	 * Each message communicated between server and client
	 * must be preceded by a 3-byte command.
	 * 
	 * Commands determine what action to be taken. 
	 */
	
	/**
	 * Prefix of acknowledgment sent by server to confirm user registration.
	 */
	public final static String MSG_ACK = "ACK";
	
	/**
	 * Prefix of heart beat sent by client to confirm liveness.
	 */
	public final static String MSG_HBT = "HBT";

	/**
	 * Prefix of Registration request sent by client.
	 */
	public final static String MSG_REG = "REG";
	
	/**
	 * Prefix of Get command sent by client to retrieve user group information.
	 */
	public final static String MSG_GET = "GET";
	
	/**
	 * Prefix of Rejecting client request sent by server
	 */
	public final static String MSG_REJ = "REJ";
	
	public final static String MSG_RMU = "RMU";
	
	public final static String MSG_ADD = "ADD";
	
	public final static String MSG_BEB = "BEB";
	
	public final static String MSG_SEQ = "SEQ";
	
	/**
	 * Prefix of user group information sent by server. 
	 * This message is special since it contains multiple lines.
	 * Here we assume the server won't send other message after
	 * sending group information.
	 */
	public final static String MSG_USG = "USG";
	
	
}
