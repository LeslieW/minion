package translator.expression;


/**
 * This class represents objectives in general - also the empty objective.
 * Only one objective is allowed, i.e. we can only maximise or minimise ONE
 * expression.
 * 
 * @author andrea
 *
 */

public class Objective implements Expression {

	
	public Expression objective;
	boolean maximise;
	private boolean willBeFlattened = true;

	// ============ CONSTRUCTOR ========================
	
	public Objective() {
		// empty objective == no objective
	}
	
	
	public Objective(Expression objective,
			         boolean maximise) {
		this.objective = objective;
		this.maximise = maximise;
	}
	
	
	// ============ METHODS =============================
	
	public Expression getObjectiveExpression() {
		return this.objective;
	}
	
	public void setObjectiveExpression(Expression objective) {
		this.objective = objective;
	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public int getType() {
		return Expression.OBJECTIVE;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		
		if(objective != null)
			this.objective = this.objective.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		if(objective != null)
			this.objective = this.objective.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		if(objective != null)
			this.objective = this.objective.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		if(this.objective != null)
			this.objective = this.objective.replaceVariableWithExpression(variableName, expression);
		
		return this;
	}
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattened;
	}
	
	public boolean isNested() {
		return false;
	}
	
	public char isSmallerThanSameType(Expression other) {
		return EQUAL;
	}
	 
	public void orderExpression() {
		if(objective != null)
			this.objective.orderExpression();
	}
	
	public Expression reduceExpressionTree() {
		if(objective != null)
			this.objective = this.objective.reduceExpressionTree();
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(objective != null)
			this.objective = this.objective.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
	
	public Expression restructure() {
		if(objective != null)
			this.objective = this.objective.restructure();
		return this;
	}
	
	public void setIsNotNested() {
		
	}
	
	public void willBeFlattenedToVariable(boolean turnOn) {
		this.willBeFlattened = turnOn;
	}
	
	/**
	 * @return true if the objective is maximised, false if it is 
	 * minimised. Please note that the method will also return false
	 * if the objective is empty.
	 */
	public boolean isMaximise() {
		return this.maximise;
	}
	
	/**
	 * 
	 * @return true if the objective is empty, i.e. there exists no objective.
	 * Returns false, if the problem is an optimisation problem (we want to
	 * maximise or minimise a certain expression)
	 */
	public boolean isEmptyObjective() {
		return (this.objective == null) ?
				true : false;
	}
	
	
	public void normalise() {
		if(this.objective != null) {
		this.objective = this.objective.reduceExpressionTree();
		this.objective = this.objective.evaluate();
		this.objective = this.objective.reduceExpressionTree();
		this.objective.orderExpression();
		}
	}
	
	public Expression evaluate() {
		if(this.objective != null) {
			this.objective.reduceExpressionTree();
			this.objective = this.objective.evaluate();
			this.objective.reduceExpressionTree();
		}
		return this;
	}
	
	
	public void order() {
		if(this.objective != null)
			this.objective.orderExpression();
	}
	
	public Objective copy() {
		
		if(this.objective == null)
			return new Objective();
		
		else return new Objective(this.objective.copy(),
				                  this.maximise);
	}
	
	
	
	public String toString() {
		String s = new String(""); 
		
		if(this.objective == null)
			return s;
		
		else {
			s = (this.maximise) ?
					"maximising" : "minimising";
			s = s.concat(" "+this.objective);
		}
		
		return s;
	}
}
