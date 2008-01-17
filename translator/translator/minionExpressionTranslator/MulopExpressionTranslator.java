package translator.minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import translator.minionModel.MinionIdentifier;
import translator.minionModel.MinionModel;
//import translator.minionModel.MinionBoundsVariable;
//import translator.minionModel.MinionDiscreteVariable;
import translator.minionModel.MinionBoolVariable;
import translator.minionModel.MinionConstant;
import translator.minionModel.MinionException;
//import translator.minionModel.MinionMaxConstraint;
import translator.minionModel.MinionSumGeqConstraint;
import translator.minionModel.MinionInEqConstraint;
import translator.minionModel.MinionEqConstraint;
import translator.minionModel.MinionDisEqConstraint;
import translator.minionModel.MinionReifiableConstraint;

import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.EssenceGlobals;
import translator.conjureEssenceSpecification.BinaryExpression;
import translator.conjureEssenceSpecification.UnaryExpression;
import translator.conjureEssenceSpecification.BinaryOperator;

import translator.preprocessor.Parameters;
import translator.preprocessor.PreprocessorException;

/**
 * Translates arithmetic and boolean expressions such as additions, substractions, disjunctions etc 
 * containing NO relations (=, <, ...) !<br>
 * Call the method "translateMulopExpression" and you will receive the freshVariable (MinionIdentifier)
 * representing the expression. In case the MinionIdentifier equals null, the expression was a boolean 
 * expression.
 * 
 * @author andrea
 * @see translator.conjureEssenceSpecification.BinaryExpression
 */

public class MulopExpressionTranslator extends AtomExpressionTranslator {

	
	
	/** the nesting depth of the expression we are considering at the moment.
	 *  Consider for example the expression "(a /\ b) \/ c": <br>
	 *  it is represented by " Expression \/ c" on top level, where the depth has value 1 and
	 *  its subexpression "(a /\ b)" has depth 2. Is used to trigger reification of boolean expressions. */
	int expressionDepth;
	
	MinionIdentifier freshVariableBuffer;
	
