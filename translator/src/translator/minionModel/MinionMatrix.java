package translator.minionModel;

public class MinionMatrix {

	MinionIdentifier[][] elements;
	String originalName;
	String minionName;
	/** offset[0] == vectorOffset, offset[1] == elementOffset */
	int[] offset;
	
	public MinionMatrix(MinionIdentifier[][] matrix, String origName, int[] matrixOffset) {
		
		elements = matrix;
		originalName = origName;
		this.offset = matrixOffset;
	}
	
	protected void setMinionName(String mName) {
		minionName = mName;
	}
	
	public String getOriginalName() {
		return originalName;
	}
	
	public String toString() {
		return minionName;
	}
	
	public int[] getOffsets() {
		return offset;
	}
	
	// corresponds to a row!
	public MinionIdentifier[] getVector(int index) 
		throws MinionException {
		index = index-offset[0];
		if(index < elements.length && index >= 0)
			return elements[index];
		else
			throw new MinionException
				("Trying to access a vector from matrix '"+originalName+"' that is out of bounds: "+(index+offset[0]));
	}
	
	public MinionIdentifier getElement(int vectorIndex, int elemIndex) 
		throws MinionException {
		vectorIndex = vectorIndex - offset[0];
		elemIndex = elemIndex - offset[1];
		if(vectorIndex < elements.length && vectorIndex >= 0) {
			if(elemIndex < elements[vectorIndex].length && elemIndex >= 0) {
				return elements[vectorIndex][elemIndex];
			}
			else throw new MinionException 
				("Trying to access an element in vector '"+(vectorIndex+offset[0])+"' from matrix '"+originalName+
						"' that is out of bounds: "+(elemIndex+offset[1]));
		}

		else throw new MinionException
			("Trying to access a vector from matrix '"+originalName+"' that is out of bounds: "+(vectorIndex+offset[0]));
	}
	
	
	public MinionIdentifier[][] getMatrix() {
		return this.elements;
	}
}
