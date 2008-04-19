package translator.tailor.gecode;

/**
 * 
 * 
 * @author andrea
 *
 */

public class GecodeBoolVar implements BooleanVariable, GecodeAtomVariable {

	private String name;
	
	public GecodeBoolVar(String name) {
		this.name = name;
	}
	
	public boolean isArgsVariable() {
		return false;
	}

	public char getType() {
		return GecodeVariable.BOOL_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public int[] getBounds() {
		return new int[] {0,1};
	}
	
	public String toString() {
		return this.name;
	}

	public String toCCString() {
		return "BoolVar "+name;
	}
}
