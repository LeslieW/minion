package translator;

import javax.swing.JFrame;
import java.io.*;
import translator.solver.Minion;
import translator.solver.Gecode;
import translator.gui.TailorGUI;
import translator.xcsp2ep.Xcsp2Ep;

/**
 * This is the main class. It can either evoke the GUI version or
 * the command-line version of the Essence' translator. The GUI version 
 * is default. Starting the translator with the "help" argument gives 
 * some information about the usage. 
 * 
 * @author andrea
 *
 */

public class Translate {

	public static final String GUI_NAME = "TAILOR v0.2";
	public static final String VERSION ="0.2";
	public static final String ESSENCE_PRIME_HEADER = "ESSENCE' 1.0\n";
	public static final String ERROR = "ERROR:";
	
	/** Available flags */
	public static final String XCSP_CONVERSION = "xcsp";
	public static final String MINION_TRANSLATION = "minion";
	public static final String GECODE_TRANSLATION = "gecode";
	public static final String HELP = "help";
	public static final String OLD_GUI = "oldgui";
	public static final String NO_COMMON_SUBEXPRS = "no-cse";
	public static final String NO_COMMON_EXPLICIT_SUBEXPRS = "no-ecse";
	public static final String TIME_OFF = "time"; // display time statistics
	public static final String NO_INFO = "silent"; // silent -> no verbose info
	public static final String NO_DIRECT_VAR_REUSE = "no-dvr";
	public static final String DIRECT_VAR_REUSE = "dvr";
	public static final String DEBUG_MODE = "debug";
	public static final String TIME_INFO_FILE = "tf"; // don't write translation time into file
	public static final String WRITE_EP_MODEL_TO_FILE = "out-ep"; // write the Essence' model into a file
	public static final String NO_PROPAGATE_SINGLE_DOMAINS = "no-sdp"; // don't propagate single domains, e.g. (1..1)
	public static final String DISCRETE_VAR_UPPER_BOUND = "discrete-ub"; // maximum domain size for when to use discrete vars
	public static final String OUTPUT = "out"; //set the output file name/directory
	public static final String NO_OF_SOLUTIONS = "sols";
	public static final String CSE_DETAILS = "cse-info"; // get information about the CSEs
	
	private static boolean giveTimeInfo = true;
	private static boolean giveTranslationInfo = true;
	
