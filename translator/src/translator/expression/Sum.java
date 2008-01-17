package translator.expression;

import java.util.ArrayList;

public class Sum extends NaryArithmeticExpression {

	
	private ArrayList<Expression> positiveArguments;
	private ArrayList<Expression> negativeArguments;
	
	
	public Sum(ArrayList<Expression> positiveArguments,
			   ArrayList<Expression> negativeArguments) {
		
		this.positiveArguments = positiveArguments;
		this.negativeArguments = negativeArguments;
	}
	
	
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
		this.orderExpressionList(this.positiveArguments);
		this.orderExpressionList(this.negativeArguments);
	}

	public String toString() {
		
		String s = "";
		
		// first the positive arguments
		if(this.positiveArguments.size() > 0)
			s = s.concat(positiveArguments.get(0).toString());
		for(int i=0; i<this.positiveArguments.size(); i++) {
			s = s.concat(" + "+this.positiveArguments.get(i));
		}
		
		for(int i=0; i< this.negativeArguments.size(); i++)
			s = s.concat("-"+this.negativeArguments.get(i));
		
		return s;
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
		
        // constant is 0: don't add it back to the list
		if(newConstant == 0)
			return this; 
		
		else if(newConstant > 0){
		    // add the constant to the beginning of the list, since it is smallest for sure
			this.positiveArguments.add(0,new ArithmeticAtomExpression(newConstant));
			return this;
		}
		else { // if newConstant < 0
			this.negativeArguments.add(0,new ArithmeticAtomExpression(newConstant));
			return this;			
		}
		
	}
	
	
	
	
	public Expression merge() {
		
		for(int i=this.positiveArguments.size()-1; i>=0; i--) {
			// merge the argument
			this.positiveArguments.add(i, positiveArguments.remove(i).merge());
			
			// if the argument is a nested addition
			if(positiveArguments.get(i).getType() == SUM) {
				Sum nestedPositiveSum = (Sum) positiveArguments.remove(i);
				
				// add the positive and negative Arguments of the nested sum
				for(int j=nestedPositiveSum.positiveArguments.size()-1; j >=0; j--) {
					this.positiveArguments.add(nestedPositiveSum.positiveArguments.remove(i));
				}
				for(int j=nestedPositiveSum.negativeArguments.size()-1; j >=0; j--) {
					this.negativeArguments.add(nestedPositiveSum.negativeArguments.remove(i));
				}
			}
		}
		
		for(int i=this.negativeArguments.size()-1; i>=0; i--) {
			// merge the argument
			this.negativeArguments.add(i, negativeArguments.remove(i).merge());
			
			// if the argument is a nested addition
			if(negativeArguments.get(i).getType() == MINUS) {
				Sum nestedNegativeSum = (Sum) negativeArguments.remove(i);
				
				// add the positive and negative Arguments of the nested sum 
				// BUT SWITCH THE OPERATORS!! (since we have a minus in front of it)
				for(int j=nestedNegativeSum.positiveArguments.size()-1; j >=0; j--) {
					this.negativeArguments.add(nestedNegativeSum.positiveArguments.remove(i));
				}
				for(int j=nestedNegativeSum.negativeArguments.size()-1; j >=0; j--) {
					this.positiveArguments.add(nestedNegativeSum.negativeArguments.remove(i));
				}
			}
		}		
		
		return this;
	}
}
