/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.util.HashSet;
import java.util.Set;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;
import chat.user.group.UserGroup;

// Periodically check if user in userGroup is ac
public class DeadCollector implements Runnable{

	private final UserGroup userGroup;

	public DeadCollector(final UserGroup userGroup){
		this.userGroup  = userGroup;
	}

	public void run() {
		while(true){
			// Consider latency
			try {
				Thread.sleep((long) (ChatSystemConstants.HEARTBEAT_RATE * 1.1));

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String bc_msg = "";
			Set<String> inactive_names = new HashSet<String>();
			synchronized(userGroup){			
				for(User user: userGroup.getUsers()){
					long current_time = System.currentTimeMillis();

					if(!user.isActive(current_time)){
						// User becomes inactive
						inactive_names.add(user.getName());
					}
				}
				
				for(String deadName : inactive_names){
					User dead = userGroup.remove(deadName);
					bc_msg += ChatSystemConstants.MSG_RMU + dead + "\n";
				}
				
				// Broadcast the dead
				new StatusBroadcaster(userGroup.getUsers(), bc_msg).run();
			}
		}
	}
}
