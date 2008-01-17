package translator.normaliser;

/**
 * The preprocessor collects info about the model, such as the
 * decision variables and their domains.
 * 
 * @author andrea
 * 
 */


import translator.conjureEssenceSpecification.*;
import translator.expression.*;

import java.util.HashMap;
import java.util.ArrayList;

public class Preprocessor {

	HashMap<String, translator.conjureEssenceSpecification.Domain> oldDecisionVariables;
	ArrayList<String> decisionVariablesNames;
	
	// parameter expressions
	HashMap<String, translator.conjureEssenceSpecification.Expression> expressionParameters;
	HashMap<String, translator.conjureEssenceSpecification.Domain> domainParameters;
	ArrayList<translator.conjureEssenceSpecification.ConstantArray> arrayParameters;
	ArrayList<String> parameterNames;
	HashMap<String, translator.conjureEssenceSpecification.Domain> parameters;
	
	
	// constant expressions
	HashMap<String, translator.conjureEssenceSpecification.Expression> constantExpressions;
	HashMap<String, translator.conjureEssenceSpecification.Domain> constantDomains;
	HashMap<String, translator.conjureEssenceSpecification.ConstantArray> constantArrays;
	ArrayList<String> constantNames;
	
	
	ExpressionMapper expressionMapper;
	
	public Preprocessor() {
		
		// decision variables
		this.oldDecisionVariables = new HashMap<String, translator.conjureEssenceSpecification.Domain>();
		this.decisionVariablesNames = new ArrayList<String>();
		
		
		// parameter info
		this.domainParameters = new HashMap<String, translator.conjureEssenceSpecification.Domain>();
		this.expressionParameters = new HashMap<String, translator.conjureEssenceSpecification.Expression>();
		this.arrayParameters = new ArrayList<translator.conjureEssenceSpecification.ConstantArray>();
		this.parameterNames = new ArrayList<String>();
		this.parameters = new HashMap<String, translator.conjureEssenceSpecification.Domain>();
		
		// constant info
		this.constantExpressions = new HashMap<String, translator.conjureEssenceSpecification.Expression>();
		this.constantDomains = new HashMap<String, translator.conjureEssenceSpecification.Domain>();
		this.constantArrays = new HashMap<String,translator.conjureEssenceSpecification.ConstantArray>();
		this.constantNames = new ArrayList<String>();
		
		
	}
	
	
	
	// ============ METHODS ===============================
	
