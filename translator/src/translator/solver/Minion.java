package translator.solver;

import java.util.HashMap;

/**
 * Solver profile of the solver Minion. Just set the features in the map according to the 
 * features defined in the targetsolver interface.
 * 
 * @author andrea
 *
 */

public class Minion extends GeneralTargetSolver {


	public Minion() {
		// general stuff
		this.solverName = "Minion";
		this.branchingStrategies = new String[] {DEFAULT_BRANCHING, FIRST_FAIL, LARGEST_DOMAIN, RANDOM_DOMAIN, STATIC_NORMAL};
		this.searchStrategies = new String[] {DEFAULT_SEARCH, DEPTH_FIRST};
		
		this.searchStrategy = DEPTH_FIRST;
		this.branchingStrategy = FIRST_FAIL;
	
		
		this.featureMap = new HashMap<Integer, Boolean>();
		
		
		// ------------ features of the solver -----------------------------
		// constraints
		featureMap.put(new Integer(NESTED_EXPRESSIONS),new Boolean(false));
		featureMap.put(new Integer(NARY_CONJUNCTION),new Boolean(true));
		featureMap.put(new Integer(NARY_DISJUNCTION),new Boolean(true));
		featureMap.put(new Integer(NARY_MULTIPLICATION),new Boolean(false));
		featureMap.put(new Integer(NARY_SUM),new Boolean(true));
		featureMap.put(new Integer(NARY_WEIGHTED_SUM),new Boolean(true));
		featureMap.put(new Integer(UNNESTED_NEGATION), new Boolean(false));
		
		// variables
		featureMap.put(new Integer(SPARSE_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(DISCRETE_BOUNDS_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(VARIABLE_ARRAY_INDEXING),new Boolean(false));
		
	}
	
	

}
