/*
 * TailorGUI.java
 *
 * Created on 10 March 2008, 15:37
 */

package translator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.filechooser.*;

import translator.TranslationSettings;
import translator.Translator;
import translator.normaliser.NormaliserSpecification;
import translator.solver.Minion;
import translator.solver.Gecode;
import translator.solver.TargetSolver;
import javax.swing.*;
import java.awt.Color;
/**
 *
 * @author  andrea
 */
public class TailorGUI extends javax.swing.JFrame {
    
	static final long serialVersionUID = 11;
	   // actionCommands
	final String SAVE_PROBLEM = "save_problem";
	final String SAVE_PARAMETER = "save_parameter";
	final String SAVE_OUTPUT = "save_output";
	final String LOAD_PROBLEM = "load_problem";
	final String LOAD_PARAMETER = "load_parameter";
	final String CLEAR_PROBLEM = "clear problem";
	final String CLEAR_PARAMETER = "clear parameter";
	//final String SAVE_OUTPUT = "save_output";
	final String CLEAR_OUTPUT = "clear output";
	final String EDIT_OUTPUT = "edit output";
	
	// translation commands
	final String PARSE = "parse"; 
	final String INSERT_PARAMETERS = "insert parameters";
	final String EVALUATE = "evaluate";
	final String ORDER = "order";
	final String NORMALISE = "full";
	final String FLATTEN = "flatten";
	final String MINION = "to_minion";
	final String GECODE = "to gecode";
	final String DEBUG = "debug";
	
//	 output tab indices
	public final int NORMALISE_TAB_NR = 0;
	public final int FLAT_TAB_NR = 1;
	public final int SOLVER_INPUT_TAB_NR = 2;
	public final int SOLUTION_TAB_NR = 3;
	
	final String ESSENCE_PRIME_HEADER ="language ESSENCE' 1.b.a";
	final String SYSTEM_START_MSG = "Welcome to "+this.TAILOR_VERSION+"\nbug reports: andrea@cs.st-and.ac.uk\n";
	final String TAILOR_VERSION = "TAILOR v0.2";
	
	
	final char CLASSIC_SKIN = 'c';
	final char GIRLIE_SKIN = 'g';
	final char ARMY_SKIN = 'a';
	final char WATERFALL_SKIN = 's';
	final char NIGHTOWL_SKIN = 'n';
	final char COLORBLIND_SKIN = 'b';
	final char PIRATE_SKIN = 'p';
	
	TranslationSettings settings;
	Translator translator;
	char skinType;
	TargetSolver solver;
	
	// translation
	JButton parseButton;
	JComboBox normaliseComboBox;
	JButton normaliseButton;
	JButton flattenButton;
	JButton minionButton;
	JButton gecodeButton;
	
	JPanel normalisePanel;
	JPanel debugPanel;
	
	// Buttons to turn off/on the checkbox for DEBUG mode
	JButton debugModeButton;
	JCheckBox debugMode;
	
	
	
	JFileChooser fileChooser;
	JTextArea normaliseOutput;
	JTextArea flatOutput;
	JTextArea minionOutput;
	JTextArea solutionOutput;
	JTabbedPane outputTabbedPanel;
	
    /** Creates new form TailorGUI */
    public TailorGUI() {
    	this.settings = new TranslationSettings();
    	this.translator = new Translator(settings);
    	this.skinType = this.CLASSIC_SKIN;
    	setColors();
        initComponents();
        updateColors();
        solver = settings.getTargetSolver();
    }
    
    public TailorGUI(TranslationSettings settings) {
    	this.settings = settings;
    	this.translator = new Translator(settings);
    	this.skinType = this.CLASSIC_SKIN;
    	setColors();
        initComponents();
        updateColors();
        solver = settings.getTargetSolver();
    }
    
    private String getSkinName(char skinType) {
    	
    	if(skinType == this.GIRLIE_SKIN)
    		return "Girlie Skin";
    	
    	else if(skinType == this.NIGHTOWL_SKIN) 
    		return "NightOwl Skin";
    	
    	else if(skinType == this.COLORBLIND_SKIN)
    		return "Colorblind Skin";
    	
    	else if(skinType == this.WATERFALL_SKIN)
    		return "Waterfall Skin";
    	
    	else if(skinType == this.PIRATE_SKIN)
    		return "Cheeky Pirate Skin";
    	
    	else if(skinType == this.ARMY_SKIN)
    		return "Boring Army Skin";
    	
    	// TODO:
    	// sunrise skin
    	// pirate skin
    	
    	return "Classic Skin";
    }
    
