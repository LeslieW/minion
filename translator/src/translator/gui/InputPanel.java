package translator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class InputPanel extends JPanel {

	static final long serialVersionUID = 7;

    // actionCommands
	final String SAVE_PROBLEM = "save_problem";
	final String SAVE_PARAMETER = "save_parameter";
	final String SAVE_OUTPUT = "save_output";
	final String LOAD_PROBLEM = "load_problem";
	final String LOAD_PARAMETER = "load_parameter";
	
	// panels
	JPanel problemPane;
	JPanel parameterPane;
	
	// panel components
	JTextArea problemInput;
	JScrollPane problemScrollPane;
	JTextArea parameterInput;
	JScrollPane parameterScrollPane;
	
	JFileChooser fileChooser;

	// general design things
	int width;
	int problemHeight;
	int parameterHeight;
	String problemFieldLabel = "Essence' Problem Model";
	String parameterFieldLabel = "Essence' Parameter Specification";	
	
	
	//external components
	JTextArea messageOutput;
	JTextArea output;
	
	public InputPanel(int width,
					  int problemHeight,
					  int parameterHeight,
			          JTextArea output,
			          JTextArea messageOutput) {
		
		this.width = width;
		this.problemHeight = problemHeight;
		this.parameterHeight = parameterHeight;
		this.output = output;
		this.messageOutput = messageOutput;
		this.fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		generateProblemInput();
		generateParameterInput();
		
	      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                  problemPane,
                  parameterPane);
	      splitPane.setOneTouchExpandable(true);
	      splitPane.setResizeWeight(0.5);
	      
	      this.add(splitPane);
	}
	
	
	public JTextArea getProblemInput() {
		return this.problemInput;
	}
	
	public JTextArea getParameterInput() {
		return this.parameterInput;
	}
	
	
	protected void generateProblemInput() {
		
		// problem text field  initialisation
		this.problemInput = new JTextArea("ESSENCE' 1.0\n", this.width, this.problemHeight);
		problemInput.setLineWrap(true);
		
		JLabel problemLabel = new JLabel(this.problemFieldLabel);
		problemLabel.setLabelFor(problemInput);
		
		this.problemScrollPane = new JScrollPane(problemInput);
		problemScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		problemScrollPane.setPreferredSize(new Dimension(width, this.problemHeight)); 
		
		
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
		
		problemButtonPanel.setBorder(
                BorderFactory.createEmptyBorder(10,10,10,10));
		
		
		// construct the panel
		this.problemPane = new JPanel(new BorderLayout());
		problemPane.add(this.problemScrollPane, BorderLayout.NORTH);
		problemPane.add(problemButtonPanel, BorderLayout.SOUTH);
		problemPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.problemFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        problemPane.getBorder()));
		
	}
	
	
	
	
	protected void generateParameterInput() {
		
//		 ============================ parameter panel ===============================
		this.parameterInput = new JTextArea("ESSENCE' 1.0\n",this.width, this.parameterHeight);
		parameterInput.setLineWrap(true);
		this.parameterScrollPane = new JScrollPane(parameterInput);
		parameterScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		parameterScrollPane.setPreferredSize(new Dimension(this.width, this.parameterHeight));
	/*	parameterScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.parameterFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        parameterScrollPane.getBorder()));*/
		
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
		
		
		this.parameterPane = new JPanel(new BorderLayout());
		parameterPane.add(parameterScrollPane,BorderLayout.NORTH);
		parameterPane.add(parameterButtonPanel, BorderLayout.SOUTH);
		parameterPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.parameterFieldLabel),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        parameterPane.getBorder()));
		
		
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
