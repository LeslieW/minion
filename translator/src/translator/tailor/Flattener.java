package translator.tailor;

import translator.solver.*;
import java.util.ArrayList;
import java.util.HashMap;
import translator.expression.*;
import translator.normaliser.NormalisedModel;


public class Flattener {

	public final String AUXVARIABLE_NAME = "_aux";
	protected int noAuxVariables;
	
	/** the target solver we want to tailor the model to */
	TargetSolver targetSolver;
	/** the list of all flattened constraints. constraints are added on the fly. */
	ArrayList<Expression> constraintBuffer;
	
	/** contains every (flattened) subexpression and it's corresponding variable*/
	HashMap<Expression,Variable> subExpressions;
	
	/** the normalised model contains the list of expression variables etc */
	NormalisedModel normalisedModel;
	
	// ========== CONSTRUCTOR ============================
	
	public Flattener(TargetSolver targetSolver,
			         NormalisedModel normalisedModel) {
		this.targetSolver = targetSolver;	
		this.normalisedModel = normalisedModel;
		this.constraintBuffer = new ArrayList<Expression>();
		this.subExpressions = new HashMap<Expression,Variable>();
		this.noAuxVariables = 0;
	}
	
	// ========== METHODS ================================
	
	/**
	 * Flattens the model according to the target solver that has been specified.
	 * 
	 * @return the flattened Normalised model that was given in the constructor
	 */
	public NormalisedModel flattenModel() throws TailorException  {
		
		if(this.targetSolver.supportsNestedExpressions()) {
			return this.normalisedModel;
		}
		
		ArrayList<Expression> constraints = this.normalisedModel.getConstraints();
		ArrayList<Expression> flattenedConstraints = new ArrayList<Expression>();
		
		for(int i=0; i<constraints.size(); i++) {
			ArrayList<Expression> flatExpression = flattenConstraint(constraints.get(i));
			for(int j=flatExpression.size()-1; j>=0; j--)
				flattenedConstraints.add(flatExpression.remove(j)); 
		}
		
		this.normalisedModel.replaceConstraintsWith(flattenedConstraints);
		
		return this.normalisedModel;
	}
	
	
	/**
	 * Flatten the parameter constraint and return the corresponding constraint
	 * 
	 * @return the list of flattened constraints that represents the parameter constraint
	 */
	protected ArrayList<Expression> flattenConstraint(Expression constraint) 
		throws TailorException {
		
		this.constraintBuffer.clear();
		
        // flatten the constraint
		Expression topExpression = flattenExpression(constraint);
		ArrayList<Expression> flattenedSubExpressions = this.constraintBuffer;
		flattenedSubExpressions.add(topExpression);
		
		// and return the constraint list
		return flattenedSubExpressions;
	}
	
	/**
	 * Flatten the parameter expression. Constraints that are added during the flattening process are stored 
	 * in the constraintList.
	 * 
	 * @param expression
	 * @return the eflattened xpression that is representative for the parameter expression. If other constraints
	 * have been added during the flattening process, they are stored in the constraintList.
	 * @throws TailorException
	 */
	protected Expression flattenExpression(Expression expression) 
		throws TailorException {
		
		if(expression instanceof RelationalExpression)
			return flattenRelationalExpression((RelationalExpression) expression);
		
		else if(expression instanceof ArithmeticExpression)
			return flattenArithmeticExpression((ArithmeticExpression) expression);
		
		else throw new TailorException("Unknown expression type (neither relational nor arithmetic):"+expression);
	}
	
