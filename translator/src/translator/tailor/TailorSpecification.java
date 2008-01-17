package translator.tailor;

import translator.normaliser.NormalisedModel;
//import translator.expression.Expression;
import translator.tailor.minion.MinionException;
import translator.solver.TargetSolver;
/**
 * Gives a specification of tailors to solvers in general. Every tailor 
 * to new target solver has to implement the tailor
 * 
 * @author andrea
 *
 */

public interface TailorSpecification {

	public final boolean USE_COMMON_SUBEXPRESSIONS = true;
	
	/**
	 * Tailor the given normalised model into a String representation
	 * of the target solver input file. 
	 *
	 */
	public String tailor(NormalisedModel normalisedModel) throws TailorException, MinionException;
	

	public String tailor(NormalisedModel normalisedModel, TargetSolver solver) throws TailorException, MinionException;
	//public String tailorExpression(Expression constraint) throws TailorException;

	
}
