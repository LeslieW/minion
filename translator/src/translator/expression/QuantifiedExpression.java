package translator.expression;

import java.util.ArrayList;

import translator.conjureEssenceSpecification.EssenceGlobals;
import translator.normaliser.NormaliserException;

public class QuantifiedExpression implements RelationalExpression {

	
	
	private boolean isUniversal;
	private String[] quantifiedVariables;
	
	private int[] intDomain;
	private Expression[] expressionDomain;
	private ArrayList<Expression[]> expressionDomainList;
	
	private Expression quantifiedExpression;
	
	
	// ===================== CONSTRUCTORS =======================
	
	public QuantifiedExpression(boolean isUniversal,
			                    String[] quantifiedVariables,
			                    int[] intDomain,
			                    Expression quantifiedExpression
			                    ) {
		
		this.isUniversal = isUniversal;
		this.quantifiedVariables = quantifiedVariables;
		this.intDomain = intDomain;
		this.quantifiedExpression = quantifiedExpression;
	}
	
	
	public QuantifiedExpression(boolean isUniversal,
            String[] quantifiers,
            Expression[] expressionDomain,
            Expression quantifiedExpression
            ) {

		this.isUniversal = isUniversal;
		this.quantifiedVariables = quantifiers;
		this.expressionDomain = expressionDomain;
		this.quantifiedExpression = quantifiedExpression;
	}

	public QuantifiedExpression(boolean isUniversal,
            String[] quantifiers,
            ArrayList<Expression[]> expressionDomain,
            Expression quantifiedExpression
            ) {

		this.isUniversal = isUniversal;
		this.quantifiedVariables = quantifiers;
		this.expressionDomainList = expressionDomain;
		this.quantifiedExpression = quantifiedExpression;
	}
	
	
	//=================== INHERITED METHODS ======================
	
	public Expression copy() {
		
		Expression copiedQuantifiedExpression = this.quantifiedExpression.copy();
		String[] copiedQuantifiers = new String[this.quantifiedVariables.length];
		for(int i=0; i<this.quantifiedVariables.length; i++)
			copiedQuantifiers[i] = this.quantifiedVariables[i];
		
		// we have an integer domain
		if(this.expressionDomain == null && this.expressionDomainList == null) {
			int[] copiedIntDomain = new int[this.intDomain.length];
			for(int i=0; i<this.intDomain.length; i++)
				copiedIntDomain[i] = this.intDomain[i];
			
			return new QuantifiedExpression(this.isUniversal,
					                         copiedQuantifiers,
					                         copiedIntDomain,
					                         copiedQuantifiedExpression);
		}
		else if(this.expressionDomainList == null){
			Expression[] copiedExpressionDomain = new Expression[this.expressionDomain.length];
			for(int i=0; i<this.intDomain.length; i++)
				copiedExpressionDomain[i] = this.expressionDomain[i].copy();
			
			return new QuantifiedExpression(this.isUniversal,
					                         copiedQuantifiers,
					                         copiedExpressionDomain,
					                         copiedQuantifiedExpression);			
		}
		else {
			ArrayList<Expression[]> copiedExpressionDomainList = new ArrayList<Expression[]>();
			
			for(int j=0; j<this.expressionDomainList.size(); j++) {
				Expression[] copiedExpressionDomain = new Expression[this.expressionDomainList.get(j).length];
				
				for(int i=0; i<this.intDomain.length; i++)
					copiedExpressionDomain[i] = this.expressionDomain[i].copy();
				
				copiedExpressionDomainList.add(copiedExpressionDomain);
			}
			return new QuantifiedExpression(this.isUniversal,
                 	copiedQuantifiers,
                 	copiedExpressionDomainList,
                 	copiedQuantifiedExpression);
		}
	}

	public Expression evaluate() {
		this.quantifiedExpression = this.quantifiedExpression.evaluate();
		
		// evaluate the domain if it only consists of expressions
		if(this.expressionDomain != null) {
			evaluateQuantifierDomain();
		
		}
		else if(this.expressionDomainList != null) {
			for(int i=0; i<this.expressionDomainList.size(); i++)
				;//evaluateQuantifierDomain(this.expressionDomain.get(i))
		}
		return this;
	}

	
	
	public int[] getDomain() {
		
		return new int[] { 0,1} ;
	}

	
	
	public int getType() {
		if(isUniversal)
			return FORALL;
		else return EXISTS;
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
	
	protected void evaluateQuantifierDomain() {
		
		boolean allExpressionsAreIntegers = true;
		
		if(this.expressionDomain != null) {
			for(int i=0; i<this.expressionDomain.length; i++) {
				Expression e = this.expressionDomain[i].evaluate();
				if(e.getType() == INT || e.getType() == BOOL) {
					allExpressionsAreIntegers = true && allExpressionsAreIntegers;
				}
				else allExpressionsAreIntegers = false;
			}
		
			if(allExpressionsAreIntegers) {
				int[] newIntDomain = new int[this.expressionDomain.length];
				for(int i=0; i<newIntDomain.length;i++)
					newIntDomain[i] = ((ArithmeticAtomExpression) expressionDomain[i]).getConstant();
				this.expressionDomain = null;
				this.intDomain = newIntDomain;
			}
		
		}
		else { // else: we have a expression domain list
			for(int j=0; j<this.expressionDomainList.size(); j++) {
				//evaluateQuantifierDomain(this.expressionDomain.get(i))
				for(int i=0; i<this.expressionDomainList.get(j).length; i++) {
					Expression e = this.expressionDomainList.get(j)[i].evaluate();
					if(e.getType() == INT || e.getType() == BOOL) {
						allExpressionsAreIntegers = true && allExpressionsAreIntegers;
					}
					else allExpressionsAreIntegers = false;
				}
		
			}
			if(allExpressionsAreIntegers) {
				int[] newIntDomain = new int[this.expressionDomain.length];
				for(int i=0; i<newIntDomain.length;i++)
					newIntDomain[i] = ((ArithmeticAtomExpression) expressionDomain[i]).getConstant();
				this.expressionDomain = null;
				this.intDomain = newIntDomain;
			}
		}
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
	
	
	protected int[] transformToIntegerRange(int lb, int ub) 
		throws NormaliserException {

		
		if(lb > ub) 
			throw new NormaliserException("Illegal bounds for quantification: lowerbound '"+lb+"' is greater than upperBound '"
					+ub+"'");
		
		
		int[] quantifiedRange = new int[(ub-lb)+1];
		for(int i=0; i<quantifiedRange.length; i++) {
			quantifiedRange[i] = i+lb;
		}
		
		return quantifiedRange;
	
	}
}
