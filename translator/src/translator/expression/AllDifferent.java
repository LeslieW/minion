package translator.expression;

public class AllDifferent implements UnaryRelationalExpression {

	private Array argument;
	private boolean isNested;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	public AllDifferent(Array argument) {
		this.argument = argument;
		this.isNested = true;
	}
	
	//============== Interfaced Methods ==================
	public Expression getArgument() {
		return this.argument;
	}

	public Expression copy() {
		return new AllDifferent((Array) this.argument.copy());
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
		this.argument = (Array) argument.evaluate();
		return this;
	}
	
	public Expression reduceExpressionTree() {
		//this.argument = (Array) this.argument.reduceExpressionTree();
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		this.argument = (Array) this.argument.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.argument = (Array) this.argument.insertValueForVariable(value, variableName);
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
		this.argument = (Array) this.argument.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		this.argument = (Array) this.argument.replaceVariableWith(oldVariable, newVariable);
		return this;
	}

}
