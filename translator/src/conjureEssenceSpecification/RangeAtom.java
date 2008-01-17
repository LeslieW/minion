package conjureEssenceSpecification;

public class RangeAtom implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : expression
	 * restriction_mode 2 : .. expression
	 * restriction_mode 3 : expression ..
	 * restriction_mode 4 : expression .. expression
	 */
	int restriction_mode;
	Expression lowerBound;
	Expression upperBound;
	
	public Expression getLowerBound(){
		return lowerBound;
	}
	public Expression getUpperBound(){
		return upperBound;
	}
	public void setLowerBound(Expression exp){
		lowerBound=exp;
	}
	public void setUpperBound(Expression exp){
		upperBound=exp;
	}
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	
	/**
	 * 
	 * @param t restriciton mode<br/>&nbsp&nbsp&nbsp 
	 * restriction_mode 1 : expression<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : .. expression<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : expression ..<br/>&nbsp&nbsp&nbsp
	 * 
	 * @param expressions expression
	 */

    public RangeAtom copy() {
	
	Expression e1 = lowerBound.copy();
	if(upperBound == null) 
	    return new RangeAtom(EssenceGlobals.RANGE_EXPR, e1);
	else 
	    return new RangeAtom(e1, upperBound.copy());

    }

	public RangeAtom(int t, Expression e){
		restriction_mode = t;
		if(t == EssenceGlobals.RANGE_EXPR) {
			lowerBound = e;
			upperBound = e;
		}
		else if(t == EssenceGlobals.RANGE_DOTS_EXPR)
			upperBound = e;
		else // if(t == EssenceGlobals.RANGE_EXPR_DOTS)
			lowerBound = e;
	}
	
	
	/**
	 * Range of form exp .. exp
	 * 
	 * @param e1
	 * @param e2
	 */
	public RangeAtom(Expression e1,Expression e2){
		restriction_mode = RANGE_EXPR_DOTS_EXPR;
		lowerBound = e1;
		upperBound = e2;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case RANGE_EXPR : return lowerBound.toString();
		case RANGE_DOTS_EXPR : return ".. " + lowerBound.toString();
		case RANGE_EXPR_DOTS : return lowerBound.toString() + ".. ";
		case RANGE_EXPR_DOTS_EXPR : return lowerBound.toString() + ".. " + upperBound.toString();
		}
		return "";
	}

}