	/**
	 * This method does the general preprocessing EVERY model needs before it can be normalised or
	 * flattened. THe basic things that are done are collecting and checking the decision variables,
	 * the parameters and constants that are specified, and mapping the model representation into
	 * the advanced representation (using the data structure in the translator.expression package).
	 * 
	 * The method returns the model in form of a normalised model. Please note that the model has
	 * still not been normalised in the sense of our defined normalisations (evaluation, ordering,
	 * etc)
	 * 
	 * @param
	 * @param
	 * @return 
	 */
	public NormalisedModel proprocessModel(EssenceSpecification problemSpecification,
			                               EssenceSpecification parameterSpecification) 
		throws NormaliserException {
	       //------- decision variables -------------------------------------
		readDecisionVariables(problemSpecification);
		readConstants(problemSpecification);
		this.expressionMapper = new ExpressionMapper(this.oldDecisionVariables,
				                                      this.constantDomains,
				                                      this.constantArrays);
 		HashMap<String,translator.expression.Domain> decisionVariables = this.expressionMapper.getNewDecisionVariables(decisionVariablesNames);
 		
 		
		//------------------ parameters -------------------------------------
		readParameters(problemSpecification, parameterSpecification);
		mergeConstantAndParameterLists();
		this.expressionMapper.constantArrays = this.constantArrays; // update the constant arrays
		
        //----------------- constant arrays -------------------------------------
		HashMap<String, translator.expression.ConstantArray> mappedConstantArrays = mapConstants(this.constantArrays);
		
		mappedConstantArrays = addParameterArraysToConstantArrays(mappedConstantArrays);
	
		// -------------- map constraints -------------------------------------
		ArrayList<translator.expression.Expression> constraintsList = expressionMapper.mapExpressions(problemSpecification.getExpressions());
	
		
		// ----------- map objective -----------------------------------------
		Objective objective = this.expressionMapper.mapObjective(problemSpecification.getObjective());
		
	
		
		return new NormalisedModel(decisionVariables,
				                   decisionVariablesNames,
				                   constraintsList,
				                   mappedConstantArrays,
				                   objective);
	}
	
	
	
	
	/**
	 * This method inserts parameters in the constraints, decisionVariables domains and 
	 * the objective
	 * 
	 * @param model
	 * @return
	 * @throws NormaliserException
	 */
	public NormalisedModel insertParameters(NormalisedModel model) 
		throws NormaliserException,Exception {
		
		ArrayList<String> constantHeap = new ArrayList<String>();
		HashMap<String,translator.expression.Expression> constantHeapMap = new HashMap<String,translator.expression.Expression>();
		
		ArrayList<String> domainHeap = new ArrayList<String>();
		HashMap<String,translator.expression.Domain> domainHeapMap = new HashMap<String,translator.expression.Domain>();
		
		ArrayList<translator.expression.Expression>  constraintList = model.constraintList;
		HashMap<String, translator.expression.Domain> decisionVariables = model.decisionVariables;

		
		System.out.println("Starting parameter insertion");
		
		//  constants and parameters are merged together!
		
		
		// -------- iterate over all constants we have collected -----------------------------
		for(int i=0; i<this.constantNames.size(); i++) {
			
			String constantName = constantNames.get(i);
			
			// ---------- we have an expression constant ----------------------
			if(this.constantExpressions.containsKey(constantName)) {
			
				translator.expression.Expression constant = this.expressionMapper.mapExpression(this.constantExpressions.get(constantName));
				constant = constant.evaluate();
				
				// if the expression is an integer insert it into constraints, domain, and constant heap
				if(constant.getType() == translator.expression.Expression.INT) {
					int value = ((ArithmeticAtomExpression) constant).getConstant();
					
					for(int j=0; j<constraintList.size(); j++) {
						constraintList.get(j).insertValueForVariable(value, constantName);
					}
					for(int j=0; j<this.decisionVariablesNames.size(); j++) {
						translator.expression.Domain domain = decisionVariables.get(decisionVariablesNames.get(j));
						domain = domain.insertValueForVariable(value, constantName);
						decisionVariables.put(this.decisionVariablesNames.get(j), domain);
					}
					
					for(int j=0; j<constantHeap.size(); j++) {
						translator.expression.Expression constExpr = constantHeapMap.get(constantHeap.get(j)); 
						constExpr = constExpr.insertValueForVariable(value, constantName);
						constantHeapMap.put(constantHeap.get(j),constExpr);
					}
					for(int j=0; j<domainHeap.size(); j++) {
						translator.expression.Domain constDomain = domainHeapMap.get(domainHeap.get(j));
						constDomain = constDomain.insertValueForVariable(value, constantName);
						domainHeapMap.put(domainHeap.get(j), constDomain);
					}
				}
				// otherwise put it on the stack
				else {
					constantHeap.add(constantName);
					constantHeapMap.put(constantName, constant);
				}
			}
			
			
			// ---------- we have a domain constant ----------------------------
			else if(this.constantDomains.containsKey(constantNames.get(i))) {
				
				translator.expression.Domain domain = this.expressionMapper.mapDomain(this.constantDomains.get(constantNames.get(i)));
				domain = domain.evaluate();
			
				
				// if the domain is constant we can insert it...
				if(domain instanceof ConstantDomain) {
					for(int j=0; j<this.decisionVariablesNames.size(); j++) {
						translator.expression.Domain domainJ = decisionVariables.get(decisionVariablesNames.get(j));
						
						// you have a simple identifier domain
						if(domainJ instanceof translator.expression.IdentifierDomain) {
							String domainJName = ((translator.expression.IdentifierDomain) domainJ).getDomainName();
							if(constantName.equals(domainJName))
								decisionVariables.put(this.decisionVariablesNames.get(j), domain);
						}
						// you have an array domain that consists of a basedomain and index domains...
						else if(domainJ instanceof translator.expression.ArrayDomain) {
							
							ArrayDomain arrayDomain = ((ArrayDomain) domainJ);
							translator.expression.Domain baseDomain = arrayDomain.getBaseDomain();
							if(baseDomain instanceof translator.expression.IdentifierDomain) {
								arrayDomain.setBaseDomain(domain);
							}
							
							translator.expression.Domain[] indexDomains = ((ArrayDomain) domainJ).getIndexDomains();
							for(int k=0;k<indexDomains.length; k++) {
								if(indexDomains[k] instanceof translator.expression.IdentifierDomain) {
									arrayDomain.setIndexDomainAt(domain, k);
								}
							}
						}
						
						
					}
					// there might be domains in quantified expressions
					for(int j=0; j<constraintList.size(); j++) { 
						constraintList.get(j).insertDomainForVariable(domain, constantName);					
					}		
					
				}
				else { // ... otherwise there might be a constant in the domain that has not been inserted yet -> put it on the heap
					domainHeap.add(constantName);
					domainHeapMap.put(constantName, domain);
				}
			}
		
			
		}
		
		
		
		
      //------------- then iterate over all the constants that are still left in the heap	
		
		for(int i=0; i<constantHeap.size(); i++) {
			
			String constantName = constantHeap.get(i);
			translator.expression.Expression constant = constantHeapMap.get(constantName);
			constant = constant.evaluate();
			
			if(constant.getType() == translator.expression.Expression.INT) {
				int value = ((ArithmeticAtomExpression) constant).getConstant();
				
				for(int j=0; j<constraintList.size(); j++) {
					constraintList.get(j).insertValueForVariable(value, constantName);
				}
				for(int j=0; j<this.decisionVariablesNames.size(); j++) {
					translator.expression.Domain domain = decisionVariables.get(decisionVariablesNames.get(j));
					domain = domain.insertValueForVariable(value, constantName);
					decisionVariables.put(this.decisionVariablesNames.get(j), domain);
				}
				
				for(int j=0; j<constantHeap.size(); j++) {
					translator.expression.Expression constExpr = constantHeapMap.get(constantHeap.get(j)); 
					constExpr = constExpr.insertValueForVariable(value, constantName);
					constantHeapMap.put(constantHeap.get(j),constExpr);
				}
				for(int j=0; j<domainHeap.size(); j++) {
					translator.expression.Domain constDomain = domainHeapMap.get(domainHeap.get(j));
					constDomain = constDomain.insertValueForVariable(value, constantName);
					domainHeapMap.put(domainHeap.get(j), constDomain);
				}				
			}
			else throw new NormaliserException
			   ("An undeclared constant/parameter value or decision variable is in the definition of constant/parameter '"+constantName+
					   "' which is defined as:"+constant);
		}
		
		
		for(int i=0; i<domainHeap.size(); i++) {
			
			String constantName = domainHeap.get(i);
			translator.expression.Domain domain = domainHeapMap.get(constantName);
			domain = domain.evaluate();
			
			//System.out.println("DOMAIN HEAP: We have a constant domain as parameter:"+constantNames.get(i)+" with domain value: "+domain);
			
			if(domain instanceof ConstantDomain) {
				for(int j=0; j<this.decisionVariablesNames.size(); j++) {
					translator.expression.Domain domainJ = decisionVariables.get(decisionVariablesNames.get(j));
					// you have a simple identifier domain
					if(domainJ instanceof translator.expression.IdentifierDomain) {
						String domainJName = ((translator.expression.IdentifierDomain) domainJ).getDomainName();
						if(constantName.equals(domainJName))
							decisionVariables.put(this.decisionVariablesNames.get(j), domain);
					}
					
					// you have an array domain that consists of a basedomain and index domains...
					else if(domainJ instanceof translator.expression.ArrayDomain) {
						
						ArrayDomain arrayDomain = ((ArrayDomain) domainJ);
						translator.expression.Domain baseDomain = arrayDomain.getBaseDomain();
						if(baseDomain instanceof translator.expression.IdentifierDomain) {
							arrayDomain.setBaseDomain(domain);
						}
						
						translator.expression.Domain[] indexDomains = ((ArrayDomain) domainJ).getIndexDomains();
						for(int k=0;k<indexDomains.length; k++) {
							if(indexDomains[k] instanceof translator.expression.IdentifierDomain) {
								arrayDomain.setIndexDomainAt(domain, k);
							}
						}
					}
					
				}
				

				// there might be domains in quantified expressions
				for(int j=0; j<constraintList.size(); j++) {
					constraintList.get(j).insertDomainForVariable(domain, constantName);
					
				}				
				
			}
			else throw new NormaliserException
			   ("An undeclared constant/parameter value or decision variable is in the definition of constant/parameter domain '"+constantName+
					   "' which is defined as:"+domain);
			
		}
		
		// update the model
		model.constraintList = constraintList;
		model.decisionVariables = decisionVariables;
		
		return model;
	}


	
	
