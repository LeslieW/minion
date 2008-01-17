package translator.expression;

public class UnaryMinus implements UnaryArithmeticExpression {

	private Expression argument;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	public UnaryMinus(Expression argument) {
		this.argument = argument;
	}
	
	//============== Interfaced Methods ==================
	public Expression getArgument() {
		return this.argument;
	}

	public Expression copy() {
		return new UnaryMinus(this.argument.copy());
	}

	public int[] getDomain() {
		int[] bounds = this.argument.getDomain();
		
		int lb = (bounds[0] < 0) ?
				     bounds[0] + 2*bounds[0] :
				    	 bounds[0] - 2*bounds[0];
		int ub = (bounds[bounds.length-1] < 0) ?
			         bounds[bounds.length-1] + 2*bounds[bounds.length-1] :
			    	     bounds[bounds.length-1] - 2*bounds[bounds.length-1];
		
        return (lb < ub) ?
        		new int[] {lb, ub} :
        			new int[] {ub, lb};
	}

	public int getType() {
		return U_MINUS;
	}

	public void orderExpression() {
		this.argument.orderExpression();

	}

	public String toString() {
		return "-"+this.argument.toString();
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		UnaryMinus otherUMinus = (UnaryMinus) e;
		
		if(this.argument.getType() == otherUMinus.argument.getType()) {
			return this.argument.isSmallerThanSameType(otherUMinus.argument);
		}
		else return (this.argument.getType() < otherUMinus.argument.getType()) ?
				SMALLER : BIGGER;		
	}
	
	public Expression evaluate() {
		
		this.argument = argument.evaluate();
		
		if(argument.getType() == INT) {
			int constant = ((ArithmeticAtomExpression) argument).getConstant();
			return (constant < 0) ? 
			 new ArithmeticAtomExpression(constant+2*constant) :
				 new ArithmeticAtomExpression(constant-2*constant);
			
		}
		else return this;
	}
	
	public Expression reduceExpressionTree() {
		this.argument = this.argument.reduceExpressionTree();
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
		return this;
	}
	
	public boolean isNested() {
		return isNested;
	}
	
	public void setIsNotNested() {
		this.isNested = false;
	}
	
	public boolean isGonnaBeReified() {
		return this.willBeReified;
	}
	
	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;
	}
	
}
