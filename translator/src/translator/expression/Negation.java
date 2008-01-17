package translator.expression;

public class Negation implements UnaryRelationalExpression {

	private Expression argument;
	
	
	//============== Constructors ==================
	public Negation(Expression argument) {
		this.argument = argument;
	}
	
	
	//============== Interfaced methods  ==================
	public Expression getArgument() {
		return this.argument;
	}

	public Expression copy() {
		return new Negation(this.argument.copy());
	}

	public int getType() {
		return NEGATION;
	}

	public void orderExpression() {
		this.argument.orderExpression();
	}
	
	public String toString() {
		return "not "+argument.toString();
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		Negation otherNeg = (Negation) e;
		
		if(this.argument.getType() == otherNeg.argument.getType()) {
			return this.argument.isSmallerThanSameType(otherNeg.argument);
		}
		else return (this.argument.getType() < otherNeg.argument.getType()) ?
				SMALLER : BIGGER;		
	}
	
	public Expression evaluate() {
		
		this.argument = argument.evaluate();
		
		if(argument.getType() == BOOL) {
			boolean constant = ((RelationalAtomExpression) argument).getBool();
			return 
			 new RelationalAtomExpression(!constant);
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
}
