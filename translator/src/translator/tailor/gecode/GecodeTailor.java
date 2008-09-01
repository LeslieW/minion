package translator.tailor.gecode;

import translator.normaliser.NormalisedModel;
import translator.TranslationSettings;
import translator.expression.*;

import translator.solver.Gecode;

import java.util.ArrayList;

/**
 * This class mapps the flattened normalised model into a 
 * Gecode model
 * 
 * @author andrea
 *
 */

public class GecodeTailor {

	
	private TranslationSettings settings;
	private NormalisedModel essencePmodel;
	
	// conversion stuff
	private ArrayList<GecodeConstraint> constraintBuffer;
	/** contains constraints that have to be added to preserve 
	 *  the bounds of single variables that are put into a buffer/container */
	private ArrayList<GecodeConstraint> additionalBoundsConstraintsBuffer;
	
	
	/** contains all the buffer arrays that have been introduced 
	 * to handle either multidimensional arrays or single variables */
	private ArrayList<GecodeArrayVariable> bufferArrays;
	private ArrayList<GecodeArrayVariable> variableList;
	private ArrayList<GecodeArrayVariable> multiDimensionalArrays;
	
	/** contains all single variables (that are then put into an Var array in the model)*/
	
	// stuff for buffering single int/boolean variables
	private ArrayList<GecodeIntVar> singleIntVariableList;
	private ArrayList<GecodeIntVar> auxIntVariableList;
	private ArrayList<GecodeBoolVar> singleBoolVariableList;
	private ArrayList<GecodeBoolVar> auxBoolVariableList;
	
	/** works as container for all integer auxiliary variables*/
	private GecodeIntVarArgs auxIntVariableBuffer;
	/** works as container for all boolean auxiliary variables*/
	private GecodeBoolVarArgs auxBoolVariableBuffer;
	
	/** contains all single integer variables */
	private GecodeIntVarArray integerVariableBuffer;
	/** contains all single boolean variables */
	private GecodeBoolVarArray booleanVariableBuffer;
	int numberOfBufferArrays = 0;
	
	
	
	
	public GecodeTailor() {
		constraintBuffer = new ArrayList<GecodeConstraint>();
		this.additionalBoundsConstraintsBuffer = new ArrayList<GecodeConstraint>();
		bufferArrays = new ArrayList<GecodeArrayVariable>();
		variableList = new ArrayList<GecodeArrayVariable>();
		multiDimensionalArrays = new ArrayList<GecodeArrayVariable>();
		singleIntVariableList = new ArrayList<GecodeIntVar>(); 
		singleBoolVariableList = new ArrayList<GecodeBoolVar>(); 
		auxIntVariableList = new ArrayList<GecodeIntVar>(); 
		auxBoolVariableList = new ArrayList<GecodeBoolVar>(); 
		settings = new TranslationSettings();
		settings.setTargetSolver(new Gecode());
		
		// initialise the buffers for the separate variables
		this.integerVariableBuffer = new GecodeIntVarArray(  ((Gecode) settings.getTargetSolver()).SINGLE_INT_VAR_ARRAY_NAME,
															0,0,0);
		this.booleanVariableBuffer = new GecodeBoolVarArray(  ((Gecode) settings.getTargetSolver()).SINGLE_BOOL_VAR_ARRAY_NAME, 0);
															
		this.auxIntVariableBuffer = new GecodeIntVarArgs(  ((Gecode) settings.getTargetSolver()).AUX_INT_VAR_ARRAY_NAME,
				0,0,0);
		this.auxBoolVariableBuffer = new GecodeBoolVarArgs(  ((Gecode) settings.getTargetSolver()).AUX_BOOL_VAR_ARRAY_NAME, 0);
															
	}
	
	
	//========== METHODS ==================
	
	/**
	 * Basic method to tailor a normalised, flattened E' model
	 * to Gecode, following the settings.
	 * @param NormalisedModel model
	 * @param TranslationSettings settings
	 * @return the corresponding GecodeModel
	 */
	public GecodeModel tailorToGecode(NormalisedModel model,
									  TranslationSettings settings) 
		throws GecodeException,Exception {
		
		this.settings = settings;
		this.essencePmodel = model;
		
		// decision variables
		tailorDecisionVariables();
		
		// constraints
		ArrayList<Expression> eConstraints = model.getConstraints();
		for(int i=0; i<eConstraints.size(); i++)
			constraintBuffer.add(tailorToGecode(eConstraints.get(i)));
		
		// collect adapted/buffered arrays
		if(this.booleanVariableBuffer.getLength() > 0)
			this.bufferArrays.add(this.booleanVariableBuffer);
		if(this.integerVariableBuffer.getLength() > 0)
			this.bufferArrays.add(this.integerVariableBuffer);
		
		
		
		// collect auxiliary variables
		tailorAuxiliaryVariables();
		ArrayList<ArgsArrayVariable> auxBufferArrays = new ArrayList<ArgsArrayVariable>();
		if(this.auxBoolVariableBuffer.getLength() > 0)
			auxBufferArrays.add(this.auxBoolVariableBuffer);
		if(this.auxIntVariableBuffer.getLength() > 0)
			auxBufferArrays.add(this.auxIntVariableBuffer);
		
		// merge constraints
		for(int i=0; i<this.additionalBoundsConstraintsBuffer.size(); i++)
			this.constraintBuffer.add(this.additionalBoundsConstraintsBuffer.get(i));
		
		GecodeModel gecodeModel = new GecodeModel(settings,
							   variableList,
							   bufferArrays,
							   constraintBuffer, 
							   this.singleIntVariableList,
							   this.singleBoolVariableList, 
							   auxBufferArrays, 
							   this.auxIntVariableList, 
							   this.auxBoolVariableList, 
							   multiDimensionalArrays);
		//System.out.println("==================================");
		//System.out.println("CC print:\n"+gecodeModel.toString()+"\n\n");
		//System.out.println("==================================");
		//System.out.println("Simple print:\n"+gecodeModel.toSimpleString()+"\n\n");
		//System.out.println("==================================");
		return gecodeModel;
	}
	
