package translator.tailor.minion;

public class MinionVariableArray implements MinionArray {

	MinionAtom[] variables;
	
	public MinionVariableArray(MinionAtom[] variables) {
		this.variables = variables;
	}
	
	
	public String toString() {
		
		if(variables.length == 0) 
			return "[]";
		
		StringBuffer s = new StringBuffer("[ "+variables[0]);
		
		for(int i=1; i<this.variables.length; i++)
			s.append(", "+variables[i]);
		
		s.append("]");
		return s.toString();
	}
	
}
