package translator.tailor.gecode;

import translator.normaliser.NormalisedModel;
import translator.TranslationSettings;
import translator.expression.*;

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
	
	private ArrayList<GecodeConstraint> constraintBuffer;
	private ArrayList<ArgsVariable> bufferArrays;
	private ArrayList<GecodeVariable> variableList;
	int numberOfBufferArrays = 0;
	
	
	
	
	public GecodeTailor() {
		constraintBuffer = new ArrayList<GecodeConstraint>();
		bufferArrays = new ArrayList<ArgsVariable>();
		variableList = new ArrayList<GecodeVariable>();
	}
	
	
	//========== METHODS ==================
	
	public GecodeModel tailorToGecode(NormalisedModel model,
									  TranslationSettings settings) 
		throws GecodeException {
		
		this.settings = settings;
		this.essencePmodel = model;
		
		tailorDecisionVariables();
		
		ArrayList<Expression> eConstraints = model.getConstraints();
		for(int i=0; i<eConstraints.size(); i++)
			constraintBuffer.add(tailorToGecode(eConstraints.get(i)));
		
		GecodeModel gecodeModel = new GecodeModel(settings,
							   variableList,
							   bufferArrays,
							   constraintBuffer);
		System.out.println("==================================");
		System.out.println("Normal print:\n"+gecodeModel.toString()+"\n\n");
		System.out.println("==================================");
		System.out.println("CC print:\n"+gecodeModel.toCCString()+"\n\n");
		System.out.println("==================================");
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
		
		for(int i=0; i < decisionVarNames.size(); i++) {
			this.variableList.add(tailorToGecode(decisionVarNames.get(i)));	
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
		
		throw new GecodeException("Cannot tailor variable '"+variableName+"' to Gecode (yet).");
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
	
	
	private void mergeVariableLists() {
		for(int i=this.bufferArrays.size()-1; i>= 0; i--) {
			this.variableList.add(this.bufferArrays.remove(i));
		}
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
	private GecodeLinear tailorSumConstraint(SumConstraint e) 
		throws GecodeException {
		
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
			
			addToBufferArrays(args);
			
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
		
		else throw new GecodeException("Cannot tailor (weighted?) sum-constraint "+e+" to Gecode yet.");
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
			result = tailorArithmeticAtom((ArithmeticAtomExpression) argumentOld);
		}
		else throw new GecodeException("Cannot tailor argument '"+argumentOld+"' of abs-expression '"+e+"' to Gecode because it is not an integer atom.");
		
		
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
			else return new GecodeArrayIntVar(var.getArrayNameOnly(),
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
	private void addToBufferArrays(ArgsVariable array) {
		this.bufferArrays.add(array);
	}
	
}
