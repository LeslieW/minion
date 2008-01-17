package translator.conjureEssenceSpecification;

public class ConstantMatrix implements ConstantArray {

	private String arrayName;
	private int[][] elements;
	
	
	// ========= CONSTRUCTOR ==================
	
	public ConstantMatrix(String arrayName,
			              int[][] elements) {
		this.arrayName = arrayName;
		this.elements = elements;
	}
	
	
	// ====== (INHERITED) METHODS ==============
	
	public String getArrayName() {
		return this.arrayName;
	}

	public int[][] getElements() {
		return this.elements;
	}
	
	public int getElementAt(int rowIndex, int colIndex) 
		throws Exception {
		if(rowIndex<this.elements.length && rowIndex >=0) {
			
			if(colIndex<this.elements[rowIndex].length && colIndex >=0)
			return this.elements[rowIndex][colIndex];
			else throw new Exception("Index of constant array '"+this
					+"' out of bounds:"+colIndex);
		}
		else throw new Exception("Index of constant array '"+this
				+"' out of bounds:"+rowIndex);
		
	}
	
	
	public String toString() {
		String s = arrayName+": [";
		
		for(int row=0; row<this.elements.length; row++) {
			if(row >0) 
				s = s.concat(",\n[");
			
			for(int col =0; col<this.elements[row].length; col++) {
				if(col==0) s =s.concat("[");
				if(col >0 ) s = s.concat(",");
				s = s.concat(elements[row][col]+"");
			}
			s = s.concat("]");
		}
		
		return s+"]";
	}

}
