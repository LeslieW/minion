package translator.tailor.minion;

public class MinionConstant implements MinionAtom {

	int value;
	
	public MinionConstant(int value) {
		this.value = value;
	}
	
	public String toString() {
		return ""+value;
	}
	
}
