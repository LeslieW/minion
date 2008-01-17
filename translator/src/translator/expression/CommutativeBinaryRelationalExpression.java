package translator.expression;


public class CommutativeBinaryRelationalExpression implements
		BinaryRelationalExpression {

	private Expression leftArgument;
	private Expression rightArgument;
	
	private int type;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	
	public CommutativeBinaryRelationalExpression(Expression left, 
			                                   int operator,
			                                   Expression right) {
		this.leftArgument = left;
		this.rightArgument = right;
		this.type = operator;
		
	}
	
	
	//============== Interfaced Methods ==================
	public Expression getLeftArgument() {
		return this.leftArgument;
	}

	public int getOperator() {
		return this.type;
	}

	public Expression getRightArgument() {
		return this.rightArgument;
	}

	public Expression copy() {
		return new CommutativeBinaryRelationalExpression(this.leftArgument.copy(),
				                                         this.type,
				                                         this.rightArgument.copy());
	}

	public int getType() {
		return this.type;
	}

	public void orderExpression() {
		this.leftArgument.orderExpression();
		this.rightArgument.orderExpression();
		
		// does swapping really work like this in java?
		if(this.leftArgument.getType() > this.rightArgument.getType()) {
			Expression swap = this.leftArgument;
			this.leftArgument = this.rightArgument;
			this.rightArgument = swap;
		}
		else if(this.leftArgument.getType() == this.rightArgument.getType()) {
			char argumentRelation = this.leftArgument.isSmallerThanSameType(this.rightArgument);
			if(argumentRelation == BIGGER) {
				Expression swap = this.leftArgument;
				this.leftArgument = this.rightArgument;
				this.rightArgument = swap;
			}	
		}
	}
	
	public String toString() {
		
		String operator = "";
		
		switch(this.type) {
		case IFF: operator = "<=>";break;
		case EQ:  operator = "="; break;
		case NEQ: operator = "!="; break;
		}
		
		return this.leftArgument+operator+this.rightArgument;
	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	
	public char isSmallerThanSameType(Expression e) {
		
		CommutativeBinaryRelationalExpression otherE = (CommutativeBinaryRelationalExpression) e;
		
		// the left expression is has the same type
		if(this.leftArgument.getType() ==  otherE.leftArgument.getType()) {
			char leftArgRelation = this.leftArgument.isSmallerThanSameType(otherE.leftArgument);
			
			// the left expression is exactly equal
			if(leftArgRelation == EQUAL) {
				// the right expression has the same type!
				if(this.rightArgument.getType() ==  otherE.rightArgument.getType()) {
					return this.rightArgument.isSmallerThanSameType(otherE.rightArgument);
				}
				else return (this.rightArgument.getType() <  otherE.rightArgument.getType()) ?
						SMALLER : BIGGER;
			}
			else return leftArgRelation;
		}
		else return (this.leftArgument.getType() <  otherE.leftArgument.getType()) ?
				SMALLER : BIGGER;
	}

	
	public Expression evaluate() {
		
		this.leftArgument = this.leftArgument.evaluate();
		this.rightArgument = this.rightArgument.evaluate();
		
		if(this.type == IFF) {
			if(leftArgument.getType() == BOOL && rightArgument.getType() == BOOL) {
				boolean leftConstant = ((RelationalAtomExpression) leftArgument).getBool();
				boolean rightConstant = ((RelationalAtomExpression) leftArgument).getBool();
				return new RelationalAtomExpression(!(!leftConstant && !rightConstant));
			}
			
		}
		
		int leftConstant = -11111;
		int rightConstant = -11111;
		
		if(leftArgument.getType() == BOOL)
			leftConstant = ((RelationalAtomExpression) leftArgument).toArithmeticExpression().getConstant();
		
		if(rightArgument.getType() == BOOL)
			rightConstant = ((RelationalAtomExpression) leftArgument).toArithmeticExpression().getConstant();
		
		else if(leftArgument.getType() == INT && rightArgument.getType() == INT) {
		
			leftConstant = ((ArithmeticAtomExpression) leftArgument).getConstant();
			rightConstant = ((ArithmeticAtomExpression) leftArgument).getConstant();
		}
		
		if(leftConstant != -11111 && rightConstant != -11111) {
			switch(this.type) {
		
			case EQ: 	return new RelationalAtomExpression(leftConstant == rightConstant);
			case NEQ: 	return new RelationalAtomExpression(leftConstant != rightConstant);
			case IFF: return new RelationalAtomExpression(leftConstant == 1 &&  rightConstant ==1);

			}
				
		}

	return this;
		
	}
	
	public Expression reduceExpressionTree() {
		this.leftArgument = this.leftArgument.reduceExpressionTree();
		this.rightArgument = this.rightArgument.reduceExpressionTree();
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
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
	
	public void willBeReified(boolean reified) {
		this.willBeReified = reified;
	}
}
