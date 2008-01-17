package translator.tailor;

import translator.solver.*;
import java.util.ArrayList;
import java.util.HashMap;
import translator.expression.*;
import translator.normaliser.NormalisedModel;


public class Flattener {

	public final String AUXVARIABLE_NAME = "aux";
	protected int noAuxVariables;
	
	/** the target solver we want to tailor the model to */
	TargetSolver targetSolver;
	/** the list of all flattened constraints. constraints are added on the fly. */
	ArrayList<Expression> constraintBuffer;
	
	/** contains every (flattened) subexpression and it's corresponding variable*/
	HashMap<String,ArithmeticAtomExpression> subExpressions;
	//HashMap<Expression,ArithmeticAtomExpression> subExpressions;
	
	/** the normalised model contains the list of expression variables etc */
	NormalisedModel normalisedModel;
	
	
	int usedCommonSubExpressions;
	
	// ========== CONSTRUCTOR ============================
	
	public Flattener(TargetSolver targetSolver,
			         NormalisedModel normalisedModel) {
		this.targetSolver = targetSolver;	
		this.normalisedModel = normalisedModel;
		this.constraintBuffer = new ArrayList<Expression>();
		this.subExpressions = new HashMap<String,ArithmeticAtomExpression>();
		this.noAuxVariables = 0;
		this.usedCommonSubExpressions = 0;
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
		
		flattenObjective();
		
		for(int i=0; i<constraints.size(); i++) {
			ArrayList<Expression> flatExpression = flattenConstraint(constraints.get(i));
			for(int j=flatExpression.size()-1; j>=0; j--) {
				Expression constraint = flatExpression.remove(j);
				//constraint = constraint.evaluate();
				// detect and remove pure TRUE statements
				if(constraint.getType() == Expression.BOOL) {
					if(!((RelationalAtomExpression) constraint).getBool())
						flattenedConstraints.add(constraint);
				}
				else flattenedConstraints.add(constraint);
			}
		}
		
		this.normalisedModel.setAmountOfCommonSubExpressionsUsed(this.usedCommonSubExpressions);
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
		
		else if(expression instanceof Array)
			return expression;  // TODO: here we will need to flatten in case the target solver does not support arrays
		
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
	 * Flatten binary relational expressions. detects sums and product constraints. Just the same as 
	 * with commutative binary expressions
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenNonCommutativeRelationalBinaryExpression(NonCommutativeRelationalBinaryExpression expression) 
	throws TailorException {

	
	Expression leftExpression = expression.getLeftArgument();
	Expression rightExpression = expression.getRightArgument();
	
	// ----------- if the target solver allows nested constraints, then return them as they are --------- 
	if(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
		leftExpression = flattenExpression(leftExpression);
		rightExpression = flattenExpression(rightExpression);
		return new NonCommutativeRelationalBinaryExpression(leftExpression,
			                                            expression.getOperator(),
			                                            rightExpression);
	}
	
	
	
	// ------- target solver does NOT allow nesting of constraints --------------------------------------
	
	if(leftExpression instanceof QuantifiedSum ||
			leftExpression instanceof QuantifiedExpression) {
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) 
			leftExpression.willBeFlattenedToVariable(true);
		
		
		leftExpression = flattenExpression(leftExpression);
	}
		
	if(rightExpression instanceof QuantifiedSum ||
			rightExpression instanceof QuantifiedExpression) {
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) 
			rightExpression.willBeFlattenedToVariable(true);
		
		
		rightExpression = flattenExpression(rightExpression);
	}
	
	
	
	// ------- 1. detect sum expressions (we can only have 1 sum expression on one side due to normalisation)-------
	if(leftExpression instanceof Sum) {
		Sum leftSum = (Sum) leftExpression;
		Sum processedSum = (Sum) leftSum.copy();
		processedSum.setWillBeConvertedToSumConstraint(true);
		SumConstraint partWiseSumConstraint = (SumConstraint) flattenSum(processedSum);
		
		rightExpression.willBeFlattenedToVariable(true);
		Expression result = flattenExpression(rightExpression);
		partWiseSumConstraint.setResult(result, 
				                        expression.getOperator(), 
				                        false); // result Is on left side
		
		addToSubExpressions(leftSum,result);
		
		if(expression.isGonnaBeFlattenedToVariable()) {
			return reifyConstraint(partWiseSumConstraint);
		}
		else return partWiseSumConstraint;
	}
	else if(rightExpression instanceof Sum) {
		Sum rightSum = (Sum) rightExpression;
		Sum processedSum = (Sum) rightSum.copy();
		processedSum.setWillBeConvertedToSumConstraint(true);
		SumConstraint partWiseSumConstraint = (SumConstraint) flattenSum(processedSum);
		
		leftExpression.willBeFlattenedToVariable(true);
		Expression result = flattenExpression(leftExpression);
		partWiseSumConstraint.setResult(result, 
				                        expression.getOperator(), 
				                        true); // result Is on left side
		
		addToSubExpressions(rightSum,result);
		
		if(expression.isGonnaBeFlattenedToVariable()) {
			return reifyConstraint(partWiseSumConstraint);
		}
		else return partWiseSumConstraint;
	}
	
	
	
