package translator.flattener;

import java.util.ArrayList;
import translator.conjureEssenceSpecification.*;



public class ExpressionFlattener {

	/**
	 * contains all translated expressions
	 * (used for common subexpression detection)
	 * - should I add atomic expressions as well, no?
	 */
	ArrayList<Expression> flattenedExpressions;
	
	/**
	 * creates auxiliary variables in a generic way
	 */
	AuxiliaryVariableCreator auxVariableCreator;
	
	/**
	 * collects all subexpressions and the variables/constraints that 
	 * represent them
	 */
	SubExpressionCollection subExpressionCollection;
	
	/**
	 * Constants that give the type of the operator
	 * that is nesting a binary expression. for instance in
	 * a*(b+c) the *-op is nesting the (b+c) operator. 
	 * OTHER is any other operator including "no operator". 
	 */
	private int OTHER = 2;
	private int PLUS = 1;
	private int MINUS = 0;
	
	
	public ExpressionFlattener() {
	
		this.flattenedExpressions = new ArrayList<Expression>();
		this.auxVariableCreator = new AuxiliaryVariableCreator();
		this.subExpressionCollection = new SubExpressionCollection();
	}
	
	


	/**
	 * Flatten Expression expression into a list of corresponding constraints.
	 * 
	 * 
	 * @param expression
	 * @param isNested TODO
	 * @return
	 */
	public FlattenedExpression flattenExpression(Expression expression, boolean isNested, int nestingOperator) 
		throws FlatteningException {
		
		switch(expression.getRestrictionMode()) {
		
		case EssenceGlobals.BRACKET_EXPR:
			return flattenExpression(expression.getExpression(), isNested, nestingOperator);
			
		case EssenceGlobals.ATOMIC_EXPR:
			return new FlattenedExpression(expression, expression.getAtomicExpression());
		
		case EssenceGlobals.NONATOMIC_EXPR:
			return new FlattenedExpression(expression, expression.getNonAtomicExpression());
			
		case EssenceGlobals.UNITOP_EXPR:
			return flattenUnaryExpression(expression.getUnaryExpression(), isNested, nestingOperator);
	
		case EssenceGlobals.BINARYOP_EXPR:
			return flattenBinaryExpression(expression.getBinaryExpression(), isNested, nestingOperator); // no nesting operator:OTHER
				
		case EssenceGlobals.FUNCTIONOP_EXPR:
			
		case EssenceGlobals.LEX_EXPR:
			
		}
		
		return null;
	}
	
	
	
	
	
	/**
	 * 
	 * A FlattenedExpression can either be:
	 *  - a variable/constant/auxVariable representing an expression
	 *  - a linear expression (composed of several expressions that are either 
	 *    added or substracted by each other)
	 *  - a single flattened expression (that is not an atom) representing the final, 
	 *    unflattened expression  
	 * 
	 * @param binExpression
	 * @param isNested
	 * @return
	 * @throws FlatteningException
	 */
	public FlattenedExpression flattenBinaryExpression(BinaryExpression binExpression, boolean isNested, int nestingOperator) 
		throws FlatteningException {
		
		int operator = binExpression.getOperator().getRestrictionMode();
		
		if(isRelationalOperator(operator)) {
			return flattenRelationalExpression(binExpression, isNested, nestingOperator);
		}
		else if(isMulOperator(operator)) {
			return flattenMulopExpression(binExpression,isNested, nestingOperator);
		}
		
		else if(isBooleanOperator(operator)) {
			
		}
		
		
		
		return null;
	}
	
	
	
