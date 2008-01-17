package translator.expression;

/**
 * Represents domains that consist of a set of sparse 
 * elements, for instance the domain (a,5,32,512*b). 
 * The sparse elements in the domain should be ordered
 * (even though we might not be able to test if the domain
 * is ordered in case there are unknown parameter values
 * in the domain, for instance)!
 * 
 * @author andrea
 *
 */

public class SparseExpressionRange implements ExpressionRange {

	Expression[] sparseElements;
	
	
	// ============ CONSTRUCTORS ==========================
	
	public SparseExpressionRange(Expression[] rangeElements) {
		this.sparseElements = rangeElements;
	}
	
	
	//============= INHERTITED METHODS ======================
	
	public Domain copy() {
		Expression[] copiedRangeElements = new Expression[this.sparseElements.length];
		for(int i=0; i<this.sparseElements.length; i++)
			copiedRangeElements[i] = this.sparseElements[i].copy();
		
		return new SparseExpressionRange(copiedRangeElements);
	}

	public Domain evaluate() {
		boolean allElementsAreIntegers = true;
		
		for(int i=0; i<this.sparseElements.length; i++) {
			sparseElements[i] = this.sparseElements[i].evaluate();
			if(sparseElements[i].getType() != Expression.INT)
				allElementsAreIntegers = false;
		}
		if(allElementsAreIntegers) {
			int[] newIntRange = new int[this.sparseElements.length];
			for(int i=0; i<this.sparseElements.length; i++)
				newIntRange[i] = ((ArithmeticAtomExpression) this.sparseElements[i]).getConstant();
			return new SparseIntRange(newIntRange);
		}
		
		return this;
	}

	public int getType() {
		return EXPR_SPARSE;
	}

	public String toString() {
		String s = "(";
		if(this.sparseElements.length > 0)
			s = ""+this.sparseElements[0].toString();
			
		for(int i=1;i<this.sparseElements.length; i++)
			s = s.concat(", "+sparseElements[i].toString());
		
		return s+")";
	}
	
	public boolean isConstantDomain() {
		return false;
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
		
		boolean allSparseElementsAreInteger = true;
		
		for(int i=0; i<this.sparseElements.length; i++) {
			this.sparseElements[i] = this.sparseElements[i].insertValueForVariable(value, variableName);
			if(this.sparseElements[i].getType() != Expression.INT) {
				allSparseElementsAreInteger = false;
			}
		}
		
		if(allSparseElementsAreInteger) {
			int[] sparseIntElements = new int[this.sparseElements.length];
			for(int i=0; i<sparseIntElements.length; i++)
				sparseIntElements[i] = ((ArithmeticAtomExpression) this.sparseElements[i]).getConstant();
			
			return new SparseIntRange(sparseIntElements);
		}
		
		return this;
	}
}
