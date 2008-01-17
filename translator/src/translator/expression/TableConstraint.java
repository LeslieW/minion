package translator.expression;


/**
 * Preliminary representation of the table constraint. 
 * 
 * @author angee
 *
 */

public class TableConstraint implements Expression {

	private Variable[] variableList;
	private ConstantTuple[] tupleList;
	private boolean isNested;
	private boolean willBeReified = false;
	
	public TableConstraint(Variable[] identifierList,
			               ConstantTuple[] tupleList) {
		this.variableList = identifierList;
		this.tupleList = tupleList;
		this.isNested = true;
	}
	
	
	public Expression copy() {
		Variable[] copiedIdentifiers = new Variable[this.variableList.length];
		for(int i=0; i<this.variableList.length; i++)
			copiedIdentifiers[i] = (Variable) this.variableList[i].copy();
		
		ConstantTuple[] copiedTuples = new ConstantTuple[this.tupleList.length];
		for(int i=0; i<this.tupleList.length; i++)
			copiedTuples[i] = this.tupleList[i].copy();
		
		return new TableConstraint(copiedIdentifiers,
				                   copiedTuples);
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
		for(int i=0; i<this.variableList.length; i++)
			this.variableList[i] = (Variable) this.variableList[i].insertValueForVariable(value, variableName);
		
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
		String s = "table( [";
		
		if(this.variableList.length >= 1)
			s = s.concat(variableList[0].toString());
		for(int i=1; i<this.variableList.length; i++)
			s = s.concat(","+this.variableList[i].toString());
		
		s = s.concat("], [");
		
		if(this.tupleList.length >= 1)
			s = s.concat(tupleList[0].toString());
		for(int i=1; i<this.tupleList.length; i++)
			s = s.concat(", "+this.tupleList[i]);
		
		return s+"] )";
	}
	
	public boolean isGonnaBeReified() {
		return this.willBeReified;
	}
	
	public void willBeReified(boolean reified) {
		this.willBeReified = reified;
	}
	
}
