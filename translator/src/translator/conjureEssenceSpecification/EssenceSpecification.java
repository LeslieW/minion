package translator.conjureEssenceSpecification;


public class EssenceSpecification {

	/**
	 * Essence' grammar: (terminals in capital letters)
	 *   model :==   [ {declaration}' ]
	 *               [ objective ]
	 *               [ SUCH THAT {expression}' ]
	 * 
	 */
	
	
	Declaration[] declarations;
	//Annotation[] annotations;
	Objective objective;
	Expression[] expressions;
	Identifier[] identifiers;
	
	public Declaration[] getDeclarations(){
		return declarations;
	}
	public Objective getObjective(){
		return objective;
	}
	public Expression[] getExpressions(){
		return expressions;
	}
	public Identifier[] getIdentifiers(){
		return identifiers;
	}
	
	public void setDeclarations(Declaration[] d){
		declarations=d;
	}
	public void setObjective(Objective o){
		objective=o;
	}
	public void setExpressions(Expression[] e){
		expressions=e;
	}
	public void setIdentifiers(Identifier[] i){
		identifiers=i;
	}
	
	public EssenceSpecification(Declaration[] d,Objective o,Expression[] e){
		declarations = d;
		objective = o;
		expressions = e;
		identifiers = new Identifier[30];
	}
	
	public String toString(){
		
		StringBuffer output = new StringBuffer("");
		for(int i =0;i<declarations.length;i++){
			output.append(declarations[i].toString()+"\n");
		}
		output.append(objective.toString()+"\n");
		if(expressions.length>0){
			output.append("such that\n");
			for(int i =0;i<expressions.length;i++){
				output.append("\t"+expressions[i].toString()+"\n");
			}
		}
		
		return output.toString();
	}
	
}
