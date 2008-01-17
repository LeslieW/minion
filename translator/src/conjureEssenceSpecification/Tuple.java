package conjureEssenceSpecification;

public class Tuple {
	
	Identifier[] identifiers;
	
	public Identifier[] getidentifier(){
		return identifiers;
	}
	public void setidentifier(Identifier[] i){
		identifiers=i;
	}
	
	public Tuple (Identifier[] i){
		identifiers = i;
	}

	public String toString(){
		
		String output = "< ";
		for(int i =0;i<identifiers.length;i++){
			output+= identifiers[i].toString() + ", ";
		}
		output += "> ";
		return output;
	}
}
