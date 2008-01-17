package translator.flattener;

import java.util.ArrayList;

import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.AtomExpression;

public class SubExpression {


	/**
	 * Represents the subexpression 
	 * and should be a 
	 * variable (most likely an auxiliary variable)
	 */
	private AtomExpression representingVariable;
	
	/**
	 * Represents the subexpression unflattenedExpression 
	 * and should be a 
	 * variable (most likely an auxiliary variable)
	 */
	//private ArrayList<Expression> expressionList;
	
	
	/**
	 * The subexpression that is represented by either the decision variable 
	 * or  the expressionList
	 */
	private Expression subExpression;
	
	
	public SubExpression(Expression expression, AtomExpression representingVariable) {
		
		this.representingVariable = representingVariable;
		//this.expressionList = new ArrayList<Expression>();
		this.subExpression = expression;
		
	}
	
	
	
}
