package translator.expression;

public class InfiniteDomain implements BasicDomain {

	Expression lowerBound;
	Expression upperBound;
	
	
	public InfiniteDomain() {
	}
	
	public InfiniteDomain(Expression bound,
						  boolean isLowerBound) {
		if(isLowerBound)
			this.lowerBound = bound;
		else 
			this.upperBound = bound;
		
	}
	
	// ============= INHERITED METHODS =================
	
	public Domain copy() {
		if(lowerBound != null)
			return new InfiniteDomain(this.lowerBound.copy(),
									  true);
		else if(upperBound != null)
			return new InfiniteDomain(this.lowerBound.copy(),
					  			      false);
		else return new InfiniteDomain();
	}

	public Domain evaluate() {
		if(this.lowerBound != null)
			this.lowerBound = this.lowerBound.evaluate();
		
		else if(this.upperBound != null)
			this.upperBound = this.upperBound.evaluate();
		
		return this;
	}

	public int getType() {
		if(this.lowerBound != null)
			return Domain.INFINITE_UB;
		
		else if(this.upperBound != null)
			return Domain.INFINITE_LB;
		
		else return Domain.INFINITE;
	}

	public char isSmallerThanSameType(BasicDomain d) {
		return Expression.EQUAL;
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
		if(this.lowerBound != null)
			this.lowerBound = this.lowerBound.insertValueForVariable(value, variableName);
		else if(this.upperBound != null)
			this.upperBound = this.upperBound.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		if(this.lowerBound != null)
			this.lowerBound = this.lowerBound.insertValueForVariable(value, variableName);
		else if(this.upperBound != null)
		this.upperBound = this.upperBound.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		return this;
	}
	
	public boolean isConstantDomain() {
		return false;
	}

	
	public String toString() {
		
		if(this.lowerBound != null)
			return "int("+lowerBound+"..)";
		else if(this.upperBound != null)
			return "int(.."+upperBound+")";
		else return "int";
	}
	
	// ========================= ADDIITONAL METHODS ==================
	
	public Expression getLowerBound() {
		return this.lowerBound;
	}
	
	public Expression getUpperBound() {
		return this.upperBound;
	}
}
