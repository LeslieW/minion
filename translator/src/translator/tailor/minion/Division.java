package translator.tailor.minion;

public class Division implements MinionConstraint {

	
	
	private MinionAtom leftArgument;
	private MinionAtom rightArgument;
	private MinionAtom result;
	
	public Division(MinionAtom leftArgument,
				  MinionAtom rightArgument,
				  MinionAtom result) {
		
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.result = result;
	}
	
	
	public String toString() {
		return "div("+leftArgument+", "+rightArgument+", "+result+")";
	}
	
}
