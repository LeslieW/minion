package conjureEssenceSpecification;

public class IdentifierList implements EssenceGlobals {

	/**
	 * restriction_mode 1 : no domain
	 * restriction_mode 2 : with domain
	 */
	int restriction_mode;
	Identifier[] identifiers;
	Domain domain;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	public Identifier[] getidentifier(){
		return identifiers;
	}
	public void setidentifier(Identifier[] i){
		identifiers=i;
	}
	public Domain getdomain(){
		return domain;
	}
	public void setdomain(Domain d){
		domain=d;
	}
	
	public IdentifierList(Identifier[] is){
		restriction_mode = IDENTIFIER_LIST;
		identifiers = is;
	}
	
	public IdentifierList(Identifier[] is,Domain d){
		restriction_mode = IDENTIFIER_LIST_DOMAIN;
		identifiers = is;
		domain = d;
	}

	public String toString(){
		
		switch(restriction_mode){
		case IDENTIFIER_LIST : return getIdentifiers();
		case IDENTIFIER_LIST_DOMAIN : return getIdentifiers() + " : " + domain.toString();
		}		
		return "";
	}
	
	public String getIdentifiers(){
		String output = "";
		output += identifiers[0];
		for(int i =1;i<identifiers.length;i++){
			output += ", "+ identifiers[i].toString();
		}
		output += " ";
		return output;
	}
	
}
