package translator.normaliser;

import java.util.ArrayList;
//import java.util.HashMap;

import translator.conjureEssenceSpecification.EssenceSpecification;
//import translator.conjureEssenceSpecification.Domain;
//import translator.conjureEssenceSpecification.ConstantArray;
import translator.expression.Expression;

public class Normaliser implements NormaliserSpecification {

	//ParameterInsertion parameterInserter;
	EssenceSpecification problemSpecification;
	EssenceSpecification parameterSpecification;
	
	ExpressionMapper expressionMapper;
	Preprocessor preprocessor;
	
	String errorMessage;
	String debugMessage;
	
	//=========================== CONSTRUCTOR =====================================================================
	
	public Normaliser(EssenceSpecification problemSpecification) {
		
		this.problemSpecification = problemSpecification;
		//this.parameterInserter = new ParameterInsertion();
		this.preprocessor = new Preprocessor();
		this.debugMessage = new String("");
		this.errorMessage = new String("");
	}
	
	public Normaliser(EssenceSpecification problemSpecification,
		           	EssenceSpecification parameterSpecification) {
		
		this.problemSpecification = problemSpecification;
		this.parameterSpecification = parameterSpecification;
		this.preprocessor = new Preprocessor();
		//this.parameterInserter = new ParameterInsertion();
		this.debugMessage = new String("");
		this.errorMessage = new String("");

	}
	
	  
	
	///=========================== INTERFACED METHODS ==insertParametersAndMapExpression();=============================================================
	
	
	/**
	 * General method to normalise the given problem- and parameter specification. Returns a 
	 * normalised model containing normalised constraints, a list of parameters that have not
	 * yet been inserted (because they appear in unenrolled quantifications) and a list of
	 * decision variables and their corresponding domain.
	 *  
	 * @return a normalised model
	 */
	public NormalisedModel normalise() throws NormaliserException,Exception {
		
		// collect decision variables, constants and  map expressions 
		NormalisedModel normalisedModel = this.preprocessor.proprocessModel(this.problemSpecification,
																			this.parameterSpecification);
		//normalisedModel = this.preprocessor.insertParameters(normalisedModel);
		
		Objective objective = normalisedModel.objective;
		
		
		ArrayList<Expression> constraintsList = normalisedModel.constraintList; 
		
		constraintsList = reduceExpressions(constraintsList);
		constraintsList = orderConstraints(constraintsList);
		constraintsList = evaluateConstraints(constraintsList);
		constraintsList = reduceExpressions(constraintsList);
		constraintsList = orderConstraints(constraintsList);
		constraintsList = restructureExpressions(constraintsList);
		objective.normalise();
		
		normalisedModel.constraintList = constraintsList;
		normalisedModel.objective = objective;
		
		return normalisedModel;
	}
	

	
	
	/**
	 * Normalise the expressions only to a certain extent: either full, evaluate only,
	 * order only etc...
	 * 
	 * @return a normalised model
	 * 
	 */
	public NormalisedModel normalise(char normalisationType) throws NormaliserException,Exception {
		
		// ------- 1.preprocess -------------------------
		NormalisedModel normalisedModel = this.preprocessor.proprocessModel(this.problemSpecification,
				this.parameterSpecification);
		
		//System.out.println("Preprocessed model");
		
		// --------2. insert constants and parameters --------------
		normalisedModel = this.preprocessor.insertParameters(normalisedModel);
		
		
		Objective objective = normalisedModel.objective;		
		ArrayList<Expression> constraintsList = normalisedModel.constraintList; 
		
		//ystem.out.println("Constraint list before normalisation:"+normalisedModel.constraintList);
		
		//--------3. normalise --------------------------------------
		if(normalisationType == NormaliserSpecification.NORMALISE_BASIC) {
			// do nothing
		}
		
		else if(normalisationType == NormaliserSpecification.NORMALISE_ORDER) {
			constraintsList = reduceExpressions(constraintsList);
			constraintsList = orderConstraints(constraintsList);
			objective.order();
	
		}
		else if(normalisationType == NormaliserSpecification.NORMALISE_EVAL) {
			constraintsList = reduceExpressions(constraintsList);
			constraintsList = evaluateConstraints(constraintsList);
			constraintsList = reduceExpressions(constraintsList);
			objective.evaluate();
		}
		else if(normalisationType == NormaliserSpecification.NORMALISE_FULL) {
			constraintsList = reduceExpressions(constraintsList);
			constraintsList = orderConstraints(constraintsList);
			constraintsList = evaluateConstraints(constraintsList);
			constraintsList = reduceExpressions(constraintsList);
			constraintsList = orderConstraints(constraintsList);
			constraintsList = restructureExpressions(constraintsList);
			objective.normalise();
		}
		
		// ----- 4. update model --------------------------
		normalisedModel.constraintList = constraintsList;
		normalisedModel.objective = objective;
	
		return normalisedModel;
	
	}

	

	/**
	 * 
	 * @param constraints
	 * @return
	 */
	public String printModel(ArrayList<Expression> constraints) {
		
		
		String s = "ESSENCE' 1.0\n\n"+this.preprocessor.prettyPrintDecisionVariables();
		s = s.concat("\n"+this.problemSpecification.getObjective().toString());
		
		s = s.concat("\n"+"such that\n");
		
		for(int i=0; i<constraints.size(); i++) {
			s = s.concat("\t"+constraints.get(i).toString());
			if(i<constraints.size()-1) s = s.concat(",");
			s = s.concat("\n");
		}
		return s;
		
	}
	
	
	public void clearParameters() {
		this.preprocessor.clearParameters();
	}
	
	
	                                                             
	   
	public String getDebugMessage() {
		String debug = new String(this.debugMessage);
		this.debugMessage = "";
		return debug;
	}
	
	protected void print_debug(String s) {
		this.debugMessage = debugMessage.concat(s);
	}
	
	
	// ===================== OTHER METHODS ===========================================
	
	
	private ArrayList<Expression> orderConstraints(
			ArrayList<Expression> constraints) throws NormaliserException {
		for(int i=0; i<constraints.size(); i++) {
			constraints.get(i).orderExpression();
		}
		return constraints;	
	}

	
	private ArrayList<Expression> reduceExpressions(
			ArrayList<Expression> constraints) throws NormaliserException {
		for(int i=0; i<constraints.size(); i++) {
			constraints.add(i, constraints.remove(i).reduceExpressionTree());
		}
		return constraints;	
	}

	
	private ArrayList<Expression> restructureExpressions(ArrayList<Expression> constraints) 
		throws NormaliserException {
		
		for(int i=0; i<constraints.size(); i++) {
		//	if(!(constraints.get(i) instanceof translator.expression.Sum))
				constraints.add(i, constraints.remove(i).restructure());
		}
		
		return constraints;
	}
	

	
	private ArrayList<Expression> evaluateConstraints(ArrayList<Expression> constraints) throws NormaliserException {
		
		reduceExpressions(constraints);
		
		for(int i=0; i<constraints.size(); i++) {
			constraints.add(i, constraints.remove(i).evaluate());
		}
		
		return reduceExpressions(constraints);	
	}
}
