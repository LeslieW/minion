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
		this.solverName = "Dummy Solver 1.0";
		this.branchingStrategies = new String[] {DEFAULT_BRANCHING, FIRST_FAIL, LARGEST_DOMAIN, RANDOM_DOMAIN};
		this.searchStrategies = new String[] {DEFAULT_SEARCH, DEPTH_FIRST, BREADTH_FIRST};
		
		this.searchStrategy = DEPTH_FIRST;
		this.branchingStrategy = FIRST_FAIL;
	
		
		this.featureMap = new HashMap<Integer, Boolean>();
		
		// constraints
		featureMap.put(new Integer(NESTED_EXPRESSIONS),new Boolean(false));
		
		// directly supported constraints (i.e. there is a direct mapping 
		// from the constraint to the equivalent constraint in the solver
		featureMap.put(new Integer(EXISTENTIAL_QUANTIFICATION), new Boolean(false));
		featureMap.put(new Integer(UNIVERSAL_QUANTIFICATION), new Boolean(false));
		featureMap.put(new Integer(QUANTIFIED_SUM), new Boolean(false));
		
		featureMap.put(new Integer(NARY_CONJUNCTION),new Boolean(false));
		featureMap.put(new Integer(NARY_DISJUNCTION),new Boolean(false));
		featureMap.put(new Integer(NARY_MULTIPLICATION),new Boolean(true));
		featureMap.put(new Integer(NARY_SUM),new Boolean(true));
		featureMap.put(new Integer(NARY_WEIGHTED_SUM),new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.NARY_SUMEQ_CONSTRAINT), new Boolean(true));  // this is wrong but for testing set to this value
		featureMap.put(new Integer(TargetSolver.NARY_SUMNEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.NARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_SUMGREATER_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.NARY_SUMLESS_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.BINARY_PRODUCT_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMEQ_CONSTRAINT), new Boolean(false)); // this is wrong but for testing set to this value
		featureMap.put(new Integer(TargetSolver.BINARY_SUMNEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMLEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMGEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMGREATER_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMLESS_CONSTRAINT), new Boolean(true));

		// nesting of constraints in other constraints
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NEGATION), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_DISJUNCTION), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_CONJUNCTION), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_EQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NEQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_LEQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_GEQ ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_GREATER), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_LESS), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_QUANTIFIED_SUM), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NARY_SUM), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_UNARY_MINUS), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMNEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMGEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMLEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMGREATER_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_NARY_SUMLESS_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMNEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMGEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMLEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMGREATER_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_NESTED_IN_BINARY_SUMLESS_CONSTRAINT), new Boolean(false));
		
		
		// are the following constraints reifiable
		featureMap.put(new Integer(REIFIED_ALLDIFFERENT), new Boolean(false));
		featureMap.put(new Integer(REIFIED_IF), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LEQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_GEQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LESS), new Boolean(true));
		featureMap.put(new Integer(REIFIED_GREATER), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LEX_LEQ), new Boolean(false));
		featureMap.put(new Integer(REIFIED_LEX_GEQ), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMNEQ_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMGREATER_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMLESS_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMNEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMGREATER_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMLESS_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_CONJUNCTION), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_DISJUNCTION), new Boolean(true));

		
		
		// variables
		featureMap.put(new Integer(SPARSE_VARIABLES),new Boolean(false));
		featureMap.put(new Integer(DISCRETE_BOUNDS_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(VARIABLE_ARRAY_INDEXING),new Boolean(false));


	}
	
	

}
