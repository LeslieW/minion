package translator;

import javax.swing.JFrame;
import java.io.*;
import translator.solver.Minion;
import translator.gui.TailorGUI;
import translator.xcsp2ep.Xcsp2Ep;

/**
 * This is the started class. It can either evoke the GUI version or
 * the command-line version of the Essence' translator. The GUI version 
 * is default. Starting the translator with the "help" argument gives 
 * some information about the usage. 
 * 
 * @author andrea
 *
 */

public class Translate {

	public static final String GUI_NAME = "TAILOR v0.2";
	public static final String EMPTY_PARAM_STRING = "ESSENCE' 1.0\n";

	/** Available flags */
	public static final String XCSP_CONVERSION = "xcsp";
	public static final String HELP = "help";
	public static final String OLD_GUI = "oldgui";
	public static final String NO_COMMON_SUBEXPRS = "no-cse";
	
	/**
	 * Start the GUI version or command-line version of the translator.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	
		if(args.length == 0) {
			printHelpMessage();
			runNewGUI();
			
		}
		
		else if(args.length == 1) {
			if(args[0].endsWith("help"))
				printHelpMessage();
			else if((args[0].endsWith("oldGui")) || (args[0].endsWith("oldGUI"))) 
				runOldGUIVersion();
			else translate(args[0]);
		}
			
		else if(args.length == 2) {
			if(args[1].endsWith(NO_COMMON_SUBEXPRS)) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseCommonSubExpressions(false);
				translate(args[0], settings);
			}
			else if(args[1].endsWith("no-ecse")) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseInferredCommonSubExpressions(false);
				translate(args[0], settings);
			}
			else if(args[1].endsWith("-dvr")) {
				TranslationSettings settings = new TranslationSettings();
				settings.setApplyDirectVariableReusage(true);
				translate(args[0], settings);
			}
			else if(args[0].endsWith(XCSP_CONVERSION)) {
				TranslationSettings settings = new TranslationSettings();
				translateXCSP(args[1], args[1]+".minion",settings);
				System.exit(0);
			}
			
			translate(args[0], args[1]);
		}
		
		else if(args.length == 3) {
			if(args[2].endsWith(NO_COMMON_SUBEXPRS)) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseCommonSubExpressions(false);
				translate(args[0], args[1], settings);
			}
			else if(args[2].endsWith("no-ecse")) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseInferredCommonSubExpressions(false);
				translate(args[0], args[1], settings);
			}
			else if(args[2].endsWith("-dvr")) {
				TranslationSettings settings = new TranslationSettings();
				settings.setApplyDirectVariableReusage(true);
				translate(args[0], args[1], settings);
			}
			else if(args[0].endsWith(XCSP_CONVERSION)) {
				TranslationSettings settings = new TranslationSettings();
				translateXCSP(args[1], args[2],settings);
			}
			else printHelpMessage();
		}

		else if(args.length == 4) {
			if(args[2].endsWith(NO_COMMON_SUBEXPRS) || args[3].endsWith(NO_COMMON_SUBEXPRS) &&
				(args[2].endsWith("no-ecse") || args[3].endsWith("no-ecse"))	) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseCommonSubExpressions(false);
				settings.setUseInferredCommonSubExpressions(false);
				translate(args[0], args[1], settings);
			}
			else if (args[2].endsWith(NO_COMMON_SUBEXPRS) || args[3].endsWith(NO_COMMON_SUBEXPRS) &&
				(args[2].endsWith("-dvr") || args[3].endsWith("-dvr"))	) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseCommonSubExpressions(false);
				settings.setApplyDirectVariableReusage(true);
				translate(args[0], args[1], settings);
			}
			else if (args[2].endsWith("no-ecse") || args[3].endsWith("no-ecse") &&
					(args[2].endsWith("-dvr") || args[3].endsWith("-dvr"))	) {
					TranslationSettings settings = new TranslationSettings();
					settings.setUseInferredCommonSubExpressions(false);
					settings.setApplyDirectVariableReusage(true);
					translate(args[0], args[1], settings);
				}
			
			else printHelpMessage();
		}
		
		else if(args.length == 5) {
			
			if((args[2].endsWith(NO_COMMON_SUBEXPRS) || args[3].endsWith(NO_COMMON_SUBEXPRS) || args[4].endsWith(NO_COMMON_SUBEXPRS) ) &&
			   (args[2].endsWith("no-ecse") || args[3].endsWith("no-ecse")  ||  args[4].endsWith("no-ecse") ) &&
			   (args[2].endsWith("-dvr") || args[3].endsWith("-dvr") || args[4].endsWith("-dvr")  )) {
				TranslationSettings settings = new TranslationSettings();
				settings.setUseCommonSubExpressions(false);
				settings.setUseInferredCommonSubExpressions(false);
				settings.setApplyDirectVariableReusage(true);
				translate(args[0], args[1], settings);
				
			}
			else printHelpMessage();
		}
		
		else printHelpMessage();
	}


	/**
	 * Starts the GUI version of the Essence' translator
	 *
	 */
	
