package translator.minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import translator.minionModel.*;

import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.EssenceGlobals;
//import translator.conjureEssenceSpecification.BinaryExpression;

import translator.preprocessor.Parameters;
import translator.preprocessor.PreprocessorException;

/**
 * Translate relational expressions containing either boolean or arithemtic 
 * expressions. It is done rather stupidly and produces a lot of fresh variables
 * and does no simplification. This should be done in the subclass "SpecialExpressionTranslator.java".
 * 
 * @author andrea
 * @see translator.conjureEssenceSpecification.BinaryExpression;
 */

public class RelationalExpressionTranslator extends MulopExpressionTranslator {

	public RelationalExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
			HashMap<String, MinionIdentifier[]> minionVecs,
			HashMap<String, MinionIdentifier[][]> minionMatrixz,
			ArrayList<String> decisionVarsNames, 
			HashMap<String, Domain> decisionVars, 
			MinionModel mm, 
			boolean useWatchedLiterals, boolean useDiscreteVariables, 
			Parameters parameterArrays, HashMap<String, MinionIdentifier[][][]> minionCubes) {	
		super(minionVars, minionVecs, minionMatrixz, decisionVarsNames, decisionVars,mm, useWatchedLiterals, useDiscreteVariables, parameterArrays, minionCubes);
		
	}
	
	
	protected MinionReifiableConstraint translateRelationalExpression(Expression constraint, boolean reifiable) 
		throws TranslationUnsupportedException, MinionException,
			ClassNotFoundException, PreprocessorException {
		
		int operator = constraint.getBinaryExpression().getOperator().getRestrictionMode();
		
		MinionIdentifier left = translateMulopExpression(constraint.getBinaryExpression().getLeftExpression());
		MinionIdentifier right = translateMulopExpression(constraint.getBinaryExpression().getRightExpression());
		
		switch(operator) {
		
		case EssenceGlobals.EQ:
			return new MinionEqConstraint(left,right);
			
		case EssenceGlobals.NEQ:
			return new MinionDisEqConstraint(left, right);

			
		case EssenceGlobals.LEQ:
			return new MinionInEqConstraint(left, right, new MinionConstant(0));
			
		case EssenceGlobals.LESS:
			return new MinionInEqConstraint(left, right, new MinionConstant(-1));
			
			
		case EssenceGlobals.GEQ:
			return new MinionInEqConstraint(right, left, new MinionConstant(0));			
			
		case EssenceGlobals.GREATER:
			return new MinionInEqConstraint(right, left, new MinionConstant(-1));	
			
			
		case EssenceGlobals.AND:
			return new MinionSumGeqConstraint(new MinionIdentifier[] {left, right}, new MinionConstant(2), useWatchedLiterals && !reifiable);
			
		case EssenceGlobals.IF:
			return new MinionInEqConstraint(left, right, new MinionConstant(0));
			
		case EssenceGlobals.OR:
			return new MinionSumGeqConstraint(new MinionIdentifier[] {left, right}, new MinionConstant(1), useWatchedLiterals && !reifiable);
			
		case EssenceGlobals.IFF:
			return new MinionEqConstraint(left,right);
			
	    default:	
	    	throw new TranslationUnsupportedException
	    	("Internal error. Cannot translate non-relational and non-boolean expression '"+constraint.toString()+"' in the relational Expression translator part.");			
		}
		
		
	}
		
	/**
	 * Returns true, if the expression would be translated by a Constraint
	 * (and cannot be represent by a variable)
	 * and false, if the constraint would be represented by a variable
	 * 
	 * @param expression
	 * @return
	 */
	protected boolean isRelationalExpression(Expression expression) {
		
		
		switch(expression.getRestrictionMode())  {
	
		
		case EssenceGlobals.BINARYOP_EXPR:
		
			int operator = expression.getBinaryExpression().getOperator().getRestrictionMode();
		
			if(operator == EssenceGlobals.MULT ||
					operator == EssenceGlobals.PLUS ||
					operator == EssenceGlobals.MINUS ||
					operator == EssenceGlobals.DIVIDE ||
					operator == EssenceGlobals.POWER)
				return false;
		
			else if(operator == EssenceGlobals.EQ ||
					operator == EssenceGlobals.NEQ ||
					operator == EssenceGlobals.LEQ ||
					operator == EssenceGlobals.GEQ ||
					operator == EssenceGlobals.GREATER ||
					operator == EssenceGlobals.LESS ||
					operator == EssenceGlobals.AND ||
					operator == EssenceGlobals.OR ||
					operator == EssenceGlobals.IF ||
					operator == EssenceGlobals.IFF)
				return true;
			else return false; //well, what else?
			
			
		case EssenceGlobals.ATOMIC_EXPR:
			return false;
			
		case EssenceGlobals.NONATOMIC_EXPR:
			return false;
			
		case EssenceGlobals.UNITOP_EXPR:
			return false;
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			//int quantifier = expression.getQuantification().getQuantifier().getRestrictionMode();
			//if(quantifier == EssenceGlobals.FORALL ||
			//   quantifier == EssenceGlobals.EXISTS)
			//	return true;
			//else {// the quantifier is a sum
				return isRelationalExpression(expression.getQuantification().getExpression());
			//}
		}
		
		return false;
	}
	
	
	/**
	 * returns true, if the expression e contains a quantified expression
	 * (on a certain level)
	 * @param e
	 * @return
	 */
	protected boolean containsQuantification(Expression e) {
		
		switch(e.getRestrictionMode()) {
			
			
		case EssenceGlobals.ATOMIC_EXPR:
			return false;
		
		case EssenceGlobals.NONATOMIC_EXPR:
			return false;
			
		case EssenceGlobals.UNITOP_EXPR:
			return containsQuantification(e.getUnaryExpression().getExpression());
			
		case EssenceGlobals.BINARYOP_EXPR:
			return containsQuantification(e.getBinaryExpression().getRightExpression()) ||
			       containsQuantification(e.getBinaryExpression().getLeftExpression());
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			return true;
			
			
		default: return false;
		
		}
	
	}
	
	/**
	 * 
	 * @param constraint
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws ClassNotFoundException
	 * @throws PreprocessorException
	 */
	
	/*protected MinionReifiableConstraint translateToRelationalExpression(Expression constraint) 
		throws TranslationUnsupportedException, MinionException,
			ClassNotFoundException, PreprocessorException {
	
	int operator = constraint.getBinaryExpression().getOperator().getRestrictionMode();
	
	MinionIdentifier left = translateMulopExpression(constraint.getBinaryExpression().getLeftExpression());
	MinionIdentifier right = translateMulopExpression(constraint.getBinaryExpression().getRightExpression());
	
	switch(operator) {
	
	case EssenceGlobals.EQ:
		return new MinionEqConstraint(left,right);
		
	case EssenceGlobals.NEQ:
		return new MinionDisEqConstraint(left, right);
		
	case EssenceGlobals.LEQ:
		return new MinionInEqConstraint(left, right, new MinionConstant(1));
		
	case EssenceGlobals.LESS:
		return new MinionInEqConstraint(left, right, new MinionConstant(-1));
		
	case EssenceGlobals.GEQ:
		return new MinionInEqConstraint(right, left, new MinionConstant(1));
		
	case EssenceGlobals.GREATER:
		return new MinionInEqConstraint(right, left, new MinionConstant(-1));
		
	case EssenceGlobals.AND:
		return new MinionSumGeqConstraint(new MinionIdentifier[] {left, right}, new MinionConstant(2), useWatchedLiteralss);
		
	case EssenceGlobals.IF:
		return new MinionInEqConstraint(left, right, new MinionConstant(1));
		
	case EssenceGlobals.OR:
		return new MinionSumGeqConstraint(new MinionIdentifier[] {left, right}, new MinionConstant(1), useWatchedLiteralss);
		
	case EssenceGlobals.IFF:
		return new MinionEqConstraint(left,right);
		
    default:	
    	throw new TranslationUnsupportedException
    	("Internal error: trying to translate non-relational expression '"+constraint.toString()+"' in relational-Expression-translator part");			
	}
	
	
}
*/
	
	protected static void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG relationalExpressionTranslator ] "+s);
    }  

	
}
