package translator.tailor;

//import translator.expression.Expression;
import translator.normaliser.NormalisedModel;
import translator.solver.TargetSolver;
import translator.TranslationSettings;
//import java.util.ArrayList;
import translator.solver.*;
import translator.tailor.minion.MinionTailor;
import translator.tailor.minion.MinionException;

public class Tailor implements TailorSpecification {

	NormalisedModel problemModel;
	TargetSolver targetSolver;
	TranslationSettings settings;
	
	MinionTailor minionTailor;
	
	// =================== CONSTRUCTOR ========================
	
	public Tailor(NormalisedModel model,
		          TranslationSettings settings) {
		
		this.problemModel = model;
		this.settings = settings;
		this.targetSolver= this.settings.getTargetSolver();
	}
	 
	// ================== INHERITED METHODS ==================
	
	
	public String tailor(NormalisedModel normalisedModel)
			throws TailorException, MinionException  {
		this.problemModel = normalisedModel;
		
		if(targetSolver instanceof Minion) {
			this.minionTailor = new MinionTailor(this.problemModel,
					                                   settings);
			return minionTailor.tailorToMinion().toString();
		}
		
		else throw new TailorException("Cannot tailor model to specified solver: '"+this.targetSolver.getSolverName()
				+"'. no tailor for solver available.");
	}

	
	
	
	public String tailor(NormalisedModel normalisedModel,
			             TargetSolver targetSolver)
	throws TailorException, MinionException  {
		this.problemModel = normalisedModel;
		this.targetSolver = targetSolver;
        
		
		if(this.targetSolver instanceof Minion) {
			this.minionTailor = new MinionTailor(problemModel,
					                                     this.settings);
			
			return minionTailor.tailorToMinion().toString();
			
		}
		
		else throw new TailorException("Cannot tailor model to specified solver: '"+this.targetSolver.getSolverName()
				+"'. no tailor for solver available.");
	}


	/**
	 * Flatten the problem model that was given in the constructor
	 * and return its String representation.
	 * @return the String representation of the flattened model
	 * 
	 * @throws TailorException
	 */
	public NormalisedModel flattenModel() throws TailorException,Exception {
		
		Flattener flattener = new Flattener(this.settings,
										    this.problemModel);
		
		this.problemModel =  flattener.flattenModel();
		return this.problemModel;
	}
	

	public String getEssenceSolution(String solverOutput) 
		throws TailorException {
		if(this.targetSolver instanceof Minion) {
			if(this.minionTailor != null) {
				return this.minionTailor.getEssenceSolution(solverOutput);
			}
			else throw new TailorException("Please tailor model before mapping the solver output.");
		}
		else throw new TailorException("Cannot return Essence' output for solver: '"+this.targetSolver.getSolverName()
				+"'. no tailor for solver available.");
	}
}
