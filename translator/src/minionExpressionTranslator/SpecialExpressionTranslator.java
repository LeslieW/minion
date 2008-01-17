package minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import preprocessor.Parameters;
import preprocessor.PreprocessorException;

import conjureEssenceSpecification.BinaryExpression;
import conjureEssenceSpecification.AtomicExpression;
import conjureEssenceSpecification.NonAtomicExpression;
import conjureEssenceSpecification.Domain;
import conjureEssenceSpecification.EssenceGlobals;
import conjureEssenceSpecification.Expression;
import conjureEssenceSpecification.UnaryExpression;
//import conjureEssenceSpecification.UnaryExpression;
import conjureEssenceSpecification.BinaryOperator;

import minionModel.*;

/**
 * Translates top-level binary expressions into rather smart Minion constraints: it 
 * can translate scalar products (simple sums and weighted sums), partwise scalar 
 * products and simple binary expressions that consist of at least one atom.
 * 
 * @author andrea
 * @see conjureEssenceSpecification.BinaryExpression
 */

public class SpecialExpressionTranslator extends RelationalExpressionTranslator {

	
	ArrayList<Expression> scalarsList;
	ArrayList<Expression> weightedScalarsList;
	
	ArrayList<MinionIdentifier> iteratedVariablesList;
	ArrayList<MinionConstant> iteratedConstantsList;
	
	MinionBoundsVariable partwiseScalarFreshVariable;
	
