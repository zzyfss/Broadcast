/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

import java.util.HashSet;
import java.util.Set;

import bc.beb.BEBroadcaster;
import chat.constant.ChatSystemConstants;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.User;
import chat.user.group.UserGroup;

/**
 * Reliable Broadcast implementation.
 */
public class rbImpl implements Broadcast{

	private final UserGroup userGroup;
	
	private BroadcastReceiver bcReceiver;
	
	private Set<Message> delivered;
	
	public rbImpl(){
		userGroup = new UGConcurrentHashMapImpl();
		delivered = new HashSet<Message>();
	}
	
	public void init(final User currentUser, final BroadcastReceiver br) {
		// Clear user group and delivered-msg set
		userGroup.clear();
		delivered.clear();
		userGroup.add(currentUser);	
		bcReceiver  = br;
	}
	
	public void addMember(final User newUser) {
		if(!userGroup.contains(newUser.getName())){
			userGroup.add(newUser);
		}
	}

	public void removeMember(final User dead) {
		userGroup.remove(dead.getName());
	}

	public void broadcast(final Message m) {
		final String msg = ChatSystemConstants.MSG_BEB + m.toString();
		// Immediately self deliver.
		deliver(m);	
		// Broadcast 
		new BEBroadcaster(userGroup.getUsers(), msg).run();
	}

	public void deliver(Message m) {
		// Check if m has been rbDeliver.
		if(! delivered.contains(m)){
			bcReceiver.receive(m);
			delivered.add(m);
		}
	}

	public String getMembers() {
		return userGroup.toString();
	}

}
