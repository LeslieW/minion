package translator.tailor.minion;

/**
 * Array Elements in Minion. IMPORTANT NOTE: we assume that 
 * every array element's indices have been normalised to 
 * zero (the offset from the indices has been already applied)
 * 
 * @author andrea
 *
 */

public class MinionArrayElement implements MinionAtom {

	String name;
	int[] indices;
	
	
	public MinionArrayElement(String name,
			                  int[] indices) {
		
		this.name = name;
		this.indices = indices;
	}
	
	public String toString() {
		String s = name+"[";
		
		for(int i=0; i<this.indices.length; i++) {
			if(i>0) s = s.concat(", ");
			s = s.concat(indices[i]+"");
		}
		return s+"]";
	}
	
	public String getVariableName() {
		return this.name;
	}
}
