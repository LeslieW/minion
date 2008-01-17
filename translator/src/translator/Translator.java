package translator;

//import java.io.FileReader;
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
 * 1. parsing<br>
 * 2. normalising<br>
 * 3. flattening<br>
 * 4. tailoring<br>
 * 
 * @author angee
 *
 */

public class Translator {
	
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
	
	
	public boolean parseAndBasicNormalisation(String problemString, String parameterString) {
		
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
			this.normaliser = new Normaliser(this.problemSpecification, this.parameterSpecification);
			this.constraintList = this.normaliser.normaliseBasic();
		}
		catch(Exception e) {
			this.errorMessage = this.errorMessage.concat("\n"+this.parser.errorMessage);
			return false;
		}
    	return true;
		
	}
	
	
	
	public String printEssenceSpecification() {
		return this.problemSpecification.toString();
	}
	
	public String printParameterSpecification() {
		return this.parameterSpecification.toString();
	}
	
	public String printAdvancedModel() {
		
		return this.normaliser.printModel(this.constraintList);
	}
	
	/*public boolean parse(String problemFileName, String parameterFileName) 
	throws FileNotFoundException {
	
	try {
		
		this.parser = new EssencePrimeParser(new EssencePrimeLexer
				(new FileReader(problemFileName)) );
		this.problemSpecification = (EssenceSpecification) parser.parse().value;
		//print_debug(problemSpec.toString());
	
		this.parser = new EssencePrimeParser(new EssencePrimeLexer
				(new FileReader(parameterFileName)) );
		this.parameterSpecification = (EssenceSpecification) parser.parse().value;
		//print_debug(parameterSpec.toString());
	}
	catch(Exception e) {
		this.errorMessage = e.getMessage();
		return false;
	}
	return true;
	
} */
	
	
	
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
	 * Normalises the parsed list of expressions. This method provides FULL normalisation,
	 * which includes parameter insertion, ordering but also general model-enhancing reformulations 
	 * such as evaluation, and reduction of the expression tree.
	 * 
	 * @return true if the normalisation process worked out and the constraint list has been
	 * stored in the member constraintList, otherwise the errorMessage can be found in the field errorMessage
	 */
    public boolean normalise() {
		
		try { // it does not matter if parameterSpec is null!
			this.normaliser = new Normaliser(this.problemSpecification, this.parameterSpecification);
			this.constraintList = this.normaliser.normalise();
			
		} catch(NormaliserException e) {
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		
		return true;
	}
	
    /**
	 * Normalises the parsed list of expressions. This method provides BASIC normalisation,
	 * which includes ONLY parameter insertion and no model-enhancing reformulations.
	 * 
	 * @return true if the normalisation process worked out and the constraint list has been
	 * stored in the member constraintList, otherwise the errorMessage can be found in the field errorMessage
	 */
    public boolean normaliseBasic() {
		try { 
			this.normaliser = new Normaliser(this.problemSpecification, this.parameterSpecification);
			this.constraintList = this.normaliser.normaliseBasic();
			
		} catch(NormaliserException e) {
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		
		return true;    	
    }
    
    /**
     * Provide basic normalisation which includes evaluation and 
	 *  parameter insertion but no ordering or reduction of expression trees.
	 * 
     * 
     * @return
     */
    public boolean normaliseEvaluate() {
    	
    	try { 
			this.normaliser = new Normaliser(this.problemSpecification, this.parameterSpecification);
			this.constraintList = this.normaliser.normaliseBasic();
			this.constraintList = this.normaliser.evaluateConstraints(constraintList);
			
		} catch(NormaliserException e) {
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		
		return true;    
    	
    }
    
    
    /**
     * Provide basic normalisation which includes evaluation and 
	 *  parameter insertion but no ordering or reduction of expression trees.
	 * 
     * 
     * @return
     */
    public boolean normaliseOrder() {
    	
    	try { 
			this.normaliser = new Normaliser(this.problemSpecification, this.parameterSpecification);
			this.constraintList = this.normaliser.normaliseBasic();
			this.constraintList = this.normaliser.reduceExpressions(constraintList);
			this.constraintList = this.normaliser.orderConstraints(constraintList);
			
		} catch(NormaliserException e) {
			this.errorMessage = errorMessage.concat(e.getMessage()+"\n");
			return false;
		}
		
		return true;    
    	
    }

    public String getErrorMessage() {
    	String error = this.errorMessage;
    	this.errorMessage = "";
    	return error;
    }
    
	
	protected void print_debug(String message) {
		
		this.debug = debug.concat(" [ DEBUG translator ] "+message+"\n");
	}
	
}
