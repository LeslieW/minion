package translator.expression;

public class Minimum implements UnaryArithmeticExpression {

	private boolean isMaximum;
	private Array argument;
	/** just used for parsing */
	private Expression expression;
	
	boolean willBeFlattened = false;
	boolean isNested = true;
	
	public Minimum(Array argument,
			       boolean isMaximum) {
		this.argument = argument;
		this.isMaximum = isMaximum;
	}
	
	public Minimum(Expression argument,
			       boolean isMaximum) {
		this.expression = argument;
		this.isMaximum = isMaximum;
	}
			       
	//=============== INHERITED METHODS ===================
	
	public Expression getArgument() {
		if(this.expression == null)
			return this.argument;
		else return this.expression;
	}
	
	public Expression copy() {
		if(this.argument == null)
			return new Minimum(this.expression.copy(), this.isMaximum);
		
		else return new Minimum(this.argument.copy(), this.isMaximum);
	}

	public Expression evaluate() {
		if(expression != null) {
			this.expression = this.expression.evaluate();
			if(expression instanceof Array) {
				argument = (Array) expression;
				expression = null;
			}
		}
		else if(argument != null)
			this.argument = (Array) argument.evaluate();
		
	
		if(argument != null) {
			if(argument instanceof VariableArray) {
				VariableArray array = (VariableArray) argument;
				if(array.getVariables().length == 1)
					return array.getVariables()[0];
			}
		}
		
		return this;
	}

	public int[] getDomain() {
		if(this.argument != null)
			return this.argument.getDomain();
		else return this.expression.getDomain();
	}

	public int getType() {
		
		return (this.isMaximum) ? Expression.MAX : Expression.MIN;
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

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		if(this.argument != null) {
			Expression e = this.argument.replaceVariableWithExpression(variableName, expression);
			if(e instanceof Array) 
				this.argument = (Array) e;
			
			else {
				try {
					throw new Exception("Replacing variable '"+variableName+"' with infeasible expression '"+expression+
							"' that modifies minimum-array into:"+e+". Expected array type.");
				} catch (Exception exc) {
					exc.printStackTrace(System.out);
					System.exit(1);
				}
			}
		}
		return this;
	}
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattened;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		return EQUAL;
	}

	public void orderExpression() {
		if(this.argument != null)
			this.argument.orderExpression();
		else 
			this.expression.orderExpression();

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
		this.willBeFlattened = reified;

	}
	
	public String toString() {
		StringBuffer s = new StringBuffer((this.isMaximum) ? "max(" : "min(");
		s.append(argument+")");
		return s.toString();
	}
	
	// =============== ADDITIONAL METHODS ========================
	
	public boolean isMaximum() {
		return this.isMaximum;
	}

}
