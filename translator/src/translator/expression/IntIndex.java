package translator.expression;

public class IntIndex implements ArrayIndex {

	private int index;
	
	// ========= constructor ================
	
	public IntIndex(int index) {
		this.index = index;
	}
	
	// =========== INHERITED METHODS =============
	
	
	public Domain copy() {
		return new IntIndex(this.index);
	}

	public Domain evaluate() {
		return this;
	}

	public int getType() {
		return Domain.INT_INDEX;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}

	public boolean isConstantDomain() {
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
