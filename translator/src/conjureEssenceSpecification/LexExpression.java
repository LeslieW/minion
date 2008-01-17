package conjureEssenceSpecification;

public class LexExpression {

	Expression leftExpression;
	Expression rightExpression;
	LexOperator lexOperator;
	
	public LexExpression(Expression left, LexOperator lexOp, Expression right) {
		
		this.leftExpression = left;
		this.rightExpression = right;
		this.lexOperator = lexOp;
		
	}
	
	public Expression getLeftExpression() {
		return this.leftExpression;
	}
	
	public Expression getRightExpression() {
		return this.rightExpression;
	}
	
	public LexOperator getLexOperator() {
		return this.lexOperator;
	}
	
	public LexExpression copy() {
		return new LexExpression(leftExpression.copy(), lexOperator.copy(), rightExpression.copy());
	}
	
	public String toString() {
		return leftExpression.toString()+" "+lexOperator.toString()+" "+rightExpression.toString();
	}
	
}
