package translator.expression;

public class AllDifferent implements UnaryRelationalExpression {

	private Expression argument;
	private boolean isNested;
	
	//============== Constructors ==================
	public AllDifferent(Expression argument) {
		this.argument = argument;
		this.isNested = true;
	}
	
	//============== Interfaced Methods ==================
	public Expression getArgument() {
		return this.argument;
	}

	public Expression copy() {
		return new AllDifferent(this.argument.copy());
	}

	public int getType() {
		return ALLDIFFERENT;
	}

	public void orderExpression() {
		this.argument.orderExpression();
	}
	
	public String toString() {
		return "alldifferent("+argument.toString()+")";
	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		AllDifferent otherAllDiff = (AllDifferent) e;
		
		if(this.argument.getType() < otherAllDiff.getArgument().getType()) {
			return SMALLER;
		}
		else if(this.argument.getType() == otherAllDiff.getArgument().getType()) {
			return this.argument.isSmallerThanSameType(otherAllDiff.getArgument());
		}
		else return BIGGER;
	}

	
	public Expression evaluate() {
		this.argument = argument.evaluate();
		return this;
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
}
