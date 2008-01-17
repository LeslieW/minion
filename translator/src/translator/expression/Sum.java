package translator.expression;

import java.util.ArrayList;

public class Sum extends NaryArithmeticExpression {

	
	private ArrayList<Expression> positiveArguments;
	private ArrayList<Expression> negativeArguments;
	private boolean convertToSumConstraint = false;
	
	// ============== CONSTRUCTOR =========================
	
	public Sum(ArrayList<Expression> positiveArguments,
			   ArrayList<Expression> negativeArguments) {
		
		this.positiveArguments = positiveArguments;
		this.negativeArguments = negativeArguments;
		
		if(this.positiveArguments == null)
			this.positiveArguments = new ArrayList<Expression>();
		if(this.negativeArguments == null)
			this.negativeArguments = new ArrayList<Expression>();
	}
	
	public Sum(Expression[] positiveArguments,
			   Expression[] negativeArguments) {
		
		this.positiveArguments = new ArrayList<Expression>();
		this.negativeArguments = new ArrayList<Expression>();
		
		for(int i=0; i<positiveArguments.length; i++)
			this.positiveArguments.add(positiveArguments[i]);
		
		for(int i=0; i<negativeArguments.length; i++)
			this.negativeArguments.add(negativeArguments[i]);
	}
	
	// ============ ADDITIONAL STUFF =========================
	
	public boolean willBeConvertedToASumConstraint() {
		return this.convertToSumConstraint;
	}
	
	public void setWillBeConvertedToSumConstraint(boolean turnOn) {
		this.convertToSumConstraint = turnOn;
	}
	
	// ============ INHERITED METHODS ========================
	
	public ArrayList<Expression> getPositiveArguments() {
		return this.positiveArguments;
	}
	
	public ArrayList<Expression> getNegativeArguments() {
		return this.negativeArguments;
	}

	public int[] getDomain() {
		
		int lowerBound = 0;
		int upperBound = 0;
		
		// positive arguments first
		for(int i=0; i<this.positiveArguments.size(); i++) {
			int[]  iBounds = positiveArguments.get(i).getDomain();
			lowerBound = lowerBound + iBounds[0];  // lowerBound = lowerBound + lb(E)
			upperBound = upperBound + iBounds[iBounds.length-1]; // upperBound = upperBound + ub(E)
            // we use size because it might be a sparse domain..
		}
		
		
		// then the negative arguments
		for(int i=0; i<this.negativeArguments.size(); i++) {
			int[] iBounds = negativeArguments.get(i).getDomain();
			lowerBound = lowerBound - iBounds[iBounds.length-1]; // lowerBound = lowerBound - ub(E)
			upperBound = upperBound - iBounds[0]; // upperBound = upperBound - lb(E) 
		}
		
		//System.out.println("These are the lower "+lowerBound+",  and ub: "+upperBound+" for the sum:"+this);
		return new int[] {lowerBound, upperBound};
	}

	public Expression copy() {
		ArrayList<Expression> positiveCopies = new ArrayList<Expression>();
		ArrayList<Expression> negativeCopies = new ArrayList<Expression>();
		
		for(int i=0; i<this.positiveArguments.size(); i++)
			positiveCopies.add(this.positiveArguments.get(i).copy());
		
		for(int i=0; i<this.negativeArguments.size(); i++)
			negativeCopies.add(this.negativeArguments.get(i).copy());
		
		return new Sum(positiveCopies, negativeCopies);
	}

	public int getType() {
		return SUM;
	}

	public void orderExpression() {
		this.positiveArguments = this.orderExpressionList(this.positiveArguments);
		this.negativeArguments = this.orderExpressionList(this.negativeArguments);
	}

