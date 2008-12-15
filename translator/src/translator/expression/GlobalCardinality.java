package translator.expression;

import translator.solver.TargetSolver;
import java.util.ArrayList;

/**
 * GCC Global Cardinality Constraint 
 * 
 * The Generalized Cardinality Constraint (GCC) constrains the number of each value
 * that a set of variables can take.
 *
 *  gcc([primary variables], [values of interest], [capacity variables])
 *
 * For each value of interest, there must be a capacity variable, which specifies
 * the number of occurrences of the value in the primary variables.
 * 
 * (description copied from Minion help)
 * 
 * @author andrea
 *
 */

public class GlobalCardinality implements GlobalConstraint {

	private Expression variables;
	private Expression values;
	private Expression capacities;
	
	private ArrayList<Expression> valuesList = new ArrayList<Expression>();
	private ArrayList<Expression> capacitiesList = new ArrayList<Expression>();
	
	private String valuesArrayName;
	private String capacitiesArrayName;
	
	//private Expression values;
	//private Expression capacityExpression;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	/**
	 * gcc(var, [var/constlist], [var/constlist])
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
			                 Expression valuesList,
			                 Expression capacitiesList) {
		this.variables = variables;
		this.values = valuesList;
		this.capacities = capacitiesList;
	}
	
	/**
	 * gcc(var, [exprlist], [exprlist])
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
			                 ArrayList<Expression> valuesList,
			                 ArrayList<Expression> capacitiesList) {
		this.variables = variables;
		this.valuesList = valuesList;
		this.capacitiesList = capacitiesList;
	}

	
	/**
	 * gcc(var, [var/constlist], [exprlist])
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
			                 Expression valuesList,
			                 ArrayList<Expression> capacitiesList) {
		this.variables = variables;
		this.values = valuesList;
		this.capacitiesList = capacitiesList;
	}
	
	/**
	 * gcc(var, [exprlist], [var/constlist])
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
			                 ArrayList<Expression> valuesList,
			                 Expression capacitiesList) {
		this.variables = variables;
		this.valuesList = valuesList;
		this.capacities = capacitiesList;
	}
	
	
	/**
	 * gcc(var, ID, [exprlist])
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
			                 String valuesList,
			                 ArrayList<Expression> capacitiesList) {
		this.variables = variables;
		this.valuesArrayName = valuesList;
		this.capacitiesList = capacitiesList;
	}
	
	/**
	 * gcc(var, ID, [var/constlist])
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
			                 String valuesList,
			                 Expression capacitiesList) {
		this.variables = variables;
		this.valuesArrayName = valuesList;
		this.capacities = capacitiesList;
	}
	
	/**
	 * gcc(var, [exprlist], ID)
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
						     ArrayList<Expression> valuesList,
			                 String capacitiesList) {
		this.variables = variables;
		this.valuesList = valuesList;
		this.capacitiesArrayName = capacitiesList;
	}
	
	/**
	 * gcc(var, [var/constlist], ID)
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
						     Expression valuesList,
			                 String capacitiesList) {
		this.variables = variables;
		this.values = valuesList;
		this.capacitiesArrayName = capacitiesList;
	}
	
	/**
	 * gcc(var, ID, ID)
	 * @param variables
	 * @param valuesList
	 * @param capacitiesList
	 */
	public GlobalCardinality(Expression variables, 
						     String valuesList,
			                 String capacitiesList) {
		this.variables = variables;
		this.valuesArrayName = valuesList;
		this.capacitiesArrayName = capacitiesList;
	}
	
	
	
	
	public Expression[] getArguments() {
		return new Expression[] {this.variables,
				                 this.values,
				                 this.capacities} ; 
	}

	
	public Expression copy() {
		
		if(this.capacities != null && 
				this.values != null)
			return new GlobalCardinality(this.variables.copy(),
					                     this.values.copy(),
					                     this.capacities.copy());
			
		
		
		if(this.capacitiesList.size() > 0) {
			ArrayList<Expression> newCapacities = new ArrayList<Expression>();
			for(int i=0; i<this.capacitiesList.size(); i++)
				newCapacities.add(this.capacitiesList.get(i).copy());
			
			if(this.valuesList.size() > 0) {
				ArrayList<Expression> newValues = new ArrayList<Expression>();
				for(int i=0; i<this.valuesList.size(); i++)
					newValues.add(this.valuesList.get(i).copy());
				
				return new GlobalCardinality(this.variables.copy(),
						                     newValues,
						                     newCapacities);
			}
			
			else {
				return new GlobalCardinality(this.variables.copy(),
						new String(this.valuesArrayName),
	                     newCapacities);
			}	
		} // end if(capacitiesList > 0)
		String newCapacity = new String(this.capacitiesArrayName);
		if(this.valuesList.size() > 0) {
			ArrayList<Expression> newValues = new ArrayList<Expression>();
			for(int i=0; i<this.valuesList.size(); i++)
				newValues.add(this.valuesList.get(i).copy());
			
			return new GlobalCardinality(this.variables.copy(),
					                     newValues,
					                     newCapacity);
		}
		else return new GlobalCardinality(this.variables.copy(),
                new String(this.valuesArrayName),
                newCapacity);
	}

	
	public Expression evaluate() {
		this.variables = this.variables.evaluate();
		// evaluate the array lists
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).evaluate());
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).evaluate());
		
		if(this.capacities != null)
			this.capacities = this.capacities.evaluate();
		if(this.values != null)
			this.values = this.values.evaluate();
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return GLOBAL_CARDINALITY;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName)
			throws Exception {
		
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).
					       insertDomainForVariable(domain, variableName));
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).
					insertDomainForVariable(domain, variableName));
		
		this.variables = this.variables.insertDomainForVariable(domain, variableName);
		
		if(this.capacities != null)
			this.capacities = this.capacities.insertDomainForVariable(domain, variableName);
		if(this.values != null)
			this.values = this.values.insertDomainForVariable(domain, variableName);
		
		if(this.capacitiesArrayName != null && 
				this.capacitiesArrayName.equals(variableName)) {
			this.capacities = createNewVariable(variableName, domain);
		}
		else if(this.valuesArrayName != null && 
				this.valuesArrayName.equals(variableName)) {
			this.values = createNewVariable(variableName, domain);
		}
		
		return this;
	}
	
	
	private Expression createNewVariable(String variableName, Domain domain) {
		
		
		if(domain instanceof BoolDomain) {
			return new RelationalAtomExpression(new SingleVariable(variableName, domain));
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
			
			return new SimpleArray(variableName,
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
			
			return new SimpleArray(variableName,
									basicDomains,
									arrayDomain.getBaseDomain());
		}
		
		
		else 
			return new ArithmeticAtomExpression(new SingleVariable(variableName, domain));
	}
		
	
	

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).
					            insertValueForVariable(value, variableName));
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).
					insertValueForVariable(value, variableName));
		this.variables = this.variables.insertValueForVariable(value, variableName);
		
		if(this.capacities != null) 
			this.capacities = this.capacities.insertValueForVariable(value, variableName);
		if(this.values != null)
			this.values = this.values.insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).
					            insertValueForVariable(value, variableName));
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).
					insertValueForVariable(value, variableName));
		this.variables = this.variables.insertValueForVariable(value, variableName);
		
		if(this.capacities != null) 
			this.capacities = this.capacities.insertValueForVariable(value, variableName);
		if(this.values != null)
			this.values = this.values.insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isLinearExpression() {
		return false;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO Auto-generated method stub
		int f;
		return 0;
	}

	public void orderExpression() {
		this.variables.orderExpression();
	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		this.variables = this.variables.replaceVariableWith(oldVariable, newVariable);
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).replaceVariableWith(oldVariable, newVariable));
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).replaceVariableWith(oldVariable, newVariable));
		
		if(this.capacities != null)
			this.capacities = this.capacities.replaceVariableWith(oldVariable, newVariable);
		if(this.values != null) 
			this.values = this.values.replaceVariableWith(oldVariable, newVariable);
		
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName,
			Expression expression) throws Exception {
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).
						replaceVariableWithExpression(variableName, expression));
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).
					replaceVariableWithExpression(variableName, expression));
		this.variables = this.variables.replaceVariableWithExpression(variableName, expression);
		
		
		if(this.capacitiesArrayName != null && 
				this.capacitiesArrayName.equals(variableName)) {
			System.out.println("Replace variable "+variableName+" with expression "+expression);
			this.capacities = expression;
			this.capacitiesArrayName = null;
		}
		
		if(this.valuesArrayName != null && 
				this.valuesArrayName.equals(variableName)) {
			this.values = expression;
			this.valuesArrayName = null;
		}
		
		return this;
	}

	public Expression restructure() {
		this.variables = this.variables.restructure();
		for(int i=0; i<this.capacitiesList.size(); i++)
			this.capacitiesList.add(this.capacitiesList.remove(i).restructure());
		for(int i=0; i<this.valuesList.size(); i++)
			this.valuesList.add(this.valuesList.remove(i).restructure());
		
		if(this.capacities != null)
			this.capacities = this.capacities.restructure();
		if(this.values != null)
			this.values = this.values.restructure();
		
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public String toSolverExpression(TargetSolver solver) throws Exception {
		throw new Exception("Internal error. Cannot give direct solver "+
				"representation of expression '"+this
				+"' for solver "+solver.getSolverName());
	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattenedToVariable = reified;

	}

	
	public String toString() {
		
		boolean capacityExpr = (this.capacities != null);
		boolean valuesExpr = (this.values != null);
		
		boolean capacities = (this.capacitiesList.size() > 0);
		boolean values = (this.valuesList.size() > 0);
		
		String valueString = this.valuesArrayName;
		if(valuesExpr) {
			valueString = this.values.toString();
		}
		else if(values) {
			StringBuffer s = new StringBuffer("["+valuesList.get(0).toString());
			for(int i=1; i<this.valuesList.size(); i++)
				s.append(", "+valuesList.get(i));
			s.append("]");
			valueString = s.toString();
		}
		
		String capacityString = this.capacitiesArrayName;
		if(capacityExpr) {
			capacityString = this.capacities.toString();
		}
		else if(capacities) {
			StringBuffer s = new StringBuffer("["+capacitiesList.get(0).toString());
			for(int i=1; i<this.capacitiesList.size(); i++)
				s.append(", "+capacitiesList.get(i));
			s.append("]");
			capacityString = s.toString();
		}
		
		return "gcc("+this.variables+", "+valueString+", "+capacityString+")";
	}
	
	
	
	// ======================== ADDITIONAL METHODS ===============
	
	public Expression getVariables() {
		return this.variables;
	}
	
	public Expression getValues() {
		return this.values;
	}
	
	public Expression getCapacities() {
		return this.capacities;
	}
	
	public void setVariables(Expression var) {
		this.variables = var;
	}
	
	public void setCapacities(Expression c) {
		this.capacities = c;
	}
	
	public void setValues(Expression values) {
		this.values = values;
	}
}

