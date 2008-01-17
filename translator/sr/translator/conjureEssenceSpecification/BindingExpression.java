package translator.conjureEssenceSpecification;

public class BindingExpression implements EssenceGlobals {
	
	
	/**
	 * restriction_mode 1 : domainIdentifers
	 * restriction_mode 2 : bindUnit memberOp Exp
	 * restriction_mode 3 : identifierList subSetOp exp
	 * DONE!
	 * 
	 * Essence' grammar: (terminals in capital letters)
	 *   { identifier }' : domain
	 * | bindingUnit memberOperator expression
	 * | identifierList subsetOperator expression
	 * 
	 */
	int restriction_mode;
	DomainIdentifiers domainidentifiers;
	String[] identifierlist;
	BindUnit bindunit;
	MemberOp memberop;
	SubSetOp subsetop;
	Expression exp;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public String[] getIdentifierList(){
		return identifierlist;
	}
	public DomainIdentifiers getDomainIdentifiers(){
		return domainidentifiers;
	}
	public BindUnit getBindUnit(){
		return bindunit;
	}
	public MemberOp getMemberOperator(){
		return memberop;
	}
	public SubSetOp getSubsetOperator(){
		return subsetop;
	}
	public Expression getExpression(){
		return exp;
	}	
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public void setDomainIdentifiers(DomainIdentifiers di){
		domainidentifiers=di;
	}
	public void setIdentifierList(String[] il){
		identifierlist=il;
	}
	public void setBindUnit(BindUnit bu){
		bindunit=bu;
	}
	public void setMemberOperator(MemberOp mo){
		memberop=mo;
	}
	public void setSubsetOperator(SubSetOp so){
		subsetop=so;
	}
	public void setExpression(Expression exp){
		this.exp=exp;
	}	

    public BindingExpression copy() {
	return new BindingExpression(domainidentifiers.copy());
    }

	
	public BindingExpression (DomainIdentifiers di){
		restriction_mode = BINDING_IDENTIFIER_DOMAIN_EXPR;
		domainidentifiers = di;
	}
	
	public BindingExpression (BindUnit bu, MemberOp m, Expression e){
		restriction_mode = BINDING_UNIT_MEMBER_EXPR;
		bindunit = bu;
		memberop = m;
		exp = e;
	}
	
	public BindingExpression (String[] il, SubSetOp s, Expression e){
		restriction_mode = BINDING_IDLIST_SUBSET_EXPR;
		identifierlist = il;
		subsetop = s;
		exp = e;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case BINDING_IDENTIFIER_DOMAIN_EXPR : return domainidentifiers.toString();
		case BINDING_UNIT_MEMBER_EXPR : return bindunit.toString() + memberop.toString() + exp.toString();
		case BINDING_IDLIST_SUBSET_EXPR : return identifierlist.toString() + subsetop.toString() + exp.toString();
		}
		
		return "";
	}

}
