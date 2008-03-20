package translator.expression;

public class SimpleVariable implements Variable {

	private String variableName;
	private boolean isSearchVariable;
	
	private int lb;
	private int ub;
	
	public SimpleVariable(String name) {
		this.variableName = name;
		this.lb = Expression.LOWER_BOUND;
		this.ub = Expression.UPPER_BOUND;
	}
	
	public String getVariableName() {
		return this.variableName;
	}

	public boolean isSearchVariable() {
		return this.isSearchVariable;
	}

	public void setToSearchVariable(boolean isSearchVariable) {
		this.isSearchVariable = isSearchVariable;
	}

	public Expression copy() {
		return new SimpleVariable(new String(this.variableName));
	}

	public Expression evaluate() {
		return this;
	}
	
	public int[] getDomain() {
		System.out.println("Getting domain of SimgleVariable :"+this);
		return new int[] {this.lb,
						  this.ub } ;
		
	}

	public void setDomain(int lb, int ub) {
		this.lb = lb;
		this.ub = ub;
	}
	
	public int getType() {
		return Expression.SIMPLE_VARIABLE;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		if(this.variableName.equals(variableName)) {
			return new ArithmeticAtomExpression(value);
		}
			
		return this;
	}


	public Expression replaceVariableName(String oldVariableName, String newVariableName) {
		
		if(this.variableName.equals(oldVariableName))
			this.variableName = newVariableName;
		
		return this;
	}
	

	public SingleVariable convertToSingleVariable(Domain domain) {
		
		return new SingleVariable(this.variableName, domain);
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		if(this.variableName.equals(variableName)) {
			return new RelationalAtomExpression(value);
		}
			
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return true;
	}

	public boolean isNested() {
		return true;
	}

	public char isSmallerThanSameType(Expression e) {
		SimpleVariable otherVariable = (SimpleVariable) e;
		
		int difference = otherVariable.getVariableName().compareTo(this.variableName);
		if(difference < 0) 
			return Expression.BIGGER;
		else if(difference > 0) 
			return Expression.SMALLER;
		else return Expression.EQUAL;
		
	}

	public void orderExpression() {
		// do nothing

	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		// do nothing

	}

	public void willBeFlattenedToVariable(boolean reified) {
		// do nothing

	}

	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(this.variableName.equals(oldVariable.getVariableName()))
			return newVariable;
		return this;
	}
	
	public String toString() {
		return this.variableName;
	}
}
