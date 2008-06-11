package translator.tailor.minion;

import java.util.ArrayList;
import java.util.HashMap;
import translator.expression.*;
import translator.solver.Minion;
import translator.TranslationSettings;

/**
 * Represents a Minion file in the MINION 3 format.
 * 
 * @author andrea
 *
 */

public class MinionModel {

	public final String MINION_HEADER = "MINION 3";
	public final int DISCRETE_UPPER_BOUND = 200;
	public final String REDUCED_ARRAY_SUFFIX = "_ORIG";
	
	ArrayList<MinionConstraint> constraintList;
	HashMap<String, ConstantDomain> decisionVariables;
	HashMap<String, MinionAtom> equalAtoms;
	HashMap<String, Boolean> variableIsDiscrete = new HashMap<String, Boolean>();
	ArrayList<String> decisionVariablesNames;
	ArrayList<String> allOriginalDecisionVariables; // we need these to remember all the original variables
	HashMap<String, ConstantDomain> allDecisionVariablesDomains; // we need these to remeber original domains
	ArrayList<String> variableAliases;
	ArrayList<Variable> auxVariables;
	HashMap<String, Domain> auxVarDomains;
	Minion solverSettings;
	int usedCommonSubExpressions;
	int usedInferredEqualSubExpressions;
	MinionAtom objective;
	boolean maximising;
	TranslationSettings settings;
	
	HashMap<String, Boolean> variableAppearsInDiseqConstraint;
	
	// ========== CONSTRUCTOR =========================================
	public MinionModel(ArrayList<MinionConstraint> constraints,
			           HashMap<String, ConstantDomain> decisionVariables,
			           ArrayList<String> decisionVariablesNames,
			           ArrayList<Variable> auxVariables,
			           Minion solverFeatures, 
			           TranslationSettings settings) {
		
		this.constraintList = constraints;
		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.allOriginalDecisionVariables = (ArrayList<String>) decisionVariablesNames.clone();
		this.allDecisionVariablesDomains = (HashMap<String, ConstantDomain>) decisionVariables.clone();
		this.auxVariables = auxVariables;
		this.auxVarDomains = new HashMap<String, Domain>();
		for(int i=0; i<this.auxVariables.size(); i++) {
			Variable auxVar = auxVariables.get(i);
			int[] bounds = auxVar.getDomain();
			if(bounds[0] == 0 && bounds[1] == 1)
				this.auxVarDomains.put(auxVar.getVariableName(), 
						               new BoolDomain());
			else this.auxVarDomains.put(auxVar.getVariableName(), 
					                    new BoundedIntRange(bounds[0], bounds[1]));
		}
		
		this.solverSettings = solverFeatures;
		this.usedCommonSubExpressions = 0;
		this.usedInferredEqualSubExpressions = 0;
		this.variableAliases = new ArrayList<String>();
		this.settings = settings;
		this.equalAtoms = new HashMap<String,MinionAtom>();
		this.variableAppearsInDiseqConstraint = new HashMap<String, Boolean>();
	}
	
	
	// =============== METHODS ========================================
	
	
	public void setAmountOfUsedCommonSubExpressions(int noCommonSubExpressionsUsed) {
		this.usedCommonSubExpressions = noCommonSubExpressionsUsed;
	}
	
	public void setAmountOfUsedEqualSubExpressions(int noEqualSubExpressionsUsed) {
		this.usedInferredEqualSubExpressions = noEqualSubExpressionsUsed;
	}
	
	public void setVariableAppearsInDisequality(String variableName) {
		this.variableAppearsInDiseqConstraint.put(variableName, new Boolean(true));
	}
	
	public void setObjective(MinionAtom objective, boolean isMaximising) {
		this.objective = objective;
		this.maximising = isMaximising;
	}
	
	public void setEqualAtoms(HashMap<String, MinionAtom> equalAtoms) {
		this.equalAtoms = equalAtoms;
	}
	
	protected void addConstraint(MinionConstraint constraint) {
		this.constraintList.add(constraint);
	}
	
