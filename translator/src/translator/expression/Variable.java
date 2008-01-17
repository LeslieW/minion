package translator.expression;

import java.util.ArrayList;

public class Variable implements Expression {

	
	private String variableName;
	
	// in case we have no parameter-values, some 
	// domains might be composed of variables
	private ArithmeticExpression upperBound;
	private ArithmeticExpression lowerBound;
	
	// in case we have parameters
	private int ub;
	private int lb;
	
	// in case we have an arrayElement
	private int[] intIndices;
	
	// in case we have an identifier indexing
	private Expression[] indices;
	
	// in case we have a sparse domain
	private ArrayList<Integer> sparseDomain;
	
	private int internal_type;

	
	//============== Constructors ==================
	
	/**
	 * constructor for the case when parameter values
	 * are given, and all variable domain bounds 
	 * are known. 
	 * 
	 * @param variableName
	 * @param upperBound
	 * @param lowerBound
	 */
	public Variable(String variableName, 
			                        int lowerBound, 
			                        int upperBound) {
		
		this.variableName = variableName;
		this.ub = upperBound;
		this.lb = lowerBound;
		
		if(lowerBound == 0 && upperBound == 1) 
			this.internal_type = BOOL_VARIABLE;
		else 
			this.internal_type = INT_VARIABLE;
	}	
	
	/**
	 * constructur for the case when parameter values are 
	 * unknown and some domain bounds might be composed
	 * of parameters
	 * 
	 * @param variableName
	 * @param upperBound
	 * @param lowerBound
	 */
	public Variable(String variableName, 
								    ArithmeticExpression lowerBound,
								    ArithmeticExpression upperBound) {
		
		this.variableName = variableName;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.internal_type = Expression.INT_VARIABLE_PARAM;
	}

	/**
	 * Represents decision variables with sparse domains.
	 * Sparse domains may not contain unknown 
	 * parameters, because it makes it impossible to order 
	 * correctly. The sparseDomain list has to be
	 * 
	 * @param variableName
	 * @param sparseDomain
	 */
	public Variable(String variableName,
					                ArrayList<Integer> sparseDomain) {
		
		this.variableName = variableName;
		this.sparseDomain = sparseDomain;
		this.internal_type = Expression.INT_VARIABLE_SPARSE;
	
	}

	/**
	 * Represents an array-element with an
	 * arbitray amount of indices and which
	 * is defined on a bounds domain, assuming that
	 * parameters are all known
	 * 
	 * @param variableName
	 * @param indices
	 * @param lb
	 * @param ub
	 */
	public Variable(String variableName,
			        int[] indices,
			        int lb,
			        int ub) {
		this.variableName = variableName;
		this.lb = lb;
		this.ub = ub;
		this.intIndices = indices;
		
		if(lb == 0 && ub == 1) 
			this.internal_type = BOOL_VARIABLE_ARRAY_ELEM;
		else 
			this.internal_type = INT_ARRAY_ELEM;
		
	}
	

	/**
	 * Represents an array-element with an
	 * arbitray amount of indices and which
	 * is defined on a bounds domain, and with
	 * unknown parameter values
	 * 
	 * @param variableName
	 * @param indices
	 * @param lb
	 * @param ub
	 */
	public Variable(String variableName,
	        int[] indices,
	        ArithmeticExpression lb,
	        ArithmeticExpression ub) {
		this.variableName = variableName;
		this.lowerBound = lb;
		this.upperBound = ub;
		this.intIndices = indices;
		this.internal_type = INT_ARRAY_ELEM_PARAM;
	}
	
	
	/**
	 * Represents arrray elements that are defined
	 * on sparse domains
	 * 
	 * @param variableName
	 * @param indices
	 * @param sparseDomain
	 */
	public Variable(String variableName,
	        int[] indices,
	        ArrayList<Integer> sparseDomain) {
		this.variableName = variableName;
		this.sparseDomain = sparseDomain;
		this.intIndices = indices;
		this.internal_type = INT_ARRAY_ELEM_SPARSE;
	}
	
	
	/**
	 * Represents array elements that are indexed
	 * with expressions (no constant indices) and
	 * are defined on an integer bounds domain (we
	 * assume that all parameters are known)
	 * 
	 * @param variableName
	 * @param indices
	 * @param lb
	 * @param ub
	 */
	public Variable(String variableName,
			        Expression[] indices,
			        int lb,
			        int ub) {
		
		this.variableName = variableName;
		this.indices = indices;
		this.lb = lb;
		this.ub = ub;
		this.internal_type = INT_ARRAY_ELEM_INDEX;
	}
		
	
	
