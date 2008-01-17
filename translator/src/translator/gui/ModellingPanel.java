package translator.gui;

import java.awt.*;
import javax.swing.*;
import translator.Translator;
import translator.TranslationSettings;

public class ModellingPanel extends JPanel  {
	
	static final long serialVersionUID = 0;
	
	Translator translator;
	TranslationSettings settings;
	
	
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
	int frameWidth;
	int frameHeight;
	int textareaWidth;
	int outputHeight;
	int messagesHeight;
	int problemInputHeight;
	int parameterInputHeight;
	
	Color backgroundColor = Color.GREEN;
	
	String outputFieldLabel = "Translation Output";
	
	
	
	
	public ModellingPanel(TranslationSettings settings,
			               int width,
			               int height) {

		this.settings = settings;
		this.translator = new Translator(this.settings);
		
		this.frameWidth = width;
		this.frameHeight = height;
		this.textareaWidth = (frameWidth/5)*2;
		this.outputHeight = (frameHeight/5)*3;
		this.messagesHeight = (frameHeight/4);
		this.problemInputHeight = (frameHeight/11)*5; // 9/4
		this.parameterInputHeight = (frameHeight/11)*3;  // 9/3
		
		
		setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(this.frameWidth, this.frameHeight));
	
		// output panel initialising
		this.outputPanel = new OutputPanel(settings, 
				                           translator,
				                            this.textareaWidth,
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
															this.outputPanel,
															this.settings,
															(frameWidth/5),
															this.frameHeight);
		
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
		this.messageOutput.setText(outputMessage);
	}
	
	protected void writeOnProblemInput(String s) {
		this.problemInput.setText("");
		this.problemInput.append(s);
	}
	
	
}
