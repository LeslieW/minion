package translator.expression;

import java.util.ArrayList;

public class QuantifiedExpression implements RelationalExpression {

	
	private boolean isUniversal;
	private String[] quantifiedVariables;
	
	private Domain domain;
	private Expression quantifiedExpression;
	
	
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
			return this.quantifiedExpression.isSmallerThanSameType(otherQuantification.quantifiedExpression);
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
			
			if(otherString.compareTo(s) <= 0) {
				orderedList.add(i, s);
				return orderedList;
			}
		}
		orderedList.add(s);
		
		return orderedList;
		
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		
		this.domain.insertValueForVariable(value, variableName);
		this.quantifiedExpression.insertValueForVariable(value, variableName);
		
		return this;
	}


	public String toString() {
		
		String s = (isUniversal) ? 
				"forall " : "exists ";
		
		s = s.concat(quantifiedVariables[0]);
	
		for(int i=1; i<this.quantifiedVariables.length; i++)
			s = s.concat(","+quantifiedVariables[i]);
	
		s = s.concat(": "+this.domain+"\n");
		s = s.concat("\t"+this.quantifiedExpression);
	
	
		return s;
	}
}
