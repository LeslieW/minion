package translator.expression;

public class AllDifferent implements UnaryRelationalExpression {

	private Array argument;
	
	/** just used for parsing ... */
	private Expression expression;
	private boolean isNested;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	public AllDifferent(Array argument) {
		this.argument = argument;
		this.isNested = true;
	}
	
	public AllDifferent(Expression e) {
		this.expression = e;
	}
	
	//============== Interfaced Methods ==================
	public Expression getArgument() {
		if(this.expression == null)
			return this.argument;
		else return this.expression;
	}

	public Expression copy() {
		if(argument != null)
			return new AllDifferent((Array) this.argument.copy());
		else return new AllDifferent(this.expression.copy());
	}

	public int getType() {
		return ALLDIFFERENT;
	}

	public void orderExpression() {
		if(argument != null)
			this.argument.orderExpression();
	}
	
	public String toString() {
		if(expression != null)
			return "alldifferent("+expression.toString()+")";
		else 
			return "alldifferent("+argument.toString()+")";
	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		AllDifferent otherAllDiff = (AllDifferent) e;
		
		if(argument != null) {
			if(this.argument.getType() < otherAllDiff.getArgument().getType()) {
				return SMALLER;
			}
			else if(this.argument.getType() == otherAllDiff.getArgument().getType()) {
				return this.argument.isSmallerThanSameType(otherAllDiff.getArgument());
			}
			else return BIGGER;
		}
		
		else return EQUAL; // TODO: 
	}

	
	public Expression evaluate() {
		if(expression == null)
			this.argument = (Array) argument.evaluate();
		else this.expression = this.expression.evaluate();
		return this;
	}
	
	public Expression reduceExpressionTree() {
		//this.argument = (Array) this.argument.reduceExpressionTree();
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		if(argument != null)
			this.argument = (Array) this.argument.insertValueForVariable(value, variableName);
		else 
			this.expression = this.expression.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		if(argument != null)
			this.argument = (Array) this.argument.insertValueForVariable(value, variableName);
		else 
			this.expression = this.expression.insertValueForVariable(value, variableName);
		return this;
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
		//this.argument = (Array) this.argument.restructure();
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		if(argument != null)
			this.argument = (Array) this.argument.insertDomainForVariable(domain, variableName);
		else 
			this.expression = this.expression.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(argument != null)
			this.argument = (Array) this.argument.replaceVariableWith(oldVariable, newVariable);
		else 
			this.expression = this.expression.replaceVariableWith(oldVariable, newVariable);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		if(argument!=null)
			this.argument = (Array) this.argument.replaceVariableWithExpression(variableName, expression);
		else {
			this.expression = this.expression.replaceVariableWithExpression(variableName, expression);
			if(expression instanceof Array)
				this.argument = (Array) expression;
		}
		
		return this;
	}
	
}
