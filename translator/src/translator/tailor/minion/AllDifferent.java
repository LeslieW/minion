package translator.tailor.minion;

public class AllDifferent implements MinionConstraint {

	MinionArray argument;
	
	
	public AllDifferent(MinionArray argument) {
		this.argument = argument;
	}
	
	
	public String toString() {
		return "alldiff("+argument+")";
	}
	
}
