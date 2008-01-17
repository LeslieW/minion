package translator.conjureEssenceSpecification;

public class AtmostConstraint {

	
	Expression array;
	int[] occurrences;
	int[] values;
	boolean isAtmost;
	
	public AtmostConstraint( Expression array,
			                  int[] occurrences,
			                  int[] values,
			                  boolean isAtmost) {
		
		this.array = array;
		this.occurrences = occurrences;
		this.values = values;
		this.isAtmost = isAtmost;
	}
	
	
	public Expression getArray() {
		return this.array;
	}
	
	public int[] getOccurrences() {
		return this.occurrences;
	}
	
	public int[] getValues() {
		return this.values;
	}
	
	public boolean isAtmost() {
		return this.isAtmost;
	}
	
	public AtmostConstraint copy() {
		
		int[] copiedOccurrences = new int[this.occurrences.length];
		for(int i=0; i<this.occurrences.length; i++)
			copiedOccurrences[i] = this.occurrences[i];
		
		int[] copiedValues = new int[this.values.length];
		for(int i=0; i<this.values.length; i++)
			copiedValues[i] = this.values[i];
		
		return new AtmostConstraint(this.array.copy(),
				           copiedOccurrences,
				           copiedValues,
				           this.isAtmost);
	}
	
	public String toString() {
		
		String s = (this.isAtmost) ? 
			       "atmost(" : "atleast(";
	
	s = s.concat(this.array.toString()+",[");
	
	for(int i=0; i<this.occurrences.length; i++) {
		if(i >0) s = s.concat(",");
		s = s.concat(this.occurrences[i]+"");
	}
	
	s= s+"], [";
	
	for(int i=0; i<this.values.length; i++) {
		if(i >0) s = s.concat(",");
		s = s.concat(this.values[i]+"");
	}
	
	
	
	return s+"])";
		
	}
}

