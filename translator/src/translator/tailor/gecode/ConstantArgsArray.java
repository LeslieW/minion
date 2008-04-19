package translator.tailor.gecode;

public class ConstantArgsArray implements ArgsAtom {
	
	private String name = null;
	private int[] values;
	
	
	public ConstantArgsArray(int[] values) {
		this.values = values;
	}
	
	public ConstantArgsArray(String name, int[] values) {
		this.values = values;
		this.name = name;
	}
	
	public String toString() {
		
		if(this.name == null) {
			StringBuffer s = new StringBuffer("[");
			for(int i=0; i<this.values.length-1; i++) {
				s.append(values[i]+", ");
			}
			s.append(values[values.length-1]+"]");
			return s.toString();
		}
		else return this.name;
	}
	
	public String toCCString() {
		return "IntArgs "+name;
	}
}
