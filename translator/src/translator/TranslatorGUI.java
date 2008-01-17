package translator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.*;

public class TranslatorGUI extends JPanel  {
	
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
	
	
	public TranslatorGUI() {
	
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
		JButton saveProblemButton = new JButton("Save problem");
		saveProblemButton.setActionCommand(SAVE_PROBLEM);
		saveProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});
		
		JButton clearProblemButton = new JButton("Clear problem");
		
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
		JButton saveParameterButton = new JButton("Save parameter");
		saveParameterButton.setActionCommand(SAVE_PARAMETER);
		saveParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});
		JButton clearParameterButton = new JButton("Clear parameter");
		
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
		
		Dimension buttonDimension = new Dimension(170,30);
		
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
		
		JButton orderButton = new JButton("Order >>");
		orderButton.setPreferredSize(buttonDimension);
		orderButton.setActionCommand(ORDER);
		orderButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		JButton evaluateButton = new JButton("Evaluate >>");
		evaluateButton.setPreferredSize(buttonDimension);
		evaluateButton.setActionCommand(EVALUATE);
		evaluateButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		
		
		JButton normaliseButton = new JButton("Normalise >>");
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
		
		JPanel translationButtonPanel = new JPanel(new FlowLayout());
		translationButtonPanel.add(parseButton);
		translationButtonPanel.add(insertParametersButton);
		translationButtonPanel.add(orderButton);
		translationButtonPanel.add(evaluateButton);
		translationButtonPanel.add(normaliseButton);
		translationButtonPanel.add(flattenButton);
		
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
	
	
	
	protected void save(String command) {
	
		int returnVal = this.fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				    if(file.createNewFile()) ;
					FileWriter writer = new FileWriter(file);
                
					if(!file.canRead())
						writeOnMessageOutput("Cannot read file: "+file.toString());
					else if(!file.canWrite())
						writeOnMessageOutput("Cannot write file: "+file.toString());
					
					
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
	}
	
	
	protected void order() {
		
		parse();
		if(this.translator.normaliseOrder()) {
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
		
		parse();
		if(this.translator.normaliseEvaluate()) {
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
		if(this.translator.normaliseBasic()) {
			writeOnOutput(this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation successful.\n");
		}
		else {
			writeOnMessageOutput("Basic Normalisation failed.\n");
			writeOnMessageOutput(this.translator.getErrorMessage());
		}
		
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
			writeOnOutput(this.translator.printEssenceSpecification());
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
	
	
	/**
	 * Append the message on the message output
	 * 
	 * @param outputMessage
	 */
	protected void writeOnMessageOutput(String outputMessage) {
		this.messageOutput.append(outputMessage);
	}
	
	
	
	
}
