package translator.conjureEssenceSpecification;

public class Variable {

	DomainIdentifiers domain_identifiers;
	
	public DomainIdentifiers getdomain_identifiers(){
		return domain_identifiers;
	}
	public void setdomain_identifiers(DomainIdentifiers di){
		domain_identifiers=di;
	}
	
	public Variable(DomainIdentifiers di){
		domain_identifiers = di;
	}
	
	public String toString(){
		return domain_identifiers.toString();
	}
	
}
