package translator.preprocessor;

import java.util.ArrayList;
//import java.util.HashMap;

//import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.UnaryExpression;
import translator.conjureEssenceSpecification.BinaryExpression;
import translator.conjureEssenceSpecification.BinaryOperator;
import translator.conjureEssenceSpecification.AtomicExpression;
import translator.conjureEssenceSpecification.NonAtomicExpression;
import translator.conjureEssenceSpecification.FunctionExpression;
import translator.conjureEssenceSpecification.QuantificationExpression;
//import translator.conjureEssenceSpecification.Objective;
import translator.conjureEssenceSpecification.EssenceGlobals;

public class ConstraintOrdering implements PreprocessorGlobals {

	
	
	private int LEFT_IS_SMALLER = 1;
	private int RIGHT_IS_SMALLER = -1;
	private int EXPR_ARE_EQUAL = 0;
	
	
	private int LEFT_OP_STRONGER = 1;
	private int RIGHT_OP_STRONGER = -1;
	private int INCOMPARABLE_OP = -2;
	private int EQUAL_OP = 0;
	
	/** buffers certain expressions during the ordering of binary expressions */
	protected ArrayList<Expression> expressionBuffer;
	
	private ArrayList<Expression> minusExpressions;
	
	
	//private ArrayList<Expression> constraints;
	//private Objective objective;
	//private Parameters parameterArrays;
	
	//private HashMap<String,Domain>  decisionVariables ;  
	//private ArrayList<String> decisionVariablesNames;
	
	
	public ConstraintOrdering() {
		this.expressionBuffer = new ArrayList<Expression>();
	}
	
	
	/**
	 * Order the Essence' Expression 'constraint'
	 * 
	 * @param constraint
	 * @return
	 */
	public Expression orderExpression(Expression constraint, boolean isNested) 
		throws PreprocessorException {
		
		print_debug("====== I WILL ORDER THIS EXPRESSION:"+constraint);
		
		switch(constraint.getRestrictionMode()) {
		
		case EssenceGlobals.ATOMIC_EXPR:
			return constraint;
		
		case EssenceGlobals.NONATOMIC_EXPR:
			return constraint;
		
		case EssenceGlobals.UNITOP_EXPR:
			return new Expression( new UnaryExpression(constraint.getUnaryExpression().getRestrictionMode(),
					              orderExpression(constraint.getUnaryExpression().getExpression(),true))
					               );		
		case EssenceGlobals.BINARYOP_EXPR:
			return orderBinaryExpression(constraint.getBinaryExpression(), isNested);
		
		case EssenceGlobals.BRACKET_EXPR:
			return new Expression(orderExpression(constraint.getExpression(),isNested));
			
		case EssenceGlobals.FUNCTIONOP_EXPR:
			return constraint;
			
		case EssenceGlobals.LEX_EXPR:
			return constraint;
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			// order domain of binding variables?
			
			return new Expression(new QuantificationExpression(constraint.getQuantification().getQuantifier(),
					                                           constraint.getQuantification().getBindingExpression(),
					                                           orderExpression(constraint.getQuantification().getExpression(),
					                                        		   (constraint.getQuantification().getQuantifier().getRestrictionMode() ==
					                                        			   EssenceGlobals.EXISTS) ?
					                                        					   true : (false || isNested)  )
					                                           ));
		default:
			throw new PreprocessorException("Unknow expression type or unsupported ordering for expression:"+constraint);
		
		}
	}
	
	
	/**
	 * Orders a binary expression: the left expression is always supposed to be 
	 * "smaller" than the right one
	 * 
	 * This is the top expression and not a subexpression (meaning, don't call this
	 * method recursively on subexpressions)
	 * 
	 * @param expression
	 * @return
	 */
	protected Expression orderBinaryExpression(BinaryExpression expression, boolean isNested) 
		throws PreprocessorException {
		
		int operator = expression.getOperator().getRestrictionMode();
		print_debug("ORDERING binary expression '"+expression+"' with operator: "+expression.getOperator());
		
		

		// this does not work! building the expressions should be done at the ending!
		// (when we reached the top-layer)
		// we cant do it globally because we need to order nested expressions (that might
		// contain + or - as well) and get mixed up. Needs to be reimplemented ARRRRGGGGH
		if(operator == EssenceGlobals.MINUS) {
			ArrayList<Expression> plusList = addRightMinusPart(expression.getRightExpression(), new ArrayList<Expression>());
			print_debug("Did the right part thing. Have +list:"+plusList+", and -list:"+this.expressionBuffer);
			
			// copy the expressionBuffer in case it needs to be reused
			minusExpressions = new ArrayList<Expression>();
			while(expressionBuffer.size() > 0) {
				minusExpressions.add(this.expressionBuffer.remove(0));
			}
			
			plusList = addLeftMinusPart(orderExpression(expression.getLeftExpression(), true), plusList);
			// updating the copy of the expressionBuffer in case it needs to be reused
			while(expressionBuffer.size() > 0) {
				minusExpressions.add(this.expressionBuffer.remove(0));
			}
			
			print_debug("Before building the expression. Have +list:"+plusList+", and -list:"+minusExpressions);
			
			Expression orderedPlusExpression = buildExpressionFromList(plusList, EssenceGlobals.PLUS);
			Expression orderedMinusExpression = buildExpressionFromList(orderExpressionList(minusExpressions),
					                                                    EssenceGlobals.MINUS);
			
			//this.expressionBuffer.clear();
			print_debug("Just ORDERED the MINUS expression: "+new Expression(new BinaryExpression(orderedPlusExpression,
					 							       new BinaryOperator(EssenceGlobals.MINUS),
					                                   orderedMinusExpression)));
			print_debug("After ordering the MINUS expression. Have +list:"+plusList+", and -list:"+this.expressionBuffer);
			return new Expression(new BinaryExpression(orderedPlusExpression,
					 							       new BinaryOperator(EssenceGlobals.MINUS),
					                                   orderedMinusExpression));
		}
		else if(!isCommutative(operator)) {
			return new Expression(new BinaryExpression(
					                 orderExpression(expression.getLeftExpression(), true),
					                  expression.getOperator(),
					                  orderExpression(expression.getRightExpression(), true)));
		}
		
		else if(isNonAssociative(operator)) {
			ArrayList<Expression> orderedExpressions = new ArrayList<Expression>();
			orderedExpressions.add(orderExpression(expression.getLeftExpression(), true));
			orderedExpressions.add(orderExpression(expression.getRightExpression(), true));
			
			orderedExpressions = orderExpressionList(orderedExpressions);
			return new Expression(new BinaryExpression(
									 orderedExpressions.get(0),
									 expression.getOperator(),
									 orderedExpressions.get(1)));
		}
		else { // we have a commutative, non-assoc operator and have to find all subexpression that
			   // are connected with the same operator
			   // operators: PLUS, MULT, OR, AND
			
			
			// collect all subexpression that have the same operator
			// let's do this for every operator:
			// first start ordering * and /\ expressions. They only require an ordering of 
			// their subexpressions and no ordering between them
			
			// MULT, AND: these are the stronger ones
			if(expressionContainsBinaryOperator(expression,EssenceGlobals.MULT)) {
				expression = orderCommutativeExpression(expression, EssenceGlobals.MULT);
			}
			
			if(expressionContainsBinaryOperator(expression,EssenceGlobals.AND))
				expression = orderCommutativeExpression(expression, EssenceGlobals.AND);
			
			
			// PLUS, OR : these are the weaker operators
			if(expressionContainsBinaryOperator(expression, EssenceGlobals.PLUS)) 
				expression = orderCommutativeExpression(expression,EssenceGlobals.PLUS);
			
			if(expressionContainsBinaryOperator(expression, EssenceGlobals.OR)) 
				expression = orderCommutativeExpression(expression,EssenceGlobals.OR);
	
			
		}
		return new Expression(expression);
		
	}
	
	
	/**
	 * Returns true, if the BinaryExpression expression has at least one occurrence
	 * of the binary operator (given its restriction mode).
	 * 
	 * @param expression
	 * @param operator
	 * @return
	 * @throws PreprocessorException
	 */
	protected boolean expressionContainsBinaryOperator(BinaryExpression expression, int operator) 
		throws PreprocessorException {
		
		if(expression.getOperator().getRestrictionMode() == operator)
			return true;
		
		else {
			boolean leftContainsOperator = false;
			boolean rightContainsOperator = false;
			
			if(expression.getLeftExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR){
				leftContainsOperator = expressionContainsBinaryOperator(expression.getLeftExpression().getBinaryExpression(), operator);
			}
			else if(expression.getLeftExpression().getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) {
				if(expression.getLeftExpression().getExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR){
					leftContainsOperator = expressionContainsBinaryOperator(expression.getLeftExpression().getExpression().getBinaryExpression(), operator);
				}
			}
			if(expression.getRightExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR){
				rightContainsOperator = expressionContainsBinaryOperator(expression.getRightExpression().getBinaryExpression(), operator);
			}
			else if(expression.getRightExpression().getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) {
				if(expression.getRightExpression().getExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR){
					rightContainsOperator = expressionContainsBinaryOperator(expression.getRightExpression().getExpression().getBinaryExpression(), operator);
				}
			}
			
			return leftContainsOperator || rightContainsOperator;
			
		}
	}
	
	
	/**
	 * The operator "operator" has to occurr in the binary expression, at least nested!
	 * 
	 * 
	 * @param expression
	 * @param operator
	 * @return
	 * @throws PreprocessorException
	 */
	protected BinaryExpression orderCommutativeExpression(BinaryExpression expression, int operator) 
		throws PreprocessorException {
		
		print_debug("ordering a commutative expression:"+expression);
		
		if(operator == EssenceGlobals.AND || 
			operator == EssenceGlobals.MULT ||
			operator == EssenceGlobals.PLUS || 
			operator == EssenceGlobals.OR) {
			ArrayList<Expression> expressionList = new ArrayList<Expression>();
			
			
			// collect all the expressions that are linked with this operator
			expressionList = collectExpressionsToBeOrdered(expression, operator, expressionList);
			
			print_debug("collected expressions to be ordered:"+expressionList+" and -list:"+this.expressionBuffer);
			
			// order all the subexpressions
			//for(int i=0;i<expressionList.size(); i++)
			//	expressionList.add(i,orderExpression(expressionList.remove(i)));
			
			expressionList = orderExpressionList(expressionList);
		
			if(expressionList.size() == 0)
				throw new PreprocessorException("Internal error. Ordering process returned null when ordering expression:"+expression);
		
			print_debug("These are the expressions in the expr buffer (-):");
			for(int i=0; i<this.expressionBuffer.size(); i++)
				print_debug(i+": "+expressionBuffer.get(i));
			
			Expression orderedExpression = buildExpressionFromList(expressionList, operator);
			orderedExpression = addBufferedExpressions(orderedExpression);
			
			if(orderedExpression.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR)
				throw new PreprocessorException("Internal error. Ordering process returned fewer expressions when ordering:"+expression);
		
			return orderedExpression.getBinaryExpression();
		}
		/*else if(operator == EssenceGlobals.PLUS || 
				operator == EssenceGlobals.OR) {
			
			return null;
		}*/
		else 
			throw new PreprocessorException
			("Internal error. Trying to order an expression for the wrong operators. Expected commutative operator instead of: "+
					(new BinaryOperator(operator))+" in expression: "+expression);
	}
	
	
	
	
	
