package translator.tailor.minion;

/**
 * Represents a leq-constraint of the form:<br>
 * 
 * leftArgument <= rightArgument + constant
 * 
 * @author andrea
 *
 */

public class IneqConstraint implements MinionConstraint {

	// leftArgument <= rightArgument + constant
	private MinionAtom leftArgument;
	private MinionAtom rightArgument;
	private int constant;
	
	// ======= CONSTRUCTORS =============================
	
	public IneqConstraint(MinionAtom leftArgument,
			              MinionAtom rightArgument,
			              int constant) {
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.constant = constant;
		
	}
	
	public IneqConstraint(MinionAtom leftArgument,
            MinionAtom rightArgument) {
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.constant = 0;

	}
	
	
	public String toString() {
		return "ineq("+leftArgument+","+rightArgument+","+constant+")";
		
	}
}
