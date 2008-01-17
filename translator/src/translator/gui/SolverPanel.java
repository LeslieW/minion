package translator.gui;

import javax.swing.*;

import translator.solver.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
	
	public final String SELECT_SOLVER = "select Solver";
	public final String CONFIRM_SOLVER = "confirm solver";
	
	/** The solvers that are available for selection */
	public final String MINION = "Minion";
	public final String DUMMY_SOLVER = "Dummy Solver";
	public final String ANGEES_SOLVER = "Angee's Solver";
	
	TargetSolver targetSolver;
	String[] availableSolvers;
	
	
	JPanel selectionPanel;
	JPanel leftPanel;
	
	// components of the selectionPanel
	ConstraintsPanel constraintsPanel;
	VariablePanel variablePanel;
	SolvingSelectionPanel solvingPanel;
	JPanel solverSpecificPanel;
	
	// components of the leftPanel
	JComboBox solverSelection;
	JButton selectSolverButton;
	JButton confirmSelectionButton;
	
	
	// ============= CONSTRUCTOR ====================================
	
	public SolverPanel(TargetSolver solver) {
		this.targetSolver = solver;
		this.availableSolvers = new String[] { MINION, DUMMY_SOLVER, ANGEES_SOLVER};
		
		this.selectionPanel = new JPanel(new GridLayout(2,2));
		this.leftPanel = new JPanel(new FlowLayout());
		
		setLayout(new BorderLayout());
		
		customiseLeftPanel();
		customiseSelectionPanel();

		
		// add the panels to this panel (at least for now...)
		this.add(selectionPanel, BorderLayout.EAST);
		this.add(leftPanel, BorderLayout.WEST);
		
	}
	
	// ============== METHODS ======================================
	
	protected void customiseLeftPanel() {
		
		// create Combo-Box with solvers to select
		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Solver Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        comboBoxPanel.getBorder()));
		this.solverSelection = new JComboBox(availableSolvers);
		this.solverSelection.setSelectedIndex(0);
		this.solverSelection.setPreferredSize(new Dimension(200,30));
		comboBoxPanel.add(solverSelection);
		
		
		// create Button to select solver
		this.selectSolverButton = new JButton("Select Solver");
		selectSolverButton.setPreferredSize(new Dimension(130,30));
		selectSolverButton.setActionCommand(SELECT_SOLVER);
		selectSolverButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           selectSolver(e.getActionCommand());   
			  }
			});
		
		this.confirmSelectionButton = new JButton("Confirm Selection");
		confirmSelectionButton.setPreferredSize(new Dimension(130,30));
		confirmSelectionButton.setActionCommand(CONFIRM_SOLVER);
		confirmSelectionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           confirmSolver(e.getActionCommand());   
			  }
			});
		
		this.leftPanel.add(comboBoxPanel);
		this.leftPanel.add(selectSolverButton);
		this.leftPanel.add(confirmSelectionButton);
	}
	
	/**
	 * Select another target solver with the selectSolverButton 
	 * @param command
	 */
	protected void selectSolver(String command) {
		String selectedSolver = (String) this.solverSelection.getSelectedItem();
		if(selectedSolver == MINION) {
			this.targetSolver = new Minion();
			
		}
		else if(selectedSolver == DUMMY_SOLVER) {
			this.targetSolver = new DummySolver();
		}
		
		this.constraintsPanel.changeSolver(this.targetSolver);
		this.variablePanel.changeSolver(this.targetSolver);
		this.solvingPanel.changeSolver(this.targetSolver);
		
	}
	
	
	protected void confirmSolver(String command) {
		
		this.selectSolverButton.setEnabled(false);
		
	}
	
	protected void customiseSelectionPanel() {
		
		// ========= start the panels ==========================================
		this.constraintsPanel = new ConstraintsPanel(this.targetSolver); 
		this.constraintsPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Constraint Feature Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        constraintsPanel.getBorder()));
		//this.constraintsPanel.setVisible(true);
		
		
		this.variablePanel = new VariablePanel(this.targetSolver);
		this.variablePanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Variable Feature Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        variablePanel.getBorder()));
		
		
		this.solvingPanel = new SolvingSelectionPanel(this.targetSolver);
		this.solvingPanel.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Solving Feature Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        solvingPanel.getBorder()));
		
		selectionPanel.add(constraintsPanel);
		selectionPanel.add(variablePanel);
		selectionPanel.add(solvingPanel);
	}
}