	/** 
	 * Tailor the flattened Essence' expression to Gecode
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	protected GecodeConstraint tailorToGecode(Expression e) 
		throws GecodeException, Exception {
			
		// linear expressions can be mapped directly
		//if(e.isLinearExpression()) {
		//	return tailorLinearExpression(e);
		//}
		
		if(e instanceof AbsoluteValue) {
			throw new GecodeException("Flattening error. Gecode does not support Absolute Values, such as:"+e);
		}
		else if(e instanceof AbsoluteConstraint) {
			return tailorAbsoluteConstraint((AbsoluteConstraint) e);
		}
		else if(e instanceof SumConstraint) {
			return tailorSumConstraint((SumConstraint) e);
		}
		else if(e instanceof ProductConstraint) {
			return tailorProductConstraint( (ProductConstraint) e);
		}
		else if(e instanceof AllDifferent) {
			return tailorAllDifferent((AllDifferent) e);
		}
		else if(e instanceof NonCommutativeRelationalBinaryExpression) {
			return tailorRelation((RelationalExpression) e);
		}
		else if(e instanceof CommutativeBinaryRelationalExpression) {
			return tailorRelation((RelationalExpression) e);
		}
		else if( e instanceof Conjunction) {
			return new GecodePostConstraint(e.toSolverExpression(settings.getTargetSolver()),
					                        true);
		}
		else if( e instanceof Disjunction) {
			return new GecodePostConstraint(e.toSolverExpression(settings.getTargetSolver()), 
					                        true);	
		}
		else if(e instanceof LexConstraint) {
			return tailorLexConstraint((LexConstraint) e);
		}
		
		
		else throw new GecodeException("Cannot tailor constraint to Gecode (yet):"+e);
	}
		
	
	/**
	 * Tailors all decision variables from the old model to Gecode representation
	 * 
	 * @throws GecodeException
	 */
	protected void tailorDecisionVariables() 
		throws GecodeException {
		
		ArrayList<String> decisionVarNames = this.essencePmodel.getDecisionVariablesNames();
		boolean firstIntVar = true;
		
		for(int i=0; i < decisionVarNames.size(); i++) {
			GecodeVariable variable = tailorToGecode(decisionVarNames.get(i));
			
			// if this variable is a single variable, add it to the collection of bool/int-vars
			if(variable instanceof GecodeBoolVar) {
				this.booleanVariableBuffer.increaseLength();
				this.singleBoolVariableList.add((GecodeBoolVar) variable);
			}
			
			else if(variable instanceof GecodeIntVar) {
				    // 1. add another variable to the buffer
					this.integerVariableBuffer.increaseLength();
					
					// 2. take care of lower bound of buffer
					if(firstIntVar) 
						this.integerVariableBuffer.setLowerBound(variable.getBounds()[0]);
					else { 
						this.integerVariableBuffer.addLowerBound(variable.getBounds()[0]);
						if(variable.getBounds()[0] > this.integerVariableBuffer.getLowerBound()) {
							this.additionalBoundsConstraintsBuffer.add(new SimpleIntRelation((GecodeIntVar) variable ,
																							 GecodeConstraint.IRT_GQ,
																							 new GecodeConstant(variable.getBounds()[0])));	
						}
					}
					
					// take care of upper bound of buffer
					if(firstIntVar) 
						this.integerVariableBuffer.setUpperBound(variable.getBounds()[1]);
					else {
						this.integerVariableBuffer.addUpperBound(variable.getBounds()[1]);
						if(variable.getBounds()[1] < this.integerVariableBuffer.getUpperBound()) {
							this.additionalBoundsConstraintsBuffer.add(new SimpleIntRelation((GecodeIntVar) variable ,
																							 GecodeConstraint.IRT_LQ,
																							 new GecodeConstant(variable.getBounds()[1])));	
						}
					}
					
					this.singleIntVariableList.add((GecodeIntVar) variable);
					firstIntVar = false;
			}
			//else if the variable is multi-dimensional ?
			
			// else add the variable array to the list of variables
			else {
				if(variable instanceof GecodeArrayVariable) {
					GecodeArrayVariable var = (GecodeArrayVariable) variable;
					if(!var.isMultiDimensional())
						this.variableList.add(var);
					else this.multiDimensionalArrays.add(var);
				}
				else throw new GecodeException("Internal error. Cannot map variable that is neither and int/bool atom nor an array variable:"+variable);
			}
		}
		
	}
	
	/**
	 * Collect all auxiliary variables (booleans and ints separately) and collect 
	 * them in an (int/bool) array-list and create a buffer for them.
	 * 
	 * @throws GecodeException
	 */
	protected void tailorAuxiliaryVariables() 
		throws GecodeException {
		
		ArrayList<Variable> auxVariables = this.essencePmodel.getAuxVariables();
		boolean firstIntVar = true;
		
		for(int i=0; i < auxVariables.size(); i++) {
			Variable oldVar = auxVariables.get(i);
			
			if(!(oldVar instanceof SingleVariable))
				throw new GecodeException("Internal error. Auxiliary variable '"+oldVar+
						"' is not a simple/single variable but of type:"+oldVar.getClass().getSimpleName());
			
			SingleVariable oldVariable = (SingleVariable) oldVar;
				
			// if this variable is a single variable, add it to the collection of bool/int-vars
			if(oldVariable.isBooleanVariable()) {
				this.auxBoolVariableBuffer.increaseLength();
				this.auxBoolVariableList.add(new GecodeBoolVar(oldVariable.getVariableName()));
			}
			
			else {
				    // 1. add another variable to the buffer
					this.auxIntVariableBuffer.increaseLength();
					
					// 2. take care of lower bound of buffer
					if(firstIntVar) 
						this.auxIntVariableBuffer.setLowerBound(oldVariable.getDomain()[0]);
					else {
						this.auxIntVariableBuffer.addLowerBound(oldVariable.getDomain()[0]);
						if(oldVariable.getDomain()[0] > this.integerVariableBuffer.getLowerBound()) {
							this.additionalBoundsConstraintsBuffer.add(new SimpleIntRelation(new GecodeIntVar(oldVariable.getVariableName(),
																												oldVariable.getDomain()[0],
																												oldVariable.getDomain()[1]) ,
																							 GecodeConstraint.IRT_GQ,
																							 new GecodeConstant(oldVariable.getDomain()[0])));	
						}
					}
					
					// take care of upper bound of buffer
					if(firstIntVar) 
						this.auxIntVariableBuffer.setUpperBound(oldVariable.getDomain()[1]);
					else { 
						this.auxIntVariableBuffer.addUpperBound(oldVariable.getDomain()[1]);
						if(oldVariable.getDomain()[1] < this.integerVariableBuffer.getUpperBound()) {
							this.additionalBoundsConstraintsBuffer.add(new SimpleIntRelation(new GecodeIntVar(oldVariable.getVariableName(),
																												oldVariable.getDomain()[0],
																												oldVariable.getDomain()[1]) ,
																							 GecodeConstraint.IRT_LQ,
																							 new GecodeConstant(oldVariable.getDomain()[1])));
						}
					}
					
					this.auxIntVariableList.add(new GecodeIntVar (oldVariable.getVariableName(),
																	 oldVariable.getDomain()[0],
																	 oldVariable.getDomain()[1]));
					firstIntVar = false;
			}
			
		}
		
	}
	
	/**
	 * Tailors variable with name variableName to a Gecode Atom.
	 * This method can only be used to tailor a definition of a 
	 * decision variable - never use it to tailor a variable/atom
	 * in a constraint expression!
	 * 
	 * @param variableName
	 * @param domain
	 * @return
	 * @throws GecodeException
	 */
	private GecodeVariable tailorToGecode(String variableName) 
		throws GecodeException {
		
		Domain domain = this.essencePmodel.getDomainOfVariable(variableName);
		if(domain == null)
			throw new GecodeException("Tailoring error. Cannot map variable '"+variableName+"' to a domain");
		
		if(domain.getType() == Domain.INT_BOUNDS) {
			BoundedIntRange intRange = (BoundedIntRange) domain;
			return new GecodeIntVar(variableName, 
									intRange.getRange()[0], 
									intRange.getRange()[1]);
		}
		
		else if(domain.getType() == Domain.SINGLE_INT) {
			SingleIntRange intRange = (SingleIntRange) domain;
			return new GecodeIntVar(variableName, 
									intRange.getRange()[0], 
									intRange.getRange()[0]);
		}
		
		else if(domain.getType() == Domain.BOOL) {
			return new GecodeBoolVar(variableName);
		}
		
		else if(domain.getType() == Domain.CONSTANT_ARRAY) {
			ConstantArrayDomain constArrayDomain = (ConstantArrayDomain) domain;
			ConstantDomain[] indexDomains = constArrayDomain.getIndexDomains();
			ConstantDomain baseDomain = constArrayDomain.getBaseDomain();
		
			int dimension = indexDomains.length;
				
			int[] indexLengths = new int[dimension];
			for(int i=0; i<indexDomains.length; i++) 
				indexLengths[i] = indexDomains[i].getFullDomain().length;
					
				
			if(baseDomain.getType() == Domain.BOOL) {
				if(dimension == 1)
					return new GecodeBoolVarArray(variableName,
											      	  indexLengths[0]);
				else {
					return new GecodeBoolVarArray(variableName,
					      	  						   indexLengths);
				}
			}
			else if(baseDomain.getType() == Domain.SINGLE_INT ||
					baseDomain.getType() == Domain.INT_SPARSE) {
				if(dimension == 1)
					return new GecodeIntVarArray(variableName, 
												 indexLengths[0],
												 baseDomain.getFullDomain());
				else  { 
					
					return new GecodeIntVarArray(variableName, 
							 						indexLengths,
							 						baseDomain.getFullDomain());
				}
			}
				
			else if(baseDomain.getType() == Domain.INT_BOUNDS) {
				if(dimension == 1)
					return new GecodeIntVarArray(variableName,
												 indexLengths[0],
												 baseDomain.getRange()[0],
												 baseDomain.getRange()[1]);
				else { 
					return new  GecodeIntVarArray(variableName,
							 						indexLengths,
							 						baseDomain.getRange()[0],
							 						baseDomain.getRange()[1]);
				}
			}
				
			else throw new GecodeException("Sorry, cannot tailor array '"+variableName+"' with base-domain "+baseDomain+" to Gecode (yet).");
			
			
		}
		
		throw new GecodeException("Cannot tailor variable '"+variableName+"' with domain-type "
				+domain.getClass().getSimpleName()+" to Gecode (yet).");
	}
	
