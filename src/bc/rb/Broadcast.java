/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

import chat.user.group.User;

public interface Broadcast {
	
	/**
	 * Initialize broadcast layer
	 * @param currentUser
	 * 			Client that invokes init()
	 * @param br
	 * 			Client who receives messages
	 */
	public void init(final User currentUser, final BroadcastReceiver br);
	
	/**
	 * Add a member to the broadcast group.
	 * @param newUser
	 */
	public void addMember(final User newUser);
	
	/**
	 * Remove a member from broadcast group.
	 * @param dead
	 */
	public void removeMember(final User dead);
	
	/**
	 * Broadcast the message m to group members. 
	 * @param m
	 */
	public void broadcast(final Message m);
	
	/**
	 * Deliver messages to 
	 * @param m
	 */
	public void deliver(final Message m);
	
	/**
	 * Set user's seq number.
	 */
	public void setSeqNum(final String userName, final int seq);
	
	/**
	 * Get the string representation of group members.
	 * @return
	 */
	public String getMembers();
}
