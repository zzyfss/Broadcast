package bc.co;

import java.util.HashMap;
import java.util.Map;


public class VectorClock {
	
	private Map<String, Integer> vector;
	
	public VectorClock(){
		vector = new HashMap<String, Integer>();
	}
	
	public VectorClock(String map_str){
		vector = new HashMap<String, Integer>();
		if(0!=map_str.length()){
			String[] entries = map_str.split(", ", 0);
			for(String entry : entries){
				int eq_idx = entry.indexOf('=');
				vector.put(entry.substring(0, eq_idx),
						Integer.valueOf(entry.substring(eq_idx+1)));
			}
		}
	}
	
	public String toString(){
		String result = vector.toString();
		
		// trim starting { and ending }
		return result.substring(1, result.length()-1);			
	}
	
	
	public void put(String user, int count){
		vector.put(user,  count);
	}

	public int getCount(String user_name){
		return vector.get(user_name);
	}
	
	public Map<String, Integer> getVector(){
		return vector;
	}

	
	public boolean isPre(VectorClock that) {
		if(that == null){
			throw new NullPointerException();
		}
		if(that == this){
			return true;
		}
		
		Map<String, Integer> that_vector = that.getVector();
		
		for(Map.Entry<String, Integer> entry: vector.entrySet()){
			String user_name = entry.getKey();
			Integer this_count = entry.getValue();
			Integer that_count = that_vector.get(user_name);
			
			// that_count might be null if error occurs
			
			if(that_count != null){
				if(that_count < this_count){
					return false;
				}
			}

		}
		return true;
	}
	
	/* Simple test */ 
	public static void main(String[] args){
		
		VectorClock vc1= new VectorClock();
		vc1.put("a", 1);
		vc1.put("b", 1);
		
		VectorClock vc2 = new VectorClock();
		vc2.put("a", 1);
		vc2.put("b", 2);
		
		assert vc1.isPre(vc2);
		assert !vc2.isPre(vc1);
		assert vc1.isPre(vc1);
		
		VectorClock  vc3 = new VectorClock(vc1.toString());
		// System.out.println(vc3 + "\n" + vc1);
		assert vc3.toString().equals(vc1.toString());
		
		VectorClock vc4 = new VectorClock("");
		System.out.println(vc4);
	}
	
}
