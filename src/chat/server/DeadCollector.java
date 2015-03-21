/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bc.beb.BEBroadcaster;

import chat.constant.ChatSystemConstants;
import chat.user.group.User;
import chat.user.group.UserGroup;

// Periodically check if user in userGroup is ac
public class DeadCollector implements Runnable{

	private final UserGroup userGroup;

	private final ExecutorService bc_pool;

	public DeadCollector(final UserGroup userGroup){
		this.userGroup  = userGroup;
		bc_pool = Executors.newFixedThreadPool(ChatSystemConstants.NUM_THREAD);
	}

	public void run() {
		while(true){
			// Consider latency
			try {
				Thread.sleep((long) (ChatSystemConstants.HEARTBEAT_RATE * 1.1));

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String bc_msg = "";
			
			Set<String> inactive_names = new HashSet<String>();			
			for(User user: userGroup.getUsers()){
				long current_time = System.currentTimeMillis();

				if(!user.isActive(current_time)){
					// User becomes inactive
					inactive_names.add(user.getName());
				}
			}

			int size = inactive_names.size();
			for(String deadName : inactive_names){
				User dead = userGroup.remove(deadName);
				bc_msg += ChatSystemConstants.MSG_RMU + dead;
				size--;

				if(size!=0){
					// Add new line if it is not the last element in the set.
					bc_msg += '\n';
				}
			}

			if(!bc_msg.equals("")){
				// Broadcast the dead
				bc_pool.execute(new BEBroadcaster(userGroup.getUsers(), bc_msg));
			}
		}
	}
}
