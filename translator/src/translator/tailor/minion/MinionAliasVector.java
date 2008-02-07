package translator.tailor.minion;

public class MinionAliasVector implements MinionAliasArray {

	String name;
	String[] elements;
	
	
	public MinionAliasVector(String name, int length) {
		this.name = name;
		this.elements = new String[length];
	}
	
	public void placeElementAt(String element, int position) 
		throws MinionException {
		
		if(position < 0 || position >= this.elements.length)
			throw new MinionException
			("Internal error: trying to insert element at index out of bound "+position
					+" in MinionAliasArray "+name);
		
		else this.elements[position] = element;
	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("[");
		
		for(int i=0; i <this.elements.length; i++) {
			if(i>0) s.append(","+this.elements[i]);
			else  s.append(this.elements[i]);
		}
		s.append("]");
		
		return s.toString();
	}
}
