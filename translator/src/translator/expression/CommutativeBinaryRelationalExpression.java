package translator.expression;

import java.util.ArrayList;

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
		case AND: operator = "/\\"; break;
		case OR: operator = "\\/"; break;
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
		else if(this.type == AND) {
			if(leftArgument.getType() == BOOL) {
				boolean leftConstant = ((RelationalAtomExpression) leftArgument).getBool();
				return (leftConstant) ?
					this.rightArgument : new RelationalAtomExpression(false);
			}
			else if(rightArgument.getType() == BOOL) {
				boolean rightConstant = ((RelationalAtomExpression) rightArgument).getBool();
				return (rightConstant) ?
					this.leftArgument : new RelationalAtomExpression(false);
			}
		}
		else if(this.type == OR) {
			if(leftArgument.getType() == BOOL) {
				boolean leftConstant = ((RelationalAtomExpression) leftArgument).getBool();
				return (leftConstant) ?
					new RelationalAtomExpression(true) : this.rightArgument;
			}
			else if(rightArgument.getType() == BOOL) {
				boolean rightConstant = ((RelationalAtomExpression) rightArgument).getBool();
				return (rightConstant) ?
					new RelationalAtomExpression(true) : this.leftArgument;
			}
		}	
			
		
		int leftConstant = -11111;
		int rightConstant = -11111;
		
		if(leftArgument.getType() == BOOL)
			leftConstant = ((RelationalAtomExpression) leftArgument).toArithmeticExpression().getConstant();
		
		if(rightArgument.getType() == BOOL)
			rightConstant = ((RelationalAtomExpression) rightArgument).toArithmeticExpression().getConstant();
		
		else if(leftArgument.getType() == INT && rightArgument.getType() == INT) {
		
			leftConstant = ((ArithmeticAtomExpression) leftArgument).getConstant();
			rightConstant = ((ArithmeticAtomExpression) rightArgument).getConstant();
		}
		
		if(leftConstant != -11111 && rightConstant != -11111) {
			switch(this.type) {
		
			case EQ: 	return new RelationalAtomExpression(leftConstant == rightConstant);
			case NEQ: 	return new RelationalAtomExpression(leftConstant != rightConstant);
			case IFF: return new RelationalAtomExpression(leftConstant == 1 &&  rightConstant ==1);
			case AND: return new RelationalAtomExpression(leftConstant == 1 && rightConstant == 1);
			case OR: return new RelationalAtomExpression(leftConstant == 1 || rightConstant == 1);
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
	
	public Expression insertValueForVariable(boolean value, String variableName) {
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
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}
	
	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;
	}
	
	
	public Expression restructure() {
	
		if(this.type == Expression.EQ) {
		
			if(this.leftArgument instanceof Sum) {
				Sum leftSum = (Sum) this.leftArgument;
			    // sum = - E   ====>   sum + E = 0
				if(this.rightArgument instanceof UnaryMinus) {
					ArrayList<Expression> positiveArgs = leftSum.getPositiveArguments();
					positiveArgs.add(((UnaryMinus) rightArgument).getArgument());
					this.rightArgument = new ArithmeticAtomExpression(0);
				}
				else if(!(this.rightArgument instanceof ArithmeticAtomExpression) &&
						!(this.rightArgument instanceof RelationalAtomExpression) ){  
					// sum = E     ====>   sum - E = 0
					ArrayList<Expression> negativeArgs = leftSum.getNegativeArguments();
					negativeArgs.add(this.rightArgument.copy());
					this.rightArgument = new ArithmeticAtomExpression(0);
					
				}
				// then flatten the sum
				this.leftArgument = this.leftArgument.reduceExpressionTree();
				this.leftArgument = this.leftArgument.restructure();
				this.leftArgument.orderExpression();
				this.leftArgument = this.leftArgument.evaluate();
				
				if(leftArgument instanceof Sum) {
					ArrayList<Expression> negArgs = ((Sum) leftArgument).getNegativeArguments();
					if(negArgs.size() > 0 && negArgs.get(0).getType() == INT){
						rightArgument = negArgs.remove(0);
					}
				}
			}
			else if(this.rightArgument instanceof Sum) {
				Sum rightSum = (Sum) this.rightArgument;
			    // sum = - E   ====>   sum + E = 0
				if(this.leftArgument instanceof UnaryMinus) {
					ArrayList<Expression> positiveArgs = rightSum.getPositiveArguments();
					positiveArgs.add(((UnaryMinus) leftArgument).getArgument());
					this.leftArgument = new ArithmeticAtomExpression(0);
				}
				else if(!(this.leftArgument instanceof ArithmeticAtomExpression) &&
						!(this.leftArgument instanceof RelationalAtomExpression) ){ 
				    // sum = E     ====>   sum - E = 0
					ArrayList<Expression> negativeArgs = rightSum.getNegativeArguments();
					negativeArgs.add(this.leftArgument.copy());
					this.leftArgument = new ArithmeticAtomExpression(0);
					
				}
				// then flatten the sum
				this.rightArgument = this.rightArgument.reduceExpressionTree();
				this.rightArgument = this.rightArgument.restructure();
				this.rightArgument.orderExpression();
				this.rightArgument = this.rightArgument.evaluate();
				
				
				if(rightArgument instanceof Sum) {
					ArrayList<Expression> negArgs = ((Sum) rightArgument).getNegativeArguments();
					if(negArgs.size() > 0 && negArgs.get(0).getType() == INT){
						leftArgument = negArgs.remove(0);
					}
				}
			}
		} // else: not EQ
		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		this.leftArgument = this.leftArgument.insertDomainForVariable(domain, variableName);
		this.rightArgument = this.rightArgument.insertDomainForVariable(domain, variableName);
		return this;
	}
	
}
