package conjureEssenceSpecification;

public class Declaration implements EssenceGlobals {
	
	/**
	 * 	 
	 * Essence' grammar: (terminals in capital letters)
	 *   declaration :==   GIVEN { parameter }'
	 *                   | WHERE { expression }'
	 *                   | LETTING { constant }'
	 *                   | FIND { variable }'
	 * 
	 */
	
	int restriction_mode;
	Parameter[] parameters;
	Expression[] expressions;
	Constant[] constants;
	DomainIdentifiers[] variables;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public Parameter[] getParameter(){
		return parameters;
	}
	public void setParameter(Parameter[] p){
		this.parameters = p;
	}
	public Expression[] getExpressions(){
		return expressions;
	}
	public void setExpressions(Expression[] exp){
		expressions=exp;
	}
	public Constant[] getConstants(){
		return constants;
	}
	public void setConstants(Constant[] c){
		this.constants=c;
	}
	public DomainIdentifiers[] getVariables(){
		return variables;
	}
	public void setVariables(DomainIdentifiers[] v){
		this.variables=v;
	}
	
	public Declaration(Parameter[] p){
		restriction_mode = GIVEN;
		this.parameters = p;
	}
	
	public Declaration(Expression[] e){
		restriction_mode = WHERE;
		this.expressions = e;
	}
	
	public Declaration(Constant[] c){
		restriction_mode = LETTING;
		this.constants = c;
	}
	
	public Declaration(DomainIdentifiers[] v){
		restriction_mode = FIND;
		this.variables = v;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case GIVEN : return "given " + parametersToString();
		case WHERE : return "where " + expressionsToString();
		case LETTING : return "letting " + constantsToString();
		case FIND : return "find " + variablesToString();
		}		
		return ""; 
	}
	
	public String parametersToString(){
		
		String output = "";
		output+=parameters[0].toString();
		for(int i = 1;i<parameters.length;i++){
			output += ", " + parameters[i].toString();
		}
		
		return output;
	}
	
	public String expressionsToString(){
		
		String output = "";
		output+=expressions[0].toString();
		for(int i = 1;i<expressions.length;i++){
			output += ", " + expressions[i].toString();
		}
		
		return output;
	}
	
	public String constantsToString(){
		
		String output = "";
		output+=constants[0].toString();
		for(int i = 1;i<constants.length;i++){
			output += ", " + constants[i].toString();
		}
		
		return output;
	}
	
	public String variablesToString(){
		
		String output = "";
		output+=variables[0].toString();
		for(int i = 1;i<variables.length;i++){
			output += ", " + variables[i].toString();
		}
		
		return output;
	}

}
