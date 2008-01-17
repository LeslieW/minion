package translator.solver;

/**
 * Represents the solver profile of the target solver. Every target solver
 * has to implement this interface.
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
	
	
	// -------------- constraints stuff ----------------
	// general
	public final char NESTED_EXPRESSIONS = 69;
	public final char NARY_CONJUNCTION = 70;
	public final char NARY_DISJUNCTION = 71;
	public final char NARY_SUM = 72;
	public final char NARY_WEIGHTED_SUM = 73;
	public final char NARY_MULTIPLICATION = 74;
	
	// nesting stuff
	public final char UNNESTED_NEGATION = 75;
	public final int REIFIED_ALLDIFFERENT = 76;
	public final int REIFIED_IF = 77;
	public final int REIFIED_LEQ = 78;
	public final int REIFIED_GEQ = 79;
	public final int REIFIED_LESS = 80;
	public final int REIFIED_GREATER = 81;
	public final int REIFIED_LEX_LEQ = 82;
	public final int REIFIED_LEX_GEQ = 83;
	
	// solving issues
	public final char SEARCH_OVER_AUXILIARY_VARIABLES = 100;
	
	
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
	public boolean supportsNaryDisjunction();
	public boolean supportsNaryConjunction();
    public boolean supportsNarySum();
    public boolean supportsNaryMultiplication();
    public boolean supportsWeightedNarySum();
    public boolean supportsUnnestedNegation();
    
    public boolean supportsReifiedAllDifferent();
    public boolean supportsReificationOf(int operation);
    
    
    // =============== CONSTRAINTS ==================
    
    
    
    
    // ====== OTHER STUFF ===========================
    public String toString();
    
}
