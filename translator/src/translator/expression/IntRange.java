package translator.expression;

/**
 * This interface represents every domain that consists of integer
 * values.
 * 
 * @author andrea
 *
 */

public interface IntRange extends ConstantDomain {

	public ExpressionRange toExpressionRange();
	
}
