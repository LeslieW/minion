package translator.tailor.minion;

public class MinionArray implements MinionConstraint {

	
	private int[] indices;
	private String name;
	boolean isRow;
	
	
	public MinionArray(int[] indices,
			           String name,
			           boolean isRow) {
		this.indices = indices;
		this.name = name;
		this.isRow = isRow;
	}
	
	
	public String toString() {
		
		String arrayAccess = (this.isRow) ? "row" : "col";
		
		String s =  arrayAccess+"("+name;
		for(int i=0; i<this.indices.length; i++) {
			if(i>0) s = s.concat(",");
			s = s.concat(indices[i]+"");
		}
		return s+")";
	}
	

	
}
