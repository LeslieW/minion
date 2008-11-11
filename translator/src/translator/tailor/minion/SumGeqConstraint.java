package translator.tailor.minion;

public class SumGeqConstraint implements MinionConstraint {

	private MinionAtom[] arguments;
	private MinionAtom result;
	private boolean isWatched;
	
	public SumGeqConstraint(MinionAtom[] arguments,
			                MinionAtom result) {
		this.arguments = arguments;
		this.result = result;
		this.isWatched = false;
	}
	
	
	public SumGeqConstraint(MinionAtom[] arguments,
            	MinionAtom result,
            	boolean isWatched) {
		this.arguments = arguments;
		this.result = result;
		this.isWatched = isWatched;
	}
	
	public String toString() {
		StringBuffer s = (isWatched) ? 
				new StringBuffer("watchsumgeq([") :
					new StringBuffer("sumgeq([");
		
		for(int i=0; i<this.arguments.length; i++) {
			if(i >0) s.append(",");
			s.append(arguments[i].toString());
		}
		return s+"], "+this.result+")";
	}
	
	public void setIsWatchedSum(boolean turnOn) {
		this.isWatched = turnOn;
	}
}
