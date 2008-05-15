package translator.expression;

import java.util.ArrayList;

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
	private boolean isNested = true;
	private boolean willBeReified = false;
	
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
		
		return "("+this.leftArgument+")"+operator+"("+this.rightArgument+")";
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
		
		//System.out.println("evaluating non-comm. rel. expr "+this);
		
		if(this.type == IF) {
			if(leftArgument.getType() == BOOL && rightArgument.getType() == BOOL) {
				boolean leftConstant = ((RelationalAtomExpression) leftArgument).getBool();
				boolean rightConstant = ((RelationalAtomExpression) rightArgument).getBool();
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
				rightConstant = ((RelationalAtomExpression) rightArgument).toArithmeticExpression().getConstant();
			
			if(leftArgument.getType() == INT) {
				leftConstant = ((ArithmeticAtomExpression) leftArgument).getConstant();
			}
			
			if(rightArgument.getType() == INT) {
				rightConstant = ((ArithmeticAtomExpression) rightArgument).getConstant();
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
			
			// Constant OP E
			else if(leftConstant != -11111) {
				
				//System.out.println("Evaluating non-com. expression: "+this+" with leftConstant:"+leftConstant);
				
				// Constant < E
				if(this.type == LESS) {  
					
					//System.out.println("Evaluating LESS expression. Right Argument: "+rightArgument+" has lb:"+rightArgument.getDomain()[0]+
					//		"and ub:"+rightArgument.getDomain()[1]);
					
					//  Constant  < lb(E) ==> true
					if(rightArgument.getDomain()[0] > leftConstant)
						return new RelationalAtomExpression(true);
					
					// Constant >= ub(E)  ==> false
					else if(leftConstant >= rightArgument.getDomain()[1]) {
						return new RelationalAtomExpression(false);
					}
				}
				// Constant <= E
				else if(this.type == LEQ) { 
					//  C <= lb(E) ==> true
					if(rightArgument.getDomain()[0] > leftConstant)
						return new RelationalAtomExpression(true);
					
					// Constant > ub(E)  ==> false
					else if(leftConstant > rightArgument.getDomain()[1]) {
						return new RelationalAtomExpression(false);
					}
				}
				
				// Constant > E
				else if(this.type == GREATER) { 
					// C > ub(E)
					if(rightArgument.getDomain()[1] < leftConstant)
						return new RelationalAtomExpression(true);
					
					// Constant <= lb(E)  ==> false
					else if(leftConstant <= rightArgument.getDomain()[0]) {
						return new RelationalAtomExpression(false);
					}
				}
				
				// Constant >= E
				else if(this.type == GEQ) { // C >= ub(E)
					if(rightArgument.getDomain()[1] < leftConstant)
						return new RelationalAtomExpression(true);
					
					// Constant < lb(E)  ==> false
					else if(leftConstant < rightArgument.getDomain()[0]) {
						return new RelationalAtomExpression(false);
					}
				}
			}
			
			
			// Constant OP E
			else if(rightConstant != -11111) {
				
				//System.out.println("Evaluating non-com. expression: "+this+" with rightConstant:"+rightConstant);
				
				// E > Constant
				if(this.type == GREATER) {  
					
					//  Constant  < lb(E) ==> true
					if(leftArgument.getDomain()[0] > rightConstant)
						return new RelationalAtomExpression(true);
					
					// Constant >= ub(E)  ==> false
					else if(rightConstant >= leftArgument.getDomain()[1]) {
						return new RelationalAtomExpression(false);
					}
				}
				// E >= Constant
				else if(this.type == GEQ) { 
					//  C <= lb(E) ==> true
					if(leftArgument.getDomain()[0] > rightConstant)
						return new RelationalAtomExpression(true);
					
					// Constant > ub(E)  ==> false
					else if(rightConstant > leftArgument.getDomain()[1]) {
						return new RelationalAtomExpression(false);
					}
				}
				
				// E < Constant
				else if(this.type == LESS) { 
					
					//System.out.println("Evaluating GREATER expression. left Argument: "+leftArgument+" has lb:"+leftArgument.getDomain()[0]+
							//"and ub:"+leftArgument.getDomain()[1]);
					
					// C > ub(E)
					if(leftArgument.getDomain()[1] < rightConstant)
						return new RelationalAtomExpression(true);
					
					// Constant <= lb(E)  ==> false
					else if(rightConstant <= leftArgument.getDomain()[0]) {
						return new RelationalAtomExpression(false);
					}
				}
				
				// E <= Constant
				else if(this.type == LEQ) { // C >= ub(E)
					if(leftArgument.getDomain()[1] < rightConstant)
						return new RelationalAtomExpression(true);
					
					// Constant < lb(E)  ==> false
					else if(rightConstant < leftArgument.getDomain()[0]) {
						return new RelationalAtomExpression(false);
					}
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
	
	public Expression insertValueForVariable(int value, String variableName) {
		this.leftArgument = this.leftArgument.insertValueForVariable(value, variableName);
		this.rightArgument = this.rightArgument.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		//System.out.println("Gonna insert "+value+" for "+variableName+" in left expression:"+leftArgument+" with type: "+leftArgument.getType());
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
				
		if(this.leftArgument instanceof Sum) {
			Sum leftSum = (Sum) this.leftArgument;
			// sum RELOP - E   ====>   sum + E RELOP 0
			if(this.rightArgument instanceof UnaryMinus) {
				ArrayList<Expression> positiveArgs = leftSum.getPositiveArguments();
				positiveArgs.add(((UnaryMinus) rightArgument).getArgument());
				this.rightArgument = new ArithmeticAtomExpression(0);
			}
			else if(!(this.rightArgument instanceof ArithmeticAtomExpression) &&
					!(this.rightArgument instanceof RelationalAtomExpression) ){ 
				// sum RELOP E     ====>   sum - E RELOP 0
				ArrayList<Expression> negativeArgs = leftSum.getNegativeArguments();
				negativeArgs.add(this.rightArgument.copy());
				this.rightArgument = new ArithmeticAtomExpression(0);
					
			}
			// then reduce the sum expression tree
			this.leftArgument = this.leftArgument.reduceExpressionTree();
			this.leftArgument = this.leftArgument.restructure();
			this.leftArgument.orderExpression();
			this.leftArgument = this.leftArgument.evaluate();
		}
		else if(this.rightArgument instanceof Sum) {
			Sum rightSum = (Sum) this.rightArgument;
		    // sum RELOP - E   ====>   sum + E RELOP 0
			if(this.leftArgument instanceof UnaryMinus) {
				ArrayList<Expression> positiveArgs = rightSum.getPositiveArguments();
				positiveArgs.add(((UnaryMinus) leftArgument).getArgument());
				this.leftArgument = new ArithmeticAtomExpression(0);
			}
			else if(!(this.leftArgument instanceof ArithmeticAtomExpression) &&
					!(this.leftArgument instanceof RelationalAtomExpression) ){
				// sum RELOP E     ====>   sum - E RELOP 0
				ArrayList<Expression> negativeArgs = rightSum.getNegativeArguments();
				negativeArgs.add(this.leftArgument.copy());
				//System.out.println("Restructuring right argument (sum) to:"+rightSum);
				this.leftArgument = new ArithmeticAtomExpression(0);
				//System.out.println("Restructured everything to expression:"+this);
				
			}
			// then flatten the sum
			this.rightArgument = this.rightArgument.reduceExpressionTree();
			//System.out.println("After reducing rght expr:"+this);
			this.rightArgument = this.rightArgument.restructure();
			//System.out.println("After restructuring right expr:"+this);
			this.rightArgument.orderExpression();
			//System.out.println("After oreding right expr:"+this);
			this.rightArgument = this.rightArgument.evaluate();
			//System.out.println("After evaluation, ordering etc (all):"+this);
		}		
	
		return this;
	}
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		this.leftArgument = this.leftArgument.insertDomainForVariable(domain, variableName);
		this.rightArgument = this.rightArgument.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		this.leftArgument = this.leftArgument.replaceVariableWith(oldVariable, newVariable);
		this.rightArgument = this.rightArgument.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
}
