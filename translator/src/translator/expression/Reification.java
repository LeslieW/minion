package translator.expression;

public class Reification implements RelationalExpression {

	private Expression reifiedExpression;
	private RelationalAtomExpression reifiedVariable;
	
	boolean isNested = true;
	boolean willBeReified = false;
	
	// ============= CONSTRUCTOR =================================
	
	public Reification(Expression reifiedExpression,
			          RelationalAtomExpression reifiedVariable) {
		this.reifiedVariable = reifiedVariable;
		this.reifiedExpression = reifiedExpression;
	}
	
	// ================= METHODS =================================
	
	public Expression copy() {
		return new Reification(this.reifiedExpression.copy(),
				               (RelationalAtomExpression) this.reifiedVariable.copy());
	}

	public Expression evaluate() {
		this.reifiedExpression = this.reifiedExpression.evaluate();
		
		if(this.reifiedVariable.getType() == BOOL) {
			if(reifiedVariable.getBool())
				return reifiedExpression;
			
		}
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return REIFICATION;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.reifiedExpression = this.reifiedExpression.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.reifiedExpression = this.reifiedExpression.insertValueForVariable(value, variableName);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
	
		this.reifiedExpression = this.reifiedExpression.replaceVariableWithExpression(variableName, expression);
		return this;
	}
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		Reification otherReification = (Reification) e;
		
		if(this.reifiedExpression.getType() == otherReification.reifiedExpression.getType()) {
			
			char difference = this.reifiedExpression.isSmallerThanSameType(otherReification.reifiedExpression);
			
			if(difference == EQUAL) {
				
				if(this.reifiedVariable.getType() == otherReification.reifiedVariable.getType()) {
					return this.reifiedVariable.isSmallerThanSameType(otherReification.reifiedVariable);
				}
				else return (this.reifiedVariable.getType() < otherReification.reifiedVariable.getType()) ?
						SMALLER : BIGGER;
			}
			else return difference;
		}
		else return (this.reifiedExpression.getType() < otherReification.reifiedExpression.getType()) ?
				SMALLER : BIGGER;
		
	}

	public void orderExpression() {
		this.reifiedExpression.orderExpression();

	}

	public Expression reduceExpressionTree() {
		this.reifiedExpression = this.reifiedExpression.reduceExpressionTree();
		
		if(this.reifiedVariable.getType() == BOOL) {
			if(reifiedVariable.getBool())
				return reifiedExpression;
			
		}
		
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;

	}

	public String toString(){
		String s ="reify( ";
		s = s.concat(this.reifiedExpression+", "+this.reifiedVariable);
		
		return s+")";
	}
	
	
	public Expression restructure() {
		this.reifiedExpression = this.reifiedExpression.restructure();
		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		this.reifiedExpression = this.reifiedExpression.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		
		this.reifiedExpression = this.reifiedExpression.replaceVariableWith(oldVariable, newVariable);
		if(this.reifiedVariable.getVariable().getVariableName().equals(oldVariable.getVariableName()))
			this.reifiedVariable = new RelationalAtomExpression(newVariable);
		return this;
	}
	
	// ================= OTHER METHODS =========================================
	
	public Expression getReifiedConstraint() {
		return this.reifiedExpression;
	}
	
	public RelationalAtomExpression getReifiedVariable() {
		return this.reifiedVariable;
	}
	
}
