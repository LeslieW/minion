package translator.expression;

public class ArithmeticAtomExpression implements ArithmeticExpression {

	private int constantValue;
	private Variable variable;
	
	private int type;
	private boolean isParameter;
	
	//============== Constructors ==================
	
	public ArithmeticAtomExpression(int value) {
		this.constantValue = value;
		this.type = INT;
		this.isParameter = false;
	}
	
	/**
	 * constructor for the case when parameter values
	 * are given, and all variable domain bounds 
	 * are known. 
	 * @param isParameter is set to true if the atom is a parameter
	 * @param variableName
	 * @param upperBound
	 * @param lowerBound
	 */
	public ArithmeticAtomExpression(Variable variable, 
			                        boolean isParameter) {
		this.variable = variable;
		this.type = this.variable.getType();
		this.isParameter = isParameter;
	}
	
	/**
	 * constructor for the case when parameter values
	 * are given, and all variable domain bounds 
	 * are known. 
	 * @param isParameter TODO
	 * @param variableName
	 * @param upperBound
	 * @param lowerBound
	 */
	public ArithmeticAtomExpression(Variable variable) {
		this.variable = variable;
		this.type = this.variable.getType();
		this.isParameter = false;
	}
	
  //	============== Interfaced methods  ==================
	
	public Expression copy() {
		
		return (this.variable == null) ?
			new ArithmeticAtomExpression(this.constantValue) :
				new ArithmeticAtomExpression((Variable) this.variable.copy());
	
	}

	
	public int[] getDomain() {

		if(variable == null) {
			return new int[] {this.constantValue};
		}
		else 
			return variable.getDomain();
	}

	
	public int getType() {
		return this.type;
	}

	public void orderExpression() {
		// do nothing
	}
	
	public String toString() {
		return (variable == null) ?
				this.constantValue+"" :
					this.variable.toString();
					
	}

	public char isSmallerThanSameType(Expression e) {
		
		ArithmeticAtomExpression otherAtom = (ArithmeticAtomExpression) e;
		
		if(this.type == INT) {
			if(this.constantValue == otherAtom.constantValue)
				return EQUAL;
			else return (this.constantValue < otherAtom.constantValue) ?
					SMALLER : BIGGER;
		}
		else if(this.variable.getType() == otherAtom.variable.getType()){
			return this.variable.isSmallerThanSameType(otherAtom.variable);
		}
		else {	
			return (this.variable.getType() < otherAtom.variable.getType()) ?
					SMALLER : BIGGER;
		}
	}
	
	
	public Expression evaluate() {
		this.variable = (Variable) this.variable.evaluate();
		return this;
	}
	
	
	//=============== OTHER METHODS =====================================
	
	public int getConstant() {
		return this.constantValue;
	}
	
	public Variable getVariable() {
		return this.variable;
	}
	
	public boolean isParameter() {
		return this.isParameter;
	}
	
	
	public Expression reduceExpressionTree() {
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		if(this.variable != null) {
			Expression e = (SingleVariable) this.variable.insertValueForVariable(value,variableName);
			if(e.getType() == INT)
				return (ArithmeticAtomExpression) e;
		}
		
		return this;
	}
	
	
}
