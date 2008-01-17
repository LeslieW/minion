package translator.solver;

/**
 * Represents the solver profile of the target solver. Every target solver
 * has to implement this interface. All the methods that are declared here
 * are defined in the abstract class GeneralTargetSolver.java. So if you want
 * to add another solver, just extend the abstract class GeneralTargetSolver.java.
 * (and you get the whole functionality with just setting the features of your 
 * solver)
 * 
 * @author andrea
 *
 */

public interface TargetSolver {

	// branching strategies
	public final String FIRST_FAIL = "smallest domain first (first fail)";
	public final String LARGEST_DOMAIN = "largest domain first";
	public final String RANDOM_DOMAIN = "random";
	public final String STATIC_NORMAL = "static normal order";
	public final String DEFAULT_BRANCHING = "default";
	
	// search strategies
	public final String DEPTH_FIRST = "depth first";
	public final String BREADTH_FIRST = "breadth first";
	public final String DEFAULT_SEARCH = "default"; 
	

	// variable stuff
	public final char SPARSE_VARIABLES = 50;
	public final char DISCRETE_BOUNDS_VARIABLES = 51;
	public final char VARIABLE_ARRAY_INDEXING = 52;
	
	
	// ============= constraints stuff =================
	/** supports nested expressions in ANY constraint */
	public final char NESTED_EXPRESSIONS = 69;
	
	// -------------constraint kinds ------------------
	/** suppors quantified sums */
	public final int QUANTIFIED_SUM = 300;
	/** suppors universal quantification */
	public final int UNIVERSAL_QUANTIFICATION = 301;
	/** suppors existential quantification  */
	public final int EXISTENTIAL_QUANTIFICATION = 302;
	
	
	// n-ary constraints
	public final char NARY_CONJUNCTION = 70;
	public final char NARY_DISJUNCTION = 71;
	public final char NARY_SUM = 72;
	public final char NARY_WEIGHTED_SUM = 73;
	public final char NARY_MULTIPLICATION = 74;
	public final int NARY_SUMEQ_CONSTRAINT = 75;
	public final int NARY_PRODUCT_CONSTRAINT = 76;
	public final int BINARY_PRODUCT_CONSTRAINT = 77;
	public final int BINARY_SUMEQ_CONSTRAINT = 78;
	public final int BINARY_SUMLEQ_CONSTRAINT = 79;
	public final int NARY_SUMLEQ_CONSTRAINT = 80;
	public final int BINARY_SUMGEQ_CONSTRAINT = 81;
	public final int NARY_SUMGEQ_CONSTRAINT = 82;
	public final int BINARY_SUMNEQ_CONSTRAINT = 83;
	public final int NARY_SUMNEQ_CONSTRAINT = 84;
	public final int BINARY_SUMGREATER_CONSTRAINT = 85;
	public final int NARY_SUMGREATER_CONSTRAINT = 86;
	public final int BINARY_SUMLESS_CONSTRAINT = 87;
	public final int NARY_SUMLESS_CONSTRAINT = 88;
	
	//---- nesting of constraints as arguments in other constraints -------
	public final int CONSTRAINT_NESTED_IN_NEGATION = 100;
	public final int CONSTRAINT_NESTED_IN_CONJUNCTION = 101;
	public final int CONSTRAINT_NESTED_IN_IF = 102;
	public final int CONSTRAINT_NESTED_IN_LEQ = 103;
	public final int CONSTRAINT_NESTED_IN_GEQ = 104;
	public final int CONSTRAINT_NESTED_IN_LESS = 105;
	public final int CONSTRAINT_NESTED_IN_GREATER = 106;
	public final int CONSTRAINT_NESTED_IN_EQ = 107;
	public final int CONSTRAINT_NESTED_IN_NEQ = 108;
	public final int CONSTRAINT_NESTED_IN_IFF = 109;
	public final int CONSTRAINT_NESTED_IN_DISJUNCTION = 110;
	public final int CONSTRAINT_NESTED_IN_ELEMENT = 111;
	public final int CONSTRAINT_NESTED_IN_QUANTIFIED_SUM = 112;
	public final int CONSTRAINT_NESTED_IN_NARY_SUM = 113;            // x1 + x2 + .. + xn 
	public final int CONSTRAINT_NESTED_IN_NARY_SUMEQ_CONSTRAINT = 114;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUMEQ_CONSTRAINT = 117;
	public final int CONSTRAINT_NESTED_IN_NARY_PRODUCT_CONSTRAINT = 115;
	public final int CONSTRAINT_NESTED_IN_BINARY_PRODUCT_CONSTRAINT = 116;
	public final int CONSTRAINT_NESTED_IN_NARY_SUMLEQ_CONSTRAINT = 114;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUMLEQ_CONSTRAINT = 118;
	public final int CONSTRAINT_NESTED_IN_NARY_SUMGEQ_CONSTRAINT = 119;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUMGEQ_CONSTRAINT = 120;
	public final int CONSTRAINT_NESTED_IN_NARY_SUMNEQ_CONSTRAINT = 121;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUMNEQ_CONSTRAINT = 122;
	public final int CONSTRAINT_NESTED_IN_NARY_SUMLESS_CONSTRAINT = 123;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUMLESS_CONSTRAINT = 124;
	public final int CONSTRAINT_NESTED_IN_NARY_SUMGREATER_CONSTRAINT = 125;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUMGREATER_CONSTRAINT = 126;
	public final int CONSTRAINT_NESTED_IN_UNARY_MINUS = 127;
	public final int CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE = 128;
	
