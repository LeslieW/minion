package translator.tailor.minion;

import translator.expression.*;

/**
 * Represents the Minion Product constraint
 * 
 * 
 * @author andrea
 *
 */

public class ProductConstraint implements MinionConstraint {

	
	ArithmeticAtomExpression leftExpression;
	ArithmeticAtomExpression rightExpression;
	ArithmeticAtomExpression result;
	
	
	public ProductConstraint(ArithmeticAtomExpression leftArgument,
			                 ArithmeticAtomExpression rightArgument,
			                 ArithmeticAtomExpression result) {
		
		this.leftExpression = leftArgument;
		this.rightExpression = rightArgument;
		this.result = result;
	}

	
	public String toString() {
		
		return "product("+leftExpression.toString()+","+rightExpression.toString()+", "+result.toString()+")";
	
	}
}
