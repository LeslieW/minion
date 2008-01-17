package translator.solver;

/**
 * Solver profile of the solver Minion
 * 
 * @author andrea
 *
 */

public class Minion implements TargetSolver {

	
	private char branchingStrategy;
	private char searchStrategy;
	
	// ============ CONSTRUCTOR =============
	
	public Minion() {
		this.searchStrategy = DEPTH_FIRST;
		this.branchingStrategy = FIRST_FAIL;
		
	}
	
	
	// ======== GENERAL STUFF ==============

	public String getSolverName() {
		return "Minion";
	}

	// ======= SOLVING PROCESS ==============
	
	public char getBranchingStrategy() {
		return this.branchingStrategy;
	}
	
	public char getSearchStrategy() {
		return this.searchStrategy;
	}
 	
	public void setBranchingStrategy(char strategy) {
		this.branchingStrategy = strategy;
	}
	
	public void setSearchStrategy(char strategy) {
		this.searchStrategy = strategy;
	}
	
	public boolean supportsStrategy(char strategy) {
		int f;
		return false;
	}
	
	// ======= CONSTRAINT FEATURES ===========
	
	public boolean supportsNaryConjunction() {
		return true;
	}

	public boolean supportsNaryDisjunction() {
		return true;
	}

	public boolean supportsNaryMultiplication() {
		return false;
	}

	public boolean supportsNarySum() {
		return true;
	}

	public boolean supportsNestedExpressions() {
		return true;
	}

	public boolean supportsWeightedNarySum() {
		return true;
	}

	
	// ============ VARIABLES ========================
	
	public boolean supportsSparseVariables() {
		return true;
	}

	public boolean supportsVariableArrayIndexing() {
		return false;
	}

	// ========= SOLVING PROCESS =====================
	
}