	/**
	 * Maps operators (only relational)
	 * 
	 * @param essencePOperator
	 * @return
	 * @throws GecodeException
	 */
	protected char mapOperatorToGecode(int essencePOperator) 
		throws GecodeException {
		
		switch(essencePOperator) {
		
		// relational integer operators
		case Expression.EQ : return GecodeConstraint.IRT_EQ;
		case Expression.GEQ : return GecodeConstraint.IRT_GQ;
		case Expression.GREATER : return GecodeConstraint.IRT_GR;
		case Expression.LEQ : return GecodeConstraint.IRT_LQ;
		case Expression.LESS : return GecodeConstraint.IRT_LE;
		case Expression.NEQ : return GecodeConstraint.IRT_NQ;
		
		// boolean operators 
		case Expression.AND : return GecodeConstraint.BOT_AND;
		case Expression.OR : return GecodeConstraint.BOT_OR;
		case Expression.IF : return GecodeConstraint.BOT_IMP;
		case Expression.IFF : return GecodeConstraint.BOT_EQV;
		
		// lex operators
		case Expression.LEX_GEQ : return GecodeConstraint.IRT_GQ;
		case Expression.LEX_GREATER : return GecodeConstraint.IRT_GR;
		case Expression.LEX_LEQ : return GecodeConstraint.IRT_LQ;
		case Expression.LEX_LESS : return GecodeConstraint.IRT_LE;
		
		default:
			throw new GecodeException("Cannot map Essence' operator with number '"+essencePOperator+"' because it is unknown.");
		}
		
		
		
	}
	
	/*
	private void mergeVariableLists() {
		for(int i=this.bufferArrays.size()-1; i>= 0; i--) {
			this.variableList.add(this.bufferArrays.remove(i));
		}
	}*/
	
	
	private GecodeConstraint tailorLexConstraint(LexConstraint e) 
		throws GecodeException {
		
		Expression leftArg = e.getLeftArray();
		Expression rightArg = e.getRightArray();
		
		if(!(leftArg instanceof SingleArray)) throw new GecodeException("Infeasible argument for lex constraint "+e+".\nExpected array instead of "+leftArg+" of type "+leftArg.getClass().getSimpleName());
		if(!(rightArg instanceof SingleArray)) throw new GecodeException("Infeasible argument for lex constraint "+e+".\nExpected array instead of :"+rightArg+" of type "+rightArg.getClass().getSimpleName());
			
		// 3 types of SingleArrays : SimpleArray, IndexedArray, VariableArray
		GecodeArray leftArray = tailorArray((SingleArray) leftArg);
		GecodeArray rightArray = tailorArray((SingleArray) rightArg);
		
		if(leftArray instanceof GecodeBoolVarArgs && 
				rightArray instanceof GecodeBoolVarArgs) 
			return new SimpleBoolRelation((GecodeBoolVarArgs) leftArray,
										this.mapOperatorToGecode(e.getOperator()),
										(GecodeBoolVarArgs) rightArray);
		
		else if(leftArray instanceof GecodeIntVarArgs && 
				rightArray instanceof GecodeIntVarArgs) 
			return new SimpleIntRelation((GecodeIntVarArgs) leftArray,
										this.mapOperatorToGecode(e.getOperator()),
										(GecodeIntVarArgs) rightArray);
		
		
		
		throw new GecodeException("Cannot tailor lex constraint "+e+" yet, sorry.");
	}
	
