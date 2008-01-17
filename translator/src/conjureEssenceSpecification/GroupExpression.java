package conjureEssenceSpecification;

public class GroupExpression implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : {}
	 * restriction_mode 2 : ##
	 * restriction_mode 3 : <>
	 * restriction_mode 4 : []
	 * restriction_mode 5 : [] : domain
	 */
	int restriction_mode;
	Expression[] exp;
	Domain domain;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	
	public Expression[] getexp(){
		return exp;
	}	
	public void setExpression(Expression[] e){
		exp=e;
	}
	
	public Domain getDomain(){
		return domain;
	}
	public void setDomain(Domain d){
		domain=d;
	}
	
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : {}<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : ##<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : <><br/>&nbsp&nbsp&nbsp
	 * restriction_mode 4 : []<br/>&nbsp&nbsp&nbsp
	 * @param e
	 */
	public GroupExpression (int t,Expression[] e){
		restriction_mode = t;
		exp = e;
	}
	
	public GroupExpression (Expression[] e, Domain d){
		restriction_mode = GROUP_MATRIX_DOMAIN;
		exp = e;
		domain = d;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case GROUP_SET : return "{" + expressionList() + "}";
		case GROUP_MSET : return "#" + expressionList() + "#";
		case GROUP_TUPLE : return "<" + expressionList() + ">";
		case GROUP_MATRIX : return "[" + expressionList() + "]";
		case GROUP_MATRIX_DOMAIN : return "[" + expressionList() + "] : " + domain.toString();
		}		
		return "";
	}
	
	public String expressionList(){
		
		String output = "";
		output += exp[0].toString();
		for (int i = 1; i< exp.length;i++){
			output+=", "+exp[i].toString();
		}
		output+=" ";
		return output;
	}

}
