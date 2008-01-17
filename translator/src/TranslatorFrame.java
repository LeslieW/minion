import java.io.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.event.ActionListener.*;


public class TranslatorFrame {

	int state;
	boolean parametersAreSet;
	boolean problemIsSet;
	boolean problemChoosed;
	boolean parameterChoosed;
	boolean translationFinished;
	boolean parameterSelected;
	boolean problemSelected;

	Color backgroundColor;
	
	String parameterFileName;	
	String problemFileName;
	String outputFileName;
	String minionOutput;
	
	File problemFile;
	File parameterFile;
	
	JFrame window;
	JTextArea output;
	JScrollPane scrollPane;
	//JTextPane output;
	JPanel contentPanel;
	
	// buttongroups
	//ButtonGroup problemFileSelection;
	//ButtonGroup parameterButtonGroup;
	
	// Parameter File Selection Stuff
	//JRadioButton emptyParameterButton;
	//JRadioButton specifiedParameterButton;
	JLabel parameterLabel;
	JTextArea parameterFileNameLabel;
	JButton chooseParameterFileButton;
	JButton selectParameterButton;
	JFileChooser parameterFileChooser;
	
	// Problem File Selection Stuff
	JButton chooseProblemFileButton;
	JButton selectProblemButton;
	JLabel problemsLabel;
	JTextArea problemFileNameLabel;
	
	// Buttons
	JButton exitButton;
	JButton translateButton;
	