	/**
	 * tailors different kinds of arrays (Simple, Indexed or VariableArray)
	 * to Gecode representation
	 * 
	 * @param array
	 * @return
	 * @throws GecodeException
	 */
	private GecodeArray tailorArray(SingleArray array) 
		throws GecodeException {
		
		
		if(array instanceof SimpleArray) 
			return tailorSimpleArray((SimpleArray) array);
	
		else if(array instanceof IndexedArray) {
			return tailorIndexedArray((IndexedArray) array);
		}
		
		throw new GecodeException("Cannot tailor array "+array+" yet, sorry.");
	}
	
	
	private GecodeArray tailorIndexedArray(IndexedArray indexedArray) 
		throws GecodeException {
		
		// TODO
		int f;
		throw new GecodeException("Cannot tailor indexed array "+indexedArray+" yet, sorry.");
	}
	
	
	/**
	 * tailors a simple array to the corresponding Gecode representation
	 * 
	 * @param simpleArray
	 * @return
	 * @throws GecodeException
	 */
	private GecodeArray tailorSimpleArray(SimpleArray simpleArray)
		throws GecodeException {
		
		String variableName = simpleArray.getArrayName();
		
		BasicDomain[] iDomains = simpleArray.getIndexDomains();
		ConstantDomain[] indexDomains = new ConstantDomain[iDomains.length];
		for(int i=0; i<indexDomains.length; i++) {
			if(iDomains[i] instanceof ConstantDomain)
				indexDomains[i] = (ConstantDomain) iDomains[i];
			else throw new GecodeException("Cannot tailor array "+simpleArray+" because index-domain "+indexDomains[i]+" is not constant.");
		}
		
		Domain bDomain = simpleArray.getBaseDomain();
		if(!(bDomain instanceof ConstantDomain))
			throw new GecodeException("Cannot tailor array "+simpleArray+" with non-constant domain yet, sorry.");
		ConstantDomain baseDomain = (ConstantDomain) bDomain;
		
		int dimension = indexDomains.length;
		
		int[] indexLengths = new int[dimension];
		for(int i=0; i<indexDomains.length; i++) 
			indexLengths[i] = indexDomains[i].getFullDomain().length;
				
			
		if(baseDomain.getType() == Domain.BOOL) {
			if(dimension == 1)
				return new GecodeBoolVarArray(variableName,
										      	  indexLengths[0]);
			else {
				return new GecodeBoolVarArray(variableName,
				      	  						   indexLengths);
			}
		}
		else if(baseDomain.getType() == Domain.SINGLE_INT ||
				baseDomain.getType() == Domain.INT_SPARSE) {
			if(dimension == 1)
				return new GecodeIntVarArray(variableName, 
											 indexLengths[0],
											 baseDomain.getFullDomain());
			else  { 
				
				return new GecodeIntVarArray(variableName, 
						 						indexLengths,
						 						baseDomain.getFullDomain());
			}
		}
			
		else if(baseDomain.getType() == Domain.INT_BOUNDS) {
			if(dimension == 1)
				return new GecodeIntVarArray(variableName,
											 indexLengths[0],
											 baseDomain.getRange()[0],
											 baseDomain.getRange()[1]);
			else { 
				return new  GecodeIntVarArray(variableName,
						 						indexLengths,
						 						baseDomain.getRange()[0],
						 						baseDomain.getRange()[1]);
			}
		}
			
		else throw new GecodeException("Sorry, cannot tailor array '"+variableName+"' with base-domain "+baseDomain+" to Gecode (yet).");
		
		
	}
	
	
	/**
	 * Translates a product constraint to Gecode's mult constraint
	 * 
	 */
	private GecodeMult tailorProductConstraint(ProductConstraint product) 
		throws GecodeException,Exception {
		
		Expression resultOld = product.getResult();
		Expression[] arguments = product.getArguments();
		
		if(arguments.length > 2)
			throw new GecodeException("Flattening error of expression:"+product+". Gecode only supports binary multiplication.");
		// else:  we don't need to care about 1 argument only -> evaluation should have taken care of that
		
		GecodeAtom leftArg, rightArg;
		
		if(arguments[0] instanceof ArithmeticAtomExpression) {
			leftArg = tailorArithmeticAtom((ArithmeticAtomExpression) arguments[0]);
		}
		else if(arguments[0] instanceof RelationalAtomExpression) 
			leftArg = tailorRelationalAtom((RelationalAtomExpression) arguments[0]);
			
		else throw new GecodeException("Cannot tailor '"+arguments[0]+"' in expression '"+product+
				"' to Gecode:\nit is not an atom but of type: "+arguments[0].getClass().getSimpleName());
		
		if(arguments[1] instanceof ArithmeticAtomExpression) {
			rightArg = tailorArithmeticAtom((ArithmeticAtomExpression) arguments[1]);
		}
		else if(arguments[1] instanceof RelationalAtomExpression) 
			rightArg = tailorRelationalAtom((RelationalAtomExpression) arguments[1]);
			
		else throw new GecodeException("Cannot tailor '"+arguments[1]+"' in expression '"+product+
				"' to Gecode:\nit is not an atom but of type: "+arguments[1].getClass().getSimpleName());
		
		
		// result expression
		GecodeAtom result;
		if(resultOld instanceof ArithmeticAtomExpression) {
			result = tailorArithmeticAtom((ArithmeticAtomExpression) resultOld);
		}
		else if(resultOld instanceof RelationalAtomExpression) 
			result = tailorRelationalAtom((RelationalAtomExpression) resultOld);
		else throw new GecodeException("Cannot tailor '"+resultOld+"' in expression '"+product+
				"' to Gecode:\nit is not an atom but of type: "+resultOld.getClass().getSimpleName());
		
		
		return new GecodeMult(leftArg,rightArg,result);
	}
	
	/**
	 * Returns Gecode's alldifferent (distinct) constraint
	 * 
	 * @param alldiff
	 * @return
	 * @throws GecodeException
	 */
	private GecodeDistinct tailorAllDifferent(AllDifferent alldiff) 
		throws GecodeException {
		
		Expression e = alldiff.getArgument();
		if(!(e instanceof SingleArray)) 
			throw new GecodeException("Expected array as argument of alldifferent '"+
					alldiff+"' instead of: "+e);
		
		GecodeArray array = tailorArray((SingleArray) e);
		if(array instanceof GecodeIntArray)
			return new GecodeDistinct((GecodeIntArray) array);
		
		else throw new GecodeException("Expected integer array instead of '"+
				array+"' with basic domain:"+((Array) e).getBaseDomain().getClass().getSimpleName());
	}
	
