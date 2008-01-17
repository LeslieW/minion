package translator.solver;

import java.util.HashMap;
import translator.expression.Expression;
/**
 * Solver profile of the solver Minion
 * 
 * @author andrea
 *
 */

public abstract class GeneralTargetSolver implements TargetSolver {

	/**
	 * This feature map contains all features of the solver.
	 * Each feature is encoded by an Integer value, that is 
	 * specified for each feature in the TargetSolver interface.
	 * Solver-specific features are excluded.
	 * The HashMap HAS TO BE private, in order to let noone mess
	 * around with the features of the solver.
	 */
	protected HashMap<Integer, Boolean> featureMap;
	
	protected String solverName;
	
	// ------- solving ---------------
	protected String[] branchingStrategies;
	protected String[] searchStrategies;
	protected String branchingStrategy;
	protected String searchStrategy;
	
	// ============ CONSTRUCTOR =============
	
	public GeneralTargetSolver() {
		
	}
	
	
	// ======== GENERAL STUFF ==============

	public String getSolverName() {
		return this.solverName;
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
	
	public void setSearchOverAuxiliaryVariables(boolean turnOn) {
		this.featureMap.put(new Integer(SEARCH_OVER_AUXILIARY_VARIABLES),turnOn);
	}
	
	public boolean willSearchOverAuxiliaryVariables() {
		return this.featureMap.get(new Integer(SEARCH_OVER_AUXILIARY_VARIABLES));
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

	public boolean supportsConstraintsNestedInNegation() {
		return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_NEGATION));
	}
	
	
	
	public boolean supportsReifiedAllDifferent() {
		return this.featureMap.get(new Integer(REIFIED_ALLDIFFERENT));
	}
	
	public boolean supportsReificationOf(int operation) {
		
		switch(operation) {
		
		case Expression.ALLDIFFERENT:
			return this.featureMap.get(new Integer(REIFIED_ALLDIFFERENT));
		
		case Expression.IF:
			return this.featureMap.get(new Integer(REIFIED_IF));
			
		case Expression.LEQ:
			return this.featureMap.get(new Integer(REIFIED_LEQ));
			
		case Expression.GEQ:
			return this.featureMap.get(new Integer(REIFIED_GEQ));
			
		case Expression.LESS:
			return this.featureMap.get(new Integer(REIFIED_LESS));
			
		case Expression.GREATER:
			return this.featureMap.get(new Integer(REIFIED_GREATER));
			
		case Expression.LEX_LEQ:
			return this.featureMap.get(new Integer(REIFIED_LEX_LEQ));
			
		case Expression.LEX_GEQ:
			return this.featureMap.get(new Integer(REIFIED_LEX_GEQ));
			
		case Expression.ELEMENT_CONSTRAINT:
			return this.featureMap.get(new Integer(REIFIED_ELEMENT));
		}
		
		return false;
	}
	
	
	public boolean supportsConstraintsNestedAsArgumentOf(int operator) {
		
		switch(operator) {
		
		case Expression.IF:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_IF));
			
		case Expression.LEQ:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_LEQ));
			
		case Expression.GEQ:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_GEQ));
			
		case Expression.LESS:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_LESS));
			
		case Expression.GREATER:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_GREATER));
			
		case Expression.EQ:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_EQ));
			
		case Expression.NEQ:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_NEQ));
			
		case Expression.ELEMENT_CONSTRAINT:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_ELEMENT));
		}
		
		return false;
		
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
	
	
	
	
	// ============= OTHER STUFF ===============================
	
	public String toString() {
		return this.solverName;
	}
}