	/**
	 * Build a binary expression from a list of expressions [e1,e2,e3,...,en] and an operator OP:
	 * 
	 *   e1 OP e2 OP e3 OP ... OP en
	 * 
	 * @param expressionList
	 * @param operator
	 * @return
	 */
	protected Expression buildExpressionFromList(ArrayList<Expression> expressionList, int operator) {
		
		Expression expression = null;
		
		print_debug("Building expression from expressionList: "+expressionList);
		
		if(expressionList.size() > 1) { // at least 2 elements left
			return new Expression(new BinaryExpression(expressionList.remove(0),
				                               new BinaryOperator(operator),
				                               buildExpressionFromList(expressionList,operator)));
			
		}
		else if(expressionList.size() == 1) { // just 1 element left
			return expressionList.remove(0);
		}
		return expression;
	}
	
	
	
	/**
	 * Given an Expression expression, add elements from the -list (that is the list containing 
	 * expressions that are linked with Minuses and have been extracted from the expression while
	 * ordering +expressions.
	 * 
	 * @param expression
	 * @return
	 * @throws PreprocessorException
	 */
	protected Expression addBufferedExpressions(Expression expression) 
		throws PreprocessorException {
		
		if(this.expressionBuffer.size() == 0)
			return expression;
		
		// order the expression Buffer
		this.expressionBuffer = this.orderExpressionList(this.expressionBuffer);
		
		Expression newExpression = expression;
		
			while(this.expressionBuffer.size() > 0)
				newExpression = new Expression(new BinaryExpression(newExpression,
					                                            new BinaryOperator(EssenceGlobals.MINUS),
					                                            expressionBuffer.remove(0)));	
	
		return newExpression;
	}
	
	
	/*protected Expression addBufferedExpressions(Expression expression) 
	throws PreprocessorException {
	
	if(this.expressionBuffer.size() == 0)
		return expression;
	
	// order the expression Buffer
	this.expressionBuffer = this.orderExpressionList(this.expressionBuffer);
	
	// if the Expression  is an addition, it is smaller and belongs on the left side)
	boolean expressionIsSmaller = false;
	if(expression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
		if(expression.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.PLUS) 
			expressionIsSmaller = true;	
	}
	else if(expression.getRestrictionMode() < EssenceGlobals.BINARYOP_EXPR) 
		expressionIsSmaller = true;
	
	
	Expression newExpression = null;
	
   //EXPRESSION - element
	if(expressionIsSmaller) {
		newExpression = expression;
	
		while(this.expressionBuffer.size() > 0)
			newExpression = new Expression(new BinaryExpression(newExpression,
				                                            new BinaryOperator(EssenceGlobals.MINUS),
				                                            expressionBuffer.remove(0)));	
	}
	// - element + EXPRESSION
	else {
		Expression leftExpression = new Expression(new UnaryExpression(EssenceGlobals.NEGATION, 
						                                               expressionBuffer.remove(0)));
			
		while(this.expressionBuffer.size() > 0) {
			leftExpression = new Expression(new BinaryExpression(leftExpression,
					                        					new BinaryOperator(EssenceGlobals.MINUS),
					                        					expressionBuffer.remove(0)));
		}
		
		newExpression = new Expression(new BinaryExpression(leftExpression,
				                                            new BinaryOperator(EssenceGlobals.PLUS),
				                                            expression));
	}
	
	return newExpression;
}*/

	
	
