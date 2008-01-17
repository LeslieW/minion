package translator.expression;

public class LexConstraint implements RelationalExpression {

	Array leftArray;
	Array rightArray;
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
	
	
	// ============= INHERITED METHODS ============================
	public Expression copy() {
		return new LexConstraint((Array) this.leftArray.copy(),
				                 this.operator,
				                 (Array) this.rightArray.copy());
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

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		this.leftArray = (Array) this.leftArray.insertDomainForVariable(domain, variableName);
		this.rightArray = (Array) this.rightArray.insertDomainForVariable(domain, variableName);
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.leftArray = (Array) this.leftArray.insertValueForVariable(value, variableName);
		this.rightArray = (Array) this.rightArray.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.leftArray = (Array) this.leftArray.insertValueForVariable(value, variableName);
		this.rightArray = (Array) this.rightArray.insertValueForVariable(value, variableName);
		
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
	
	// ============== OTHER METHODS ==========================

	public Array getLeftArray() {
		return this.leftArray;
	}
	
	public Array getRightArray() {
		return this.rightArray;
	}
	
	public int getOperator() {
		return this.operator;
	}
	
}
