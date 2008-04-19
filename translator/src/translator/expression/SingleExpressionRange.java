package translator.expression;

public class SingleExpressionRange implements ExpressionRange, SingleRange {

	private Expression expressionRange;
	
	
	public SingleExpressionRange(Expression expression) {
		this.expressionRange = expression;
	}
	
	
	// ========= INHERITED METHODS ====================
	
	public char isSmallerThanSameType(BasicDomain d) {
		 
		SingleExpressionRange other = (SingleExpressionRange) d;
		
		if(this.expressionRange.getType() == other.expressionRange.getType()) {			
			return this.expressionRange.isSmallerThanSameType(other.expressionRange);
		}
		else return (this.expressionRange.getType() == other.expressionRange.getType())  ?
				Expression.SMALLER : Expression.BIGGER;

	}

	public Expression[] getLowerAndUpperBound() {
		return new Expression[] {this.expressionRange, this.expressionRange};
	}
	
	public Domain copy() {
		return new SingleExpressionRange(this.expressionRange.copy());
	}

	public Domain evaluate() {
		this.expressionRange = this.expressionRange.evaluate();
		
		if(this.expressionRange.getType() == Expression.INT)
			return new SingleIntRange(((ArithmeticAtomExpression) expressionRange).getConstant());
		
		return this;
	}

	public int getType() {
		return Domain.SINGLE_EXPR;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		this.expressionRange = this.expressionRange.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		this.expressionRange = this.expressionRange.insertValueForVariable(value, variableName);
		return this;
	}

	
	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		return this;
	}
	
	public boolean isConstantDomain() {
		return false;
	}

	public String toString() {
		return this.expressionRange+"";
	}
	
	// ======== ADDITIONAL STUFF ==============
	
	public Expression getSingleExpressionRange() {
		return this.expressionRange;
	}
	
}
