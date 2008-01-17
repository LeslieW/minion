package translator.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.*;
import translator.Translator;

import translator.normaliser.NormaliserSpecification;
import translator.solver.*;

public class ModellingPanel extends JPanel  {
	
	static final long serialVersionUID = 0;
	
	Translator translator;
	
	// all the fields
	JTextArea problemInput;
	JScrollPane problemScrollPane;
	JTextArea parameterInput;
	JScrollPane parameterScrollPane;
	
	JTextArea output;
	JScrollPane outputScrollPane;
	JTextArea messageOutput;
	JScrollPane messageScrollPane;
	
	// Buttons
	JButton parseButton;
	
	JFileChooser fileChooser;
	
	// some parameters for the sizes of textfields and buttons
	int modelcols = 500;
	int modelrows = 400;
	
	int parameterRows = 100;
	int messagesRows = 100;
	
	Color backgroundColor = Color.GREEN;
	String problemFieldLabel = "Essence' Problem Model";
	String parameterFieldLabel = "Essence' Parameter Specification";	
	String outputFieldLabel = "Essence' Reformulated";
	
	// actionCommands
	final String PARSE = "parse"; 
	final String INSERT_PARAMETERS = "insertParameters";
	final String EVALUATE = "evaluate";
	final String ORDER = "order";
	final String NORMALISE = "normalise";
	final String FLATTEN = "flatten";
	
	final String SAVE_PROBLEM = "save_problem";
	final String SAVE_PARAMETER = "save_parameter";
	final String SAVE_OUTPUT = "save_output";
	final String LOAD_PROBLEM = "load_problem";
	final String LOAD_PARAMETER = "load_parameter";
	final String MINION = "to_minion";
	