	public void readDecisionVariables(EssenceSpecification problemSpec) 
		throws NormaliserException {
		
		Declaration[] declarations = problemSpec.getDeclarations();
		
		for(int i=0; i<declarations.length; i++) {
			if(declarations[i].getRestrictionMode() == EssenceGlobals.FIND) {
				DomainIdentifiers[] decisionVariables = declarations[i].getVariables();
				for(int j=0; j<decisionVariables.length; j++)
					readDecisionVariable(decisionVariables[j]);
				
 			}
		}
	}
	
	
	public void readConstants(EssenceSpecification problemSpec) 
		throws NormaliserException {
		
		this.constantExpressions.clear();
		
		Declaration[] declarations = problemSpec.getDeclarations();
		
		for(int i=0; i<declarations.length; i++) {
			if(declarations[i].getRestrictionMode() == EssenceGlobals.LETTING) {
				Constant[] constantDef = declarations[i].getConstants();
				for(int j=0; j<constantDef.length; j++)
					readConstant(constantDef[j]);
				
 			}
		}
	
	}
		

	/**
	 * Read the parameters: collect parameter declarations in the problem
	 * specification and then find theit definition in the parameter file.
	 * 
	 * @param problemSpec
	 * @param parameterSpec
	 */
	public void readParameters(EssenceSpecification problemSpec,
			                   EssenceSpecification parameterSpec) 
		throws NormaliserException {
		
		// if there are no parameter definitions, return
		if(parameterSpec == null || parameterSpec.getDeclarations() == null)
			return;
		
		int noDeclaredParameters = 0;
		int noDefinedParameters = 0;
		
		Declaration[] declarations= problemSpec.getDeclarations();
		
		
		// ---- 1.collect parameter declarations ---------------------------- 
		for(int i=0; i<declarations.length; i++) {
			if(declarations[i].getRestrictionMode() == EssenceGlobals.GIVEN) {
				noDeclaredParameters++;
				translator.conjureEssenceSpecification.Parameter[] parameterDeclarations = declarations[i].getParameter();
				for(int j=0; j<parameterDeclarations.length; j++) {
					DomainIdentifiers params = parameterDeclarations[j].getDomainIdentifiers();
					String[] parametersNames = params.getIdentifiers();
					for(int k=0; k<parametersNames.length; k++) {
						this.parameterNames.add(parametersNames[k]);
						this.parameters.put(parametersNames[k], params.getDomain());
					}
				}
			}
		}
		
		
		//-------2. collect parameter definition from the parameter file --------------
		Declaration[] parameterDeclarations = parameterSpec.getDeclarations();
		
		for(int i=0; i<parameterDeclarations.length; i++) {
			if(parameterDeclarations[i].getRestrictionMode() == EssenceGlobals.PARAM) {
				noDefinedParameters++;
				Constant[] paramDef = parameterDeclarations[i].getParameterDefinitions();
				
				for(int j=0; j<paramDef.length; j++){
					
					if(paramDef[j].getRestrictionMode() == EssenceGlobals.CONSTANT_DOMAIN) {
						DomainConstant domainConst = paramDef[j].getDomainConstant();
						String paramName = domainConst.getName();
						if(this.parameters.containsKey(paramName)) {
							translator.conjureEssenceSpecification.Domain domain = this.parameters.get(paramName);
							if(hasSameUnderLyingDomain(domainConst.getDomain(), domain)) {
								this.domainParameters.put(paramName, domainConst.getDomain());
							}
							else throw new NormaliserException("The domain '"+domain+"' declared for parameter '"+paramName+
									"' does not conform to the domain '"+domainConst.getDomain()+"' given in its definition");
						}
						else throw new NormaliserException("The parameter '"+paramName+
								"' has not been declared. Please declare it in the problem model by using the 'given' statement.");
						
					}
					
					else if(paramDef[j].getRestrictionMode() == EssenceGlobals.CONSTANT) {
						ExpressionConstant exprConst = paramDef[j].getExpressionConstant();
						String paramName = exprConst.getName();
						if(this.parameters.containsKey(paramName)) {
							translator.conjureEssenceSpecification.Domain domain = this.parameters.get(paramName);
							if(hasSameUnderLyingDomain(exprConst.getExpression(), domain)) {
								this.expressionParameters.put(paramName, exprConst.getExpression());
							}
							else throw new NormaliserException("The domain '"+domain+"' declared for parameter '"+paramName+
									"' does not conform to the domain '"+exprConst.getDomain()+"' given in its definition");
						}
						else throw new NormaliserException("The parameter '"+paramName+
						"' has not been declared. Please declare it in the problem model by using the 'given' statement.");
					}
					
					
					else if(paramDef[j].getRestrictionMode() == EssenceGlobals.CONSTANT_ARRAY) {
						translator.conjureEssenceSpecification.ConstantArray array = paramDef[j].getConstantArray();
						String arrayName = array.getArrayName();
						if(this.parameters.containsKey(arrayName)) {
							translator.conjureEssenceSpecification.Domain domain = parameters.get(arrayName);
							if(domain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN ||
									domain.getRestrictionMode() == EssenceGlobals.IDENTIFIER_DOMAIN) {
								this.arrayParameters.add(array);
							}
							else throw new NormaliserException("The domain '"+domain+"' declared for parameter '"+arrayName+
									"' does not conform to the type of a constant array.");
						}
						else throw new NormaliserException("The parameter '"+array.getArrayName()+
						"' has not been declared. Please declare it in the problem model by using the 'given' statement.");
					}
				}
			}
		}
		
		if(noDeclaredParameters != noDefinedParameters) {
			throw (noDeclaredParameters < noDefinedParameters) ?
					new NormaliserException("There are more parameters defined than declared.\n"+
							"Please declare every parameter in the problem file using the 'given' statement.") :
						new NormaliserException("There are more parameters declared than defined.\n"+
								"Please specify the value for every parameter you have declared in the problem file\n"+
								"by using the 'param' statement in a parameter file.");
		}
		
	}
	
	
	public HashMap<String, translator.conjureEssenceSpecification.Domain> getDecisionVariableHashMap() {
		return this.oldDecisionVariables;
	}
	