	/**
	 * Tailors an Essence' sum constraint 
	 *  sum(+/- X_i)  ~r   Y
	 *  to the corresponding Gecode Expression.
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	private GecodeConstraint tailorSumConstraint(SumConstraint e) 
		throws GecodeException, Exception {
		
		// we can simply post a linear constraint, since the 
		// expression has been linearised by flattening
		if(settings.getUseGecodeMinimodelPostConstraints()) {
			StringBuffer sumConstraint = new StringBuffer(e.toGecodeString());
			return new GecodePostConstraint(sumConstraint.toString());
		}
		
		
		
		// otherwise map to linear constraint
		Expression[] positiveArgs = e.getPositiveArguments();
		Expression[] negativeArgs = e.getNegativeArguments();
		Expression resultOld = e.getResult();
		
		// ---- check if its an un-weighted sum
		if(negativeArgs.length == 0) {
			boolean noMultiplication = true;
			boolean allAtomExpressions = true;
			for(int i=0; i<positiveArgs.length; i++) {
				noMultiplication &= !(positiveArgs[i] instanceof Multiplication);
				allAtomExpressions &= (positiveArgs[i] instanceof AtomExpression);
			}
			if(noMultiplication) {
				
				if(!allAtomExpressions) 
					throw new GecodeException("Flattening error. There is a non-atomic argument in the sum constraint: "+e);
				
				GecodeAtom[] args = new GecodeAtom[positiveArgs.length];
				for(int i=0; i<args.length; i++) {
					args[i] = (positiveArgs[i] instanceof ArithmeticAtomExpression) ? 
							tailorArithmeticAtom((ArithmeticAtomExpression) positiveArgs[i]) : 
								tailorRelationalAtom((RelationalAtomExpression) positiveArgs[i])	;
				}
				
				if(!(resultOld instanceof AtomExpression))
					throw new GecodeException("Flattening error. Expected atom as result in "+e+" instead of: "+resultOld);
				
				GecodeAtom result = (resultOld instanceof ArithmeticAtomExpression) ? 
						tailorArithmeticAtom((ArithmeticAtomExpression) resultOld) : 
							tailorRelationalAtom((RelationalAtomExpression) resultOld)	;
						
                return new GecodeLinear(args,
                						mapOperatorToGecode(e.getOperator()),
                						result);						
			}
			// end(if the sum is not weighted)
		} // end(if the sum is not weighted (no negative args))
		
		
		// ------ else it is a weighted sum
		
		int weights[] = new int[positiveArgs.length + negativeArgs.length];
		GecodeAtom[] variables = new GecodeAtom[weights.length];
		
		// first positive arguments
		for(int i=0; i<positiveArgs.length; i++) {
			if(positiveArgs[i] instanceof Multiplication) {
				ArrayList<Expression> multArgs = ((Multiplication) positiveArgs[i]).getArguments();
				if(multArgs.size() > 2) 
					throw new GecodeException("Flattening error. Expected binary multiplication instead of "+positiveArgs[i]+" in :"+e);
				if(multArgs.get(0).getType() != Expression.INT) 
					throw new GecodeException("Flattening error. Expected linear multiplication instead of "+positiveArgs[i]+" in :"+e);
				weights[i] = ((ArithmeticAtomExpression) multArgs.get(0)).getConstant();
				
				Expression variable = multArgs.get(1);
				if(!(variable instanceof AtomExpression))
						throw new GecodeException("Flattening error. There is a non-atomic argument "+variable+" in the sum constraint: "+e);
				
				variables[i] = (variable instanceof ArithmeticAtomExpression) ? 
						tailorArithmeticAtom((ArithmeticAtomExpression) variable) : 
							tailorRelationalAtom((RelationalAtomExpression) variable)	;
			}
			else if(positiveArgs[i] instanceof AtomExpression){
				weights[i] = 1;
				variables[i] = (positiveArgs[i] instanceof ArithmeticAtomExpression) ? 
						tailorArithmeticAtom((ArithmeticAtomExpression) positiveArgs[i]) : 
							tailorRelationalAtom((RelationalAtomExpression) positiveArgs[i])	;
			}
			else throw new GecodeException("Flattening error. Expected atom as result in "+e+" instead of: "+positiveArgs[i]);
		}
		// then negative arguments
		for(int i=0; i<negativeArgs.length; i++) {
			if(negativeArgs[i] instanceof Multiplication) {
				ArrayList<Expression> multArgs = ((Multiplication) negativeArgs[i]).getArguments();
				if(multArgs.size() > 2) 
					throw new GecodeException("Flattening error. Expected binary multiplication instead of "+negativeArgs[i]+" in :"+e);
				if(multArgs.get(0).getType() != Expression.INT) 
					throw new GecodeException("Flattening error. Expected linear multiplication instead of "+negativeArgs[i]+" in :"+e);
				weights[i+positiveArgs.length] = -((ArithmeticAtomExpression) multArgs.get(0)).getConstant();
				
				Expression variable = multArgs.get(1);
				if(!(variable instanceof AtomExpression))
						throw new GecodeException("Flattening error. There is a non-atomic argument "+variable+" in the sum constraint: "+e);
				
				variables[i+positiveArgs.length] = (variable instanceof ArithmeticAtomExpression) ? 
						tailorArithmeticAtom((ArithmeticAtomExpression) variable) : 
							tailorRelationalAtom((RelationalAtomExpression) variable)	;
			}
			else if(negativeArgs[i] instanceof AtomExpression){
				weights[i+positiveArgs.length] = -1;
				variables[i+positiveArgs.length] = (negativeArgs[i] instanceof ArithmeticAtomExpression) ? 
						tailorArithmeticAtom((ArithmeticAtomExpression) negativeArgs[i]) : 
							tailorRelationalAtom((RelationalAtomExpression) negativeArgs[i])	;
			}
			else throw new GecodeException("Flattening error. Expected atom as result in "+e+" instead of: "+negativeArgs[i]);
		}
		
		// map the result
		if(!(resultOld instanceof AtomExpression))
			throw new GecodeException("Flattening error. Expected atom as result in "+e+" instead of: "+resultOld);
		
		GecodeAtom result = (resultOld instanceof ArithmeticAtomExpression) ? 
				tailorArithmeticAtom((ArithmeticAtomExpression) resultOld) : 
					tailorRelationalAtom((RelationalAtomExpression) resultOld)	;
		
		return new GecodeLinear(variables, 
								weights, 
								mapOperatorToGecode(e.getOperator()), 
								result);	
		
	}
	
	/**
	 * Tailor simple expressions (non-commutative and commutative expr).
	 * to Gecode. They can be on boolean or arithmetic (int) variables.
	 * 
	 * @param expression
	 * @return
	 * @throws GecodeException
	 */
	private RelationalConstraint tailorRelation(RelationalExpression expression) 
		throws GecodeException, Exception {
		
		if(expression instanceof NonCommutativeRelationalBinaryExpression) {
			NonCommutativeRelationalBinaryExpression nonCommExpr = (NonCommutativeRelationalBinaryExpression) expression;
			Expression leftExpression = nonCommExpr.getLeftArgument();
			Expression rightExpression = nonCommExpr.getRightArgument();
			
			// this is an arithmetic relation
			if(leftExpression instanceof ArithmeticAtomExpression) {
				GecodeAtom leftArg = tailorArithmeticAtom((ArithmeticAtomExpression) leftExpression);
				if(rightExpression instanceof ArithmeticAtomExpression) {
					GecodeAtom rightArg = tailorArithmeticAtom((ArithmeticAtomExpression) rightExpression);
					return tailorSimpleIntegerRelation(leftArg, 
													   this.mapOperatorToGecode(nonCommExpr.getOperator()), 
													   rightArg);					
				}
				else throw new GecodeException((rightExpression instanceof RelationalAtomExpression) ? 
						"Type error. Cannot apply relational operator on arithmetic and boolean argument in: "+expression : 
					    "Flattening error. Cannot tailor "+expression+" to Gecode.\nExpected an atom instead of: "+rightExpression);
			}
			
			// this is a boolean relation
			else if(leftExpression instanceof RelationalAtomExpression) {
				GecodeAtom leftArg = tailorRelationalAtom((RelationalAtomExpression) leftExpression);
				if(rightExpression instanceof RelationalAtomExpression) {
					GecodeAtom rightArg = tailorRelationalAtom((RelationalAtomExpression) rightExpression);
					return tailorSimpleBooleanRelation(leftArg, 
													   this.mapOperatorToGecode(nonCommExpr.getOperator()), 
													   rightArg);					
				}
				else throw new GecodeException((rightExpression instanceof RelationalAtomExpression) ? 
						"Type error. Cannot apply relational operator on arithmetic and boolean argument in: "+expression : 
					    "Flattening error. Cannot tailor "+expression+" to Gecode.\nExpected an atom instead of: "+rightExpression);
			}
			else throw new GecodeException("Flattening error. Cannot tailor "+expression+
					" to Gecode.\nExpected an atom instead of :"+leftExpression);
		}
		
		if(expression instanceof CommutativeBinaryRelationalExpression) {
			CommutativeBinaryRelationalExpression commExpr = (CommutativeBinaryRelationalExpression) expression;
			Expression leftExpression = commExpr.getLeftArgument();
			Expression rightExpression = commExpr.getRightArgument();
			
			// this is an arithmetic relation
			if(leftExpression instanceof ArithmeticAtomExpression) {
				GecodeAtom leftArg = tailorArithmeticAtom((ArithmeticAtomExpression) leftExpression);
				if(rightExpression instanceof ArithmeticAtomExpression) {
					GecodeAtom rightArg = tailorArithmeticAtom((ArithmeticAtomExpression) rightExpression);
					return tailorSimpleIntegerRelation(leftArg, 
													   this.mapOperatorToGecode(commExpr.getOperator()), 
													   rightArg);					
				}
				else throw new GecodeException((rightExpression instanceof RelationalAtomExpression) ? 
						"Type error. Cannot apply relational operator on arithmetic and boolean argument in: "+expression : 
					    "Flattening error. Cannot tailor "+expression+" to Gecode.\nExpected an atom instead of: "+rightExpression);
			}
			
			// this is a boolean relation
			else if(leftExpression instanceof RelationalAtomExpression) {
				GecodeAtom leftArg = tailorRelationalAtom((RelationalAtomExpression) leftExpression);
				if(rightExpression instanceof RelationalAtomExpression) {
					GecodeAtom rightArg = tailorRelationalAtom((RelationalAtomExpression) rightExpression);
					return tailorSimpleBooleanRelation(leftArg, 
													   this.mapOperatorToGecode(commExpr.getOperator()), 
													   rightArg);					
				}
				else throw new GecodeException((rightExpression instanceof RelationalAtomExpression) ? 
						"Type error. Cannot apply relational operator on arithmetic and boolean argument in: "+expression : 
					    "Flattening error. Cannot tailor "+expression+" to Gecode.\nExpected an atom instead of: "+rightExpression);
			}
			
			// else it has not been flattened properly
			else throw new GecodeException("Flattening error. Cannot tailor "+expression+
					" to Gecode.\nExpected an atom instead of :"+leftExpression);
		}
		
		throw new GecodeException("Sorry, cannot tailor relation "+expression+" to Gecode (yet).");
	}

	
	/**
	 * Tailors 2 (hopefully arithmetic) arguments and a relational 
	 * operator to the corresponding Gecode 'rel' constraint/propagator
	 * 
	 * 
	 * @param leftArgument
	 * @param operator
	 * @param rightArgument
	 * @return
	 * @throws GecodeException
	 */
	private SimpleIntRelation tailorSimpleIntegerRelation(GecodeAtom leftArgument, 
														  char operator, 
														  GecodeAtom rightArgument)
		throws GecodeException {
		
		
		if(leftArgument instanceof GecodeIntAtomVariable) {
				if(rightArgument instanceof GecodeIntAtomVariable) {
					return new SimpleIntRelation((GecodeIntAtomVariable) leftArgument,
												  operator,
												  (GecodeIntAtomVariable) rightArgument);
				}
				else if(rightArgument instanceof GecodeConstant) {
					return new SimpleIntRelation((GecodeIntAtomVariable) leftArgument,
							  					  operator,
							  					  (GecodeConstant) rightArgument);
				}
				else throw new GecodeException("Sorry, cannot tailor "+leftArgument+" "+operator+" "+rightArgument
						+" to Gecode.\nExpected constant or variable atom instead of: "+rightArgument);
		}
		
		else if(leftArgument instanceof GecodeConstant) {
				if(rightArgument instanceof GecodeIntAtomVariable) {
					return new SimpleIntRelation((GecodeIntAtomVariable) rightArgument,
												  this.invertRelationalOperator(operator),
												   (GecodeConstant) leftArgument);
				}
				else throw new GecodeException("Sorry, cannot tailor "+leftArgument+" "+operator+" "+rightArgument
						+" to Gecode.\nExpected a variable atom instead of: "+rightArgument);
		}
		
		throw new GecodeException("Sorry, cannot tailor "+leftArgument+" "+operator+" "+rightArgument+" to Gecode.\n");
	}
	
	
	/**
	 * Tailors 2 (hopefully) boolean arguments and a relational operator 
	 * to the corresponding Gecode 'rel' constraint/propagator
	 * 
	 * 
	 * @param leftArgument
	 * @param operator
	 * @param rightArgument
	 * @return
	 * @throws GecodeException
	 */
	private SimpleBoolRelation tailorSimpleBooleanRelation(GecodeAtom leftArgument, 
														  char operator, 
														  GecodeAtom rightArgument)
		throws GecodeException {
		
		
		if(leftArgument instanceof GecodeBoolAtomVariable) {
				if(rightArgument instanceof GecodeBoolAtomVariable) {
					return new SimpleBoolRelation((GecodeBoolAtomVariable) leftArgument,
												  operator,
												  (GecodeBoolAtomVariable) rightArgument);
				}
				else if(rightArgument instanceof GecodeConstant) {
					return new SimpleBoolRelation((GecodeBoolAtomVariable) leftArgument,
							  					  operator,
							  					  (GecodeConstant) rightArgument);
				}
				else throw new GecodeException("Sorry, cannot tailor "+leftArgument+" "+operator+" "+rightArgument
						+" to Gecode.\nExpected constant or variable atom instead of: "+rightArgument);
		}
		
		else if(leftArgument instanceof GecodeConstant) {
				if(rightArgument instanceof GecodeBoolAtomVariable) {
					return new SimpleBoolRelation((GecodeBoolAtomVariable) rightArgument,
												  this.invertRelationalOperator(operator),
												   (GecodeConstant) leftArgument);
				}
				else throw new GecodeException("Sorry, cannot tailor "+leftArgument+" "+operator+" "+rightArgument
						+" to Gecode.\nExpected a variable atom instead of: "+rightArgument);
		}
		
		throw new GecodeException("Sorry, cannot tailor "+leftArgument+" "+operator+" "+rightArgument+" to Gecode.\n");
	}
	
	
	/**
	 * Tailor an absolute value expression to Gecode
	 * 
	 * @param e
	 * @return
	 */
	private GecodeAbs tailorAbsoluteConstraint(AbsoluteConstraint e) 
		throws GecodeException,Exception {
		
		Expression argumentOld = e.getArgument();
		Expression resultOld = e.getResult();
		
		GecodeAtom argument,result;
		
		if(argumentOld instanceof ArithmeticAtomExpression) {
			argument = tailorArithmeticAtom((ArithmeticAtomExpression) argumentOld);
		}
		else throw new GecodeException("Cannot tailor argument '"+argumentOld+"' of abs-expression '"+e+"' to Gecode because it is not an integer atom.");
		
		if(resultOld instanceof ArithmeticAtomExpression) {
			result = tailorArithmeticAtom((ArithmeticAtomExpression) resultOld);
		}
		else throw new GecodeException("Cannot tailor argument '"+resultOld+"' of abs-expression '"+e+"' to Gecode because it is not an integer atom.");
		
		
		if(!(argument instanceof GecodeIntVar && 
				result instanceof GecodeIntVar)) {
			throw new GecodeException("Error. Cannot tailor absolute constraint '"+e+"' to Gecode: argument and result have to be integer variables.");
		}
		
		return new GecodeAbs((GecodeIntVar) argument,
				              (GecodeIntVar) result);
	}
	
