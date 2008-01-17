package translator.gui;

import translator.solver.TargetSolver;

/**
 * This interface specifies any selection item in the graphical user interface
 * that allows to select features of the target solver.
 * Every component that enables selection to customise the settings for 
 * solver-dependent translation has to implement this interface.
 * 
 * @author andrea
 *
 */

public interface SelectionPanel {

	public TargetSolver getSolver();
	public void changeSolver(TargetSolver otherSolver);
	public void applyChoiceToSolver();
	public void updateCheckBoxes();
	
}
