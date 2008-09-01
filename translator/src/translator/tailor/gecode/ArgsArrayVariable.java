package translator.tailor.gecode;

public interface ArgsArrayVariable extends GecodeArray, ArgsAtom {

	//public GecodeAtom[] getVariables();
	
	//public GecodeAtom getVariableAt(int index) throws GecodeException;
	
	/** get the length of the array */
	public int getLength();
	/** get the lower bound representing the array*/
	public int getUpperBound();
	/** get the upper bound representing the array */
	public int getLowerBound();
	/** returns true if the array represents parts of an array 
	 * (e.g. a row of a multi-dim. array) */
	public boolean isIndexedArray();
	/** returns the indices (index ranges) of each index domain - returns null
	 * if the array is not a partial array */
	public int[] getIndexDomains();
	/** returns a set of integers that represent the referenced row, col etc of the 
	 * array (and returns null if the array is not indexed (partial)). For instance,
	 * if a 2-dim. array M is referenced as in M[..,3] which corresponds to the 
	 * 3rd (4th) column of the 2-dim, array, then the referenced indices [lb,ub][3,3] */
	public int[][] getReferencedIndex();
	/** 
	 * ONLY FOR PARTIAL ARRAYS!!!
	 * Returns the String that represents the definition of the args array variable. 
	 * (its declaration and definition, assuming it has not yet been declared before)
	 * @return
	 */
	public String getArrayDefinition();
	
}
