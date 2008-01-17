package translator.gui;

import java.awt.*;

//import java.awt.event.ActionEvent;
//import java.io.*;

import javax.swing.*;

import translator.Translator;

public class ModellingPanel extends JPanel  {
	
	static final long serialVersionUID = 0;
	
	Translator translator;
	
	// all the fields
	JTextArea problemInput;
	JTextArea parameterInput;
	JTextArea output;
	JTextArea messageOutput;
	
	
	// the panels
	TranslationPanel translationButtonPanel;
	InputPanel inputPanel;
	OutputPanel outputPanel;
	
	JFileChooser fileChooser;
	
	// some parameters for the sizes of textfields and buttons
	int frameWidth = 1200;
	int frameHeight = 700;
	int textareaWidth = (frameWidth/5)*2;
	int outputHeight = (frameHeight/5)*3;
	int messagesHeight = (frameHeight/4);
	int problemInputHeight = (frameHeight/7)*3;
	int parameterInputHeight = (frameHeight/7)*2;
	
	Color backgroundColor = Color.GREEN;
	
	String outputFieldLabel = "Translation Output";
	
	
	
	
	public ModellingPanel() {
	
		translator = new Translator();
		
		setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1100, 700));
	
		// output panel initialising
		this.outputPanel = new OutputPanel(this.textareaWidth,
											this.outputHeight,
											this.messagesHeight);
		this.output = this.outputPanel.getOutPut();
		this.messageOutput = this.outputPanel.getMessageOutput();
		
		// input
		this.inputPanel = new InputPanel(this.textareaWidth,
										this.problemInputHeight,
										this.parameterInputHeight,
				                         this.output,
				                         this.messageOutput);
		this.problemInput = inputPanel.getProblemInput();
	    this.parameterInput = inputPanel.getParameterInput();  
	
		// =================== all together =================================
		
		this.translationButtonPanel = new TranslationPanel(this.translator,
															this.problemInput,
															this.parameterInput,
															this.output,
															this.messageOutput);
		
		add(this.inputPanel, BorderLayout.WEST);
		add(this.translationButtonPanel, BorderLayout.CENTER);
		add(this.outputPanel, BorderLayout.EAST); 
		
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
