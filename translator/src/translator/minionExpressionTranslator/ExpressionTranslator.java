package translator.minionExpressionTranslator;

import translator.minionModel.MinionException;
import translator.minionModel.MinionIdentifier;
import translator.minionModel.MinionModel;
import translator.minionModel.MinionConstraint;
import translator.conjureEssenceSpecification.EssenceGlobals;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.Objective;
import translator.preprocessor.PreprocessorException;
import translator.preprocessor.Parameters;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * We assume that all expressions are fully evaluated that are assigned to the Translator.
 * 
 * @author andrea
 *
 */

public class ExpressionTranslator implements MinionTranslatorGlobals {

    /** contains the corresponding MinionIdentifier for each decision variable*/
	HashMap<String, MinionIdentifier> minionVariables;
	HashMap<String, MinionIdentifier[]> minionVectors;
	HashMap<String, MinionIdentifier[][]> minionMatrices;
	HashMap<String, MinionIdentifier[][][]> minionCubes;
	
	HashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
        MinionModel minionModel;
	Parameters parameterArrays;
	
	//GlobalConstraintTranslator globalConstraintTranslator;
	
	/** translator for all expressions except universal and existential quantification */
	QuantifiedMulopExpressionTranslator expressionTranslator;
	
	/** translator for quantified expressions */
	//QuantificationTranslator quantificationTranslator;
	QuantifierTranslator quantificationTranslator;
	
