package translator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;

import translator.TranslationSettings;
import translator.Translator;

public class OutputPanel extends JPanel {

	static final long serialVersionUID = 8;
		
	
	TranslationSettings settings;
	Translator translator;
	
	   // actionCommands
	final String SAVE_OUTPUT = "save_output";
	final String CLEAR_OUTPUT = "clear output";
	final String EDIT_OUTPUT = "edit output";
	
//	 output tab indices
	public final int NORMALISE_TAB_NR = 0;
	public final int FLAT_TAB_NR = 1;
	public final int MINION_TAB_NR = 2;
	public final int SOLUTION_TAB_NR = 3;
	
	final String OUTPUT_FILENAME = "out.minion";
	String MINION_EXEC_PATH = "/home/andrea/minion/bin/minion";
	
	JFileChooser fileChooser;
	JTabbedPane outputTabbedPanel;
	
	// panels
	JPanel outputPanel;
	JPanel messagePanel;
	
	// components
	JTextArea normaliseOutput;
	JTextArea flatOutput;
	JTextArea minionOutput;
	JTextArea solutionOutput;
	JScrollPane minionOutputScrollPane;
	JTextArea messageOutput;
	JScrollPane messageScrollPane;
	
	
	
	
	// general design things
	int width;
	int outputHeight;
	int messagesHeight;
	String outputLabelString = "Translation Output";
	String messagesLabelString = "System Messages";	
	
	
	
