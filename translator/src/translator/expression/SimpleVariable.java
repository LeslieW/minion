package translator.expression;

/**
 * Implements AtomExpression for parser reasons
 * 
 * @author andrea
 *
 */

public class SimpleVariable implements Variable, AtomExpression {

	private String variableName;
	private boolean isSearchVariable;
	
	private int lb;
	private int ub;
	
	public SimpleVariable(String name) {
		this.variableName = name;
		this.lb = Expression.LOWER_BOUND;
		this.ub = Expression.UPPER_BOUND;
	}
	
	public String getVariableName() {
		return this.variableName;
	}

	public boolean isSearchVariable() {
		return this.isSearchVariable;
	}

	public void setToSearchVariable(boolean isSearchVariable) {
		this.isSearchVariable = isSearchVariable;
	}

	public Expression copy() {
		return new SimpleVariable(new String(this.variableName));
	}

	public Expression evaluate() {
		return this;
	}
	
	public int[] getDomain() {
		//System.out.println("Getting domain of SimpleVariable :"+this);
		return new int[] {this.lb,
						  this.ub } ;
		
	}

	public void setDomain(int lb, int ub) {
		this.lb = lb;
		this.ub = ub;
	}
	
	public int getType() {
		return Expression.SIMPLE_VARIABLE;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		
		if(this.variableName.equals(variableName)) {
			
			//System.out.println("Inserting domain '"+domain+"' for variable "+variableName+" in variable "+this);
			
			// We need to pack the variable as a RelationalAtom or Arithmetic Atom because 
			// during parsing we only create Variable types, which are not feasible standalone...
			if(domain instanceof BoolDomain) {
				return new RelationalAtomExpression(new SingleVariable(this.variableName, domain));
			}
			
			else if(domain instanceof ArrayDomain)  {
				ArrayDomain arrayDomain = (ArrayDomain) domain;
				Domain[] indexDomains = arrayDomain.getIndexDomains();
				BasicDomain[] basicDomains = new BasicDomain[arrayDomain.getIndexDomains().length];
				
				for(int i=0; i<basicDomains.length; i++) {
					
					try {
						if(indexDomains[i] instanceof BasicDomain)
							basicDomains[i] = (BasicDomain) indexDomains[i];
						else throw new Exception("Infeasible array domain: "+domain+". Cannot use domain '"+indexDomains[i]+
								"' as an index domain. Expected a range or identifier.");
						} catch (Exception e) {
							e.printStackTrace(System.out);
							System.exit(1);
					}
						
				}
				
				return new SimpleArray(this.variableName,
										basicDomains,
										arrayDomain.getBaseDomain());
			}
			
			else if(domain instanceof ConstantArrayDomain) {
				ConstantArrayDomain arrayDomain = (ConstantArrayDomain) domain;
				Domain[] indexDomains = arrayDomain.getIndexDomains();
				BasicDomain[] basicDomains = new BasicDomain[arrayDomain.getIndexDomains().length];
				
				for(int i=0; i<basicDomains.length; i++) {
					
					try {
						if(indexDomains[i] instanceof BasicDomain)
							basicDomains[i] = (BasicDomain) indexDomains[i];
						else throw new Exception("Infeasible array domain: "+domain+". Cannot use domain '"+indexDomains[i]+
								"' as an index domain. Expected a range or identifier.");
						} catch (Exception e) {
							e.printStackTrace(System.out);
							System.exit(1);
					}
						
				}
				
				return new SimpleArray(this.variableName,
										basicDomains,
										arrayDomain.getBaseDomain());
			}
			
			
			else 
				return new ArithmeticAtomExpression(new SingleVariable(this.variableName, domain));
		}
		
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		if(this.variableName.equals(variableName)) {
			return new ArithmeticAtomExpression(value);
		}
			
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		if(this.variableName.equals(variableName)) {
			return expression;
		}
		//this = this.quantifiedExpression.replaceVariableWithExpression(variableName, expression);
		return this;
	}
	
	
	public Expression replaceVariableName(String oldVariableName, String newVariableName) {
		
		if(this.variableName.equals(oldVariableName))
			this.variableName = newVariableName;
		
		return this;
	}
	

	public SingleVariable convertToSingleVariable(Domain domain) {
		
		return new SingleVariable(this.variableName, domain);
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		if(this.variableName.equals(variableName)) {
			return new RelationalAtomExpression(value);
		}
			
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return true;
	}

	public boolean isNested() {
		return true;
	}

	public char isSmallerThanSameType(Expression e) {
		SimpleVariable otherVariable = (SimpleVariable) e;
		
		int difference = otherVariable.getVariableName().compareTo(this.variableName);
		if(difference < 0) 
			return Expression.BIGGER;
		else if(difference > 0) 
			return Expression.SMALLER;
		else return Expression.EQUAL;
		
	}

	public void orderExpression() {
		// do nothing

	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		// do nothing

	}

	public void willBeFlattenedToVariable(boolean reified) {
		// do nothing

	}

	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(this.variableName.equals(oldVariable.getVariableName()))
			return newVariable;
		return this;
	}
	
	public String toString() {
		return this.variableName;
	}
	
	public boolean isLinearExpression() {
		return true;
	}
	
	public String toSolverExpression(translator.solver.TargetSolver solver) 
	throws Exception {
		
		if(solver instanceof translator.solver.Gecode) {
			return this.toSolverExpression(solver);
		}
		

		throw new Exception("Internal error. Cannot give direct solver representation of expression '"+this
			+"' for solver "+solver.getSolverName());
	}
	
}
