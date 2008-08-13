package translator.tailor.gecode;

/**
 * @author andrea
 *
 * Represents (always 1-dimensional) arrays in Gecode
 * 
 */

public interface GecodeArrayVariable extends GecodeVariable {
	
	/** get the length of the array */
	public int getLength();
	/** get the lower bound representing the array*/
	public int getUpperBound();
	/** get the upper bound representing the array */
	public int getLowerBound();
}