	/**
	 * Flatten a relational expression
	 * 
	 * @param expression
	 * @return
	 */
	private  Expression flattenRelationalExpression(RelationalExpression expression) 
		throws TailorException {
		
		if(expression instanceof RelationalAtomExpression) 
			return flattenRelationalAtomExpression((RelationalAtomExpression) expression);
		
		else if(expression instanceof UnaryRelationalExpression) 
			return flattenUnaryRelationalExpression( (UnaryRelationalExpression) expression);
		
		else if(expression instanceof NonCommutativeRelationalBinaryExpression)
			return flattenNonCommutativeRelationalBinaryExpression((NonCommutativeRelationalBinaryExpression) expression);
		
		else if(expression instanceof QuantifiedExpression)
			return flattenQuantifiedExpression((QuantifiedExpression) expression);
		
		else if(expression instanceof Conjunction)
			return flattenConjunction((Conjunction) expression);
		
		else if(expression instanceof Disjunction)
			return flattenDisjunction((Disjunction) expression);
		
		else if(expression instanceof ElementConstraint) 
			return flattenElementConstraint((ElementConstraint) expression);
		
		else throw new TailorException("Cannot tailor relational expression yet, or unknown expression:"+expression);
	}
	
	
	
	/**
	 * Flatten the element constraint. 
	 * 
	 * @param elementConstraint
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenElementConstraint(ElementConstraint elementConstraint) 
		throws TailorException {
		
		
		// 1. flatten subexpressions
		boolean noConstraintsAsArguments = !this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT);
		Expression[] arguments = elementConstraint.getArguments();
		for(int i=0; i<arguments.length; i++) {
			if(noConstraintsAsArguments) 
				arguments[i].willBeFlattenedToVariable(true);

			arguments[i] = flattenExpression(arguments[i]);
		}
		
		// 2. if the constraint has to ne reified	
		if(elementConstraint.isGonnaBeReified()) {
			return reifyConstraint(elementConstraint);
		}
		else 			
			return elementConstraint;
	}
	
	/**
	 * 
	 * @param disjunction
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenDisjunction(Disjunction disjunction) 
		throws TailorException {

		// 1. ---- first flatten the arguments ----------------------
		ArrayList<Expression> arguments = disjunction.getArguments();
		for(int i=arguments.size()-1; i>=0; i--)
			arguments.add(i, flattenExpression(arguments.remove(i)));
		
		
		
		// 2. --- if the conjunction is nested, then just return it if n-ary conjunction is 
		//        is supported by the target solver
		if(this.targetSolver.supportsNaryDisjunction()) {
			return disjunction;
		}
		// 2. --- else the solver does not support n-ary disjunction, we have to flatten it to binary
		else {
			return flattenNaryToBinaryExpressions(arguments,null,Expression.OR);
		}
	
	}
	
	/**
	 * Flatten a conjunction: if the target solver supports n-ary conjunction, then just flatten the 
	 * subexpressions. Otherwise flatten the conjunction to conjunctions with 2 elements.
	 * @param conjunction
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenConjunction(Conjunction conjunction) 
		throws TailorException {
		
		// 1. ---- first flatten the arguments ----------------------
		ArrayList<Expression> arguments = conjunction.getArguments();
		for(int i=arguments.size()-1; i>=0; i--)
			arguments.add(i, flattenExpression(arguments.remove(i)));
		
		// 2. ---- if the conjunction is not nested/will be reified
		//         then split it into independent constraints
		if(!conjunction.isGonnaBeReified()) {
			for(int i=arguments.size()-1; i>0; i--) {
				this.constraintBuffer.add(arguments.remove(i));
			}
			return arguments.remove(0);
		}
		
		// 2. --- if the conjunction is nested, then just return it if n-ary conjunction is 
		//        is supported by the target solver
		else if(this.targetSolver.supportsNaryConjunction()) {
			return conjunction;
		}
		// 2. ----else the solver does not support n-ary conjunction, we have to flatten it to binary
		else {
			return flattenNaryToBinaryExpressions(arguments,null,Expression.AND);
		}

	}
	
	/**
	 * Flatten an n-ary relational expression to a binary one (recursivly). For example, consider a conjunction 
	 * and(a,b,c,d) that represents a /\ b /\ c /\ d. This method flattens the 4-ary conjunction 
	 * to a set of binary conjunctions (that are reified):
	 * 
	 * reify( and(a,b), aux1)         a /\ b == aux1
	 * reify( and(aux1, c), aux2)    a /\ b /\ c == aux2
	 * reify( aux2, d)               a /\ b /\ c /\ d == aux2 /\ d
	 * 
	 * This can be applied for any relational n-ary operator.
	 * Initially the parameter "reifiedVariable" has to be null (since there has not been
	 * any reification yet)! 
	 * 
	 * @param arguments
	 * @param reifiedVariable
	 * @param operator
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenNaryToBinaryExpressions(ArrayList<Expression> arguments, Expression reifiedVariable, int operator) 
	throws TailorException {
		
		// if the disjunction/conjunction only has 1 element, it has to hold
		if(arguments.size() == 1 && reifiedVariable == null)
			return arguments.remove(0);
		
		// get the 2 expressions we are building the conjunction from
		Expression leftExpression = arguments.remove(0);
		Expression rightExpression = null;
		if(reifiedVariable != null)
			rightExpression = reifiedVariable;
		else rightExpression = arguments.remove(0);
		
		// the last 2 elements, just return a conjunction of them
		if(arguments.size() == 0) {
			return new CommutativeBinaryRelationalExpression(leftExpression, operator, rightExpression);
		}
		else {
			CommutativeBinaryRelationalExpression binConjunction = new CommutativeBinaryRelationalExpression(leftExpression,
																										operator,
																										rightExpression);
			RelationalAtomExpression auxVariable = reifyConstraint(binConjunction);
			return flattenNaryToBinaryExpressions(arguments, auxVariable, operator);
		}
	}
	
	/**
	 * Flatten unary expressions. Depending on if the expression will be reified and if it
	 * has to be reified for the solver and if the solver supports the reification of the 
	 * constraint, it is flattened.
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenUnaryRelationalExpression(UnaryRelationalExpression expression) 
		throws TailorException {
		
		// first flatten the argument
		Expression argument = flattenExpression(expression.getArgument());
		
		
		// then continue to flatten the whole expression
		if(expression.getType() == Expression.NEGATION) {
			if(this.targetSolver.supportsConstraintsNestedInNegation())
				return new Negation(argument);
			else {
			   return reifyConstraint(new Negation(argument));
			}
		}
		else if(expression.getType() == Expression.ALLDIFFERENT) {
			if(expression.isGonnaBeReified()) {
                return reifyConstraint(new AllDifferent(argument));	
			}
			else return new AllDifferent(argument);
		}
		
		else throw new TailorException("Unknown unary relational expression:"+expression); 
	}
	
	
	
	

	
	/**
	 * Flatten atom expressions. If the target solver supports indexing with decision variables,
	 * we need not further flatten expressions. If it does not, we have to flatten the 
	 * atom expression to an element constraint
	 * @param atom
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenRelationalAtomExpression(RelationalAtomExpression atom) 
		throws TailorException {
		
		if(atom.getType() == Expression.BOOL_VARIABLE_ARRAY_ELEM) {
			Variable arrayVariable = atom.getVariable();
			if(arrayVariable.getType() == Expression.ARRAY_VARIABLE) {
				Expression[] indices = ((ArrayVariable) arrayVariable).getExpressionIndices();
				
				// if we have an array element indexed by something that is not a single integer
				if(indices != null) {
					if(this.targetSolver.supportsVariableArrayIndexing()) {
						for(int i=0; i<indices.length; i++) {
							// if the target solver only allows single variables as array indices (and no expressions)
							if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ARRAY_INDEXING)) {
								indices[i].willBeFlattenedToVariable(true);
							}
							indices[i] = flattenExpression(indices[i]);
						}
						return atom;	
					}
					// the solver does not support array indexing with something other than an integer
					else {
						if(indices.length == 1) {
							Expression index = indices[0];
							if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
								index.willBeFlattenedToVariable(true);
							Variable auxVariable = createAuxVariable(0,1);
							if(atom.isGonnaBeReified()) {
								return reifyConstraint(new ElementConstraint(arrayVariable,
															  index,
															  auxVariable));
							}
							else return new ElementConstraint(arrayVariable,
															  index,
															  auxVariable);
						}
					}
					// translate to an element constraint if the target solver supports it
					// find out if the element constraint has to be nested too!!
				}
			}
			else throw new TailorException("Cannot dereference variable that is not of type array:"+atom);
		}
		
		return atom;
	}
	
	
	/**
	 * Flattens non-commutative binary relational expressions, such as <,>=, =>, lex> etc. Simply flattens the 
	 * subexpressions and returns a new expression
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenNonCommutativeRelationalBinaryExpression(NonCommutativeRelationalBinaryExpression expression )
		throws TailorException {
		
		// if the target solver's constraint only supports variables as parameters, 
		//   we need to reify the left and right argument
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
			expression.getLeftArgument().willBeFlattenedToVariable(true);
			expression.getRightArgument().willBeFlattenedToVariable(true);
		}
		
		// flatten the subexpressions
		Expression leftFlattenedArgument = flattenExpression(expression.getLeftArgument());
		Expression rightFlattenedArgument = flattenExpression(expression.getRightArgument());
		
		if(expression.isGonnaBeReified()) {
			return reifyConstraint(new NonCommutativeRelationalBinaryExpression(leftFlattenedArgument,
					                                                           expression.getOperator(),
					                                                           rightFlattenedArgument));
		}
		
		else return new NonCommutativeRelationalBinaryExpression(leftFlattenedArgument,
				                            expression.getOperator(),
				                            rightFlattenedArgument);
	}
	
	/**
	 * 
	 * @param quantification
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenQuantifiedExpression(QuantifiedExpression quantification)
		throws TailorException {
		
		// ----- 1. get the domain over which the quantification ranges ------------
		Domain bindingDomain = quantification.getQuantifiedDomain();
		int domainType = bindingDomain.getType();
		if(domainType != Domain.BOOL &&
			domainType!= Domain.INT_BOUNDS &&
			  domainType != Domain.INT_SPARSE)
			throw new TailorException("Cannot unfold quantified expression '"+quantification
					+"'. The binding domain is not entirely constant:"+bindingDomain);
		
		int[] domainElements = null;
		if(domainType == Domain.BOOL)
			domainElements = ((BoolDomain) bindingDomain).getFullDomain();
		
		else if(domainType == Domain.INT_BOUNDS) 
			domainElements = ((BoundedIntRange) bindingDomain).getFullDomain();
		
		else domainElements = ((SparseIntRange) bindingDomain).getFullDomain();
		
		
		// 2. ----- get the binding variables ----------------------------------
		String[] bindingVariables = quantification.getQuantifiedVariables();
		ArrayList<String> variableList = new ArrayList<String>();
		for(int i=0; i<bindingVariables.length; i++)
			variableList.add(bindingVariables[i]);
		
		
		// 3 ------- create unfolded expressions --------------------------------
		ArrayList<Expression> unfoldedExpressions = insertVariablesForValues(variableList,
				                                                             domainElements,
				                                                             quantification.getQuantifiedExpression());
		
		// flatten all the unfolded expressions
		/*for(int i=unfoldedExpressions.size()-1; i>=0; i--) {
			unfoldedExpressions.add(flattenExpression(unfoldedExpressions.remove(i)));
		}*/
		
