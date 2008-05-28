package translator.xcsp2ep.mapper;

import java.util.HashMap;
import java.util.ArrayList;

import translator.expression.Domain;
import translator.expression.BoolDomain;
import translator.expression.Expression;
import translator.expression.Variable;
import translator.expression.ConstantArray;
import translator.expression.Objective;
import translator.normaliser.NormalisedModel;

public class EssencePModel {

	public final String ESSENCE_P_VERSION = "language ESSENCE' 1.b.a";
	
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
	
	int usedCommonSubExpressions;
	int usedEqualSubExpressions;
	
	//=============== CONSTRUCTORS ==================================
	
	public EssencePModel(HashMap<String, Domain> decisionVariables,
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
	}
	
	public EssencePModel(HashMap<String, Domain> decisionVariables,
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
	}
	 
	// =============== METHODS =======================================


	public NormalisedModel mapToNormalisedModel() {
		
		//System.out.println("Mapping to normalised model");
		
		NormalisedModel model = new NormalisedModel(this.decisionVariables,
				                   this.decisionVariablesNames,
				                   this.constraintList,
				                   new Objective());
		 /*return new NormalisedModel(this.decisionVariables,
									  this.decisionVariablesNames,
									  this.constraintList,
									  this.objective);
	
														 */
		
		/* public NormalisedModel(HashMap<String, Domain> decisionVariables,
				   ArrayList<String> decisionVariablesNames,
				   ArrayList<Expression> constraints,
				   Objective objective) {
				   */
		
		
		model.evaluateDomains();
		//System.out.println("Returning model");
		return model;
	}
	

	
	public ArrayList<String> getDecisionVariablesNames() {
		return this.decisionVariablesNames;
	}
	
	public ArrayList<Expression> getConstraints() {
		return this.constraintList;
	}
	

	public String toString() {
		
		// header
		StringBuffer s = new StringBuffer(this.ESSENCE_P_VERSION+"\n\n");
		
	
		// decision variables
		ArrayList<String> booleanVariables = new ArrayList<String>();
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = decisionVariablesNames.get(i);
			if(this.decisionVariables.get(variableName) instanceof BoolDomain)
				booleanVariables.add(variableName);
			else s.append("find\t"+variableName+"\t: "+this.decisionVariables.get(variableName)+"\n");
		}
		if(booleanVariables.size() > 0) {
			s.append("find\t"+booleanVariables.get(0));
			for(int i=1; i<booleanVariables.size(); i++) {
				if(i % 20 == 0) 
					s.append(",\n\t\t"+booleanVariables.get(i));
				else 
					s.append(", "+booleanVariables.get(i));
			}
			s.append(" : bool\n");
		}
		
		s.append("\n");
		// auxiliary variables
		for(int i=0; i<this.auxiliaryVariables.size(); i++) {
			Variable auxVar = auxiliaryVariables.get(i);
			if(i%5 ==0) s.append("\n$");
			s.append("  "+auxVar+" : {"+auxVar.getDomain()[0]+", "+auxVar.getDomain()[1]+"}  ");
		}
		s.append(printStatistics()+"\n");
		
		
		// objective
		if(!this.objective.isEmptyObjective())
			s.append("\n"+this.objective.toString()+"\n");
		
		// constraints
		s.append("such that\n");
		
		if(this.constraintList.size() == 0)
			return s.toString();
		
		for(int i=0; i<this.constraintList.size()-1; i++)
			s.append("\t"+constraintList.get(i)+",\n\n");
		s.append("\t"+constraintList.get(constraintList.size()-1)+"\n");
		
		return s.toString();
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
