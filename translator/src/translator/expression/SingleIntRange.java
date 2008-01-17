package translator.expression;

public class SingleIntRange implements ConstantDomain {

	
	private int value;
	
	
	// ======= CONSTRUCTOR =========================
	
	public SingleIntRange(int rangeValue) {
		this.value = rangeValue;
	}
	
	
	// =========== INHERITED METHODS ================
	
	public int[] getFullDomain() {
		return new int[] {value};
	}

	public int[] getRange() {
		return new int[] {value,value};
	}

	public char isSmallerThanSameType(BasicDomain d) {
		
		SingleIntRange other = (SingleIntRange) d;
		if(this.value == other.value)
			return Expression.EQUAL;
		else return (this.value == other.value) ?
				Expression.SMALLER : Expression.BIGGER;
		
		
	}

	public Domain copy() {
		return new SingleIntRange(this.value);
	}

	public Domain evaluate() {
		return this;
	}

	public int getType() {
		return Domain.SINGLE_INT;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}

	public boolean isConstantDomain() {
		return true;
	}

	public String toString() {
		return this.value+"";
	}
	
	//============ ADDITIONAL STUFF ===================
	
	public int getSingleRange() {
		return this.value;
	}
	
}
