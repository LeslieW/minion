package translator.expression;

/**
 * This interface represents all domains that are composed of constants
 * only. This includes boolean domains and all types of integer domains.
 * 
 * @author andrea
 *
 */

public interface ConstantDomain extends Domain {

	/**
	 * Get the range of the domain. This is either a lower and an
	 * upper bound or a sparse domain or a mixture of it 
	 * (depending on the type of domain) 
	 * @return the range of the domain. This is either a lower and an
	 * upper bound or a sparse domain or a mixture of it 
	 * (depending on the type of domain) 
	 */
	public int[] getRange();
	
	/**
	 * 
	 * @return the domain represented by an ordered list of all 
	 * values that are in the domain 
	 * (instead of an lowerbound-upperbound representation.)
	 */
	public int[] getFullDomain();
	
}
