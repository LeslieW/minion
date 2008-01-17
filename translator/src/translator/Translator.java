package translator;

import java.io.StringReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import translator.conjureEssenceSpecification.EssenceSpecification;
import translator.essencePrimeParser.EssencePrimeLexer;
import translator.essencePrimeParser.EssencePrimeParser;

import translator.normaliser.*;
import translator.expression.Expression;
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
	
	
	// different types of normalisation
	public static final char NORMALISE_BASIC = 0;
	public static final char NORMALISE_EVAL = 1;
	public static final char NORMALISE_ORDER = 2;
	public static final char NORMALISE_CANCELLATION =3;
	public static final char NORMALISE_FULL = 4;
	
	
	/** Essence' parser */
    EssencePrimeParser parser ;
    /** Essence' syntax tree of problem file */
    EssenceSpecification problemSpecification;  
    /** Essence' syntax tree of parameter file */
    EssenceSpecification parameterSpecification ; 
	/** normalises the Essence' model */
    Normaliser normaliser;
    /** list of constraint expressions */
    ArrayList<Expression> constraintList;
    
	String errorMessage;
    String debug;
    
    
    
    // ========================= CONSTRUCTOR ========================================
    
	public Translator() {
		this.errorMessage = new String("");
		this.debug = "";
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
			this.problemSpecification = (EssenceSpecification) parser.parse().value;
			//print_debug(problemSpec.toString());
    	
			this.parser = new EssencePrimeParser(new EssencePrimeLexer
					(new StringReader(parameterString)) );
			this.parameterSpecification = (EssenceSpecification) parser.parse().value;
			//print_debug(parameterSpec.toString());
		}
		catch(Exception e) {
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
			this.problemSpecification = (EssenceSpecification) parser.parse().value;
			//print_debug(problemSpec.toString());
		}
		catch(Exception e) {
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
			
		
		// ====== first do the basic stuff ============
		try { 
			this.normaliser = new Normaliser(this.problemSpecification, this.parameterSpecification);
			this.constraintList = this.normaliser.normaliseBasic();	
		} catch(NormaliserException e) {
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		if(normaliseType == NORMALISE_BASIC) return true;
		
		
		switch(normaliseType) {
		
		case NORMALISE_EVAL:
			try {
				this.constraintList = this.normaliser.evaluateConstraints(constraintList);	
			} catch(NormaliserException e) {
				this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
				return false;
			}
			return true;
		
		case NORMALISE_ORDER:
			try { 
				this.constraintList = this.normaliser.orderConstraints(constraintList);
			
			} catch(NormaliserException e) {
				this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
				return false;
			}
		
		case NORMALISE_FULL:
			try {
				this.constraintList = this.normaliser.evaluateConstraints(constraintList);	
				this.constraintList = this.normaliser.orderConstraints(constraintList);
			} catch(NormaliserException e) {
				this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
				return false;
			}
			return true;			
		
			
			
		default: return true;   
		}
	
	}
	
  
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
    
	
	protected void print_debug(String message) {
		
		this.debug = debug.concat(" [ DEBUG translator ] "+message+"\n");
	}
	
	
	public String printInitialProblemSpecification() {
		return this.problemSpecification.toString();
	}
	
	public String printIntialParameterSpecification() {
		return this.parameterSpecification.toString();
	}
	
	public String printAdvancedModel() {
		return this.normaliser.printModel(this.constraintList);
	}
}
