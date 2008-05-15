package translator.expression;

/**
 * Represents the global sum constraint of the form 
 * 
 * arguments[0] + arguments[1] + ... + arguments[n] RELOP result
 * 
 * where there can be an arbitrary amount of arguments. RELOP is 
 * any of the following relational operators: {<,>,>=, <=, !=, =} 
 * This representation
 * is only revelant when flattening and for interfacing to certain 
 * solvers.
 * 
 * IMPORTANT NOTE: in the sum constraint, the result is always on the
 * RIGHT side. This means that in constructing the sum, the arguments
 * are arranged (and the operator adapted) such that the result is on 
 * the right side.  
 * 
 * @author andrea
 *
 */


public class SumConstraint implements GlobalConstraint {

	private Expression[] positiveArguments;
	private Expression[] negativeArguments;
	private Expression result;
	private int relationalOperator;
	
	private boolean isNested = true;
	private boolean willBeReified = false;
	/** this flag is set to true if the sum has to be binary (but it does not mean that it
	 * actually IS binary */
	private boolean hasToBeBinary = false;

	
	// ======== CONSTRUCTOR ============================
	
	public SumConstraint(Expression[] positiveArguments,
			             Expression[] negativeArguments,
                         int relationalOperator, 
                         Expression result, 
                         boolean resultIsOnLeftSide) {
		this.positiveArguments = positiveArguments;
		this.negativeArguments = negativeArguments;
		
		if(resultIsOnLeftSide)
			this.relationalOperator = switchOperator(relationalOperator);
		else this.relationalOperator = relationalOperator;
		
		this.result = result;
	
		// just make sure we don't get a nullpointer
		if(this.positiveArguments == null)
			this.positiveArguments = new Expression[0];
		if(this.negativeArguments == null)
			this.negativeArguments = new Expression[0];
	}

	
	public SumConstraint(Expression[] positiveArguments,
			             Expression[] negativeArguments) {
		
		this.positiveArguments = positiveArguments;
		this.negativeArguments = negativeArguments;
		
		// just make sure we don't get a nullpointer
		if(this.positiveArguments == null)
			this.positiveArguments = new Expression[0];
		if(this.negativeArguments == null)
			this.negativeArguments = new Expression[0];
	}

	
	// =========== ADDITIONAL METHODS ======================
	
	public int getOperator() {
		return this.relationalOperator;
	}
	
	
	public void setResult(Expression result,
			              int relationalOperator,
			              boolean resultIsOnLeftSide) {
		
		if(resultIsOnLeftSide)
			this.relationalOperator = switchOperator(relationalOperator);
		else this.relationalOperator = relationalOperator;
		
		this.result = result;
		//this.resultIsOnLeftSide = resultIsOnLeftSide;
	}
	
	
	public boolean hasResult() {
		return (this.result != null);
	}
	
	/**
	 * This method is used to adapt the operator when we switch
	 * the result to the right side of the (in)equation. 
	 * Obviously, EQ and NEQ do not change since they are 
	 * commutative.
	 * 
	 * @param relop
	 * @return
	 */
	private int switchOperator(int relop) {
		
		switch(relop) {
		
		case Expression.LEQ:
			return Expression.GEQ;
		case Expression.GEQ:
			return Expression.LEQ;	
		case Expression.GREATER:
			return Expression.LESS;
		case Expression.LESS:
			return Expression.GREATER;
		
		default: return relop; // EQ and NEQ are commutative...
		}
		
		
	}
	
	// ========== INHERITED METHODS ====================
	
	public Expression[] getArguments() {
		Expression[] allArguments = new Expression[this.positiveArguments.length + this.negativeArguments.length];
		for(int i=0; i<this.positiveArguments.length; i++) 
			allArguments[i] = positiveArguments[i];
		
		for(int i=0; i<this.negativeArguments.length; i++) {
			allArguments[this.positiveArguments.length+i] = negativeArguments[i];
		}
		
		return allArguments;
	}

