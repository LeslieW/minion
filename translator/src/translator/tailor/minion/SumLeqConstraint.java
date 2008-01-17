package translator.tailor.minion;

public class SumLeqConstraint implements MinionConstraint {

	private MinionAtom[] arguments;
	private MinionAtom result;
	private boolean isWatched;
	
	public SumLeqConstraint(MinionAtom[] arguments,
			                MinionAtom result) {
		this.arguments = arguments;
		this.result = result;
		this.isWatched = false;
	}
	
	public SumLeqConstraint(MinionAtom[] arguments,
            MinionAtom result,
            boolean isWatched) {
		this.arguments = arguments;
		this.result = result;
		this.isWatched = isWatched;
}
	
	
	public String toString() {
		String s = (isWatched) ? 
				"watchsumleq([" :
					"sumleq([";
		
		
		for(int i=0; i<this.arguments.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(arguments[i].toString());
		}
		return s+"], "+this.result+")";
	}
	
}
