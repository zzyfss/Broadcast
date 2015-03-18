/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.user.group;

import chat.constant.ChatSystemConstants;

/**
 * Represents an user in chat system.
 */
public class User {
	
	private final String name;
	
	private final String ipAddress;
	
	private final int port;
	
	/** 
	 * When the heart beat of user is received.  
	 * Used to check if the user is still alive 
	 * in chat system.
	 */
	private long lastHeartBeat;
	
	/**
	 * Parse message in the format "name, ipAddress:port".
	 * @param msg
	 */
	public User(String user) {
		int comma_index = user.indexOf(',');
		this.name = user.substring(0, comma_index);
		int port_index = user.indexOf(':');
		this.ipAddress = user.substring(comma_index+2, port_index);
		this.port = Integer.parseInt(user.substring(port_index + 1));
	}
	
	public User(String name, String ip_addr, int port){
		this.name = name;
		this.ipAddress = ip_addr;
		this.port = port;
		
		/** Set the last heart beat of the user to current time */ 
		lastHeartBeat = System.currentTimeMillis();
	}
	
	/** 
	 * User information is formatted as
	 * "name,ip_addr:port"
	 */
	public String toString(){
		return String.format("%s, %s:%d", name, ipAddress, port); 
	}

	/** Getters/Setters */
	public long getLastHeartBeat() {
		return lastHeartBeat;
	}

	public void setLastHeartBeat(long lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}

	public String getName() {
		return name;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}
	
	public boolean isActive(long currentTime) {
		return ((getLastHeartBeat() + ChatSystemConstants.HEARTBEAT_RATE)
				> currentTime);
	}
}
