package translator.normaliser;

import java.util.ArrayList;
import java.util.HashMap;

import translator.expression.*;



/**
 * This class normalises Essence' problem models and Essence' problem instances
 * 
 * @author andrea
 *
 */

public class Normaliser implements NormaliserSpecification {

	/** decision variables */
	HashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	
	/** parameter stuff  */
	HashMap<String, Domain> parameters;
	ArrayList<String> parameterNames;
	HashMap<String, ConstantArray> constantArrays;
	ArrayList<String> constantArrayNames;
	HashMap<String, int[]> constantArrayOffsets;
	HashMap<String, Domain> parameterDomains;
	
	//=========================== CONSTRUCTOR =====================================================================
	
	public Normaliser() {
		this.decisionVariables = new HashMap<String, Domain> ();
		this.decisionVariablesNames = new ArrayList<String>();
		this.parameters = new HashMap<String, Domain> ();
		this.parameterNames = new ArrayList<String>();
		this.constantArrays = new HashMap<String, ConstantArray> ();
		this.constantArrayNames = new ArrayList<String>();
		this.constantArrayOffsets = new HashMap<String, int[]>();
		this.parameterDomains = new HashMap<String, Domain> ();
	}
	  
	
	///=========================== INTERFACED METHODS ==insertParametersAndMapExpression();=============================================================
	
	
	/**
	 * General method to normalise the given problem- and parameter specification. Returns a 
	 * normalised model containing normalised constraints, a list of parameters that have not
	 * yet been inserted (because they appear in unenrolled quantifications) and a list of
	 * decision variables and their corresponding domain.
	 *  
	 * @return a normalised model
	 */
	public NormalisedModel normalise(EssencePrimeModel problemClass, 
									 EssencePrimeModel parameterFile) 
		throws NormaliserException, Exception {
		
		// get all the decision variable and parameter declarations => written into 
		// member hashmaps and arraylists 
		ArrayList<GeneralDeclaration> remainingDeclarations = readDeclarations(problemClass.getDeclarations());
		//System.out.println("Decision variable declarations:"+this.decisionVariablesNames);
		
		// merge parameter definitions from parameter file to problem file (putting parameter definitions first)
		ArrayList<GeneralDeclaration> parameterDefinitions = parameterFile.getDeclarations();
		for(int i= parameterDefinitions.size()-1; i>=0; i--) {
			remainingDeclarations.add(0, parameterDefinitions.remove(i));
		}
		
		//System.out.println("DEFINITIONS are:"+remainingDeclarations);
		
		// insert expression parameters
		// insert domain parameters in constraints list and the objective
		ArrayList<Expression> constraints = problemClass.getConstraints();
		//System.out.println("Getting parsed constraints:"+constraints);
		
		constraints.add(0,problemClass.getObjective());
		constraints = insertParameters(remainingDeclarations,
									   constraints);
		
		// compute the offsets of all constant arrays
		this.computeConstantArrayOffsets();
		
		//System.out.println("Constraints after inserting parameters:"+constraints);
		
		
		// update the types of variables since we now what domain they have now...
		constraints = updateExpressionTypes(constraints);
		//	System.out.println("Constraints after updating expressions:"+constraints);
		
		// orderExpressions
		constraints = orderConstraints(constraints);
		
		//System.out.println("Constraints BEFORE evaluation:"+constraints);
		// evaluate them
		constraints = evaluateConstraints(constraints);
		//System.out.println("Constraints AFTER evaluation:"+constraints);
		
		// reduce Expressions
		constraints = reduceExpressions(constraints);
		
		// restructure Expressions
		constraints = restructureExpressions(constraints);
		
		// order again
		constraints = orderConstraints(constraints);
		
		//System.out.println("Constraints after normalisation:"+constraints);
		
		Objective objective = (Objective) constraints.remove(0);
		
		if(this.constantArrayNames.size() ==0)
			return new NormalisedModel(this.decisionVariables,
									   this.decisionVariablesNames,
									   constraints,
									   objective);
		
		else 
			return new NormalisedModel(this.decisionVariables,
										this.decisionVariablesNames,
										constraints,
										this.constantArrays,
										this.constantArrayOffsets,
										objective);
	}
	

	
	
