package translator.conjureEssenceSpecification;

public class Objective implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : solve
	 * restriction_mode 2 : minimising
	 * restriction_mode 3 : maximising
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
	
	public Objective(){
		restriction_mode = SOLVE;
	}
	
	public boolean isMinimising() {
		if(restriction_mode == MINIMISING)
			return true;
		else return false;
	}
	
	public Objective(boolean minimising, Expression e){
		if(minimising){
			restriction_mode = MINIMISING;
		}
		else{
			restriction_mode = MAXIMISING;
		}
		exp = e;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case SOLVE : return "solve ";
		case MINIMISING : return "minimising " + exp.toString();
		case MAXIMISING : return "maximising " + exp.toString();
		}		
		return "";
	}

}
