package minionModel;

public class MinionCube {

	MinionIdentifier[][][] elements;
	String originalName;
	String minionName;
	/** offset[0] == matrixOffset, offset[1] == vectorOffset, offset[2] == elementOffset */
	int[] offset;
	
	
	public MinionCube(MinionIdentifier[][][] cubeElements, String origName, int[] offsets) {
		this.elements = cubeElements;
		this.originalName = origName;
		this.offset = offsets;
	}
	
	
	protected void setMinionName(String mName) {
		this.minionName = mName;
	}
	
	public String getOriginalName() {
		return this.originalName;
	}
	
	public String toString() {
		return this.minionName;
	}
	
	public int[] getOffsets() {
		return this.offset;
	}
	
	
	public MinionIdentifier[][] getMatrix(int matrixIndex) 
		throws MinionException {
		
		matrixIndex = matrixIndex-offset[0];
		if(matrixIndex < elements.length && matrixIndex >= 0) 
			return elements[matrixIndex];
		
		else throw new MinionException
		(originalName+"["+matrixIndex+offset[0]+"] cannot be accessed: the matrix index '"
				+(matrixIndex+offset[0])+"' is out of bounds.");	
	}
	
	// corresponds to a row!
	public MinionIdentifier[] getVector(int matrixIndex, int vectorIndex) 
		throws MinionException {
		matrixIndex = matrixIndex-offset[0];
		if(matrixIndex < elements.length && matrixIndex >= 0) {
			vectorIndex = vectorIndex - offset[1];
			if(vectorIndex < elements[matrixIndex].length && vectorIndex >= 0) { 
			     return elements[matrixIndex][vectorIndex];
			}
			else throw new MinionException
			(originalName+"["+matrixIndex+offset[0]+", "+(vectorIndex+offset[1])+"] cannot be accessed: the vector index "
					+(vectorIndex+offset[1])+" is out of bounds.");
		}
		else throw new MinionException
		(originalName+"["+matrixIndex+offset[0]+"] cannot be accessed: the matrix index '"
				+(matrixIndex+offset[0])+"' is out of bounds.");	
	}
	
	
	public MinionIdentifier getElement(int matrixIndex, int vectorIndex, int elemIndex) 
		throws MinionException {
		matrixIndex = matrixIndex - offset[0];
		vectorIndex = vectorIndex - offset[1];
		elemIndex = elemIndex - offset[2];
		if(matrixIndex < elements.length && matrixIndex >= 0) {
			if(vectorIndex < elements[matrixIndex].length && vectorIndex >= 0) {
				if(elemIndex < elements[matrixIndex][vectorIndex].length && elemIndex >= 0) {
					return elements[matrixIndex][vectorIndex][elemIndex];
				}
				else throw new MinionException 
				(originalName+"["+(matrixIndex+offset[0])+","+(vectorIndex+offset[1])+","+(elemIndex+offset[2])+"]"+
						" cannot be accessed: element index '"+(elemIndex+offset[2])+"' is out of bounds.");
			}

			else throw new MinionException
			(originalName+"["+matrixIndex+offset[0]+", "+(vectorIndex+offset[1])+"] cannot be accessed: the vector index "
					+(vectorIndex+offset[1])+" is out of bounds.");
		}
		else throw new MinionException
		(originalName+"["+matrixIndex+offset[0]+"] cannot be accessed: the matrix index "
				+(matrixIndex+offset[0])+" is out of bounds.");
	}
	
	
	public MinionIdentifier[][][] getCube() {
		return this.elements;
	}
	
}
