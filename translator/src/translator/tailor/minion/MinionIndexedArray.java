package translator.tailor.minion;

import translator.expression.ConstantDomain;
import translator.expression.SingleIntRange;

public class MinionIndexedArray implements MinionArray {

	// the indices that are used in this array
	private ConstantDomain[] indices;
	private String name;
	/** indexIsWholeDomain and indices have the same size: 
	    it indicates if a range at a certain position stands
	    for the whole row/col/... of the array */
	boolean[] indexIsWholeDomain;
	
	int[] offsetsFromZero;
	
	public MinionIndexedArray(String name,
							  ConstantDomain[] indices,
			                  boolean[] indexIsWholeDomain,
			                  int[] offsetsFromZero) {
		this.indices = indices;
		this.name = name;
		this.indexIsWholeDomain = indexIsWholeDomain;
		this.offsetsFromZero = offsetsFromZero;
	}
	
	
	public String toString() {
		
		String s = "[ ";
		
		
		
		// vector (some elements of a vector)
		if(this.indices.length == 1) {
			
			if(this.indexIsWholeDomain[0])
				return this.name;
			
			int[] fullRange = this.indices[0].getFullDomain();
 			
			String elements = "";
			
			for(int j=0; j<fullRange.length; j++) {	
				if(j > 0) elements = elements.concat(", ");
				elements = elements.concat(this.name+"["+(fullRange[j]-this.offsetsFromZero[0])+"]");
			}	
			s= s.concat(elements);
		}
		
		// matrix
		else if(this.indices.length == 2) {
			
			// the whole row
			if(indices[0] instanceof SingleIntRange) {
				if(this.indexIsWholeDomain[1]) {
					return s+"row("+this.name+", "+(indices[0].getFullDomain()[0]- this.offsetsFromZero[0])+")]";
				}

				
			}
			
			if(indices[1] instanceof SingleIntRange) {
				
				if(this.indexIsWholeDomain[0]) {
					return s+"col("+this.name+", "+(indices[1].getFullDomain()[0]-this.offsetsFromZero[1])+")]";
				}
				
			}
			
			
				int[] rowIndices = this.indices[0].getFullDomain();
				int[] colIndices = this.indices[1].getFullDomain();
				String elements = "";
				
				for(int i=0; i<rowIndices.length; i++) {
					for(int j=0; j<colIndices.length; j++) {
						if(j > 0 || i > 0) elements = elements.concat(", ");
						elements = elements.concat(this.name+"["+(rowIndices[i]-this.offsetsFromZero[0])+", "+(colIndices[j]-this.offsetsFromZero[1])+"]");
					}
					
				}
				s= s.concat(elements);
			
		}
		
		
		return s+" ]";
	}
	

	
}
