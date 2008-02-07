package translator.conjureEssenceSpecification;

/**
 * Essence' grammar: (terminals in capital letters)
 * identifier BE DOMAIN domain
 *
 */

public class DomainConstant {
	
	String identifier;
	Domain domain;
	
	public String getName(){
		return identifier;
	}
	public Domain getDomain(){
		return domain;
	}
	
	public DomainConstant copy() {
		return new DomainConstant(new String(identifier), domain.copy());
	}
	
	public DomainConstant(String i,Domain d){
		identifier = i;
		domain = d;
	}
	
	public String toString(){
		return identifier + " be domain " + domain.toString();
	}

}
