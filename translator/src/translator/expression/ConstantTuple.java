package translator.expression;

public class ConstantTuple {

	
	int[] tupleElements;
	
	public ConstantTuple(int[] tupleElements) {
		this.tupleElements = tupleElements;
	}
	
	
	
	// ============= INHERITED METHODS =====================
	
	public ConstantTuple copy() {
		
		int[] copiedElements = new int[this.tupleElements.length];
		for(int i=0; i<copiedElements.length; i++)
			copiedElements[i] = this.tupleElements[i];
		
		return new ConstantTuple(copiedElements);
	}
	

	
	public String toString() {
		String s = "<";
		if(tupleElements.length >= 1)
			s = s.concat(""+tupleElements[0]);
		for(int i=1; i<this.tupleElements.length; i++)
			s = s.concat(","+tupleElements[i]);
		
		return s+">";
	}
	
	// ============= ADDITONAL METHODS =====================================
	

	public int[] getTuple() {
		return this.tupleElements;
	}
	
	public int getValueAt(int index) throws Exception {
		
		if(index < tupleElements.length && index >= 0)
			return this.tupleElements[index];
		
		else throw new Exception
		("Index '"+index+"' for accessing element in constant tuple "+this.toString()
				+" is out of bounds.");
	}
	
}
