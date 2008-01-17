package translator.expression;

/**
 * Represents a product constraint of the form:
 * arguments[0] * arguments[1] * ... * arguments[n] = result
 * 
 * where there can be an arbitrary amount of arguments. This representation
 * is only revelant when flattening and for interfacing to certain 
 * solvers.
 * 
 * @author andrea
 *
 */

public class ProductConstraint implements GlobalConstraint {

	private Expression[] arguments;
	private Expression result;
	
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	// ============ constructor ================================
	

	public ProductConstraint(Expression[] arguments,
			                 Expression result) {
		this.arguments = arguments;
		this.result = result;
	}
	
	
	// =========== INHERITED METHODS ===========================
	
	public Expression[] getArguments() {
		return this.arguments;
	}

	public Expression copy() {
		Expression[] copiedArguments = new Expression[this.arguments.length];
		for(int i=0; i<this.arguments.length; i++)
			copiedArguments[i] = this.arguments[i].copy();

		
		return new ProductConstraint(copiedArguments,
				                     this.result.copy());
	}

	public Expression evaluate() {
		for(int i=0; i<this.arguments.length; i++)
			this.arguments[i] = this.arguments[i].evaluate();
		
		this.result = this.result.evaluate();
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return (this.arguments.length == 2) ?
				Expression.BINARY_PRODUCT_CONSTRAINT :Expression.NARY_PRODUCT_CONSTRAINT;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.arguments.length; i++)
			this.arguments[i] = this.arguments[i].insertValueForVariable(value, variableName);
		this.result = this.result.insertValueForVariable(value, variableName);
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		ProductConstraint otherProduct = (ProductConstraint) e;
		
		if(this.arguments.length == otherProduct.arguments.length) {
		
			for(int i=0; i<this.arguments.length; i++) {
			
				if(this.arguments[0].getType() == otherProduct.arguments[i].getType()) {	
					char difference = arguments[0].isSmallerThanSameType(otherProduct.arguments[i]);
					if(difference != EQUAL) return difference;
				}
				else return (this.arguments[0].getType() < otherProduct.arguments[i].getType()) ?
						SMALLER : BIGGER;
			}
			
			if(this.result.getType() == otherProduct.result.getType()) {
				return this.result.isSmallerThanSameType(otherProduct.result);
			}
			else return (this.result.getType() < otherProduct.result.getType()) ?
					SMALLER : BIGGER;
			
		}
		else return (this.arguments.length < otherProduct.arguments.length) ?
				SMALLER : BIGGER;
	}

	public void orderExpression() {
		for(int i=0; i<this.arguments.length; i++)
			this.arguments[i].orderExpression();
		
		this.result.orderExpression();
		
	}

	public Expression reduceExpressionTree() {
		for(int i=0; i<this.arguments.length; i++)
			this.arguments[i] = this.arguments[i].reduceExpressionTree();
		
		this.result = this.result.reduceExpressionTree();
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;

	}
	
	public String toString() {
		
		String s = ""+this.arguments[0];
		
		for(int i=1; i<this.arguments.length; i++)
			s = s.concat(" * "+arguments[i]);
		
		return s+" = "+this.result;
		
	}

	public Expression restructure() {
		this.result = this.result.restructure();
		
		for(int i=0; i<this.arguments.length; i++) 
			this.arguments[i] = this.arguments[i].restructure();
		
		return this;
	}
	
	// ==================== ADDITIONAL METHODS ============================
	
	public Expression[] getProductArguments() {
		return this.arguments;
	}
	
	public Expression getResult() {
		return this.result;
	}
	
}
