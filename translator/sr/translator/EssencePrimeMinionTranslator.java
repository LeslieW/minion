package translator;

import java.io.*;
import translator.minionExpressionTranslator.TranslationUnsupportedException;
import translator.minionModel.MinionModel;
import translator.minionModel.MinionException;
import translator.preprocessor.PreprocessorException;



public class EssencePrimeMinionTranslator extends EssencePrimeTranslator implements minionModel.MinionGlobals {

    MinionModel minionModel;
    MinionModelTranslator translator;
    boolean useWatchedLiterals;
    boolean useDiscreteVariables;

    public EssencePrimeMinionTranslator(String problemFile, String parametersFile) 
	   throws FileNotFoundException, Exception {

    // this is done in the super-class:
	//1. parsing: gives us the two specs(syntax-trees): 'essencePrimeProblem' and 'essencePrimeParameters'
    //2. pre-processing gives us the following: 
    	  // HashMap<String, Domain> decisionVariables;
    	  //  ArrayList<String> decisionVariablesNames;
    	  //  ArrayList<Expression> constraints;
	super(problemFile, parametersFile);
		       
	translator = new MinionModelTranslator(decisionVariables, decisionVariablesNames, constraints, objective, parameterArrays);
	useWatchedLiterals = USE_WATCHED_LITERALS;
	useDiscreteVariables = USE_DISCRETE_VARIABLES;
    }

    
    
    public String translate(String[] parameters) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
    	
    	if(parameters != null) {
    		for(int i=0; i<parameters.length; i++) {
    			print_debug("Processed PPPPParameter is: "+parameters[i]);
    			processParameter(parameters[i]);
    		}
    	} 

    	print_debug("starting to call the specific translator now");
    	minionModel = translator.produceMinionModel(useWatchedLiterals, useDiscreteVariables);    	    	
    	return minionModel.toString();
	
    }

    /**
     * Process the parameters given as arguments
     * @param parameter an argument given in the command line
     */
    private void processParameter(String parameter) {
    	
    	if(parameter.endsWith("unwatched")) 
    		useWatchedLiterals = false;
    	
    	else if(parameter.endsWith("watched"))
    		useWatchedLiterals = true;
    	
    	else if(parameter.endsWith("discrete"))
    		useDiscreteVariables = true;
    	
    	else if(parameter.endsWith("bounds"))
    		useDiscreteVariables = false;
    	
    	else print_message("Unknown parameter: "+parameter);
    	
    	print_debug("set useWatchedLiterals to: "+useWatchedLiterals+" and useDiscreteVariables to:"+useDiscreteVariables);
    }
    
    
    /** 
    If the DEBUG-flag in the Globals-interface is set to true, then
    print the debug-messages. These messages are rather interesting 
    for the developper than for the user.
    @param s the String to be printed on the output
   */

    private static void print_debug(String s) {
    	if(TranslatorGlobals.DEBUG)
    		System.out.println("[ DEBUG ePrimeMinionTranslator ] "+s);
    }
	
    private void print_message(String s) {
    	if(TranslatorGlobals.PRINT_MESSAGE)
    		System.out.println("[ WARNING ] "+s);
    }
    
    
    
}
