package translator.expression;

public class Array implements Expression {

	private String arrayName;
	private Index[] indexRanges;
	private Domain baseDomain;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ========== CONSTRUCTOR =============
	
	public Array(String arrayName,
			     Index[] indexRanges,
			     Domain baseDomain) {
		
		this.arrayName = arrayName;
		this.indexRanges = indexRanges;
		this.baseDomain = baseDomain;
	}
	
	
	// ======== INHERITED MEHTODS ==========
	
	public Expression copy() {
		Index[] copiedIndexRanges = new Index[this.indexRanges.length];
		for(int i=0; i<this.indexRanges.length; i++)
			copiedIndexRanges[i] = this.indexRanges[i].copy();
		return new Array(new String(this.arrayName),
				         copiedIndexRanges,
				         this.baseDomain.copy());
	}

	public Expression evaluate() {
		for(int i=0; i<this.indexRanges.length; i++)
			this.indexRanges[i] = this.indexRanges[i].evaluate();
	
		return this;
	}

	public int[] getDomain() {
		if(this.baseDomain instanceof ConstantDomain) {
			return ((ConstantDomain) baseDomain).getRange();
		}
		else return new int[] { Expression.LOWER_BOUND, Expression.UPPER_BOUND };
	}

	public int getType() {
		return Expression.ARRAY;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.indexRanges.length; i++)
			this.indexRanges[i] = this.indexRanges[i].insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		int f;
		// TODO Auto-generated method stub
		return 0;
	}

	public void orderExpression() {
		// do nothing
	}

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;
	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattenedToVariable = reified;

	}

}