	protected void applyEqualAtoms() 
		throws MinionException {
		
		//System.out.println("Equal atoms are: "+equalAtoms);
		
		// if there are not equal atoms, do nothing
		if(this.equalAtoms.size() == 0)
			return;
		
		// check all decision variables for equal expressions
		for(int i=this.decisionVariablesNames.size()-1; i >= 0; i--) {
			
			// this hashmap maps every altered array to the amount of variables it has left (after removal)
			//HashMap<String,Integer> flattenReplacedArrayCount = new HashMap<String, Integer>();
			
			String replacedVarName = this.decisionVariablesNames.get(i);
			
			// in this case the replaced variable can only be a single variable and no array variable
			if(this.equalAtoms.containsKey(replacedVarName)) {
				MinionAtom newVariable = this.equalAtoms.get(replacedVarName);
				
				Domain replacedVarDomain = this.decisionVariables.get(replacedVarName);
				if(replacedVarDomain == null) throw new MinionException("Internal error: Unknown replacement variable: "+replacedVarName);
				
				// we are replacing a single variable
				else {
					// add a new alias
					StringBuffer alias = new StringBuffer("ALIAS ").append(replacedVarName+" = "+newVariable.toString());
					addAlias(alias.toString());
					// remove the replaced decision variable
					this.decisionVariablesNames.remove(i);
					this.decisionVariables.remove(replacedVarName);
				}
			}
			
			else {
				Domain domain = this.decisionVariables.get(replacedVarName);
				
				if(domain instanceof ConstantArrayDomain) {
					
					ConstantArrayDomain constantArrayDomain = (ConstantArrayDomain) domain;
					ConstantDomain[] indices  = constantArrayDomain.getIndexDomains();
					
					// -------------------we have a vector --------------------------------------------
					if(indices.length == 1) {
												
						int cols = (indices[0].getRange()[1] - indices[0].getRange()[0]) +1;
						// create an alias vector
						MinionAliasVector aliasVector = new MinionAliasVector(replacedVarName, cols);
						// change the name of the original vector
						String reducedOriginalArrayName = replacedVarName+this.REDUCED_ARRAY_SUFFIX;
						// add a counter for the original vector in the map
						int remainingElements = 0;
						
						boolean elementHasBeenReplaced = false;
						
						for(int col=0; col<cols; col++ ) {
							
							//System.out.println("checking for equal variables of:"+replacedVarName+"["+col+"] in equalAtoms:"+equalAtoms.toString());
							
							// this element is going to be replaced
							if(this.equalAtoms.containsKey(replacedVarName+"["+col+"]")) {
								// replace the element with the equal atom at this position in the alias vector
								String replacedElement = equalAtoms.get(replacedVarName+"["+col+"]").toString();
								//System.out.println("MinionAtom "+replacedVarName+"["+col+"]"+" equals replacing atom:"+replacedElement);
								
								// if we replace something from our own vector, we need to get the new name...
								if(replacedElement.startsWith(replacedVarName+"[")) {
									aliasVector.placeElementAt(replacedElement.replaceFirst(replacedVarName, reducedOriginalArrayName), col);
								}
								else aliasVector.placeElementAt(replacedElement, col);
								
								elementHasBeenReplaced = true;
							}
							// this element is NOT going to be replaced
							else {								
								// place the element into the alias array - using the name of the reduced array 
								aliasVector.placeElementAt(reducedOriginalArrayName+"["+remainingElements+"]", col);
								remainingElements++;
							}
						}
						
						if(elementHasBeenReplaced) {
							// add the alias vector as an alias for the replaced matrix
							String alias = "ALIAS "+replacedVarName+"["+cols+"] = "+aliasVector.toString();
							this.addAlias(alias);
							
							// remove the old decision variable
							this.decisionVariablesNames.remove(i);
							this.decisionVariables.remove(replacedVarName);
							
							// add the reduced array, in case there are any elements left
							if(remainingElements > 0) {
								this.decisionVariablesNames.add(i, reducedOriginalArrayName);
								this.decisionVariables.put(reducedOriginalArrayName, 
										                   new ConstantArrayDomain(new BoundedIntRange[] { new BoundedIntRange(0,remainingElements-1) },
										                		   			       constantArrayDomain.getBaseDomain()));
							}
						}
						else { // we have not removed anything, so keep things as they were
							aliasVector = null; // maybe this saves space in the compiler??
							
						}
					}
					// ------------------------we have a matrix ---------------------------------------------------
					else if(indices.length ==2) {
						
						
						HashMap<String, Integer> indexMappings = new HashMap<String,Integer>();
						
						int cols = (indices[0].getRange()[1] - indices[0].getRange()[0]) +1;
						int rows = (indices[1].getRange()[1] - indices[1].getRange()[0]) +1;
						
						// create an alias vector
						MinionAliasMatrix aliasMatrix = new MinionAliasMatrix(replacedVarName, rows,cols);
						// change the name of the original vector
						String reducedOriginalArrayName = replacedVarName+this.REDUCED_ARRAY_SUFFIX;
						// add a counter for the original vector in the map
						int remainingElements = 0;
						
						boolean elementHasBeenReplaced = false;
						
						
						for(int row =0; row<rows; row++) {
							for(int col=0; col<cols; col++ ) {
							
								//System.out.println("checking for equal variables of:"+replacedVarName+"["+row+", "+col+"] in equalAtoms:"+equalAtoms.toString());
							
							// this element is going to be replaced
								if(this.equalAtoms.containsKey(replacedVarName+"["+row+", "+col+"]")) {
								// replace the element with the equal atom at this position in the alias vector
									MinionAtom replacingAtom = equalAtoms.get(replacedVarName+"["+row+", "+col+"]");
									//System.out.println("MinionAtom "+replacedVarName+"["+col+"]"+" equals replacing atom: "+replacingAtom);
									String replacedElement = replacingAtom.getVariableName();
									
								// 	if we replace something from our own matrix, we need to get the new name and the new index
									if(replacedElement.equals(replacedVarName)) {
										
										//System.out.println("Replacing "+replacedVarName+"["+row+", "+col+"] with "+replacingAtom);
										int newIndex = indexMappings.get(replacingAtom.toString()); // the atom must have been already added since we order expressions
										String newVarName = reducedOriginalArrayName+"["+newIndex+"]";
										
										aliasMatrix.placeElementAt(newVarName, row,col);
									}
									else aliasMatrix.placeElementAt(replacingAtom.toString(), row,col);
								
									elementHasBeenReplaced = true;
								}
							// this element is NOT going to be replaced
								else {								
								// place the element into the alias array - using the name of the reduced array 
									aliasMatrix.placeElementAt(reducedOriginalArrayName+"["+remainingElements+"]",row, col);
									indexMappings.put(replacedVarName+"["+row+", "+col+"]", remainingElements);
									remainingElements++;
									
								}
							}
						}
						
						if(elementHasBeenReplaced) {
							// add the alias vector as an alias for the replaced matrix
							String alias = "ALIAS "+replacedVarName+"["+rows+","+cols+"] = "+aliasMatrix.toString();
							this.addAlias(alias);
							
							// remove the old decision variable
							this.decisionVariablesNames.remove(i);
							this.decisionVariables.remove(replacedVarName);
							
							// add the reduced array, in case there are any elements left
							if(remainingElements > 0) {
								this.decisionVariablesNames.add(i, reducedOriginalArrayName);
								this.decisionVariables.put(reducedOriginalArrayName, 
										                   new ConstantArrayDomain(new BoundedIntRange[] { new BoundedIntRange(0,remainingElements-1) },
										                		   			       constantArrayDomain.getBaseDomain()));
							}
						}
						else { // we have not removed anything, so keep things as they were
							aliasMatrix = null; // maybe this saves space in the compiler??
							
						}
						
						
						
					}
					// TODO: extend this! or at least make it feasible to pass for other 3-dim. arrays
					else throw new MinionException("Sorry, cannot perform direct variable usage on arrays larger than 2-dimensions yet, sorry:"+
							replacedVarName);
					
				}
			}
			
		}
		
		
		
	}
	
	
	public String toString() {
		
		//long startTime = System.currentTimeMillis();
		
		StringBuffer s = new StringBuffer(MINION_HEADER+"\n\n");
		s.append("#"+this.settings.OUTPUTFILE_HEADER+"\n");
		s.append("#"+this.settings.OUTPUTFILE_HEADER_BUGS+"\n\n");
		
		s.append(printStatistics()+"\n\n");
		//long stopTime = System.currentTimeMillis();
		//System.out.println("Statistics toString Time: "+(stopTime - startTime)/1000.0+"sec");
		
		//startTime = System.currentTimeMillis();
		s.append(printVariables());
		//stopTime = System.currentTimeMillis();
		//System.out.println("Variables toString Time: "+(stopTime - startTime)/1000.0+"sec");
		
		//startTime = System.currentTimeMillis();
		s.append(printSearch());
		//stopTime = System.currentTimeMillis();
		//System.out.println("Search toString Time: "+(stopTime - startTime)/1000.0+"sec");
		
		//startTime = System.currentTimeMillis();
		s.append("\n"+printConstraints());
		//stopTime = System.currentTimeMillis();
		//System.out.println("Constraints toString Time: "+(stopTime - startTime)/1000.0+"sec");
		
		/*if(this.printStatistics) {
			System.out.println("Amount of Auxiliary Variables:"+this.auxVariables.size());
			System.out.println("Amount of Common Subexpressions:"+this.usedCommonSubExpressions);
			System.out.println("Amount of inferred common subexpressions:"+this.usedInferredEqualSubExpressions);
			System.out.println("Amount of constraints:"+this.constraintList.size());
		}*/
		
		this.printModelStatistics();
		return s+"\n**EOF**\n";
	}
	
	
	private String printSearch() {
		
		StringBuffer s = new StringBuffer("**SEARCH**\n\n");
		
		if(this.objective != null) {
			if(this.maximising)
				s.append("MAXIMISING ");
			else s.append("MINIMISING ");
			
            s.append(this.objective+"\n\n");					 
		}
		
		
		
		StringBuffer print = new StringBuffer("PRINT [");
		
		StringBuffer decisionVarString = new StringBuffer();
		for(int i=0; i<this.allOriginalDecisionVariables.size(); i++) {
			if(i > 0) decisionVarString.append(",");
			if(i % 8 == 0) decisionVarString.append("\n");
			
			if(this.allDecisionVariablesDomains.get(this.allOriginalDecisionVariables.get(i)).getType() != Domain.CONSTANT_ARRAY)
				decisionVarString.append("["+this.allOriginalDecisionVariables.get(i)+"]");
			else decisionVarString.append(this.allOriginalDecisionVariables.get(i));
		}
		print.append(decisionVarString).append("]\n\n");	
		
		
		
		
		StringBuffer varDecisionVarString = new StringBuffer("");
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			if(i > 0) varDecisionVarString.append(",");
			if(i % 8 == 0) varDecisionVarString.append("\n");
			varDecisionVarString.append(this.decisionVariablesNames.get(i));
		}
		
