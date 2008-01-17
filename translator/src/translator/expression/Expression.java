package translator.expression;




public interface Expression {

	public final boolean DEBUG = true;
	
	// RelationalAtomExpression.java
	public final int BOOL = 0;
	public final int BOOL_VARIABLE = 5;
	public final int BOOL_VARIABLE_ARRAY_ELEM = 6;
		
	// --------------------------------------------------------
	
	//ArithmeticAtomExpressions.java
	public final int INT = 3;
	public final int INT_VAR = 21;
	public final int INT_ARRAY_VAR = 22;
	
	
	// internal types for Variables
	/** a decision variable that is indexed by a constant value and has an integer bounds domain */
	public final int SINGLE_VARIABLE = 11;
	
	/** an array element that is indexed by a constant value and has an integer bounds domain */
	public final int ARRAY_VARIABLE = 14;

	
    //	 --------------------------------------------------------
	// Unary Relational Expressions
	public final int NEGATION = 31;
	public final int ALLDIFFERENT = 33;
	
	
	// unary arithmetic expressions
	public final int U_MINUS = 30;
	public final int ABS = 32;
	
	
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
	
	// Binary arithmetic commutative operators
	public final int PLUS = 110;
	public final int MINUS = 111;
	
	//public final int SUM = 200;
	
	// Binary arithmetic non-commutative operators
	public final int DIV = 112;
	public final int MULT = 113;
	public final int POWER = 114;
	
	// Quantifications
	
	public final int SUM = 200;
	public final int FORALL = 201;
	public final int EXISTS = 202;
	
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
	
}
