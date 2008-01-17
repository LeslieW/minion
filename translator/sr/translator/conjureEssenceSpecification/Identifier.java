package translator.conjureEssenceSpecification;

public class Identifier  implements EssenceGlobals {

	
	String identifier;
	Domain domain;
	
	public String getidentifier(){
		return identifier;
	}
	public void setidentifier(String s){
		identifier=s;
	}
	
	public Domain getdomain(){
		return domain;
	}
	public void setdomain(Domain d){
		domain = d;
	}


    public Identifier copy() {

	String s = new String();
	s = identifier;
	Identifier ident = new Identifier(s); 

	if(domain == null) {
	    return ident;
	}
	else {
	    Domain d = this.getdomain().copy();
	    return new Identifier(ident,d);
	}
    }
	
	public Identifier(String s){
		identifier = s;
	}
	
	public Identifier(Identifier id, Domain domain){		
		identifier = id.identifier;
		this.domain=domain;			
	}
	
	public String toString(){
		return identifier+" ";
	}

}
