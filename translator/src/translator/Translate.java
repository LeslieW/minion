package translator;

import javax.swing.JFrame;
import java.io.*;
import translator.solver.Minion;

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

	public static final String GUI_NAME = "TAILOR v0.1";
	public static final String EMPTY_PARAM_STRING = "Essence' 1.0\n";
	
	/**
	 * Start the GUI version or command-line version of the translator.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	
		if(args.length == 0) {
			printHelpMessage();
			startGuiVersion();
		}

		else if(args.length == 1) {
	                if(args[0].endsWith("help"))
			     printHelpMessage();
			else translate(args[0]);
		}
		else if(args.length == 2)
			translate(args[0], args[1]);
		
		
		else printHelpMessage();
	}


	/**
	 * Starts the GUI version of the Essence' translator
	 *
	 */
	
	private static void startGuiVersion() {
		JFrame frame = new JFrame(GUI_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    //Add content to the window.
		frame.add(new translator.gui.TranslatorGUI());

	    //Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	
	/**
	 * Translate the problem file, specified by the filename, to a Minion
	 * instance and write it into a Minion file.
	 * 
	 * @param filename
	 */
	private static void translate(String filename) {
		
		try {
			
			String problemString = readStringFromFile(filename);
	    	Translator translator = new Translator(new TranslationSettings());
	    	translator.tailorTo(problemString,
	    			                EMPTY_PARAM_STRING,
	    			                new Minion()); 
			String minionString = translator.getTargetSolverInstance();
			File outputFile = writeStringIntoFile(filename+".minion", minionString);
			System.out.println("Translated '"+filename+"' to Minion and written output into '"+outputFile.getAbsolutePath()+"'.\n");
	    	
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from problemfile:"+filename);
			System.exit(1);
		}
		
	}
	
	
	private static void translate(String problemFileName, String parameterFileName) {
		try {
			String problemString = readStringFromFile(problemFileName);
			String parameterString = readStringFromFile(parameterFileName);
	    	Translator translator = new Translator(new TranslationSettings());
	    	
	    	translator.tailorTo(problemString,
	    			                parameterString,
	    			                new Minion()); 
			String minionString = translator.getTargetSolverInstance();
			
			File outputFile = writeStringIntoFile(parameterFileName+".minion", minionString);
			System.out.println("Translated '"+problemFileName+"' and '"+parameterFileName+
					"' to Minion and written output into '"+outputFile.getAbsolutePath()+"'.\n");
	    	
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("Cannot translate to Minion from problem file '"+problemFileName+
					"' and parameter file '"+parameterFileName+"'.");
			System.exit(1);
		}
	}
	
	
	private static void printHelpMessage() {
		System.out.println("Usage: java -jar translator.jar [options]");
		System.out.println("If no options are given, the graphical translator version is started.\n");
		System.out.println("Available options are:");
		System.out.println("help or -help");
		System.out.println("\tprints this help message");
		System.out.println("filename.eprime");
		System.out.println("\tEssence' problem specification: Translates 'filename.eprime' to Minion input format");
		System.out.println("filename.eprime filename.param\n\tEssence' problem and parameter specification:");
		System.out.println("\tTranslates 'filename.eprime' with respect to the parameters given in"); 
		System.out.println("\t'filename.param' to Minion input format.");
	}
	
	
	private static String readStringFromFile(String filename) 
		throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
    	String loadedString = "";
    	String s = reader.readLine();
    	while(s != null && !(s.equalsIgnoreCase("null"))) {
    		loadedString = loadedString.concat(s+"\n");
    		s = reader.readLine();
    	}
    	reader.close();
    	return loadedString;
		
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
