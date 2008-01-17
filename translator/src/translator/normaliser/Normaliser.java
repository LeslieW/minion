package translator.normaliser;

import java.util.ArrayList;
import java.util.HashMap;

import translator.conjureEssenceSpecification.EssenceSpecification;
import translator.conjureEssenceSpecification.Domain;
import translator.expression.Expression;

public class Normaliser implements NormaliserSpecification {

	ParameterInsertion parameterInserter;
	EssenceSpecification problemSpecification;
	EssenceSpecification parameterSpecification;
	
	ExpressionMapper expressionMapper;
	
	String errorMessage;
	String debugMessage;
	
	//=========================== CONSTRUCTOR =====================================================================
	
	public Normaliser(EssenceSpecification problemSpecification) {
		
		this.problemSpecification = problemSpecification;
		this.parameterInserter = new ParameterInsertion();
		this.debugMessage = new String("");
		this.errorMessage = new String("");
	}
	
	public Normaliser(EssenceSpecification problemSpecification,
		           	EssenceSpecification parameterSpecification) {
		
		this.problemSpecification = problemSpecification;
		this.parameterSpecification = parameterSpecification;
		this.parameterInserter = new ParameterInsertion();
		this.debugMessage = new String("");
		this.errorMessage = new String("");

	}
	
	  
	
	///=========================== INTERFACED METHODS ===============================================================
	
	
	/**
	 * General method to normalise the given problem- and parameter specification. Returns a 
	 * normalised model containing normalised constraints, a list of parameters that have not
	 * yet been inserted (because they appear in unenrolled quantifications) and a list of
	 * decision variables and their corresponding domain.
	 *  
	 * @return a normalised model
	 */
	public NormalisedModel normalise() throws NormaliserException {
		
		ArrayList<Expression> constraintsList = normaliseConstraints();
		//if(this.expressionMapper != null) -> not necessary since normalisation creates an expressionMapper
		ArrayList<String> decisionVariablesNames = this.parameterInserter.getDecisionVariablesNames();
		Objective objective = this.expressionMapper.mapObjective(
                this.parameterInserter.insertParametersInObjective(
                		      this.problemSpecification.getObjective())
                		      );
		objective.normalise();
		
 		HashMap<String,translator.expression.Domain> decisionVariables = this.expressionMapper.getNewDecisionVariables(decisionVariablesNames);
		Parameters parameterArrays = this.parameterInserter.getParameters();
	
		
		return new NormalisedModel(decisionVariables,
				                   decisionVariablesNames,
				                   constraintsList,
				                   parameterArrays,
				                   objective);
	}
	
	/**
	 * Normalise the expressions only to a certain extent: either full, evaluate only,
	 * order only etc...
	 * 
	 * @return a normalised model
	 * 
	 */
	public NormalisedModel normalise(char normalisationType) throws NormaliserException {
		
		//	first insert parameters and map the expressions to the new format
		ArrayList<translator.expression.Expression> constraintsList = insertParametersAndMapExpression();
		Objective objective = this.expressionMapper.mapObjective(
				                                this.parameterInserter.insertParametersInObjective(
				                                		      this.problemSpecification.getObjective())
				                                		      );
		
		ArrayList<String> decisionVariablesNames = this.parameterInserter.getDecisionVariablesNames();
 		HashMap<String,translator.expression.Domain> decisionVariables = this.expressionMapper.getNewDecisionVariables(decisionVariablesNames);
		Parameters parameterArrays = this.parameterInserter.getParameters();
		
		
		
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
			objective.normalise();
		}
		
