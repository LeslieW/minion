package translator.normaliser;

import java.util.HashMap;
import java.util.ArrayList;

import translator.expression.ArithmeticAtomExpression;
import translator.expression.ConstantArrayDomain;
import translator.expression.ConstantDomain;
import translator.expression.Domain;
import translator.expression.Expression;
import translator.expression.Variable;
import translator.expression.ConstantArray;
import translator.expression.Objective;
import translator.expression.SingleIntRange;
import translator.expression.BoundedIntRange;
import translator.expression.Conjunction;

public class NormalisedModel {

	/** parameter values that we might not have inserted yet
	because they occur in quantifications */
	public HashMap<String, ConstantArray> constantArrays;
	HashMap<String, int[]> constantOffsetsFromZero;
	HashMap<String, int[]> variableOffsetsFromZero;
	
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
	
	//public NormalisedModel() {}
	
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
		this.variableOffsetsFromZero = new HashMap<String,int[]>();
		this.subExpressions = new HashMap<String,ArithmeticAtomExpression> ();
		this.equalAtoms = new HashMap<String, ArithmeticAtomExpression>();
		this.replaceableVariables = new ArrayList<ArithmeticAtomExpression>();
		
		this.computeVariableArrayOffsets();
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
		this.variableOffsetsFromZero = new HashMap<String,int[]>();
		this.objective = objective;
		this.auxiliaryVariables = new ArrayList<Variable>();
		this.usedCommonSubExpressions = 0;
		this.subExpressions = new HashMap<String,ArithmeticAtomExpression> ();
		this.equalAtoms = new HashMap<String, ArithmeticAtomExpression>();
		this.replaceableVariables = new ArrayList<ArithmeticAtomExpression>();

		this.computeVariableArrayOffsets();
	}
	 
	// =============== METHODS =======================================


	public void evaluateDomains() {
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			Domain d = this.decisionVariables.get(decisionVariablesNames.get(i));
			d = d.evaluate();
			this.decisionVariables.put(this.decisionVariablesNames.get(i), d);
		}
		
	}
	
	
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
	
	
	public void setVariableOffsetsFromZero(HashMap<String, int[]> varOffsets) {
		this.variableOffsetsFromZero = varOffsets;
	}
	
	
	/**
	 * Returns the offset from zero of the variable array's 
	 * index 'dimension'. Hence, if a variable array's index 
	 * domains are 
	 * 
	 * x : [2..7, 1..4]
	 *    
	 * then the offset from zero at dimension 0 is 2 and 
	 * at dimension 1 is 1. 
	 * 
	 * @param varName
	 * @param dimension
	 * @return
	 * @throws NormaliserException
	 */
	public int getVariableOffsetAt(String varName, int dimension) 
		throws NormaliserException {
		
		int[] offsets = this.variableOffsetsFromZero.get(varName);
		
		if(offsets == null) 
			throw new NormaliserException("Trying to get offset from unknown decision variable array: "+varName);
		
		
		else if(dimension > offsets.length || dimension < 0) 
			throw new NormaliserException("Trying to get offset from zero of constant array '"+
					varName+"' at index out of bounds :"+dimension);
			
		return offsets[dimension];
	}
	

	public Variable getLastAddedAuxiliaryVariable() {
		if(this.auxiliaryVariables.size() ==0)
			return null;
		return this.auxiliaryVariables.get(this.auxiliaryVariables.size()-1);
	}
	
	/* public void removeLastAuxiliaryVariable() {
		if(this.auxiliaryVariables.size() > 0)
			this.auxiliaryVariables.remove(this.auxiliaryVariables.size()-1);
	}*/
	
	public void propagateSingleRangeDecisionVariables() {
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String varName = this.decisionVariablesNames.get(i);
			Domain domain = this.decisionVariables.get(varName);
			//System.out.println("Testing "+varName+"'s domain for singleIntRanges: "+domain+" with "+domain.getClass());
			if(domain instanceof SingleIntRange) {
				//System.out.println("We found a single int range: "+varName+" with "+domain);
				int value = ((SingleIntRange) domain).getSingleRange();
				for(int j=0; j<this.constraintList.size(); j++) {
					Expression constraint = constraintList.remove(j);
					constraint = constraint.insertValueForVariable(value, varName);
					//System.out.println("Inserted "+value+" for "+varName+" in constraint: "+constraint);
					constraint.orderExpression();
					constraint = constraint.evaluate();
					constraint = constraint.restructure();
					if(constraint instanceof Conjunction) {
						ArrayList<Expression> arguments = ((Conjunction) constraint).getArguments();
						//System.out.println("Gonna insert the arguments:"+arguments);
						int nbArgs = arguments.size();
						for(int k=0; k< nbArgs; k++ ) {
							//System.out.println("Adding constraint:"+arguments.get(0));
							this.constraintList.add(j, arguments.remove(0));
							
						}
						j = j + nbArgs - 1;
						
					}
					else constraintList.add(j, constraint);
				}
			}
		}
		
		//return false;
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
		if(this.objective.getObjectiveExpression() != null)
			this.objective.setObjectiveExpression(objExpression);
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
		return this.objective.getObjectiveExpression();
	}
	
	public boolean isObjectiveMaximising() {
		return this.objective.isMaximise();
	}
	
	public void setFlattenedObjectiveExpression(Expression objective) {
		this.objective.setObjectiveExpression(objective);
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
	
	public void deleteLastAuxVariable() {
		//System.out.println("Removing last aux var: "+
			//	this.auxiliaryVariables.get(this.auxiliaryVariables.size()-1));
		this.auxiliaryVariables.remove(this.auxiliaryVariables.size()-1);
	}
	
	public ArrayList<Variable> getAuxVariables() {
		return this.auxiliaryVariables;
	}
	
	public String toString() {
		
		// header
		StringBuffer s = new StringBuffer("ESSENCE' 1.0\n\n");
		
	
		// decision variables
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = decisionVariablesNames.get(i);
			s.append("find\t"+variableName+"\t: "+this.decisionVariables.get(variableName)+"\n");
		}
		
		
		s.append("\n\n");
		// auxiliary variables
		for(int i=0; i<this.auxiliaryVariables.size(); i++) {
			Variable auxVar = auxiliaryVariables.get(i);
			if(i%5 ==0) s.append("\n$");
			s.append("  "+auxVar+" : {"+auxVar.getDomain()[0]+", "+auxVar.getDomain()[1]+"}  ");
		}
		s.append("$\n"+printStatistics());
		
		
		// objective
		s.append("\n"+this.objective.toString()+"\n\n");
		
		// constraints
		s.append("such that\n");
		
		if(this.constraintList.size() == 0)
			return s.toString();
		
		for(int i=0; i<this.constraintList.size()-1; i++)
			s.append("\t"+constraintList.get(i)+",\n\n");
		s.append("\t"+constraintList.get(constraintList.size()-1)+"\n");
		
		return s.toString();
	}
	