	public HashMap<String, translator.conjureEssenceSpecification.ConstantArray> getConstantArrays() {
		return this.constantArrays;
	}
	
	
	public HashMap<String, translator.conjureEssenceSpecification.Domain> getDomainConstants() {
		return this.constantDomains;
	}
	
	public ArrayList<String> getDecisionVariablesNames() {
		return this.decisionVariablesNames;
	}
	

	
	public HashMap<String,translator.conjureEssenceSpecification.Expression> getExpressionParameters() {
		return this.expressionParameters;
	}
	
	
	
	
    public String prettyPrintDecisionVariables() {
    	
    	String s = "";
    	
    	for(int i=0; i<this.decisionVariablesNames.size(); i++) {
    		String variableName = decisionVariablesNames.get(i);
    		s = s.concat("find\t"+variableName+" : "+this.oldDecisionVariables.get(variableName).toString()+"\n");
    	}
    	
    	return s;
    }
    
    public void clearParameters() {
    	this.parameterNames.clear();
    	this.parameters.clear();
    	this.domainParameters.clear();
    	this.expressionParameters.clear();
    	this.arrayParameters.clear();
    }
    
	
	// ================== HELPER METHODS =======================
	
 
    /**
	 * Before calling this method, the constant map has to be generated!!
	 * index
	 * @param oldConstantArrays
	 * @return
	 * @throws NormaliserException
	 */
	private HashMap<String, translator.expression.ConstantArray> mapConstants
	                                        (HashMap<String, translator.conjureEssenceSpecification.ConstantArray> oldConstantArrays) 
		throws NormaliserException {
		
		HashMap<String, translator.expression.ConstantArray> newConstants = new HashMap<String, translator.expression.ConstantArray>(); 
		
		
		
		for(int i=0; i<this.constantNames.size(); i++) {
			
			if(oldConstantArrays.containsKey(constantNames.get(i))) {
				
				translator.conjureEssenceSpecification.ConstantArray array = oldConstantArrays.get(constantNames.get(i));

				
				if(array instanceof translator.conjureEssenceSpecification.ConstantVector) {
					translator.conjureEssenceSpecification.ConstantVector oldVector = (translator.conjureEssenceSpecification.ConstantVector) array;
					translator.expression.ConstantVector newVector = 
						                             new translator.expression.ConstantVector(array.getArrayName(), oldVector.getElements());
					newConstants.put(newVector.getArrayName(), newVector);
				}
				
				else if(array instanceof translator.conjureEssenceSpecification.ConstantMatrix) {
					translator.conjureEssenceSpecification.ConstantMatrix oldMatrix = (translator.conjureEssenceSpecification.ConstantMatrix) array;
					translator.expression.ConstantMatrix newMatrix = 
						                             new translator.expression.ConstantMatrix(oldMatrix.getArrayName(), oldMatrix.getElements());
					newConstants.put(newMatrix.getArrayName(), newMatrix);
				}
				
				else throw new NormaliserException("Sorry, I do not support constant arrays that are other than 1- or 2-dimensional.");
				
				
			}
			
		}
	
		return newConstants;
	}
	
    
  
