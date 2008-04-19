package translator.tailor.gecode;

public class GecodeArrayIntVar implements IntegerVariable {

	private String name;
	private int[] indices;
	private int lb;
	private int ub;
	
	public GecodeArrayIntVar(String name,
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
		return GecodeVariable.INT_ARRAY_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public String toString() {
		
		StringBuffer s = new StringBuffer(this.name+"[");
		
		for(int i=0; i<this.indices.length-1; i++)
			s.append(indices[i]+",");
		
		s.append(indices[indices.length-1]+"]");
		
		return s.toString();
	}
	
	public String toCCString() {
		StringBuffer s = new StringBuffer(this.name+"[");
		
		for(int i=0; i<this.indices.length-1; i++)
			s.append(indices[i]+",");
		
		s.append(indices[indices.length-1]+"]");
		
		return s.toString();
	}
}