	private static void runOldGUIVersion() {
		JFrame frame = new JFrame(GUI_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    //Add content to the window.
		frame.add(new translator.gui.TranslatorGUI());

	    //Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void runNewGUI() {
		
		JFrame frame = new TailorGUI();
		frame.setVisible(true);
	}
	
	private static void translate(String filename, TranslationSettings settings) {
		
		try {
			System.out.println("\nTranslating "+filename);
			long startTime = System.currentTimeMillis();
			
			String problemString = readStringFromFile(filename);
	    	Translator translator = new Translator(settings);
	    	translator.tailorTo(problemString,
	    			                EMPTY_PARAM_STRING,
	    			                new Minion()); 
			String minionString = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			
			double translationTime = (stopTime - startTime) / 1000.0;
			minionString = "# Translation Time: "+translationTime+"\n"+minionString;
			
			File outputFile = writeStringIntoFile(filename+".minion", minionString);
			System.out.println("Translated '"+filename+"' to Minion and written output\n into '"+outputFile.getAbsolutePath()+"'.\n");
			System.out.println("Translation Time: "+translationTime+" sec");
	    	
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from problemfile:"+filename);
			System.exit(1);
		}
		
		
		
	}
	
	
	/**
	 * Translate the problem file, specified by the filename, to a Minion
	 * instance and write it into a Minion file.
	 * 
	 * @param filename
	 */
	private static void translate(String filename) {
		
		try {
			System.out.println("\nTranslating "+filename);
			long startTime = System.currentTimeMillis();
			
			String problemString = readStringFromFile(filename);
	    	Translator translator = new Translator(new TranslationSettings());
	    	translator.tailorTo(problemString,
	    			                EMPTY_PARAM_STRING,
	    			                new Minion()); 
			String minionString = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			
			double translationTime = (stopTime - startTime) / 1000.0;
			minionString = "# Translation Time: "+translationTime+"\n"+minionString;
			
			File outputFile = writeStringIntoFile(filename+".minion", minionString);
			System.out.println("Translated '"+filename+"' to Minion and written output\n into '"+outputFile.getAbsolutePath()+"'.\n");
			System.out.println("Translation Time: "+translationTime+" sec");
	    	
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from problemfile:"+filename);
			System.exit(1);
		}
		
	}
	
	
	private static void translate(String problemFileName, String parameterFileName, TranslationSettings settings) {
		try {
			
			System.out.println("\nTranslating "+problemFileName+" with "+parameterFileName);
			long startTime = System.currentTimeMillis();
			
			
			String problemString = readStringFromFile(problemFileName);
			String parameterString = readStringFromFile(parameterFileName);
	    	Translator translator = new Translator(settings);
	    	
	    	translator.tailorTo(problemString,
	    			                parameterString,
	    			                new Minion()); 
			String minionString = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			double translationTime = (stopTime - startTime) / 1000.0;
			minionString = "# Translation Time: "+translationTime+"\n"+minionString;
			
			File outputFile = writeStringIntoFile(parameterFileName+".minion", minionString);
			System.out.println("Translated '"+problemFileName+"' and '"+parameterFileName+
					"' to Minion and written output\n into '"+outputFile.getName()+"'.\n");
			System.out.println("Translation Time: "+translationTime+" sec");
			
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from problem file '"+problemFileName+
					"' and parameter file '"+parameterFileName+"'.");
			System.exit(1);
		}
	}



