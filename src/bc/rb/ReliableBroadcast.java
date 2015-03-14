package bc.rb;

import chat.user.group.User;

public interface ReliableBroadcast {
	public void init(User currentUser, BroadcastReceiver br);
	public void addMember(User newUser);
	public void removeMember(String name);
	public void rbroadcast(Message m);
}
