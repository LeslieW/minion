package translator.expression;

/**
 * This interface is for all global constraints that have more than 2
 * arguments. If the global constraint has 1 argument, then please
 * implement the UnaryRelationalExpression interface and for 2 arguments
 * please use the NonCommutativeRelationalBinaryExpression interface
 *
 * @author andrea
 *
 */

public interface GlobalConstraint extends RelationalExpression {

	public Expression[] getArguments();
	
}
