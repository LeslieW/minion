package translator.expression;

import java.util.ArrayList;

/**
 * Preliminary representation of the table constraint. Will be extended as soon as 
 * there is a tuple-type  
 * 
 * @author angee
 *
 */

public class TableConstraint implements RelationalExpression {

	private Variable[] variableList;
	private ConstantTuple[] tupleList;
	private boolean isNested;
	private boolean willBeReified = false;
	/** negative tables are table constraints that contain conflicting
	 * value tuples. By default, table constraints are NOT conflicting */
	private boolean isConflictingTable = false;
	
	
	public TableConstraint(Variable[] identifierList,
			               ConstantTuple[] tupleList) {
		this.variableList = identifierList;
		this.tupleList = tupleList;
		this.isNested = true;
	}
	
	
	public TableConstraint(ArrayList<Variable> idents,
			               ArrayList<ConstantTuple> tuples) {
		
		this.variableList = new Variable[idents.size()];
		for(int i=idents.size()-1; i>= 0; i--)
			variableList[i] = idents.remove(i);
		
		this.tupleList = new ConstantTuple[tuples.size()];
		for(int i=tuples.size()-1; i>=0; i--)
			tupleList[i] = tuples.remove(i);
	}
	
	// ======== INHERITED METHODS =======================
	
	public Expression copy() {
		Variable[] copiedIdentifiers = new Variable[this.variableList.length];
		for(int i=0; i<this.variableList.length; i++)
			copiedIdentifiers[i] = (Variable) this.variableList[i].copy();
		
		ConstantTuple[] copiedTuples = new ConstantTuple[this.tupleList.length];
		for(int i=0; i<this.tupleList.length; i++)
			copiedTuples[i] = this.tupleList[i].copy();
		
		TableConstraint table =  new TableConstraint(copiedIdentifiers,
				                   copiedTuples);
		table.setToConflictingTableConstraint(this.isConflictingTable);
		return table;
	}

	public Expression evaluate() {
		for(int i=0;i<this.variableList.length; i++ )
			this.variableList[i] = (Variable) this.variableList[i].evaluate();
			
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return TABLE_CONSTRAINT;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.variableList.length; i++) {
			Expression e = this.variableList[i].insertValueForVariable(value, variableName);
			if(e instanceof Variable) {
				this.variableList[i] = (Variable) e;
			}
			else {
				if(e.getType() == INT) {
					int v = ((ArithmeticAtomExpression) e).getConstant();
					for(int j=0; j<this.tupleList.length; j++) {
						ConstantTuple valueTuple = tupleList[j];
						// if the const value in tuple representing the variable at position i equals the value
						if(valueTuple.tupleElements[i] == v) {
							ArrayList<Expression> eqConstraints= new ArrayList<Expression>();
							for(int k=0; k<variableList.length; k++) {
								if(k != i) {
									if(!this.isConflictingTable) {
										eqConstraints.add(new CommutativeBinaryRelationalExpression(
											                      new ArithmeticAtomExpression(variableList[k]),
											                      EQ,
											                      new ArithmeticAtomExpression(valueTuple.tupleElements[k]))
									                  );
									}
									else {
										eqConstraints.add(new CommutativeBinaryRelationalExpression(
							                      new ArithmeticAtomExpression(variableList[k]),
							                      NEQ,
							                      new ArithmeticAtomExpression(valueTuple.tupleElements[k]))
					                  );
									}
								}
							} 
					
							return new Conjunction(eqConstraints);
						
						}
						
					}
					// if we reach this point, then the constant tuple list did not have the value we inserted the variable for
					if(!this.isConflictingTable)
						return new RelationalAtomExpression(false);
					else 
						return new RelationalAtomExpression(true);
				}
			}
			//this.variableList[i] = (Variable) this.variableList[i].insertValueForVariable(value, variableName);
		}
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		for(int i=0; i<this.variableList.length; i++)
			this.variableList[i] = (Variable) this.variableList[i].insertValueForVariable(value, variableName);
		
