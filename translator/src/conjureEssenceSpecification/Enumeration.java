package conjureEssenceSpecification;

public class Enumeration {
	
	Identifier[] identifiers;
	
	public Identifier[] getidentifiers(){
		return identifiers;
	}
	
	public Enumeration copy() {
		Identifier[] ids = new Identifier[identifiers.length];
		for(int i=0; i<ids.length; i++) 
			ids[i] = identifiers[i].copy();
		
		return new Enumeration(ids);
	}
	
	public Enumeration(Identifier[] i){
		identifiers = i;
	}

	public String toString(){
		
		String output = "";
		
		for(int i =0;i<identifiers.length;i++){
			output+= identifiers[i].toString();
		}
		return output;
	}
	
}