	private FlattenedExpression flattenMulopExpression(BinaryExpression mulopExpression, boolean isNested, int nestingOperator) 
		throws FlatteningException {
		
		int operator = mulopExpression.getOperator().getRestrictionMode();
		
		if(!this.isMulOperator(operator))
			throw new FlatteningException("Internal error. Trying to flatten non-mulop expression '"+
					mulopExpression+"' in method flattenMilopExpression.");
		
		if(operator == EssenceGlobals.PLUS) {
			
			
			
		} // end: if op is PLUS
		
		else if(operator == EssenceGlobals.MINUS) {
			
			
		} // end: if op is MINUS
		
		else {
			
			
		} // end else: op is anything else but PLUS and MINUS
		
		return null;
	}
	
	
	/**
	 * Flatten a relational binary expression (relational: composed by one of the operators from 
	 * {=,!=,<,>,<=,>=}. If the operator is not relational, an exception will be thrown.
	 * <br>
	 *  The result of flattening a binary relation is on of the following:<br>
	 *  1. An auxiliary variable, representing the subexpression<br>
	 *  2. a final expression (if the expression is not nested)<br>
	 *  If further constraints are added during the flattening process, they are added on
	 *  the fly. The returned flattened expression is already added to the list of 
	 *  flattened constraints. Auxiliary variables are stored in the auxiliaryVariable creator.
	 * 
	 * @param relExpression
	 * @param isNested
	 * @return
	 * @throws FlatteningException
	 */
	private FlattenedExpression flattenRelationalExpression(BinaryExpression relExpression, boolean isNested, int nestingOperator) 
		throws FlatteningException {
		
		if(!isRelationalOperator(relExpression.getOperator().getRestrictionMode()))
				throw new FlatteningException
					("Internal error. Trying to flatten non-relational binary expression \n'"+relExpression.toString()+"'\n"+
							" in the method 'flattenRelationalExpression'.");
		
		FlattenedExpression leftFlatExpression = flattenExpression(relExpression.getLeftExpression(), true, OTHER); // isNested
		FlattenedExpression rightFlatExpression = flattenExpression(relExpression.getRightExpression(), true, OTHER); // isNested
		
		Expression leftExpression = null;
		Expression rightExpression = null;
		boolean leftIsLinear = false; // left expression is a linear Expression
		boolean rightIsLinear = false;
		
		
		/**
		 * First collect the flattened expression of the left and right part of the relation
		 */
		
		// the left expression is represented by an auxVariable (or variable or constant)
		if(leftFlatExpression.isAtomExpression()) {
			leftExpression = leftFlatExpression.getAtomAsExpression();
		}
		// we cannot have a final expression that is nested in a binary relation
		else if(leftFlatExpression.isFinalExpression()) {
			throw new FlatteningException
			("Internal error: a nested expression has been translated as if not nested (returned a final expression):"
					+leftFlatExpression.getFinalExpression());
		}
        // we have a linear representation 
		else { 
			leftIsLinear = true;
			leftExpression = new Expression(leftFlatExpression.getLinearExpression());
		}
		
		
		// we have an auxiliary variable for the right subexpression
		if(rightFlatExpression.isAtomExpression()) 
			rightExpression = rightFlatExpression.getAtomAsExpression();
		
		else if(rightFlatExpression.isFinalExpression())
			throw new FlatteningException
			("Internal error: a nested expression has been translated as if not nested (returned a final expression):"
					+rightFlatExpression.getFinalExpression());
		
		else  { // we have a linear representation
			rightIsLinear = true;
			rightExpression = new Expression(rightFlatExpression.getLinearExpression());
		}
		
		
		
		
		/**
		 * Now compose the left with the right expression depending on its type (atom or linear expression)
		 */
		
		// 1st case: we have two linear expressions
		if(leftIsLinear && rightIsLinear) {
			
			LinearExpression rightLinearExpression = rightExpression.getLinearExpression();
			LinearExpression leftLinearExpression = leftExpression.getLinearExpression();
			
			
			// CASE:(RelationalExpressionLeft) RELOP Right_lin_expression 
			if(leftLinearExpression.isRelationalLinearExpression()) {
						
			    // 1. auxVar1 = RelationalExpressionLeft
				AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
				Expression leftFlattenedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable1),
						new BinaryOperator(EssenceGlobals.EQ),
						leftExpression));
				this.flattenedExpressions.add(leftFlattenedExpression);
						
