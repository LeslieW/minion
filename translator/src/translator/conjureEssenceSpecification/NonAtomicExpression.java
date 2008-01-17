package translator.conjureEssenceSpecification;

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
	/** 'name' of the matrix-element  */
	private String arrayName;
	/** index(indices) of matrix element */
	//Expression[] expressionList;
	
	Index[] indexList;
	
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : expression [expressions]<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : expression (expressions)<br/>&nbsp&nbsp&nbsp
	 * @param e
	 * @param es
	 */
/*	public NonAtomicExpression(int t,String e, Expression[] es){
		restriction_mode = t;
		arrayName = e;
		expressionList = es;
	}*/
	
	
	public NonAtomicExpression(String arrayName, Index[] indices) {
		this.arrayName = arrayName;
		this.indexList = indices;
	}
	
	
	public int getRestrictionMode(){
		return EssenceGlobals.NONATOMIC_EXPR_BRACKET;
	}

	public String getArrayName(){
		return arrayName;
	}
	//protected void setExpression(Expression e){
	//	expression=e;
	//}
	public Index[] getIndexList(){
		return this.indexList;
	}
//	protected void setExpressionList(Expression[] es){
//		expressionList=es;
//	}
	

    public NonAtomicExpression copy() {
    	Index[] expressions = new Index[this.indexList.length];
    	for(int i = 0; i < indexList.length; i++)
    		expressions[i] = indexList[i].copy();
    	return new NonAtomicExpression(new String(this.arrayName),expressions); 
    }
	

	
	public String toString(){
	
		String s = arrayName+"[";
			
		for(int i=0; i<this.indexList.length; i++) {
			if(i >0 ) s = s.concat(",");
				
			s = s.concat(indexList[i].toString());
		}
			
		return s+"]";
	}
/*		switch(restriction_mode){
		case NONATOMIC_EXPR_BRACKET : return arrayName.toString() + "[ "+ getExpressions() + "] ";
		case NONATOMIC_EXPR_PAREN : return arrayName.toString() + "( "+ getExpressions() + ") ";
		}		
		return "";
	}
    
    private String getExpressions(){
	String output = "";

	if(expressionList.length > 0) {
	    for(int i = expressionList.length-1; i>= 0; i--)
	    	if(i!=0)
	    		output+= expressionList[i].toString()+", ";
	    	else output += expressionList[i].toString();
		 for(int i = 0; i<expressionList.length; i++)
		    	if(i!=expressionList.length-1)
		    		output+= expressionList[i].toString()+", ";
		    	else output += expressionList[i].toString();
	}
	return output;
    }
    */

}