	public Expression copy() {
		Expression[] copiedPosArguments = new Expression[this.positiveArguments.length];
		for(int i=0; i<this.positiveArguments.length; i++)
			copiedPosArguments[i] = this.positiveArguments[i].copy();

		Expression[] copiedNegArguments = new Expression[this.negativeArguments.length];
		for(int i=0; i<this.negativeArguments.length; i++)
			copiedNegArguments[i] = this.negativeArguments[i].copy();
		
		return new SumConstraint(copiedPosArguments,
					             copiedNegArguments,
				                 this.relationalOperator, 
				                 this.result.copy(), 
				                 false);
	}

	public Expression evaluate() {
		
		int noZeros = 0;
		
		for(int i=0; i<this.positiveArguments.length; i++) {
			this.positiveArguments[i] = this.positiveArguments[i].evaluate();
			if(positiveArguments[i].getType() == INT) {
				if(((ArithmeticAtomExpression) positiveArguments[i]).getConstant() == 0)
					noZeros++;
			}
		}	
			
		if(noZeros > 0) {
			Expression[] newPositiveArgs = new Expression[this.positiveArguments.length - noZeros];
			int j = 0;
			for(int i=0; i<this.positiveArguments.length; i++) {
				if(this.positiveArguments[i].getType() == INT) {
					if(((ArithmeticAtomExpression) positiveArguments[i]).getConstant() != 0) {
						newPositiveArgs[j] = this.positiveArguments[i];
						j++;
					}
				}
				else {
					newPositiveArgs[j] = this.positiveArguments[i];
					j++;
				}
			}
			this.positiveArguments = newPositiveArgs;
		}
		
		
		noZeros = 0;
		
		for(int i=0; i<this.negativeArguments.length; i++) {
			this.negativeArguments[i] = this.negativeArguments[i].evaluate();
			if(negativeArguments[i].getType() == INT) {
				if(((ArithmeticAtomExpression) negativeArguments[i]).getConstant() == 0)
					noZeros++;
			}
		}
		
		// if there is a zero as argument of the sum
		if(noZeros > 0) {
			Expression[] newNegativeArgs = new Expression[this.negativeArguments.length - noZeros];
			int j = 0;
			for(int i=0; i<this.negativeArguments.length; i++) {
				if(this.negativeArguments[i].getType() == INT) {
					if(((ArithmeticAtomExpression) negativeArguments[i]).getConstant() != 0) {
						newNegativeArgs[j] = this.negativeArguments[i];
						j++;
					}
				}
				else {
					newNegativeArgs[j] = this.negativeArguments[i];
					j++;
				}
			}
			this.negativeArguments = newNegativeArgs;
		}
		
		this.result = this.result.evaluate();
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		switch(this.relationalOperator) {
		
		case Expression.EQ:
			return (this.positiveArguments.length + this.negativeArguments.length == 2) ?
					 Expression.BINARY_SUMEQ_CONSTRAINT : Expression.NARY_SUMEQ_CONSTRAINT;
		
		case Expression.LEQ:
			return (this.positiveArguments.length + this.negativeArguments.length == 2) ?
					 Expression.BINARY_SUMLEQ_CONSTRAINT : Expression.NARY_SUMLEQ_CONSTRAINT;
			
		case Expression.GEQ:
			return (this.positiveArguments.length + this.negativeArguments.length == 2) ?
					 Expression.BINARY_SUMGEQ_CONSTRAINT : Expression.NARY_SUMGEQ_CONSTRAINT;
			
		case Expression.LESS:
			return (this.positiveArguments.length + this.negativeArguments.length == 2) ?
					 Expression.BINARY_SUMLESS_CONSTRAINT : Expression.NARY_SUMLESS_CONSTRAINT;
			
		case Expression.GREATER:
			return (this.positiveArguments.length + this.negativeArguments.length == 2) ?
					 Expression.BINARY_SUMGREATER_CONSTRAINT : Expression.NARY_SUMGREATER_CONSTRAINT;
			
		case Expression.NEQ:
			return (this.positiveArguments.length + this.negativeArguments.length == 2) ?
					 Expression.BINARY_SUMNEQ_CONSTRAINT : Expression.NARY_SUMNEQ_CONSTRAINT;
			
		default: return 0; // very ugly, i know
		}
		
	}

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.positiveArguments.length; i++)
			this.positiveArguments[i] = this.positiveArguments[i].insertValueForVariable(value, variableName);
		for(int i=0; i<this.negativeArguments.length; i++)
			this.negativeArguments[i] = this.negativeArguments[i].insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		for(int i=0; i<this.positiveArguments.length; i++)
			this.positiveArguments[i] = this.positiveArguments[i].insertValueForVariable(value, variableName);
		for(int i=0; i<this.negativeArguments.length; i++)
			this.negativeArguments[i] = this.negativeArguments[i].insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		for(int i=0; i<this.positiveArguments.length; i++) {
			this.positiveArguments[i] =  this.positiveArguments[i].replaceVariableWithExpression(variableName, expression);
		}
		for(int i=0; i<this.negativeArguments.length; i++) {
			this.negativeArguments[i] = this.negativeArguments[i].replaceVariableWithExpression(variableName, expression);
		}
		return this;
	}
	
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		
		SumConstraint otherSum = (SumConstraint) e;
		int thisNoOfArguments = this.positiveArguments.length + this.negativeArguments.length;
		int otherNoOfArguments = otherSum.positiveArguments.length + otherSum.positiveArguments.length;
		
		//print_debug("Number of arguments: this ("+this+") has:"+thisNoOfArguments+" and other ("+otherSum+") has:"+otherNoOfArguments);
		
		if(thisNoOfArguments < otherNoOfArguments) 
			return SMALLER;
		else if(thisNoOfArguments == otherNoOfArguments) {
			// then compare recursivly
			if(this.positiveArguments.length > 0) {
				
				// both have the same amount of positive arguments 
				if(this.positiveArguments.length == otherSum.positiveArguments.length) {
						
					for(int i=0; i<this.positiveArguments.length; i++) {
						Expression thisArgument = this.positiveArguments[i];
						Expression otherArgument = otherSum.positiveArguments[i];
						
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
				else return (this.positiveArguments.length > otherSum.positiveArguments.length) ?
						SMALLER : BIGGER;
						
			} // end if: this object has positive arguments
			
			else { // this object has no positive arguments
				
				// if the other sum has positive arguments it is smaller than this object
				if(otherSum.positiveArguments.length > 0) return BIGGER;
				
				else { // both sums have no positive elements
					for(int i=0; i<this.negativeArguments.length; i++) {
						Expression thisArgument = this.negativeArguments[i];
						Expression otherArgument = otherSum.negativeArguments[i];
						
						// both arguments have the same type
						if(thisArgument.getType() == otherArgument.getType()) {
							char argumentRelation = thisArgument.isSmallerThanSameType(otherArgument);
							if(argumentRelation != EQUAL)
								return argumentRelation;
						}
						else return (thisArgument.getType() < otherArgument.getType()) ?
								SMALLER : BIGGER;
					}
					
					if(this.result.getType() == otherSum.result.getType()) {
						return this.result.isSmallerThanSameType(otherSum.result);
					}
					else return (this.result.getType() < otherSum.result.getType()) ?
							SMALLER : BIGGER;
					
				}
			} // end else:  this object has no positive arguments
		}
		else return BIGGER;
	
	}

	public void orderExpression() {
		for(int i=0; i<this.positiveArguments.length; i++)
			this.positiveArguments[i].orderExpression();
		
		for(int i=0; i<this.negativeArguments.length; i++)
			this.negativeArguments[i].orderExpression();
		
		this.result.orderExpression();
		
	}

	public Expression reduceExpressionTree() {
		for(int i=0; i<this.positiveArguments.length; i++)
			this.positiveArguments[i] = this.positiveArguments[i].reduceExpressionTree();
		
		for(int i=0; i<this.negativeArguments.length; i++)
			this.negativeArguments[i] = this.negativeArguments[i].reduceExpressionTree();
		
		this.result = this.result.reduceExpressionTree();
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;

	}
	
	public String toString() {
		
		
		String operator = "";
		
		switch(this.relationalOperator) {
		case IF: 	operator = "=>"; break;
		case LESS:	operator = "<"; break; 
		case LEQ: 	operator = "<="; break;
		case GEQ:   operator = ">="; break;
		case GREATER: operator = ">"; break;	
		case EQ: operator = "="; break;
		case NEQ: operator = "!="; break;
		
		}
		
		String sumPart = "";
		
		if(this.positiveArguments.length > 0)
			sumPart = ""+this.positiveArguments[0];
		
		for(int i=1; i<this.positiveArguments.length; i++)
			sumPart = sumPart.concat(" + "+positiveArguments[i]);
		
		for(int i=0; i<this.negativeArguments.length; i++)
			sumPart = sumPart.concat(" - "+negativeArguments[i]);
		
		String resultPart = this.result.toString();
		
	/*	return (resultIsOnLeftSide) ?
				resultPart+operator+sumPart:
				sumPart+operator+resultPart;
				*/
		//return (resultIsOnLeftSide) ? 
				//operator+"("+resultPart+","+sumPart+")" :
				return operator+"("+sumPart+","+resultPart+")";
	}

	
	// ==================== ADDITIONAL METHODS ============================
	
	public Expression[] getPositiveArguments() {
		return this.positiveArguments;
	}
	
	public Expression[] getNegativeArguments() {
		return this.negativeArguments;
	}
	
	public Expression getResult() {
		return this.result;
	}

	public boolean hasToBeBinary() {
		return this.hasToBeBinary;
	}
	
	public void setHasToBeBinary(boolean isBinary) {
		this.hasToBeBinary = isBinary;
	}
	
	public int getRelationalOperator() {
		return this.relationalOperator;
	}
	
	public int[] getSumDomain() {
		
		int lowerBound = 0;
		int upperBound = 0;
		
		// positive arguments first
		for(int i=0; i<this.positiveArguments.length; i++) {
			int[]  iBounds = positiveArguments[i].getDomain();
			lowerBound = lowerBound + iBounds[0];  // lowerBound = lowerBound + lb(E)
			upperBound = upperBound + iBounds[iBounds.length-1]; // upperBound = upperBound + ub(E)
            // we use size because it might be a sparse domain..
		}
		
		
		// then the negative arguments
		for(int i=0; i<this.negativeArguments.length; i++) {
			int[] iBounds = negativeArguments[i].getDomain();
			lowerBound = lowerBound - iBounds[iBounds.length-1]; // lowerBound = lowerBound - ub(E)
			upperBound = upperBound - iBounds[0]; // upperBound = upperBound - lb(E) 
		}
		
		
		return new int[] {lowerBound, upperBound};
	}
	
	public Expression restructure() {
		
	/*	if(this.positiveArguments.length ==0 || this.negativeArguments.length ==0)
			return this;
		
		// cancellation:
		// if we find 2 equal arguments where one is in the positive list
		// and one in the negative list, remove them both
		for(int i=this.positiveArguments.length-1; i>=0; i--) {
			Expression argument = positiveArguments[i];
			for(int j=this.negativeArguments.length-1; j>=0; j--) {
				if(argument.equals(negativeArguments[j])) {
					positiveArguments.remove(i);
					negativeArguments.remove(j);
				}
			}
		}*/
		

		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		for(int i=0; i<this.positiveArguments.length; i++)
			this.positiveArguments[i] = this.positiveArguments[i].insertDomainForVariable(domain, variableName);
		for(int i=0; i<this.negativeArguments.length; i++)
			this.negativeArguments[i] = this.negativeArguments[i].insertDomainForVariable(domain, variableName);
		
		return this;
	}
	
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		this.result = this.result.replaceVariableWith(oldVariable, newVariable);
		for(int i=0; i<this.positiveArguments.length; i++)
			this.positiveArguments[i] = this.positiveArguments[i].replaceVariableWith(oldVariable, newVariable);
		for(int i=0; i<this.negativeArguments.length; i++)
			this.negativeArguments[i] = this.negativeArguments[i].replaceVariableWith(oldVariable, newVariable);
		return this;
	}
	
/*	public boolean isResultOnLeftSide() {
		return this.resultIsOnLeftSide;
	}*/
}
