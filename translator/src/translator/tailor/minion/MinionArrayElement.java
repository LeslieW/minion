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

	private String name;
	private int[] indices;
	
	
	public MinionArrayElement(String name,
			                  int[] indices) {
		
		this.name = name;
		this.indices = indices;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer(name+"[");
		
		for(int i=0; i<this.indices.length; i++) {
			if(i>0) s.append(", ");
			s.append(indices[i]+"");
		}
		return s+"]";
	}
	
	public String getVariableName() {
		return this.name;
	}
	
	protected int[] getIndices() {
		return this.indices;
	}
}
