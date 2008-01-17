 
package translator.minionExpressionTranslator;


public interface MinionTranslatorGlobals {


	    public final boolean DEBUG = false;
	    public final boolean PRINT_MESSAGE = true;
	    
	    public final String OUTPUT_FILENAME = "outFile.minion";

	    public final int INTEGER_DOMAIN_LOWER_BOUND = -999999; // 1073741824; // -2^30
	    public final int INTEGER_DOMAIN_UPPER_BOUND = 999999; //1073741824; // 2^30

	    public final String FLATTENED_MATRIX_NAME = "flattened_";
	
}
