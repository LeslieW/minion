package translator.tailor.minion;

public class Modulo implements MinionConstraint {

	private MinionAtom leftArgument;
	private MinionAtom rightArgument;
	private MinionAtom result;
	
	public Modulo(MinionAtom leftArgument,
				  MinionAtom rightArgument,
				  MinionAtom result) {
		
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.result = result;
	}
	
	
	public String toString() {
		return "modulo("+leftArgument+", "+rightArgument+", "+result+")";
	}
	
}
