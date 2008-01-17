package translator.normaliser;

import java.util.HashMap;
import java.util.ArrayList;
import translator.expression.Domain;
import translator.expression.Expression;

public class NormalisedModel {

	/** parameter values that we might not have inserted yet
	because they occur in quantifications */
	Parameters parameterArrays;
	
	/** decision variables and their corresponding domain */
	HashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	
	/** constraints list */
	ArrayList<Expression> constraintList;
	
	
	//=============== CONSTRUCTORS ==================================
	
	public NormalisedModel(HashMap<String, Domain> decisionVariables,
            ArrayList<String> decisionVariablesNames,
            ArrayList<Expression> constraints) {

		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.constraintList = constraints;

	}
	
	public NormalisedModel(HashMap<String, Domain> decisionVariables,
			               ArrayList<String> decisionVariablesNames,
			               ArrayList<Expression> constraints,
			               Parameters parameterArrays) {
		
		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.constraintList = constraints;
		this.parameterArrays = parameterArrays;
	}
	 
	// =============== METHODS =======================================

	/**
	 * Returns true, if there exists a decision variable with 
	 * the name variableName
	 */
	public boolean isDecisionVariable(String variableName) {
		return (this.decisionVariables.get(variableName) != null);
	}
	
	/**
	 * 
	 * @param variableName
	 * @return the domain of the variable with variableName. If there
	 * 	exists no variable with that name it returns null.  
	 */
	public Domain getDomainOfVariable(String variableName) {
		return this.decisionVariables.get(variableName);
	}
	
	
	public ArrayList<Expression> getConstraints() {
		return this.constraintList;
	}
	
	
	public String toString() {
		
		// header
		String s = "ESSENCE' 1.0\n\n";
		
		// decision variables
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = decisionVariablesNames.get(i);
			s = s.concat("find\t"+variableName+"\t: "+this.decisionVariables.get(variableName)+"\n");
		}
		
		// objective
		
		// constraints
		
		s = s.concat("such that\n");
		
		for(int i=0; i<this.constraintList.size()-1; i++)
			s = s.concat("\t"+constraintList.get(i)+",\n\n");
		s= s.concat("\t"+constraintList.get(constraintList.size()-1)+"\n");
		
		return s;
	}
}
