package translator.tailor.minion;

public class OccurrenceLeq implements MinionConstraint {

	// leq->atmost,  geq->atleast
	boolean isLeq;
	MinionArray array;
	int[] values;
	int[] occurrences;
	
	
	public OccurrenceLeq(MinionArray array,
			             int[] values,
			             int[] occurrences,
			             boolean isLeq) {
		
		this.array = array;
		this.values = values;
		this.occurrences = occurrences;
		this.isLeq = isLeq;
	}
	
	
	public String toString() {
		
		String header = (this.isLeq) ? "occurrenceleq(" : "occurrencegeq(";
		
		header = header.concat(this.array+", ");
		
		String result = "";
		
		int length = this.values.length;
		if(this.values.length != this.occurrences.length)
			length = (values.length < this.occurrences.length) ?
					values.length : occurrences.length;
		
		for(int i=0; i<length; i++) {
			result  = result.concat(header+this.values[i]+", "+this.occurrences[i]+")");
			if(length > 1 && i != length-1) result = result.concat("\n");
		}
		
		return result;
	}
	
}