	/**
	 * Returns Gecode int arrays.
	 * 
	 * @param array
	 * @return
	 * @throws GecodeException
	 */
	private GecodeArrayVariable tailorToGecodeIntArray(Array array) 
		throws GecodeException {
		
	
		if(array instanceof SimpleArray) {
			SimpleArray simpleArray = (SimpleArray) array;
			BasicDomain[] indexDomains = simpleArray.getIndexDomains();
			
			if(indexDomains.length > 1)
				throw new GecodeException("Cannot tailor multi-dimensional arrays like '"+array+"' yet, sorry.");
			
			if(!(indexDomains[0] instanceof ConstantDomain))
				throw new GecodeException("Expected constant index domain instead of:"+indexDomains[0]);
			
			ConstantDomain indexDomain = (ConstantDomain) indexDomains[0];
			int length = indexDomain.getRange()[1] - indexDomain.getRange()[0] + 1; 
			
			Domain baseDomain = simpleArray.getBaseDomain();
			
			if(baseDomain instanceof BoolDomain) {
				return new GecodeBoolVarArray(simpleArray.getArrayName(),
 											  length);
			}
			else {
				if(!(baseDomain instanceof ConstantDomain)) 
					throw new GecodeException("Expected constant index domain instead of:"+baseDomain);
				
				ConstantDomain constBaseDomain = (ConstantDomain) baseDomain;
				
				
				return new GecodeIntVarArray(simpleArray.getArrayName(),
				     						length,
				     						constBaseDomain.getRange()[0],
				     						constBaseDomain.getRange()[1]);
			}
		}
		
		throw new GecodeException("Cannot tailor array '"+array+"' of type "+array.getClass().getSimpleName()+
				" yet, sorry.");
	}
	
