package translator.expression;

import java.util.ArrayList;

/**
 * Represents a quantified sum. I thought long about just converting 
 * every quantified sum straight to the normal sum representation, but
 * this might cause quite an overhead, if the sum is very big. Also,
 * for some target solvers this representation might be better if the
 * expressions must not be nested and functions can be defined.
 * 
 * @author andrea
 *
 */

public class QuantifiedSum implements ArithmeticExpression {

	
	private String[] quantifiedVariables;
	
	private Domain domain;
	private Expression quantifiedExpression;
	
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	// =========== CONSTRUCTORS =============================
	
	public QuantifiedSum(String[] quantifiedVariables,
			             Domain quantifiedDomain,
			             Expression quantifiedExpression) {
	
		this.quantifiedVariables = quantifiedVariables;
		this.domain = quantifiedDomain;
		this.quantifiedExpression = quantifiedExpression;
	}
	
	public QuantifiedSum(ArrayList<String> quantifiedVars,
            			Domain quantifiedDomain,
            			Expression quantifiedExpression) {

		this.quantifiedVariables = new String[quantifiedVars.size()];
		for(int i=quantifiedVars.size()-1; i>=0; i--)
			this.quantifiedVariables[i] = quantifiedVars.remove(i);
		
		this.domain = quantifiedDomain;
		this.quantifiedExpression = quantifiedExpression;
	}
	
	
	// ========== INHERITED METHODS =========================
	
	public ArithmeticExpression copy() {
		String[] copiedVariables = new String[this.quantifiedVariables.length];
		
		for(int i=0; i<this.quantifiedVariables.length; i++)
			copiedVariables[i] = new String(this.quantifiedVariables[i]);
		
		return new QuantifiedSum(copiedVariables,
				                 this.domain.copy(),
				                 this.quantifiedExpression.copy());
	}

	public ArithmeticExpression evaluate() {
		this.quantifiedExpression = this.quantifiedExpression.evaluate();
		this.domain = this.domain.evaluate();
		return this;
	}

	public int[] getDomain() {
		return new int[] {Expression.LOWER_BOUND, Expression.UPPER_BOUND};
	}

	public int getType() {
		return Q_SUM;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.domain = this.domain.insertValueForVariable(value, variableName);
		this.quantifiedExpression = this.quantifiedExpression.insertValueForVariable(value, variableName);
		return this;
	}

	public Expression insertValueForVariable(boolean value, String variableName) {
		//this.domain = this.domain.insertValueForVariable(value, variableName);
		this.quantifiedExpression = this.quantifiedExpression.insertValueForVariable(value, variableName);
		return this;
	}

	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		this.quantifiedExpression = this.quantifiedExpression.replaceVariableWithExpression(variableName, expression);
		return this;
	}
	
	public char isSmallerThanSameType(Expression e) {
	
		QuantifiedSum otherSum = (QuantifiedSum) e;
		
		if(this.quantifiedVariables.length == otherSum.quantifiedVariables.length) {
			
			if(this.quantifiedExpression.getType() == otherSum.quantifiedExpression.getType()) {
				return this.quantifiedExpression.isSmallerThanSameType(otherSum.quantifiedExpression);
			}
			else return (this.quantifiedExpression.getType() < otherSum.quantifiedExpression.getType()) ?
					SMALLER : BIGGER;
		}
		else 
		return (this.quantifiedVariables.length < otherSum.quantifiedVariables.length) ?
				SMALLER : BIGGER;
	}

	public void orderExpression() {
		//if(this.quantifiedExpression.getType() != EQ &&
		//		this.quantifiedExpression.getType() != NEQ) 
		this.quantifiedExpression.orderExpression();

	}

	public Expression reduceExpressionTree() {
		this.quantifiedExpression = this.quantifiedExpression.reduceExpressionTree();
		return this;
	}
	
	public String toString() {
		
		String s = "sum "+quantifiedVariables[0];
		
		for(int i=1; i<this.quantifiedVariables.length; i++)
			s = s.concat(","+quantifiedVariables[i]);
		
		s = s.concat(": "+this.domain+".\n");
		s = s.concat("\t"+this.quantifiedExpression);
		
		
		return s;
		
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
		
		if(this.domain instanceof IdentifierDomain) {
			String domainName = ((IdentifierDomain) this.domain).getDomainName();
			
			if(domainName.equals(variableName)) 
				this.domain= domain;	
		}
		
		
		this.quantifiedExpression = this.quantifiedExpression.insertDomainForVariable(domain, variableName);
		
		return this;
	}
	
    public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		
		this.quantifiedExpression = this.quantifiedExpression.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
    
    public boolean isLinearExpression() {
		//return this.quantifiedExpression.isLinearExpression();
    	return false;
	}
    
    public String toSolverExpression(translator.solver.TargetSolver solver) 
	throws Exception {
		
		
		throw new Exception("Internal error. Cannot give direct solver representation of expression '"+this
			+"' for solver "+solver.getSolverName());
	}
    
	// ================ OTHER METHODS ======================================
	
	public Domain getQuantifiedDomain() {
		return this.domain;
	}
	
	public String[] getQuantifiedVariables() {
		return this.quantifiedVariables;
	}

	public Expression getQuantifiedExpression() {
		return this.quantifiedExpression;
	}
	
	
	public void updateDomain(Domain domain) {
		this.domain = domain;
	}
}
