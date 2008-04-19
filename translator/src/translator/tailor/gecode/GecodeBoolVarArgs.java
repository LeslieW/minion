package translator.tailor.gecode;

public class GecodeBoolVarArgs implements ArgsVariable, BooleanVariable {

	private String name;
	private GecodeAtom[] variables;
	
	public GecodeBoolVarArgs(String name,
							 GecodeAtom[] variables) {
		this.variables = variables;
		this.name = name;
	}
	
	public int[] getBounds() {
		return new int[] {0,1};
	}

	public char getType() {
		return GecodeVariable.BOOL_ARG_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public boolean isArgsVariable() {
		return true;
	}

	public String toString() {
		return this.name;
	}
	
	public GecodeAtom[] getVariables() {
		return this.variables;
	}
	
	public GecodeAtom getVariableAt(int index) 
		throws GecodeException {
		
		if(index < 0 || index >= this.variables.length)
			throw new GecodeException("Index "+index+" out of bounds for:"+this.toString());
		else return 
			this.variables[index];
	}
	
	public String toCCString() {
		return "BoolVarArgs "+name;
	}
}
