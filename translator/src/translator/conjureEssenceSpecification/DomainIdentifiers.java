package translator.conjureEssenceSpecification;

public class DomainIdentifiers {
	
	/**
	* 
	* Essence' grammar: (terminals in capital letters)
	*   { identifier }' : domain
	*/
	
	String[] identifiers;
	Domain domain;
	
	public String[] getIdentifiers(){
		return identifiers;
	}
	public Domain getDomain(){
		return domain;
	}
	
	public void setIdentifiers(String[] is){
		identifiers=is;
	}
	public void setDomain(Domain d){
		domain=d;
	}

    public DomainIdentifiers copy() {
	String[] ids = new String[identifiers.length];
	for(int i=0; i < identifiers.length; i++) 
	    ids[i] = new String(identifiers[i]);
	return new DomainIdentifiers(ids, domain.copy());
    }
	
	public DomainIdentifiers(String[] i, Domain d){
		identifiers = i;
		domain = d;
	}
	
	public String toString(){
		String output = "";
		output+=identifiers[0];
		for(int i =1;i<identifiers.length;i++){
			output+=", "+identifiers[i];
		}
		output += ":" +  domain.toString();
		return output;
	}

}
