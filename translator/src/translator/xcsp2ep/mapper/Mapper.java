package translator.xcsp2ep.mapper;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import translator.xcsp2ep.parser.PredicateTokens.RelationalOperator;
import translator.xcsp2ep.parser.PredicateTokens;
import translator.xcsp2ep.mapper.functionalsParser.*;
import translator.xcsp2ep.parser.XCSPInstance;
import translator.xcsp2ep.parser.components.*;
import translator.expression.*;
import translator.normaliser.Objective;

/**
 * The mapper maps the XCSP instance to an Essence' model
 * 
 * @author andrea
 *
 */

public class Mapper {

	// list of Essence' decision variable names
	ArrayList<String> variableNames;
	// map of Essence' decision variables and their corresponding domain
	HashMap<String, Domain> variablesMap;
	
	
	public Mapper() {
		this.variableNames = new ArrayList<String>();
		this.variablesMap = new HashMap<String, Domain>();
	}

	//======================================================
	
	
	public EssencePModel mapToEssencePrime(XCSPInstance xcspInstance) 
		throws MapperException,Exception  {
		
		// fill the variablesNames and variablesMap
		mapVariableList(xcspInstance.getVariables());
		
		// map the constraints
		ArrayList<Expression> constraints = mapConstraints(xcspInstance);
	
		// create an Essence' model
		return new EssencePModel(this.variablesMap,
								 this.variableNames,
								 constraints,
								 new Objective());
	}
	
	/**
	 * Generate the set of Essence' constraints defined in an XCSP instance
	 * 
	 * @param xcspInstance
	 * @return
	 * @throws MapperException
	 * @throws Exception
	 */
	protected ArrayList<Expression> mapConstraints(XCSPInstance xcspInstance) 
		throws MapperException, Exception {
		
		ArrayList<Expression> constraintsList = new ArrayList<Expression>();
	
		int nbConstraints = xcspInstance.getNbGlobalConstraints()+ xcspInstance.getNbExtensionConstraints()+ xcspInstance.getNbIntensionConstraints();
		Map<String, PConstraint> constraintsMap = xcspInstance.getMapOfConstraints();
		// get every constraint out of the map (constraints are called C0, C1, C2,,,
		for(int i =0; i<nbConstraints; i++) {
			PConstraint xcspConstraint = constraintsMap.get("C"+i);
			if(xcspConstraint!=null)
				constraintsList.add(mapConstraint(xcspConstraint));
		}
		
		return constraintsList;
	}
	
	/**
	 * Map a xcspConstraint to an Essence' Expression
	 * 
	 * @param xcspConstraint
	 * @return
	 * @throws MapperException
	 * @throws Exception
	 */
	protected Expression mapConstraint(PConstraint xcspConstraint) 
		throws MapperException, Exception  {
		
		if(xcspConstraint instanceof PIntensionConstraint)
			return mapIntensionalConstraint((PIntensionConstraint) xcspConstraint);
		
		else if(xcspConstraint instanceof PGlobalConstraint)
			return mapGlobalConstraint((PGlobalConstraint) xcspConstraint);
		
		else if(xcspConstraint instanceof PExtensionConstraint) 
			return mapExtensionalConstraint((PExtensionConstraint) xcspConstraint);
		
		else throw new MapperException("Unknown or unsupported constraint type:"+xcspConstraint);
	}
	
	

