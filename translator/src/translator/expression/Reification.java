package translator.expression;

public class Reification implements RelationalExpression {

	private Expression reifiedExpression;
	private Variable reifiedVariable;
	
	boolean isNested = true;
	boolean willBeReified = false;
	
	// ============= CONSTRUCTOR =================================
	
	public Reification(Expression reifiedExpression,
			           Variable reifiedVariable) {
		this.reifiedVariable = reifiedVariable;
		this.reifiedExpression = reifiedExpression;
	}
	
	// ================= METHODS =================================
	
	public Expression copy() {
		return new Reification(this.reifiedExpression.copy(),
				               (Variable) this.reifiedVariable.copy());
	}

	public Expression evaluate() {
		this.reifiedExpression = this.reifiedExpression.evaluate();
		
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

	public boolean isGonnaBeReified() {
		return this.isGonnaBeReified();
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void orderExpression() {
		this.reifiedExpression.orderExpression();

	}

	public Expression reduceExpressionTree() {
		this.reifiedExpression = this.reifiedExpression.reduceExpressionTree();
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeReified(boolean reified) {
		this.willBeReified = reified;

	}

	public String toString(){
		String s ="reify( ";
		s = s.concat(this.reifiedExpression+", "+this.reifiedVariable);
		
		return s+")";
	}
	
	
	// ================= OTHER METHODS =========================================
	
	public Expression getReifiedConstraint() {
		return this.reifiedExpression;
	}
	
	public Variable getReifiedVariable() {
		return this.reifiedVariable;
	}
	
}
