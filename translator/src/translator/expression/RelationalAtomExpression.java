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

	boolean bool;
	SingleVariable variable;
	boolean isParameter;
	
   //============== Constructors ==================
	
	public RelationalAtomExpression(boolean bool) {
		this.bool = bool;
		variable = null;
		this.isParameter = false;
	}
	
	public RelationalAtomExpression(SingleVariable variableName) {
		this.variable = variableName;
		this.isParameter = false;
	}
	
	public RelationalAtomExpression(SingleVariable variableName, boolean isParameter) {
		this.variable = variableName;
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
					new RelationalAtomExpression((SingleVariable) this.variable.copy());
	}

	public int[] getDomain() {
		if(variable == null) {
			return (this.bool) ? 
					new int[] {1} :
						new int[] {0};
		}
		else return variable.getDomain();
	}

	public int getType() {
		return (variable == null) ?
				BOOL : variable.getType();
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
		else return this.variable.isSmallerThanSameType(otherAtom.variable);
	}

	
	public Expression evaluate() {
		return this;
	}
	

	public Expression reduceExpressionTree() {
		return this;
	}
	
	//========================= OTHER METHODS ===================================
	
	public boolean getBool(){
		return this.bool;
	}
	
	public SingleVariable getVariable() {
		return this.variable;
	}
	
	public boolean isParameter() {
		return this.isParameter;
	}

	
}