	public ExpressionTranslator(ArrayList<String> decisionVarsNames, 
								HashMap<String, Domain> decisionVars,
								MinionModel mm,
								boolean useWatchedLiterals, 
								boolean useDiscreteVariables, 
								Parameters parameterArrays) 
		throws ClassNotFoundException, MinionException {
		
		this.minionVariables = new HashMap<String, MinionIdentifier>();
		this.minionVectors = new HashMap<String, MinionIdentifier[]>();
		this.minionMatrices = new HashMap<String, MinionIdentifier[][]>();
		this.minionCubes = new HashMap<String, MinionIdentifier[][][]>();
		
		this.decisionVariablesNames = decisionVarsNames;
		this.minionModel= mm;
		this.decisionVariables = decisionVars;
		this.parameterArrays = parameterArrays;
		
		print_debug("ExpressionTranslator: useWatchedLiterals is "+useWatchedLiterals);

		
		this.expressionTranslator = new QuantifiedMulopExpressionTranslator(this.minionVariables,
				                                                    this.minionVectors,
				                                                    this.minionMatrices, 
				                                            minionCubes,decisionVarsNames,decisionVariables, 
				                                            this.minionModel, useWatchedLiterals, useDiscreteVariables, parameterArrays);	
		
		this.quantificationTranslator = new QuantifierTranslator(this.minionVariables, this.minionVectors, this.minionMatrices, 
									 minionCubes, decisionVariablesNames, minionModel, 
				                                                 decisionVariables, parameterArrays, useWatchedLiterals, useDiscreteVariables);
	}
	
	
	/**
	 * Translate an expression into a MinionConstraint that is put into the MinionModel. 
	 * 
	 * @param e the Expression that is being translated to a MinionConstraint
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	
		public void translate (Expression e)
			throws TranslationUnsupportedException, MinionException, PreprocessorException,
			ClassNotFoundException {
			
	
		      print_debug("translating expression: "+e.toString());

		      switch (e.getRestrictionMode()) {
		    
		      case EssenceGlobals.ATOMIC_EXPR:
		    	  expressionTranslator.translateSingleAtomExpression(e);
		    	  break;
		    	  
		      case EssenceGlobals.NONATOMIC_EXPR:
		    	  expressionTranslator.translateSingleAtomExpression(e);
		    	  break;
		      
		      case EssenceGlobals.BINARYOP_EXPR:
		    	  if(doesNotContainQuantifications(e)) {
		    		  print_debug("RRRRRRRRRRRRRRRRRRRRRRRAAAAAAAAAAAAAAAAAAAAAAAA beginning to translate constraint:"+e.toString());
		    		  MinionConstraint constraint = expressionTranslator.translateSpecialExpression(e, false);
		    		  if(constraint!= null)
		    			  print_debug("translated expression  to :"+constraint.toString());
		    		  else 
		    			  print_debug("translated expression  to null.");
		    		
		    			  
		    		  if(constraint != null) // the constraint is not false 
		    			  minionModel.addConstraint(constraint);
		    		  print_debug("finished translating now...");
			      }	
		    	  else {
		    	  	  //quantificationTranslator.translate(e.getQuantification());
		    	     MinionConstraint constraint = quantificationTranslator.translate(e,false);
		    	     if(constraint != null)
		    	    	 this.minionModel.addConstraint(constraint);
		    	  }
		    	  break;
		      
		      case EssenceGlobals.QUANTIFIER_EXPR:
		    	  //quantificationTranslator.translateQuantification(e);
		    	  quantificationTranslator.translate(e.getQuantification());
		    	  break;
	
		      case EssenceGlobals.FUNCTIONOP_EXPR:
		    	  minionModel.addConstraint(expressionTranslator.translateSpecialExpression(e, false));
		    	  break;
		    	  
		      case EssenceGlobals.LEX_EXPR:
		    	  minionModel.addConstraint(expressionTranslator.translateSpecialExpression(e, false));
		    	  break;
		    	  
		      case EssenceGlobals.BRACKET_EXPR:
		    	  translate(e.getExpression());
		    	  break;
		    	  
		      default:
		    	  throw new TranslationUnsupportedException
			      ("Cannot translate expression: "+e.toString());		    	  
		     
		      }
		      //print_debug("lalalaal");
		}
		
		
		/**
		 */
		public void translateObjective (Objective objective)
		throws TranslationUnsupportedException, MinionException, PreprocessorException,
		ClassNotFoundException {
		
			Expression e = objective.getExpression();
			MinionIdentifier objectiveVariable = null;
			
	      print_debug("translating objective expression: "+e.toString());

	      switch (e.getRestrictionMode()) {
	    
	      case EssenceGlobals.ATOMIC_EXPR:
	    	  objectiveVariable = expressionTranslator.translateAtomExpression(e);
	    	  break;
	    	  
	      case EssenceGlobals.NONATOMIC_EXPR:
	    	  objectiveVariable = expressionTranslator.translateAtomExpression(e);
	    	  break;
	      
	      case EssenceGlobals.BINARYOP_EXPR:
	    	  if(doesNotContainQuantifications(e)) {
	    		  int operator = e.getBinaryExpression().getOperator().getRestrictionMode();
	    		  if(operator == EssenceGlobals.PLUS ||
	    			  operator == EssenceGlobals.MINUS ||
	    			  operator == EssenceGlobals.MULT ||
	    			  operator == EssenceGlobals.DIVIDE ||
	   				  operator == EssenceGlobals.POWER) {
	    			  objectiveVariable = expressionTranslator.translateMulopExpression(e);
	    			  break;
	    		  }    		  
	    	  }
	    	  throw new TranslationUnsupportedException("Cannot translate quantication as objective yet, sorry.");
	    	  
	      
	      case EssenceGlobals.QUANTIFIER_EXPR:
	    	  if(e.getQuantification().getQuantifier().getRestrictionMode() == EssenceGlobals.SUM) {
	    		  //this.quantificationTranslator.translatingObjective(true);
	    	  // 	translation
	    		  //this.quantificationTranslator.translate(e.getQuantification());
	    		  
	    		  objectiveVariable = this.expressionTranslator.translateMulopQuantification(e.getQuantification());
	    	  
	    		  //this.quantificationTranslator.translatingObjective(false);
	    		  //this.quantificationTranslator.resetObjectiveVariable();
	    		  break;
	    	  }
	    	  else 
	    		  throw new TranslationUnsupportedException("Cannot translate quantication as objective yet, sorry.");
	    	  

	    	  
	      default:
	    	  throw new TranslationUnsupportedException
		      ("The expression is not applicable as an objective: "+e.toString());		    	  
	     
	      }
	      
	      if(objectiveVariable==null)
	    	  throw new TranslationUnsupportedException
	 		 		("Internal error. Could not translate the objective expression:"+e.toString()+
	 		 				". Translation neither returned a variable nor a constraint.");
	      
	      minionModel.addObjective(objectiveVariable, objective.isMinimising());
  
	}
	
		
		/**
		 * Determines if the Expression e contains any quantifications. 
		 * 
		 * @param e
		 * @return true if the Expression e does not contain any quantifications
		 */      
		
		private boolean doesNotContainQuantifications(Expression e) {
			
			if(e.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				return doesNotContainQuantifications(e.getBinaryExpression().getLeftExpression()) &&
				doesNotContainQuantifications(e.getBinaryExpression().getRightExpression()) ;
				
			}
			else if(e.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR)
				return false;
			
			else if(e.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR)
				return doesNotContainQuantifications(e.getExpression());
			
			else if(e.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR)
				return doesNotContainQuantifications(e.getUnaryExpression().getExpression());
			
			return true;
		}
		
		      /** 
		       * If the DEBUG-flag in the Globals-interface is set to true, then
		       * print the debug-messages. These messages are rather interesting 
		       * for the developper than for the user.
		       * @param s : the String to be printed on the output
		       */

		       protected static void print_debug(String s) {
		       	if(DEBUG)
		       		System.out.println("[ DEBUG expressionTranslatorTop ] "+s);
		       }  
	
		
		
	
	
	//public void translate(Expression constraint) throws TranslationUnsupportedException, MinionException ;
	
}
