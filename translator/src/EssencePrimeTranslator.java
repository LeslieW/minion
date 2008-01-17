import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import conjureEssenceSpecification.*;
import essencePrimeParser.*;
import minionModel.*;
import preprocessor.*;
import minionExpressionTranslator.*;

/**
	The abstract class EssencePrimeTranslator takes care of all translation steps
	that are done independent of the solver we want to translate to. This
	includes parsing and producing a syntax tree, followed by inserting
	parameter values, expression evaluation, collecting all decision variables,
	and simplifying constraints. As a result, a HashMap "decisionVariables"
	contains all decision variables with their corresponding domain and an
	ArrayList "decisionVariablesNames" holding all decision variables names. 
	The evaluated and simplified constraints are stored in the ArrayList 
	"constraints". The constructor of the abstract class performs all
	these solver-independent steps.
	@author andrea
*/
public abstract class EssencePrimeTranslator implements TranslatorGlobals {

	/** Essence' parser */
    EssencePrimeParser parser ;
    /** Essence' syntax tree of problem file */
    EssenceSpecification problemSpec;  
    /** Essence' syntax tree of parameter file */
    EssenceSpecification parameterSpec ; 
    
    /** takes care about solver-independent preprocessing steps*/
    Preprocessor preprocessor;
    /** contains name and domain of each defined decision variable*/
    HashMap<String, Domain> decisionVariables;
    /** contains names of all defined decision variables*/
    ArrayList<String> decisionVariablesNames;
    /** contains the parameter arrays with values*/
    Parameters parameterArrays; 
    /** contains all constraints defined */
    ArrayList<Expression> constraints;
    /** contains objective, if there is one */
    Objective objective;
    /** filename in which the output will be written */
    static String outputFileName;
    
    
    
    /** 
       The constructor takes two Strings giving the problem- and parameter-
       filename. It parses both files producing two syntax trees.
       Then both trees are preprocessed and the preprocessor gives 
       the set of decision variables and the set of constraints.
       @param problemFileName  the String filename of the problem specification
       @param parameterFileName  the String filename of the parameter specification
       @throws FileNotFoundException, Exception
     */
    EssencePrimeTranslator(String problemFileName, String parameterFileName)
		throws FileNotFoundException, Exception {
    	
    // 	parsing
    	parser = new EssencePrimeParser(new EssencePrimeLexer
					(new FileReader(problemFileName)) );
    	problemSpec = (EssenceSpecification) parser.parse().value;
    	print_debug(problemSpec.toString());
    	
    	parser = new EssencePrimeParser(new EssencePrimeLexer
					(new FileReader(parameterFileName)) );
    	parameterSpec = (EssenceSpecification) parser.parse().value;
    	print_debug(parameterSpec.toString());
	
	// 	pre-processing phase
    	preprocessor = new Preprocessor(problemSpec, parameterSpec);
    	print_debug("Finished preprocessing phase.");
    	
    	this.parameterArrays = preprocessor.getParameterArrays();
    	this.decisionVariables = preprocessor.getDecisionVariables();
    	this.decisionVariablesNames = preprocessor.getDecisionVariablesNames();
    	print_debug("These are the decisionVariables' names: "+decisionVariablesNames.toString());
    	constraints = preprocessor.getConstraints();
    	objective = preprocessor.getObjective();
		
    	print_debug("The decision-variables: "+decisionVariables.toString());
    	print_debug("The list of constraints: "+constraints.toString());
    }
    
