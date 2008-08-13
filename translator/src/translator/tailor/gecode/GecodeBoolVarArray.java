package translator.tailor.gecode;

public class GecodeBoolVarArray implements GecodeArrayVariable, BooleanVariable {

	private int length;
	private String name;
	
	public GecodeBoolVarArray (String name,
							   int length) {
		this.name = name;
		this.length = length;
	}
	
	
	//=============== INHERITED METHODS ===============
	
	public int getLength() {
		return this.length;
	}

	public int getLowerBound() {
		return 0;
	}

	public int getUpperBound() {
		return 1;
	}

	public int[] getBounds() {
		return new int[] {0,1};
	}

	public char getType() {
		return BOOL_ARRAY_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public String toDeclarationCCString() {
		return "BoolVarArray "+this.name;
	}

	public boolean isArgsVariable() {
		return false;
	}
	
	// ============ ADDITONAL METHODS =================
	
	public void increaseLength() {
		this.length++;
	}

}
