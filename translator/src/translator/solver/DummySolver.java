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

		// variables
		featureMap.put(new Integer(SPARSE_VARIABLES),new Boolean(false));
		featureMap.put(new Integer(DISCRETE_BOUNDS_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(VARIABLE_ARRAY_INDEXING),new Boolean(false));


	}
	
	

}
