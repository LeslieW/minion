package translator.expression;

public class AbsoluteValue implements UnaryArithmeticExpression {

	private Expression argument;
	boolean isNested;
	boolean willBeReified = false;
	
	//============== Constructors ==================
	public AbsoluteValue(Expression argument) {
		this.argument = argument;
		this.isNested = true;
	}
	
	
	//============== Interfaced Methods ==================
	public Expression getArgument() {
		return this.argument;
	}

	public Expression copy() {
		return new AbsoluteValue(this.argument.copy());
	}

	public int[] getDomain() {
		int[] bounds = this.argument.getDomain();
		
		int lb = (bounds[0] < 0) ?
				     bounds[0] + 2*bounds[0] :
				     bounds[0];
				     
        int ub = (bounds[bounds.length-1] < 0) ?
			         bounds[bounds.length-1] + 2*bounds[bounds.length-1] :
			    	 bounds[bounds.length-1];
			     
		return new int[] {lb, ub};
	}

	public int getType() {
		return ABS;
	}

	public void orderExpression() {
		this.argument.orderExpression();
	}
	
	public String toString() {
		return "|"+this.argument+"|";
	}
 
	public Expression evaluate() {
		
		this.argument = argument.evaluate();
		
		if(argument.getType() == INT) {
			int constant = ((ArithmeticAtomExpression) argument).getConstant();
			return (constant < 0) ? 
			 new ArithmeticAtomExpression(constant+2*constant) :
				 new ArithmeticAtomExpression(constant);
			
		}
		else return this;
	}
	
	
	public char isSmallerThanSameType(Expression e) {
		
		AbsoluteValue otherAbs = (AbsoluteValue) e;
		
		if(this.argument.getType() == otherAbs.argument.getType())
			return this.argument.isSmallerThanSameType(otherAbs.argument);
		else return (this.argument.getType() < otherAbs.argument.getType()) ?
				SMALLER : BIGGER;
	}
	
	public Expression reduceExpressionTree() {
		this.argument = this.argument.reduceExpressionTree();
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
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
		this.argument = this.argument.restructure();
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		this.argument = this.argument.insertDomainForVariable(domain, variableName);
		return this;
	}
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		this.argument= this.argument.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
}
