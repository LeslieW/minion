package translator.conjureEssenceSpecification;

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
	Constant[] parameterDefinitions;
	
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
	
	public Constant[] getParameterDefinitions() {
		return this.parameterDefinitions;
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
	
	public Declaration(Constant[] parameterDefs, boolean areParameters) {
		if(areParameters) {
			this.parameterDefinitions = parameterDefs;
			this.restriction_mode = EssenceGlobals.PARAM;
		}
		else {
			this.constants = parameterDefs;
			this.restriction_mode = EssenceGlobals.LETTING;
		}
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
		case PARAM: return "param "+parameterDefToString();
		}		
		return ""; 
	}
	
	
	public String parameterDefToString() {
		
		String output = "";
		for(int i = 0;i<this.parameterDefinitions.length;i++){
			if(i > 0) output = output.concat(",");
			output += parameterDefinitions[i].toString();
		}
		
		return output;
		
	}
	
	public String parametersToString(){
		
		String output = "";
		for(int i = 0;i<parameters.length;i++){
			if(i > 0) output = output.concat(",");
			output += parameters[i].toString();
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
