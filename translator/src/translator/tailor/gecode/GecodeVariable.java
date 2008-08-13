package translator.tailor.gecode;

/**
 * Interface for Gecode variables datastructures.
 * 
 * @author andrea
 *
 */

public interface GecodeVariable extends GecodeAtom {

	//public final char INT_CONST = 0;
	public final char BOOL_VAR = 1;
	public final char INT_VAR = 2;
	public final char BOOL_ARG_VAR = 3;
	public final char INT_ARG_VAR =4;
	public final char BOOL_INT_VAR = 5;
	
	public final char BOOL_CONST_ARRAY_VAR = 10;
	public final char INT_CONST_ARRAY_VAR = 11;
	public final char BOOL_ARRAY_VAR = 12;
	public final char INT_ARRAY_VAR = 13;
	
	public String getVariableName();
	
	public char getType();
	
	public String toString();
	
	public int[] getBounds();
	
}
