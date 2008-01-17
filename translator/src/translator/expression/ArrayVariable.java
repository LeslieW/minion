package translator.expression;

/**
 * Represents an array element (and not a whole array or a 
 * whole matrix). ArrayElements have an arbitrary amount of 
 * indices, that can either be purely integers or Expressions.  
 * Evaluation of array variables can result in an array element
 * that has integer indices only.
 * 
 * @author andrea
 *
 */

public class ArrayVariable implements Variable {

	private String arrayName;
	
	private int[] intIndices;
	private Expression[] exprIndices;
	
	private Domain domain;
	private boolean isSearchVariable;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	// ========== CONSTRUCTORS ==========================
	
	public ArrayVariable(String arrayName,
			             int[] intIndices,
			             Domain domain) {
		this.arrayName = arrayName;
		this.intIndices = intIndices;
		this.domain = domain;
		this.isSearchVariable = true;
		this.isNested = true;
	}
	
	
	public ArrayVariable(String arrayName,
			             Expression[] indices,
			             Domain domain) {
		
		this.arrayName = arrayName;
		this.exprIndices = indices;
		this.domain = domain;
		this.isSearchVariable = true;
		this.isNested = true;
	}
	
	
	//========== INHERITED METHODS ========================
	
	public boolean isSearchVariable() {
		return this.isSearchVariable;
	}

	public void setToSearchVariable(boolean isSearchVariable) {
		this.isSearchVariable = isSearchVariable;

	}

	public Expression copy() {
		
		String copiedArrayName = new String(this.arrayName);
		Domain copiedDomain = this.domain.copy();
		
		// array is indexed by integers
		if(this.exprIndices == null) {
			int[] copiedIndices = new int[this.intIndices.length];
			
			for(int i=0; i<copiedIndices.length; i++) 
				copiedIndices[i] = this.intIndices[i];
			
			return new ArrayVariable(copiedArrayName, 
					                 copiedIndices, 
					                 copiedDomain);
		}
		else {
			Expression[] copiedIndices = new Expression[this.exprIndices.length];
			
			for(int i=0; i<copiedIndices.length; i++) 
				copiedIndices[i] = this.exprIndices[i].copy();			
			return new ArrayVariable(copiedArrayName, 
	                 copiedIndices, 
	                 copiedDomain);
		}
	}

	public Variable evaluate() {
		if(this.exprIndices != null) {
			boolean allIndicesAreInteger = true;
			
			for(int i=0; i<this.exprIndices.length; i++) {
				exprIndices[i] = exprIndices[i].evaluate();
				if(exprIndices[i].getType() != INT)
					allIndicesAreInteger = false;
			}
			if(allIndicesAreInteger) {
				int[] newIntIndices = new int[this.exprIndices.length];
				for(int i=0; i<this.exprIndices.length;i++)
					newIntIndices[i] = ((ArithmeticAtomExpression) exprIndices[i]).getConstant();
				return new ArrayVariable(this.arrayName,
						                 newIntIndices,
						                 this.domain);
			}
		}
		
		return this;
	}

	public int[] getDomain() {
		if(this.domain.isConstantDomain())
			return ((ConstantDomain) this.domain).getRange();
		else return new int[] {Expression.LOWER_BOUND, Expression.UPPER_BOUND};
	}

	public int getType() {
		return ARRAY_VARIABLE;
	}

	public char isSmallerThanSameType(Expression e) {
		
		ArrayVariable otherVariable = (ArrayVariable) e;
		
		// this variable is indexed by integers
		if(this.intIndices != null) {
			
			// if the other variable is indexed with expressions
			if(otherVariable.intIndices == null) {
				if(otherVariable.exprIndices.length < this.intIndices.length)
					return BIGGER;
				else return SMALLER;
			}
			
			// both vars have int indices and they have the same amount of indices
			else if(otherVariable.intIndices.length == this.intIndices.length) {
				for(int i=0; i<intIndices.length; i++) {
					int diff = this.intIndices[i] - otherVariable.intIndices[i];
					if(diff < 0) return SMALLER;
					else if(diff > 0) return BIGGER;
				}
				return EQUAL;
			}
			else return (otherVariable.intIndices.length > this.intIndices.length) ?
					SMALLER : BIGGER;
			
		}
		// thius variable is indexed by expressions
		else {
			// the other variable is indexed by integers
			if(otherVariable.exprIndices == null) {
				if(this.exprIndices.length < otherVariable.exprIndices.length)
					return SMALLER;
				else return BIGGER;
			}
			// the other variable is also indexed by expressions
			else if(otherVariable.exprIndices.length == this.exprIndices.length) {
				for(int i=0; i<exprIndices.length; i++) {
					char diff = this.exprIndices[i].isSmallerThanSameType(otherVariable.exprIndices[i]);
					if(diff != EQUAL) return diff;
				}
				return EQUAL;
			}
			else return (otherVariable.exprIndices.length > this.exprIndices.length) ?
				SMALLER : BIGGER;
		}
		
		
	}

	public void orderExpression() {
		// do nothing

	}

	public Expression reduceExpressionTree() {
		return this;
	}

	
	public String toString() {
		String s = this.arrayName+"[";
		
		if(this.intIndices!=null) {
			s = s.concat(intIndices[0]+"");
			for(int i=1; i<intIndices.length; i++)
				s = s.concat(","+intIndices[i]);
		}
		else {
			s = s.concat(exprIndices[0].toString());
			for(int i=1; i<exprIndices.length; i++)
				s = s.concat(","+exprIndices[i].toString());			
		}
		return s+"]";
	}
	
	
	public String getVariableName() {
		String s = this.arrayName+"[";
		
		if(this.intIndices!=null) {
			s = s.concat(intIndices[0]+"");
			for(int i=1; i<intIndices.length; i++)
				s = s.concat(","+intIndices[i]);
		}
		else {
			s = s.concat(exprIndices[0].toString());
			for(int i=1; i<exprIndices.length; i++)
				s = s.concat(","+exprIndices[i].toString());			
		}
		return s+"]";
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
	
		if(this.exprIndices != null) {
			boolean allIndicesAreInteger = true;
			for(int i=0; i< this.exprIndices.length; i++) {
				this.exprIndices[i] = this.exprIndices[i].insertValueForVariable(value, variableName);
				if(exprIndices[i].getType() != INT)
					allIndicesAreInteger = false;
			}
		
			if(allIndicesAreInteger) {
				int[] newIntIndices = new int[this.exprIndices.length];
				for(int i=0; i<this.exprIndices.length;i++)
					newIntIndices[i] = ((ArithmeticAtomExpression) exprIndices[i]).getConstant();
				return new ArrayVariable(this.arrayName,
						newIntIndices,
						this.domain);
			}
		}
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
		return this;
	}
	
	// ====================== OTHER METHODS ========================
	
	/**
	 * @return the integer indices of the array element. if the indices are 
	 * expressions then null is returned
	 */
	public int[] getIntegerIndices() {
		return this.intIndices;
	}
	
	/**
	 * Just return the array name without the brackets and indices
	 * @return the array name (without brackets and indices)
	 */
	public String getArrayNameOnly() {
		return this.arrayName;
	}
	/**
	 * 
	 * @return the Expression indices of the array element. if the indices are 
	 * integers then null is returned
	 */
	public Expression[] getExpressionIndices() {
		return this.exprIndices;
	}
	

}