	/**
	 * Normalise the expressions only to a certain extent: either full, evaluate only,
	 * order only etc...
	 * 
	 * @return a normalised model
	 * 
	 */
	public NormalisedModel normalise(EssencePrimeModel problemClass, 
			                         EssencePrimeModel parameter,
			                         char normalisationType) 
	throws NormaliserException,Exception {
		
		return normalise(problemClass, parameter);
	
	}

	
	/** 
	 * This method normalises a problem class only.
	 * 
	 * @param problemClass
	 * @return
	 * @throws NormaliserExceptionHashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	 */
	public NormalisedModel normaliseProblemClass(EssencePrimeModel problemClass)
		throws NormaliserException {
		
		
		return null;
	}
	

	/**
	 * 
	 * @param constraints
	 * @return
	 */
	public String printModel(EssencePrimeModel model) {
		return model.toString();		
	}
	

	
	                                                             
	
	// ===================== OTHER METHODS ===========================================
	
	/**
	 * Reads all the decision variable declarations (FIND) and parameter declarations (GIVEN)
	 * and stores the variables in the corresponding member HashMap or ArrayList. 
	 * The remaining definitions are returned.
	 * @return remaining declarations/definitions
	 */
	private ArrayList<GeneralDeclaration> readDeclarations(ArrayList<GeneralDeclaration> declarations) 
		throws NormaliserException {
		
		// iterate over all declarations/definitions
		for(int i=declarations.size()-1; i>=0; i--) {
			
			// decision variable declaration
			if(declarations.get(i) instanceof VariableDeclaration) {
				
				VariableDeclaration declaration = (VariableDeclaration) declarations.remove(i);
				ArrayList<String> variableNames = declaration.getNames();
				for(int j=variableNames.size()-1; j>= 0; j--) {
					this.decisionVariables.put(variableNames.get(j), declaration.getDomain());
					this.decisionVariablesNames.add(0,variableNames.remove(j));
				}
			}
			
			// parameter declaration
			else if(declarations.get(i) instanceof ParameterDeclaration) {
				
				ParameterDeclaration parameterDecl = (ParameterDeclaration) declarations.remove(i);
				ArrayList<String> parameterNames = parameterDecl.getNames();
				for(int j=parameterNames.size()-1; j>= 0; j--) {
					this.parameters.put(parameterNames.get(j), parameterDecl.getDomain());
					this.parameterNames.add(0,parameterNames.get(j));	
				}
				/* check if there are any domain declarations  */ 
				if(parameterDecl.getDomain() != null) {
					Domain domain = parameterDecl.getDomain();
					
					//System.out.println("Parameterdecl "+parameterDecl+" has a domain defined :"+domain);	
					for(int j=parameterNames.size()-1; j >=0; j--) {
						//System.out.println("Inserting domain for "+parameterNames.get(j)+" that is :"+domain);	
						this.parameterDomains.put(parameterNames.remove(j), domain);
					}
					
				}
			}
			
			// else: it is a definition, so do nothing
		}
		
		
		return declarations;
	}
	
	
	/**
	 * This method deals with the insertion of defined parameter values. All defined parameters 
	 * are inserted according to their order of definition.
	 * 
	 * @param definitions
	 * @param constraints
	 * @return
	 * @throws NormaliserException
	 */
	private ArrayList<Expression> insertParameters(ArrayList<GeneralDeclaration> definitions,
														   ArrayList<Expression> constraints) 
		throws NormaliserException {
		
		// buffers for insertion
		HashMap<String, Expression> expressionParameters = new HashMap<String, Expression>();
		ArrayList<String> remainingExpressionParameters = new ArrayList<String>();
		HashMap<String, Domain> domainParameters = new HashMap<String, Domain>();
		ArrayList<String> remainingDomainParameters = new ArrayList<String>();
		ArrayList<Expression> constantRestrictions = new ArrayList<Expression>();
		
		
		//System.out.println("About to insert parameters. Got definitions:");
		//for(int i=0; i<definitions.size(); i++) {
		//	System.out.println((i+1)+". "+definitions.get(i));
		//}
		
		
		
		// first collect all the stuff f
		for(int i=definitions.size()-1; i>=0; i--) {
			
			/* LETTING expression parameter/constant */
			if(definitions.get(i) instanceof ExpressionDefinition) {
				ExpressionDefinition exprDefinition = (ExpressionDefinition) definitions.get(i);
				expressionParameters.put(exprDefinition.getName(), exprDefinition.getExpression());
				remainingExpressionParameters.add(exprDefinition.getName());
			}
			
			else if(definitions.get(i) instanceof DomainDefinition) {
				DomainDefinition domainDefinition = (DomainDefinition) definitions.get(i);
				domainParameters.put(domainDefinition.getName(), domainDefinition.getDomain());
				remainingDomainParameters.add(domainDefinition.getName());
			}
			
			/* WHERE restrictions on constants/parameters */
			else if(definitions.get(i) instanceof ConstantRestriction) {
				
				ArrayList<Expression> restrictions = ( (ConstantRestriction) definitions.remove(i)).getExpression();
				if(constantRestrictions.size() == 0)
					constantRestrictions = restrictions;
				else if(restrictions.size() > constantRestrictions.size()) {
					for(int j=constantRestrictions.size()-1; j>=0; j--)
						restrictions.add(constantRestrictions.remove(j));
					constantRestrictions = restrictions;
				}
				else {
					for(int j=restrictions.size()-1; j>=0; j--)
						constantRestrictions.add(restrictions.remove(j));
				}
			}
			else throw new NormaliserException("Internal error. Unknow definition/declaration type: "+definitions.get(i));
		}
		
		
		
		
		
		// iterate over all definitions and collect parameter values
		// we need to do this in the right order 
		int amountOfDefinitions = definitions.size();
		int i = 0;
		int iterator = 0;
		
		
		while(iterator < amountOfDefinitions && i<definitions.size() && i >=0 ) {
			
			//System.out.println("Definitions:"+definitions+" and iterator: "+iterator+" and i:"+i);
			iterator++;
			
			/*  --------EXPRESSION PARAMETER---------- */
			if(definitions.get(i) instanceof ExpressionDefinition) {
				
				ExpressionDefinition definition = (ExpressionDefinition) definitions.remove(i);
				String parameterName = definition.getName();				
				Expression expression = expressionParameters.get(parameterName);
				expression = expression.evaluate();
				
				/*  integer parameter/constant  */
				if(expression instanceof ArithmeticAtomExpression) {
					
					int value = ((ArithmeticAtomExpression) expression).getConstant();
					// insert value in constraints
					constraints = insertValueForVariable( value,
													      parameterName,
													      constraints);
					// insert values in decision variables/parameters  domains
					insertValueForVariableInDomains(value, parameterName);
					
					// insert value in other parameter expressions
					expressionParameters = insertValueForVariableInParameterExpression(value, 
																					   parameterName,
																					   expressionParameters,
																					   remainingExpressionParameters);
					// insert value in other parameter domains
					domainParameters = insertValueForVariableInParameterDomain(value, 
																				   parameterName,
																				   domainParameters,
																				   remainingDomainParameters);
					
					// insert value in the domains over which the expression parameters range 	
					parameterDomains = insertValueForVariableInParameterDomain(value,
																			   parameterName,
																			   this.parameterDomains,
																			   this.parameterNames);
					
					// insert parameter in constant restrictions (WHERE)
					constantRestrictions = insertValueForVariable( value,
																   parameterName,
																   constantRestrictions);
					
					//System.out.println("After inserting parameter "+parameterName+" in stuff.Here are constraints:"+constraints);
					// TODO: do something about the domain
				} // end if: expression is an arithmetic atom
				
				/* boolean parameter/constant */
				else if(expression instanceof RelationalAtomExpression) {
					
					boolean value = ((RelationalAtomExpression) expression).getBool();
					
					// insert value in constraints
					constraints = insertValueForVariable( value,
														  parameterName,
														  constraints);
					// insert values in decision variables/parameters  domains
					insertValueForVariableInDomains(value, parameterName);
					
					// insert value in other parameters expressions
					expressionParameters = insertValueForVariableInParameterExpression(value, 
																					   parameterName,
																					   expressionParameters,
																					   remainingExpressionParameters);
					// insert value in other parameter domains
					domainParameters = insertValueForVariableInParameterDomain(value, 
																				   parameterName,
																				   domainParameters,
																				   remainingDomainParameters);
					
					// insert value in the domains over which the expression parameters range 	
					parameterDomains = insertValueForVariableInParameterDomain(value,
																			   parameterName,
																			   this.parameterDomains,
																			   this.parameterNames);
					// insert parameter in constant restrictions (WHERE)
					constantRestrictions = insertValueForVariable( value,
																   parameterName,
																   constantRestrictions);
				} // end else if: expression is a relational atom
				
				else { // the expression still contains a parameter or similar
					   // put it in the buffer and evaluate it later
					if(expression instanceof ConstantArray) {
						// insert parameters in constraints
						ConstantArray constantArray = (ConstantArray) expression;
					    //System.out.println("Before inserting constant array "+constantArray+" for parameter "+parameterName+" into constraints:"+constraints);
						constraints = insertExpressionForVariable(constantArray,
															      parameterName,
																  constraints);
						//System.out.println("After inserting constant array "+constantArray+" for parameter "+parameterName+". Constraints:"+constraints);
						this.constantArrays.put(parameterName, constantArray);
						this.constantArrayNames.add(parameterName);
					}
					
					else 
					throw new NormaliserException("Cannot evaluate parameter '"+parameterName
							+"' to a constant value, but to '"+expression+
							"'. Please make sure that all constants/parameters have been declared and defined in the right order.");
				}
				
				//System.out.println("END of WHILE: Definitions:"+definitions+" and iterator: "+iterator+" and i:"+i);
				
			} // end if: definition is an expression definition
			
			
			
			
			/*  --------DOMAIN PARAMETER---------- */
			else if(definitions.get(i) instanceof DomainDefinition) {
				
				DomainDefinition domainDefinition = (DomainDefinition) definitions.remove(i);
				String parameterName = domainDefinition.getName();
				Domain domain = domainParameters.get(parameterName);
				domain = domain.evaluate();
				
				if(domain instanceof ConstantDomain) {
					ConstantDomain constDomain = (ConstantDomain) domain;
					
					// insert in variable's domains
					constraints = insertDomainForVariable(constDomain, parameterName, constraints);
					
					// insert domain constant in decision variable's domains
					insertValueForVariableInDomains(constDomain, parameterName); 
					
					// insert parameter in constant restrictions (WHERE)
					constantRestrictions = insertDomainForVariable( domain,
																   parameterName,
																   constantRestrictions);
					// insert domain in other parameter domains
					domainParameters = insertValueForVariableInDomains(domain,
																	   parameterName,
							 										   domainParameters,
							 										   remainingDomainParameters); 
					
					// insert domain in other parameter domains
					this.parameterDomains = insertValueForVariableInDomains(domain,
																	   parameterName,
							 										   this.parameterDomains,
							 										   this.parameterNames); 
					// TODO: insert in parameter's domains (but we do not use them anyway)
					
				} // end if: domain is constant
				
				else { // domain is not constant -> there is still a variable/constant/parameter that has not been inserted 
					throw new NormaliserException("Cannot evaluate parameter '"+parameterName
							+"' to a constant domain, but to '"+domain+
							"'. Please make sure that all constants/parameters have been declared and defined in the right order.");
				}
				
			} // end if: definition is a Domain-definition	
			
		} // end: while (over definitions over a certain amount of iterations)
		
		
		
		/* CONSTANT  RESTRICTIONS (WHERE) */
		for(int j=0; j<constantRestrictions.size(); j++) {
			
			Expression restriction = constantRestrictions.get(j);
			Expression r = restriction.copy().evaluate();
			
			if(r.getType() == Expression.BOOL) {
				boolean statement = ((RelationalAtomExpression) r).getBool();
				if(!statement) {
					throw new NormaliserException("Your problem model/problem instance does not hold the constant restriction:"+restriction);
				}
			}
			else throw new NormaliserException("Could not evaluate the constant restriction '"+restriction+"' to true or false."+
					"Please make sure that all constants/parameters have been declared and defined in the right order.");
		}
		
		
		
		return constraints;
	}
	
