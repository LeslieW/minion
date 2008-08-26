package translator.solver;

import java.util.HashMap;

public class Gecode extends GeneralTargetSolver {

	
	// translation issues
	public final String SINGLE_INT_VAR_ARRAY_NAME = "_int_var_buffer";
	public final String SINGLE_BOOL_VAR_ARRAY_NAME = "_bool_var_buffer";
	public final String AUX_INT_VAR_ARRAY_NAME = "_aux_int_var_buffer";
	public final String AUX_BOOL_VAR_ARRAY_NAME = "_aux_bool_var_buffer";
	
	
	// solving issues
	public final String GECODE_DEFAULT_VAR_BRANCHING = BRANCH_SMALLEST_DOMAIN;
	public final String GECODE_DEFAULT_VAL_BRANCHING = BRANCH_SMALLEST_VALUE;
	
	
	
	public Gecode() {
		
		this.solverName = "Gecode";
		this.version ="2.1.1";
		this.inputFileExtension = "cc";
		
		/*
		 * 	Gecode's branchings on int vars: 
          Gecode::INT_VAR_NONE,    Gecode::INT_VAR_MIN_MIN,  Gecode::INT_VAR_MIN_MAX,  Gecode::INT_VAR_MAX_MIN, 
  		  Gecode::INT_VAR_MAX_MAX, Gecode::INT_VAR_SIZE_MIN, Gecode::INT_VAR_SIZE_MAX, Gecode::INT_VAR_DEGREE_MIN, 
  		  Gecode::INT_VAR_DEGREE_MAX, Gecode::INT_VAR_SIZE_DEGREE_MIN, Gecode::INT_VAR_SIZE_DEGREE_MAX, 
  		  Gecode::INT_VAR_REGRET_MIN_MIN, Gecode::INT_VAR_REGRET_MIN_MAX, 
  		  Gecode::INT_VAR_REGRET_MAX_MIN, Gecode::INT_VAR_REGRET_MAX_MAX 
		 */
		
		this.varBranchingStrategies = new String[] {DEFAULT_VAR_BRANCHING,
												TargetSolver.BRANCH_SMALLEST_DOMAIN,  // INT_VAR_SIZE_MIN
												TargetSolver.BRANCH_LARGEST_DOMAIN, // INT_VAR_SIZE_MAX
												TargetSolver.BRANCH_LARGEST_MAXIMUM, // INT_VAR_MAX_MAX
												TargetSolver.BRANCH_LARGEST_MINIMUM, // INT_VAR_MIN_MAX
												TargetSolver.BRANCH_SMALLEST_MAXIMUM, // INT_VAR_MAX_MIN
												TargetSolver.BRANCH_SMALLEST_MINIMUM // INT_VAR_MIN_MIN
												
												};
		
		this.valBranchingStrategies = new String[] {DEFAULT_VAL_BRANCHING,
													TargetSolver.BRANCH_SMALLEST_VALUE,  // INT_VAL_MIN
													TargetSolver.BRANCH_LARGEST_VALUE};  // INT_VAL_MAX
		
		this.searchStrategies = new String[] {DEFAULT_SEARCH};
		
		// default settings for translation
		this.searchStrategy = DEPTH_FIRST;
		this.varBranchingStrategy = this.GECODE_DEFAULT_VAR_BRANCHING;
		this.valBranchingStrategy = this.GECODE_DEFAULT_VAL_BRANCHING;
		
		// arrays are indexed starting from 0
		this.arrayIndexStart = 0;
		
		// initialise features of Gecode
		this.featureMap = new HashMap<Integer, Boolean>();
		
		featureMap.put(new Integer(TargetSolver.USE_COMMON_SUBEXPRESSIONS), new Boolean(true));
		featureMap.put(new Integer(SEARCH_OVER_AUXILIARY_VARIABLES),new Boolean(false));
		featureMap.put(new Integer(TargetSolver.ARRAY_INDEX_START_VALUE), new Boolean(true));
		
		
		// ------------ features of the solver -----------------------------
		// general
		
		// can expressions be arbitrarily nested?
		featureMap.put(new Integer(NESTED_EXPRESSIONS),new Boolean(false));
		featureMap.put(new Integer(NESTED_LINEAR_EXPRESSIONS),new Boolean(true));
		// can we express an objective directly?
		featureMap.put(new Integer(TargetSolver.SUPPORTS_OBJECTIVE), new Boolean(false));
		// can the objective be an expression(true) or does it have to be a variable(false)?
		featureMap.put(new Integer(TargetSolver.CONSTRAINT_OBJECTIVE), new Boolean(false));
		
		// directly supported constraints (i.e. there is a direct mapping 
		// from the constraint to the equivalent constraint in the solver
		featureMap.put(new Integer(EXISTENTIAL_QUANTIFICATION), new Boolean(false));
		featureMap.put(new Integer(UNIVERSAL_QUANTIFICATION), new Boolean(false));
		featureMap.put(new Integer(QUANTIFIED_SUM), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.SUPPORTS_ATLEAST), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.SUPPORTS_ATMOST), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.SUPPORTS_OCCURRENCE), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.SUPPORTS_TABLE), new Boolean(true));
		
		featureMap.put(new Integer(TargetSolver.SUPPORTS_1DIM_LEX), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.SUPPORTS_2DIM_LEX), new Boolean(false));
		
		featureMap.put(new Integer(NARY_CONJUNCTION),new Boolean(true));
		featureMap.put(new Integer(NARY_DISJUNCTION),new Boolean(true));
		featureMap.put(new Integer(NARY_MULTIPLICATION),new Boolean(false));
		featureMap.put(new Integer(NARY_SUM),new Boolean(true));
		featureMap.put(new Integer(NARY_WEIGHTED_SUM),new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.NARY_SUMEQ_CONSTRAINT), new Boolean(true));  // this is wrong but for testing set to this value
		featureMap.put(new Integer(TargetSolver.NARY_SUMNEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_SUMGREATER_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.NARY_SUMLESS_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_PRODUCT_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMEQ_CONSTRAINT), new Boolean(true)); // this is wrong but for testing set to this value
		featureMap.put(new Integer(TargetSolver.BINARY_SUMNEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMGREATER_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.BINARY_SUMLESS_CONSTRAINT), new Boolean(true));
	
		// nesting of constraints in other constraints
		// (set to true, if the constraint allows other constraints nested
		//  as parameter)
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NEGATION), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_DISJUNCTION), new Boolean(false)); // true befoire
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_CONJUNCTION), new Boolean(false)); // true before
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_IF), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_IFF), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_EQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NEQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_LEQ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_GEQ ), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_GREATER), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_LESS), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_ELEMENT), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_QUANTIFIED_SUM), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_NARY_SUM), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_UNARY_MINUS), new Boolean(false));
		featureMap.put(new Integer(CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE_ARGUMENT), new Boolean(false));
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
		featureMap.put(new Integer(REIFIED_EQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_NEQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_GEQ), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LESS), new Boolean(true));
		featureMap.put(new Integer(REIFIED_GREATER), new Boolean(true));
		featureMap.put(new Integer(REIFIED_LEX_LEQ), new Boolean(false));
		featureMap.put(new Integer(REIFIED_LEX_GEQ), new Boolean(false));
		featureMap.put(new Integer(REIFIED_ELEMENT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_ABSOLUTE_VALUE), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_UNARY_MINUS), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMNEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMGREATER_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_SUMLESS_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_PRODUCT_CONSTRAINT), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMNEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMGEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMLEQ_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMGREATER_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_BINARY_SUMLESS_CONSTRAINT), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_CONJUNCTION), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_NARY_DISJUNCTION), new Boolean(true));
		featureMap.put(new Integer(TargetSolver.REIFIED_ATLEAST), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_ATMOST), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_OCCURRENCE), new Boolean(false));
		featureMap.put(new Integer(TargetSolver.REIFIED_TABLE), new Boolean(false));
		
		// variables
		featureMap.put(new Integer(SPARSE_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(DISCRETE_BOUNDS_VARIABLES),new Boolean(true));
		featureMap.put(new Integer(VARIABLE_ARRAY_INDEXING),new Boolean(false));
		featureMap.put(new Integer(VARIABLE_ALIASES), new Boolean(true));
	}
	
	
	/**
	 * Returns the Gecode alias for the given variable branching strategy
	 * 
	 * @param branching
	 * @return 
	 * @throws Exception if the variable branching strategy is unknown
	 */
	public String toGecodeVariableBranching(String branching) {
		
		if(branching.equals(TargetSolver.BRANCH_LARGEST_DOMAIN))
			return "INT_VAR_SIZE_MAX";
		
		else if(branching.equals(TargetSolver.BRANCH_SMALLEST_DOMAIN))
			return "INT_VAR_SIZE_MIN";
		
		else if(branching.equals(TargetSolver.BRANCH_LARGEST_MAXIMUM)) 
			return "INT_VAR_MAX_MAX";
		
		else if(branching.equals(TargetSolver.BRANCH_LARGEST_MINIMUM)) 
			return "INT_VAR_MIN_MAX";
		
		else if(branching.equals(TargetSolver.BRANCH_SMALLEST_MINIMUM)) 
			return "INT_VAR_MIN_MIN";
		
		else if(branching.equals(TargetSolver.BRANCH_SMALLEST_MAXIMUM)) 
			return "INT_VAR_MAX_MIN";
		
		else return toGecodeVariableBranching(GECODE_DEFAULT_VAR_BRANCHING);
	}
	
	
	/**
	 * Converts the internal value branching representation to the 
	 * Gecode alias (global) of the value branching strategy.  
	 * 
	 * @param branching
	 * @return
	 * @throws Exception if the value branching is unkown 
	 */
	public String toGecodeValueBranching(String branching) {
		
		if(branching.equals(TargetSolver.BRANCH_LARGEST_VALUE))
			return "INT_VAL_MAX";
		
		else if(branching.equals(TargetSolver.BRANCH_SMALLEST_VALUE))
			return "INT_VAL_MIN";
		
		else return toGecodeValueBranching(GECODE_DEFAULT_VAL_BRANCHING);
	}
	
}
