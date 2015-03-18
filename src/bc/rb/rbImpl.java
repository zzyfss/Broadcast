package bc.rb;

import bc.beb.BEBroadcaster;
import chat.user.group.UGConcurrentHashMapImpl;
import chat.user.group.User;
import chat.user.group.UserGroup;

public class rbImpl implements Broadcast{

	private final UserGroup userGroup;
	
	public rbImpl(){
		userGroup = new UGConcurrentHashMapImpl();
	}
	
	public void init(User currentUser, BroadcastReceiver br) {
		userGroup.add(currentUser);	
	}
	
	public void addMember(User newUser) {
		if(!userGroup.contains(newUser.getName())){
			userGroup.add(newUser);
		}
	}

	public void removeMember(String name) {
		userGroup.remove(name);
	}

	public void broadcast(Message m) {
		String msg = m.toString();
		new BEBroadcaster(userGroup.getUsers(), msg).run();
	}

}
