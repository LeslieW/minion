package translator.expression;

/**
 * Non-linear constraint of the form:
 * 
 * E1  op  E2  = result
 * 
 * where op is a non-linear operator, such as power, division or modulo
 * 
 * @author andrea
 *
 */

public class BinaryNonLinearConstraint implements RelationalExpression {

	
	private int operator;
	private Expression leftArgument;
	private Expression rightArgument;
	private Expression result;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	public BinaryNonLinearConstraint(Expression leftArgument,
									 int operator,
									 Expression rightArgument,
									 Expression result) {
		this.leftArgument = leftArgument;
		this.operator = operator;
		this.rightArgument = rightArgument;
		this.result = result;
	}
	
	
	// ===================== METHODS ================================
	
	public Expression getLeftArgument() {
		return this.leftArgument;
	}
	
	public Expression getRightArgument() {
		return this.rightArgument;
	}
	
	public Expression getResult() {
		return this.result;
	}
	
	// ======================= INHERITED METHODS =========================
	
	public Expression copy() {
		int newop = this.operator;
		return new BinaryNonLinearConstraint(this.leftArgument.copy(),
											 newop,
											 this.rightArgument.copy(),
											 this.result.copy());
	}

	public Expression evaluate() {
		this.leftArgument = this.leftArgument.evaluate();
		this.rightArgument = this.rightArgument.evaluate();
		this.result = this.result.evaluate();
		
		if(this.leftArgument.getType() == INT  && 
				this.rightArgument.getType() == INT) {
		
			int left = ((ArithmeticAtomExpression) this.leftArgument).getConstant();
			int right = ((ArithmeticAtomExpression) this.rightArgument).getConstant();
			int result;
			
			if(this.operator == MOD) 
				result = left % right;
			else if(this.operator == POWER) {
				result = 1;
				for(int i=1; i<=right; i++)
					result = result*left;
			}
			else if(this.operator == DIV) {
				result = left / right;
			}
			else return this;
			
			if(this.result.getType() == INT) {
				return new RelationalAtomExpression(result == ((ArithmeticAtomExpression) this.result).getConstant());
			}
			else return new CommutativeBinaryRelationalExpression(new ArithmeticAtomExpression(result),
																  EQ,
																  this.result);
				
		}
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return this.operator;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		
		this.leftArgument = this.leftArgument.insertDomainForVariable(domain, variableName);
		this.rightArgument = this.rightArgument.insertDomainForVariable(domain, variableName);
		this.result = this.result.insertDomainForVariable(domain, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO!!!
		return EQUAL;
	}

	public void orderExpression() {
		this.leftArgument.orderExpression();
		this.rightArgument.orderExpression();
		this.result.orderExpression();

	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		this.leftArgument = this.leftArgument.replaceVariableWith(oldVariable, newVariable);
		this.rightArgument = this.rightArgument.replaceVariableWith(oldVariable, newVariable);
		this.result = this.result.replaceVariableWith(oldVariable, newVariable);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName,
			Expression expression) {
		
		this.leftArgument = this.leftArgument.replaceVariableWithExpression(variableName, expression);
		this.rightArgument = this.rightArgument.replaceVariableWithExpression(variableName, expression);
		this.result = this.result.replaceVariableWithExpression(variableName, expression);
		
		return this;
	}

	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		this.isNested= false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattenedToVariable = reified;

	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer(this.leftArgument.toString());
		
		
		if(this.operator == POWER)
			s.append("**");
		else if(this.operator == MOD)
			s.append("%");
		else if(this.operator == DIV)
			s.append("/");
		
		s.append(this.rightArgument+" = "+this.result);
		return s.toString();
	}

}
