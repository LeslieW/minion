package translator.tailor.minion;

/**
 * Constraint minuseq(a,b) corresponds to a = -b
 * 
 * @author andrea
 *
 */

public class MinusEq implements MinionConstraint {

	
	private MinionAtom leftArgument;
	private MinionAtom rightArgument;
	
	
	public MinusEq(MinionAtom leftArgument,
			       MinionAtom rightArgument) {
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
	}
	
	public String toString() {
		return "minuseq("+leftArgument+","+rightArgument+")";
	}
}
