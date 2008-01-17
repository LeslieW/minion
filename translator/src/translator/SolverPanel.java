package translator;

import javax.swing.*;

import translator.solver.*;
import java.awt.*;

/**
 * This panel represents the solver selection part of the graphical 
 * user interface. It allows to select one of the target solvers and
 * customize settings for the translation process.
 * 
 * The basic settings can be made for the constraints as targets,
 * variables and solving features, such as branching heuristics. 
 * There is also a solver specific selection of features.
 * 
 * @author andrea
 *
 */

public class SolverPanel extends JPanel {

	static final long serialVersionUID = 1;
	
	TargetSolver targetSolver;
	String[] availableSolvers;
	
	
	JPanel selectionPanel;
	JPanel leftPanel;
	
	ConstraintsPanel constraintsPanel;
	JPanel variablePanel;
	JPanel solvingPanel;
	JPanel solverSpecificPanel;
	
	JComboBox solverSelection;
	
	public SolverPanel(TargetSolver solver) {
		this.targetSolver = solver;
		this.availableSolvers = new String[] { "Minion", "DummySolver", "Hihi"};
		
		this.selectionPanel = new JPanel(new GridLayout(2,2));
		this.leftPanel = new JPanel(new FlowLayout());
		
		setLayout(new BorderLayout());
		
		customiseLeftPanel();
		customiseSelectionPanel();
		
		
		// add the panels to this panel (at least for now...)
		this.add(selectionPanel, BorderLayout.EAST);
		this.add(leftPanel, BorderLayout.WEST);
		
	}
	
	
	protected void customiseLeftPanel() {
		
		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Solver Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        comboBoxPanel.getBorder()));
		this.solverSelection = new JComboBox(availableSolvers);
		this.solverSelection.setSelectedIndex(0);
		this.solverSelection.setPreferredSize(new Dimension(150,30));
		comboBoxPanel.add(solverSelection);
		this.leftPanel.add(comboBoxPanel);
	}
	
	protected void customiseSelectionPanel() {
		
		// ========= start the panels ==========================================
		this.constraintsPanel = new ConstraintsPanel(this.targetSolver); 
		this.constraintsPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Constraint Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        constraintsPanel.getBorder()));
		//this.constraintsPanel.setVisible(true);
		
		
		
		
		// ============= add the panels to the selection panel ==================
		selectionPanel.add(constraintsPanel);
		
	}
}