	/**
	 * For MULT, AND
	 * 
	 * @param expression
	 * @param operator
	 * @param expressionList
	 * @return
	 * @throws PreprocessorException
	 */
	private ArrayList<Expression> collectExpressionsToBeOrdered(BinaryExpression expression, int operator, ArrayList<Expression> expressionList) 
		throws PreprocessorException {

		print_debug("START Collecting expressions:"+expressionList+"\n that need to ordered from expression: '"+expression
				+"' with operator:"+new BinaryOperator(operator));	
		
		Expression leftExpression = expression.getLeftExpression();
		Expression rightExpression = expression.getRightExpression();
		
		int currentOperator = expression.getOperator().getRestrictionMode();
		
		// OP is the same operator as we are ordering 
		if(currentOperator == operator) {
			
			if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) 
				expressionList = collectExpressionsToBeOrdered(orderExpression(leftExpression,true).getBinaryExpression(), operator, expressionList);
			else expressionList.add(orderExpression(leftExpression, true));
			
			
			if(rightExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR)
				expressionList = collectExpressionsToBeOrdered(orderExpression(rightExpression, true).getBinaryExpression(), operator, expressionList);
			else expressionList.add(orderExpression(rightExpression, true));			
		}
		
		
		
		// this is a different operator
		else {
			
			print_debug("We have a different operator: "+new BinaryOperator(operator));
			
			// only in this case we can collect further binary+ expressions inside the binary- expression.
			if(operator == EssenceGlobals.PLUS && currentOperator == EssenceGlobals.MINUS) {
				
				
				// start with the left part
				boolean leftContainsPlusOperator = false;
				boolean leftContainsMinusOperator = false;
				//boolean leftSubExpressionHasWeakerOperator = false;
				//boolean leftIsBracketed = false;
				
				// check if the left part of the binary expression contains anything we might want to collect
				if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
					leftContainsPlusOperator = expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),EssenceGlobals.PLUS);
					leftContainsMinusOperator = expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),EssenceGlobals.MINUS);
					//leftSubExpressionHasWeakerOperator = (strongerBinaryOperator(operator, 
					//		                                                    leftExpression.getBinaryExpression().getOperator().getRestrictionMode()
					//		                             )  == LEFT_OP_STRONGER);
				}
				else if(leftExpression.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) {
					if(leftExpression.getExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
						leftContainsPlusOperator = expressionContainsBinaryOperator(leftExpression.getExpression().getBinaryExpression(),
																					EssenceGlobals.PLUS);
						leftContainsMinusOperator = expressionContainsBinaryOperator(leftExpression.getExpression().getBinaryExpression(),
								                                                     EssenceGlobals.MINUS);
						//leftSubExpressionHasWeakerOperator = (strongerBinaryOperator(operator, 
						//		                                                    leftExpression.getExpression().getBinaryExpression().getOperator().getRestrictionMode()
						//		                             )  == LEFT_OP_STRONGER);
						//leftIsBracketed = true;
					}
				}
				
				
				
				
				// now check the right part
				boolean rightContainsPlusOperator = false;
				boolean rightContainsMinusOperator = false;
				//boolean rightSubExpressionHasWeakerOperator = false;
				//boolean rightIsBracketed = false;
				
				
				// check if the right part of the binary expression contains anything we might want to collect
				if(rightExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
					rightContainsPlusOperator = expressionContainsBinaryOperator(rightExpression.getBinaryExpression(),EssenceGlobals.PLUS);
					rightContainsMinusOperator = expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),EssenceGlobals.MINUS);
					//rightSubExpressionHasWeakerOperator = (strongerBinaryOperator(operator, 
					//		                                                    rightExpression.getBinaryExpression().getOperator().getRestrictionMode()
					//		                             )  == LEFT_OP_STRONGER);
				}
				else if(rightExpression.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) {
					if(rightExpression.getExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
						rightContainsPlusOperator = expressionContainsBinaryOperator(rightExpression.getExpression().getBinaryExpression(),
																			EssenceGlobals.PLUS);
						rightContainsMinusOperator = expressionContainsBinaryOperator(rightExpression.getExpression().getBinaryExpression(),
                                 											EssenceGlobals.MINUS);
						////rightSubExpressionHasWeakerOperator = (strongerBinaryOperator(operator, 
						//		                                                    rightExpression.getExpression().getBinaryExpression().getOperator().getRestrictionMode()
						//		                             )  == LEFT_OP_STRONGER);
						//rightIsBracketed = true;
					}
				}
				
				
				
				
				// NOW, DEPENDING ON THE STRUCTURE OF THE LEFT AND RIGHT SUBEXPRESSION
				
				// +:         A   -   B              ----> just return the whole expression  (A - B)
				if(!leftContainsPlusOperator && !rightContainsPlusOperator && 
						!leftContainsMinusOperator && !rightContainsMinusOperator) {
						expressionList.add(leftExpression);
						this.expressionBuffer.add(rightExpression);
				}
				
				
				else {	
					print_debug("The expression '"+expression+
								"' contains something we can collect. So we add parts from the left subexpression:"+leftExpression);
					// the left expression may contain a +operation and is not nested by a stronger operator
					expressionList = this.addLeftMinusPart(leftExpression, expressionList);
					print_debug("AFTER LEFT PART: elements we have in the expression buffer:"+this.expressionBuffer);
					print_debug("AFTER LEFT PART: Our expressionList (+List):"+expressionList);
					
					print_debug("The expression '"+expression+
							"' contains something we can collect. So we add parts from the right subexpression:"+rightExpression);
			
					expressionList = this.addRightMinusPart(rightExpression, expressionList); //rightIsBracketed);
					print_debug("AFTER RIGHT PART: elements we have in the expression buffer:"+this.expressionBuffer);
				}
				
				
				
				
			}
			else expressionList.add(new Expression(expression));
			
			
			
			
		} // else different op
			
	return expressionList;
	}
	
	/**
	 * 
	 * @param expression
	 * @param expressionList
	 * @param isBracketed
	 * @return
	 * @throws PreprocessorException
	 */
	protected ArrayList<Expression> addRightMinusPart(Expression expression, ArrayList<Expression> expressionList) 
		throws PreprocessorException {
		
		boolean containsPlusOperator = false;
		//boolean containsMinusOperator = false;
		boolean subExpressionHasWeakerOperator = false;
		boolean isBracketedBinaryExpression = false;
		
		if(expression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			containsPlusOperator = expressionContainsBinaryOperator(expression.getBinaryExpression(),EssenceGlobals.PLUS);
			//containsMinusOperator = expressionContainsBinaryOperator(expression.getBinaryExpression(),EssenceGlobals.MINUS);
			subExpressionHasWeakerOperator = (strongerBinaryOperator(EssenceGlobals.PLUS, 
					                                                    expression.getBinaryExpression().getOperator().getRestrictionMode()
					                             )  >= EQUAL_OP); // plus is stronger or equal to nested op
		}
		else if(expression.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) {
			if(expression.getExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				containsPlusOperator = expressionContainsBinaryOperator(expression.getExpression().getBinaryExpression(),
																			EssenceGlobals.PLUS);
				//containsMinusOperator = expressionContainsBinaryOperator(expression.getExpression().getBinaryExpression(),
				//		                                                     EssenceGlobals.MINUS);
				subExpressionHasWeakerOperator = (strongerBinaryOperator(EssenceGlobals.PLUS, 
						                                                    expression.getExpression().getBinaryExpression().getOperator().getRestrictionMode()
						                             )  >= EQUAL_OP); // PLUS is stronger or equal to nested op
				isBracketedBinaryExpression = true;
			}
		}
		else { // this is an atom or something similar, so just add it to the -list
			this.expressionBuffer.add(expression);
			return expressionList;
		}
		
		
		
		// if expression is binary and has +subexpressions that are not nested within a stronger operator
		if(containsPlusOperator &&
			subExpressionHasWeakerOperator) {
			
			// 'expression' is BRACKETED
			if(isBracketedBinaryExpression) {
                //	LEFT - (A + B)       =>     LEFT - A - B
				if(expression.getExpression().getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.PLUS) {
					// add A and B to the -list
					this.expressionBuffer.add(expression.getExpression().getBinaryExpression().getLeftExpression());
					this.expressionBuffer.add(expression.getExpression().getBinaryExpression().getRightExpression());
				}
				// LEFT -  (A - B)
				else {
					
					// add A to the -list
					this.expressionBuffer.add(expression.getExpression().getBinaryExpression().getLeftExpression());
					// add B to the +list
					expressionList.add(expression.getExpression().getBinaryExpression().getRightExpression());
				}
			}
			
			
			
			// expression has NO BRACKETS
			else {
	               //	LEFT -  A+B
				if(expression.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.PLUS) {
					// add A to -list
					this.expressionBuffer.add(expression.getBinaryExpression().getLeftExpression());
					
					// add B to +list
					// if B is binary and contains the + operator, recursively start again
					if(expression.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
						
						BinaryExpression binaryB = expression.getBinaryExpression().getRightExpression().getBinaryExpression();
						
						if(expressionContainsBinaryOperator(binaryB,
								                   EssenceGlobals.PLUS)  
						   || expressionContainsBinaryOperator(binaryB,
										                   EssenceGlobals.MINUS)) {
							return collectExpressionsToBeOrdered(binaryB, EssenceGlobals.PLUS,expressionList);	
						}
							
					}
					// otherwise, just add B to the +list
					expressionList.add(expression.getBinaryExpression().getRightExpression());
					
				}
				
				else {  // LEFT  - A-B
					this.expressionBuffer.add(expression.getBinaryExpression().getLeftExpression());
					this.expressionBuffer.add(expression.getBinaryExpression().getRightExpression());
				}
					
			}// end else: expression has no brackets
		}
		else {
			this.expressionBuffer.add(expression);
			
		}
		return expressionList;
	}
 	
	
	/**
	 * We have the left expression of a binary substraction:
	 * 
	 * LEFT - RIGHT
	 * 
	 * where we want to fetch subexpressions that we can add to 
	 * the addition list (the list that contains all subexpressions
	 * that can be added together). LEFT and RIGHT can both contain
	 * any type of (sub)expression. Here we are just dealing with 
	 * the left part. There are various different cases, depending on the
	 * structure of LEFT:
	 * 
	 * - if LEFT contains no other binary expressions, or only binary expressions
	 *   that contain neither + nor -, we can simply add it to the addition list
	 *   
	 * - if LEFT contains subexpressions that contain either - or + and these sub-
	 *   expressions are not nested by stronger operators, we can continue our search
	 *   for more subexpressions recursively 
	 * 
	 * @param leftExpression
	 * @param expressionList
	 * @param operator
	 * @return
	 */
	
	protected ArrayList<Expression> addLeftMinusPart(Expression leftExpression, ArrayList<Expression> expressionList) 
		throws PreprocessorException {
	
	// start with the left part
	boolean containsPlusOperator = false;
	boolean containsMinusOperator = false;
	boolean subExpressionHasWeakerOperator = false;
	boolean isBracketedBinaryExpression = false;
	
	// check if the left part of the binary expression contains anything we might want to collect
	if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
		containsPlusOperator = expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),EssenceGlobals.PLUS);
		containsMinusOperator = expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),EssenceGlobals.MINUS);
		subExpressionHasWeakerOperator = (strongerBinaryOperator(EssenceGlobals.PLUS, 
				                                                    leftExpression.getBinaryExpression().getOperator().getRestrictionMode()
				                             )  >= EQUAL_OP); // TODO: or equal?
	}
	else if(leftExpression.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) {
		if(leftExpression.getExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			containsPlusOperator = expressionContainsBinaryOperator(leftExpression.getExpression().getBinaryExpression(),
																		EssenceGlobals.PLUS);
			containsMinusOperator = expressionContainsBinaryOperator(leftExpression.getExpression().getBinaryExpression(),
					                                                     EssenceGlobals.MINUS);
			subExpressionHasWeakerOperator = (strongerBinaryOperator(EssenceGlobals.PLUS, 
					                                                    leftExpression.getExpression().getBinaryExpression().getOperator().getRestrictionMode()
					                             )  >= EQUAL_OP);
			isBracketedBinaryExpression = true;
		}
	}
		
	
	// the left expression contains a +operation and is not nested by a stronger operator
	// (A +/- B)  - RIGHT
	if(containsPlusOperator &&
			subExpressionHasWeakerOperator ) {
		BinaryExpression leftSubExpression = null;
		
		if(isBracketedBinaryExpression) 
			leftSubExpression = leftExpression.getExpression().getBinaryExpression();
		else leftSubExpression = leftExpression.getBinaryExpression();
		
		expressionList = collectExpressionsToBeOrdered(leftSubExpression, EssenceGlobals.PLUS, expressionList);
	}
	
	// the left expression contains a -operation (but no +op!)  that is not nested by a stronger operator     
	// (which can only be -op)
	
	//  A-B   -   RIGHT
	else if(containsMinusOperator && 
			!isBracketedBinaryExpression &&
			leftExpression.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.MINUS) {
			// add A to +list
			expressionList = addLeftMinusPart(leftExpression.getBinaryExpression().getLeftExpression(), 
					                          expressionList);
			// add B to -List
			this.expressionBuffer.add(leftExpression.getBinaryExpression().getRightExpression());
		
	}
	
	
	// (A-B)   -   RIGHT
	else if(containsMinusOperator &&
			isBracketedBinaryExpression &&
			leftExpression.getExpression().getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.MINUS) {
		// add A to +list
		expressionList = addLeftMinusPart(leftExpression.getExpression().getBinaryExpression().getLeftExpression(), 
				                          expressionList);
		// add B to -List
		this.expressionBuffer.add(leftExpression.getExpression().getBinaryExpression().getRightExpression());
	}
	
	
	else {  // LEFT  - RIGHT
		print_debug("just add the expression to the list:"+leftExpression);
 		expressionList.add(leftExpression);
	}
	
	
	return expressionList;
	
}




	
	
	
	/**
	 * 
	 * @param leftOperator
	 * @param rightOperator
	 * @return
	 */
	
	protected int strongerBinaryOperator(int leftOperator, int rightOperator) {
		
		if(leftOperator == rightOperator)
			return EQUAL_OP;
		
		// TODO: still needs extension!!!
		switch(leftOperator) {
			
		case EssenceGlobals.MINUS:
			if(rightOperator == EssenceGlobals.OR || 
				rightOperator == EssenceGlobals.AND	)
				return INCOMPARABLE_OP;
			else return RIGHT_OP_STRONGER;
				
		case EssenceGlobals.PLUS:
			if(rightOperator == EssenceGlobals.OR || 
					rightOperator == EssenceGlobals.AND	)
					return INCOMPARABLE_OP;
			else if(rightOperator == EssenceGlobals.MINUS)
				return LEFT_OP_STRONGER;
			else return RIGHT_OP_STRONGER;
			
		case EssenceGlobals.MULT:
			if(rightOperator == EssenceGlobals.OR || 
					rightOperator == EssenceGlobals.AND	)
					return INCOMPARABLE_OP;
			else if(rightOperator == EssenceGlobals.PLUS ||
				rightOperator == EssenceGlobals.MINUS)
				return LEFT_OP_STRONGER;
			else return RIGHT_OP_STRONGER;
			
		case EssenceGlobals.POWER:
			if(rightOperator == EssenceGlobals.PLUS ||
				rightOperator == EssenceGlobals.MINUS ||
				 rightOperator == EssenceGlobals.MULT)
				return LEFT_OP_STRONGER;
			else return RIGHT_OP_STRONGER;
			
		case EssenceGlobals.OR:
			return RIGHT_OP_STRONGER;
			
		case EssenceGlobals.AND:
			if(rightOperator == EssenceGlobals.OR)
				return LEFT_OP_STRONGER;
			else return RIGHT_OP_STRONGER;
			
		default:
			return INCOMPARABLE_OP;
		
		}
		
	}
	
	
	/**
	 * INSERTION SORT has been applied here
	 * 
	 * given a list of Expressions, order and return it. We assume that each expression in the list
	 * is linked with the same operator and that the order of the expressions does not
	 * alter the sematics of the expression. 
	 * 
	 * We also assume that no further ordering of the subexpressions in the expressions is necessary.
	 * 
	 * @param expressionList
	 * @return
	 */
	protected ArrayList<Expression> orderExpressionList(ArrayList<Expression> expressionList) 
		throws PreprocessorException {
		
		ArrayList<Expression> orderedList = new ArrayList<Expression>();
		
		
		for(int i=0;i<expressionList.size(); i++) {
			orderedList = addToOrderedList(expressionList.get(i), orderedList);
			print_debug("added new element to ordered list:"+orderedList);
		}
		
		if(orderedList.size() != expressionList.size())
			throw new PreprocessorException
			("Internal error: something with the sorting process of expressions has gone wrong. The ordered List has a different length "+
					" than the unordered list: \nOrdered list: "+orderedList+"\nUnordered list: "+expressionList);
		
	    return orderedList;	
	}
	
	
	/**
	 * helper method for INSERTION SORT
	 * 
	 * Add the Expression newElement to the already ordered (and possibly empty) ArrayList.
	 * We assume that all elements in the array are connected by the same operator and that
	 * the expression gained by applying all operators has the same semantics if the elements
	 * are ordered differently.
	 * 
	 * @param newElement
	 * @param orderedList
	 * @return
	 */
	protected ArrayList<Expression> addToOrderedList(Expression newElement, ArrayList<Expression> orderedList) 
		throws PreprocessorException {
		
		print_debug("About to insert "+newElement+" into the orderedList: "+orderedList);
		int newElementType = newElement.getRestrictionMode();
		if(newElementType == EssenceGlobals.BRACKET_EXPR)
			newElementType = newElement.getExpression().getRestrictionMode();
		
	
		for(int i=0; i<orderedList.size(); i++) {
			
			int currentElementType = orderedList.get(i).getRestrictionMode();
			if(currentElementType == EssenceGlobals.BRACKET_EXPR)
				currentElementType = orderedList.get(i).getExpression().getRestrictionMode();
			
			
			if(currentElementType > newElementType) {
				orderedList.add(i,newElement);
				return orderedList;
			}
			else if(currentElementType < newElementType) {
				// we compared the element to t he last in the list
				if(i == orderedList.size()-1) {
					orderedList.add(orderedList.size(), newElement);
					return orderedList;
				}
			    // else: do nothing -> just increase i
			}
			else { // same type! -> order same type
				//print_debug("the element to be inserted, "+newElement+", has same type as compared element "+orderedList.get(i));
				if(smallerSameExpressionType(newElement,orderedList.get(i)) == LEFT_IS_SMALLER) {
					orderedList.add(i,newElement);
					return orderedList;
				} // else, iterate once more!	
			}	
		}
		
		// if we arrive here, the new element to be inserted was larger than all elements in the ordered list, so add
		// it in the ending of the list
		orderedList.add(orderedList.size(), newElement);
		return orderedList;
	}
	
	
	/**
	 * determines which expression (of the same type) is smaller, Returns:
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 */
	private int smallerSameExpressionType(Expression leftExpression, Expression rightExpression) 
		throws PreprocessorException {
		
		
		if(leftExpression.getRestrictionMode() != rightExpression.getRestrictionMode())
			throw new PreprocessorException("Internal error. Expected same type of expressions:'"+rightExpression+"' and '"+leftExpression+"'.");
		
		switch(leftExpression.getRestrictionMode()) {
		
		case EssenceGlobals.ATOMIC_EXPR:
			return smallerAtomicExpression(leftExpression.getAtomicExpression(), rightExpression.getAtomicExpression());
		
		
		case EssenceGlobals.NONATOMIC_EXPR:
			return smallerNonAtomicExpression(leftExpression.getNonAtomicExpression(),
					                                                        rightExpression.getNonAtomicExpression());
			
		case EssenceGlobals.UNITOP_EXPR:
			return smallerUnaryExpression(leftExpression.getUnaryExpression(), rightExpression.getUnaryExpression());
			
		case EssenceGlobals.BRACKET_EXPR:
			int leftNestedType = leftExpression.getExpression().getRestrictionMode();
			int rightNestedType = rightExpression.getExpression().getRestrictionMode();
			
			if(leftNestedType < rightNestedType)       return LEFT_IS_SMALLER;
			else if(leftNestedType > rightNestedType)  return RIGHT_IS_SMALLER;
			else return smallerSameExpressionType(leftExpression.getExpression(), rightExpression.getExpression());
			
		case EssenceGlobals.BINARYOP_EXPR:
			return smallerBinaryExpression(leftExpression.getBinaryExpression(), rightExpression.getBinaryExpression());
			
		case EssenceGlobals.FUNCTIONOP_EXPR:
			return smallerFunctionExpression(leftExpression.getFunctionExpression(), rightExpression.getFunctionExpression());
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			return smallerQuantifiedExpression(leftExpression.getQuantification(), rightExpression.getQuantification());
			
		case EssenceGlobals.LEX_EXPR:
			// TODO:!
			
		default: throw new PreprocessorException("Unknown binary expression type or ordering not avaliable yet:"+leftExpression);
			
		}
		
	
	}
	
	


	/**
	 * Returns:
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * Order of quantifiers:
	 *  sum < forall < exists
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 */
	private int smallerQuantifiedExpression(QuantificationExpression leftExpression, QuantificationExpression rightExpression) 
		throws PreprocessorException {
		
		int leftType = leftExpression.getQuantifier().getRestrictionMode();
		int rightType = rightExpression.getQuantifier().getRestrictionMode();
		
		if(leftType < rightType)
			return LEFT_IS_SMALLER;
		else if(leftType > rightType)
			return RIGHT_IS_SMALLER;
		
		else { // we have the same quantification type
			int leftNestedExpression = leftExpression.getExpression().getRestrictionMode();
			int rightNestedExpression = rightExpression.getExpression().getRestrictionMode();
			
			if(leftNestedExpression < rightNestedExpression)
				return LEFT_IS_SMALLER;
			else if(rightNestedExpression < leftNestedExpression)
				return RIGHT_IS_SMALLER;
			
			else { // both nested types are the same
				return smallerSameExpressionType(leftExpression.getExpression(), rightExpression.getExpression());
			}
		}	
	}
	
	
	
	/**
	 * Returns:
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * Order:
	 * alldiff < element
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 */
	private int smallerFunctionExpression(FunctionExpression leftExpression, FunctionExpression rightExpression) 
		throws PreprocessorException {
		
		int leftType = leftExpression.getRestrictionMode();
		int rightType = rightExpression.getRestrictionMode();
		
		if(leftType < rightType)
			return LEFT_IS_SMALLER;
		else if(leftType > rightType)
			return RIGHT_IS_SMALLER;
		
		else { // both types are the same
			switch(leftType) {
			
			case EssenceGlobals.ALLDIFF:
				int leftParameterType = leftExpression.getExpression1().getRestrictionMode();
				int rightParameterType = rightExpression.getExpression1().getRestrictionMode();
				
				if(leftParameterType < rightParameterType)
					return LEFT_IS_SMALLER;
				else if(leftParameterType > rightParameterType)
					return RIGHT_IS_SMALLER;
				else // same parameter type
					return smallerSameExpressionType(leftExpression.getExpression1(), rightExpression.getExpression1());
				
				
			case EssenceGlobals.ELEMENT:
				int leftParamType1 = leftExpression.getExpression1().getRestrictionMode();
				int rightParamType1 = rightExpression.getExpression1().getRestrictionMode();
				
				if(leftParamType1 < rightParamType1)
					return LEFT_IS_SMALLER;
				else if(leftParamType1 > rightParamType1)
					return RIGHT_IS_SMALLER;
				
				else { // the FIRST parameter has same type 
					int difference1 =  smallerSameExpressionType(leftExpression.getExpression1(), rightExpression.getExpression1());
					
					if(difference1 != EXPR_ARE_EQUAL)
						return difference1;
					
					else {// the whole bloody thing again with the SECOND parameter
						int leftParamType2 = leftExpression.getExpression2().getRestrictionMode();
						int rightParamType2 = rightExpression.getExpression2().getRestrictionMode();
						
						if(leftParamType2 < rightParamType2)
							return LEFT_IS_SMALLER;
						else if(leftParamType2 > rightParamType2)
							return RIGHT_IS_SMALLER;
						else { // same parameter type 
							int difference2 =  smallerSameExpressionType(leftExpression.getExpression2(), rightExpression.getExpression2());
							
							if(difference2 != EXPR_ARE_EQUAL)
								return difference2;
							
							else { // do the whole bloody thing again again for the THIRD parameter
								int leftParamType3 = leftExpression.getExpression3().getRestrictionMode();
								int rightParamType3 = rightExpression.getExpression3().getRestrictionMode();
								
								if(leftParamType3 < rightParamType3)
									return LEFT_IS_SMALLER;
								else if(leftParamType3 > rightParamType3)
									return RIGHT_IS_SMALLER;
								else // same parameter type 
									return   smallerSameExpressionType(leftExpression.getExpression3(), rightExpression.getExpression3());
							}			
	
						}
						
					}
				}
				
			default:
					throw new PreprocessorException("Sorry, cannot order global constraint expression yet:"+leftExpression);
					
				
			}
			
		}
		
	}
	
	
	/**
	 * Returns:
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 */
	private int smallerBinaryExpression(BinaryExpression leftExpression, BinaryExpression rightExpression) 
		throws PreprocessorException {
		
		int leftOperator = leftExpression.getOperator().getRestrictionMode();
		int rightOperator = rightExpression.getOperator().getRestrictionMode();
		
		if(leftOperator < rightOperator)
			return LEFT_IS_SMALLER;
		else if(rightOperator < leftOperator)
			return RIGHT_IS_SMALLER;
		
		else { // we have the same operator
			   // we assume that the subexpressions are already ordered!
			
			// (leftleft OP rightleft)   versus   (leftright OP rightright)          
			int leftleftType = leftExpression.getLeftExpression().getRestrictionMode();
			int rightleftType = rightExpression.getLeftExpression().getRestrictionMode();
			
			if(leftleftType < rightleftType)
				return LEFT_IS_SMALLER;
			else if(leftleftType > rightleftType)
				return RIGHT_IS_SMALLER;
			
			else { // the left type is the same type
				int difference = smallerSameExpressionType(leftExpression.getLeftExpression(),
						                                        rightExpression.getLeftExpression());
				
				// both left expressions are exactly the same
				if(difference == EXPR_ARE_EQUAL) {
					int leftrightType = leftExpression.getRightExpression().getRestrictionMode();
					int rightrightType = rightExpression.getRightExpression().getRestrictionMode();
					
					if(leftrightType < rightrightType) return LEFT_IS_SMALLER;
					else if(rightrightType < leftrightType) return RIGHT_IS_SMALLER;
					else return smallerSameExpressionType(leftExpression.getRightExpression(),
                                                               rightExpression.getRightExpression());
				} 
				else return difference;
			}
				
		}
		
	}
	
	
	/**
	 * Returns true, if the left expression is smaller (or exactly equal) to the right expression
	 * 
	 * Order of unary operators:
	 *  negation < not < abs
	 *
	 *  Returns: 
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 * @throws PreprocessorException
	 */
	private int smallerUnaryExpression(UnaryExpression leftExpression, UnaryExpression rightExpression) 
		throws PreprocessorException {
		
		int leftType = leftExpression.getRestrictionMode();
		int rightType = rightExpression.getRestrictionMode();
		
		if(leftType < rightType)
			return LEFT_IS_SMALLER;
		else if(rightType < leftType)
			return RIGHT_IS_SMALLER;
		
		// we have the same type of unary operator
		else {
			int rightNestedType = rightExpression.getExpression().getRestrictionMode();
			int leftNestedType = leftExpression.getExpression().getRestrictionMode();
			
			if(leftNestedType < rightNestedType)
				return LEFT_IS_SMALLER;
			else if(leftNestedType > rightNestedType)
				return RIGHT_IS_SMALLER;
			else { // the nested types are both the same
				return smallerSameExpressionType(leftExpression.getExpression(), rightExpression.getExpression());
			}
			
		}
	}
	
	
	/**
	 * given two atomic Expressions, is the first (left) expression smaller than the other? 
	 * Returns: 
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * restriction modes are ordered as follows:
	 * boolean < number < identifier
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 */
	private int smallerAtomicExpression(AtomicExpression leftExpression, AtomicExpression rightExpression) 
		throws PreprocessorException {
		
		int leftType = leftExpression.getRestrictionMode();
		int rightType = rightExpression.getRestrictionMode();
		
		if(leftType < rightType) {
			return LEFT_IS_SMALLER;
		}
		else if(rightType < leftType) {
			return RIGHT_IS_SMALLER;
		}
		
		// rightType = leftType
		else {
			
			switch(leftType) {
			
			case EssenceGlobals.BOOLEAN:
				if(leftExpression.getBool() == rightExpression.getBool())
					return EXPR_ARE_EQUAL;
				else return (!leftExpression.getBool() || rightExpression.getBool()) ?  // !a \/ b   ==     a => b    ==    a <= b
						      LEFT_IS_SMALLER : RIGHT_IS_SMALLER ;
						       
			case EssenceGlobals.NUMBER:
				if (leftExpression.getNumber() == rightExpression.getNumber())
					return EXPR_ARE_EQUAL;
				else if (leftExpression.getNumber() < rightExpression.getNumber())
					return LEFT_IS_SMALLER;
				else return RIGHT_IS_SMALLER;
			
			case EssenceGlobals.IDENTIFIER:
				// compareTo returns a value <0 if this string is lexicographical smaller than the other string (and 0 if they are equal)
				int lexDifference = leftExpression.getString().compareTo(rightExpression.getString());
				if(lexDifference > 0) return RIGHT_IS_SMALLER;
				else if(lexDifference <0) return LEFT_IS_SMALLER;
				else return EXPR_ARE_EQUAL;
				
			default: // there are no other types, actually
				throw new PreprocessorException("Unknown atomic type:"+leftExpression);
					
			}
		}
	
	}
	
	
	
	
	/**
	 * Is The first (left) expression smaller than the other one?
	 * 
	 * matrix-elements are ordered:
	 *  - according to the amount of indices, where
	 *    for example     m[1] < m[1,2] < m[1,2,3] <  ...
	 * 
	 *  - according to the lexicographical ordering of their indices,
	 *    if they have the same amount of indices
	 *    m[1,2] < m[1,3] < m[3,4] < m[a,1] < ...
	 * 
	 * Returns: 
	 *  1: left expression is smaller
	 *  0: both expressions are equal
	 * -1: the left expression is greater 
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 * @throws PreprocessorException
	 */
	private int smallerNonAtomicExpression(NonAtomicExpression leftExpression, NonAtomicExpression rightExpression)
	throws PreprocessorException {
	
	// the expressions in the indices of the matrixelement, e.g. m[1,4] where 1,4 are 2 index expressions 
	Expression[] leftIndexExpressions = leftExpression.getExpressionList();
	Expression[] rightIndexExpressions = rightExpression.getExpressionList();
	
	// matrix-elements are first ordered according to the amount of indices
	if(leftIndexExpressions.length < rightIndexExpressions.length) {
		return LEFT_IS_SMALLER ;
		}
	else if(leftIndexExpressions.length > rightIndexExpressions.length) {
		return RIGHT_IS_SMALLER;
	}
	else { // leftIndexExpressions.length == rightIndexExpressions.length
		String leftIndexString = "";
		String rightIndexString = "";
		// build a string out of the index-expressions of each matrix element, e.g. m[2,5] => "25"
		for(int i=0; i<leftIndexExpressions.length; i++) {
			leftIndexString = leftIndexString.concat(leftIndexExpressions[i].toString());
			rightIndexString = rightIndexString.concat(rightIndexExpressions[i].toString());
		}
		// if the leftIndexString is lex<= than the rightIndexString, compareTo returns a value below 0 if smaller and 0 if equal
		int lexDifference = leftIndexString.compareTo(rightIndexString);
		
		if(lexDifference < 0)     return LEFT_IS_SMALLER;
		else if(lexDifference >0) return RIGHT_IS_SMALLER;
		else return EXPR_ARE_EQUAL;
	}
}
	
	
	/**
	 * returns true, if the operator (given its restriction mode) is
	 * commutative.
	 * @param operator
	 * @return
	 */
	private boolean isCommutative(int operator) {
		if(operator == EssenceGlobals.EQ ||
				operator == EssenceGlobals.NEQ ||
				operator == EssenceGlobals.PLUS ||
				operator == EssenceGlobals.MULT ||
				operator == EssenceGlobals.IFF ||
				operator == EssenceGlobals.AND ||
				operator == EssenceGlobals.OR)
			return true;
		else 
			return false;		
	}
	
	/**
	 * Returns true if the precedence of the operator (given its
	 * restriction mode) is non-associative in the parser.
	 * This means that there can only be one occurrence of the 
	 * operator in an expression (except when nested in parenthesis).
	 * For instance, "=" is non-assoc, so
	 *  a = b = c     is not allowed, but
	 *  a = (b = c)   is allowed
	 * 
	 * @param operator
	 * @return
	 */
	private boolean isNonAssociative(int operator) {
		if(operator == EssenceGlobals.EQ ||
				operator == EssenceGlobals.NEQ ||
				operator == EssenceGlobals.IFF)
			return true;
		else 
			return false;		
	}
	
	
	 /** 
     *  Print a debug message if the DEBUG flag is set.
     *  @param s the String to be printed
     *  */

    private void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG constraintOrdering ] "+s);
    }
	
	
	/**
	 * Orders left and right expression, which are of the same type and returns an Expression where
	 * the 
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 *//*
	private Expression[] orderSameTypeOfExpression(Expression rightExpression, Expression leftExpression) 
		throws PreprocessorException {
		
		
		if(leftExpression.getRestrictionMode() != rightExpression.getRestrictionMode())
			throw new PreprocessorException("Internal error. Expected same type of expressions:'"+rightExpression+"' and '"+leftExpression+"'.");
		
		
		if(leftExpression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR){
			AtomicExpression[] atomExpressions = orderAtomicExpressions(leftExpression.getAtomicExpression(), rightExpression.getAtomicExpression());
			return new Expression[] { new Expression(atomExpressions[0]),
					                     new Expression(atomExpressions[1]) };
		}
		
		else if(leftExpression.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
			NonAtomicExpression[] matrixElements = orderNonAtomicExpressions(leftExpression.getNonAtomicExpression(),
					                                                         rightExpression.getNonAtomicExpression()); 
			return new Expression[] {
                    new Expression(matrixElements[0]),
                    new Expression(matrixElements[1]) };
		}
		// TODO!!
		
		
		return null;
	}*/
	
	
	
	/**
	 * given two atomic Expressions, order them and return them in an array, where the first element 
	 * of the returned array is smaller than the second element
	 * 
	 * restriction modes are ordered as follows:
	 * boolean < number < identifier
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 */
	/*private AtomicExpression[] orderAtomicExpressions(AtomicExpression leftExpression, AtomicExpression rightExpression) 
		throws PreprocessorException {
		
		int leftType = leftExpression.getRestrictionMode();
		int rightType = rightExpression.getRestrictionMode();
		
		if(leftType < rightType) {
			return new AtomicExpression[] {leftExpression, rightExpression};
		}
		else if(rightType < leftType) {
			return new AtomicExpression[] {rightExpression, leftExpression};
		}
		
		// rightType = leftType
		else {
			
			switch(leftType) {
			
			case EssenceGlobals.BOOLEAN:
				return (!leftExpression.getBool() || rightExpression.getBool()) ?  // !a \/ b   ==     a => b    ==    a <= b
						       new AtomicExpression[] {leftExpression, rightExpression} :
						    	   new AtomicExpression[] { rightExpression, leftExpression} ;
						       
			case EssenceGlobals.NUMBER:
				return (leftExpression.getNumber() < rightExpression.getNumber()) ? 
						new AtomicExpression[] {leftExpression, rightExpression} :
							new AtomicExpression[] {rightExpression, leftExpression} ;
			
			case EssenceGlobals.IDENTIFIER:
				// compareTo returns a value <0 if this string is lexicographical smaller than the other string
				if(leftExpression.getString().compareTo(rightExpression.getString()) < 0) 
					return new AtomicExpression[] {leftExpression, rightExpression} ;	
				
				else return new AtomicExpression[] {rightExpression, leftExpression} ;
				
			default: // there are no other types, actually
				throw new PreprocessorException("Unknown atomic type:"+leftExpression);
					
			}
		}
	
	}*/
	
	

	/**
	 * order two non-atomic Expressions and return them in an array. The first array-element is the smaller one.
	 * 
	 * matrix-elements are ordered:
	 *  - according to the amount of indices, where
	 *    for example     m[1] < m[1,2] < m[1,2,3] <  ...
	 * 
	 *  - according to the lexicographical ordering of their indices,
	 *    if they have the same amount of indices
	 *    m[1,2] < m[1,3] < m[3,4] < m[a,1] < ...
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 * @return
	 * @throws PreprocessorException
	 */
	/*private NonAtomicExpression[] orderNonAtomicExpressions(NonAtomicExpression leftExpression, NonAtomicExpression rightExpression)
		throws PreprocessorException {
		
		// the expressions in the indices of the matrixelement, e.g. m[1,4] where 1,4 are 2 index expressions 
		Expression[] leftIndexExpressions = leftExpression.getExpressionList();
		Expression[] rightIndexExpressions = rightExpression.getExpressionList();
		
		// matrix-elements are first ordered according to the amount of indices
		if(leftIndexExpressions.length < rightIndexExpressions.length) {
			return new NonAtomicExpression[] {leftExpression, rightExpression} ;
 		}
		else if(leftIndexExpressions.length > rightIndexExpressions.length) {
			return new NonAtomicExpression[] {rightExpression, leftExpression};
		}
		else { // leftIndexExpressions.length == rightIndexExpressions.length
			String leftIndexString = "";
			String rightIndexString = "";
			// build a string out of the index-expressions of each matrix element, e.g. m[2,5] => "25"
			for(int i=0; i<leftIndexExpressions.length; i++) {
				leftIndexString = leftIndexString.concat(leftIndexExpressions[i].toString());
				rightIndexString = rightIndexString.concat(rightIndexExpressions[i].toString());
			}
			
			
		}
		
		return null;
	}*/
	
	
}
