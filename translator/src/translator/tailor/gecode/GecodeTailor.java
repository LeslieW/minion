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
	/** contains all single variables (that are then put into an Var array in the model)*/
	
	// stuff for buffering single int/boolean variables
	private ArrayList<GecodeIntVar> singleIntVariableList;
	private ArrayList<GecodeIntVar> auxIntVariableList;
	private ArrayList<GecodeBoolVar> singleBoolVariableList;
	private ArrayList<GecodeBoolVar> auxBoolVariableList;
	
	/** works as container for all integer auxiliary variables*/
	private GecodeIntVarArray auxIntVariableBuffer;
	/** works as container for all boolean auxiliary variables*/
	private GecodeBoolVarArray auxBoolVariableBuffer;
	
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
															
		this.auxIntVariableBuffer = new GecodeIntVarArray(  ((Gecode) settings.getTargetSolver()).AUX_INT_VAR_ARRAY_NAME,
				0,0,0);
		this.auxBoolVariableBuffer = new GecodeBoolVarArray(  ((Gecode) settings.getTargetSolver()).AUX_BOOL_VAR_ARRAY_NAME, 0);
															
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
		throws GecodeException {
		
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
		// TODO: add multidimensional arrays
		
		
		// collect auxiliary variables
		tailorAuxiliaryVariables();
		ArrayList<GecodeArrayVariable> auxBufferArrays = new ArrayList<GecodeArrayVariable>();
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
							   this.auxBoolVariableList);
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
		throws GecodeException {
		
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
				if(variable instanceof GecodeArrayVariable)
					this.variableList.add((GecodeArrayVariable) variable);
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
	 * Tailors variable with name variableName to a Gecode Atom
	 * 
	 * @param variableName
	 * @param domain
	 * @return
	 * @throws GecodeException
	 */
	protected GecodeVariable tailorToGecode(String variableName) 
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
	protected char mapOperators(int essencePOperator) 
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
	
	/**
	 * Translates a product constraint to Gecode's mult constraint
	 * 
	 */
	private GecodeMult tailorProductConstraint(ProductConstraint product) 
		throws GecodeException {
		
		Expression resultOld = product.getResult();
		Expression[] arguments = product.getArguments();
		
		if(arguments.length > 2)
			throw new GecodeException("Flattening error of expression:"+product+". Gecode only supports binary multiplication.");
		// else:  we don't need to care about 1 argument only -> evaluation should have taken care of that
		
		GecodeAtom leftArg, rightArg;
		
		if(arguments[0] instanceof ArithmeticAtomExpression) {
			leftArg = tailorArithmeticAtom((ArithmeticAtomExpression) arguments[0]);
		}
		else throw new GecodeException("Cannot tailor '"+arguments[0]+"' in expression '"+product+"' to Gecode because it is not an integer atom.");
		
		if(arguments[1] instanceof ArithmeticAtomExpression) {
			rightArg = tailorArithmeticAtom((ArithmeticAtomExpression) arguments[1]);
		}
		else throw new GecodeException("Cannot tailor '"+arguments[1]+"' in expression '"+product+"' to Gecode because it is not an integer atom.");
		
		
		// result expression
		GecodeAtom result;
		if(resultOld instanceof ArithmeticAtomExpression) {
			result = tailorArithmeticAtom((ArithmeticAtomExpression) resultOld);
		}
		else throw new GecodeException("Cannot tailor '"+resultOld+"' in expression '"+product+"' to Gecode because it is not an integer atom.");
		
		
		return new GecodeMult(leftArg,rightArg,result);
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
	private GecodePostConstraint tailorSumConstraint(SumConstraint e) 
		throws GecodeException {
		
		// we can simply post a linear constraint, since the 
		// expression has been linearised by flattening
		StringBuffer sumConstraint = new StringBuffer(e.toGecodeString());
		
		return new GecodePostConstraint(sumConstraint.toString());
		
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
			
			//addToBufferArrays(args);
			
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
	
	
	
	/**
	 * Tailor an absolute value expression to Gecode
	 * 
	 * @param e
	 * @return
	 */
	private GecodeAbs tailorAbsoluteConstraint(AbsoluteConstraint e) 
		throws GecodeException {
		
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
	 * Tailors arithmetic atom expressions to Gecode atom
	 * 
	 * @param e
	 * @return
	 * @throws GecodeException
	 */
	private GecodeAtom tailorArithmeticAtom(ArithmeticAtomExpression e) 
		throws GecodeException {
		
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
			else return new GecodeIntArrayElem(var.getArrayNameOnly(),
											  var.getIntegerIndices(),
											  var.getDomain()[0],
											  var.getDomain()[1]);
		}
		
		else throw new GecodeException("Unknown arithmetic atomic type: "+e);
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
}
