package translator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OutputPanel extends JPanel {

	static final long serialVersionUID = 8;
		
	   // actionCommands
	final String SAVE_PROBLEM = "save_problem";
	final String SAVE_PARAMETER = "save_parameter";
	final String SAVE_OUTPUT = "save_output";
	final String LOAD_PROBLEM = "load_problem";
	final String LOAD_PARAMETER = "load_parameter";
	
	JFileChooser fileChooser;

	
	// panels
	JPanel outputPanel;
	JPanel messagePanel;
	
	// components
	JTextArea output;
	JScrollPane outputScrollPane;
	JTextArea messageOutput;
	JScrollPane messageScrollPane;
	
	
	
	// general design things
	int width;
	int outputHeight;
	int messagesHeight;
	String outputLabelString = "Translation Output";
	String messagesLabelString = "System Messages";	
	
	
	
	public OutputPanel(int width,
			           int outputHeight,
			           int messagesHeight) {
		
		this.width = width;
		this.outputHeight = outputHeight;
		this.messagesHeight = messagesHeight;
		
		this.setLayout(new BorderLayout());
		this.fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		
		generateOutput();
		generateMessagesOutput();
		
		this.add(this.outputPanel, 
	                    BorderLayout.PAGE_START);
		this.add(this.messagePanel,
	                    BorderLayout.PAGE_END);
		
	}
	
	
	public JTextArea getOutPut() {
		return this.output;
	}
	
	public JTextArea getMessageOutput() {
		return this.messageOutput;
	}
	
	
	protected void generateOutput() {
		
		// text area 
		this.output = new JTextArea("",width, outputHeight);
		this.outputScrollPane = new JScrollPane(output);
		this.output.setEditable(false);
		outputScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScrollPane.setPreferredSize(new Dimension(width, outputHeight));

		JLabel outputLabel = new JLabel(this.outputLabelString);
		outputLabel.setLabelFor(output);
		
		// buttons
		JButton saveOutputButton = new JButton("Save output");
		saveOutputButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save();   
			  }
			});
		JButton runMinionButton = new JButton("Run in Minion");
		runMinionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           runMinion();   
			  }
			});
		runMinionButton.setEnabled(false);
		
		JButton clearOutputButton = new JButton("Clear output");
		clearOutputButton.setEnabled(false);
		
		
		
		JPanel outputButtonPanel = new JPanel(new GridLayout(0,3));
		outputButtonPanel.add(saveOutputButton,0);
		outputButtonPanel.add(runMinionButton);
		outputButtonPanel.add(clearOutputButton,1);
		
		this.outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(this.outputScrollPane, BorderLayout.NORTH);
		outputPanel.add(outputButtonPanel, BorderLayout.SOUTH);
		outputPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.outputLabelString),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        outputPanel.getBorder()));
		
	}
	
	
	protected void generateMessagesOutput() {
		
		this.messageOutput = new JTextArea("", width,this.messagesHeight);
		this.messageScrollPane = new JScrollPane(messageOutput);
		this.messageOutput.setEditable(false);
		messageScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		messageScrollPane.setPreferredSize(new Dimension(width,this.messagesHeight));

		JLabel messageLabel = new JLabel(this.messagesLabelString);
		messageLabel.setLabelFor(messageOutput);
		
		this.messagePanel = new JPanel(new GridLayout());
		this.messagePanel.add(messageScrollPane);
		messagePanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(this.messagesLabelString),
								BorderFactory.createEmptyBorder(5,5,5,5)),
        messagePanel.getBorder()));
		
	}
	
	
	protected void runMinion() {
		
	}
	
	
	/**
	 * Save a problem, parameter or output file, depending on the 
	 * command passed. 
	 * 
	 * @param command
	 */
	protected void save() {
	
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
	
					writer.write(this.output.getText());
					writeOnMessageOutput("Saving output in: " + file.getName() + "." +"\n");
					
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
	 * Append the message on the message output
	 * 
	 * @param outputMessage
	 */
	protected void writeOnMessageOutput(String outputMessage) {
		this.messageOutput.append(outputMessage);
	}
	
	
}