	// -------------- 2. detect product constraints --------------------------------------
	// a*b RELOP c    ====>  a*b = t,      t RELOP c
	if(leftExpression instanceof Multiplication) {
		// flatten multiplication to partwise product constraint
		Multiplication multiplication = (Multiplication) leftExpression;
		Multiplication processedMultiplication = (Multiplication) multiplication.copy();
		processedMultiplication.setWillBeConverteredToProductConstraint(true);
		ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(processedMultiplication);
		
		ArithmeticAtomExpression auxVar = null;
		
		// check for common subexpressions
		if(hasCommonSubExpression(multiplication)) 
			auxVar = getCommonSubExpression(multiplication);
		else {
			auxVar = new ArithmeticAtomExpression(createAuxVariable(multiplication.getDomain()[0], 
					                                                multiplication.getDomain()[1]));
			addToSubExpressions(multiplication, auxVar);
			productConstraint.setResult(auxVar);
			this.constraintBuffer.add(productConstraint);
		}
		
		// aux = Multiplication
	
		
		rightExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
		rightExpression = flattenExpression(rightExpression);
		
		if(expression.isGonnaBeFlattenedToVariable())
			return reifyConstraint(new NonCommutativeRelationalBinaryExpression(auxVar,
																				expression.getOperator(),
					                                                           rightExpression));
		else return new NonCommutativeRelationalBinaryExpression(auxVar,
				expression.getOperator(),
                rightExpression);
	}
	else if(rightExpression instanceof Multiplication) {
		// flatten multiplication to partwise product constraint
		Multiplication multiplication = (Multiplication) rightExpression;
		Multiplication processedMultiplication = (Multiplication) multiplication.copy();
		processedMultiplication.setWillBeConverteredToProductConstraint(true);
		ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(processedMultiplication);
		
		ArithmeticAtomExpression auxVar = null;
		
		// check for common subexpressions
		if(hasCommonSubExpression(multiplication)) 
			auxVar = getCommonSubExpression(multiplication);
		else {
			auxVar = new ArithmeticAtomExpression(createAuxVariable(multiplication.getDomain()[0], 
					                                                multiplication.getDomain()[1]));
			addToSubExpressions(multiplication, auxVar);
		}
		
		// aux = Multiplication
		productConstraint.setResult(auxVar);
		this.constraintBuffer.add(productConstraint);
		
		leftExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
		leftExpression = flattenExpression(leftExpression);
		
		if(expression.isGonnaBeFlattenedToVariable())
			return reifyConstraint(new NonCommutativeRelationalBinaryExpression(leftExpression,
																				expression.getOperator(),
					                                                           auxVar));
		else return new NonCommutativeRelationalBinaryExpression(leftExpression,
																expression.getOperator(),
																auxVar);
	}
	
	//3. just flatten the stuff dependeing on the implementation of the corresponding constraint
	//    in the target solver
	

