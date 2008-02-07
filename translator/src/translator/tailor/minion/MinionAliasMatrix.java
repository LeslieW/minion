package translator.tailor.minion;

public class MinionAliasMatrix implements MinionAliasArray {

	String name;
	String[][] elements;
	
	public MinionAliasMatrix(String name, int cols, int rows) {
		
		this.name = name;
		this.elements = new String[rows][cols];
	}
	
	
	public void placeElementAt(String element, int rows, int cols) 
	throws MinionException {
	
		
		if(rows < 0 || rows >= this.elements.length)
			throw new MinionException
			("Internal error: trying to insert element at row index out of bound "+rows
					+" in MinionAliasArray "+name);
		
		if(cols < 0 || cols >= this.elements[0].length)
			throw new MinionException
			("Internal error: trying to insert element at column index out of bound "+cols
					+" in MinionAliasArray "+name);
	
		else this.elements[rows][cols] = element;
}

	public String toString() {
	
		StringBuffer s = new StringBuffer("[");
	
		for(int row=0; row <this.elements.length; row++) {
			
			if(row>0) s.append(",\n[");
			else s.append("[");
			
			for(int col=0; col<this.elements[row].length; col++) {
			
				if(col>0) s.append(","+this.elements[row][col]);
				else  s.append(this.elements[row][col]);
			}
			s.append("]");
		}
		s.append("]");
		
		return s.toString();
	}
}
