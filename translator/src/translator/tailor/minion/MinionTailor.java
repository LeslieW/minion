package translator.tailor.minion;

import translator.expression.*;
import java.util.ArrayList;
import java.util.HashMap;
import translator.normaliser.NormalisedModel;
import translator.solver.Minion;
//import translator.tailor.TailorSpecification;
import translator.TranslationSettings;
import translator.solver.TargetSolver;

public class MinionTailor {

	public final String MINION_AUXVAR_NAME = "aux";
	public final String FLATTENED_TO_VECTOR_SUFFIX = "_flattened2Vector";
	
	int noMinionAuxVars;
	int usedCommonSubExpressions;
	boolean useCommonSubexpressions;
	
	MinionModel minionModel;
	HashMap<String, int[]> offsetsFromZero;
	NormalisedModel normalisedModel;
	Minion solverSettings;
	HashMap<String, MinionAtom> minionSubExpressions;
	HashMap<String, ArithmeticAtomExpression> essenceSubExpressions;
	TranslationSettings settings;
	
	// ======== CONSTRUCTOR ==================================
	
	public MinionTailor(NormalisedModel normalisedModel,
						TranslationSettings settings) {
			
		this.settings = settings;
		this.offsetsFromZero = new HashMap<String,int[]>();
		this.normalisedModel = normalisedModel;
		TargetSolver solver = settings.getTargetSolver();
		if(solver instanceof Minion)
			this.solverSettings = (Minion) solver;
		else this.solverSettings = new Minion();
		this.noMinionAuxVars = this.normalisedModel.getAuxVariables().size();
		this.usedCommonSubExpressions = this.normalisedModel.getAmountOfCommonSubExpressionsUsed();
		this.minionSubExpressions = new HashMap<String,MinionAtom>();
		this.useCommonSubexpressions = this.settings.useCommonSubExpressions();
		this.essenceSubExpressions = this.normalisedModel.getSubExpressions();
		
	}
	
	// ====== TRANSLATION TO MINION REPRESENTATION ===========
	
	
	public MinionModel tailorToMinion() 
		throws MinionException {
		
		// 1. tailor the variables and create a new empty model
		
		long startTime = System.currentTimeMillis();
		
		this.minionModel = new MinionModel(new ArrayList<MinionConstraint>(),
				                           mapDecisionVariables(),
				                           this.normalisedModel.getDecisionVariablesNames(),
				                           this.normalisedModel.getAuxVariables(),
				                           this.solverSettings, settings
				                           );
		
		tailorObjective();
		if(this.settings.giveTranslationTimeInfo()) {
			long stopTime = System.currentTimeMillis();
			System.out.println("Variable Tailoring Time: "+(stopTime - startTime)/1000.0+"sec");
		}
		startTime = System.currentTimeMillis();
		
		
		
		// 2. tailor the constraints
		for(int i=this.normalisedModel.getConstraints().size()-1; i>=0; i--) {
			//System.out.println("Tailoring constraint:"+normalisedModel.getConstraints().get(i));
			minionModel.addConstraint(toMinion(this.normalisedModel.getConstraints().remove(i)));
		}
		
		this.minionModel.setAmountOfUsedCommonSubExpressions(this.usedCommonSubExpressions);
		this.minionModel.setAmountOfUsedEqualSubExpressions(this.normalisedModel.getAmountOfEqualSubExpressionsUsed());
		
		if(this.settings.giveTranslationTimeInfo()) {
			long stopTime = System.currentTimeMillis();
			System.out.println("Constraint Tailoring Time: "+(stopTime - startTime)/1000.0+"sec");
		}
		
		if(this.settings.applyDirectVariableReusage()) {
			this.minionModel.setEqualAtoms(tailorEqualAtoms());
			this.minionModel.applyEqualAtoms();
		}
		
		return minionModel;
		
		
		
	}
	
	/**
	 * Tailor the HashMap of the normalised model (that contains the original (string) and 
	 * the replacement for the original variable (ArithmeticAtomExpression) and tailor 
	 * it to a MinionAtom.
	 * 
	 * @return
	 */
	private HashMap<String, MinionAtom> tailorEqualAtoms() 
		throws MinionException {
		
		HashMap<String, MinionAtom> equalAtoms = new HashMap<String, MinionAtom>();
		ArrayList<ArithmeticAtomExpression> oldOriginalVariables = this.normalisedModel.getReplaceableVariables();
		
		for(int i=0; i< oldOriginalVariables.size(); i++) {
			
			ArithmeticAtomExpression oldAtom = oldOriginalVariables.get(i);
			MinionAtom replacement = toMinion(this.normalisedModel.getReplacementFor(oldAtom.toString()));
			equalAtoms.put(toMinion(oldAtom).toString(),replacement);
			
		}
		
		return equalAtoms;
	}
	
	private void tailorObjective()
		throws MinionException {
		
		Expression objective = this.normalisedModel.getObjectiveExpression();
		if(objective instanceof RelationalAtomExpression ||
				objective instanceof ArithmeticAtomExpression) {
			this.minionModel.setObjective((MinionAtom) toMinion(objective), this.normalisedModel.isObjectiveMaximising());
		}
	}
	
	/**
	 * Tailors the normalised model, that was given in the constructor, to 
	 * a minion model.
	 * 
	 */
	public MinionModel tailorToMinion(NormalisedModel normalisedModel) 
		throws MinionException {
		
		this.normalisedModel = normalisedModel;
		
		// 1. tailor the variables and create a new empty model
		MinionModel minionModel = new MinionModel(new ArrayList<MinionConstraint>(),
				                           mapDecisionVariables(),
				                           this.normalisedModel.getDecisionVariablesNames(),
				                           this.normalisedModel.getAuxVariables(),
				                           this.solverSettings, settings
				                           );
		
		// 2. tailor the constraints
		for(int i=this.normalisedModel.getConstraints().size()-1; i>=0; i--) {
			//System.out.println("Tailoring constraint:"+normalisedModel.getConstraints().get(i));
			minionModel.addConstraint(toMinion(this.normalisedModel.getConstraints().remove(i)));
		}
		
		return minionModel;
	}
	
	
	public String getEssenceSolution(String solverOutput) {
		return this.minionModel.getEssenceSolution(solverOutput);
	}
	
	/**
	 * 
	 * @return
	 * @throws MinionException
	 */
	protected HashMap<String, ConstantDomain> mapDecisionVariables() 
		throws MinionException {
		
		HashMap<String,ConstantDomain> decisionVariables = new HashMap<String, ConstantDomain>();
		ArrayList<String> decisionVariablesNames = this.normalisedModel.getDecisionVariablesNames();
		
		for(int i=0; i<decisionVariablesNames.size(); i++) {
			String varName = decisionVariablesNames.get(i);
			Domain domain = this.normalisedModel.getDomainOfVariable(varName);
			domain = domain.evaluate();
			if(domain instanceof ConstantDomain) {
				ConstantDomain constantDomain = (ConstantDomain) domain;
				decisionVariables.put(varName, constantDomain);
				
				if(constantDomain instanceof ConstantArrayDomain) {
					ConstantDomain[] indices = ((ConstantArrayDomain) constantDomain).getIndexDomains();
					int[] offsets = new int[indices.length];
					
					for(int j=0; j<offsets.length; j++) {
						
						if(indices[j] instanceof BoolDomain)
							offsets[j] = 0;
						else if(indices[j] instanceof BoundedIntRange)
							offsets[j] = ((BoundedIntRange) indices[j]).getRange()[0]; // lowerBound
						
						else throw new MinionException("Cannot translate sparse domains as array-indices yet: variable '"+
								varName+"', domain: "+domain);
					}
					this.offsetsFromZero.put(varName, offsets);
				}
					
				
			}
			else throw new MinionException("Found non-constant domain:"+domain+". Please define all parameters.");
			
		}
		
		return decisionVariables;
	}
	
