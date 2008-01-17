package preprocessor;

import conjureEssenceSpecification.*;

import java.util.HashMap;


/**
 * ExpressionEvaluator evaluates Essence' expressions.
 *  Expressions are either atoms
 *  (boolean, integer or (matrix-)identifiers) or expressions 
 *  composed by unary and binary operators or quantifications.
 *  For further information, 
 *  check the documentation of expressions in the conjure package.
 *  <br>
 *  Only expressions in Essence' are evaluated - for instance, 
 *  sets and set-operators are illegal and exceptions are thrown 
 *  accordingly.
 *  <br>
 *  <br>
 *  Observations:
 *  <ol>
 *   <li> An expression can be evaluated to an atomic expression 
 *   	 (a number or a boolean) if
 *       it does not contain a decision variable.
 *   <li> If an expression contains a decision variable, then it 
 *       contains a relational operator of the following set : 
 *	{ =, !=, <, >, <=, >=}
 *    <li> If the expression contains a relational operator, then 
 *        there is some further evaluation that can be done.
 *   <li> all feasible expressions not containing a relational operator can
 *       be evaluated to an atomic expression.
 *  </ol>
 *  This evaluator does NOT simplify expressions by doing arithmetic on the 
 *  whole expression, such as transforming "x + 3 = 4" into "x = 1", but
 *  only on one side of the relational operator.
 */

public class ExpressionEvaluator implements PreprocessorGlobals {

	/** contains all defined parameters (simple values or domains) and their value */
	HashMap<String, Constant> parameters;

	Parameters parameterArrays;
    /** 
     *  @param params a HashMap<String, Constant> containing all constant  
     *                   identifiers with their corresponding value.
     * @param parameterArray TODO
    */
    public ExpressionEvaluator(HashMap<String, Constant> params, Parameters parameterArray) {
    	parameters = params;
    	this.parameterArrays = parameterArray;
    }

    /** 
     *  Evaluates Expression e to an AtomicExpression. This method should    
     *  only be evoked, if the resulting expression explicitly has to
     *  be atomic - meaning that the Expression e must not contain any
     *  decision variable. It does not check, if e is an illegal atomic
     *  expression.
     *
     *  @param e an Expression containing no decision variables
     *  @return an AtomicExpression resulting from evaluating e
     *  @exception PreprocessorException is thrown when 
     *  the Expression cannot be evaluated to an AtomicExpression
     *  
    */
    public AtomicExpression evaluateExpression(Expression e) 
	throws PreprocessorException {
	
	switch(e.getRestrictionMode()) {
	    
	case EssenceGlobals.ATOMIC_EXPR:
	    return e.getAtomicExpression();

	case EssenceGlobals.UNITOP_EXPR:
	    return evaluateExpression(evalUnitOpExpression(e.getUnaryExpression() ));
	    
	case EssenceGlobals.BINARYOP_EXPR:
	    return evaluateExpression(evalBinaryExpression(e.getBinaryExpression() ));
	    
	    
	default:
	    throw new PreprocessorException
		("Evaluation of the Expression '"+e.toString()+"' does not evaluate to an atomic expression.");

	} // end switch:Expr.restr_mode	

    }


