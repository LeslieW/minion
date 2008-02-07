package translator.tailor.minion;

public class Element implements MinionConstraint {

	// element( indexedArray, index, result )
	MinionArray indexedArray;
	MinionAtom index;
	MinionAtom result;
	
	
	public Element(MinionArray indexedArray,
			       MinionAtom index,
			       MinionAtom result) {
		this.indexedArray = indexedArray;
		this.index = index;
		this.result = result;
	}
	
	
	public String toString() {
		
		return  "element("+this.indexedArray+", "+this.index+", "+this.result+")";
	
	}
	
}
