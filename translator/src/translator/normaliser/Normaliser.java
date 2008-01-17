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
	
	//ExpressionMapper expressionMapper;
	
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
	
	public ArrayList<translator.expression.Expression> normalise() throws NormaliserException { 
	
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
	
	
	public ArrayList<translator.expression.Expression> normaliseEvaluate() throws NormaliserException {
		ArrayList<translator.expression.Expression> constraintList = insertParametersAndMapExpression();
		return evaluateConstraints(constraintList);
	}
	
	
	public ArrayList<translator.expression.Expression> normaliseBasic() throws NormaliserException {
		return insertParametersAndMapExpression();
	}
	
	
	public ArrayList<Expression> evaluateConstraints(ArrayList<Expression> constraints) throws NormaliserException {
		
		for(int i=0; i<constraints.size(); i++) {
			constraints.add(i, constraints.remove(i).evaluate());
		}
		
		return reduceExpressions(constraints);	
	}

	
	
	public ArrayList<translator.conjureEssenceSpecification.Expression> insertParameters(EssenceSpecification problemSpecification,
			                                                                             EssenceSpecification parameterSpecification)
			throws NormaliserException {
		
		return parameterInserter.insertParameters(problemSpecification, parameterSpecification);
	}

	
	
	public ArrayList<Expression> mapExpressionList(ArrayList<translator.conjureEssenceSpecification.Expression> oldExpressionList)
			throws NormaliserException {
		
			HashMap<String, Domain> decisionVariables = this.parameterInserter.getDecisionVariables(this.problemSpecification);
			
			ExpressionMapper expressionMapper = null;
			
			// if there were no parameters specified
			if(this.parameterSpecification ==null || 
					this.parameterSpecification.getDeclarations() == null ||
					  this.parameterSpecification.getDeclarations().length == 0)
				expressionMapper =  new ExpressionMapper(decisionVariables, 
					                                 	new HashMap<String, Domain> ());
			// if there have been parameters specified
			else 
				expressionMapper = new ExpressionMapper(decisionVariables, 
						this.parameterInserter.getDecisionVariables(this.problemSpecification));
			
			
			ArrayList<Expression> newExpressionList = new ArrayList<Expression>();
			
			for(int i=0; i < oldExpressionList.size(); i++) {
				newExpressionList.add(expressionMapper.mapExpression(oldExpressionList.get(i)));
			}
			
		return newExpressionList;
	}

	
	public ArrayList<Expression> orderConstraints(
			ArrayList<Expression> constraints) throws NormaliserException {
		for(int i=0; i<constraints.size(); i++) {
			constraints.get(i).orderExpression();
		}
		return constraints;	
	}

	
	public ArrayList<Expression> reduceExpressions(
			ArrayList<Expression> constraints) throws NormaliserException {
		for(int i=0; i<constraints.size(); i++) {
			constraints.add(i, constraints.remove(i).reduceExpressionTree());
		}
		return constraints;	
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
		
			print_debug("Empty parameter spec.");
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
	                                                             
	   
	public String getDebugMessage() {
		String debug = new String(this.debugMessage);
		this.debugMessage = "";
		return debug;
	}
	
	protected void print_debug(String s) {
		this.debugMessage = debugMessage.concat(s);
	}
	
}
