package translator.expression;


/**
 * Preliminary representation of the table constraint. 
 * 
 * @author angee
 *
 */

public class TableConstraint implements Expression {

	private String[] identifierList;
	private ConstantTuple[] tupleList;
	private boolean isNested;
	
	public TableConstraint(String[] identifierList,
			               ConstantTuple[] tupleList) {
		this.identifierList = identifierList;
		this.tupleList = tupleList;
		this.isNested = false;
	}
	
	
	public Expression copy() {
		String[] copiedIdentifiers = new String[this.identifierList.length];
		for(int i=0; i<this.identifierList.length; i++)
			copiedIdentifiers[i] = new String(this.identifierList[i]);
		
		ConstantTuple[] copiedTuples = new ConstantTuple[this.tupleList.length];
		for(int i=0; i<this.tupleList.length; i++)
			copiedTuples[i] = this.tupleList[i].copy();
		
		return new TableConstraint(copiedIdentifiers,
				                   copiedTuples);
	}

	public Expression evaluate() {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void orderExpression() {
		// TODO Auto-generated method stub

	}

	public Expression reduceExpressionTree() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setIsNotNested() {
		this.isNested = false;

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
