package translator.minionExpressionTranslator;


import java.util.ArrayList;
import translator.minionModel.MinionConstraint;
import translator.minionModel.MinionIdentifier;
import translator.conjureEssenceSpecification.Expression;




public class ExpressionRepresentation {

	private Expression expression;
	/**
	 * The constraints that represent Expression expression. The last constraint in the list is the 
	 * one that would need to be reified if the Expression would need to be reified.
	 */
	private ArrayList<MinionConstraint> constraintList;
	private MinionIdentifier variable;

	
	/**
	 * Expression expression is represented by the MinionConstraint constraint 
	 * @param constraint
	 * @param expression
	 */
	public ExpressionRepresentation(MinionConstraint constraint, Expression expression) {
		
		this.expression = expression;
		this.constraintList = new ArrayList<MinionConstraint>();
		constraintList.add(constraint);
	}
	
	/**
	 * Expression expression is represented by the array of constraints constraints
	 * 
	 * @param constraints
	 * @param expression
	 */
	public ExpressionRepresentation(MinionConstraint[] constraints, Expression expression) {
		
		this.expression = expression;
		this.constraintList = new ArrayList<MinionConstraint>();
		for(int i=0; i<constraints.length; i++)
			constraintList.add(constraints[i]);
	}
	
	
	/**
	 * Expression expression is represented by the MinionIdentifier variable.
	 * 
	 * @param variable
	 * @param expression
	 */
	public ExpressionRepresentation(MinionIdentifier variable, Expression expression) {
	
		this.expression = expression;
		this.variable = variable;
		
	}
	
	
	/**
	 * Add a constraint to the representation of Expression expression. This is only possible if there
	 * alreay are some constraints that represent the expression.
	 * 
	 * @param constraint
	 * @throws TranslationUnsupportedException
	 */
	public void addConstraint(MinionConstraint constraint) 
		throws TranslationUnsupportedException {
		
		if(this.constraintList.size() == 0)
			throw new TranslationUnsupportedException
			("Internal error: trying to add a constraint "+constraint+" to expression representation that contains no constraint.");
		
		else 
			this.constraintList.add(constraint);
		
	}
	
	
	/**
	 * Return the constraintsList that represents Expression expression. The last constraint in the list is the 
	 * one that would need to be reified if the Expression would need to be reified.
	 * @return
	 */
	public ArrayList<MinionConstraint> getConstraintList() {
		return this.constraintList;
	}
	
	/**
	 * Return the MinionIdentifier that represents Expression expression. If the expression is represented 
	 * by a constraint (or several constraints), the method returns null.
	 * @return
	 */
	public MinionIdentifier getVariable() {
		return this.variable;
	}
	
	
	/**
	 * Return the expression represented by the constraints/variable.
	 * @return
	 */
	public Expression getExpression() {
		return this.expression;
	}
	
}
