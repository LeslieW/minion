package preprocessor;

import java.util.ArrayList;
//import java.util.HashMap;

//import conjureEssenceSpecification.Domain;
import conjureEssenceSpecification.Expression;
import conjureEssenceSpecification.UnaryExpression;
import conjureEssenceSpecification.BinaryExpression;
import conjureEssenceSpecification.BinaryOperator;
import conjureEssenceSpecification.AtomicExpression;
import conjureEssenceSpecification.NonAtomicExpression;
import conjureEssenceSpecification.FunctionExpression;
import conjureEssenceSpecification.QuantificationExpression;
//import conjureEssenceSpecification.Objective;
import conjureEssenceSpecification.EssenceGlobals;

public class ConstraintOrdering implements PreprocessorGlobals {

	
	
	private int LEFT_IS_SMALLER = 1;
	private int RIGHT_IS_SMALLER = -1;
	private int EXPR_ARE_EQUAL = 0;
	
	//private ArrayList<Expression> constraints;
	//private Objective objective;
	//private Parameters parameterArrays;
	
	//private HashMap<String,Domain>  decisionVariables ;  
	//private ArrayList<String> decisionVariablesNames;
	
	
	public ConstraintOrdering() {
		
	}
	
	
	/**
	 * Order the Essence' Expression 'constraint'
	 * 
	 * @param constraint
	 * @return
	 */
	public Expression orderExpression(Expression constraint) 
		throws PreprocessorException {
		
		switch(constraint.getRestrictionMode()) {
		
		case EssenceGlobals.ATOMIC_EXPR:
			return constraint;
		
		case EssenceGlobals.NONATOMIC_EXPR:
			return constraint;
		
		case EssenceGlobals.UNITOP_EXPR:
			return new Expression( new UnaryExpression(constraint.getUnaryExpression().getRestrictionMode(),
					              orderExpression(constraint.getUnaryExpression().getExpression()))
					               );		
		case EssenceGlobals.BINARYOP_EXPR:
			return orderBinaryExpression(constraint.getBinaryExpression());
		
		case EssenceGlobals.BRACKET_EXPR:
			return orderExpression(constraint.getExpression());
			
		case EssenceGlobals.FUNCTIONOP_EXPR:
			return constraint;
			
		case EssenceGlobals.LEX_EXPR:
			return constraint;
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			// order domain of binding variables?
			
			return new Expression(new QuantificationExpression(constraint.getQuantification().getQuantifier(),
					                                           constraint.getQuantification().getBindingExpression(),
					                                           orderExpression(constraint.getQuantification().getExpression())
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
	protected Expression orderBinaryExpression(BinaryExpression expression) 
		throws PreprocessorException {
		
		int operator = expression.getOperator().getRestrictionMode();
		
		if(!isCommutative(operator)) {
			return new Expression(new BinaryExpression(
					                 orderExpression(expression.getLeftExpression()),
					                  expression.getOperator(),
					                  orderExpression(expression.getRightExpression())));
		}
		
		else if(isNonAssociative(operator)) {
			ArrayList<Expression> orderedExpressions = new ArrayList<Expression>();
			orderedExpressions.add(orderExpression(expression.getLeftExpression()));
			orderedExpressions.add(orderExpression(expression.getRightExpression()));
			
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
			
			expressionList = collectExpressionsToBeOrdered(expression, operator, expressionList);
			print_debug("collected expressions to be ordered:"+expressionList);
			
			expressionList = orderExpressionList(expressionList);
		
			if(expressionList.size() == 0)
				throw new PreprocessorException("Internal error. Ordering process returned null when ordering expression:"+expression);
		
			Expression orderedExpression = buildExpressionFromList(expressionList, operator);
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

		print_debug("START Collecting expressions:"+expressionList+"\n that need to ordered from expression:"+expression
				+" with operator:"+new BinaryOperator(operator));	
		
		Expression leftExpression = expression.getLeftExpression();
		Expression rightExpression = expression.getRightExpression();
		
		// OP is the same operator as we are ordering 
		if(expression.getOperator().getRestrictionMode() == operator) {
			
			if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) 
				expressionList = collectExpressionsToBeOrdered(leftExpression.getBinaryExpression(), operator, expressionList);
			else expressionList.add(leftExpression);
			
			
			if(rightExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR)
				expressionList = collectExpressionsToBeOrdered(rightExpression.getBinaryExpression(), operator, expressionList);
			else expressionList.add(rightExpression);			
		}
		// this is a different operator
		else {
			
			if(leftExpression.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR &&
					rightExpression.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR)
				expressionList.add(new Expression(expression));
			
			
			else if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR &&
					rightExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				
					
				//	first order the two binary subexpressions, then add them to the list	
				if(operator == EssenceGlobals.AND ||
						operator == EssenceGlobals.MULT) {
					
					Expression leftPart = leftExpression;
					Expression rightPart = rightExpression;
					
					if(expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),operator))
						leftPart = new Expression(orderCommutativeExpression(leftExpression.getBinaryExpression(), operator));
					
					if(expressionContainsBinaryOperator(rightExpression.getBinaryExpression(),operator))
						rightPart = new Expression(orderCommutativeExpression(rightExpression.getBinaryExpression(), operator));
					
				expressionList.add(new Expression(
						                new BinaryExpression(
						                         leftPart,
						                         expression.getOperator(),
						                         rightPart
						                 )));
				}
				
				else if(operator == EssenceGlobals.PLUS || 
						//operator == EssenceGlobals.MINUS ||
						operator == EssenceGlobals.OR){
					// PLUS, MINUS, OR: search for more of them
					expressionList = collectExpressionsToBeOrdered(leftExpression.getBinaryExpression(), operator, expressionList);
					return collectExpressionsToBeOrdered(rightExpression.getBinaryExpression(), operator, expressionList);
					
				}
				else throw new PreprocessorException
				("Interal error: expected different operator in ordering process than:"+new BinaryOperator(operator)+" in expression: "+expression);
			}
			
			
			else if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				
				if(operator == EssenceGlobals.AND ||
						operator == EssenceGlobals.MULT) {
					Expression leftPart = leftExpression;
					if(expressionContainsBinaryOperator(leftExpression.getBinaryExpression(),operator))
						leftPart = new Expression(orderCommutativeExpression(leftExpression.getBinaryExpression(), operator));
					
					expressionList.add(new Expression(
			                new BinaryExpression(
			                         leftPart,
			                         expression.getOperator(),
			                         rightExpression)));	
				}
				else if(operator == EssenceGlobals.PLUS || 
						//operator == EssenceGlobals.MINUS ||
						operator == EssenceGlobals.OR) {
					
					if(expression.getOperator().getRestrictionMode() == EssenceGlobals.MULT || 
							expression.getOperator().getRestrictionMode() == EssenceGlobals.AND) {
						return expressionList; // there is nothing more to add
					}
					else {
						// I don't think we are allowed to add the right part -> the current operator is not the same as the 
						// one we are looking for
						//expressionList.add(rightExpression);
						if(expressionContainsBinaryOperator(leftExpression.getBinaryExpression(), operator))
							return  collectExpressionsToBeOrdered(leftExpression.getBinaryExpression(), operator, expressionList);
						else return expressionList;
					}
				}
				else throw new PreprocessorException
				("Interal error: expected different operator in ordering process than:"+new BinaryOperator(operator)+" in expression: "+expression);
			}
			
			
			else { //if(rightExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				
				if(operator == EssenceGlobals.AND ||
						operator == EssenceGlobals.MULT) {
					Expression rightPart = rightExpression;
					if(expressionContainsBinaryOperator(rightExpression.getBinaryExpression(),operator))
						rightPart = new Expression(orderCommutativeExpression(rightExpression.getBinaryExpression(), operator));
					
					expressionList.add(new Expression(
			                new BinaryExpression(
			                         leftExpression,
			                         expression.getOperator(),
			                         rightPart
			                         )));	
				}
				else if(operator == EssenceGlobals.PLUS || 
						//operator == EssenceGlobals.MINUS ||
						operator == EssenceGlobals.OR) {
					if(expression.getOperator().getRestrictionMode() == EssenceGlobals.MULT || 
							expression.getOperator().getRestrictionMode() == EssenceGlobals.AND) {
						return expressionList; // there is nothing more to add
					}
					else {
						// I don't think we are allowed to add the right part -> the current operator is not the same as the 
						// one we are looking for
						//expressionList.add(rightExpression);
						if(expressionContainsBinaryOperator(rightExpression.getBinaryExpression(), operator))
							return collectExpressionsToBeOrdered(rightExpression.getBinaryExpression(), operator, expressionList);
						else return expressionList;
					}
					
					
					//expressionList.add(leftExpression);
					//expressionList =  collectExpressionsToBeOrdered(leftExpression.getBinaryExpression(), operator, expressionList);
				}
				else throw new PreprocessorException
				("Interal error: expected different operator in ordering process than:"+new BinaryOperator(operator)+" in expression: "+expression);
			}
			
				
		}
		
		print_debug("Collecting expressions:"+expressionList+"\n that need to ordered from expression:"+expression
				+" with operator:"+new BinaryOperator(operator));	
		
		return expressionList;
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
		int elementType = newElement.getRestrictionMode();
		
	
		for(int i=0; i<orderedList.size(); i++) {
			
			if(orderedList.get(i).getRestrictionMode() > elementType) {
				orderedList.add(i,newElement);
				return orderedList;
			}
			else if(orderedList.get(i).getRestrictionMode() < elementType) {
				// we compared the element to t he last in the list
				if(i == orderedList.size()-1) {
					orderedList.add(orderedList.size(), newElement);
					return orderedList;
				}
			    // else: do nothing -> just increase i
			}
			else { // same type! -> order same type
				print_debug("the element to be inserted, "+newElement+", has same type as compared element "+orderedList.get(i));
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
