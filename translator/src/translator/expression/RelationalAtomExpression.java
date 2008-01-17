package translator.expression;


/**
 * Relational atom expressions are either boolean constants
 * or variables of type boolean.
 * 
 * @author andrea
 *
 */

public class RelationalAtomExpression implements
		  RelationalExpression {

	private boolean bool;
	private Variable variable;
	private boolean isParameter;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
   //============== Constructors ==================
	
	public RelationalAtomExpression(boolean bool) {
		this.bool = bool;
		variable = null;
		this.isParameter = false;
	}
	
	public RelationalAtomExpression(Variable variable) {
		this.variable = variable;
		this.isParameter = false;
	}
	
	public RelationalAtomExpression(Variable variable, boolean isParameter) {
		this.variable = variable;
		this.isParameter = isParameter;
	}
	
	
	//============== Interfaced methods  ==================
	
	public ArithmeticAtomExpression toArithmeticExpression() {
		
		if(variable == null) { 
			return (bool) ?
					new ArithmeticAtomExpression(1) : 
						new ArithmeticAtomExpression(0);
		}
		else 
			return new ArithmeticAtomExpression(this.variable, isParameter);
	}
	
	public Expression copy() {
		return (variable == null) ? 
				new RelationalAtomExpression(bool) :
					new RelationalAtomExpression((Variable) this.variable.copy());
	}

	public int[] getDomain() {
		if(variable == null) {
			return (this.bool) ? 
					new int[] {1,1} :
						new int[] {0,0};
		}
		else return new int[] {0,1};
	}

	public int getType() {
		if(variable == null)
			return BOOL;
		
		else return (variable.getType() == Expression.ARRAY_VARIABLE) ?
				Expression.BOOL_ARRAY_VAR :
					Expression.BOOL_VARIABLE;
	}
	
	public void orderExpression() {
		// do nothing
	}
	
	public String toString() {
		if(variable == null) {
			return (this.bool) ?
					"true" : "false";
		}
		else return variable.toString();
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		RelationalAtomExpression otherAtom = (RelationalAtomExpression) e;
		
		if(this.variable != null) {
			if(this.bool == otherAtom.bool) return EQUAL;
			else return (!this.bool &  otherAtom.bool) ?
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

	
	public RelationalAtomExpression evaluate() {
		if(this.variable != null)
			this.variable = (Variable) this.variable.evaluate();
		return this;
	}
	

	public RelationalAtomExpression reduceExpressionTree() {
		return this;
	}
	
	//========================= OTHER METHODS ===================================
	
	public boolean getBool(){
		return this.bool;
	}
	
	public Variable getVariable() {
		return this.variable;
	}
	
	public boolean isParameter() {
		return this.isParameter;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		
		if(this.variable != null) {
			this.variable = (Variable) this.variable.insertValueForVariable(value, variableName);
		}
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		
		if(this.variable != null) {
			System.out.println("Inserting "+value+" for constant "+variableName+" in "+this);	
			if(this.variable instanceof SingleVariable) {
				System.out.println("Inserting "+value+" for constant "+variableName+" in "+this);	
				Expression e = this.variable.insertValueForVariable(value,variableName);
				if(e.getType() == BOOL)
					return (RelationalAtomExpression) e;
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
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		return this;
	}
}
