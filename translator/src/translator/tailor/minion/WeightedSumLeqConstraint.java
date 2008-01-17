package translator.tailor.minion;

public class WeightedSumLeqConstraint implements MinionConstraint {

	private int[] weights;
	private MinionAtom[] arguments;
	private MinionAtom result;
	
	
	public WeightedSumLeqConstraint(MinionAtom[] arguments,
									int[] weights,
									MinionAtom result) {
		
		this.arguments = arguments;
		this.weights = weights;
		this.result = result;
	}
	
	
	public String toString() {
		String s = "weightedsumleq(";
	
		String constants = ""+this.weights[0];
		for(int i=1; i<this.weights.length; i++) {
			constants = constants.concat(","+this.weights[i]);
		}
		
		String args = ""+this.arguments[0];
		for(int i=1; i<this.arguments.length; i++) {
			args = args.concat(","+this.arguments[i]);
		}
		
		return s+"["+constants+"], ["+args+"], " +result+")";
	}
	
}