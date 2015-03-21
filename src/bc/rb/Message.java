/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

public class Message{
	
	private String content;
	private int number;
	private String sender;
	
	public Message(final String sender, final String content, final int number){
		this.sender = sender;
		this.content = content;
		this.number = number;
	}
	
	/**
	 * Raw message has a format "msg_number:user_name:msg_content".
	 * @param rawMsg
	 */
	public Message(final String rawMsg){
		// Index of first colon
		final int first_col_idx = rawMsg.indexOf(':');
		final int sec_col_idx = rawMsg.indexOf(':', first_col_idx+1);
		
		// Parse raw message
		this.number = Integer.parseInt(rawMsg.substring(0, first_col_idx));
		this.sender = rawMsg.substring(first_col_idx+1, sec_col_idx);
		this.content = rawMsg.substring(sec_col_idx+1);
	}
	
	public int getNumber(){
		return number;
	}
	
	public String getSender(){
		return sender;
	}

	public String getContent(){
		return content;
	}
	
	public void setContent(final String content){
		this.content = content;
	}
	
	public void setNumber(final int number){
		this.number = number;
	}
	
	public void setSender(final String sender) {
		this.sender = sender;
	}
	
	public String toString(){
		return number + ":" + sender + ":" + content;
	}
	
	@Override
	public boolean equals(Object other){
		if(other==this){
			return true;
		}
		else if(!(other instanceof Message)){
			return false;
		}
		else{
			final Message o_msg = (Message) other;
			return (o_msg.getNumber() == this.getNumber())
				&& (o_msg.getSender().equals(sender))
				&& (o_msg.getContent().equals(this.getContent()));
		}
	}
	
	@Override
	public int hashCode() {
		return content.hashCode() + sender.hashCode() + number;
	}
	
}
