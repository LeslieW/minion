package translator.solver;

import java.util.HashMap;

/**
 * Solver profile of the solver Minion
 * 
 * @author andrea
 *
 */

public class Minion implements TargetSolver {

	/**
	 * This feature map contains all features of the solver.
	 * Each feature is encoded by an Integer value, that is 
	 * specified for each feature in the TargetSolver interface.
	 * Solver-specific features are excluded.
	 * The HashMap HAS TO BE private, in order to let noone mess
	 * around with the features of the solver.
	 */
	private HashMap<Integer, Boolean> featureMap;
	
	// ------- solving ---------------
	private String[] branchingStrategies;
	private String[] searchStrategies;
	private String branchingStrategy;
	private String searchStrategy;
	
	// ============ CONSTRUCTOR =============
	
	public Minion() {
		// general stuff
		this.branchingStrategies = new String[] {DEFAULT_BRANCHING, FIRST_FAIL, LARGEST_DOMAIN, RANDOM_DOMAIN, STATIC_NORMAL};
		this.searchStrategies = new String[] {DEFAULT_SEARCH, DEPTH_FIRST};
		
		this.searchStrategy = DEPTH_FIRST;
		this.branchingStrategy = FIRST_FAIL;
	
		
		this.featureMap = new HashMap<Integer, Boolean>();
		
		// constraints
		featureMap.put(new Integer(NESTED_EXPRESSIONS),new Boolean(false));
		featureMap.put(new Integer(NARY_CONJUNCTION),new Boolean(true));
		featureMap.put(new Integer(NARY_DISJUNCTION),new Boolean(true));
		featureMap.put(new Integer(NARY_MULTIPLICATION),new Boolean(false));
		featureMap.put(new Integer(NARY_SUM),new Boolean(true));
		featureMap.put(new Integer(NARY_WEIGHTED_SUM),new Boolean(true));

		// variables
		featureMap.put(new Integer(SPARSE_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(DISCRETE_BOUNDS_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(VARIABLE_ARRAY_INDEXING),new Boolean(false));
		
		

	}
	
	
	// ======== GENERAL STUFF ==============

	public String getSolverName() {
		return "Minion";
	}

	public void setFeature(char feature, boolean turnOn) {
		if(this.featureMap.get(feature))
			featureMap.put(new Integer(feature), new Boolean(turnOn));
	}
	
	// ======= SOLVING PROCESS ==============
	
	public String getBranchingStrategy() {
		return this.branchingStrategy;
	}
	
	public String getSearchStrategy() {
		return this.searchStrategy;
	}
 	
	public void setBranchingStrategy(String strategy) {
		if(supportsBranchingStrategy(strategy))
			this.branchingStrategy = strategy;
	}
	
	public void setSearchStrategy(String strategy) {
		if(supportsSearchStrategy(strategy))
			this.searchStrategy = strategy;
	}
	
	public boolean supportsBranchingStrategy(String strategy) {
		for(int i=0; i<this.branchingStrategies.length; i++)
			if(this.branchingStrategies[i] == strategy)
				return true;
		return false;
	}
	
	public boolean supportsSearchStrategy(String strategy) {
		for(int i=0; i<this.searchStrategies.length; i++)
			if(this.searchStrategies[i] == strategy)
				return true;
		return false;
	}
	
	public String[] getBranchingStrategies() {
		return this.branchingStrategies;
	}
	
	public String[] getSearchStrategies() {
		return this.searchStrategies;
	}
	
	
	// ======= CONSTRAINT FEATURES ===========
	
	public boolean supportsNaryConjunction() {
		return this.featureMap.get(new Integer(NARY_CONJUNCTION));
	}

	public boolean supportsNaryDisjunction() {
		return this.featureMap.get(new Integer(NARY_DISJUNCTION));
	}

	public boolean supportsNaryMultiplication() {
		return this.featureMap.get(new Integer(NARY_MULTIPLICATION));
	}

	public boolean supportsNarySum() {
		return this.featureMap.get(new Integer(NARY_SUM));
	}

	public boolean supportsNestedExpressions() {
		return this.featureMap.get(new Integer(NESTED_EXPRESSIONS));
	}

	public boolean supportsWeightedNarySum() {
		return this.featureMap.get(new Integer(NARY_WEIGHTED_SUM));
	}

	
	// ============ VARIABLES ========================
	
	public boolean supportsSparseVariables() {
		return this.featureMap.get(new Integer(SPARSE_VARIABLES));
	}

	public boolean supportsDiscreteBoundVariables() {
		return this.featureMap.get(new Integer(DISCRETE_BOUNDS_VARIABLES));
	}
	
	public boolean supportsVariableArrayIndexing() {
		return this.featureMap.get(new Integer(VARIABLE_ARRAY_INDEXING));
	}

	// ========= SOLVING PROCESS =====================
	
}
