package translator.expression;

/**
 * Represents domains that consist of a set of sparse 
 * integer elements, for instance the domain (1,5,32,512). 
 * The sparse elements in the domain have to be ordered!
 * 
 * @author andrea
 *
 */

public class SparseIntRange implements IntRange {

	private int[] sparseElements;
	
	// =============== CONSTRUCTOR ==================
	
	public SparseIntRange(int[] sparseElements) {
		this.sparseElements = sparseElements;
	}
	
	// =========== INHERITED METHODS ==================
	
	public int[] getFullDomain() {
		return this.sparseElements;
	}

	public int[] getRange() {
		return this.sparseElements;
	}

	public Domain copy() {
		int[] newSparseElements = new int[this.sparseElements.length];
		for(int i=0; i<newSparseElements.length; i++)
			newSparseElements[i] = this.sparseElements[i];
		
		return new SparseIntRange(newSparseElements);
	}

	public Domain evaluate() {
		return this;
	}

	public int getType() {
		return INT_SPARSE;
	}

	public String toString() {
		String s = "int(";
		if(this.sparseElements.length > 0)
			s = s.concat(""+this.sparseElements[0]);
			
		for(int i=1;i<this.sparseElements.length; i++)
			s = s.concat(", "+sparseElements[i]);
		
		return s+")";
	}

	public boolean isConstantDomain() {
		return true;
	}
	
	public ExpressionRange toExpressionRange() {
		
		Expression[] sparseExprElements = new Expression[this.sparseElements.length];
		for(int i=0; i<this.sparseElements.length; i++) {
			sparseExprElements[i] = new ArithmeticAtomExpression(sparseElements[i]);
		}
		
		return new SparseExpressionRange(sparseExprElements);
	}
	
	
	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		
		SparseIntRange otherSparse = (SparseIntRange) d;
		
		if(this.sparseElements.length == otherSparse.sparseElements.length) {
			
			for(int i=0; i<this.sparseElements.length; i++) {	
				if(this.sparseElements[i] != otherSparse.sparseElements[i]) 
					return (this.sparseElements[i] < otherSparse.sparseElements[i]) ?
						Expression.SMALLER : Expression.BIGGER;
			}
			return Expression.EQUAL;
		}
		else return (this.sparseElements.length < otherSparse.sparseElements.length) ?
				Expression.SMALLER : Expression.BIGGER;
	}
	
	
}
