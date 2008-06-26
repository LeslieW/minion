package translator.expression;

public class ArithmeticAtomExpression implements ArithmeticExpression,AtomExpression {

	private int constantValue;
	private Variable variable;
	
	private int type;
	private boolean isParameter;
	private boolean isNested;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	
	public ArithmeticAtomExpression(int value) {
		this.constantValue = value;
		this.type = INT;
		this.isParameter = false;
		this.isNested = true;
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
		this.type = (this.variable.getType() == Expression.ARRAY_VARIABLE ||
					this.variable.getType() == Expression.SIMPLE_ARRAY_VARIABLE) ?
				Expression.INT_ARRAY_VAR :
					Expression.INT_VAR; 
			
			//this.variable.getType();
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
		this.type = (this.variable.getType() == Expression.ARRAY_VARIABLE ||
					this.variable.getType() == Expression.SIMPLE_ARRAY_VARIABLE) ?
				Expression.INT_ARRAY_VAR :
					Expression.INT_VAR;
		this.isParameter = false;
	}
	
  //	============== Interfaced methods  ==================
	
	public ArithmeticAtomExpression copy() {
		
		return (this.variable == null) ?
			new ArithmeticAtomExpression(this.constantValue) :
				new ArithmeticAtomExpression((Variable) this.variable.copy());
	
	}

	
	public int[] getDomain() {

		if(variable == null) {
			return new int[] {this.constantValue, this.constantValue};
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
	
	
	public ArithmeticAtomExpression evaluate() {
		if(this.variable != null)
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
	
	
	public RelationalAtomExpression toRelationalAtomExpression() {
		if(variable == null) {
			return (this.constantValue > 0) ?
			new RelationalAtomExpression(true) :
				new RelationalAtomExpression(false);
		}
		else return new RelationalAtomExpression(this.variable);
	}
	
	public Expression reduceExpressionTree() {
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		
		//System.out.println("inserting value for "+variableName+" with value "+value+" does it match?:"+this.toString());
		
		if(this.variable != null) {
			// if this is a simple decision variable
			if(this.variable instanceof SingleVariable || this.variable instanceof SimpleVariable) {
				Expression e = this.variable.insertValueForVariable(value,variableName);
				if(e.getType() == INT)
					return (ArithmeticAtomExpression) e;
				else return this;
			}
			
			// this is an array element
			else {
				this.variable = (Variable) this.variable.insertValueForVariable(value,variableName);
				this.variable.evaluate();
				return this;
			}
		}
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		if(this.variable != null) {
			if(variable instanceof SingleVariable)
				return this;
			this.variable = (Variable) this.variable.insertValueForVariable(value, variableName);
		}
		
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		if(this.variable != null) {
			Expression e = this.variable.replaceVariableWithExpression(variableName, expression);
			if(!(e instanceof Variable) ) {
				return e; 
			}
			else this.variable = (Variable) e;
		}
		return this;
	}
	
	public boolean isNested() {
		return isNested;
	}
	
	public void setIsNotNested() {
		this.isNested = false;
	}
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}
	
	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;
	}
	
	public Expression restructure() {
		return this;	
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		if(this.variable != null) {
			Expression e = this.variable.insertDomainForVariable(domain, variableName);
			if(e instanceof Variable) {
				this.variable = (Variable) e;
				//System.out.println("1 Inserted domain "+domain+" for variable "+variableName+" in: "+this+
				//" and variable "+variable+" is of type:"+variable.getClass().getSimpleName());
			}
		}
		//if(variable != null)
		//System.out.println("2 Inserted domain "+domain+" for variable "+variableName+" in: "+this+
		//		" and variable is of type:"+variable.getClass().getSimpleName());
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(this.variable != null) {
			if(this.variable.getVariableName().equals(oldVariable.getVariableName()))
				this.variable = newVariable;
		}
		return this;
	}
	
}
