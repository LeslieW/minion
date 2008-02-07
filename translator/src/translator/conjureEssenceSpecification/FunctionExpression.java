package translator.conjureEssenceSpecification;

public class FunctionExpression implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : allDiff
	 * restriction_mode 2 : min
	 * restriction_mode 3 : max
	 * restriction_mode 4 : dom
	 * restriction_mode 5 : ran
	 * restriction_mode 6 : inv
	 * restriction_mode 7 : image
	 * restriction_mode 8 : atleast
	 * restriction_mode 9 : atmost
	 */
	int restriction_mode;	
	Expression exp1,exp2,exp3;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	
	public Expression getExpression1(){
		return exp1;
	}
	public Expression getExpression2(){
		return exp2;
	}
	public Expression getExpression3(){
		return exp3;
	}
	
	protected void setExpression1(Expression exp){
		exp1=exp;
	}
	protected void setExpression2(Expression exp){
		exp2=exp;
	}
	protected void setExpression3(Expression exp){
		exp3=exp;
	}
	

    public FunctionExpression copy() {

	int rm = restriction_mode;
	switch(restriction_mode) {

	case EssenceGlobals.IMAGE:
	    return new FunctionExpression(exp1.copy(), exp2.copy());

	case EssenceGlobals.ATLEAST:
	    return new FunctionExpression(rm, exp1.copy(), exp2.copy(), exp3.copy());

	case EssenceGlobals.ATMOST :
	    return new FunctionExpression(rm, exp1.copy(), exp2.copy(), exp3.copy());
	    
	default:
	    return new FunctionExpression(rm, exp1.copy());

	}
    }

	/**
	 * 
	 * @param t -  restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : allDiff<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : min<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : max<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 4 : dom<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 5 : ran<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 6 : inv<br/>&nbsp&nbsp&nbsp
	 * @param e
	 */
	public FunctionExpression(int t, Expression e){
		restriction_mode = t;
		exp1 = e;
	}
	
	public FunctionExpression(Expression e1,Expression e2){
		restriction_mode = IMAGE;
		exp1 = e1;
		exp2 = e2;
	}
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 8 : atleast<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 9 : atmost<br/>&nbsp&nbsp&nbsp
	 * @param e1
	 * @param e2
	 * @param e3
	 */
	public FunctionExpression(int t,Expression e1,Expression e2,Expression e3){
		restriction_mode = t;
		exp1 = e1;
		exp2 = e2;
		exp3 = e3;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case ALLDIFF : return "alldiff ( " + exp1.toString() + " ) ";
		case MIN : return "min ( " + exp1.toString() + " ) ";
		case MAX : return "max ( " + exp1.toString() + " ) ";
		case DOM : return "dom ( " + exp1.toString() + " ) ";
		case RAN : return "ran ( " + exp1.toString() + " ) ";
		case INV : return "inv ( " + exp1.toString() + " ) ";
		case IMAGE : return "image ( " + exp1.toString() + ", " + exp2.toString() + " ) ";
		case ATLEAST : return "atleast ( " + exp1.toString() + ", " + exp2.toString() + ", " + exp3.toString() +" ) ";
		case ATMOST : return "atmost ( " + exp1.toString() + ", " + exp2.toString() + ", " + exp3.toString() +" ) ";
		case OCCURRENCE : return "occurrence("+exp1.toString()+", "+exp2.toString()+", "+exp3.toString()+")";
		case ELEMENT : return "element("+exp1.toString()+", "+exp2.toString()+", "+exp3.toString()+")";
		}		
		return "";
	}

}
