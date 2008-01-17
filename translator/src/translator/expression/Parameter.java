package translator.expression;

public interface Parameter {

	public final int EXPRESSION = 0;
	public final int DOMAIN = 1;
	
	/**
	 * Apply a restriction on the parameter, given by an WHERE expression
	 * which is some kind of a constraint on a parameter
	 * (see Essence' grammar, if that is not clear). 
	 * 
	 * @param whereRestriction
	 * @return
	 */
	public Parameter applyRestriction(Expression whereRestriction);
	
	/**
	 * @return the type of the parameter (domain or expression)
	 */
	public int getType();
	
	/**
	 * Evaluates the domain or the expression represented by the parameter
	 * @return the evaluated parameter
	 */
	public Parameter evaluate();
	
	
	/**
	 * @return the parameter name 
	 */
	public String getParameterName();
	
	public String toString();
	
}
