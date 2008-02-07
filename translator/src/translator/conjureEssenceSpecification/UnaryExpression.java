package translator.conjureEssenceSpecification;

public class UnaryExpression implements EssenceGlobals {
		
	/**
	 * restriction_mode 1 : negation
	 * restriction_mode 2 : mod
	 * restriction_mode 3 : not
	 */
	int restriction_mode;
	Expression exp;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	
	public Expression getExpression(){
		return exp;
	}
	public void setExpression(Expression e){
		exp=e;
	}

    public UnaryExpression copy() {
    	int rm = restriction_mode;
    	return new UnaryExpression(rm, exp.copy());
    }
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : negation<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : mod<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : not<br/>&nbsp&nbsp&nbsp
	 * @param e
	 */
	public UnaryExpression(int t, Expression e){
		restriction_mode = t;
		exp = e;		
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case NEGATION : return "- " + exp.toString();
		case ABS : return "| " + exp.toString() + "| ";
		case NOT : return "! " + exp.toString();
		}
		return "";
	}

}
