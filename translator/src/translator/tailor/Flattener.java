package translator.tailor;

import translator.solver.*;
import java.util.ArrayList;
import translator.expression.*;

public class Flattener {

	/** the target solver we want to tailor the model to */
	TargetSolver targetSolver;
	/** the list of all flattened constraints. constraints are added on the fly. */
	ArrayList<Expression> constraintList;
	
	// ========== CONSTRUCTOR ============================
	
	public Flattener(TargetSolver targetSolver) {
		this.targetSolver = targetSolver;	
		this.constraintList = new ArrayList<Expression>();
	}
	
	// ========== METHODS ================================
	
	/**
	 * Flatten the parameter constraint and return the corresponding constraint
	 * 
	 * @return the list of flattened constraints that represents the parameter constraint
	 */
	public ArrayList<Expression> flattenConstraint(Expression constraint) {
		
		this.constraintList.clear();
		
		if(this.targetSolver.supportsNestedExpressions()) {
			this.constraintList.add(constraint);
			return this.constraintList;
		}
		
		// else flatten the constraint
		// and return the constraint list
		
		return null;
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
	 * 
	 * @param expression
	 * @return
	 */
	private  Expression flattenRelationalExpression(RelationalExpression expression) {
		
		if(expression instanceof RelationalAtomExpression) 
			return null;
		
		return null;
	}
	
	/**
	 * 
	 * @param expression
	 * @return
	 */
	private Expression flattenArithmeticExpression(ArithmeticExpression expression) {
		
		return null;
	}
}
