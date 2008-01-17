package translator;

import java.util.ArrayList;
import java.util.HashMap;

import translator.minionModel.*;
import translator.conjureEssenceSpecification.*;

import translator.preprocessor.Parameters;
import translator.preprocessor.PreprocessorException;

import translator.minionExpressionTranslator.ExpressionTranslator;
import translator.minionExpressionTranslator.TranslationUnsupportedException;

/**
 * MinionModelTranslator produces a constraint model for Minion
 * from a list of decision variables and constraints.
 *
 */
public class MinionModelTranslator implements MinionGlobals {

	    private HashMap<String, Domain> decisionVariables;
	    private ArrayList<String> decisionVariablesNames;
	    private ArrayList<Expression> constraints;
	    private Objective objective;
	    private Parameters parameterArrays;
	  
	    
	    MinionModel myMinionModel;
	    ExpressionTranslator constraintsTranslator;
	    
	public MinionModelTranslator(HashMap<String, Domain> decisionVars, 
							     ArrayList<String> varNames, 
							     ArrayList<Expression> constrs, 
							     Objective objective, 
							     Parameters parameterArrays) 
		throws ClassNotFoundException, MinionException {
		
			decisionVariables = decisionVars;
			decisionVariablesNames = varNames;
			constraints = constrs;
			this.objective = objective;
			this.parameterArrays = parameterArrays;		
			
			myMinionModel = new MinionModel();
			
	}
	
	
	/**
	 * Creates a model instance for Minion using the decision variables
	 * and constraints given in the constructor.
	 * @param useWatchedLiterals TODO
	 * @param useDiscreteVariables TODO
	 * @return a minion Model specifications
	 * @throws MinionException
	 */
	public MinionModel produceMinionModel(boolean useWatchedLiterals, boolean useDiscreteVariables) 
		throws MinionException, TranslationUnsupportedException, PreprocessorException,
		ClassNotFoundException {
		
		constraintsTranslator = new ExpressionTranslator(
				 decisionVariablesNames,
				 decisionVariables, 
				 myMinionModel, 
				 useWatchedLiterals, 
				 useDiscreteVariables, 
				 parameterArrays);
		
		print_debug("These are the constraint expressions:"+constraints.toString());
		createMinionConstraints();
		
		translateObjective(objective);
		// we can only compute all absolute indices after introducing all fresh variables
		myMinionModel.computeAllAbsoluteIndices();
		
		return myMinionModel;
	}
	
	
	/**
	 * 
	 * @throws MinionException
	 */
	protected void createMinionConstraints() 
		throws MinionException, TranslationUnsupportedException, PreprocessorException,
		ClassNotFoundException {
		
		for(int i=0; i< constraints.size(); i++) {
			print_debug("About to translate "+constraints.get(i));
			constraintsTranslator.translate(constraints.get(i));
			print_debug("After translation");
			print_debug("Translated "+constraints.get(i));
		}
		
	}
	
	
	/**
	 * Translate the Objective, if there is one
	 * @param objective
	 * @throws MinionException
	 * @throws TranslationUnsupportedException
	 * @throws PreprocessorException
	 * @throws ClassNotFoundException
	 */
	private void translateObjective(Objective objective) 
	throws MinionException, TranslationUnsupportedException, PreprocessorException,
		ClassNotFoundException  {		
		// we have no objective
		if(objective.getExpression()==null)
			return;
		
		else constraintsTranslator.translateObjective(objective);
	}
	
	   /** 
    If the DEBUG-flag in the Globals-interface is set to true, then
    print the debug-messages. These messages are rather interesting 
    for the developper than for the user.
    @param s the String to be printed on the output
   */

    private static void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG minionModelTranslator ] "+s);
    }
	
}