		// universal quantification 
		if(quantification.getType() == Expression.FORALL) {
			Conjunction conjunction = new Conjunction(unfoldedExpressions);
			conjunction.reduceExpressionTree();
			if(quantification.isGonnaBeReified()) {
				conjunction.willBeFlattenedToVariable(true);
				for(int i=conjunction.getArguments().size()-1; i>=0; i--) 
					conjunction.getArguments().get(i).willBeFlattenedToVariable(true);
			}
			
			
			return flattenConjunction(conjunction);
		}
		// existential quantification
		else {
			Disjunction disjunction = new Disjunction(unfoldedExpressions);
			disjunction.reduceExpressionTree();
			
			for(int i=disjunction.getArguments().size()-1; i>=0; i--) 
				disjunction.getArguments().get(i).willBeFlattenedToVariable(true);
			
			return flattenDisjunction(disjunction);
		}
		
	}
	
	
	/**
	 * This is a helper function for flattening quantified expressions (the unfolding of the quantification). The variablelist containts all binding 
	 * variables that should be inserted intp the expression.
	 * 
	 * @param variableList
	 * @param values
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private ArrayList<Expression> insertVariablesForValues(ArrayList<String> variableList, int[] values, Expression expression)
		throws TailorException {
		
		ArrayList<Expression> unfoldedExpressions = new ArrayList<Expression>();
		
		//System.out.println("want to insert values into expression :"+expression);
		
		// this is the last variable we have to insert
		if(variableList.size() == 1) {
			String variableName = variableList.get(0);
			for(int i=0; i<values.length; i++) {
				Expression unfoldedExpression = expression.copy().insertValueForVariable(values[i], variableName);
				//System.out.println("Inserted '"+values[i]+"' for variable '"+variableName+"' in expression '"+expression+"' and got expression:"+unfoldedExpression);
				unfoldedExpression = unfoldedExpression.evaluate();
				unfoldedExpressions.add(unfoldedExpression);
			}
			return unfoldedExpressions;
		}
		// we have some more variables to insert into the expression
		else {
			String variableName = variableList.remove(0);
			for(int i=0; i<values.length; i++) {
				Expression unfoldedExpression = expression.copy().insertValueForVariable(values[i], variableName);
				ArrayList<Expression> furtherUnfoldedExpressions = insertVariablesForValues(variableList, values, unfoldedExpression);
				
				for(int j=0; j<furtherUnfoldedExpressions.size(); j++) {
					unfoldedExpressions.add(furtherUnfoldedExpressions.get(j).evaluate());
				}
			}
			
		}
		
		return unfoldedExpressions;
	}
	/**
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenCommutativeBinaryRelationalExpression(CommutativeBinaryRelationalExpression expression )
	throws TailorException {
	
	// detect products, sums etc. before flattening the arguments!	
		return expression;
	}
	
	
	/**
	 * 
	 * @param expression
	 * @return
	 */
	private Expression flattenArithmeticExpression(ArithmeticExpression expression) {
		
		return expression;
	}
	
	
	
	// ========================= GENERAL HELPER METHODS ===========================
	
	/**
	 * reify the constraint and return the auxiliary variable that "represents" it.
	 * If there is a common subexpression, the corresponding auxiliary variable is 
	 * picked.
	 * @param constraint that is definetly reifiable by the target solver
	 * @return the auxiliary variable that represents the expression
	 */
	private RelationalAtomExpression reifyConstraint(Expression constraint) 
		throws TailorException {
		
		if(!this.targetSolver.supportsReificationOf(constraint.getType()))
			throw new TailorException
			("Cannot reify constraint because its reification is not supported by the target solver:"+constraint);
		
		Variable auxVariable = null;
		
		// if we have a common subexpression, use the corresponding variable
		if(this.subExpressions.containsKey(constraint))
			auxVariable = this.subExpressions.get(constraint);
		
		// if we have no common subexpression, create a new auxiliary variable
		// and add the constraint to the list of subexpressions
		else  {
			auxVariable = createAuxVariable(0, 1);
			this.subExpressions.put(constraint,auxVariable);
		}
		
		// add the flattened constraint to the constraint buffer
		this.constraintBuffer.add(new Reification(constraint,
				                                  auxVariable));
		
		return new RelationalAtomExpression(auxVariable);
	}
	
	/**
	 * Create an auxiliary variable. It is added to the normalised model
	 * and is set to be searched on if specified in the target solver.
	 * 
	 * @param lb
	 * @param ub
	 * @return an auxiliary variables over the bounds lb,ub.
	 */
	private Variable createAuxVariable(int lb, int ub) {
		
		Variable auxVariable = null;
		
		if(lb ==0 && ub == 1)
			auxVariable = new SingleVariable(AUXVARIABLE_NAME+noAuxVariables++,
					                         new BoolDomain());
		else auxVariable = new SingleVariable(AUXVARIABLE_NAME+noAuxVariables++, 
				                                  new BoundedIntRange(lb,ub));
		
		if(!this.targetSolver.willSearchOverAuxiliaryVariables())
			auxVariable.setToSearchVariable(false);
		
		this.normalisedModel.addAuxiliaryVariable(auxVariable);
		
		return auxVariable;
	}
}
