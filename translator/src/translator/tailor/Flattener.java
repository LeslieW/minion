package translator.tailor;

import translator.solver.*;
import java.util.ArrayList;
import java.util.HashMap;
import translator.expression.*;
import translator.normaliser.NormalisedModel;
import translator.TranslationSettings;

public class Flattener {


	public final int CONSTANT_ARRAY_OFFSET_FROM_ZERO = 1;
	public final String AUXVARIABLE_NAME = "aux";
	
	protected int noAuxVariables;
	
	TranslationSettings settings;
	/** the target solver we want to tailor the model to */
	TargetSolver targetSolver;
	/** the list of all flattened constraints. constraints are added on the fly. */
	ArrayList<Expression> constraintBuffer;
	
	/** contains every (flattened) subexpression and it's corresponding variable*/
	HashMap<String,ArithmeticAtomExpression> subExpressions;
	//HashMap<Expression,ArithmeticAtomExpression> subExpressions;
	
	/** contains expressions that are equal -*/
	HashMap<String,ArithmeticAtomExpression> equalSubExpressions;
	HashMap<String,ArithmeticAtomExpression> equalAtomsMap;
	ArrayList<ArithmeticAtomExpression> replaceableVariables;
	
	HashMap<String, int[]> constantOffsetsFromZero;
	
	/** the normalised model contains the list of expression variables etc */
	NormalisedModel normalisedModel;
	
	/** flags for certain reformulations */
	boolean useCommonSubExpressions;
	boolean useEqualSubExpressions;
	boolean applyStrongCopyPropagation;
	
	int usedCommonSubExpressions;
	int usedEqualSubExpressions;
	
	// helper vars
	boolean flatteningNegatedArgument = false;
	
	
	// ========== CONSTRUCTOR ============================
	
	public Flattener(TranslationSettings settings,
			         NormalisedModel normalisedModel) {
		this.settings = settings;
		this.targetSolver = this.settings.getTargetSolver();	
		this.normalisedModel = normalisedModel;
		this.constraintBuffer = new ArrayList<Expression>();
		this.subExpressions = new HashMap<String,ArithmeticAtomExpression>();
		this.equalSubExpressions = new HashMap<String,ArithmeticAtomExpression>();
		this.noAuxVariables = 0;
		this.usedCommonSubExpressions = 0;
		this.usedEqualSubExpressions = 0;
		this.useCommonSubExpressions = this.settings.useCommonSubExpressions();
		this.applyStrongCopyPropagation = this.settings.applyStrictCopyPropagation();
		this.useEqualSubExpressions = this.settings.useEqualCommonSubExpressions();
		this.equalAtomsMap = new HashMap<String,ArithmeticAtomExpression>();
		this.replaceableVariables = new ArrayList<ArithmeticAtomExpression>();
	}
	
	// ========== METHODS ================================
	
	/**
	 * Flattens the model according to the target solver that has been specified.
	 * 
	 * @return the flattened Normalised model that was given in the constructor
	 */
	public NormalisedModel flattenModel() throws TailorException,Exception  {
		
		if(this.targetSolver.supportsNestedExpressions()) {
			return this.normalisedModel;
		}
		
		ArrayList<Expression> constraints = this.normalisedModel.getConstraints();
		//System.out.println("Constraints before flattening:"+constraints);
		ArrayList<Expression> flattenedConstraints = new ArrayList<Expression>();
		this.constraintBuffer.clear();
		
		this.normalisedModel.setFlattenedObjectiveExpression(flattenObjective());
		for(int i=0; i<this.constraintBuffer.size(); i++)
			flattenedConstraints.add(this.constraintBuffer.get(i));
			
		for(int i=0; i<constraints.size(); i++) {
			ArrayList<Expression> flatExpression = flattenConstraint(constraints.get(i));
			for(int j=flatExpression.size()-1; j>=0; j--) {
				Expression constraint = flatExpression.remove(j);
				//System.out.println("Evaluating constraint: "+constraint);
				constraint = constraint.evaluate();
				//System.out.println("Evaluated constraint: "+constraint+ " of type :"+constraint.getClass());
				// detect and remove pure TRUE statements
				if(constraint.getType() == Expression.BOOL) {
					if(!((RelationalAtomExpression) constraint).getBool())
						flattenedConstraints.add(constraint);
				}
				else if(constraint instanceof RelationalExpression) {
					
					// boolean variable x  --- > x = true
					if(constraint instanceof RelationalAtomExpression) 
						flattenedConstraints.add(new CommutativeBinaryRelationalExpression(new RelationalAtomExpression(true),
								   														   Expression.EQ,
								   														   constraint));
					else flattenedConstraints.add(constraint);
				}
				
				else throw new TailorException
				("Constraint expression has to be a relational expression and not:"+constraint+" of type "+constraint.getClass().getSimpleName());
			}
		}
		
		this.normalisedModel.setAmountOfCommonSubExpressionsUsed(this.usedCommonSubExpressions);
		this.normalisedModel.setAmountOfEqualSubExpressionsUsed(this.usedEqualSubExpressions);
		this.normalisedModel.replaceConstraintsWith(flattenedConstraints);
		
		if(this.useCommonSubExpressions)
			this.normalisedModel.setSubExpressions(this.subExpressions);
		
		if(this.settings.applyDirectVariableReusage())
			this.normalisedModel.setEqualAtoms(this.equalAtomsMap, this.replaceableVariables);
		
		
		//System.out.println("Common subexpressions:\n"+this.subExpressions);
		//System.out.println("Constraints:"+flattenedConstraints);
		//for(int i=0; i <flattenedConstraints.size(); i++)
		//	System.out.println("Constraint: "+flattenedConstraints.get(i)+" will be reified? "+flattenedConstraints.get(i).isGonnaBeFlattenedToVariable());
	
		return this.normalisedModel;
		
	}
	
	

	
	
	/**
	 * Flatten the parameter constraint and return the corresponding constraint
	 * 
	 * @return the list of flattened constraints that represents the parameter constraint
	 */
	protected ArrayList<Expression> flattenConstraint(Expression constraint) 
		throws TailorException,Exception {
		
		this.constraintBuffer.clear();
		
        // flatten the constraint
		constraint.setIsNotNested();
		
		// we don't need to flatten constraint since linear expressions can be arbitrarily nested
		if(this.targetSolver.supportsFeature(TargetSolver.NESTED_LINEAR_EXPRESSIONS) &&
				constraint.isLinearExpression()) {
			ArrayList<Expression> constraints = new ArrayList<Expression>();
			constraints.add(constraint); 
			return constraints;
		}
		
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
	 * @return the flattened expression that is representative for the parameter expression. If other constraints
	 * have been added during the flattening process, they are stored in the constraintList.
	 * @throws TailorException
	 */
	protected Expression flattenExpression(Expression expression) 
		throws TailorException,Exception {
		
		if(expression instanceof RelationalExpression)
			return flattenRelationalExpression((RelationalExpression) expression);
		
		else if(expression instanceof ArithmeticExpression)
			return flattenArithmeticExpression((ArithmeticExpression) expression);
		
		else if(expression instanceof Array)
			return expression;  // TODO: here we will need to flatten in case the target solver does not support arrays
		
		else throw new TailorException("Unknown variable/expression: "+expression+" (expression with no domain associated). Type:"
				+expression.getClass().getSimpleName());
	}
	
	/**
	 * Flatten a relational expression
	 * 
	 * @param expression
	 * @return
	 */
	private  Expression flattenRelationalExpression(RelationalExpression expression) 
		throws TailorException,Exception {
		
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
		
		else if(expression instanceof LexConstraint) {
			return flattenLexConstraint((LexConstraint) expression);
		}
		
		else if (expression instanceof Atmost) {
			return flattenAtmost((Atmost) expression);
		}
		
		else if(expression instanceof TableConstraint)
			return flattenTableConstraint((TableConstraint) expression);
		
		else return expression;
		//else throw new TailorException("Cannot tailor relational expression yet, or unknown expression:"+expression);
	}
	
	
	
	/**
	 * Flattens a table constraint: only variables are flattened.
	 * 
	 * @param table
	 * @return
	 * @throws TailorException
	 * @throws Exception
	 */
	private Expression flattenTableConstraint(TableConstraint table) 
		throws TailorException,Exception {
		
		if(this.targetSolver.supportsConstraint(Expression.TABLE_CONSTRAINT)) {
			Variable[] flattenedVars = table.getVariables();
			
			for(int i=0; i<flattenedVars.length; i++) {
				flattenedVars[i].willBeFlattenedToVariable(true);
				flattenedVars[i] = ((ArithmeticAtomExpression) flattenExpression(new ArithmeticAtomExpression(flattenedVars[i]))).getVariable();
			}
			
			if(table.isGonnaBeFlattenedToVariable()) {
				if( this.targetSolver.supportsReificationOf(Expression.TABLE_CONSTRAINT)) {
				
					TableConstraint flatTable = new  TableConstraint(flattenedVars, table.getTupleList());
					flatTable.setToConflictingTableConstraint(table.isConflictingTableConstraint());
					return reifyConstraint(flatTable);
					
				}
				else throw new TailorException("Cannot tailor TABLECONSTRAINT '"+table+"' to "+targetSolver.getSolverName()+":\n"+
						"The solver does not support reification of tables.");
			}
			
			else { 
				TableConstraint flatTable = new  TableConstraint(flattenedVars, table.getTupleList());
				flatTable.setToConflictingTableConstraint(table.isConflictingTableConstraint());
				return flatTable;
			}
			
		}
		else throw new TailorException("Cannot tailor TABLE constraint: it is not supported by "+this.targetSolver.getSolverName());
		
	}
	
	
	/**
	 * Flatten atmost
	 * 
	 * @param atmost
	 * @return
	 * @throws TailorException
	 * @throws Exception
	 */
	private Expression flattenAtmost(Atmost atmost) 
		throws TailorException,Exception {
		
		// ------------- 1st case: the target solver supports ATLEAST/ATMOST ---------------
		if((atmost.isAtmost() && this.targetSolver.supportsConstraint(Expression.ATMOST_CONSTRAINT)) ||
				!atmost.isAtmost() && this.targetSolver.supportsConstraint(Expression.ATLEAST_CONSTRAINT)) {
			
			Array array = atmost.getArray();
			array = (Array) flattenExpression(array);
		
			if(atmost.isGonnaBeFlattenedToVariable()) {
				if((atmost.isAtmost() &&  this.targetSolver.supportsReificationOf(Expression.ATMOST_CONSTRAINT)) ||
						!atmost.isAtmost() && this.targetSolver.supportsReificationOf(Expression.ATLEAST_CONSTRAINT)) {
					return reifyConstraint(new Atmost(array,
													atmost.getOccurrences(),
													atmost.getValues(),
													atmost.isAtmost()));
				}
				else throw new TailorException("Cannot tailor ATMOST/ATLEAST constraint '"+atmost
						+"' to target solver yet: it is not reifiable.");
				
			}
			
			return new Atmost(array,
				          atmost.getOccurrences(),
				          atmost.getValues(),
				          atmost.isAtmost());
		}
		
		
		// -------- 2nd case: the solver does not support atleast/atmost -------------------
		else throw new TailorException("Cannot tailor ATMOST/ATLEAST to "+targetSolver.getSolverName()+
				" yet: it does not support ATLEAST/ATMOST.");
	}
	
	
	
	/**
	 * Flatten a lex constraint
	 * 
	 * @param lexConstraint
	 * @return
	 * @throws TailorException
	 * @throws Exception
	 */
	private Expression flattenLexConstraint(LexConstraint lexConstraint) 
		throws TailorException, Exception {
		
		Array leftArray = (Array) flattenExpression(lexConstraint.getLeftArray());
		Array rightArray = (Array) flattenExpression(lexConstraint.getRightArray());
		
		
		if(lexConstraint.isGonnaBeFlattenedToVariable()) {
			if(this.targetSolver.supportsReificationOf(lexConstraint.getOperator())) 			
				return reifyConstraint(new LexConstraint(leftArray, 
				                 lexConstraint.getOperator(), 
				                 rightArray));
			
			else throw new TailorException("Cannot reify lex constraint since the reification is not supported by the target solver:"+lexConstraint);
			
		}
		return new LexConstraint(leftArray, 
				                 lexConstraint.getOperator(), 
				                 rightArray);
	}

	/**
	 * This method adds expressions to the common subexpression list, if there is an assignment
	 * Expression = integer, such that each time 'Expression' reoccurs again, it is substitued 
	 * with 'integer'.
	 * 
	 * @param leftExpression
	 * @param rightExpression
	 */
	private void addStrongEqualityToSubExpressions(Expression leftExpression, Expression rightExpression) {
		
		if(!this.applyStrongCopyPropagation)
			return;
		
		if(leftExpression.getType() == Expression.INT) {
			addToSubExpressions(rightExpression,leftExpression);
		}
		
		else if(rightExpression.getType() == Expression.INT)
			addToSubExpressions(leftExpression, rightExpression);
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
	throws TailorException,Exception {

	
	Expression leftExpression = expression.getLeftArgument();
	Expression rightExpression = expression.getRightArgument();
	
	
	//System.out.println("FLATTENING non-commutative expression "+expression);
	
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
		//System.out.println("flattening right expresion of the sum:"+rightExpression);
		Expression result = flattenExpression(rightExpression);
		//System.out.println("AFTER flattening right expresion of the sum:"+rightExpression);
		partWiseSumConstraint.setResult(result, 
				                        expression.getOperator(), 
				                        false); // result Is on left side
		
		if(expression.getOperator() == Expression.EQ && !expression.isGonnaBeFlattenedToVariable())
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
		
		if(expression.getOperator() == Expression.EQ && !expression.isGonnaBeFlattenedToVariable())
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
	
	
	//System.out.println("Gonna flatten left expression: "+leftExpression+" and right expression:"+rightExpression+" with op:"+expression.getOperator());
	
	// E => BOOL
	if(rightExpression.getType() == Expression.BOOL && 
			expression.getOperator() == Expression.IF	) {
		if(((RelationalAtomExpression) rightExpression).getBool()) {
			return new RelationalAtomExpression(true);
		}
		else return flattenExpression(new Negation(leftExpression));
		
	}
	
	
	leftExpression = flattenExpression(leftExpression);
	rightExpression = flattenExpression(rightExpression);
	
	
	//System.out.println("FLATTENED left expression: "+leftExpression+" and right expression:"+rightExpression+" with op:"+expression.getOperator());
	// BOOL => E
	if(leftExpression.getType() == Expression.BOOL && expression.getOperator() == Expression.IF) {
		if(!((RelationalAtomExpression) leftExpression).getBool())
			return new RelationalAtomExpression(true);
		else return rightExpression;
	}
	
	
	// E => BOOL
	//if(rightExpression.getType() == Expression.BOOL && expression.getOperator() == Expression.IF) {
	//	if(((RelationalAtomExpression) rightExpression).getBool()) {
	//		return new RelationalAtomExpression(true);
	//	}
	//	else return flattenExpression(new Negation(leftExpression));
	//}
	
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
		throws TailorException,Exception {
	
		//if(expression.getOperator() == Expression.IFF)
			//System.out.println("Flattening expression:"+expression);
		expression.orderExpression();
		Expression leftExpression = expression.getLeftArgument();
		Expression rightExpression = expression.getRightArgument();
		boolean leftAndRightAreEqual = false;
		
		if(expression.getType() == Expression.EQ)
			addStrongEqualityToSubExpressions(leftExpression,rightExpression);
		
		// case: ATOM EQ ATOM where ATOM is not a constant!
		if(this.settings.applyDirectVariableReusage() && 
				!expression.isGonnaBeFlattenedToVariable() &&
				(expression.getOperator() == Expression.EQ || expression.getOperator() == Expression.IFF) && 
				(leftExpression instanceof ArithmeticAtomExpression || leftExpression instanceof RelationalAtomExpression) &&
				(rightExpression instanceof ArithmeticAtomExpression || rightExpression instanceof RelationalAtomExpression) &&
				(leftExpression.getType() != Expression.INT && leftExpression.getType() != Expression.BOOL &&
						rightExpression.getType() != Expression.INT && rightExpression.getType() != Expression.BOOL ) && 
				
				// we don't want a variable that will be translated to an element constraint
				!(leftExpression.getType() == Expression.INT_ARRAY_VAR &&  leftExpression instanceof ArithmeticAtomExpression &&
						((ArrayVariable) ((ArithmeticAtomExpression)leftExpression).getVariable()).isIndexedBySomethingNotConstant()  ) &&
				!(leftExpression.getType() == Expression.INT_ARRAY_VAR &&  leftExpression instanceof RelationalAtomExpression &&
						((ArrayVariable) ((RelationalAtomExpression)leftExpression).getVariable()).isIndexedBySomethingNotConstant()  ) &&
				!(rightExpression.getType() == Expression.INT_ARRAY_VAR &&  rightExpression instanceof ArithmeticAtomExpression &&
						((ArrayVariable) ((ArithmeticAtomExpression)rightExpression).getVariable()).isIndexedBySomethingNotConstant()  ) &&
				!(rightExpression.getType() == Expression.INT_ARRAY_VAR &&  rightExpression instanceof RelationalAtomExpression &&
						((ArrayVariable) ((RelationalAtomExpression)rightExpression).getVariable()).isIndexedBySomethingNotConstant()  ))						
						 
				{
			// TODO: might not necessary
			//System.out.println("Found equal atoms: left:"+leftExpression+" = right:"+rightExpression);
			
			rightExpression.willBeFlattenedToVariable(true);
			rightExpression = flattenExpression(rightExpression);
			
			addToDirectlyEqualAtoms(rightExpression, leftExpression);
			// then return and don't translate this expression
			return new RelationalAtomExpression(true);
		}
		
		// we can exploit equality if the expression won't be reified
		if(!expression.isGonnaBeFlattenedToVariable() && 
				(expression.getOperator() == Expression.IFF ||
				  expression.getOperator() == Expression.EQ)) {
			if(hasCommonSubExpression(leftExpression)) {
				addToEqualExpressions(rightExpression, getCommonSubExpression(leftExpression));
			}
			else if(hasCommonSubExpression(rightExpression)) {
				addToEqualExpressions(leftExpression, getCommonSubExpression(rightExpression));
			}
			else 
				leftAndRightAreEqual = true;
			
		}
		
		//System.out.println("We have a comm bin expr between: "+leftExpression+" "+expression.getOperator()+" "+rightExpression);
		
		if(rightExpression instanceof QuantifiedSum ||
				rightExpression instanceof QuantifiedExpression) {
			
			//if(expression.isGonnaBeFlattenedToVariable()) 
				//rightExpression.willBeFlattenedToVariable(true);
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
				rightExpression.willBeFlattenedToVariable(true);
			}
			
			// just testing this... 
			if(rightExpression instanceof QuantifiedSum)
				rightExpression.willBeFlattenedToVariable(false);
			
			rightExpression = flattenExpression(rightExpression);
		}
		
		if(leftExpression instanceof QuantifiedSum ||
				leftExpression instanceof QuantifiedExpression) {
			//if(expression.isGonnaBeFlattenedToVariable()) 
				//leftExpression.willBeFlattenedToVariable(true);
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
				leftExpression.willBeFlattenedToVariable(true);
			}
			
			if(leftExpression instanceof QuantifiedSum)
				leftExpression.willBeFlattenedToVariable(false);
			
			leftExpression = flattenExpression(leftExpression);
		}
		
		
		
		//System.out.println("We have an equality between(after qsum): "+leftExpression+" == "+rightExpression);
		
		// 1. detect sum expressions (we can only have 1 sum expression on one side due to normalisation)
		if(rightExpression instanceof Sum) {
			//System.out.println("We have a sum on the right hand side\n: "+rightExpression);
			rightExpression.reduceExpressionTree();
			Sum rSum = (Sum) rightExpression;
			Sum rightSum = (Sum) rSum.copy();
			rightSum.setWillBeConvertedToSumConstraint(true);
			SumConstraint partWiseSumConstraint = (SumConstraint) flattenSum(rightSum);
			
			leftExpression.willBeFlattenedToVariable(true);
			Expression result = flattenExpression(leftExpression);
			partWiseSumConstraint.setResult(result, 
					                        expression.getOperator(), 
					                        true); // result Is on left side
			
			if(!expression.isNested())
				partWiseSumConstraint.setIsNotNested();
			
			if(expression.getOperator() == Expression.EQ && !expression.isGonnaBeFlattenedToVariable())
				addToSubExpressions(rSum,result);
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				return reifyConstraint(partWiseSumConstraint);
			}
			else return partWiseSumConstraint;
		}
		else if(leftExpression instanceof Sum) {
			//System.out.println("We have a sum on the left hand side\n: "+this);
			leftExpression.reduceExpressionTree();
			Sum lSum = (Sum) leftExpression;
			Sum leftSum = (Sum) lSum.copy();
			leftSum.setWillBeConvertedToSumConstraint(true);
			SumConstraint partWiseSumConstraint = (SumConstraint) flattenSum(leftSum);
			
			rightExpression.willBeFlattenedToVariable(true);
			Expression result = flattenExpression(rightExpression);
			partWiseSumConstraint.setResult(result, 
					                        expression.getOperator(), 
					                        false); // result Is on left side
			
			if(expression.getOperator() == Expression.EQ && !expression.isGonnaBeFlattenedToVariable())
				addToSubExpressions(lSum,result);
			
			if(!expression.isNested())
				partWiseSumConstraint.setIsNotNested();
			
			//System.out.println("left expression is sum etc ... and expr "+expression+" will be flattened to variable? "+expression.isGonnaBeFlattenedToVariable());
			
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				return reifyConstraint(partWiseSumConstraint);
			}
			else return partWiseSumConstraint;
		}
		
	
		
