/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bc.beb.BEBroadcaster;
import chat.constant.ChatSystemConstants;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.User;
import chat.user.group.UserGroup;

/**
 * Reliable Broadcast implementation.
 */
public class RbImpl implements Broadcast{

	private final UserGroup userGroup;
	
	private BroadcastReceiver bcReceiver;
	
	private final Map<String, Set<Message>> delivered;
	
	private final ExecutorService beb_pool;
	
	// Sequence number of current user. (only for reference)
	private int seqNum;
	
	public RbImpl(){
		userGroup = new UGConcurrentHashMapImpl();
		delivered = new HashMap<String, Set<Message>>();
		beb_pool = Executors.newFixedThreadPool(ChatSystemConstants.NUM_THREAD);
	}
	
	public void init(final User currentUser, final BroadcastReceiver br) {
		// Clear user group and delivered-msg set
		userGroup.clear();
		delivered.clear();
		
		seqNum = 0;
		
		addMember(currentUser);
		bcReceiver  = br;	
	}
	
	public void addMember(final User newUser) {
		if(!userGroup.contains(newUser.getName())){
			userGroup.add(newUser);
			Set<Message> u_delivered = new HashSet<Message>();
			delivered.put(newUser.getName(), u_delivered);			
		}
	}

	public void removeMember(final User dead) {
		// Remove user from the group
		userGroup.remove(dead.getName());
		
		// Get delivered messages of the dead
		final Set<Message> d_msg_set = delivered.get(dead.getName());
		
		if(	null != d_msg_set ){
			// Broadcast each message in the delivered set.
			for(Message m: d_msg_set){
				final String msg = ChatSystemConstants.MSG_BEB + m.toString();
				beb_pool.execute(new BEBroadcaster(userGroup.getUsers(), msg));
			}	
		}
	}

	public void broadcast(final Message m) {
		
		m.setNumber(seqNum);
		
		seqNum++;
		
		final String msg = ChatSystemConstants.MSG_BEB + m.toString();
		
		// Immediately self deliver.
		deliver(m);	
		
		// Broadcast 
		beb_pool.execute(new BEBroadcaster(userGroup.getUsers(), msg));
	}

	public void deliver(Message m) {
		// Check if m has been rbDeliver.
		final String sender = m.getSender();
		
		// get the delivered message set of the sender
		Set<Message> s_delivered = delivered.get(sender);
		
		// rbDeliver if it has not been delivered.
		if(!s_delivered.contains(m)){
			bcReceiver.receive(m);
			s_delivered.add(m);
		}
	}

	public String getMembers() {
		return userGroup.toString();
	}

	public void setSeqNum(String userName, int seq) {
		// Do nothing since reliable broadcast doesn't need seq number.
	}

}
