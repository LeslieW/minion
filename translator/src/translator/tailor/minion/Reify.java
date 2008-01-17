package translator.tailor.minion;

public class Reify implements MinionConstraint {

	private MinionConstraint reifiedConstraint;
	private MinionAtom reifiedVariable;
	
	
	public Reify(MinionConstraint reifiedConstraint,
			     MinionAtom reifiedVariable) {
		
		this.reifiedConstraint = reifiedConstraint;
		this.reifiedVariable = reifiedVariable;
	}
	
	public String toString() {
		return "reify("+this.reifiedConstraint+", "+this.reifiedVariable+")";
	}
	
}