    private void setColors() {
    	
    	// Color lightGreenish = new java.awt.Color(211,245,177);
		Color lightBlueish = new java.awt.Color(177,210,240);//(225,242,253);//(220,220,245);//(190,230,240); //220,220,245
    	//Color darkRed = new java.awt.Color(142,5,5);
    	//Color reallyDarkRed = new java.awt.Color(82,10,5);
		//Color cheekyOrange = new java.awt.Color(252,105,20);
		//Color chalkYellow = new java.awt.Color(250,233,193);
		//Color sunShineYellow = new java.awt.Color(241,224,9);
		//Color cheekyBarbiePink = new java.awt.Color(241,9,142);
		Color barbiePink = new java.awt.Color(215,24,141);
		Color babyBarbiePink = new java.awt.Color(254,222,241);
		Color stepmotherPurple = new Color(73,1,45);
		Color thickPurple = new Color(147,23,99);
		//Color darkWateryBlue = new Color(54,129,185);
		Color wateryBlue = new Color(74,149,205);
		Color caveWaterBlue = new Color(3,60,99);
		Color peasantArmyGreen = new Color(89,130,74);
		Color paleArmyGreenBrown = new Color(237,240,208);
		Color darkMountainGreen = new Color(13,40,10);
		Color sandyBrown = new Color(198, 190, 152);
		
    	if(this.skinType == this.CLASSIC_SKIN) {
    		
    		this.bgColor = lightBlueish; //lightGreenish;
    		this.inputBgColor = lightBlueish; //lightGreenish;
    		this.outputBgColor =lightBlueish; //lightGreenish;
    		this.textAreaColor = new java.awt.Color(240,240,240);
    		this.textAreaFontColor = java.awt.Color.black;
    		this.inputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.outputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.buttonColor = new JButton().getBackground();//UIManager.getColor(new JButton());
    		
    	}
    	
    	else if(this.skinType == this.NIGHTOWL_SKIN) {
    		
    		this.textAreaColor = java.awt.Color.black;
    		this.textAreaFontColor = java.awt.Color.white; // or green?
    		this.bgColor = lightBlueish; //lightGreenish;
    		this.inputBgColor = new JPanel().getBackground(); //lightBlueish; //lightGreenish;
    		this.outputBgColor = new JPanel().getBackground();//lightBlueish; //lightGreenish;
    		//this.textAreaColor = new java.awt.Color(240,240,240);
    		//this.textAreaFontColor = java.awt.Color.black;
    		this.inputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.outputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.buttonColor = new JButton().getBackground();
    		//this.buttonColor = lightBlueish;
    		
    	}
    	
    	else if(this.skinType == this.GIRLIE_SKIN) {
    		this.bgColor = barbiePink;
    		this.inputBgColor = barbiePink;
    		this.outputBgColor = barbiePink;
    		this.textAreaColor = babyBarbiePink;
    		this.textAreaFontColor = stepmotherPurple;
    		this.buttonColor = thickPurple;
    		this.inputFont = new java.awt.Font("Dialog", 0, 12);
    		this.outputFont = new java.awt.Font("Dialog", 0, 12);
    
    	}
    	
    	else if(this.skinType == this.COLORBLIND_SKIN) {
    		this.bgColor = java.awt.Color.pink;
    		this.inputBgColor = new java.awt.Color(211,245,177);
    		this.outputBgColor = new java.awt.Color(211,177,245);
    		this.textAreaColor = java.awt.Color.yellow;
    		this.textAreaFontColor = java.awt.Color.black;
    		this.inputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.outputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.buttonColor = java.awt.Color.LIGHT_GRAY;
    	}
    	
    	else if(this.skinType == this.WATERFALL_SKIN) {
    		this.bgColor = wateryBlue;
    		this.inputBgColor = wateryBlue;
    		this.outputBgColor = wateryBlue;
    		this.textAreaColor = lightBlueish;
    		this.textAreaFontColor = caveWaterBlue;
    		this.inputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.outputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.buttonColor = new JButton().getBackground();
    		//this.buttonColor = lightBlueish;
    	}
    	
    	else if(this.skinType == this.ARMY_SKIN) {
    		this.bgColor = peasantArmyGreen;
    		this.inputBgColor = peasantArmyGreen;
    		this.outputBgColor = peasantArmyGreen;
    		this.textAreaColor = paleArmyGreenBrown;
    		this.textAreaFontColor = darkMountainGreen;
    		this.inputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.outputFont = new java.awt.Font("DialogInput", 0, 12);
    		this.buttonColor = sandyBrown;
    	}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    	
    	String path = "tailorLogo3.png";
    	java.net.URL imgURL = getClass().getResource(path);
    	this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());
    	
        java.awt.GridBagConstraints gridBagConstraints;

        this.setBackground(this.bgColor);
        this.setTitle(this.TAILOR_VERSION+" - "+getSkinName(this.skinType));
        
        inputPanel = new javax.swing.JPanel();
        inputSplitPane = new javax.swing.JSplitPane();
        problemInputPanel = new javax.swing.JPanel();
        problemInputScrollPane = new javax.swing.JScrollPane();
        problemInput = new javax.swing.JTextPane();
        problemButtonPanel = new javax.swing.JPanel();
        saveProblemButton = new javax.swing.JButton();
        loadProblemButton = new javax.swing.JButton();
        clearProblemButton = new javax.swing.JButton();
        colLineLabelProblem = new javax.swing.JLabel();
        parameterInputPanel = new javax.swing.JPanel();
        parameterInputScrollPane = new javax.swing.JScrollPane();
        parameterInput = new javax.swing.JTextPane();
        parameterButtonPanel = new javax.swing.JPanel();
        saveParameterButton = new javax.swing.JButton();
        loadParameterButton = new javax.swing.JButton();
        clearParameterButton = new javax.swing.JButton();
        colLineLabelParameter = new javax.swing.JLabel();
        middlePanel = new javax.swing.JPanel();
        outputPanel = new javax.swing.JPanel();
        outputSplitPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        this.solverButtonPanel = new JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        //outputTextPane = new javax.swing.JTextPane();
        outputButtonPanel = new javax.swing.JPanel();
        saveOutputButton = new javax.swing.JButton();
        clearOutputButton = new javax.swing.JButton();
        runMinionButton = new javax.swing.JButton();
        messagesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageOutput = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        settingsMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusCycleRoot(false);
        //setName("mainFrame"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());
        getContentPane().setBackground(this.bgColor);

        //inputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modelling"));
        //inputPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        inputPanel.setMinimumSize(new java.awt.Dimension(200, 400));
        inputPanel.setName("modellingPanel"); // NOI18N
        inputPanel.setPreferredSize(new java.awt.Dimension(480, 670));
        inputPanel.setLayout(new java.awt.GridBagLayout());
        this.inputPanel.setBackground(this.inputBgColor);

        inputSplitPane.setDividerSize(15);
        inputSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        inputSplitPane.setResizeWeight(0.7);
        inputSplitPane.setLastDividerLocation(this.inputSplitPane.getSize().height/2);
        inputSplitPane.setMinimumSize(new java.awt.Dimension(180, 380));
        inputSplitPane.setName("inputSplitPane"); // NOI18N
        inputSplitPane.setOneTouchExpandable(true);
        inputSplitPane.setPreferredSize(new java.awt.Dimension(390, 590));

        problemInputPanel.setLayout(new java.awt.GridBagLayout());

        problemInputScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Essence' Problem Model"));
        problemInputScrollPane.setMinimumSize(new java.awt.Dimension(190, 200));
        problemInputScrollPane.setPreferredSize(new java.awt.Dimension(390, 450));

        problemInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        problemInput.setFont(this.inputFont);
        problemInput.setBackground(this.textAreaColor);
        problemInput.setForeground(this.textAreaFontColor);
        problemInput.setMinimumSize(new java.awt.Dimension(190, 200));
        problemInput.setPreferredSize(new java.awt.Dimension(390, 400));
        problemInput.setText(this.ESSENCE_PRIME_HEADER+"\n");
        problemInput.setCaretPosition((this.ESSENCE_PRIME_HEADER+"\n").length());
        this.problemInput.addCaretListener( new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				int column = 0;
				int caretPosition = problemInput.getCaretPosition();
				Element root = problemInput.getDocument().getDefaultRootElement();
				int l = root.getElementIndex( caretPosition );
				int lineStart = root.getElement( l ).getStartOffset();
				column =  caretPosition - lineStart + 1;
				
				
				int line =  root.getElementIndex( caretPosition ) + 1;
			
				updateLineColPositions(line,column);
			}
		});
        problemInputScrollPane.setViewportView(problemInput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        problemInputPanel.add(problemInputScrollPane, gridBagConstraints);

        
        
        //problemButtonPanel.setPreferredSize(new java.awt.Dimension(200, 30));
        problemButtonPanel.setLayout(new java.awt.GridLayout(1, 0, 30, 0));
        Dimension buttonSize = (new JButton()).getPreferredSize(); //new Dimension(20,30);
        
        saveProblemButton.setText("save");
        saveProblemButton.setActionCommand(SAVE_PROBLEM);
        saveProblemButton.setBackground(this.buttonColor);
        //saveProblemButton.setPreferredSize(buttonSize);
		saveProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});
        problemButtonPanel.add(saveProblemButton);
        

        loadProblemButton.setText("load");
        loadProblemButton.setActionCommand(LOAD_PROBLEM);
        loadProblemButton.setBackground(this.buttonColor);
        //loadProblemButton.setPreferredSize(buttonSize);
        loadProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           load(e.getActionCommand());   
			  }
			});	
        problemButtonPanel.add(loadProblemButton);

        clearProblemButton.setText("clear");
        clearProblemButton.setActionCommand(this.CLEAR_PROBLEM);
        clearProblemButton.setBackground(this.buttonColor);
        //clearProblemButton.setPreferredSize(buttonSize);
        clearProblemButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           clearInputField(e.getActionCommand());   
			  }
			});
        problemButtonPanel.add(clearProblemButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        problemInputPanel.add(problemButtonPanel, gridBagConstraints);

        colLineLabelProblem.setText("Line:0   Column:0");
        colLineLabelProblem.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        problemInputPanel.add(colLineLabelProblem, gridBagConstraints);

        inputSplitPane.setLeftComponent(problemInputPanel);

        parameterInputPanel.setLayout(new java.awt.GridBagLayout());

        parameterInputScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Essence' Parameter Specification"));
        parameterInputScrollPane.setAutoscrolls(true);
        parameterInputScrollPane.setMinimumSize(new java.awt.Dimension(190, 150));
        parameterInputScrollPane.setPreferredSize(new java.awt.Dimension(390, 200));

        parameterInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        parameterInput.setFont(this.inputFont);
        parameterInput.setBackground(this.textAreaColor);
        parameterInput.setForeground(this.textAreaFontColor);
        parameterInput.setMinimumSize(new java.awt.Dimension(190, 150));
        parameterInput.setPreferredSize(new java.awt.Dimension(390, 200));
        parameterInput.setText(this.ESSENCE_PRIME_HEADER+"\n");
        problemInput.setCaretPosition((this.ESSENCE_PRIME_HEADER+"\n").length());
        parameterInput.addCaretListener( new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				int column = 0;
				int caretPosition = parameterInput.getCaretPosition();
				Element root = parameterInput.getDocument().getDefaultRootElement();
				int l = root.getElementIndex( caretPosition );
				int lineStart = root.getElement( l ).getStartOffset();
				column =  caretPosition - lineStart + 1;
				
				
				int line =  root.getElementIndex( caretPosition ) + 1;
			
				updateParameterLineColPositions(line,column);
			}
		});
        parameterInputScrollPane.setViewportView(parameterInput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        parameterInputPanel.add(parameterInputScrollPane, gridBagConstraints);

        //parameterButtonPanel.setLayout(new java.awt.GridLayout(1, 0));
        parameterButtonPanel.setLayout(new java.awt.GridLayout(1, 0, 30, 0));
        
        saveParameterButton.setText("save");
        saveParameterButton.setActionCommand(SAVE_PARAMETER);
        saveParameterButton.setBackground(this.buttonColor);
        //saveParameterButton.setPreferredSize(buttonSize);
		saveParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});	
        parameterButtonPanel.add(saveParameterButton);

        loadParameterButton.setText("load");
        loadParameterButton.setActionCommand(LOAD_PARAMETER);
        loadParameterButton.setBackground(this.buttonColor);
        //loadParameterButton.setPreferredSize(buttonSize);
		loadParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           load(e.getActionCommand());   
			  }
			});	
        parameterButtonPanel.add(loadParameterButton);

        clearParameterButton.setText("clear");
        clearParameterButton.setActionCommand(this.CLEAR_PARAMETER);
        clearParameterButton.setBackground(this.buttonColor);
        //clearParameterButton.setPreferredSize(buttonSize);
		clearParameterButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           clearInputField(e.getActionCommand());   
			  }
			});
        parameterButtonPanel.add(clearParameterButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        parameterInputPanel.add(parameterButtonPanel, gridBagConstraints);

        colLineLabelParameter.setText("Line:0   Column:0");
        colLineLabelParameter.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        parameterInputPanel.add(colLineLabelParameter, gridBagConstraints);

        inputSplitPane.setRightComponent(parameterInputPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        inputPanel.add(inputSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        getContentPane().add(inputPanel, gridBagConstraints);

        middlePanel.setMaximumSize(new java.awt.Dimension(180, 32767));
        middlePanel.setMinimumSize(new java.awt.Dimension(100, 400));
        middlePanel.setPreferredSize(new java.awt.Dimension(150, 600));
        middlePanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(middlePanel, gridBagConstraints);
        
        
        this.solverButtonPanel.setLayout(new java.awt.GridLayout(0, 1, 80, 0));
        
        Dimension solverButtonDimension = new Dimension(150,40);
        this.minionButton = new JButton("> to Minion >");
		minionButton.setPreferredSize(solverButtonDimension);
		minionButton.setActionCommand(MINION);
		//minionButton.setBackground(this.buttonColor);
		minionButton.setBackground(new JButton().getBackground());
		minionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		solverButtonPanel.add(minionButton);
		
		this.gecodeButton = new JButton("> to Gecode >");
		this.gecodeButton.setPreferredSize(solverButtonDimension);
		gecodeButton.setActionCommand(GECODE);
		gecodeButton.setBackground(new JButton().getBackground());
		gecodeButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           translate(e.getActionCommand());   
			  }
			});
		//this.gecodeButton.setEnabled(false);
		//solverButtonPanel.add(gecodeButton);
		
		middlePanel.add(solverButtonPanel);
		middlePanel.setBackground(this.bgColor);
		//middlePanel.setOpaque(false);
		//minionButton.setEnabled(false);
        
        
        //outputPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        //outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Solving")));
        outputPanel.setMinimumSize(new java.awt.Dimension(200, 400));
        outputPanel.setPreferredSize(new java.awt.Dimension(420, 600));
        outputPanel.setLayout(new java.awt.GridBagLayout());
        outputPanel.setBackground(this.outputBgColor);
        
        outputSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        outputSplitPane.setResizeWeight(0.7);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        outputScrollPane.setMinimumSize(new java.awt.Dimension(190, 200));
        outputScrollPane.setPreferredSize(new java.awt.Dimension(400, 450));

        //outputTextPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        //outputTextPane.setEditable(false);
        //outputTextPane.setMinimumSize(new java.awt.Dimension(190, 200));
        //outputTextPane.setPreferredSize(new java.awt.Dimension(390, 450));
        initOutput(390,450);
        //outputScrollPane.setViewportView(outputTextPane);
        outputScrollPane.setViewportView(this.outputTabbedPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel1.add(outputScrollPane, gridBagConstraints);

        //outputButtonPanel.setLayout(new java.awt.GridLayout(1, 0));
        outputButtonPanel.setLayout(new java.awt.GridLayout(1, 0, 30, 0));

        saveOutputButton.setText("save");
        saveOutputButton.setActionCommand(this.SAVE_OUTPUT);
        saveOutputButton.setBackground(this.buttonColor);
        //saveOutputButton.setPreferredSize(buttonSize);
        saveOutputButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           save(e.getActionCommand());   
			  }
			});
        outputButtonPanel.add(saveOutputButton);

        clearOutputButton.setText("clear");
        clearOutputButton.setBackground(this.buttonColor);
        //clearOutputButton.setPreferredSize(buttonSize);
        outputButtonPanel.add(clearOutputButton);

        runMinionButton.setText("run Minion");
        runMinionButton.setBackground(this.buttonColor);
        //runMinionButton.setPreferredSize(buttonSize);
        runMinionButton.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           runMinion();   
			  }
			});
        runMinionButton.setEnabled(false);
        outputButtonPanel.add(runMinionButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        jPanel1.add(outputButtonPanel, gridBagConstraints);

        outputSplitPane.setLeftComponent(jPanel1);

        messagesPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "System Messages"));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(190, 50));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(390, 150));

        messageOutput.setColumns(20);
        messageOutput.setRows(5);
        messageOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        messageOutput.setText(this.SYSTEM_START_MSG);
        messageOutput.setBackground(this.textAreaColor);
        messageOutput.setForeground(this.textAreaFontColor);
        jScrollPane1.setViewportView(messageOutput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        messagesPanel.add(jScrollPane1, gridBagConstraints);

        outputSplitPane.setRightComponent(messagesPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        outputPanel.add(outputSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        getContentPane().add(outputPanel, gridBagConstraints);

        initMenuBar();
        
        pack();
        
        
        
    }// </editor-fold>//GEN-END:initComponents

    
    private void initMenuBar() {
    	settingsMenu.setText("Settings");
        menuBar.add(settingsMenu);
     
        // path to Minion
        JMenuItem setPathToMinion = new JMenuItem("Set Path to Minion ...");
        setPathToMinion.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changePath();   
			  }
			});
        settingsMenu.add(setPathToMinion);
        //settingsMenu.addSeparator();
        
        
        
        // translation settings
        JMenu translationSettings = new JMenu("Translation");
        JMenuItem cseDetection = new JCheckBoxMenuItem("Basic Common Subexpression Elimination");
        JMenuItem ecseDetection = new JCheckBoxMenuItem("Derived Common Subexpressions' Elimination");
        JMenuItem directVarReusage = new JCheckBoxMenuItem("Directly reuse variables");
        cseDetection.setSelected(this.settings.useCommonSubExpressions());
        ecseDetection.setSelected(this.settings.useEqualCommonSubExpressions());
        directVarReusage.setSelected(this.settings.applyDirectVariableReusage());
        cseDetection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		          JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
		          settings.setUseCommonSubExpressions(item.isSelected());
			  }
			});
        ecseDetection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		          JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
		          settings.setUseExplicitCommonSubExpressions(item.isSelected());
			  }
			});
        directVarReusage.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		          JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
		          settings.setApplyDirectVariableReusage(item.isSelected());
			  }
			});
        
        translationSettings.add(cseDetection);
        translationSettings.add(ecseDetection);
        translationSettings.add(directVarReusage);
        //settingsMenu.add(translationSettings);
        menuBar.add(translationSettings);
        
        
        // solving settings
        JMenu solvingSettings = new JMenu("Solving");
        JMenuItem allSolutions = new JCheckBoxMenuItem("Find all solutions");
        allSolutions.setSelected(settings.getNoOfSolutions() == settings.FIND_ALL_SOLUTIONS);
        allSolutions.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		          JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
		          if(item.isSelected())
		        	  settings.setNoOfSolutions(settings.FIND_ALL_SOLUTIONS);
		          else settings.setNoOfSolutions(settings.DEFAULT_NO_OF_SOLUTIONS);
			  }
			});
        solvingSettings.add(allSolutions);
        //settingsMenu.add(solvingSettings);
        menuBar.add(solvingSettings);
        
        
        //      skin selection (just for fun)
        JMenu skinSubMenu = new JMenu("Skin Selection");
        ButtonGroup skinButtonGroup = new ButtonGroup();
        JMenuItem classicSkinSelection = new JRadioButtonMenuItem(getSkinName(this.CLASSIC_SKIN));
        JMenuItem girlieSkinSelection = new JRadioButtonMenuItem(getSkinName(this.GIRLIE_SKIN));
        JMenuItem nightOwlSkinSelection = new JRadioButtonMenuItem(getSkinName(this.NIGHTOWL_SKIN));
        JMenuItem colorBlindSkinSelection = new JRadioButtonMenuItem(getSkinName(this.COLORBLIND_SKIN));
        JMenuItem sunriseSkinSelection = new JRadioButtonMenuItem(getSkinName(this.WATERFALL_SKIN));
        JMenuItem armySkinSelection = new JRadioButtonMenuItem(getSkinName(this.ARMY_SKIN));
        skinButtonGroup.add(classicSkinSelection);
        skinButtonGroup.add(nightOwlSkinSelection);
        skinButtonGroup.add(colorBlindSkinSelection);
        skinButtonGroup.add(girlieSkinSelection);
        skinButtonGroup.add(sunriseSkinSelection);
        skinButtonGroup.add(armySkinSelection);
        classicSkinSelection.setSelected(this.skinType == this.CLASSIC_SKIN);
        girlieSkinSelection.setSelected(this.skinType == this.GIRLIE_SKIN);
        nightOwlSkinSelection.setSelected(this.skinType == this.NIGHTOWL_SKIN);
        colorBlindSkinSelection.setSelected(this.skinType == this.COLORBLIND_SKIN);
        sunriseSkinSelection.setSelected(this.skinType == this.WATERFALL_SKIN);
        armySkinSelection.setSelected(this.skinType == this.ARMY_SKIN);
        
        classicSkinSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changeSkin(CLASSIC_SKIN);   
			  }
			});
        girlieSkinSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changeSkin(GIRLIE_SKIN);   
			  }
			});
        nightOwlSkinSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changeSkin(NIGHTOWL_SKIN);   
			  }
			});
        colorBlindSkinSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changeSkin(COLORBLIND_SKIN);   
			  }
			});
        sunriseSkinSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changeSkin(WATERFALL_SKIN);   
			  }
			});
        armySkinSelection.addActionListener(new java.awt.event.ActionListener() {
			  public void actionPerformed (ActionEvent e) {
		           changeSkin(ARMY_SKIN);   
			  }
			});
        skinSubMenu.add(classicSkinSelection);
        skinSubMenu.add(girlieSkinSelection);
        skinSubMenu.add(nightOwlSkinSelection);
        skinSubMenu.add(colorBlindSkinSelection);
        skinSubMenu.add(sunriseSkinSelection);
        skinSubMenu.add(armySkinSelection);
        settingsMenu.add(skinSubMenu);
        //settingsMenu.addSeparator();
        
        // Help Menu
        helpMenu.setText("Help");
        menuBar.add(helpMenu);
        JMenuItem bugReports = new JMenuItem("Bug Reports");
        JMenuItem about = new JMenuItem("About");
        helpMenu.add(bugReports);
        helpMenu.add(about);

        setJMenuBar(menuBar);
    }
    
    private void initOutput(int width, int outputHeight) {
    	
		// normalise text area
		this.normaliseOutput = new JTextArea("", width, outputHeight);
		this.normaliseOutput.setEditable(false);
		this.normaliseOutput.setBackground(this.textAreaColor);
		this.normaliseOutput.setForeground(this.textAreaFontColor);
		JScrollPane normaliseScrollPane = new JScrollPane(normaliseOutput);
		normaliseScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		normaliseScrollPane.setPreferredSize(new Dimension(width, outputHeight));
		
		
		// flat text area
		this.flatOutput = new JTextArea("",width,outputHeight);
		this.flatOutput.setEditable(false);
		this.flatOutput.setBackground(this.textAreaColor);
		this.flatOutput.setForeground(this.textAreaFontColor);
		JScrollPane flatScrollPane = new JScrollPane(flatOutput);
		flatScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		flatScrollPane.setPreferredSize(new Dimension(width, outputHeight));
		
		
		
		// minion output text area 
		this.minionOutput = new JTextArea("",width, outputHeight);
		JScrollPane minionOutputScrollPane = new JScrollPane(minionOutput);
		this.minionOutput.setEditable(true);
		this.minionOutput.setBackground(this.textAreaColor);
		this.minionOutput.setForeground(this.textAreaFontColor);
		minionOutput.setFont(this.outputFont);
		minionOutputScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		minionOutputScrollPane.setPreferredSize(new Dimension(width, outputHeight));

		JLabel outputLabel = new JLabel("Minion format");
		outputLabel.setLabelFor(minionOutput);
		
		
		// solution text area
		this.solutionOutput = new JTextArea("", width, outputHeight);
		JScrollPane solutionScrollPane = new JScrollPane(this.solutionOutput);
		this.solutionOutput.setEditable(false);
		this.solutionOutput.setBackground(this.textAreaColor);
		this.solutionOutput.setForeground(this.textAreaFontColor);
		solutionScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		solutionScrollPane.setPreferredSize(new Dimension(width, outputHeight));

		JLabel solutionOutputLabel = new JLabel("Solutions in Essence'");
		solutionOutputLabel.setLabelFor(solutionOutput);
		
		this.outputTabbedPanel = new JTabbedPane();
		
		// don't change the order!!
		outputTabbedPanel.add("Normalised E'", normaliseScrollPane);
		outputTabbedPanel.add("Flat E'", flatScrollPane);
		outputTabbedPanel.add("Solver Input", minionOutputScrollPane);
		outputTabbedPanel.add("Essence' solution", solutionScrollPane);
		outputTabbedPanel.setPreferredSize(new Dimension(width, outputHeight));
		
		outputTabbedPanel.setEnabledAt(this.NORMALISE_TAB_NR, false);
		outputTabbedPanel.setEnabledAt(this.FLAT_TAB_NR, false);
		outputTabbedPanel.setEnabledAt(this.SOLUTION_TAB_NR, false);
		outputTabbedPanel.setSelectedIndex(this.SOLVER_INPUT_TAB_NR);
    }
    
    private void changeSkin(char skinType) {
    	
    	this.skinType = skinType;
    	setColors();
    	updateColors();
    	this.setTitle(this.TAILOR_VERSION+"  "+getSkinName(this.skinType));
    }
    
    private void updateColors() {
    	
    	this.setBackground(this.bgColor);
    	 getContentPane().setBackground(this.bgColor);
    	this.inputPanel.setBackground(this.inputBgColor);
    	this.inputSplitPane.setBackground(this.inputBgColor);
    	this.problemButtonPanel.setBackground(this.inputBgColor);
    	this.parameterButtonPanel.setBackground(this.inputBgColor);
    	this.problemInputPanel.setBackground(this.inputBgColor);
    	this.parameterInputPanel.setBackground(this.inputBgColor);
    	this.problemInputScrollPane.setBackground(this.inputBgColor);
    	this.parameterInputScrollPane.setBackground(this.inputBgColor);
    	
    	this.outputPanel.setBackground(this.outputBgColor);
    	this.outputSplitPane.setBackground(this.outputBgColor);
    	this.outputButtonPanel.setBackground(this.outputBgColor);
    	this.outputScrollPane.setBackground(this.outputBgColor);
    	this.outputSplitPane.setBackground(this.outputBgColor);
    	this.outputTabbedPanel.setBackground(this.outputBgColor);
    	this.messagesPanel.setBackground(this.outputBgColor);
    	this.middlePanel.setBackground(this.bgColor);
    	
    	// text areas
    	this.problemInput.setBackground(this.textAreaColor);
    	this.problemInput.setForeground(this.textAreaFontColor);
    	this.parameterInput.setForeground(this.textAreaFontColor);
    	this.parameterInput.setBackground(this.textAreaColor);
    	this.messageOutput.setBackground(this.textAreaColor);
    	this.messageOutput.setForeground(this.textAreaFontColor);
    	this.normaliseOutput.setBackground(this.textAreaColor);
		this.normaliseOutput.setForeground(this.textAreaFontColor);
		this.flatOutput.setBackground(this.textAreaColor);
		this.flatOutput.setForeground(this.textAreaFontColor);
		this.minionOutput.setBackground(this.textAreaColor);
		this.minionOutput.setForeground(this.textAreaFontColor);
		this.solutionOutput.setBackground(this.textAreaColor);
		this.solutionOutput.setForeground(this.textAreaFontColor);
		
		// Button colors
		saveProblemButton.setBackground(this.buttonColor);
		this.saveProblemButton.setFocusPainted(true);
		saveParameterButton.setBackground(this.buttonColor);
		saveOutputButton.setBackground(this.buttonColor);
		loadProblemButton.setBackground(this.buttonColor);
		loadParameterButton.setBackground(this.buttonColor);
		clearProblemButton.setBackground(this.buttonColor);
		clearParameterButton.setBackground(this.buttonColor);
		clearOutputButton.setBackground(this.buttonColor);
		minionButton.setBackground(this.buttonColor);
		gecodeButton.setBackground(this.buttonColor);
		this.runMinionButton.setBackground(this.buttonColor);
		
    }
    
    private void changePath() {
		
		int returnVal = this.fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				
				String minionPath = file.getAbsolutePath();
				//this.minionPathField.setText(minionPath);
				this.settings.setPathToMinion(minionPath);
				this.writeOnMessageOutput("Changed Minion path to "+minionPath);
  			
			} catch(Exception e) {
				System.out.println("Could not change path to Minion:\n"+e.getMessage()+"\n");
				return;
			}
                	
			
		} /*else {
			writeOnMessageOutput("Changing Minion path command cancelled by user.\n");
		}	*/
	}
    
    /**
	 * Load a problem, parameter or output file, depending on the 
	 * command passed. 	
	 * @param command
	 */
	protected void load(String command) {
		
		
			String type = "";
			if(command.endsWith(LOAD_PROBLEM)) 
				type = "problem";
				
			else if(command.equalsIgnoreCase(LOAD_PARAMETER))
				type = "parameter";

			fileChooser.setDialogTitle("Load "+type+" File");
		    int returnVal = fileChooser.showOpenDialog(this);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	  File file = fileChooser.getSelectedFile();
				  String fileName = file.getPath();
				  this.fileChooser.setCurrentDirectory(file);  
				    
		    	if(command.endsWith(LOAD_PROBLEM))
		    		writeOnMessageOutput("Loading problem file:\n " +fileName+"\n");
		    	else if(command.endsWith(LOAD_PARAMETER))
		    		writeOnMessageOutput("Loading parameter file:\n " +fileName+"\n");		   
			
		    try {
		    	BufferedReader reader = new BufferedReader(new FileReader(file));
		    	StringBuffer loadedString = new StringBuffer("");
		    	String s = reader.readLine();
		    	while(s != null && !(s.equalsIgnoreCase("null"))) {
		    		//s = reader.readLine();
		    		//if(s != null || s.equalsIgnoreCase("null")) break;
		    		loadedString.append(s+"\n");
		    		s = reader.readLine();
		    	}
		    	
		    	
		    	if(command.equalsIgnoreCase(LOAD_PROBLEM))
		    		writeOnProblemInput(loadedString.toString());
		    	else if(command.equalsIgnoreCase(LOAD_PARAMETER))
		    		writeOnParameterInput(loadedString.toString());
		    	
		    	reader.close();
		    	
		    } catch(Exception e) {
		    	writeOnMessageOutput(e.getMessage());
		    	return;
		    }
		    
		    }
		    else {
	            writeOnMessageOutput("Choosing file cancelled by user.\n");
	        }
		
	}
    
    
	/**
	 * Save a problem, parameter or output file, depending on the 
	 * command passed. 
	 * 
	 * @param command
	 */
	protected void save(String command) {
	
		//int returnVal = this.fileChooser.showSaveDialog(this);
		
		String type = "";
		if(command.endsWith(SAVE_PROBLEM))
			type = "problem";
		else if(command.equalsIgnoreCase(SAVE_PARAMETER)) {
			type = "parameter";
			//FileFilter filter = new FileNameExtensionFilter("param");
		}
		else if(command.equalsIgnoreCase(SAVE_OUTPUT)) {
			type =this.solver.getSolverName()+" input";
		}
		
		fileChooser.setDialogTitle("Save "+type+" file");
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
					
					
					if(command == SAVE_PROBLEM) { 
                    	writer.write(this.problemInput.getText());
                    	writeOnMessageOutput("Saving Essence' problem in:\n " + file.getName() + "." +"\n");
                    }
					else if(command == SAVE_PARAMETER) {
						writer.write(this.parameterInput.getText());
                    	writeOnMessageOutput("Saving Essence' parameter in:\n " + file.getName() + "." +"\n");
					}
					else if(command == SAVE_OUTPUT) {						
							String text = "";
							int selection = this.outputTabbedPanel.getSelectedIndex();
							
							if(selection == this.SOLVER_INPUT_TAB_NR)
								text = this.minionOutput.getText();
							else if(selection == this.FLAT_TAB_NR)
								text = this.flatOutput.getText();
							else if(selection == this.SOLUTION_TAB_NR)
								text = this.solutionOutput.getText();
							else if(selection == this.NORMALISE_TAB_NR)
								text = this.normaliseOutput.getText();
							else text = this.minionOutput.getText();
							
							
							writer.write(text);
							writeOnMessageOutput("Saving "+solver.getSolverName()+" input in:\n " + file.getName() + "." +"\n");
						
					}
					writer.flush();
                    writer.close();
                    
        	
					
			} catch(Exception e) {
				writeOnMessageOutput(e.getMessage()+"\n");
				return;
			}
                	
			
		} else {
			writeOnMessageOutput("Save command cancelled by user.\n");
		}	
	}
	
	/**
	 * Claer 
	 * 
	 * @param command
	 */
	protected void clearInputField(String command) {
		
		if(command.equals(this.CLEAR_PROBLEM)) {
			this.problemInput.setText(this.ESSENCE_PRIME_HEADER);
			writeOnMessageOutput("Cleared problem input.\n");
		}
		
		else if(command.equals(this.CLEAR_PARAMETER)) {
			this.parameterInput.setText(this.ESSENCE_PRIME_HEADER);
			writeOnMessageOutput("Cleared parameter input.\n");
		}
	}
	