		return new NormalisedModel(decisionVariables,
                decisionVariablesNames,
                constraintsList,
                parameterArrays,
                objective);
	
	}
	
	
	public ArrayList<translator.expression.Expression> normaliseConstraints() throws NormaliserException { 
	
		// first insert parameters and map the expressions to the new format
		ArrayList<translator.expression.Expression> constraintList = insertParametersAndMapExpression();
		
		// 1. order expressions
		constraintList = orderConstraints(constraintList);
		
		// 2. reduce expressions
		constraintList = reduceExpressions(constraintList);
		
		// 3. evaluate expressions
		constraintList = evaluateConstraints(constraintList);
		
		// 4. re-order them
		constraintList = orderConstraints(constraintList);
		
		return constraintList;
	}
	
	
	


	
	
	public ArrayList<translator.conjureEssenceSpecification.Expression> insertParameters(EssenceSpecification problemSpecification,
			                                                                             EssenceSpecification parameterSpecification)
			throws NormaliserException {
		
		return parameterInserter.insertParameters(problemSpecification, parameterSpecification);
	}

	
	
	public ArrayList<Expression> mapExpressionList(ArrayList<translator.conjureEssenceSpecification.Expression> oldExpressionList)
			throws NormaliserException {
		
			HashMap<String, Domain> decisionVariables = this.parameterInserter.getOldDecisionVariables(this.problemSpecification);
			
			//ExpressionMapper expressionMapper = null;
			
			// if there were no parameters specified
			if(this.parameterSpecification ==null || 
					this.parameterSpecification.getDeclarations() == null ||
					  this.parameterSpecification.getDeclarations().length == 0)
				this.expressionMapper =  new ExpressionMapper(decisionVariables, 
					                                 	new HashMap<String, Domain> (), 
					                                 	this.parameterInserter.getParameters());
			// if there have been parameters specified
			else 
				this.expressionMapper = new ExpressionMapper(decisionVariables, 
						this.parameterInserter.getParameterDomainMap(), 
						this.parameterInserter.getParameters());
			
			
			ArrayList<Expression> newExpressionList = new ArrayList<Expression>();
			
			for(int i=0; i < oldExpressionList.size(); i++) {
				translator.expression.Expression mappedExpression = expressionMapper.mapExpression(oldExpressionList.get(i));
				mappedExpression.setIsNotNested();
				newExpressionList.add(mappedExpression);
			}
			
		return newExpressionList;
	}

	

	
	public String printModel(ArrayList<Expression> constraints) {
		
		String s = this.parameterInserter.prettyPrintDecisionVariables();
		s = s.concat("\n"+this.problemSpecification.getObjective().toString());
		
		s = s.concat("\n"+"such that\n");
		
		for(int i=0; i<constraints.size(); i++) 
			s = s.concat("\t"+constraints.get(i).toString()+"\n");
		
		return s;
		
	}
	
	
	public void clearParameters() {
		this.parameterInserter.clearParameters();
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

	
	/**
	 * Inserts parameters into the constraints (given by the EssenceSpec) and 
	 * maps the constraint expressions to the new representation. This step 
	 * ALWAYS has to be done during translation. If there are no parameters,
	 * there shoud be no problem.
	 * 
	 * @return the list of expression.Expressions with parameter values inserted in the 
	 *     expressions
	 * @throws NormaliserException
	 */
	private ArrayList<translator.expression.Expression> insertParametersAndMapExpression () 
		throws NormaliserException {
		

		ArrayList<translator.conjureEssenceSpecification.Expression> oldConstraints = null;

	    // 	if there are no parameters given
		if(this.parameterSpecification == null || 
				this.parameterSpecification.getDeclarations() == null ||
				   this.parameterSpecification.getDeclarations().length == 0) {
		
			oldConstraints = new ArrayList<translator.conjureEssenceSpecification.Expression>();
			for(int i=0; i<this.problemSpecification.getExpressions().length; i++)
				oldConstraints.add(this.problemSpecification.getExpressions()[i]);
		}
		else { // if there are some parameters given, insert them in the expressions

			oldConstraints = insertParameters(this.problemSpecification, 
					                          this.parameterSpecification);
		}
		
		ArrayList<translator.expression.Expression> constraintList = mapExpressionList(oldConstraints);
		return constraintList;
		
	}
	
	
	private ArrayList<Expression> evaluateConstraints(ArrayList<Expression> constraints) throws NormaliserException {
		
		reduceExpressions(constraints);
		
		for(int i=0; i<constraints.size(); i++) {
			constraints.add(i, constraints.remove(i).evaluate());
		}
		
		return reduceExpressions(constraints);	
	}
}
