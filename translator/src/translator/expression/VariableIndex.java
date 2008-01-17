package translator.expression;

import translator.expression.ArithmeticAtomExpression;

/**
 * Represents indices in arrays that are represented by an
 * identifier, for instance a variable or a parameter.
 * 
 * @author andrea
 *
 */

public class VariableIndex implements Index {

	private Variable indexVariable;
	
	public VariableIndex(Variable index) {
		this.indexVariable = index;
	}
	
	// ======= INHERITED METHODS ====================
	
	public Index copy() {
		return new VariableIndex((Variable) this.indexVariable.copy());
	}

	public Index evaluate() {
		this.indexVariable = (Variable) this.indexVariable.evaluate();
		return this;
	}

	public int getType() {
		return Index.VAR_INDEX;
	}

	public Index insertValueForVariable(int value, String variableName) {
		Expression insertion = this.indexVariable.insertValueForVariable(value, variableName);
		if(insertion.getType() == INT_INDEX)
			return new IntIndex( ((ArithmeticAtomExpression) insertion).getConstant());
		
		return this;
	}

	public boolean isConstantIndex() {
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
