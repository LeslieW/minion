package translator.expression;

public class LexConstraint implements RelationalExpression {

	Expression leftArray;
	Expression rightArray;
	int operator;
	boolean willBeFlattenedToVariable = false;
	boolean isNested = true;
	
	// ============= CONSTRUCTOR ========================
	
	public LexConstraint(Array leftArray,
			              int operator,
			              Array rightArray) {
		
		this.leftArray = leftArray;
		this.rightArray = rightArray;
		this.operator = operator;
	}
	
	public LexConstraint(Expression left,
			             int operator,
			             Expression right) {
		
		this.leftArray = left;
		this.rightArray = right;
		this.operator = operator;
	}
	
	// ============= INHERITED METHODS ============================
	public Expression copy() {
		return new LexConstraint( this.leftArray.copy(),
				                 this.operator,
				                 this.rightArray.copy());
	}

	public Expression evaluate() {
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return this.operator;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		this.leftArray = this.leftArray.insertDomainForVariable(domain, variableName);
		this.rightArray =  this.rightArray.insertDomainForVariable(domain, variableName);
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.leftArray =  this.leftArray.insertValueForVariable(value, variableName);
		this.rightArray =  this.rightArray.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.leftArray =  this.leftArray.insertValueForVariable(value, variableName);
		this.rightArray =  this.rightArray.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		this.leftArray = this.leftArray.replaceVariableWithExpression(variableName, expression);
		this.rightArray = this.rightArray.replaceVariableWithExpression(variableName, expression);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		LexConstraint otherLex  = (LexConstraint ) e;
		
		if(this.operator == otherLex.getType()) {
			
			if(this.leftArray.getType() == otherLex.leftArray.getType()) {
				
				char diff = this.leftArray.isSmallerThanSameType(otherLex.leftArray);
				
				if(diff == EQUAL) {
					if(this.rightArray.getType() == otherLex.rightArray.getType()) {
						
						return this.rightArray.isSmallerThanSameType(otherLex.rightArray);
					}
					else return (this.rightArray.getType() < otherLex.rightArray.getType()) ?
							SMALLER : BIGGER;
				}
				else return diff;
			}
			else return (this.leftArray.getType() < otherLex.leftArray.getType()) ?
					SMALLER : BIGGER;
		}
		else return (this.operator < otherLex.getType()) ?
				SMALLER : BIGGER;
	
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
		this.isNested = false;
	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattenedToVariable = reified;
	}
	
	public String toString() {
		
		String s = ""+this.leftArray;
		
		String opString = "";
		
		switch(this.operator) {
		case Expression.LEX_GEQ: opString = "lex>=";break;
		case Expression.LEX_LEQ: opString = "lex<=";break;
		case Expression.LEX_LESS: opString = "lex<"; break;
		case Expression.LEX_GREATER: opString = "lex>"; break;
		}
		
		return s+" "+opString+" "+this.rightArray;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		return this;
	}
	
	// ============== OTHER METHODS ==========================

	public Expression getLeftArray() {
		return this.leftArray;
	}
	
	public Expression getRightArray() {
		return this.rightArray;
	}
	
	public int getOperator() {
		return this.operator;
	}
	
}