	public String toString() {
		
		String s = "";
		
		 // first the positive arguments
		if(this.positiveArguments.size() > 0)
			s = s.concat(positiveArguments.get(0).toString());
		for(int i=1; i<this.positiveArguments.size(); i++) {
			s = s.concat(" + "+this.positiveArguments.get(i));
		}
		
		for(int i=0; i< this.negativeArguments.size(); i++)
			s = s.concat("-"+this.negativeArguments.get(i));
				
		return s;
		
		
		/*
	//	INFIX representation
	  
		if(this.positiveArguments.size() > 0)
			s = s.concat("+("+positiveArguments.get(0).toString());
		
		for(int i=1; i<this.positiveArguments.size(); i++) {
			s = s.concat(" "+this.positiveArguments.get(i));
		}
		
		s = s.concat("), -(");
		
		for(int i=0; i< this.negativeArguments.size(); i++)
			s = s.concat(" "+this.negativeArguments.get(i));
		
		
		return s.concat(")");
		*/
	}
	
	/**
	 * Ordering of sums: Sums are ordered according to their amount of 
	 * elements: a sum with a smaller amount of elements is smaller than
	 * a sum with a higher amount of elements. Sums can have positive 
	 * and negative elements. Since the + operator is smaller than 
	 * the - operator, positive elements precede negative elements and
	 * sums consisting of solely positive elements are smaller than sums
	 * containing negative elements (assuming they have the same amount
	 * of elements). Examples:<br>
	 *      a + b     <br>
	 *  <   a - b     <br>
	 *  < - a - b     <br> 
	 *  <   a + b + c <br> 
	 *  <   a + b - c <br>
	 */
	public char isSmallerThanSameType(Expression e) {
		
		// compare between positive and negative arguments!
		Sum otherSum = (Sum) e;
		int thisNoOfArguments = this.positiveArguments.size() + this.negativeArguments.size();
		int otherNoOfArguments = otherSum.positiveArguments.size() + otherSum.positiveArguments.size();
		
		//print_debug("Number of arguments: this ("+this+") has:"+thisNoOfArguments+" and other ("+otherSum+") has:"+otherNoOfArguments);
		
		if(thisNoOfArguments < otherNoOfArguments) 
			return SMALLER;
		else if(thisNoOfArguments == otherNoOfArguments) {
			// then compare recursivly
			if(this.positiveArguments.size() > 0) {
				
				// both have the same amount of positive arguments 
				if(this.positiveArguments.size() == otherSum.positiveArguments.size()) {
						
					for(int i=0; i<this.positiveArguments.size(); i++) {
						Expression thisArgument = this.positiveArguments.get(i);
						Expression otherArgument = otherSum.positiveArguments.get(i);
						
						// both arguments have the same type
						if(thisArgument.getType() == otherArgument.getType()) {
							char argumentRelation = thisArgument.isSmallerThanSameType(otherArgument);
							if(argumentRelation != EQUAL)
								return argumentRelation;
						}
						else return (thisArgument.getType() < otherArgument.getType()) ?
								SMALLER : BIGGER;
					}
					// if we reached the end of the loop without returning anything, we have the same expression
					return EQUAL;
				}
				else return (this.positiveArguments.size() > otherSum.positiveArguments.size()) ?
						SMALLER : BIGGER;
						
			} // end if: this object has positive arguments
			
			else { // this object has no positive arguments
				
				// if the other sum has positive arguments it is smaller than this object
				if(otherSum.positiveArguments.size() > 0) return BIGGER;
				
				else { // both sums have no positive elements
					for(int i=0; i<this.negativeArguments.size(); i++) {
						Expression thisArgument = this.negativeArguments.get(i);
						Expression otherArgument = otherSum.negativeArguments.get(i);
						
						// both arguments have the same type
						if(thisArgument.getType() == otherArgument.getType()) {
							char argumentRelation = thisArgument.isSmallerThanSameType(otherArgument);
							if(argumentRelation != EQUAL)
								return argumentRelation;
						}
						else return (thisArgument.getType() < otherArgument.getType()) ?
								SMALLER : BIGGER;
					}
					// if we reached the end of the loop without returning anything, we have the same expression
					return EQUAL;
					
				}
			} // end else:  this object has no positive arguments
		}
		else return BIGGER;
	}

	
	public Expression evaluate() {
		
		
		ArrayList<Integer> positiveConstants = new ArrayList<Integer>();
		ArrayList<Integer> negativeConstants = new ArrayList<Integer>();
		
	
		/** 1. first evaluate every argument (positive and negative) */
		for(int i=0; i<this.positiveArguments.size(); i++) 
			positiveArguments.add(i, positiveArguments.remove(i).evaluate());
		for(int i=0; i<this.negativeArguments.size(); i++) 
			negativeArguments.add(i, negativeArguments.remove(i).evaluate());
	
			
		/** 2. then look for constants in the arguments and collect them
		// (start loop from the tail of the list, because we are removing elements) */
		for(int i=this.positiveArguments.size()-1;i>=0; i--) {
			Expression argument = this.positiveArguments.get(i);
			if(argument.getType() == INT) 
				positiveConstants.add(((ArithmeticAtomExpression) positiveArguments.remove(i)).getConstant());
			
			else if(argument.getType() == BOOL)
				positiveConstants.add( ( (RelationalAtomExpression) positiveArguments.remove(i) 
						).toArithmeticExpression().getConstant());
		}
		for(int i=this.negativeArguments.size()-1; i>=0; i--) {
			Expression argument = this.negativeArguments.get(i);
			if(argument.getType() == INT) 
				negativeConstants.add(((ArithmeticAtomExpression) negativeArguments.remove(i)).getConstant());
			
			else if(argument.getType() == BOOL)
				negativeConstants.add( ( (RelationalAtomExpression) negativeArguments.remove(i) 
						).toArithmeticExpression().getConstant());
		}
		
		/** 3. build the resulting new constant from the identity of addition/substraction */
		int newConstant = 0;
		
		/** 4. sum up all collected constants according to +- */
		for(int i=0; i<positiveConstants.size(); i++) {
			newConstant = newConstant + positiveConstants.get(i);
		}
		for(int i=0; i<negativeConstants.size(); i++) {
			newConstant = newConstant - negativeConstants.get(i);
		}
		
		
		/** 4. merge the resulting new constant with the rest of the sum  */
		
     
		
		if(this.positiveArguments.size() == 0 && this.negativeArguments.size() == 0)
			return new ArithmeticAtomExpression(newConstant);
		
		// constant is 0: don't add it back to the list -> only do this if 
		//                we have some other elements      
		else if(newConstant == 0)
			return this; 
		
		else if(newConstant > 0){	
		    // add the constant to the beginning of the list, since it is smallest for sure
			this.positiveArguments.add(0,new ArithmeticAtomExpression(newConstant));
			return this;
		}
		else { // if newConstant < 0
			// we add the positive value to the negative list, since it will be displayed with a minus anyway
			this.negativeArguments.add(0,new ArithmeticAtomExpression(newConstant-2*newConstant));
			return this;			
		}
		
	}
	
	
	
	
	public Expression reduceExpressionTree() {
		
		for(int i=this.positiveArguments.size()-1; i>=0; i--) {
			// merge the argument
			this.positiveArguments.add(i, positiveArguments.remove(i).reduceExpressionTree());
			
			//print_debug("looking if we can merge this element to the sum:"+this.positiveArguments.get(i));
			
			if(positiveArguments.get(i) instanceof UnaryMinus) {
				UnaryMinus elem = (UnaryMinus) positiveArguments.remove(i);
				this.negativeArguments.add(elem.getArgument());
			}
			
			// if the argument is a nested addition
			if(positiveArguments.get(i).getType() == SUM) {
				Sum nestedPositiveSum = (Sum) positiveArguments.remove(i);
				
				//print_debug("we can merge this element :"+nestedPositiveSum+" to the sum:"+this);
				
				// add the positive and negative Arguments of the nested sum
				for(int j=nestedPositiveSum.positiveArguments.size()-1; j >=0; j--) {
					//print_debug("about to remove positive element '"+nestedPositiveSum.positiveArguments.get(j)+"' at position "+j+" from subSum and put it to this sum.");
					this.positiveArguments.add(nestedPositiveSum.positiveArguments.remove(j));
				}
				for(int j=nestedPositiveSum.negativeArguments.size()-1; j >=0; j--) {
					//print_debug("about to remove negative element '"+nestedPositiveSum.negativeArguments.get(j)+"' at position "+j+" from subSum and put it to this sum.");
					this.negativeArguments.add(nestedPositiveSum.negativeArguments.remove(j));
				}
			}
			 //if the argument is an expression that is negated by a unary minus 
			else if(positiveArguments.get(i).getType() == U_MINUS) {
				this.negativeArguments.add( ( (UnaryMinus) this.positiveArguments.remove(i)).getArgument());
			}
		}
		
		//print_debug("Merged positive elements: "+this.positiveArguments);
		
		for(int i=this.negativeArguments.size()-1; i>=0; i--) {
			// merge the argument
			this.negativeArguments.add(i, negativeArguments.remove(i).reduceExpressionTree());
			
			// if the argument is a nested addition
			if(negativeArguments.get(i).getType() == SUM) {
				Sum nestedNegativeSum = (Sum) negativeArguments.remove(i);
				
				// add the positive and negative Arguments of the nested sum 
				// BUT SWITCH THE OPERATORS!! (since we have a minus in front of it)
				for(int j=nestedNegativeSum.positiveArguments.size()-1; j >=0; j--) {
					this.negativeArguments.add(nestedNegativeSum.positiveArguments.remove(j));
				}
				for(int j=nestedNegativeSum.negativeArguments.size()-1; j >=0; j--) {
					this.positiveArguments.add(nestedNegativeSum.negativeArguments.remove(j));
				}
			}
			
			// if the argument is an expression that is negated by a unary minus 
			else if(negativeArguments.get(i).getType() == U_MINUS) {
				this.positiveArguments.add( ( (UnaryMinus) this.negativeArguments.remove(i)).getArgument());
			}
		}		
		
		
		// if there is only one element left, then reduce the sum to the element-expression
		if(this.positiveArguments.size() == 1 && this.negativeArguments.size() == 0) 
			return this.positiveArguments.remove(0);
		
		else if(this.positiveArguments.size() == 0 && this.negativeArguments.size() == 1)
			return new UnaryMinus(this.negativeArguments.remove(0));
		
		return this;
	}
	
