package translator.conjureEssenceSpecification;

public class TableConstraint {

	
	String[] identifierList;
	ConstantTuple[] tupleList;
	
	
	public TableConstraint(String[] identifierList,
			               ConstantTuple[] tupleList) {
		this.identifierList = identifierList;
		this.tupleList = tupleList;
	}
	
	
	public TableConstraint copy() {
		
		String[] copiedIdentifiers = new String[this.identifierList.length];
		for(int i=0; i<this.identifierList.length; i++)
			copiedIdentifiers[i] = new String(this.identifierList[i]);
		
		ConstantTuple[] copiedTuples = new ConstantTuple[this.tupleList.length];
		for(int i=0; i<this.tupleList.length; i++)
			copiedTuples[i] = this.tupleList[i].copy();
		
		return new TableConstraint(copiedIdentifiers,
				                   copiedTuples);
	}
	
	
	public String toString() {
		String s = "table( [";
		
		if(this.identifierList.length >= 1)
			s = s.concat(identifierList[0]);
		for(int i=1; i<this.identifierList.length; i++)
			s = s.concat(","+this.identifierList[i]);
		
		s = s.concat("], [");
		
		if(this.tupleList.length >= 1)
			s = s.concat(tupleList[0].toString());
		for(int i=1; i<this.tupleList.length; i++)
			s = s.concat(", "+this.tupleList[i]);
		
		return s+"] )";
	}
	
	
}
