package conjureEssenceSpecification;

public class MemberOp implements EssenceGlobals {
	/**
	 * restriction_mode 1 : member of
	 * restriction_mode 2 : not in
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
	 * restriction_mode 1 : member of<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : not in<br/>&nbsp&nbsp&nbsp
	 */
	public MemberOp (int t){
		restriction_mode = t;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case MEMBER_OF : return "in ";
		case NOT_IN : return "not in ";
		}
		
		return "";
	}
}
