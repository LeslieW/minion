package translator.expression;

public class IntIndex implements Index {

	private int index;
	
	// ========= constructor ================
	
	public IntIndex(int index) {
		this.index = index;
	}
	
	// =========== INHERITED METHODS =============
	
	
	public Index copy() {
		return new IntIndex(this.index);
	}

	public Index evaluate() {
		return this;
	}

	public int getType() {
		return Index.INT_INDEX;
	}

	public Index insertValueForVariable(int value, String variableName) {
		return this;
	}

	public boolean isConstantIndex() {
		return true;
	}

	public String toString() {
		return this.index+"";
	}
	
	// =========== OTHER METHODS =================
	
	public int getIndex() {
		return this.index;
	}
	
}