		return this;
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		for(int i=0; i<this.variableList.length; i++) {
			Expression e = this.variableList[i].replaceVariableWithExpression(variableName, expression);
			if(e instanceof Variable) 
				this.variableList[i] = (Variable) e;
			
			else {
				try {
					throw new Exception("Replacing variable '"+variableName+"' with infeasible expression '"+expression+
							"' that modifies table-constraint-array into:"+e+". Expected variable type.");
				} catch (Exception exc) {
					exc.printStackTrace(System.out);
					System.exit(1);
				}
			}
		}
		return this;
	}
	
	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		TableConstraint otherTableConstraint = (TableConstraint) e;
		
		if(this.variableList.length == otherTableConstraint.variableList.length) {
			
			if(this.tupleList.length == otherTableConstraint.tupleList.length) {
				
				// compare the variable list
				for(int i=0; i<this.variableList.length; i++) {
					if(this.variableList[i].getType() == otherTableConstraint.variableList[i].getType()) {
						
						// if both variables DO NOT have the same name
						if(variableList[i].getVariableName().compareTo(
								        otherTableConstraint.variableList[i].getVariableName()) != 0) {
						
							return (variableList[i].getVariableName().compareTo(
							        otherTableConstraint.variableList[i].getVariableName()) < 0) ?
							        		SMALLER : BIGGER;
						}
						
					}
					else return (this.variableList[i].getType() < otherTableConstraint.variableList[i].getType()) ?
							SMALLER : BIGGER;
				}// if we get out of the loop without returning something, the table is the same..
				return EQUAL;
				
				
			} // end if: tuple list has the same length
			else return (this.tupleList.length < otherTableConstraint.tupleList.length) ?
					SMALLER : BIGGER;
			
			
		} // end if: variable list has the same length
		else return (this.variableList.length < otherTableConstraint.variableList.length) ?
			 SMALLER : BIGGER;
		
	}

	public void orderExpression() {
		// do nothing
	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public String toString() {
		
		String conflicting = (this.isConflictingTable) ? "Conflict" : "";
		
		StringBuffer s = new StringBuffer("table"+conflicting+"( [");
		
		if(this.variableList.length >= 1)
			s.append(variableList[0].toString());
		for(int i=1; i<this.variableList.length; i++)
			s.append(","+this.variableList[i].toString());
		
		s.append("], [");
		
		if(this.tupleList.length >= 1)
			s.append(tupleList[0].toString());
		for(int i=1; i<this.tupleList.length; i++)
			s.append(", "+this.tupleList[i]);
		
		return s+"] )";
	}
	
	
	public StringBuffer toStringBuffer() {
		
		String conflicting = (this.isConflictingTable) ? "Conflict" : "";
		
		StringBuffer s = new StringBuffer("table"+conflicting+"( [");
		
		if(this.variableList.length >= 1)
			s.append(variableList[0].toString());
		for(int i=1; i<this.variableList.length; i++)
			s.append(","+this.variableList[i].toString());
		
		s.append("], [");
		
		if(this.tupleList.length >= 1)
			s.append(tupleList[0].toString());
		for(int i=1; i<this.tupleList.length; i++)
			s.append(", "+this.tupleList[i]);
		
		 s.append("] )");
		 return s;
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
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		for(int i=0; i<this.variableList.length; i++)
			this.variableList[i] = (Variable) this.variableList[i].replaceVariableWith(oldVariable, newVariable);
		return this;
	}
	
	// ============= ADDITIONAL METHODS ====================================
	
	
	public Variable[] getVariables() {
		return this.variableList;
	}
	
	public ConstantTuple[] getTupleList() {
		return this.tupleList;
	}
	
	public boolean isConflictingTableConstraint() {
		return this.isConflictingTable;
	}
	
	public void setToConflictingTableConstraint(boolean isConflicting) {
		this.isConflictingTable = isConflicting;
	}
}
