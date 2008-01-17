package translator.tailor;

//import translator.expression.Expression;
import translator.normaliser.NormalisedModel;
import translator.solver.TargetSolver;

public class Tailor implements TailorSpecification {

	NormalisedModel problemModel;
	TargetSolver targetSolver;
	
	
	// =================== CONSTRUCTOR ========================
	
	public Tailor(NormalisedModel model,
		          TargetSolver targetSolver) {
		
		this.problemModel = model;
		this.targetSolver= targetSolver;
	}
	 
	// ================== INHERITED METHODS ==================
	
	public String tailor(NormalisedModel normalisedModel)
			throws TailorException {
		// TODO Auto-generated method stub
		return null;
	}


}