	protected void print_debug(String message) {
		if(DEBUG)
			System.out.println("[ DEBUG sum ] "+message);
	}
	
	
	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.positiveArguments.size(); i++) {
			this.positiveArguments.add(i, this.positiveArguments.remove(i).insertValueForVariable(value, variableName));
		}
		for(int i=0; i<this.negativeArguments.size(); i++) {
			this.negativeArguments.add(i, this.negativeArguments.remove(i).insertValueForVariable(value, variableName));
		}
		return this;
	}
	
	
	public Expression restructure() {
	
		if(this.positiveArguments.size() ==0 || this.negativeArguments.size() ==0)
			return this;
		
		// cancellation:
		// if we find 2 equal arguments where one is in the positive list
		// and one in the negative list, remove them both
		for(int i=this.positiveArguments.size()-1; i>=0; i--) {
			Expression argument = positiveArguments.get(i);
			for(int j=this.negativeArguments.size()-1; j>=0; j--) {
				Expression negArgument = this.negativeArguments.get(j);
				if(negArgument.getType() == argument.getType()) {
					if(negArgument.isSmallerThanSameType(argument) == Expression.EQUAL) {
						//System.out.println("The two arguments are the same:"+positiveArguments.get(i)+" and "+negativeArguments.get(j));
						positiveArguments.remove(i);
						negativeArguments.remove(j);
					}
				}
			}
		}
		

		
		return this;
	}
	
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		for(int i=0; i<this.positiveArguments.size(); i++)
			this.positiveArguments.add(i,this.positiveArguments.remove(i).insertDomainForVariable(domain, variableName));
		for(int i=0; i<this.negativeArguments.size(); i++)
			this.negativeArguments.add(i,this.negativeArguments.remove(i).insertDomainForVariable(domain, variableName));
		
		return this;
	}
}
