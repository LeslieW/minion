package translator.tailor.minion;

public class AllDifferent implements MinionConstraint {

	MinionArray argument;
	
	
	public AllDifferent(MinionArray argument) {
		this.argument = argument;
	}
	
	
	public StringBuffer toStringFormat() {
		return new StringBuffer("alldiff(").append(argument).append(")");
	}
	
	public String toString() {
		return "alldiff("+argument+")";
	}
	
}