	/**
	 * Maps an extensional constraint (i.e. a table constraint) to an Essence'
	 * table constraint. Only extensional constraints with 'supported' semantic
	 * can be converted (no 'conflicting' table constraints).
	 * 
	 * @param xcspConstraint
	 * @return
	 * @throws MapperException
	 */
	private Expression mapExtensionalConstraint(PExtensionConstraint xcspConstraint) 
		throws MapperException {
		
		PRelation relation = xcspConstraint.getRelation();
		
		if(relation.getSemantics().equals("supports")) {
			// tuples
			int[][] xcspTuples = relation.getTuples();
			ConstantTuple[] tuples = new ConstantTuple[xcspTuples.length];
			for(int i=0; i<xcspTuples.length; i++) {
				tuples[i] = new ConstantTuple(xcspTuples[i]);
			}
	
			PVariable[] xcspVariables = xcspConstraint.getScope();
			if(xcspTuples[0].length != xcspVariables.length)
				throw new MapperException("Infeasible extensional constraint: tuple-length '"+xcspTuples[0].length
						+"' does not match scope of variables:"+xcspVariables.length);
			
			Variable[] variables = new Variable[xcspVariables.length];
			for(int i=0; i<variables.length; i++)
				variables[i] = mapVariable(xcspVariables[i]);
			
			return new TableConstraint(variables, tuples);
		}
		
		else throw new MapperException("Cannot map 'conflicting' extensional constraint (table constraint) yet.");

	}
	
	
	/**
	 * Map a global XCSP constraint to a global Essence' constraint
	 * 
	 * @param xcspGlobal
	 * @return
	 * @throws MapperException
	 */
	private Expression mapGlobalConstraint(PGlobalConstraint xcspGlobal) 
		throws MapperException {
		
		if(xcspGlobal instanceof PAllDifferent) {
			PAllDifferent xcspAllDiff = (PAllDifferent) xcspGlobal;
			ArithmeticAtomExpression[] variables = new ArithmeticAtomExpression[xcspAllDiff.getScope().length];
			for(int i=0; i<variables.length; i++) {
				variables[i] = new ArithmeticAtomExpression(mapVariable(xcspAllDiff.getScope()[i]));
			}
			VariableArray argument = new VariableArray(variables);
			return new AllDifferent(argument);
		}
		else if(xcspGlobal instanceof PElement) 
			return mapElementConstraint((PElement) xcspGlobal);
		
		else if(xcspGlobal instanceof PWeightedSum)
			return mapWeightedSum((PWeightedSum) xcspGlobal);
		
		throw new MapperException("Unknown or unsupported global constraint type:"+xcspGlobal);
	}
	

	
	/**
	 * Map an intensional Constraint that consists of a functional expression 
	 * and a list of parameter variables for that functional expression
	 * 
	 * @param xcspConstraint
	 * @return
	 * @throws MapperException
	 * @throws Exception
	 */
	private Expression mapIntensionalConstraint(PIntensionConstraint xcspConstraint) 
		throws MapperException,Exception  {
		
		// parse and map the predicate functional
		PPredicate predicate = xcspConstraint.getPredicate();
		Expression predicateExpression = mapFunctional(predicate.getFunctionalExpression());
		
		// constraint parameters
		String[] predicateParameters = predicate.getFormalParameters();
		String parameterString = xcspConstraint.getParameters();
		// split the string into PVariables and Integers and return them as AromExpression[]
		ArrayList<AtomExpression> parameters = mapParameterString(parameterString);
		
		if(predicateParameters.length != parameters.size())
			throw new MapperException("Scope of predicate and scope of constraint do not match.\nPredicate scope: "
					+printArrayObjects(predicateParameters)+"\nConstraint scope: "+parameters);
		
	
		for(int i=0; i<parameters.size(); i++) {
			
			if(parameters.get(i).getType() == Expression.INT)
				predicateExpression = predicateExpression.insertValueForVariable(((ArithmeticAtomExpression) parameters.get(i)).getConstant(),
															                      predicateParameters[i]);
			
			else if(parameters.get(i) instanceof ArithmeticAtomExpression)
			// 2. insert constraint parameters for predicate parameters into the predicateExpression
				predicateExpression = predicateExpression.replaceVariableWith(new SimpleVariable(predicateParameters[i]), 
					                                                          ((ArithmeticAtomExpression) parameters.get(i)).getVariable());
			else throw new MapperException("Unknown or unsupported Parameter for Functional:"+parameters.get(i));
		}
		
		return predicateExpression;
	}
	
	/**
	 * Map a simple variable to an Essence' variable (is rather easy
	 * because there are only single variables in XCSP)
	 * 
	 * @param xscpVariable
	 * @return
	 * @throws MapperException
	 */
	private Variable mapVariable(PVariable xscpVariable) 
		throws MapperException {
		
		return new SingleVariable(xscpVariable.getName(),
								  this.variablesMap.get(xscpVariable.getName()));
	}
	
	/**
	 * Map the variable list from XCSP format to Essence' format 
	 * 
	 * @param xcspVariables
	 * @throws MapperException
	 */
	private void mapVariableList(PVariable[] xcspVariables) 
		throws MapperException {
		
		for(int i=0; i<xcspVariables.length; i++) {
			PVariable xcspVariable = xcspVariables[i];
			this.variableNames.add(xcspVariable.getName());
			this.variablesMap.put(xcspVariable.getName(), mapDomain(xcspVariable.getDomain()));
		}
	}
	
