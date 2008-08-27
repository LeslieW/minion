package translator.tailor.gecode;

public class GecodeIntVarArgs implements IntegerVariable, ArgsVariable {

	private GecodeAtom[] variables;
	private String name;
	private int ub;
	private int lb;
	
	public GecodeIntVarArgs(String name,
			                GecodeAtom[] variables,
			                int lb, 
			                int ub) {
		this.variables = variables;
		this.name = name;
		this.lb = lb;
		this.ub = ub;
	}
	
	
	//========== INHERITED METHODS ==================
	
	public int[] getBounds() {
		return new int[] {lb, ub};
	}

	public int getLowerBound() {
		return this.lb;
	}
	
	public int getUpperBound() {
		return this.lb;
	}
	
	public int getLength() {
		return this.variables.length;
	}
	
	public boolean isArgsVariable() {
		return true;
	}

	public char getType() {
		return GecodeVariable.INT_ARG_VAR;
	}

	public String getVariableName() {
		return this.name;
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
	
	public String toDeclarationCCString() {
		return "IntVarArgs "+name;
	}
}
