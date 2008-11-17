package translator;

import java.io.StringReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

//import translator.conjureEssenceSpecification.EssenceSpecification;
import translator.essencePrimeParser.EssencePrimeLexer;
import translator.essencePrimeParser.EssencePrimeParser;

import translator.normaliser.*;
import translator.expression.Expression;
import translator.expression.EssencePrimeModel;

import translator.solver.TargetSolver;
import translator.tailor.*;
import translator.TranslationSettings;
/**
 * The translator provides all the core steps during the translation.
 * These steps are : <br>
 * 1. parsing (target solver independent)<br>
 * 2. normalising (target solver independent)<br>
 * 3. flattening<br>
 * 4. tailoring<br>
 * 
 * @author angee
 *
 */

public class Translator {
	
	/** parser for Essence' */
	EssencePrimeParser parser;
	
    /** tailors a normalised model to a target solver */
    Tailor tailor;
    
    /** Essence' syntax tree of problem file */
    EssencePrimeModel problemSpecification;  
    /** Essence' syntax tree of parameter file */
    EssencePrimeModel parameterSpecification ; 
    /** the normalised model. is produced by the normalise() method */
    NormalisedModel normalisedModel;
    
    /** the string contains the problem model for the target solver */
    String targetSolverInstance;
    
	String errorMessage;
    String debug;
    String cseInfo;
  
    TranslationSettings settings;
    
    boolean normalisedModelHasBeenFlattened;
    
    // ========================= CONSTRUCTOR ========================================
    
	public Translator(TranslationSettings settings) {
		this.settings = settings;
		this.errorMessage = new String("");
		this.debug = "";
		this.normalisedModelHasBeenFlattened = false;
	}
	