	/**
	 * Tailors arithmetic atom expressions to Gecode atom
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	private GecodeAtom tailorArithmeticAtom(ArithmeticAtomExpression e) 
		throws GecodeException, Exception {

		if(e.getType() == Expression.INT) {
			return new GecodeConstant(e.getConstant());
		}
		
		else if(e.getType() == Expression.INT_VAR) {
			Variable var = e.getVariable();
			return new GecodeIntVar(var.getVariableName(),
									var.getDomain()[0],
									var.getDomain()[1]);
		}
		else if(e.getType() == Expression.INT_ARRAY_VAR) {
			ArrayVariable var = (ArrayVariable) e.getVariable();
			if(var.getExpressionIndices() != null) 
				throw new GecodeException("Sorry. Cannot tailor array variables with non-integer indices such as '"+e+"' yet.");
			
			return new GecodeIntArrayElem(var.getArrayNameOnly(),
											  var.getIntegerIndices(),
											  var.getDomain()[0],
											  var.getDomain()[1]);
			
		}
		
		else throw new GecodeException("Unknown arithmetic atomic type: "+e);
	}
	
	
	/**
	 * Tailors boolean atom expressions to Gecode atoms
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	private GecodeAtom tailorRelationalAtom(RelationalAtomExpression e) 
		throws GecodeException, Exception {

		if(e.getType() == Expression.BOOL) {
			return new GecodeConstant(e.getBool() ? 1 : 0);
		}
		
		else if(e.getType() == Expression.BOOL_VAR) {
			Variable var = e.getVariable();
			return new GecodeBoolVar(var.getVariableName());
		}
		else if(e.getType() == Expression.BOOL_ARRAY_VAR) {
			ArrayVariable var = (ArrayVariable) e.getVariable();
			if(var.getExpressionIndices() != null) 
				throw new GecodeException("Sorry. Cannot tailor array variables with non-integer indices such as '"+e+"' yet.");
			
			/*if(!var.isIndexAdaptedToSolver()) {
				int[] offsets = new int[var.getIntegerIndices().length];
				Domain domain = this.essencePmodel.getDomainOfVariable(var.getArrayNameOnly());
				if(!(domain instanceof ConstantArrayDomain)) 
					throw new GecodeException("Expected constant domain for variable '"+var+"' instead of: "+domain);
				ConstantDomain[] indexDomains = ((ConstantArrayDomain) domain).getIndexDomains();
				if(indexDomains.length != offsets.length) 
					throw new GecodeException("Unfeasible amount of array indices: "+var.getArrayNameOnly()+" is "+indexDomains.length+
							"-dimensional but it is indexed with "+offsets.length+" indices.");
				for(int i=0; i<indexDomains.length; i++) {
					// offset = lb_solver - lb_index
					offsets[i] = this.settings.getTargetSolver().getArrayIndexingStartValue() - indexDomains[i].getRange()[0];
				}
				var = var.adaptOffsetToIndices(offsets);
			}*/
			
