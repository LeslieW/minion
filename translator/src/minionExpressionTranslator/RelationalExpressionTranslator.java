package minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import minionModel.*;

import conjureEssenceSpecification.Domain;
import conjureEssenceSpecification.Expression;
import conjureEssenceSpecification.EssenceGlobals;

import preprocessor.Parameters;
import preprocessor.PreprocessorException;

/**
 * Translate relational expressions containing either boolean or arithemtic 
 * expressions. It is done rather stupidly and produces a lot of fresh variables
 * and does no simplification. This should be done in the subclass "SpecialExpressionTranslator.java".
 * 
 * @author andrea
 * @see conjureEssenceSpecification.BinaryExpression;
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