	public MulopExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
			HashMap<String, MinionIdentifier[]> minionVecs,
			HashMap<String, MinionIdentifier[][]> minionMatrixz,
			ArrayList<String> decisionVarsNames,
			HashMap<String, Domain> decisionVariables,
			MinionModel mm, 
			boolean useWatchedLiterals, boolean useDiscreteVariables, 
			Parameters parameterArrays, HashMap<String, MinionIdentifier[][][]> minionCubes) {	
		super(minionVars, minionVecs, minionMatrixz, minionCubes, decisionVarsNames,decisionVariables, mm, useWatchedLiterals, useDiscreteVariables, parameterArrays);
		
		expressionDepth = 0;
		
	
	}
	
	
	/**
	 * Translates Expression e to a MinionIdentifier. The Expression HAS to be 
	 * binary and Constantisting of expressions combined with multiplication operators,
	 * such as +,*,-,/\ etc. The Essence' grammar forbids multiple relational 
	 * operators in one expression, meaning relational parts inside
	 * of mulop-expressions, like "a+b = c*d = e". 
	 * @param e the atom Expression that will be translated
	 * @return the MinionIdentifier representing e 
	 * @throws TranslationUnsupportedException
	 */
	protected MinionIdentifier translateMulopExpression(Expression e) 
		throws TranslationUnsupportedException, MinionException, ClassNotFoundException, PreprocessorException {
		
		print_debug("translating mulop expression: "+e.toString());
		
		if(e.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
				e.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
			return translateAtomExpression(e);
		
		else if(e.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR) {
		    QuantifierTranslator quantifierTranslator = new QuantifierTranslator(minionVariables, minionVectors,
											 minionMatrices, minionCubes,decisionVariablesNames, minionModel, decisionVariables, parameterArrays, useWatchedLiterals, useDiscreteVariables);
		    MinionReifiableConstraint reifiableConstraint = (MinionReifiableConstraint) quantifierTranslator.translate(e, true);
		    if(reifiableConstraint == null)
			throw new TranslationUnsupportedException
			    ("Internal error: quantification nested in binary expression has not been reified: return value is null:"+e.toString());
		    else {
            	MinionBoolVariable reifiedVariable = new MinionBoolVariable(1,"freshVariable"+noTmpVars++);
            	minionModel.add01Variable(reifiedVariable);
            	minionModel.addReificationConstraint(reifiableConstraint, reifiedVariable);
            	print_debug("returning reified variable after translating in translateMulopE:"+e.toString());
            	return reifiedVariable;
		    }
		}
		else if(e.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR)
			return translateUnaryExpression(e.getUnaryExpression());
		
		else if(e.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR)
			return translateMulopExpression(e.getExpression());
		
		print_debug("translating mulop expression: "+e.toString()+" with operator :"+e.getBinaryExpression().getOperator().toString());
		
		int operator = e.getBinaryExpression().getOperator().getRestrictionMode();
		
		if(operator == EssenceGlobals.PLUS ||
				operator == EssenceGlobals.MINUS ||
				operator == EssenceGlobals.MULT ||
				operator == EssenceGlobals.DIVIDE ||
				operator == EssenceGlobals.POWER)
			return translateArithmeticExpression(e.getBinaryExpression());
		
		else if(operator == EssenceGlobals.AND ||
				operator == EssenceGlobals.OR ||
				operator == EssenceGlobals.IF ||
				operator == EssenceGlobals.IFF)
			return translateBooleanExpression(e.getBinaryExpression());
		
		else if(operator == EssenceGlobals.EQ ||
				operator == EssenceGlobals.NEQ ||
				operator == EssenceGlobals.LEQ ||
				operator == EssenceGlobals.GEQ ||
				operator == EssenceGlobals.GREATER ||
				operator == EssenceGlobals.LESS) {
			return translateRelExpression(e.getBinaryExpression());
		}
			
		else 
			throw new TranslationUnsupportedException
				("Internal error: trying to translate a relational expression '"+e.toString()+"' in the MulopExpressionTranslator.");
	}
	/**
	 * Translates Expression e to a MinionIdentifier. The Expression HAS to be 
	 * binary and Constantisting of expressions combined with multiplication operators,
	 * such as +,*,-,/\ etc. The Essence' grammar forbids multiple relational 
	 * operators in one expression, meaning relational parts inside
	 * of mulop-expressions, like "a+b = c*d = e". 
	 * @param e the atom Expression that will be translated
	 * @return the MinionIdentifier representing e 
	 * @throws TranslationUnsupportedException
	 */

	
	/**
	 * Translates an arithmetic operation, stated in BinaryExpression be. Arithmetic 
	 * operators are +,*,-,/,^ and no other are allowed in the Expressions assigned to
	 * this method (the Essence' grammar restricts expressions to this anyway).
	 * The following steps are done:
	 * <ol>
	 *    <li> Translate the two subexpressions receiving two MinionIdentifiers
	 *    </li>
	 *    <li> Compute the bounds of the fresh variable according to the operation
	 *    		in the binary Expression
	 *    </li>
	 *    <li> Create a new MinionIdentifier and add it to the MinionModel
	 *    </li>
	 *    <li> add the constraint to the MinionModel
	 *    </li>
	 *  </ol>
	 * @param be the BinaryExpression that will be translated into a MinionIdentifier 
	 * @param reifiable TODO
	 * @return the MinionIdentifier representing the arithmetic operation done by the
	 * BinaryExpression be. It is a fresh variable.
	 * @throws TranslationUnsupportedException
	 */
	
	private MinionIdentifier translateArithmeticExpression(BinaryExpression be) 
		throws TranslationUnsupportedException, MinionException, 
		ClassNotFoundException, PreprocessorException {
			    
	    // 1. translate the two subexpressions
		MinionIdentifier[] ids = translateSubExpressions(be);
		MinionIdentifier left = ids[0];
		MinionIdentifier right = ids[1];
		print_debug("test, ids length:"+ids.length);
		print_debug("left part:"+left.toString());
		print_debug("right part:"+right.toString());
		print_debug("translated subexpressions:left: "+left.toString()+" and right:"+right.toString());
		
	    // 2. compute the bounds of each variable in order to compute bounds for the 
	    // fresh variable
	    int op = be.getOperator().getRestrictionMode();
	    int[] freshBounds = computeBoundsOfFreshVariable(left, op, right);
	
	   
	    // 3. create a new MinionIdentifier
	    // in case there has not been a fresh variable yet	
	    MinionIdentifier freshVariable = variableCreator.addFreshVariable
	    	(freshBounds[0], freshBounds[1], "freshVariable"+noTmpVars++, useDiscreteVariables);
	    
	    
	    // 4. translate expression
	    if(op == EssenceGlobals.MULT)
	    	minionModel.addProductConstraint(left, right, freshVariable);
	    
	    else if(op == EssenceGlobals.PLUS) {
	    	//MinionIdentifier[] expr = new MinionIdentifier[2];
	    	//expr[0] = left;
	    	//expr[1] = right;
	    	minionModel.addSumConstraint(ids, freshVariable, useWatchedLiterals);
	    	//minionModel.addSumLeqVariablesConstraint(expr,freshVariable, useWatchedLiterals);				
	    	//minionModel.addSumGeqVariablesConstraint(expr,freshVariable, useWatchedLiterals);				
	    	}
	    
	    else if(op == EssenceGlobals.MINUS) {
	    	MinionIdentifier[] expr = new MinionIdentifier[2];
	    	expr[0] = left;
	    	expr[1] = right;
	    	MinionConstant[] weights = new MinionConstant[2];
	    	weights[0] = new MinionConstant(1);
	    	weights[1] = new MinionConstant(-1);
		
	    	minionModel.addWeightedSumLeqConstraint(expr,weights,freshVariable);
	    	minionModel.addWeightedSumGeqConstraint(expr,weights,freshVariable);
	    }
	    else if(op == EssenceGlobals.DIVIDE) {
	    	if(be.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) 
	    		if(be.getRightExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER)
	    			if(be.getRightExpression().getAtomicExpression().getNumber() == 0)
	    				throw new TranslationUnsupportedException("Division by zero in constraint expression: "+be.toString());
	    	
	    	minionModel.addProductConstraint(right, freshVariable, left);
	    }
	    
	    // TODO: power constraint!
	    else 
	    	throw new TranslationUnsupportedException
		    	("Cannot translate this binary expression yet: "+be.toString())
		    ;
	    return freshVariable;

	}

	
	
	/**
	 * Translates boolean expressions by applying reification (so rather stupidly). Cases
	 * where reification can be omitted should be captured in the subclass "SpecialExpressionTranslator.java". 
	 * We assume that the boolean expressions have already
	 * been evaluated and simplified. For instance that an Expression
	 * "(a \/ b ) /\ (c => d)" is  split into its components "(a /\ b)" and "(c => d)"
	 * to produce a better solver model. 
	 * @param be BinaryExpression that is boolean and will be translated
	 * @return the MinionIdentifier representing the boolean expression. If the return value is
	 * 	null, then no reification has been done.
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	
	private MinionIdentifier translateBooleanExpression(BinaryExpression be) 
		throws TranslationUnsupportedException, MinionException, ClassNotFoundException, 
		PreprocessorException{

		print_debug("Translating boolean expression: "+be.toString());
		
	    // 1. translate the two subexpressions
		MinionIdentifier[] ids = translateSubExpressions(be);
		MinionIdentifier left = ids[0];
		MinionIdentifier right = ids[1];
		
		// 2. create fresh variable
		MinionBoolVariable freshVariable = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
		minionModel.add01Variable(freshVariable);
		minionVariables.put("freshVariable"+(noTmpVars++), freshVariable);
	    
		//3. translate constraints
	    int op = be.getOperator().getRestrictionMode();
	    
	    switch(op) {
	    
	    case EssenceGlobals.AND: // a /\ b ---> sum(a,b) >= 2
	    			MinionSumGeqConstraint andConstraint = new MinionSumGeqConstraint(new MinionIdentifier[] {left,right}, new MinionConstant(2), false);

	    			minionModel.addReificationConstraint(andConstraint, freshVariable);
	    			break;
	    			
	    case EssenceGlobals.OR: // a \/ b  ---> sum(a,b) >= 1
    				MinionSumGeqConstraint orConstraint = new MinionSumGeqConstraint(new MinionIdentifier[] {left,right}, new MinionConstant(1), false);
    				minionModel.addReificationConstraint(orConstraint, freshVariable);
    				break;
	    
	    case EssenceGlobals.IF: // a => b  --->  a <= b
  					MinionInEqConstraint ifConstraint = new MinionInEqConstraint(left, right, new MinionConstant(1));
  					minionModel.addReificationConstraint(ifConstraint, freshVariable);
  					break;
	    
	    case EssenceGlobals.IFF: // a <=> b  ---> a == b
					MinionEqConstraint iffConstraint = new MinionEqConstraint(left, right);
					minionModel.addReificationConstraint(iffConstraint, freshVariable);

	    		break;
  				
	    default:	
	    	throw new TranslationUnsupportedException
	    	("Internal error: trying to translate non-boolean expression '"+be.toString()+"' in Boolean Expression translator part");
	    }
	    	    	   
		return freshVariable;		
	}
		
	
	
	
	private MinionIdentifier translateRelExpression(BinaryExpression be) 
	throws TranslationUnsupportedException, MinionException,
		ClassNotFoundException, PreprocessorException{

    // 1. translate the two subexpressions
	MinionIdentifier[] ids = translateSubExpressions(be);
	MinionIdentifier left = ids[0];
	MinionIdentifier right = ids[1];
	
	// 2. create fresh variable
	MinionBoolVariable freshVariable = new MinionBoolVariable(1, "freshVariable"+noTmpVars);
	minionModel.add01Variable(freshVariable);
	minionVariables.put("freshVariable"+(noTmpVars++), freshVariable);
    
	//3. translate constraints
    int op = be.getOperator().getRestrictionMode();
    
    print_debug("tRANSLATING expression "+be.toString()+" with operator :"+be.getOperator().toString());
    
    switch(op) {
    
    case EssenceGlobals.NEQ: // a = b ---> max(a,b) == 1
				MinionDisEqConstraint diseqConstraint = new MinionDisEqConstraint(left, right);
				minionModel.addReificationConstraint(diseqConstraint, freshVariable);

    			break;
    			
    case EssenceGlobals.LESS: // a < b
			MinionInEqConstraint lessConstraint = new MinionInEqConstraint(left, right, new MinionConstant(-1));
			minionModel.addReificationConstraint(lessConstraint, freshVariable);
			break;
    
    case EssenceGlobals.LEQ: //   a <= b
					MinionInEqConstraint leqConstraint = new MinionInEqConstraint(left, right, new MinionConstant(1));
					minionModel.addReificationConstraint(leqConstraint, freshVariable);
					break;
					
    case EssenceGlobals.GEQ: //   a >= b
			MinionInEqConstraint geqConstraint = new MinionInEqConstraint(right, left, new MinionConstant(1));
			minionModel.addReificationConstraint(geqConstraint, freshVariable);
			break;
    
    case EssenceGlobals.GREATER: //   a > b
			MinionInEqConstraint greaterConstraint = new MinionInEqConstraint(right, left, new MinionConstant(-1));
			minionModel.addReificationConstraint(greaterConstraint, freshVariable);
			break;
			
    case EssenceGlobals.EQ: // a <=> b  ---> a == b
				MinionEqConstraint eqConstraint = new MinionEqConstraint(left, right);
				minionModel.addReificationConstraint(eqConstraint, freshVariable);

    		break;
				
    default:	
    	throw new TranslationUnsupportedException
    	("Internal error: trying to translate non-relational expression '"+be.toString()+"' in relational Expression translator part with operator:"
    			+be.getOperator().toString());
    }
    	    	   
	return freshVariable;		
}
	

	
	/**
	 * Translate the two subexpressions of a binary mulop-expression.
	 * @param be the BinaryExpression whose subexpressions will be translated
	 * @return the two MinionIdentifiers corresponding to the subexpressions:
	 * 	the first arrayelement to the left, the second the right subexpression.
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	
	private MinionIdentifier[] translateSubExpressions(BinaryExpression be) 
		throws TranslationUnsupportedException,MinionException, ClassNotFoundException, PreprocessorException {
	
    MinionIdentifier left = null;
    MinionIdentifier right = null;
    
    if(be.getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
       be.getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
    		left = translateAtomExpression(be.getLeftExpression());
    else if (be.getLeftExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
    	left = translateUnaryExpression(be.getLeftExpression().getUnaryExpression());
    }
    else 
    	left = translateMulopExpression(be.getLeftExpression());
    
    if(be.getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    	be.getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
    	right = translateMulopExpression(be.getRightExpression());
    else if (be.getRightExpression().getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
    	right = translateUnaryExpression(be.getRightExpression().getUnaryExpression());
    }
    else 
    	right = translateMulopExpression(be.getRightExpression());
    
    return new MinionIdentifier[] {left, right};
	
	}
	
	
	/**
	 * TODO!!
	 * 
	 * @param e
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	protected MinionIdentifier translateUnaryExpression(UnaryExpression e)
		throws TranslationUnsupportedException,MinionException, PreprocessorException, ClassNotFoundException  {
		
		print_debug("Translating unary expression:"+e);
		
		switch(e.getRestrictionMode()) {
		
		case EssenceGlobals.NOT:
			return translateBooleanNegation(e);
		
		case EssenceGlobals.NEGATION:
			throw new TranslationUnsupportedException("Sorry, cannot translate negated expressions, like '"+e.toString()+"' yet, sorry.");
		
		case EssenceGlobals.ABS: 
			throw new TranslationUnsupportedException("Sorry, cannot translate absolute values, like in '"+e.toString()+"' yet, sorry.");
		
			default:
				throw new TranslationUnsupportedException("Unknown unary operator in '"+e.toString()+"'.");
		}
		
	}
	
	
	protected MinionIdentifier translateBooleanNegation(UnaryExpression e)
		throws TranslationUnsupportedException, PreprocessorException, 
		MinionException, ClassNotFoundException {
		
		Expression negatedExpression = e.getExpression();
		
		if(negatedExpression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
				negatedExpression.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
			MinionIdentifier identifier = translateAtomExpression(negatedExpression);
			if(!Class.forName ("translator.minionModel.MinionBoolVariable").isInstance (identifier))
				throw new TranslationUnsupportedException("Cannot negate a non-boolean expression:"+negatedExpression.toString());
			
			MinionBoolVariable negatedIdentifier = (MinionBoolVariable) identifier;
			negatedIdentifier.setPolarity(0);
			return negatedIdentifier;
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
				if(!Class.forName ("translator.minionModel.MinionBoolVariable").isInstance (identifier))
					throw new TranslationUnsupportedException("Cannot negate a non-boolean expression:"+negatedExpression.toString());
				
				MinionBoolVariable negatedIdentifier = (MinionBoolVariable) identifier;
				negatedIdentifier.setPolarity(0);
				return negatedIdentifier;
			}
			else if (operator == EssenceGlobals.EQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.NEQ));
				return translateMulopExpression(negatedExpression);
			}
			else if (operator == EssenceGlobals.NEQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.EQ));
				return translateMulopExpression(negatedExpression);
			}
			else if (operator == EssenceGlobals.GEQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.LESS));
				return translateMulopExpression(negatedExpression);
			}
			else if (operator == EssenceGlobals.LEQ) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.GREATER));
				return translateMulopExpression(negatedExpression);
			}
			else if (operator == EssenceGlobals.LESS) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.GREATER));
				return translateMulopExpression(negatedExpression);
			}
			else if (operator == EssenceGlobals.GREATER) {
				negatedExpression.getBinaryExpression().setOperator(new BinaryOperator(EssenceGlobals.LEQ));
				return translateMulopExpression(negatedExpression);
			}
			else throw new TranslationUnsupportedException
				("Unknown Operator in negated expression:"+negatedExpression.toString());
			
		}
		else if (negatedExpression.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) {
			
			if(negatedExpression.getUnaryExpression().getRestrictionMode() == EssenceGlobals.NOT)
				return translateMulopExpression(negatedExpression.getUnaryExpression().getExpression());
			
			else {
				MinionIdentifier identifier = translateUnaryExpression(negatedExpression.getUnaryExpression());
				if(!Class.forName ("translator.minionModel.MinionBoolVariable").isInstance (identifier))
					throw new TranslationUnsupportedException("Cannot negate a non-boolean expression:"+negatedExpression.toString());
				
				MinionBoolVariable negatedIdentifier = (MinionBoolVariable) identifier;
				negatedIdentifier.setPolarity(0);
				return negatedIdentifier;		
			}
		}
		
		else if(negatedExpression.getRestrictionMode() == EssenceGlobals.ALLDIFF || 
				negatedExpression.getRestrictionMode() == EssenceGlobals.ELEMENT) {
				
				MinionReifiableConstraint constraint = translateGlobalConstraint(negatedExpression);
				MinionBoolVariable reifiedVariable = (MinionBoolVariable) 
					           variableCreator.addFreshVariable(0, 1, "freshVariable"+noTmpVars++, useDiscreteVariables);
				reifiedVariable.setPolarity(0);
				minionModel.addReificationConstraint(constraint, reifiedVariable);
				return reifiedVariable;
		}
		
		else throw new TranslationUnsupportedException
		("Cannot translate negated special subexpression yet:"+e.toString());
	}
	
	
	/**
	 * Computes the bound of a fresh variable that would be the result
	 * of the binary operation<br>
	 * LEFT operator RIGHT <br>
	 * where LEFT and RIGHT are represented by a MinionIdentifier. Each of them
	 * has lower and upper bounds that are stated in lower_left/right and upper_left/right.
	 * @param left TODO
	 * @param operator
	 * @param right TODO
	 * @return the lower and upper bound of the fresh variable, where int[0] represents
	 * 	the lower and int[1] the upper bound.
	 * @throws TranslationUnsupportedException
	 */
	
	
	private int[] computeBoundsOfFreshVariable(MinionIdentifier left, int operator, 
											   MinionIdentifier right)  
		throws TranslationUnsupportedException {
		
	    int lower_left = left.getLowerBound();
	    int upper_left = left.getUpperBound();
	    int lower_right = right.getLowerBound();
	    int upper_right = right.getUpperBound();	   
	
		
		int lower_tmp = 0;
		int upper_tmp = 0;
		
		switch(operator) {
	
    case EssenceGlobals.PLUS:
    	// different algebraic signs do not matter
    	lower_tmp = lower_left + lower_right;
    	upper_tmp = upper_left + upper_right;
    	break;
	
    case EssenceGlobals.MINUS:
	// 	different algebraic signs do not matter
    	lower_tmp = lower_left - upper_right;
    	upper_tmp = upper_left - lower_right;
    	break;
	
    case EssenceGlobals.MULT:
    		if(upper_left < 0 && upper_right < 0 ||  // all negative
    		lower_left >= 0 && lower_right >= 0) { // all positive => 2 cases 
    			lower_tmp = lower_left * lower_right;
    			upper_tmp = upper_left * upper_right;
    		}
    		else if 	(upper_left < 0) { // 1 case 
    			lower_tmp = lower_left * upper_right;
    			upper_tmp = upper_left * lower_right;
    		}
    		else if(lower_left < 0 &&  upper_left >= 0 && upper_right < 0) { // 1 case 
    			lower_tmp = upper_left * lower_right;
    			upper_tmp = lower_left * lower_right;
    		}
    		else if(lower_left < 0 && upper_left >= 0 && upper_right >= 0) { // 2 cases 
	    // 	lower_tmp = min(lower_left * upper_right, upper_left * upper_right);
    			lower_tmp = (lower_left * upper_right < upper_left * upper_right) ?
    					lower_left * upper_right :
    						upper_left * upper_right;
    					    upper_tmp = upper_left * upper_right;
    		}
    		else if(lower_left >= 0 && upper_right < 0) { // 1 case
    			lower_tmp = upper_left * lower_right;
    			upper_tmp = lower_left * upper_right;
    		}	
    		else if(lower_left >= 0 && lower_right < 0 && upper_right >= 0) { // 1 case
    			lower_tmp = upper_left * lower_right;
    			upper_tmp = upper_left * upper_right;
    		}
	
    		break;

    case EssenceGlobals.DIVIDE:
    		lower_tmp = lower_left / upper_left;
    		upper_tmp = upper_left / lower_left;
       
    case EssenceGlobals.POWER:
    		if(lower_left > 0 && lower_right >0) {		
    			lower_tmp = lower_left^lower_right;
    			upper_tmp = upper_left^upper_right;
    		}

		}
		
		if(lower_tmp < MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND)
			lower_tmp = MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND;
		
		if(upper_tmp > MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND)
			upper_tmp = MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND;
		
		return new int[] {lower_tmp, upper_tmp};
	}
	
	protected static void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG mulopExpressionTranslator ] "+s);
    }  


	
}
