/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package bc.rb;

public class Message{
	
	private String content;
	private int number;
	
	public Message(final String content, final int number){
		this.content = content;
		this.number = number;
	}
	
	public Message(final String rawMsg){
		final int colon_idx = rawMsg.indexOf(':');
		this.number = Integer.parseInt(rawMsg.substring(0, colon_idx));
		this.content = rawMsg.substring(colon_idx+1);
	}
	
	public int getNumber(){
		return number;
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
	
	public String toString(){
		return number + ":" + content;
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
				&& (o_msg.getContent().equals(this.getContent()));
		}
	}
	
	@Override
	public int hashCode() {
		return content.hashCode() + number;
	}
	
}
