package translator.tailor;

import translator.normaliser.NormalisedModel;
//import translator.expression.Expression;

/**
 * Gives a specification of tailors to solvers in general. Every tailor 
 * to new target solver has to implement the tailor
 * 
 * @author andrea
 *
 */

public interface TailorSpecification {

	
	
	/**
	 * Tailor the given normalised model into a String representation
	 * of the target solver input file. 
	 *
	 */
	public String tailor(NormalisedModel normalisedModel) throws TailorException;
	

	
	//public String tailorExpression(Expression constraint) throws TailorException;

	
}