protected void translate(String command) {
		
		if(command == PARSE) {
			//parse();
			return;
		}
		else if(command == INSERT_PARAMETERS) {
			//insertParameters();
		}
		else if(command == EVALUATE) {
			//evaluate();
		}
		else if(command == ORDER) {
			//order();
		}
		else if(command == NORMALISE) {
			//normalise();
		}
		else if(command == FLATTEN) {
			//flatten();
		}
		else if(command == MINION) {
			toMinion();
		}
		else if(command == GECODE) {
			toGecode();
		}
	}
	
	
	protected boolean toMinion() {
		
		this.solver = new Minion();
		this.settings.setTargetSolver(solver);
		
		if(!flatten()) return false;
		//System.out.println("Flattened the stuff, now tailoring.");
		boolean tailoring = this.translator.tailorTo(solver);
		
		if(tailoring) {	
			writeOnOutput(this.SOLVER_INPUT_TAB_NR, this.translator.getTargetSolverInstance());
			writeOnMessageOutput("Tailored model to target solver "+solver.getSolverName()+ "\n");
			setCaretPositionOnOutput(this.SOLVER_INPUT_TAB_NR, 1);
			this.runMinionButton.setEnabled(true);
		}
		else {
			writeOnMessageOutput("===================== ERROR ======================\n"+
					"Tailoring to target solver "+solver.getSolverName()+" failed.\n"+
					this.translator.getErrorMessage()+"\n"+
					"===============================================\n");
			return false;
		}
		return true;
	}
	
	
	protected boolean toGecode() {
		
		this.solver = new Gecode();
		this.settings.setTargetSolver(solver);
			
		
		if(!flatten()) return false;
		//System.out.println("Flattened the stuff, now tailoring.");
		boolean tailoring = this.translator.tailorTo(solver);
		
		if(tailoring) {	
			writeOnOutput(this.SOLVER_INPUT_TAB_NR, this.translator.getTargetSolverInstance());
			writeOnMessageOutput("Tailored model to target solver "+solver.getSolverName()+ "\n");
			setCaretPositionOnOutput(this.SOLVER_INPUT_TAB_NR, 1);
			this.runMinionButton.setEnabled(false);
		}
		else {
			writeOnMessageOutput("===================== ERROR ======================\n"+
					"Tailoring to target solver "+solver.getSolverName()+" failed.\n"+
					this.translator.getErrorMessage()+"\n"+
					"===============================================\n");
			return false;
		}
		return true;
	}
	
	
	protected boolean flatten() {
		
		if(!normalise()) return false;
		boolean flattening = this.translator.flatten(this.solver);
		
		if(flattening) {
			//System.out.println("Flattening is fertig.");
			writeOnOutput(this.FLAT_TAB_NR, this.translator.printAdvancedModel());
			writeOnMessageOutput("Flattened constraints for target solver "+solver.getSolverName()+"\n");
			//System.out.println("Flattening is WIRKLICK fertig.");
		}
		else {
			writeOnMessageOutput("================== ERROR ======================\n"+
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
			this.writeOnOutput(this.NORMALISE_TAB_NR, this.translator.printAdvancedModel());
			this.writeOnMessageOutput("Full Normalisation (ordering, evaluation, restructuring) successful.\n");
		}
		else {
			this.writeOnMessageOutput("================== ERROR ======================\n"+
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
			writeOnOutput(NORMALISE_TAB_NR, this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation with Ordering successful.\n");
		}
		else {
			writeOnMessageOutput("================== ERROR ======================\n"+
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
			writeOnOutput(this.NORMALISE_TAB_NR,this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation with Evaluation successful.\n");
		}
		else {
			writeOnMessageOutput("================== ERROR ======================\n"+
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
			writeOnOutput(this.NORMALISE_TAB_NR, this.translator.printAdvancedModel());
			writeOnMessageOutput("Basic Normalisation successful.\n");
			//printDebugMessages();
		}
		else {
			writeOnMessageOutput("================== ERROR ======================\n"+"Basic Normalisation failed.\n"
			     +this.translator.getErrorMessage()+"\n"+
			"===============================================\n");
			return false;

		}
		return true;
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
			writeOnOutput(this.NORMALISE_TAB_NR, this.translator.printInitialProblemSpecification());
			writeOnMessageOutput("Parsing OK.\n");
		}
		else {
			writeOnMessageOutput("===================== ERROR ======================\n"+
			"Parse Error.\n"+this.translator.getErrorMessage()+"\n"+
			"===============================================\n");
			System.err.println(this.translator.getErrorMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Executes Minion: first the string from the solver input tab is read and 
	 * written into a file. Then that file is given as input to Minion with 
	 * respect to the solving settings given (amount of solutions etc).
	 * 
	 */
	protected void runMinion() {
		
		try {
			String outputFileName = this.settings.getSolverInputFileName();
			if(outputFileName == null)
				outputFileName = "out."+this.settings.getTargetSolver().getSolverInputExtension();
			
			//-------------- write the output into a file --------------- 
			writeOnMessageOutput("Creating Minion file: "+outputFileName+"\n");
			File file  = new File(outputFileName);
		    if(file.createNewFile()) ;
			FileWriter writer = new FileWriter(file);
        
			if(!file.canRead())
				writeOnMessageOutput("Cannot read file: \n "+file.toString()+"\nPlease change reading permissions.");
			else if(!file.canWrite())
				writeOnMessageOutput("Cannot write file: \n "+file.toString()+"\nPlease change writing permissions.");

			writer.write(this.minionOutput.getText());
			String message = "Saved Minion input in file: \n " + file.getName() + "." +"\n";
			writeOnMessageOutput(message);
			
			writer.flush();
            writer.close();
            
            String minionExecPath = this.settings.getPathToMinion(); 
            
            String[] commandArguments;
            int noOfSolutions = this.settings.getNoOfSolutions();
            writeOnMessageOutput(message+"\n\nSearch for "+
            		((noOfSolutions == settings.FIND_ALL_SOLUTIONS) ? "all" : noOfSolutions)+" solution(s)" +
            				". This might take a while.");
            
            if(noOfSolutions == 1) {
            	commandArguments = new String[] { minionExecPath, outputFileName };
            }
            else if(noOfSolutions == this.settings.FIND_ALL_SOLUTIONS){
            	commandArguments = new String[] {minionExecPath, "-findallsols", outputFileName};
            }
            else {
            	commandArguments = new String[] {minionExecPath, "-sollimit "+noOfSolutions, outputFileName};
            }
            
            Process process = Runtime.getRuntime().exec(commandArguments);
            
            // first read the input and the error stream
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            BufferedReader input =new BufferedReader(new InputStreamReader(inputStream));
            BufferedReader error =new BufferedReader(new InputStreamReader(errorStream));
            
            String line;
            String solverOutputString = "";
            while ((line = input.readLine()) != null) {
            	solverOutputString = solverOutputString+line+"\n";
            }
            line = "";
            StringBuffer solverErrorMessage = new StringBuffer("");
            while ((line = error.readLine()) != null) {
              	solverErrorMessage.append(line+"\n");
            }              
            
            // then wait for process (to conform with Microsoft Windows)
            process.waitFor();
            
            int exitValue = process.exitValue();
              
            
            if(exitValue == 0) {
                  writeOnOutput(this.SOLUTION_TAB_NR, this.translator.getEssenceSolution(solverOutputString));
                  input.close();
            }
            else {
                writeOnOutput(this.SOLUTION_TAB_NR, "$ Error in executing Minion.\n$ See system messages below for details.\n");
           
                if(solverOutputString.length() > 1)
                	this.writeOnMessageOutput("Minion returned the following error message:\n"+solverOutputString);
                input.close(); 
       
                this.writeOnMessageOutput("Minion returned the following error message:\n"+solverErrorMessage);
                error.close();
            }
		
		} catch(Exception e) {
			writeOnMessageOutput("Could not run Minion:\n"+e.getMessage()+"\n"+
					"You can change the path to your Minion executable in 'Settings'.\n");
			return;
		}	
	}
	
	
	/**
	 * Write a String on the tab with number 'tabNumber'. 
	 * There are tabs for normalised E', flat E', solver input
	 * and the E' solution.
	 * 
	 * @param tabNumber
	 * @param text
	 */
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
		
		else if(tabNumber == this.SOLVER_INPUT_TAB_NR) {
			this.minionOutput.setText(text);
			this.outputTabbedPanel.setTitleAt(this.SOLVER_INPUT_TAB_NR, 
					this.settings.getTargetSolver().getSolverName()+" Input");
			this.outputTabbedPanel.setEnabledAt(this.SOLVER_INPUT_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.SOLVER_INPUT_TAB_NR);
		}
		
		else if(tabNumber == this.SOLUTION_TAB_NR) {
			this.solutionOutput.setText(text);
			this.outputTabbedPanel.setEnabledAt(this.SOLUTION_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.SOLUTION_TAB_NR);
		}
	}
    
    /**
     * Puts the caret at a given position on the output with 
     * number tabNumber.
     * 
     * @param tabNumber
     * @param position
     */
  public void setCaretPositionOnOutput(int tabNumber, int position) {
		
		if(tabNumber == this.NORMALISE_TAB_NR) {
			this.normaliseOutput.setCaretPosition(position);
			this.outputTabbedPanel.setEnabledAt(this.NORMALISE_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.NORMALISE_TAB_NR);
		}
			
		else if(tabNumber == this.FLAT_TAB_NR) {
			this.flatOutput.setCaretPosition(position);
			this.outputTabbedPanel.setEnabledAt(this.FLAT_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.FLAT_TAB_NR);
		}
		
		else if(tabNumber == this.SOLVER_INPUT_TAB_NR) {
			this.minionOutput.setCaretPosition(position);
			this.outputTabbedPanel.setEnabledAt(this.SOLVER_INPUT_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.SOLVER_INPUT_TAB_NR);
		}
		
		else if(tabNumber == this.SOLUTION_TAB_NR) {
			this.solutionOutput.setCaretPosition(position);
			this.outputTabbedPanel.setEnabledAt(this.SOLUTION_TAB_NR, true);
			this.outputTabbedPanel.setSelectedIndex(this.SOLUTION_TAB_NR);
		}
	}
	
	
	private void writeOnMessageOutput(String message) {
		this.messageOutput.setText(message);
	}
	
	
	private void writeOnProblemInput(String input) {
		this.problemInput.setText(input);
	}
	
	private void writeOnParameterInput(String input) {
		this.parameterInput.setText(input);
	}
	
	protected void updateLineColPositions(int line, int col) {
		this.colLineLabelProblem.setText("Line: "+line+"  Column: "+col);
	}
	
	protected void updateParameterLineColPositions(int line, int col) {
		this.colLineLabelParameter.setText("Line: "+line+"  Column: "+col);
	}
	
    /*private void jMenu1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jMenu1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu1KeyPressed
	*/

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentResized

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TailorGUI().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearOutputButton;
    private javax.swing.JButton clearParameterButton;
    private javax.swing.JButton clearProblemButton;
    private javax.swing.JLabel colLineLabelParameter;
    private javax.swing.JLabel colLineLabelProblem;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JSplitPane inputSplitPane;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel middlePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadParameterButton;
    private javax.swing.JButton loadProblemButton;
    private javax.swing.JTextArea messageOutput;
    private javax.swing.JPanel messagesPanel;
    private javax.swing.JPanel outputButtonPanel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JSplitPane outputSplitPane;
    //private javax.swing.JTextPane outputTextPane;
    private javax.swing.JPanel parameterButtonPanel;
    private javax.swing.JTextPane parameterInput;
    private javax.swing.JPanel parameterInputPanel;
    private javax.swing.JScrollPane parameterInputScrollPane;
    private javax.swing.JPanel problemButtonPanel;
    private javax.swing.JTextPane problemInput;
    private javax.swing.JPanel problemInputPanel;
    private javax.swing.JScrollPane problemInputScrollPane;
    private javax.swing.JButton runMinionButton;
    private javax.swing.JButton saveOutputButton;
    private javax.swing.JButton saveParameterButton;
    private javax.swing.JButton saveProblemButton;
    private javax.swing.JPanel solverButtonPanel;
    // End of variables declaration//GEN-END:variables
    //private InputPanel inputPanel;
    //private OutputPanel outputPanel;
    
    private java.awt.Color inputBgColor;
    private java.awt.Color outputBgColor;
    private java.awt.Color bgColor;
    private java.awt.Color textAreaColor;
    private java.awt.Color textAreaFontColor;
    private java.awt.Font inputFont;
    private java.awt.Font outputFont;
    private java.awt.Color buttonColor;
    private java.awt.Color buttonFontColor;
    private java.awt.Color buttonPressedColor;
}
