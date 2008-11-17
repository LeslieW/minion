package translator.expression;

import java.util.ArrayList;

public class QuantifiedExpression implements RelationalExpression {

	
	private boolean isUniversal;
	private String[] quantifiedVariables;
	
	private Domain domain;
	private Expression quantifiedExpression;
	private boolean isNested = true;
	private boolean willBeReified = false;
	private boolean isNestedInConjunction = false;
	private boolean isNestedInDisjunction = false;
	
	// ===================== CONSTRUCTORS =======================
	
	public QuantifiedExpression(boolean isUniversal,
			                    String[] quantifiedVariables,
			                    Domain domain,
			                    Expression quantifiedExpression
			                    ) {
		
		this.isUniversal = isUniversal;
		this.quantifiedVariables = quantifiedVariables;
		this.domain = domain;
		this.quantifiedExpression = quantifiedExpression;
	}

	public QuantifiedExpression(boolean isUniversal,
            					ArrayList<String> quantifiedVars,
            					Domain domain,
            					Expression quantifiedExpression
            	                ) {

		this.isUniversal = isUniversal;
		this.quantifiedVariables = new String[quantifiedVars.size()];
		for(int i=quantifiedVars.size()-1; i>=0; i--)
			this.quantifiedVariables[i] = quantifiedVars.remove(i);
		
		this.domain = domain;
		this.quantifiedExpression = quantifiedExpression;
	}

	
	//=================== INHERITED METHODS ======================
	
	public Expression copy() {
		
		Expression copiedQuantifiedExpression = this.quantifiedExpression.copy();
		String[] copiedQuantifiers = new String[this.quantifiedVariables.length];
		for(int i=0; i<this.quantifiedVariables.length; i++)
			copiedQuantifiers[i] = this.quantifiedVariables[i];
		
		Domain copiedDomain = this.domain.copy();
	
		return new QuantifiedExpression(this.isUniversal,
				                        copiedQuantifiers,
				                        copiedDomain,
				                        copiedQuantifiedExpression);
	}

	public Expression evaluate() {
		this.quantifiedExpression = this.quantifiedExpression.evaluate();
		this.domain = this.domain.evaluate();
		
		return this;
	}

	
	
	public int[] getDomain() {	
		return new int[] { 0,1} ;
	}

	
	
	public int getType() {
		return (isUniversal) ?
				FORALL : EXISTS;
	}

	
	
	public char isSmallerThanSameType(Expression e) {
		
		QuantifiedExpression otherQuantification = (QuantifiedExpression) e;
		
		// if we have the same amount of quantifiers, we compare the expressions
		if(this.quantifiedVariables.length == otherQuantification.quantifiedVariables.length){
			if(this.quantifiedExpression.getType() == 
				otherQuantification.quantifiedExpression.getType())	
			return this.quantifiedExpression.isSmallerThanSameType
				(otherQuantification.quantifiedExpression);
			else return (this.quantifiedExpression.getType() < 
				otherQuantification.quantifiedExpression.getType()) ?
						SMALLER : BIGGER;
		}
		
		else return (this.quantifiedVariables.length < otherQuantification.quantifiedVariables.length) ?
				SMALLER : BIGGER;
	}

	public void orderExpression() {
		this.quantifiedExpression.orderExpression();
		this.quantifiedVariables = lexOrderStringList(this.quantifiedVariables);
	}

	public Expression reduceExpressionTree() {
		this.quantifiedExpression = this.quantifiedExpression.reduceExpressionTree();
		return this;
	}

	
	
	//=================================== OTHER METHODS ======================================
	
	
	public void updateDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Domain getQuantifiedDomain() {
		return this.domain;
	}
	
	public Expression getQuantifiedExpression() {
		return this.quantifiedExpression;
	}
	
	public String[] getQuantifiedVariables() {
		return this.quantifiedVariables;
	}
	
	
	protected String[] lexOrderStringList(String[] stringList){
		
		ArrayList<String> orderedStringList = new ArrayList<String>();
		
		for(int i=0; i<stringList.length; i++) 
			orderedStringList = insertIntoOrderedList(stringList[i], orderedStringList);
		
		for(int i=0; i<stringList.length; i++) 
			stringList[i] = orderedStringList.get(i);
		
		
		return stringList;
	}
	
	
	
	protected ArrayList<String> insertIntoOrderedList(String s, ArrayList<String> orderedList) {
		
		if(orderedList.size() ==0) {
			orderedList.add(s);
			return orderedList;
		}
			
		for(int i=0; i< orderedList.size(); i++) {
			String otherString = orderedList.get(i);
			
			if(otherString.compareTo(s) >= 0) {
				orderedList.add(i, s);
				return orderedList;
			}
		}
		orderedList.add(s);
		
		return orderedList;
		
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		
		this.domain.insertValueForVariable(value, variableName);
		//System.out.println("inserted value in domain:"+domain);
		this.quantifiedExpression.insertValueForVariable(value, variableName);
		//System.out.println("inserted value in qExpression:"+quantifiedExpression);
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		
		//this.domain.insertValueForVariable(value, variableName);
		this.quantifiedExpression.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		this.quantifiedExpression = this.quantifiedExpression.replaceVariableWithExpression(variableName, expression);
		return this;
	}


	public String toString() {
		
		String s = (isUniversal) ? 
				"forall " : "exists ";
		
		s = s.concat(quantifiedVariables[0]);
	
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
		this.quantifiedExpression = this.quantifiedExpression.restructure();		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		
		if(this.domain instanceof IdentifierDomain) {
			String domainName = ((IdentifierDomain) this.domain).getDomainName();
			
			
			if(domainName.equals(variableName)) 
				this.domain= domain;	
		}
		
		// add the quantified domain as the domain of each binding variable
		for(int i=0; i<this.quantifiedVariables.length; i++) {
			String quantifiedVar = quantifiedVariables[i];
			//System.out.println("inserting binding domain "+this.domain+" for quantified variable "+quantifiedVar+" in quantified expr:"+this);
			this.quantifiedExpression = this.quantifiedExpression.insertDomainForVariable(this.domain, quantifiedVar);
		}
		
		
		this.quantifiedExpression = this.quantifiedExpression.insertDomainForVariable(domain, variableName);
		
		return this;
	}
	
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		
		this.quantifiedExpression = this.quantifiedExpression.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
	
	public boolean isLinearExpression() {
		return false;
		//return this.quantifiedExpression.isLinearExpression();
	}
	
	public String toSolverExpression(translator.solver.TargetSolver solver) 
	throws Exception {
		
		
		throw new Exception("Internal error. Cannot give direct solver representation of expression '"+this
			+"' for solver "+solver.getSolverName());
	}

	public boolean isNestedInConjunction() {
		return this.isNestedInConjunction;
	}
	
	public boolean isNestedInDisjunction() {
		return this.isNestedInDisjunction;
	}
	
	public void setIsNestedInConjunction(boolean turnOn) {
		this.isNestedInConjunction = turnOn;
	}
	
	public void setIsNestedInDisjunction(boolean turnOn) {
		this.isNestedInDisjunction = turnOn;
	}
}