		StringBuffer varorder = new StringBuffer("VARORDER ["+varDecisionVarString);
		
		StringBuffer auxVarString = new StringBuffer("");
		for(int i=0; i<this.auxVariables.size(); i++) {
			if(i > 0) auxVarString.append(",");
			if(i % 8 == 0) auxVarString.append("\n");	
			auxVarString.append(this.auxVariables.get(i).toString());
		}
		
		
		if(auxVarString.length() > 0) 
			varorder.append(",").append(auxVarString).append("]\n\n");
		
		else varorder.append("]\n\n");
		
		return s+print.toString()+varorder;
	}
	
	private String printStatistics() {
		
		return "# amount of common subexpressions used:"+this.usedCommonSubExpressions+"\n" +
				"# amount of inferred common subexpressions used:"+this.usedInferredEqualSubExpressions+"\n"+
				"# amount of original variables saved (over direct equality):"+this.equalAtoms.size()+"\n"+
				"# amount of constraints:"+this.constraintList.size()+"\n"+
				"# amount of auxiliary variables: "+this.auxVariables.size();
	}
	private String printConstraints() {
		
		StringBuffer s = new StringBuffer("**CONSTRAINTS**\n\n");
		
		
		
		for(int i=0; i<this.constraintList.size(); i++)
			s.append(this.constraintList.get(i).toString()+"\n");
		
		return s.toString();
	}
	
	
	private String printVariables()  {
		
		StringBuffer s = new StringBuffer("**VARIABLES**\n");
	
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			String varName = decisionVariablesNames.get(i);
			ConstantDomain domain = this.decisionVariables.get(varName);
			StringBuffer domainString = new StringBuffer("");
			StringBuffer rangeString = new StringBuffer("");
			
			//System.out.println("printing variable "+varName+" with domain "+domain+" and type :"+domain.getType());
			
			if(domain instanceof BoolDomain)
				domainString.append("BOOL");
			
			else if(domain instanceof SparseIntRange) {
				int[] range = ((SparseIntRange) domain).getFullDomain();
				int domainSize = range[range.length-1] - range[0] -1;
				if(domainSize <= settings.DISCRETE_UPPER_BOUND) {
					domainString.append("DISCRETE");
					rangeString.append("{"+range[0]+".."+range[range.length-1]+"}");
					// add diseqaulity constraints
					int value = range[0];
					for(int j=0; j<range.length; j++) {
						//System.out.println("Checking if range["+j+"]: "+range[j]+"=?"+value+ "(val) for var "+varName);
						if(range[j] != value) {
							while(range[j] != value) {
								this.constraintList.add(new DiseqConstraint(new MinionSingleVariable(varName),
																		new MinionConstant(value)));
							//System.out.println("Added diseq constraint: "+varName+" != "+value);
								value++;
							}
						}
						value++;
					}
				}
				else { // TODO: sometimes we cannot use sparse bound vars (tables)				
					domainString.append("SPARSEBOUND");
					rangeString.append("{"+range[0]);
					for(int j=1; j<range.length; j++)
						rangeString.append(","+range[j]);
					rangeString.append("}");
				}
			}
			
			else if(domain instanceof BoundedIntRange) {
				int[] range = ((BoundedIntRange) domain).getRange();
				if(range[1] - range[0] > settings.getDiscreteUpperBound())
					domainString.append("BOUND");
				else domainString.append("DISCRETE");
				rangeString.append("{"+range[0]+".."+range[1]+"}");
			}
			
			else if(domain instanceof SingleIntRange) {
				//domainString.append("SPARSEBOUND");
				domainString.append("DISCRETE");
				rangeString.append("{"+ ( (SingleIntRange) domain).getSingleRange() +".."+( (SingleIntRange) domain).getSingleRange()+"}");
			}
			
			else if(domain instanceof ConstantArrayDomain) {
				ConstantArrayDomain arrayDomain = (ConstantArrayDomain) domain; 
				ConstantDomain baseDomain = arrayDomain.getBaseDomain();
				
				// 1.get base domain: domain type and range
				if(baseDomain instanceof BoolDomain)
					domainString.append("BOOL");
				else if(baseDomain instanceof SparseIntRange) {
					domainString.append("SPARSEBOUND");
					int[] range = ((SparseIntRange) baseDomain).getFullDomain();
					rangeString.append("{"+range[0]);
					for(int j=1; i<range.length; j++)
						rangeString.append(","+range[j]);
					rangeString.append("}");
				}
				else if(baseDomain instanceof BoundedIntRange) {
					int[] range = ((BoundedIntRange) baseDomain).getRange();
					if(range[1] - range[0] > settings.getDiscreteUpperBound())
						domainString.append("BOUND");
					else domainString.append("DISCRETE");
					
					rangeString.append("{"+range[0]+".."+range[1]+"}");
				}
				
				// 2. get ranges of the array
				ConstantDomain[] indexDomains = arrayDomain.getIndexDomains();
				int[] ranges = new int[indexDomains.length];
				StringBuffer indexString = new StringBuffer("");
				
				for(int j=0; j<ranges.length; j++) {
					if(indexDomains[j] instanceof BoolDomain)
						ranges[j] = 2;
					else if(indexDomains[j] instanceof BoundedIntRange) {
						int[] range = ((BoundedIntRange) indexDomains[j]).getRange();
						ranges[j] = range[1]-range[0]+1; // ub, since we have already set the bound according to their offset from zero
					}
					else if(indexDomains[j] instanceof SingleIntRange)
						ranges[j] = 1;
					// continue here when we have more different types of variables arrays (with defined ranges)
				}
				
				indexString.append(ranges[0]+"");
				for(int j=1; j<ranges.length; j++)
					indexString.append(","+ranges[j]);
				varName = varName+("["+indexString+"]");
			}
			
			s.append(domainString+" "+varName+" "+rangeString+"\n");
			
		}
		
		if(this.auxVariables.size() > 0)
			s.append("\n# auxiliary variables\n");
		
		for(int i=0; i<this.auxVariables.size(); i++) {
			Variable auxVar = auxVariables.get(i);
			int[] range = auxVar.getDomain();
			StringBuffer domainString = new StringBuffer("");
			StringBuffer rangeString = new StringBuffer("");
			if(range[0] == 0 && range[1] == 1)
				domainString.append("BOOL");
			
			else { // TODO: when do you make it a discrete bounds domain? 
				if(range[1]-range[0] > this.DISCRETE_UPPER_BOUND)
					domainString.append("BOUND");
				else domainString.append("DISCRETE");
				rangeString.append("{"+range[0]+".."+range[1]+"}");
			}
			
			s.append(domainString+" "+auxVar.getVariableName()+" "+rangeString+"\n");
			
		}
		
		
		s.append("\n# aliases\n");
		for(int i=this.variableAliases.size()-1; i>= 0; i--)
			s.append(variableAliases.get(i)+"\n");
		
		
		return s+"\n";
	}
	
	
	
	protected void addAuxiliaryVariable(Variable auxVariable) {
		this.auxVariables.add(auxVariable);
		int[] bounds = auxVariable.getDomain();
		if(bounds[0] == 0 && bounds[1] == 1)
			this.auxVarDomains.put(auxVariable.getVariableName(), 
					               new BoolDomain());
		else this.auxVarDomains.put(auxVariable.getVariableName(), 
				                    new BoundedIntRange(bounds[0], bounds[1]));
	}
	
	
	protected boolean variableHasBooleanDomain(String variableName) {
		
		if(this.decisionVariables.containsKey(variableName)) {
			Domain domain = this.decisionVariables.get(variableName);
			if(domain instanceof ArrayDomain) {
				Domain baseDomain = ((ArrayDomain) domain).getBaseDomain();
				return (baseDomain instanceof BoolDomain);
			}
			else if(domain instanceof ConstantArrayDomain) {
				Domain baseDomain = ((ConstantArrayDomain) domain).getBaseDomain();
				return (baseDomain instanceof BoolDomain);
			}
			return (domain instanceof BoolDomain);
		}
		else {
			//for(int i=0; i<this.auxVariables.size(); i++) {
				if(this.auxVarDomains.containsKey(variableName)) {
					Domain domain = auxVarDomains.get(variableName);
					return (domain instanceof BoolDomain);	
				}
			//}
				
			
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
		for(int i=0; i<this.allOriginalDecisionVariables.size(); i++) {
			String variableName = this.allOriginalDecisionVariables.get(i);
			ConstantDomain domain = this.allDecisionVariablesDomains.get(variableName);
			
			//System.out.println("Decision variable: "+variableName);
			
			// single element
			if(domain.getType() != Domain.CONSTANT_ARRAY) {
				int endlinePosition = 6;
				while(solverOutputString.charAt(endlinePosition) != '\n' &&
						solverOutputString.charAt(endlinePosition) != '\r' ) {
					endlinePosition++;
				}
				String solution = solverOutputString.substring(5, endlinePosition-1);
				solutionString = solutionString.concat("variable "+variableName+" is "+solution+", \n\n");
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
					
					String intList = "";
					//String intList = solverOutputString.substring(5, 7);
					//System.out.println("First part of inList is:"+intList+" (solverOutput.subString(5,7)");
					for(int j=6; j<endlinePosition; j++) {
						String number = "";
						while(solverOutputString.charAt(j) != ' ' &&
								solverOutputString.charAt(j) != '\n' &&
								   solverOutputString.charAt(j) != '\r') {
							number = number+solverOutputString.charAt(j);
							j++;
						}
						//System.out.println("Number :"+number+" and intList:"+intList);
						intList = intList.concat(number+", ");
					}
					intList = intList.substring(0, intList.length()-2);
					solutionString = solutionString.concat("variable "+variableName+" is ["+intList+"],\n\n");
					solverOutputString = solverOutputString.delete(0, endlinePosition);
				}
				
				else if(indexDomains.length == 2) {
					/*int endlinePosition = 6;
					while(solverOutputString.charAt(endlinePosition) != '\n' &&
							solverOutputString.charAt(endlinePosition) != '\r' ) {
						endlinePosition++;
					}*/
					int amountOfRows = indexDomains[0].getRange()[1]-indexDomains[0].getRange()[0] + 1;
					
					String matrixString = "\n\t[ ";
					for(int row=0;row<amountOfRows; row++) {
						boolean first = true;
						String intList = "";
		
						int pos = 6;
						
						while(solverOutputString.charAt(pos) != '\n' &&
								solverOutputString.charAt(pos) != '\r' ) {
							String integer = "";
							//System.out.println("This the current solver output: "+solverOutputString+"\n=======================\n");
							
							
							while( solverOutputString.charAt(pos) != ' ' ) {
								integer = integer+solverOutputString.charAt(pos);
								pos++;
							}
						
							if(first)
								intList = integer;
							else intList = intList+", "+integer;
						
							first = false;
							
							
						
							pos++;
						}
						if(row >= 1)
							matrixString = matrixString.concat(",\n\t  ["+intList+"]");
						else matrixString = matrixString.concat("["+intList+"]");
						solverOutputString = solverOutputString.delete(0, pos);
					}
					matrixString = matrixString.concat(" ],\n\n");
					solutionString = solutionString+"variable "+variableName+" is "+matrixString;
				}
				
				// 3-dimensional variable
				else if(indexDomains.length == 3) {
					int endlinePosition = 6;
					
					int amountOfPlanes = indexDomains[0].getRange()[1]-indexDomains[0].getRange()[0]+ 1;
					int amountOfRows = indexDomains[1].getRange()[1]-indexDomains[1].getRange()[0]+ 1;
					
					StringBuffer cubeString = new StringBuffer("[   ");
					
					for(int plane =0; plane<amountOfPlanes; plane++) {
						
						StringBuffer matrixString = new StringBuffer("[");
						
						for(int row=0;row<amountOfRows; row++) {
							boolean first = true;
							StringBuffer intList = new StringBuffer("");
			
							int pos = 6;
							
							while(solverOutputString.charAt(pos) != '\n' &&
									solverOutputString.charAt(pos) != '\r' ) {
								StringBuffer integer = new StringBuffer("");
								//System.out.println("This the current solver output: "+solverOutputString+"\n=======================\n");
								
								
								while( solverOutputString.charAt(pos) != ' ' ) {
									integer.append(solverOutputString.charAt(pos));
									pos++;
								}
							
								if(first)
									intList = integer;
								else intList.append(", "+integer);
							
								first = false;
								
								
							
								pos++;
							}
							if(row >= 1)
								matrixString.append(",\n\t ["+intList+"]");
							else matrixString.append("["+intList+"]");
							solverOutputString = solverOutputString.delete(0, pos);
						}
					matrixString.append(" ],");
						
					cubeString.append("\n\t"+matrixString+"\n");	
					
					}
					cubeString.append(" ]");
					solutionString = solutionString+"variable "+variableName+" is "+cubeString+"\n\n";
					
					
					
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
				//System.out.println("translating Minion part to Essence:"+solverOutput.substring(0, i+16)+"\n\n\n");
				
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
		
		for(int i=0; i<this.allOriginalDecisionVariables.size(); i++) {
			String variableName = this.allOriginalDecisionVariables.get(i);
			ConstantDomain domain = this.allDecisionVariablesDomains.get(variableName);
			
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
	
	public void printModelStatistics() {
		if(this.settings.giveTranslationInfo()) {
			System.out.println(this.printStatistics());
		}
	}
	
	protected int getAmountOfAuxiliaryVariables() {
		return this.auxVariables.size();
	}
	
	protected int getAmountOfConstraints() {
		return this.constraintList.size();
	}
}
