package translator.conjureEssenceSpecification;

public class Quantifier implements EssenceGlobals{
	
	/**
	 * restriction_mode 1 : forall
	 * restriction_mode 2 : there exists
	 * restriction_mode 3 : sum
	 */
	int restriction_mode;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	
	public void setRestrictionMode(int rm) {
		restriction_mode = rm;
	}
	
    public Quantifier copy() {
    	int t = restriction_mode;
    	return new Quantifier(t);
    }
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : forall<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : there exists<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : sum<br/>&nbsp&nbsp&nbsp
	 */
	public Quantifier (int t){		
		restriction_mode = t;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case FORALL : return "forall ";
		case EXISTS : return "there exists ";
		case SUM : return "sum of ";
		}
		return "";
	}

}
