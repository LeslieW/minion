package translator.expression;

import java.util.ArrayList;

public class VariableDeclaration implements Declaration {

	/** variable names */
	private ArrayList<String> names;
	private Domain domain;
	boolean isParameter;
	
	public VariableDeclaration(ArrayList<String> names,
								Domain domain,
								boolean isParameter) {
		this.names = names;
		this.domain = domain;
		this.isParameter = isParameter;
	}
	
	public VariableDeclaration(ArrayList<String> names,
							   Domain domain) {
		this.names = names;
		this.domain = domain;
		this.isParameter = false;
	}
	
	
	
	//=======================================
	
	public ArrayList<String> getNames() {
		return this.names;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public void setIsParameter(boolean isParameter) {
		this.isParameter = isParameter;
	}
	
	public String toString() {
		String thing = (isParameter) ? "given " : "find ";
		
		StringBuffer s = new StringBuffer(thing);
		
		for(int i=0; i<this.names.size()-1; i++)
			s.append(names.get(i)+",");
		s.append(names.get(names.size()-1));
		
		return s+" : "+domain;
	}
}
