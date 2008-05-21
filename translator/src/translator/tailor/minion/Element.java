package translator.tailor.minion;

public class Element implements MinionConstraint {

	// element( indexedArray, index, result )
	MinionArray indexedArray;
	MinionAtom index;
	MinionAtom result;
	boolean isWatched; // true if it's a watched Element constraint 
	
	public Element(MinionArray indexedArray,
			       MinionAtom index,
			       MinionAtom result) {
		this.indexedArray = indexedArray;
		this.index = index;
		this.result = result;
		this.isWatched = false;
	}
	
	public Element(MinionArray indexedArray,
		       MinionAtom index,
		       MinionAtom result,
		       boolean isWatched) {
	this.indexedArray = indexedArray;
	this.index = index;
	this.result = result;
	this.isWatched = isWatched;
}
	
	
	public String toString() {
		
		if(isWatched) 
			return  "watchelement("+this.indexedArray+", "+this.index+", "+this.result+")";
	
		else return  "element("+this.indexedArray+", "+this.index+", "+this.result+")";
	
	}
	
}
