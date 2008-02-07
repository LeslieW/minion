package translator.expression;

public class ExpressionParameter implements Parameter {

	private Expression expression;
	private String parameterName;
	
	// ============ CONSTRUCTOR ============================
	
	public ExpressionParameter(Expression expression,
			                   String parameterName) {
		this.expression = expression;
		this.parameterName = parameterName;
	}
	
	
	// =========== INHERITED METHOD =========================
	
	public ExpressionParameter applyRestriction(Expression whereRestriction) {
		
		// if the expression is arithmetic and not relational, it
		// cannot be a restriction an we ignore it
		if(whereRestriction instanceof ArithmeticExpression) 
			return this;
		
		//TODO: else: we apply the expression. Still, I don't really know
		// what kind of expressions this can be. Only param < Const like?
		
		return this;
	}

	public ExpressionParameter evaluate() {
		this.expression = this.expression.evaluate();
		return this;
	}

	public String getParameterName() {
		return this.parameterName;
	}

	public int getType() {
		return EXPRESSION;
	}

	
	public String toString() {
		return this.parameterName+": "+this.expression;	
	}
	
	// ============= OTHER METHODS =================================
	
	public Expression getExpression() {
		return this.expression;
	}
}
