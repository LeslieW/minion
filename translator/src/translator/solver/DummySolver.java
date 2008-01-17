package translator.solver;

import java.util.HashMap;

/**
 * This dummy solver is for demonstration and partly for testing. It can be
 * easily used as a template for a new target solver.
 * 
 * @author andrea
 *
 */

public class DummySolver extends GeneralTargetSolver {

	

	
	// ============ CONSTRUCTOR =============
	
	public DummySolver() {
		// general stuff
		this.solverName = "Dummy Solver";
		this.branchingStrategies = new String[] {DEFAULT_BRANCHING, FIRST_FAIL, LARGEST_DOMAIN, RANDOM_DOMAIN};
		this.searchStrategies = new String[] {DEFAULT_SEARCH, DEPTH_FIRST, BREADTH_FIRST};
		
		this.searchStrategy = DEPTH_FIRST;
		this.branchingStrategy = FIRST_FAIL;
	
		
		this.featureMap = new HashMap<Integer, Boolean>();
		
		// constraints
		featureMap.put(new Integer(NESTED_EXPRESSIONS),new Boolean(false));
		featureMap.put(new Integer(NARY_CONJUNCTION),new Boolean(false));
		featureMap.put(new Integer(NARY_DISJUNCTION),new Boolean(false));
		featureMap.put(new Integer(NARY_MULTIPLICATION),new Boolean(true));
		featureMap.put(new Integer(NARY_SUM),new Boolean(true));
		featureMap.put(new Integer(NARY_WEIGHTED_SUM),new Boolean(true));

		// nesting of constraints in other constraints
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NEGATION), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_DISJUNCTION), new Boolean(true));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_CONJUNCTION), new Boolean(true));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_EQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NEQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_LEQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_GEQ ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_GREATER), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_LESS), new Boolean(false));
		
		
		// are the following constraints reifiable
		featureMap.put(new Integer(REIFIED_ALLDIFFERENT), new Boolean(false));
		featureMap.put(new Integer(REIFIED_IF), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LEQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_GEQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LESS), new Boolean(true));
		featureMap.put(new Integer(REIFIED_GREATER), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LEX_LEQ), new Boolean(false));
		featureMap.put(new Integer(REIFIED_LEX_GEQ), new Boolean(false));
		
		
		
		// variables
		featureMap.put(new Integer(SPARSE_VARIABLES),new Boolean(false));
		featureMap.put(new Integer(DISCRETE_BOUNDS_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(VARIABLE_ARRAY_INDEXING),new Boolean(false));


	}
	
	

}