		if(leftExpression instanceof Multiplication) {
			// flatten multiplication to partwise product constraint
			Multiplication multiplication = (Multiplication) leftExpression;

			//System.out.println("Left expression is a multiplication:"+multiplication);
			Multiplication processedMultiplication = (Multiplication) multiplication.copy();
			processedMultiplication.setWillBeConverteredToProductConstraint(true);
			ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(processedMultiplication);
			
			
			if(expression.getOperator() == Expression.EQ && 
					!expression.isGonnaBeFlattenedToVariable()) {
				rightExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
				rightExpression = flattenExpression(rightExpression);
				
				if(!hasCommonSubExpression(multiplication))	
					addToSubExpressions(multiplication, rightExpression);
				
				productConstraint.setResult(rightExpression);
				if(!expression.isNested())
					productConstraint.setIsNotNested();
				
				if(expression.isGonnaBeFlattenedToVariable()) {
					return reifyConstraint(productConstraint);
				}
				else return productConstraint;
			}
			
			ArithmeticAtomExpression auxVar = null;
			
			// check for common subexpressions
			if(hasCommonSubExpression(multiplication)) 
				auxVar = getCommonSubExpression(multiplication);
			else {
				if(expression.getOperator() == Expression.EQ) {
					int lb = rightExpression.getDomain()[0];
					int ub = rightExpression.getDomain()[1];
					
					if(multiplication.getDomain()[0] > lb)
						lb = multiplication.getDomain()[0];
					if(multiplication.getDomain()[1] < ub)
						ub = multiplication.getDomain()[1];
					
					auxVar = new ArithmeticAtomExpression(createAuxVariable(lb, 
                            												ub));
				}
				else {
					auxVar = new ArithmeticAtomExpression(createAuxVariable(multiplication.getDomain()[0], 
						                                                multiplication.getDomain()[1]));
				}
				addToSubExpressions(multiplication, auxVar);
				productConstraint.setResult(auxVar);
				this.constraintBuffer.add(productConstraint);
			}
			
			// aux = Multiplication
		
			//System.out.println("Looking at right expression:"+rightExpression+" of expression:"+expression);
			//System.out.println("this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()):"+
					//this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
			
			rightExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
			
			if(rightExpression instanceof Multiplication) {
				Multiplication multiplicationRight = (Multiplication) rightExpression;
				
				Multiplication processedMultiplicationRight = (Multiplication) multiplicationRight.copy();
				processedMultiplicationRight.setWillBeConverteredToProductConstraint(true);
				ProductConstraint productConstraintRight = (ProductConstraint) flattenMultiplication(processedMultiplicationRight);
				
				// expression is nested
				if(expression.isGonnaBeFlattenedToVariable()) {
					
					ArithmeticAtomExpression auxVar2 = null;
					
					if(hasCommonSubExpression(multiplicationRight)) 	
						auxVar2 = getCommonSubExpression(multiplicationRight);
					else {
						
						if(expression.getOperator() == Expression.EQ) {
							int lb = leftExpression.getDomain()[0];
							int ub = leftExpression.getDomain()[1];
							
							//System.out.println("Left expressions bounds: "+lb+".."+ub);
							
							if(multiplicationRight.getDomain()[0] > lb)
								lb = multiplicationRight.getDomain()[0];
							if(multiplicationRight.getDomain()[1] < ub)
								ub = multiplicationRight.getDomain()[1];
							
							
							//System.out.println("Choosed the bounds: "+lb+".."+ub+" for aux var: "+auxVar2);
							
							auxVar2 = new ArithmeticAtomExpression(createAuxVariable(lb, 
		                            												ub));
						}
						else {
							auxVar2 = new ArithmeticAtomExpression(createAuxVariable(multiplicationRight.getDomain()[0], 
								                                                multiplicationRight.getDomain()[1]));
						}
						
						addToSubExpressions(multiplicationRight, auxVar2);
						productConstraintRight.setResult(auxVar2);
						this.constraintBuffer.add(productConstraintRight);
					}
					
					return reifyConstraint(new CommutativeBinaryRelationalExpression(auxVar,expression.getOperator(),auxVar2));
				}
				// expression is NOT nested
				else {
					productConstraintRight.setResult(auxVar); // use the same aux var as the product above
					if(!productConstraintRight.isNested())
						productConstraint.setIsNotNested();
					
					if(!hasCommonSubExpression(multiplicationRight)) 
						addToSubExpressions(multiplicationRight, auxVar);
					
					return productConstraintRight;
				}
			}
			
			// else: the right expression is NOT a multiplication
			else {
				rightExpression = flattenExpression(rightExpression);
			
			
				if(expression.isGonnaBeFlattenedToVariable())
					return reifyConstraint(new CommutativeBinaryRelationalExpression(auxVar,
																					expression.getOperator(),
						                                                           rightExpression));
				else return new CommutativeBinaryRelationalExpression(auxVar,
						expression.getOperator(),
						rightExpression);
			}
		}
		else if(rightExpression instanceof Multiplication) {
			// flatten multiplication to partwise product constraint
			Multiplication multiplication = (Multiplication) rightExpression;
		
			Multiplication processedMultiplication = (Multiplication) multiplication.copy();
			processedMultiplication.setWillBeConverteredToProductConstraint(true);
			ProductConstraint productConstraint = (ProductConstraint) flattenMultiplication(processedMultiplication);
			
			
			if(expression.getOperator() == Expression.EQ && !expression.isGonnaBeFlattenedToVariable()) {
				leftExpression.willBeFlattenedToVariable(this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator()));
				leftExpression = flattenExpression(leftExpression);
				
				if(!hasCommonSubExpression(multiplication))	
					addToSubExpressions(multiplication, leftExpression);
				
				productConstraint.setResult(leftExpression);
				if(expression.isGonnaBeFlattenedToVariable()) {
					return reifyConstraint(productConstraint);
				}
				else return productConstraint;
			}
			
			
			ArithmeticAtomExpression auxVar = null;
			
			// check for common subexpressions
			if(hasCommonSubExpression(multiplication)) 
				auxVar = getCommonSubExpression(multiplication);
			else {
				if(expression.getOperator() == Expression.EQ) {
					int lb = leftExpression.getDomain()[0];
					int ub = leftExpression.getDomain()[1];
					
					//System.out.println("Left expressions bounds: "+lb+".."+ub);
					
					if(multiplication.getDomain()[0] > lb)
						lb = multiplication.getDomain()[0];
					if(multiplication.getDomain()[1] < ub)
						ub = multiplication.getDomain()[1];
					
					
					//System.out.println("Choosed the bounds: "+lb+".."+ub+" for aux var: "+auxVar);
					
					auxVar = new ArithmeticAtomExpression(createAuxVariable(lb, 
                            												ub));
				}
				else {
					auxVar = new ArithmeticAtomExpression(createAuxVariable(multiplication.getDomain()[0], 
						                                                multiplication.getDomain()[1]));
				}
				
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
		
	
		// direct absolute value
		if(rightExpression instanceof AbsoluteValue && 
				expression.getOperator() == Expression.EQ &&
				 (!expression.isGonnaBeFlattenedToVariable() || 
						 this.targetSolver.supportsReificationOf(Expression.ABS))) {
			
			Expression argument = ((AbsoluteValue) rightExpression).getArgument();
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE_RESULT)
					|| expression.isGonnaBeFlattenedToVariable())
				leftExpression.willBeFlattenedToVariable(true);
			
			
			if(hasCommonSubExpression(leftExpression))
				leftExpression = getCommonSubExpression(leftExpression);
			
