package translator.tailor.gecode;

/**
 * Represents an array element, such as x[1,2] of type integer
 * 
 * @author andrea
 *
 */

public class GecodeIntArrayElem implements GecodeIntAtomVariable {

	private String name;
	private int[] indices;
	private int lb;
	private int ub;
	
	public GecodeIntArrayElem(String name,
			                 int[] indices,
			                 int lb,
			                 int ub) {
		this.name = name;
		this.indices = indices;
		this.lb = lb;
		this.ub = ub;
	}
	
	public int[] getBounds() {
		return new int[] {lb, ub};
	}

	public boolean isArgsVariable() {
		return false;
	}

	public char getType() {
		return GecodeVariable.INT_ARRAY_ELEM_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public String toString() {
		
		StringBuffer s = new StringBuffer(this.name);
		
		// we can dereference when the array is 1-dimensional
		if(indices.length == 1) {
			s.append("["+indices[0]+"]");
		}
		
		// if it is multidimensional, we have to call a method, hence we use (,)
		else {
			s.append("(");
			for(int i=0; i<this.indices.length-1; i++)
				s.append(indices[i]+",");
		
			s.append(indices[indices.length-1]+")");
		}
		
		return s.toString();
	}
	
	public String toDeclarationCCString() {
		StringBuffer s = new StringBuffer(this.name);
		
		// we can dereference when the array is 1-dimensional
		if(indices.length == 1) {
			s.append("["+indices[0]+"]");
		}
		
		// if it is multidimensional, we have to call a method, hence we use (,)
		else {
			s.append("(");
			for(int i=0; i<this.indices.length-1; i++)
				s.append(indices[i]+",");
		
			s.append(indices[indices.length-1]+")");
		}
		
		return s.toString();
	}
}
