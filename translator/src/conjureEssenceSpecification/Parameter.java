package conjureEssenceSpecification;

public class Parameter implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : Domain Identifiers
	 * restriction_mode 2 : Enumeration
	 * 
	 * 
	 * 	Essence' grammar: (terminals in capital letters)
	 *  parameter :==   domainIdentifiers
	 *                | ENUM "(..)"
	 */
	int restriction_mode;
	DomainIdentifiers domain_identifiers;
	String enumeration;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public DomainIdentifiers getDomainIdentifiers(){
		return domain_identifiers;
	}
	public void setDomainIdentifiers(DomainIdentifiers di){
		domain_identifiers=di;
	}
	public String getEnumeration(){
		return enumeration;
	}
	
	public Parameter(DomainIdentifiers di){
		restriction_mode = PARAMETER_DOM_IDENTIFIERS;
		domain_identifiers = di;
	}
	
	public Parameter(String s){
		restriction_mode = PARAMETER_ENUMERATION;
		enumeration = s;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case PARAMETER_DOM_IDENTIFIERS : return domain_identifiers.toString();
		case PARAMETER_ENUMERATION : return enumeration + "";
		}
		return "";
	}

}
