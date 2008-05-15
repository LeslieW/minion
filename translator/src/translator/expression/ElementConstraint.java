package translator.expression;

public class ElementConstraint implements GlobalConstraint {

	// element(variableArray, index, value) ===> variableArray[index] = value
	
	// variableArray is of type Array actually
	private Expression variableArray;
	private Expression index;
	private Expression value;
	
	private boolean willBeReified = false;
	private boolean isNested = true;
	
	// ============== CONSTRUCTOR ============================
	
	public ElementConstraint(Expression variableArray,
			                 Expression index,
			                 Expression value) {
		this.variableArray = variableArray;
		this.index = index;
		this.value = value;
	}
	
	public  ElementConstraint(Expression variableArray,
			                  Expression index) {
		this.variableArray = variableArray;
		this.index = index;
	}
	
	// ============= INHERITED METHODS ========================
	
	public Expression[] getArguments() {
		return new Expression[] {this.variableArray, this.index, this.value} ;
	}

	public Expression copy() {
		return new ElementConstraint(this.variableArray.copy(),
								     this.index.copy(),
								     this.value.copy());         
	}

	public Expression evaluate() {
		this.variableArray = this.variableArray.evaluate();
		this.value = this.value.evaluate();
		this.index = this.index.evaluate();
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return Expression.ELEMENT_CONSTRAINT;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.variableArray = this.variableArray.insertValueForVariable(value, variableName);
		this.index = this.index.insertValueForVariable(value, variableName);
		this.value = this.value.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.variableArray = this.variableArray.insertValueForVariable(value, variableName);
		this.index = this.index.insertValueForVariable(value, variableName);
		this.value = this.value.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		this.variableArray = this.variableArray.replaceVariableWithExpression(variableName, expression);
		this.index = this.index.replaceVariableWithExpression(variableName, expression);
		this.value = this.value.replaceVariableWithExpression(variableName, expression);
		
		return this;	
	}
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		ElementConstraint otherElementConstraint = (ElementConstraint) e;
		
		if(this.variableArray.getType() == otherElementConstraint.variableArray.getType()) {
			
			char difference = this.variableArray.isSmallerThanSameType(otherElementConstraint.variableArray);
			
			// they have the same variable as first argument
			if(difference == EQUAL) {
				if(this.index.getType() == otherElementConstraint.index.getType()) {
					difference = this.index.isSmallerThanSameType(otherElementConstraint.index);
					
					// they have the same index variable as second argument
					if(difference == EQUAL) {
						if(this.value.getType() == otherElementConstraint.value.getType()) {
							
							return this.value.isSmallerThanSameType(otherElementConstraint.value);
						}
						else return (this.value.getType() < otherElementConstraint.value.getType()) ?
								SMALLER : BIGGER;
					}
					else return difference;
				}
				else return (this.index.getType() < otherElementConstraint.index.getType()) ?
						SMALLER : BIGGER;
			}
			else return difference;
			
		}
		else return (this.variableArray.getType() < otherElementConstraint.variableArray.getType()) ?
				SMALLER : BIGGER;
		
	}

	public void orderExpression() {
		this.variableArray.orderExpression();
		this.index.orderExpression();
		this.value.orderExpression();
	}

	public Expression reduceExpressionTree() {
		this.variableArray = this.variableArray.reduceExpressionTree();
		this.index = this.index.reduceExpressionTree();
		this.value = this.value.reduceExpressionTree();
		
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;

	}

	public String toString() {
		
		String s = "element("+this.variableArray+", "+this.index+", "+this.value+")";
		
		return s;
	}

	public Expression restructure() {
		
		this.index = this.index.restructure();
		this.variableArray = this.variableArray.restructure();
		this.value = this.value.restructure();
		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		this.index = this.index.insertDomainForVariable(domain, variableName);
		this.value = this.value.insertDomainForVariable(domain, variableName);
		this.variableArray = this.variableArray.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		
		this.index = this.index.replaceVariableWith(oldVariable, newVariable);
		this.value = this.value.replaceVariableWith(oldVariable, newVariable);
		this.variableArray = this.variableArray.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
	
	
	//======================== ADDITIONAL METHODS =============================
	
	public void setResultExpression(Expression result) {
		this.value = result;
	}
	
	
	public Expression getValueExpression() {
		return this.value;
	}
	
	public Expression getIndexExpression() {
		return this.index;
	}
}
