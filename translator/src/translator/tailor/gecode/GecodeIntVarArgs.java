package translator.tailor.gecode;

public class GecodeIntVarArgs implements IntegerVariable, ArgsArrayVariable {

	//private GecodeAtom[] variables;
	private String name;
	private int length;
	private int ub;
	private int lb;
	
	public GecodeIntVarArgs(String name,
			                int length,
			                int lb, 
			                int ub) {
		this.length = length;
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
		return this.length;
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
	
	
	public String toDeclarationCCString() {
		return "IntVarArgs "+name+"("+this.length+")";
	}
	
	 // ============= ADDITIONAL METHODS =================
	
	
	public void addLowerBound(int newLb) {
		if(newLb < lb)
			this.lb = newLb;
	}

	
	public void addUpperBound(int newUb) {
		if(newUb > ub)
			this.ub = newUb;
	}
	
	public void increaseLength() {
		this.length++;
	}
	
	public void setLowerBound(int lb) {
		this.lb = lb;
	}
	
	public void setUpperBound(int ub) {
		this.ub = ub;
	}
	
}