    /** 
       Given a HashMap of decision variables with their domains and
       a list of all constraints, translate to the solver input language.
       This method includes all translation steps that are solver-dependent.
     * @param parameters TODO
       */
    public abstract String translate(String[] parameters) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException ;
    
  
/** 
 * Read arguments, create outputfile and construct a 
 * corresponding Translator that writes the output into the
 * outpur file.
 * 
 * @param args Arguments for EssencePrimeTranslator:
 *          <ol>
 *          	<li> Essence' problem specification
 *          	</li>
 *          	<li> Essence' parameter specification
 *          	</li>
 *          	<li> Output filename (optional)
 *          	</li>
 *          </ol>
 * 
 */
    public static void main(String[] args) {
	
    	outputFileName = TranslatorGlobals.outPutFileName;	
    	String[] parameters = null;   
    	
	if (args.length < 2 || args.length > 5) {
		printWrongArgumentsErrorMsg();
	    return ;
	}
       	
	try {
	    if(args.length >= 3) {
	    	if(args[2].startsWith("-")) {
	    		parameters = new String[args.length-2];
	    		for(int i=0; i<parameters.length; i++)
	    			parameters[i] = args[i+2];
	    	}
	    	else 
		    	outputFileName = (String) args[2];
	    }
	    
	    		
	    
	    
		if(args.length >= 4) {
	    	parameters = new String[args.length-3];
	    	for(int i=0; i<parameters.length; i++)
	    		parameters[i] = args[3+i];	
	    }
	    
	    // translate to Minion
	    EssencePrimeMinionTranslator translator = new EssencePrimeMinionTranslator(args[0], args[1]) ;
	   	String minionOutput = translator.translate(parameters) ;
	   	print_message("Translation Successful") ;
	    
	   	// write output into file
	   	writeOutputIntoFile(minionOutput);  
    	print_message("Output written into "+outputFileName) ;		
	    
	    
	}
	  catch(Exception e) {
	      System.out.println(e);
	      System.out.println("Bailing out.");
	  }
    }
    
    
    private static void printWrongArgumentsErrorMsg() {    	
    	
    	String unwatchedDefault = "";
    	String watchedDefault = "";
    	String discreteDefault = "";
    	String boundsDefault = "";
    	if(minionModel.MinionGlobals.USE_WATCHED_LITERALS) 
    		watchedDefault = "(default)";
    	else unwatchedDefault = "(default)";
    		
    	if(minionModel.MinionGlobals.USE_DISCRETE_VARIABLES)
    		discreteDefault = "(default)";
    	else boundsDefault ="(default)";
    	
	    print_message("Usage: java EssencePrimeTranslator <spec1> <spec2> [<outfile>] [-watch] [-vartype] \n") ;
	    print_message("\t spec1:\t\tEssence' problem specification");
	    print_message("\t spec2:\t\tEssence' parameter specification");
	  //  print_message("\t solver:\ttarget solver ('0' for Minion)\n");
	    print_message("\t outfile:\tfilename of output, default is "+MinionTranslatorGlobals.OUTPUT_FILENAME);
	    print_message("\t watch:\t\t'-unwatched' for non-watched literal constraints "+unwatchedDefault);
	    print_message("\t\t\t'-watched' for watched literal constraints "+watchedDefault);
	    print_message("\t vartype:\t'-discrete' for discrete bound variables "+discreteDefault);
	    print_message("\t\t\t'-bounds' for bound variables "+boundsDefault);
	    print_message("\nexample:\n shell> java EssencePrimeTranslator problem.cm parameters.param out.minion -unwatched -discrete");	
    }
    
    
    private static void writeOutputIntoFile(String output) 
    	throws IOException {
    	FileWriter outputFile = new FileWriter(outputFileName);
    	outputFile.write(output);
    	outputFile.flush();
    	outputFile.close();	 		
    }
    
    
    /** 
    If the DEBUG-flag in the Globals-interface is set to true, then
    print the debug-messages. These messages are rather interesting 
    for the developper than for the user.
    @param s the String to be printed on the output
   */

    private static void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG essencePrimeTranslator ] "+s);
    }

   /** 
   If the PRINT_MESSAGE-flag in the Globals-interface is set to true, then
    print some general messages on the output-stream. These messages are 
    relevant for the user and won't give enough information for the
    developper.
    @param s  the String to be printed on the output
     */

   private static void print_message(String s) {
	if(PRINT_MESSAGE)
	    System.out.println(s);
   	}
    
}
