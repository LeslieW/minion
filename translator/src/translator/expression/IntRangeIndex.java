package translator.expression;

public class IntRangeIndex implements Index {

	private int lowerIndex;
	private int upperIndex;
	private int type;
	
	
	// ============== CONSTRUCTORS ================
	
	public IntRangeIndex(int lowerIndex,
			             int upperIndex) {
		this.lowerIndex = lowerIndex;
		this.upperIndex = upperIndex;
		this.type = Index.LOWER_UPPER_RANGE_INT_INDEX;
	}
	
	public IntRangeIndex(int index,
					boolean isLowerIndex) {
		if(isLowerIndex)
			this.lowerIndex = index;
		else this.upperIndex = index;
		
		this.type = (isLowerIndex) ?
				Index.LOWER_RANGE_INT_INDEX :
					Index.UPPER_RANGE_INT_INDEX;
	}
	
	
	// =========== INHERITED METHODS ===============
	
	public Index copy() {
		if(this.type == Index.LOWER_UPPER_RANGE_INT_INDEX)
			return new IntRangeIndex(this.lowerIndex, this.upperIndex);
		
		else return (this.type == Index.LOWER_RANGE_INT_INDEX) ?
				new IntRangeIndex(this.lowerIndex, true) :
					new IntRangeIndex(this.upperIndex, false);
	}

	public Index evaluate() {
		return this;
	}

	public int getType() {
		return this.type;
	}

	public Index insertValueForVariable(int value, String variableName) {
		return this;
	}

	public boolean isConstantIndex() {
		return true;
	}

	
	// ============ ADDITIONAL STUFF ========================
	
	public int getLowerIndex() {
		return this.lowerIndex;
	}
	
	public int getUpperIndex() {
		return this.upperIndex;
	}
	
	
}
