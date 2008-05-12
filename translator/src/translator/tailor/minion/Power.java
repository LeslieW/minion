package translator.tailor.minion;

public class Power implements MinionConstraint {

	
	private MinionAtom leftArgument;
	private MinionAtom rightArgument;
	private MinionAtom result;
	
	public Power(MinionAtom leftArgument,
				  MinionAtom rightArgument,
				  MinionAtom result) {
		
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.result = result;
	}
	
	
	public String toString() {
		return "pow("+leftArgument+", "+rightArgument+", "+result+")";
	}
	
}