	/**
	 * Represents array elements that are indexed
	 * with expressions (no constant indices) and
	 * are defined on an bounds domain that might be
	 * composed of serveral expressions (not all
	 * parameters are know)
	 * 
	 * @param variableName
	 * @param indices
	 * @param lowerBound
	 * @param upperBound
	 */
	public Variable(String variableName,
				    Expression[] indices,
				    ArithmeticExpression lb,
				    ArithmeticExpression ub) {
		
		this.variableName = variableName;
		this.lowerBound = lb;
		this.upperBound = ub;
		this.indices = indices;
		this.internal_type = INT_ARRAY_ELEM_INDEX_PARAM;
		
		
	}
	
	/**
	 * Represents array elements that are indexed
	 * with expressions (no constant indices) and
	 * are defined on a sparse domain
	 * 
	 * @param variableName
	 * @param indices
	 * @param sparseDomain
	 */
	public Variable(String variableName,
    	Expression[] indices,
    	ArrayList<Integer> sparseDomain) {
		
		this.variableName = variableName;
		this.sparseDomain = sparseDomain;
		this.indices = indices;
		this.internal_type = INT_ARRAY_ELEM_INDEX_SPARSE;
	}
 //	============== Interfaced methods  ==================
	
	public Expression copy() {
		switch(this.internal_type) {
		
		case INT_VARIABLE:
			return new Variable(new String(this.variableName), 
                                                this.lb, 
                                                this.ub); 
		case INT_VARIABLE_PARAM:
			return new Variable(new String(this.variableName), 
                                             (ArithmeticExpression) this.lowerBound.copy(), 
                                             (ArithmeticExpression) this.upperBound.copy()); 
			
		case INT_VARIABLE_SPARSE:
			ArrayList<Integer> list = new ArrayList<Integer>();
			for(int i=0; i<this.sparseDomain.size(); i++)
				list.add(new Integer(sparseDomain.get(i)));
			
			return new Variable(new String(this.variableName),
					                            list);
			
		case INT_ARRAY_ELEM:
			int[] copiedIndices = new int[this.intIndices.length];
			for(int i=0; i<this.intIndices.length; i++)
				copiedIndices[i] = this.intIndices[i];
			return new Variable(new String(this.variableName),
					            copiedIndices,
					            this.lb,
					            this.ub);
	
		case INT_ARRAY_ELEM_PARAM:
			int[] copiedIndices1 = new int[this.intIndices.length];
			for(int i=0; i<this.intIndices.length; i++)
				copiedIndices1[i] = this.intIndices[i];
			return new Variable(new String(this.variableName),
		            copiedIndices1,
		            (ArithmeticExpression) this.lowerBound.copy(), 
                    (ArithmeticExpression) this.upperBound.copy());
			
		case INT_ARRAY_ELEM_SPARSE:
			ArrayList<Integer> list1 = new ArrayList<Integer>();
			for(int i=0; i<this.sparseDomain.size(); i++)
				list1.add(new Integer(sparseDomain.get(i)));	
			
			int[] copiedIndices2 = new int[this.intIndices.length];
			for(int i=0; i<this.intIndices.length; i++)
				copiedIndices2[i] = this.intIndices[i];
			return new Variable(new String(this.variableName),
		            copiedIndices2,
		            list1);
			
		case INT_ARRAY_ELEM_INDEX:
			Expression[] copiedIndices3 = new Expression[this.indices.length];
			for(int i=0; i<this.indices.length; i++)
				copiedIndices3[i] = this.indices[i].copy();
			return new Variable(new String(this.variableName),
					            copiedIndices3,
					            this.lb,
					            this.ub);			
			
		case INT_ARRAY_ELEM_INDEX_PARAM:
			Expression[] copiedIndices4 = new Expression[this.indices.length];
			for(int i=0; i<this.indices.length; i++)
				copiedIndices4[i] = this.indices[i].copy();
			return new Variable(new String(this.variableName),
		            copiedIndices4,
		            (ArithmeticExpression) this.lowerBound.copy(), 
                    (ArithmeticExpression) this.upperBound.copy());			
			
		case INT_ARRAY_ELEM_INDEX_SPARSE:
			ArrayList<Integer> list2 = new ArrayList<Integer>();
			for(int i=0; i<this.sparseDomain.size(); i++)
				list2.add(new Integer(sparseDomain.get(i)));	
			
			Expression[] copiedIndices5 = new Expression[this.indices.length];
			for(int i=0; i<this.indices.length; i++)
				copiedIndices5[i] = this.indices[i].copy();
			return new Variable(new String(this.variableName),
		            copiedIndices5,
		            list2);			
			
		default:
			return null;
		}
	}

	
	public int[] getDomain() {
		
		int[] bounds = null;
		
		// we have a sparse domain
		if(this.sparseDomain != null) {
			bounds = new int[this.sparseDomain.size()];
			for(int i=0; i<this.sparseDomain.size(); i++)
				bounds[i] = this.sparseDomain.get(i);
		}
		// parameters have to be known
		else {
			bounds = new int[] {this.lb, this.ub };
		}
		
		return bounds;
	}

	
	public int getType() {
		
		if(this.internal_type == INT_VARIABLE ||
		    this.internal_type == INT_VARIABLE_SPARSE ||
			this.internal_type == INT_VARIABLE_PARAM)
			return INT_VAR;
		
		else return INT_ARRAY_VAR;
	}

	
	public void orderExpression() {
		// do nothing
	}
	