    /**
     * This method needs extension, but translation should be fine as it is now. 
     * Just checks for incompabilities between the declared parameter domain 
     * and the defined parameter value.
     * 
     */
    private boolean hasSameUnderLyingDomain(translator.conjureEssenceSpecification.Expression expression, 
    										translator.conjureEssenceSpecification.Domain baseDomain) {
    	
    	// Expressions do not contain constant arrays at the moment
    	if(baseDomain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN)
    		return false;
  	
    	
    	return true;
    }
    
    /**
     * Returns true if subsetDomain is (or might be) a subset of baseDomain. It is not 
     * always possible to determine if one domain is a subset of the other because
     * there still might be some constants or other parameters that have not been inserted 
     * yet.
     * 
     */
    private boolean hasSameUnderLyingDomain(translator.conjureEssenceSpecification.Domain subsetDomain, 
    										translator.conjureEssenceSpecification.Domain baseDomain) {
    	
    	if(subsetDomain.getRestrictionMode() == EssenceGlobals.IDENTIFIER_DOMAIN)
    		return true;
    	
    	else if(baseDomain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN) {
    		if(subsetDomain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN)
    			return true;
    		else return false;
    	}
    	
    	else if(baseDomain.getRestrictionMode() ==EssenceGlobals.BOOLEAN_DOMAIN) {
    		return (subsetDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN);
    	}
    	
    	else if(baseDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
    		return (subsetDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE);
    	}
    	
    	return true;
    }
    
    
	private void  readConstant(Constant constant) 
	 throws NormaliserException {
		
		// constant expression
		if(constant.getRestrictionMode() == EssenceGlobals.CONSTANT) {
			ExpressionConstant c = constant.getExpressionConstant();
			this.constantExpressions.put(c.getName(), c.getExpression());
			this.constantNames.add(c.getName());
			return;
			
		}
		// domain constant
		else if(constant.getRestrictionMode() == EssenceGlobals.CONSTANT_DOMAIN) {
			DomainConstant d = constant.getDomainConstant();
			this.constantDomains.put(d.getName(), d.getDomain());
			this.constantNames.add(d.getName());
		}
		// constant array
		else if(constant.getRestrictionMode() == EssenceGlobals.CONSTANT_ARRAY) {
			translator.conjureEssenceSpecification.ConstantArray a = constant.getConstantArray();
			this.constantArrays.put(a.getArrayName(), a);
			this.constantNames.add(a.getArrayName());
			
		}
		else throw new NormaliserException ("Unknown constant type:"+constant);
	}
	
	
	
	
	private void readDecisionVariable(DomainIdentifiers declaration) 
		throws NormaliserException {
		
		String[] decisionVarNames = declaration.getIdentifiers();
		translator.conjureEssenceSpecification.Domain domain = declaration.getDomain();
		
		for(int i=0; i<decisionVarNames.length; i++) {
			if(this.oldDecisionVariables.containsKey(decisionVarNames[i]))
				throw new NormaliserException("Decision variable '"+decisionVarNames[i]+"' has been declared twice.");
			
			this.oldDecisionVariables.put(decisionVarNames[i], domain);
			this.decisionVariablesNames.add(decisionVarNames[i]);
		}
		
	}
	
	
	
