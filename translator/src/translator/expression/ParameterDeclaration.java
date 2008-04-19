package translator.expression;

import java.util.ArrayList;

public class ParameterDeclaration implements Declaration {

	private ArrayList<String> names;
	private Domain domain;
	
	public ParameterDeclaration(ArrayList<String> names,
								Domain domain) {
		this.names = names;
		this.domain = domain;
	}
	
	public ParameterDeclaration(String name,
							    Domain domain) {
		this.names = new ArrayList<String>();
		names.add(name);
		this.domain = domain;
	}
	
	//============== INHERITED METHODS ===============
	
	public ArrayList<String> getNames() {
		return this.names;
	}
	
	//============== ADDITIONAL METHODS ==============
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("given ");
		
		for(int i=0; i<this.names.size()-1; i++)
			s.append(names.get(i)+",");
		s.append(names.get(names.size()-1));
		
		return s+" : "+domain;
	}

}
