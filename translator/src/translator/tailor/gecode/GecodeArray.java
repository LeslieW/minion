package translator.tailor.gecode;

public interface GecodeArray extends GecodeVariable {

	/** get the length of the array */
	public int getLength();
	/** get the lower bound representing the array*/
	public int getUpperBound();
	/** get the upper bound representing the array */
	public int getLowerBound();
	
}
