package translator.xcsp2ep.parser.components;

import translator.xcsp2ep.parser.PredicateManager;

public class PPredicate { 

	private String name;

	private String[] formalParameters;

	private String functionalExpression;


	public String getName() {
		return name;
	}

	public String[] getFormalParameters() {
		return formalParameters;
	}

	public String getFunctionalExpression() {
		return this.functionalExpression;
	}
	
	public PPredicate(String name, String formalParametersExpression, String functionalExpression) {
		this.name = name;
		this.formalParameters =  PredicateManager.extractFormalParameters(formalParametersExpression,true);
		this.functionalExpression = functionalExpression;
	}

	public String toString() {
		return "  predicate " + name + " with functional expression = " + functionalExpression+" and formal params:"+printParameters();
	}
	
	private String printParameters() {
		
		StringBuffer s = new StringBuffer("");
		for(int i=0; i<this.formalParameters.length; i++)
			s.append(formalParameters[i]+" ");
		
		return s.toString();
	}
}
