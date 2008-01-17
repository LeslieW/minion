package translator.expression;

/**
 * Represents Boolean domains.
 * 
 * @author andrea
 *
 */

public class BoolDomain implements ConstantDomain {

	
	public BoolDomain() {
	}
	
	public int[] getFullDomain() {
		return new int[] {0,1};
	}
	
	public int[] getRange() {
		return new int[] {0,1};
	}
	
	public Domain copy() {
		return new BoolDomain();
	}

	public Domain evaluate() {
		return this;
	}

	public int getType() {
		return BOOL;
	}

	public String toString() {
		return "(0,1)";
	}
	
	
	public boolean isConstantDomain() {
		return true;
	}
	
	public ExpressionRange toExpressionRange() {	
		return new BoundedExpressionRange(new ArithmeticAtomExpression(0),
				                          new ArithmeticAtomExpression(1));
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}
}
