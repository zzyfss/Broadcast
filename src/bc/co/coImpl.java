package bc.co;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bc.beb.BEBroadcaster;
import bc.rb.Broadcast;
import bc.rb.BroadcastReceiver;
import bc.rb.Message;
import bc.rb.Unicaster;
import chat.constant.ChatSystemConstants;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.User;
import chat.user.group.UserGroup;

public class coImpl implements Broadcast {
	
	private VectorClock clock;

	private final UserGroup userGroup;

	private User currentUser;

	private BroadcastReceiver bcReceiver;

	private final Map<String, Set<Message>> rb_delivered;

	private final List<Message> co_pending;

	private final ExecutorService beb_pool;

	public coImpl(){
		userGroup = new UGConcurrentHashMapImpl();
		rb_delivered = new HashMap<String, Set<Message>>();
		co_pending = new ArrayList<Message>();
		beb_pool = Executors.newFixedThreadPool(ChatSystemConstants.NUM_THREAD);
	}

	public void init(final User currentUser, final BroadcastReceiver br) {
		// Clear user group, delivered msg set and seq number
		userGroup.clear();
		rb_delivered.clear();
		co_pending.clear();
		
		// Instantiate a vector clock
		clock = new VectorClock();

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


			// Put user into vector clock
			clock.put(newUser.getName(), 0);

			if(newUser != currentUser){
				synchronized(clock){
					// Send current user's seq to new member (unicast)
					final String seq_msg = ChatSystemConstants.MSG_SEQ 
							+ currentUser.getName() 
							+ ":" + clock.getCount(currentUser.getName());
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
		synchronized(clock){
			
			// Set vector clock of message
			m.setVectorClock(clock);
			
			// Craft actual broadcast message (over network)
			msg = ChatSystemConstants.MSG_BEB + m.toString();
			
			// Immediately self deliver.
			bcReceiver.receive(m);
			
			// Increment self counter
			String uname = currentUser.getName();
			clock.put(uname, clock.getCount(uname) + 1);
		}
		
		// Broadcast 
		beb_pool.execute(new BEBroadcaster(userGroup.getUsers(), msg));
	}
	
	// Reliable deliver
	public void deliver(Message m) {

		final String sender = m.getSender();

		// get the delivered message set of the sender
		Set<Message> s_delivered = rb_delivered.get(sender);

		//System.out.println("----sender = " + sender);
		
		if(!s_delivered.contains(m)){
			s_delivered.add(m);
			
			// Check if the sender is client himself 
			if(!m.getSender().equals(currentUser.getName())){
				// if not, add message to pending list
				co_pending.add(m);
				
				// causal deliver
				coDeliver();
			}		
		}
	}

	public void coDeliver(){

		synchronized(clock){
			// Deliver all qualified messages
			boolean changed;
			do{
				changed = false;
				
				// List of message to be deleted after delivery
				List<Message> dlist = new ArrayList<Message>();
				for(Message m: co_pending){
					if(m.getVectorClock().isPre(clock)){
						
						// Deliver message to client
						bcReceiver.receive(m);
						
						// Increment sender's counter
						String uname = m.getSender();
						clock.put(uname, clock.getCount(uname) + 1);
						
						// Add message to delete list
						dlist.add(m);
						changed = true;				
					}	
				}
				
				// remove delivered message
				co_pending.removeAll(dlist);
				
			} while(changed);
		}
	}

	public String getMembers() {
		return userGroup.toString();
	}

	
	public void setSeqNum(String userName, int seq) {
		// Set user's sequence number
		synchronized(clock){
			clock .put(userName, new Integer(seq));
		}
	}


}
