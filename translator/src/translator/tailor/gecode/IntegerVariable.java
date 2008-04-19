package translator.tailor.gecode;

/**
 * Represents all integer variables (single variables)
 * 
 * @author andrea
 *
 */

public interface IntegerVariable extends GecodeVariable {

	/** 
	 * Returns the lower and upper bound of the variable.
	 * returns int-array with [lb, ub]
	 * @return
	 */
	public int[] getBounds();
	
	/**
	 * Returns true if the variable is an argument variable
	 * 
	 * @return
	 */
	public boolean isArgsVariable();
 }
