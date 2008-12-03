package translator.expression;

import translator.solver.TargetSolver;

public class BinaryMinimum implements BinaryArithmeticExpression {

	
	private Expression leftArgument;
	private Expression rightArgument;
	private boolean isMaximum;
	
	boolean willBeFlattened = false;
	boolean isNested = true;
	
	
	public BinaryMinimum(Expression e1, 
						 Expression e2, 
						 boolean isMaximum) {
		this.leftArgument = e1;
		this.rightArgument = e2;
		this.isMaximum = isMaximum;
	}
	
	
	// ========== INHERITED METHODS =====================
	
	public Expression getLeftArgument() {
		return this.leftArgument;
	}

	public Expression getRightArgument() {
		return this.rightArgument;
	}

	public Expression copy() {
		boolean isMax = this.isMaximum;
		return new BinaryMinimum(this.leftArgument.copy(),
				                 this.rightArgument.copy(),
				                 isMax);
	}

	public Expression evaluate() {
		this.leftArgument = this.leftArgument.evaluate();
		this.rightArgument = this.rightArgument.evaluate();
		
		if(leftArgument.getType() == Expression.INT && 
				rightArgument.getType() == Expression.INT) {
			int left = ((ArithmeticAtomExpression) leftArgument).getConstant();
			int right = ((ArithmeticAtomExpression) rightArgument).getConstant();
			if(this.isMaximum)
				return (right > left) ? this.rightArgument : this.leftArgument;
			else return (right < left) ? this.rightArgument : this.leftArgument;
		}
		
		return this;
	}

	public int[] getDomain() {
		int[] bounds = new int[2];
		int lb_left = this.leftArgument.getDomain()[0];
		int lb_right = this.rightArgument.getDomain()[0];
		bounds[0] = (lb_left < lb_right) ? lb_left : lb_right;
		
		int ub_left = this.leftArgument.getDomain()[1];
		int ub_right = this.rightArgument.getDomain()[1];
		bounds[1] = (ub_left > ub_right) ? ub_left : ub_right;
		
		return bounds;
	}

	public int getType() {
		return (this.isMaximum) ? Expression.MAX_BIN : Expression.MIN_BIN; 
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) 
		throws Exception {
		this.leftArgument = this.leftArgument.insertDomainForVariable(domain, variableName);
		this.rightArgument = this.rightArgument.insertDomainForVariable(domain, variableName);
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattened;
	}

	public boolean isLinearExpression() {
		return (this.leftArgument.isLinearExpression() && 
				this.rightArgument.isLinearExpression());
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void orderExpression() {
		this.leftArgument.orderExpression();
		this.rightArgument.orderExpression();

	}

	public Expression reduceExpressionTree() {
		this.leftArgument = this.leftArgument.reduceExpressionTree();
		this.rightArgument = this.rightArgument.reduceExpressionTree();
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		this.leftArgument = this.leftArgument.replaceVariableWith(oldVariable, newVariable);
		this.rightArgument = this.rightArgument.replaceVariableWith(oldVariable, newVariable);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName,
			Expression expression) throws Exception {
		this.leftArgument = this.leftArgument.replaceVariableWithExpression(variableName, expression);
		this.rightArgument = this.rightArgument.replaceVariableWithExpression(variableName, expression);
		return this;
	}

	public Expression restructure() {
		this.leftArgument = this.leftArgument.restructure();
		this.rightArgument = this.rightArgument.restructure();
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public String toSolverExpression(TargetSolver solver) throws Exception {
		throw new Exception("No solver expression for "+this+" yet.");
	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattened = reified;

	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("");
		
		if(this.isMaximum)  
				s.append("max(");
		else s.append("min(");
		
		s.append(this.leftArgument+", ");
		s.append(this.rightArgument+")");
				
		return s.toString();
	}

	public boolean isMaximum() {
		return this.isMaximum;
	}
}
