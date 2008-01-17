package translator.tailor.minion;

import translator.expression.ConstantDomain;

public class MinionIndexedArray implements MinionArray {

	
	private ConstantDomain[] indices;
	private String name;
	boolean isRow;
	
	
	public MinionIndexedArray(ConstantDomain[] indices,
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
