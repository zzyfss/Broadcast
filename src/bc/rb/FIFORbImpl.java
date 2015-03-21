/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bc.beb.BEBroadcaster;
import chat.constant.ChatSystemConstants;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.User;
import chat.user.group.UserGroup;

/**
 * FIFO Reliable Broadcast implementation.
 */
public class FIFORbImpl implements Broadcast{

	private final UserGroup userGroup;

	private User currentUser;

	private BroadcastReceiver bcReceiver;

	private final Map<String, Set<Message>> rb_delivered;

	private final Map<String, PriorityQueue<Message>> fifo_pending;

	private final Map<String, Integer> seqNum;

	private final ExecutorService beb_pool;

	public FIFORbImpl(){
		userGroup = new UGConcurrentHashMapImpl();
		rb_delivered = new HashMap<String, Set<Message>>();
		fifo_pending = new HashMap<String, PriorityQueue<Message>>();
		seqNum = new HashMap<String, Integer>();
		beb_pool = Executors.newFixedThreadPool(ChatSystemConstants.NUM_THREAD);
	}

	public void init(final User currentUser, final BroadcastReceiver br) {
		// Clear user group, delivered msg set and seq number
		userGroup.clear();
		rb_delivered.clear();
		fifo_pending.clear();
		seqNum.clear();

		// Set currentUser
		this.currentUser = currentUser;

		// Add current user to member group
		addMember(currentUser);

		// Set client
		bcReceiver  = br;
	}

	public void addMember(final User newUser) {
		if(!userGroup.contains(newUser.getName())){
			// Add user to userGroup
			userGroup.add(newUser);

			// Instantiate a reliable delivered set for new user
			Set<Message> u_rb_delivered = new HashSet<Message>();
			rb_delivered.put(newUser.getName(), u_rb_delivered);

			// Instantiate a fifo delivered set
			PriorityQueue<Message> u_fifo_delivered = new PriorityQueue<Message>();
			fifo_pending.put(newUser.getName(), u_fifo_delivered);

			// Instantiate a seq number of new user
			seqNum.put(newUser.getName(), new Integer(0));

			if(newUser != currentUser){

				synchronized(seqNum.get(currentUser.getName())){
					// Send current user's seq to new member (unicast)
					final String seq_msg = ChatSystemConstants.MSG_SEQ 
							+ currentUser.getName() 
							+ ":" + seqNum.get(currentUser.getName());
					new Unicaster(newUser, seq_msg).run();
				}
			}

		}
	}

	public void removeMember(final User dead) {

		// Remove user from the group
		userGroup.remove(dead.getName());

		// Get delivered messages of the dead
		final Set<Message> d_msg_set = rb_delivered.get(dead.getName());

		if(	null != d_msg_set ){
			// Broadcast each message in the delivered set.
			for(Message m: d_msg_set){
				final String msg = ChatSystemConstants.MSG_BEB + m.toString();
				beb_pool.execute(new BEBroadcaster(userGroup.getUsers(), msg));
			}	
		}
	}

	public void broadcast(final Message m) {

		String msg = "";
		synchronized(seqNum.get(currentUser.getName())){

			// Set message sequence number
			int seq = seqNum.get(currentUser.getName());
			m.setNumber(seq);
			
			// Immediately self deliver.
			deliver(m);	

			// Increment seq number
			seq++;
			seqNum.put(currentUser.getName(), new Integer(seq));

			// Craft actual broadcast message (over network)
			msg = ChatSystemConstants.MSG_BEB + m.toString();
		}

		// Broadcast 
		beb_pool.execute(new BEBroadcaster(userGroup.getUsers(), msg));
	}
	
	// Reliable deliver
	public void deliver(Message m) {

		final String sender = m.getSender();

		// get the delivered message set of the sender
		Set<Message> s_delivered = rb_delivered.get(sender);

		// fifo deliver if it has not been delivered.
		if(!s_delivered.contains(m)){
			s_delivered.add(m);
			fifoDeliver(m);
		}
	}

	public void fifoDeliver(Message m){

		final String sender = m.getSender();

		// get the fifo pending queue associated to the sender
		final PriorityQueue<Message> s_pending = fifo_pending.get(sender);

		synchronized(seqNum.get(sender)){

			// Get the sender's seq number 
			int seq = seqNum.get(sender).intValue();

			if(m.getNumber() == seq) {			
				// Deliver message to client
				bcReceiver.receive(m);

				// Increment sender sequence number
				seq++;

				// Deliver all messages that are qualified
				Message p_msg;
				while(null != (p_msg=s_pending.peek())){

					// Delivered
					if(p_msg.getNumber() == seq){
						bcReceiver.receive(p_msg);

						// Increment seq
						seq ++;

						// Remove message from the pending queue
						s_pending.poll();
					}
					else{
						// Even the smallest element in the queue doesn't match seq number
						break;
					}
				} // End while	
			}
			else{
				// Add message to the pending set if its number doesn't match seq
				s_pending.add(m);
			}

			// Update seq number
			seqNum.put(sender, new Integer(seq));
		}
	}

	public String getMembers() {
		return userGroup.toString();
	}

	public void setSeqNum(String userName, int seq) {
		// Set user's sequence number
		synchronized(seqNum.get(userName)){
			seqNum.put(userName, new Integer(seq));
		}
	}

}