	public ModellingPanel() {
	
		translator = new Translator();
		
		setLayout(new BorderLayout());
		
	
		
		// ================= problem panel =====================================
		
		// problem text field 
		this.problemInput = new JTextArea("ESSENCE' 1.0\n", modelrows, modelcols);
		this.problemScrollPane = new JScrollPane(problemInput);
		problemScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		problemScrollPane.setPreferredSize(new Dimension(500, 400));

		JLabel problemLabel = new JLabel(this.problemFieldLabel);
		problemLabel.setLabelFor(problemInput);
		problemInput.setLineWrap(true);
		/*problemScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.problemFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        problemScrollPane.getBorder()));
		*/

		// problem buttons
		JButton loadProblemButton = new JButton("Load problem");
		//loadProblemButton.setEnabled(false);
		loadProblemButton.setActionCommand(LOAD_PROBLEM);
		loadProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           load(e.getActionCommand());   
			  }
			});		
		
		JButton saveProblemButton = new JButton("Save problem");
		saveProblemButton.setActionCommand(SAVE_PROBLEM);
		saveProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});
		
		JButton clearProblemButton = new JButton("Clear problem");
		clearProblemButton.setEnabled(false);
		
		JPanel problemButtonPanel = new JPanel(new GridLayout(0,3));
		problemButtonPanel.add(saveProblemButton,0);
		problemButtonPanel.add(loadProblemButton,1);
		problemButtonPanel.add(clearProblemButton,2);
	/*	problemButtonPanel.setBorder(BorderFactory.createCompoundBorder(
				                    BorderFactory.createEmptyBorder(5,5,5,5),
	                             	problemScrollPane.getBorder()));
	                             	*/
		problemButtonPanel.setBorder(
                BorderFactory.createEmptyBorder(10,10,10,10));
		
		
		JPanel problemPane = new JPanel(new BorderLayout());
		problemPane.add(problemScrollPane, BorderLayout.NORTH);
		problemPane.add(problemButtonPanel, BorderLayout.SOUTH);
		problemPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.problemFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        problemPane.getBorder()));
		
		
		
		this.fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		
		// ============================ parameter panel ===============================
		this.parameterInput = new JTextArea("ESSENCE' 1.0\n",parameterRows, modelcols);
		parameterInput.setLineWrap(true);
		this.parameterScrollPane = new JScrollPane(parameterInput);
		parameterScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		parameterScrollPane.setPreferredSize(new Dimension(500, 150));
		parameterScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.parameterFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        parameterScrollPane.getBorder()));
		
		JLabel parameterLabel = new JLabel(this.parameterFieldLabel);
		parameterLabel.setLabelFor(parameterInput);
		
		// parameter button panel
		JButton loadParameterButton = new JButton("Load parameter");
		loadParameterButton.setActionCommand(LOAD_PARAMETER);
		loadParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           load(e.getActionCommand());   
			  }
			});		
		
		JButton saveParameterButton = new JButton("Save parameter");
		saveParameterButton.setActionCommand(SAVE_PARAMETER);
		saveParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});
		JButton clearParameterButton = new JButton("Clear parameter");
		clearParameterButton.setEnabled(false);
		
		JPanel parameterButtonPanel = new JPanel(new GridLayout(0,3));
		parameterButtonPanel.add(saveParameterButton,0);
		parameterButtonPanel.add(loadParameterButton,1);
		parameterButtonPanel.add(clearParameterButton,2);
		parameterButtonPanel.setBorder(
                BorderFactory.createEmptyBorder(10,10,10,10));
		
		
		JPanel parameterPane = new JPanel(new BorderLayout());
		parameterPane.add(parameterScrollPane,BorderLayout.NORTH);
		parameterPane.add(parameterButtonPanel, BorderLayout.SOUTH);
		
		
		// split pane for modelling and put problem and parameter panes into it
	      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                  problemPane,
                  parameterPane);
	      splitPane.setOneTouchExpandable(true);
	      splitPane.setResizeWeight(0.5);
		
		
	      
	      
	      
	      
		this.output = new JTextArea("",modelrows, modelcols);
		this.outputScrollPane = new JScrollPane(output);
		this.output.setEditable(false);
		outputScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScrollPane.setPreferredSize(new Dimension(500, 600));
		outputScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.outputFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        outputScrollPane.getBorder()));
		JLabel outputLabel = new JLabel(this.outputFieldLabel);
		outputLabel.setLabelFor(output);
		
		
		
		
		this.messageOutput = new JTextArea("", messagesRows, modelcols);
		this.messageScrollPane = new JScrollPane(messageOutput);
		this.messageOutput.setEditable(false);
		messageScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		messageScrollPane.setPreferredSize(new Dimension(500,100));
		messageScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("System Messages"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        messageScrollPane.getBorder()));
		JLabel messageLabel = new JLabel("System Messages");
		messageLabel.setLabelFor(messageOutput);
		
		
		// ================== translator button panel =======================
		
		Dimension buttonDimension = new Dimension(180,30);
		
		JButton parseButton = new JButton("Parse >>");
		parseButton.setPreferredSize(buttonDimension);
		parseButton.setActionCommand(PARSE);
		parseButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		JButton insertParametersButton = new JButton("Insert Parameters >>");
		insertParametersButton.setPreferredSize(buttonDimension);
		insertParametersButton.setActionCommand(INSERT_PARAMETERS);
		insertParametersButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		JButton orderButton = new JButton("Normalise: Order >>");
		orderButton.setPreferredSize(buttonDimension);
		orderButton.setActionCommand(ORDER);
		orderButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		JButton evaluateButton = new JButton("Normalise: Evaluate >>");
		evaluateButton.setPreferredSize(buttonDimension);
		evaluateButton.setActionCommand(EVALUATE);
		evaluateButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		
		JButton normaliseButton = new JButton("Normalise: Full >>");
		normaliseButton.setPreferredSize(buttonDimension);
		normaliseButton.setActionCommand(NORMALISE);
		normaliseButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		
		JButton flattenButton = new JButton("Flatten >>");
		flattenButton.setPreferredSize(buttonDimension);
		flattenButton.setActionCommand(FLATTEN);
		flattenButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		
		JButton minionButton = new JButton("To Minion >>");
		minionButton.setPreferredSize(buttonDimension);
		minionButton.setActionCommand(MINION);
		minionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		minionButton.setEnabled(false);
		
		
		JPanel translationButtonPanel = new JPanel(new FlowLayout());
		translationButtonPanel.add(parseButton);
		translationButtonPanel.add(insertParametersButton);
		translationButtonPanel.add(orderButton);
		translationButtonPanel.add(evaluateButton);
		translationButtonPanel.add(normaliseButton);
		translationButtonPanel.add(flattenButton);
		translationButtonPanel.add(minionButton);
		
		translationButtonPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Translation"),
								BorderFactory.createEmptyBorder(10,10,10,10)),
        translationButtonPanel.getBorder()));
		translationButtonPanel.setPreferredSize(new Dimension(180, 600));
		
		
		// ===================== right panel ===============================
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(outputScrollPane, 
	                    BorderLayout.PAGE_START);
		rightPanel.add(messageScrollPane,
	                    BorderLayout.CENTER);

	
		
		// ====================== left panel ===============================
		JPanel leftPanel = new JPanel(new GridLayout(1,0));
		leftPanel.add(splitPane);
		leftPanel.setBorder(BorderFactory.createCompoundBorder(
	                       BorderFactory.createTitledBorder("Essence' Modelling"),
	                       BorderFactory.createEmptyBorder(5,5,5,5)));

		
		
		// =================== all together =================================
		add(leftPanel, BorderLayout.WEST);
		add(translationButtonPanel, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.EAST); 
		
	}
	
	
	/**
	 * Load a problem, parameter or output file, depending on the 
	 * command passed. 	
	 * @param command
	 */
	protected void load(String command) {
		
			
			fileChooser = new JFileChooser();
			
			String type = "";
			if(command.endsWith(LOAD_PROBLEM))
				type = "problem";
			else if(command.equalsIgnoreCase(LOAD_PARAMETER))
				type = "parameter";

			fileChooser.setDialogTitle("Choose "+type+" File");
		    int returnVal = fileChooser.showOpenDialog(this);
		    
		    // TODO: write this into a text-area instead of printing it on out
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	  File file = fileChooser.getSelectedFile();
				    String fileName = file.getPath();
				    
		    	if(command.endsWith(LOAD_PROBLEM))
		    		writeOnMessageOutput("Loading problem file: " +fileName+"\n");
		    	else if(command.endsWith(LOAD_PARAMETER))
		    		writeOnMessageOutput("Loading parameter file: " +fileName+"\n");		   
			
		    try {
		    	BufferedReader reader = new BufferedReader(new FileReader(file));
		    	String loadedString = "";
		    	String s = reader.readLine();
		    	while(s != null && !(s.equalsIgnoreCase("null"))) {
		    		//s = reader.readLine();
		    		//if(s != null || s.equalsIgnoreCase("null")) break;
		    		loadedString = loadedString.concat(s+"\n");
		    		s = reader.readLine();
		    	}
		    	
		    	
		    	if(command.equalsIgnoreCase(LOAD_PROBLEM))
		    		writeOnProblemInput(loadedString);
		    	else if(command.equalsIgnoreCase(LOAD_PARAMETER))
		    		writeOnParameterInput(loadedString);
		    	
		    } catch(Exception e) {
		    	writeOnMessageOutput(e.getMessage());
		    	return;
		    }
		    
		    }
		    else {
	            this.messageOutput.append("Choosing file cancelled by user.\n");
	        }
		
	}
	
	
	/**
	 * Save a problem, parameter or output file, depending on the 
	 * command passed. 
	 * 
	 * @param command
	 */
	protected void save(String command) {
	
		int returnVal = this.fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				    if(file.createNewFile()) ;
					FileWriter writer = new FileWriter(file);
                
					if(!file.canRead())
						writeOnMessageOutput("Cannot read file: "+file.toString()+"\n");
					else if(!file.canWrite())
						writeOnMessageOutput("Cannot write file: "+file.toString()+"\n");
					
					
					if(command == SAVE_PROBLEM) { 
                    	writer.write(this.problemInput.getText());
                    	writeOnMessageOutput("Saving Essence' problem in: " + file.getName() + "." +"\n");
                    }
					else if(command == SAVE_PARAMETER) {
						writer.write(this.parameterInput.getText());
                    	writeOnMessageOutput("Saving Essence' parameter in: " + file.getName() + "." +"\n");
					}
					else if(command == SAVE_OUTPUT) {
						writer.write(this.output.getText());
                    	writeOnMessageOutput("Saving output in: " + file.getName() + "." +"\n");
					}
					writer.flush();
                    writer.close();
                    
        	
					
			} catch(Exception e) {
				writeOnMessageOutput(e.getMessage()+"\n");
				return;
			}
                	
			
		} else {
			writeOnMessageOutput("Save command cancelled by user.");
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
	}
	
	
	protected void flatten() {
		TargetSolver solver = new Minion();
		
		if(this.translator.getInitialProblemSpecification() == null)
			parse();
		if(!this.translator.hasBeenNormalised())
			normalise();
		boolean flattening = this.translator.flatten(solver);
		
		if(flattening) {
			writeOnOutput(this.translator.printAdvancedModel());
			writeOnMessageOutput("Flattened constraints for target solver "+solver.getSolverName()+"\n");
		}
		else {
			writeOnMessageOutput("Flattening for target solver "+solver.getSolverName()+" failed.\n");
			writeOnMessageOutput(this.translator.getErrorMessage());			
		}
		
	}
	
	
	protected void normalise() {
		
		if(this.translator.getInitialProblemSpecification() == null)
			parse();
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_FULL)) {
			writeOnOutput(this.translator.printAdvancedModel());
			writeOnMessageOutput("Full Normalisation (ordering, evaluation, restructuring) successful.\n");
		}
		else {
			writeOnMessageOutput("Full Normalisation (ordering, evaluation, restructuring) failed.\n");
			writeOnMessageOutput(this.translator.getErrorMessage());
		}
	}
	
	
	protected void order() {
		
		if(this.translator.getInitialProblemSpecification() == null)
			parse();
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_ORDER)) {
			writeOnOutput(this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation with Ordering successful.\n");
		}
		else {
			writeOnMessageOutput("Basic Normalisation with Ordering failed.\n");
			writeOnMessageOutput(this.translator.getErrorMessage());
		}
	}
	
	/**
	 * Parse, insert parameters and evaluate
	 *
	 */
	protected void evaluate() {
		
		if(this.translator.getInitialProblemSpecification() == null)
			parse();
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_EVAL)) {
			writeOnOutput(this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation with Evaluation successful.\n");
		}
		else {
			writeOnMessageOutput("Basic Normalisation with Evaluation failed.\n");
			writeOnMessageOutput(this.translator.getErrorMessage());
		}
	}
	
	/**
	 * Just insert parameters into the file
	 *
	 */
	protected void insertParameters() {
		
		parse();
		if(this.translator.normalise(NormaliserSpecification.NORMALISE_BASIC)) {
			writeOnOutput(this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation successful.\n");
			printDebugMessages();
		}
		else {
			writeOnMessageOutput("Basic Normalisation failed.\n");
			writeOnMessageOutput(this.translator.getErrorMessage());
			printDebugMessages();

		}
		
	}
	
	
	
	protected void printDebugMessages() {
		writeOnMessageOutput(this.translator.getDebugMessages());
	}
	
	/**
	 * Parse the input given in the Essence' problem and 
	 * parameter text area 
	 * 
	 *
	 */
	protected void parse() {
		
		String problemSpec = this.problemInput.getText();
		String parameterSpec = this.parameterInput.getText();
		
		boolean parsingSuccessfull = false;
		
		if(parameterSpec.equalsIgnoreCase("")) 
			parsingSuccessfull = translator.parse(problemSpec);
		
		else parsingSuccessfull = translator.parse(problemSpec, parameterSpec);
		
		if(parsingSuccessfull) {
			writeOnOutput(this.translator.printInitialProblemSpecification());
			writeOnMessageOutput("Parsing OK.\n");
		}
		else {
			writeOnMessageOutput("Parse Error.\n"+this.translator.getErrorMessage()+"\n");
		}
	}
	
	/**
	 * Clear the whole output and replace it with the parameter
	 * String outputMessage
	 * @param outputMessage
	 */
	protected void writeOnOutput(String outputMessage) {
		this.output.setText("");
		this.output.append(outputMessage);
	}
	
	
	protected void writeOnParameterInput(String s) {
		this.parameterInput.setText("");
		this.parameterInput.append(s);
	}
	
	/**
	 * Append the message on the message output
	 * 
	 * @param outputMessage
	 */
	protected void writeOnMessageOutput(String outputMessage) {
		this.messageOutput.append(outputMessage);
	}
	
	protected void writeOnProblemInput(String s) {
		this.problemInput.setText("");
		this.problemInput.append(s);
	}
	
	
}