	public OutputPanel(TranslationSettings settings,
						Translator translator,
			           int width,
			           int outputHeight,
			           int messagesHeight) {
		
		this.settings = settings;
		this.translator = translator;
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
	
	
	public void setOutputEditable() {
		this.minionOutput.setEditable(true);
	}
	
	public JTextArea getOutPut() {
		return this.minionOutput;
	}
	
	public JTextArea getMessageOutput() {
		return this.messageOutput;
	}
	
	
	protected void generateOutput() {
		
		// normalise text area
		this.normaliseOutput = new JTextArea("", width, outputHeight);
		this.normaliseOutput.setEditable(false);
		JScrollPane normaliseScrollPane = new JScrollPane(normaliseOutput);
		normaliseScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		normaliseScrollPane.setPreferredSize(new Dimension(width, outputHeight));
		
		
		// flat text area
		this.flatOutput = new JTextArea("",width,outputHeight);
		this.flatOutput.setEditable(false);
		JScrollPane flatScrollPane = new JScrollPane(flatOutput);
		flatScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		flatScrollPane.setPreferredSize(new Dimension(width, outputHeight));
		
		
		
		// minion output text area 
		this.minionOutput = new JTextArea("",width, outputHeight);
		this.minionOutputScrollPane = new JScrollPane(minionOutput);
		this.minionOutput.setEditable(true);
		minionOutputScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		minionOutputScrollPane.setPreferredSize(new Dimension(width, outputHeight));

		JLabel outputLabel = new JLabel(this.outputLabelString);
		outputLabel.setLabelFor(minionOutput);
		
		
		// solution text area
		this.solutionOutput = new JTextArea("", this.width, this.outputHeight);
		JScrollPane solutionScrollPane = new JScrollPane(this.solutionOutput);
		this.solutionOutput.setEditable(false);
		solutionScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		solutionScrollPane.setPreferredSize(new Dimension(width, outputHeight));

		JLabel solutionOutputLabel = new JLabel("Solutions in Essence'");
		solutionOutputLabel.setLabelFor(solutionOutput);
		
		
		// buttons
		JButton saveOutputButton = new JButton("Save Minion File");
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
		//runMinionButton.setEnabled(false);
		
		JButton clearOutputButton = new JButton("Clear output");
		clearOutputButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           clearOutput();   
			  }
			});
		
		
		
		JPanel outputButtonPanel = new JPanel(new GridLayout(0,3));
		outputButtonPanel.add(saveOutputButton,0);
		outputButtonPanel.add(runMinionButton);
		outputButtonPanel.add(clearOutputButton,1);
		outputButtonPanel.setBorder(
                BorderFactory.createEmptyBorder(5,5,5,5));
		
		
		
		
		
		
		this.outputPanel = new JPanel(new BorderLayout());
		this.outputTabbedPanel = new JTabbedPane();
		
		// don't change the order!!
		outputTabbedPanel.add("Normalised E'", normaliseScrollPane);
		outputTabbedPanel.add("Flat E'", flatScrollPane);
		outputTabbedPanel.add("Minion Input", this.minionOutputScrollPane);
		outputTabbedPanel.add("Essence' solution", solutionScrollPane);
		outputTabbedPanel.setPreferredSize(new Dimension(this.width, this.outputHeight));
		
		outputTabbedPanel.setEnabledAt(this.NORMALISE_TAB_NR, false);
		outputTabbedPanel.setEnabledAt(this.FLAT_TAB_NR, false);
		outputTabbedPanel.setEnabledAt(this.SOLUTION_TAB_NR, false);
		outputTabbedPanel.setSelectedIndex(this.MINION_TAB_NR);
		
		outputPanel.add(outputTabbedPanel, BorderLayout.NORTH);
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
	
	
	/**
	 * Clear the output text field.
	 *
	 */
	protected void clearOutput() {
		this.minionOutput.setText("");
		writeOnMessageOutput("Cleared output.\n");
	}
	
	
	/**
	 * 
	 *
	 */
	protected void runMinion() {
	
		try {
			//-------------- write the output into a file --------------- 
			writeOnMessageOutput("Creating minion file: "+this.OUTPUT_FILENAME+"\n");
			File file  = new File(this.OUTPUT_FILENAME);
		    if(file.createNewFile()) ;
			FileWriter writer = new FileWriter(file);
        
			if(!file.canRead())
				writeOnMessageOutput("Cannot read file: \n "+file.toString()+"\n");
			else if(!file.canWrite())
				writeOnMessageOutput("Cannot write file: \n "+file.toString()+"\n");

			writer.write(this.minionOutput.getText());
			writeOnMessageOutput("Saved minion output in: \n " + file.getName() + "." +"\n");
			
			writer.flush();
            writer.close();
            
            String minionExecPath = this.settings.getPathToMinion();            
            String[] commandArguments = new String[] { minionExecPath, this.OUTPUT_FILENAME };
            
            Process process = Runtime.getRuntime().exec(commandArguments);
            InputStream inputStream = process.getInputStream();
            
            process.waitFor();
            
            int exitValue = process.exitValue();
              
            BufferedReader input;
            
            if(exitValue == 0) {
                String line;
                
                input =new BufferedReader(new InputStreamReader(inputStream));
                String s = "";
                  while ((line = input.readLine()) != null) {
                	 s = s+line+"\n";
                  }
                  writeOnOutput(this.SOLUTION_TAB_NR, s+"\n\n\n\n"+this.translator.getEssenceSolution(s));
                  //writeOnOutput(this.SOLUTION_TAB_NR, this.translator.getEssenceSolution(s));
                  input.close();
            }
            else {
                String line;
                
                input =new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s = "";
                  while ((line = input.readLine()) != null) {
                	  s = s+line+"\n";
                  }
                  writeOnMessageOutput(s);
                  input.close();
            }
		
		} catch(Exception e) {
			writeOnMessageOutput("Could not run Minion:\n"+e.getMessage()+"\n"+
					"You can change the path to your Minion executable in 'Settings'.\n");
			return;
		}
	
		
	}
	
	
	/**
	 * Save a problem, parameter or output file, depending on the 
	 * command passed. 
	 * 
	 */
	protected void save() {
	
		int returnVal = this.fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				    if(file.createNewFile()) ;
					FileWriter writer = new FileWriter(file);
                
					if(!file.canRead())
						writeOnMessageOutput("Cannot read file:\n "+file.toString()+"\n");
					else if(!file.canWrite())
						writeOnMessageOutput("Cannot write file:\n "+file.toString()+"\n");
	
					writer.write(this.minionOutput.getText());
					writeOnMessageOutput("Saving output in:\n " + file.getName() + "." +"\n");
					
					writer.flush();
                    writer.close();
  			
			} catch(Exception e) {
				writeOnMessageOutput("Could not save file:\n"+e.getMessage()+"\n");
				return;
			}
                	
			
		} else {
			writeOnMessageOutput("Save command cancelled by user.\n");
		}	
	}
	
	
	public void disableOutput(int tabNumber) {
		this.outputTabbedPanel.setEnabledAt(tabNumber, false);
	}
	
	
	
	public void writeOnOutput(int tabNumber, String text) {
		
		if(tabNumber == this.NORMALISE_TAB_NR) {
			this.normaliseOutput.setText(text);
			this.outputTabbedPanel.setEnabledAt(this.NORMALISE_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.NORMALISE_TAB_NR);
		}
			
		else if(tabNumber == this.FLAT_TAB_NR) {
			this.flatOutput.setText(text);
			this.outputTabbedPanel.setEnabledAt(this.FLAT_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.FLAT_TAB_NR);
		}
		
		else if(tabNumber == this.MINION_TAB_NR) {
			this.minionOutput.setText(text);
			this.outputTabbedPanel.setEnabledAt(this.MINION_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.MINION_TAB_NR);
		}
		
		else if(tabNumber == this.SOLUTION_TAB_NR) {
			this.solutionOutput.setText(text);
			this.outputTabbedPanel.setEnabledAt(this.SOLUTION_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.SOLUTION_TAB_NR);
		}
	}
	
	/**
	 * Append the message on the message output
	 * 
	 * @param outputMessage
	 */
	protected void writeOnMessageOutput(String outputMessage) {
		this.messageOutput.setText(" "+outputMessage);
	}
	
	
	
	
	
}