	public String toString() {
		
		// pures decision variable
		if(indices == null && intIndices == null) {
			return this.variableName;
		}
		// expression indices
		else if(indices != null) {
			String s = this.variableName;
			s = s.concat("["+indices[0].toString());
			for(int i=1; i<indices.length; i++)
				s = s.concat(","+indices[i].toString());
			s = s.concat("]");
			return s;
		}
		else { // integer indices
			String s = this.variableName;
			s = s.concat("["+intIndices[0]);
			for(int i=1; i<intIndices.length; i++)
				s = s.concat(","+intIndices[i]);
			s = s.concat("]");
			return s;
		}
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		
		Variable otherVariable = (Variable) e;
		
		// we have decision variable
		if(this.getType() == INT_VAR) { // returns <0 if object is lexicographically smaller	
			int lexComparison = this.variableName.compareTo(otherVariable.variableName);
		
			if(lexComparison == 0) return EQUAL;
			else return (lexComparison < 0) ?
					SMALLER : BIGGER;
		}
		// we have an array element
		else {
			
			if(this.indices != null) {
				if(otherVariable.indices != null) {
					// this object has less indices than the other
					if(this.indices.length < otherVariable.indices.length)
						return SMALLER;
					
					// this object has the same amount of indices than the other
					else if(this.indices.length == otherVariable.indices.length) {
						for(int i=0; i< this.indices.length; i++) {
							if(this.indices[i].getType() == otherVariable.indices[i].getType()) {
								char indexIrelation = this.indices[i].isSmallerThanSameType(otherVariable.indices[i]);
								// if the indices are not equal, break the loop and return
								if(indexIrelation != EQUAL)
									return indexIrelation;
							}
							else return (this.indices[i].getType() < otherVariable.indices[i].getType()) ?
									SMALLER : BIGGER;
						}
//						 if we terminate the for-loop we have exactly the same type/expression
						return EQUAL;
					}
					
					// this object has more indices than the other
					else return BIGGER;
				} // end if: otherVariable.indices != null
				else {
					// the other variable has integers as indices, we have composed expressions/variables
					return BIGGER; 
				}
			} // end if: indices are not null
			else {
				// the other variable has composed expressions or variables as indices
				if(otherVariable.intIndices == null)
					return SMALLER;
				
				else { // both this and the other have integer indices
					
					if(this.intIndices.length == otherVariable.intIndices.length) {
						
						for(int i=0; i<this.intIndices.length; i++) {
							if(this.intIndices[i] != otherVariable.intIndices[i])
								return (this.intIndices[i] < otherVariable.intIndices[i]) ?
										SMALLER : BIGGER;
						}
						// if we terminate the for-loop we have exactly the same type/expression
						return EQUAL;
					}
					else return (this.intIndices.length < otherVariable.intIndices.length) ?
						         SMALLER : BIGGER;
					
				} // end else: the other variable has intIndices, just like this object
			}
		} // end else: this is an array Element
		
	}
	
	
	public Expression evaluate() {
		
		// we have a decision variable or an integer-indexed variable array element
		if(this.indices == null) {
			return this;
		}
		
		else {
			ArrayList<Integer> integerIndices = new ArrayList<Integer>();
			
			for(int i=0; i<this.indices.length; i++) {
				Expression evaluatedIndex = indices[i].evaluate();
				
				if(evaluatedIndex.getType() == INT) {
					int intIndex = ((ArithmeticAtomExpression) evaluatedIndex).getConstant();
					indices[i] = new ArithmeticAtomExpression(intIndex);
					integerIndices.add(intIndex);
				}
			}
			
			// every index has been evaluated to an integer, so convert it to an
			// integer-indexed array element
			if(integerIndices.size() == this.indices.length) {
				int[] newIntIndices = new int[this.indices.length];
				for(int i=integerIndices.size()-1; i>=0; i--)
					newIntIndices[i] = integerIndices.remove(i);
				return (this.lowerBound != null) ? 
						new Variable(this.variableName, 
						            newIntIndices,
						            this.lowerBound,
						            this.upperBound) 
				  :
					new Variable(this.variableName, 
				            newIntIndices,
				            this.lb,
				            this.ub);
			}
			else return this;
		}
		
	}
	
	public Expression merge() {
		return this;
	}
	
	
	//=============== ADDITIONAL METHODS =================
	
	public int getInternalType() {
		return this.internal_type;
	}
	
	

	
}
