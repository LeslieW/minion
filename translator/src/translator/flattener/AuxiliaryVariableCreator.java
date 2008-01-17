package translator.flattener;

import java.util.ArrayList;
import translator.conjureEssenceSpecification.*;

public class AuxiliaryVariableCreator {

	private ArrayList<AtomicExpression> auxiliaryVariables; 
	private int noAuxVars;
	
	public AuxiliaryVariableCreator() {
		
		this.auxiliaryVariables = new ArrayList<AtomicExpression>();
		this.noAuxVars = 0;
	}
	
	
	/**
	 * Create a new auxiliary variable with the corresponding bounds. The 
	 * variables are all stored in a list.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 * @return
	 */
	public AtomicExpression createNewTemporaryVariable(int lowerBound, int upperBound) {
		
		AuxiliaryVariable auxVariable = new AuxiliaryVariable(FlatteningGlobals.AUX_VAR_NAME+this.noAuxVars++, 
				                                              lowerBound, 
				                                              upperBound);
		AtomicExpression expression = new AtomicExpression(auxVariable);
		this.auxiliaryVariables.add(expression);
		return expression;
	}
	
}
