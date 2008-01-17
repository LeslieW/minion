package translator.expression;

/**
 * Represents arithmetic binary expressions that are composed by an 
 * operator that is NOT commutative. At the moment this includes
 * division (/) and power(^) and will contain modulo as well.
 * 
 * @author andrea
 *
 */


public class NonCommutativeArithmeticBinaryExpression implements
		BinaryArithmeticExpression {

	
	private Expression leftArgument;
	private Expression rightArgument;
	
	private int type;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	
	public NonCommutativeArithmeticBinaryExpression(Expression leftArgument,
			                                  int operator,
			                                  Expression rightArgument) {
		
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.type = operator;
	}
	
	
	//============== Interfaced Methods ==================
	
	public Expression getLeftArgument() {
		return this.getLeftArgument();
	}

	public Expression getRightArgument() {
		return this.getRightArgument();
	}

	public int[] getDomain() {
	
/*		ArrayList<Integer> rightBounds = this.rightArgument.getDomain();
 		
		int left_lb = leftBounds.get(0);
		int right_lb = rightBounds.get(0);
		
		int left_ub = leftBounds.get(leftBounds.size()-1);
		int right_ub = rightBounds.get(rightBounds.size()-1);
		
		switch(this.type) {
		
		case DIV:
			
			
		case POWER:
			
		
		}*/
		
		// TODO: enhance the bounds! The bounds could be smaller!
		return this.leftArgument.getDomain();
	}

	public Expression copy() {
		return new NonCommutativeArithmeticBinaryExpression(this.leftArgument.copy(),
													  this.type,
													  this.rightArgument.copy());
	}

	public int getType() {
		return this.type;
	}

	public void orderExpression() {
		this.leftArgument.orderExpression();
		this.rightArgument.orderExpression();

	}

	public String toString() {
		String operator = "";
		
		switch(this.type) {
		
		case DIV: operator = "/";
		case POWER: operator = "^";
		case MINUS: operator ="-";
		case PLUS: operator ="+";
		case MULT: operator="*";
		
		}
		return this.leftArgument+operator+this.rightArgument;
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		NonCommutativeArithmeticBinaryExpression otherE = (NonCommutativeArithmeticBinaryExpression) e;
		
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
		
		
		if(leftArgument.getType() == INT && rightArgument.getType() == INT) {
			
			int constant = (this.type == DIV) ?
					     ((ArithmeticAtomExpression) leftArgument).getConstant() / 
			                     ((ArithmeticAtomExpression) rightArgument).getConstant() 
			                     :
						 power(((ArithmeticAtomExpression) leftArgument).getConstant(), 
				                     ((ArithmeticAtomExpression) rightArgument).getConstant()); 			                    	 
			return new ArithmeticAtomExpression(constant);
		}
		else return this;
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
	
	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;
	}
	
	//======================== ADDITIONAL METHODS ===============================
	private int power(int base, int exponent) {
		
		int result = 1;
		
		if(exponent > 0) {
			for(int i=exponent; i>0; i++)
				result = base*result;
		}
		else if(exponent < 0){
			int divident = 1;
			for(int i=exponent; i<0; i++)
				divident = base*divident;
			result = 1/divident;
		}
		
		return result;
	}
	

	
}
