package translator.tailor.gecode;

/**
 * Simple integer variable for Gecode. 
 * 
 * @author andrea
 *
 */

public class GecodeIntVar implements IntegerVariable, GecodeAtomVariable {

	private String name;
	private int lb;
	private int ub;
	
	public GecodeIntVar(String name, 
			            int lb,
			            int ub) {
		
		this.name = name;
		this.lb = lb;
		this.ub = ub;
	}
	
	
	public int[] getBounds() {
		return new int[] {lb, ub};
	}

	public boolean isArgsVariable() {
		return false;
	}

	public char getType() {
		return GecodeVariable.INT_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
	
	public String toCCString() {
		return "IntVar "+this.name;
	}
}
