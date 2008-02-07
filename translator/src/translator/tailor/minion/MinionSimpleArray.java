package translator.tailor.minion;


public class MinionSimpleArray implements MinionArray {

	private String arrayName;
	
	
	public MinionSimpleArray(String arrayName) {
		this.arrayName = arrayName;
	}
	
	public String getArrayName() {
		return this.arrayName;
	}
	
	public String toString() {
		return "["+this.arrayName+"]";
	}
	
	
}
