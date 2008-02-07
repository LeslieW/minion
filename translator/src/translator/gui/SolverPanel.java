package translator.gui;

import javax.swing.*;

import translator.solver.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import translator.TranslationSettings;

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
	
	public final boolean PUBLIC_VERSION = true;
	
	int width;
	int height;
	
	
	public final String SELECT_SOLVER = "select Solver";
	public final String CONFIRM_SOLVER = "confirm solver";
	
	/** The solvers that are available for selection */
	public final String MINION = "Minion";
	public final String DUMMY_SOLVER = "Dummy Solver";
	public final String ANGEES_SOLVER = "Angee's Solver";
	
	//TargetSolver targetSolver;
	TranslationSettings translationSettings;
	String[] availableSolvers;
	
	
	JPanel selectionPanel;
	JPanel settingsPanel;
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
	
	public SolverPanel(TranslationSettings settings,
			           int width,
			           int height) {
		this.translationSettings = settings;
		this.availableSolvers = new String[] { MINION, DUMMY_SOLVER};
	
		this.width = width;
		this.height = height;
		this.setPreferredSize(new Dimension(this.width, this.height));
		
		
		this.selectionPanel = new JPanel(new GridLayout(2,2));
		this.selectionPanel.setPreferredSize(new Dimension((width/3)*2, height));
		
		this.leftPanel = new JPanel(new BorderLayout());
		this.leftPanel.setPreferredSize(new Dimension((width/3), height));
		
		setLayout(new BorderLayout());
		
		customiseLeftPanel();
		customiseSelectionPanel();
		
		// ------- depending on if this is the public version create solver settings ------------
		if(this.PUBLIC_VERSION) {
			this.settingsPanel = new SettingsPanel(this.translationSettings,
					                               (width/3)*2, 
					                               height);
			this.add(settingsPanel);
		}
		else  {
			
			this.add(selectionPanel, BorderLayout.EAST);
			this.add(leftPanel, BorderLayout.WEST);
		}

		
		
		
		// add the panels to this panel (at least for now...)
		
		
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
		this.solverSelection.setActionCommand(SELECT_SOLVER);
		this.solverSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           selectSolver(e.getActionCommand());   
			  }
			});
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
		
		
		
		// ---------- buttons to confirm the whole selection ----------------
		JPanel buttonPanel = new JPanel(new GridLayout(0,2));
		
		this.confirmSelectionButton = new JButton("Confirm Settings");
		confirmSelectionButton.setPreferredSize(new Dimension(150,30));
		confirmSelectionButton.setActionCommand(CONFIRM_SOLVER);
		confirmSelectionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           confirmSettings(e.getActionCommand());   
			  }
			});
		
		JButton defaultSettingsButton = new JButton("Default Settings");
		defaultSettingsButton.setPreferredSize(new Dimension(150,30));
		defaultSettingsButton.setEnabled(false);
		
		buttonPanel.add(confirmSelectionButton);
		buttonPanel.add(defaultSettingsButton);
		buttonPanel.setBorder(
                BorderFactory.createEmptyBorder(15,15,15,15));
		
		
		// ------- the left panel ---------------------------
		this.leftPanel.add(comboBoxPanel, BorderLayout.NORTH);
		//this.leftPanel.add(selectSolverButton);
		this.leftPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Select another target solver with the selectSolverButton 
	 * @param command
	 */
	protected void selectSolver(String command) {
		TargetSolver targetSolver = null;
		
		String selectedSolver = (String) this.solverSelection.getSelectedItem();
		if(selectedSolver == MINION) {
			targetSolver = new Minion();
			
		}
		else if(selectedSolver == DUMMY_SOLVER) {
			targetSolver = new DummySolver();
		}
		
		else targetSolver = new Minion();
		
		this.constraintsPanel.changeSolver(targetSolver);
		this.variablePanel.changeSolver(targetSolver);
		this.solvingPanel.changeSolver(targetSolver);
		
		this.translationSettings.setTargetSolver(targetSolver);
		
	}
	
	
	protected void confirmSettings(String command) {
		
		TargetSolver targetSolver = null;
		
		String selectedSolver = (String) this.solverSelection.getSelectedItem();
		if(selectedSolver == MINION) {
			targetSolver = new Minion();
			
			// apply constraints choices
			//this.constraintsPanel.changeSolver(targetSolver);
			this.constraintsPanel.applyChoiceToSolver();
			targetSolver = this.constraintsPanel.solver;
			this.constraintsPanel.disableAllCheckboxes();
			
			// apply variable choices
			//this.variablePanel.changeSolver(targetSolver);
			this.variablePanel.applyChoiceToSolver();
			targetSolver = this.variablePanel.solver;
			this.variablePanel.disableAllCheckboxes();
			
			// apply solving choices
			//this.solvingPanel.changeSolver(targetSolver);
			this.solvingPanel.applyChoiceToSolver();
			targetSolver = this.solvingPanel.solver;
			this.solvingPanel.disableAllCheckboxes();
			
			this.translationSettings.setTargetSolver(targetSolver);
		}
		
		
		
		
	}
	
	protected void customiseSelectionPanel() {
		
		// ========= start the panels ==========================================
		this.constraintsPanel = new ConstraintsPanel(this.translationSettings.getTargetSolver()); 
		this.constraintsPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Constraint Feature Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        constraintsPanel.getBorder()));
		//this.constraintsPanel.setVisible(true);
		
		
		this.variablePanel = new VariablePanel(this.translationSettings.getTargetSolver());
		this.variablePanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Variable Feature Selection"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        variablePanel.getBorder()));
		
		
		this.solvingPanel = new SolvingSelectionPanel(this.translationSettings.getTargetSolver());
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