	/**
	 * 
	 * @param constraint that should be flattened to Minion level.
	 * @return the String representation in Minion for the given 
	 * expression. 
	 */
	protected MinionConstraint toMinion(Expression constraint) 
		throws MinionException {
		
		//System.out.println("Tailoring expression "+constraint+" that is going to be reified? "+constraint.isGonnaBeFlattenedToVariable());
		
		if(constraint instanceof ArithmeticAtomExpression)
			return toMinion((ArithmeticAtomExpression) constraint);
		
		if(constraint instanceof CommutativeBinaryRelationalExpression)
			return toMinion((CommutativeBinaryRelationalExpression) constraint);
		
		if(constraint instanceof NonCommutativeRelationalBinaryExpression)
			return toMinion((NonCommutativeRelationalBinaryExpression) constraint);
		
		if(constraint instanceof SumConstraint)
			return toMinion((SumConstraint) constraint);
		
		if(constraint instanceof Reification) 
			return toMinion((Reification) constraint);
		
		if(constraint instanceof Disjunction)
			return toMinion((Disjunction) constraint);
	
		if(constraint instanceof Conjunction)
			return toMinion((Conjunction) constraint);
		
		if(constraint instanceof translator.expression.ProductConstraint)
			return toMinion((translator.expression.ProductConstraint) constraint);
		
		if(constraint instanceof Multiplication)
			return toMinion((Multiplication) constraint);
		
		if(constraint instanceof RelationalAtomExpression)
			return toMinion((RelationalAtomExpression) constraint);
		
		if(constraint instanceof translator.expression.AllDifferent) 
			return toMinion((translator.expression.AllDifferent) constraint);
		
		if(constraint instanceof LexConstraint) 
			return toMinion((LexConstraint) constraint);
		
		if(constraint instanceof ElementConstraint)
			return toMinion((ElementConstraint) constraint);
		
		if(constraint instanceof Atmost)
			return toMinion((Atmost) constraint);
	
		if(constraint instanceof TableConstraint)
			return toMinion((TableConstraint) constraint);
		
		if(constraint instanceof AbsoluteValue) 
			return toMinion((AbsoluteValue) constraint);
		
		throw new MinionException("Cannot tailor expression to Minion yet:"+constraint);
	}
	
	
	/**
	 * Tailors table constraints to Minion.
	 * 
	 * @param table
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(TableConstraint table) 
		throws MinionException {
		
		Variable[] oldVars = table.getVariables();
		MinionAtom[] variables = new MinionAtom[oldVars.length];
		for(int i=0; i<oldVars.length; i++) {
			oldVars[i].willBeFlattenedToVariable(true);
			variables[i] = toMinion(new ArithmeticAtomExpression(oldVars[i]));
		}
		
		if(table.isGonnaBeFlattenedToVariable()){
			if(this.solverSettings.supportsReificationOf(Expression.TABLE_CONSTRAINT))
				return reifyMinionConstraint(new Table(variables, table.getTupleList()));
			else throw new MinionException("Cannot tailor TABLE constraint to Minion:"+table+
					"\ntable is not reifiable.");
		}
		else 	
			return new Table(variables, table.getTupleList());
	}
	
	
	/**
	 * Maps atmost/atleast to the compat version of the occurrenceLeq/Geq constraint in Minion
	 * 
	 * @param atmost
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(Atmost atmost) 
		throws MinionException {
		
		MinionArray array = toMinionArray(atmost.getArray());
		
		int[] occurrences = atmost.getOccurrences();
		int[] values = atmost.getValues();
		
		if(occurrences.length != values.length)
			throw new MinionException("Cannot tailor atmost/atleast constraint:"+atmost+
					".\n Occurrences and values have different sizes.");
 		
		if(atmost.isGonnaBeFlattenedToVariable()) {
			if((atmost.isAtmost() && this.solverSettings.supportsReificationOf(Expression.ATLEAST_CONSTRAINT)) ||
					(!atmost.isAtmost() && this.solverSettings.supportsReificationOf(Expression.ATMOST_CONSTRAINT))){
				return reifyMinionConstraint(new OccurrenceLeq(array,
				                 values,
				                 occurrences,
				                 atmost.isAtmost()));
			}
			else throw new MinionException("Cannot tailor ATMOST/ATLEAST constraint to Minion:"+atmost+
					"\natmost/atleast is not reifiable.");
		}
		else 
			return new OccurrenceLeq(array,
				                 values,
				                 occurrences,
				                 atmost.isAtmost());
	}
	
	
	/**
	 * Tailor a element constraint to Minion element constraint. The arguments of the element
	 * constraint should all been flattened to atom expressions.
	 * 
	 * @param elementConstraint
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(ElementConstraint elementConstraint) 
		throws MinionException {
		
		
		MinionArray indexedArray = toMinionArray((Array) elementConstraint.getArguments()[0]);
		Expression indexExpression = elementConstraint.getArguments()[1];
		Expression resultExpression = elementConstraint.getArguments()[2];
		
		// check if the index is an atom
		if(!(indexExpression instanceof ArithmeticAtomExpression) &&
				!(indexExpression instanceof RelationalAtomExpression)) 
			throw new MinionException("Cannot tailor expression '"+elementConstraint+"' to Minion element constraint:\n"
					+" The index-expression has to be an atom:"+indexExpression);
		
		MinionAtom index = (MinionAtom) toMinion(indexExpression);
		
		// check if the result is an atom
		if(!(resultExpression instanceof ArithmeticAtomExpression) &&
				!(resultExpression instanceof RelationalAtomExpression)) 
			throw new MinionException("Cannot tailor expression '"+elementConstraint+"' to Minion element constraint:\n"
					+" The result-expression has to be an atom:"+resultExpression);
		
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		if(elementConstraint.isGonnaBeFlattenedToVariable()) {
			this.minionModel.constraintList.add(new Element(indexedArray,
					                                        index,
					                                        result));
			return result;
		}
		else return new Element(indexedArray,
								index,
								result);
	}
	
	/**
	 * Converts LexConstraints into  Minion lex constraitn. Since Minion
	 * only provides a lexless and lexleq constraint, geq and greater
	 * are converted into leq and less respectively.
	 * 
	 * We also need to flatten arrays to 1-dimensional vectors
	 * 
	 * @param lexConstraint
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(LexConstraint lexConstraint)
		throws MinionException {
		
		MinionArray leftArray = toMinionArray(lexConstraint.getLeftArray());
		MinionArray rightArray = toMinionArray(lexConstraint.getRightArray());
		
		if(lexConstraint.getOperator() == Expression.LEX_GEQ) {
			return new LexlessConstraint(rightArray, leftArray, false);
		}
		else if(lexConstraint.getOperator() == Expression.LEX_GREATER) {
			return new LexlessConstraint(rightArray, leftArray, true);
		}
		else if(lexConstraint.getOperator() == Expression.LEX_LEQ) {
			return new LexlessConstraint(leftArray, rightArray, false);
		}
		else if(lexConstraint.getOperator() == Expression.LEX_LESS) {
			return new LexlessConstraint(leftArray, rightArray, true);
		}
		
		else throw new MinionException("Unknown lex-operator in lex-constraint:"+lexConstraint);
	}
	
	
	protected MinionConstraint toMinion(translator.expression.AllDifferent allDiff) 
		throws MinionException {
		
		MinionArray array = toMinionArray((Array) allDiff.getArgument());
		return new AllDifferent(array);
	}
	
	/**
	 * Tailor simple array
	 * 
	 * @param array
	 * @return
	 * @throws MinionException
	 */
	protected MinionArray toMinionArray(Array array) 
		throws MinionException {
		
		if(array instanceof SimpleArray) {
			
			if(((SimpleArray) array).willBeFlattenedToVector()) {
				
				SimpleArray simpleArray = (SimpleArray) array;
				String arrayName = simpleArray.getArrayName();
				String alias = "ALIAS "+arrayName+this.FLATTENED_TO_VECTOR_SUFFIX;
				
				BasicDomain[] indexDomains = simpleArray.getIndexDomains();
				
				// case: 1-dimensional array -> is already a vector
				if(indexDomains.length == 1)
					return new MinionSimpleArray(((SimpleArray) array).getArrayName());
				
				
				// case: 2-dimensional array
				if(indexDomains.length == 2) {
					int noRows = 0;
					int noCols = 0;
					
					if(indexDomains[0] instanceof ConstantDomain) {
						ConstantDomain rowDomain = (ConstantDomain) indexDomains[0];	
						noRows = rowDomain.getRange()[1] - rowDomain.getRange()[0] + 1;
					}
					else throw new MinionException("Cannot tailor to Minion if index-domain '"+indexDomains[0]+"' of "+arrayName+
							" cannot be evaluated to a constant.");
					
					if(indexDomains[1] instanceof ConstantDomain) {
						ConstantDomain colDomain = (ConstantDomain) indexDomains[1];						
						noCols = colDomain.getRange()[1] - colDomain.getRange()[0] + 1;
					}
					else throw new MinionException("Cannot tailor to Minion if index-domain '"+indexDomains[1]+"' of "+arrayName+
							" cannot be evaluated to a constant.");
					
					String elements = "";
					for(int row=0; row < noRows; row++){
						for(int col=0; col<noCols; col++) {
							elements = elements+arrayName+"["+row+","+col+"]";
							if(row != noRows-1 || col != noCols-1) elements = elements+",";
							if(col == noCols-1) elements = elements+"\n\t\t";	
						}
					}
					
					alias = alias+"["+(noRows*noCols)+"] = ["+elements+"]\n";
				}
				
				// case : 3-dimensional array
				else if(indexDomains.length ==3) {
					int noPlanes = 0;
					int noRows = 0;
					int noCols = 0;
					
					
					// get the amount of rows, cols and planes
					if(indexDomains[0] instanceof ConstantDomain) {
						ConstantDomain rowDomain = (ConstantDomain) indexDomains[0];	
						noRows = rowDomain.getRange()[1] - rowDomain.getRange()[0] + 1;
					}
					else throw new MinionException("Cannot tailor to Minion if index-domain '"+indexDomains[0]+"' of "+arrayName+
							" cannot be evaluated to a constant.");
					
					if(indexDomains[1] instanceof ConstantDomain) {
						ConstantDomain colDomain = (ConstantDomain) indexDomains[1];						
						noCols = colDomain.getRange()[1] - colDomain.getRange()[0] + 1;
					}
					else throw new MinionException("Cannot tailor to Minion if index-domain '"+indexDomains[1]+"' of "+arrayName+
							" cannot be evaluated to a constant.");
					
					if(indexDomains[2] instanceof ConstantDomain) {
						ConstantDomain planesDomain = (ConstantDomain) indexDomains[2];						
						noPlanes = planesDomain.getRange()[2] - planesDomain.getRange()[0] + 1;
					}
					else throw new MinionException("Cannot tailor to Minion if index-domain '"+indexDomains[2]+"' of "+arrayName+
							" cannot be evaluated to a constant.");
					
					// put together the flattened vector
					String elements = "";
					for(int plane=0; plane < noPlanes; plane++) {
						for(int row=0; row < noRows; row++){
							for(int col=0; col<noCols; col++) {
								
								elements = elements+arrayName+"["+plane+","+row+","+col+"]";
								if(plane != noPlanes-1) elements = elements+",";
								if(col == noCols-1) elements = elements+"\n\t\t";
							
							}
						}
					}
					alias = alias+"["+(noPlanes*noRows*noCols)+"] = ["+elements+"]\n";
				}
				else throw new MinionException("Sorry, cannot flatten arrays with more than 3 dimensions:"+array);
				
				
				this.minionModel.addAlias(alias);
				return new MinionSimpleArray(arrayName+this.FLATTENED_TO_VECTOR_SUFFIX);
			
 			}
			
			return new MinionSimpleArray(((SimpleArray) array).getArrayName());			
		}
		else if(array instanceof IndexedArray) {
			
			String arrayName = ((IndexedArray) array).getArrayName();
			BasicDomain[] indexRanges = ((IndexedArray) array).getIndexRanges();
			ConstantDomain[] indices = new ConstantDomain[indexRanges.length];
			boolean[] indexIsWholeDomain = new boolean[indexRanges.length];
			
			int[] indexOffsets = null;
			if(this.offsetsFromZero.containsKey(arrayName))
				indexOffsets = this.offsetsFromZero.get(arrayName);
			else throw new MinionException("Internal error: did not find any index-offsets for array:"+arrayName+
					". Maybe the array is undefined?");
			
 			for(int i=0; i<indexRanges.length; i++) {
 				indexRanges[i] = (BasicDomain) indexRanges[i].evaluate();
				if(indexRanges[i] instanceof ConstantDomain) {
					indices[i] = (ConstantDomain) indexRanges[i];
					indexIsWholeDomain[i] = isFullDomain(arrayName, indices[i], i);
				}
				else throw new MinionException("Sorry, cannot translate arrays that are indexed with variables yet:"+array);
			}
			
			return new MinionIndexedArray(arrayName, 
					                      indices, 
					                      indexIsWholeDomain,
					                      indexOffsets);
			 
		}
		
		else if(array instanceof VariableArray) {
			
			VariableArray varArray = (VariableArray) array;
			AtomExpression[] variables = varArray.getVariables();
			MinionAtom[] minionVariables= new MinionAtom[variables.length];
			
			for(int i=0; i<variables.length; i++) {
				if(variables[i] instanceof RelationalAtomExpression) {
					variables[i].willBeFlattenedToVariable(true);
					minionVariables[i] = (MinionAtom) toMinion((RelationalAtomExpression) variables[i]);
				}
				else if(variables[i] instanceof ArithmeticAtomExpression) {
					variables[i].willBeFlattenedToVariable(true);
					minionVariables[i] = (MinionAtom) toMinion((ArithmeticAtomExpression) variables[i]);
				}
				else throw new MinionException("Unfeasible type '"+variables[i]+"' in VariableArray: "+varArray);
			}
			
			return new MinionVariableArray(minionVariables);
		}
		
		else throw new MinionException("Sorry, cannot translate array type yet:"+array);

	}
	
