package translator.expression;

public class MinimumConstraint implements RelationalExpression {

	private Array arguments;
	private Expression result;
	private boolean isMaximum;
	private boolean willBeFlattenedToVar = false;
	private boolean isNested = true;
	
	public MinimumConstraint(Array arguments,
							 Expression result,
							 boolean isMaximum) {
		this.arguments = arguments;
		this.result = result;
		this.isMaximum = isMaximum;
	}
	
	
	// ============== INHERITED METHODS =====================
	
	public Expression copy() {
		return new MinimumConstraint((Array) this.arguments.copy(),
									 this.result.copy(),
									 isMaximum);
	}

	public Expression evaluate() {
		
		this.arguments = (Array) arguments.evaluate();
		this.result = this.result.evaluate();
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return this.isMaximum ? MAX_CONSTRAINT : MIN_CONSTRAINT;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		
		this.arguments = (Array) this.arguments.insertDomainForVariable(domain, variableName);
		this.result = this.arguments.insertDomainForVariable(domain, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.arguments = (Array) this.arguments.insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		this.arguments = (Array) this.arguments.insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVar;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		int f;
		// TODO Auto-generated method stub
		return EQUAL;
	}

	public void orderExpression() {
		this.arguments.orderExpression();
		this.result.orderExpression();

	}

	public Expression reduceExpressionTree() {
		this.arguments = (Array) this.arguments.reduceExpressionTree();
		this.result = this.result.reduceExpressionTree();
		
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		
		this.arguments = (Array) this.arguments.replaceVariableWith(oldVariable, newVariable);
		this.result = this.result.replaceVariableWith(oldVariable, newVariable);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName,
			Expression expression) {
		
		this.arguments = (Array) this.arguments.replaceVariableWithExpression(variableName, expression);
		this.result = this.result.replaceVariableWithExpression(variableName, expression);
		
		return this;
	}

	public Expression restructure() {
		this.arguments = (Array) this.arguments.restructure();
		this.result = this.result.restructure();
		return this;	
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattenedToVar = reified;

	}

	
	public String toString() {
		StringBuffer  s = new StringBuffer(this.result.toString());
		s.append(" = ");
		s.append(this.isMaximum ? "max(" : "min(");
		s.append(this.arguments);
		s.append(")");
		return s.toString();
	}
	
	// ================== ADDITIONAL METHODS ========================
	
	public Array getArguments() {
		return this.arguments;
	}
	
	
	public Expression getResult() {
		return this.result;
	}
	
	public boolean isMaximum() {
		return this.isMaximum;
	}
}