	/**
	 * Computes the offsets of the indices of every declared constant array. Method can only
	 * be evoiked after ALL parameters have been inserted. Otherwise we might not have the 
	 * full information about the constant arrays offset. 
	 * 
	 * @throws NormaliserException
	 */
	private void computeConstantArrayOffsets() 
		throws NormaliserException {
		
		
		for(int i=0; i<this.parameterNames.size(); i++) {
		
			String parameterName = parameterNames.get(i);
			
			if(this.constantArrays.containsKey(parameterName)) {
			
				ConstantArray constantArray = this.constantArrays.get(parameterName);
			
				/* if there is a specification of the constant array's domain */
				if(this.parameterDomains.containsKey(parameterName)) {
					Domain domain = parameterDomains.get(parameterName).evaluate();
					if(!(domain instanceof ConstantArrayDomain)) 
						throw new NormaliserException("Cannot compute offsets of indices of constant array \n"+parameterName+
								" when its domain '"+domain+"' is not constant.\nPlease declare all parameters and constants before using them.");
			
					ConstantArrayDomain arrayDomain = (ConstantArrayDomain) domain;
			
					int[] offsets = new int[arrayDomain.getIndexDomains().length];
					for(int j=0; j<offsets.length; j++) {
						ConstantDomain indexDomain = arrayDomain.getIndexDomains()[j];
						offsets[j] = indexDomain.getRange()[0];
					}
			
					this.constantArrayOffsets.put(parameterName, offsets);
			
				} /* no specification of the domain, so take default values */
				else { 			
					int[] offsets = (constantArray.getDimension() == 1) ? 
							new int[] {NormaliserSpecification.CONSTANT_ARRAY_OFFSET_FROM_ZERO} : 
								new int[] {NormaliserSpecification.CONSTANT_ARRAY_OFFSET_FROM_ZERO,
							        	NormaliserSpecification.CONSTANT_ARRAY_OFFSET_FROM_ZERO};
							this.constantArrayOffsets.put(parameterName, offsets);
				}
			} // end: if this is a constant array
		
		} // end: for-loop
	}
	
	
	/**
	 * Replace every occurrence of variableName with the expression 'expression' in the 
	 * expression list. then return the list.
	 * 
	 * @param expression
	 * @param variableName
	 * @param constraints
	 * @return
	 */
	private ArrayList<Expression> insertExpressionForVariable(Expression expression,
															  String variableName,
															  ArrayList<Expression> constraints) {
		
		for(int i=0; i<constraints.size(); i++) {
			Expression constraint = constraints.remove(i);
			constraint = constraint.replaceVariableWithExpression(variableName, expression);
			constraints.add(i, constraint);
		}
		
		return constraints;
	}
	
