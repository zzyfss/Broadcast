/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.user.group;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chat.constant.*;

/** 
 * An implementation of UserGroup
 * using ConcurrentHashMap as its underlying data structure,
 * which is thread-safe and guarantees performance
 * under intensive read/write operations.
 * 
 * This implementation is ideal for multi-threaded chat server.
 */
public class UGConcurrentHashMapImpl implements UserGroup {

	/**
	 * The user group is a map as <userName, User object>. 
	 */
	private final Map<String, User> userGroup;

	public UGConcurrentHashMapImpl(){
		userGroup = new ConcurrentHashMap<String, User>();
	}

	/**
	 * Constructor.
	 * @param concurrencyLevel
	 * 		the estimated number of concurrently updating threads.
	 */
	public UGConcurrentHashMapImpl(int concurrencyLevel){
		userGroup =
				new ConcurrentHashMap<String, User>(ChatSystemConstants.INIT_CAP, 
						ChatSystemConstants.LOAD_FACTOR,
						concurrencyLevel);
	}

	public boolean add(final User user) {

		/**
		 * Use synchronized block to help avoid adding
		 * two users with the same name.
		 */
		synchronized(userGroup){
			if(contains(user.getName())){
				return false;
			}
			userGroup.put(user.getName(), user);
		}	

		return true;
	}

	public User remove(final String name) {
		final User removed;
		removed = userGroup.remove(name);
		return removed;
	}

	public boolean contains(final String name) {
		final User user = userGroup.get(name);

		if (user == null){
			return false;
		}
		else {
			if(user.isActive(System.currentTimeMillis())) {
				return true;
			}
			else{
				userGroup.remove(name);
				return false;
			}
		}
	}

	public String toString(){
		final StringBuilder sb = new StringBuilder();
		synchronized(this){
			for(User usr : userGroup.values()){
				sb.append(usr);
				sb.append('\n');
			}
		}

		return sb.toString();
	}

	public User get(final String name) {
		return userGroup.get(name);
	}

	public Collection<String> getUserNames() {
		return userGroup.keySet();
	}

	public Collection<User> getUsers() {
		return userGroup.values();
	}

}
