package translator.expression;

import java.util.ArrayList;

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
		return this.leftArgument;
	}

	public Expression getRightArgument() {
		return this.rightArgument;
	}

	public int[] getDomain() {
	
		//ArrayList<Integer> rightBounds = this.rightArgument.getDomain();
		//ArrayList<Integer> leftBounds = this.rightArgument.getDomain();
		
		int[] rightBounds = this.rightArgument.getDomain();
 		int[] leftBounds = this.leftArgument.getDomain();
		
		int left_lb = leftBounds[0];
		int right_lb = rightBounds[0];
		
		int left_ub = leftBounds[1];
		int right_ub = rightBounds[1];
		
		
		int lb = LOWER_BOUND; 
		int ub = UPPER_BOUND;
		
		switch(this.type) {
		
		case DIV:
			if(left_lb >=0 && right_lb >= 0)
				lb = 1;
			else 
				lb = (left_ub > right_ub) ? -left_ub : -right_ub;
			
		
			if(left_ub >= 0 && right_ub >= 0)
				ub = left_ub/right_lb;
			else ub = (left_ub > right_ub) ? left_ub : right_ub;
			break;
			
		case POWER:
			lb = 0;
			ub = 1;
			if(right_ub > 0) {
				for(int i=0; i<right_ub; i++)
					ub = ub*left_ub;
			}
			else {
				ub = left_ub;
			}
			break;
			
		
		case MOD:
			lb = 0;
			ub = right_ub-1;
			
		}
		
		
		
		// TODO: enhance the bounds! The bounds could be smaller!
		return new int[] {lb, ub};
	}

	public Expression copy() {
		int op = type;
		return new NonCommutativeArithmeticBinaryExpression(this.leftArgument.copy(),
													  op,
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
		
		case DIV: operator = "/"; break;
		case POWER: operator = "^"; break;
		case MINUS: operator ="-"; break;
		case PLUS: operator ="+"; break;
		case MULT: operator="*"; break;
		case MOD: operator ="%";break;
		
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
      		
      			int left = ((ArithmeticAtomExpression) this.leftArgument).getConstant();
      			int right = ((ArithmeticAtomExpression) this.rightArgument).getConstant();
      			int result;
      			
      			if(this.type == MOD) 
		             result = left % right;
      			
      			else if(this.type == POWER) {
      				result = 1;
      				for(int i=1; i<=right; i++)
      					result = result*left;
      			}
      			
      			else if(this.type == DIV) {
      				result = left / right;
      			}
      			
      			else return this;
			/* int constant = (this.type == DIV) ?
					     ((ArithmeticAtomExpression) leftArgument).getConstant() / 
			                     ((ArithmeticAtomExpression) rightArgument).getConstant() 
			                     :
						 power(((ArithmeticAtomExpression) leftArgument).getConstant(), 
				                     ((ArithmeticAtomExpression) rightArgument).getConstant()); 		
			   */                  
			                     
                  
			return new ArithmeticAtomExpression(result);
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
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		this.leftArgument = this.leftArgument.replaceVariableWithExpression(variableName, expression);
		this.rightArgument = this.rightArgument.replaceVariableWithExpression(variableName, expression);
		
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
		this.leftArgument = this.leftArgument.restructure();
		this.rightArgument = this.rightArgument.restructure();
		
		return this;
	}
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		this.leftArgument = this.leftArgument.insertDomainForVariable(domain, variableName);
		this.rightArgument = this.rightArgument.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		this.leftArgument = this.leftArgument.replaceVariableWith(oldVariable, newVariable);
		this.rightArgument = this.rightArgument.replaceVariableWith(oldVariable, newVariable);
		return this;
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
	
	
	public int getOperator() {
		
		if(this.type == MOD)
			return MOD;
		
		return this.type;
	}
	
}