	public SpecialExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
			HashMap<String, MinionIdentifier[]> minionVecs,
			HashMap<String, MinionIdentifier[][]> minionMatrixz,
			HashMap<String, MinionIdentifier[][][]> minionCubes, ArrayList<String> decisionVarsNames, HashMap<String, Domain> decisionVars, MinionModel mm, boolean useWatchedLiterals, boolean useDiscreteVariables, Parameters parameterArrays) {	
		super(minionVars, minionVecs, minionMatrixz,decisionVarsNames, decisionVars, mm, useWatchedLiterals, useDiscreteVariables, parameterArrays, minionCubes);
		
		scalarsList = new ArrayList<Expression> ();
		weightedScalarsList = new ArrayList<Expression> ();
		
		/** buffer to store variables to be used for a sum constraint*/
		iteratedVariablesList = new ArrayList<MinionIdentifier>();
		/** buffer to store constants to be used for a weighted sum constraint*/
		iteratedConstantsList = new ArrayList<MinionConstant>();
	}
	


	/**
	 * Translates top-level binary expressions into rather smart Minion constraints: it 
	 * can translate scalar products (simple sums and weighted sums), partwise scalar 
	 * products and simple binary expressions that consist of at least one atom.
	 * 
	 * @param e a Expression being a binary Expression that is translated rather sophisticatedly
	 * @param reifiable states if the translated expression has to be reifiable
	 * @return TODO
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	
	
	protected MinionConstraint translateSpecialExpression(Expression e, boolean reifiable) 
		throws TranslationUnsupportedException, MinionException, 
			ClassNotFoundException, PreprocessorException {
		
		//MinionConstraint minionConstraint = null;
		print_debug("Translatin special expression "+e.toString());
		
		if(e.getRestrictionMode() == EssenceGlobals.FUNCTIONOP_EXPR) {
			return translateGlobalConstraint(e);
		}
		
		if(e.getRestrictionMode() == EssenceGlobals.LEX_EXPR) {
			return translateLexConstraint(e);
		}
		
		if(e.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
			if(e.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT) {
				return translateBooleanNegationToConstraint(e.getUnaryExpression(), reifiable);
			}
		}
		
		BinaryExpression constraint = e.getBinaryExpression();
		print_debug("This is the binary special expression to be translated:"+e.toString());

		int operator = constraint.getOperator().getRestrictionMode();
		if(operator == EssenceGlobals.AND || operator == EssenceGlobals.OR ||
			operator == EssenceGlobals.IF || operator == EssenceGlobals.IFF)
			return translateBooleanRelationExpression(e, reifiable);
		
		print_debug("Checking if '"+e.toString()+" is a arithm. sum.");
		if(isIteratedArithmeticExpression(e, reifiable)) {
			print_debug("This is an arithemtic iterated expression: "+e.toString());
			return translateIteratedArithmeticExpression(constraint,reifiable);
		}
		print_debug("Checking if '"+e.toString()+" is a arithm. sum : NOOO");
		
		// 3. check for simple expressions (where one is an atom and the relation is =)
		if(isSimpleExpression(constraint)) {
			print_debug("This is a simple expression:"+e.toString());
			return translateSimpleExpression(constraint, reifiable);
		}
	
		return translateRelationalExpression(e, reifiable);
			
			
	}
	
	/**
	 * translate a boolean relation Expression e into a MinionConstraint. Detects 
	 * iterated conjunction and disjunction (even when nested in the expression)  
	 * by the structure of the expression syntax tree.
	 * @param e
	 * @param willBeReified states if the expression will later be reified (true)
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws ClassNotFoundException
	 * @throws PreprocessorException
	 */
	private MinionConstraint translateBooleanRelationExpression(Expression e, boolean willBeReified) 	
	 throws TranslationUnsupportedException, MinionException, 
	ClassNotFoundException, PreprocessorException {
		
		Expression left = e.getBinaryExpression().getLeftExpression();
		Expression right = e.getBinaryExpression().getRightExpression();
		
		print_debug("Translating boolean relation :"+e.toString()+", that will be reified:"+willBeReified);
		
		switch(e.getBinaryExpression().getOperator().getRestrictionMode()) {
		
		case EssenceGlobals.AND:
			if(!willBeReified){
				MinionConstraint constraintLeft = null;
				MinionConstraint constraintRight = null;
				if(left.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
						left.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
					constraintLeft = translateSpecialExpression(left,willBeReified);
					if(constraintLeft!=null)
						minionModel.addConstraint(constraintLeft);				
				} else translateSingleAtomExpression(left);
				
				if(right.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
						right.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
					constraintRight = translateSpecialExpression(right,willBeReified);
					if(constraintRight!=null)
						minionModel.addConstraint(constraintRight);
				} else translateSingleAtomExpression(right);
				
				if(constraintRight!= null)
					return constraintRight;
				else return constraintLeft;
			}
			else { // stuff might be reified, so we need to reify it :)
				ArrayList<MinionIdentifier> conjunction = new ArrayList<MinionIdentifier>();
				
				// reify the right subexpression (subtree)
				if(right.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
						right.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
					MinionConstraint rightConstraint = translateSpecialExpression(right, willBeReified);
					if(rightConstraint==null)
						throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
					MinionIdentifier auxVar1 = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
					minionModel.addReificationConstraint((MinionReifiableConstraint) rightConstraint,auxVar1);
					conjunction.add(auxVar1);
				} // else we have an atom on the right side (subtree)
				else  conjunction.add(translateAtomExpression(right));
			
				
				// check if there are any iterated conjunctions
				while(left.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
					if(left.getBinaryExpression().getOperator().getRestrictionMode() != EssenceGlobals.AND) break;
					Expression leftRightExpression = left.getBinaryExpression().getRightExpression();					
						// if the right expression of the left subtree is not an atom
					if(leftRightExpression.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
							leftRightExpression.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
						MinionConstraint leftRightConstraint = translateSpecialExpression(leftRightExpression, willBeReified);
						if(leftRightConstraint==null)
							throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"
										+leftRightExpression.toString());
						MinionIdentifier auxVar = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
						minionModel.addReificationConstraint((MinionReifiableConstraint) leftRightConstraint,auxVar);
						conjunction.add(auxVar);
					}
					else { // we have an atom
						conjunction.add(translateAtomExpression(leftRightExpression));
					}
					left = left.getBinaryExpression().getLeftExpression();
				}
			
				if(left.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
						left.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
					MinionConstraint leftConstraint = translateSpecialExpression(left, willBeReified);
					if(leftConstraint == null)
						throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"
							+left.toString());
					MinionIdentifier auxVar2 = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
					minionModel.addReificationConstraint((MinionReifiableConstraint) leftConstraint,auxVar2);
					conjunction.add(auxVar2);
				} // we have an atom on the left side
				else conjunction.add(translateAtomExpression(left));
				
				MinionIdentifier[] conjunctedVars = new MinionIdentifier[conjunction.size()];
				Object[] buffer = conjunction.toArray();
				for(int i=0; i<conjunctedVars.length; i++)
					conjunctedVars[i] = (MinionIdentifier) buffer[i];	
					
				return new MinionSumConstraint(conjunctedVars, new MinionConstant(conjunctedVars.length), this.useWatchedLiterals && !willBeReified);
			}
				
		case EssenceGlobals.OR:
			ArrayList<MinionIdentifier> disjunction = new ArrayList<MinionIdentifier>();
			
			if(right.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
					right.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				MinionConstraint constraintRight = translateSpecialExpression(right,true);
				if(constraintRight==null) 
					throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
			
				MinionIdentifier auxVar1 = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
				minionModel.addReificationConstraint((MinionReifiableConstraint) constraintRight,auxVar1);
				disjunction.add(auxVar1);
			}
			else disjunction.add(translateAtomExpression(right));
			
			// check if there are some iterated disjunctions
			
			while(left.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				if(left.getBinaryExpression().getOperator().getRestrictionMode() != EssenceGlobals.OR) break;
				
				Expression leftRightExpression = left.getBinaryExpression().getRightExpression();
					
				if(leftRightExpression.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
						leftRightExpression.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
					MinionConstraint leftRightConstraint = translateSpecialExpression(leftRightExpression, true); // will be reified
					if(leftRightConstraint==null)
						throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"
								+leftRightExpression.toString());
					MinionIdentifier auxVar = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
					minionModel.addReificationConstraint((MinionReifiableConstraint) leftRightConstraint,auxVar);
					disjunction.add(auxVar);
				} else disjunction.add(translateAtomExpression(leftRightExpression));
					
				left = left.getBinaryExpression().getLeftExpression();
			}
			
			if(left.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
					left.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				MinionConstraint constraintLeft = translateSpecialExpression(left,true);
				if(constraintLeft==null)
					throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
			
				MinionIdentifier auxVar2 = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
				minionModel.addReificationConstraint((MinionReifiableConstraint) constraintLeft,auxVar2);
				disjunction.add(auxVar2);
			} 
			else disjunction.add(translateAtomExpression(left));
			
			MinionIdentifier[] disjunctedVars = new MinionIdentifier[disjunction.size()];
			Object[] buffer = disjunction.toArray();
			for(int i=0; i<disjunctedVars.length; i++)
				disjunctedVars[i] = (MinionIdentifier) buffer[i];	
			
			return new MinionSumGeqConstraint(disjunctedVars, new MinionConstant(1), this.useWatchedLiterals && !willBeReified);
		
			
		case EssenceGlobals.IF:
			if(willBeReified) throw new TranslationUnsupportedException("Cannot translate expression '"+e.toString()+
						"', because Minion does not support reification of inequalities yet.");
			
			MinionIdentifier auxVar1If = null;
			if(left.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
					left.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				MinionConstraint constraintLeftIf = translateSpecialExpression(left,true);
				if(constraintLeftIf==null)
					throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
			
				auxVar1If = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
				minionModel.addReificationConstraint((MinionReifiableConstraint) constraintLeftIf,auxVar1If);
			} else auxVar1If = translateAtomExpression(left); 
			
			
			MinionIdentifier auxVar2If = null;
			if(right.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
					right.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				MinionConstraint constraintRightIf = translateSpecialExpression(right,true); //willBeReified);
				if(constraintRightIf==null) 
					throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
			
				auxVar2If = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
				minionModel.addReificationConstraint((MinionReifiableConstraint) constraintRightIf,auxVar2If);
			}
			else auxVar2If = translateAtomExpression(right);
			
			return new MinionInEqConstraint(auxVar1If,auxVar2If, new MinionConstant(0));
			
			
		case EssenceGlobals.IFF:
			MinionIdentifier auxVar1Iff = null;
			
			if(left.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
					left.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				MinionConstraint constraintLeftIff = translateSpecialExpression(left,true);
				if(constraintLeftIff==null)
					throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
			
				auxVar1Iff = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
				minionModel.addReificationConstraint((MinionReifiableConstraint) constraintLeftIff,auxVar1Iff);
			} 
			else auxVar1Iff = translateAtomExpression(left);
			
			MinionIdentifier auxVar2Iff = null;

			if(right.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR && 
					right.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				MinionConstraint constraintRightIff = translateSpecialExpression(right,true);
				if(constraintRightIff==null) 
					throw new MinionException("Internal error. Could not reify constraint, because translation returned null:"+e.toString());
			
				auxVar2Iff = variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, this.useDiscreteVariables);
				minionModel.addReificationConstraint((MinionReifiableConstraint) constraintRightIff,auxVar2Iff);
			}
			else auxVar2Iff = translateAtomExpression(right);
			
			return new MinionEqConstraint(auxVar1Iff,auxVar2Iff);
			
		
		
			
		default: throw new TranslationUnsupportedException("Internal error. Trying to translate non-boolean-relation '"+e.toString()+
				"' in boolean-relation translator part.");
		}

	}

    
  
	
	/**
	 * The constraint is simple, if one of its expressions (left or right hand) is atomic
	 * (or non-atomic, meaning it is a vector-element) and the relation is equality.
	 * @param constraint the BinaryExpression that is checked for being simple 
	 * @return true if constraint is simple
	 */	
	private boolean isSimpleExpression(BinaryExpression constraint) {
		if(constraint.getOperator().getRestrictionMode() == EssenceGlobals.EQ) {
			print_debug("This might be a simple expression:"+constraint.toString());
			
		    if(constraint.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
		       constraint.getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR ||
		       constraint.getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR ||
		       constraint.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		    	return true;
		    }
		    if(constraint.getLeftExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
		    	if(constraint.getLeftExpression().getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT){
		    		if(constraint.getLeftExpression().getUnaryExpression().getExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
		    			constraint.getLeftExpression().getUnaryExpression().getExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
		    		return true;
		    	}
		    }
		    if(constraint.getRightExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
		    	if(constraint.getRightExpression().getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT){
		    		print_debug("We found a negated simple expression:"+constraint.toString());
		    		return true;
		    	}
		    }
		}
		return false;
	}
	
	
	/**
	 * Translates a simple Expression on top (meaning that constraint may not be a subexpression
	 * of another expression. 
	 * @param constraint the BinaryExpression that contains at least one AtomicExpression
	 * 		on the left or right hand side and whose operator is equality. 
	 * @param reifiable TODO
	 * @return TODO
	 * @throws TranslationUnsupportedException
	 */	
	protected MinionConstraint translateSimpleExpression(BinaryExpression constraint, boolean reifiable) 
		throws TranslationUnsupportedException, MinionException, 
		ClassNotFoundException, PreprocessorException {
		
		Expression e_left = constraint.getLeftExpression();
		Expression e_right = constraint.getRightExpression();
		
		print_debug("translating simple expression: "+constraint.toString());
		
		
		if(e_left.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR && 
	             e_right.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {		
			 print_debug("We got a 2 non-atoms here here:"+constraint.toString());
			Expression[] indexExpressionsLeft = e_left.getNonAtomicExpression().getExpressionList();
			Expression[] indexExpressionsRight = e_right.getNonAtomicExpression().getExpressionList();		
			boolean leftIsDynamicAssignment = false;
			boolean rightIsDynamicAssignment = false;
			
			  for(int i=0; i<indexExpressionsLeft.length; i++) {
				  if(indexExpressionsLeft[i].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) 
					 leftIsDynamicAssignment = true;  
			  }
			  
			  for(int i=0; i<indexExpressionsRight.length; i++) {
				  if(indexExpressionsRight[i].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) 
					 rightIsDynamicAssignment = true;  
			  }
			  
			  print_debug("Left is dynamic:"+leftIsDynamicAssignment+", and right:"+rightIsDynamicAssignment+" in: "+constraint.toString());
			  
			  if(leftIsDynamicAssignment && rightIsDynamicAssignment && !reifiable) {
				  print_debug("We got a vveeeeeeeery special case here:"+constraint.toString());
				  MinionIdentifier freshVariable = translateElementConstraintIndex(e_left.getNonAtomicExpression(),null);
				  translateElementConstraintIndex(e_right.getNonAtomicExpression(), freshVariable);
				  return null;
			  }
			  else if(leftIsDynamicAssignment  && !reifiable) {
				  MinionIdentifier rightIdentifier = translateAtomExpression(e_right);
				  translateElementConstraintIndex(e_left.getNonAtomicExpression(), rightIdentifier);
				  return null;
			  }
			  else if(rightIsDynamicAssignment  && !reifiable) {
				  MinionIdentifier leftIdentifier = translateAtomExpression(e_left);
				  translateElementConstraintIndex(e_right.getNonAtomicExpression(), leftIdentifier);
				  return null;
			  }
			  
		}
		
		if(e_left.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR && 
	             e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {		
			Expression[] indexExpressionsLeft = e_left.getNonAtomicExpression().getExpressionList();	
			boolean leftIsDynamicAssignment = false;
	
			  for(int i=0; i<indexExpressionsLeft.length; i++) {
				  if(indexExpressionsLeft[i].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) 
					 leftIsDynamicAssignment = true;  
			  }
			  
			  if(leftIsDynamicAssignment  && reifiable) {
				  MinionIdentifier rightIdentifier = translateAtomExpression(e_right);
				  translateElementConstraintIndex(e_left.getNonAtomicExpression(), rightIdentifier);
				  return null;
			  }	 
		}
		
		if(e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR && 
	             e_right.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {		
			Expression[] indexExpressionsRight = e_right.getNonAtomicExpression().getExpressionList();		
			boolean rightIsDynamicAssignment = false;
			  
			  for(int i=0; i<indexExpressionsRight.length; i++) {
				  if(indexExpressionsRight[i].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) 
					 rightIsDynamicAssignment = true;  
			  }
			  
			  if(rightIsDynamicAssignment  && reifiable) {
				  MinionIdentifier leftIdentifier = translateAtomExpression(e_left);
				  translateElementConstraintIndex(e_right.getNonAtomicExpression(), leftIdentifier);
				  return null;
			  }
		}
		
		
		if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
		    e_left.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) && 
	            (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
	             e_right.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) ) {
	
			// we can assume the EQ operator here
			MinionIdentifier leftIdentifier = translateAtomExpression(e_left);
			if(leftIdentifier == null) { // we can reuse variables!
				reuseVariableInExpression(e_left,translateAtomExpression(e_right));
				return null;
			}
			else 
				return new MinionEqConstraint(leftIdentifier,
		    								  translateAtomExpression(e_right));
		}
		
		Expression simpleExpression = e_left;
		Expression complexExpression = e_right;		
		
		if((e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) ||
				   (e_right.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR &&
				    e_right.getNonAtomicExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR_BRACKET) ||
				    e_right.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
			simpleExpression = e_right;
			complexExpression = e_left;
		}
	
		return translateSimpleExpression(simpleExpression,complexExpression, reifiable);
		
		
	}
	
	
	/**
	 * 
	 * 
	 * @param expression
	 * @param identifier
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	private void reuseVariableInExpression(Expression expression, MinionIdentifier identifier) 
		throws TranslationUnsupportedException, MinionException {
		
		switch(expression.getRestrictionMode()) {
		
		case EssenceGlobals.ATOMIC_EXPR :
		
			if(expression.getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
				// TODO!!
				//String variableName = expression.getAtomicExpression().getString();
				// minionModel.insertIdentifierFor(variableName, identifier);
				break;
			}
		
		case EssenceGlobals.NONATOMIC_EXPR :
			if(expression.getNonAtomicExpression().getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
					throw new TranslationUnsupportedException 
						("Please access matrix elements by m[i,j,k] instead of m[i][j][k] as in :"+expression.toString());
			
			if(expression.getNonAtomicExpression().getExpression().getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER) 
				throw new TranslationUnsupportedException 
				("Please access matrix elements by m[i,j,k] instead of m[i][j][k] as in :"+expression.toString());
	
			
			NonAtomicExpression matrixElement = expression.getNonAtomicExpression();
			  //   matrixName [ indexExpressions ] 
			Expression[] indexExpressions = matrixElement.getExpressionList();
			String matrixName = matrixElement.getExpression().getAtomicExpression().getString();
			
			  if(parameterArrays.isParameter(matrixName))
				  throw new TranslationUnsupportedException 
					("Cannot assign a value to a parameter-array: :"+expression.toString());
			  
			  // make sure that the indices are legal
			  for(int i=0; i<indexExpressions.length; i++) {
				  if(indexExpressions[i].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
					  // TODO: translateElementConstraintIndex(matrixElement);
					  // find a smart way to do this efficiently (imposing the element constraint should be rather easy now)
					  ;
				  
				  if(indexExpressions[i].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
					  throw new TranslationUnsupportedException
				  		("Illegal (non-atomic) index '"+indexExpressions[i]+"' for vector/matrix element:"+matrixElement.toString());
				  
				  if(indexExpressions[i].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER)
					  // TODO: translateElementConstraintIndex(matrixElement);
					  // find a smart way to do this efficiently (imposing the element constraint should be rather easy now)
					  ;	
					  
				  else if(indexExpressions[i].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
					  throw new TranslationUnsupportedException
				  	("	Illegal (non-integer) index '"+indexExpressions[i]+"' for vector/matrix element:"+matrixElement.toString());
			  }
			  
			  
			  // is THIS necessary?? I don't think so...
			  if(decisionVariables.containsKey(matrixName)) {
				   if(!minionVectors.containsKey(matrixName) && !minionMatrices.containsKey(matrixName) && !minionCubes.containsKey(matrixName)) 
					   variableCreator.addNewVariable(matrixName);
			  }
			  else throw new TranslationUnsupportedException("Unknown variable or matrix-element name :"+matrixElement.toString());
			  
			  
			  switch(indexExpressions.length) {
			  
			  case 1: // 1-dimensional 
				  // the identifier is a simple element
				  if(minionVectors.containsKey(matrixName)) {
					  MinionIdentifier[] variableVector = minionVectors.get(matrixName); 
				  
					  int matrixIndex = indexExpressions[0].getAtomicExpression().getNumber();
					  int offset = minionModel.getVectorOffset(matrixName);//variableVector[0].getOriginalName());
					  matrixIndex = matrixIndex - offset;
				  
					  if(matrixIndex < 0 || matrixIndex >= variableVector.length)
						  throw new TranslationUnsupportedException		  
						  ("The index '"+matrixIndex+"' assigned to '"+matrixName+"' is out of bounds.");
				  
					  variableVector[matrixIndex] = identifier;
					  minionModel.insertIdentifierInVectorAt(matrixName, matrixIndex, identifier);
					  return;
				  }	
				  // the identifier is a vector 
				  else if(minionMatrices.containsKey(matrixName)) 
					throw new TranslationUnsupportedException
					("Cannot use vector '"+matrixElement.toString()+"' as an atomic expression.");
				  
				  else  
					  	throw new TranslationUnsupportedException
					  	("Unknown variable: "+matrixElement.toString());
				  
				  
			  case 2: //2-dimensional
				  print_debug("ok, we have a 2-dim matrix: "+matrixElement.toString());
				  MinionIdentifier[][] variableMatrix = minionMatrices.get(matrixName);
				  if(variableMatrix == null)
					  throw new TranslationUnsupportedException
					  	("Unknown variable matrix: "+matrixElement.toString());
				  
				  print_debug("and we know it!!! :) ");
				  int vectorIndex = indexExpressions[0].getAtomicExpression().getNumber();
				  int elementIndex = indexExpressions[1].getAtomicExpression().getNumber();
				  
				  print_debug("WITHOUT OFFSET: the vectorIndex is:"+vectorIndex+", and the elementIndex:"+elementIndex);
				  
				  int[] offsets = minionModel.getMatrixOffsets(matrixName);
				  
				  print_debug("offsets[0]:"+offsets[0]+", and offsets[1]:"+offsets[1]);
				  
				  vectorIndex = vectorIndex - offsets[0];
				  elementIndex = elementIndex - offsets[1];
				  
				  print_debug("WITH OFFSET: the vectorIndex is:"+vectorIndex+", and the elementIndex:"+elementIndex);
				  
				  if(vectorIndex <0 || vectorIndex >= variableMatrix.length)
					  throw new TranslationUnsupportedException		  
					  ("The vector-index '"+(vectorIndex+offsets[0])+"' assigned to '"+matrixName+"' is out of bounds.");
				
				  if(elementIndex <0 || elementIndex >= variableMatrix[vectorIndex].length)
					  throw new TranslationUnsupportedException		  
					  ("The element-index '"+(elementIndex+offsets[1])+"' assigned to '"+matrixName+"' is out of bounds.");
				  
				  variableMatrix[vectorIndex][elementIndex] = identifier;
				  minionModel.insertIdentifierInMatrixAt(matrixName, vectorIndex, elementIndex, identifier);
				  return;
			
			  case 3: // 3-dimensional
				  print_debug("Translating 3-dim matrix element: "+matrixElement.toString());
				  MinionIdentifier[][][] variableCube = minionCubes.get(matrixName);
				  if(variableCube == null)
					  throw new TranslationUnsupportedException
					  	("Unknown variable cube: "+matrixElement.toString());
					  
				  int matrixIndex = indexExpressions[0].getAtomicExpression().getNumber();
				  int vectIndex = indexExpressions[1].getAtomicExpression().getNumber();
				  int elemIndex = indexExpressions[2].getAtomicExpression().getNumber();
				  
				  int[] cOffsets = minionModel.getCubeOffsets(matrixName);
				  matrixIndex = matrixIndex - cOffsets[0];
				  vectIndex = vectIndex - cOffsets[1];
				  elemIndex = elemIndex - cOffsets[2];
				  
				  if(matrixIndex <0 || matrixIndex >= variableCube.length)
					  throw new TranslationUnsupportedException		  
					  ("The matrix-index '"+(matrixIndex+cOffsets[0])+"' assigned to '"+matrixName+"' is out of bounds in :"+matrixElement.toString()); 
					  
				  if(vectIndex <0 || vectIndex >= variableCube[matrixIndex].length)
					  throw new TranslationUnsupportedException		  
					  ("The vector-index '"+(vectIndex+cOffsets[1])+"' assigned to '"+matrixName+"' is out of bounds in:"+matrixElement.toString());
				
				  if(elemIndex <0 || elemIndex >= variableCube[matrixIndex][vectIndex].length)
					  throw new TranslationUnsupportedException		  
					  ("The element-index '"+(elemIndex+cOffsets[2])+"' assigned to '"+matrixName+"' is out of bounds in:"+matrixElement.toString());  
				  
				  variableCube[matrixIndex][vectIndex][elemIndex] = identifier;
				  minionModel.insertIdentifierInCubeAt(matrixName, matrixIndex, vectIndex, elemIndex, identifier);
				  return;
				  
			  default: // multi-dimensional
					  throw new TranslationUnsupportedException
					  ("Multi-dimensional matrices (over 2-dimensions) are not supported yet, sorry.");
			  
			  
			  }
		
			
			  
		default : throw new TranslationUnsupportedException 
			("Internal error. Expected an atom during translation of an assignment to :"+expression.toString());
		}
	}
	
	
	/**
	 * 
	 * @param simpleExpression is either an AtomicExpression or a NonAtomicExpression
	 * @param complexExpression is neither an AtomicExpression nor a NonAtomicExpression
	 * @param reifiable TODO
	 * @return TODO
	 * @throws TranslationUnsupportedException
	 */
	
	private MinionConstraint translateSimpleExpression(Expression simpleExpression, Expression complexExpression, boolean reifiable) 
		throws TranslationUnsupportedException, MinionException, 
		ClassNotFoundException, PreprocessorException {
				
		// TODO: check for scalar products!
		    
		MinionIdentifier id3 = null;
		
		if(simpleExpression.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
			if(simpleExpression.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT) {
				Expression negatedAtomExpression = simpleExpression.getUnaryExpression().getExpression();
				id3 = translateAtomExpression(negatedAtomExpression);	
				id3.setPolarity(0);
				print_debug("We have a negated expression as simple expression:"+simpleExpression);
			}
		}
		else id3 = translateAtomExpression(simpleExpression);	
		
			switch(complexExpression.getRestrictionMode()) {
			    
			case EssenceGlobals.BINARYOP_EXPR:
			    
			    MinionIdentifier[] ids = new MinionIdentifier[2];
			    ids[0] = translateMulopExpression(complexExpression.getBinaryExpression().getLeftExpression());
			    ids[1] = translateMulopExpression(complexExpression.getBinaryExpression().getRightExpression());
			    
			    //MinionIdentifier id3 = translateAtomExpression(simpleExpression);		   

			    switch (complexExpression.getBinaryExpression().getOperator().getRestrictionMode()) {
				
			    case EssenceGlobals.PLUS:
			    	return new MinionSumConstraint(ids,id3, useWatchedLiterals && !reifiable);

				
			    case EssenceGlobals.MINUS:
				// use weighted sum -> first has to be implemented in 
			    	MinionConstant[] consts = new MinionConstant[2];
			    	consts[0] = new MinionConstant(1);
			    	consts[1] = new MinionConstant(-1);		
			    	return new MinionWeightedSumConstraint(ids,consts,id3);

				
			    case EssenceGlobals.MULT:
			    	if(!reifiable)
			    		return new MinionProductConstraint(ids[0], ids[1], id3);
			    	else {
			    		int upperBound = max(new int[] { ids[0].getUpperBound()*ids[1].getUpperBound(),
			    										 ids[0].getLowerBound()*ids[1].getLowerBound(),
			    										 ids[0].getUpperBound()*ids[1].getLowerBound(),
			    										 ids[0].getLowerBound()*ids[1].getUpperBound()}
			  		                          );
			    		int lowerBound = min(new int[] { ids[0].getUpperBound()*ids[1].getUpperBound(),
								 						 ids[0].getLowerBound()*ids[1].getLowerBound(),
								 						 ids[0].getUpperBound()*ids[1].getLowerBound(),
								 						 ids[0].getLowerBound()*ids[1].getUpperBound()}
                       );			    		
			    		MinionBoundsVariable freshVariable = new MinionBoundsVariable(lowerBound,
			    				                                                      upperBound,
			    				                                                      "freshVariable"+noTmpVars++);
			    		minionModel.addProductConstraint(ids[0],ids[1],freshVariable);
			    		return new MinionEqConstraint(freshVariable, id3);
			    	}
			    		
			    		
			    	
			    case EssenceGlobals.DIVIDE: // wait till Chris implements divison constraint
			    	throw new TranslationUnsupportedException
				    ("Translation of division not supported yet: "+simpleExpression.toString()+" = "+complexExpression.toString());
			    	// minionModel.addProductConstraint(ids[1], id3, ids[0]);		       

			    case EssenceGlobals.OR:  // a OR b  ---> reify(max([a,b],1), tmp)
			
			    	MinionBoolVariable reifiedOrVar = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(reifiedOrVar);
					minionVariables.put("freshVariable"+(noTmpVars++), reifiedOrVar);
				
			    	MinionMaxConstraint or_constraint = new MinionMaxConstraint(ids, new MinionConstant(1));			
			    	minionModel.addReificationConstraint(or_constraint, reifiedOrVar);
				
			    	return new MinionEqConstraint(id3, reifiedOrVar);
			 

			    case EssenceGlobals.AND:// a AND b ---> reify( min([a,b], 1), tmp) 
			    	MinionBoolVariable reifiedAndVar = new MinionBoolVariable(1, "freshVariable"+noTmpVars);	 
			    	minionModel.add01Variable(reifiedAndVar);
					minionVariables.put("freshVariable"+(noTmpVars++), reifiedAndVar);
				
			    	MinionMinConstraint and_constraint = new MinionMinConstraint(ids, new MinionConstant(1));
			    	minionModel.addReificationConstraint(and_constraint, reifiedAndVar);
				
			    	return new MinionEqConstraint(id3,reifiedAndVar);
			    	
				
			    case EssenceGlobals.IF:// a => b   --->   a >= b			
			    	MinionInEqConstraint if_constraint = new MinionInEqConstraint(ids[0],ids[1],new MinionConstant(0));
			    	
			    	MinionBoolVariable v = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v);
					minionVariables.put("freshVariable"+(noTmpVars++), v);

			    	minionModel.addReificationConstraint(if_constraint, v);
				
			    	return new MinionEqConstraint(id3,v);
			    

			    case EssenceGlobals.IFF: // a <=> b ---> a = b						
			    	MinionEqConstraint eq_constraint = new MinionEqConstraint(ids[0],ids[1]);
			    	MinionBoolVariable v_iff = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_iff);
					minionVariables.put("freshVariable"+(noTmpVars++), v_iff);

			    	minionModel.addReificationConstraint(eq_constraint, v_iff);
			    	return new MinionEqConstraint(id3,v_iff);	    			
			    	
			    	
			    case EssenceGlobals.EQ: 
			    	MinionEqConstraint eq_constraint1 = new MinionEqConstraint(ids[0],ids[1]);
			    	MinionBoolVariable v_eq = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_eq);
					minionVariables.put("freshVariable"+(noTmpVars++), v_eq);

			    	minionModel.addReificationConstraint(eq_constraint1, v_eq);
			    	return new MinionEqConstraint(id3,v_eq);	    						    	
			    	
			    case EssenceGlobals.NEQ: 
			    	MinionDisEqConstraint neq_constraint = new MinionDisEqConstraint(ids[0],ids[1]);
			    	MinionBoolVariable v_neq = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_neq);
					minionVariables.put("freshVariable"+(noTmpVars++), v_neq);

			    	minionModel.addReificationConstraint(neq_constraint, v_neq);
			    	return new MinionEqConstraint(id3,v_neq);	  
			    	
			    case EssenceGlobals.LEQ: 
			    	MinionInEqConstraint leq_constraint = new MinionInEqConstraint(ids[0],ids[1], new MinionConstant(0));
			    	MinionBoolVariable v_leq = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_leq);
					minionVariables.put("freshVariable"+(noTmpVars++), v_leq);

			    	minionModel.addReificationConstraint(leq_constraint, v_leq);
			    	return new MinionEqConstraint(id3,v_leq);	
			    	
			    case EssenceGlobals.GEQ: 
			    	MinionInEqConstraint geq_constraint = new MinionInEqConstraint(ids[1],ids[0], new MinionConstant(0));
			    	MinionBoolVariable v_geq = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_geq);
					minionVariables.put("freshVariable"+(noTmpVars++), v_geq);

			    	minionModel.addReificationConstraint(geq_constraint, v_geq);
			    	return new MinionEqConstraint(id3,v_geq);	
			    	
			    case EssenceGlobals.LESS: 
			    	MinionInEqConstraint less_constraint = new MinionInEqConstraint(ids[0],ids[1], new MinionConstant(-1));
			    	MinionBoolVariable v_less = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_less);
					minionVariables.put("freshVariable"+(noTmpVars++), v_less);

			    	minionModel.addReificationConstraint(less_constraint, v_less);
			    	return new MinionEqConstraint(id3,v_less);				    	
			    	
			    case EssenceGlobals.GREATER: 
			    	MinionInEqConstraint gr_constraint = new MinionInEqConstraint(ids[1],ids[0], new MinionConstant(-1));
			    	MinionBoolVariable v_gr = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
			    	minionModel.add01Variable(v_gr);
					minionVariables.put("freshVariable"+(noTmpVars++), v_gr);

			    	minionModel.addReificationConstraint(gr_constraint, v_gr);
			    	return new MinionEqConstraint(id3,v_gr);	
			    	
			    default: 
				throw new TranslationUnsupportedException
				    ("Translation of Expression not supported yet: "+simpleExpression.toString()+" = "+complexExpression.toString());
				
			    }
			
			    
			case EssenceGlobals.UNITOP_EXPR:
				//MinionIdentifier simpleIdentifier = translateAtomExpression(simpleExpression);
				MinionIdentifier complexIdentifier = translateUnaryExpression(complexExpression.getUnaryExpression());
			    return new MinionEqConstraint(id3,complexIdentifier);

			default:
			    throw new TranslationUnsupportedException
				("Translation of Expression not supported yet: "+simpleExpression.toString()+" = "+complexExpression.toString());
			    
			}
	}
		
	
	
	  /**
     * Removes the atomic subexpression "true" in conjunctions. Consider
     * for instance that the expression "a \/ true" can be evaluated to "a".
     *  
     * @param e the Expression whose atomic subexpressions are removed
     * @return Expression e without subexpressions "true"
     * @throws PreprocessorException
     */
      protected Expression removeAtomicSubExpressions (Expression e)  {

   	   // TODO: i just commented the following because it caused a null-pointer
   	   //e = evaluator.evalExpression(e);
   	   
   	   switch(e.getRestrictionMode()) {	    

   	   case EssenceGlobals.BINARYOP_EXPR:
   		   Expression left = e.getBinaryExpression().getLeftExpression();
   		   Expression right = e.getBinaryExpression().getRightExpression();
  	    
   		   
   		   if(e.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.AND) {
   			   if(left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
   				   if(left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
   					   if(left.getAtomicExpression().getBool()) 
   						   return removeAtomicSubExpressions(right);
   				   }
   			   } 
   			   else if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
   				   if(right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
   					   if(right.getAtomicExpression().getBool()) 
   						   return removeAtomicSubExpressions(left);			
   				   }
   			   }
   		   }
   		   else if(e.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.IF) {
   			   if(left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
   				   if(left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
   					   if(left.getAtomicExpression().getBool()) 
   						   return removeAtomicSubExpressions(right);
   				   }
   			   } 
   		   }
   		   else return new Expression(new BinaryExpression(removeAtomicSubExpressions(left),
  							 	e.getBinaryExpression().getOperator(), 
  							 	removeAtomicSubExpressions(right)));
  	  
   	   default: 
   		   return e;
   	   }
      	}
      
      
    
      
      /**
       * Translate an expression where its expression tree or both branches of the tree can be
       * mapped top a weithed sum constraint. This method can only be evoked, if the expression
       * is sure to have a mappable structure!!
       * 
       * *  compute the values of the leaf (or multiplication-subtree) of the expression
       *  	structure and store the corresponding values. <br>
       *  Example: (we know that "Right" must contain either an atom like "x" or 
       *                a factorised variable like 2*x) <br>
       *<br>
       *             upperTree <br>
       *                  /    \ <br>
       *              Left     Right <br>
       *<br>
       * 
       * @param be
       * @param willBeReified
       * @return
       * @throws TranslationUnsupportedException
       * @throws MinionException
       */
      protected MinionConstraint translateIteratedArithmeticExpression(BinaryExpression be, boolean willBeReified) 
          throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
  		
    	  // ATOM = sum
    	  if(be.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    		 be.getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
    		  computeIteratedLists(be.getRightExpression(), false);
    		  return translateSum(translateAtomExpression(be.getLeftExpression()),
    				              be.getOperator(),
    				              false, willBeReified); //isResultOnRightSide	  
    	  }
    	  //Neg(ATOM) = sum
    	  else if(isNegatedAtomExpression(be.getLeftExpression())) {
    		computeIteratedLists(be.getRightExpression(),false);
    		MinionIdentifier result = translateMulopExpression(be.getLeftExpression().getUnaryExpression().getExpression());
    		result.setPolarity(0);
  		  	return translateSum(result,
	              be.getOperator(),
	              false, willBeReified); //isResultOnRightSide	
    		  
    	  }
    	  // sum = Neg(ATOM)
    	  else if(isNegatedAtomExpression(be.getRightExpression())) {
      		computeIteratedLists(be.getLeftExpression(),false);
      		MinionIdentifier result = translateMulopExpression(be.getRightExpression().getUnaryExpression().getExpression());
      		result.setPolarity(0);
    		  	return translateSum(result,
  	              be.getOperator(),
  	              true, willBeReified); //isResultOnLeftySide	
      		  
      	  }
    	  
    	  // sum = ATOM
    	  else if(be.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    			  be.getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) { 		
    		     computeIteratedLists(be.getLeftExpression(), false);
    		     return translateSum(translateAtomExpression(be.getRightExpression()),
    		    		 							be.getOperator(),
    		    		 							true, willBeReified); //isResultOnRightSide	    
    	  }
    	  // sum = sum
    	  // // both are scalar product structures, let's translate both 
    	  else if(be.getLeftExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR && 
    			  be.getRightExpression().getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
    		  computeIteratedLists(be.getRightExpression(), false);
    		  MinionIdentifier auxVariable = variableCreator.addFreshVariable(MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND, 
    				                  MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND, 
    				                  "freshVariable"+noTmpVars++, 
    				                  this.useDiscreteVariables);
    		  if(willBeReified) {
    			  MinionConstraint firstSumConstraint = translatePartwiseSum(auxVariable,false, willBeReified);
    			  computeIteratedLists(be.getLeftExpression(), false);
       			  if(be.getOperator().getRestrictionMode() == EssenceGlobals.EQ) {
       	   			  MinionConstraint secondSumConstraint = translateSum(auxVariable,be.getOperator(),true, willBeReified);
        			  MinionIdentifier reifiedVariable1 = variableCreator.addFreshVariable(0,1,
    	                            "freshVariable"+noTmpVars++, this.useDiscreteVariables);
        			  MinionIdentifier reifiedVariable2 = variableCreator.addFreshVariable(0,1,
                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
        			  minionModel.addConstraint(new MinionReifyConstraint((MinionReifiableConstraint) firstSumConstraint, reifiedVariable1));
        			  minionModel.addConstraint(new MinionReifyConstraint((MinionReifiableConstraint) secondSumConstraint, reifiedVariable2));
        			  return new MinionSumConstraint(new MinionIdentifier[] {reifiedVariable1, reifiedVariable2}, 
        					                        new MinionConstant(2),
        					                        this.useWatchedLiterals);         
       			  }
       			  else {
					  MinionIdentifier auxVariable2 = variableCreator.addFreshVariable(MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND, 
			                  MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND, 
			                  "freshVariable"+noTmpVars++, 
			                  this.useDiscreteVariables);
       				MinionConstraint secondSumConstraint = translatePartwiseSum(auxVariable2,true, willBeReified);
  					minionModel.addConstraint(firstSumConstraint);
   					minionModel.addConstraint(secondSumConstraint);
       				switch(be.getOperator().getRestrictionMode()) {
       				case EssenceGlobals.GEQ:
       					return new MinionInEqConstraint(auxVariable2,auxVariable,new MinionConstant(0));
       				case EssenceGlobals.LEQ:
       					return new MinionInEqConstraint(auxVariable,auxVariable2,new MinionConstant(0));
       				case EssenceGlobals.GREATER:
       					return new MinionInEqConstraint(auxVariable2,auxVariable,new MinionConstant(-1));
       				case EssenceGlobals.LESS:
       					return new MinionInEqConstraint(auxVariable,auxVariable2,new MinionConstant(-1));
       				case EssenceGlobals.NEQ:
       					return new MinionDisEqConstraint(auxVariable,auxVariable2);
       				default: throw new TranslationUnsupportedException
				  	("Cannot apply operator "+be.getOperator().toString()+
				  			" on to arithmetic expressions. Expected a relational operator (=,!=, >,>=,<,<=). "); 
       				}
       			  }
 
    		  }
    		  else {
    			  print_debug("Translating DOUBLE scalar thing without needing tp reify it:"+be.toString());
    			  this.minionModel.addConstraint(translatePartwiseSum(auxVariable,false,willBeReified));
    			  computeIteratedLists(be.getLeftExpression(), false);
    			  if(be.getOperator().getRestrictionMode() == EssenceGlobals.EQ) 
        			  return translatePartwiseSum(auxVariable,true,willBeReified);
    			  
    			  else {
					  MinionIdentifier auxVariable2 = variableCreator.addFreshVariable(MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND, 
			                  MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND, 
			                  "freshVariable"+noTmpVars++, 
			                  this.useDiscreteVariables);
					  this.minionModel.addConstraint(translatePartwiseSum(auxVariable2, true, willBeReified));
    					  
    				  switch(be.getOperator().getRestrictionMode()) {
    				  case EssenceGlobals.NEQ:
    					  return new MinionDisEqConstraint(auxVariable, auxVariable2);
    				  case EssenceGlobals.GEQ:
    					  return new MinionInEqConstraint(auxVariable2,auxVariable,new MinionConstant(0));
    				  case EssenceGlobals.LEQ:
    					  return new MinionInEqConstraint(auxVariable, auxVariable2, new MinionConstant(0));
    				  case EssenceGlobals.GREATER:
    					  return new MinionInEqConstraint(auxVariable2,auxVariable,new MinionConstant(-1));
    				  case EssenceGlobals.LESS:
    					  return new MinionInEqConstraint(auxVariable,auxVariable2,new MinionConstant(-1));
    				  default: throw new TranslationUnsupportedException
    				  	("Cannot apply operator "+be.getOperator().toString()+
    				  			" on to arithmetic expressions. Expected a relational operator (=,!=, >,>=,<,<=). "); 
    				  }
    			  }
    			  
    		  }
    	  }
    	  
  		return null;
  	}
  	
      /**
       * Translate a sum from the variables (and possibly constants) from the iterated sum lists,
       * and related by the operator relop. 
       * @param result
     * @param relop
     * @param isResultOnRightSide TODO
     * @param willBeReified TODO
     * @return
       */
      private MinionConstraint translateSum(MinionIdentifier result, BinaryOperator relop, boolean isResultOnRightSide, boolean willBeReified) 
      	throws TranslationUnsupportedException, MinionException {
    	  
    	  print_debug("translating sum with result:"+result.toString());
			  Object[] variableBuffer = iteratedVariablesList.toArray();
   			  MinionIdentifier[] variables = new MinionIdentifier[variableBuffer.length];	
   			  for(int i=0; i<variableBuffer.length; i++)
   				 variables[i] = (MinionIdentifier) variableBuffer[i];  
    	  
    	  
   		  if(iteratedConstantsList.size() == 0) {	  
   			  iteratedVariablesList.clear();
   			  print_debug("no constants, got a sum here.");
   			  switch(relop.getRestrictionMode()) {
   			  
   			  case EssenceGlobals.GEQ:
   				  if(isResultOnRightSide)
   					  return new MinionSumGeqConstraint(variables, result, this.useWatchedLiterals);
   				  else return new MinionSumLeqConstraint(variables, result, this.useWatchedLiterals);
   			  case EssenceGlobals.LEQ:
   				  if(isResultOnRightSide)
   					  return new MinionSumLeqConstraint(variables, result, this.useWatchedLiterals);
   				  else return new MinionSumGeqConstraint(variables, result, this.useWatchedLiterals);
   			  case EssenceGlobals.EQ:
   				return new MinionSumConstraint(variables, result, this.useWatchedLiterals);
   			  case EssenceGlobals.NEQ:
   				  int lowerBound = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
   				  int upperBound = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
   				  MinionIdentifier auxVariable = variableCreator.addFreshVariable(lowerBound, upperBound,
   						                                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   				  MinionSumConstraint sumConstraint = new MinionSumConstraint(variables,auxVariable, this.useWatchedLiterals);
   				  minionModel.addConstraint(sumConstraint);
   				  return new MinionDisEqConstraint(auxVariable, result);
   			  case EssenceGlobals.GREATER:
   				  int lowerB = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
   				  int upperB = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
   				  MinionIdentifier auxVar = variableCreator.addFreshVariable(lowerB, upperB,
   						                                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   				  MinionSumConstraint greaterConstraint = new MinionSumConstraint(variables,auxVar, this.useWatchedLiterals);	  
   				  
   				  if(!willBeReified) {
   					minionModel.addConstraint(greaterConstraint);
   					  if(isResultOnRightSide) // result <= ausVar -1
   						  return new MinionInEqConstraint(result, auxVar, new MinionConstant(-1));
   					  else return new MinionInEqConstraint(auxVar, result, new MinionConstant(-1));
   				  }
   				  else {
   					  MinionIdentifier reifiedVariable1 = variableCreator.addFreshVariable(0,1,
   							                            "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   					  MinionReifyConstraint reifiedSum = new MinionReifyConstraint(greaterConstraint,reifiedVariable1);
   					  MinionInEqConstraint ineqConstraint = null;
  					  if(isResultOnRightSide) // result <= ausVar -1
   						   ineqConstraint = new MinionInEqConstraint(result, auxVar, new MinionConstant(-1));
   					  else ineqConstraint = new MinionInEqConstraint(auxVar, result, new MinionConstant(-1));
  					  
  					  MinionIdentifier reifiedVariable2 = variableCreator.addFreshVariable(0,1,
	                            "freshVariable"+noTmpVars++, this.useDiscreteVariables);
  					  MinionReifyConstraint reifiedInEq = new MinionReifyConstraint(ineqConstraint, reifiedVariable2);
  					  
  					  this.minionModel.addConstraint(reifiedSum);
  					  this.minionModel.addConstraint(reifiedInEq);
  					  return new MinionSumConstraint(new MinionIdentifier[] {reifiedVariable1, reifiedVariable2}, 
  							                         new MinionConstant(2),
  							                         this.useWatchedLiterals);
   				  }
   			  case EssenceGlobals.LESS:
  				  int lbLess = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
   				  int ubLess = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
   				  MinionIdentifier auxVariableLess = variableCreator.addFreshVariable(lbLess, ubLess,
   						                                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   				  MinionSumConstraint lessSumConstraint = new MinionSumConstraint(variables,auxVariableLess, this.useWatchedLiterals);
   				  
  				  if(!willBeReified) {
     					minionModel.addConstraint(lessSumConstraint);
     					  if(isResultOnRightSide) // auxVar <= result -1
     						  return new MinionInEqConstraint(auxVariableLess,result, new MinionConstant(-1));
     					  else return new MinionInEqConstraint(result, auxVariableLess, new MinionConstant(-1));
     				  }
     				  else {
     					  MinionIdentifier reifiedVariable1 = variableCreator.addFreshVariable(0,1,
     							                            "freshVariable"+noTmpVars++, this.useDiscreteVariables);
     					  MinionReifyConstraint reifiedSum = new MinionReifyConstraint(lessSumConstraint,reifiedVariable1);
     					  MinionInEqConstraint ineqConstraint = null;
    					  if(isResultOnRightSide) // result <= ausVar -1
     						   ineqConstraint = new MinionInEqConstraint(auxVariableLess,result, new MinionConstant(-1));
     					  else ineqConstraint = new MinionInEqConstraint(result, auxVariableLess, new MinionConstant(-1));
    					  
    					  MinionIdentifier reifiedVariable2 = variableCreator.addFreshVariable(0,1,
  	                            "freshVariable"+noTmpVars++, this.useDiscreteVariables);
    					  MinionReifyConstraint reifiedInEq = new MinionReifyConstraint(ineqConstraint, reifiedVariable2);
    					  
    					  this.minionModel.addConstraint(reifiedSum);
    					  this.minionModel.addConstraint(reifiedInEq);
    					  return new MinionSumConstraint(new MinionIdentifier[] {reifiedVariable1, reifiedVariable2}, 
    							                         new MinionConstant(2),
    							                         this.useWatchedLiterals);
     				  }
   			  
   			default:
   					throw new TranslationUnsupportedException
   					("Illegal operator '"+relop+"' on arithmetic expressions in on :"+result.getOriginalName()
   							   +". Expected a relational operator (=,>,<,>=,<=,!=).");
   			  }
   		  }
			  
   		  // we have to translate weighted sums
   		  if(willBeReified)
   			  throw new TranslationUnsupportedException
   			  ("Internal error. Try to produce weighted sum constraint even though it is not reifiable. Associated variables:"
   					 +"result: " +result.getOriginalName()+", variables: "+iteratedVariablesList.toString()+", constants: "
   					 +iteratedConstantsList.toString());
   		  
   		  print_debug("before weighted sum translation, before adjustment: iteratedConstants "+iteratedConstantsList.toString()+", variables:"
 				  +iteratedVariablesList.toString());
   		  
   		  if(iteratedConstantsList.size() < iteratedVariablesList.size()) {
   			  int difference = iteratedVariablesList.size()-iteratedConstantsList.size();
   			  print_debug("before weighted sum translation, before adjustment: iteratedConstants "+iteratedConstantsList.toString()+", variables:"
   				  +iteratedVariablesList.toString()+" with difference:"+difference);
			  for(int i=0; i<difference; i++)
				  iteratedConstantsList.add(new MinionConstant(1));
   		  }
   		  // translate weighted sums
   		  print_debug("before weighted sum translation: iteratedConstants "+iteratedConstantsList.toString()+", variables:"
   				  +iteratedVariablesList.toString());
   		 
		  Object[] constantBuffer = iteratedConstantsList.toArray();
		  MinionConstant[] constants = new MinionConstant[constantBuffer.length];	
			  for(int i=0; i<constantBuffer.length; i++)
				 constants[i] = (MinionConstant) constantBuffer[i];  
   		  
   		  
   		  iteratedVariablesList.clear();
   		  iteratedConstantsList.clear();
   		  
 			  switch(relop.getRestrictionMode()) {
   			  
   			  case EssenceGlobals.GEQ:
   				  if(isResultOnRightSide)
   					  return new MinionWeightedSumGeqConstraint(variables,constants, result);
   				  else return new MinionWeightedSumLeqConstraint(variables, constants, result);
   			  case EssenceGlobals.LEQ:
   				  if(isResultOnRightSide)
   					  return new MinionWeightedSumLeqConstraint(variables,constants, result);
   				  else return new MinionWeightedSumGeqConstraint(variables,constants, result);
   			  case EssenceGlobals.EQ:
   				return new MinionWeightedSumConstraint(variables, constants, result);
   				
   			  case EssenceGlobals.NEQ:
   				  int lowerBound = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
   				  int upperBound = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
   				  MinionIdentifier auxVariable = variableCreator.addFreshVariable(lowerBound, upperBound,
   						                                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   				  MinionWeightedSumConstraint sumConstraint = new MinionWeightedSumConstraint(variables,constants, auxVariable);
   				  minionModel.addConstraint(sumConstraint);
   				  return new MinionDisEqConstraint(auxVariable, result);
   				  
   			  case EssenceGlobals.GREATER:
   				  int lowerB = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
   				  int upperB = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
   				  MinionIdentifier auxVar = variableCreator.addFreshVariable(lowerB, upperB,
   						                                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   				  MinionWeightedSumConstraint greaterConstraint = new MinionWeightedSumConstraint(variables,constants, auxVar);	  
		  
   				  minionModel.addConstraint(greaterConstraint);
   				  if(isResultOnRightSide) // result <= ausVar -1
   					  return new MinionInEqConstraint(result, auxVar, new MinionConstant(-1));
   				  else return new MinionInEqConstraint(auxVar, result, new MinionConstant(-1));
   				  
   			  case EssenceGlobals.LESS:
  				  int lbLess = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
   				  int ubLess = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
   				  MinionIdentifier auxVariableLess = variableCreator.addFreshVariable(lbLess, ubLess,
   						                                              "freshVariable"+noTmpVars++, this.useDiscreteVariables);
   				  MinionWeightedSumConstraint lessSumConstraint = new MinionWeightedSumConstraint(variables,constants, auxVariableLess);
  
   				  minionModel.addConstraint(lessSumConstraint);
   				  	if(isResultOnRightSide) // auxVar <= result -1
   				  		return new MinionInEqConstraint(auxVariableLess,result, new MinionConstant(-1));
   				  	else return new MinionInEqConstraint(result, auxVariableLess, new MinionConstant(-1));
     			
   			default:
   					throw new TranslationUnsupportedException
   					("Illegal operator '"+relop+"' on arithmetic expressions in on :"+result.getOriginalName()
   							   +". Expected a relational operator (=,>,<,>=,<=,!=).");
   			  }
 
    	  
      }
      
      
      /**
       * Translate a sum from the variables (and possibly constants) from the iterated sum lists,
       * and related by the operator relop. 
       * @param result
     * @param isResultOnRightSide TODO
     * @param willBeReified TODO
     * @return
       */
      private MinionConstraint translatePartwiseSum(MinionIdentifier result, boolean isResultOnRightSide, 
    		  		boolean willBeReified) 
      	throws TranslationUnsupportedException, MinionException {
    	  
    	  print_debug("translating sum with result:"+result.toString());
			  Object[] variableBuffer = iteratedVariablesList.toArray();
   			  MinionIdentifier[] variables = new MinionIdentifier[variableBuffer.length];	
   			  for(int i=0; i<variableBuffer.length; i++)
   				 variables[i] = (MinionIdentifier) variableBuffer[i];  
    	  
    	  
   		  if(iteratedConstantsList.size() == 0) {	  
   			  iteratedVariablesList.clear();
   			  print_debug("no constants, got a sum here.");
   			  
   				  if(isResultOnRightSide)
   					  return new MinionSumConstraint(variables, result, this.useWatchedLiterals);
   				  else return new MinionSumConstraint(variables, result, this.useWatchedLiterals);
   			
   		  }
			  
   		  // we have to translate weighted sums
   		  if(willBeReified)
   			  throw new TranslationUnsupportedException
   			  ("Internal error. Try to produce weighted sum constraint even though it is not reifiable. Associated variables:"
   					 +"result: " +result.getOriginalName()+", variables: "+iteratedVariablesList.toString()+", constants: "
   					 +iteratedConstantsList.toString());
   		  
   		  print_debug("before weighted sum translation, before adjustment: iteratedConstants "+iteratedConstantsList.toString()+", variables:"
 				  +iteratedVariablesList.toString());
   		  
   		  if(iteratedConstantsList.size() < iteratedVariablesList.size()) {
   			  int difference = iteratedVariablesList.size()-iteratedConstantsList.size();
   			  print_debug("before weighted sum translation, before adjustment: iteratedConstants "+iteratedConstantsList.toString()+", variables:"
   				  +iteratedVariablesList.toString()+" with difference:"+difference);
			  for(int i=0; i<difference; i++)
				  iteratedConstantsList.add(new MinionConstant(1));
   		  }
   		  
   		  // translate weighted sums
   		  print_debug("before weighted sum translation: iteratedConstants "+iteratedConstantsList.toString()+", variables:"
   				  +iteratedVariablesList.toString());
   		 
		  Object[] constantBuffer = iteratedConstantsList.toArray();
		  MinionConstant[] constants = new MinionConstant[constantBuffer.length];	
			  for(int i=0; i<constantBuffer.length; i++)
				 constants[i] = (MinionConstant) constantBuffer[i];  
   		  
   		  
   		  iteratedVariablesList.clear();
   		  iteratedConstantsList.clear();
   		    
   		  if(isResultOnRightSide)
   			  return new MinionWeightedSumGeqConstraint(variables,constants, result);
   		  else return new MinionWeightedSumLeqConstraint(variables, constants, result);
   		
      }
      
      
      /**
       * Compute the list of variables and constants for the (weighted) sum.
       * The lists are stored in the globals iteratedVariablesList for variables
       * and iteratedConstantsList for constants. 
       * @param e
       * @param isNegativeExpression is true, if e has been encapsulated by a MINUS
       * @throws TranslationUnsupportedException
       * @throws MinionException
       */
      private void computeIteratedLists(Expression e, boolean isNegativeExpression) 
          throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
    	
    	  switch(e.getRestrictionMode()) {
    	  
    	  case EssenceGlobals.ATOMIC_EXPR:
    		  AtomicExpression atom = e.getAtomicExpression();
    		  if(atom.getRestrictionMode() == EssenceGlobals.NUMBER) 
    			  iteratedVariablesList.add(new MinionConstant(atom.getNumber()));
    		  else if(atom.getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
    			  if(!decisionVariablesNames.contains(atom.getString()))
    				  throw new TranslationUnsupportedException("Unknown variable in expression: "+e.toString());
    			  iteratedVariablesList.add(translateAtomicExpression(atom));
    		  }
    		  else if(atom.getRestrictionMode() == EssenceGlobals.BOOLEAN) 
    			  iteratedVariablesList.add(new MinionConstant(atom.getBool() ? 1 : 0));
    		  
    		  else throw new TranslationUnsupportedException("Unknown atom type: "+atom.toString());
    		  break;
    		  
    	  case EssenceGlobals.NONATOMIC_EXPR:
    		  iteratedVariablesList.add(translateNonAtomicExpression(e.getNonAtomicExpression()));
    		  break;
    		  
    	  case EssenceGlobals.UNITOP_EXPR:
    		  print_debug("We have a negated expression:"+e.toString());
    		  MinionIdentifier identifier = translateMulopExpression(e.getUnaryExpression().getExpression());
    		  identifier.setPolarity(0);
    		  print_debug("This is the identifer that SHOULD be negated:"+identifier.toString());
    		  iteratedVariablesList.add(identifier);
    		  break;
    		  
    	  case EssenceGlobals.BINARYOP_EXPR:
    		  BinaryExpression be = e.getBinaryExpression();
    		  // left(branch) + right(leaf)
    		  if(be.getOperator().getRestrictionMode() == EssenceGlobals.PLUS) {
    			  computeIteratedLists(be.getRightExpression(), false);
    			  computeIteratedLists(be.getLeftExpression(), false);
    		  }
    		  // left(branch) - right(leaf)
    		  else if(be.getOperator().getRestrictionMode() == EssenceGlobals.MINUS) {
    			  // if it is an atom, just add a negative constant to constant list
    			  if(be.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR || 
    					  be.getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR ||
    					     isNegatedAtomExpression(be.getRightExpression())) {
    				  if(iteratedConstantsList.size() < iteratedVariablesList.size()) {
						  for(int i=0; i<iteratedVariablesList.size()-iteratedConstantsList.size(); i++)
							  iteratedConstantsList.add(new MinionConstant(1));
					  }
    				  iteratedConstantsList.add(new MinionConstant(-1));
    				  print_debug("added -1 for the minus. New constant list:"+iteratedConstantsList.toString());
    				  computeIteratedLists(be.getRightExpression(), false);
    			  }
    			  else computeIteratedLists(be.getRightExpression(), true);
    			  
    			  computeIteratedLists(be.getLeftExpression(), false);
    		  }
    		  // left(atom) * right(atom)
    		  else if(be.getOperator().getRestrictionMode() == EssenceGlobals.MULT) {
    			  Expression left = be.getLeftExpression();
    			  Expression right = be.getRightExpression();
    			  
    			  
    			  // we first add/translate the left part
    			  if(left.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) 
    				  iteratedVariablesList.add(translateNonAtomicExpression(left.getNonAtomicExpression()));
    			  else if(left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
    				  switch(left.getAtomicExpression().getRestrictionMode()) {
    				  // number
    				  case EssenceGlobals.NUMBER:
    					  int constantValue = left.getAtomicExpression().getNumber();
    					  MinionConstant constant = new MinionConstant(isNegativeExpression ? (-constantValue) : constantValue);
    					  
    					  if(iteratedConstantsList.size() < iteratedVariablesList.size()) {
    						  for(int i=0; i<iteratedVariablesList.size()-iteratedConstantsList.size(); i++)
    							  iteratedConstantsList.add(new MinionConstant(1));
    					  }
    					  iteratedConstantsList.add(constant);
    					  break;
    				  case EssenceGlobals.BOOLEAN:
    					  int boolValue = left.getAtomicExpression().getBool() ? 1 : 0;
    					  MinionConstant boolConstant = new MinionConstant(isNegativeExpression ? -boolValue : boolValue);
    					  
    					  if(iteratedConstantsList.size() < iteratedVariablesList.size()) {
    						  for(int i=0; i<iteratedVariablesList.size()-iteratedConstantsList.size(); i++)
    							  iteratedConstantsList.add(new MinionConstant(1));
    					  }
    					  iteratedConstantsList.add(boolConstant);
    					  break;
    				  case EssenceGlobals.IDENTIFIER:
    					  AtomicExpression leftAtom = left.getAtomicExpression();
    		   			  if(!decisionVariablesNames.contains(leftAtom.getString()))
    	    				  throw new TranslationUnsupportedException("Unknown variable: "+e.toString());
    	    			  iteratedVariablesList.add(translateAtomicExpression(leftAtom));
    	    			  break;
    	    			  
    				  default : throw new TranslationUnsupportedException
		              ("Internal error. Expected arithmetic sum structure instead of:"+e.toString());
    				  }
    			  }
    			  else throw new TranslationUnsupportedException
	              ("Internal error. Expected atom instead of:"+e.toString());	  
    			  
    			  
    			  
    			  // same for right part
      			  if(right.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) 
    				  iteratedVariablesList.add(translateNonAtomicExpression(right.getNonAtomicExpression()));
    			  else if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
    				  switch(right.getAtomicExpression().getRestrictionMode()) {
    				  // number
    				  case EssenceGlobals.NUMBER:
    					  int constantValue = right.getAtomicExpression().getNumber();
    					  MinionConstant constant = new MinionConstant(isNegativeExpression ? (-constantValue) : constantValue);
    					  print_debug("before adding right part of mult: "+iteratedConstantsList.toString());
    					  // we could not have had a number in the left part, because it would have been evaluated to a single number
    					  if(iteratedConstantsList.size() <= iteratedVariablesList.size()+1) {
    						  for(int i=0; i<iteratedVariablesList.size()-iteratedConstantsList.size()-1; i++)
    							  iteratedConstantsList.add(new MinionConstant(1));
    					  }
    					  
    					  iteratedConstantsList.add(constant);
    					  print_debug("after adding right part of mult: "+iteratedConstantsList.toString());
    					  break;
    				  case EssenceGlobals.BOOLEAN:
       					  int boolValue = right.getAtomicExpression().getBool() ? 1 : 0;
    					  MinionConstant boolConstant = new MinionConstant(isNegativeExpression ? -boolValue : boolValue);
    					  
    					  if(iteratedConstantsList.size() <= iteratedVariablesList.size()+1) {
    						  for(int i=0; i<iteratedVariablesList.size()-iteratedConstantsList.size()-1; i++)
    							  iteratedConstantsList.add(new MinionConstant(1));
    					  }
    					  iteratedConstantsList.add(boolConstant);
    					  break;
    				  case EssenceGlobals.IDENTIFIER:
    					  AtomicExpression rightAtom = right.getAtomicExpression();
    		   			  if(!decisionVariablesNames.contains(rightAtom.getString()))
    	    				  throw new TranslationUnsupportedException("Unknown variable: "+e.toString());
    	    			  iteratedVariablesList.add(translateAtomicExpression(rightAtom));
    	    			  break;
    	    			  
    				  default : throw new TranslationUnsupportedException
		              ("Internal error. Expected arithmetic sum structure instead of:"+e.toString());
    				  }
    			  }
    			  else throw new TranslationUnsupportedException
	              ("Internal error. Expected atom instead of:"+e.toString());	
    			  
    		  }
    	  }
    	  
    	  
      }
     
      
      private boolean isNegatedAtomExpression(Expression expression) {
    	  
    	  if(expression.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
    		  if(expression.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT) {
    			  Expression negatedExpression = expression.getUnaryExpression().getExpression(); 
    			  print_debug("It is a not("+negatedExpression+")");
    			  if(negatedExpression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    			     negatedExpression.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
    				  return true;
    		  }
    	  }
    	  
    	  return false;
      }
      
      /**
       * returns true, if the expression has a structure that can be mapped to a 
       * sum or weighted sum constraint.
       * @param e
     * @param willBeReified TODO
       * @return
       */
      protected boolean isIteratedArithmeticExpression(Expression e, boolean willBeReified) {
    	  
    	  if(e.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR)
    		  return false;
    	  
    	  Expression left = e.getBinaryExpression().getLeftExpression();
    	  Expression right = e.getBinaryExpression().getRightExpression();
    	  
    	  if(left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    		left.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR ||
    	     isNegatedAtomExpression(left)) {
    		  print_debug("left part is an atom, what is right:"+right.toString()+", we need a binary expr.");
    		  if(right.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
    			  print_debug("THis might be a iterated sum:"+e.toString());
    			  return isIteratedArithmeticSubExpression(right.getBinaryExpression(), willBeReified);
    		  }
    		  else return false;
    	  }
    	  else if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    			  right.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR ||
    			  isNegatedAtomExpression(right)) {
    		  print_debug("right part is an atom, what is left:"+left.toString()+", we need a binary expr.");
    		  if(left.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR)
    			  return isIteratedArithmeticSubExpression(left.getBinaryExpression(), willBeReified);
    		  else return false;
    	  }
    	  else if(left.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR && 
    			  right.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
    		  
    		  print_debug("both parts are binary expressions: left"+left+", right:"+right);
    		  int operator = e.getBinaryExpression().getOperator().getRestrictionMode();	
    		  if(operator == EssenceGlobals.AND || operator == EssenceGlobals.OR ||
    			  operator == EssenceGlobals.IF || operator == EssenceGlobals.IFF)
    			  willBeReified = true;
    		  
    		  boolean lefto = isIteratedArithmeticSubExpression(left.getBinaryExpression(), willBeReified); 
    		  print_debug("Left part is scalarlike: "+lefto);
    		  boolean righto = isIteratedArithmeticSubExpression(right.getBinaryExpression(), willBeReified);
    		  print_debug("Right part is scalarlike: "+righto+", left part: "+lefto);
    		  return lefto && righto;
    	  }
    	  else return false;
      }
      
      
      /**
       * We know how the syntax tree of the expressions looks like:
       * operators on the left side, atoms on the right side.
       * Right branch may only consist of an atom or an atoms linked
       * by multiplication where at most 1 element is a decision variable. 
       * 
       * @param be right (or left) branch of the expression tree. 
     * @param willBeReified TODO
       * @return true, if the branch has a structure that can be mapped to
       * a weighted sum constraint
       */
      private boolean isIteratedArithmeticSubExpression(BinaryExpression be, boolean willBeReified) {
    	  
	  int operator = be.getOperator().getRestrictionMode();
	  if(!(operator == EssenceGlobals.PLUS ||
	      operator == EssenceGlobals.MINUS ||
	       operator == EssenceGlobals.MULT))
	      return false;
    	  
    	  Expression right = be.getRightExpression();
    	  Expression left = be.getLeftExpression();
    	  print_debug("Checking if is scalar. left:"+left.toString()+" and right:"+right.toString());
    	  
    	  switch(left.getRestrictionMode()) {
    	  
    	  case EssenceGlobals.BINARYOP_EXPR:
    		  if(left.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.PLUS ||
    		     left.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.MINUS) {
    			  boolean rightp = isRightIteratedSubExpression(right);
    			  boolean leftp = isIteratedArithmeticSubExpression(left.getBinaryExpression(), willBeReified);
    			  print_debug("The right part "+right+" is scalar:"+rightp);
				  print_debug("The left part "+left+" is scalar:"+leftp);
    			  return leftp && rightp;
    			 // return isIteratedArithmeticSubExpression(left.getBinaryExpression(), willBeReified) && 
    			 //        isRightIteratedSubExpression(right);
    		  }
    	  
    		  else if(left.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.MULT) {
    			  if(willBeReified) {
    				  print_debug("it will be reified, AAAIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII:"+be.toString());
    				  return false; // we cannot reify weighted sums
    			  }
    			  else {
    				  print_debug("The right part "+right+" is scalar:"+isRightIteratedSubExpression(right));
    				  print_debug("The left part "+left+" is scalar:"+isRightIteratedSubExpression(left));
    				  return isRightIteratedSubExpression(right) && isRightIteratedSubExpression(left);
    			  }
    		  }
    		  else return false;
    	  
    	  case EssenceGlobals.ATOMIC_EXPR:
	      if(operator == EssenceGlobals.MULT) {
		  if(left.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) 
		      return true;
		  else { 
		      if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ) {
			  if(right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)
			      return true;
		      }
		      return false;
		  }
	      }

	      else return true;
    		
    	  case EssenceGlobals.NONATOMIC_EXPR:
	      if(operator == EssenceGlobals.MULT) {
		  if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ) 
		      if(right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)
			  return true;
		  
		  return false;
	      }
	      else return true;
	      
    	  
	      
    	  case EssenceGlobals.UNITOP_EXPR:
    		  print_debug("Got a unit-op here:"+left.toString());
    		  if(isNegatedAtomExpression(left)) {
    			  print_debug("It is a negated atom:"+left.toString());
    			  if(left.getUnaryExpression().getExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
    				  AtomicExpression leftE = left.getUnaryExpression().getExpression().getAtomicExpression();
    			      if(operator == EssenceGlobals.MULT) {
    					  if(leftE.getRestrictionMode() == EssenceGlobals.NUMBER) 
    					      return true;
    					  else { 
    					      if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ) {
    						  if(right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)
    						      return true;
    					      }
    					      return false;
    					  }
    				  }
    			      else {
    			    	  print_debug("This part really is scalar:"+left.toString()+" and its negated part:"+leftE.toString());
    			    	  return true;
    			      }
    			  }
    			  
    			  else if(left.getUnaryExpression().getExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
    			      if(operator == EssenceGlobals.MULT) {
    					  if(right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ) 
    					      if(right.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)
    						  return true;
    					  
    					  return false;
    			      }
    				  else return true;
    			  }
    		  }
    		  return false;
    		  
    	  default: 
	      return false;
    	  }   	  
    	 
      }
      
      
      

      /**
       * We inspect the right branch of a subtree, that may only consist of an 
       * atom or a multiplication between two atoms where one has to be a number 
       * or boolean.
       * @param e
       * @return 
       */
      private boolean isRightIteratedSubExpression(Expression e) {
    	  
    	  switch(e.getRestrictionMode()) {
    	  
    	  case EssenceGlobals.ATOMIC_EXPR:
    		  return true;
    		  
    	  case EssenceGlobals.NONATOMIC_EXPR:
    		  return true;
    	  
    	  case EssenceGlobals.UNITOP_EXPR:
    		  if(isNegatedAtomExpression(e))
    			  return true;
    		  else return false;
    		  
    		  
    	  case EssenceGlobals.BINARYOP_EXPR:
    		  boolean thereIsAnumber = false;
    		  if(e.getBinaryExpression().getOperator().getRestrictionMode() != EssenceGlobals.MULT)
    			  return false;
    		  
     		  if(e.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
     			  if(e.getBinaryExpression().getLeftExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER || 
     				 e.getBinaryExpression().getLeftExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)
     				 thereIsAnumber = true;
     			  else thereIsAnumber = false;	  
     		  }			  
     		  else if(e.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
     			  thereIsAnumber = false;
         	  } 
     		  else return false;
    		  
     		  
    		  if(e.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
    			  if(e.getBinaryExpression().getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER || 
    	     		 e.getBinaryExpression().getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)
    	     			 thereIsAnumber = true;    
    		  }
    		  else if(e.getBinaryExpression().getRightExpression().getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) 
    		    return false;
    		  
    		  if(thereIsAnumber) return true; 
    		  else return false;
    		  
    	  default:
    		  return false;
    	  
    	  }
    	  
    	  
      }
      
      
      
      protected MinionConstraint translateBooleanNegationToConstraint(UnaryExpression e, boolean willBeReified)
		throws TranslationUnsupportedException, PreprocessorException, 
		MinionException, ClassNotFoundException {
		
		Expression negatedExpression = e.getExpression();
		
		if(negatedExpression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
				negatedExpression.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
			MinionIdentifier identifier = translateAtomExpression(negatedExpression);
			if(!Class.forName ("minionModel.MinionBoolVariable").isInstance (identifier))
				throw new TranslationUnsupportedException("Cannot negate a non-boolean expression:"+negatedExpression.toString());
			
			return new MinionEqConstraint(identifier,new MinionConstant(0)); 
		}
		
		else if(negatedExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			int operator = negatedExpression.getBinaryExpression().getOperator().getRestrictionMode();
			if(operator == EssenceGlobals.PLUS || 
					operator == EssenceGlobals.MINUS ||
					operator == EssenceGlobals.MULT ||
					operator == EssenceGlobals.DIVIDE ||
					operator == EssenceGlobals.AND ||
					operator == EssenceGlobals.OR ||
					operator == EssenceGlobals.IF ||
					operator == EssenceGlobals.IFF) {
				MinionIdentifier identifier = translateMulopExpression(negatedExpression);
				if(!Class.forName ("minionModel.MinionBoolVariable").isInstance (identifier))
					throw new TranslationUnsupportedException("Cannot negate a non-boolean expression:"+negatedExpression.toString());
				
				MinionBoolVariable negatedIdentifier = (MinionBoolVariable) identifier;
				negatedIdentifier.setPolarity(0);
				return new MinionEqConstraint(negatedIdentifier,new MinionConstant(0)); 
			}
			else if (operator == EssenceGlobals.EQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.NEQ));
				return translateSpecialExpression(negatedExpression, willBeReified);
			}
			else if (operator == EssenceGlobals.NEQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.EQ));
				return translateSpecialExpression(negatedExpression, willBeReified);
			}
			else if (operator == EssenceGlobals.GEQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.LESS));
				return translateSpecialExpression(negatedExpression, willBeReified);
			}
			else if (operator == EssenceGlobals.LEQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.GREATER));
				return translateSpecialExpression(negatedExpression, willBeReified);
			}
			else if (operator == EssenceGlobals.LESS) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.GREATER));
				return translateSpecialExpression(negatedExpression, willBeReified);
			}
			else if (operator == EssenceGlobals.GREATER) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.LEQ));
				return translateSpecialExpression(negatedExpression, willBeReified);
			}
			else throw new TranslationUnsupportedException
				("Unknown Operator in negated expression:"+negatedExpression.toString());
			
		}
		else if (negatedExpression.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
			
			if(negatedExpression.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT)
				return translateSpecialExpression(negatedExpression.getUnaryExpression().getExpression(),willBeReified);
			
			else {
				throw new TranslationUnsupportedException
				("Cannot translate negated special subexpression yet:"+e.toString());
			}
		}
		
