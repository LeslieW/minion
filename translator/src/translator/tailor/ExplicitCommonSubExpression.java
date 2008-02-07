package translator.tailor;

import java.sql.Timestamp;
import java.util.ArrayList;

import translator.normaliser.NormalisedModel;
import translator.tailor.Reformulation;
import translator.tailor.TailorException;
import translator.expression.*;

public class ExplicitCommonSubExpression implements Reformulation {

	private long time = 0;
	private NormalisedModel model;
	
	//=========== CONSTRUCTOR ==================
	public ExplicitCommonSubExpression() {
		
	}
	
	//=========== INHERITED METHODS ===================
	
	public NormalisedModel enhance(NormalisedModel model)
			throws TailorException {
		Timestamp counter = new Timestamp(0);
		this.model = model;
		
		// check constraints for explicit equivalence of 2 variables
		ArrayList<Expression> constraints = this.model.getConstraints();
		
		for(int i=0; i<constraints.size(); i++) {
			
			Expression constraint = constraints.get(i);
			
			if(constraint.getType() == Expression.EQ) {
				CommutativeBinaryRelationalExpression equality = (CommutativeBinaryRelationalExpression) constraint;
				Expression leftArgument = equality.getLeftArgument();
				Expression rightArgument = equality.getRightArgument();
				
				// if case: ATOM EQ ATOM
				if((leftArgument instanceof RelationalAtomExpression || 
						leftArgument instanceof ArithmeticAtomExpression) &&
						(rightArgument instanceof RelationalAtomExpression || 
								rightArgument instanceof ArithmeticAtomExpression)) {
					
					processExplicitEqualityOf(leftArgument, rightArgument);
				}
			}
		}
		
		this.time = counter.getTime();
		return this.model;
	}

	
	public long getReformulationTime() {
		return this.time;
	}

	
	public String printStatistics() {
		return "";
	}

	
	// ============= ADDITIONAL METHODS ==================
	
	private void processExplicitEqualityOf(Expression leftAtom, Expression rightAtom) {
		
		// do something!
		
	}
	
}
