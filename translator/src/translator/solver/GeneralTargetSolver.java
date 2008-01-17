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
	
	public boolean supportsNestedExpressions() {
		return this.featureMap.get(new Integer(NESTED_EXPRESSIONS));
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

	
	/**
	 * Given the constraint alias (defined in Expression.java) for 
	 * operations (e.g. FORALL, ELEMENT_CONSTRAINT, etc), return 
	 * true, if the constraint is supported 
	 * 
	 * @param constraint
	 * @return
	 */
	public boolean supportsConstraint(int constraint) {
	
		
		switch(constraint) {
		
		case Expression.Q_SUM:
			return this.featureMap.get(new Integer(QUANTIFIED_SUM));
			
		case Expression.FORALL:
			return this.featureMap.get(new Integer(UNIVERSAL_QUANTIFICATION));
		
		case Expression.EXISTS:
			return this.featureMap.get(new Integer(EXISTENTIAL_QUANTIFICATION));
		
		case Expression.NARY_PRODUCT_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.NARY_PRODUCT_CONSTRAINT));
			
		case Expression.NARY_SUMEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.NARY_SUMEQ_CONSTRAINT));
			
		case Expression.NARY_SUMNEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.NARY_SUMNEQ_CONSTRAINT));
			
		case Expression.NARY_SUMLEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.NARY_SUMGEQ_CONSTRAINT));
			
		case Expression.NARY_SUMGEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.NARY_SUMLEQ_CONSTRAINT));
			
		case Expression.NARY_SUMGREATER_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.NARY_SUMGREATER_CONSTRAINT));
			
		case Expression.NARY_SUMLESS_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.NARY_SUMLESS_CONSTRAINT));
			
		case Expression.BINARY_PRODUCT_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_PRODUCT_CONSTRAINT));
			
		case Expression.BINARY_SUMEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_SUMEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMNEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_SUMNEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMGEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_SUMGEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMLEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_SUMLEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMGREATER_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_SUMGREATER_CONSTRAINT));
			
		case Expression.BINARY_SUMLESS_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.BINARY_SUMLESS_CONSTRAINT));
	
		case Expression.NARY_CONJUNCTION:
			return this.featureMap.get(new Integer(TargetSolver.NARY_CONJUNCTION));
		
		case Expression.NARY_DISJUNCTION:
			return this.featureMap.get(new Integer(TargetSolver.NARY_CONJUNCTION));
			
		}
		
		
		return false;
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
			
		case Expression.NARY_PRODUCT_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_PRODUCT_CONSTRAINT));
			
		case Expression.NARY_SUMEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_SUMEQ_CONSTRAINT));
			
		case Expression.NARY_SUMNEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_SUMNEQ_CONSTRAINT));
			
		case Expression.NARY_SUMLEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_SUMGEQ_CONSTRAINT));
			
		case Expression.NARY_SUMGEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_SUMLEQ_CONSTRAINT));
			
		case Expression.NARY_SUMGREATER_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_SUMGREATER_CONSTRAINT));
			
		case Expression.NARY_SUMLESS_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_SUMLESS_CONSTRAINT));
			
		case Expression.BINARY_PRODUCT_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_PRODUCT_CONSTRAINT));
			
		case Expression.BINARY_SUMEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_SUMEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMNEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_SUMNEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMLEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_SUMLEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMGEQ_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_SUMGEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMGREATER_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_SUMGREATER_CONSTRAINT));
			
		case Expression.BINARY_SUMLESS_CONSTRAINT:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_BINARY_SUMLESS_CONSTRAINT));
			
		case Expression.ABS:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_ABSOLUTE_VALUE));
			
		case Expression.U_MINUS:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_UNARY_MINUS));

		case Expression.NARY_CONJUNCTION:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_CONJUNCTION));
			
		case Expression.AND:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_CONJUNCTION));
			
		case Expression.OR:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_DISJUNCTION));
			
		case Expression.NARY_DISJUNCTION:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NARY_DISJUNCTION));
			
		case Expression.EQ:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_EQ));
			
		case Expression.NEQ:
			return this.featureMap.get(new Integer(TargetSolver.REIFIED_NEQ));
		}
		
		return false;
	}
	
	
	public boolean supportsConstraintsNestedAsArgumentOf(int operator) {
		
		switch(operator) {
		
		case Expression.NEGATION:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_NEGATION));
		
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
			
		case Expression.Q_SUM:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_QUANTIFIED_SUM));
			
		case Expression.SUM:
			return this.featureMap.get(new Integer(CONSTRAINT_NESTED_IN_NARY_SUM));
			
		case Expression.NARY_PRODUCT_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_PRODUCT_CONSTRAINT));
			
		case Expression.NARY_SUMEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMEQ_CONSTRAINT));
			
		case Expression.NARY_SUMNEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMNEQ_CONSTRAINT));
			
		case Expression.NARY_SUMLEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMLEQ_CONSTRAINT));
			
		case Expression.NARY_SUMGEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMGEQ_CONSTRAINT));
			
		case Expression.NARY_SUMGREATER_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMGREATER_CONSTRAINT));
			
		case Expression.NARY_SUMLESS_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMLESS_CONSTRAINT));
			
		case Expression.BINARY_PRODUCT_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_PRODUCT_CONSTRAINT));
			
		case Expression.BINARY_SUMEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMNEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMNEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMGEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMGEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMLEQ_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMLEQ_CONSTRAINT));
			
		case Expression.BINARY_SUMGREATER_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMGREATER_CONSTRAINT));
			
		case Expression.BINARY_SUMLESS_CONSTRAINT: 
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMLESS_CONSTRAINT));
			
		case Expression.ABS:
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE));
			
		case Expression.U_MINUS:
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_UNARY_MINUS));
		
		case Expression.OR:
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_DISJUNCTION));
		
		case Expression.AND:
			return this.featureMap.get(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_CONJUNCTION));
		
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
	
	
	// ============ SETTING STUFF ============================
	
	 public void setFeature(int feature, boolean turnOn) {
		 this.featureMap.put(new Integer(feature), new Boolean(turnOn));
	 }
	
	
	// ============= OTHER STUFF ===============================
	
	 public boolean supportsFeature(int feature) {
		 
		 if(this.featureMap.containsKey(feature))
			 return this.featureMap.get(feature);
		 
		 else return false;
		 
	 }
	 
	 
	public String toString() {
		return this.solverName;
	}
}
