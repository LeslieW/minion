package translator.expression;

import java.util.ArrayList;

/**
 * This class represents array variables when they are parsed since we do not 
 * know their underlying domain yet 
 * 
 * Implements AtomExpression for parser reasons (VariableArray)
 * 
 * @author andrea
 *
 */

public class SimpleArrayVariable implements Variable, AtomExpression {

	private String arrayName;
	private ArrayList<Domain> indices;
	private boolean isSearchVariable = false;
	private boolean willBeFlattened = true;
	private boolean isNested = true;
	
	public SimpleArrayVariable(String name,
							   ArrayList<Domain> indices) {
		this.arrayName = name;
		this.indices = indices;
	}
	
	// ========== INHERITED MEHTODS ======================
	
	public String getVariableName() {
		return arrayName;
	}

	public boolean isSearchVariable() {
		return this.isSearchVariable;
	}

	public void setToSearchVariable(boolean isSearchVariable) {
		this.isSearchVariable = isSearchVariable;

	}

	public Expression copy() {
		
		ArrayList<Domain> copiedIndices = new ArrayList<Domain>();
		for(int i=0; i<this.indices.size(); i++)
			copiedIndices.add(i, this.indices.get(i).copy());
	
		
		return new SimpleArrayVariable(new String(this.arrayName),
									   copiedIndices);
	}

	public Expression evaluate() {
		
		for(int i=0; i<this.indices.size(); i++)
			this.indices.add(i, this.indices.remove(i).evaluate());
		
		// TODO: if all indices are expressions/ints => return ArrayVariabke
		
		return this;
	}

	public int[] getDomain() {
		return new int[] { Expression.LOWER_BOUND, Expression.UPPER_BOUND};
	}

