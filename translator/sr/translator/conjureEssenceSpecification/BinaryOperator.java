package translator.conjureEssenceSpecification;

public class BinaryOperator implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : +
	 * restriction_mode 2 : -
	 * restriction_mode 3 : /
	 * restriction_mode 4 : *
	 * restriction_mode 5 : ^
	 * restriction_mode 6 : AND
	 * restriction_mode 7 : OR
	 * restriction_mode 8 : if
	 * restriction_mode 9 : iff
	 * restriction_mode 10 : =
	 * restriction_mode 11 : !=
	 * restriction_mode 12 : <=
	 * restriction_mode 13 : <
	 * restriction_mode 14 : >=
	 * restriction_mode 15 : >
	 * restriction_mode 16 : Intersection
	 * restriction_mode 17 : Union
	 * restriction_mode 18 : subsetOp
	 * restriction_mode 19 : memberOp 
	 */
	int restriction_mode;
	SubSetOp subsetop;
	MemberOp memberop;
	
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public SubSetOp getSubsetOperator(){
		return subsetop;
	}
	public void setSubsetOperator(SubSetOp sso){
		subsetop = sso;
	}
	public MemberOp getMemberOperator(){
		return memberop;
	}
	public void setMemberOperator(MemberOp mo){
		memberop = mo;
	}
	
	

    public BinaryOperator copy() {	
	int rm = restriction_mode;
	return new BinaryOperator(rm);

    }

    /**
	 * 
	 * @param t - restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : +<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : -<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : /<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 4 : *<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 5 : ^<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 6 : AND<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 7 : OR<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 8 : if<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 9 : iff<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 10 : =<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 11 : !=<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 12 : <=<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 13 : <<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 14 : >=<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 15 : ><br/>&nbsp&nbsp&nbsp
	 * restriction_mode 16 : Intersection<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 17 : Union<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 18 : subsetOp<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 19 : memberOp <br/>&nbsp&nbsp&nbsp
	 */
    
	public BinaryOperator(int t){
		restriction_mode = t;
	}
	
	public BinaryOperator(SubSetOp s){
		restriction_mode = 18;
		subsetop = s;
	}
	
	public BinaryOperator(MemberOp m){
		restriction_mode = 19;
		memberop = m;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case PLUS : return " + ";
		case MINUS : return " - ";
		case DIVIDE : return " / ";
		case MULT : return " * ";
		case POWER : return " ^ ";
		case AND : return " AND ";
		case OR : return " OR ";
		case IF : return " IF ";
		case IFF : return " IFF ";
		case EQ : return " = ";
		case NEQ : return " != ";
		case LEQ : return " <= ";
		case LESS : return " < ";
		case GEQ : return " >= ";
		case GREATER : return " > ";
		case INTERSEC : return "Intersection ";
		case UNION : return "Union ";
		case SUBSET_OP : return subsetop.toString();
		case MEMBER_OP : return memberop.toString();
		}
		
		return "";
	}
	

}
