package translator.gui;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import translator.solver.TargetSolver;

public class VariablePanel extends JPanel implements SelectionPanel {

	static final long serialVersionUID = 5;
	
	
	TargetSolver solver;
	
	
	/** available options for the target solver 
	 * These have to be updated according to the 
	 * target solver interface */
	JCheckBox sparseVariables;
	JCheckBox discreteBoundVariables;
	JCheckBox variableArrayIndices;

	JPanel checkPanel;
	
	public VariablePanel(TargetSolver solver) {
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
		updateCheckBoxes();
		//createCheckBoxPanel();
		//this.add(checkPanel);
		
	}
	
	
	/**
	 * Apply the selection to the solver instance in the Constraint Panel
	 * 
	 *
	 */
	public void applyChoiceToSolver() {
		boolean selectedChoice = this.sparseVariables.isSelected();
		this.solver.setFeature(TargetSolver.SPARSE_VARIABLES, selectedChoice);
		
		selectedChoice = this.discreteBoundVariables.isSelected();
		this.solver.setFeature(TargetSolver.DISCRETE_BOUNDS_VARIABLES, selectedChoice);
		
		selectedChoice = this.variableArrayIndices.isSelected();
		this.solver.setFeature(TargetSolver.VARIABLE_ARRAY_INDEXING, selectedChoice);
	}
	
	public void updateCheckBoxes() {
		
		boolean isSupported = this.solver.supportsSparseVariables();
		sparseVariables.setSelected(isSupported);
		
		isSupported = this.solver.supportsNaryConjunction();
		discreteBoundVariables.setEnabled(isSupported);
		discreteBoundVariables.setSelected(isSupported);
		
		isSupported = this.solver.supportsVariableArrayIndexing();
		variableArrayIndices.setEnabled(isSupported);
		variableArrayIndices.setSelected(isSupported);
		
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
	
		sparseVariables = new JCheckBox("Use sparse variables");
		boolean isSupported = this.solver.supportsSparseVariables();
		sparseVariables.setSelected(isSupported);
		
		discreteBoundVariables = new JCheckBox("Use discrete bounds variables");
		isSupported = this.solver.supportsNaryConjunction();
		discreteBoundVariables.setEnabled(isSupported);
		discreteBoundVariables.setSelected(isSupported);
		
		variableArrayIndices = new JCheckBox("Use variable array indices");
		isSupported = this.solver.supportsVariableArrayIndexing();
		variableArrayIndices.setEnabled(isSupported);
		variableArrayIndices.setSelected(isSupported);
		
		
	}
	
	
	/**
	 * Create the panel containing all the check boxes.
	 *
	 */
	
	protected void createCheckBoxPanel() {
		
		this.checkPanel = new JPanel(new GridLayout(0, 1));
		checkPanel.add(sparseVariables);
		checkPanel.add(discreteBoundVariables);
		checkPanel.add(variableArrayIndices);
		
	}
}