			return new GecodeBoolArrayElem(var.getArrayNameOnly(),
											  var.getIntegerIndices());
		}

		
		else throw new GecodeException("Unknown arithmetic atomic type: "+e);
	}
	

	/**
	 * Tailors linear expressions. Does not involve much, but adapting 
	 * atoms, such as array dereferences where the indices need to 
	 * be adapted to start from zero. 
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	private GecodePostConstraint tailorLinearExpression(Expression e) 
		throws GecodeException,Exception {
		
		return new GecodePostConstraint(adaptLinearExpression(e));
	}
	
	
	/**
	 * Does not involve much, but adapting atoms, such as array 
	 * dereferences where the indices need to be adapted to start from zero.
	 * or multidimensional arrays 
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	private String adaptLinearExpression(Expression e) 
		throws GecodeException, Exception {
		
		if(e instanceof AbsoluteConstraint)
			return "|"+
				adaptLinearExpression(((AbsoluteConstraint) e).getArgument())+"| == "
				+adaptLinearExpression(((AbsoluteConstraint) e).getResult());
		
		else if(e instanceof AbsoluteValue) 
			return "|"+
			adaptLinearExpression(((AbsoluteValue) e).getArgument())+"|";
		
		
		else if(e instanceof ArithmeticAtomExpression) {
		
			if(e.getType() == Expression.INT_ARRAY_VAR) {
				ArrayVariable var = (ArrayVariable) ((ArithmeticAtomExpression) e).getVariable();
				if(var.getExpressionIndices() != null) 
					throw new GecodeException("Sorry. Cannot tailor array variables with non-integer indices such as '"+e+"' yet.");
			
				int dimension = var.getIntegerIndices().length;
				if(dimension == 0) 
					throw new GecodeException("Cannot tailor array variable "+var+" with non-constant index yet, sorry.");
				
				if(!var.isIndexAdaptedToSolver()) {
					int[] offsets = new int[dimension];
					Domain domain = this.essencePmodel.getDomainOfVariable(var.getArrayNameOnly());
					if(!(domain instanceof ConstantArrayDomain)) 
						throw new GecodeException("Expected constant domain for variable '"+var+"' instead of: "+domain);
					ConstantDomain[] indexDomains = ((ConstantArrayDomain) domain).getIndexDomains();
					if(indexDomains.length != offsets.length) 
						throw new GecodeException("Unfeasible amount of array indices: "+var.getArrayNameOnly()+" is "+indexDomains.length+
								"-dimensional but it is indexed with "+offsets.length+" indices.");
					for(int i=0; i<indexDomains.length; i++) {
						// offset = lb_solver - lb_index
						offsets[i] = this.settings.getTargetSolver().getArrayIndexingStartValue() - indexDomains[i].getRange()[0];
					}
					var = var.adaptOffsetToIndices(offsets);
				}
				
				if(dimension == 1) 
					return var.toString();
				
				else {
					StringBuffer s =  new StringBuffer(var.getArrayNameOnly()+"(");
					int[] intIndices = var.getIntegerIndices();
					
					for(int i=0; i<intIndices.length; i++) {
						if(i > 0) s.append(",");
						s.append(intIndices[i]);
					}
					return s.toString()+")";
					
				}
			}
			else return e.toString(); 
		}
		
		else if(e instanceof RelationalAtomExpression) {
			
			if(e.getType() == Expression.BOOL_ARRAY_VAR) {
				ArrayVariable var = (ArrayVariable) ((RelationalAtomExpression) e).getVariable();
				if(var.getExpressionIndices() != null) 
					throw new GecodeException("Sorry. Cannot tailor array variables with non-integer indices such as '"+e+"' yet.");
				
				if(!var.isIndexAdaptedToSolver()) {
					int[] offsets = new int[var.getIntegerIndices().length];
					Domain domain = this.essencePmodel.getDomainOfVariable(var.getArrayNameOnly());
					if(!(domain instanceof ConstantArrayDomain)) 
						throw new GecodeException("Expected constant domain for variable '"+var+"' instead of: "+domain);
					ConstantDomain[] indexDomains = ((ConstantArrayDomain) domain).getIndexDomains();
					if(indexDomains.length != offsets.length) 
						throw new GecodeException("Unfeasible amount of array indices: "+var.getArrayNameOnly()+" is "+indexDomains.length+
								"-dimensional but it is indexed with "+offsets.length+" indices.");
					for(int i=0; i<indexDomains.length; i++) {
						// offset = lb_solver - lb_index
						offsets[i] = this.settings.getTargetSolver().getArrayIndexingStartValue() - indexDomains[i].getRange()[0];
					}
					var = var.adaptOffsetToIndices(offsets);
				}
				
				if(var.getIntegerIndices().length == 1) 
					return var.toString();
				
				else {
					StringBuffer s =  new StringBuffer(var.getArrayNameOnly()+"(");
					int[] intIndices = var.getIntegerIndices();
					
					for(int i=0; i<intIndices.length; i++) {
						if(i > 0) s.append(",");
						s.append(intIndices[i]);
					}
					return s.toString()+")";
					
				}
				
			}
			else return e.toString(); 
		}
		
		else if(e instanceof SumConstraint) {
			StringBuffer s = new StringBuffer("");
			SumConstraint sum = (SumConstraint) e;
			Expression[] args = sum.getPositiveArguments();
			for(int i=0; i<args.length; i++) {
				if(i > 0) s.append(" + ");
				s.append(adaptLinearExpression(args[i]));
			}
			args = sum.getNegativeArguments();
			for(int i=0; i<args.length; i++) {
				s.append("-"+adaptLinearExpression(args[i]));
			}
			s.append(" "+this.relationalOperatorToString(mapOperatorToGecode(sum.getOperator()))+
					" "+adaptLinearExpression(sum.getResult()));
			return s.toString();
		} 
		
		else if(e instanceof CommutativeBinaryRelationalExpression) {
			CommutativeBinaryRelationalExpression expr = (CommutativeBinaryRelationalExpression) e;
			return this.adaptLinearExpression(expr.getLeftArgument())+" "
				+relationalOperatorToString(mapOperatorToGecode(expr.getOperator()))+
				" "+adaptLinearExpression(expr.getRightArgument());
		}
		
		else if(e instanceof NonCommutativeRelationalBinaryExpression) {
			NonCommutativeRelationalBinaryExpression expr = (NonCommutativeRelationalBinaryExpression) e;
			return this.adaptLinearExpression(expr.getLeftArgument())+" "
				+relationalOperatorToString(mapOperatorToGecode(expr.getOperator()))+
				" "+adaptLinearExpression(expr.getRightArgument());
		}
		
		else if(e instanceof Sum) {
			StringBuffer s = new StringBuffer("");
			Sum sum = (Sum) e;
			ArrayList<Expression> args = sum.getPositiveArguments();
			for(int i=0; i<args.size(); i++) {
				if(i > 0) s.append(" + ");
				s.append(adaptLinearExpression(args.get(i)));
			}
			args = sum.getNegativeArguments();
			for(int i=0; i<args.size(); i++) {
				s.append("-"+adaptLinearExpression(args.get(i)));
			}
			return s.toString();
		}
		
		else if(e instanceof Multiplication) {
			ArrayList<Expression> args = ((Multiplication) e).getArguments();
			StringBuffer s = new StringBuffer("");
			for(int i=0; i<args.size(); i++) {
				if(i > 0) s.append("*");
				s.append(this.adaptLinearExpression(args.get(i)));
			}
			
			return s.toString();
		}
		
		else if(e instanceof ProductConstraint) {
			ProductConstraint product = (ProductConstraint) e;
			Expression[] args = product.getArguments();
			StringBuffer s = new StringBuffer("");
			for(int i=0; i<args.length; i++) {
				if(i > 0) s.append("*");
				s.append(this.adaptLinearExpression(args[i]));
			}
			s.append(" == "+this.adaptLinearExpression(product.getResult()));	
			return s.toString();
		}
		
		//else if(e instanceof QuantifiedSum) {
			//Expression e = flattenQuantifiedSum((QuantifiedSum) e);
			
		//}
		
		else 
			throw new GecodeException("Cannot tailor linear expression "+e+" of type "+e.getClass().getSimpleName()+" yet, sorry.");
	}
	
	/**
	 * Inverts relational arithmetic Gecode operators.
	 * 
	 * @param operator
	 * @return
	 * @throws GecodeException
	 */
	public char invertRelationalOperator(char operator) 
		throws GecodeException {
			
		if(operator == GecodeConstraint.IRT_EQ) 
			return GecodeConstraint.IRT_NQ;
		
		else if (operator == GecodeConstraint.IRT_GQ)
			return GecodeConstraint.IRT_LE;
		
		else if (operator == GecodeConstraint.IRT_LQ)
			return GecodeConstraint.IRT_GR;
		
		else if (operator == GecodeConstraint.IRT_LE)
			return GecodeConstraint.IRT_GQ;
		
		else if (operator == GecodeConstraint.IRT_GR)
			return GecodeConstraint.IRT_LQ;
		
		else if (operator == GecodeConstraint.IRT_NQ)
			return GecodeConstraint.IRT_EQ;
		
		throw new GecodeException("Internal error. Cannot convert unknown operator: "+operator+".");
	}
	
	
	private String relationalOperatorToString(int operator) 
		throws GecodeException {
		
		if(operator == GecodeConstraint.IRT_EQ) 
			return "==";
		
		else if (operator == GecodeConstraint.IRT_GQ)
			return ">=";
		
		else if (operator == GecodeConstraint.IRT_LQ)
			return "<=";
		
		else if (operator == GecodeConstraint.IRT_LE)
			return "<";
		
		else if (operator == GecodeConstraint.IRT_GR)
			return ">";
		
		else if (operator == GecodeConstraint.IRT_NQ)
			return "!=";
		
		throw new GecodeException("Internal error. Cannot convert unknown operator: "+operator+".");
		
	}
	
	/**
	 * Add array (that represents the arguments of a constraint) to the list.
	 * 
	 * @param array
	 */
	/*
	private void addToBufferArrays(ArgsVariable array) {
		this.bufferArrays.add(array);
	}
	*/
	
	/*
	// this is an un-weighted sum
	if(e.getNegativeArguments().length == 0) {
		// map the sum arguments 
		Expression[] arguments = e.getPositiveArguments();
		GecodeAtom[] sumArguments = new GecodeAtom[arguments.length];
		for(int i=0; i<arguments.length; i++) {
			if(arguments[i] instanceof ArithmeticAtomExpression)
				sumArguments[i] = tailorArithmeticAtom((ArithmeticAtomExpression) arguments[i]);
			else throw new GecodeException("Flattening error. Cannot tailor '"+e+
					"' to linear sum constraint when sum-argument '"+arguments[i]+"' is not an atom expression.");
		}
		
		// map the relational operator
		char relation = mapOperators(e.getOperator());
		
		// map the result expression
		GecodeAtom result;
		if(e.getResult() instanceof ArithmeticAtomExpression)
			result = tailorArithmeticAtom((ArithmeticAtomExpression) e.getResult());
		else throw new GecodeException("Flattening error. Cannot tailor '"+e+
				"' to linear sum constraint when sum-result '"+e.getResult()+"' is not an atom expression.");

		ArgsVariable args;
		
		if(sumArguments[0] instanceof GecodeBoolVar) {
			args = new GecodeBoolVarArgs(GecodeConstraint.BUFFERARRAY_NAME+this.numberOfBufferArrays++,
										 sumArguments);
		}
		else args = new GecodeIntVarArgs(GecodeConstraint.BUFFERARRAY_NAME+this.numberOfBufferArrays++,
				sumArguments,
				e.getDomain()[0],
				e.getDomain()[1]);
		
		//addToBufferArrays(args);((SumConstraint) e)
		
		if(result instanceof GecodeIntVar) {
			return new GecodeLinear(args,
								relation,
								(GecodeIntVar) result);
		}
		else if(result instanceof GecodeBoolVar) {
			return new GecodeLinear(args,
						relation,
						(GecodeIntVar) result);
		}
		else if(result instanceof GecodeConstant) {
			return new GecodeLinear(args,
					relation,	
					(GecodeConstant) result);
		}
		else throw new GecodeException("Cannot tailor sum-constraint "+e+" to Gecode: unknown result type:"+result);
	}
	
	 throw new GecodeException("Cannot tailor (weighted?) sum-constraint "+e+" to Gecode yet.");
	 */
}
