package translator.gui;

import javax.swing.*;
import java.awt.*;
import translator.solver.*;
import translator.expression.Expression;

public class ConstraintsPanel extends JPanel implements SelectionPanel {

	static final long serialVersionUID = 2;
	
	TargetSolver solver;
	
	/** available options for the target solver 
	 * These have to be updated according to the 
	 * target solver interface */
	JCheckBox nestedExpressions;
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
		updateCheckBoxes();
		//this.add(checkPanel);
		
	}
	
	public void updateCheckBoxes() {
		
		boolean isSupported = this.solver.supportsNestedExpressions();
		nestedExpressions.setEnabled(isSupported);
		nestedExpressions.setSelected(isSupported);
		
		isSupported = this.solver.supportsConstraint(Expression.NARY_DISJUNCTION);
		naryDisjunction.setEnabled(isSupported);
		naryDisjunction.setSelected(isSupported);
		
		isSupported = this.solver.supportsConstraint(Expression.NARY_CONJUNCTION);
		naryConjunction.setEnabled(isSupported);
		naryConjunction.setSelected(isSupported);
		
		isSupported = this.solver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT);
		narySum.setEnabled(isSupported);
		narySum.setSelected(isSupported);
		
		//isSupported = this.solver.supportsWeightedNarySum();
		//naryWeightedSum.setEnabled(isSupported);
		//naryWeightedSum.setSelected(isSupported);
		
		isSupported = this.solver.supportsConstraint(Expression.NARY_PRODUCT_CONSTRAINT);
		naryMultiplication.setEnabled(isSupported);
		naryMultiplication.setSelected(isSupported);
	}
	
	/**
	 * Apply the selection to the solver instance in the Constraint Panel
	 * 
	 *
	 */
	public void applyChoiceToSolver() {
		
		boolean selectedChoice = this.nestedExpressions.isSelected();
		this.solver.setFeature(TargetSolver.NESTED_EXPRESSIONS, selectedChoice);
		
		selectedChoice = this.naryConjunction.isSelected();
		this.solver.setFeature(TargetSolver.NARY_CONJUNCTION, selectedChoice);
		
		selectedChoice = this.naryDisjunction.isSelected();
		this.solver.setFeature(TargetSolver.NARY_DISJUNCTION, selectedChoice);
		
		selectedChoice = this.naryMultiplication.isSelected();
		this.solver.setFeature(TargetSolver.NARY_MULTIPLICATION, selectedChoice);
		
		selectedChoice = this.narySum.isSelected();
		this.solver.setFeature(TargetSolver.NARY_SUM, selectedChoice);
		
		//selectedChoice = this.naryWeightedSum.isSelected();
		//this.solver.setFeature(TargetSolver.NARY_WEIGHTED_SUM, selectedChoice);
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
	
		this.nestedExpressions = new JCheckBox("Allow nested constraints");
		boolean isSupported = this.solver.supportsNestedExpressions();
		nestedExpressions.setEnabled(isSupported);
		nestedExpressions.setSelected(isSupported);
		
		naryDisjunction = new JCheckBox("Use n-ary disjunction");
		isSupported = this.solver.supportsConstraint(Expression.NARY_DISJUNCTION);
		naryDisjunction.setEnabled(isSupported);
		naryDisjunction.setSelected(isSupported);
		
		naryConjunction = new JCheckBox("Use n-ary conjunction");
		isSupported = this.solver.supportsConstraint(Expression.NARY_CONJUNCTION);
		naryConjunction.setEnabled(isSupported);
		naryConjunction.setSelected(isSupported);
		
		narySum = new JCheckBox("Use n-ary sum");
		isSupported = this.solver.supportsConstraint(Expression.NARY_SUMEQ_CONSTRAINT);
		narySum.setEnabled(isSupported);
		narySum.setSelected(isSupported);
		
		//naryWeightedSum = new JCheckBox("Use n-ary weighted sum");
		//isSupported = this.solver.supportsWeightedNarySum();
		//naryWeightedSum.setEnabled(isSupported);
		//naryWeightedSum.setSelected(isSupported);
		
		naryMultiplication = new JCheckBox("Use n-ary multiplication");
		isSupported = this.solver.supportsConstraint(Expression.NARY_PRODUCT_CONSTRAINT);
		naryMultiplication.setEnabled(isSupported);
		naryMultiplication.setSelected(isSupported);
		
	}
	
	
	/**
	 * Create the panel containing all the check boxes.
	 *
	 */
	
	protected void createCheckBoxPanel() {
		
		this.checkPanel = new JPanel(new GridLayout(0, 1));
		checkPanel.add(nestedExpressions);
		checkPanel.add(naryDisjunction);
		checkPanel.add(naryConjunction);
		checkPanel.add(narySum);
		//checkPanel.add(naryWeightedSum);
		checkPanel.add(naryMultiplication);
		
	}
	
	public void disableAllCheckboxes() {
		
		this.naryConjunction.setEnabled(false);
		this.naryDisjunction.setEnabled(false);
		this.naryMultiplication.setEnabled(false);
		this.narySum.setEnabled(false);
		//this.naryWeightedSum.setEnabled(false);
		this.nestedExpressions.setEnabled(false);
		
	}
	
}