	private HashMap<String, translator.expression.ConstantArray> addParameterArraysToConstantArrays(
			                                HashMap<String, translator.expression.ConstantArray> mappedConstantArrays) 
			                                throws NormaliserException {
	
		for(int i=0; i<this.arrayParameters.size(); i++) {
			translator.conjureEssenceSpecification.ConstantArray array = this.arrayParameters.get(i);
			
			if(array instanceof translator.conjureEssenceSpecification.ConstantVector) {
				translator.conjureEssenceSpecification.ConstantVector oldVector = (translator.conjureEssenceSpecification.ConstantVector) array;
				translator.expression.ConstantVector newVector = new translator.expression.ConstantVector(array.getArrayName(), oldVector.getElements());
				mappedConstantArrays.put(array.getArrayName(), newVector);
			}
			
			else if(array instanceof translator.conjureEssenceSpecification.ConstantMatrix) {
				translator.conjureEssenceSpecification.ConstantMatrix oldMatrix = (translator.conjureEssenceSpecification.ConstantMatrix) array;
				translator.expression.ConstantMatrix newMatrix = new translator.expression.ConstantMatrix(array.getArrayName(), oldMatrix.getElements());
				mappedConstantArrays.put(array.getArrayName(), newMatrix);
			}
			
			else throw new NormaliserException("Sorry, I do not support constant arrays that are other than 1- or 2-dimensional.");
			
		}
		
		return mappedConstantArrays;
	}
	
	
	