	/**
	 * Replace every occurrence of 'variableName' with domain 'domain' in the given 
	 * expression list. Return the list after replacement.
	 * 
	 * @param domain
	 * @param variableName
	 * @param constraints
	 * @return
	 * @throws NormaliserException
	 */
	private ArrayList<Expression> insertDomainForVariable(Domain domain,
			                                              String variableName,
			                                              ArrayList<Expression> constraints)
			                                      	throws NormaliserException {
		
		for(int i=0; i<constraints.size(); i++) {
			Expression constraint = constraints.remove(i);
			constraint = constraint.insertDomainForVariable(domain, variableName);
			constraint = constraint.evaluate();
			constraints.add(i, constraint);
		}
		
		return constraints;
	}
			                                             
	
	
	/**
	 * Replace every occurrence of 'variableName' with integer 'value' in the expressions 
	 * that are mapped to other parameter/constant values.
	 * 
	 * @param value
	 * @param variableName
	 * @param parameterExpressions
	 * @param parameterNames
	 * @return
	 */
	private HashMap<String, Expression> insertValueForVariableInParameterExpression(int value,
																					String variableName,
																					HashMap<String, Expression> parameterExpressions,
																					ArrayList<String> parameterNames) {
		for(int i=0; i<parameterNames.size(); i++) {
			
			String parameter = parameterNames.get(i);
			Expression expression = parameterExpressions.get(parameter);
			expression = expression.insertValueForVariable(value, variableName);
			parameterExpressions.put(parameter, expression);
		}
		
		return parameterExpressions;
	}
	
