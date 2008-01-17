package translator.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import translator.Translator;
import translator.solver.*;
import translator.normaliser.NormaliserSpecification;
import translator.TranslationSettings;

public class TranslationPanel extends JPanel {
	
	static final long serialVersionUID = 3;
	
	// field 
	int height;
	int width;
	
	// translation commands
	final String PARSE = "parse"; 
	final String INSERT_PARAMETERS = "insert parameters";
	final String EVALUATE = "evaluate";
	final String ORDER = "order";
	final String NORMALISE = "full";
	final String FLATTEN = "flatten";
	final String MINION = "to_minion";
	final String DEBUG = "debug";
	
	
	
	// Buttons for translation
	JButton parseButton;
	JComboBox normaliseComboBox;
	JButton normaliseButton;
	JButton flattenButton;
	JButton minionButton;
	
	JPanel normalisePanel;
	JPanel debugPanel;
	
	// Buttons to turn off/on the checkbox for DEBUG mode
	JButton debugModeButton;
	JCheckBox debugMode;
	
	Translator translator;
	TranslationSettings settings;
	OutputPanel outputPanel;
	
	//JTextArea output;
	//JTextArea messageOutput;
	JTextArea problemInput;
	JTextArea parameterInput;
	
	public TranslationPanel(Translator translator,
			                JTextArea problemInput,
							JTextArea parameterInput,
			                OutputPanel outputPanel,
			                TranslationSettings settings,
			                int width,
			                int height) {
		
		this.outputPanel = outputPanel;
		this.translator = translator;
		this.problemInput = problemInput;
		this.parameterInput = parameterInput;
		this.settings = settings;
		this.width = width;
		this.height = height;
		
		
		this.setLayout(new BorderLayout());
		
		createTranslationButtons();
		createDebugButtons();
		setDebugMode(this.debugMode.isSelected());	
		
		
		JPanel translationButtonPanel = new JPanel(new FlowLayout());
		translationButtonPanel.add(this.parseButton);
		translationButtonPanel.add(this.normalisePanel);
		translationButtonPanel.add(this.flattenButton);
		translationButtonPanel.add(this.minionButton);
		translationButtonPanel.setPreferredSize(new Dimension((this.width/10)*9,(this.height/7)*5));
		
		this.add(translationButtonPanel, BorderLayout.NORTH);
		this.add(this.debugPanel, BorderLayout.SOUTH);
		
		this.setPreferredSize(new Dimension(this.width, this.height));
		this.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Translation"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						this.getBorder()));
	}
	
	
	
	
	protected void createDebugButtons() {
		
		this.debugMode = new JCheckBox("debug mode");
		this.debugMode.setSelected(false);
		this.debugMode.setActionCommand(DEBUG);
		this.debugMode.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		          changeDebugMode(e.getActionCommand());   
			  }
			});
		this.debugPanel = new JPanel(new BorderLayout());
		this.debugPanel.add(debugMode, BorderLayout.SOUTH);
		this.debugPanel.setPreferredSize(new Dimension(160,200));
	}
	
	
	protected void createTranslationButtons() {
		
		Dimension buttonDimension = new Dimension(150,30);
		
		
		// ------ parse button ---------------------------------
		this.parseButton = new JButton("Parse >>");
		parseButton.setPreferredSize(buttonDimension);
		parseButton.setActionCommand(PARSE);
		parseButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		
		//---- normalise options --------------------------------
		this.normalisePanel = new JPanel(new BorderLayout());
		
		Dimension comboBoxDimension = new Dimension(160,30); 
		this.normaliseComboBox = new JComboBox(new String[] { INSERT_PARAMETERS, EVALUATE, ORDER, NORMALISE });
		this.normaliseComboBox.setSelectedIndex(3);
		this.normaliseComboBox.setPreferredSize(comboBoxDimension);
		
		this.normaliseButton = new JButton("Normalise >>");
		normaliseButton.setPreferredSize(new Dimension(100,30));
		//normaliseButton.setActionCommand((String) this.normaliseComboBox.getSelectedItem());
		normaliseButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate((String) normaliseComboBox.getSelectedItem());   
			  }
			});
		normalisePanel.add(normaliseComboBox, BorderLayout.NORTH);
		normalisePanel.add(normaliseButton, BorderLayout.SOUTH);
		normalisePanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Normalisation"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        normalisePanel.getBorder()));
		normalisePanel.setPreferredSize(new Dimension(180, 110));
		
		
		
		//---- flatten button -------------------------------------
		this.flattenButton = new JButton("Flatten >>");
		flattenButton.setPreferredSize(buttonDimension);
		flattenButton.setActionCommand(FLATTEN);
		flattenButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		
		// --- to minion button -------------------------------------
		this.minionButton = new JButton("To Minion >>");
		minionButton.setPreferredSize(buttonDimension);
		minionButton.setActionCommand(MINION);
		minionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		//minionButton.setEnabled(false);
		
		
		
	}
	
	
	public boolean getDebugMode() {
		return this.debugMode.isSelected();
	}
	
	
	protected void changeDebugMode(String DEBUG) {
		setDebugMode(this.debugMode.isSelected());	
	}
	
	
	private void setDebugMode(boolean debugOn) {
		
		if(debugOn) {
			this.parseButton.setVisible(true);
			this.normaliseButton.setVisible(true);
			this.flattenButton.setVisible(true);
			this.normaliseComboBox.setVisible(true);
			this.normalisePanel.setVisible(true);
			this.outputPanel.disableOutput(outputPanel.NORMALISE_TAB_NR);
			this.outputPanel.disableOutput(outputPanel.FLAT_TAB_NR);
			this.outputPanel.disableOutput(outputPanel.SOLUTION_TAB_NR);
			
		}
		else {
			this.parseButton.setVisible(false);
			this.normaliseButton.setVisible(false);
			this.flattenButton.setVisible(false);
			this.normaliseComboBox.setVisible(false);
			this.normalisePanel.setVisible(false);
		}
		
	}
	
	/**
	 * Translate the speicification given in the spec input area and the 
	 * parameters given in the parameter input area
	 * @param command
	 */
	protected void translate(String command) {
		
		if(command == PARSE) {
			parse();
			return;
		}
		else if(command == INSERT_PARAMETERS) {
			insertParameters();
		}
		else if(command == EVALUATE) {
			evaluate();
		}
		else if(command == ORDER) {
			order();
		}
		else if(command == NORMALISE) {
			normalise();
		}
		else if(command == FLATTEN) {
			flatten();
		}
		else if(command == MINION) {
			toMinion();
		}
	}
	
	
	protected boolean toMinion() {
		
		TargetSolver solver = this.settings.getTargetSolver();
		
		if(!(solver instanceof Minion))
			solver = new Minion();
		
		
		if(!flatten()) return false;
		
		boolean tailoring = this.translator.tailorTo(solver);
		
		if(tailoring) {	
			this.outputPanel.writeOnOutput(this.outputPanel.MINION_TAB_NR, this.translator.getTargetSolverInstance());
			this.outputPanel.writeOnMessageOutput("Tailored model to target solver "+solver.getSolverName()+"\n");
		}
		else {
			this.outputPanel.writeOnMessageOutput("===================== ERROR ======================\n"+
					"Tailoring to target solver "+solver.getSolverName()+" failed.\n"+
					this.translator.getErrorMessage()+"\n"+
					"===============================================\n");
			return false;
		}
		return true;
	}
	
	
	
	
	protected boolean flatten() {
		TargetSolver solver = new Minion();
		
		if(!normalise()) return false;
		boolean flattening = this.translator.flatten(solver);
		
		if(flattening) {
			this.outputPanel.writeOnOutput(this.outputPanel.FLAT_TAB_NR, this.translator.printAdvancedModel());
			this.outputPanel.writeOnMessageOutput("Flattened constraints for target solver "+solver.getSolverName()+"\n");
		}
		else {
			this.outputPanel.writeOnMessageOutput("================== ERROR ======================\n"+
					"Flattening for target solver "+solver.getSolverName()+" failed.\n"+
					this.translator.getErrorMessage()+"\n"+
				"===============================================\n");
			return false;
		}
		return true;
	}
	
	
	protected boolean normalise() {
		
		if(!parse()) return false;
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_FULL)) {
			this.outputPanel.writeOnOutput(this.outputPanel.NORMALISE_TAB_NR, this.translator.printAdvancedModel());
			this.outputPanel.writeOnMessageOutput("Full Normalisation (ordering, evaluation, restructuring) successful.\n");
		}
		else {
			this.outputPanel.writeOnMessageOutput("================== ERROR ======================\n"+
				"Full Normalisation (ordering, evaluation, restructuring) failed.\n"+
				this.translator.getErrorMessage()+"\n"+
				"===============================================\n");
			return false;
		}
		
		return true;
	}
	
	
	
	protected boolean order() {
		
		if(!parse()) return false;
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_ORDER)) {
			this.outputPanel.writeOnOutput(this.outputPanel.NORMALISE_TAB_NR, this.translator.printAdvancedModel());
			this.outputPanel.writeOnMessageOutput("Basic Normalisation with Ordering successful.\n");
		}
		else {
			this.outputPanel.writeOnMessageOutput("================== ERROR ======================\n"+
				"Basic Normalisation with Ordering failed.\n"+
				this.translator.getErrorMessage()+"\n"+
				"===============================================\n");
			return false;
		}
		return true;
	}
	
	/**
	 * Parse, insert parameters and evaluate
	 *
	 */
	protected boolean evaluate() {
		
		if(!parse()) return false;
		
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_EVAL)) {
			this.outputPanel.writeOnOutput(this.outputPanel.NORMALISE_TAB_NR,this.translator.printAdvancedModel());
			this.outputPanel.writeOnMessageOutput("Basic Normalisation with Evaluation successful.\n");
		}
		else {
			this.outputPanel.writeOnMessageOutput("================== ERROR ======================\n"+
				"Basic Normalisation with Evaluation failed.\n"+
			     this.translator.getErrorMessage()+"\n"+
				"===============================================\n");
			return false;
		}
		return true;
	}
	
	/**
	 * Just insert parameters into the file
	 *
	 */
	protected boolean insertParameters() {
		
		if(!parse()) return false;
		
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_BASIC)) {
			this.outputPanel.writeOnOutput(this.outputPanel.NORMALISE_TAB_NR, this.translator.printAdvancedModel());
			this.outputPanel.writeOnMessageOutput("Basic Normalisation successful.\n");
			printDebugMessages();
		}
		else {
			this.outputPanel.writeOnMessageOutput("================== ERROR ======================\n"+"Basic Normalisation failed.\n"
			     +this.translator.getErrorMessage()+"\n"+
			"===============================================\n");
			return false;

		}
		return true;
	}
	
	
	
	protected void printDebugMessages() {
		this.outputPanel.writeOnMessageOutput(this.translator.getDebugMessages());
	}
	
	/**
	 * Parse the input given in the Essence' problem and 
	 * parameter text area 
	 * 
	 *
	 */
	protected boolean parse() {
		
		String problemSpec = this.problemInput.getText();
		String parameterSpec = this.parameterInput.getText();
		
		boolean parsingSuccessfull = false;
		
		if(parameterSpec.equalsIgnoreCase("")) 
			parsingSuccessfull = translator.parse(problemSpec);
		
		else parsingSuccessfull = translator.parse(problemSpec, parameterSpec);
		
		if(parsingSuccessfull) {
			this.outputPanel.writeOnOutput(this.outputPanel.NORMALISE_TAB_NR, this.translator.printInitialProblemSpecification());
			this.outputPanel.writeOnMessageOutput("Parsing OK.\n");
		}
		else {
			this.outputPanel.writeOnMessageOutput("===================== ERROR ======================\n"+
			"Parse Error.\n"+this.translator.getErrorMessage()+"\n"+
			"===============================================\n");
			return false;
		}
		return true;
	}
	
	
	
	protected void writeOnParameterInput(String s) {
		this.parameterInput.setText("");
		this.parameterInput.append(s);
	}
	
	
	
	protected void writeOnProblemInput(String s) {
		this.problemInput.setText("");
		this.problemInput.append(s);
	}
	
}
