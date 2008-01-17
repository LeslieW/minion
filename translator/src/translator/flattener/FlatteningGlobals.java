package translator.flattener;

public interface FlatteningGlobals {

	public final String AUX_VAR_NAME = "_aux";
	
	/**
	 * The minimum amount of +- operators in a row 
	 * to make a linear equation out of the expression
	 * e.g: minumum 3:
	 * E1 + E2 - E3 + E4 relop E5
	 *        is a linear equation (with arbitrary Es) while
	 * E1 + E2 - E3  relop E5
	 *       is not
	 */
	public final int MIN_NO_OPS_IN_LIN_EQ = 2;
	
	public final int MAX_BOUND_AUX_VAR = 20000;
	public final int MIN_BOUND_AUX_VAR = -20000;
	
	
}
