package translator.tailor.minion;

import java.util.ArrayList;
import java.util.HashMap;
import translator.expression.*;
import translator.solver.Minion;

/**
 * Represents a Minion file in the MINION 3 format.
 * 
 * @author andrea
 *
 */

public class MinionModel {

	public final String MINION_HEADER = "MINION 3";
	
	ArrayList<MinionConstraint> constraintList;
	HashMap<String, ConstantDomain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	ArrayList<Variable> auxVariables;
	Minion solverSettings;
	
	// ========== CONSTRUCTOR =========================================
	public MinionModel(ArrayList<MinionConstraint> constraints,
			           HashMap<String, ConstantDomain> decisionVariables,
			           ArrayList<String> decisionVariablesNames,
			           ArrayList<Variable> auxVariables,
			           Minion solverFeatures) {
		
		this.constraintList = constraints;
		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.auxVariables = auxVariables;
		this.solverSettings = solverFeatures;
	}
	
	
	// =============== METHODS ========================================
	
	protected void addConstraint(MinionConstraint constraint) {
		this.constraintList.add(constraint);
	}
	
	public String toString() {
		
		String s = MINION_HEADER+"\n\n";
		
		s = s.concat(printVariables());
		
		s = s.concat("\n"+printConstraints());
		
		return s;
	}
	
	
	
	private String printConstraints() {
		
		String s = "\t**CONSTRAINTS**\n\n";
		
		for(int i=0; i<this.constraintList.size(); i++)
			s = s.concat(this.constraintList.get(i).toString()+"\n");
		
		return s;
	}
	
	
	private String printVariables()  {
		
		String s = "\t**VARIABLES**\n";
	
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String varName = decisionVariablesNames.get(i);
			ConstantDomain domain = this.decisionVariables.get(varName);
			String domainString = "";
			String rangeString = "";
			if(domain instanceof BoolDomain)
				domainString = "BOOL";
			
			else if(domain instanceof SparseIntRange) {
				domainString = "SPARSEBOUND";
				int[] range = ((SparseIntRange) domain).getFullDomain();
				rangeString = "{"+range[0];
				for(int j=1; j<range.length; j++)
					rangeString = rangeString.concat(", "+range[j]);
				rangeString = rangeString.concat("}");
			}
			
			else if(domain instanceof BoundedIntRange) {
				domainString = "BOUND";
				int[] range = ((BoundedIntRange) domain).getRange();
				rangeString = "{"+range[0]+", "+range[1]+"}";
			}
			else if(domain instanceof ConstantArrayDomain) {
				ConstantArrayDomain arrayDomain = (ConstantArrayDomain) domain; 
				ConstantDomain baseDomain = arrayDomain.getBaseDomain();
				
				// 1.get base domain: domain type and range
				if(baseDomain instanceof BoolDomain)
					domainString = "BOOL";
				else if(baseDomain instanceof SparseIntRange) {
					domainString = "SPARSEBOUND";
					int[] range = ((SparseIntRange) baseDomain).getFullDomain();
					rangeString = "{"+range[0];
					for(int j=1; i<range.length; j++)
						rangeString = rangeString.concat(", "+range[j]);
					rangeString = rangeString.concat("}");
				}
				else if(baseDomain instanceof BoundedIntRange) {
					domainString = "BOUND";
					int[] range = ((BoundedIntRange) baseDomain).getRange();
					rangeString = "{"+range[0]+", "+range[1]+"}";
				}
				
				// 2. get ranges of the array
				ConstantDomain[] indexDomains = arrayDomain.getIndexDomains();
				int[] ranges = new int[indexDomains.length];
				String indexString = "";
				
				for(int j=0; j<ranges.length; j++) {
					if(indexDomains[j] instanceof BoolDomain)
						ranges[j] = 2;
					else if(indexDomains[j] instanceof BoundedIntRange) {
						int[] range = ((BoundedIntRange) indexDomains[j]).getRange();
						ranges[j] = range[1]-range[0]+1; // ub, since we have already set the bound according to their offset from zero
					}
					// continue here when we have more different types of variables arrays (with defined ranges)
				}
				
				indexString = ranges[0]+"";
				for(int j=1; j<ranges.length; j++)
					indexString = indexString.concat(","+ranges[j]);
				varName = varName.concat("["+indexString+"]");
			}
			
			s = s.concat(domainString+" "+varName+" "+rangeString+"\n");
			
		}
		
		if(this.auxVariables.size() > 0)
			s = s.concat("\n# auxiliary variables\n");
		
		for(int i=0; i<this.auxVariables.size(); i++) {
			Variable auxVar = auxVariables.get(i);
			int[] range = auxVar.getDomain();
			String domainString = "";
			String rangeString = "";
			if(range[0] == 0 && range[1] == 1)
				domainString = "BOOL";
			
			else { // TODO: when do you make it a discrete bounds domain? 
				domainString = "BOUND";
				rangeString = "{"+range[0]+", "+range[1]+"}";
			}
			
			s = s.concat(domainString+" "+auxVar.getVariableName()+" "+rangeString+"\n");
			
		}
		
		
		return s;
	}
	
	
	
	protected void addAuxiliaryVariable(Variable auxVariable) {
		this.auxVariables.add(auxVariable);
	}
	
	
	protected boolean variableHasBooleanDomain(String variableName) {
		
		if(this.decisionVariables.containsKey(variableName)) {
			Domain domain = this.decisionVariables.get(variableName);
			return (domain instanceof BoolDomain);
		}
		else {
			for(int i=0; i<this.auxVariables.size(); i++) {
				if(this.auxVariables.get(i).getVariableName().equals(variableName)) {
					int[] bounds = auxVariables.get(i).getDomain();
					return (bounds[0] == 0 && bounds[1] == 1);
						
				}
			}
				
			
		}
		
		return false;
	}
	
	// ========== STATISTICAL DETAILS =================================
	
	protected int getAmountOfAuxiliaryVariables() {
		return this.auxVariables.size();
	}
	
	protected int getAmountOfConstraints() {
		return this.constraintList.size();
	}
}
