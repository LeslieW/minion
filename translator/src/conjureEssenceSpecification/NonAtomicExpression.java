package conjureEssenceSpecification;

public class NonAtomicExpression implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : expression [expressions]
	 * restriction_mode 2 : expression (expressions)
	 * 
	 * 	 
	 * Essence' grammar: (terminals in capital letters)
	 *   expression "[" { expression }' "]"
	 * | expression "(" { expression }' ")"	
	 * 
	 */
	int restriction_mode;
	/** 'name' of the matrix-element  */
	Expression expression;
	/** index(indices) of matrix element */
	Expression[] expressionList;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}

	public Expression getExpression(){
		return expression;
	}
	//protected void setExpression(Expression e){
	//	expression=e;
	//}
	public Expression[] getExpressionList(){
		return expressionList;
	}
//	protected void setExpressionList(Expression[] es){
//		expressionList=es;
//	}
	

    public NonAtomicExpression copy() {
    	int rm = restriction_mode;
    	Expression[] expressions = new Expression[expressionList.length];
    	for(int i = 0; i < expressionList.length; i++)
    		expressions[i] = expressionList[i].copy();
    	return new NonAtomicExpression(rm, expression.copy(),expressions); 
    }
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : expression [expressions]<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : expression (expressions)<br/>&nbsp&nbsp&nbsp
	 * @param e
	 * @param es
	 */
	public NonAtomicExpression(int t,Expression e, Expression[] es){
		restriction_mode = t;
		expression = e;
		expressionList = es;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case NONATOMIC_EXPR_BRACKET : return expression.toString() + "[ "+ getExpressions() + "] ";
		case NONATOMIC_EXPR_PAREN : return expression.toString() + "( "+ getExpressions() + ") ";
		}		
		return "";
	}
    
    private String getExpressions(){
	String output = "";

	if(expressionList.length > 0) {
	   /* for(int i = expressionList.length-1; i>= 0; i--)
	    	if(i!=0)
	    		output+= expressionList[i].toString()+", ";
	    	else output += expressionList[i].toString();*/
		 for(int i = 0; i<expressionList.length; i++)
		    	if(i!=expressionList.length-1)
		    		output+= expressionList[i].toString()+", ";
		    	else output += expressionList[i].toString();
	}
	return output;
    }
    

}
