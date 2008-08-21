package translator.tailor;

//import translator.expression.Expression;
import translator.normaliser.NormalisedModel;
import translator.solver.TargetSolver;
import translator.TranslationSettings;
//import java.util.ArrayList;
import translator.solver.*;
import translator.tailor.minion.MinionTailor;
import translator.tailor.minion.MinionException;
import translator.tailor.gecode.GecodeTailor;
import translator.tailor.gecode.GecodeException;

public class Tailor {

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
	
	/**
	 * Tailors the normalisedModel in Essence' to the specified targetsolver
	 * and returns the String that represents the target solver model.
	 * 
	 */
	public String tailor(NormalisedModel normalisedModel, TargetSolver solver)
			throws TailorException, MinionException, GecodeException  {
		this.problemModel = normalisedModel;
		this.targetSolver = solver;
		
		if(solver instanceof Minion) {
			this.minionTailor = new MinionTailor();
			translator.tailor.minion.MinionModel model = minionTailor.tailorToMinion(normalisedModel, settings);
			long startTime = System.currentTimeMillis();
			String minionInput = model.toString();
			if(this.settings.giveTranslationTimeInfo()) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Minion Model toString Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			return minionInput;
		}
		
		else if(solver instanceof Gecode) {
			//writeInfo("Flattened Essence' for solver Gecode:\n"+this.problemModel.toString());
			GecodeTailor gecodeTailor = new GecodeTailor();
			translator.tailor.gecode.GecodeModel model = gecodeTailor.tailorToGecode(normalisedModel, settings);
			
			long startTime = System.currentTimeMillis();
			String gecodeInput = model.toString();
			if(this.settings.giveTranslationTimeInfo()) {
				long stopTime = System.currentTimeMillis();
				writeTimeInfo("Gecode Model toString Time: "+(stopTime - startTime)/1000.0+"sec");
			}
			return gecodeInput;
			
			//throw new TailorException("Cannot tailor model to specified solver: '"+solver.getSolverName()
			//		+"'. The Gecode-Tailor is still under development.");
		}
		
		else throw new TailorException("Cannot tailor model to specified solver: '"+solver.getSolverName()
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
		
		this.targetSolver = settings.getTargetSolver();
		this.problemModel =  flattener.flattenModel();
		return this.problemModel;
	}
	

	/**
	 * Given the solverOutput, this method returns the solution 
	 * of the problem instance in Essence'
	 * 
	 * @param solverOutput
	 * @return
	 * @throws TailorException
	 */
	public String getEssenceSolution(String solverOutput) 
		throws TailorException {
		if(this.targetSolver instanceof Minion) {
			if(this.minionTailor != null) {
				return this.minionTailor.getEssenceSolution(solverOutput);
			}
			else throw new TailorException("Please tailor model before mapping the solver output.");
		}
		else throw new TailorException("Cannot return Essence' output for solver '"+this.targetSolver.getSolverName()
				+"'. no tailor for solver available.");
	}
	
	
	/*private void writeInfo(String info) {
		if(this.settings.giveTranslationInfo())
			System.out.println(info);
	}*/
	
	private void writeTimeInfo(String info) {
		if(this.settings.giveTranslationTimeInfo())
			System.out.println(info);
	}
}
