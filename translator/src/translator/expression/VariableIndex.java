package translator.expression;

import translator.expression.ArithmeticAtomExpression;

/**
 * Represents indices in arrays that are represented by an
 * identifier, for instance a variable or a parameter.
 * 
 * @author andrea
 *
 */

public class VariableIndex implements ArrayIndex {

	private Variable indexVariable;
	
	public VariableIndex(Variable index) {
		this.indexVariable = index;
	}
	
	// ======= INHERITED METHODS ====================
	
	public Domain copy() {
		return new VariableIndex((Variable) this.indexVariable.copy());
	}

	public Domain evaluate() {
		this.indexVariable = (Variable) this.indexVariable.evaluate();
		return this;
	}

	public int getType() {
		return Domain.VAR_INDEX;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		Expression insertion = this.indexVariable.insertValueForVariable(value, variableName);
		if(insertion.getType() == INT_INDEX)
			return new IntIndex( ((ArithmeticAtomExpression) insertion).getConstant());
		
		return this;
	}

	public boolean isConstantDomain() {
		return false;
	}

	public String toString() {
		return this.indexVariable.toString();
	}
	
	// =========== ADDITIONAL METHODS =============================
	
	public Variable getVariable() {
		return this.indexVariable;
	}
}
