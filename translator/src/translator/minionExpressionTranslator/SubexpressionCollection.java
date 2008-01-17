package translator.minionExpressionTranslator;

import java.util.ArrayList;
import translator.conjureEssenceSpecification.Expression;
import translator.minionModel.MinionConstraint;
import translator.minionModel.MinionIdentifier;

public class SubexpressionCollection {
	

	ArrayList<ExpressionRepresentation> expressionRepList;
	
	
	public SubexpressionCollection() {	
		this.expressionRepList = new ArrayList<ExpressionRepresentation>();
	}
	

	
	/**
	 * Returns true if we have already collected (and translated) a subexpression that looks exactly 
	 * the same as Expression expression and false otherwise.
	 * 
	 * @param expression
	 * @return
	 */
	public boolean hasCommonSubexpression(Expression expression) {
		
		boolean hasCommonSubExpression = false;
		
		for(int i=0; i<this.expressionRepList.size(); i++) {
			// if both expressions (as strings) are the same
			if(this.expressionRepList.get(i).getExpression().toString().compareTo(expression.toString()) == 0)
				hasCommonSubExpression = true;
		}
		return hasCommonSubExpression;
	}
	
	
	
	/**
	 * Add a (sub)Expression expression that was translated to MinionConstraint constraint 
	 * to the list of translated expressions. 
	 * 
	 * @param constraint
	 * @param expression
	 */
	public void addSubExpression(MinionConstraint constraint, Expression expression) {
		
		// we have already added the expression -> this should actually not be happening, right?
		if(hasCommonSubexpression(expression))
			return;
		
		else 
			this.expressionRepList.add(new ExpressionRepresentation(constraint, expression));
		
	}
	
	
	/**
	 * Add a (sub)Expression expression that was translated to MinionIdentifier variable 
	 * to the list of translated expressions.
	 * 
	 * @param variable
	 * @param expression
	 */
	public void addSubExpression(MinionIdentifier variable, Expression expression) {
		
        // we have already added the expression -> this should actually not be happening, right?
		if(hasCommonSubexpression(expression))
			return;
		
		else this.expressionRepList.add(new ExpressionRepresentation(variable,expression));
		
	}
	
	
	/**
	 * Add a (sub)Expression expression that was translated to the array MinionConstraint[] constraints 
	 * to the list of translated expressions. 
	 * 
	 * @param constraints
	 * @param expression
	 */
	public void addSubExpression(MinionConstraint[] constraints, Expression expression) {
		
        // we have already added the expression -> this should actually not be happening, right?
		if(hasCommonSubexpression(expression))
			return;
		
		else this.expressionRepList.add(new ExpressionRepresentation(constraints, expression));
		
	}
	
	
	/**
	 * Return the ExpressionRepresentation (can consist of MinionConstraint(s) or a MinionIdentifier)
	 * that has already been translated. Returns null if there is no common subexpression (yet).
	 * 
	 * @param expression
	 * @return
	 */
	public ExpressionRepresentation getExpressionRepresentation(Expression expression) {
		
		for(int i=0; i<this.expressionRepList.size(); i++) {
			// if both expressions (as strings) are the same
			if(this.expressionRepList.get(i).getExpression().toString().compareTo(expression.toString()) == 0)
				return this.expressionRepList.get(i);
		}
		
		return null;
	}
	
}
