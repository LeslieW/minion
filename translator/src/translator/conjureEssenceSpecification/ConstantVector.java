package translator.conjureEssenceSpecification;

/**
 * Represents a constant vector (1-dimensional array)
 * that 
 * 
 * @author andrea
 *
 */

public class ConstantVector implements ConstantArray {
	
	private int[] elements;
	private String arrayName;
	
	// ========= CONSTRUCTOR ==================
	
	public ConstantVector(String arrayName,
			              int[] elements) {
		this.arrayName = arrayName;
		this.elements = elements;
	}
	
	
	// ====== (INHERITED) METHODS ==============
	
	public String getArrayName() {
		return this.arrayName;
	}

	public int[] getElements() {
		return this.elements;
	}
	
	public int getElementAt(int index) 
		throws Exception {
		if(index<this.elements.length && index >=0)
			return this.elements[index];
		else throw new Exception("Index of constant array '"+this
				+"' out of bounds:"+index);
	}
	
	public String toString() {
		String s = arrayName+" be [";
		
		for(int i=0; i<this.elements.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(elements[i]+"");
		}
		
		return s+"]";
	}
	
}
