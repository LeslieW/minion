package translator.expression;

public interface BinaryRelationalExpression extends RelationalExpression {

	public Expression getLeftArgument();
	public Expression getRightArgument();
	public int getOperator();
	
}
  