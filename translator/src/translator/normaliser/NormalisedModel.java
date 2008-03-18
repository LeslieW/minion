package translator.normaliser;

import java.util.HashMap;
import java.util.ArrayList;

import translator.expression.ArithmeticAtomExpression;
import translator.expression.Domain;
import translator.expression.Expression;
import translator.expression.Variable;
import translator.expression.ConstantArray;

public class NormalisedModel {

	/** parameter values that we might not have inserted yet
	because they occur in quantifications */
	public HashMap<String, ConstantArray> constantArrays;
	HashMap<String, int[]> constantOffsetsFromZero;
	
	/** decision variables and their corresponding domain */
	HashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	
	/** constraints list */
	ArrayList<Expression> constraintList;
	
	/** the objective expression */
	Objective objective;
	
	/** auxiliary variables */
	ArrayList<Variable> auxiliaryVariables;
	
	/** normalised models subexpressions */
	HashMap<String,ArithmeticAtomExpression> subExpressions;
	
	HashMap<String,ArithmeticAtomExpression> equalAtoms;
	ArrayList<ArithmeticAtomExpression> replaceableVariables;
	
	int usedCommonSubExpressions;
	int usedEqualSubExpressions;
	
	//=============== CONSTRUCTORS ==================================
	
	public NormalisedModel() {}
	
	public NormalisedModel(HashMap<String, Domain> decisionVariables,
						   ArrayList<String> decisionVariablesNames,
						   ArrayList<Expression> constraints,
						   Objective objective) {

		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.constraintList = constraints;
		this.objective = objective;
		this.auxiliaryVariables = new ArrayList<Variable>();
		this.usedCommonSubExpressions = 0;
		this.usedEqualSubExpressions = 0;
		this.constantArrays = new HashMap<String, ConstantArray>();
		this.constantOffsetsFromZero = new HashMap<String,int[]>();
		this.subExpressions = new HashMap<String,ArithmeticAtomExpression> ();
		this.equalAtoms = new HashMap<String, ArithmeticAtomExpression>();
		this.replaceableVariables = new ArrayList<ArithmeticAtomExpression>();
	}
	
	public NormalisedModel(HashMap<String, Domain> decisionVariables,
			               ArrayList<String> decisionVariablesNames,
			               ArrayList<Expression> constraints,
			               HashMap<String, ConstantArray> constantArrays,
			               HashMap<String,int[]> constantArrayOffsets,
			               Objective objective) {
		
		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.constraintList = constraints;
		this.constantArrays = constantArrays;
		this.constantOffsetsFromZero = constantArrayOffsets;
		
		this.objective = objective;
		this.auxiliaryVariables = new ArrayList<Variable>();
		this.usedCommonSubExpressions = 0;
		this.subExpressions = new HashMap<String,ArithmeticAtomExpression> ();
		this.equalAtoms = new HashMap<String, ArithmeticAtomExpression>();
		this.replaceableVariables = new ArrayList<ArithmeticAtomExpression>();
	}
	 
	// =============== METHODS =======================================


	public int getOffsetFromZeroAt(String arrayName, int index) 
		throws NormaliserException {
		
		int offsets[] = this.constantOffsetsFromZero.get(arrayName);
		
		// the arrayName is not a 
		if(offsets == null)
			throw new NormaliserException("Trying to get offset from zero of unknown constant array '"+
					arrayName+"' at index:"+index);
		
		if(index <0 || index >= offsets.length)
			throw new NormaliserException("Trying to get offset from zero of constant array '"+
					arrayName+"' at index out of bounds :"+index);
		
		return offsets[index];
	}
	
	
	
	

	public Variable getLastAddedAuxiliaryVariable() {
		if(this.auxiliaryVariables.size() ==0)
			return null;
		return this.auxiliaryVariables.get(this.auxiliaryVariables.size()-1);
	}
	
	public void removeLastAuxiliaryVariable() {
		if(this.auxiliaryVariables.size() > 0)
			this.auxiliaryVariables.remove(this.auxiliaryVariables.size()-1);
	}
	
	public int getAmountOfCommonSubExpressionsUsed() {
		return this.usedCommonSubExpressions;
	}
	
