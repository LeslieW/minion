package translator.expression;

public class ExpressionRangeIndex implements Index {

	Expression lowerIndex;
	Expression upperIndex;
	int type;
	
	// ================== CONSTRUCTORS ======================
	
	public ExpressionRangeIndex (Expression lowerIndex,
			           Expression upperIndex) {
		
		this.lowerIndex = lowerIndex;
		this.upperIndex = upperIndex;
		this.type = Index.LOWER_UPPER_RANGE_EXPR_INDEX;
	}
	
	
	public ExpressionRangeIndex(Expression index,
					boolean isLowerIndex) {
		if(isLowerIndex)
			this.lowerIndex = index;
		
		else this.upperIndex = index;
		this.type = (isLowerIndex) ? 
				Index.LOWER_RANGE_EXPR_INDEX :
					Index.UPPER_RANGE_EXPR_INDEX;
		
	}
	
	// ============== INHERITED METHODS =====================
	
	public Index copy() {
		if(this.type == Index.LOWER_RANGE_EXPR_INDEX)
			return new ExpressionRangeIndex(this.lowerIndex.copy(), true);
		
		else if (this.type == Index.UPPER_RANGE_EXPR_INDEX)
			return new ExpressionRangeIndex(this.upperIndex.copy(), false);
		
		else return new ExpressionRangeIndex(this.lowerIndex.copy(),
								   this.upperIndex.copy());
	}

	public Index evaluate() {
		if(this.lowerIndex != null) 
			this.lowerIndex = this.lowerIndex.evaluate();
		
		if(this.upperIndex != null)
			this.upperIndex = this.upperIndex.evaluate();
		
		if(this.upperIndex != null && this.upperIndex.getType() == Expression.INT) {
			
			if(this.type == Index.UPPER_RANGE_EXPR_INDEX)
				return new IntRangeIndex( ((ArithmeticAtomExpression) this.upperIndex).getConstant(), false);
			
			else if(this.type == Index.LOWER_UPPER_RANGE_EXPR_INDEX && this.lowerIndex.getType() == Expression.INT) 
				return new IntRangeIndex(((ArithmeticAtomExpression) this.lowerIndex).getConstant(),
					                	((ArithmeticAtomExpression) this.upperIndex).getConstant()); 
			
		}
		else if(this.type == Index.LOWER_RANGE_EXPR_INDEX && this.lowerIndex.getType() == Expression.INT)
			return new IntRangeIndex(((ArithmeticAtomExpression) this.lowerIndex).getConstant(), true);
		
		return this;
	}

	public int getType() {
		return this.type;
	}

	public Index insertValueForVariable(int value, String variableName) {
		if(this.lowerIndex != null)
			this.lowerIndex = this.lowerIndex.insertValueForVariable(value, variableName);
		
		if(this.upperIndex != null)
			this.upperIndex = this.upperIndex.insertValueForVariable(value, variableName);
		
		return this.evaluate();
	}

	public boolean isConstantIndex() {
		return false;
	}

	// ============== ADDITIONAL MEHTODS ==================================
	
	public Expression getLowerExpressionIndex() {
		return this.lowerIndex;
	}
	
	public Expression getUpperExpressionIndex() {
		return this.upperIndex;
	}
	
}