	// ----- reification of constraints ----------------
	public final int REIFIED_IF = 277;
	public final int REIFIED_LEQ = 278;
	public final int REIFIED_GEQ = 279;
	public final int REIFIED_LESS = 280;
	public final int REIFIED_GREATER = 281;
	public final int REIFIED_LEX_LEQ = 282;
	public final int REIFIED_LEX_GEQ = 283;
	public final int REIFIED_ELEMENT = 284;
	public final int REIFIED_NARY_SUMEQ_CONSTRAINT = 285;
	public final int REIFIED_BINARY_SUMEQ_CONSTRAINT = 288;
	public final int REIFIED_NARY_PRODUCT_CONSTRAINT = 286;
	public final int REIFIED_ALLDIFFERENT = 287;
	public final int REIFIED_BINARY_PRODUCT_CONSTRAINT = 289;
	public final int REIFIED_NARY_SUMGEQ_CONSTRAINT = 290;
	public final int REIFIED_BINARY_SUMGEQ_CONSTRAINT = 291;
	public final int REIFIED_NARY_SUMLEQ_CONSTRAINT = 292;
	public final int REIFIED_BINARY_SUMLEQ_CONSTRAINT = 293;
	public final int REIFIED_NARY_SUMNEQ_CONSTRAINT = 294;
	public final int REIFIED_BINARY_SUMNEQ_CONSTRAINT = 295;
	public final int REIFIED_NARY_SUMGREATER_CONSTRAINT = 296;
	public final int REIFIED_BINARY_SUMGREATER_CONSTRAINT = 297;
	public final int REIFIED_NARY_SUMLESS_CONSTRAINT = 298;
	public final int REIFIED_BINARY_SUMLESS_CONSTRAINT = 299;
	public final int REIFIED_UNARY_MINUS = 250;
	public final int REIFIED_ABSOLUTE_VALUE = 251;
	public final int REIFIED_NARY_CONJUNCTION = 252;
	public final int REIFIED_NARY_DISJUNCTION = 253;
	public final int REIFIED_SUM = 254;
	public final int REIFIED_MULTIPLICATION = 255;
	
	
	// ============== solving issues =======================
	public final char SEARCH_OVER_AUXILIARY_VARIABLES = 200;
	
	
	// solver specific stuff
	
	
	// ======= GENERAL STUFF ======================
	
	public String getSolverName();
	
	/** Set the feature of a solver to true or false (i.e. turn it on(true)
	 *  or off (false)). Features can only be turned on if they are 
	 *  supported. It is also not possible to add NEW features with
	 *  this method - Only features intitiated in the constructor
	 *  and that are supported, can be modified.  */
	public void setFeature(char feature, boolean turnOn);
	
	// ======== SOLVING PROCESS ===================
	
	public String getBranchingStrategy();
	public void setBranchingStrategy(String strategy);
	public String getSearchStrategy();
	public void setSearchStrategy(String strategy);
	public boolean supportsBranchingStrategy(String strategy);
	public boolean supportsSearchStrategy(String strategy);
	public String[] getBranchingStrategies();
	public String[] getSearchStrategies();
	public void setSearchOverAuxiliaryVariables(boolean turnOn);
	public boolean willSearchOverAuxiliaryVariables();
	
	// ======== VARIABLE TYPES =====================
	
	public boolean supportsSparseVariables();
	public boolean supportsVariableArrayIndexing();
	public boolean supportsDiscreteBoundVariables();
	
	//========= CONSTRAINTS FEATURES ===============
	
	public boolean supportsNestedExpressions();
	public boolean supportsConstraint(int constraint);
	public boolean supportsReificationOf(int operation);
	public boolean supportsConstraintsNestedAsArgumentOf(int operator);
	    
	
	
/*	public boolean supportsNaryDisjunction();
	public boolean supportsNaryConjunction();
    public boolean supportsNarySum();
    public boolean supportsNaryMultiplication();
    public boolean supportsWeightedNarySum();
    public boolean supportsConstraintsNestedInNegation();
    
    public boolean supportsReifiedAllDifferent();
 */
    
    
    //=========	SETTING FEATURES ====================
    
    /**
     * Sets the feature (given by the int value) to the 
     * boolean parameter turnOn. 
     * 
     * @param the int representation for the feature and 
     * the boolean it should be set to 
     *  
     */
    public void setFeature(int feature, boolean turnOn);
    
    
    
    
    // ====== OTHER STUFF ===========================
    public String toString();
    
    
}
