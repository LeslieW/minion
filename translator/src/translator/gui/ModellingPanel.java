package translator.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.*;
import translator.Translator;

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
	
	
	// the panels
	TranslationPanel translationButtonPanel;
	
	
	JFileChooser fileChooser;
	
	// some parameters for the sizes of textfields and buttons
	int modelcols = 500;
	int modelrows = 400;
	
	int parameterRows = 100;
	int messagesRows = 100;
	
	Color backgroundColor = Color.GREEN;
	String problemFieldLabel = "Essence' Problem Model";
	String parameterFieldLabel = "Essence' Parameter Specification";	
	String outputFieldLabel = "Translation Output";
	
	// actionCommands

	
	final String SAVE_PROBLEM = "save_problem";
	final String SAVE_PARAMETER = "save_parameter";
	final String SAVE_OUTPUT = "save_output";
	final String LOAD_PROBLEM = "load_problem";
	final String LOAD_PARAMETER = "load_parameter";
	
	
	public ModellingPanel() {
	
		translator = new Translator();
		
		setLayout(new BorderLayout());
		
	
		
		// ================= problem panel =====================================
		
		// problem text field 
		this.problemInput = new JTextArea("ESSENCE' 1.0\n", modelrows, modelcols);
		this.problemScrollPane = new JScrollPane(problemInput);
		problemScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		problemScrollPane.setPreferredSize(new Dimension(500, 450));

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
		parameterScrollPane.setPreferredSize(new Dimension(500, 250));
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
		messageScrollPane.setPreferredSize(new Dimension(500,200));
		messageScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("System Messages"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        messageScrollPane.getBorder()));
		JLabel messageLabel = new JLabel("System Messages");
		messageLabel.setLabelFor(messageOutput);
		
		
		// ================== translator button panel =======================
		

		
		
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
		
		this.translationButtonPanel = new TranslationPanel(this.translator,
															this.problemInput,
															this.parameterInput,
															this.output,
															this.messageOutput);
		
		add(leftPanel, BorderLayout.WEST);
		add(this.translationButtonPanel, BorderLayout.CENTER);
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
