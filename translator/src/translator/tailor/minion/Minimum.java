package translator.tailor.minion;

public class Minimum implements MinionConstraint {

	private MinionArray arguments;
	private MinionAtom result;
	boolean isMinimum; // if false it is a maximum
	
	
	// =========================================
	
	public Minimum(MinionArray arguments,
							 MinionAtom result,
							 boolean isMinimum) {
		
		this.arguments = arguments;
		this.result =result;
		this.isMinimum = isMinimum;
	}
	
	
	// =========================================
	
	public String toString() {
	
		StringBuffer s = new StringBuffer(isMinimum ? "min(" : "max(");
		s.append(this.arguments);
		s.append(", ");
		s.append(this.result);
		s.append(")");
		
		return s.toString();
	}
	
}
