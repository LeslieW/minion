package translator.expression;

public interface BinaryArithmeticExpression extends ArithmeticExpression {

	public Expression getLeftArgument();
	public Expression getRightArgument();
	
}