/*		else if(negatedExpression.getRestrictionMode() == EssenceGlobals.ALLDIFF || 
				negatedExpression.getRestrictionMode() == EssenceGlobals.ELEMENT) {
				
				MinionReifiableConstraint constraint = translateGlobalConstraint(negatedExpression);
				MinionBoolVariable reifiedVariable = (MinionBoolVariable) 
					           variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, useDiscreteVariables);
				reifiedVariable.setPolarity(0);
				minionModel.addReificationConstraint(constraint, reifiedVariable);
				TODO: don't know what to do here? Does this case even occur?

		}*/
		
		else throw new TranslationUnsupportedException
		("Cannot translate negated special subexpression yet:"+e.toString());
	}
	
	
      
      /**
       * Return the maximum integer value of an array of integer values
       * 
       * @param values
       * @return the maximum of the integers specified in values
       */
      
   public int max(int[] values) {
	   
	   int currentMax = values[0];
	   
	   for(int i=0; i<values.length; i++) {
		   if(values[i] >= currentMax)
			   currentMax = values[i];
	   }
	   
	   return currentMax;
   }
	
   
   /**
    * Return the maximum integer value of an array of integer values
    * 
    * @param values
    * @return the maximum of the integers specified in values
    */
   
   public int min(int[] values) {
	   
	   int currentMin = values[0];
	   
	   for(int i=0; i<values.length; i++) {
		   if(values[i] <= currentMin)
			   currentMin = values[i];
	   }
	   
	   return currentMin;
}
	

	protected MinionModel getMinionModel() {
		return minionModel;
	}
	
	protected static void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG specialExpressionTranslator ] "+s);
    }  

	
}
