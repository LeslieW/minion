package translator.tailor.minion;

public class MinionAbsoluteValue implements MinionConstraint {

	// |x| = y
	MinionAtom x;
	MinionAtom y;
	
	public MinionAbsoluteValue(MinionAtom nested,
								MinionAtom abs) {
		this.x = nested;
		this.y = abs;
	}
	
	public String toString() {
		
		// |x| = y
		return "abs("+y+","+x+")";
	}
}
