/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.user.group;

import java.lang.String;
import java.util.Collection;

/**
 * This interface is used to edit/retrieve the information of
 * user group a chat system maintains.
 * It stores a list of User objects.
 * 
 */
public interface UserGroup {
	
	/**
	 * Add user to the group.
	 * @param user
	 * @return true if succeeded. 
	 * 		false if failed (e.g. User already exists).
	 */
	public boolean add(final User user);
	
	/**
	 * Remove user from the group. 
	 * @param name 
	 * 		The name of the user to be removed
	 * @return the removed User object.
	 */
	public User remove(final String name);
	
	/**
	 * Get user from the group.
	 * @param name
	 * @return
	 * 		 The retrieved User object.
	 */
	public User get(final String name);
	
	/**
	 * Check if user exists in the group. 
	 * @param name
	 * 		The user name.
	 * @return true if it exists; otherwise false.
	 */
	public boolean contains(final String name);
	
	/**
	 * Get a set of active users.
	 * @return String
	 */
	public Collection<User> getUsers();
	
	/**
	 * Remove all users from the group.
	 */
	public void clear();
	
}