	/**
	 * This method returns true if the array/matrix is indexed by the 
	 * ConstantDomain d at dimension 'index', then the range given by
	 * d corresponds exactly to the declared range of the matrix. 
	 * For example, if i have the matrix 'M' that is defined as 
	 * 
	 * M : matrix indexed by [int(1..5),int(1..10)] of bool
	 * 
	 * then if I look at expression M[1..5, 3..6], the range the matrix is indexed with with at 
	 * the first dimension (1..5) corresponds exactly to the range the matrix is declared with(int(1..5)),
	 * while the second dimension is indexed with (3..6) which does not correspond to int(1..10),
	 * as defined.  
	 * 
	 * @param arrayName
	 * @param d
	 * @param index
	 * @return
	 * @throws MinionException
	 */
	protected boolean isFullDomain(String arrayName, ConstantDomain d, int index) 
		throws MinionException {
		
		Domain domain = this.normalisedModel.getDomainOfVariable(arrayName);
		//System.out.println("Checking if domain is full: "+domain+" with type:"+domain.getType());
		
		
		if(domain == null)
			throw new MinionException("Unknown array variable '"+arrayName+", or cannot tailor constant vector to Minion");
		
		
		if(domain instanceof ArrayDomain) {
			ArrayDomain arrayDomain  = (ArrayDomain) domain;
			Domain[] indexDomains = arrayDomain.getIndexDomains();
			if(index < indexDomains.length && index >= 0) {
				Domain indexDomain = indexDomains[index];
					
				if(indexDomain instanceof ConstantDomain) { 
						if(indexDomain.getType() == d.getType()) {
						    if(d.isSmallerThanSameType((ConstantDomain) indexDomain) == Expression.EQUAL)
						    	return true;
						    
						}
				}
				return false;
			}
			else throw new MinionException("Wrong dimensions: the variable '"+arrayName+"' has been declared to be a "+indexDomains.length+
					"-dimensional array/matrix, so you cannot index it at dimension '"+index+"', sorry.");
			
		}
		else if (domain instanceof ConstantArrayDomain) {
			ConstantArrayDomain arrayDomain  = (ConstantArrayDomain) domain;
			Domain[] indexDomains = arrayDomain.getIndexDomains();
			if(index < indexDomains.length && index >= 0) {
				Domain indexDomain = indexDomains[index];
					
				if(indexDomain instanceof ConstantDomain) { 
						if(indexDomain.getType() == d.getType()) {
						    if(d.isSmallerThanSameType((ConstantDomain) indexDomain) == Expression.EQUAL)
						    	return true;
						    
						}
				}
				return false;
			}
			else throw new MinionException("Wrong dimensions: the variable '"+arrayName+"' has been declared to be a "+indexDomains.length+
					"-dimensional array/matrix, so you cannot index it at dimension '"+index+"', sorry.");
			
		}
		else throw new MinionException("Incompatible types: cannot dereference variable that is not an array/matrix variable:"+arrayName);
		
		
	}
	
	
	/**
	 * This method always returns a minion atom, since there is NO way a multiplication
	 * can occur in Minion without being flattened.
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	protected MinionAtom toMinion(Multiplication constraint) 
		throws MinionException {
		
		ArrayList<Expression> arguments = constraint.getArguments();
		if(arguments.size() != 2)
			throw new MinionException("Minion's product constraint only takes two arguments. Therefore cannot translate:"+constraint);
		
		arguments.get(0).willBeFlattenedToVariable(true);
		arguments.get(1).willBeFlattenedToVariable(true);
		
		MinionAtom leftArg = (MinionAtom) toMinion(arguments.get(0));
		MinionAtom rightArg = (MinionAtom) toMinion(arguments.get(1));
		
		MinionAtom auxVariable = createMinionAuxiliaryVariable(constraint.getDomain()[0], constraint.getDomain()[1]);
		ProductConstraint product = new ProductConstraint(leftArg,
				 										  rightArg,
				 										  auxVariable);
		this.minionModel.addConstraint(product);
		return auxVariable;
	}
	
	
	/**
	 * 
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(translator.expression.ProductConstraint constraint) 
		throws MinionException {
		
		Expression[] arguments = constraint.getArguments();
		if(arguments.length != 2)
			throw new MinionException("Minion's product constraint only takes two arguments. Therefore cannot translate:"+constraint);
		
		arguments[0].willBeFlattenedToVariable(true);
		arguments[1].willBeFlattenedToVariable(true);
		
		MinionAtom[] productArgs = new MinionAtom[2];
		productArgs[0] = (MinionAtom) toMinion(arguments[0]);
		productArgs[1] = (MinionAtom) toMinion(arguments[1]);
		
		
		Expression resultExpression = constraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		ProductConstraint minionProduct = new ProductConstraint(productArgs[0],
				                                                productArgs[1],
				                                                result);
		
		if(constraint.isGonnaBeFlattenedToVariable()) 
			return reifyMinionConstraint(minionProduct);
		else return minionProduct;
	
	}
	
	
	/**
	 * Translate a conjunction by a sum constraint
	 * 
	 * @param conjunction
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(Conjunction conjunction) 
		throws MinionException {
		
		ArrayList<Expression> conjointArgs = conjunction.getArguments();
		MinionAtom[] arguments = new MinionAtom[conjointArgs.size()];
		
		if(arguments.length == 1) {
			if(conjunction.isGonnaBeFlattenedToVariable())
				return (MinionAtom) toMinion(conjointArgs.get(0));
			else return new EqConstraint((MinionAtom) toMinion(conjointArgs.get(0)), 
					                      new MinionConstant(1));
		}
		
		for(int i=0; i<arguments.length; i++) {
			Expression conjointArg = conjointArgs.get(i);
			conjointArg.willBeFlattenedToVariable(true);
			arguments[i] = (MinionAtom) toMinion(conjointArg);
		}
		
		if(conjunction.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(new SumGeqConstraint(arguments, new MinionConstant(arguments.length)));
		}
		else return new SumGeqConstraint(arguments, new MinionConstant(arguments.length));
		
		
	}
	
	
	/**
	 * Translate a disjunction by a sum constraint
	 * 
	 * @param disjunction
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(Disjunction disjunction)
		throws MinionException {
		
		
		ArrayList<Expression> disjointArgs = disjunction.getArguments();
		MinionAtom[] arguments = new MinionAtom[disjointArgs.size()];
		
		if(arguments.length == 1) {
			if(disjunction.isGonnaBeFlattenedToVariable())
				return (MinionAtom) toMinion(disjointArgs.get(0));
			else return new EqConstraint((MinionAtom) toMinion(disjointArgs.get(0)), 
					                      new MinionConstant(1));
		}
		
		for(int i=0; i<arguments.length; i++) {
			Expression disjointArg = disjointArgs.get(i);
			disjointArg.willBeFlattenedToVariable(true);
			arguments[i] = (MinionAtom) toMinion(disjointArg);
		}
		
		if(disjunction.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(new SumGeqConstraint(arguments, new MinionConstant(1)));
		}
		else return new SumGeqConstraint(arguments, new MinionConstant(1));
	}
	
	
	/**
	 * Map a reification constraint to Minion. This method does not
	 * return a variable!!
	 * 
	 * @param reification
	 * @return a Reify Constraint
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(Reification reification) 
		throws MinionException {
		
		//System.out.println("Generating reified constraint:"+reification);
		
		Expression constraint = reification.getReifiedConstraint();
		
		// still need to flatten if we have a sum as an argument
		if(constraint instanceof SumConstraint) {
			SumConstraint sumConstraint = (SumConstraint) constraint;
			if(sumConstraint.getOperator() == Expression.EQ) {
				constraint.willBeFlattenedToVariable(true);
			}
			else constraint.willBeFlattenedToVariable(false);
		}
		else constraint.willBeFlattenedToVariable(false); // we don't need to reify this constraint again
		MinionConstraint reifiedConstraint = toMinion(constraint);
		
		if(reifiedConstraint instanceof MinionAtom)
			return new EqConstraint((MinionAtom) reifiedConstraint, (MinionAtom) toMinion(reification.getReifiedVariable()) );
		
		MinionAtom reifiedVariable = (MinionAtom) toMinion(reification.getReifiedVariable());
		addToSubExpressions(reifiedConstraint, reifiedVariable);
		
		//System.out.println("Mapped it now to :"+reifiedConstraint+" with var:"+reifiedVariable);
		
		if(reification.isGonnaBeFlattenedToVariable()) {
			this.minionModel.addConstraint(reifiedConstraint);
			return reifiedVariable;
		}
		//if(reification.isGonnaBeFlattenedToVariable())
		//	return reifyMinionConstraint(new Reify(reifiedConstraint, reifiedVariable));
		
		return new Reify(reifiedConstraint, reifiedVariable);
	}
	
	
	/**
	 * Tailor absolute value constraint | x | into Minion. For this we need 
	 * to flatten the representation as |x | = y and use y instead.
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionAtom toMinion(AbsoluteValue constraint) 
		throws MinionException {
		
		if(constraint.getArgument() instanceof ArithmeticAtomExpression) {
			ArithmeticAtomExpression e = (ArithmeticAtomExpression) constraint.getArgument();
		
			MinionAtom auxVar;
			MinionAtom argument = toMinion(e);
			
			if(hasCommonSubExpression(argument)) {
				auxVar = getCommonSubExpression(argument);
			}
			else {
				int ub = e.getDomain()[1];
				
				if(ub <0) ub = -ub;
				
				auxVar = createMinionAuxiliaryVariable(0,ub);
				addToSubExpressions(argument, auxVar);
			}
			
			/*if(constraint.isGonnaBeFlattenedToVariable()) {
				return auxVar;
			} 
			else return new MinionAbsoluteValue(argument, auxVar);
			*/
			this.minionModel.constraintList.add(new MinionAbsoluteValue(argument, auxVar));
			return auxVar;
		}
		
		else throw new MinionException("Internal error or invalid argument. Cannot tailor constraint expression '"+constraint.getArgument()
				+"' as argument of an absolute constraint: "+constraint);
		
	}
	
	
	/**
	 * Translate a sum constraint to Minion. A sum constraint is a collection
	 * of positive and negative arguments that are added up and in a relation
	 * (<,>,=,!=,<=,>=) to a certain result expression.
	 * 
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(SumConstraint sumConstraint) 
		throws MinionException {
		
		int operator = sumConstraint.getRelationalOperator();
		
		if(operator == Expression.LESS ||
				operator == Expression.GREATER ||
				operator == Expression.NEQ)
			return toMinionStrongIneqSumConstraint(sumConstraint);
		
		else 
			return toMinionWeakIneqSumConstraint(sumConstraint);
		
	}
	
	
	/**
	 * Tailor a strong inequality (involving <,>,!=) to a Minion sum constraint.
	 * (Don't worry about commutativity of the operators - in the internal
	 * representation of the SumConstraint, the result part is always on the
	 * right side.
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinionStrongIneqSumConstraint(SumConstraint sumConstraint)
		throws MinionException {
		
		//System.out.println("tailoring a string sum constraint to minion:"+sumConstraint);
		
		Expression[] positiveArguments = sumConstraint.getPositiveArguments();
		Expression[] negativeArguments = sumConstraint.getNegativeArguments();
		Expression resultExpression = sumConstraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		int operator = sumConstraint.getRelationalOperator();
		
		
		ArithmeticAtomExpression auxVariable = null;
		if(hasCommonSubExpression(new Sum(positiveArguments, negativeArguments))) {
			//System.out.println("COMMON subexpression for sum constraint:"+auxVariabe+" while translating: "+sumConstraint);
			auxVariable = getCommonSubExpression(new Sum(positiveArguments, negativeArguments)).copy();
			//System.out.println("COMMON subexpression "+auxVariable+" for sum constraint: "+sumConstraint);
		}
			
		else {
			auxVariable = new ArithmeticAtomExpression(createAuxVariable(sumConstraint.getSumDomain()[0], 
					                                                     sumConstraint.getSumDomain()[1]));
			addToSubExpressions(new Sum(positiveArguments, negativeArguments), auxVariable);
																					 
		
			SumConstraint firstSum = new SumConstraint(positiveArguments,
				                                   negativeArguments,
				                                   Expression.EQ,
				                                   auxVariable,
				                                   true);
			//System.out.println("intermediate sum consteaint:"+firstSum+" while translating: "+sumConstraint);
			MinionConstraint firstConstraint = toMinionWeakIneqSumConstraint(firstSum);
			this.minionModel.addConstraint(firstConstraint);
		}
		
		
		
		if(sumConstraint.isGonnaBeFlattenedToVariable()) {
			if(operator == Expression.LESS) {
				IneqConstraint constraint =  
							new IneqConstraint((MinionAtom) toMinion(auxVariable), result, -1) ;
						
				return reifyMinionConstraint(constraint);		
			}
			else if(operator == Expression.GREATER) {
				IneqConstraint constraint =  
							new IneqConstraint(result, (MinionAtom) toMinion(auxVariable), -1) ;
						
				return reifyMinionConstraint(constraint);	
			}
			else if(operator == Expression.NEQ) {
				DiseqConstraint constraint =  new DiseqConstraint((MinionAtom) toMinion(auxVariable), result);		
				return reifyMinionConstraint(constraint);					
				
			}
			else throw new MinionException("Internal error. Tried to tailor non-strong relational sum by strong sum method:"+sumConstraint);
		}
		else {
			if(operator == Expression.LESS) {
				return  new IneqConstraint((MinionAtom) toMinion(auxVariable), result, -1) ;		
			}
			else if(operator == Expression.GREATER) {
				return new IneqConstraint(result, (MinionAtom) toMinion(auxVariable), -1) ;
						
			}
			else if(operator == Expression.NEQ) {
				return  new DiseqConstraint((MinionAtom) toMinion(auxVariable), result);		
				
			}
			else throw new MinionException("Internal error. Tried to tailor non-strong relational sum by strong sum method:"+sumConstraint);
			
			
		}

	}
	
	
	/**
	 * Translate SumConstraints that have the following operators:
	 * <=. >=. =
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinionWeakIneqSumConstraint(SumConstraint sumConstraint)
		throws MinionException {
	
		Expression[] positiveArgs = sumConstraint.getPositiveArguments();
		Expression[] negativeArgs = sumConstraint.getNegativeArguments();
		
		//System.out.println("Tailoring a weak sumConstraint "+sumConstraint+" that needs to be reified? "+sumConstraint.isGonnaBeFlattenedToVariable());
		// ---------------- we have to reify the sum!! --------------------------------------
		// // don't use watched or weighted sum then!
		if(sumConstraint.isGonnaBeFlattenedToVariable() && negativeArgs.length ==0) {
	
			//System.out.println("Tailoring a weak sumConstraint that needs to be reified:"+sumConstraint);
			
			MinionAtom[] arguments = new MinionAtom[positiveArgs.length+negativeArgs.length];
			
			for(int i=0; i<positiveArgs.length; i++) {
				positiveArgs[i].willBeFlattenedToVariable(true);
				arguments[i] = (MinionAtom) toMinion(positiveArgs[i]);
			}
			Expression resultExpression = sumConstraint.getResult();
			resultExpression.willBeFlattenedToVariable(true);
			MinionAtom result = (MinionAtom) toMinion(resultExpression);
			
			int operator = sumConstraint.getRelationalOperator();
			
			if(operator == Expression.LEQ) 
				return reifyMinionConstraint(new SumLeqConstraint(arguments, result));
			
			else if(operator == Expression.GEQ)
				return reifyMinionConstraint(new SumGeqConstraint(arguments,result));
			
			else if(operator == Expression.EQ) {
				SumLeqConstraint sum1 = new SumLeqConstraint(arguments, result);
				SumGeqConstraint sum2 = new SumGeqConstraint(arguments, result);
				
				MinionAtom auxVariable1 = reifyMinionConstraint(sum1);
				MinionAtom auxVariable2 = reifyMinionConstraint(sum2);
				
				MinionAtom reifiedVariable = createMinionAuxiliaryVariable();
				
				ProductConstraint conjunction = new ProductConstraint(auxVariable1,auxVariable2,reifiedVariable);
				this.minionModel.addConstraint(conjunction);
				
				return reifiedVariable;
				
			}
			else throw new MinionException("Interal error: expected only weak operator instead of operator '"
					+operator+"' in constraint:"+sumConstraint);
		}
		
		//--------------- else: we don't have to reify the constraint -----------------------------------------
		else {	
			// check what kind of sum we have here and look for the right kind to match up with
			boolean hasNegativeArguments = (negativeArgs.length > 0) ? true : false;
			boolean hasMultiplication = false;
			
			for(int i=0; i<positiveArgs.length; i++) {
				if(positiveArgs[i] instanceof UnaryMinus)
					hasNegativeArguments = true;
				
				if(positiveArgs[i] instanceof Multiplication) {
					hasMultiplication = true;
				}
			}
			
			if(hasNegativeArguments || hasMultiplication) {
				return toMinionWeakWeightedSumConstraint(sumConstraint);
				
			}
			// positive, non linear
			else return toSimpleUnReifiedWeakSumConstraint(sumConstraint);
		
		}
	}
	
	
	/**
	 * Return a weightedsumleq/weightedsumgeq (or both for =) constraint. 
	 * It will NOT reify a constraint!!
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinionWeakWeightedSumConstraint(SumConstraint sumConstraint) 
		throws MinionException {
		
		Expression[] positiveArguments = sumConstraint.getPositiveArguments();
		Expression[] negativeArguments = sumConstraint.getNegativeArguments();
		
		int operator = sumConstraint.getRelationalOperator();
		Expression resultExpression = sumConstraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		// check for the case when E1 - E2 RELOP 0   ===>   E1 RELOP E2
		if(positiveArguments.length == 1 && 
				negativeArguments.length == 1 &&
				resultExpression.getType() == Expression.INT) {
			
			if(((ArithmeticAtomExpression) resultExpression).getConstant() == 0) {
				Expression posArg = positiveArguments[0];
				posArg.willBeFlattenedToVariable(true);
				Expression negArg = negativeArguments[0];
				negArg.willBeFlattenedToVariable(true);
				
				if(!sumConstraint.isGonnaBeFlattenedToVariable()) {
					if(operator == Expression.LEQ)
						return new IneqConstraint((MinionAtom) toMinion(posArg), (MinionAtom) toMinion(negArg));
				
					else if(operator == Expression.GEQ)
						return new IneqConstraint((MinionAtom) toMinion(negArg), (MinionAtom) toMinion(posArg));
				
					else if(operator == Expression.EQ) 
						return new EqConstraint((MinionAtom) toMinion(negArg), (MinionAtom) toMinion(posArg));
				}
				else {
					if(operator == Expression.LEQ)
						return reifyMinionConstraint(new IneqConstraint((MinionAtom) toMinion(posArg), (MinionAtom) toMinion(negArg)));
				
					else if(operator == Expression.GEQ)
						return reifyMinionConstraint(new IneqConstraint((MinionAtom) toMinion(negArg), (MinionAtom) toMinion(posArg)));
				
					else if(operator == Expression.EQ) 
						return reifyMinionConstraint(new EqConstraint((MinionAtom) toMinion(negArg), (MinionAtom) toMinion(posArg)));
				}
			}	
		}
		
		
		int[] weights = new int[positiveArguments.length + negativeArguments.length];
		MinionAtom[] arguments = new MinionAtom[positiveArguments.length + negativeArguments.length];
			
			for(int i=0; i<positiveArguments.length; i++) {
				Expression argument = positiveArguments[i];
				
				if(argument instanceof Multiplication) {
					argument.orderExpression();
					argument.evaluate();
					Multiplication product = (Multiplication) argument;
					
					if(product.getArguments().size() > 2) 
						throw new MinionException("Cannot translate n-ary product constraint:"+argument);
					
					else if(product.getArguments().size() == 1) {
						weights[i] = 1;
						Expression onlyArg = product.getArguments().remove(0);
						onlyArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(onlyArg);
					}
					
					// we have exactly 2 arguments
					if(product.getArguments().get(0).getType() == Expression.INT) {
						weights[i] = ((ArithmeticAtomExpression) product.getArguments().remove(0)).getConstant();
						Expression otherArg = product.getArguments().remove(0);
						otherArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(otherArg);
					}
					else {
						product.willBeFlattenedToVariable(true);
						weights[i] = 1;
						arguments[i] = (MinionAtom) toMinion(product);
					}
				} // end if: multiplication
				
				else { // any other type
					argument.willBeFlattenedToVariable(true);
					weights[i] = 1;
					arguments[i] = (MinionAtom) toMinion(argument);	
				}
			} // end for: all positive arguments
			
			
			
			// all negative arguments
			for(int i=positiveArguments.length; i<positiveArguments.length+negativeArguments.length; i++) {
				Expression argument = negativeArguments[i-positiveArguments.length];
				
				if(argument instanceof Multiplication) {
					argument.orderExpression();
					argument.evaluate();
					Multiplication product = (Multiplication) argument;
					
					if(product.getArguments().size() > 2) 
						throw new MinionException("Cannot translate n-ary product constraint:"+argument);
					
					else if(product.getArguments().size() == 1) {
						weights[i] = -1;
						Expression onlyArg = product.getArguments().remove(0);
						onlyArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(onlyArg);
					}
					
					// we have exactly 2 arguments
					if(product.getArguments().get(0).getType() == Expression.INT) {
						weights[i] = -((ArithmeticAtomExpression) product.getArguments().remove(0)).getConstant();
						Expression otherArg = product.getArguments().remove(0);
						otherArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(otherArg);
					}
					else {
						product.willBeFlattenedToVariable(true);
						weights[i] = -1;
						arguments[i] = (MinionAtom) toMinion(product);
					}
				} // end if: multiplication
				
				else { // any other type
					argument.willBeFlattenedToVariable(true);
					weights[i] = -1;
					arguments[i] = (MinionAtom) toMinion(argument);	
				}
			} // end for: all negative arguments
			
		
			boolean allWeightsAreOnes = true;
			
			for(int i=0; i<weights.length; i++)
				allWeightsAreOnes = allWeightsAreOnes && (weights[i] == 1);
			
			if(!sumConstraint.isGonnaBeFlattenedToVariable()) {
			
				if(operator == Expression.LEQ) {
					return (allWeightsAreOnes) ?
							new SumLeqConstraint(arguments, result) :
								new WeightedSumLeqConstraint(arguments,weights, result);
				}
				else if(operator == Expression.GEQ) {
					return  (allWeightsAreOnes)  ? 
							new SumGeqConstraint(arguments, result) :
								new WeightedSumGeqConstraint(arguments,weights, result);
				}
				else if(operator == Expression.EQ) {
					MinionConstraint weightedConstraint1 =  (allWeightsAreOnes) ?
							new SumGeqConstraint(arguments, result) :
								new WeightedSumGeqConstraint(arguments, weights, result);
						
							this.minionModel.addConstraint(weightedConstraint1);
							return (allWeightsAreOnes) ? 
									new SumLeqConstraint(arguments, result) :
										new WeightedSumLeqConstraint(arguments,weights, result);
				
				
				}	
				else throw new MinionException("Internal error: tried to translate non-weak sumConstraint with weak-sumConstraint method:"+sumConstraint);

			}
			else {
				
				if(operator == Expression.LEQ) {
					return reifyMinionConstraint((allWeightsAreOnes) ?
							new SumLeqConstraint(arguments, result) :
								new WeightedSumLeqConstraint(arguments,weights, result));
				}
				else if(operator == Expression.GEQ) {
					return  reifyMinionConstraint((allWeightsAreOnes)  ? 
							new SumGeqConstraint(arguments, result) :
								new WeightedSumGeqConstraint(arguments,weights, result));
				}
				else if(operator == Expression.EQ) {
					MinionConstraint weightedConstraint1 =  (allWeightsAreOnes) ?
							new SumGeqConstraint(arguments, result) :
								new WeightedSumGeqConstraint(arguments, weights, result);
			
					MinionConstraint weightedConstraint2 =  (allWeightsAreOnes) ? 
									new SumLeqConstraint(arguments, result) :
										new WeightedSumLeqConstraint(arguments,weights, result);
									
					MinionAtom auxVariable1 = reifyMinionConstraint(weightedConstraint1);
					MinionAtom auxVariable2 = reifyMinionConstraint(weightedConstraint2);
									
					MinionAtom reifiedVariable = createMinionAuxiliaryVariable();
									
					ProductConstraint conjunction = new ProductConstraint(auxVariable1,auxVariable2,reifiedVariable);
					this.minionModel.addConstraint(conjunction);
									
					return reifiedVariable;
				
				
				}	
				else throw new MinionException("Internal error: tried to translate non-weak sumConstraint with weak-sumConstraint method:"+sumConstraint);
				
				
				
			}
	}
	
	/**
	 * Return a simple sumleq/sumgeq (or both for =) constraint. The sum MAY NOT contain
	 * any negative arguments!! This method will return 
	 * a watched literal sum constriant if the arguments are all booleans.
	 * It will NOT reify a constraint!!
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toSimpleUnReifiedWeakSumConstraint(SumConstraint sumConstraint) 
		throws MinionException {
		
		int operator = sumConstraint.getRelationalOperator();
		Expression[] positiveArgs = sumConstraint.getPositiveArguments();
		Expression resultExpression = sumConstraint.getResult();
	
		// we need this to determine if we can apply the watched literal constraint
		boolean areBooleanArguments = true && (resultExpression.getType() == Expression.BOOL ||
				                                resultExpression.getType() == Expression.INT);
		
		MinionAtom[] arguments = new MinionAtom[positiveArgs.length];
	
		for(int i=0; i<positiveArgs.length; i++) {
			positiveArgs[i].willBeFlattenedToVariable(true);
			arguments[i] = (MinionAtom) toMinion(positiveArgs[i]);
			if(areBooleanArguments) {
				if(!this.minionModel.variableHasBooleanDomain(arguments[i].getVariableName())) {
					areBooleanArguments = false && areBooleanArguments;
				}
			}
		}
		
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		
		if(operator == Expression.LEQ) {
			if(this.minionModel.solverSettings.useWatchedSum()) {
				if(areBooleanArguments) {
					return new SumLeqConstraint(arguments, result, true);
				}
				else return new SumLeqConstraint(arguments, result, false);
			}
			else return new SumLeqConstraint(arguments, result);
		}
		
		else if(operator == Expression.GEQ) {
			if(this.minionModel.solverSettings.useWatchedSum()) {
				if(areBooleanArguments) {
					return new SumGeqConstraint(arguments, result, true);
				}
				else return new SumGeqConstraint(arguments, result, false);
			}
			else return new SumGeqConstraint(arguments, result);	
		}
		
		else if(operator == Expression.EQ) {
			if(this.minionModel.solverSettings.useWatchedSum()) {
				if(areBooleanArguments) {
					this.minionModel.addConstraint(new SumLeqConstraint(arguments, result, true));
					return new SumGeqConstraint(arguments, result, true);
				}
				else {
					this.minionModel.addConstraint(new SumLeqConstraint(arguments, result, false));
					return new SumGeqConstraint(arguments, result, false);
				}
			}
			else {
				this.minionModel.addConstraint(new SumLeqConstraint(arguments, result));
				return new SumGeqConstraint(arguments, result);	
			}
		}
		else throw new MinionException("Internal error: expected only sumconstraint with operators =,<=,>= instead of:"+sumConstraint);
		
		
	}
	
	/**
	 * Converts non commutative binary relations to Minion representation
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(NonCommutativeRelationalBinaryExpression constraint) 
		throws MinionException {
		
		// lex-constraints!!!! -> move them to another expression type!!
		//System.out.println("Translating expression: "+constraint);
		
		int operator = constraint.getOperator();
		Expression leftExpression = constraint.getLeftArgument();
		ArithmeticAtomExpression leftArgument = null;
		Expression rightExpression = constraint.getRightArgument();
		ArithmeticAtomExpression rightArgument = null;
		
		//MinionAtom rightAtom = null;
		//MinionAtom leftAtom = null;
		
		// get the left and right atoms 
		if(!(leftExpression instanceof ArithmeticAtomExpression)) {
			if(leftExpression instanceof RelationalAtomExpression) 
				leftArgument = ((RelationalAtomExpression)leftExpression).toArithmeticExpression();
			/* else if(leftExpression instanceof UnaryMinus) {
				UnaryMinus minusExpression = (UnaryMinus) leftExpression;
				if(minusExpression.getArgument() instanceof ArithmeticAtomExpression) {
					return new MinusEq(toMinion(rightArgument), toMinion((ArithmeticAtomExpression) minusExpression.getArgument()));
				}
				else throw new MinionException("Internal error. Cannot translate constraint nested in another expression as in:"+constraint);	
			}
			else if(leftExpression instanceof AbsoluteValue) {
				leftAtom = toMinion((AbsoluteValue) leftExpression);
			} */
			else throw new MinionException("Cannot translate constraint nested in another expression as in:"+constraint);	
				
		}
		else leftArgument = (ArithmeticAtomExpression) leftExpression;
		
		if(!(rightExpression instanceof ArithmeticAtomExpression)) {
			if(rightExpression instanceof RelationalAtomExpression) 
				rightArgument = ((RelationalAtomExpression)rightExpression).toArithmeticExpression();
			/*else if(rightExpression instanceof UnaryMinus) {
				UnaryMinus minusExpression = (UnaryMinus) rightExpression;
				if(minusExpression.getArgument() instanceof ArithmeticAtomExpression) {
					// TODO: fix this!
					return new MinusEq(toMinion(leftArgument), toMinion((ArithmeticAtomExpression) minusExpression.getArgument()));
				}
				else throw new MinionException("Internal error. Cannot translate constraint nested in another expression as in:"+constraint);	
			}
			else if(rightExpression instanceof AbsoluteValue) {
				rightAtom = toMinion((AbsoluteValue) rightExpression);
			} */
			else throw new MinionException("Cannot translate constraint nested in another expression as in:"+constraint);	
				
		}
		else rightArgument = (ArithmeticAtomExpression) rightExpression;
		
		//if(leftAtom == null) leftAtom = toMinion(leftArgument);
		//if(rightAtom == null) rightAtom = toMinion(rightArgument);
		
		MinionConstraint minionConstraint = null;
		
		if(operator == Expression.LEQ)
			minionConstraint =new IneqConstraint(toMinion(leftArgument), toMinion(rightArgument),0);
		
		else if(operator == Expression.GEQ)
			minionConstraint = new IneqConstraint(toMinion(rightArgument), toMinion(leftArgument), 0);
		
		else if(operator == Expression.LESS)
			minionConstraint = new IneqConstraint(toMinion(leftArgument), toMinion(rightArgument),-1);
		
		else if(operator == Expression.GREATER)
			minionConstraint = new IneqConstraint(toMinion(rightArgument), toMinion(leftArgument), -1);
		
		else if(operator == Expression.IF)
			minionConstraint = new IneqConstraint(toMinion(leftArgument), toMinion(rightArgument),0);
		
		else throw new MinionException("Unknown non-commutative binary relation:"+constraint);
		
		
		if(constraint.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(minionConstraint);
		}
		else return minionConstraint;
	}
	
	
	
	/**
	 * Converts a commutative binary relational expression into the appropriate minion
	 * constraint. IMPORTANT NOTE: requires the arguments to be atoms. 
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(CommutativeBinaryRelationalExpression constraint) 
		throws MinionException {
		
		int operator = constraint.getOperator();
		Expression leftExpression = constraint.getLeftArgument();
		ArithmeticAtomExpression leftArgument = null;
		Expression rightExpression = constraint.getRightArgument();
		ArithmeticAtomExpression rightArgument = null;
		
		MinionAtom rightAtom = null;
		MinionAtom leftAtom = null;
		
		// get the left and right atoms 
		if(!(leftExpression instanceof ArithmeticAtomExpression)) {
			if(leftExpression instanceof RelationalAtomExpression) 
				leftArgument = ((RelationalAtomExpression)leftExpression).toArithmeticExpression();
			else if(leftExpression instanceof UnaryMinus) {
				UnaryMinus minusExpression = (UnaryMinus) leftExpression;
				if(minusExpression.getArgument() instanceof ArithmeticAtomExpression) {
					return new MinusEq(toMinion(rightArgument), toMinion((ArithmeticAtomExpression) minusExpression.getArgument()));
				}
				else throw new MinionException("Internal error. Cannot translate constraint nested in another expression as in:"+constraint);	
			}
			else if(leftExpression instanceof AbsoluteValue) {
				leftAtom = toMinion((AbsoluteValue) leftExpression);
			}
			else throw new MinionException("Cannot translate constraint nested in another expression as in:"+constraint);	
				
		}
		else leftArgument = (ArithmeticAtomExpression) leftExpression;
		
		if(!(rightExpression instanceof ArithmeticAtomExpression)) {
			if(rightExpression instanceof RelationalAtomExpression) 
				rightArgument = ((RelationalAtomExpression)rightExpression).toArithmeticExpression();
			else if(rightExpression instanceof UnaryMinus) {
				UnaryMinus minusExpression = (UnaryMinus) rightExpression;
				if(minusExpression.getArgument() instanceof ArithmeticAtomExpression) {
					return new MinusEq(toMinion(leftArgument), toMinion((ArithmeticAtomExpression) minusExpression.getArgument()));
				}
				else throw new MinionException("Internal error. Cannot translate constraint nested in another expression as in:"+constraint);	
			}
			else if(rightExpression instanceof AbsoluteValue) {
				rightAtom = toMinion((AbsoluteValue) rightExpression);
			}
			else throw new MinionException("Internal error. Cannot translate constraint nested in another expression as in:"+constraint);	
				
		}
		else rightArgument = (ArithmeticAtomExpression) rightExpression;
		
		MinionConstraint minionConstraint = null;
		
		if(leftAtom == null) leftAtom = toMinion(leftArgument);
		if(rightAtom == null) rightAtom = toMinion(rightArgument);
		
		
		if(operator == Expression.EQ ||
				operator == Expression.IFF) {
			
			minionConstraint =  new EqConstraint(leftAtom, 
					                		     rightAtom);
		}
		else if(operator == Expression.NEQ) {
			minionConstraint =  new DiseqConstraint(leftAtom, 
	                								rightAtom);
		}
		else throw new MinionException("Unknown commutative binary relation:"+constraint);	
		
		if(constraint.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(minionConstraint);
		}
		else return minionConstraint;
		
		
	}
	
	
	/**
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionAtom reifyMinionConstraint(MinionConstraint constraint)
		throws MinionException {
	
		//System.out.println("REIFIING the constraint:"+constraint);
	
		MinionAtom auxVariable = null;
		
		if(hasCommonSubExpression(constraint)) {
			return getCommonSubExpression(constraint);
		}
		else auxVariable = createMinionAuxiliaryVariable();
		
		MinionConstraint reifiedConstraint = new Reify(constraint, auxVariable);
		this.minionModel.addConstraint(reifiedConstraint);
		
		return auxVariable;
	}
		
	
	private MinionAtom createMinionAuxiliaryVariable() {
		
		SingleVariable auxVar = new SingleVariable(MINION_AUXVAR_NAME+this.noMinionAuxVars++,
                new translator.expression.BoolDomain());
		MinionSingleVariable auxVariable = new MinionSingleVariable(MINION_AUXVAR_NAME+(this.noMinionAuxVars-1));
		this.minionModel.addAuxiliaryVariable(auxVar);
		
		return auxVariable;
	}
	
	private MinionAtom createMinionAuxiliaryVariable(int lb, int ub) {
		
		SingleVariable auxVar = new SingleVariable(MINION_AUXVAR_NAME+this.noMinionAuxVars++,
                									new translator.expression.BoundedIntRange(lb,ub));
		MinionSingleVariable auxVariable = new MinionSingleVariable(MINION_AUXVAR_NAME+(this.noMinionAuxVars-1));
		this.minionModel.addAuxiliaryVariable(auxVar);
		
		return auxVariable;
	}
	
	
	/**
	 * Create an auxliary variable represented as an Expression (not the minion representation)
	 * Is added to the list of aux variables in the minion model.
	 * 
	 * @return
	 *//*
	private Variable createAuxVariable() {
		
		SingleVariable variable = new SingleVariable(this.MINION_AUXVAR_NAME+this.noMinionAuxVars++, new BoolDomain());
		this.minionModel.addAuxiliaryVariable(variable);
		return variable;
		
	}*/
	
	
	private Variable createAuxVariable(int lb, int ub) {
		
		SingleVariable variable = new SingleVariable(this.MINION_AUXVAR_NAME+this.noMinionAuxVars++, new BoundedIntRange(lb, ub));
		this.minionModel.addAuxiliaryVariable(variable);
		return variable;
		
	}
	
	
	/**
	 * Return the String representation of the Atomic arithmetic expression.
	 * 
	 * NOTE: there are ONLY CONSTANT indices allowed now. Every occurrence
	 * of another expression should have been flattened to an element constraint
	 * by now.
	 * 
	 * @param atom
	 * @return
	 * @throws MinionException
	 */
	private MinionAtom toMinion(ArithmeticAtomExpression atom) 
		throws MinionException {
		
		if(atom.getType() == Expression.INT)
			return new MinionConstant(atom.getConstant());
		
		else {
			Variable variable = atom.getVariable();
			//System.out.println("Mapping Atom "+atom+" to minion");
			
			if(variable instanceof ArrayVariable) {
				ArrayVariable arrayElement = (ArrayVariable) variable;
				
				if(this.offsetsFromZero.containsKey(arrayElement.getArrayNameOnly())) {
					int[] offsets = this.offsetsFromZero.get(((ArrayVariable) variable).getArrayNameOnly());
					
					int[] indices = arrayElement.getIntegerIndices();
					if(indices == null) 
						throw new MinionException("Cannot translate array element with non-constant element index:"+atom);
					
					//System.out.println("Computing offsets of indices of "+arrayElement+" and indices-length is "+indices.length);
					
					for(int i=0; i<indices.length;i++) {
						//System.out.println("Offset of array "+variable+" is "+offsets[i]+" so we compute: "+indices[i]+"-"+offsets[i]+" = "
							//	+(indices[i]-offsets[i]));
						indices[i] = indices[i]-offsets[i];
					}
					
				//	System.out.println("Finished computing offsets and got:"+new MinionArrayElement(arrayElement.getArrayNameOnly(),indices)); 
					
					return new MinionArrayElement(arrayElement.getArrayNameOnly(),indices);
				}
				else throw new MinionException("Cannot find offsets for array element:"+atom);
			}
			else return new MinionSingleVariable(variable.getVariableName());
		}

	}
	
	
	protected MinionConstraint toMinion(RelationalAtomExpression atom) 
	throws MinionException {
	
	if(atom.getType() == Expression.BOOL)
		return (((RelationalAtomExpression) atom).getBool()) ? 
				new MinionConstant(1) :
					new MinionConstant(0);
	
	else {
		Variable variable = atom.getVariable();
		
		if(variable instanceof ArrayVariable) {
			ArrayVariable arrayElement = (ArrayVariable) variable;
			
			if(this.offsetsFromZero.containsKey(arrayElement.getArrayNameOnly())) {
				int[] offsets = this.offsetsFromZero.get(((ArrayVariable) variable).getArrayNameOnly());
				
				int[] indices = arrayElement.getIntegerIndices();
				if(indices ==null) 
					throw new MinionException("Cannot translate array element with non-constant element index:"+atom);
				
				for(int i=0; i<indices.length;i++) {
					indices[i] = indices[i]-offsets[i];
				}
				
				//System.out.println("Converted old relational expression "+atom+" to minion var:"+new MinionArrayElement(arrayElement.getArrayNameOnly(),indices));
				
				return new MinionArrayElement(arrayElement.getArrayNameOnly(),indices);
			}
			else throw new MinionException("Cannot find offsets for array element:"+atom);
		}
		else return new MinionSingleVariable(variable.getVariableName());
	}

	
	
}
	
	protected void addToSubExpressions(Expression constraint, ArithmeticAtomExpression representative) {
		if(!this.useCommonSubexpressions)
			return;
		//System.out.println("PUTTING "+representative+" to represent "+constraint+" into common subexpressions");
		this.essenceSubExpressions.put(constraint.toString(), representative);
	}
	
	
	protected void addToSubExpressions(MinionConstraint constraint, MinionAtom representative) {
		if(!this.useCommonSubexpressions)
			return;
		
		this.minionSubExpressions.put(constraint.toString(), representative);
	}
	
	
	protected ArithmeticAtomExpression getCommonSubExpression(Expression constraint) {
		this.usedCommonSubExpressions++;
		//System.out.println("Used common subexpression:"+constraint);
		return this.essenceSubExpressions.get(constraint.toString());
		
	}
	
	protected MinionAtom getCommonSubExpression(MinionConstraint constraint) {
		this.usedCommonSubExpressions++;
		return this.minionSubExpressions.get(constraint.toString());
		
	}
	
	
	
	protected boolean hasCommonSubExpression(MinionConstraint constraint) {
		if(!this.useCommonSubexpressions)
			return false;
		
		return this.minionSubExpressions.containsKey(constraint.toString());
			
	}
	
	protected boolean hasCommonSubExpression(Expression constraint) {
		if(!this.useCommonSubexpressions)
			return false;
		
		return this.essenceSubExpressions.containsKey(constraint.toString());
		
	}
	
}