			else {
				//System.out.println("About to flatten left expression "+leftExpression+" of abs constraint "+expression);
				leftExpression = flattenExpression(leftExpression);
				//System.out.println("Flattenend left (result) expression of absConstriant:"+leftExpression);
				addToSubExpressions(rightExpression, leftExpression);
			//	System.out.println("Added d left (result) expression "+leftExpression+" to CSE of:"+rightExpression);
			}
			
			
			// check if the solver supports variables only as result of absolute value
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE_ARGUMENT))
				argument.willBeFlattenedToVariable(true);
			
			argument = flattenExpression(argument);
			
			// add subexpression after flattening
			if(!hasCommonSubExpression(argument)) {
				addToSubExpressions(argument, leftExpression);
			}
			
			AbsoluteConstraint absConstraint = new AbsoluteConstraint(argument, leftExpression);
			
			//System.out.println("Flattenend expression to absConstriant:"+absConstraint);
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				//this.constraintBuffer.add(absConstraint);
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" and will return result-expression :"+leftExpression);
				//return leftExpression;
				return reifyConstraint(absConstraint);
			}
			else  {
				if(!expression.isNested())
					absConstraint.setIsNotNested();
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" which i am returning now");
				return absConstraint;
			}
		}
		else if(leftExpression instanceof AbsoluteValue && 
				expression.getOperator() == Expression.EQ &&
				 (!expression.isGonnaBeFlattenedToVariable() || 
						 this.targetSolver.supportsReificationOf(Expression.ABS))) {
		
			Expression argument = ((AbsoluteValue) leftExpression).getArgument();
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE_RESULT)
					|| expression.isGonnaBeFlattenedToVariable())
				rightExpression.willBeFlattenedToVariable(true);
			rightExpression = flattenExpression(rightExpression);
			
			if(!hasCommonSubExpression(leftExpression)) {
				addToSubExpressions(leftExpression, rightExpression);
			}
			
			// check if the solver supports variables only as result of absolute value
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_ABSOLUTE_VALUE_ARGUMENT))
				argument.willBeFlattenedToVariable(true);
			
			argument = flattenExpression(argument);
			
			// add subexpression after flattening
			if(!hasCommonSubExpression(argument)) {
				addToSubExpressions(argument, rightExpression);
			}
			
			AbsoluteConstraint absConstraint = new AbsoluteConstraint(argument, rightExpression);
			if(!expression.isNested())
				absConstraint.setIsNotNested();
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				//this.constraintBuffer.add(absConstraint);
				//return rightExpression;
				return reifyConstraint(absConstraint);
			}
			else return absConstraint;
			
		}
		
		
		
		// max values
		if(rightExpression instanceof Minimum && 
				expression.getOperator() == Expression.EQ &&
				(!expression.isGonnaBeFlattenedToVariable() ||
						this.targetSolver.supportsReificationOf(Expression.MIN))) {
			
			Expression argument = ((Minimum) rightExpression).getArgument();
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_MIN)
					|| expression.isGonnaBeFlattenedToVariable())
				leftExpression.willBeFlattenedToVariable(true);
			
			
			if(hasCommonSubExpression(leftExpression))
				leftExpression = getCommonSubExpression(leftExpression);
			
			else {
				//System.out.println("About to flatten left expression "+leftExpression+" of abs constraint "+expression);
				leftExpression = flattenExpression(leftExpression);
				//System.out.println("Flattenend left (result) expression of absConstriant:"+leftExpression);
				addToSubExpressions(rightExpression, leftExpression);
			//	System.out.println("Added d left (result) expression "+leftExpression+" to CSE of:"+rightExpression);
			}
			
			
			// check if the solver supports variables only as result of absolute value
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_MIN))
				argument.willBeFlattenedToVariable(true);
			
			argument = flattenExpression(argument);
			
			// add subexpression after flattening
			if(!hasCommonSubExpression(argument)) {
				addToSubExpressions(argument, leftExpression);
			}
			
			if(!(argument instanceof Array))
				throw new TailorException("Invalid argument of "+rightExpression+": "+argument+" is not an array.");
			
			MinimumConstraint minConstraint = new MinimumConstraint((Array) argument, 
																	leftExpression, 
																	((Minimum) rightExpression).isMaximum());
			
			//System.out.println("Flattenend expression to absConstriant:"+absConstraint);
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				//this.constraintBuffer.add(minConstraint);
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" and will return result-expression :"+leftExpression);
				return reifyConstraint(minConstraint);
			}
			else  {
				if(!expression.isNested())
					minConstraint.setIsNotNested();
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" which i am returning now");
				return minConstraint;
			}
		}
		else if(leftExpression instanceof Minimum && 
				expression.getOperator() == Expression.EQ && 
				(!expression.isGonnaBeFlattenedToVariable() ||
						this.targetSolver.supportsReificationOf(Expression.MIN))) {
		
			Expression argument = ((Minimum) leftExpression).getArgument();
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_MIN)
					|| expression.isGonnaBeFlattenedToVariable())
				rightExpression.willBeFlattenedToVariable(true);
			rightExpression = flattenExpression(rightExpression);
			
			if(!hasCommonSubExpression(leftExpression)) {
				addToSubExpressions(leftExpression, rightExpression);
			}
			
			// check if the solver supports variables only as result of absolute value
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_MIN))
				argument.willBeFlattenedToVariable(true);
			
			argument = flattenExpression(argument);
			
			// add subexpression after flattening
			if(!hasCommonSubExpression(argument)) {
				addToSubExpressions(argument, rightExpression);
			}
			
			
			if(!(argument instanceof Array))
				throw new TailorException("Invalid argument of "+leftExpression+": "+argument+" is not an array.");
			
			MinimumConstraint minConstraint = new MinimumConstraint((Array) argument, 
																	rightExpression, 
																	((Minimum) leftExpression).isMaximum());
			
			
			if(!expression.isNested())
				minConstraint.setIsNotNested();
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				//this.constraintBuffer.add(minConstraint);
				//return rightExpression;
				return reifyConstraint(minConstraint);
			}
			else return minConstraint;
			
		}
		
		
		
		// -------------------------------------------
		
		
		
		// modulo, div, power
		if(rightExpression instanceof NonCommutativeArithmeticBinaryExpression && 
				expression.getOperator() == Expression.EQ && 
				(!expression.isGonnaBeFlattenedToVariable() ||
						this.targetSolver.supportsReificationOf(
								((NonCommutativeArithmeticBinaryExpression) rightExpression).getOperator()))) {
			
			//System.out.println("Flattening "+expression);
			NonCommutativeArithmeticBinaryExpression e = (NonCommutativeArithmeticBinaryExpression) rightExpression;
			
			Expression leftArgument = (e).getLeftArgument();
			Expression rightArgument = (e).getRightArgument();
			
			//if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.)
			//		|| expression.isGonnaBeFlattenedToVariable())
			leftExpression.willBeFlattenedToVariable(true);
			
			
			if(hasCommonSubExpression(leftExpression)) {
				//System.out.println("Left expression "+leftExpression+" has a common subexpression:"+getCommonSubExpression(leftExpression));
				leftExpression = getCommonSubExpression(leftExpression);
			}
			
			else {
				//System.out.println("About to flatten left expression "+leftExpression+" of nl constraint "+expression);
				leftExpression = flattenExpression(leftExpression);
				//System.out.println("Flattenend left (result) expression of nl Constriant:"+leftExpression);
				if(!expression.isNested()) {
					addToSubExpressions(rightExpression, leftExpression);
					//System.out.println("Added left (result) expression "+leftExpression+" to CSE of:"+rightExpression);
				}
			}
			
			
			// check if the solver supports variables only as result of absolute value
			//if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_MIN))
			leftArgument.willBeFlattenedToVariable(true);
			rightArgument.willBeFlattenedToVariable(true);
			
			leftArgument = flattenExpression(leftArgument);
			rightArgument = flattenExpression(rightArgument);
			
			
			BinaryNonLinearConstraint nonLinearConstraint = new BinaryNonLinearConstraint(leftArgument,
																							e.getOperator(),
																							rightArgument,
																							leftExpression);
			
			//System.out.println("Flattenend expression to absConstriant:"+nonLinearConstraint);
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				//this.constraintBuffer.add(nonLinearConstraint);
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" and will return result-expression :"+leftExpression);
				//return leftExpression;
				return reifyConstraint(nonLinearConstraint);
			}
			else  {
				if(!expression.isNested())
					nonLinearConstraint.setIsNotNested();
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" which i am returning now");
				return nonLinearConstraint;
			}
		}
		else if(leftExpression instanceof NonCommutativeArithmeticBinaryExpression 
				&& expression.getOperator() == Expression.EQ && 
				(!expression.isGonnaBeFlattenedToVariable() ||
						this.targetSolver.supportsReificationOf(
								((NonCommutativeArithmeticBinaryExpression) rightExpression).getOperator()))) {
			
			NonCommutativeArithmeticBinaryExpression e = (NonCommutativeArithmeticBinaryExpression) leftExpression;
			//System.out.println("Flattening "+e);
			
			
			Expression leftArgument = (e).getLeftArgument();
			Expression rightArgument = (e).getRightArgument();
			
			//if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.)
			//		|| expression.isGonnaBeFlattenedToVariable())
			rightExpression.willBeFlattenedToVariable(true);
			
			
			if(hasCommonSubExpression(rightExpression))
				rightExpression = getCommonSubExpression(rightExpression);
			
			else {
				//System.out.println("About to flatten left expression "+leftExpression+" of abs constraint "+expression);
				rightExpression = flattenExpression(rightExpression);
				//System.out.println("Flattenend left (result) expression of absConstriant:"+leftExpression);
				if(!expression.isNested()) {
					addToSubExpressions(leftExpression, rightExpression);
					//System.out.println("Added  left (result) expression "+leftExpression+" to CSE of:"+rightExpression);
				}
			}
			
			
			// check if the solver supports variables only as result of absolute value
			//if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(TargetSolver.CONSTRAINT_NESTED_IN_MIN))
			leftArgument.willBeFlattenedToVariable(true);
			rightArgument.willBeFlattenedToVariable(true);
			
			leftArgument = flattenExpression(leftArgument);
			rightArgument = flattenExpression(rightArgument);
			
			
			BinaryNonLinearConstraint nonLinearConstraint = new BinaryNonLinearConstraint(leftArgument,
																							e.getOperator(),
																							rightArgument,
																							rightExpression);
			
			//System.out.println("Flattenend expression to absConstriant:"+absConstraint);
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				this.constraintBuffer.add(nonLinearConstraint);
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" and will return result-expression :"+leftExpression);
				return rightExpression;
			}
			else  {
				if(!expression.isNested())
					nonLinearConstraint.setIsNotNested();
				//System.out.println("Flattenend expression to absConstriant:"+absConstraint+" which i am returning now");
				return nonLinearConstraint;
			}
		}
		
		
		
		
		// --------------------------------------------
		
		//System.out.println("Right expr is: "+rightExpression);
		
		//3. Detect element constraints
		if(!this.targetSolver.supportsVariableArrayIndexing()) {
			if( (rightExpression.getType() == Expression.INT_ARRAY_VAR ||
					rightExpression.getType() == Expression.BOOL_ARRAY_VAR)
					 ) {
				
					if(rightExpression instanceof ArithmeticAtomExpression) {
						
						ArithmeticAtomExpression rightAtomExpr = ((ArithmeticAtomExpression) rightExpression);
						
						if(rightAtomExpr.getVariable() instanceof ArrayVariable) {
						ArrayVariable arrayVar = (ArrayVariable) rightAtomExpr.getVariable();
						
						
						Expression[] indices = arrayVar.getExpressionIndices();
						
						//System.out.println("Right expression 1 "+rightExpression);
						
						if(indices != null) {
							
							int[] intIndices = new int[indices.length];
							boolean allIndicesAreInteger = true;
							
							for(int i=0; i<indices.length; i++) {
								indices[i].willBeFlattenedToVariable(true);
								indices[i] = flattenExpression(indices[i]);
								if(indices[i].getType() == Expression.INT) {
									intIndices[i] = ((ArithmeticAtomExpression) indices[i]).getConstant();
								}
								else allIndicesAreInteger = false;
							}
							
							if(allIndicesAreInteger) {
								arrayVar = new ArrayVariable(arrayVar.getArrayNameOnly(),
										                     intIndices,
										                     arrayVar.getBaseDomain());
								rightExpression = new ArithmeticAtomExpression(arrayVar);
							}
							
						}
						
						//System.out.println("Right expression again>"+rightExpression);
						
						if(this.normalisedModel.constantArrays.containsKey(arrayVar.getArrayNameOnly())) {
							rightExpression = flattenArithmeticAtomExpression((ArithmeticAtomExpression) rightExpression);
					    }
						else if(arrayVar.isIndexedBySomethingNotConstant()) {

								arrayVar.setWillBeFlattenedToPartwiseElementConstraint(true);
								//System.out.println("Array var is:"+arrayVar+" of rightExoression:"+rightExpression);
								ElementConstraint elementConstraint = //flattenToPartwiseElementConstraint(new ArithmeticAtomExpression(arrayVar));
																	flattenToPartwiseElementConstraint(rightExpression.copy());
							
								if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
									leftExpression.willBeFlattenedToVariable(true);
								leftExpression = flattenExpression(leftExpression);
							
								addToSubExpressions(rightExpression, leftExpression);
								elementConstraint.setResultExpression(leftExpression);
								//System.out.println("Flattened to element constraint: "+elementConstraint);
								
								if(expression.isGonnaBeFlattenedToVariable()) {
									this.constraintBuffer.add(elementConstraint);
									return leftExpression;
								}
								else return elementConstraint;
							
						}
						} // else : we have a SimpleArrayVariable in an arithmetic atom expression
					}
					else if(rightExpression instanceof RelationalAtomExpression) {
						if(( (ArrayVariable) ((RelationalAtomExpression) rightExpression).getVariable()).isIndexedBySomethingNotConstant()) {
						
							ArrayVariable arrayVar = (ArrayVariable) ((RelationalAtomExpression) rightExpression).getVariable();
							
							Expression[] indices = arrayVar.getExpressionIndices();
							
							if(indices != null) {
								
								int[] intIndices = new int[indices.length];
								boolean allIndicesAreInteger = true;
								
								for(int i=0; i<indices.length; i++) {
									indices[i].willBeFlattenedToVariable(true);
									indices[i] = flattenExpression(indices[i]);
									if(indices[i].getType() == Expression.INT) {
										intIndices[i] = ((ArithmeticAtomExpression) indices[i]).getConstant();
									}
									else allIndicesAreInteger = false;
								}
								
								if(allIndicesAreInteger) {
									arrayVar = new ArrayVariable(arrayVar.getArrayNameOnly(),
											                     intIndices,
											                     arrayVar.getBaseDomain());
									rightExpression = new RelationalAtomExpression(arrayVar);
								}
								
							}
							
							if(this.normalisedModel.constantArrays.containsKey(arrayVar.getArrayNameOnly())) {
								rightExpression = flattenRelationalAtomExpression((RelationalAtomExpression) rightExpression);
							}
							else if(arrayVar.isIndexedBySomethingNotConstant()) {
						 
									arrayVar.setWillBeFlattenedToPartwiseElementConstraint(true);
									ElementConstraint elementConstraint = flattenToPartwiseElementConstraint(rightExpression.copy());
							
									if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
										leftExpression.willBeFlattenedToVariable(true);
									leftExpression = flattenExpression(leftExpression);
							
									addToSubExpressions(rightExpression, leftExpression);
									elementConstraint.setResultExpression(leftExpression);
									if(expression.isGonnaBeFlattenedToVariable()) {
										this.constraintBuffer.add(elementConstraint);
										return leftExpression;
									}
									else return elementConstraint;
							
								
							}
						}
					}
				
				}	
			}
		
		
			else if((leftExpression.getType() == Expression.INT_ARRAY_VAR ||
				leftExpression.getType() == Expression.BOOL_ARRAY_VAR) 
				&& !(rightExpression instanceof SimpleArrayVariable)) {
		
				if(leftExpression instanceof ArithmeticAtomExpression) {
					
					ArrayVariable arrayVar = (ArrayVariable) ((ArithmeticAtomExpression) leftExpression).getVariable();
					
					Expression[] indices = arrayVar.getExpressionIndices();
					
					if(indices != null) {
						
						int[] intIndices = new int[indices.length];
						boolean allIndicesAreInteger = true;
						
						for(int i=0; i<indices.length; i++) {
							indices[i] = flattenExpression(indices[i]);
							if(indices[i].getType() == Expression.INT) {
								intIndices[i] = ((ArithmeticAtomExpression) indices[i]).getConstant();
							}
							else allIndicesAreInteger = false;
						}
						
						if(allIndicesAreInteger) {
							arrayVar = new ArrayVariable(arrayVar.getArrayNameOnly(),
									                     intIndices,
									                     arrayVar.getBaseDomain());
							leftExpression = new ArithmeticAtomExpression(arrayVar);
						}
						
					}
					
					//if this array is a constant array -> just flatten it
					if(this.normalisedModel.constantArrays.containsKey(arrayVar.getArrayNameOnly())) {
						leftExpression = flattenArithmeticAtomExpression((ArithmeticAtomExpression) leftExpression);
						
					}
					
					else if(arrayVar.isIndexedBySomethingNotConstant()) {
						//ArrayVariable arrayVar = (ArrayVariable) ((ArithmeticAtomExpression) leftExpression).getVariable();							
							
							arrayVar.setWillBeFlattenedToPartwiseElementConstraint(true);
							ElementConstraint elementConstraint = flattenToPartwiseElementConstraint(leftExpression.copy());
						
							if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
								rightExpression.willBeFlattenedToVariable(true);
							rightExpression = flattenExpression(rightExpression);
						
							addToSubExpressions(leftExpression, rightExpression);
							elementConstraint.setResultExpression(rightExpression);
							if(expression.isGonnaBeFlattenedToVariable()) {
								this.constraintBuffer.add(elementConstraint);
								return rightExpression;
							}
							else return elementConstraint;
						
					}	
				}
				else if(leftExpression instanceof RelationalAtomExpression) {
					
					ArrayVariable arrayVar = (ArrayVariable) ((RelationalAtomExpression) leftExpression).getVariable();
					
					
					Expression[] indices = arrayVar.getExpressionIndices();
					
					if(indices != null) {
						
						int[] intIndices = new int[indices.length];
						boolean allIndicesAreInteger = true;
						
						for(int i=0; i<indices.length; i++) {
							indices[i] = flattenExpression(indices[i]);
							if(indices[i].getType() == Expression.INT) {
								intIndices[i] = ((ArithmeticAtomExpression) indices[i]).getConstant();
							}
							else allIndicesAreInteger = false;
						}
						
						if(allIndicesAreInteger) {
							arrayVar = new ArrayVariable(arrayVar.getArrayNameOnly(),
									                     intIndices,
									                     arrayVar.getBaseDomain());
							leftExpression = new RelationalAtomExpression(arrayVar);
						}
						
					}
					
					if(this.normalisedModel.constantArrays.containsKey(arrayVar.getArrayNameOnly())) {
						leftExpression = flattenRelationalAtomExpression((RelationalAtomExpression) leftExpression);
					}
					
					else if(arrayVar.isIndexedBySomethingNotConstant()) {
					
//						 if this array is a constant array -> just flatten it
							arrayVar.setWillBeFlattenedToPartwiseElementConstraint(true);
							ElementConstraint elementConstraint = flattenToPartwiseElementConstraint(leftExpression.copy());
						
							if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
								rightExpression.willBeFlattenedToVariable(true);
							rightExpression = flattenExpression(rightExpression);
							
							addToSubExpressions(leftExpression, rightExpression);
							elementConstraint.setResultExpression(rightExpression);
							if(expression.isGonnaBeFlattenedToVariable()) {
								this.constraintBuffer.add(elementConstraint);
								return rightExpression;
							}
							else return elementConstraint;
						
						
					}
				}
				
				
			}
		
			
		
		//4. just flatten the stuff dependeing on the implementation of the corresponding constraint
		//    in the target solver
		
		//if(expression.getOperator() == Expression.IFF)
	//		System.out.print("Flattening expression with left "+leftExpression+" and right expression: "+rightExpression);
		
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getOperator())) {
			leftExpression.willBeFlattenedToVariable(true);
			rightExpression.willBeFlattenedToVariable(true);
		}
		
		//leftExpression = flattenExpression(leftExpression);
		
		leftExpression = flattenExpression(leftExpression);
		
		if(leftAndRightAreEqual && leftExpression.getType() != Expression.INT && leftExpression.getType() != Expression.BOOL) {
			//if(expression.getOperator() == Expression.IFF)
			//System.out.println("Adding  left expression "+leftExpression+" as equal to right expression: "+rightExpression);
			//addToSubExpressions(rightExpression, leftExpression);
			addToEqualExpressions(rightExpression, leftExpression);
		}
		
		rightExpression = flattenExpression(rightExpression);
		
		//rightExpression = flattenExpression(rightExpression);
		
		if(leftExpression.toString().equals(rightExpression.toString()) && 
				(expression.getOperator() == Expression.EQ || expression.getOperator() == Expression.IFF)) {
			return new RelationalAtomExpression(true);
		}
		
		if(leftExpression.getType() == Expression.INT && rightExpression.getType() == Expression.INT)
			return (new CommutativeBinaryRelationalExpression(leftExpression, 
                    expression.getOperator(),
                    rightExpression)).evaluate();
		
		else if(leftExpression.getType() == Expression.BOOL && rightExpression.getType() == Expression.BOOL)
			return (new CommutativeBinaryRelationalExpression(leftExpression, 
                    expression.getOperator(),
                    rightExpression)).evaluate();
		
		
		
		
		//if(expression.getOperator() == Expression.EQ)
		//System.out.println("Flattened left expression of relation:"+leftExpression+"\nand right expression:"+rightExpression);
		
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
		throws TailorException,Exception {

		// if the target solver supports quantified sums - do nothing
		if(this.targetSolver.supportsConstraint(Expression.Q_SUM)) {
			return quantifiedSum;
		}
		
		// 	----- 1. get the domain over which the quantification ranges ------------
		Domain bindingDomain = quantifiedSum.getQuantifiedDomain();
		int domainType = bindingDomain.getType();
		if(domainType != Domain.BOOL &&
				domainType!= Domain.INT_BOUNDS &&
				domainType != Domain.INT_SPARSE &&
			     domainType != Domain.SINGLE_INT)
					throw new TailorException("Cannot unfold quantified expression '"+quantifiedSum
						+"'. The binding domain is not entirely constant:"+bindingDomain);
			
		int[] domainElements = null;
		if(domainType == Domain.BOOL)
			domainElements = ((BoolDomain) bindingDomain).getFullDomain();
			
		else if(domainType == Domain.INT_BOUNDS) 
			domainElements = ((BoundedIntRange) bindingDomain).getFullDomain();
			
		else if(domainType == Domain.SINGLE_INT)
			domainElements = ((SingleIntRange) bindingDomain).getFullDomain();
		
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
			Expression expression = unfoldedExpressions.remove(i).evaluate();
			
			//if(expression.getType() == Expression.INT) {
			//	if(((ArithmeticAtomExpression) expression).getConstant() != 0) {
			//		positiveElements.add(expression);
			//	}
			//}
			
			//if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.SUM)) 
			//	expression.willBeFlattenedToVariable(true);
			
			//expression = flattenExpression(expression);
			
			//if(!quantifiedSum.isNested())
			//	expression.setIsNotNested();
			
			if(expression instanceof UnaryMinus) 
				negativeElements.add(( (UnaryMinus) expression).getArgument());
			else positiveElements.add(expression);
		}
		
		Sum unfoldedSum = new Sum(positiveElements,
				                   negativeElements);
		
	
		unfoldedSum.orderExpression();
		Expression reducedSum  = unfoldedSum.evaluate();
		reducedSum = reducedSum.reduceExpressionTree();
		reducedSum.orderExpression();
	
		if(!quantifiedSum.isNested()) 
			reducedSum.setIsNotNested();
		
		if(quantifiedSum.isGonnaBeFlattenedToVariable())
			reducedSum.willBeFlattenedToVariable(true);
		
		if(reducedSum instanceof Sum)
			return flattenSum((Sum) reducedSum);
		else return flattenExpression(reducedSum);
		
	}
	
	
	/**
	 * Flatten the element constraint. 
	 * 
	 * @param elementConstraint
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenElementConstraint(ElementConstraint elementConstraint) 
		throws TailorException,Exception {
		
		//System.out.println("Flattening elem expression:"+elementConstraint);
		
		// 1. flatten subexpressions
		boolean noConstraintsAsArguments = !this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT);
		Expression[] arguments = elementConstraint.getArguments();
		for(int i=0; i<arguments.length; i++) {
			if(noConstraintsAsArguments) 
				arguments[i].willBeFlattenedToVariable(true);

			arguments[i] = flattenExpression(arguments[i]);
		}
		
		if(arguments.length != 3)
			throw new TailorException("Internal error. Element constraint has less/more than 3 arguments.");
		elementConstraint = new ElementConstraint(arguments[0], arguments[1], arguments[2]);
		
		//System.out.println("Flattenend element constraint to:"+elementConstraint);
		
		// 2. if the constraint has to be reified	
		if(elementConstraint.isGonnaBeFlattenedToVariable()) {
			this.constraintBuffer.add(elementConstraint);
			elementConstraint.willBeFlattenedToVariable(false);
			return elementConstraint.getValueExpression();
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
		throws TailorException,Exception {
		
		// 1. ---- first flatten the arguments ----------------------
		ArrayList<Expression> arguments = disjunction.getArguments();
		for(int i=arguments.size()-1; i>=0; i--) {
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.OR))
				arguments.get(i).willBeFlattenedToVariable(true);
			
			Expression argument = flattenExpression(arguments.remove(i));
			if(argument.getType() == Expression.BOOL) {
				if(((RelationalAtomExpression) argument).getBool())
					return new RelationalAtomExpression(true);
			}
			else arguments.add(i, argument);
			
		}
		
		if(arguments.size() == 0)
			return new RelationalAtomExpression(false);
		
		
		// 2. --- if the conjunction is nested, then just return it if n-ary conjunction is 
		//        is supported by the target solver
		if(this.targetSolver.supportsConstraint(Expression.NARY_DISJUNCTION)) {
			if(disjunction.isGonnaBeFlattenedToVariable()) {
				
				// if we just have 1 argument, it has to be true to hold
				if(disjunction.getArguments().size() == 1) {
					Expression e = disjunction.getArguments().get(0);
					e.willBeFlattenedToVariable(true);
					return flattenExpression(e);
				}
					
				return reifyConstraint(disjunction);
			}
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
		throws TailorException,Exception {
	
		
		Expression equalExpression = null;
		
		if(conjunction.isGonnaBeFlattenedToVariable()) {
			if(hasEqualSubExpression(conjunction)) {
				equalExpression = getEqualSubExpression(conjunction);
				//this.usedEqualSubExpressions--; // this does not count as re-using a subexpression
			}
		}
		
		//System.out.println("This conjunction :"+conjunction+"\nwill be flattened to a variable?:"+conjunction.isGonnaBeFlattenedToVariable());
		
		// 1. ---- first flatten the arguments ----------------------
		ArrayList<Expression> arguments = conjunction.getArguments();
		for(int i=arguments.size()-1; i>=0; i--) {
			
			if(conjunction.isGonnaBeFlattenedToVariable() &&
				!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.AND))
				arguments.get(i).willBeFlattenedToVariable(true);
			
			if(!conjunction.isNested())
				arguments.get(i).setIsNotNested();
			
			Expression flatArgument;
			Expression constraint = arguments.remove(i);
			
			if(this.targetSolver.supportsFeature(TargetSolver.NESTED_LINEAR_EXPRESSIONS) &&
					constraint.isLinearExpression()) {
				flatArgument = constraint;
			}
			else flatArgument = flattenExpression(constraint); //.evaluate();
			
			if(flatArgument.getType() == Expression.BOOL) {
				if(!((RelationalAtomExpression) flatArgument).getBool())
					return new RelationalAtomExpression(false);
			}
			else arguments.add(i, flatArgument);
		}
		
		if(arguments.size() == 0)
			return new RelationalAtomExpression(true);
		
		// 2. ---- if the conjunction is not nested/will be reified
		//         then split it into independent constraints
		if(!conjunction.isGonnaBeFlattenedToVariable()) {
			for(int i=arguments.size()-1; i>0; i--) {
				Expression e  = arguments.remove(i);
				e.orderExpression();
				this.constraintBuffer.add(e);
			}
			if(arguments.size() == 1)
				return arguments.remove(0);
			else return new RelationalAtomExpression(true);
		}
		
		// 2. --- if the conjunction is nested, then just return it if n-ary conjunction is 
		//        is supported by the target solver
		else {
			if(conjunction.getArguments().size() == 1) {
				Expression e = conjunction.getArguments().get(0);
				e.willBeFlattenedToVariable(true);
				return flattenExpression(e);
			}
			
			// if the conjunction has changed so much during flattening,
			// we need to add it again to the subexpressions
			if(equalExpression != null) { 
				if(!hasEqualSubExpression(conjunction)) {
					addToEqualExpressions(conjunction, equalExpression);
				}
			}
			
			//System.out.println("Flattened conjunction to "+conjunction+" and gonna reify it");
			
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
		throws TailorException,Exception {
		
		Expression argument = expression.getArgument();
	
		/*  NEGATION  */
		if(expression.getType() == Expression.NEGATION) {
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.NEGATION)) 
				argument.willBeFlattenedToVariable(true);
			this.flatteningNegatedArgument = true;
			argument = flattenExpression(argument);
			this.flatteningNegatedArgument = false;
			
			// we have to flatten !E to an aux variable: aux != E
			if(expression.isGonnaBeFlattenedToVariable()) {	
				ArithmeticAtomExpression auxVariable = null;
				
				// if we have a common subexpression, use the corresponding variable
				if(hasCommonSubExpression(expression)) 
					auxVariable = getCommonSubExpression(expression);
					
				else if(hasEqualSubExpression(expression)) {
					auxVariable = getEqualSubExpression(expression);
					addToSubExpressions(expression, auxVariable);
		
					// add the flattened constraint to the constraint buffer
					this.constraintBuffer.add(new CommutativeBinaryRelationalExpression(argument,
																					   Expression.NEQ,
							                                                           auxVariable.toRelationalAtomExpression()));
				}
				// if we have no common subexpression, create a new auxiliary variable
				// and add the constraint to the list of subexpressions
				else  {
					auxVariable = new ArithmeticAtomExpression(createAuxVariable(0, 1));
					addToSubExpressions(expression, auxVariable);
				    
					// add the flattened constraint to the constraint buffer
					this.constraintBuffer.add(new CommutativeBinaryRelationalExpression(argument,
							   															Expression.NEQ,
							   															auxVariable.toRelationalAtomExpression()));
				}
				
				return auxVariable.toRelationalAtomExpression();
			} // end if: we need to flatten to a variable
			else return new Negation(argument);	
		    
			
		} // end: if(negation)
		
		
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
		throws TailorException,Exception {
		
		
		if(this.applyStrongCopyPropagation) {
			if(hasCommonSubExpression(atom))
				return getCommonSubExpression(atom);	
		}
		
		//System.out.println("Flattening the relational array element "+atom+" of type "+atom.getType());
		
		if(atom.getType() == Expression.BOOL_ARRAY_VAR) {
			Variable arrayVariable = atom.getVariable();
			if(arrayVariable.getType() == Expression.ARRAY_VARIABLE) {
				Expression[] indices = ((ArrayVariable) arrayVariable).getExpressionIndices();
				
				
				
				// if we have an array element indexed by something that is not a single integer
				if(indices != null) {
					
					//System.out.println("The array element "+atom+" is indexed by something that is not an integer");
					
					int[] intIndices = new int[indices.length];
					boolean allIndicesAreInts = true;
					
					// if the indices are constant arrays -> insert constant value for index by flattening
					for(int i=0; i<indices.length; i++) {
						indices[i] = flattenExpression(indices[i]).evaluate();
						if(indices[i].getType() == Expression.INT) {
							intIndices[i] = ((ArithmeticAtomExpression) indices[i]).getConstant();
						}
						else allIndicesAreInts = false;
					}
					
					
					if(allIndicesAreInts) {
						//System.out.println("Flattened nasty atom to expression: "+new RelationalAtomExpression(new ArrayVariable(((ArrayVariable) arrayVariable).getArrayNameOnly(),
                          //      intIndices,
                            //    ((ArrayVariable) arrayVariable)	.getBaseDomain())));
						
						return new RelationalAtomExpression(new ArrayVariable(((ArrayVariable) arrayVariable).getArrayNameOnly(),
								                                              intIndices,
								                                              ((ArrayVariable) arrayVariable)	.getBaseDomain()));
					}
					
					
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
		throws TailorException,Exception {
		
		
		if(quantification.getType() == Expression.FORALL && 
				this.targetSolver.supportsConstraint(Expression.FORALL) &&
				          !quantification.isGonnaBeFlattenedToVariable())
			return quantification;
		
		if(quantification.getType() == Expression.EXISTS && 
				this.targetSolver.supportsConstraint(Expression.EXISTS) &&
				          !quantification.isGonnaBeFlattenedToVariable())
			return quantification;
		
		
		
		// ----- 1. get the domain over which the quantification ranges ------------
		Domain bindingDomain = quantification.getQuantifiedDomain();
		int domainType = bindingDomain.getType();
		//System.out.println("Gonna flatten the quantification:"+quantification+" with domain type:"+domainType);
		if(domainType != Domain.BOOL &&
			domainType!= Domain.INT_BOUNDS &&
			  domainType != Domain.INT_SPARSE &&
			     domainType != Domain.SINGLE_INT)
			throw new TailorException("Cannot unfold quantified expression '"+quantification
					+"'. The binding domain is not entirely constant:"+bindingDomain);
		
		int[] domainElements = null;
		if(domainType == Domain.BOOL)
			domainElements = ((BoolDomain) bindingDomain).getFullDomain();
		
		else if(domainType == Domain.INT_BOUNDS) 
			domainElements = ((BoundedIntRange) bindingDomain).getFullDomain();
		
		else if(domainType == Domain.SINGLE_INT)
			domainElements = ((SingleIntRange) bindingDomain).getFullDomain();
		
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
	
		
		// 4 -------- inserting and evaluating the stuff
		for(int i=unfoldedExpressions.size()-1; i>=0; i--) {
			Expression unfoldedExpression = unfoldedExpressions.remove(i);
			unfoldedExpression = this.insertConstantArraysInExpression(unfoldedExpression);
			//System.out.println("Unfolded expression before evaluation:"+unfoldedExpression);
			unfoldedExpression.orderExpression();
			unfoldedExpression = unfoldedExpression.evaluate();
			//System.out.println("Unfolded expression after evaluation:"+unfoldedExpression);
			unfoldedExpression = unfoldedExpression.reduceExpressionTree();
			
			if(!quantification.isNested())
				unfoldedExpression.setIsNotNested();
			
			unfoldedExpressions.add(i,unfoldedExpression);
		}
		
	
		// universal quantification 
		if(quantification.getType() == Expression.FORALL) {
			if(unfoldedExpressions.size() == 1 ) {
				Expression e = unfoldedExpressions.remove(0);
				if(quantification.isGonnaBeFlattenedToVariable())
					e.willBeFlattenedToVariable(true);
				
				return flattenExpression(e);
			}
			
			Conjunction conjunction = new Conjunction(unfoldedExpressions);
			conjunction.reduceExpressionTree();
			
			//System.out.println("Flattening UNIVERSAL quantification that "+(quantification.isGonnaBeFlattenedToVariable()? " " : "NOT ")
			//		+" will be refied to:"+conjunction);
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.AND)
					&& quantification.isGonnaBeFlattenedToVariable()) {
				for(int i=conjunction.getArguments().size()-1; i>=0; i--) 
					conjunction.getArguments().get(i).willBeFlattenedToVariable(true);
			}
	 		
			if(quantification.isGonnaBeFlattenedToVariable())
				conjunction.willBeFlattenedToVariable(true);
			
			if(!quantification.isNested()) 
				conjunction.setIsNotNested();
	
			
			if(conjunction.getArguments().size() == 1)
				return flattenExpression(conjunction.getArguments().get(0));
			
			return flattenConjunction(conjunction);
			
		}
		// existential quantification
		else {
			if(unfoldedExpressions.size() == 1 ) {
				Expression e = unfoldedExpressions.remove(0);
				if(quantification.isGonnaBeFlattenedToVariable())
					e.willBeFlattenedToVariable(true);
				
				if(!quantification.isNested())
					e.setIsNotNested();
				
				return flattenExpression(e);
			}
				
			
			Disjunction disjunction = new Disjunction(unfoldedExpressions);
			//System.out.println("got disjunction: "+disjunction);
			disjunction.reduceExpressionTree();
			
			if(quantification.isGonnaBeFlattenedToVariable())
				disjunction.willBeFlattenedToVariable(true);
			
			if(!quantification.isNested())
				disjunction.setIsNotNested();
			
			if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.OR)
					&& quantification.isGonnaBeFlattenedToVariable())
				for(int i=disjunction.getArguments().size()-1; i>=0; i--) 
					disjunction.getArguments().get(i).willBeFlattenedToVariable(true);
			
			//System.out.println("got disjunction: "+disjunction);
			
			if(disjunction.getArguments().size() == 1)
				return flattenExpression(disjunction.getArguments().get(0));
			
			return flattenDisjunction(disjunction);
		}
		
	}
	
	
	
	
	/**
	 * This is a helper function for flattening quantified expressions (the unfolding of the quantification). The variablelist containts all binding 
	 * variables that should be inserted intp the expression.ArrayList<Expression> unfoldedExpressions = new ArrayList<Expression>();
	 * 
	 * @param variableList
	 * @param values
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private ArrayList<Expression> insertVariablesForValues(ArrayList<String> variableList, int[] values, Expression expression)
		throws TailorException,Exception {
	
		
		// this is the last variable we have to insert
		if(variableList.size() == 1) {
			String variableName = variableList.get(0);
			ArrayList<Expression> unfoldedExpressions = new ArrayList<Expression>();
			for(int i=0; i<values.length; i++) {
				Expression unfoldedExpression = expression.copy().insertValueForVariable(values[i], variableName);

				unfoldedExpressions.add(unfoldedExpression);
			}
			
			return unfoldedExpressions;
		}
		// we have some more variables to insert into the expression
		else {
			String variableName = variableList.remove(0);
			ArrayList<Expression> unfoldedExpressions = new ArrayList<Expression>();
			for(int i=0; i<values.length; i++) {
				
				// copy the variable list since it is modified on lower level
				ArrayList<String> copiedVariableList = new ArrayList<String>();
				for(int j=0; j<variableList.size();j++)
					copiedVariableList.add(new String(variableList.get(j)));
				
				
				Expression unfoldedExpression = expression.copy().insertValueForVariable(values[i], variableName);
				ArrayList<Expression> furtherUnfoldedExpressions = insertVariablesForValues(copiedVariableList, values, unfoldedExpression);
				
				for(int j=0; j<furtherUnfoldedExpressions.size(); j++) {
					
					
					unfoldedExpressions.add(furtherUnfoldedExpressions.get(j));//.evaluate());
			    }
			}
			
			return unfoldedExpressions;
		}
		
	
		
		
	}
	
	

	/**
	 * 
	 * @param expression
	 * @return
	 */
	private Expression flattenArithmeticExpression(ArithmeticExpression expression) 
		throws TailorException, Exception {
		
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
		
		if(expression instanceof NonCommutativeArithmeticBinaryExpression) {
			return flattenNonCommArithm((NonCommutativeArithmeticBinaryExpression) expression);
		}
		
		return expression;
	}
	
	
	/**
	 * Flattens a non-linear expression to the target solver.
	 * 
	 * @param e
	 * @return
	 * @throws TailorException
	 * @throws Exception
	 */
	private Expression flattenNonCommArithm(NonCommutativeArithmeticBinaryExpression e)
		throws TailorException, Exception {
		
		//System.out.println("1 Expression 's type:"+e.getType()+" of "+e.toString()+" and class:"+e.getClass());
		
		Expression leftArgument = e.getLeftArgument();
		Expression rightArgument = e.getRightArgument();
		
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(e.getType())) {
			leftArgument.willBeFlattenedToVariable(true);
			rightArgument.willBeFlattenedToVariable(true);
		}
		
		Expression aux;
		
		if(this.hasCommonSubExpression(e)) {
			aux = this.getCommonSubExpression(e);
			return aux;
		}
		
		aux = new ArithmeticAtomExpression(this.createAuxVariable(e.getDomain()[0], e.getDomain()[1]));
		this.addToSubExpressions(e, aux);
		
		//System.out.println("Expression 's type:"+e.getType()+" of "+e.toString()+" and class:"+e.getClass());
		
		leftArgument = flattenExpression(leftArgument);
		rightArgument = flattenExpression(rightArgument);
		
		BinaryNonLinearConstraint nonLinConstraint = new BinaryNonLinearConstraint(leftArgument,
																					e.getType(),
																					rightArgument,
																					aux);
		//System.out.println("Constructed new constraint "+nonLinConstraint+" from "+e+" with type:"+e.getType());
		this.constraintBuffer.add(nonLinConstraint);
		return aux;
	}
	
	
	
	/**
	 * Flattens a multiplication, similar to the flattening of a sum.
	 * 
	 * @param multiplication
	 * @return
	 * @throws TailorException
	 */
	private Expression flattenMultiplication(Multiplication multiplication) 
		throws TailorException,Exception {
		
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
			//System.out.println("We have to flatten multiplications to binary mults");
			if(multiplication.isGonnaBeFlattenedToVariable()) {
				//System.out.println("We have to flatten the multiplication:"+multiplication);
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
				//System.out.println("We have flattened the multiplication:"+multiplication+" to product constraint"+productConstraint);
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
	throws TailorException,Exception {

		
		
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
		throws TailorException,Exception  {
		
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
		throws TailorException,Exception {
	
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
			//if(!sum.isGonnaBeFlattenedToVariable()) {
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
				
				
				int lb = sum.getDomain()[0];
				int ub = sum.getDomain()[1];//sumConstraint.getSumDomain()[1];
				
				//System.out.println("Flattening sum to aux var with domain:"+lb+".."+ub);
				
				ArithmeticAtomExpression auxVariable = new ArithmeticAtomExpression(createAuxVariable(lb,ub));
				sumConstraint.setResult(auxVariable, 
						                Expression.EQ, 
						                true);
				//return reifyConstraint(sumConstraint);
				this.constraintBuffer.add(sumConstraint);
                addToSubExpressions(sum,auxVariable);
				return auxVariable;
			}
			
			
		}
	}
	
	private Sum flattenQuantifiedSumArguments(Sum sum) 
		throws TailorException,Exception {
		
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
		throws TailorException,Exception {
		
		
		
		if(this.targetSolver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT)) {
			
			// 1. flatten the sum arguments and convert them to Expression-Arrays
			Expression[] positiveArguments = new Expression[sum.getPositiveArguments().size()];
			Expression[] negativeArguments = new Expression[sum.getNegativeArguments().size()];
			
			boolean flattenToVariable = !(this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.NARY_SUMEQ_CONSTRAINT));
			
				for(int i=sum.getPositiveArguments().size()-1; i>=0; i--) {
					//System.out.println("Matching "+auxVariables.get(i).getVariableName()+" to "+variableName);
					Expression argument = sum.getPositiveArguments().get(i);
					
					// only flatten argument if it is non-linear - if the argument is x*Constant, then
					// we can leave it and translate the rest to a weighted sum
					if(argument instanceof Multiplication) {
						Multiplication mult = (Multiplication) argument;
						if(mult.isNonLinearMultiplication())
							argument.willBeFlattenedToVariable(flattenToVariable);
						// else do nothing
					}
					/*else if(argument instanceof UnaryMinus) {
						UnaryMinus uminus = (UnaryMinus) argument;
						if(uminus.getArgument() instanceof Multiplication) {
							Multiplication mult = (Multiplication) argument;
							if(mult.isNonLinearMultiplication())
								argument.willBeFlattenedToVariable(flattenToVariable);
						}
						else argument.willBeFlattenedToVariable(flattenToVariable);
					}*/
					else {
						argument.willBeFlattenedToVariable(flattenToVariable);
					}
					
					positiveArguments[i] = flattenExpression(argument);
					
					//System.out.println("sum arguments AFTER flattening of sum that will be part of SumConstraint:"+positiveArguments[i]);
				}
			
				for(int i=sum.getNegativeArguments().size()-1; i>=0; i--) {
					Expression argument = sum.getNegativeArguments().get(i);
					
					// only flatten argument if it is non-linear - if the argument is x*Constant, then
					// we can leave it and translate the rest to a weighted sum
					if(argument instanceof Multiplication) {
						Multiplication mult = (Multiplication) argument;
						if(mult.isNonLinearMultiplication())
							argument.willBeFlattenedToVariable(flattenToVariable);
						// else do nothing
					}
					else 
						argument.willBeFlattenedToVariable(flattenToVariable);
					
					negativeArguments[i] = flattenExpression(argument);
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
	throws TailorException,Exception {
	
		
	Expression objective = this.normalisedModel.getObjectiveExpression();
	if(objective == null)
		return objective;
	
	if(this.targetSolver.supportsFeature(TargetSolver.SUPPORTS_OBJECTIVE)) {
		
		if(this.targetSolver.supportsFeature(TargetSolver.CONSTRAINT_OBJECTIVE))
			return flattenExpression(objective);
		
		else {
			//System.out.println("Flattening objective to variable!");
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
		throws TailorException,Exception {
		
		Expression argument = expression.getArgument();
		if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(expression.getType()))
			argument.willBeFlattenedToVariable(true);
		
		argument = flattenExpression(argument);
		argument = argument.evaluate();
		
		//System.out.println("Flattened expression '"+expression+"' 's argument to: "+argument);
		
		if(expression instanceof UnaryMinus) {
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				UnaryMinus uminusExpr = new UnaryMinus(argument);
				ArithmeticAtomExpression aux;
				
				if(hasCommonSubExpression(uminusExpr))
					aux = getCommonSubExpression(uminusExpr);
				else { 
					aux = new ArithmeticAtomExpression(this.createAuxVariable(uminusExpr.getDomain()[0], uminusExpr.getDomain()[1]));
					this.addToSubExpressions(uminusExpr, aux);
				}
				
				
				
				this.constraintBuffer.add(new CommutativeBinaryRelationalExpression(aux, Expression.EQ, uminusExpr));
				return aux;
			} 
			
			// expression is not flattened to a variable
			else 
				return new UnaryMinus(argument);
		}
			
		else if(expression instanceof AbsoluteValue) {
		
			if(expression.isGonnaBeFlattenedToVariable()) {
				ArithmeticAtomExpression aux;
				
				if(hasCommonSubExpression(expression))
					return getCommonSubExpression(expression);
				else { 
					aux = new ArithmeticAtomExpression(this.createAuxVariable(expression.getDomain()[0], 
																	          expression.getDomain()[1]));
					this.addToSubExpressions(expression, aux);
				}	
				
				this.constraintBuffer.add(new AbsoluteConstraint(argument, 
																  aux));
				return aux;
			} 
			
			// expression is not flattened to a variable
			else 
				return new AbsoluteValue(argument);
		}
			
		
		else if(expression instanceof Minimum) {
			
			Minimum e = (Minimum) expression;
			
			if(expression.isGonnaBeFlattenedToVariable()) {
				ArithmeticAtomExpression aux;
				
				if(hasCommonSubExpression(expression))
					return getCommonSubExpression(expression);
				else { 
					aux = new ArithmeticAtomExpression(this.createAuxVariable(expression.getDomain()[0], 
																	          expression.getDomain()[1]));
					this.addToSubExpressions(expression, aux);
				}	
				
				if(argument instanceof Array) {				
					this.constraintBuffer.add(new MinimumConstraint((Array) argument, 
					  											     aux,
																     e.isMaximum()));
				}
				else throw new TailorException("Internal error. Flattened array to "+argument+
						" that is not of type array but:"+argument.getClass().getName());
				
				return aux;
				
				
			}
			else return new Minimum(argument,e.isMaximum());
		}
		
		else throw new TailorException("Unknown unary arithmetic constraint:"+expression);
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
		throws TailorException, Exception {
		
		if(this.applyStrongCopyPropagation) {
			if(hasCommonSubExpression(atom))
				return getCommonSubExpression(atom);	
		}
		
		//System.out.println("1 Atom "+atom+" is gonna be flattened to a variuabe? "+atom.isGonnaBeFlattenedToVariable());
		//System.out.println("\n1 Flattening atom:"+atom+" with type:"+atom.getType());
		
		if(atom.getType() == Expression.INT_ARRAY_VAR) {
			Variable variable = atom.getVariable();
			variable = (Variable) variable.evaluate();
			
			
			if(variable.getType() == Expression.ARRAY_VARIABLE) {
				ArrayVariable arrayVariable = (ArrayVariable) variable;
				Expression[] indices = arrayVariable.getExpressionIndices();
				
				
				
				
					//System.out.println("constant arrays: "+this.normalisedModel.constantArrays);
					//System.out.println("The array element '"+atom+"' is a constant array?"+
					//		this.normalisedModel.constantArrays.containsKey( arrayVariable.getArrayNameOnly() )
					//		+"\n with name:"+arrayVariable.getArrayNameOnly());
				
				// ---------- if this is a constant variable --------------------------------------------------------
				if(this.normalisedModel.constantArrays.containsKey( arrayVariable.getArrayNameOnly() )) {
		
					ConstantArray constArray = this.normalisedModel.constantArrays.get(arrayVariable.getArrayNameOnly());
				
					int[] intIndices = null;
					
					// the indices might be another constant-array...
					if(indices != null) {
						
						intIndices = new int[indices.length];
						boolean allAreInteger = true;
						
						for(int i=0; i<indices.length; i++) {
							Expression newIndex = flattenExpression(indices[i]).evaluate();
							if(newIndex.getType() == Expression.INT) {
								intIndices[i] = ((ArithmeticAtomExpression) newIndex).getConstant();
							}
							else allAreInteger = false;
						}
						
						if(!allAreInteger) 
							throw new TailorException("Could not tailor indexed constant array '"+atom+
									"' to a constant, because not all indices could be derived to a constant value:"+indices);
						
					}
					else intIndices = arrayVariable.getIntegerIndices();
					
					
					// we have a vector
					if(intIndices.length == 1) {
						if(constArray instanceof ConstantVector) {
							ConstantVector vector = (ConstantVector) constArray;
							int rowOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(vector.getArrayName(), 0);
							return new ArithmeticAtomExpression(vector.getElementAt(intIndices[0]-rowOffsetFromZero));
						}
						else throw new TailorException("Illegal index dimensions of constant array element '"+atom+
								"' that dereferences the array '"+constArray+
								"'.\nPlease make sure you have an index for every dimension of the array.");
					}
					// matrix
					else if(intIndices.length == 2) {
						if(constArray instanceof ConstantMatrix) {
							ConstantMatrix matrix = (ConstantMatrix) constArray;
							//System.out.println("Flattening constant matrix variable "+atom+" to "+matrix.getElementAt(intIndices[0]-this.CONSTANT_ARRAY_OFFSET_FROM_ZERO, 
                             //       intIndices[1]-this.CONSTANT_ARRAY_OFFSET_FROM_ZERO));
							int rowOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(matrix.getArrayName(), 0);
							int colOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(matrix.getArrayName(), 1);
							
							return new ArithmeticAtomExpression(matrix.getElementAt(intIndices[0]-rowOffsetFromZero, 
									                                                intIndices[1]-colOffsetFromZero));
							
						}
						else throw new TailorException("Illegal index dimensions of constant array element '"+atom+
								"' that dereferences the array '"+constArray+
								"'.\nPlease make sure you have an index for every dimension of the array.");
						
					}
					// cube
					//else if(intIndices.length == 3) 
					else throw new TailorException("Cannot tailor constant elements with more than 2 dimensions yet, sorry:"+atom);
					
				}
				
				
				
				// --------- if we have an array element indexed by something that is not a single integer -----------
				if(indices != null) {
					
					//System.out.println("The array element "+atom+" is indexed by something that is not an integer");
					
					int[] intIndices = new int[indices.length];
					boolean allIndicesAreInts = true;
					
					// if the indices are constant arrays -> insert constant value for index by flattening
					for(int i=0; i<indices.length; i++) {
						indices[i] = flattenExpression(indices[i]).evaluate();
						if(indices[i].getType() == Expression.INT) {
							intIndices[i] = ((ArithmeticAtomExpression) indices[i]).getConstant();
						}
						else allIndicesAreInts = false;
					}
					
					
					if(allIndicesAreInts) {
						return new ArithmeticAtomExpression(new ArrayVariable(arrayVariable.getArrayNameOnly(),
								                                              intIndices,
								                                              arrayVariable.getBaseDomain()));
					}
					
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
							index = flattenExpression(index);
							
							Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
							if(!(domain instanceof ArrayDomain) && !(domain instanceof ConstantArrayDomain))
								throw new TailorException
								("Indexed variable '"+arrayVariable.getArrayNameOnly()+"' is not an array:"+atom);
							//ArithmeticAtomExpression auxVariable = new ArithmeticAtomExpression(createAuxVariable(0,1));
							//creating aux variable
							
							ArithmeticAtomExpression auxVariable = null;
							if(hasCommonSubExpression(atom)) {
								auxVariable = getCommonSubExpression(atom);
							}
							else {
								//System.out.println("Domain "+domain+" is of type:"+domain.getType());
								//domain = domain.evaluate(); 
								//System.out.println("Domain "+domain+" is of type (after evaluation):"+domain.getType());
								
								if(!(domain instanceof ConstantArrayDomain))
										throw new TailorException("Cannot index array element '"+atom+
												"' because the indexed variable's domain '"+domain+"' is not a constant array domain.");
								ConstantDomain baseDomain = (ConstantDomain) ((ConstantArrayDomain) domain).getBaseDomain();
							
								int lowerBound = baseDomain.getRange()[0];
								int upperBound = baseDomain.getRange()[1];
								auxVariable = new ArithmeticAtomExpression(createAuxVariable(lowerBound, upperBound));
								addToSubExpressions(atom,auxVariable);
							}
							
							
							//System.out.println("Domain of variable "+arrayVariable.getArrayNameOnly()+" is :"+domain.toString());
							
							
							Domain[] indexDs = null;
							if(domain instanceof ArrayDomain)
								indexDs = ((ArrayDomain) domain).getIndexDomains();
							else if(domain instanceof ConstantArrayDomain)
								indexDs = ((ConstantArrayDomain) domain).getIndexDomains();
							
							BasicDomain[] indexDomains = new BasicDomain[indexDs.length];
							for(int i=0; i<indexDs.length; i++) {
								if(!(indexDs[i] instanceof BasicDomain))
									throw new TailorException("");
								indexDomains[i] = (BasicDomain) indexDs[i];
							}
							
							SimpleArray array = new SimpleArray(arrayVariable.getArrayNameOnly(),
									                            indexDomains,
									                            arrayVariable.getBaseDomain());
							if(atom.isGonnaBeFlattenedToVariable()) {
								this.constraintBuffer.add(new ElementConstraint(array,
															  index,
															  auxVariable));
								return auxVariable;
							}
							else return new ElementConstraint(array,
															  index,
															  auxVariable);
						}
						else if(indices.length == 2) {
							Expression rowIndexExpression = indices[0].evaluate();
							Expression colIndexExpression = indices[1].evaluate();
							
							//System.out.println("Flattening expression with row:"+rowIndexExpression+" and col:"+colIndexExpression);
							
							// ------------- 1.case:  matrix[row:int, colExpr:E] ---> element(m[row,..], colExpr, aux) -----
							if(rowIndexExpression.getType() == Expression.INT) {
								
								int row = ((ArithmeticAtomExpression) rowIndexExpression).getConstant();
								Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
								domain = domain.evaluate();
								
								if(domain instanceof ConstantArrayDomain) {
								    //	 creating m[row,..]   (indexedArray)
									BasicDomain[] arrayIndices = new BasicDomain[2];
									
									//Domain varDomain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
									//if(!(varDomain instanceof ConstantArrayDomain))
									//	throw new TailorException("Cannot flatten expression '"+atom+"' because I cannot compute the domain offset from zero of domain:"+domain);
									//int offsetFromZero = ((ConstantArrayDomain) varDomain).getOffsetFromZeroAt(0);
									
									arrayIndices[0] = new SingleIntRange(row); //-offsetFromZero);
									arrayIndices[1] = ((ConstantArrayDomain) domain).getIndexDomains()[1]; 
									IndexedArray indexedArray = new IndexedArray(arrayVariable.getArrayNameOnly(),
											                                     arrayIndices,
											                                     ((ConstantArrayDomain) domain).getBaseDomain());
									// creating aux variable
									ArithmeticAtomExpression auxVariable = null;
									if(hasCommonSubExpression(atom)) {
										auxVariable = getCommonSubExpression(atom);
									}
									else {
										if(!(domain instanceof ConstantArrayDomain))
											throw new TailorException("Cannot index array element '"+atom+
													"' because the indexed variable's domain '"+domain+"' is not a constant array domain.");
										ConstantDomain baseDomain = (ConstantDomain) ((ConstantArrayDomain) domain).getBaseDomain();
								
										int lowerBound = baseDomain.getRange()[0];
										int upperBound = baseDomain.getRange()[1];
										auxVariable = new ArithmeticAtomExpression(createAuxVariable(lowerBound, upperBound));
										addToSubExpressions(atom,auxVariable);
									}
									
									// prepare  index expression
									if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
										colIndexExpression.willBeFlattenedToVariable(true);
									colIndexExpression = flattenExpression(colIndexExpression);
									
									// return element constraint
									if(atom.isGonnaBeFlattenedToVariable()) {
										this.constraintBuffer.add(new ElementConstraint(indexedArray,
											                           colIndexExpression,
											                           auxVariable));
										return auxVariable;
										
									}
									else return new ElementConstraint(indexedArray,
											                           colIndexExpression,
											                           auxVariable);
									
								}
								else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
										+"the domain of variable "+arrayVariable+" is not constant.");
							}
							
							
							// ------------2.case: matrix[rowExpr:E, col:int]   ---> element(m[..,col],  rowExpr, aux) ---------
							else if(colIndexExpression.getType() == Expression.INT) {
								int col = ((ArithmeticAtomExpression) colIndexExpression).getConstant();
								Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
								domain = domain.evaluate();
								//System.out.println("Domain "+domain+" is the domain of variable: "+arrayVariable.getArrayNameOnly()+
								//		" and it is of Type: "+domain.getType());
								
								if(domain instanceof ConstantArrayDomain) {
								    //	 creating m[row,..]   (indexedArray)
									BasicDomain[] arrayIndices = new BasicDomain[2];
									//Domain varDomain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
									//if(!(varDomain instanceof ConstantArrayDomain))
									//	throw new TailorException("Cannot flatten expression '"+atom
									//			+"' because I cannot compute the domain offset from zero of domain:"+domain);
									//int offsetFromZero = ((ConstantArrayDomain) varDomain).getOffsetFromZeroAt(1);
							
									arrayIndices[0] = ((ConstantArrayDomain) domain).getIndexDomains()[0]; 
									arrayIndices[1] = new SingleIntRange(col); //-offsetFromZero);
									IndexedArray indexedArray = new IndexedArray(arrayVariable.getArrayNameOnly(),
											                                     arrayIndices,
											                                     ((ConstantArrayDomain) domain).getBaseDomain());
									// creating aux variable
									ArithmeticAtomExpression auxVariable = null;
									if(hasCommonSubExpression(atom)) {
										auxVariable = getCommonSubExpression(atom);
									}
									else {
										if(!(domain instanceof ConstantArrayDomain))
											throw new TailorException("Cannot index array element '"+atom+
													"' because the indexed variable's domain '"+domain+"' is not a constant array domain.");
										ConstantDomain baseDomain = (ConstantDomain) ((ConstantArrayDomain) domain).getBaseDomain();
								
										int lowerBound = baseDomain.getRange()[0];
										int upperBound = baseDomain.getRange()[1];
										auxVariable = new ArithmeticAtomExpression(createAuxVariable(lowerBound, upperBound));
										
										addToSubExpressions(atom,auxVariable);
									}
									
									//System.out.println("have an atom expression: "+atom+" with INT col:"+col+" and row:"+rowIndexExpression);
									
									// prepare  index expression
									if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT)) {
										rowIndexExpression.willBeFlattenedToVariable(true);		
										//System.out.println("We have to flatten the row index expression to a variable");
									}
									rowIndexExpression = flattenExpression(rowIndexExpression);
									
									//System.out.println("have an atom expression: "+atom+" with INT col:"+col+" and flattened row:"+rowIndexExpression);
									
									//System.out.println("2  Atom "+atom+" is gonna be flattened to a variuabe? "+atom.isGonnaBeFlattenedToVariable());
									
									// return element constraint
									if(atom.isGonnaBeFlattenedToVariable()) {
										this.constraintBuffer.add(new ElementConstraint(indexedArray,
											                           rowIndexExpression,
											                           auxVariable));
										return auxVariable;
										
									}
									else return new ElementConstraint(indexedArray,
											                           rowIndexExpression,
											                           auxVariable);
									
								}
								else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
										+"the domain of variable "+arrayVariable+" is not constant.");
							}
							
							
							
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
	 * Flatten an expression like M[x] to an element constraint element(M,x, ?) where 
	 * the result is not set yet.
	 * 
	 * ONLY CALL THIS METHOD IF:
	 * - dynamic array indexing is NOT supported by the target solver
	 * - the expression is an arithmetic or relational ATOM expression that is 
	 *   an array element indexed by something non-constant 
	 * 
	 * @param expression
	 * @return
	 * @throws TailorException
	 */
	private ElementConstraint flattenToPartwiseElementConstraint(Expression expression)
		throws TailorException,Exception {
		
		if(expression instanceof ArithmeticAtomExpression) {
			
			ArithmeticAtomExpression atom = (ArithmeticAtomExpression) expression;
			ArrayVariable arrayVariable = (ArrayVariable) atom.getVariable();
			
			
			Expression[] indices = arrayVariable.getExpressionIndices();
	
			//System.out.println("constant arrays: "+this.normalisedModel.constantArrays);
			//System.out.println("The array element '"+atom+"' is a constant array?? with name:"+arrayVariable.getArrayNameOnly());
			
			// ---------- if this is a constant variable --------------------------------------------------------
			if(this.normalisedModel.constantArrays.containsKey( arrayVariable.getArrayNameOnly() )) {
				throw new TailorException("Cannot tailor constant elements that are indexed by decision variables :"+atom);
				
			}
			
			// --------- if we have an array element indexed by something that is not a single integer -----------	
				// the solver does not support array indexing with something other than an integer
			else {
				//System.out.println("Flattening to partwise elem constraint: "+expression);
					if(indices.length == 1) {
						Expression index = indices[0];
						//System.out.println("Flattening index '"+index+"' in expression: "+expression);
						
						if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
							index.willBeFlattenedToVariable(true);
						index = flattenExpression(index);
						
						
						// OIOIOI
						//this.constraintBuffer.get(this.constraintBuffer.size()-1).willBeFlattenedToVariable(false);
						
						//System.out.println("Flattened index to '"+index+"' in expression: "+expression);
						
						Domain d = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
						//System.out.println("Domain of variable "+(arrayVariable).getArrayNameOnly()+" is :"+d.toString()+" with type:"
							//	+d.getType());
						
						if(!(d instanceof ArrayDomain) && !(d instanceof ConstantArrayDomain))
							throw new TailorException("Indexed variable '"+(arrayVariable).getArrayNameOnly()+"' is not an array:"+atom);
						
						Domain[] indexDs;
						
						if(d instanceof ArrayDomain) 
							indexDs = ((ArrayDomain) d).getIndexDomains();
						else // ConstantArrayDomain 							
							indexDs = ((ConstantArrayDomain) d).getIndexDomains();
							
							BasicDomain[] indexDomains = new BasicDomain[indexDs.length];
							for(int i=0; i<indexDs.length; i++) {
								if(!(indexDs[i] instanceof BasicDomain))
									throw new TailorException("");
								indexDomains[i] = (BasicDomain) indexDs[i];
							}
						
							SimpleArray array = new SimpleArray((arrayVariable).getArrayNameOnly(),
								                            indexDomains,
								                            (arrayVariable).getBaseDomain());
						
						
							return new ElementConstraint(array,
												     index);
						
					
					}
					else if(indices.length == 2) {
						Expression rowIndexExpression = indices[0].evaluate();
						Expression colIndexExpression = indices[1].evaluate();
						
						// ------------- 1.case:  matrix[row:int, colExpr:E] ---> element(m[row,..], colExpr, aux) -----
						if(rowIndexExpression.getType() == Expression.INT) {
							
							int row = ((ArithmeticAtomExpression) rowIndexExpression).getConstant();
							Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
							domain = domain.evaluate();
							
							if(domain instanceof ConstantArrayDomain) {
							    //	 creating m[row,..]   (indexedArray)
								BasicDomain[] arrayIndices = new BasicDomain[2];
								arrayIndices[0] = new SingleIntRange(row);
								arrayIndices[1] = ((ConstantArrayDomain) domain).getIndexDomains()[1]; 
								IndexedArray indexedArray = new IndexedArray(arrayVariable.getArrayNameOnly(),
										                                     arrayIndices,
										                                     ((ConstantArrayDomain) domain).getBaseDomain());
									
								// prepare  index expression
								if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
									colIndexExpression.willBeFlattenedToVariable(true);
								colIndexExpression = flattenExpression(colIndexExpression);
								
								// return element constraint
								return new ElementConstraint(indexedArray,
										                           colIndexExpression);
								
							}
							else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
									+"the domain of variable "+arrayVariable+" is not constant.");
						}
						
						
						// ------------2.case: matrix[rowExpr:E, col:int]   ---> element(m[..,col],  rowExpr, aux) ---------
						else if(colIndexExpression.getType() == Expression.INT) {
							int col = ((ArithmeticAtomExpression) colIndexExpression).getConstant();
							Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
							domain = domain.evaluate();
							//System.out.println("Domain "+domain+" is the domain of variable: "+arrayVariable.getArrayNameOnly()+
								//	" and it is of Type: "+domain.getType());
							
							if(domain instanceof ConstantArrayDomain) {
							    //	 creating m[row,..]   (indexedArray)
								BasicDomain[] arrayIndices = new BasicDomain[2];
								arrayIndices[0] = ((ConstantArrayDomain) domain).getIndexDomains()[0]; 
								arrayIndices[1] = new SingleIntRange(col);
								IndexedArray indexedArray = new IndexedArray(arrayVariable.getArrayNameOnly(),
										                                     arrayIndices,
										                                     ((ConstantArrayDomain) domain).getBaseDomain());
								
								
								// prepare  index expression
								if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT)) 
									rowIndexExpression.willBeFlattenedToVariable(true);
								
								//System.out.println("Row index "+rowIndexExpression+" will be flattened to variable? "+rowIndexExpression.isGonnaBeFlattenedToVariable());
								rowIndexExpression = flattenExpression(rowIndexExpression);
								//System.out.println("Flattened roIndex >"+rowIndexExpression);
								
								// return element constraint
								return new ElementConstraint(indexedArray,
										                           rowIndexExpression);
								
							}
							else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
									+"the domain of variable "+arrayVariable+" is not constant.");
						}
						
						// ----------- 3.case: matrix[rowExpr:E, colExpr:E]  ----> element(flatM, rowExpr*noRows + colExpr, aux)
						else {
							Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
							domain = domain.evaluate();
							
							if(domain instanceof ConstantArrayDomain) {
							    
								if(domain instanceof ConstantArrayDomain) {
									
									SimpleArray flatArray = new SimpleArray(arrayVariable.getArrayNameOnly(),
																			((ConstantArrayDomain) domain).getIndexDomains(),
																			((ConstantArrayDomain) domain).getBaseDomain());
									
								    int noRows = ((ConstantArrayDomain) domain).getIndexDomains()[0].getRange()[1] - 
								    	         ((ConstantArrayDomain) domain).getIndexDomains()[0].getRange()[0] + 1;
								    
								    // index = noRows*rowExpr + colExpr
								    Sum indexExpression = new Sum(new Expression[] { new Multiplication(
								    		                                             new Expression[] {new ArithmeticAtomExpression(noRows),
								    		                                            		           rowIndexExpression}
								    		                                             ),
								    		                                        colIndexExpression},
								    		                                        new Expression[0]);
									// prepare  index expression
									if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
										indexExpression.willBeFlattenedToVariable(true);
									Expression index = flattenExpression(indexExpression);
									
									flatArray.setWillBeFlattenedToVector(true);
									
									// return element constraint
									return new ElementConstraint(flatArray,
											                      index);
									
								}
								else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
										+"the domain of variable "+arrayVariable+" is not constant.");
								
							}
							else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
									+"the domain of variable "+arrayVariable+" is not constant.");
						}
						
						
						
					}
					else if(indices.length == 3)
						throw new TailorException("Sorry, cannot translate dynamic array indexing for 3-dimensional arrays yet:"+atom);
				}	
			
				// translate to an element constraint if the target solver supports it
				// find out if the element constraint has to be nested too!!
			}
		
		else if(expression instanceof RelationalAtomExpression) {
			
			RelationalAtomExpression atom = (RelationalAtomExpression) expression;
			ArrayVariable arrayVariable = (ArrayVariable) atom.getVariable();
			
			
			Expression[] indices = arrayVariable.getExpressionIndices();
	
			//System.out.println("constant arrays: "+this.normalisedModel.constantArrays);
			//System.out.println("The array element '"+atom+"' is a constant array?? with name:"+arrayVariable.getArrayNameOnly());
			
			// ---------- if this is a constant variable --------------------------------------------------------
			if(this.normalisedModel.constantArrays.containsKey( arrayVariable.getArrayNameOnly() )) {
				throw new TailorException("Cannot tailor constant elements that are indexed by decision variables :"+atom);
				
			}
			
			// --------- if we have an array element indexed by something that is not a single integer -----------	
				// the solver does not support array indexing with something other than an integer
			else {
					if(indices.length == 1) {
						Expression index = indices[0];
						if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
							index.willBeFlattenedToVariable(true);
						
						Domain d = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
						//System.out.println("Domain of variable "+(arrayVariable).getArrayNameOnly()+" is :"+d.toString());
						
						if(!(d instanceof ArrayDomain) && !(d instanceof ConstantArrayDomain))
							throw new TailorException("Indexed variable '"+(arrayVariable).getArrayNameOnly()+"' is not an array:"+atom);
						
						Domain[] indexDs = ((ArrayDomain) d).getIndexDomains();
						BasicDomain[] indexDomains = new BasicDomain[indexDs.length];
						for(int i=0; i<indexDs.length; i++) {
							if(!(indexDs[i] instanceof BasicDomain))
								throw new TailorException("");
							indexDomains[i] = (BasicDomain) indexDs[i];
						}
						
						SimpleArray array = new SimpleArray((arrayVariable).getArrayNameOnly(),
								                            indexDomains,
								                            (arrayVariable).getBaseDomain());
						
						
						return new ElementConstraint(array,
												     index);
					}
					else if(indices.length == 2) {
						Expression rowIndexExpression = indices[0].evaluate();
						Expression colIndexExpression = indices[1].evaluate();
						
						// ------------- 1.case:  matrix[row:int, colExpr:E] ---> element(m[row,..], colExpr, aux) -----
						if(rowIndexExpression.getType() == Expression.INT) {
							
							int row = ((ArithmeticAtomExpression) rowIndexExpression).getConstant();
							Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
							domain = domain.evaluate();
							
							if(domain instanceof ConstantArrayDomain) {
							    //	 creating m[row,..]   (indexedArray)
								BasicDomain[] arrayIndices = new BasicDomain[2];
								arrayIndices[0] = new SingleIntRange(row);
								arrayIndices[1] = ((ConstantArrayDomain) domain).getIndexDomains()[1]; 
								IndexedArray indexedArray = new IndexedArray(arrayVariable.getArrayNameOnly(),
										                                     arrayIndices,
										                                     ((ConstantArrayDomain) domain).getBaseDomain());
									
								// prepare  index expression
								if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
									colIndexExpression.willBeFlattenedToVariable(true);
								colIndexExpression = flattenExpression(colIndexExpression);
								
								// return element constraint
								return new ElementConstraint(indexedArray,
										                           colIndexExpression);
								
							}
							else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
									+"the domain of variable "+arrayVariable+" is not constant.");
						}
						
						
						// ------------2.case: matrix[rowExpr:E, col:int]   ---> element(m[..,col],  rowExpr, aux) ---------
						else if(colIndexExpression.getType() == Expression.INT) {
							int col = ((ArithmeticAtomExpression) colIndexExpression).getConstant();
							Domain domain = this.normalisedModel.getDomainOfVariable(arrayVariable.getArrayNameOnly());
							domain = domain.evaluate();
							//System.out.println("Domain "+domain+" is the domain of variable: "+arrayVariable.getArrayNameOnly()+
							//		" and it is of Type: "+domain.getType());
							
							if(domain instanceof ConstantArrayDomain) {
							    //	 creating m[row,..]   (indexedArray)
								BasicDomain[] arrayIndices = new BasicDomain[2];
								arrayIndices[0] = ((ConstantArrayDomain) domain).getIndexDomains()[0]; 
								arrayIndices[1] = new SingleIntRange(col);
								IndexedArray indexedArray = new IndexedArray(arrayVariable.getArrayNameOnly(),
										                                     arrayIndices,
										                                     ((ConstantArrayDomain) domain).getBaseDomain());
								
								
								// prepare  index expression
								if(!this.targetSolver.supportsConstraintsNestedAsArgumentOf(Expression.ELEMENT_CONSTRAINT))
									rowIndexExpression.willBeFlattenedToVariable(true);
								rowIndexExpression = flattenExpression(rowIndexExpression);
								
								// return element constraint
								return new ElementConstraint(indexedArray,
										                           rowIndexExpression);
								
							}
							else throw new TailorException("Cannot flatten expression '"+atom+"' to element constraint: "
									+"the domain of variable "+arrayVariable+" is not constant.");
						}
						
						
						
					}
				}
				// translate to an element constraint if the target solver supports it
				// find out if the element constraint has to be nested too!!
			}
		
	
			
		
		
		throw new TailorException("Interal error: cannot tailor expression '"+expression+
				"' to a partwise element-constraint.");
	}
	
	
	// ========================= GENERAL HELPER METHODS ===========================
	
	/**
	 * We can only insert certain constant values after unfolding a quantified expression.
	 * This is why this method has to be called only after unfolding a quantified expression
	 * otherwise it is useless and changes nothing that cann
	 * 
	 */
	private Expression insertConstantArraysInExpression(Expression expression) 
		throws TailorException,Exception {
		
		
		
		
		if(expression instanceof RelationalAtomExpression) 
			return expression;
		
		else if(expression instanceof UnaryRelationalExpression)  {
			Expression argument = insertConstantArraysInExpression( ((UnaryRelationalExpression) expression).getArgument());
			argument= argument.evaluate();
			
			if(expression instanceof AllDifferent)
				return new AllDifferent((Array) argument);
			
			else if(expression instanceof Negation)
				return new Negation(argument);
			
			else if(expression instanceof UnaryMinus)
				return new UnaryMinus(argument);
			
			else return expression;
		}
		
		else if(expression instanceof NonCommutativeRelationalBinaryExpression) {
			NonCommutativeRelationalBinaryExpression expr = ((NonCommutativeRelationalBinaryExpression) expression);
			Expression leftExpression = insertConstantArraysInExpression(expr.getLeftArgument());
			Expression rightExpression = insertConstantArraysInExpression(expr.getRightArgument());
			return new NonCommutativeRelationalBinaryExpression(leftExpression,
																											expr.getOperator(),
																											rightExpression   );
		}
		
		else if(expression instanceof NonCommutativeArithmeticBinaryExpression) {
			NonCommutativeArithmeticBinaryExpression expr = ((NonCommutativeArithmeticBinaryExpression) expression);
			Expression leftExpression = insertConstantArraysInExpression(expr.getLeftArgument());
			Expression rightExpression = insertConstantArraysInExpression(expr.getRightArgument());
			return new NonCommutativeArithmeticBinaryExpression(leftExpression,
																											expr.getOperator(),
																											rightExpression   );
		}
		
		else if(expression instanceof QuantifiedExpression)
			return expression;
		
		else if(expression instanceof Conjunction) {
			Conjunction expr = ((Conjunction) expression);
			ArrayList<Expression> arguments = expr.getArguments();
			for(int i=0; i<arguments.size(); i++) {
				arguments.add(i, this.insertConstantArraysInExpression(arguments.remove(i)));
			}
			return new Conjunction(arguments);
		}
		
		else if(expression instanceof Disjunction){
			Disjunction expr = ((Disjunction) expression);
			ArrayList<Expression> arguments = expr.getArguments();
			for(int i=0; i<arguments.size(); i++) {
				arguments.add(i, this.insertConstantArraysInExpression(arguments.remove(i)));
			}
			return new Disjunction(arguments);
		}
		
		else if(expression instanceof ElementConstraint) {
			ElementConstraint expr = (ElementConstraint) expression;
			Expression[] arguments = expr.getArguments();
			for(int i=0; i<arguments.length; i++) {
				arguments[i] = this.insertConstantArraysInExpression(arguments[i]);
			}
			return new ElementConstraint(arguments[0],arguments[1], arguments[2]);
		}	
			
		else if(expression instanceof QuantifiedSum)
			return expression;
		
		else if(expression instanceof CommutativeBinaryRelationalExpression) {
			CommutativeBinaryRelationalExpression expr = ((CommutativeBinaryRelationalExpression) expression);
			Expression leftExpression = insertConstantArraysInExpression(expr.getLeftArgument());
			Expression rightExpression = insertConstantArraysInExpression(expr.getRightArgument());
			return new CommutativeBinaryRelationalExpression(leftExpression,
																										expr.getOperator(),
																										rightExpression   );
		}
		
		else if(expression instanceof QuantifiedSum)
			return  expression;
			
		else if(expression instanceof UnaryArithmeticExpression) {
			if(expression instanceof UnaryMinus)
				return new UnaryMinus(insertConstantArraysInExpression( ((UnaryArithmeticExpression) expression).getArgument()));
			
			if(expression instanceof AbsoluteValue)
				return new AbsoluteValue(insertConstantArraysInExpression( ((UnaryArithmeticExpression) expression).getArgument()));
			
			return expression;
		}
		
		else if(expression instanceof Sum){
			Sum expr = ((Sum) expression);
			ArrayList<Expression> posArguments = expr.getPositiveArguments();
			for(int i=0; i<posArguments.size(); i++) {
				posArguments.add(i, this.insertConstantArraysInExpression(posArguments.remove(i)));
			}
			ArrayList<Expression> negArguments = expr.getNegativeArguments();
			for(int i=0; i<negArguments.size(); i++) {
				negArguments.add(i, this.insertConstantArraysInExpression(negArguments.remove(i)));
			}
			return new Sum(posArguments,negArguments);
		}
		
		else if(expression instanceof Multiplication){
			Multiplication expr = ((Multiplication) expression);
			ArrayList<Expression> arguments = expr.getArguments();
			for(int i=0; i<arguments.size(); i++) {
				arguments.add(i, this.insertConstantArraysInExpression(arguments.remove(i)));
			}
			return new Multiplication(arguments);
		}
		
		
		// HERE IS THE STUFF THAT IS ACTUALLY HAPPENING: insert constant array value
		else if(expression instanceof ArithmeticAtomExpression) {
			
			ArithmeticAtomExpression  atom = (ArithmeticAtomExpression) expression;
		
			if(atom.getType() == Expression.INT_ARRAY_VAR) {
				Variable arrayVariable = atom.getVariable();
				
				//System.out.println("Trying to find constant array variable match for int array var:"+arrayVariable);
				
				if(arrayVariable.getType() == Expression.ARRAY_VARIABLE) {
					
					ArrayVariable aVar = (ArrayVariable) arrayVariable;
					Expression[] indices = aVar.getExpressionIndices();
					
					// we still have expression indices
					if(indices!=null) {
						boolean someThingChanged = false;
						
						for(int j=0; j<indices.length; j++) {
							Expression e = this.insertConstantArraysInExpression(indices[j]);
							if(!e.toString().equals(indices[j].toString())) {
								someThingChanged = true;
								indices[j] = e;
							}
						}
						if(!someThingChanged)
							return expression;
					
						Expression e = new ArrayVariable(aVar.getArrayNameOnly(), 
									indices,
										aVar.getBaseDomain());
						//System.out.println("After inserting expressions into atom:"+atom+" we get: "+e+
						//		" in which we will insert constant arrays again...");
						return this.insertConstantArraysInExpression(new ArithmeticAtomExpression(new ArrayVariable(aVar.getArrayNameOnly(), 
												 										indices,
												 										aVar.getBaseDomain())));
						//return expression;
					}
						
					//System.out.println("constant arrays: "+this.normalisedModel.constantArrays);
					//System.out.println("The array element '"+atom+"' is a constant array?? with nameL:"+((ArrayVariable) arrayVariable).getArrayNameOnly());
					
					// ---------- if this is a constant variable --------------------------------------------------------
					if(this.normalisedModel.constantArrays.containsKey( ((ArrayVariable) arrayVariable).getArrayNameOnly() )) {
					
						
						ConstantArray constArray = this.normalisedModel.constantArrays.get(((ArrayVariable) arrayVariable).getArrayNameOnly());
					
						if(indices != null) 
							throw new TailorException("Sorry, cannot tailor constant arrays that are indexed by decision variables yet.");
						
						int[] intIndices = ((ArrayVariable) arrayVariable).getIntegerIndices();
						
						// we have a vector
						if(intIndices.length == 1) {
							if(constArray instanceof ConstantVector) {
								ConstantVector vector = (ConstantVector) constArray;
								int rowOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(vector.getArrayName(), 0);
								
								return new ArithmeticAtomExpression(vector.getElementAt(intIndices[0]-rowOffsetFromZero));
							}
							else throw new TailorException("Illegal index dimensions of constant array element '"+atom+
									"' that dereferences the array '"+constArray+
									"'.\nPlease make sure you have an index for every dimension of the array.");
						}
						// matrix
						else if(intIndices.length == 2) {
							if(constArray instanceof ConstantMatrix) {
								ConstantMatrix matrix = (ConstantMatrix) constArray;
								
								int rowOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(matrix.getArrayName(), 0);
								int colOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(matrix.getArrayName(), 1);
								
								//System.out.println("Matrix rowOffset:"+rowOffsetFromZero+", colOffset:"+colOffsetFromZero);
								
								return new ArithmeticAtomExpression(matrix.getElementAt(intIndices[0]-rowOffsetFromZero, 
										                                                intIndices[1]-colOffsetFromZero));
								
							}
							else throw new TailorException("Illegal index dimensions of constant array element '"+atom+
									"' that dereferences the array '"+constArray+
									"'.\nPlease make sure you have an index for every dimension of the array.");
							
						}
						// cube
						//else if(intIndices.length == 3) 
						else throw new TailorException("Cannot tailor constant elements with more than 2 dimensions yet, sorry:"+atom);
						
					}
				}
				else if(arrayVariable.getType() == Expression.SIMPLE_ARRAY_VARIABLE) {
					
					//System.out.println("Inserting the constant shit in simple array var:"+expression);
					SimpleArrayVariable simpleVar = (SimpleArrayVariable) expression;
					
					if(this.normalisedModel.constantArrays.containsKey(simpleVar.getVariableName())) {
						ConstantArray constArray = this.normalisedModel.constantArrays.get(simpleVar.getVariableName());
						
						//System.out.println("1 inserting constArray "+simpleVar.getVariableName()+" in "+expression);
						Expression e = simpleVar.replaceVariableWithExpression(simpleVar.getVariableName(), constArray);
						//System.out.println("2 inserted constArray "+simpleVar.getVariableName()+" in "+expression+": "+e);
						return e;
					}
					
				}
			}
		}
		
		
		
		
		// HERE IS THE STUFF THAT IS ACTUALLY HAPPENING: insert constant array value
		else if(expression instanceof RelationalAtomExpression) {
			
			RelationalAtomExpression  atom = (RelationalAtomExpression) expression;
		
			if(atom.getType() == Expression.BOOL_ARRAY_VAR) {
				Variable arrayVariable = atom.getVariable();
				
				//System.out.println("Trying to find constant array variable match for :"+arrayVariable);
				
				if(arrayVariable.getType() == Expression.ARRAY_VARIABLE) {
					
					ArrayVariable aVar = (ArrayVariable) arrayVariable;
					Expression[] indices = aVar.getExpressionIndices();
					
					// we still have expression indices
					if(indices!=null) {
						
						for(int j=0; j<indices.length; j++) {
							indices[j] = this.insertConstantArraysInExpression(indices[j]);
						}
						Expression e = new ArrayVariable(aVar.getArrayNameOnly(), 
									indices,
										aVar.getBaseDomain());
						//System.out.println("After inserting expressions into atom:"+atom+" we get: "+e+
						//		" in which we will insert constant arrays again...");
						return this.insertConstantArraysInExpression(new ArrayVariable(aVar.getArrayNameOnly(), 
												 										indices,
												 										aVar.getBaseDomain()));
						//return expression;
					}
						
					//System.out.println("constant arrays: "+this.normalisedModel.constantArrays);
					//System.out.println("The array element '"+atom+"' is a constant array?? with nameL:"+((ArrayVariable) arrayVariable).getArrayNameOnly());
					
					// ---------- if this is a constant variable --------------------------------------------------------
					if(this.normalisedModel.constantArrays.containsKey( ((ArrayVariable) arrayVariable).getArrayNameOnly() )) {
					
						
						ConstantArray constArray = this.normalisedModel.constantArrays.get(((ArrayVariable) arrayVariable).getArrayNameOnly());
					
						if(indices != null) 
							throw new TailorException("Sorry, cannot tailor constant arrays that are indexed by decision variables yet.");
						
						int[] intIndices = ((ArrayVariable) arrayVariable).getIntegerIndices();
						
						// we have a vector
						if(intIndices.length == 1) {
							if(constArray instanceof ConstantVector) {
								ConstantVector vector = (ConstantVector) constArray;
								int rowOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(vector.getArrayName(), 0);
								
								return new RelationalAtomExpression(vector.getElementAt(intIndices[0]-rowOffsetFromZero) > 0);
							}
							else throw new TailorException("Illegal index dimensions of constant array element '"+atom+
									"' that dereferences the array '"+constArray+
									"'.\nPlease make sure you have an index for every dimension of the array.");
						}
						// matrix
						else if(intIndices.length == 2) {
							if(constArray instanceof ConstantMatrix) {
								ConstantMatrix matrix = (ConstantMatrix) constArray;
								
								int rowOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(matrix.getArrayName(), 0);
								int colOffsetFromZero = this.normalisedModel.getOffsetFromZeroAt(matrix.getArrayName(), 1);
								
								//System.out.println("Matrix rowOffset:"+rowOffsetFromZero+", colOffset:"+colOffsetFromZero);
								
								return new RelationalAtomExpression(matrix.getElementAt(intIndices[0]-rowOffsetFromZero, 
										                                                intIndices[1]-colOffsetFromZero) > 0);
								
							}
							else throw new TailorException("Illegal index dimensions of constant array element '"+atom+
									"' that dereferences the array '"+constArray+
									"'.\nPlease make sure you have an index for every dimension of the array.");
							
						}
						// cube
						//else if(intIndices.length == 3) 
						else throw new TailorException("Cannot tailor constant elements with more than 2 dimensions yet, sorry:"+atom);
						
					}
				}
				else if(arrayVariable.getType() == Expression.SIMPLE_ARRAY_VARIABLE) {
					
					//System.out.println("Inserting the constant shit in simple array var:"+expression);
					SimpleArrayVariable simpleVar = (SimpleArrayVariable) expression;
					
					if(this.normalisedModel.constantArrays.containsKey(simpleVar.getVariableName())) {
						ConstantArray constArray = this.normalisedModel.constantArrays.get(simpleVar.getVariableName());
						
						//System.out.println("1 inserting constArray "+simpleVar.getVariableName()+" in "+expression);
						Expression e = simpleVar.replaceVariableWithExpression(simpleVar.getVariableName(), constArray);
						//System.out.println("2 inserted constArray "+simpleVar.getVariableName()+" in "+expression+": "+e);
						return e;
					}
					
				}
			}
		}
		
		
		
		
		
		
		
		else if(expression instanceof SimpleArrayVariable) {
			
				//System.out.println("Inserting the constant shit in simple array var:"+expression);
				SimpleArrayVariable simpleVar = (SimpleArrayVariable) expression;
				
				if(this.normalisedModel.constantArrays.containsKey(simpleVar.getVariableName())) {
					ConstantArray constArray = this.normalisedModel.constantArrays.get(simpleVar.getVariableName());
					
					//System.out.println("1 inserting constArray "+simpleVar.getVariableName()+" in "+expression);
					Expression e = simpleVar.replaceVariableWithExpression(simpleVar.getVariableName(), constArray);
					//System.out.println("2 inserted constArray "+simpleVar.getVariableName()+" in "+expression+": "+e);
					return e;
				}
			
		}
		
		return expression;
	}
	
	
	
	
	private void addToDirectlyEqualAtoms(Expression leftExpression, Expression rightExpression) 
		throws TailorException {
		
		if(!this.settings.applyDirectVariableReusage())
			return;
		
		//System.out.println("Adding to directly equal atoms: replacing "+leftExpression+" with "+rightExpression);
		
		if(rightExpression instanceof ArithmeticAtomExpression)
			this.equalAtomsMap.put(leftExpression.toString(), (ArithmeticAtomExpression) rightExpression);
		
		else if(rightExpression instanceof RelationalAtomExpression) {
			this.equalAtomsMap.put(leftExpression.toString(), ((RelationalAtomExpression) rightExpression).toArithmeticExpression());
		}
		else throw new TailorException("Internal error: cannot add expression that is not at atom to equal atom hashmap:"+rightExpression);
		
		
		if(leftExpression instanceof ArithmeticAtomExpression)
			this.replaceableVariables.add((ArithmeticAtomExpression) leftExpression);
		
		else if(leftExpression instanceof RelationalAtomExpression)
			this.replaceableVariables.add( ((RelationalAtomExpression) leftExpression).toArithmeticExpression());
		
		else throw new TailorException("Internal error: cannot add expression that is not at atom to replaceable variables list:"+leftExpression);
		
	}
	
	private void addToSubExpressions(Expression subExpression, Expression representative) {
		
		if(!this.useCommonSubExpressions)
			return;
		
		//System.out.println("Add subexpression "+subExpression+" of type "+subExpression.getType()+" represented by "+representative);
		
		if(representative instanceof ArithmeticAtomExpression)
			this.subExpressions.put(subExpression.toString(), (ArithmeticAtomExpression) representative);
		else if(representative instanceof RelationalAtomExpression) 
			this.subExpressions.put(subExpression.toString(), ((RelationalAtomExpression)representative).toArithmeticExpression());
		
	}
	
	private void addToEqualExpressions(Expression subExpression, Expression representative) {
		
		if(!this.useEqualSubExpressions)
			return;
		
		//System.out.println("Add subexpression "+subExpression+" of type "+subExpression.getType()+" represented by "+representative);
		
		if(representative instanceof ArithmeticAtomExpression)
			this.equalSubExpressions.put(subExpression.toString(), (ArithmeticAtomExpression) representative);
		
		else if(representative instanceof RelationalAtomExpression) 
			this.equalSubExpressions.put(subExpression.toString(), ((RelationalAtomExpression)representative).toArithmeticExpression());
		
	}
	
	
	
	private ArithmeticAtomExpression getCommonSubExpression(Expression expression) {
		this.usedCommonSubExpressions++;
		//System.out.println("Using common subexpression: "+expression);
		return this.subExpressions.get(expression.toString());
	}
	
	private ArithmeticAtomExpression getEqualSubExpression(Expression expression) {
		this.usedEqualSubExpressions++;
		return this.equalSubExpressions.get(expression.toString());
	}
	
	
	private boolean hasCommonSubExpression(Expression expression) {
		//System.out.println("Checking if '"+expression+"' has a common subexpression in:"+this.subExpressions);
		
		if(!this.useCommonSubExpressions)
			return false;
		
		return this.subExpressions.containsKey(expression.toString());
	}
	
	private boolean hasEqualSubExpression(Expression expression) {
		//System.out.println("Checking if '"+expression+"' has a common subexpression in:"+this.subExpressions);
		
		if(!this.useEqualSubExpressions)
			return false;
		
		return this.equalSubExpressions.containsKey(expression.toString());
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
		
		//reify(FALSE = atom) --> atom != aux, return aux     reify(TRUE = atom) --> return atom
		/*
		if(constraint instanceof CommutativeBinaryRelationalExpression) {
			CommutativeBinaryRelationalExpression binExp = (CommutativeBinaryRelationalExpression) constraint;
			if((binExp.getOperator() == Expression.EQ || binExp.getOperator() == Expression.IFF) 
					&& (binExp.getLeftArgument().getType() == Expression.BOOL) 
					&& (binExp.getRightArgument() instanceof RelationalAtomExpression)) {
				
				if(((RelationalAtomExpression) binExp.getLeftArgument()).getBool()) {
					return (RelationalAtomExpression) binExp.getRightArgument();
				}
				else {
					ArithmeticAtomExpression auxVariable = null;
					if(hasCommonSubExpression(constraint)) {
						auxVariable = getCommonSubExpression(constraint);
						return auxVariable.toRelationalAtomExpression();
					}
					auxVariable = new ArithmeticAtomExpression(createAuxVariable(0, 1));
					addToSubExpressions(constraint, auxVariable);
					this.constraintBuffer.add(new CommutativeBinaryRelationalExpression((RelationalAtomExpression) binExp.getRightArgument(),
							                                                            Expression.NEQ,
							                                                            auxVariable.toRelationalAtomExpression()));
					return auxVariable.toRelationalAtomExpression();
				}
			}
					
		} */
		
		
		if(!this.targetSolver.supportsReificationOf(constraint.getType()))
			throw new TailorException
			("Cannot reify constraint because its reification is not supported by the target solver:"+constraint);
		
		ArithmeticAtomExpression auxVariable = null;
		
		//System.out.println("Reifying constraint "+constraint+" and flattening a negation? "+this.flatteningNegatedArgument);
		//System.out.println("Reifying constraint "+constraint+" that has a CSE? "+hasCommonSubExpression(constraint));
		
		// the constraint is false
		if(this.flatteningNegatedArgument) {
			auxVariable = new ArithmeticAtomExpression(0);
			addToSubExpressions(constraint, auxVariable);
			
			Reification reification = new Reification(constraint,
                    auxVariable.toRelationalAtomExpression());
			
			Expression r = reification.evaluate();
			
			System.out.println("Reified constraint to: "+r);
			
			this.constraintBuffer.add(r);
		}
			
		// if we have a common subexpression, use the corresponding variable
		else if(hasCommonSubExpression(constraint)) 
			auxVariable = getCommonSubExpression(constraint);
			
		else if(hasEqualSubExpression(constraint)) {
			auxVariable = getEqualSubExpression(constraint);
			addToSubExpressions(constraint, auxVariable);
			
			
			
			// add the flattened constraint to the constraint buffer
			this.constraintBuffer.add(new Reification(constraint,
					                                  auxVariable.toRelationalAtomExpression()));
		}
			
		// if we have no common subexpression, create a new auxiliary variable
		// and add the constraint to the list of subexpressions
		else  {
			auxVariable = new ArithmeticAtomExpression(createAuxVariable(0, 1));
			addToSubExpressions(constraint, auxVariable);
		
		
		
		// add the flattened constraint to the constraint buffer
		    this.constraintBuffer.add(new Reification(constraint,
				                                  auxVariable.toRelationalAtomExpression()));
		}
		
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