	/**
	 * Map an XCSP Domain to an Essence' domain. Domain names are 
	 * omitted here because they do not matter in Essence' models.
	 * 
	 * @param xcspDomain
	 * @return
	 */
	private Domain mapDomain(PDomain xcspDomain) 
		throws MapperException {
		
		//String domainName = xcspDomain.getName();
		int[] domainValues = xcspDomain.getValues();
		
		if(isSparseDomain(domainValues)) 
			return new SparseIntRange(domainValues);
		
		
		// we have a bounds domain
		else {
			if(domainValues[0] == 0 && domainValues[domainValues.length-1] == 1)
				return new BoolDomain();
			
			return new BoundedIntRange(domainValues[0], domainValues[domainValues.length-1]);
		}
		
	}
	
	
	/**
	 * Parse the functionals (represented as a string) and return the corresponding 
	 * Essence' expression.
	 * 
	 * @param functionalString
	 * @return
	 * @throws Exception
	 */
	private RelationalExpression mapFunctional(String functionalString) 
		throws Exception {
		
		FunctionalsParser parser = new FunctionalsParser(new FunctionalsLexer(new StringReader(functionalString)) );
		return (RelationalExpression) parser.parse().value;
	}
	
	
	private ArrayList<AtomExpression> mapParameterString(String parameters) 
		throws MapperException {
		
		ArrayList<AtomExpression> list = new ArrayList<AtomExpression>();
		
		int i = 0;
		StringBuffer elem = new StringBuffer("");
		while(i < parameters.length()) {
			while(i < parameters.length() && 
					parameters.charAt(i) != ' ') {
				elem.append(parameters.charAt(i));
				i++;	
			}
			// we have a variable
			if(elem.charAt(0) == 'V') {
				list.add(new ArithmeticAtomExpression(new SimpleVariable(elem.toString())));
			} // we have an integer value
			else list.add(new ArithmeticAtomExpression(new Integer(elem.toString())));
			
			elem = new StringBuffer("");
			if(parameters.length() > i)
				parameters = parameters.substring(i+1);
			else break;
			i = 0;
		}
		
		return list;
	}
	
	/**
	 * Map a weighted sum to a corresponding sum expression in Essence'
	 * 
	 * @param xcspSum
	 * @return
	 * @throws MapperException
	 */
	private Expression mapWeightedSum(PWeightedSum xcspSum) 
		throws MapperException {
		
		// variables
		PVariable[] xcspVariables = xcspSum.getScope();
		int[] coeff = xcspSum.getCoeffs();
		
		ArrayList<Expression> positiveArgs = new ArrayList<Expression>();
		ArrayList<Expression> negativeArgs = new ArrayList<Expression>();
		Variable[] variables = new Variable[xcspVariables.length];
		for(int i=0; i<variables.length; i++) {
			variables[i] = mapVariable(xcspVariables[i]);
			if(coeff[i] == 1) {
				positiveArgs.add(new ArithmeticAtomExpression(variables[i]));
			}
			else if(coeff[i] == -1) {
				negativeArgs.add(new ArithmeticAtomExpression(variables[i]));
			}
			if(coeff[i] > 1) {
				Multiplication mult = new Multiplication(new Expression[] {
						new ArithmeticAtomExpression(coeff[i]),
						new ArithmeticAtomExpression(variables[i])
				                                                           }
				                                         );
				positiveArgs.add(mult);
			}
			else if(coeff[i] < -1) {
				Multiplication mult = new Multiplication(new Expression[] {
						new ArithmeticAtomExpression(-coeff[i]),
						new ArithmeticAtomExpression(variables[i])
				                                                           }
				                                         );
				negativeArgs.add(mult);
			}
			// else the coefficient is 0 and we can omit the variable
		}
		Sum sum = new Sum(positiveArgs, negativeArgs);
			
		// limit
		int limit = xcspSum.getLimit();
		
		// operator
		int operator = mapOperator(xcspSum.getOperator());	
		
		
		if(isCommutativeOperator(xcspSum.getOperator().toString())) {
			return new CommutativeBinaryRelationalExpression(sum, 
					                                         operator, 
					                                         new ArithmeticAtomExpression(limit));
		}
		
		else return new NonCommutativeRelationalBinaryExpression(sum, 
				                                                 operator, 
				                                                 new ArithmeticAtomExpression(limit));
	}
	
	
	/**
	 * Maps a XCSP element constraint to an Essence' element constraint
	 * 
	 * element(index, table, value)  <-->   table[index] = value
	 * 
	 * @param xcspElement
	 * @return
	 * @throws MapperException
	 * @throws Exception
	 */
	private ElementConstraint mapElementConstraint(PElement xcspElement) 
		throws MapperException {
		
		// index
		Expression index = mapVariable(xcspElement.getIndexVariable());
		
		// value
		AtomExpression value;
		Object xcspValue = xcspElement.getValue();
		if(xcspValue instanceof PVariable)
			value = new ArithmeticAtomExpression(mapVariable((PVariable) xcspValue));
		else if(xcspValue instanceof Integer)
			value = new ArithmeticAtomExpression((Integer) xcspValue);
		else throw new MapperException("Unknown Object '"+xcspValue+"' as 'value' for element constraint: "+xcspElement);
		
		// array (table)
		Object[] xcspTable = xcspElement.getTable();
		AtomExpression[] table = new AtomExpression[xcspTable.length];
		for(int i=0; i < table.length; i++) {
			if(xcspTable[i] instanceof PVariable)
				table[i] = new ArithmeticAtomExpression(mapVariable((PVariable) xcspTable[i]));
			else if(xcspTable[i] instanceof Integer)
				table[i] = new ArithmeticAtomExpression((Integer) xcspTable[i]);
			else throw new MapperException("Unknown Object '"+xcspTable[i]+"' in 'table' for element constraint: "+xcspElement);
		}
		
		return new ElementConstraint(new VariableArray(table), index, value);
	}
	
