package translator.gui;

import javax.swing.*;

import translator.TranslationSettings;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
//import java.io.FileWriter;

import javax.swing.JFileChooser;

public class SettingsPanel extends JPanel {

	static final long serialVersionUID = 11;
	
	TranslationSettings settings;


	int width;
	int height;
	
	String outPutFileName;
	
	JFileChooser fileChooser;
	JTextField minionPathField;
	
	JCheckBox commonSubExpressions;
	JCheckBox strictCopyPropagation;
	
	JComboBox branchingComboBox;
	JComboBox searchComboBox;
	
	JPanel minionPathPanel;
	JPanel reformulationPanel;
	JPanel buttonPanel;
	JPanel solvingSettingsPanel;
	
	// components of the leftPanel
	JComboBox solverSelection;
	JButton selectSolverButton;
	JButton confirmSelectionButton;
	
	public SettingsPanel(TranslationSettings settings,
			             int width,
			             int height) {
		
		this.settings = settings;
		this.width = width;
		this.height = height;
		this.setPreferredSize(new Dimension(width, height));
		
		
		this.fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		this.setLayout(new FlowLayout());
		
		setUpMinionPathChooser();
		setupReformulationSettings();
		setupSolvingSettingsPanel();
		customiseButtonPanel();
		
		this.solvingSettingsPanel.setPreferredSize(new Dimension((width/7)*3, height/4));
		this.reformulationPanel.setPreferredSize(new Dimension((width/7)*3, height/4));
		//this.buttonPanel.setPreferredSize(new Dimension(width/2,height/8));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(this.solvingSettingsPanel, BorderLayout.WEST);
		panel.add(this.reformulationPanel, BorderLayout.EAST);
		panel.setPreferredSize(new Dimension((width/10)*9, (height/4)*3));
		
		
		
		this.add(this.minionPathPanel, BorderLayout.NORTH);
		this.add(panel, BorderLayout.CENTER);
		//this.add(this.reformulationPanel, BorderLayout.CENTER);
		//this.add(this.solvingSettingsPanel, BorderLayout.SOUTH);
		this.add(this.buttonPanel, BorderLayout.SOUTH);
		
		
		this.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("General Settings"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						this.getBorder()));
		
		
	}

	
	protected void customiseButtonPanel() {
		
		
		
		// ---------- buttons to confirm the whole selection ----------------
		this.buttonPanel = new JPanel(new GridLayout(0,2));
		Dimension buttonDimension = new Dimension(150,30);	
		
		this.confirmSelectionButton = new JButton("Confirm Settings");
		//confirmSelectionButton.setPreferredSize(new Dimension(150,30));
		confirmSelectionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           confirmSettings();   
			  }
			});
		this.confirmSelectionButton.setPreferredSize(buttonDimension);
		
		JButton defaultSettingsButton = new JButton("Default Settings");
		//defaultSettingsButton.setPreferredSize(new Dimension(150,30));
		defaultSettingsButton.setEnabled(false);
		
		defaultSettingsButton.setPreferredSize(buttonDimension);
		
		
		buttonPanel.add(confirmSelectionButton);
		buttonPanel.add(defaultSettingsButton);
		buttonPanel.setBorder(
                BorderFactory.createEmptyBorder(25,25,25,25));
		buttonPanel.setPreferredSize(new Dimension((this.width/10)*6, (this.height/8)));
		
		
	}
	
	
	
	protected void confirmSettings() {
		
		this.settings.setApplyStrictCopyPropagation(this.strictCopyPropagation.isSelected());
		this.settings.setUseCommonSubExpressions(this.commonSubExpressions.isSelected());
		
	}
	
	
	private void setUpMinionPathChooser() {
		
		// ---- a panel for the path selection -------------------
		this.minionPathPanel = new JPanel(new FlowLayout());
		
		String minionPath = this.settings.getPathToMinion();
		
		this.minionPathField = new JTextField(minionPath);
		minionPathField.setColumns(50);
	
		JLabel pathLabel = new JLabel("Path to Minion executable:");
		
		JButton pathButton = new JButton("Change path");
		pathButton.setPreferredSize(new Dimension(150,30));
		pathButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changePath();   
			  }
			});
		
		
		minionPathPanel.add(pathLabel);
		minionPathPanel.add(minionPathField);
		minionPathPanel.add(pathButton);
	}
	
	
	
	
	private void setupReformulationSettings() {
		
		this.reformulationPanel = new JPanel(new GridLayout(2,0));
		
		this.commonSubExpressions = new JCheckBox("Re-use common subexpressions");
		this.commonSubExpressions.setSelected(this.settings.useCommonSubExpressions());
		
		this.strictCopyPropagation = new JCheckBox("Apply strict copy propagation");
		this.strictCopyPropagation.setSelected(this.settings.applyStrictCopyPropagation());
		
		this.reformulationPanel.add(this.commonSubExpressions);
		this.reformulationPanel.add(this.strictCopyPropagation);
		
		this.reformulationPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Reformulation Settings"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						this.getBorder()));
		
		this.reformulationPanel.setPreferredSize(new Dimension((width/5)*2,(height/4)*3 ));
		
	}
	
	
	
	protected void setupSolvingSettingsPanel() {
		
		this.solvingSettingsPanel = new JPanel(new GridLayout(2,0));
		
		Dimension comboBoxDimension = new Dimension(300,30);
		
		JPanel searchPanel = new JPanel();
		this.searchComboBox = new JComboBox(this.settings.getTargetSolver().getSearchStrategies());
		this.searchComboBox.setSelectedIndex(0);
		this.searchComboBox.setPreferredSize(comboBoxDimension);
		searchPanel.add(searchComboBox);
		searchPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Choose Search Strategy"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        searchPanel.getBorder()));
		
		
		JPanel branchingPanel = new JPanel();
		this.branchingComboBox = new JComboBox(this.settings.getTargetSolver().getBranchingStrategies());
		this.branchingComboBox.setSelectedIndex(0);
		this.branchingComboBox.setPreferredSize(comboBoxDimension);
		branchingPanel.add(branchingComboBox);
		branchingPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Choose Branching Strategy (Variable Ordering)"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        branchingPanel.getBorder()));
		
		this.solvingSettingsPanel.add(branchingPanel);
		this.solvingSettingsPanel.add(searchPanel);
		this.solvingSettingsPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Solving Settings"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						this.getBorder()));
	}
	
	private void changePath() {
		
		int returnVal = this.fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				
				String minionPath = file.getAbsolutePath();
				this.minionPathField.setText(minionPath);
				this.settings.setPathToMinion(minionPath);
  			
			} catch(Exception e) {
				System.out.println("Could not change path to Minion:\n"+e.getMessage()+"\n");
				return;
			}
                	
			
		} /*else {
			writeOnMessageOutput("Changing Minion path command cancelled by user.\n");
		}	*/
	}

	public void updateCheckBoxes() {
		this.searchComboBox.removeAllItems();
		String[] searchStrategies = this.settings.getTargetSolver().getSearchStrategies();
		for(int i=0; i<searchStrategies.length; i++)
			this.searchComboBox.addItem(searchStrategies[i].toString());
		
		this.branchingComboBox.removeAllItems();
		String[] branchingStrategies = this.settings.getTargetSolver().getBranchingStrategies();
		for(int i=0; i<branchingStrategies.length; i++)
			this.branchingComboBox.addItem(branchingStrategies[i].toString());

	}
	
	
}

