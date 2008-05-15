package translator.expression;




public interface Expression {

	public final boolean DEBUG = true;
	
	public final String VARIABLE_ARRAY_NAME = "variableArray";
	
	public final int UPPER_BOUND = 100000;
	public final int LOWER_BOUND = -100000;
	public final int UPPER_BOUND_MAX = 1073741000;
	public final int LOWER_BOUND_MAX = -1073741000;
	
	// RelationalAtomExpression.java
	public final int FALSE = -1;
	public final int BOOL = 0;
	public final int BOOL_VARIABLE = 5;
	public final int BOOL_ARRAY_VAR = 6;
		
	// --------------------------------------------------------
	
	//ArithmeticAtomExpressions.java
	public final int INT = 3;
	public final int INT_VAR = 21;
	public final int INT_ARRAY_VAR = 22;
	
	// Array
	public final int CONSTANT_VECTOR = 24;
	public final int CONSTANT_MATRIX = 25;
	public final int SIMPLE_ARRAY = 26;
	public final int INDEXED_ARRAY = 27;
	public final int COMPOSED_ARRAY = 28;
	public final int VARIABLE_ARRAY = 29;

	
	// internal types for Variables
	/** a decision variable that is indexed by a constant value and has an integer bounds domain */
	public final int SINGLE_VARIABLE = 11;
	public final int SIMPLE_VARIABLE = 12;
	
	/** an array element that is indexed by a constant value and has an integer bounds domain */
	public final int ARRAY_VARIABLE = 14;
	public final int SIMPLE_ARRAY_VARIABLE = 15;
	
    //	 --------------------------------------------------------
	// Unary Relational Expressions
	public final int NEGATION = 41;
	public final int ALLDIFFERENT = 220;
	
	
	// unary arithmetic expressions
	public final int U_MINUS = 40;
	public final int ABS = 42;
	
	
   // --------------------------------------------------------
	
	// Binary Relational Operators (non-commutative)
	// already ordered according to their ordering
	// (except LEX-constraints!!)
	public final int LESS = 102;
	public final int LEQ = 104;
	public final int GREATER = 106;
	public final int GEQ = 108;
	public final int LEX_LESS = 103;
	public final int LEX_LEQ = 105;
	public final int LEX_GREATER = 107;
	public final int LEX_GEQ = 109;
	public final int IF = 116;
	
	
	// Binary commutative relational operators
	// already ordered!
	public final int EQ = 100;
	public final int NEQ = 101;
	public final int IFF = 115;
	public final int OR  = 117;
	public final int AND = 118;
	public final int XOR = 119; // no support for this operator!
	
	// Binary arithmetic commutative operators
	public final int PLUS = 110;
	public final int MINUS = 111;
	public final int MIN = 120;
	public final int MAX = 121;
	
	//public final int SUM = 200;
	
	// Binary arithmetic non-commutative operators
	public final int DIV = 112;
	public final int MULT = 113;
	public final int POWER = 114;
	public final int MOD = 115;
	
	// Quantifications
	
	public final int SUM = 200;
	public final int Q_SUM = 201;
	public final int FORALL = 202;
	public final int EXISTS = 203;
	
	// Global Constraints
	//public final int ALLDIFFERENT = 220;  => is defined above
	public final int TABLE_CONSTRAINT = 221;
	public final int ELEMENT_CONSTRAINT = 222;
	public final int ATMOST_CONSTRAINT = 223;
	public final int ATLEAST_CONSTRAINT = 224;
	public final int OCCURRENCE = 225;
	
	public final int REIFICATION = 250;
	
    //	 x1 + .. + xn = y (no implementation yet)
	public final int NARY_SUMEQ_CONSTRAINT = 251; 
	public final int NARY_SUMLEQ_CONSTRAINT = 254; 
	public final int NARY_SUMGEQ_CONSTRAINT = 257; 
	public final int NARY_SUMNEQ_CONSTRAINT = 252; 
	public final int NARY_SUMLESS_CONSTRAINT = 253; 
	public final int NARY_SUMGREATER_CONSTRAINT = 255; 
	public final int NARY_PRODUCT_CONSTRAINT = 290;
	public final int NARY_DISJUNCTION = 291;
	public final int NARY_CONJUNCTION = 292;
	public final int BINARY_SUMEQ_CONSTRAINT = 270;
	public final int BINARY_SUMNEQ_CONSTRAINT = 271;
	public final int BINARY_SUMLESS_CONSTRAINT = 272;
	public final int BINARY_SUMLEQ_CONSTRAINT = 273;
	public final int BINARY_SUMGREATER_CONSTRAINT = 274;
	public final int BINARY_SUMGEQ_CONSTRAINT = 275;
	public final int BINARY_PRODUCT_CONSTRAINT = 280;
	public final int ABSOLUTE_CONSTRAINT = 281;
	public final int MIN_CONSTRAINT = 282;
	public final int MAX_CONSTRAINT = 283;
	
	// other implicit operations
	public final int ARRAY_INDEXING = 300;
	
	
	public final int OBJECTIVE = 500;
	// --------------------------------------------------------
	
	
	public final char SMALLER = 's';
	public final char BIGGER = 'g';
	public final char EQUAL = 'e';
	
