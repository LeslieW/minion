package translator.gui;

import translator.solver.TargetSolver;
import java.awt.*;

import javax.swing.*;

public class SolvingSelectionPanel extends JPanel implements SelectionPanel {

	static final long serialVersionUID = 6;
	
	TargetSolver solver;
	
	JPanel checkPanel;
	
	
	JPanel searchPanel;
	JComboBox searchComboBox;
	JPanel branchingPanel;
	JComboBox branchingComboBox;
	
	// =========== CONSTRUCTOR ============================
	
	public SolvingSelectionPanel(TargetSolver solver) {
		this.solver = solver;
		
		createComboBoxes();
		createComboBoxPanel();
		
		this.add(checkPanel);
	}
	
	// ========== INHERITED METHODS ========================
	
	public void applyChoiceToSolver() {
		String selectedBranching = (String) this.branchingComboBox.getSelectedItem();
		this.solver.setBranchingStrategy(selectedBranching);
		
		String selectedSearch = (String) this.searchComboBox.getSelectedItem();
		this.solver.setSearchStrategy(selectedSearch);
	}

	public void changeSolver(TargetSolver otherSolver) {
		this.solver = otherSolver;
		updateCheckBoxes();

	}

	public TargetSolver getSolver() {
		return this.solver;
	}

	public void updateCheckBoxes() {
		this.searchComboBox.removeAllItems();
		String[] searchStrategies = this.solver.getSearchStrategies();
		for(int i=0; i<searchStrategies.length; i++)
			this.searchComboBox.addItem(searchStrategies[i].toString());
		
		this.branchingComboBox.removeAllItems();
		String[] branchingStrategies = this.solver.getBranchingStrategies();
		for(int i=0; i<branchingStrategies.length; i++)
			this.branchingComboBox.addItem(branchingStrategies[i].toString());

	}

	// ======== OTHER METHODS ================================
	
	private void createComboBoxes() {
		
		Dimension comboBoxDimension = new Dimension(300,30);
		
		this.searchPanel = new JPanel();
		this.searchComboBox = new JComboBox(this.solver.getSearchStrategies());
		this.searchComboBox.setSelectedIndex(0);
		this.searchComboBox.setPreferredSize(comboBoxDimension);
		searchPanel.add(searchComboBox);
		searchPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Choose Search Strategy"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        searchPanel.getBorder()));
		
		
		this.branchingPanel = new JPanel();
		this.branchingComboBox = new JComboBox(this.solver.getBranchingStrategies());
		this.branchingComboBox.setSelectedIndex(0);
		this.branchingComboBox.setPreferredSize(comboBoxDimension);
		branchingPanel.add(branchingComboBox);
		branchingPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Choose Branching Strategy (Variable Ordering)"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        branchingPanel.getBorder()));
	}
	
	private void createComboBoxPanel() {
		this.checkPanel = new JPanel(new GridLayout(0, 1));
		checkPanel.add(branchingPanel);
		checkPanel.add(searchPanel);
	}
	
	
	public void disableAllCheckboxes() {
		
		this.searchComboBox.setEnabled(false);
		this.branchingComboBox.setEnabled(false);
		
	}
}
