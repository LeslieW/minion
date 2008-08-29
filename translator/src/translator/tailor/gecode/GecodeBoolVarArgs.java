package translator.tailor.gecode;

public class GecodeBoolVarArgs implements ArgsArrayVariable, BooleanVariable {

	private String name;
	private int length;
	
	public GecodeBoolVarArgs(String name,
							 int length) {
		this.length = length;
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
	
	public int getLength() {
		return this.length;
	}
	
	public int getLowerBound() {
		return 0;
	}
	
	public int getUpperBound() {
		return 1;
	}
	
	public String toDeclarationCCString() {
		return "BoolVarArgs "+name+"("+this.length+")";
	}
	
	// ============= ADDITIONAL METHODS ===============
	
	public void increaseLength() {
		this.length++;
	}
}