	public int getType() {
		return Expression.SIMPLE_ARRAY_VARIABLE;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		
		//System.out.println("Inserting domain '"+domain+"' for variable "+variableName+" in variable "+this);
		
		// update the indices first, then start with the whole expression
		for(int i=0; i<this.indices.size(); i++) {
			if(indices.get(i) instanceof SingleExpressionRange) {
				Expression indexExpression  = ((SingleExpressionRange) indices.remove(i)).getSingleExpressionRange();
				indexExpression = indexExpression.insertDomainForVariable(domain, variableName);
				indices.add(i, new SingleExpressionRange(indexExpression));
				
			}
		}
		
		
		//try {
			
		if(this.arrayName.equals(variableName)) {
			
			if(!(domain instanceof MatrixDomain))
				throw new Exception("Infeasible variable indexing: "+this+". The variable has not been declared of type matrix domain, but:"
				+domain);
	
	
			MatrixDomain arrayDomain = (MatrixDomain) domain;
			
			if(this.indices.size() != arrayDomain.getIndexDomains().length)
				throw new Exception("Infeasible variable indexing: "+this+". Only "+this.indices.size()+
						" indices have been defined while "+arrayDomain.getIndexDomains().length+" have been declared in domain "+arrayDomain
						+". Please specify an index (range) for every defined index.");
			
			
			
			boolean allIndicesAreInts = true;
			boolean allIndicesAreSingleExpressions = true;
			
			// check the type of the indices
			for(int i=this.indices.size()-1; i>=0; i--) {
				
				Domain index = indices.get(i);
				
				//System.out.println("Looking at index "+index+" with domain type:"+index.getType()+" in "+this
				//		+" which is instance of SingleExpressionRange? "+(index instanceof SingleExpressionRange));
				
				index = indices.remove(i).evaluate();
				
				if(!(index instanceof SingleIntRange)) 
					allIndicesAreInts = false;
				
				if(!(index instanceof SingleRange)) {
					//System.out.println("Index "+index+" is not a SingleRange but "+index.getClass());
					allIndicesAreSingleExpressions = false;
				}
					
				if(!(index instanceof BasicDomain)) 
					throw new Exception("Cannot translate array element '"+this+"' that is indexed by :"+index+
							". Expected an integer, expression, or range.");
				
			
				
				indices.add(i, index);
			}
			
			
			/* case: simple array variable, like x[1,2] */
			if(allIndicesAreInts) {
				
				//System.out.println("ALL indices are integers of "+this);
				
				int[] intIndices = new int[this.indices.size()];
				for(int i=intIndices.length-1; i>=0; i--) {
					intIndices[i] = ((SingleIntRange) indices.remove(i)).getSingleRange();
				}
					
				if(domain instanceof BoolDomain) 
					return new RelationalAtomExpression(new ArrayVariable(arrayName,
																		  intIndices,
																		  domain));
				
				else return new ArithmeticAtomExpression(new ArrayVariable(arrayName,
						  intIndices,
						  domain));
					
			}
			
			/* case: simple array variable with expression in index, like x[y,z+1] */
			else if(allIndicesAreSingleExpressions) {
				
				//System.out.println("All index expressions of "+this+" are expressions. ");
				
				Expression[] exprIndices = new Expression[this.indices.size()];
				
				for(int i=exprIndices.length-1; i>=0; i--) {
					//System.out.println("Index '"+indices.get(i)+"' has type :"+indices.get(i).getType());
					exprIndices[i] = ((SingleRange) indices.remove(i)).getSingleExpressionRange();
				}
				
				if(domain instanceof BoolDomain) 
					return new RelationalAtomExpression(new ArrayVariable(arrayName,
																		  exprIndices,
																		  domain));
				
				else return new ArithmeticAtomExpression(new ArrayVariable(arrayName,
						  												   exprIndices,
						  												   domain));
			}
			
			/* case: we have ranges in the index, hence we are dealing with an array */
			else {
				
				//System.out.println("The indices of "+this+" are not only expressions.. ");
				
				BasicDomain[] arrayIndices = new BasicDomain[this.indices.size()];
				
				
				
				
				for(int i=arrayIndices.length-1; i>= 0; i--) {
					BasicDomain indexDomain = (BasicDomain) this.indices.remove(i);
					
					if(indexDomain instanceof InfiniteDomain) {
						InfiniteDomain infRange = (InfiniteDomain) indexDomain;
						// ..
						if(infRange.getType() == Domain.INFINITE)
							arrayIndices[i] = (BasicDomain) arrayDomain.getIndexDomains()[i];
						
						// int(..ub)  or int(lb..)
						else {
							Expression lowerBound, upperBound;
							
							if(!(arrayDomain.getIndexDomains()[i] instanceof ExpressionRange)) 
								throw new Exception("Infeasible index domain "+arrayDomain.getIndexDomains()[i]+" declared for:"+this);
							
							ExpressionRange declaredRange  = (ExpressionRange) arrayDomain.getIndexDomains()[i];
							
							// int(..ub)
							if(infRange.getType() == Domain.INFINITE_LB){
								upperBound = infRange.getUpperBound(); 
								lowerBound = declaredRange.getLowerAndUpperBound()[0];
							}
							else {
								lowerBound = infRange.getLowerBound();
								upperBound = declaredRange.getLowerAndUpperBound()[1];
							}
							
							
							if(lowerBound.getType() == Expression.INT && 
									upperBound.getType() == Expression.INT) {								
								arrayIndices[i] = new BoundedIntRange(((ArithmeticAtomExpression) lowerBound).getConstant(),
																	((ArithmeticAtomExpression) upperBound).getConstant());
							}
							else arrayIndices[i] = new BoundedExpressionRange(lowerBound, upperBound); 
								
						}
						
					}
					
					else arrayIndices[i] = indexDomain;
				}
				
				return new IndexedArray(this.arrayName,
						                arrayIndices,
										domain);
				
			
			}
		}
		
		/*} catch(Exception e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}*/
		
		
		//System.out.println("I left the simpleArrayVariable as it is: "+this);
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		
		for(int i=0; i<this.indices.size(); i++)
			this.indices.add(i, this.indices.remove(i).insertValueForVariable(value, variableName));
		
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		//try { 
		
			//System.out.println("1 Replacing "+variableName+" in "+this+" with "+expression);
			
		if(this.arrayName.equals(variableName)) {
			
			//System.out.println("1 Replacing "+this+" in "+this+" with "+variableName);
			
			if(!(expression instanceof ConstantArray))
				return expression;
				
		
			ConstantArray constantArray = (ConstantArray) expression;
			//System.out.println("1 Replacing "+variableName+" in "+this+" with "+expression+". dim:"+constantArray.getDimension()+
			//		" vs. #indices:"+indices.size());
			
			
			if(constantArray.getDimension() != this.indices.size())
				throw new Exception("I cannot match a constant value from constant array "+variableName+" to variable "+this+
						",\n because the dimensions don't match: "+this+" is "+this.indices.size()+"-dimensionial and "
						+expression+" is "+constantArray.getDimension()+"-dimensionial.");
			
			boolean allIndicesAreInts = true;
			boolean allIndicesAreSingleExpressions = true;
			
			for(int i=this.indices.size()-1; i>=0; i--) {
				
				Domain index = indices.get(i);	
				index = indices.remove(i).evaluate();
				
				if(!(index instanceof SingleIntRange)) 
					allIndicesAreInts = false;
				
				if(!(index instanceof SingleRange)) 
					//System.out.println("Index "+index+" is not a SingleRange but "+index.getClass());
					allIndicesAreSingleExpressions = false;
						
				if(!(index instanceof BasicDomain)) 
					throw new Exception("Cannot translate array element '"+this+"' that is indexed by :"+index+
							". Expected an integer, expression, or range.");
				
				indices.add(i, index);
			}
		
			if(allIndicesAreInts) {
				int[] intIndices = new int[this.indices.size()];
				for(int i=intIndices.length-1; i>=0; i--) {
					intIndices[i] = ((SingleIntRange) indices.remove(i)).getSingleRange();
				}
				if(constantArray.getArrayDomain() != null){
					if(!(constantArray.getArrayDomain() instanceof MatrixDomain))
						throw new Exception("Infeasible domain for constant array "+variableName+": "+constantArray.getArrayDomain()+
								".\nExpected an array domain.");
				}
				
				int[] offsets = constantArray.getIndexOffsets();
				if(offsets.length > 0) {
					if(offsets.length != intIndices.length)
						throw new Exception("Domains of constant array element "+this+" and constant array "+variableName+" don't match.\n"+
								this+" is "+intIndices.length+"-dimensional and "+variableName+" is defined as "+offsets.length+"-dimensional.");
					for(int i=0; i<offsets.length; i++) {
						//System.out.println("Dereferenced "+variableName+" : index "+intIndices[i]+" - "+offsets[i]+"(offset)");
						intIndices[i] = intIndices[i]-offsets[i];
					}
				}
				//else System.out.println("No offsets for constArray:"+constantArray.getArrayName());
				
				if(constantArray instanceof ConstantVector) {
					return new ArithmeticAtomExpression( ((ConstantVector) constantArray).getElementAt(intIndices[0]) );
				}
				
				else if(constantArray instanceof ConstantMatrix) {
					//System.out.println("Dereferenced "+variableName+" : adapted index "+intIndices[0]+","+intIndices[1]);
					return new ArithmeticAtomExpression( ((ConstantMatrix) constantArray).getElementAt(intIndices[0],
																										intIndices[1]) );
				}
			}
			
			if(allIndicesAreSingleExpressions) {
				
				Expression[] indexExpressions = new Expression[this.indices.size()];
				for(int i=indexExpressions.length-1; i>=0;i--)
					indexExpressions[i] = ((SingleRange) indices.remove(i)).getSingleExpressionRange();
				
				return new ArithmeticAtomExpression(new ArrayVariable(this.arrayName, 
															          indexExpressions,
															          new InfiniteDomain()));
			}
			//	throw new Exception("Cannot translate indexing constant arrays with non-integer values, as in "+expression+" yet, sorry.");
			
			else
				throw new Exception("Cannot translate indexing constant arrays with integer ranges, as in "+expression+" yet, sorry.");
		}
		
		//} catch (Exception e) {
		//	e.printStackTrace(System.out);
		//	System.exit(1);
		//}
		//System.out.println("2 Replaced "+this+" in "+this+" with "+variableName);
		//this = this.quantifiedExpression.replaceVariableWithExpression(variableName, expression);
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattened;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void orderExpression() {
		// do nothing

	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		
		return this;
	}

	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattened = reified;

	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer(this.arrayName+"[");
		
		if(indices.size() > 0) {
			for(int i=0; i<this.indices.size()-1; i++)
				s.append(indices.get(i)+",");
		
			s.append(indices.get(indices.size()-1));
		}
		s.append("]");
		
		return s.toString();
	}
}