public StringBuffer toStringBuffer() {
		
		// header
		StringBuffer s = new StringBuffer("ESSENCE' 1.0\n\n");
		
	
		// decision variables
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = decisionVariablesNames.get(i);
			s.append("find\t"+variableName+"\t: "+this.decisionVariables.get(variableName)+"\n");
		}
		
		
		s.append("\n\n");
		// auxiliary variables
		for(int i=0; i<this.auxiliaryVariables.size(); i++) {
			Variable auxVar = auxiliaryVariables.get(i);
			if(i%5 ==0) s.append("\n$");
			s.append("  "+auxVar+" : {"+auxVar.getDomain()[0]+", "+auxVar.getDomain()[1]+"}  ");
		}
		s.append("$\n"+printStatistics());
		
		
		// objective
		s.append("\n"+this.objective.toString()+"\n\n");
		
		// constraints
		s.append("such that\n");
		
		if(this.constraintList.size() == 0)
			return s;
		
		for(int i=0; i<this.constraintList.size()-1; i++)
			s.append("\t"+constraintList.get(i)+",\n\n");
		s.append("\t"+constraintList.get(constraintList.size()-1)+"\n");
		
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
	
	
	
	private void computeVariableArrayOffsets() {
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String varName = decisionVariablesNames.get(i);
			Domain domain = this.decisionVariables.get(varName);
			domain = domain.evaluate();
			
			//System.out.println("Adding index offset of variable array :"+varName);
			
			if(domain instanceof ConstantArrayDomain) {
				ConstantArrayDomain arrayDomain = (ConstantArrayDomain) domain;
				ConstantDomain[] indexDomains = arrayDomain.getIndexDomains();
				
				int[] offsetsFromZero = new int[indexDomains.length];
				for(int j=0; j<offsetsFromZero.length; j++) {
					int lb_j = indexDomains[j].getRange()[0];
					offsetsFromZero[j] = lb_j;
					
					if(indexDomains[j] instanceof SingleIntRange) {
						int range = ((SingleIntRange) indexDomains[j]).getSingleRange();
						indexDomains[j] = new SingleIntRange(range - offsetsFromZero[j]);
					}
					else if(indexDomains[j] instanceof BoundedIntRange) {
						int[] bounds = ((BoundedIntRange) indexDomains[j]).getRange();
						indexDomains[j] = new BoundedIntRange(bounds[0]-offsetsFromZero[j],
								                              bounds[1]-offsetsFromZero[j]);
					}
					
					
				}
				
				arrayDomain.setIndexDomains(indexDomains);
				
				//System.out.println("Adding index offset of variable array :"+varName);
				this.variableOffsetsFromZero.put(varName, offsetsFromZero);
				//System.out.println("Added index offset of variable array :"+varName);
				this.decisionVariables.put(varName, arrayDomain);
			}
		}
		
	}
	
	

	
}
