package bc.rb;

import java.net.ServerSocket;
import java.util.Collection;

import bc.beb.BEBroadcaster;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.User;
import chat.user.group.UserGroup;

public class rbImpl implements Broadcast{

	private final UserGroup userGroup;
	
	private BroadcastReceiver bcReceiver;
	
	public rbImpl(){
		userGroup = new UGConcurrentHashMapImpl();
	}
	
	public void init(final User currentUser, final BroadcastReceiver br) {
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
		final String msg = m.toString();
		new BEBroadcaster(userGroup.getUsers(), msg).run();
	}

	public void deliver(Message m) {
		
	}

	public String getMembers() {
		return userGroup.toString();
	}

}
