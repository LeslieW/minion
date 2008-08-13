package translator.tailor.gecode;

public class GecodeConstant implements GecodeAtom {

	private int value;
	
	public GecodeConstant(int value) {
		this.value = value;
	}
	
	
	public String toString() {
		return this.value+"";
	}
	
	public String toDeclarationCCString() {
		return this.value+"";
	}
}
