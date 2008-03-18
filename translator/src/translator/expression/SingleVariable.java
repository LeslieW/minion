package translator.expression;

public class SingleVariable implements Variable {

	
	private String variableName;
	
	private Domain domain;
	
	private boolean isSearchVariable;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	
	/**
	 * constructor for the case when parameter values
	 * are given, and all variable domain bounds 
	 * are known. 
	 * 
	 * @param variableName
	 * @param upperBound
	 * @param lowerBound
	 */
	public SingleVariable(String variableName, 
			                     Domain domain) {
		
		this.variableName = variableName;
		this.domain = domain;
		this.isSearchVariable = true;
	}	
	

	
 //	============== Interfaced methods  ==================
	
	public boolean isSearchVariable() {
		return this.isSearchVariable;
	}
	
	
	public void setToSearchVariable(boolean isSearchVariable) {
		this.isSearchVariable = isSearchVariable;
	}
	
	
	public Expression copy() {
		String copiedVarName = new String(this.variableName);
		return new SingleVariable(copiedVarName,
				                  this.domain.copy());
	}

	
	public int[] getDomain() {
		if(this.domain instanceof ConstantDomain)
			return ((ConstantDomain) this.domain).getRange();
		
		else return new int [] {Expression.LOWER_BOUND, Expression.UPPER_BOUND};
	}

	
	public int getType() {
		return SINGLE_VARIABLE; 	
	}

	
	public void orderExpression() {
		// do nothing
	}
	
	public String toString() {
		return this.variableName;
	}
	
	public String getVariableName() {
		return this.variableName;
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		SingleVariable otherVariable = (SingleVariable) e;
		
		 // returns <0 if object is lexicographically smaller	
		int lexComparison = this.variableName.compareTo(otherVariable.variableName);
		
		if(lexComparison == 0) return EQUAL;
		else return (lexComparison < 0) ?
				SMALLER : BIGGER;
	
	}
	
	
	public Variable evaluate() {
		if(this.domain != null)
			this.domain = this.domain.evaluate();
		return this;
	}
	
	public Expression reduceExpressionTree() {
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		
		if(this.variableName.equals(variableName)) {
			return new ArithmeticAtomExpression(value);
		}
		else return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		
		if(this.variableName.equals(variableName)) {
			return new RelationalAtomExpression(value);
		}
		else return this;
	}

	public boolean isNested() {
		return isNested;
	}
	
	public void setIsNotNested() {
		this.isNested = false;
	}
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}
	
	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;
	}
	public Expression restructure() {
		return this;
	}
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		return this;
	}
	
	public boolean isBooleanVariable() {
		return (this.domain.getType() == Domain.BOOL);
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(this.variableName.equals(oldVariable.getVariableName()))
			return newVariable;
		return this;
	}
}