	PrintStream aPrintStream  = 
	      new PrintStream(
	        new MyOutputStream(
	          new ByteArrayOutputStream()));
	
	
	public TranslatorFrame() {

		initialiseState();
		
		this.outputFileName = "out.minion";
		this.backgroundColor = Color.orange;
		
		this.window = new JFrame("Essence' Translator");
		
		//this.window.add(this.output);
		contentPanel = new JPanel(new BorderLayout());
		//contentPane.setBorder(someBorder);
		
		contentPanel.setLayout(null);
		//contentPanel.add(this.output, BorderLayout.CENTER);
		//contentPane.add(anotherComponent, BorderLayout.PAGE_END);
		this.window.setContentPane(contentPanel);
		
		
		// Parameter selection stuff
		//this.parameterButtonGroup = new ButtonGroup();
	
		//emptyParameterButton = new JRadioButton("Empty parameter file");
		//emptyParameterButton.setBounds(30, 200, 200, 20);
		//specifiedParameterButton = new JRadioButton("Parameter file from file");
		//specifiedParameterButton.setBounds(30, 220, 200, 20);
		
		//parameterButtonGroup.add(emptyParameterButton);
		//parameterButtonGroup.add(specifiedParameterButton);
		
		parameterLabel = new JLabel("Essence' Parameter Selection");
		parameterLabel.setBounds(30, 170, 220, 20);
		
		selectParameterButton = new JButton("Select Parameter ");
		selectParameterButton.setBounds(220, 240, 200, 30);
		selectParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           selectParameter();   
			  }
			});
		
		chooseParameterFileButton = new JButton("Choose Parameter File");
		chooseParameterFileButton.setBounds(500, 200, 170, 30);
		chooseParameterFileButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           startParameterFileChooser();      
			  }
			});
		
		parameterFileNameLabel = new JTextArea();
		parameterFileNameLabel.setBounds(30, 200, 450, 30);
		parameterFileNameLabel.setEditable(false);
		
		// Problem selection stuff
		problemsLabel = new JLabel("Essence' Problem File Selection");
		problemsLabel.setBounds(30, 30, 220, 20);
		
		selectProblemButton = new JButton("Select Problem");
		selectProblemButton.setBounds(220, 100, 200, 30);
		selectProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           selectProblem();   
			  }
			});
		
		chooseProblemFileButton = new JButton("Choose Problem File");
		chooseProblemFileButton.setBounds(500, 60, 170, 30);
		chooseProblemFileButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           startProblemFileChooser();      
			  }
			});
		problemFileNameLabel = new JTextArea();
		problemFileNameLabel.setBounds(30, 60, 450, 30);
		problemFileNameLabel.setEditable(false);
		
		
		translateButton = new JButton("Translate to Minion");
		translateButton.setBounds(220,340,200,30);
		translateButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
				  try{ 
		           translate();
				  } catch(Exception exception) {
					    System.out.println(exception);
					    System.out.println("Bailing out.");
					}
			  }
			});
		
		
		// other buttons
		exitButton = new JButton("Exit");
		exitButton.setBounds(275, 550, 80, 30);
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		                 System.exit(0);
			  }
			});
		
		
		// output Text area
		this.output = new JTextArea();
		output.setBounds(new Rectangle (40, 380, 600, 150));
		//output.setBackground(Color.orange);
		output.setEditable(false);
		
		this.scrollPane = new JScrollPane(this.output);
		// wenn man das Setzen der Bounds auskommentiert, sieht man die scrollbargarnicht
		scrollPane.setBounds(new Rectangle (40, 380, 600, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().add(output);
        //scrollPane.setAutoscrolls(true);
	    //JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
	    //scrollBar.setVisible(true);
		//scrollPane.setVisible(true);
	    
	    //output.setLineWrap(true);
	    //output.setWrapStyleWord(true);
		
		//contentPanel.add(parameterButtonGroup);
		contentPanel.add(exitButton);
		//contentPanel.add(this.output, BorderLayout.CENTER);
		//contentPanel.add(output);
		contentPanel.add(scrollPane);
		contentPanel.add(problemsLabel);
		contentPanel.add(problemFileNameLabel);
		contentPanel.add(chooseProblemFileButton);
		contentPanel.add(selectProblemButton);
		
		//contentPanel.add(emptyParameterButton);
		//contentPanel.add(specifiedParameterButton);
		contentPanel.add(parameterLabel);
		contentPanel.add(selectParameterButton);
		contentPanel.add(parameterFileNameLabel);
		contentPanel.add(chooseParameterFileButton);
		contentPanel.add(translateButton);
		
		contentPanel.setBackground(this.backgroundColor);
		
		this.window.setVisible(true);
		this.window.setBackground(Color.orange);
		
	    window.addWindowListener(new WindowAdapter() {
	  	  public void windowClosing (WindowEvent e) {
	                 System.exit(0);
	  	  }
	  	});

	    window.setSize(700,630);

		//scrollPane.setBorder(new Border(1));
		
	    adaptToState();	
		
		System.setOut(aPrintStream);
		//System.out.println("hello World!");
		//for(int i=0; i<50; i++)
		//	System.out.println("oida... "+i);
		
	}
	
	void initialiseState() {
	
	this.parametersAreSet = false;
	this.problemIsSet = false;
	this.problemChoosed = false;
	this.parameterChoosed = false;
	this.translationFinished = false;
	this.problemSelected = false;
	this.parameterSelected = false;
	}
	
	
	void startParameterFileChooser() {
		
		parameterFileChooser = new JFileChooser();
		parameterFileChooser.setDialogTitle("Choose Parameter File");
	    //ExampleFileFilter filter = new ExampleFileFilter();
	    //filter.addExtension("jpg");
	    //filter.addExtension("gif");
	    //filter.setDescription("JPG & GIF Images");
	    //chooser.setFileFilter(filter);
	    int returnVal = parameterFileChooser.showOpenDialog(contentPanel);
	    // TODO: what if person cancels the choosen parameter file?
	    File file = parameterFileChooser.getSelectedFile();
	    this.parameterFileName = file.getPath();
	    
	    
	    // TODO: write this into a text-area instead of printing it on out
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("Parameter file choosen: " +
	            this.parameterFileName);
	    }
	    else {
            output.append("Choosing parameter file cancelled by user.\n");
        }
		this.parameterChoosed = true;
	    //this.parameterFileName = parameterFileChooser.getSelectedFile().getName();
	    adaptToState();
	}
	
	void startProblemFileChooser() {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose Problem File");
	    //ExampleFileFilter filter = new ExampleFileFilter();
	    //filter.addExtension("jpg");
	    //filter.addExtension("gif");
	    //filter.setDescription("JPG & GIF Images");
	    //chooser.setFileFilter(filter);
	    int returnVal = fileChooser.showOpenDialog(contentPanel);
	    // TODO: what if person cancels the choosen parameter file?
	    File file = fileChooser.getSelectedFile();
	    this.problemFileName = file.getPath();
	    
	    // TODO: write this into a text-area instead of printing it on out
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("Problem file choosen: " +
	            this.problemFileName);  		
	    }
	    else {
            output.append("Choosing problem file cancelled by user.\n");
        }
		this.problemChoosed = true;
		//this.problemFileName = fileChooser.getSelectedFile().getName();
	    adaptToState();
	}
	
	
	void selectProblem() {
		this.problemIsSet = true;
		this.problemSelected = true;
		this.chooseProblemFileButton.setEnabled(false);
		this.problemFileNameLabel.setBackground(this.backgroundColor);
		adaptToState();
	}
	
	void selectParameter() {
		this.parametersAreSet = true;
		this.parameterSelected = true;
		this.chooseParameterFileButton.setEnabled(false);
		this.parameterFileNameLabel.setBackground(this.backgroundColor);
		adaptToState();
	}
	
	
	void translate() throws Exception, IOException {
		
	    EssencePrimeMinionTranslator translator = new EssencePrimeMinionTranslator(this.problemFileName, this.parameterFileName) ;
	    this.minionOutput = translator.translate(null) ;
	    System.out.println("Translation Successful") ;
	    
	    // write output into file
	    //writeOutputIntoFile(this.outputFileName, minionOutput);  
	    //System.out.println("Output written into "+outputFileName) ;		
		
	    this.translationFinished = true;
	    saveTranslatedFile(); 
	    adaptToState();
	    this.translationFinished = false;
	    this.problemSelected = false;
		this.parameterSelected = false;
	}
	
	void saveTranslatedFile() throws IOException  {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Minion File");
	    //ExampleFileFilter filter = new ExampleFileFilter();
	    //filter.addExtension("jpg");
	    //filter.addExtension("gif");
	    //filter.setDescription("JPG & GIF Images");
	    //chooser.setFileFilter(filter);
		
        int returnVal = fileChooser.showSaveDialog(this.window);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            //This is where a real application would save the file.
            output.append("Saving Minion model in  " + file.getName() + ".\n");
        } else {
            output.append("Save command cancelled by user.\n");
        }
     
	    File file = fileChooser.getSelectedFile();
	    
	    FileWriter outputFile = new FileWriter(file);
	    outputFile.write(this.minionOutput);
	    outputFile.flush();
	    outputFile.close();	 
		this.minionOutput = new String();
		System.out.println("Saved Minion model in "+file.getAbsolutePath());
	}
	
	void adaptToState() {
		
		if(this.parametersAreSet && this.problemIsSet) {
			this.translateButton.setEnabled(true);
		}
		else 
			this.translateButton.setEnabled(false);	
		
		
		if(this.parameterSelected) {
			this.chooseParameterFileButton.setEnabled(false);
			this.selectParameterButton.setEnabled(false);
		}
		
		if(this.problemSelected) {
			this.chooseProblemFileButton.setEnabled(false);
			this.selectProblemButton.setEnabled(false);
		}
		
		if(this.problemChoosed) {
			//if(!this.problemIsSet)
				this.selectProblemButton.setEnabled(true);
			this.problemFileNameLabel.setText(this.problemFileName);
		}
		else {
			this.selectProblemButton.setEnabled(false);
		}
		
		if(this.parameterChoosed) {
			//if(!this.parametersAreSet)
				this.selectParameterButton.setEnabled(true);
			this.parameterFileNameLabel.setText(this.parameterFileName);
		}
		else { 
			this.selectParameterButton.setEnabled(false);
		}
	
		if(this.translationFinished) {
			this.chooseParameterFileButton.setEnabled(true);
			this.chooseProblemFileButton.setEnabled(true);
			this.parameterFileNameLabel.setBackground(Color.white);
			this.problemFileNameLabel.setBackground(Color.white);
			
		}
		
		
	}
	

	   
	class MyOutputStream extends FilterOutputStream{

		public MyOutputStream(OutputStream arg0) {
			super(arg0);
			}
		
		public void write(byte b[]) throws IOException {
	         String aString = new String(b);
	         //String alreadyInTextField = output.getText();
	         
	         //output.setText(alreadyInTextField.concat(aString));
	         output.append(aString);
	         }

	      public void write(byte b[], int off, int len) throws IOException {
	         String aString = new String(b , off , len);    
	        // String alreadyInTextField = output.getText();
	     
	         //output.setText(alreadyInTextField.concat(aString));
	         output.append(aString);
	         }
		
	}
	
}
