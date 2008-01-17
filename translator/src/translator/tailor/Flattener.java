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
		
		//System.out.println("Flattening expression:"+expression);
		
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
		
		else if(expression instanceof QuantifiedSum)
			return flattenQuantifiedSum((QuantifiedSum) expression);
		
		else if(expression instanceof CommutativeBinaryRelationalExpression) 
			return flattenCommutativeBinaryRelationalExpression(
					(CommutativeBinaryRelationalExpression) expression);
		
		else return expression;
		//else throw new TailorException("Cannot tailor relational expression yet, or unknown expression:"+expression);
	}
	
	
	/**
	 * Flatten a quantified sum into a sum representation by unfolding the
	 * quantification (if necessary for the solver).
	 * 
	 * @param quantifiedSum
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenQuantifiedSum(QuantifiedSum quantifiedSum) 
		throws TailorException {
		
		// if the target solver supports quantified sums - do nothing
		if(this.targetSolver.supportsConstraint(Expression.Q_SUM)) {
			return quantifiedSum;
		}
		
		// 	----- 1. get the domain over which the quantification ranges ------------
		Domain bindingDomain = quantifiedSum.getQuantifiedDomain();
		int domainType = bindingDomain.getType();
		if(domainType != Domain.BOOL &&
				domainType!= Domain.INT_BOUNDS &&
				domainType != Domain.INT_SPARSE)
					throw new TailorException("Cannot unfold quantified expression '"+quantifiedSum
						+"'. The binding domain is not entirely constant:"+bindingDomain);
			
		int[] domainElements = null;
		if(domainType == Domain.BOOL)
			domainElements = ((BoolDomain) bindingDomain).getFullDomain();
			
		else if(domainType == Domain.INT_BOUNDS) 
			domainElements = ((BoundedIntRange) bindingDomain).getFullDomain();
			
		else domainElements = ((SparseIntRange) bindingDomain).getFullDomain();
			
			
		// 2. ----- get the binding variables ----------------------------------
		String[] bindingVariables = quantifiedSum.getQuantifiedVariables();
		ArrayList<String> variableList = new ArrayList<String>();
		for(int i=0; i<bindingVariables.length; i++)
			variableList.add(bindingVariables[i]);
			
			
		// 3 ------- create unfolded expressions --------------------------------
		ArrayList<Expression> unfoldedExpressions = insertVariablesForValues(variableList,
					                                                             domainElements,
					                                                             quantifiedSum.getQuantifiedExpression());
		
		
		ArrayList<Expression> positiveElements = new ArrayList<Expression>();
		ArrayList<Expression> negativeElements = new ArrayList<Expression>();
		
		for(int i=unfoldedExpressions.size()-1; i>=0; i--) {
			Expression expression = unfoldedExpressions.remove(i);
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.SUM)) 
				expression.willBeFlattenedToVariable(true);
			
			expression = flattenExpression(expression);
				
			if(expression instanceof UnaryMinus) 
				negativeElements.add(( (UnaryMinus) expression).getArgument());
			else positiveElements.add(expression);
		}
		
		Sum flattenedSum = new Sum(positiveElements,
				                   negativeElements);
		
		if(quantifiedSum.isGonnaBeReified()) {
			return reifyConstraint(flattenedSum);
		}
		
		return flattenedSum;
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
		
		// 2. if the constraint has to be reified	
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
		for(int i=arguments.size()-1; i>=0; i--) {
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.OR))
				arguments.get(i).willBeFlattenedToVariable(true);
			arguments.add(i, flattenExpression(arguments.remove(i)));
		}
		
		
		// 2. --- if the conjunction is nested, then just return it if n-ary conjunction is 
		//        is supported by the target solver
		if(this.targetSolver.supportsConstraint(Expression.NARY_DISJUNCTION)) {
			return disjunction;
		}
		// 2. --- else the solver does not support n-ary disjunction, we have to flatten it to binary
		else {
			return flattenRelationalNaryToBinaryCommutativeExpressions(arguments,null,Expression.OR);
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
		for(int i=arguments.size()-1; i>=0; i--) {
			if(conjunction.isGonnaBeReified() &&
				!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.AND))
				arguments.get(i).willBeFlattenedToVariable(true);
			arguments.add(i, flattenExpression(arguments.remove(i)));
		}
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
		else if(this.targetSolver.supportsConstraint(Expression.NARY_CONJUNCTION)) {
			return conjunction;
		}
		// 2. ----else the solver does not support n-ary conjunction, we have to flatten it to binary
		else {
			return flattenRelationalNaryToBinaryCommutativeExpressions(arguments,null,Expression.AND);
		}

	}
	
	
	private Expression flattenArithmeticNaryToBinaryExpression(ArrayList<Expression> arguments, 
																Expression auxVariable, 
																int operator) 
		throws TailorException {
		
		// if the disjunction/conjunction only has 1 element, it has to hold
		if(arguments.size() == 1 && auxVariable == null)
			return arguments.remove(0);
		
		// get the 2 expressions we are building the conjunction from
		Expression rightExpression = arguments.remove(0);
		Expression leftExpression = null;
		if(auxVariable != null)
			leftExpression = auxVariable;
		else leftExpression = arguments.remove(0);
		
		// the last 2 elements, just return a conjunction of them
		if(arguments.size() == 0) {
			return new CommutativeBinaryRelationalExpression(rightExpression, operator, leftExpression);
		}
		else {// check if this is OK TODO!!
			CommutativeBinaryRelationalExpression binConjunction = new CommutativeBinaryRelationalExpression(leftExpression,
																										operator,
																										rightExpression);
			RelationalAtomExpression auxVariable2 = reifyConstraint(binConjunction);
			return flattenRelationalNaryToBinaryCommutativeExpressions(arguments, auxVariable2, operator);
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
	private Expression flattenRelationalNaryToBinaryCommutativeExpressions(ArrayList<Expression> arguments, Expression reifiedVariable, int operator) 
	throws TailorException {
		
		// if the disjunction/conjunction only has 1 element, it has to hold
		if(arguments.size() == 1 && reifiedVariable == null)
			return arguments.remove(0);
		
		// get the 2 expressions we are building the conjunction from
		Expression rightExpression = arguments.remove(0);
		Expression leftExpression = null;
		if(reifiedVariable != null)
			leftExpression = reifiedVariable;
		else leftExpression = arguments.remove(0);
		
		// the last 2 elements, just return a conjunction of them
		if(arguments.size() == 0) {
			return new CommutativeBinaryRelationalExpression(rightExpression, operator, leftExpression);
		}
		else {
			CommutativeBinaryRelationalExpression binConjunction = new CommutativeBinaryRelationalExpression(leftExpression,
																										operator,
																										rightExpression);
			RelationalAtomExpression auxVariable = reifyConstraint(binConjunction);
			return flattenRelationalNaryToBinaryCommutativeExpressions(arguments, auxVariable, operator);
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
			if(this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.NEGATION))
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
		//System.out.println("flattening non-c.bin.rel.expression:"+expression+" with operator: "+expression.getOperator());
		
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
		
		if(quantification.getType() == Expression.FORALL && 
				this.targetSolver.supportsConstraint(Expression.FORALL))
			return quantification;
		
		if(quantification.getType() == Expression.EXISTS && 
				this.targetSolver.supportsConstraint(Expression.EXISTS))
			return quantification;
		
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
		
		//System.out.println("unfolded quantification to list: "+unfoldedExpressions);
		
		// universal quantification 
		if(quantification.getType() == Expression.FORALL) {
			Conjunction conjunction = new Conjunction(unfoldedExpressions);
			conjunction.reduceExpressionTree();
			if(quantification.isGonnaBeReified()) {
				conjunction.willBeFlattenedToVariable(true);
				for(int i=conjunction.getArguments().size()-1; i>=0; i--) 
					conjunction.getArguments().get(i).willBeFlattenedToVariable(true);
			}
			
			Expression e = flattenConjunction(conjunction);
			//System.out.println("After flattening the resulting conjunction: "+e);
			
			return e;//flattenConjunction(conjunction);
			
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
	 * Flattens commutative binary relational expressions. Equality is a special case 
	 * (helps to detect sums and products) 
	 * 
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenCommutativeBinaryRelationalExpression(CommutativeBinaryRelationalExpression expression )
	throws TailorException {
	
		Expression leftExpression = expression.getLeftArgument();
		Expression rightExpression = expression.getRightArgument();
		
		// EQUALITY: we have to detect products and sums
		if(expression.getOperator() == Expression.EQ) {
			return flattenEquality(expression);
		}
		else {
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
				leftExpression.willBeFlattenedToVariable(true);
				rightExpression.willBeFlattenedToVariable(true);
			}
			
			return flattenInequality(expression);
		}
	}
	
	/**
	 * 
	 * 
	 * @param inequality
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenInequality(CommutativeBinaryRelationalExpression inequality) 
		throws TailorException {
		
		Expression leftExpression = inequality.getLeftArgument();
		Expression rightExpression = inequality.getRightArgument();
		
		// ----- 1. check if the equality represents a sum ------------
		if(leftExpression instanceof QuantifiedSum)
			leftExpression = flattenQuantifiedSum((QuantifiedSum) leftExpression);
		if(rightExpression instanceof QuantifiedSum)
			rightExpression = flattenQuantifiedSum((QuantifiedSum) rightExpression);
		
		boolean leftSideIsSum = (leftExpression instanceof Sum);
		boolean rightSideIsSum = (rightExpression instanceof Sum);
		
		// 1. CASE: both sides represent sums
		if(leftSideIsSum && rightSideIsSum) 
			return flattenEqualityOfTwoSums((Sum) leftExpression, 
					                         inequality.getOperator(),
					                         (Sum) rightExpression, 
					                         inequality.isGonnaBeReified());
		else if(leftSideIsSum || rightSideIsSum) {
			Expression resultExpression = (leftSideIsSum) ? rightExpression : leftExpression;
			Expression sumExpression = (leftSideIsSum) ? leftExpression : rightExpression;
			
			if(this.targetSolver.supportsConstraint(getNaryConstraintVariantOf(inequality.getOperator()))) {
				SumConstraint  sumConstraint = createSumConstraint((Sum) sumExpression,
					                                          inequality.getOperator(), 
					                                          resultExpression, 
					                                          rightSideIsSum); // leftSideIsResult
				
				if(inequality.isGonnaBeReified()) {
					return reifyConstraint(sumConstraint);
				}
				else return sumConstraint;
			}
			else if(this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
				int[] bounds = ((Sum) sumExpression).getDomain();
				Variable auxVariable = createAuxVariable(bounds[0],bounds[1]);
				SumConstraint  sumConstraint = createSumConstraint((Sum) sumExpression,
																	Expression.EQ, 
																	new ArithmeticAtomExpression(auxVariable), 
																	rightSideIsSum); // leftSideIsResult
				this.constraintBuffer.add(sumConstraint);
				
				Expression finalConstraint = (leftSideIsSum) ? 
						new CommutativeBinaryRelationalExpression(new ArithmeticAtomExpression(auxVariable),
						                                                               inequality.getOperator(),
						                                                               resultExpression)
				:
					new CommutativeBinaryRelationalExpression(resultExpression,
															inequality.getOperator(),
															new ArithmeticAtomExpression(auxVariable));
			    if(inequality.isGonnaBeReified())
			    	return reifyConstraint(finalConstraint);
			    else return finalConstraint;
			}
		}
				
		
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(inequality.getOperator())) {
			leftExpression.isGonnaBeReified();
			rightExpression.isGonnaBeReified();
		}
		leftExpression = flattenExpression(leftExpression);
		rightExpression = flattenExpression(rightExpression);
				
		return new CommutativeBinaryRelationalExpression(leftExpression, 
																inequality.getOperator(),
						                                         rightExpression);
	}
	
	/**
	 * Flatten equality: detect product and sum constraints and flatten them to those, 
	 * if they are supported by the target solver.
	 * 
	 * @param equality
	 * @return
	 */
	private Expression flattenEquality(CommutativeBinaryRelationalExpression equality) 
		throws TailorException {
		
		Expression leftExpression = equality.getLeftArgument();
		Expression rightExpression = equality.getRightArgument();
		
		if(this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.EQ)) {
			return new CommutativeBinaryRelationalExpression(flattenExpression(leftExpression),
					                                    Expression.EQ,
					                                    flattenExpression(rightExpression));
		}
		
		// ----- 1. check if the equality represents a sum ------------
		if(leftExpression instanceof QuantifiedSum)
			leftExpression = flattenQuantifiedSum((QuantifiedSum) leftExpression);
		if(rightExpression instanceof QuantifiedSum)
			rightExpression = flattenQuantifiedSum((QuantifiedSum) rightExpression);
		
		boolean leftSideIsSum = (leftExpression instanceof Sum);
		boolean rightSideIsSum = (rightExpression instanceof Sum);
		
		
		if(leftSideIsSum && rightSideIsSum) {
			return flattenEqualityOfTwoSums((Sum) leftExpression, 
					                         Expression.EQ,
					                         (Sum) rightExpression, 
					                         equality.isGonnaBeReified());
			// create 2 sums -> depending on negative arguments and positive arguments
			// either weighted or unweighted version with the same auxVariable as 
			// parameter
			
		}
		else if(leftSideIsSum || rightSideIsSum) {
			 
			Expression resultExpression = (leftSideIsSum) ? rightExpression : leftExpression;
			Expression sumExpression = (leftSideIsSum) ? leftExpression : rightExpression;
	
			SumConstraint sumConstraint = createSumConstraint((Sum) sumExpression,
					                                          Expression.EQ, 
					                                          resultExpression, 
					                                          true);
			
			if(equality.isGonnaBeReified()) {
				return reifyConstraint(sumConstraint);
			}
			else return sumConstraint;
		}
		
		
		
		
		return equality;
	}
	
	
	/**
	 * Flatten the expression when we have a sum that is equal to another sum
	 * 
	 * a + b + c RELOP x + y + z
	 * 
	 * Depending on if one (or both) of the sums is already represented by a common
	 * subexpression, flatten the expression.
	 * 
	 * @param leftSum
	 * @param relationalOperator TODO
	 * @param rightSum
	 * @param hasToBeFlattenedToVariable
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenEqualityOfTwoSums(Sum leftSum, 
			                                    int relationalOperator, 
			                                    Sum rightSum, 
			                                    boolean hasToBeFlattenedToVariable)
		throws TailorException {
		
		
		int lowerBound = min(leftSum.getDomain()[0], rightSum.getDomain()[0]);
		int upperBound = max(leftSum.getDomain()[1], rightSum.getDomain()[1]);
		
	
		
		// 1. CASE: both sums have a common subexpression represented by a variable
		//          so just set both variables equal to another
		if(hasCommonSubExpression(leftSum) && hasCommonSubExpression(rightSum)) {
			Variable leftSumVariable = getCommonSubExpression(leftSum);
			Variable rightSumVariable = getCommonSubExpression(rightSum);
			
			return new CommutativeBinaryRelationalExpression(new ArithmeticAtomExpression(leftSumVariable), 
					                                         relationalOperator, 
					                                         new ArithmeticAtomExpression(rightSumVariable));
		}
		
		// 2. CASE: the left/right expression has a  common subexpression represented by a variable
		//          so set the right/left sum to be equal to that variable
		if(hasCommonSubExpression(leftSum) || hasCommonSubExpression(rightSum)) {
			Variable sumVariable = (hasCommonSubExpression(leftSum)) ?
					     getCommonSubExpression(leftSum) : 
					    	 getCommonSubExpression(rightSum);					     					 			    
					     
			Expression[] positiveArguments = (hasCommonSubExpression(leftSum)) ? 
					(rightSum.getPositiveArguments() != null) ?
				    	(Expression[]) rightSum.getPositiveArguments().toArray() :
				    		new Expression[0]  
				    		               :
                     (leftSum.getPositiveArguments() != null) ?		               
						(Expression[]) leftSum.getPositiveArguments().toArray() :
						 new Expression[0];
						
			Expression[] negativeArguments = (hasCommonSubExpression(leftSum)) ? 
					(rightSum.getNegativeArguments() != null) ?
					    	(Expression[]) rightSum.getNegativeArguments().toArray() :
					    		new Expression[0]  
					    		               :
	                     (leftSum.getNegativeArguments() != null) ?		               
							(Expression[]) leftSum.getNegativeArguments().toArray() :
							 new Expression[0];
			
			// flatten the arguments 
			boolean needsToBeFlattenedToVariable =this.targetSolver.supportsConstraintsNestedAsArgumentOf(getNaryConstraintVariantOf(relationalOperator));	
			
			
			for(int i=0; i<positiveArguments.length; i++){
				if(needsToBeFlattenedToVariable)
					positiveArguments[i].willBeFlattenedToVariable(true);
				positiveArguments[i] = flattenExpression(positiveArguments[i]);
			}
			for(int i=0; i<negativeArguments.length; i++){
				if(needsToBeFlattenedToVariable)
					negativeArguments[i].willBeFlattenedToVariable(true);
				negativeArguments[i] = flattenExpression(negativeArguments[i]);
			}
			
			// create a sum constraint
			SumConstraint sumConstraint = new SumConstraint(positiveArguments,
					                                        negativeArguments,
					                                        relationalOperator, 
					                                        new ArithmeticAtomExpression(sumVariable), 
					                                        hasCommonSubExpression(leftSum)); // is the result on the left side?
			
			if(!this.targetSolver.supportsConstraint(getNaryConstraintVariantOf(relationalOperator))) {
				sumConstraint.setHasToBeBinary(true);
			}
			
			// flatten the sum constraint (necessary to make it binary)
			Expression finalSumConstraint = flattenExpression(sumConstraint);
			
			
			if(hasToBeFlattenedToVariable)
				return reifyConstraint(finalSumConstraint);
			else return finalSumConstraint;
		}
		
		// 3. CASE: no common subexpressions, create 2 sums that are both equal to 
		//          the same auxiliary variable 
		else {
			Variable sumResult = createAuxVariable(lowerBound, upperBound);
	
			// ------------ create first sum -------------------------------------
			// auxVariable = rightSum
			SumConstraint sumConstraint1 = createSumConstraint(rightSum, 
					                                           Expression.EQ, 
					                                           new ArithmeticAtomExpression(sumResult), 
					                                           true);
			
			if(!this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
				sumConstraint1.setHasToBeBinary(true);
			}
			// flatten the sum constraint (necessary to make it binary)
			Expression finalSumConstraint1 = flattenExpression(sumConstraint1);
	
			
			//------------ create second sum -------------------------------------
			// case EQ:       leftSum = auxVariable
			// other RELOP:   leftSum = auxVariable2
			//                auxVariable2 RELOP auxVariable
			
			
			if(relationalOperator == Expression.EQ) {
				SumConstraint sumConstraint2 = createSumConstraint(leftSum, 
															   Expression.EQ,
					                                           new ArithmeticAtomExpression(sumResult), 
					                                           false);
			
				if(!this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
					sumConstraint2.setHasToBeBinary(true);
				}
			// flatten the sum constraint (necessary to make it binary)
				Expression finalSumConstraint2 = flattenExpression(sumConstraint2);
			
			
			
				if(hasToBeFlattenedToVariable) {
					RelationalAtomExpression reifiedRightSum = reifyConstraint(finalSumConstraint1);
					RelationalAtomExpression reifiedLeftSum = reifyConstraint(finalSumConstraint2);
				
					Conjunction reifiedSum = new Conjunction(new Expression[] {reifiedRightSum, reifiedLeftSum});
					return reifyConstraint(reifiedSum);
				}
				else {
					this.constraintBuffer.add(finalSumConstraint1);
					return finalSumConstraint2;
				}
			}
			// other RELOPS:
			else {
				Variable sumResult2 = createAuxVariable(lowerBound, upperBound);
				SumConstraint sumConstraint2 = createSumConstraint(leftSum, 
											   Expression.EQ,
											   new ArithmeticAtomExpression(sumResult2), 
											   false); // resultIsOnLeftSide
				if(!this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
					sumConstraint2.setHasToBeBinary(true);
				}
			// flatten the sum constraint (necessary to make it binary)
				Expression finalSumConstraint2 = flattenExpression(sumConstraint2);
				this.constraintBuffer.add(finalSumConstraint2);
				
				Expression finalExpression = new CommutativeBinaryRelationalExpression(new RelationalAtomExpression(sumResult),
																						relationalOperator,
																						new RelationalAtomExpression(sumResult2));
				if(hasToBeFlattenedToVariable) {
					return reifyConstraint(finalExpression);
				}
				else {
					return finalExpression;
				}
				
			}
		}
		
	}
	
	private int getNaryConstraintVariantOf(int relop) 
		throws TailorException {
		
		switch(relop) {
		
		case Expression.EQ:
			return Expression.NARY_SUMEQ_CONSTRAINT;	
			
		case Expression.NEQ:
			return Expression.NARY_SUMNEQ_CONSTRAINT;	
			
		case Expression.LEQ:
			return Expression.NARY_SUMLEQ_CONSTRAINT;	
			
		case Expression.GEQ:
			return Expression.NARY_SUMGEQ_CONSTRAINT;	
			
		case Expression.LESS:
			return Expression.NARY_SUMLESS_CONSTRAINT;	
			
		case Expression.GREATER:
			return Expression.NARY_SUMGREATER_CONSTRAINT;	
			
		case Expression.AND:
			return Expression.AND;
			
		}
		
		throw new TailorException("Cannot find n-ary constraint version of relational operator:"+relop);
	}
	
	/**
	 * Creates a sum constraint, fiven a Sum expression and a result. Both subexpressions
	 * are flattened before creating the sum.
	 * 
	 * @param sum
	 * @param relationalOperator TODO
	 * @param sumResult
	 * @param resultIsOnLeftSide TODO
	 * @return
	 * @throws TailorException
	 */
	private SumConstraint createSumConstraint(Sum sum, 
											  int relationalOperator, 
											  Expression sumResult, 
											  boolean resultIsOnLeftSide)
		throws TailorException {
		
		Expression[] positiveArguments = new Expression[0];
		Expression[] negativeArguments = new Expression[0];
		
		if(sum.getPositiveArguments() != null) {
			positiveArguments = new Expression[sum.getPositiveArguments().size()];
			for(int i=0; i<sum.getPositiveArguments().size(); i++) {
				positiveArguments[i] = sum.getPositiveArguments().get(i);
			}
			
		}
		if(sum.getNegativeArguments() != null) {
			negativeArguments = new Expression[sum.getNegativeArguments().size()];
			for(int i=0; i<sum.getNegativeArguments().size(); i++) {
				negativeArguments[i] = sum.getNegativeArguments().get(i);
			}
		}				
		
		// flatten the arguments 
		boolean needsToBeFlattenedToVariable = this.targetSolver.supportsConstraintsNestedAsArgumentOf(relationalOperator);	
		for(int i=0; i<positiveArguments.length; i++){
			if(needsToBeFlattenedToVariable)
				positiveArguments[i].willBeFlattenedToVariable(true);
			positiveArguments[i] = flattenExpression(positiveArguments[i]);
		}
		for(int i=0; i<negativeArguments.length; i++){
			if(needsToBeFlattenedToVariable)
				negativeArguments[i].willBeFlattenedToVariable(true);
			negativeArguments[i] = flattenExpression(negativeArguments[i]);
		}		
		
		if(needsToBeFlattenedToVariable)
			sumResult.willBeFlattenedToVariable(true);
		sumResult = flattenExpression(sumResult);
		
		// create a sum constraint
		return  new SumConstraint(positiveArguments,
				                  negativeArguments,
				                  relationalOperator, 
				                  sumResult, 
				                  resultIsOnLeftSide);		
	
	}
	
	/**
	 * 
	 * @param expression
	 * @return
	 */
	private Expression flattenArithmeticExpression(ArithmeticExpression expression) 
		throws TailorException {
		
		if(expression instanceof QuantifiedSum)
			return flattenQuantifiedSum((QuantifiedSum) expression);
		
		if(expression instanceof ArithmeticAtomExpression)
			return flattenArithmeticAtomExpression((ArithmeticAtomExpression) expression);
			
		if(expression instanceof UnaryArithmeticExpression)
			return flattenUnaryArithmeticExpression((UnaryArithmeticExpression) expression);
		
		if(expression instanceof Sum)
			return flattenSum((Sum) expression);
		
		return expression;
	}
	
	
	private Expression flattenSum(Sum sum) 
		throws TailorException {
		
		if(this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
			ArrayList<Expression> negativeArguments = sum.getNegativeArguments();
			for(int i=0;i<negativeArguments.size(); i++)
				negativeArguments.add(i, flattenExpression(negativeArguments.remove(i)));
				
			ArrayList<Expression> positiveArguments = sum.getPositiveArguments();
			for(int i=0;i<positiveArguments.size(); i++)
				positiveArguments.add(i, flattenExpression(positiveArguments.remove(i)));
			
			if(sum.isGonnaBeReified()) 
				return reifyConstraint(sum);
			else return sum;
		}
		else {
			Expression plusConstraint = flattenRelationalNaryToBinaryCommutativeExpressions(sum.getPositiveArguments(),
																		null,
																		Expression.PLUS);
			Expression finalConstraint = flattenRelationalNaryToBinaryCommutativeExpressions(sum.getNegativeArguments(),
					 													plusConstraint,
					 													Expression.MINUS);
			
		}
		
		return sum;
	}
	
	
	
	/**
	 * Flatten a unary expression
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenUnaryArithmeticExpression(UnaryArithmeticExpression expression) 
		throws TailorException {
		
		Expression argument = expression.getArgument();
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getType()))
			argument.willBeFlattenedToVariable(true);
		
		argument = flattenExpression(argument);
		
		if(expression.isGonnaBeReified())
			return reifyConstraint(expression);
		
		else return expression;
	}
	
	/**
	 * If the expression is indexed by a variable, and variable indexing is 
	 * not supported in the target solver, then transform it into an element
	 * constraint
	 * 
	 * @param atom
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenArithmeticAtomExpression(ArithmeticAtomExpression atom) 
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
	
	// ========================= GENERAL HELPER METHODS ===========================
	
	
	private Variable getCommonSubExpression(Expression expression) {
		return this.subExpressions.get(expression);
	}
	
	private boolean hasCommonSubExpression(Expression expression) {
		return this.subExpressions.containsKey(expression);
	}
	
	
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
	 * Reifies the constraint with the reified variable, both given as 
	 * arguments. The reified Constraint is added to the list of 
	 * constraints and the reified constraint is added to the list
	 * of subexpressions. If the constraint has a common subexpression,
	 * it will not be detected, since the method only reifies the 
	 * expression with the reified variable given as argument!!
	 * 
	 * @param constraint
	 * @param reifiedVariable
	 * @throws TailorException
	 */
	private void reifyConstraint(Expression constraint, Variable reifiedVariable) 
		throws TailorException {
		
		if(!this.targetSolver.supportsReificationOf(constraint.getType()))
			throw new TailorException
			("Cannot reify constraint because its reification is not supported by the target solver:"+constraint);
		
		this.subExpressions.put(constraint,reifiedVariable);
		
		this.constraintBuffer.add(new Reification(constraint,
                				  reifiedVariable));
		
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
	
	
	private int max(int value1, int value2) {
		
		return (value1 < value2) ?
				value2 : value1;
	}
	
	private int min(int value1, int value2) {
		
		return (value1 < value2) ?
				value1 : value2;
	}
}
