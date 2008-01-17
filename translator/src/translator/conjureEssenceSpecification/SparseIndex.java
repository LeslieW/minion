package translator.conjureEssenceSpecification;

public class SparseIndex implements Index {

	Expression[] sparseElements;
	
	
	public SparseIndex(Expression[] sparseElements) {
		this.sparseElements = sparseElements;
	}
	
	
	public Index copy() {
		Expression[] copiedSparseElements = new Expression[this.sparseElements.length];
		for(int i=0; i<this.sparseElements.length; i++)
			copiedSparseElements[i] = this.sparseElements[i].copy();
		
		return new SparseIndex(copiedSparseElements);
	}

	public int getType() {
		return EssenceGlobals.SPARSE_INDEX;
	}

	public String toString() {
		String s = "(";
		
		for(int i=0; i<this.sparseElements.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(sparseElements[i].toString());
		}
			
		
		return s+")";
	}
	
	public Expression[] getSparseElements() {
		return this.sparseElements;
	}
	
}
