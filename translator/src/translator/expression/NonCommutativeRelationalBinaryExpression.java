package translator.expression;

/**
 * Contains any boolean relation that is NOT commutative. 
 * Possible operators are: IMPLIES, >, <, >=, <=, lex*, operators 
 * 
 * @author andrea
 *
 */

public class NonCommutativeRelationalBinaryExpression implements 
                        BinaryRelationalExpression {

	private Expression leftArgument;
	private Expression rightArgument;
	
	private int type;
	
	//============== Constructors ==================
	public NonCommutativeRelationalBinaryExpression(Expression leftArgument,
											 int operator,
	                                         Expression rightArgument) {
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.type = operator;
	}
	
	//============== Interfaced Methods ==================
	public Expression getLeftArgument() {
		return this.leftArgument;
	}

	public Expression getRightArgument() {
		return this.rightArgument;
	}

	public int getOperator() {
		return this.type;
	}
	
	public Expression copy() {
		return new NonCommutativeRelationalBinaryExpression(this.leftArgument.copy(),
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
		case IF: 	operator = "=>"; break;
		case LESS:	operator = "<"; break; 
		case LEQ: 	operator = "<="; break;
		case GEQ:   operator = ">="; break;
		case GREATER: operator = ">"; break;	
		case LEX_LESS:	operator = "lex<"; break; 
		case LEX_LEQ: 	operator = "lex<="; break;
		case LEX_GEQ:   operator = "lex>="; break;
		case LEX_GREATER: operator = "lex>"; break;	
		}
		
		return this.leftArgument+operator+this.rightArgument;
	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	
	public char isSmallerThanSameType(Expression e) {
		
		NonCommutativeRelationalBinaryExpression otherE = (NonCommutativeRelationalBinaryExpression) e;
		
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
		
		this.leftArgument =  this.leftArgument.evaluate();
		this.rightArgument = this.rightArgument.evaluate();
		
		
		if(this.type == IF) {
			if(leftArgument.getType() == BOOL && rightArgument.getType() == BOOL) {
				boolean leftConstant = ((RelationalAtomExpression) leftArgument).getBool();
				boolean rightConstant = ((RelationalAtomExpression) leftArgument).getBool();
				return new RelationalAtomExpression(!leftConstant || rightConstant);
			}
			// T => E  ----> E
			// F => E  ----> T
			else if(leftArgument.getType() == BOOL) {
				boolean guard = ((RelationalAtomExpression) leftArgument).getBool();
				return (guard) ?
						this.rightArgument :
							new RelationalAtomExpression(true);
			}
			
		}
		// all other operators (that are arithmetic)
		else {
			
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
			
				case LESS: 	return new RelationalAtomExpression(leftConstant < rightConstant);
				case LEQ: 	return new RelationalAtomExpression(leftConstant <= rightConstant);
				case GREATER: return new RelationalAtomExpression(leftConstant > rightConstant);
				case GEQ: 	return new RelationalAtomExpression(leftConstant >= rightConstant);
			
				case LEX_LESS: 	return new RelationalAtomExpression(leftConstant < rightConstant);
				case LEX_LEQ: 	return new RelationalAtomExpression(leftConstant <= rightConstant);
				case LEX_GREATER: return new RelationalAtomExpression(leftConstant > rightConstant);
				case LEX_GEQ: 	return new RelationalAtomExpression(leftConstant >= rightConstant);
				}
					
			}
		}
	
		return this;
	}
	
	public Expression reduceExpressionTree() {
		this.leftArgument = this.leftArgument.reduceExpressionTree();
		this.rightArgument = this.rightArgument.reduceExpressionTree();
		return this;
	}
	
}
