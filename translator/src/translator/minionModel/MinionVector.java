package translator.minionModel;

public class MinionVector {

	MinionIdentifier[] elements;
	String originalName;
	String minionName;
	int offset;
	
	public MinionVector(MinionIdentifier[] vector, String origName, int vectorOffset) {
		
		elements = vector;
		originalName = origName;
		this.offset = vectorOffset;
		
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
	
	public MinionIdentifier[] getElements() {
		return elements;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public MinionIdentifier getElementAt(int index) 
		throws MinionException 	{
		index = index-offset;
		
		if(index >= 0 && index < elements.length)
			return elements[index];
		else 
			throw new MinionException 
			("Trying to access an element from vector '"+originalName+"' that is out of bounds: "+index+offset);
	}
	
}
