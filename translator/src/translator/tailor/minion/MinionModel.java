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
	public final int DISCRETE_UPPER_BOUND = 20;
	
	ArrayList<MinionConstraint> constraintList;
	HashMap<String, ConstantDomain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	ArrayList<String> variableAliases;
	ArrayList<Variable> auxVariables;
	Minion solverSettings;
	int usedCommonSubExpressions;
	MinionAtom objective;
	boolean maximising;
	
	
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
		this.usedCommonSubExpressions = 0;
		this.variableAliases = new ArrayList<String>();
	}
	
	
	// =============== METHODS ========================================
	
	
	public void setAmountOfUsedCommonSubExpressions(int noCommonSubExpressionsUsed) {
		this.usedCommonSubExpressions = noCommonSubExpressionsUsed;
	}
	
	public void setObjective(MinionAtom objective, boolean isMaximising) {
		this.objective = objective;
		this.maximising = isMaximising;
	}
	
	protected void addConstraint(MinionConstraint constraint) {
		this.constraintList.add(constraint);
	}
	
	public String toString() {
		
		String s = MINION_HEADER+"\n\n";
		s = s.concat("# This file has been automatically generated by the Essence' translator.\n");
		s = s.concat("# bug-reports: andrea@cs.st-and.ac.uk\n\n");
		
		s = s.concat(printStatistics());
		
		s = s.concat(printVariables());
		
		s = s.concat(printSearch());
		
		s = s.concat("\n"+printConstraints());
		
		return s+"\n**EOF**\n";
	}
	
	
	private String printSearch() {
		
		String s = "**SEARCH**\n\n";
		
		if(this.objective != null) {
			s = (this.maximising) ? 
					 s.concat("MAXIMISING ")
					 : s.concat("MINIMISING ");
            s = s.concat(this.objective+"\n\n");					 
		}
		
		
		
		String print = "PRINT [";
		
		String decisionVarString = "";
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			if(i > 0) decisionVarString= decisionVarString.concat(",");
			if(i % 5 == 0) decisionVarString = decisionVarString.concat("\n");
			
			if(this.decisionVariables.get(this.decisionVariablesNames.get(i)).getType() != Domain.CONSTANT_ARRAY)
				decisionVarString = decisionVarString.concat("["+this.decisionVariablesNames.get(i)+"]");
			else decisionVarString = decisionVarString.concat(this.decisionVariablesNames.get(i));
		}
		print =print+decisionVarString+"]\n\n";	
		
		
		
		
		String varDecisionVarString = "";
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			if(i > 0) varDecisionVarString= varDecisionVarString.concat(",");
			if(i % 5 == 0) varDecisionVarString = varDecisionVarString.concat("\n");
			varDecisionVarString = varDecisionVarString.concat(this.decisionVariablesNames.get(i));
		}
		
		String varorder = "VARORDER ["+varDecisionVarString;
		
		String auxVarString = "";
		for(int i=0; i<this.auxVariables.size(); i++) {
			if(i > 0) auxVarString= auxVarString.concat(",");
			if(i % 5 == 0) auxVarString = auxVarString.concat("\n");	
			auxVarString = auxVarString.concat(this.auxVariables.get(i).toString());
		}
		
		
		if(!auxVarString.equals(""))
			varorder = varorder+","+auxVarString+"]\n\n";
		else varorder = varorder+"]";
		
		return s+print+varorder;
	}
	
	private String printStatistics() {
		
		return "# amount of common subexpressions used:"+this.usedCommonSubExpressions+"\n\n";
		
	}
	private String printConstraints() {
		
		String s = "**CONSTRAINTS**\n\n";
		
		for(int i=0; i<this.constraintList.size(); i++)
			s = s.concat(this.constraintList.get(i).toString()+"\n");
		
		return s;
	}
	
	
	private String printVariables()  {
		
		String s = "**VARIABLES**\n";
	
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
					rangeString = rangeString.concat(","+range[j]);
				rangeString = rangeString.concat("}");
			}
			
			else if(domain instanceof BoundedIntRange) {
				domainString = "DISCRETE";
				int[] range = ((BoundedIntRange) domain).getRange();
				rangeString = "{"+range[0]+".."+range[1]+"}";
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
						rangeString = rangeString.concat(","+range[j]);
					rangeString = rangeString.concat("}");
				}
				else if(baseDomain instanceof BoundedIntRange) {
					domainString = "DISCRETE";
					int[] range = ((BoundedIntRange) baseDomain).getRange();
					rangeString = "{"+range[0]+".."+range[1]+"}";
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
				if(range[1]-range[0] > this.DISCRETE_UPPER_BOUND)
					domainString = "BOUND";
				else domainString = "DISCRETE";
				rangeString = "{"+range[0]+".."+range[1]+"}";
			}
			
			s = s.concat(domainString+" "+auxVar.getVariableName()+" "+rangeString+"\n");
			
		}
		
		
		s = s.concat("\n# aliases\n");
		for(int i=0; i<this.variableAliases.size(); i++)
			s = s.concat(variableAliases.get(i)+"\n");
		
		
		return s+"\n";
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
	
	
	public void addAlias(String alias) {
		// only add aliases that are NOT already in the list
		for(int i=0; i<this.variableAliases.size();i++) {
			if(variableAliases.get(i).equals(alias))
				return;
		}
		
		this.variableAliases.add(alias);
	}
	
	
	
	public String getEssenceSolution(String output) {
		
		if(this.objective != null) 
			return getEssenceSolutionFromOptimisationProblem(output);
		
		StringBuilder solverOutputString = new StringBuilder(output);
		
		String solutionSpecification = "$ Solving statistics:\n$\n";
		String commentStuff = "";
		
		// collect all the stuff in the Minion comments
		int start = 0;
		boolean noSolution = true;	
		
		//System.out.println("This is the WHOLE BLOODY string (-9):"+solverOutputString.substring(0, solverOutputString.length()-9));
		
		while(start+8 < solverOutputString.length()) {
			
			//System.out.println("This is the bloody string we're testing: '"+solverOutputString.substring(start, start+3)+"' and: "+
					//solverOutputString.substring(start+6, start+9));
			// cut all out till we hit the solutions
			if(solverOutputString.substring(start, start+4).equals("Solu")) {
				 start++;
			}
			else if(solverOutputString.substring(start, start+4).equals("Solv")) {
				 start++;
			}
			else if(solverOutputString.substring(start, start+3).equals("Sol")) {
				commentStuff = solverOutputString.substring(0, start-1);
				solverOutputString = solverOutputString.delete(0, start-1);
				noSolution = false;
				break;
			}
			
			// problem is not solvable
			else if(solverOutputString.substring(start, start+3).equals("ble")  &&
					solverOutputString.substring(start+6, start+8).equals("no")) {
				//System.out.println("NO SOLUTION!!!");
				commentStuff = solverOutputString.substring(0, start+8);
				//solverOutputString = solverOutputString.delete(0, solverOutputString.length());
				noSolution = true;
				break;
			}
			else start++;
		}
		
		
		StringBuilder commentString = new StringBuilder(commentStuff);
		//System.out.println("This is the comment string:"+commentStuff);
		String interestingComments = "";
		int count = 0;
		int interestingCommentsStart = 0;

		
		// first get the first part of statistics
		for(int i=0; i<commentString.length(); i++) {
			//System.out.println("iterating through output at position:"+i+" which is:"+commentString.charAt(i));
			if(commentString.charAt(i) == '#') {
				commentString.deleteCharAt(i);
				commentString.insert(i, '$');
			}
			if(i+7 < commentString.length() && 
				 commentString.substring(i,i+7).equals("Parsing")) 
				interestingCommentsStart = i;
			if( i+7 < commentString.length() && 
				 (commentString.substring(i,i+7).equals("Parsing") ||
				   commentString.substring(i,i+5).equals("Setup") ||
				   commentString.substring(i,i+5).equals("First") || 
				   commentString.substring(i,i+7).equals("Initial"))){
				commentString.insert(i,'$');
				commentString.insert(i+1, ' ');
				if(count == 0) 
					start = i;
				count++;
				i = i+2;
			}
		}
		
		
		interestingComments = commentString.substring(interestingCommentsStart, commentString.length());
	
		
		String solutionString = "";
		
		// then map variables to solutions -> the solutionString holds all Solutions now
		if(!noSolution) {
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = this.decisionVariablesNames.get(i);
			ConstantDomain domain = this.decisionVariables.get(variableName);
			
			//System.out.println("Decision variable: "+variableName);
			
			// single element
			if(domain.getType() != Domain.CONSTANT_ARRAY) {
				int endlinePosition = 6;
				while(solverOutputString.charAt(endlinePosition) != '\n' &&
						solverOutputString.charAt(endlinePosition) != '\r' ) {
					endlinePosition++;
				}
				String solution = solverOutputString.substring(5, endlinePosition-1);
				solutionString = solutionString.concat("variable "+variableName+" is "+solution+", \n");
				//System.out.println("And this is the solution string:"+solutionString);
				solverOutputString = solverOutputString.delete(0, endlinePosition);
			}
			// array element
			else {
				ConstantDomain[] indexDomains = ((ConstantArrayDomain) domain).getIndexDomains();
				
				// we have a vector
				if(indexDomains.length == 1) {
					int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}
					
					String intList = solverOutputString.substring(5, 7);
					for(int j=8; j<endlinePosition; j++) {
						String number = "";
						while(solverOutputString.charAt(j) != ' ' &&
								solverOutputString.charAt(j) != '\n' &&
								   solverOutputString.charAt(j) != '\r') {
							number = number+solverOutputString.charAt(j);
							j++;
						}
						intList = intList.concat(", "+number);
					}
					
					solutionString = solutionString.concat("variable "+variableName+" is ["+intList+"],\n");
					solverOutputString = solverOutputString.delete(0, endlinePosition);
				}
				
				else if(indexDomains.length == 2) {
					int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}
					int amountOfRows = indexDomains[0].getRange()[1]-indexDomains[0].getRange()[0] + 1;
					
					String matrixString = "[ ";
					for(int row=0;row<amountOfRows; row++) {
						String intList = solverOutputString.substring(5, 7);
						for(int j=8; j<endlinePosition; j++) {
							intList = intList.concat(", "+solverOutputString.charAt(j));
							j++;
						}
						if(row >= 1)
							matrixString = matrixString.concat(",\n\t["+intList+"]");
						else matrixString = matrixString.concat("["+intList+"]");
						
						solverOutputString = solverOutputString.delete(0, endlinePosition);
			
					}
					matrixString = matrixString.concat(" ],\n");
					solutionString = solutionString+"variable "+variableName+" is "+matrixString;
				}
				else if(indexDomains.length == 3) {
					int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}
					
					
					
				}
			}
			
			
		}
		commentString = solverOutputString;
		} 
		else commentString = commentString.delete(0,interestingCommentsStart);
		
		
		
		
		// get the last statistics
		
		//System.out.println("Comment string before polishing:"+commentString);
		
		for(int i=0; i<commentString.length(); i++) {
			//System.out.println("iterating through output at position:"+i+" which is:"+commentString.charAt(i));
			if(commentString.charAt(i) == '#') {
				commentString.deleteCharAt(i);
				commentString.insert(i, '$');
			}
			if( i+7 < commentString.length() && 
				 (commentString.substring(i,i+7).equals("Solutio") ||
				   //commentString.substring(i,i+5).equals("Nodes") || 
				   commentString.substring(i,i+5).equals("Solve") ||
				   commentString.substring(i,i+7).equals("Problem") || 
				   commentString.substring(i,i+5).equals("Total"))){
				commentString.insert(i,'$');
				commentString.insert(i+1, ' ');
				i = i+2;
			}
			else if(i+6 < commentString.length() && 
					(commentString.substring(i,i+5).equals("\nTime") ||
							commentString.substring(i,i+6).equals("\nNodes"))) {
				commentString.insert(i+1,'$');
				commentString.insert(i+2, ' ');
				i = i+3;
			}
			
		}
		//System.out.println("Comment string AFTER polishing:"+commentString);
		
		if(noSolution)
			return "$ no solution found\n\n"+commentString;
		
		else solutionSpecification = solutionSpecification+interestingComments+commentString;
		
		solutionSpecification = solutionSpecification+"\n\n"+solutionString;
		// cut off the last ','
		solutionSpecification = solutionSpecification.substring(0,solutionSpecification.length()-2);
		return solutionSpecification;
	
	}
	
	
	
	
	
	private String getEssenceSolutionFromOptimisationProblem(String output) {
		
		StringBuffer solverOutput = new StringBuffer(output);
		String essence = "$ solving statistics\n$\n";
		
		int i=0;
		
		while(i+7<solverOutput.length() ) {
			if(solverOutput.substring(i, i+7).equals("Parsing")) {
				solverOutput = solverOutput.delete(0, i-1);
				break;
			}
			else i++;
		}
		//		 check: if there where no solutions
		if(i+7==solverOutput.length()) {
			return essence+"$ no solutions\n";
		}
		
		i = 0;
		while(i+16<solverOutput.length()) {
			
			if(solverOutput.substring(i, i+16).equals("found with Value")) {
				while(solverOutput.charAt(i+16) != '\n' && 
						i+16 < solverOutput.length()) {
					i++;
				}
				System.out.println("translating Minion part to Essence:"+solverOutput.substring(0, i+16)+"\n\n\n");
				
				essence = essence+printEssenceOptimisationSolution(solverOutput.substring(0,i+16))+"\n\n$------------------------\n";
				solverOutput = solverOutput.delete(0, i+16);
				i=0;
			}
			else i++;
		}
		
		StringBuffer commentString = solverOutput;
		
		for(i=0; i<commentString.length(); i++) {
			//System.out.println("iterating through output at position:"+i+" which is:"+commentString.charAt(i));
			if(commentString.charAt(i) == '#') {
				commentString.deleteCharAt(i);
				commentString.insert(i, '$');
			}
			if( i+7 < commentString.length() && 
				 (commentString.substring(i,i+7).equals("Solutio") ||
				   //commentString.substring(i,i+5).equals("Nodes") || 
				   commentString.substring(i,i+5).equals("Solve") ||
				   commentString.substring(i,i+7).equals("Problem") || 
				   commentString.substring(i,i+5).equals("Total"))){
				commentString.insert(i,'$');
				commentString.insert(i+1, ' ');
				i = i+2;
			}
			else if(i+6 < commentString.length() && 
					(commentString.substring(i,i+5).equals("\nTime") ||
							commentString.substring(i,i+6).equals("\nNodes"))) {
				commentString.insert(i+1,'$');
				commentString.insert(i+2, ' ');
				i = i+3;
			}
			
		}
		
		return essence+"\n"+commentString;
	}
	
	
	private String printEssenceOptimisationSolution(String solverOutput) {
		
		StringBuffer output = new StringBuffer(solverOutput);
		String essence = "";
		
		int solutionStart = 0;
		int count = 0;
		
//		 first get the first part of statistics
		for(int i=0; i<output.length(); i++) {
			//System.out.println("iterating through output at position:"+i+" which is:"+commentString.charAt(i));
			if(output.charAt(i) == '#') {
				output.deleteCharAt(i);
				output.insert(i, '$');
			}
			if( i+7 < output.length() && 
				 (output.substring(i,i+7).equals("Parsing") ||
				   output.substring(i,i+5).equals("Setup") ||
				   output.substring(i,i+5).equals("First") || 
				   output.substring(i,i+7).equals("Initial"))){
				output.insert(i,'$');
				output.insert(i+1, ' ');
				i = i+2;
			}
			if(i+3 < output.length() && 
					output.substring(i, i+3).equals("Sol") &&
					count == 0) {
				solutionStart = i;
				count++;
			}
		}
		
		if(solutionStart > 0) {
			essence = essence+output.substring(0,solutionStart-1)+"\n";
		}
		
		String solverOutputString = new String(output.substring(solutionStart));
		String solutionString = "";
		
		// then map variables to solutions -> the solutionString holds all Solutions now
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String variableName = this.decisionVariablesNames.get(i);
			ConstantDomain domain = this.decisionVariables.get(variableName);
			
			//System.out.println("Decision variable: "+variableName);
			
			// single element
			if(domain.getType() != Domain.CONSTANT_ARRAY) {
				int endlinePosition = 6;
				while(solverOutputString.charAt(endlinePosition) != '\n' &&
						solverOutputString.charAt(endlinePosition) != '\r' ) {
					endlinePosition++;
				}
				String solution = solverOutputString.substring(5, endlinePosition-1);
				solutionString = solutionString.concat("variable "+variableName+" is "+solution+", \n");
				//System.out.println("And this is the solution string:"+solutionString);
				solverOutputString = solverOutputString.substring(endlinePosition+1);
			}
			// array element
			else {
				ConstantDomain[] indexDomains = ((ConstantArrayDomain) domain).getIndexDomains();
				
				// we have a vector
				if(indexDomains.length == 1) {
					int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}
					
					String intList = solverOutputString.substring(5, 7);
					for(int j=8; j<endlinePosition; j++) {
						String number = "";
						while(solverOutputString.charAt(j) != ' ' &&
								solverOutputString.charAt(j) != '\n' &&
								   solverOutputString.charAt(j) != '\r') {
							number = number+solverOutputString.charAt(j);
							j++;
						}
						intList = intList.concat(", "+number);
					}
					
					solutionString = solutionString.concat("variable "+variableName+" is ["+intList+"],\n");
					solverOutputString = solverOutputString.substring(endlinePosition+1);
				}
				
				else if(indexDomains.length == 2) {
					int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}
					int amountOfRows = indexDomains[0].getRange()[1]-indexDomains[0].getRange()[0] + 1;
					
					String matrixString = "[ ";
					for(int row=0;row<amountOfRows; row++) {
						String intList = solverOutputString.substring(5, 6);
						
						for(int j=7; j<endlinePosition; j++) {
							String number = "";
							while(solverOutputString.charAt(j) != ' ' &&
									solverOutputString.charAt(j) != '\n' &&
									   solverOutputString.charAt(j) != '\r') {
								number = number+solverOutputString.charAt(j);
								j++;
							}
							intList = intList.concat(", "+number);
							//j++;
						}
						if(row >= 1)
							matrixString = matrixString.concat(",\n\t["+intList+"]");
						else matrixString = matrixString.concat("["+intList+"]");
						
						solverOutputString = solverOutputString.substring(endlinePosition+1);
			
					}
					matrixString = matrixString.concat(" ],\n");
					solutionString = solutionString+"variable "+variableName+" is "+matrixString;
				}
				else if(indexDomains.length == 3) {
					int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}
					
					
					
				}
				
			}
		}
			
		
		essence = essence+"\n"+solutionString;
		StringBuffer commentString = new StringBuffer(solverOutputString);
		// get the last statistics
		
		//System.out.println("Comment string before polishing:"+commentString);
		
		for(int i=0; i<commentString.length(); i++) {
			//System.out.println("iterating through output at position:"+i+" which is:"+commentString.charAt(i));
			if(commentString.charAt(i) == '#') {
				commentString.deleteCharAt(i);
				commentString.insert(i, '$');
			}
			if( i+7 < commentString.length() && 
				 (commentString.substring(i,i+7).equals("Solutio") ||
				   //commentString.substring(i,i+5).equals("Nodes") || 
				   commentString.substring(i,i+5).equals("Solve") ||
				   commentString.substring(i,i+7).equals("Problem") || 
				   commentString.substring(i,i+5).equals("Total"))){
				commentString.insert(i,'$');
				commentString.insert(i+1, ' ');
				i = i+2;
			}
			else if(i+6 < commentString.length() && 
					(commentString.substring(i,i+5).equals("\nTime") ||
							commentString.substring(i,i+6).equals("\nNodes"))) {
				commentString.insert(i+1,'$');
				commentString.insert(i+2, ' ');
				i = i+3;
			}
			
		}
		
		return essence+commentString;
				
	}
	
	// ========== STATISTICAL DETAILS =================================
	
	protected int getAmountOfAuxiliaryVariables() {
		return this.auxVariables.size();
	}
	
	protected int getAmountOfConstraints() {
		return this.constraintList.size();
	}
}
