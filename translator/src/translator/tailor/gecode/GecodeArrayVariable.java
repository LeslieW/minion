package translator.tailor.gecode;

/**
 * @author andrea
 *
 * Represents (always 1-dimensional) arrays in Gecode
 * 
 */

public interface GecodeArrayVariable extends GecodeArray {
	
	/** get the length of the array */
	public int getLength();
	/** get the lower bound representing the array*/
	public int getUpperBound();
	/** get the upper bound representing the array */
	public int getLowerBound();
	/** get the ranges of each of the arrays indices/dimensions */
	public int[] getLengths();
	/** returns true when the array has a dimension greater than 1*/
	public boolean isMultiDimensional();
	
}
