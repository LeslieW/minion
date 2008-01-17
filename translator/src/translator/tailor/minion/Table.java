package translator.tailor.minion;

import translator.expression.ConstantTuple;

public class Table implements MinionConstraint {

	MinionArray array;
	MinionAtom[] variableList;
	ConstantTuple[] tuples;
	
	
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
	
	
	public String toString() {
		
		String s = "table(";
	
		String variables = "";
		
		// depending on if we have a list of variables or an array
		if(this.variableList != null) {
			variables = "[";
			for(int i=0; i<this.variableList.length; i++) {
				if(i >0) variables = variables+",";
				variables = variables.concat(variableList[i]+"");
			}
			variables = variables+"]";
		}
		else {
			variables = this.array.toString();
		}
		
		
		s = s+variables;
		
		String tuples = "";
		for(int i=0; i<this.tuples.length; i++) {
			if(i > 0) tuples = tuples+", ";
			tuples = tuples+this.tuples[i];
		}
		
		return s+",{"+tuples+"})";
	}
	
}