	/**
	 * Start the GUI version or command-line version of the translator.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	
		
		
		if(args.length == 0) {
			runNewGUI();	
		}
		
		TranslationSettings settings = new TranslationSettings();
	
		
		for(int i=0; i<args.length; i++) {
			
			// we have a flag here
			if(args[i].startsWith("-")) {
				
				if(args[i].equalsIgnoreCase("-"+HELP) || args[i].equalsIgnoreCase("-h")) {
					//if(settings.giveTranslationInfo)
					printWelcomeMessage();
					printHelpMessage(settings);
					System.exit(0);
				}
				
				else if(args[i].equalsIgnoreCase("-"+NO_COMMON_SUBEXPRS)) {
					settings.setUseCommonSubExpressions(false);
				}
				
				else if(args[i].equalsIgnoreCase("-"+NO_COMMON_EXPLICIT_SUBEXPRS)) {
					settings.setUseExplicitCommonSubExpressions(false);	
					settings.useEqualSubExpressions = false;
					settings.useExplicitCommonSubExpressions = false;
				}
				
				else if(args[i].equalsIgnoreCase("-"+NO_PROPAGATE_SINGLE_DOMAINS)) {
					settings.setPropagateSingleIntRanges(false);
				}
				
				else if(args[i].equalsIgnoreCase("-"+WRITE_EP_MODEL_TO_FILE)) {
					settings.setWriteEssencePrimeModelIntoFile(true);
				}
				
				else if(args[i].equalsIgnoreCase("-"+CSE_DETAILS)) {
					settings.setCseDetails(true);
				}
				
				else if(args[i].equalsIgnoreCase("-"+TIME_OFF)) {
					settings.setGiveTranslationTimeInfo(true);
					giveTimeInfo = true;
				}
				
				else if(args[i].equalsIgnoreCase("-"+OUTPUT)) {
					
					if(args.length <= i+1) {
						printWelcomeMessage();
						System.out.println("Sorry, could not read output filename for translation.");
						System.exit(1);
					}
					settings.setSolverInputFileName(args[i+1]);
					i++;
				}
				
				else if(args[i].equalsIgnoreCase("-"+TIME_INFO_FILE)) {
					settings.setPrintTranslationTimeIntoFile(false);
				}
				
				else if(args[i].equalsIgnoreCase("-"+DISCRETE_VAR_UPPER_BOUND)) {
					if(i + 1 >= args.length) {
						printWelcomeMessage();
						System.out.println("Sorry, could not read upper bound for discrete variables.");
						System.exit(1);
					}
					Integer upperBound = new Integer(args[i+1]);
					if(upperBound < 0) 
						System.out.println("Sorry, cannot set a discrete upper bound that is smaller than zero:"+upperBound+
								"\nUpper bound flag ignored.");
					
					settings.setDiscreteUpperBound(upperBound);
					i++;
				}
				
				else if(args[i].equalsIgnoreCase("-"+NO_OF_SOLUTIONS)) {
					if(i + 1 >= args.length) {
						printWelcomeMessage();
						System.out.println("Sorry, could not read the number of solutions to search for.");
						System.exit(1);
					}
					Integer noOfSolutions = new Integer(args[i+1]);
					if(noOfSolutions < 0) 
						System.out.println("Sorry, cannot set the amount of solutions to an amount smaller than zero:"+noOfSolutions+
								"\n.To search for all solutions set the amount to '0'. Number of solutions flag ignored.");
					
					settings.setNoOfSolutions(noOfSolutions);
					i++;
				}
				
				else if(args[i].equalsIgnoreCase("-"+NO_INFO) || args[i].equalsIgnoreCase("-s")) {
					settings.giveTranslationInfo = false;
					settings.giveTranslationTimeInfo = false;
					giveTimeInfo = false;
					giveTranslationInfo = false;
				}
				
				
				else if(args[i].equalsIgnoreCase("-"+NO_DIRECT_VAR_REUSE)) {
					settings.setApplyDirectVariableReusage(false);
				}
				
				else if(args[i].equalsIgnoreCase("-"+DIRECT_VAR_REUSE)) {
					settings.setApplyDirectVariableReusage(true);
				}

				else if(args[i].equalsIgnoreCase("-"+GECODE_TRANSLATION) || args[i].equalsIgnoreCase("-g")) {
					settings.setTargetSolver(new Gecode());
				}
				
				else if(args[i].equalsIgnoreCase("-"+MINION_TRANSLATION) || args[i].equalsIgnoreCase("-m")) {
					settings.setTargetSolver(new Minion());
				}
				
				else if(args[i].equalsIgnoreCase("-"+DEBUG_MODE)) {
					settings.debugMode = true;
				}
				
				else if(args[i].equalsIgnoreCase("-"+XCSP_CONVERSION)) {
					if(i+1 == args.length) {
						System.out.println("No xml-input file specified...");
						System.out.println("Aborting translation process.\n");
						printHelpMessage(settings);
						System.exit(1);
					}
					else if(i+2 == args.length) {
						if(args[i+1].startsWith("-")) {
							System.out.println("No xml-input file specified...");
							System.out.println("Aborting translation process.\n");
							printHelpMessage(settings);
							System.exit(1);
						}
							
						String outFileName;
						if(settings.getSolverInputFileName() == null) {
							outFileName = args[i+1]+"."+settings.targetSolver.getSolverInputExtension();
						}
						else outFileName = settings.getSolverInputFileName();
						
						translateXCSP(args[i+1], outFileName, settings);
						System.exit(0);
					}
					else if(i+3 == args.length) {
						if(args[i+1].startsWith("-") || args[i+2].startsWith("-")) {
							printWelcomeMessage();
							System.out.println("No xml-input file specified...");
							System.out.println("Aborting translation process.\n");
							System.exit(1);
						}
						translateXCSP(args[i+1], args[i+2], settings);
						System.exit(0);
					}
					else {
						printWelcomeMessage();
						System.out.println("Too many input files specified for XCSP translation...");
						System.out.println("Trying to translate '"+args[i+1]+" as XCSP input file and '"+
								args[i+2]+"' as Minion output file.");
						translateXCSP(args[i+1], args[i+2], settings);
						System.exit(0);
					}
				}
				
				else {
					printWelcomeMessage();
					System.out.println("Sorry, did not understand the flag '"+args[i]+"'\n");
					//printHelpMessage();
					System.exit(1);
				}
				
			}
			// we have an input file here
			else {
				if(i+1 == args.length) {
					translate(args[i], settings);
					System.exit(0);
				}
				else if(i+2 == args.length) {
					translate(args[i], args[i+1], settings);
					System.exit(0);
				}
				else {
					printWelcomeMessage();
					System.out.println("Too many input files specified for translation...");
					System.out.println("Trying to translate '"+args[i+1]+" as input file and '"+
							args[i+2]+"' as parameter file.");
					translateXCSP(args[i], args[i+1], settings);
					System.exit(1);
				}
			}
			
		}
		
		
	
	}


	// ======================== METHODS THAT INITIATE TRANSLATION =======================================
	
	private static void runNewGUI() {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new TailorGUI();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    	
	}
	
	
	/**
	 * Translates a problem file only (without parameter instantiations)
	 * 
	 * @param filename
	 * @param settings
	 */
	private static void translate(String filename, TranslationSettings settings) {
		
		try {
			
			if(settings.giveTranslationInfo)
				printWelcomeMessage();
			
			settings = readModelName(filename, settings);
			giveTimeInfo = settings.giveTranslationTimeInfo;
			writeInfo("\nTranslating "+filename);
			long startTime = System.currentTimeMillis();
			
			String problemString = readStringFromFile(filename);
	    	Translator translator = new Translator(settings);
	    	boolean noError = translator.tailorTo(problemString,
	    			                ESSENCE_PRIME_HEADER,
	    			                settings.targetSolver);
	    	
	    	if(!noError) {
				System.out.println(ERROR+"Cannot translate to "+settings.targetSolver.getSolverName()
						+" from problem file '"+filename+"'.");
				System.out.println(translator.errorMessage);
				System.exit(1);
	    	}
	    	
			String solverInstance = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			
			double translationTime = (stopTime - startTime) / 1000.0;
			if(settings.getPrintTranslationTimeIntoFile()) {
				if(settings.targetSolver instanceof Minion)
					solverInstance = "# Translation Time: "+translationTime+"\n"+solverInstance;
				else solverInstance = "/* Translation Time: "+translationTime+"*/\n"+solverInstance;
			}
			
			String outFileName;
			if(settings.getSolverInputFileName() == null) {
				outFileName = filename+"."+settings.targetSolver.getSolverInputExtension();
			}
			else outFileName = settings.getSolverInputFileName();
			
			File solverInputFile = writeStringIntoFile(outFileName,
					                              solverInstance);
			if(settings.cseDetails) {
				writeStringIntoFile(outFileName+".cseinfo", translator.getCseInfo());
				System.out.println("Written CSE info into file: "
						+outFileName+".cseinfo");
			}
			
			writeInfo("Translated '"+filename+"' to "+settings.targetSolver.getSolverName()
					+" and written solver input\n into '"+solverInputFile.getAbsolutePath()+"'.\n");
			writeTimeInfo("Translation Time: "+translationTime+" sec");
			
			
			
	    	
		} catch(Exception e) {
			System.out.println(ERROR);
			if(settings.debugMode)
				e.printStackTrace(System.out);
			else System.out.println(e.getMessage());
			System.out.println("Cannot translate to "+settings.targetSolver.getSolverName()+" from problemfile:"+filename);
			System.exit(1);
		}
		
		
		
	}
	
	
	