	/**
	 * Replace every occurrence of 'variableName' with integer 'value' in the expressions 
	 * that are mapped to other parameter/constant values.
	 * 
	 * @param value
	 * @param variableName
	 * @param parameterDomains
	 * @param parameterNames
	 * @return
	 */
	private HashMap<String, Domain> insertValueForVariableInParameterDomain(int value,
																					String variableName,
																					HashMap<String, Domain> parameterDomains,
																					ArrayList<String> parameterNames) {
		//System.out.println("Inserting "+value+" for "+variableName+" in domains "+parameterDomains);
		for(int i=0; i<parameterNames.size(); i++) {
			//System.out.println("Inserting "+value+" for "+variableName+" in domains "+parameterDomains);
			String parameter = parameterNames.get(i);
			Domain domain = parameterDomains.get(parameter);
			if(domain != null) {
				//System.out.println("Inserting "+value+" for "+variableName+" in domain:"+domain);
				domain = domain.insertValueForVariable(value, variableName);
				//System.out.println("Inserted "+value+" for "+variableName+" in domain:"+domain);
				parameterDomains.put(parameter, domain);
			}
		}
		
		return parameterDomains;
	}
	
	
	/**
	 * Replace every occurrence of 'variableName' with integer 'value' in the expressions 
	 * that are mapped to other parameter/constant values.
	 * 
	 * @param value
	 * @param variableName
	 * @param parameterDomains
	 * @param parameterNames
	 * @return
	 */
	private HashMap<String, Domain> insertValueForVariableInParameterDomain(boolean value,
																					String variableName,
																					HashMap<String, Domain> parameterDomains,
																					ArrayList<String> parameterNames) {
		for(int i=0; i<parameterNames.size(); i++) {
			
			String parameter = parameterNames.get(i);
			Domain domain = parameterDomains.get(parameter);
			domain = domain.insertValueForVariable(value, variableName);
			parameterDomains.put(parameter, domain);
		}
		
		return parameterDomains;
	}
	
