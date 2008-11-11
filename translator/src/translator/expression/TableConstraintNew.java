package translator.expression;

import translator.solver.TargetSolver;

public class TableConstraintNew implements RelationalExpression {

	private Expression variableArray;
	private Expression valueMatrix;
	
	private boolean isNested;
	private boolean willBeReified = false;
	/** negative tables are table constraints that contain conflicting
	 * value tuples. By default, table constraints are NOT conflicting */
	private boolean isConflictingTable = false;
	
	// ============ CONSTRUCTOR =====================
	
	public TableConstraintNew(Expression variableArray,
			   Expression constMatrix) {

		this.variableArray = variableArray;
		this.valueMatrix = constMatrix;
	}
	
	// =========== INHERITED METHODS ==================
	
	public Expression copy() {
		return new TableConstraintNew(this.variableArray.copy(),
				                      this.valueMatrix.copy());
	}

	public Expression evaluate() {
		this.valueMatrix = this.valueMatrix.evaluate();
		this.variableArray = this.variableArray.evaluate();
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return Expression.TABLE_CONSTRAINT;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName)
			throws Exception {
		this.valueMatrix = this.valueMatrix.insertDomainForVariable
		    	(domain, variableName);
		this.variableArray = this.variableArray.insertDomainForVariable
			(domain, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.valueMatrix = this.valueMatrix.insertValueForVariable
			(value, variableName);
		this.variableArray = this.variableArray.insertValueForVariable
			(value, variableName);
		return this;
	}

	public Expression insertValueForVariable(boolean value, 
											String variableName) {
		this.valueMatrix = valueMatrix.insertValueForVariable
			(value, variableName);
		this.variableArray = this.variableArray.insertValueForVariable
			(value, variableName);
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}

	public boolean isLinearExpression() {
		return false;
	}

	public boolean isNested() {
		return this.isNested;	
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO: do the comparaison at some pouint
		int f;
		return EQUAL;
	}

	public void orderExpression() {
		this.valueMatrix.orderExpression();
		this.variableArray.orderExpression();
	}

	public Expression reduceExpressionTree() {
		this.variableArray = this.variableArray.reduceExpressionTree();
		this.valueMatrix = this.valueMatrix.reduceExpressionTree();
		return this;
	}

	public Expression replaceVariableWith(Variable oldVariable,
			Variable newVariable) {
		this.variableArray = this.variableArray.replaceVariableWith
			(oldVariable, newVariable);
		this.valueMatrix = this.valueMatrix.replaceVariableWith
			(oldVariable, newVariable);
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName,
			Expression expression) throws Exception {
		this.variableArray = this.variableArray.replaceVariableWithExpression
			(variableName, expression);
		this.valueMatrix = this.valueMatrix.replaceVariableWithExpression
			(variableName, expression);
		return this;
	}

	public Expression restructure() {
		this.valueMatrix = this.valueMatrix.restructure();
		this.variableArray = this.variableArray.restructure();
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public String toSolverExpression(TargetSolver solver) throws Exception {
		throw new Exception("There is no solver representation of "
				+this+" for solver "+solver);
	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;

	}
	
	public String toString() {
		
		AtomExpression[] variableList = null;
		ConstantMatrix constMatrix = null;
		
		if(this.variableArray instanceof VariableArray) {
			variableList = ((VariableArray) this.variableArray).getVariables();
		}
		
		if(this.valueMatrix instanceof ConstantMatrix)
			constMatrix = (ConstantMatrix) this.valueMatrix;
		
		else {
			//System.out.println("Value matrix' type:"
			//		+this.valueMatrix.getClass().getSimpleName());
			return new String("TABLE");
		}
		
		int[][] tupleList = constMatrix.getElements();
		
		String conflicting = (this.isConflictingTable) ? "Conflict" : "";
		
		StringBuffer s = new StringBuffer("table"+conflicting+"( [");
		
		if(variableList.length >= 1)
			s.append(variableList[0].toString());
		for(int i=1; i<variableList.length; i++)
			s.append(","+variableList[i].toString());
		
		s.append("], [");
		
		if(tupleList.length >= 1)
			s.append("["+tupleList[0][0]);
		for(int i=1; i<tupleList.length; i++)
			for(int j=0; j<tupleList[0].length; j++) {
				s.append(", "+tupleList[i][j]);
				if(j == tupleList.length-1) 
					s.append("], ");
			}
		
		return s+"] )";
		
	}

	
	// ============ ADDITIONAL METHODS ========================
	
	public AtomExpression[] getVariables() {
		
		if(this.variableArray instanceof VariableArray) {
			return ((VariableArray) this.variableArray).variables;
		}
		
		return null;
	}
	
	public ConstantTuple[] getTupleList() {
		
		if(this.valueMatrix instanceof ConstantMatrix) {
			int[][] elements = ((ConstantMatrix) this.valueMatrix).getElements();
			ConstantTuple[] tupleList= new ConstantTuple[elements.length];
			for(int i=0; i<tupleList.length; i++) {
				tupleList[i] = new ConstantTuple(elements[i]);
			}
			return tupleList;
		}
		
		return null;
	}
	
	public boolean isConflictingTableConstraint() {
		return this.isConflictingTable;
	}
	
	
	public void setVariables(VariableArray newVars) {
		this.variableArray = newVars;
	}
	
	public VariableArray getVariableArray() {
		if(this.variableArray instanceof VariableArray)
			return (VariableArray) this.variableArray;
		else return null;
	}
}