	private static void translate(String problemFileName, String parameterFileName) {
		try {
			System.out.println("\nTranslating "+problemFileName+" with "+parameterFileName);
			long startTime = System.currentTimeMillis();
			
			
			String problemString = readStringFromFile(problemFileName);
			String parameterString = readStringFromFile(parameterFileName);
	    	Translator translator = new Translator(new TranslationSettings());
	    	
	    	translator.tailorTo(problemString,
	    			                parameterString,
	    			                new Minion()); 
			String minionString = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			double translationTime = (stopTime - startTime) / 1000.0;
			minionString = "# Translation Time: "+translationTime+"\n"+minionString;
			
			File outputFile = writeStringIntoFile(parameterFileName+".minion", minionString);
			System.out.println("Translated '"+problemFileName+"' and '"+parameterFileName+
					"' to Minion and written output\n into '"+outputFile.getAbsolutePath()+"'.\n");
			System.out.println("Translation Time: "+translationTime+" sec");
			
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from problem file '"+problemFileName+
					"' and parameter file '"+parameterFileName+"'.");
			System.exit(1);
		}
	}
	
	
	private static void translateXCSP(String inputFileName, String outputFileName, TranslationSettings settings) {
		
		try {
			System.out.println("\nTranslating XCSP file '"+inputFileName+"'");
			long startTime = System.currentTimeMillis();
			Xcsp2Ep xcspConverter = new Xcsp2Ep();
			Translator translator = new Translator(settings);
			
			translator.tailorTo(xcspConverter.translateToNormalisedModel(inputFileName), new Minion());
			String minionString = translator.getTargetSolverInstance();
			long stopTime = System.currentTimeMillis();
			
			double translationTime = (stopTime - startTime) / 1000.0;
			minionString = "# Translation Time: "+translationTime+"\n"+minionString;
			
			File outputFile = writeStringIntoFile(outputFileName, minionString);
			System.out.println("Translated '"+inputFileName+
					"' to Minion and written output\n into '"+outputFile.getAbsolutePath()+"'.\n");
			System.out.println("Translation Time: "+translationTime+" sec");
			
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from XCSP file '"+inputFileName);
			System.exit(1);
		}
	}
	
	private static void printHelpMessage() {
		System.out.println("\nUsage: java -jar tailor.jar [options]");
		System.out.println("If no options are given, the graphical translator version is started.\n");
		System.out.println("Available options are:");
		System.out.println("filename.eprime");
		System.out.println("\tEssence' problem specification: Translates 'filename.eprime' to Minion input format");
		System.out.println("\tThe generated Minion file is written into 'filename.eprime.minion'\n");
		System.out.println("filename.eprime filename.param\n\tEssence' problem and parameter specification:");
		System.out.println("\tTranslates 'filename.eprime' with respect to the parameters given in"); 
		System.out.println("\t'filename.param' to Minion input format.");
		System.out.println("\tThe generated Minion file is written into 'filename.param.minion'\n");
		System.out.println("-xcsp filename.xml");
		System.out.println("\tTranslates the XCSP file 'filename.xml' to Minion input format.");
		System.out.println("\tThe generated Minion file is written into 'filename.xml.minion'\n");
		System.out.println("-xcsp input.xml output.minion");
		System.out.println("\tTranslates the XCSP file 'input.xml' to Minion input format.");
		System.out.println("\tThe generated Minion file is written into 'output.minion'\n");
		System.out.println("help or -help\n\tprints this help message");
		System.out.println("-no-cse");
		System.out.println("\tTurn off reusing equivalent auxiliary variables (exploiting common subexpressions).");
		System.out.println("\tDefault: on");
		System.out.println("-no-ecse");
		System.out.println("\tTurn off inferring equivalent expressions (which saves auxiliary variables)");
		System.out.println("\tDefault: on");
		System.out.println("-dvr");
		System.out.println("\tTranslate with directly reusing variables (e.g. in x=y, replacing x with y).");
		System.out.println("\tDefault: off (not stable yet)");
	}
	
	
	private static String readStringFromFile(String filename) 
		throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
    	StringBuffer loadedString = new StringBuffer("");
    	String s = reader.readLine();
    	while(s != null && !(s.equalsIgnoreCase("null"))) {
    		loadedString.append(s+"\n");
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
}
