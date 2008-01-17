package translator.tailor.minion;

import java.util.ArrayList;
import java.util.HashMap;
import translator.expression.*;
import translator.solver.Minion;

/**
 * Represents a Minion file in the MINION 3 format.
 * 
 * @author andrea
 *
 */

public class MinionModel {

	public final String MINION_HEADER = "MINION 3";
	
	ArrayList<MinionConstraint> constraintList;
	HashMap<String, ConstantDomain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	ArrayList<Variable> auxVariables;
	Minion solverSettings;
	
	// ========== CONSTRUCTOR =========================================
	public MinionModel(ArrayList<MinionConstraint> constraints,
			           HashMap<String, ConstantDomain> decisionVariables,
			           ArrayList<String> decisionVariablesNames,
			           ArrayList<Variable> auxVariables,
			           Minion solverFeatures) {
		
		this.constraintList = constraints;
		this.decisionVariables = decisionVariables;
		this.decisionVariablesNames = decisionVariablesNames;
		this.auxVariables = auxVariables;
		this.solverSettings = solverFeatures;
	}
	
	
	// =============== METHODS ========================================
	
	protected void addConstraint(MinionConstraint constraint) {
		this.constraintList.add(constraint);
	}
	
	protected String printModel() {
		
		String s = MINION_HEADER+"\n\n";
		
		return s;
	}
	
	// ========== STATISTICAL DETAILS =================================
	
	protected int getAmountOfAuxiliaryVariables() {
		return this.auxVariables.size();
	}
	
	protected int getAmountOfConstraints() {
		return this.constraintList.size();
	}
}