	leftExpression.willBeFlattenedToVariable(true);
	rightExpression.willBeFlattenedToVariable(true);
	
	
	leftExpression = flattenExpression(leftExpression);
	rightExpression = flattenExpression(rightExpression);
	
	
	if(expression.isGonnaBeFlattenedToVariable()) {
		return reifyConstraint(new NonCommutativeRelationalBinaryExpression(leftExpression,
			                                            expression.getOperator(),
			                                            rightExpression));
	}
	return new NonCommutativeRelationalBinaryExpression(leftExpression,
			                                            expression.getOperator(),
			                                            rightExpression);
	
}
	
	
	/**
	 * Flatten binary relational expressions. detects sums and product constraints. Just the same as 
	 * with non-commutative binary expressions
	 * 
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenCommutativeBinaryRelationalExpression(CommutativeBinaryRelationalExpression expression) 
		throws TailorException {
	
		
		Expression leftExpression = expression.getLeftArgument();
		Expression rightExpression = expression.getRightArgument();
		
		//System.out.println("We have an equality between (before qsum detection): "+leftExpression+" == "+rightExpression);
		
		if(leftExpression instanceof QuantifiedSum ||
				leftExpression instanceof QuantifiedExpression) {
			if(expression.isGonnaBeFlattenedToVariable()) 
				leftExpression.willBeFlattenedToVariable(true);
			leftExpression = flattenExpression(leftExpression);
		}
		
		if(rightExpression instanceof QuantifiedSum ||
				rightExpression instanceof QuantifiedExpression) {
			if(expression.isGonnaBeFlattenedToVariable()) 
				rightExpression.willBeFlattenedToVariable(true);
			rightExpression = flattenExpression(rightExpression);
		}
		
		//System.out.println("We have an equality between(after qsum): "+leftExpression+" == "+rightExpression);
		
		// 1. detect sum expressions (we can only have 1 sum expression on one side due to normalisation)
		if(leftExpression instanceof Sum) {
			leftExpression.reduceExpressionTree();
			Sum leftSum = (Sum) leftExpression;
			leftSum.setWillBeConvertedToSumConstraint(true);
			SumConstraint partWiseSumConstraint = (SumConstraint) flattenSum(leftSum);
			
			rightExpression.willBeFlattenedToVariable(true);
			Expression result = flattenExpression(rightExpression);
			partWiseSumConstraint.setResult(result, 
					                        expression.getOperator(), 
					                        false); // result Is on left side
			
			addToSubExpressions(leftSum,result);
			

			if(expression.isGonnaBeFlattenedToVariable()) {
				return reifyConstraint(partWiseSumConstraint);
			}
			else return partWiseSumConstraint;
		}
		else if(rightExpression instanceof Sum) {
			//System.out.println("We have a sum on the right hand side\n: "+rightExpression);
			rightExpression.reduceExpressionTree();
			Sum rightSum = (Sum) rightExpression;
			rightSum.setWillBeConvertedToSumConstraint(true);
			SumConstraint partWiseSumConstraint = (SumConstraint) flattenSum(rightSum);
			
			leftExpression.willBeFlattenedToVariable(true);
			Expression result = flattenExpression(leftExpression);
			partWiseSumConstraint.setResult(result, 
					                        expression.getOperator(), 
					                        true); // result Is on left side
			
			addToSubExpressions(rightSum,result);
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				return reifyConstraint(partWiseSumConstraint);
			}
			else return partWiseSumConstraint;
		}
	
		
		if(leftExpression instanceof Multiplication) {
			// flatten multiplication to partwise product constraint
			Multiplication multiplication = (Multiplication) leftExpression;
			Multiplication processedMultiplication = (Multiplication) multiplication.copy();
			processedMultiplication.setWillBeConverteredToProductConstraint(true);
			ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(processedMultiplication);
			
			ArithmeticAtomExpression auxVar = null;
			
			// check for common subexpressions
			if(hasCommonSubExpression(multiplication)) 
				auxVar = getCommonSubExpression(multiplication);
			else {
				auxVar = new ArithmeticAtomExpression(createAuxVariable(multiplication.getDomain()[0], 
						                                                multiplication.getDomain()[1]));
				addToSubExpressions(multiplication, auxVar);
				productConstraint.setResult(auxVar);
				this.constraintBuffer.add(productConstraint);
			}
			
			// aux = Multiplication
		
			
			rightExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
			rightExpression = flattenExpression(rightExpression);
			
			if(expression.isGonnaBeFlattenedToVariable())
				return reifyConstraint(new CommutativeBinaryRelationalExpression(auxVar,
																					expression.getOperator(),
						                                                           rightExpression));
			else return new CommutativeBinaryRelationalExpression(auxVar,
					expression.getOperator(),
	                rightExpression);
		}
		else if(rightExpression instanceof Multiplication) {
			// flatten multiplication to partwise product constraint
			Multiplication multiplication = (Multiplication) rightExpression;
			Multiplication processedMultiplication = (Multiplication) multiplication.copy();
			processedMultiplication.setWillBeConverteredToProductConstraint(true);
			ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(processedMultiplication);
			
			ArithmeticAtomExpression auxVar = null;
			
			// check for common subexpressions
			if(hasCommonSubExpression(multiplication)) 
				auxVar = getCommonSubExpression(multiplication);
			else {
				auxVar = new ArithmeticAtomExpression(createAuxVariable(multiplication.getDomain()[0], 
						                                                multiplication.getDomain()[1]));
				addToSubExpressions(multiplication, auxVar);
			}
			
			// aux = Multiplication
			productConstraint.setResult(auxVar);
			this.constraintBuffer.add(productConstraint);
			
			leftExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
			leftExpression = flattenExpression(leftExpression);
			
			if(expression.isGonnaBeFlattenedToVariable())
				return reifyConstraint(new CommutativeBinaryRelationalExpression(leftExpression,
																					expression.getOperator(),
						                                                           auxVar));
			else return new CommutativeBinaryRelationalExpression(leftExpression,
																	expression.getOperator(),
																	auxVar);
		}
		
	
		
		// 2. detect product constraints
		if(leftExpression instanceof Multiplication) {
			Multiplication multiplication = (Multiplication) leftExpression;
			multiplication.setWillBeConverteredToProductConstraint(true);
			ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(multiplication);
			
			rightExpression.willBeFlattenedToVariable(true);
			productConstraint.setResult(rightExpression);
			if(expression.isGonnaBeFlattenedToVariable())
				return reifyConstraint(productConstraint);
			else return productConstraint;
		}
		else if(rightExpression instanceof Multiplication) {
			Multiplication multiplication = (Multiplication) rightExpression;
			multiplication.setWillBeConverteredToProductConstraint(true);
			ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(multiplication);
			
			leftExpression.willBeFlattenedToVariable(true);
			productConstraint.setResult(leftExpression);
			if(expression.isGonnaBeFlattenedToVariable())
				return reifyConstraint(productConstraint);
			else return productConstraint;
		}
		
		
		//3. just flatten the stuff dependeing on the implementation of the corresponding constraint
		//    in the target solver
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
			leftExpression.willBeFlattenedToVariable(true);
			rightExpression.willBeFlattenedToVariable(true);
		}
		leftExpression = flattenExpression(leftExpression);
		rightExpression = flattenExpression(rightExpression);
		
		if(expression.isGonnaBeFlattenedToVariable()) {
			return reifyConstraint(new CommutativeBinaryRelationalExpression(leftExpression, 
				                                         expression.getOperator(),
				                                         rightExpression));
		}
		
		else return new CommutativeBinaryRelationalExpression(leftExpression, 
				                                         expression.getOperator(),
				                                         rightExpression);
		
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
		
		//System.out.println("Flattening quantified sum:"+quantifiedSum);
		
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
		
	
		//System.out.println("Flattened quantified sum to:"+flattenedSum);
		
		flattenedSum.orderExpression();
		Expression reducedSum  = flattenedSum.evaluate();
		//System.out.println("After evaluating the flattened quantified sum to:"+reducedSum+" amd the expression has type:"+reducedSum.getType());
		reducedSum = reducedSum.reduceExpressionTree();
		
		//System.out.println("After reducing the flattened quantified sum to:"+reducedSum);
		
		/*if(quantifiedSum.isGonnaBeFlattenedToVariable()) {
			if(reducedSum instanceof RelationalAtomExpression ||
					reducedSum instanceof ArithmeticAtomExpression)
				return reducedSum;
			else return reifyConstraint(reducedSum);
		}
		
		return reducedSum;*/
		
		if(reducedSum instanceof Sum)
			return flattenSum((Sum) reducedSum);
		else return reducedSum;
		
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
		if(elementConstraint.isGonnaBeFlattenedToVariable()) {
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
			if(disjunction.isGonnaBeFlattenedToVariable())
				return reifyConstraint(disjunction);
			else return disjunction;
		}
		// 2. --- else the solver does not support n-ary disjunction, we have to flatten it to binary
		else {
			if(disjunction.isGonnaBeFlattenedToVariable()) {
				return reifyConstraint(flattenRelationalNaryToBinaryCommutativeExpressions(arguments,null,Expression.OR));
			}
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
			if(conjunction.isGonnaBeFlattenedToVariable() &&
				!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.AND))
				arguments.get(i).willBeFlattenedToVariable(true);
			Expression argument = flattenExpression(arguments.remove(i)).evaluate();
			if(argument.getType() == Expression.BOOL) {
				if(!((RelationalAtomExpression) argument).getBool())
					return new RelationalAtomExpression(false);
			}
			else arguments.add(i, argument);
		}
		
		
		// 2. ---- if the conjunction is not nested/will be reified
		//         then split it into independent constraints
		if(!conjunction.isGonnaBeFlattenedToVariable()) {
			for(int i=arguments.size()-1; i>0; i--) {
				this.constraintBuffer.add(arguments.remove(i));
			}
			return arguments.remove(0);
		}
		
		// 2. --- if the conjunction is nested, then just return it if n-ary conjunction is 
		//        is supported by the target solver
		else {
			if(this.targetSolver.supportsConstraint(Expression.NARY_CONJUNCTION)) {
				return reifyConstraint(conjunction);
			}
		// 2. ----else the solver does not support n-ary conjunction, we have to flatten it to binary
			else {
				return reifyConstraint(flattenRelationalNaryToBinaryCommutativeExpressions(arguments,null,Expression.AND));
				
			}

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
			return new CommutativeBinaryRelationalExpression(leftExpression, operator, rightExpression);
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
			if(!(argument instanceof Array)) 
				throw new TailorException("Internal error: flattening of argument of alldifferent, '"+argument+
						"' did not result in an array.");
				
			
			
			if(expression.isGonnaBeFlattenedToVariable()) {
                return reifyConstraint(new AllDifferent((Array) argument));	
			}
			else return new AllDifferent((Array) argument);
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
							if(atom.isGonnaBeFlattenedToVariable()) {
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
	 * 
	 * @param quantification
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenQuantifiedExpression(QuantifiedExpression quantification)
		throws TailorException {
		
		
		if(quantification.getType() == Expression.FORALL && 
				this.targetSolver.supportsConstraint(Expression.FORALL) &&
				          !quantification.isGonnaBeFlattenedToVariable())
			return quantification;
		
		if(quantification.getType() == Expression.EXISTS && 
				this.targetSolver.supportsConstraint(Expression.EXISTS) &&
				          !quantification.isGonnaBeFlattenedToVariable())
			return quantification;
		
		
		//System.out.println("Gonna flatten the quantification:"+quantification);
		
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
		
		
		// universal quantification 
		if(quantification.getType() == Expression.FORALL) {
			Conjunction conjunction = new Conjunction(unfoldedExpressions);
			conjunction.reduceExpressionTree();
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.AND)
					&& quantification.isGonnaBeFlattenedToVariable()) {
				for(int i=conjunction.getArguments().size()-1; i>=0; i--) 
					conjunction.getArguments().get(i).willBeFlattenedToVariable(true);
			}
			
			if(quantification.isGonnaBeFlattenedToVariable())
				conjunction.willBeFlattenedToVariable(true);
			
			Expression e = flattenConjunction(conjunction);
			//System.out.println("After flattening the resulting conjunction: "+e);
			
			return e;//flattenConjunction(conjunction);
			
		}
		// existential quantification
		else {
			Disjunction disjunction = new Disjunction(unfoldedExpressions);
			disjunction.reduceExpressionTree();
			
			if(quantification.isGonnaBeFlattenedToVariable())
				disjunction.willBeFlattenedToVariable(true);
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.OR)
					&& quantification.isGonnaBeFlattenedToVariable())
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
				//System.out.println("Generated  expression:"+unfoldedExpression);
				//System.out.println("Inserted '"+values[i]+"' for variable '"+variableName+"' in expression '"+expression+"' and got expression:"+unfoldedExpression);
				unfoldedExpression = unfoldedExpression.evaluate();
				unfoldedExpression = unfoldedExpression.reduceExpressionTree();
				//System.out.println("Generated evaluated, reduced expression:"+unfoldedExpression);
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
	 * 
	 * @return the int that stands for the n-ary constraint variant of the 
	 * operation given as parameter
	 */
	
	/*private int getNaryConstraintVariantOf(int relop) 
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
	*/

	
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
		
		if(expression instanceof Multiplication)
			return flattenMultiplication((Multiplication) expression);
		
		return expression;
	}
	
	
	
	/**
	 * Flattens a multiplication, similar to the flattening of a sum.
	 * 
	 * @param multiplication
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenMultiplication(Multiplication multiplication) 
		throws TailorException {
		
		ArrayList<Expression> arguments= multiplication.getArguments();
		
		if(multiplication.willBeConvertedToProductConstraint()) {
			//System.out.println("Multiplication will be flattened to a product constraint:"+multiplication);
			return flattenToPartWiseProductConstraint(multiplication);
		}
		
		if(this.targetSolver.supportsConstraint(Expression.NARY_PRODUCT_CONSTRAINT)) {
			for(int i=0; i<arguments.size(); i++) {
				Expression argument = arguments.remove(i);
				if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.NARY_PRODUCT_CONSTRAINT)) {
					argument.willBeFlattenedToVariable(true);
				}
				arguments.add(i, flattenExpression(argument));
				
			}
			
			if(multiplication.isGonnaBeFlattenedToVariable())
				return reifyConstraint(multiplication);
			else return multiplication;
		}
		// we have to flatten the multiplication to a binary multiplication
		else {
			
			if(multiplication.isGonnaBeFlattenedToVariable()) {
				Multiplication binaryMultiplication = flattenToBinaryMultiplication(((Multiplication) multiplication.copy()).getArguments(), null);
				
				ArithmeticAtomExpression auxVariable = null;
				
				if(hasCommonSubExpression(binaryMultiplication)) {
					auxVariable = getCommonSubExpression(binaryMultiplication);
				}
				else {
					auxVariable = new ArithmeticAtomExpression(
														 createAuxVariable(multiplication.getDomain()[0], multiplication.getDomain()[1])
														 );
					this.addToSubExpressions(binaryMultiplication, auxVariable);
				}
				ProductConstraint productConstraint = new ProductConstraint(new Expression[] {binaryMultiplication.getArguments().get(0),
																								binaryMultiplication.getArguments().get(1)}, 
																								auxVariable);
				this.constraintBuffer.add(productConstraint);
				return auxVariable;
				
			}
			return flattenToBinaryMultiplication(multiplication.getArguments(), null);
			
		}
		
	}
	
	
	
	
	/**
	 * Flattens an n-ary multiplication to a multiplication with only 2 arguments.
	 * The method works recursively, so for calling it, set the second parameter to null.
	 * 
	 * @param arguments
	 * @param argument
	 * @return
	 * @throws TailorException
	 */
	private Multiplication flattenToBinaryMultiplication(ArrayList<Expression> arguments, 
																		   Expression argument) 
	throws TailorException {

		
		
		if(arguments.size() == 2 && argument == null) {
			Expression arg1 = arguments.remove(0);
			Expression arg2 = arguments.remove(0);
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.BINARY_PRODUCT_CONSTRAINT)) {
				arg1.willBeFlattenedToVariable(true);
				arg2.willBeFlattenedToVariable(true);
			}
			return new Multiplication(new Expression[] {flattenExpression(arg1), 
					                                    flattenExpression(arg2)});
		
		}
		else if (arguments.size() < 2 && argument == null) 
			throw new TailorException("Internal error: Cannot flatten product to binary sum with less than 2 arguments:"+arguments.toString()); 	

		else {
			Expression arg1 = null;
			if(argument == null)
				arg1 = arguments.remove(0);
			else arg1 = argument;

			Expression arg2 = arguments.remove(0);
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.BINARY_PRODUCT_CONSTRAINT)) {
				arg1.willBeFlattenedToVariable(true);
				arg2.willBeFlattenedToVariable(true);
			}
			arg1 = flattenExpression(arg1);
			arg2 = flattenExpression(arg2);
			
			if(arguments.size() ==0) {
				return new Multiplication(new Expression[] {arg1, arg2});
			}
			
			ProductConstraint productConstraint = new ProductConstraint(new Expression[] {arg1, arg2});
				
			
			Multiplication m = new Multiplication(new Expression[] {arg1, arg2} );
			int lb = m.getDomain()[0];
			int ub = m.getDomain()[1];
			ArithmeticAtomExpression auxVariable = new ArithmeticAtomExpression(createAuxVariable(lb,ub));

			productConstraint.setResult(auxVariable);
			this.constraintBuffer.add(productConstraint);
			
			return flattenToBinaryMultiplication(arguments, auxVariable);
		}
}

	
	/**
	 * This method does NOT perform reification!! It returns a product constraint without result
	 * 
	 * @param multiplication
	 * @return
	 * @throws TailorException
	 */
	private ProductConstraint flattenToPartWiseProductConstraint(Multiplication multiplication) 
		throws TailorException  {
		
		ArrayList<Expression> arguments = multiplication.getArguments();
		
		if(this.targetSolver.supportsConstraint(Expression.NARY_PRODUCT_CONSTRAINT)) {
			for(int i=0; i<arguments.size(); i++) {
				if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.NARY_PRODUCT_CONSTRAINT))
					arguments.get(i).willBeFlattenedToVariable(true);
				arguments.add(i, flattenExpression(arguments.remove(i)));
			}
			return new ProductConstraint(arguments);
		}
		else {
			Multiplication binaryMultiplication = flattenToBinaryMultiplication(multiplication.getArguments(), null);
			
			return new ProductConstraint(new Expression[] {binaryMultiplication.getArguments().remove(0),
				                                                     	binaryMultiplication.getArguments().remove(0)});
			
		}
	}
	
	
	/**
	 * Flatten a Sum of expressions, for instance 'a+b+c+d', depending on their context: 
	 * 
	 * 1. if it is part of a relation, i.e. a sum constraint, such as <br>
	 * 'a+b+c+d = 10'<br>
	 * then return a partwise sumConstraint, without specifying the result (which is
	 * one layer/branch above)<br>
	 * <br>
	 * 
	 * 2. if it is nested and nesting is not allowed, such as in<br>
	 * '(a+b+c+d)*x'<br>
	 * where the expression is flattened to a sumconstraint using an auxiliary
	 * variable 'a+b+c+d=t' and the whole expression is replaced by its result, 
	 * i.e. the auxiliary variable which is returned. (so we would get 't*x')
	 * 
	 * 
	 * 3. if the sum is allowed in the context of the constraint it appears, we
	 * just recursively flatten the arguments and return it. 
	 * 
	 * @param sum
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenSum(Sum sum) 
		throws TailorException {
	
		//System.out.println("Flattening a sum:"+sum);
		
		// if there are any quantified sums as arguments, flatten them immediately
		sum = flattenQuantifiedSumArguments(sum);
		sum.reduceExpressionTree();
		
		// convert to a sum constraint
		if(sum.willBeConvertedToASumConstraint()) {		
			return flattenSumPartToSumConstraint(sum);
		}
		// treat it as a normal sum -> might be flattened to a variable
		else {
			if(!sum.isGonnaBeFlattenedToVariable()) {
				if(sum.getPositiveArguments() != null)
					for(int i=0; i<sum.getPositiveArguments().size(); i++) {
						sum.getPositiveArguments().add(i,flattenExpression(sum.getPositiveArguments().remove(i)));
					}
				if(sum.getPositiveArguments() != null)
					for(int i=0; i<sum.getNegativeArguments().size(); i++) {
						sum.getNegativeArguments().add(i,flattenExpression(sum.getNegativeArguments().remove(i)));
					}
				return sum;
			}
			// we will have to flatten the sum to a sumConstraint anyway
			else {
				// if it has a common subexpression
				if(hasCommonSubExpression(sum)) {
					return getCommonSubExpression(sum);
				}
				
				
				SumConstraint sumConstraint = flattenSumPartToSumConstraint((Sum) sum.copy());
				if(sumConstraint.hasResult()) 
					throw new TailorException("Internal error: expect half empty sum constraint instead of:"+sumConstraint);
				
				int lb = sumConstraint.getSumDomain()[0];
				int ub = sumConstraint.getSumDomain()[1];
				
				ArithmeticAtomExpression auxVariable = new ArithmeticAtomExpression(createAuxVariable(lb,ub));
				sumConstraint.setResult(auxVariable, 
						                Expression.EQ, 
						                true);
				this.constraintBuffer.add(sumConstraint);
                addToSubExpressions(sum,auxVariable);
				return auxVariable;
			}
			
			
		}
	}
	
	private Sum flattenQuantifiedSumArguments(Sum sum) 
		throws TailorException {
		
		for(int i=0; i<sum.getNegativeArguments().size(); i++) {
			if(sum.getNegativeArguments().get(i) instanceof QuantifiedSum) {
				Expression e = sum.getNegativeArguments().remove(i);
				e = flattenQuantifiedSum((QuantifiedSum) e);
				sum.getNegativeArguments().add(i,e);
			}
				
		}
		
		for(int i=0; i<sum.getPositiveArguments().size(); i++) {
			if(sum.getPositiveArguments().get(i) instanceof QuantifiedSum) {
				Expression e = sum.getPositiveArguments().remove(i);
				e = flattenQuantifiedSum((QuantifiedSum) e);
				sum.getPositiveArguments().add(i,e);
			}
				
		}
		return sum;
	}
	
	
	/**
	 * Flatten a sum 'a+b+c' to a part of a sum constraint, without 
	 * specifying its result: sum([a,b,c], ?) and return it, so that
	 * the result can be specified later.
	 * 
	 * @param sum
	 * @return
	 * @throws TailorException
	 */
	private SumConstraint flattenSumPartToSumConstraint(Sum sum) 
		throws TailorException {
		
		
		
		if(this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
			
			// 1. flatten the sum arguments and convert them to Expression-Arrays
			Expression[] positiveArguments = new Expression[sum.getPositiveArguments().size()];
			Expression[] negativeArguments = new Expression[sum.getNegativeArguments().size()];
			
			
				for(int i=sum.getPositiveArguments().size()-1; i>=0; i--) {
					//System.out.println("sum arguments of sum that will be part of SumConstraint:"+sum.getPositiveArguments().get(i));
					positiveArguments[i] = flattenExpression(sum.getPositiveArguments().get(i));
					//System.out.println("sum arguments AFTER flattening of sum that will be part of SumConstraint:"+positiveArguments[i]);
				}
			
				for(int i=sum.getNegativeArguments().size()-1; i>=0; i--) {
					negativeArguments[i] = flattenExpression(sum.getNegativeArguments().get(i));
				}
			
			// 2. create a sum constraint with them 
			return new SumConstraint(positiveArguments,
					                 negativeArguments);
				
				
		}
		// we have to flatten the stuff into binary sum constraints
		else {
			return flattenToBinarySumConstraint(sum);
		}
	}
	
	
	
	/**
	 * Flatten a sum (containing positive and negative elements) to a 
	 * binary sum constraint.
	 * 
	 * @param sum
	 * @return
	 * @throws TailorException
	 */
	private SumConstraint flattenToBinarySumConstraint(Sum sum) 
		throws TailorException {
		
		
		ArrayList<Expression> positiveArguments = sum.getPositiveArguments();
		ArrayList<Expression> negativeArguments = sum.getPositiveArguments();
		SumConstraint partWiseSumConstraint = null;
		
		if(positiveArguments.size() > 0)
			partWiseSumConstraint = flattenSumToPartWiseBinarySumConstraint(positiveArguments, null, true); //isPositive
		else {
			// if the first element is negative anyway -> make it positive
			if(negativeArguments.get(0) instanceof UnaryMinus)
				negativeArguments.add(0, ( (UnaryMinus) negativeArguments.remove(0)).getArgument());
			// otherwise make it negative
			else negativeArguments.add(0, new UnaryMinus(negativeArguments.remove(0)));
			
			// just flatten the negative part
			return flattenSumToPartWiseBinarySumConstraint(negativeArguments, null, false);
			
		}
		
		if(negativeArguments.size() == 0) {
			return partWiseSumConstraint;
		}
		else {
			int lb= partWiseSumConstraint.getSumDomain()[0];
			int ub= partWiseSumConstraint.getSumDomain()[1];
			// introduce an aux variable for the positive stuff (aux1) and then 
			ArithmeticAtomExpression auxVariable1 = new ArithmeticAtomExpression(createAuxVariable(lb,ub));
			
			partWiseSumConstraint.setResult(auxVariable1, Expression.EQ, true);
			this.constraintBuffer.add(partWiseSumConstraint);
			
			
			//for the negative part (aux2)
			SumConstraint partWiseSumConstraint2 =  flattenSumToPartWiseBinarySumConstraint(negativeArguments, auxVariable1, false); //isPositive
			lb= partWiseSumConstraint2.getSumDomain()[0];
			ub= partWiseSumConstraint2.getSumDomain()[1];
			ArithmeticAtomExpression auxVariable2 = new ArithmeticAtomExpression(createAuxVariable(lb,ub));
			
			partWiseSumConstraint.setResult(auxVariable2, Expression.EQ, true);
			this.constraintBuffer.add(partWiseSumConstraint2);
			
			return new SumConstraint(new Expression[] {auxVariable1} ,
                                     new Expression[] {auxVariable2});
			// and return (aux1 - aux2) and open result
			
		}
	}
	
	/**
	 * 
	 * 
	 * @param positiveArgs
	 * @param argument
	 * @param isPositive TODO
	 * @return
	 * @throws TailorException
	 */
	private SumConstraint flattenSumToPartWiseBinarySumConstraint(ArrayList<Expression> positiveArgs, 
														  Expression argument, 
														  boolean isPositive) 
		throws TailorException {
		
		if(positiveArgs.size() == 2 && argument == null) {
			return (isPositive)? 
					new SumConstraint(new Expression[] {positiveArgs.remove(0), positiveArgs.remove(0)},
					                 new Expression[0])
			:
				new SumConstraint(new Expression[] {positiveArgs.remove(0)} ,
		                          new Expression[] {positiveArgs.remove(0)});
		}
		else if (positiveArgs.size() < 2 && argument == null) 
				throw new TailorException("Internal error: Cannot flatten sum to binary sum with less than 2 arguments:"+positiveArgs.toString()); 	
		
		else {
			Expression arg1 = null;
			if(argument == null)
				arg1 = positiveArgs.remove(0);
			else arg1 = argument;
			
			Expression arg2 = positiveArgs.remove(0);
			SumConstraint sumConstraint = new SumConstraint(new Expression[] {arg1, arg2},
					                 new Expression[0]);
			
			if(positiveArgs.size() ==0)
				return sumConstraint;
			
			int lb = arg1.getDomain()[0] + arg2.getDomain()[0];
			int ub = arg1.getDomain()[1] + arg2.getDomain()[1];
			ArithmeticAtomExpression auxVariable = new ArithmeticAtomExpression(createAuxVariable(lb,ub));
			
			sumConstraint.setResult(auxVariable, Expression.EQ, true);
			this.constraintBuffer.add(sumConstraint);
			return flattenSumToPartWiseBinarySumConstraint(positiveArgs, auxVariable, isPositive);
		}
	}
	

	/**
	 * Flatten objective 
	 * 
	 * @return
	 * @throws TailorException
	 */
	protected Expression flattenObjective() 
	throws TailorException {
	
	Expression objective = this.normalisedModel.getObjectiveExpression();
	if(this.targetSolver.supportsFeature(TargetSolver.SUPPORTS_OBJECTIVE)) {
		
		if(this.targetSolver.supportsFeature(TargetSolver.CONSTRAINT_OBJECTIVE))
			return flattenExpression(objective);
		
		else {
			objective.willBeFlattenedToVariable(true);
			return flattenExpression(objective);
		}
		
	}
	else throw new TailorException("Cannot tailor objective since target solver does not support it.");
	
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
		
		if(expression.isGonnaBeFlattenedToVariable())
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
							if(atom.isGonnaBeFlattenedToVariable()) {
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
	
	
	private void addToSubExpressions(Expression subExpression, Expression representative) {
		
		//System.out.println("Add subexpression "+subExpression+" of type "+subExpression.getType()+" represented by "+representative);
		
		if(representative instanceof ArithmeticAtomExpression)
			this.subExpressions.put(subExpression.toString(), (ArithmeticAtomExpression) representative);
		else if(representative instanceof RelationalAtomExpression) 
			this.subExpressions.put(subExpression.toString(), ((RelationalAtomExpression)representative).toArithmeticExpression());
		
	}
	
	
	
	private ArithmeticAtomExpression getCommonSubExpression(Expression expression) {
		this.usedCommonSubExpressions++;
		return this.subExpressions.get(expression.toString());
	}
	
	private boolean hasCommonSubExpression(Expression expression) {
		/*System.out.println("Checking if the expression "+expression+" has a common subexpression in:"+this.subExpressions+
				": this is:"+this.subExpressions.containsKey(expression.toString())+" and object: "+this.subExpressions.containsValue(expression.toString()));
		System.out.println("Expression "+expression+" Is of type: "+expression.getType());
		*/
		return this.subExpressions.containsKey(expression.toString());
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
		
		if(constraint instanceof RelationalAtomExpression) return (RelationalAtomExpression)  constraint;
		
		if(!this.targetSolver.supportsReificationOf(constraint.getType()))
			throw new TailorException
			("Cannot reify constraint because its reification is not supported by the target solver:"+constraint);
		
		ArithmeticAtomExpression auxVariable = null;
		
		// if we have a common subexpression, use the corresponding variable
		if(hasCommonSubExpression(constraint))
			auxVariable = getCommonSubExpression(constraint);
		
		// if we have no common subexpression, create a new auxiliary variable
		// and add the constraint to the list of subexpressions
		else  {
			auxVariable = new ArithmeticAtomExpression(createAuxVariable(0, 1));
			addToSubExpressions(constraint, auxVariable);
		}
		
		
		// add the flattened constraint to the constraint buffer
		this.constraintBuffer.add(new Reification(constraint,
				                                  auxVariable.toRelationalAtomExpression()));
		
		return auxVariable.toRelationalAtomExpression();
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
		
		//System.out.println("Creating aux var with bounds: lb:"+lb+" and ub:"+ub);
		
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
	 
	
	/*private int max(int value1, int value2) {
		
		return (value1 < value2) ?
				value2 : value1;
	}
	
	private int min(int value1, int value2) {
		
		return (value1 < value2) ?
				value1 : value2;
	}*/
}