	/**
	 * Put all parameters from the parameter lists (hashmap etc) into the constants list.
	 * This is done to enhance parameter insertion
	 * 
	 * @throws NormaliserException
	 */
	private void mergeConstantAndParameterLists() 
		throws NormaliserException {
		
		for(int i=0; i<this.parameterNames.size(); i++) {
			
			String parameterName  = parameterNames.get(i);
			this.constantNames.add(parameterName);
			
			if(this.expressionParameters.containsKey(parameterName)) {
				if(constantExpressions.containsKey(parameterName))
					throw new NormaliserException("Constant and parameter have the same name: "+parameterName);
				
				this.constantExpressions.put(parameterName, this.expressionParameters.remove(parameterName));
				
			}
			else if(this.domainParameters.containsKey(parameterName)) {
				if(constantDomains.containsKey(parameterName))
					throw new NormaliserException("Constant and parameter have the same name: "+parameterName);
				
				this.constantDomains.put(parameterName, this.domainParameters.remove(parameterName));
				
			}
			
		}
		
		for(int i=this.arrayParameters.size()-1; i>=0; i--) {
			translator.conjureEssenceSpecification.ConstantArray array = this.arrayParameters.get(i);
			this.constantArrays.put(array.getArrayName(), array);
		}
		
		
	}
	
}
