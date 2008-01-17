package translator.tailor;

import translator.solver.*;
import java.util.ArrayList;
import translator.expression.*;
import translator.normaliser.NormalisedModel;

public class Flattener {

	public final String AUXVARIABLE_NAME = "_aux";
	
	
	/** the target solver we want to tailor the model to */
	TargetSolver targetSolver;
	/** the list of all flattened constraints. constraints are added on the fly. */
	ArrayList<Expression> constraintList;
	
	/** the normalised model contains the list of expression variables etc */
	NormalisedModel normalisedModel;
	
	// ========== CONSTRUCTOR ============================
	
	public Flattener(TargetSolver targetSolver,
			         NormalisedModel normalisedModel) {
		this.targetSolver = targetSolver;	
		this.normalisedModel = normalisedModel;
		this.constraintList = new ArrayList<Expression>();
	}
	
	// ========== METHODS ================================
	
	/**
	 * Flattens the model according to the target solver that has been specified.
	 * 
	 */
	public NormalisedModel flattenModel() {
		
		return null;
	}
	
	
	/**
	 * Flatten the parameter constraint and return the corresponding constraint
	 * 
	 * @return the list of flattened constraints that represents the parameter constraint
	 */
	protected ArrayList<Expression> flattenConstraint(Expression constraint) 
		throws TailorException {
		
		this.constraintList.clear();
		
		if(this.targetSolver.supportsNestedExpressions()) {
			this.constraintList.add(constraint);
			return this.constraintList;
		}
		
		// else flatten the constraint
		Expression topExpression = flattenExpression(constraint);
		ArrayList<Expression> flattenedSubExpressions = this.constraintList;
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
		
		else throw new TailorException("Cannot tailor relational expression yet, or unknown expression:"+expression);
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
			if(this.targetSolver.supportsUnnestedNegation())
				return new Negation(argument);
			else {
				Variable auxVariable = createAuxVariable(0, 1);
				this.constraintList.add(new CommutativeBinaryRelationalExpression(new Negation(argument),
						                                                          Expression.EQ,
						                                                          auxVariable));
				return new RelationalAtomExpression(auxVariable);
			}
		}
		else if(expression.getType() == Expression.ALLDIFFERENT) {
			if(expression.isGonnaBeReified()) {
				if(this.targetSolver.supportsReifiedAllDifferent()) {
					Variable auxVariable = createAuxVariable(0, 1);
					this.constraintList.add(new CommutativeBinaryRelationalExpression(new AllDifferent(argument),
							                                                          Expression.EQ,
							                                                          auxVariable));
				}
				else throw new TailorException("Cannot tailor expression to solver because solver "+this.targetSolver.toString()+
						" does not support the reification of 'alldifferent'.");
			}
			return new AllDifferent(argument);
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
		
		// if the target solver allows stuff like m[x,y+1] where m,x,y are decision variables,
		// we need not have to flatten it
		if(this.targetSolver.supportsVariableArrayIndexing())
			return atom;
		
		if(atom.getType() == Expression.BOOL_VARIABLE_ARRAY_ELEM) {
			Variable arrayVariable = atom.getVariable();
			if(arrayVariable.getType() == Expression.ARRAY_VARIABLE) {
				Expression[] indices = ((ArrayVariable) arrayVariable).getExpressionIndices();
				if(indices != null) {
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
	 * @param expression
	 * @return
	 */
	private Expression flattenArithmeticExpression(ArithmeticExpression expression) {
		
		return null;
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
			auxVariable = new SingleVariable(AUXVARIABLE_NAME,
					                         new BoolDomain());
		else auxVariable = new SingleVariable(AUXVARIABLE_NAME, 
				                                  new BoundedIntRange(lb,ub));
		
		if(!this.targetSolver.willSearchOverAuxiliaryVariables())
			auxVariable.setToSearchVariable(false);
		
		this.normalisedModel.addAuxiliaryVariable(auxVariable);
		
		return auxVariable;
	}
}