	/**
	 * Returns true if the String represents an XCSP operator String
	 * that is a commutative operator
	 * 
	 * @param operator
	 * @return
	 * @throws MapperException
	 */
	private boolean isCommutativeOperator(String operator) 
		throws MapperException {
		
		if(operator.equals(PredicateTokens.ADD) ||
				operator.equals(PredicateTokens.MUL) ||
				operator.equals(PredicateTokens.EQ) ||
				operator.equals(PredicateTokens.NE) ||
				operator.equals(PredicateTokens.IFF) ||
				operator.equals(PredicateTokens.OR) ||
				operator.equals(PredicateTokens.AND) )
			return true;
			
		return false;
	}
	
	
	/**
	 * Maps A XCSP RelationalOperator to the corresponding 
	 * operator (in int form) for Essence'
	 * 
	 * @param operator
	 * @return
	 * @throws MapperException
	 */
	private int mapOperator(RelationalOperator operator) 
		throws MapperException {
		
		String op = operator.toString();
		
		if(op.equals(PredicateTokens.ABS))
			return Expression.ABS;		
		
		else if(op.equals(PredicateTokens.ADD))
			return Expression.PLUS;
		
		else if(op.equals(PredicateTokens.AND))
			return Expression.AND;
		
		else if(op.equals(PredicateTokens.DIV))
			return Expression.DIV;
		
		else if(op.equals(PredicateTokens.EQ))
			return Expression.EQ;
		
		else if(op.equals(PredicateTokens.GE))
			return Expression.GEQ;
		
		else if(op.equals(PredicateTokens.GT))
			return Expression.GREATER;
		
		else if(op.equals(PredicateTokens.IF))
			return Expression.IF;
		
		else if(op.equals(PredicateTokens.IFF))
			return Expression.IFF;
		
		else if(op.equals(PredicateTokens.LE))
			return Expression.LEQ;
		
		else if(op.equals(PredicateTokens.LT))
			return Expression.LESS;
		
		else if(op.equals(PredicateTokens.MAX))
			return Expression.MAX;
		
		else if(op.equals(PredicateTokens.MIN))
			return Expression.MIN;
		
		else if(op.equals(PredicateTokens.MOD))
			return Expression.MOD;
		
		else if(op.equals(PredicateTokens.MUL))
			return Expression.MULT;
		
		else if(op.equals(PredicateTokens.NE))
			return Expression.NEQ;
		
		else if(op.equals(PredicateTokens.NEG))
			return Expression.NEGATION;
		
		else if(op.equals(PredicateTokens.NOT))
			return Expression.NEGATION;
		
		else if(op.equals(PredicateTokens.OR))
			return Expression.OR;
		
		else if(op.equals(PredicateTokens.POW))
			return Expression.POWER;
		
		else if(op.equals(PredicateTokens.SUB))
			return Expression.MINUS;
		
		else if(op.equals(PredicateTokens.XOR))
			return Expression.XOR;
		
		else throw new MapperException("Unknown or unsupported operator: "+op);
	}
	
	/**
	 * Determine if the given int list is a list of constantly
	 * by-one increasing numbers or not. 
	 * [1,2,3,4,5] -> constantly increasing by one -> bounds domain
	 * [1,3,5]     -> sparse domain 
	 * 
	 * @param domainValues
	 * @return
	 * @throws MapperException
	 */
	private boolean isSparseDomain(int[] domainValues) 
		throws MapperException {
		
		boolean constantlyIncreasingValues = true;
		
		if(domainValues.length ==0)
			throw new MapperException("Empty domain.");
		
		int lastValue = domainValues[0];
		
		for(int i=1; i<domainValues.length; i++) {
			if(lastValue != domainValues[i]-1)
				constantlyIncreasingValues = false;
			lastValue = domainValues[i];
		}
		
		return !constantlyIncreasingValues;
	}
	
	/**
	 * Print an array of objects [O1, O2, O3, ..., On] like
	 * 
	 * O1, O2, O3, ..., On
	 * 
	 * @param array
	 * @return
	 */
	public String printArrayObjects(Object[] array) {
		
		StringBuffer s = new StringBuffer("");
		
		for(int i=0; i<array.length-1; i++)
			s.append(array[i].toString()+", ");
		
		if(array.length > 0)
			s.append(array[array.length-1].toString());
		
		return s.toString();
	}
}
