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
	public final int NARY_SUM_CONSTRAINT = 75;
	public final int NARY_PRODUCT_CONSTRAINT = 76;
	public final int BINARY_PRODUCT_CONSTRAINT = 77;
	public final int BINARY_SUM_CONSTRAINT = 78;
	
	
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
	public final int CONSTRAINT_NESTED_IN_NARY_SUM_CONSTRAINT = 114; // x1 + x2 + .. + xn = s
	public final int CONSTRAINT_NESTED_IN_NARY_PRODUCT_CONSTRAINT = 115;
	public final int CONSTRAINT_NESTED_IN_BINARY_PRODUCT_CONSTRAINT = 116;
	public final int CONSTRAINT_NESTED_IN_BINARY_SUM_CONSTRAINT = 117;
	
	// ----- reification of constraints ----------------
	public final int REIFIED_IF = 77;
	public final int REIFIED_LEQ = 78;
	public final int REIFIED_GEQ = 79;
	public final int REIFIED_LESS = 80;
	public final int REIFIED_GREATER = 81;
	public final int REIFIED_LEX_LEQ = 82;
	public final int REIFIED_LEX_GEQ = 83;
	public final int REIFIED_ELEMENT = 84;
	public final int REIFIED_NARY_SUM_CONSTRAINT = 85;
	public final int REIFIED_NARY_PRODUCT_CONSTRAINT = 86;
	public final int REIFIED_ALLDIFFERENT = 87;
	public final int REIFIED_BINARY_SUM_CONSTRAINT = 88;
	public final int REIFIED_BINARY_PRODUCT_CONSTRAINT = 89;
	
	
	
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
	
	//========= CONSTRAINTS FEATURES ===============
	
	public boolean supportsNestedExpressions();
	public boolean supportsConstraint(int constraint);
	
	public boolean supportsNaryDisjunction();
	public boolean supportsNaryConjunction();
    public boolean supportsNarySum();
    public boolean supportsNaryMultiplication();
    public boolean supportsWeightedNarySum();
    public boolean supportsConstraintsNestedInNegation();
    
    public boolean supportsReifiedAllDifferent();
    public boolean supportsReificationOf(int operation);
    public boolean supportsConstraintsNestedAsArgumentOf(int operator);
    
    // =============== CONSTRAINTS ==================
    
    
    
    
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
