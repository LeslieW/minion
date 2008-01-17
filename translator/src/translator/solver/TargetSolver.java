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
	public final char FIRST_FAIL = 0;
	
	
	// search strategies
	public final char DEPTH_FIRST = 0;
	
	
	
	// ======= GENERAL STUFF ======================
	
	/** returns the solver name */
	public String getSolverName();
	public boolean supportsNestedExpressions();
	
	// ======== SOLVING PROCESS ===================
	
	public char getBranchingStrategy();
	public void setBranchingStrategy(char strategy);
	public char getSearchStrategy();
	public void setSearchStrategy(char strategy);
	public boolean supportsStrategy(char strategy);
	
	// ======== VARIABLE TYPES =====================
	
	public boolean supportsSparseVariables();
	public boolean supportsVariableArrayIndexing();
	
	//========= CONSTRAINTS FEATURES ===============
	
	public boolean supportsNaryDisjunction();
	public boolean supportsNaryConjunction();
    public boolean supportsNarySum();
    public boolean supportsNaryMultiplication();
    public boolean supportsWeightedNarySum();
    
    // =============== CONSTRAINTS ==================
    
 
    
}
