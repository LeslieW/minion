package translator.flattener;

import java.util.ArrayList;
import translator.conjureEssenceSpecification.Expression;


public class SubExpressionCollection {

	ArrayList<FlattenedExpression> expressionRepList;
	
	
	public SubExpressionCollection() {	
		this.expressionRepList = new ArrayList<FlattenedExpression>();
	}
	
	
	
	/**
	 * Returns true if we have already collected (and translated) a subexpression that looks exactly 
	 * the same as Expression expression and false otherwise.
	 * 
	 * @param expression
	 * @return
	 */
	public boolean hasCommonSubExpression(Expression expression) {
		
		boolean hasCommonSubExpression = false;
		
		for(int i=0; i<this.expressionRepList.size(); i++) {
			// if both expressions (as strings) are the same
			if(this.expressionRepList.get(i).getOriginalExpression().toString().compareTo(expression.toString()) == 0)
				hasCommonSubExpression = true;
		}
		return hasCommonSubExpression;
	}

	/**
	 * Add an expression to the the list of flattened expressions. If it 
	 * already occurs in the list, it is not added.
	 * 
	 * @param subExpression
	 */
	public void addSubExpression(FlattenedExpression subExpression) {
		
		if(hasCommonSubExpression(subExpression.getOriginalExpression()))
			return;
		else this.expressionRepList.add(subExpression);
	}
	
	/**
	 * Return the flattened expression that corresponds to the parameter expression.
	 * Returns null if the expression has not been flattened yet. 
	 * 
	 * @param expression
	 * @return
	 */
	public FlattenedExpression getExpressionRepresentation(Expression expression) {
		
		for(int i=0; i<this.expressionRepList.size(); i++) {
			// if both expressions (as strings) are the same
			if(this.expressionRepList.get(i).getOriginalExpression().toString().compareTo(expression.toString()) == 0)
				return this.expressionRepList.get(i);
		}
		
		return null;
	}
	
}
