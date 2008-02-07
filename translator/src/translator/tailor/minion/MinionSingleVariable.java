package translator.tailor.minion;

public class MinionSingleVariable implements MinionAtom {

	String name;
	
	public MinionSingleVariable(String name) {
		this.name = name;
	}
	
	
	public String toString() {
		return this.name;
	}

	public String getVariableName() {
		return this.name;
	}
}
