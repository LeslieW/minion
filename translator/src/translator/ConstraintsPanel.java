package translator;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import translator.solver.*;


public class ConstraintsPanel extends JPanel {

	static final long serialVersionUID = 2;
	
	TargetSolver solver;
	
	
	/** available options for the target solver 
	 * These have to be updated according to the 
	 * target solver interface */
	JCheckBox naryDisjunction;
	JCheckBox naryConjunction;
	JCheckBox narySum;
	JCheckBox naryMultiplication;
	JCheckBox naryWeightedSum;
	
	JPanel checkPanel;
	
	public ConstraintsPanel(TargetSolver solver) {
		this.solver = solver;
		
		createCheckBoxes();
		createCheckBoxPanel();
		
		this.add(checkPanel);
	}
	
	
	/**
	 * Change the checkboxes according to the new solver,
	 * given by the parameter.
	 * 
	 * @param otherSolver
	 */
	public void changeSolver(TargetSolver otherSolver) {
		
		this.solver = otherSolver;
		createCheckBoxes();
		createCheckBoxPanel();
		this.add(checkPanel);
		
	}
	
	
	/**
	 * Apply the selection to the solver instance in the Constraint Panel
	 * 
	 *
	 */
	public void applyChoiceToSolver() {
		// TODO!
		
	}
	
	public TargetSolver getSolver() {
		return this.solver;
	}
	/**
	 * Create the check boxes for the solver profile selection.
	 * Only enable a checkbox to be changed if the features 
	 * is actually supported.
	 */
	protected void createCheckBoxes() {
	
		naryDisjunction = new JCheckBox("Use n-ary disjunction");
		boolean isSupported = this.solver.supportsNaryDisjunction();
		naryDisjunction.setEnabled(isSupported);
		naryDisjunction.setSelected(isSupported);
		
		naryConjunction = new JCheckBox("Use n-ary conjunction");
		isSupported = this.solver.supportsNaryConjunction();
		naryConjunction.setEnabled(isSupported);
		naryConjunction.setSelected(isSupported);
		
		narySum = new JCheckBox("Use n-ary sum");
		isSupported = this.solver.supportsNarySum();
		narySum.setEnabled(isSupported);
		narySum.setSelected(isSupported);
		
		naryWeightedSum = new JCheckBox("Use n-ary weighted sum");
		isSupported = this.solver.supportsWeightedNarySum();
		naryWeightedSum.setEnabled(isSupported);
		naryWeightedSum.setSelected(isSupported);
		
		naryMultiplication = new JCheckBox("Use n-ary multiplication");
		isSupported = this.solver.supportsNaryMultiplication();
		naryMultiplication.setEnabled(isSupported);
		naryMultiplication.setSelected(isSupported);
		
	}
	
	
	/**
	 * Create the panel containing all the check boxes.
	 *
	 */
	
	protected void createCheckBoxPanel() {
		
		this.checkPanel = new JPanel(new GridLayout(0, 1));
		checkPanel.add(naryDisjunction);
		checkPanel.add(naryConjunction);
		checkPanel.add(narySum);
		checkPanel.add(naryWeightedSum);
		checkPanel.add(naryMultiplication);
		
	}
	
}
