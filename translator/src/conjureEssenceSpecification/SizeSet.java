package conjureEssenceSpecification;

public class SizeSet implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : size exp
	 * restriction_mode 2 : maxsize exp
	 */
	int restriction_mode;
	Expression exp;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	public Expression getexpression(){
		return exp;
	}
	public void setexpression(Expression e){
		exp=e;
	}
	
	/**
	 * 
	 * @param t restriction mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : size exp<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : maxsize exp<br/>&nbsp&nbsp&nbsp
	 * @param e
	 */
	public SizeSet(int t, Expression e){
		restriction_mode = t;
		exp = e;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case SIZESET : return "( size " + exp.toString() + ") ";
		case SIZESET_MAX : return "( maxsize " + exp.toString() + ") ";
		}
		return "";
	}

}
