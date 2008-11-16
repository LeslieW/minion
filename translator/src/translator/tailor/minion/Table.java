package translator.tailor.minion;

import translator.expression.ConstantTuple;

public class Table implements MinionConstraint {

	MinionArray array;
	MinionAtom[] variableList;
	ConstantTuple[] tuples;
	boolean isNegative = false;
	
	public Table(MinionAtom[] variableList,
			     ConstantTuple[] tuples) {
		
		this.variableList = variableList;
		this.tuples = tuples;
	}
	
	public Table(MinionArray array,
			     ConstantTuple[] tuples) {
		this.array = array;
		this.tuples = tuples;
	}
	
	public Table(MinionAtom[] atoms, int[] ints) {
		
	}
	
	public void setIsNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}
	
	
	public String toString() {
		
		//System.out.println("This table is negative? "+this.isNegative);
		
		String negative = (this.isNegative) ? "negative" : "";
		
		StringBuffer s = new StringBuffer(negative+"table(");
	
		StringBuffer variables; // = new StringBuffer("");
		
		// depending on if we have a list of variables or an array
		if(this.variableList != null) {
			variables = new StringBuffer("[");
			for(int i=0; i<this.variableList.length; i++) {
				if(i >0) variables.append(",");
				variables.append(variableList[i]+"");
			}
			variables.append("]");
		}
		else {
			variables = new StringBuffer(this.array.toString());
		}
		
		
		s.append(variables);
		
		StringBuffer tuples = new StringBuffer("");
		for(int i=0; i<this.tuples.length; i++) {
			if(i > 0) tuples.append(", ");
			tuples.append(this.tuples[i]);
		}
		
		return s.append(",{"+tuples+"})").toString();
	}
	
}