	public int getAmountOfEqualSubExpressionsUsed() {
		return this.usedEqualSubExpressions;
	}
	
	public ArrayList<ArithmeticAtomExpression> getReplaceableVariables() {
		return this.replaceableVariables;
	}
	
	/**
	 * Given the String representation of the original variable, we 
	 * return the equal variable, with which it is going to be replaced.
	 * 
	 * @param originalVariable
	 * @return
	 */
	public ArithmeticAtomExpression getReplacementFor(String originalVariable) {
		return this.equalAtoms.get(originalVariable);
	}
	
	public void setAmountOfCommonSubExpressionsUsed(int noCommonSubExpressionsUsed) {
		this.usedCommonSubExpressions = noCommonSubExpressionsUsed;
	}
	
	public void setAmountOfEqualSubExpressionsUsed(int number) {
		this.usedEqualSubExpressions = number;
	}
	
	public void setObjectiveExpression(Expression objExpression) {
		if(this.objective.objective != null)
			this.objective.objective = objExpression;
	}
	
	public void setSubExpressions(HashMap<String, ArithmeticAtomExpression> subexpressions) {
		this.subExpressions = subexpressions;
	}
	
	
	public void setEqualAtoms(HashMap<String, ArithmeticAtomExpression> equalAtoms,
			 				  ArrayList<ArithmeticAtomExpression> equalAtomsNames) {
		this.equalAtoms = equalAtoms;
		this.replaceableVariables = equalAtomsNames;
	}

	
	public HashMap<String, ArithmeticAtomExpression> getSubExpressions() {
		return this.subExpressions;
	}
	
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
		if(this.decisionVariables.containsKey(variableName))
			return this.decisionVariables.get(variableName);
		else return null;
	}
	
	public Expression getObjectiveExpression() {
		return this.objective.objective;
	}
	
	public boolean isObjectiveMaximising() {
		return this.objective.maximise;
	}
	
	public void setFlattenedObjectiveExpression(Expression objective) {
		this.objective.objective = objective;
	}
	
	public ArrayList<String> getDecisionVariablesNames() {
		return this.decisionVariablesNames;
	}
	
	public ArrayList<Expression> getConstraints() {
		return this.constraintList;
	}
	
	
	public void replaceConstraintsWith(ArrayList<Expression> newConstraints) {
		this.constraintList.clear();
		this.constraintList = newConstraints;
	}
	
	public void addAuxiliaryVariable(Variable variable) {
		this.auxiliaryVariables.add(variable);
	}
	
	public ArrayList<Variable> getAuxVariables() {
		return this.auxiliaryVariables;
	}
	
	public String toString() {
		
		// header
		String s = "ESSENCE' 1.0\n\n";
		
	
		// decision variables
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = decisionVariablesNames.get(i);
			s = s.concat("find\t"+variableName+"\t: "+this.decisionVariables.get(variableName)+"\n");
		}
		
		
		s = s.concat("\n\n");
		// auxiliary variables
		for(int i=0; i<this.auxiliaryVariables.size(); i++) {
			Variable auxVar = auxiliaryVariables.get(i);
			if(i%5 ==0) s = s.concat("\n$");
			s = s.concat("  "+auxVar+" : {"+auxVar.getDomain()[0]+", "+auxVar.getDomain()[1]+"}  ");
		}
		s = s.concat("$\n"+printStatistics());
		
		
		// objective
		s = s.concat("\n"+this.objective.toString()+"\n\n");
		
		// constraints
		s = s.concat("such that\n");
		
		if(this.constraintList.size() == 0)
			return s;
		
		for(int i=0; i<this.constraintList.size()-1; i++)
			s = s.concat("\t"+constraintList.get(i)+",\n\n");
		s= s.concat("\t"+constraintList.get(constraintList.size()-1)+"\n");
		
		return s;
	}
	
	
	private String printStatistics() {
		String s = "\n$ Statistical data about translation\n";
		s = s.concat("$ amount of subexpressions used: "+this.usedCommonSubExpressions+"\n");
		s = s.concat("$ amount of nested subexpressions used: "+this.usedEqualSubExpressions+"\n"); 
		return s;
	}
	
	
	public HashMap<String, Domain> getDecisionVariables() {
		return this.decisionVariables;
	}
}
