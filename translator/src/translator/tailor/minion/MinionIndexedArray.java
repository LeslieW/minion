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
					return "["+this.name+"["+(indices[0].getFullDomain()[0]- this.offsetsFromZero[0])+",_]]";
				}

				
			}
			
			if(indices[1] instanceof SingleIntRange) {
				
				if(this.indexIsWholeDomain[0]) {
					return "["+this.name+"[_,"+(indices[1].getFullDomain()[0]-this.offsetsFromZero[1])+"]]";
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
		// 3-dimensional
		else if(this.indices.length == 3) {
			
			
			// M[ int, ?, ?]
			if(indices[0] instanceof SingleIntRange) {
				// M[int,_,_]
				if(this.indexIsWholeDomain[1] && this.indexIsWholeDomain[2])
					return s+this.name+"["+(indices[0].getFullDomain()[0]-this.offsetsFromZero[0])+",_,_]]";
				
				// M[int,_,(l..u)]
				else if(this.indexIsWholeDomain[1]) {
					String elements = "";
					int matrixIndex = (indices[0].getFullDomain()[0]-this.offsetsFromZero[0]);
					String rowIndex = "_";
					int[] colIndices = this.indices[2].getFullDomain();
					
					for(int i=0; i<colIndices.length; i++) {
						if(i > 0) elements=elements+",";
						elements = elements.concat(this.name+"["+matrixIndex+","+rowIndex+","+(colIndices[i]-this.offsetsFromZero[2])+"]");
					}
					return s+elements+"]";
				}
				
				// M[int,(l..u),_]
				else if(this.indexIsWholeDomain[2]) {
					String elements = "";
					int matrixIndex = (indices[0].getFullDomain()[0]-this.offsetsFromZero[0]);
					String colIndex = "_";
					int[] rowIndices = this.indices[1].getFullDomain();
					
					for(int i=0; i<rowIndices.length; i++) {
						if(i > 0) elements=elements+",";
						elements = elements.concat(this.name+"["+matrixIndex+","+(rowIndices[i]-this.offsetsFromZero[1])+","+colIndex+"]");
					}
					return s+elements+"]";
					
				}
				else {
					String elements = "";
					int matrixIndex = (indices[0].getFullDomain()[0]-this.offsetsFromZero[0]);
					int[] rowIndices = this.indices[1].getFullDomain();
					int[] colIndices = this.indices[2].getFullDomain();
					
					for(int i=0; i<rowIndices.length; i++) {
						for(int j=0; j<colIndices.length; j++) {
							if(i >0 || j>0) elements = elements+",";
							elements = elements+this.name+"["+matrixIndex+","+(rowIndices[i]-this.offsetsFromZero[1])+
							      ","+(colIndices[j]-this.offsetsFromZero[2])+"]";
						}
					}
				}	
				
			}
			
			// M[?,int,?]
			else if(this.indices[1] instanceof SingleIntRange) {
				// M[_,int,_]
				if(this.indexIsWholeDomain[0] && this.indexIsWholeDomain[2]) 
					return "["+this.name+"[_,"+(indices[1].getFullDomain()[0]-this.offsetsFromZero[1])+",_]]";
				
				
				else if(this.indexIsWholeDomain[0]) {
					String elements = "";
					String matrixIndex = "_";
					int rowIndex = (indices[1].getFullDomain()[0]-this.offsetsFromZero[1]);
					int[] colIndices = this.indices[2].getFullDomain();
					
					for(int i=0; i<colIndices.length; i++) {
						if(i > 0) elements=elements+",";
						elements = elements.concat(this.name+"["+matrixIndex+","+rowIndex+","+(colIndices[i]-this.offsetsFromZero[2])+"]");
					}
					return s+elements+"]";
				}
				
				else if(this.indexIsWholeDomain[2]) {
					String elements = "";
					String colIndex = "_";
					int[] matrixIndices = indices[0].getFullDomain();
					int rowIndex = (this.indices[1].getFullDomain()[0]- this.offsetsFromZero[1]);
					
					for(int i=0; i<matrixIndices.length; i++) {
						if(i > 0) elements=elements+",";
						elements = elements.concat(this.name+"["+(matrixIndices[i]-this.offsetsFromZero[0])+
								","+rowIndex+","+colIndex+"]");
					}
					return s+elements+"]";
				}
				
			}
			
			String elements = "";
			int[] matrixIndices= this.indices[0].getFullDomain();
			int[] rowIndices = this.indices[1].getFullDomain();
			int[] colIndices = this.indices[2].getFullDomain();
			
			for(int k=0; k<matrixIndices.length; k++) {
				for(int i=0; i<rowIndices.length; i++) {
					for(int j=0; j<colIndices.length; j++) {
						if(i >0 || j>0 || k>0) elements = elements+",";
						elements = elements+this.name+"["+(matrixIndices[k]-this.offsetsFromZero[0])+","+(rowIndices[i]-this.offsetsFromZero[1])+
					      	","+(colIndices[j]-this.offsetsFromZero[2])+"]";
					}
				}
			}
			
			return s+elements+"]";
			
			
			
		}
		
		return s+" ]";
	}
	

	
}