	/**
	 * Translates a poblem file together with a parameter file
	 * 
	 * @param problemFileName
	 * @param parameterFileName
	 * @param settings
	 */
	private static void translate(String problemFileName, String parameterFileName, TranslationSettings settings) {
		try {
			if(settings.giveTranslationInfo)
				printWelcomeMessage();
			
			settings = readModelName(parameterFileName, settings);
			giveTimeInfo = settings.giveTranslationTimeInfo;
			writeInfo("\nTranslating "+problemFileName+" with "+parameterFileName);
			long startTime = System.currentTimeMillis();
			
			
			String problemString = readStringFromFile(problemFileName);
			String parameterString = readStringFromFile(parameterFileName);
	    	Translator translator = new Translator(settings);
	    	
	    	boolean noError = translator.tailorTo(problemString,
	    			                parameterString,
	    			                settings.targetSolver); 
	    	if(!noError) {
				System.out.println(ERROR+"Cannot translate to "+settings.targetSolver.getSolverName()
						+" from problem file '"+problemFileName+
						"' and parameter file '"+parameterFileName+"'.");
				System.out.println(translator.errorMessage);
				System.exit(1);
	    	}
	    	
			String solverInstance = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			double translationTime = (stopTime - startTime) / 1000.0;
			
			if(settings.getPrintTranslationTimeIntoFile())
				if(settings.targetSolver instanceof Minion)
					solverInstance = "# Translation Time: "+translationTime+"\n"+solverInstance;
				else // it's Gecode 
					solverInstance = "/* Translation Time: "+translationTime+"*/\n"+solverInstance;
			
			String outFileName;
			if(settings.getSolverInputFileName() == null) {
				outFileName = parameterFileName+"."+settings.targetSolver.getSolverInputExtension();
			}
			else outFileName = settings.getSolverInputFileName();
			File solverInputFile = writeStringIntoFile(outFileName,
												  solverInstance);
			
			if(settings.cseDetails) {
				writeStringIntoFile(outFileName+".cseinfo", translator.getCseInfo());
				System.out.println("Written CSE info into file: "
							+outFileName+".cseinfo");
			}
			
			writeInfo("Translated '"+problemFileName+"' and '"+parameterFileName+
					"' to "+settings.targetSolver.getSolverName()+
					" and written solver-input\n into '"+solverInputFile.getName()+"'.\n");
			writeTimeInfo("Translation Time: "+translationTime+" sec");
			
			
			
		} catch(Exception e) {
			System.out.println(ERROR);
			if(settings.debugMode)
				e.printStackTrace(System.out);
			else System.out.println(e.getMessage());
			System.out.println(ERROR+"Cannot translate to "+settings.targetSolver.getSolverName()
					+" from problem file '"+problemFileName+
					"' and parameter file '"+parameterFileName+"'.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}


	/**
	 * Translates an XCSP instance
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 * @param settings
	 */
	private static void translateXCSP(String inputFileName, String outputFileName, TranslationSettings settings) {
		
		try {
			if(settings.giveTranslationInfo)
				printWelcomeMessage();
			
			giveTimeInfo = settings.giveTranslationTimeInfo;
			writeInfo("\nTranslating XCSP file '"+inputFileName+"'");
			long startTime = System.currentTimeMillis();
			Xcsp2Ep xcspConverter = new Xcsp2Ep(settings);
			Translator translator = new Translator(settings);
			
			boolean noError = translator.tailorTo(xcspConverter.translateToNormalisedModel(inputFileName), settings.targetSolver);
			if(!noError) {
				System.out.println(ERROR+"Cannot translate to "+settings.targetSolver.getSolverName()
						+" from XCSP instance '"+inputFileName+"'.");
				System.out.println(translator.errorMessage);
				System.exit(1);
	    	}
			
			String minionString = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			double translationTime = (stopTime - startTime) / 1000.0;
			
			if(settings.getPrintTranslationTimeIntoFile())
				minionString = "# Translation Time: "+translationTime+"\n"+minionString;
			
			File solverInputFile = writeStringIntoFile(outputFileName, minionString);
			
			if(settings.cseDetails) {
				writeStringIntoFile(outputFileName+".cseinfo", translator.getCseInfo());				
				System.out.println("Written CSE info into file: "
							+outputFileName+".cseinfo");
				
			}
			
			writeInfo("Translated '"+inputFileName+
					"' to "+settings.targetSolver.getSolverName()
					+" and written solver input\n into '"+solverInputFile.getAbsolutePath()+"'.\n");
			writeTimeInfo("Translation Time: "+translationTime+" sec");
			
			
			
		} catch(Exception e) {
			System.out.println(ERROR);
			if(settings.debugMode)
				e.printStackTrace(System.out);
			else System.out.println(e.getMessage());
			System.out.println(ERROR+"Cannot translate to "+settings.targetSolver.getSolverName()+" from XCSP file '"+inputFileName);
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	
	// ======================= HELPER METHODS ============================================================
	
	private static void printHelpMessage(TranslationSettings settings) {
		
		System.out.println("\nUsage: java -jar tailor.jar [flags] [inputfiles]");
		System.out.println("If no arguments are given, the graphical translator version will start.\n");
		
		System.out.println("Possible inputfiles are:");
		System.out.println("filename.eprime");
		System.out.println("\tEssence' problem specification: Translates 'filename.eprime' to Minion input format");
		System.out.println("\tThe generated Minion file is written into 'filename.eprime.minion'\n");
		System.out.println("filename.eprime filename.param\n\tEssence' problem and parameter specification:");
		System.out.println("\tTranslates 'filename.eprime' with respect to the parameters given in"); 
		System.out.println("\t'filename.param' to Minion input format.");
		System.out.println("\tThe generated Minion file is written into 'filename.param.minion'\n");
		
		System.out.println("-"+XCSP_CONVERSION+" filename.xml");
		System.out.println("\tTranslates the XCSP file 'filename.xml' to Minion input format.");
		System.out.println("\tThe generated Minion file is written into 'filename.xml.minion'");
		System.out.println("\tPlease make sure that '-xcsp' is the last flag you set.\n");
		System.out.println("-"+XCSP_CONVERSION+" input.xml output.minion");
		System.out.println("\tTranslates the XCSP file 'input.xml' to Minion input format.");
		System.out.println("\tThe generated Minion file is written into 'output.minion'");
		System.out.println("\tPlease make sure that '-xcsp' is the last flag you set.\n");
		
		//System.out.println("\nAvailable flags:");
		System.out.println("\n\nGeneral flags:");
		System.out.println("-"+HELP+" or -h\n\tprints this help message");
		System.out.println("-"+MINION_TRANSLATION+" or -m");
		System.out.println("\tTranslates input files to solver Minion (default).");
		System.out.println("-"+GECODE_TRANSLATION+" or -g");
		System.out.println("\tTranslates input files to solver Gecode (not stable yet).");
		System.out.println("-"+OUTPUT+" FILENAME");
		System.out.println("\tWrites solver input into FILENAME");
		
		System.out.println("\n\nTranslation flags:");
		System.out.println("-"+NO_COMMON_SUBEXPRS);
		System.out.println("\tTurn off eliminating common subexpressions during flattening.");
		System.out.println("\tDefault: on");
		//System.out.println("-"+NO_DIRECT_VAR_REUSE);
		//System.out.println("\tTurn off directly reusing variables (e.g. in x=y, replacing x with y).");
		//System.out.println("\tDefault: on");
		System.out.println("-"+DIRECT_VAR_REUSE);
		System.out.println("\tTurn on directly reusing variables (e.g. in x=y, replacing x with y).");
		System.out.println("\tDefault: off");
		System.out.println("-"+NO_COMMON_EXPLICIT_SUBEXPRS);
		System.out.println("\tTurn off eliminating complex common subexpressions.");
		System.out.println("\tDefault: on");
		System.out.println("-"+NO_PROPAGATE_SINGLE_DOMAINS);
		System.out.println("\tTurn off propagating single domains on constraints.");
		System.out.println("\t(for instance, if variable X ranges over (1..1) then replace every .");
		System.out.println("\toccurrence of X with 1).");
		System.out.println("\tDefault: on");
		System.out.println("-"+DISCRETE_VAR_UPPER_BOUND+" BOUND");
		System.out.println("\tSet the maximum domain size for which to use discrete variables,");
		System.out.println("\ti.e. if BOUND equals 300, every variable with a domain size smaller or ");
		System.out.println("\tequal 300 will be represented by a discrete variable");
		System.out.println("\t(allowing domain consistency)");
		System.out.println("\tDefault: "+settings.getDiscreteUpperBound());
		System.out.println("-"+NO_OF_SOLUTIONS+" AMOUNT");
		System.out.println("\tSet the amount of solutions to search for in the target solver.");
		System.out.println("\tTo search for all solutions, set AMOUNT to '"+settings.getFindAllSolutionsAlias()+"'.");
		System.out.println("\tDefault amount of solutions to search for: "+settings.getNoOfSolutions());
		
		System.out.println("\n\nOutput flags:");
		System.out.println("-"+TIME_OFF);
		System.out.println("\tDisplay time statistics");
		System.out.println("\tDefault: don't show time statistics");
		System.out.println("-"+NO_INFO);
		System.out.println("\tSilent mode. Don't give translation information.");
		System.out.println("\tDefault: give translation info");
		System.out.println("-"+DEBUG_MODE);
		System.out.println("\tDebug mode. Prints stack trace when exception is thrown.");
		System.out.println("\tDefault: off.");
		System.out.println("-"+TIME_INFO_FILE);
		System.out.println("\tDon't write translation time into the output file.");
		System.out.println("\tDefault: on.");
		System.out.println("-"+WRITE_EP_MODEL_TO_FILE);
		System.out.println("\tWrite the generated Essence' model into a file.");
		System.out.println("\tDefault: off");
		System.out.println("-"+CSE_DETAILS);
		System.out.println("\tGive detailed information about common subexpression elimination");
		System.out.println("\tDefault: off.");

	}
	
	
	private static String readStringFromFile(String filename) 
		throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
    	StringBuffer loadedString = new StringBuffer("");
    	String s = reader.readLine();
    	while(s != null && !(s.equalsIgnoreCase("null"))) {
    		loadedString.append(s);
    		loadedString.append("\n");
    		s = reader.readLine();
    	}
    	reader.close();
    	return loadedString.toString();
		
	}
	
	
	private static File writeStringIntoFile(String fileName, String stringToWrite) 
		throws IOException {
		
		File file = new File(fileName);
		file.createNewFile();
		
		FileWriter writer = new FileWriter(file);
		writer.write(stringToWrite);
        	
		writer.flush();
        writer.close();
		
		return file;
	}
	
	private static void printWelcomeMessage() {
		System.out.println("# Welcome to TAILOR v"+VERSION);
		System.out.println("# Use flag -h for help. \n# Submit bug reports to: andrea@cs.st-and.ac.uk");
	}
	
	private static void writeInfo(String info) {
		if(giveTranslationInfo)
			System.out.println(info);
	}
	
	private static void writeTimeInfo(String info) {
		if(giveTimeInfo)
			System.out.println(info);
	}
	
	private static TranslationSettings readModelName(String filename, TranslationSettings settings) {
		
		int positionOfDot = 0;
		int startOfName = 0;
		
		for(int i = 0; i < filename.length(); i++) {
			if(filename.charAt(i) == '/') {
				startOfName = i;
			}
		}
		
		
		for(positionOfDot = 0; positionOfDot < filename.length(); positionOfDot++) {
			if(filename.charAt(positionOfDot) == '.') {
				settings.setModelName(filename.substring(startOfName+1,positionOfDot));
				return settings;
			}
		}
		settings.setModelName(filename.substring(0,positionOfDot));
		return settings;
	
	}
}
