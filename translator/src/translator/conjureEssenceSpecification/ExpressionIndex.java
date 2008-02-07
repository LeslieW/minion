package translator.conjureEssenceSpecification;

/**
 * Any array index that consists soly of a single expression,
 * for instance x[1], x[y], x[i+3] etc where '1', 'y' and 'i+3'
 * are expressions.
 * 
 * @author andrea
 *
 */

public class ExpressionIndex implements Index {

	Expression expression;
	
	
	public ExpressionIndex(Expression indexExpression) {
		this.expression = indexExpression;
	}
	
	// ======= INHERITED METHODS ==============
	
	
	public Index copy() {
		return new ExpressionIndex(this.expression.copy());
	}

	public int getType() {
		return EssenceGlobals.EXPRESSION_INDEX;
	}

	public Expression getIndexExpression() {
		return this.expression;
	}
	
	public String toString() {
		return expression.toString();
	}
	
}
