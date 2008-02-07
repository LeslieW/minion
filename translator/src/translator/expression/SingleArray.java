package translator.expression;

/**
 * Single arrays are arrays that are defined as a decision variable
 * of a matrix domain. They can be composed to form other arrays (this
 * is why this interface exists)
 * 
 * @author andrea
 *
 */


public interface SingleArray extends Array {

	public String getArrayName();
	
}