    /** 
     *  Evaluates Expression e to an Expression, that only consists
     *  of decision variables combined with integers/booleans that 
     *  cannot be further evaluated (without applying artihmetic on
     *  the whole expression). 
     *
     *  @param e an Expression to be evaluated
     *  @return the evaluated Expression e
     *  @exception PreprocessorException          
       */
    public Expression evalExpression(Expression e) 
	throws PreprocessorException {      
	
	switch(e.getRestrictionMode()) {
	    
	    
	case EssenceGlobals.BRACKET_EXPR:// (E)
	    return (evalExpression(e.getExpression()));
	    
	    
	case EssenceGlobals.ATOMIC_EXPR:
	    // 	if we found an identifier, check if it is a parameter
		if(e.getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
			
			print_debug("evaluating identifier in an expression:"+e.getAtomicExpression().getString());
			
			if(parameters.containsKey(e.getAtomicExpression().getString())) {
				AtomicExpression atomicExpression = e.getAtomicExpression();	
				Constant c = (Constant) parameters.get(atomicExpression.getString());
				if(c == null) /* we did not find e in the parameter-hashmap*/ 
					return e;
		    		
				if(c.getRestrictionMode() == EssenceGlobals.CONSTANT_DOMAIN) 
					throw new PreprocessorException("Domain identifier '"+atomicExpression.toString()+"' used in constraint expression.");
		    
				Expression new_e = c.getExpressionConstant().getExpression();
				return evalExpression(new_e);
			}
		
		}
		return e;	
	    
	case EssenceGlobals.NONATOMIC_EXPR: // ID [ E1, E2, .. En ]  with n >= 1
	    Expression[] exps = e.getNonAtomicExpression().getExpressionList();
	    
	    for(int i = 0; i < exps.length; i++)
		exps[i] = evalExpression(exps[i]);

	    // check if it is a parameter
	    if(e.getNonAtomicExpression().getExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
	    	if(e.getNonAtomicExpression().getExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER){
	    		String arrayName = e.getNonAtomicExpression().getExpression().getAtomicExpression().getString();
	    		if(parameterArrays.isParameter(arrayName)) {
	    			return insertParameterArrayElement(new NonAtomicExpression(EssenceGlobals.NONATOMIC_EXPR_BRACKET,
							  							e.getNonAtomicExpression().getExpression(),
							  							exps));
	    		}
	    	}
	    		
	    }
	    
	    return new Expression(new NonAtomicExpression(EssenceGlobals.NONATOMIC_EXPR_BRACKET,
							  e.getNonAtomicExpression().getExpression(),
							  exps));


	case EssenceGlobals.UNITOP_EXPR:
	    return evalUnitOpExpression(e.getUnaryExpression());
	    

	case EssenceGlobals.BINARYOP_EXPR:
	    return evalBinaryExpression(e.getBinaryExpression());
	    

	case EssenceGlobals.FUNCTIONOP_EXPR:
	    return evalFunctionOpExpression(e.getFunctionExpression());

	    
	case EssenceGlobals.QUANTIFIER_EXPR:
		return evalQuantifierExpression(e.getQuantification());

	case EssenceGlobals.LEX_EXPR:
		return new Expression(new LexExpression(evalExpression(e.getLexExpression().getLeftExpression()),
				                 e.getLexExpression().getLexOperator(),
				                 evalExpression(e.getLexExpression().getRightExpression())));
	    
	default:
	    throw new PreprocessorException
		("Evaluation of the Expression '"+e.toString()+"' is not supported (yet).");
	    
	} // end switch:Expr.restr_mode
	
	
  }
  
    
    private Expression insertParameterArrayElement (NonAtomicExpression matrixElement) 
    	throws PreprocessorException {
    	
    	String arrayName = matrixElement.getExpression().getAtomicExpression().getString();
    	Expression[] indexExpressions = matrixElement.getExpressionList();
    	
  	  	for(int i=0; i<indexExpressions.length; i++) {
  	  		if(indexExpressions[i].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
  	  			return new Expression(matrixElement);
		  
  	  		if(indexExpressions[i].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
  	  			return new Expression(matrixElement);
  	  	}
    	
    	switch(indexExpressions.length) {
    	
  	  case 1: // vector
		  int index = indexExpressions[0].getAtomicExpression().getNumber();
		  if(!parameterArrays.isParameterVector(arrayName))
			  throw new PreprocessorException
			  	("Cannot apply parameter array element '"+matrixElement.toString()+"'.'"+arrayName+"' is not a known parameter vector.");
	  
		  int vectorValue = parameterArrays.getVectorElementAt(arrayName, index);
		  return new Expression(new AtomicExpression(vectorValue));
	  
	  case 2: // matrix
		  int rowIndex = indexExpressions[0].getAtomicExpression().getNumber();
		  int colIndex = indexExpressions[1].getAtomicExpression().getNumber();
		  if(!parameterArrays.isParameterMatrix(arrayName))
			  throw new PreprocessorException
			  	("Cannot apply parameter array element '"+matrixElement.toString()+"'.'"+arrayName+"' is not a known parameter matrix."); 
	  
		  int matrixValue = parameterArrays.getMatrixElementAt(arrayName, rowIndex, colIndex);
		  if(!(matrixValue == UNDEFINED_PARAMETER_ARRAY_ELEMENT))
			  return new Expression(new AtomicExpression(matrixValue));
		  else return new Expression(matrixElement);
	  
	  case 3: // cube
		  int plane = indexExpressions[0].getAtomicExpression().getNumber();
		  int row = indexExpressions[1].getAtomicExpression().getNumber();
		  int col = indexExpressions[2].getAtomicExpression().getNumber();
		  if(!parameterArrays.isParameterCube(arrayName))
			  throw new PreprocessorException
			  	("Cannot apply parameter array element '"+matrixElement.toString()+"'.'"+arrayName+"' is not a known parameter cube."); 
		  
		  int cubeValue = parameterArrays.getCubeElementAt(arrayName, plane, row, col);
		  return new Expression(new AtomicExpression(cubeValue));
		  
	  default:
		  throw new PreprocessorException
		  	("Parameter arrays with more than 3 dimensions are not supported yet:"+matrixElement.toString());
    	
  	  	
    	}
    	
    }
    
    
    /**
     *  Evaluates UnaryExpression e to an Expression, that only consists
     *  of decision variables and integers/booleans and cannot be further 
     *  evaluated but split into subexpressions.<br>
     *  IMPORTANT NOTE: the evaluation of the absolute value is not very advanced.
     *
     *  @param e an UnitOpExpression containing no decision variables
     *  @return an Expression resulting from evaluating e
     *  @exception PreprocessorException        
      */

    private Expression evalUnitOpExpression(UnaryExpression e) 
		throws PreprocessorException {

	// evaluate inner expression
	Expression inner_expr = evalExpression(e.getExpression());
	
	switch(e.getRestrictionMode()) {
	    
	case EssenceGlobals.NEGATION:
	    // check if we have a double negation
	    print_debug("we found a negation: "+e.toString());

	    if(inner_expr.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) { 
		if(inner_expr.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION)
		    return inner_expr.getUnaryExpression().getExpression(); // -(-(e)) == e		   
	    }
	    // check if we have a double negation
	    else if(inner_expr.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) { 
		if(inner_expr.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)
		    if(inner_expr.getAtomicExpression().getNumber() < 0)
			return new Expression(new AtomicExpression(inner_expr.getAtomicExpression().getNumber()));
		    else 
			return new Expression(new AtomicExpression(-(inner_expr.getAtomicExpression().getNumber())));
	    }
	    else if (inner_expr.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) { 
		if(inner_expr.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
		    print_message
			("WARNING: interpreting negation-operator as NOT when applied to boolean expression: "
			 +e.toString());
		    return (inner_expr.getAtomicExpression().getBool()) ?
			new Expression(new AtomicExpression(true)) :
			new Expression(new AtomicExpression(false));
		}
	    }

	    
	    return new Expression(new UnaryExpression(EssenceGlobals.NEGATION,inner_expr));
	    


	case EssenceGlobals.ABS:
	    // check if the expression is negative: | NEG(e) |
	    if(inner_expr.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) { 
		if(inner_expr.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION)
		    return inner_expr.getUnaryExpression().getExpression(); // |-(e)| == e
	    }
	    else if(inner_expr.getRestrictionMode() == EssenceGlobals.NUMBER) {
		int number = inner_expr.getAtomicExpression().getNumber();
		return (number < 0) ?
		    new Expression(new AtomicExpression(number - 2*number)) : 
		    new Expression(new AtomicExpression(number));
	    }
	    else if(inner_expr.getRestrictionMode() == EssenceGlobals.BOOLEAN) {		
		return new Expression(inner_expr);
	    }
	    else // i don't know how to further evaluate exprs like | x - 5 |
	    	return new Expression(new UnaryExpression(EssenceGlobals.ABS,inner_expr));
	    
	    
	case EssenceGlobals.NOT:
	    switch(inner_expr.getRestrictionMode()) {
		
	    case EssenceGlobals.ATOMIC_EXPR:
		// we have a boolean
		if(inner_expr.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)
		    return (inner_expr.getAtomicExpression().getBool()) ? 
			new Expression(new AtomicExpression(false)) :
			new Expression(new AtomicExpression(true));
		
		// we have a variable
		else if (inner_expr.getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER)
		    return new Expression(new UnaryExpression(EssenceGlobals.NOT,inner_expr));		

		else throw new PreprocessorException
			 ("'NOT'-operator used on non-boolean value/identifier in expression "+e.toString());
		
	    default:
		return new Expression(new UnaryExpression(EssenceGlobals.NOT,inner_expr));
		
	    }// end: switch(NOT-expr.restr_mode)	    			    

	default:
	    throw new PreprocessorException
		("Evaluation of the Expression '"+e.toString()+"' is not supported (yet).");		
	} //end: switch(expr.restr_mode)	    			

    }



    /** 
     * Evaluates binary Expressions. This is the far most complicated
     * method and also evokes the private method 
     * "Expression evalBinExpressionsAdvanced(BinaryOpExpression e)"
     * which takes care of rather nasty cases for evaluation.
     * <br>
     * Here are the evaluation rules in detail
     * (for both evalBinaryExpression(..) and evalBinExpressionAdvanced(..)
     * which is marked with A). All composed expressions are evaluated 
     * before applying the evaluation rules.
     * <br>
     *(b_i ... boolean atoms, n_i ... integer atoms, E_i ... arbitrary 
     * composed expression) 
     * <ol>
     * <li> E1 + E2 --> eval(E1) + eval(E2)
     *   <ul>
     *    <li> n1 + n2 -->  n
     *    <li> b1 + b2 --> if (b1 and b2) then n else b
     *    <li> A: n1 + (n2 + E) --> n + E
     *    <li> A: n1 + (n2 - E) --> n - E
     *    <li> A: (n2 + E) + n1 --> n + E
     *    <li> A: (n2 - E) + n1 --> n - E
     *    <li> and analogous cases 
     *   </ul>
     * <li> E1 - E2 --> eval(E1) - eval(E2)
     *   <ul>
     *    <li> n1 - n2 -->  n
     *    <li> b1 - b2 --> b 
     *    <li> A: n1 - (n2 + E) --> n - E
     *    <li> A: n1 - (n2 - E) --> n + E
     *    <li> A: (n2 + E) - n1 --> n + E
     *    <li> A: (n2 - E) - n1 --> n - E
     *    <li> and analogous cases 
     *   </ul>
     * <li> E1 * E2 --> eval(E1) * eval(E2)
     *   <ul>
     *    <li> n1 * n2 -->  n
     *    <li> b1 * b2 --> b
     *   </ul>
     * <li> E1 \ E2 --> eval(E1) \ eval(E2)
     *   <ul>
     *    <li> n1 \ n2 -->  n
     *    <li> b1 \ b2 --> b
     *   </ul>
     * <li> E1 /\ E2 --> eval(E1) /\ eval(E2)
     *   <ul>
     *    <li> b1 /\ b2 --> b
     *    <li> b /\ E -->  if b then E else false
     *    <li> and analogous cases      
     *   </ul>
     * <li> E1 \/ E2 --> eval(E1) \/ eval(E2)
     *   <ul>
     *    <li> b1 \/ b2 --> b
     *    <li> b \/ E -->  if b then true else E
     *    <li> and analogous cases 
     *   </ul>
     * <li> E1 op E2 ---> eval(E1) op eval (E2)
     *
     * </ol>
     @param e is a BinaryOpExpression to be evaluated
     @return Expression 
     @throws PreprocessorException
        */

    private Expression evalBinaryExpression(BinaryExpression e) 
	  throws PreprocessorException {
	
	// evalutate both nested expressions
    print_debug("evaluating the binary expression "+e.toString());   	
	Expression e_left = evalExpression(e.getLeftExpression());
	print_debug("evaluated the left expression to '"+e_left+"' of binary expression "+e.toString());   
	Expression e_right = evalExpression(e.getRightExpression());
	print_debug("evaluated the right expression to '"+e_right+"' of binary expression "+e.toString());   
	
	
	switch(e.getOperator().getRestrictionMode()) {
	
	case EssenceGlobals.PLUS:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	    	(e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
	    	if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
	    			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) 
	    		return new Expression(new AtomicExpression
	    				(e_left.getAtomicExpression().getNumber() + e_right.getAtomicExpression().getNumber()));	    
	    	else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
	    			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
	    		if(e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool())
	    			return new Expression(new AtomicExpression(2));
	    		else if(e_left.getAtomicExpression().getBool() || e_right.getAtomicExpression().getBool())
	    			return new Expression(new AtomicExpression(true));
	    		else 
	    			return new Expression(new AtomicExpression(false));
	    	}
	        print_debug("both atomic with an +, but not evaluatable");
	    } // E1 + (-E2) ==> E1 - E2
	    else if(e_right.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
	    	if(e_right.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION) {
	    		return new Expression(new BinaryExpression(e_left,new BinaryOperator(EssenceGlobals.MINUS),e_right.getUnaryExpression().getExpression()));
	    	}	
	    } // E1 + (E21 *|/ -E22) ==> E1 - (E21 *|/ E22) 
	    else if(e_right.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
	    	if(e_right.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
	    		if(e_right.getBinaryExpression().getRightExpression().getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION)
	    			return new Expression(new BinaryExpression(e_left,new BinaryOperator(EssenceGlobals.MINUS),
	    					                                   new Expression(new BinaryExpression(e_right.getBinaryExpression().getLeftExpression(),
	    					                                		   e_right.getBinaryExpression().getOperator(),
	    					                                		   e_right.getBinaryExpression().getRightExpression().getUnaryExpression().getExpression()))));
	    	}
	    	else if(e_right.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
	    		if(e_right.getBinaryExpression().getLeftExpression().getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION)
	    			return new Expression(new BinaryExpression(e_left, 
	    					new BinaryOperator(EssenceGlobals.MINUS),
	    					new Expression(new BinaryExpression(e_right.getBinaryExpression().getLeftExpression().getUnaryExpression().getExpression(),
	    					                                   e_right.getBinaryExpression().getOperator(),
	    					                                   e_right.getBinaryExpression().getRightExpression()))));
	    	}
	    	
	    }

	    break;


	case EssenceGlobals.MINUS:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    int result  = e_left.getAtomicExpression().getNumber() - e_right.getAtomicExpression().getNumber();
		    return new Expression(new AtomicExpression(result));
		}

		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) ||
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) 
		    if(e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool())
			return new Expression(new AtomicExpression(false));
		    else if(e_left.getAtomicExpression().getBool() && !e_right.getAtomicExpression().getBool())
			return new Expression(new AtomicExpression(true));
		    else if(!e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool())
			return new Expression(new AtomicExpression(-1));
		    else 
			return new Expression(new AtomicExpression(false));
	    
	    //	  E1 - (-E2) ==> E1 + E2
	    else if(e_right.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
	    	if(e_right.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION) {
	    		return new Expression(new BinaryExpression(e_left,new BinaryOperator(EssenceGlobals.PLUS),e_right.getUnaryExpression().getExpression()));
	    	}	
	    } // E1 - (E21 *|/ -E22) ==> E1 + (E21 *|/ E22) 
	    else if(e_right.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
	    	if(e_right.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
	    		if(e_right.getBinaryExpression().getRightExpression().getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION)
	    			return new Expression(new BinaryExpression(e_left,new BinaryOperator(EssenceGlobals.PLUS),
	    					                                   e_right.getUnaryExpression().getExpression()));
	    	}
	    	else if(e_left.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
	    		if(e_left.getBinaryExpression().getRightExpression().getUnaryExpression().getRestrictionMode() == EssenceGlobals.NEGATION)
	    			return new Expression(new BinaryExpression(e_left.getUnaryExpression().getExpression(),
	    					                                   new BinaryOperator(EssenceGlobals.PLUS),e_right));
	    	}
	    	
	    }
	    
	    
	    break;


	case EssenceGlobals.MULT:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) 
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() * e_right.getAtomicExpression().getNumber()));
	    
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    if (e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool())
			return new Expression(new AtomicExpression(true));
		    else 
			return new Expression(new AtomicExpression(false));
		}
		    
	    
	    break;


	case EssenceGlobals.DIVIDE:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    // the user has to care for him/herself if the division might produce real numbers
		    if(e_right.getAtomicExpression().getNumber() != 0) { 
			int result  = (e_left.getAtomicExpression().getNumber()) / (e_right.getAtomicExpression().getNumber());
			return new Expression(new AtomicExpression(result));
		    }
		    else 
			throw new PreprocessorException 
			    ("Division by zero in expression: "+e.toString());
		}

		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) ||
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) 

		    if(e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool()) //  1/1 = 1
			return new Expression(new AtomicExpression(true)); 

		    else if(!e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool()) // 0/1 = 0
			return new Expression(new AtomicExpression(false));

		    else if (!e_right.getAtomicExpression().getBool()) // 0/0 or 1/0 is undefined 
			throw new PreprocessorException 
			    ("Division bt zero in expression :"+e.toString());
	    break;


	case EssenceGlobals.POWER:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    // compute the E1 to power of E2
		    int result = 1;
		    int base = e_left.getAtomicExpression().getNumber();
		    int exponent = e_right.getAtomicExpression().getNumber();
		    if(exponent>=0)
			for(int i=0;i< exponent; i++) 
			    result = result*base;
		    else {
			for(int i=exponent;i<0; i++)
			    result = result*base;
			result = 1/result;
		    }
		    return new Expression(new AtomicExpression(result));
		}

		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) 
		    if(e_left.getAtomicExpression().getBool())// 1^(0,1) = 1
			return new Expression(new AtomicExpression(true));

		    else if(e_right.getAtomicExpression().getBool()) // 0^1 = 0
			return new Expression(new AtomicExpression(false));

		    else if (!e_right.getAtomicExpression().getBool()) { // 0^0 is not clearly defined 
			System.out.println
			    ("Zero to power of zero is interpreted as being 1. (0^0 = 1(true))");
			return new Expression(new AtomicExpression(true));
		    }

	    break;


	case EssenceGlobals.AND:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) 
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getBool() && e_right.getAtomicExpression().getBool()));	    
	    //BOOL and E
		else if(e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		    if(e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
			return (e_left.getAtomicExpression().getBool()) ?
			    e_right :
			    new Expression(new AtomicExpression(false));
		    } 
		}
	    //E and BOOL
		else if(e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		    if(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
			return (e_right.getAtomicExpression().getBool()) ?
			    e_left  :
			    new Expression(new AtomicExpression(false));
		    } 
		    
		}
	    break;
	    
	    
	case EssenceGlobals.OR:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) 
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getBool() || e_right.getAtomicExpression().getBool()));	    
	    }
	    // BOOL or E
	    else if(e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		if(e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
		    return (e_left.getAtomicExpression().getBool()) ?
			new Expression(new AtomicExpression(true)) :
			e_right;
		} 
	    }
	    // E or BOOL
	    else if(e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		if(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
		    return (e_right.getAtomicExpression().getBool()) ?
			new Expression(new AtomicExpression(true)) :
			e_left;
		} 
	    }
	    
	    break;
	    
	    
	case EssenceGlobals.IF:  // e1 => e2  (implies)
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    boolean left_bool = e_left.getAtomicExpression().getBool();
		    boolean right_bool =  e_right.getAtomicExpression().getBool();
		    return (left_bool == true && right_bool == false) ?
			new Expression(new AtomicExpression(false)) :	    			
			new Expression(new AtomicExpression(true));	    			
		}
	    if(e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		if(e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
		    if(e_left.getAtomicExpression().getBool()) // true IF Expr  ---->  Expr
			return e_right;
		    else return new Expression(new AtomicExpression(false));  // false IF Expr  ---> false
		}

	    }

	    break;

	    
	case EssenceGlobals.IFF:  // e1 <=> e2  (double implication)
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) )
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    boolean left_bool = e_left.getAtomicExpression().getBool();
		    boolean right_bool =  e_right.getAtomicExpression().getBool();
		    return new Expression(new AtomicExpression(left_bool == right_bool));	    			
		}		    
	    break;
	    
	    //	default:
	    //throw new PreprocessorException
	    //("Evaluation of binary expression "+e.toString()+"is not supperted (yet).");
	    
	case EssenceGlobals.EQ:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getBool() == e_right.getAtomicExpression().getBool()));
		}
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() == e_right.getAtomicExpression().getNumber()));
		}
	    }
	    break;
	    
	case EssenceGlobals.NEQ:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getBool() != e_right.getAtomicExpression().getBool()));
		}
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() != e_right.getAtomicExpression().getNumber()));
		}
	    }
	    break;
	    

	case EssenceGlobals.LEQ:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    return new Expression(new AtomicExpression  
					  ( ! ((e_left.getAtomicExpression().getBool() == true) &&  
					   (e_right.getAtomicExpression().getBool() == false)) ));			
		}
		
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() <= e_right.getAtomicExpression().getNumber()));
		}
	    }
	    break;


	case EssenceGlobals.LESS:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    return new Expression(new AtomicExpression // false (0) < true (1)
					  ( (e_left.getAtomicExpression().getBool() == false) &&  
					   (e_right.getAtomicExpression().getBool() == true) ));			
		}
		
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() < e_right.getAtomicExpression().getNumber()));
		}
	    }
	    break;



	case EssenceGlobals.GEQ:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    return new Expression(new AtomicExpression
					  ( ! ((e_left.getAtomicExpression().getBool() == false) &&  
					   (e_right.getAtomicExpression().getBool() == true)) ));			
		}
		
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() >= e_right.getAtomicExpression().getNumber()));
		}
	    }
	    break;



	case EssenceGlobals.GREATER:
	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ) {
		if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) &&
		   (e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)) {
		    return new Expression(new AtomicExpression  // true (1) > false (1)
					  ( (e_left.getAtomicExpression().getBool() == true) &&  
					   (e_right.getAtomicExpression().getBool() == false) ));			
		}
		
		else if((e_left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) &&
			(e_right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)) {
		    return new Expression(new AtomicExpression
					  (e_left.getAtomicExpression().getNumber() > e_right.getAtomicExpression().getNumber()));
		}
	    }
	    break;	    	    

	default:



	    // the following operators are not allowed: intersec, union, subset_of, member_of
	    if(e.getOperator().getRestrictionMode() == EssenceGlobals.INTERSEC ||
	       e.getOperator().getRestrictionMode() == EssenceGlobals.UNION ||
	       e.getOperator().getRestrictionMode() == EssenceGlobals.SUBSET_OP ||
	       e.getOperator().getRestrictionMode() == EssenceGlobals.MEMBER_OP)
		throw new PreprocessorException
		    ("Illegal operator '"+e.getOperator().toString()+"' found in expression: "+e.toString());


	    // have to construct new Object because I have evaluated 
	    // both sub-expressions e_left and e_right
	    break;
	}

    print_debug("tried it all. now evaluating the binary expression more advanced.");
	return evalBinExpressionAdvanced(new BinaryExpression(e_left,e.getOperator(),e_right) );

	//	return new Expression(new BinaryOpExpression(e_left,e.getbiop(),e_right));
    }

    /** 
     *	This method is concerned eith further evaluation of
     *  BinaryOpExpressions and is evoked in certain
     *  cases by evalBinaryExpression(e). An example for further 
     *  evaluation would be 5 - (E + 3) ---> (5-3) - E ---> 2 - E
     *
     *  Further details including the evaluation rules can be found in the
     *  documentation of the method evalBinaryExpression(BinaryOpExpression e).
     *  @param e the BinaryOpExpression to be evaluated
     *  @return evaluated Expression
     *  @throws PreprocessorException 
     *  
     */

    private Expression evalBinExpressionAdvanced(BinaryExpression e) 
		throws PreprocessorException {
	
	// ATOM op1 (E1 op2 E2)
	if((e.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	   (e.getRightExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR  )){
	    AtomicExpression ae_left = e.getLeftExpression().getAtomicExpression();
	    BinaryExpression binExp_r = (evalBinExpressionAdvanced(e.getRightExpression().getBinaryExpression())).getBinaryExpression();
	    
	    // NUM op1 (E1 op2 E2)
	    if(ae_left.getRestrictionMode() == EssenceGlobals.NUMBER){
		BinaryOperator op1 = e.getOperator();
		BinaryOperator op2 = binExp_r.getOperator();
		
		// NUM1 op1 (NUM2 op2 E) 		    
		if(binExp_r.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_r.getLeftExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			
			switch(op1.getRestrictionMode()) {			    
			    
			    
			    // NUM1 + (NUM2 op E) ==> eval(NUM1 + NUM2) op E             iff op \in {+,-}
			case EssenceGlobals.PLUS: // op must not be nonlinear operator
			    if((op2.getRestrictionMode() == EssenceGlobals.MINUS) ||
			       (op2.getRestrictionMode() == EssenceGlobals.PLUS)){
				Expression expr = new Expression(new AtomicExpression
								 (ae_left.getNumber() +
								  binExp_r.getLeftExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression(expr,
									     op2,
									     binExp_r.getRightExpression() ));
			    }				
			    // op is non-linear
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE)||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) { 			    
				// sort this out later (tmp variables etc) !!!
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    
			    
			    // NUM1 - (NUM2 op E) = eval(NUM1 - NUM2) switch(op) E      iff op \in {+,-}
			case EssenceGlobals.MINUS:

			    // NUM1 - (NUM2 + E) --> eval(NUM1 - NUM2) - E
			    if(op2.getRestrictionMode() == EssenceGlobals.PLUS) {
				Expression expr = new Expression(new AtomicExpression
								 (ae_left.getNumber() - 
								  binExp_r.getLeftExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression
						      (expr, 
						       new BinaryOperator(EssenceGlobals.MINUS),
						       binExp_r.getRightExpression() ));
			    }

			    // NUM1 - (NUM2 - E) --> eval(NUM1 - NUM2) + E
			    else if (op2.getRestrictionMode() == EssenceGlobals.MINUS) {
				Expression expr = new Expression(new AtomicExpression
								 (ae_left.getNumber() -
								  binExp_r.getLeftExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression
						      (expr, 
						       new BinaryOperator(EssenceGlobals.PLUS), 
						       binExp_r.getRightExpression()));
			    }
			    			    
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE) ||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) {		
				// sort this out later (tmp variables etc)			    
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			}
		    }
		}
		// NUM1 op1 (E op2 NUM2)
		else if(binExp_r.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_r.getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			
			
			switch(op1.getRestrictionMode()) {
			    
			    
			    // NUM1 + (E op2 NUM2) ---> eval(NUM1 op2 NUM2) + E             iff op2 \in {+,-}
			case EssenceGlobals.PLUS: // op must not be nonlinear operator

			    if((op2.getRestrictionMode() == EssenceGlobals.MINUS) ||
			       (op2.getRestrictionMode() == EssenceGlobals.PLUS)){
				Expression expr = evalBinaryExpression
				    (new BinaryExpression(
							    new Expression(ae_left),
							    op2, 
							    binExp_r.getRightExpression()));
				return new Expression(new BinaryExpression
						      (expr, 
						       new BinaryOperator(EssenceGlobals.PLUS),
						       binExp_r.getLeftExpression()));
			    }
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE)||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) { 			    
				// op is non-linear
				// sort this out later (tmp variables etc)
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    

			    
			    // NUM1 - (E op2 NUM2) 
			case EssenceGlobals.MINUS:			    

			    if(op2.getRestrictionMode() == EssenceGlobals.PLUS) {
				Expression expr = new Expression(new AtomicExpression
								 (ae_left.getNumber() - 
								  binExp_r.getRightExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression
						      (expr, 
						       new BinaryOperator(EssenceGlobals.MINUS), 
						       binExp_r.getLeftExpression()));
			    }
			    // NUM1 - (E - NUM2) ---> eval(NUM1 + NUM2) - E 
			    else if (op2.getRestrictionMode() == EssenceGlobals.MINUS) {
				Expression expr = new Expression(new AtomicExpression
								 (ae_left.getNumber() + 
								  binExp_r.getRightExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression
						      (expr, 
						       new BinaryOperator(EssenceGlobals.MINUS), 
						       binExp_r.getLeftExpression()));
			    }
			    
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE) ||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) {		
				// op is non-linear
				// sort this out later (tmp variables etc)			    
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    
			default:
			    // here has to be done further work
			    return new Expression(e);
			    
			    
			} // end switch(op1)
			
		    } // end if(ATOM == NUM)
		    
		} //end if( NUM op1 (E op2 ATOM))
		
	    }
	    
	    // BOOL op1 (E1 op2 E2)
	    else if(ae_left.getRestrictionMode() == EssenceGlobals.BOOLEAN) {
		
		BinaryOperator op1 = e.getOperator();
		BinaryOperator op2 = binExp_r.getOperator();
		
		// BOOL1 op1 (BOOL2 op2 E) 		    
		if(binExp_r.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_r.getLeftExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
						
			switch(op1.getRestrictionMode()) {			    

			    
			    // BOOL1 or (BOOL2 op2 E)
			case EssenceGlobals.OR:
			    
			    // BOOL1 or (BOOL2 or E) ==> eval(BOOL1 or BOOL2) or E 
			    if(op2.getRestrictionMode() == EssenceGlobals.OR)
				return new Expression(new BinaryExpression
						      (new Expression (new AtomicExpression
								       (ae_left.getBool() ||
									binExp_r.getLeftExpression().getAtomicExpression().getBool() )),
						       op1,
						       binExp_r.getRightExpression()));
			    
			    else if((op2.getRestrictionMode() == EssenceGlobals.AND) ||
				    (op2.getRestrictionMode() == EssenceGlobals.IF)||
				    (op2.getRestrictionMode() == EssenceGlobals.EQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.NEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.GEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LESS)||
				    (op2.getRestrictionMode() == EssenceGlobals.GREATER)||
				    (op2.getRestrictionMode() == EssenceGlobals.IFF)) { 
				// sort this out later (tmp variables etc) !!!
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    


			    // (BOOL1 and E) is covered in evalBinExpressions(BE e)
			case EssenceGlobals.AND:  
			    return new Expression(e);
			

			default:
			    if((op1.getRestrictionMode() == EssenceGlobals.AND) ||
			       (op1.getRestrictionMode() == EssenceGlobals.IF)||
			       (op1.getRestrictionMode() == EssenceGlobals.EQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.NEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.GEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LESS)||
			       (op1.getRestrictionMode() == EssenceGlobals.GREATER)||
			       (op1.getRestrictionMode() == EssenceGlobals.IFF)) {
				// care about this later with temp vars
				return new Expression(e);
			    }
			    else  throw new PreprocessorException
				      ("Unfeasible operator '"+op1.toString()+"' in binary expression: "+e.toString());
			    
			    
			}
		    }
		}
		// BOOL1 op1 (E op2 BOOL2) 		    
		if(binExp_r.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_r.getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
			
			switch(op1.getRestrictionMode()) {			    
			    
			    
			    // BOOL1 or (BOOL2 op2 E)
			case EssenceGlobals.OR:
			    
			    // BOOL1 or (E or BOOL2) ==> eval(BOOL1 or BOOL2) or E 
			    if(op2.getRestrictionMode() == EssenceGlobals.OR)
				return new Expression(new BinaryExpression
						      (new Expression(new AtomicExpression
								      (ae_left.getBool() ||
								       binExp_r.getRightExpression().getAtomicExpression().getBool() )),
						       op1,
						       binExp_r.getLeftExpression()));

			    else if((op2.getRestrictionMode() == EssenceGlobals.AND) ||
				    (op2.getRestrictionMode() == EssenceGlobals.IF)||
				    (op2.getRestrictionMode() == EssenceGlobals.EQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.NEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.GEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LESS)||
				    (op2.getRestrictionMode() == EssenceGlobals.GREATER)||
				    (op2.getRestrictionMode() == EssenceGlobals.IFF)) { 
				// sort this out later (tmp variables etc) !!!
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    


			    // (BOOL1 and E) is covered in evalBinExpressions(BE e)
			case EssenceGlobals.AND:  
			    return new Expression(e);
			

			default:
			    if((op1.getRestrictionMode() == EssenceGlobals.IF)||
			       (op1.getRestrictionMode() == EssenceGlobals.EQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.NEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.GEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LESS)||
			       (op1.getRestrictionMode() == EssenceGlobals.GREATER)||
			       (op1.getRestrictionMode() == EssenceGlobals.IFF)) {
				// care about this later with temp vars
				return new Expression(e);
			    }
			    else  throw new PreprocessorException
				      ("Unfeasible operator '"+op1.toString()+"' in binary expression: "+e.toString());
			    
			    
			}
		    }
		}
	    }
	} 	// end if (ATOM op1 (E1 op2 E2))
	
	
	// ---------------------------------------------------------------------------
	// part 2: (E1 op2 E2) op1 ATOM
	// (symmetric to part 1)
	// ---------------------------------------------------------------------------
	
	
	// (E1 op2 E2) op1 ATOM 
	if((e.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) &&
	   (e.getLeftExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR  )){
	    AtomicExpression ae_right = e.getRightExpression().getAtomicExpression();
	    BinaryExpression binExp_l = (evalBinExpressionAdvanced(e.getLeftExpression().getBinaryExpression())).getBinaryExpression();
	
	    // (E1 op2 E2) op1 NUM 
	    if(ae_right.getRestrictionMode() == EssenceGlobals.NUMBER){
		BinaryOperator op1 = e.getOperator();
		BinaryOperator op2 = binExp_l.getOperator();
		
		// (NUM2 op2 E) op1 NUM1 
		if(binExp_l.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_l.getLeftExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			
			switch(op1.getRestrictionMode()) {			    
			    
			    
			    // (NUM2 op E) + NUM1 --->  eval(NUM1 + NUM2) op E             iff op \in {+,-}
			case EssenceGlobals.PLUS: // op must not be nonlinear operator
			    if((op2.getRestrictionMode() == EssenceGlobals.MINUS) ||
			       (op2.getRestrictionMode() == EssenceGlobals.PLUS)){
				Expression expr = new Expression(new AtomicExpression
								 (ae_right.getNumber() +
								  binExp_l.getLeftExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression(expr,
									     op2,
									     binExp_l.getRightExpression() ));
			    }				
			    // op is non-linear
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE)||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) { 			    
				// sort this out later (tmp variables etc) !!!
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    
			    
			    // (NUM2 op E)- NUM1 ---> eval(NUM1 - NUM2) op E      iff op \in {+,-}
			case EssenceGlobals.MINUS:
			    if (op2.getRestrictionMode() == EssenceGlobals.PLUS ||
				op2.getRestrictionMode() == EssenceGlobals.MINUS) {
				Expression expr = new Expression(new AtomicExpression
								 (ae_right.getNumber() - 
								  binExp_l.getLeftExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression
						      (expr, 
						       op2,
						       binExp_l.getRightExpression() ));
			    }
			    
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE) ||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) {		
				// sort this out later (tmp variables etc)			    
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			}
		    }
		}		
		// (E op2 NUM2) op1 NUM1 
		if(binExp_l.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_l.getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			
			
			switch(op1.getRestrictionMode()) {
			    
			    
			    // (E op2 NUM2) + NUM1 --->  eval(NUM1 op2 NUM2) + E       iff op2 \in {+,-}
			case EssenceGlobals.PLUS: // op must not be nonlinear operator
			    
			// (E - NUM2) + NUM1 --->  eval(NUM1 - NUM2) + E
			    if(op2.getRestrictionMode() == EssenceGlobals.MINUS) {
				Expression expr = new Expression(new AtomicExpression
								 (ae_right.getNumber() -
								  binExp_l.getRightExpression().getAtomicExpression().getNumber() ));
				return new Expression(new BinaryExpression
						      (expr, 
						   new BinaryOperator(EssenceGlobals.PLUS),
						       binExp_l.getLeftExpression() ));
				
			    }			
			    // (E + NUM2) + NUM1 --->  eval(NUM1 + NUM2) + E
			    else if (op2.getRestrictionMode() == EssenceGlobals.PLUS) {
				Expression expr =  new Expression(new AtomicExpression
								  (ae_right.getNumber() +
								   binExp_l.getRightExpression().getAtomicExpression().getNumber() ));
				
				return new Expression(new BinaryExpression
						      (expr, 
						       new BinaryOperator(EssenceGlobals.PLUS),
						   binExp_l.getLeftExpression() ));			
			    }
			    
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE)||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) { 			    
			    // op is non-linear
			    // sort this out later (tmp variables etc)
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			
			    
			    
			    //(E op2 NUM2)- NUM1 ---> E op2 eval(NUM2 - NUM1)      iff op \in {+,-}
			case EssenceGlobals.MINUS:
			    if(op2.getRestrictionMode() == EssenceGlobals.PLUS ||
			       op2.getRestrictionMode() == EssenceGlobals.MINUS) {
				Expression expr = new Expression(new AtomicExpression
								 (binExp_l.getRightExpression().getAtomicExpression().getNumber() -
								  ae_right.getNumber() ));					  
				return new Expression(new BinaryExpression
						      (binExp_l.getLeftExpression(), 
						       op2, 
						       expr ));
			    }
			    
			    else if((op2.getRestrictionMode() == EssenceGlobals.MULT) ||
				    (op2.getRestrictionMode() == EssenceGlobals.DIVIDE) ||
				    (op2.getRestrictionMode() == EssenceGlobals.POWER)) {		
				// sort this out later (tmp variables etc)			    
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    
			default:
			    // here has to be done further work
			return new Expression(e);
			
			
			} // end switch(OP)
			
		    } // end if E22 == number)
		    
		} // E1 op (E21 op E22): if E22 is atom?
		
		
		
	    }
	    
	    // (E1 op2 E2) op1 BOOL
	    else if(ae_right.getRestrictionMode() == EssenceGlobals.BOOLEAN) {
		
		BinaryOperator op1 = e.getOperator();
		BinaryOperator op2 = binExp_l.getOperator();
		
		// (BOOL2 op2 E) op1 BOOL1 
		if(binExp_l.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_l.getLeftExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
			
			switch(op1.getRestrictionMode()) {			    
			    
			    
			    // (BOOL2 op2 E) or BOOL1 
			case EssenceGlobals.OR:
			    
			    // (BOOL2 or E) or BOOL1 ---> eval(BOOL1 or BOOL2) or E 
			    if(op2.getRestrictionMode() == EssenceGlobals.OR)
				return new Expression(new BinaryExpression
						      (new Expression(new AtomicExpression
								      (ae_right.getBool() || 
								      binExp_l.getLeftExpression().getAtomicExpression().getBool() )),
						       op1,
						       binExp_l.getRightExpression()));

			    else if((op2.getRestrictionMode() == EssenceGlobals.AND) ||
				    (op2.getRestrictionMode() == EssenceGlobals.IF)||
				    (op2.getRestrictionMode() == EssenceGlobals.EQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.NEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.GEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LESS)||
				    (op2.getRestrictionMode() == EssenceGlobals.GREATER)||
				    (op2.getRestrictionMode() == EssenceGlobals.IFF)) { 
				// sort this out later (tmp variables etc) !!!
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    


			    // (BOOL1 and E) is covered in evalBinExpressions(BE e)
			case EssenceGlobals.AND:  
			    return new Expression(e);
			

			default:
			    if((op1.getRestrictionMode() == EssenceGlobals.AND) ||
			       (op1.getRestrictionMode() == EssenceGlobals.IF)||
			       (op1.getRestrictionMode() == EssenceGlobals.EQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.NEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.GEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LESS)||
			       (op1.getRestrictionMode() == EssenceGlobals.GREATER)||
			       (op1.getRestrictionMode() == EssenceGlobals.IFF)) {
				// care about this later with temp vars
				return new Expression(e);
			    }
			    else  throw new PreprocessorException
				      ("Unfeasible operator '"+op1.toString()+"' in binary expression: "+e.toString());
				
			    
			}
		    }
		}
		// (E op2 BOOL2) op1 BOOL1  
		if(binExp_l.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
		    if(binExp_l.getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
						
			switch(op1.getRestrictionMode()) {			    

			    
			    // (BOOL2 op2 E) or BOOL1
			case EssenceGlobals.OR:
			    
			    // (E or BOOL2) or BOOL1 ---> eval(BOOL1 or BOOL2) or E 
			    if(op2.getRestrictionMode() == EssenceGlobals.OR)
				return new Expression(new BinaryExpression
						      (new Expression(new AtomicExpression
								      (ae_right.getBool() || 
								       binExp_l.getRightExpression().getAtomicExpression().getBool() )),
						       op1,
						       binExp_l.getLeftExpression() ));

			    else if((op2.getRestrictionMode() == EssenceGlobals.AND) ||
				    (op2.getRestrictionMode() == EssenceGlobals.IF)||
				    (op2.getRestrictionMode() == EssenceGlobals.EQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.NEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.GEQ)||
				    (op2.getRestrictionMode() == EssenceGlobals.LESS)||
				    (op2.getRestrictionMode() == EssenceGlobals.GREATER)||
				    (op2.getRestrictionMode() == EssenceGlobals.IFF)) { 
				// sort this out later (tmp variables etc) !!!
				return new Expression(e);
			    }
			    else throw new PreprocessorException
				     ("Unfeasible operator in binary expression: "+e.toString());
			    


			    // (BOOL1 and E) is covered in evalBinExpressions(BE e)
			case EssenceGlobals.AND:  
			    return new Expression(e);
			

			default:
			    if((op1.getRestrictionMode() == EssenceGlobals.IF)||
			       (op1.getRestrictionMode() == EssenceGlobals.EQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.NEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.GEQ)||
			       (op1.getRestrictionMode() == EssenceGlobals.LESS)||
			       (op1.getRestrictionMode() == EssenceGlobals.GREATER)||
			       (op1.getRestrictionMode() == EssenceGlobals.IFF)) {
				// care about this later with temp vars
				return new Expression(e);
			    }
			    else  throw new PreprocessorException
				      ("Unfeasible operator '"+op1.toString()+"' in binary expression: "+e.toString());
			    
			    
			}
		    }
		}
	    }
	    
	} 	// end if ((E1 op2 E2) op1 ATOM)
	
	print_debug("We survived the adavanced evaluation:"+e.toString());
	
	return new Expression(e);
    }
    
    
	
    /**
     * Evaluates expressions in functional Expressions, such as allDifferent(Expression), etc.
     * 
     * @param e the FunctionExpression to be evaluated
     * @return the evaluated functional Expression e
     * @throws PreprocessorException
     */
	public Expression evalFunctionOpExpression(FunctionExpression e)
	    throws PreprocessorException {
		
		Expression e1 = evalExpression(e.getExpression1());

		if((e.getRestrictionMode()== EssenceGlobals.ALLDIFF) ||
		   (e.getRestrictionMode()== EssenceGlobals.MIN) ||
		   (e.getRestrictionMode()== EssenceGlobals.MAX) ||
		   (e.getRestrictionMode()== EssenceGlobals.DOM) ||
		   (e.getRestrictionMode()== EssenceGlobals.RAN) ||		    
		   (e.getRestrictionMode()== EssenceGlobals.INV))
		    return new Expression(new FunctionExpression(e.getRestrictionMode(),e1));
		    
		else if(e.getRestrictionMode()== EssenceGlobals.IMAGE) {
		    Expression e2 = evalExpression(e.getExpression2());
		    return new Expression(new FunctionExpression(e1,e2));
		}
		
		else { 
		    Expression e2 = evalExpression(e.getExpression2());
		    Expression e3 = evalExpression(e.getExpression3());
		    return new Expression(new FunctionExpression(e.getRestrictionMode(),e1,e2,e3));		    
		}

	}


   /**
    * Evaluates all expressions that occur in quantifications.
    * 
    * @param e the QuantificationExpression to be evaluated 
    * @return the evaluated quantified Expression e
    * @throws PreprocessorException
    */
    private Expression evalQuantifierExpression(QuantificationExpression e)
		throws PreprocessorException {
	
	Domain domain = evalExpressionInDomain(e.getBindingExpression().getDomainIdentifiers().getDomain());
	Expression expression = evalExpression(e.getExpression());
	
	return new Expression(new QuantificationExpression(e.getQuantifier(),
							 		new BindingExpression(
							 				new DomainIdentifiers(e.getBindingExpression().getDomainIdentifiers().getIdentifiers(), domain)),
							 				expression
							 ));
	
    }

    /**
     * Evaluates all expression in Domain d to integers.
     * 
     * @param d the Domain whose expressions will be evaluated
     * @return Domain d whose expressions are evaluated to integers
     * @throws PreprocessorException
     */
 public Domain evalExpressionInDomain(Domain d) 
	throws PreprocessorException {

	print_debug("domain '"+d.toString()+"' will be evaluated.");	
	 
	switch(d.getRestrictionMode()) {

	case EssenceGlobals.BRACKETED_DOMAIN:
	    return evalExpressionInDomain(d.getDomain());

	case EssenceGlobals.BOOLEAN_DOMAIN:
	    return d;
	    
	case EssenceGlobals.INTEGER_RANGE:

	  	print_debug("domain '"+d.toString()+"' is an integer domain.");

	    if(d.getIntegerDomain().getRestrictionMode() == EssenceGlobals.INT_DOMAIN_RANGE) {		
	    	RangeAtom[] rangeList  = d.getIntegerDomain().getRangeList();

		for(int i=0; i<rangeList.length; i++) {

		    if(rangeList[i].getRestrictionMode() == EssenceGlobals.RANGE_EXPR_DOTS_EXPR) {
		    	Expression e1 = evalExpression(rangeList[i].getLowerBound());
		    	Expression e2 = evalExpression(rangeList[i].getUpperBound());
		    	rangeList[i] = new RangeAtom(e1,e2);
		    }
		    else if(rangeList[i].getRestrictionMode() == EssenceGlobals.RANGE_EXPR) {
		    	Expression e = evalExpression(rangeList[i].getLowerBound());
		    	rangeList[i] = new RangeAtom(EssenceGlobals.RANGE_EXPR, e);
		    }
		    else 
			throw new PreprocessorException
			    ("Illegal range type in domain "+d.toString());
		}

		// I assume that there are only e1..e2 kinds of rangeatoms (rest is forbidden)
		return new Domain(new IntegerDomain(rangeList));
	    }
	    else  // int domain without range: INT
		return d;
	    

	case EssenceGlobals.IDENTIFIER_RANGE: // IdentifierDomain :  identifier  [ { rangeAtom' } ]
		
	  	print_debug("domain '"+d.toString()+"' is an identifier domain.");
    	Constant constant = parameters.get(d.getIdentifierDomain().getIdentifier());
    	
    	if(constant == null) 
    		throw new PreprocessorException("Unknown identifier used as domain identifier: "+d.getIdentifierDomain().getIdentifier());
    	
    	else if(constant.getRestrictionMode() == EssenceGlobals.IDENTIFIER_DOMAIN)
    		throw new PreprocessorException("Constant identifier used as domain identifier: "+d.getIdentifierDomain().getIdentifier());	
    	
    	Domain newDomain = evalExpressionInDomain(constant.getDomainConstant().getDomain());
		
    	
    	// the identifier-domain is restricted to a range
	    if(d.getIdentifierDomain().getRestrictionMode() == EssenceGlobals.IDENTIFIER_DOMAIN_RANGE) {
	    		RangeAtom[] rangeList  = d.getIdentifierDomain().getRangeList();
	    		for(int i=0; i<rangeList.length; i++) {
	    			Expression e1 = evalExpression(rangeList[i].getLowerBound());
	    			Expression e2 = evalExpression(rangeList[i].getUpperBound());
	    			rangeList[i] = new RangeAtom(e1,e2);
	    		}
	    		// We assume that there are only the (e1..e2) kind of rangeatoms (rest is forbidden)
	    		// NOTE: If the restrictions are stupid or not in the bounds of the original domain, there is 
	    		// no error or warning message given!!
	    		return new Domain(new IdentifierDomain(d.getIdentifierDomain().getIdentifier(),rangeList));
	    }
	    
	    return newDomain;
	       	    			    	
	
	case EssenceGlobals.MATRIX_DOMAIN:
	  	print_debug("domain '"+d.toString()+"' is a matrix domain.");
	    
	  	Domain[] indexDomain = d.getMatrixDomain().getIndexDomains();
	  	for(int i=0; i< indexDomain.length; i++)
	  		indexDomain[i] = evalExpressionInDomain(indexDomain[i]);
	  			
	    return new Domain(new MatrixDomain(indexDomain,
	    				   		           evalExpressionInDomain(d.getMatrixDomain().getRangeDomain())));
	    									
	}// end switch

	return d;
 }	    

    
 /**
  * 
  * Determine if the expression (or a relational subexpression) is false
  * when it is completely evaluated. <br>
  * E.g.  3 < 2  /\  x[2] = 4 <br>
  * is evaluated to false, because 3 < 2 is false. But x[2] = 4 would be
  * evaluated to true, since it can not be proven wrong. But since it is 
  * disjuncted with 3 < 2, the whole expression is evaluated to false.
  * @param e the Expression that is checked for being true or false if completly evaluated
  * @return true if the Expression e cannot be proven to be false if completly evaluated
  * @throws PreprocessorException
  */

    public boolean isFeasibleExpression(Expression e) 
	throws PreprocessorException {

	if(e.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {

	    Expression e_left = evalExpression(e.getBinaryExpression().getLeftExpression());
	    Expression e_right = evalExpression(e.getBinaryExpression().getRightExpression());

	    if(e_left.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) 
		e_left = e_left.getExpression();
	    if(e_right.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) 
		e_right = e_right.getExpression();

	    if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) && 
	       (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR)) {
	
		AtomicExpression expr_left = e_left.getAtomicExpression();
		AtomicExpression expr_right = e_right.getAtomicExpression();

		switch(e.getBinaryExpression().getOperator().getRestrictionMode()) {
		    
		case EssenceGlobals.EQ :		
		    return (expr_left.getNumber() == expr_right.getNumber());	
		    
		case EssenceGlobals.NEQ :
		    return (expr_left.getNumber() != expr_right.getNumber());
		    
		case EssenceGlobals.GEQ :
		    return (expr_left.getNumber() >= expr_right.getNumber());
		    
		case EssenceGlobals.LEQ :
		    return (expr_left.getNumber() <= expr_right.getNumber());
		    
		case EssenceGlobals.GREATER :
		    return (expr_left.getNumber() > expr_right.getNumber());
		    
		case EssenceGlobals.LESS :
		    return (expr_left.getNumber() < expr_right.getNumber());
		    
		default:
		    return (isFeasibleExpression(new Expression(expr_left)) && isFeasibleExpression(new Expression(expr_right)));
		    
		}
	    }
	    return (isFeasibleExpression(e_left) && isFeasibleExpression(e_right));
	}

	else if(e.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
	    // in case we have boolean atoms
	    if(e.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)  
		return e.getAtomicExpression().getBool();
	    
	    return true;
	}
	
	else return true;
    }


  /**
   * Removes the atomic subexpression "true" in conjunctions. Consider
   * for instance that the expression "a \/ true" can be evaluated to "a".
   *  
   * @param e the Expression whose atomic subexpressions are removed
   * @return Expression e without subexpressions "true"
   * @throws PreprocessorException
   */
    public Expression removeAtomicSubExpressions (Expression e) 
	throws PreprocessorException {

	e = evalExpression(e);

	switch(e.getRestrictionMode()) {	    

	case EssenceGlobals.BINARYOP_EXPR:
	    Expression left = e.getBinaryExpression().getLeftExpression();
	    Expression right = e.getBinaryExpression().getRightExpression();
	    
	    if(e.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.AND) {
	    	if(left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
	    		if(left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
	    			if(left.getAtomicExpression().getBool()) 
	    				return right;
	    		}
	    	} 
	    	else if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
	    		if(right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
	    			if(right.getAtomicExpression().getBool()) 
	    				return left;			
	    		}
	    	}
	    }
	    return new Expression(new BinaryExpression(removeAtomicSubExpressions(left),
							 e.getBinaryExpression().getOperator(), 
							      removeAtomicSubExpressions(right)));
	  
	default: 
	    return e;
	}
    }

    
 
 /** 
  *  Check if the identifier appears in Expression e.
  *  
  *  @param id the identifier String to be looked for
  *  @param e  the Expression to be searched in
  *  @return true if the identifier appears somewhere in the expression e.
  *   
 */

    public boolean appearsInExpression(String id, Expression e)
	throws PreprocessorException {
	
    	
	switch(e.getRestrictionMode()) {

	case EssenceGlobals.ATOMIC_EXPR:
	    if(e.getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) 
	    	return id.equals(e.getAtomicExpression().getString());
	   	
	    else return false;	    
	
	case EssenceGlobals.BINARYOP_EXPR :
	    return (appearsInExpression(id, e.getBinaryExpression().getLeftExpression()) || appearsInExpression(id, e.getBinaryExpression().getRightExpression()));
	
	case EssenceGlobals.UNITOP_EXPR:
	    return appearsInExpression(id, e.getUnaryExpression().getExpression());
	
	case EssenceGlobals.NONATOMIC_EXPR :
	    boolean indices = false;
	    Expression[] index_exprs = e.getNonAtomicExpression().getExpressionList();
	    for(int i=0; i< index_exprs.length; i++)
		indices = appearsInExpression(id, index_exprs[i]) || indices;
	    return indices;
	    
	case EssenceGlobals.BRACKET_EXPR:
	    return appearsInExpression(id, e.getExpression());
	
	case EssenceGlobals.FUNCTIONOP_EXPR:
	    FunctionExpression expr = e.getFunctionExpression();
	    boolean b = appearsInExpression(id, expr.getExpression1());
	    if(expr.getExpression2() != null)
		b = appearsInExpression(id,expr.getExpression2()) || b;
	    if(expr.getExpression3() != null)
		b = appearsInExpression(id, expr.getExpression3()) || b;
	    return b;

	case EssenceGlobals.LEX_EXPR:
		return (appearsInExpression(id, e.getLexExpression().getLeftExpression()) || appearsInExpression(id, e.getLexExpression().getRightExpression()) );
	    
	default: return false;
	    
	}
	
    }
  /**
   * Prints String s on the standard-output in case the DEBUG flag is set true.
   * @param s the String to print when the DEBUG flag is set true.
   *  
   *  */

    private void print_debug(String s) {
	if(DEBUG)
	    System.out.println("[ DEBUG expressionEvaluator ] "+s);
    }

    /**
     * Prints String s on the standard-output in case the PRINT_MESSAGE flag is set true.
     * @param s the String to print when the PRINT_MESSAGE flag is set true.
     *  
     *  */

    private void print_message(String s) {
	if(PRINT_MESSAGE)
	    System.out.println(s);
    }    


}
