package translator.tailor.gecode;

public interface ArgsArrayVariable extends GecodeVariable, ArgsAtom {

	//public GecodeAtom[] getVariables();
	
	//public GecodeAtom getVariableAt(int index) throws GecodeException;
	
	/** get the length of the array */
	public int getLength();
	/** get the lower bound representing the array*/
	public int getUpperBound();
	/** get the upper bound representing the array */
	public int getLowerBound();
	
}
