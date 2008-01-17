package translator.tailor.minion;

public class LexlessConstraint implements MinionConstraint {

	
	MinionArray leftArray;
	MinionArray rightArray;
	boolean lexless;
	
	public LexlessConstraint(MinionArray left,
			                 MinionArray right,
			                 boolean lexless) {
		this.leftArray = left;
		this.rightArray = right;
	}
	
	
	public String toString() {
		
		String s = (this.lexless) ?  
				"<lexless("  : "lexleq(";
		
		s = s.concat(this.leftArray+", "+this.rightArray);
		
		return s+")";
	}
	
}
