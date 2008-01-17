package translator.conjureEssenceSpecification;

public class TableConstraint {

	
	AtomExpression[] atomExpressionList;
	ConstantTuple[] tupleList;
 	
	
	public TableConstraint(AtomExpression[] atomExpressions,
			               ConstantTuple[]  tupleList) {
		this.atomExpressionList = atomExpressions;
		this.tupleList = tupleList;
	}
	
	
	public TableConstraint copy() {
		
		AtomExpression[] copiedIdentifiers = new AtomExpression[this.atomExpressionList.length];
		for(int i=0; i<this.atomExpressionList.length; i++)
			copiedIdentifiers[i] = this.atomExpressionList[i].copy();
		
		ConstantTuple[] copiedTuples = new ConstantTuple[this.tupleList.length];
		for(int i=0; i<this.tupleList.length; i++)
			copiedTuples[i] = this.tupleList[i].copy();
		
		return new TableConstraint(copiedIdentifiers,
				                   copiedTuples);
	}
	
	
	
	public String toString() {
		String s = "table( [";
		
		if(this.atomExpressionList.length >= 1)
			s = s.concat(atomExpressionList[0].toString());
		for(int i=1; i<this.atomExpressionList.length; i++)
			s = s.concat(","+this.atomExpressionList[i]);
		
		s = s.concat("], [");
		
		if(this.tupleList.length >= 1)
			s = s.concat(tupleList[0].toString());
		for(int i=1; i<this.tupleList.length; i++)
			s = s.concat(", "+this.tupleList[i]);
		
		return s+"] )";
	}
	
	
	public AtomExpression[] getVariables() {
		return this.atomExpressionList;
	}
	
	public ConstantTuple[] getConstantTuples() {
		return this.tupleList;
	}
}