	/**
	 * Replace every occurrence of 'variableName' with boolean 'value' in the expressions 
	 * that are mapped to other parameter/constant values.
	 * 
	 * @param value
	 * @param variableName
	 * @param parameterExpressions
	 * @param parameterNames
	 * @return
	 */
	private HashMap<String, Expression> insertValueForVariableInParameterExpression(boolean value,
																					String variableName,
																					HashMap<String, Expression> parameterExpressions,
																					ArrayList<String> parameterNames) {
		for(int i=0; i<parameterNames.size(); i++) {
			
			String parameter = parameterNames.get(i);
			Expression expression = parameterExpressions.get(parameter);
			expression = expression.insertValueForVariable(value, variableName);
			parameterExpressions.put(parameter, expression);
		}
		
		return parameterExpressions;
	}
	
	
	/**
	 * Replace every occurrence of 'name' with the integer 'value' in the 
	 * decision variable's domains  
	 * 
	 * @param value
	 * @param name
	 * @throws NormaliserException
	 */
	private void insertValueForVariableInDomains(int value, String name) 
		throws NormaliserException {
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			
			String variableName = this.decisionVariablesNames.get(i);
			Domain domain = this.decisionVariables.get(variableName);
			if(domain == null)
				throw new NormaliserException("Internal error. Cannot find decision variable '"+variableName+
						" in Domain-HashMap.");
			
			domain = domain.insertValueForVariable(value, name);
			this.decisionVariables.put(variableName, domain);
		}
		
	}
	
	/**
	 * Replace every occurrence of 'name' with the Domain 'newDomain' in the 
	 * decision variable's domains  
	 * 
	 * @param newDomain
	 * @param name
	 * @throws NormaliserException
	 */
	private void insertValueForVariableInDomains(Domain newDomain, String name) 
		throws NormaliserException {
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			
			String variableName = this.decisionVariablesNames.get(i);
			Domain domain = this.decisionVariables.get(variableName);
			if(domain == null)
				throw new NormaliserException("Internal error. Cannot find decision variable '"+variableName+
						" in Domain-HashMap.");
			
			domain = domain.replaceVariableWithDomain(name, newDomain);
			this.decisionVariables.put(variableName, domain);
		}
		
	}
	
	
	/**
	 * Replace every occurrence of 'name' with the Domain 'newDomain' in the 
	 * decision variable's domains  
	 * 
	 * @param newDomain
	 * @param name
	 * @throws NormaliserException
	 */
	private HashMap<String, Domain> insertValueForVariableInDomains(Domain newDomain,
												 String name,
												 HashMap<String, Domain> domains,
												 ArrayList<String> domainNames) 
		throws NormaliserException {
		
		for(int i=0; i<domainNames.size(); i++) {
			
			String variableName = domainNames.get(i);
			Domain domain = domains.get(variableName);
			/*if(domain == null)
				throw new NormaliserException("Internal error. Cannot find decision variable '"+variableName+
						" in Domain-HashMap."); */
			//System.out.println("Inserting domain "+newDomain+" for "+name+" in "+domains.get(variableName));
			
			if(domain != null) {
				domain = domain.replaceVariableWithDomain(name, newDomain);
				domains.put(variableName, domain);
			}
			//System.out.println("Inserted domain "+newDomain+" for "+name+" in "+domains.get(variableName));
		}
		
		return domains;
	}
	
	/**
	 * Replace every occurrence of 'name' with the integer 'value' in the 
	 * decision variable's domains  
	 * 
	 * @param value
	 * @param name
	 * @throws NormaliserException
	 */
	private void insertValueForVariableInDomains(boolean value, String name) 
		throws NormaliserException {
		
		for(int i=0; i<this.decisionVariablesNames.size(); i++) {
			
			String variableName = this.decisionVariablesNames.get(i);
			Domain domain = this.decisionVariables.get(variableName);
			if(domain == null)
				throw new NormaliserException("Internal error. Cannot find decision variable '"+variableName+
						" in Domain-HashMap.");
			
			domain = domain.insertValueForVariable(value, name);
			this.decisionVariables.put(variableName, domain);
		}
		
	}
	
	
	
	/**
	 * Insert a value for a variable/parameter with name 'name' in a list
	 * of expressions. Returns the list of expressions with all occurrences 
	 * of 'name' replaced by 'value'.
	 * 
	 * @param value
	 * @param name
	 * @param constraints
	 * @return
	 */
	private ArrayList<Expression> insertValueForVariable(int value,
														String name,
														ArrayList<Expression> constraints) {
	
		for(int i=constraints.size()-1; i>=0; i--) {
			Expression constraint = constraints.remove(i);
			constraint = constraint.insertValueForVariable(value, name);
			constraint = constraint.evaluate();
			constraints.add(i, constraint);
		}
		
		return constraints;
	}
	
	
	/**
	 * Insert a value for a variable/parameter with name 'name' in a list
	 * of expressions. Returns the list of expressions with all occurrences 
	 * of 'name' replaced by boolean 'value'.
	 * 
	 * @param value
	 * @param name
	 * @param constraints
	 * @return
	 */
	private ArrayList<Expression> insertValueForVariable(boolean value,
														String name,
														ArrayList<Expression> constraints) {
	
		for(int i=constraints.size()-1; i>=0; i++) {
			Expression constraint = constraints.remove(i);
			constraint = constraint.insertValueForVariable(value, name);
			constraints.add(i, constraint);
		}
		
		return constraints;
	}
	
	
	
	private ArrayList<Expression> updateExpressionTypes(ArrayList<Expression> constraints)
		throws NormaliserException {
		
		// iterate over all decision variables
		for(int j=0; j<this.decisionVariablesNames.size(); j++) {
		
			String variableName = this.decisionVariablesNames.get(j);
			Domain domain = this.decisionVariables.get(variableName);
			
			// iterate over all constraints
			for(int i=0; i<constraints.size(); i++) {
				//System.out.println("Inserting domain '"+domain+"' for variable:"+variableName);
				Expression constraint = constraints.remove(i);
				constraint = constraint.insertDomainForVariable(domain, variableName);
				constraints.add(i,constraint);
			}
		}
		
		return constraints;
	}
	
	private ArrayList<Expression> orderConstraints(ArrayList<Expression> constraints) throws NormaliserException {
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

	
	private ArrayList<Expression> restructureExpressions(ArrayList<Expression> constraints) 
		throws NormaliserException {
		
		for(int i=0; i<constraints.size(); i++) {
		//	if(!(constraints.get(i) instanceof translator.expression.Sum))
				constraints.add(i, constraints.remove(i).restructure());
		}
		
		return constraints;
	}
	

	
	private ArrayList<Expression> evaluateConstraints(ArrayList<Expression> constraints) throws NormaliserException {
		
		reduceExpressions(constraints);
		
		for(int i=0; i<constraints.size(); i++) {
			constraints.add(i, constraints.remove(i).evaluate());
		}
		
		return reduceExpressions(constraints);	
	}
}