	/**
	 * Return true, if the problem specification and the parameter specification
	 * can be parsed without error. If there is a parse error, the error message is stored 
	 * in the class-member 'String errorMessage'.
	 * 
	 * @param problemFileName
	 * @param parameterFileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public boolean parse(String problemString, String parameterString) {
			
		try {
			// clear the old parameters
			//this.normaliser.clearParameters();
			
			this.parser = new EssencePrimeParser(new EssencePrimeLexer
					(new StringReader(problemString)) );
			this.problemSpecification = (EssencePrimeModel) parser.parse().value;
			if(parser.hadErrorRecovery) {
				this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
				return this.settings.allowParseErrorRecovery;
			}
			//print_debug(problemSpec.toString());
    	
			this.parser = new EssencePrimeParser(new EssencePrimeLexer
					(new StringReader(parameterString)) );
			this.parameterSpecification = (EssencePrimeModel) parser.parse().value;
			if(parser.hadErrorRecovery) {
				this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
				return this.settings.allowParseErrorRecovery;
			}
			//print_debug(parameterSpec.toString());
		}
		catch(Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
			return false;
		}
    	return true;
		
	}
	
	
	/**
	 * Returns true if the problem file (specified by the filename) can be 
	 * parsed. If there is a parse error, the error message is stored 
	 * in the class-member String errorMessage. 
	 * If the input was parsable, it is stored in the members problemSpec and 
	 * parameterSpec.
	 * 
	 * @param problemFileName
	 * @return
	 * @throws FileNotFoundException if the file cannot be found in the directory
	 * structure
	 */
	public boolean parse(String problemString) {
	
		try {
			this.parser = new EssencePrimeParser(new EssencePrimeLexer
				(new StringReader(problemString)) );
			this.problemSpecification = (EssencePrimeModel) parser.parse().value;
			if(parser.hadErrorRecovery) {
				this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
				return this.settings.allowParseErrorRecovery;
			}
			
			this.normalisedModelHasBeenFlattened = false;
			// create an empty parameter file
			this.parameterSpecification = new EssencePrimeModel();
			//print_debug(problemSpec.toString());
		}
		catch(Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = e.getMessage();
			//this.errorMessage = "\n"+this.parser.errorMessage;
			this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
			return false;
		}
		return true;
	
	}
	
	
	/**
	 * Normalise the current problem specification according to the normalise type.
	 * The normalise type can be either NORMALISE_BASIC NORMALISE_EVAL NORMALISE_ORDER
	 * NORMALISE_CANCELLATION or NORMALISE_FULL. If no valid normalisation type is given,
	 * the translator does nothing at all. If an exception is thrown or an error occurred,
	 * the method returns false and the error message can be obtained by calling the 
	 * getErrorMessage() method. 
	 * 
	 * @param normaliseType
	 * @return true if translation was successful and false if an error occurred (which 
	 * can be obtained by calling getErrorMessage() )
	 */
	public boolean normalise(char normaliseType) {
		
		if(this.problemSpecification == null) {
			this.errorMessage = errorMessage.concat("Please parse problem file before normalisation.");
			return false;
		}
			
		try { 
			long startTime = System.currentTimeMillis();
			Normaliser normaliser = new Normaliser();
			this.normalisedModel = normaliser.normalise(this.problemSpecification,
					                                    this.parameterSpecification,
					                                    normaliseType);
			this.normalisedModelHasBeenFlattened = false;
			
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Normalisation Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			
		} catch(Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		
		return true;   
		
	}
	
	
	/**
	 * Given a target solver, flatten the corresponding normalised model. To evoke this method, the
	 * model MUST have been parsed and normalised (at least basically normalised).
	 * 
	 * @param targetSolver
	 * @return
	 */
	public boolean flatten(TargetSolver targetSolver) {
		
		if(this.normalisedModel == null) {
			this.errorMessage = errorMessage.concat("Please normalise problem model before flattening");
			return false;
		}
		
		try {
			long startTime = System.currentTimeMillis();
			this.settings.setTargetSolver(targetSolver);
			this.tailor = new Tailor(this.normalisedModel,
				 				 	 this.settings);
			this.normalisedModel = tailor.flattenModel();
			this.normalisedModelHasBeenFlattened = true;
			
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Flattening Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			
		} catch(Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * Given a target solver, flatten the corresponding normalised model. To evoke this method, the
	 * model MUST have been parsed and normalised (at least basically normalised).
	 * 
	 * @param targetSolver
	 * @return
	 */
	public boolean tailorTo(NormalisedModel model, TargetSolver targetSolver) {
		
		this.normalisedModel = model;
		if(settings.getPropagateSingleIntRanges())
			normalisedModel.propagateSingleRangeDecisionVariables();

		try {
			// flattening
			long startTime = System.currentTimeMillis();
			this.settings.setTargetSolver(targetSolver);
			this.tailor = new Tailor(this.normalisedModel,
				 				 	 this.settings);
			this.normalisedModel = tailor.flattenModel();
			this.normalisedModelHasBeenFlattened = true;
			
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Flattening Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			
			// tailoring
			this.targetSolverInstance = this.tailor.tailor(this.normalisedModel, settings.targetSolver);
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Tailoring Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			if(this.settings.getCseDetails())
				this.cseInfo = this.tailor.getCseInfo();
			
		} catch(Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		return true;
	}
	
	
	
	
	/**
	 * Tailor the already normalised and flattened normalised model to the specified 
	 * target solver. This is the final step in the translation process.
	 * 
	 * @param targetSolver
	 * @return
	 */
	public boolean tailorTo(TargetSolver targetSolver) {
		
		if(this.normalisedModel == null) {
			this.errorMessage = errorMessage.concat("Please normalise problem model before flattening");
			return false;
		}
		if(this.normalisedModelHasBeenFlattened) {
			try{ 
				long startTime = System.currentTimeMillis();
				//System.out.println("Starting to tailor the stuff");
				this.targetSolverInstance = this.tailor.tailor(this.normalisedModel, settings.targetSolver);
				if(this.settings.giveTranslationTimeInfo) {
					long stopTime = System.currentTimeMillis();
					writeTimeInfo("Tailoring Time: "+(stopTime - startTime)/1000.0+"sec");
				}
				if(this.settings.getCseDetails())
					this.cseInfo = this.tailor.getCseInfo();
				return true;
				
			} catch (Exception e) {
				if(settings.debugMode)
					e.printStackTrace(System.out);
				this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
				return false;
			}
		}
		else { 
			this.errorMessage = this.errorMessage.concat("Please flatten the model before tailoring it to a target solver");
			return false;
		}
		
	}
	
	
	/** 
	 * This method performs the WHOLE translation process with full normalisation.
	 * The target solver instance/problem file is given in the field 
	 * 'targetSolverInstance' in String format. 
	 * 
	 * @param problemString
	 * @param parameterString
	 * @param targetSolver
	 * @return
	 */
	public boolean tailorTo(String problemString, 
			                String parameterString, 
			                TargetSolver targetSolver) {
		
		try {
			
			// ---- 1. first parse the problem and parameter string
			long startTime = System.currentTimeMillis();
			this.parser = new EssencePrimeParser(new EssencePrimeLexer
				(new StringReader(problemString)) );
			this.problemSpecification = (EssencePrimeModel) parser.parse().value;
			if(parser.hadErrorRecovery) {
				//System.out.println("Error recovery. Stop translation NOW.");
				this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
				return false;
			}
			
			
			this.parser = new EssencePrimeParser(new EssencePrimeLexer
					(new StringReader(parameterString)) );
				
			this.parameterSpecification = (EssencePrimeModel) parser.parse().value;
			if(parser.hadErrorRecovery) {
				this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
				return false;
			}
			
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Parse Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			
			// ---- 2. then normalise the essence' problem model
			//System.out.println("Problem spec after parsing:"+this.problemSpecification);
			startTime = System.currentTimeMillis();
			Normaliser normaliser = new Normaliser();
			this.normalisedModel = normaliser.normalise(this.problemSpecification,
					                                    this.parameterSpecification,
					                                    NormaliserSpecification.NORMALISE_FULL);
			if(this.settings.getPropagateSingleIntRanges())
				this.normalisedModel.propagateSingleRangeDecisionVariables();
			
			//System.out.println("Problem spec after normalisation:"+normalisedModel);
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Normalisation Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			//System.out.println("Normalisation Time: "+(System.currentTimeMillis() - startTime)/1000.0+"sec");
			
			// ---- 3. flatten the essence' problem model according to the specified solver
			startTime = System.currentTimeMillis();
			this.settings.setTargetSolver(targetSolver);
			this.tailor = new Tailor(this.normalisedModel,
	 				                 this.settings);
			this.normalisedModel = tailor.flattenModel();			
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Flattening Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			
			// ---- 4. tailor the essence' problem model to the target solver's input format
			startTime = System.currentTimeMillis();
			this.targetSolverInstance = this.tailor.tailor(this.normalisedModel, settings.targetSolver);
			if(this.settings.giveTranslationTimeInfo) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Tailoring Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			if(this.settings.getCseDetails())
				this.cseInfo = this.tailor.getCseInfo();
			return true;			
			
		}
		catch(Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = e.getMessage();
			//this.errorMessage = "\n"+this.parser.errorMessage;
			this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
			return false;
		}		
	}
	
	
	
	public String getEssenceSolution(String solverOutput) {
		
		try {
			return this.tailor.getEssenceSolution(solverOutput);
			
		} catch (Exception e) {
			if(settings.debugMode)
				e.printStackTrace(System.out);
			this.errorMessage = e.getMessage();
			//System.out.println(errorMessage);
			return "";
		}
		
	
	}
	
	
	/**
	 * Method to determine if the problem model has already been normalised.
	 * @return true if the problem model has been normalised
	 */
	public boolean hasBeenNormalised() {
		return (this.normalisedModel == null) ?
			false : true;
	}
	
	public boolean hasBeenFlattened() {
		return this.normalisedModelHasBeenFlattened;
	}
	
	// ====================== OUTPUT METHODS =====================================================
  
    /**
     * Returns the error message that has been given by the last exception. The error message
     * is no longer stored after returning it.
     * 
     * @return the error messages that has been collected after throwing the last exception
     */
    public String getErrorMessage() {
    	String error = this.errorMessage;
    	this.errorMessage = "";
    	return error;
    }
    
    
    public EssencePrimeModel getInitialProblemSpecification() {
    	return this.problemSpecification;
    }
	
    public EssencePrimeModel getInitialParameterSpecification() {
    	return this.parameterSpecification;
    }
    
	protected void print_debug(String message) {
		
		this.debug = debug.concat(" [ DEBUG translator ] "+message+"\n");
	}
	
	
	public String printInitialProblemSpecification() {
		return this.problemSpecification.toString();
	}
	
	public String printIntialParameterSpecification() {
		return this.parameterSpecification.toString();
	}
	
	/**
	 * Print the constraints in the current state they are in.
	 * @return
	 */
	public String printConstraints() {
		if(this.normalisedModel != null) {
			ArrayList<Expression> constraints = this.normalisedModel.getConstraints();
			StringBuffer s= new StringBuffer(constraints.get(0).toString());
			for(int i=1; i<constraints.size(); i++)
				s.append(",\n\t"+constraints.get(i));
			return s+"\n";
		}
		else {
			ArrayList<Expression> constraints = this.problemSpecification.getConstraints();
			StringBuffer s = new StringBuffer(constraints.get(0).toString());
			for(int i=1; i<constraints.size(); i++) 
				s.append(",\n\t"+constraints.get(i).toString());
			return s+"\n";
		}
	}
	
	/**
	 * Print the model in the current state that it is in.
	 * @return
	 */
	public String printAdvancedModel() {
		if(this.normalisedModel != null)
			return this.normalisedModel.toString();
		else return this.problemSpecification.toString();
	}
	
	/**
	 * Return the final translated problem model in the 
	 * target solver's input language
	 * 
	 * @return the final translated problem model in the 
	 * target solver's input language
	 */
	public String getTargetSolverInstance() {
		return this.targetSolverInstance;
	}
	
	
	public String[] getPrintedVariables() {
		return this.settings.getPrintedVariables();
	}
	
	public String getCseInfo() {
		return this.cseInfo;
	}
	
	private void writeTimeInfo(String info) {
		if(this.settings.giveTranslationInfo)
			System.out.println(info);
	}
}