	/**
	 * Orders the Expression expression and returns
	 * the ordered Expression 
	 * @param expression
	 * @return
	 */
	public void orderExpression();
	
	
	/**
	 * Returns the type of the expression (e.g. integer,
	 * conjunction, etc) 
	 * @return
	 */
	public int getType();
	
	/**
	 * Returns a deep copy of the Expression
	 * @return
	 */
	public Expression copy();
	
	/**
	 * Returns the String representation of the expression
	 * @return
	 */
	public String toString();
	
	
	/**
	 * !!! This method can only be used when parameters are all
	 * known !!! <br>
	 * <br>
	 * Returns the domain of the current expression, that is,
	 * the upper and lower bound of the expression. If the 
	 * expression is an sparse-domain variable, the sparse
	 * domain will be returned. If the expression consists 
	 * of a sparse domain element, this element is mapped to a 
	 * bounds element.<br>
	 * The first list-element is the smallest element.
	 * If only one value is returned, the Expression is
	 * a constant (or a decision variable with lb=ub)
	 *  
	 * @return
	 */
	public int[] getDomain();
	
	
	/**
	 * This method returns a value to order the expression, when compared
	 * to another one. If the orderWeight of expression E1 is smaller
	 * than the orderWeight of E2, then E1 is smaller than E2 according to 
	 * the Essence' order of expressions.
	 * The weights are basically corresponding to the types, so (the relations
	 * between) the type-values MUST NOT be changed!
	 * 
	 * @return
	 */
	//public int getWeight();
	
	
	/**
	 * This method may ONLY be called if the parameter expression
	 * has EXACTLY the same type as the object!!<br>
	 * <br>
	 * Returns true if the expression-object is smaller than the 
	 * parameter Expression e (according to the Expression ordering
	 * of Essence'). The subexpressions are assumed to be already
	 * ordered! <br>
	 * Return types are either SMALLER, BIGGER or EQUAL.
	 */
	public char isSmallerThanSameType(Expression e); 
	
	
	/**
	 * This method evaluates the object expression. It's type might change!
	 * Please note that the result from evaluation is better if the expression
	 * has been ordered first! 
	 *
	 */
	public Expression evaluate();
	
	
	
	/**
	 * Optimises the expression tree: merges adjacent n-ary nodes of the same 
	 * type such that linear sums or conjunction can be detected and represented
	 * by a single n-ary node in the expression tree (instead of several binary
	 * nodes). 
	 * 
	 * @return
	 */
	public Expression reduceExpressionTree();
	
	/**
	 * Insert the int value for the variable/parameter/identifier with the 
	 * name variableName.
	 * 
	 * @param value
	 * @param variableName
	 * @return the expression with all occurrences of variableName replaced with
	 * int value
	 */
	public Expression insertValueForVariable(int value, String variableName);
	
		
	/**
	 * Insert the boolean value for the variable/parameter/identifier 
	 * given by the variablenName.
	 * 
	 * @param value
	 * @param variableName
	 * @return the expression with all occurrences of variableName replaced with
	 * boolean value
	 */
	public Expression insertValueForVariable(boolean value, String variableName);
	
	/**
	 * This method is used to 
	 *  - either replace 'variableName' with a domain
	 *  - or turn a simple Variable (that has an unknown domain) 
	 *    with name 'variableName' into a proper variable with domain 'domain' 
	 * 
	 * @param domain
	 * @param variableName
	 * @return
	 * @throws Exception TODO
	 */
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception;
	
	/**
	 * Gives information if the expression is a subexpression. By default, all
	 * expressions are nested when created.
	 * @return true if the expression is nested by another expression
	 */
	public boolean isNested();
	
	/**
	 * Sets the expression to be not nested. By default, every expression is nested.
	 * This method has not been used anywhere in the flattening process.
	 */
	public void setIsNotNested();
	
	
	/**
	 * Returns true, if this expression has to be flattened to a single variable,
	 * i.e. the flattening process (or any other process) requires the expression
	 * to be represented as a variable only
	 * 
	 * @return true if the expression needs to be flattened to a single variable 
	 * and false if not
	 */
	public  boolean isGonnaBeFlattenedToVariable();
	
	/**
	 * Set if this expression should be represented by a single variable or not.
	 * 
	 * @param reified
	 */
	public void willBeFlattenedToVariable(boolean reified);
	
	
	/**
	 * Restructuring works mainly on equations and relations, but also on conjunctions
	 * and disjunctions. It includes cancellation of expressions and merging sums of 
	 * expressions to one side of a relation.
	 * It also reduces the expression tree and performs evaluation and ordering
	 * 
	 * @return the restructured expression
	 */
	public Expression restructure();
	
	
	
	/**
	 * Replace every occurrence of oldVariable with newVariable => do not
	 * care about if they have the same domain. This method should only be
	 * evoked if the replacement does not alter the problem.
	 * 
	 * @param oldVariable the variable that is to be replaced
	 * @param newVariable the variable that takes the place of oldVariable
	 * @return the expression where every occurrence of oldVariable has been replaced 
	 *          by newVariable
	 */
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable);
	
	
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression);
}
