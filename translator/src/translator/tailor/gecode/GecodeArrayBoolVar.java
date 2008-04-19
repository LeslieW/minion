package translator.tailor.gecode;

public class GecodeArrayBoolVar implements BooleanVariable {

	private String name;
	private int[] indices;
	
	
	public GecodeArrayBoolVar(String name,
			                  int[] indices) {
		
		this.name = name;
		this.indices = indices;
	}
	
	public boolean isArgsVariable() {
		return false;
	}

	public int[] getBounds() {
		return new int[] {0,1};
	}

	public char getType() {
		return GecodeVariable.BOOL_ARRAY_VAR;
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
