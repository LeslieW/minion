package translator.tailor.minion;

public class False implements MinionConstraint {

	public False() {
	}
	
	public String toString() {
		return "eq(0,1) # FALSE";
	}
}
