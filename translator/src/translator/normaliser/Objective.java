package translator.normaliser;

import translator.expression.Expression;

/**
 * This class represents objectives in general - also the empty objective.
 * Only one objective is allowed, i.e. we can only maximise or minimise ONE
 * expression.
 * 
 * @author andrea
 *
 */

public class Objective {

	
	Expression objective;
	boolean maximise;
	

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
	
	protected void evaluate() {
		if(this.objective != null) {
			this.objective.reduceExpressionTree();
			this.objective = this.objective.evaluate();
			this.objective.reduceExpressionTree();
		}
	}
	
	
	protected void order() {
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