			    // 	CASE: (RelationalExpressionLeft) RELOP (RelationalExpressionRight) 
				if(rightLinearExpression.isRelationalLinearExpression()) {
							// what we have to do:
							//  1.  auxVar1 = RelationalExpressionLeft  (already done)
							//  2.  auxVar2 = RelationalExpressionRight
							//  3a. IF NOT NESTED:  auxVar1 RELOP auxVar2
							//  3b.  IF NESTED: auxVar3 = (auxVar1 RELOP auxVar2)
							//  4b.             return auxVar3 (bool)
														
					AtomicExpression auxVariable2 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					Expression rightFlattenedExpression = new Expression(new BinaryExpression(
							new Expression(auxVariable2),
							new BinaryOperator(EssenceGlobals.EQ),
							rightExpression));
					
					
					if(!isNested) {
						// // 2. auxVar2 = RelationalExpressionRight
						this.flattenedExpressions.add(rightFlattenedExpression);
						
						//3a. IF NOT NESTED:  auxVar1 RELOP auxVar2
						Expression flattenedExpression = new Expression(
								                            new BinaryExpression(
								                            		new Expression(auxVariable1),
								                            		relExpression.getOperator(),
								                            		new Expression(auxVariable2)));
						this.flattenedExpressions.add(flattenedExpression);
						leftFlatExpression.replaceFinalExpressionWith(flattenedExpression);
						return leftFlatExpression;
					}
					else {
						//  3b.  IF NESTED: auxVar3 = (auxVar1 RELOP auxVar2)
						//  4b.             return auxVar3 (bool)
						
						AtomicExpression auxVariable3 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
							// 3. auxVar3 = auxVar1 RELOP auxVar2
					    Expression reifiedExpression = new Expression(new BinaryExpression(
					    		new Expression(new BinaryExpression(new Expression(auxVariable1),
					    				relExpression.getOperator(),
					    				new Expression(auxVariable2))),
					    				new BinaryOperator(EssenceGlobals.EQ),
					    				new Expression(auxVariable3)));
					    this.flattenedExpressions.add(reifiedExpression);
							
					    // 4b. return auxVar3 (bool)
					    leftFlatExpression.replaceWithVariable(auxVariable3, new Expression(relExpression));
					    return leftFlatExpression;
					}
				}	
			   // CASE: (RelationalExpressionLeft) RELOP (MulopExpressionRight) 
				else { 
							// what we have to do:
							// 1. auxVar1 = RelationalExpressionLeft (already done)
					        // 2a. IF NOT NESTED: auxVar1 = MulopExpressionRight
					        // 3a.    return finalExpression
							// 2b. IF NESTED: 2. auxVar2 = (auxVar1 RELOP MulopExpressionRight)
							// 3b.    return auxVar2 (bool)
						
					
					Expression reifiedExpression = new Expression(new BinaryExpression(
							new Expression(auxVariable1),
							relExpression.getOperator(),
							rightExpression));
					
						
					if(!isNested) {
						// 2a. (auxVar1 RELOP MulopExpressionRight)
						this.flattenedExpressions.add(reifiedExpression);
						
						// 3.a return final expression
						leftFlatExpression.replaceFinalExpressionWith(reifiedExpression);
						return leftFlatExpression;
					}
					
					AtomicExpression auxVariable2 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					
					// 2b. auxVar2 = (auxVar1 RELOP MulopExpressionRight)
					Expression flattenedExpression = new Expression(new BinaryExpression(
							new Expression(auxVariable2),
							new BinaryOperator(EssenceGlobals.EQ),
							reifiedExpression));
					this.flattenedExpressions.add(flattenedExpression);
							
					// 3b. return auxVar2 
					leftFlatExpression.replaceWithVariable(auxVariable2, new Expression(relExpression));
					return leftFlatExpression;
					
				} // end else : (RelationalExpressionLeft) RELOP (MulopExpressionRight)
				
						
						
			} // end if left is relational
					
			// 	CASE:  (MulopExpressionLeft) = (RelationalExpressionRight) 
			else if(rightExpression.getLinearExpression().isRelationalLinearExpression()) {
				// what we have to do:
				// 1. auxVar1 = RelationalExpressionRight  (already done)
				// 2a. IF NOT NESTED: auxVar1 RELOP MulopExpressionLeft
				// 3a.               return finalExpression  
				// 2b. IF IS NESTED: auxVar2 = (auxVar1 RELOP MulopExpressionLeft)
				// 3b.               return auxVar2 (bool)
				
				AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
							
				// 1. auxVar1 = RelationalExpressionRight
				Expression rightFlattenedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable1),
						relExpression.getOperator(),
						rightExpression));
				this.flattenedExpressions.add(rightFlattenedExpression);
							
				Expression reifiedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable1),
						relExpression.getOperator(),
						leftExpression));
				
							
				if(!isNested) {
                    // 2a. (auxVar1 RELOP MulopExpressionLeft)
					this.flattenedExpressions.add(reifiedExpression);
					// 3a. return finalExpression
					leftFlatExpression.replaceFinalExpressionWith(reifiedExpression);
					return leftFlatExpression;	
				}
				
				AtomicExpression auxVariable2 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
				// 2b. auxVar2 = (auxVar1 RELOP MulopExpressionLeft)
				Expression flattenedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable2),
						new BinaryOperator(EssenceGlobals.EQ),
						reifiedExpression));
				this.flattenedExpressions.add(flattenedExpression);
							
				// 3b. return auxVar2 (bool)
				leftFlatExpression.replaceWithVariable(auxVariable2, new Expression(relExpression));
				return leftFlatExpression;
			
			} // end else if rightExpression is Relational
				
					
			else { // CASE:  LeftMulopExpression RELOP RightMulopExpression
				
				// what we have to do:
				//     1. auxVariable1 = MulopLeftExpression
				// IF RELOP == EQ AND NOT NESTED: 
				//       2a. auxVariable1 =  MulopRightExpression
				//       3a. return finalExpression
				// ELSE:
				//       2b. auxVariable2 = MulopRightExpression
				// IF NOT NESTED
				//       3a. auxVariable1 RELOP auxVariable2
				//       4a. return finalExpression
				// IF NESTED:
				//       3b. auxVariable3 = auxVariable1 RELOP auxVariable2
				//       4b. return auxVariable3
				
				
				// 1. auxVariable1 = MulopLeftExpression
				AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
				Expression leftFlattenedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable1),
						new BinaryOperator(EssenceGlobals.EQ),
						leftExpression));
				this.flattenedExpressions.add(leftFlattenedExpression);				
				
				
				if(relExpression.getOperator().getRestrictionMode() == EssenceGlobals.EQ &&
				   !isNested) {
					// 2a. auxVariable1 =  MulopRightExpression
					Expression rightFlattenedExpression = new Expression(new BinaryExpression(
							new Expression(auxVariable1),
							new BinaryOperator(EssenceGlobals.EQ),
							rightExpression));
					this.flattenedExpressions.add(rightFlattenedExpression);
					// 3a. return finalExpression
					// (here it does not matter which of {rightFlattenedExpression, leftFlattenedExpression} we pick)
					leftFlatExpression.replaceFinalExpressionWith(rightFlattenedExpression);
					return leftFlatExpression;
				}
				// 2b. auxVariable2 = MulopRightExpression
				AtomicExpression auxVariable2 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
				Expression rightFlattenedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable2),
						new BinaryOperator(EssenceGlobals.EQ),
						rightExpression));
				this.flattenedExpressions.add(rightFlattenedExpression);					
				
			
				Expression flattenedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable1),
						relExpression.getOperator(),
						new Expression(auxVariable2)));
				
					
				if(!isNested) {
                    // 3a. auxVariable1 RELOP auxVariable2
					this.flattenedExpressions.add(flattenedExpression);
					// 4a. return finalExpression
					leftFlatExpression.replaceFinalExpressionWith(flattenedExpression);
					return leftFlatExpression;					
				}
				
			
				// if it is nested
				AtomicExpression auxVariable3 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
				
				// 3b. auxVariable3 = auxVariable1 RELOP auxVariable2
				Expression reifiedExpression = new Expression(new BinaryExpression(
						new Expression(auxVariable3),
						new BinaryOperator(EssenceGlobals.EQ),
						flattenedExpression));
				this.flattenedExpressions.add(reifiedExpression);
							
				// 4b. return auxVar3 (bool)
				leftFlatExpression.replaceWithVariable(auxVariable3, new Expression(relExpression));
				return leftFlatExpression;
				
				
			} // end else : both left and right are linear mulop expressions
	
			
			
		} // end if(both the right and left expression are linear)
		
		
		
		// only one expression is a linear expression
		else if(leftIsLinear || rightIsLinear) {
			/** what we need to do:<br>
			 * 1. flatten expression that is not linear: flattenedVar<br>
			 * 2a. IF NOT NESTED:<br>
			 *        2aa. IF LinearExpression is NOT a relation:<br>
			 *             2aa. flattenedVar RELOP mulopExpression (depending on right/left)<br>
			 *             3aa. return finalExpression<br>
			 *        2ab. IF LinearExpression is a relation:<br>
			 *             2ab. auxVariable1 = LinearRelation<br>
			 *             3ab. auxVariable2 = flattenedVar RELOP auxVarible1 (depending on left/right)<br>
			 *             4ab. return auxVariable2 (bool)<br>
			 * 2b. IF NESTED:<br>
			 *        2ba. IF LinearExpression is NOT a relation<br>
			 *             2ba. auxVariable1 = flattenedVar RELOP mulopExpression (depending on left/right)<br>
			 *             3ba. return auxVariable1<br>
			 *        2bb. IF LinearExpression is a relation:<br>
			 *             2bb. auxVariable1 = LinearRelation<br>
			 *             3bb. auxVariable2 = auxVariable1 RELOP flattenedVar (depending on left/right)<br>
			 *             4bb. return auxVariable2<br>
			 *             			 
			 *                     
			*/ 
			

			LinearExpression linearExpression = (leftIsLinear) ? 
						leftExpression.getLinearExpression() : 
							rightExpression.getLinearExpression();
						
			Expression otherExpression = (leftIsLinear) ?
					    rightExpression : leftExpression;
								
			// 1. flatten expression that is not linear: flattenedVar		
			AtomExpression flattenedOtherExpression = flattenExpression(otherExpression, true, OTHER).getAtomExpression(); // isNested
			
			
			// 2a. IF NOT NESTED
			if(!isNested) {
				Expression finalExpression = null;
				
				if(!linearExpression.isRelationalLinearExpression()) {
					
					// 2aa. flattenedVar RELOP mulopExpression (depending on right/left)<br>
					
					if(linearExpression.hasPositiveElements() && linearExpression.hasNegativeElements()) {
						finalExpression = new Expression(new LinearExpression(
								                             linearExpression.getPositiveElements(),
								                             linearExpression.getNegativeElements(),
								                             relExpression.getOperator().getRestrictionMode(),
								                             flattenedOtherExpression,
								                             leftIsLinear
								                             ));
					}
					else if(linearExpression.hasPositiveElements()) {
						finalExpression = new Expression(new LinearExpression(
								                      linearExpression.getPositiveElements(),
								                      relExpression.getOperator().getRestrictionMode(),
								                      flattenedOtherExpression,
								                      true, // isPositive
								                      leftIsLinear));
					} else { // only negative elements
						finalExpression = new Expression(new LinearExpression(
								                      linearExpression.getPositiveElements(),
								                      relExpression.getOperator().getRestrictionMode(),
								                      flattenedOtherExpression,
								                      false, // isPositive is false
								                      leftIsLinear
								                      ));				                      
					} 
					
					// 3aa. return finalExpression
					this.flattenedExpressions.add(finalExpression);
					leftFlatExpression.replaceFinalExpressionWith(finalExpression);
					return leftFlatExpression;
					
				} // end: if linear is not relational
				
				// 2ab. IF LinearExpression is a relation
				else {
				     //  
					// 2ab. auxVariable1 = LinearRelation<br>
					// 3ab. auxVariable2 = flattenedVar RELOP auxVarible1 (depending on left/right)<br>
					// 4ab. return auxVariable2 (bool)<br>
					
					AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					
					// 2ab. auxVariable1 = LinearRelation
					Expression flattenedLinearExpression = new Expression(new BinaryExpression(
								new Expression(auxVariable1),
								new BinaryOperator(EssenceGlobals.EQ),
								new Expression(linearExpression)));
					
					this.flattenedExpressions.add(flattenedLinearExpression);
					
					// flattenedVar RELOP auxVarible1 (depending on left/right)
					AtomicExpression auxVariable2 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					Expression reifiedExpression = (leftIsLinear) ? 
							new Expression(new BinaryExpression(
									new Expression(flattenedLinearExpression),
									relExpression.getOperator(),
									flattenedOtherExpression.getCorrespondingExpression()))
					:
							new Expression(new BinaryExpression(
							flattenedOtherExpression.getCorrespondingExpression(),
							relExpression.getOperator(),
							new Expression(flattenedLinearExpression)));  
					
				    // 3ab.auxVariable2 = flattenedVar RELOP auxVarible1 
					finalExpression = new Expression(new BinaryExpression(
												new Expression(auxVariable2),
												new BinaryOperator(EssenceGlobals.EQ),
												reifiedExpression
					                  ));		

					// 4ab. return auxVariable2 (bool)
					this.flattenedExpressions.add(finalExpression);
					leftFlatExpression.replaceFinalExpressionWith(finalExpression);
					return leftFlatExpression;
					
					
				} //end else: LinearExpression is a relation
				
				
			} //end if:is not nested
			
			else { /**
					 * 2b. IF NESTED:<br>
			 *        2ba. IF LinearExpression is NOT a relation<br>
			 *             2ba. auxVariable1 = flattenedVar RELOP mulopExpression (depending on left/right)<br>
			 *             3ba. return auxVariable1<br>
			 *        2bb. IF LinearExpression is a relation:<br>
			 *             2bb. auxVariable1 = LinearRelation<br>
			 *             3bb. auxVariable2 = auxVariable1 RELOP flattenedVar (depending on left/right)<br>
			 *             4bb. return auxVariable2<br>
			 *             */
				if(!linearExpression.isRelationalLinearExpression()) {
					AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					Expression flattenedExpression = null;
					
					// flattenedVar RELOP mulopExpression (depending on left/right)
					if(linearExpression.hasPositiveElements() && linearExpression.hasNegativeElements()) {
						flattenedExpression = new Expression(new LinearExpression(
								                             linearExpression.getPositiveElements(),
								                             linearExpression.getNegativeElements(),
								                             relExpression.getOperator().getRestrictionMode(),
								                             flattenedOtherExpression,
								                             leftIsLinear // resultIsOnRightSide
								                             ));
					}
					else if(linearExpression.hasPositiveElements()) {
						flattenedExpression = new Expression(new LinearExpression(
								                      linearExpression.getPositiveElements(),
								                      relExpression.getOperator().getRestrictionMode(),
								                      flattenedOtherExpression,
								                      true, // isPositive
								                      leftIsLinear)); // resultIsOnRightSide
					} else { // only negative elements
						flattenedExpression = new Expression(new LinearExpression(
								                      linearExpression.getPositiveElements(),
								                      relExpression.getOperator().getRestrictionMode(),
								                      flattenedOtherExpression,
								                      false, // isPositive is false
								                      leftIsLinear
								                      ));				                      
					} 								
					
					// 2ba. auxVariable1 = flattenedVar RELOP mulopExpression
					Expression reifiedExpression = new Expression(new BinaryExpression(
							                          new Expression(auxVariable1),
							                          new BinaryOperator(EssenceGlobals.EQ),
							                          flattenedExpression));
					// 3ba. return auxVariable1
					this.flattenedExpressions.add(reifiedExpression);
					leftFlatExpression.replaceWithVariable(auxVariable1, new Expression(relExpression));
					return leftFlatExpression;
					
				} // end if: linearExpression is not a relation
				
				else {
					/**  IF LinearExpression is a relation:<br>
			          *      2bb. auxVariable1 = LinearRelation<br>
			          *      3bb. auxVariable2 = auxVariable1 RELOP flattenedVar (depending on left/right)<br>
			          *      4bb. return auxVariable2<br>
			 	      **/
					
					AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					
					// 2bb. auxVariable1 = LinearRelation
					Expression flattenedLinearExpression = new Expression(new BinaryExpression(
								new Expression(auxVariable1),
								new BinaryOperator(EssenceGlobals.EQ),
								new Expression(linearExpression)));			
					this.flattenedExpressions.add(flattenedLinearExpression);
					
					
					// auxVariable1 RELOP flattenedVar (depending on left/right)
					AtomicExpression auxVariable2 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
					Expression flattenedExpression = (leftIsLinear) ? 
							new Expression(new BinaryExpression(
									new Expression(flattenedLinearExpression),
									relExpression.getOperator(),
									flattenedOtherExpression.getCorrespondingExpression()))
					:
							new Expression(new BinaryExpression(
							flattenedOtherExpression.getCorrespondingExpression(),
							relExpression.getOperator(),
							new Expression(flattenedLinearExpression)));  
					
				    // 3bb.auxVariable2 = auxVariable1 RELOP flattenedVar 
					Expression reifiedExpression = new Expression(new BinaryExpression(
												new Expression(auxVariable2),
												new BinaryOperator(EssenceGlobals.EQ),
												flattenedExpression
					                  ));		
					this.flattenedExpressions.add(reifiedExpression);
					
					// 4bb. return auxVariable2
					leftFlatExpression.replaceWithVariable(auxVariable2, new Expression(relExpression));
					return leftFlatExpression;
					
				} // end else: linearExpression is a relation
				
				
			} //end else: relation is nested
			
			
		} // end else if: either left or right is linear
		
		
		else { // both expressions are NOT linear (both are variables/constants/atoms)
			
			if(!isNested) {
				// what to do:
				// 1. left RELOP right
				// 2. return final expression
				Expression finalExpression = new Expression(new BinaryExpression(
						                           leftExpression,
						                           relExpression.getOperator(),
						                           rightExpression));
				this.flattenedExpressions.add(finalExpression);
				
				leftFlatExpression.replaceFinalExpressionWith(finalExpression);
				return leftFlatExpression;	
			}
			else {
				// what to do:
				// 1. auxVariable1 = left RELOP right
				// 2. return auxVariable
				
				Expression nestedExpression = new Expression(new BinaryExpression(
						                           leftExpression,
						                           relExpression.getOperator(),
						                           rightExpression));
				AtomicExpression auxVariable1 = this.auxVariableCreator.createNewTemporaryVariable(0,1);
				
				//1. auxVariable1 = left RELOP right
				Expression reifiedExpression = new Expression(new BinaryExpression(
												new Expression(auxVariable1),
												relExpression.getOperator(),
												nestedExpression));
				this.flattenedExpressions.add(reifiedExpression);
				
				 // 2. return auxVariable
				leftFlatExpression.replaceWithVariable(auxVariable1, new Expression(relExpression));
				return leftFlatExpression;
			}
			
			
		} // end else: both left and right are not linear 
		
	}
	
	
	

	
	/**
	 * Flattens unary expressions to a (set of) flattened expression(s).
	 * ! (subexpression)
	 * - (subexpression)
	 *  | subexpression |
	 *  
	 *  TODO: possibly buggy!!! overhead in storage of expressions!!
	 *  
	 * @param unaryExpression
	 * @return
	 */
	public FlattenedExpression flattenUnaryExpression(UnaryExpression unaryExpression, boolean isNested, int nestingOperator) 
		throws FlatteningException {
		
		FlattenedExpression subExpression = null;
		
		// we have detected a common subexpression
		if(this.subExpressionCollection.hasCommonSubExpression(unaryExpression.getExpression())) {
			subExpression = subExpressionCollection.getExpressionRepresentation(unaryExpression.getExpression());
		}
		else { 
			subExpression = flattenExpression(unaryExpression.getExpression(), true, OTHER);
			this.subExpressionCollection.addSubExpression(subExpression);
		}
		
		// the flattened subexpression should be a variable
		if(subExpression.isAtomExpression()) {
			Expression nestedExpression = subExpression.getAtomAsExpression();
			UnaryExpression newUnaryExpression = new UnaryExpression(unaryExpression.getRestrictionMode(), nestedExpression);
		
			if(isNested) {
					// auxVar = unaryOP (nestedExpression)
					AtomicExpression auxVariable = this.auxVariableCreator.createNewTemporaryVariable(0, 1);
					Expression reifiedExpression = new Expression(new BinaryExpression(
														              new Expression(auxVariable),
														              new BinaryOperator(EssenceGlobals.EQ),
														              new Expression(newUnaryExpression)));
					
					// updating the subExpressionRepresentation:
					//    we add the reified expression, and the variable representing the flattened expression is auxVariable
					//subExpression.addExpression(reifiedExpression);
					subExpression.replaceWithVariable(auxVariable, new Expression(unaryExpression));
					
					
					this.flattenedExpressions.add(reifiedExpression);
					return subExpression;
			}
			else {
				// there is no aux-variable representing the whole expression anymore
				subExpression.replaceFinalExpressionWith(new Expression(newUnaryExpression));
				
				this.flattenedExpressions.add(new Expression(newUnaryExpression));
				return subExpression;
			}
			
		}
		else throw new FlatteningException("Error occurred during flattening of expression :"+unaryExpression+
				". Expected to flatten the subexpression to a variable, not to:"+subExpression);
		
	}
	
	
	
	/**
	 * Returns true if the given restriction mode (int operator) stands
	 * for a relational operator (=, !=, <,>,>=,<=)
	 * @param operator
	 * @return
	 */
	private boolean isRelationalOperator(int operator) {
		
		if(operator == EssenceGlobals.EQ ||
				operator == EssenceGlobals.NEQ ||
				operator == EssenceGlobals.LEQ ||
				operator == EssenceGlobals.GEQ ||
				operator == EssenceGlobals.LESS ||
				operator == EssenceGlobals.GREATER)
			return true;
		else return false;
	}
	
	/**
	 * Returns true if the given restriction mode (int operator) stands
	 * for a multiplication operator (+,-,*,\,^)
	 * @param operator
	 * @return
	 */
	private boolean isMulOperator(int operator) {
		
		if(operator == EssenceGlobals.PLUS ||
				operator == EssenceGlobals.MINUS ||
				operator == EssenceGlobals.MULT ||
				operator == EssenceGlobals.DIVIDE ||
				operator == EssenceGlobals.POWER)
			return true;
		else return false;
	}
	
	
	/**
	 * Returns true if the given restriction mode (int operator) stands
	 * for a boolean operator (/\, \/, =>, <=>)
	 * @param operator
	 * @return
	 */
	private boolean isBooleanOperator(int operator) {
		
		if(operator == EssenceGlobals.AND ||
				operator == EssenceGlobals.OR ||
				operator == EssenceGlobals.IF ||
				operator == EssenceGlobals.IFF)
			return true;
		else return false;
		
	}
	
}
