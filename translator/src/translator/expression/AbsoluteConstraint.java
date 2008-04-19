package translator.expression;

import translator.expression.ArithmeticAtomExpression;

/**
 * Represents the absolute constraint 
 * | argument | = result 
 * 
 * @author andrea
 *
 */

public class AbsoluteConstraint implements GlobalConstraint {

	private Expression argument;
	private Expression result;
	
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	public AbsoluteConstraint(Expression argument,
							  Expression result) {
		
		this.argument = argument;
		this.result = result;
	}
	
	
	// ============ INHERITED METHODS ==============================
	
	public Expression[] getArguments() {
		return new Expression[] {this.argument};
	}

	public Expression copy() {
		
		AbsoluteConstraint copy = new AbsoluteConstraint(this.argument.copy(),
														 this.result.copy());
		copy.willBeReified = this.willBeReified;
		return copy;
	}

	public Expression evaluate() {
		this.argument = argument.evaluate();
		this.result =  result.evaluate();
		
		
		if(argument.getType() == Expression.INT && result.getType() == INT) {
			int argValue  = ((ArithmeticAtomExpression) argument).getConstant();
			int resultValue = ((ArithmeticAtomExpression) result).getConstant();
			
			if(argValue < 0) {
				argValue = -argValue; 
			}
			
			return new RelationalAtomExpression(argValue == resultValue);
		}
		
		else if(argument.getType() == Expression.INT) {
			int argValue = ((ArithmeticAtomExpression) argument).getConstant();
			if(argValue >= 0)
				return new CommutativeBinaryRelationalExpression(new ArithmeticAtomExpression(argValue),
																 Expression.EQ,
																 result);
		}
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return Expression.ABSOLUTE_CONSTRAINT;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		this.argument = this.argument.insertDomainForVariable(domain, variableName);
		this.result = this.result.insertDomainForVariable(domain, variableName);
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO Auto-generated method stub
		int f;
		return Expression.EQUAL;
	}

	public void orderExpression() {
		this.argument.orderExpression();
		this.result.orderExpression();
	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		
		this.argument = this.argument.replaceVariableWith(oldVariable, newVariable);
		this.result = this.result.replaceVariableWith(oldVariable, newVariable);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		this.argument = this.argument.replaceVariableWithExpression(variableName, expression);
		this.result = this.result.replaceVariableWithExpression(variableName, expression);
		
		return this;
	}
	
	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified =reified;

	}

	public String toString() {
		
		return "|"+this.argument+"| = "+this.result;
	}
	
	// ============= ADDITIONAL METHODS ============================
	
	public Expression getArgument() {
		return this.argument;
	}
	
	public Expression getResult() {
		return this.result;
	}
}
