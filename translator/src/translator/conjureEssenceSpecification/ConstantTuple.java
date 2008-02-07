package translator.conjureEssenceSpecification;

public class ConstantTuple {

	
	int[] tupleElements;
	
	public ConstantTuple(int[] tupleElements) {
		this.tupleElements = tupleElements;
	}
	
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
	
	public int[] getTupleElements() {
		return this.tupleElements;
	}
	
}
