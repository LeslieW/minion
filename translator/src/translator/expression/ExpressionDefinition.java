package translator.expression;

public class ExpressionDefinition implements Definition {

	private String name;
	private Expression expression;
	private Domain domain;
	
	
	public ExpressionDefinition(String name,
								Expression expression) {
		
		this.name = name;
		this.expression = expression;
	}
	
	public ExpressionDefinition(String name,
								Expression expression,
								Domain domain) {
		this.name = name;
		this.expression = expression;
		this.domain = domain;
	}
	
	// ========== INHERITED METHODS =====================
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		
		String s = "letting "+name;
		s = (this.domain == null) ? s : s+" : "+this.domain;
		
		return s+" be "+expression;
	}

	
	//============ ADDITIONAL METHOPDS +================
	
	public Expression getExpression() {
		return this.expression;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
}
