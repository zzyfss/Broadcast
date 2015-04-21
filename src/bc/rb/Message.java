/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

import bc.co.VectorClock;

public class Message implements Comparable<Message>{
	
	private String content;
	private int number;
	private String sender;
	private VectorClock vc;
	
	public Message(final String sender, final String content){
		this.sender = sender;
		this.content = content;
		this.number = 0;
		vc = null;
	}
	
	/**
	 * Raw message has a format "msg_number:vector_clock:user_name:msg_content".
	 * @param rawMsg
	 */
	public Message(final String rawMsg){
		// Index of first colon
		String[] parts = rawMsg.split(":", 4);
		// Parse raw message
		this.number = Integer.parseInt(parts[0]);
		this.vc = new VectorClock(parts[1]);
		this.sender = parts[2];
		this.content = parts[3];
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
	
	public VectorClock getVectorClock(){
		return vc;
	}
	
	public void setVectorClock(final VectorClock vc){
		this.vc = vc;
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
		String vc_str = "";
		if(vc != null){
			vc_str = vc.toString();
		}
		
		return number + ":" + vc_str + ":" + sender + ":" + content;
	}
	
	// Only used without vectorclock 
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

	@Override
	public int compareTo(Message o) {
		if(o == null){
			throw new NullPointerException();
		}
		if(o == this){
			return 0;
		}
		else{
			return this.getNumber() - o.getNumber();
		}
	}
	
}
