package conjureEssenceSpecification;

public class SubSetOp implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : proper subset
	 * restriction_mode 2 : subset
	 */
	int restriction_mode;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	
	/**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : subset not equal<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : subset equal<br/>&nbsp&nbsp&nbsp
	 */
	public SubSetOp (int t){
		restriction_mode = t;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case PROPER_SUBSET : return "proper subset ";
		case SUBSET : return "subset ";
		}
		return "";
	}

}
