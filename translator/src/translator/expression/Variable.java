package translator.expression;

public interface Variable extends Expression {

	/**
	 * Returns true if the variable should be searched on.
	 * 
	 * @return true, if the object is a search variable, i.e.
	 * it is proposed to perform search on it during the solving process
	 */
	public boolean isSearchVariable();
	
	/**
	 * Sets the variable to be a search variable, if the passed 
	 * parameter is true and to a unsearched variable if false is given.
	 * @param isSearchVariable
	 */
	public void setToSearchVariable(boolean isSearchVariable);
	
}
