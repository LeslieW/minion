package translator.tailor.gecode;

public class GecodeBoolVarArray implements GecodeArrayVariable, BooleanVariable {

	private int length;
	private int[] lengths;
	private String name;
	
	public GecodeBoolVarArray (String name,
							   int length) {
		this.name = name;
		this.length = length;
	}
	
	/**
	 * Constructor for multidimensional arrays
	 * 
	 * @param name
	 * @param lengths
	 */
	public GecodeBoolVarArray (String name,
			   int[] lengths) {
		this.name = name;
		this.lengths = lengths;
		this.length = 1;
		for(int i = 0; i<this.lengths.length; i++) 
			length *= lengths[i];
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
		if(this.lengths != null) 
			return GecodeConstraint.FLATTENED_ARRAY_PREFIX+name;
		return this.name;
	}

	public String toDeclarationCCString() {
		if(this.lengths != null) 
			return "BoolVarArray "+GecodeConstraint.FLATTENED_ARRAY_PREFIX+name;
		return "BoolVarArray "+this.name;
	}

	public boolean isArgsVariable() {
		return false;
	}
	
	public String toString() {
		if(this.lengths != null) 
			return GecodeConstraint.FLATTENED_ARRAY_PREFIX+name;
		return name;
	}
	
	// ============ ADDITONAL METHODS =================
	
	public void increaseLength() {
		this.length++;
	}

	public boolean isMultiDimensional() {
		return this.lengths == null;
	}
	
	public int[] getLengths() {
		return this.lengths;
	}
}
