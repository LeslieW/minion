package translator;

import translator.solver.TargetSolver;
import translator.solver.Minion;
import java.io.*;

/**
 * This class contains the settings for a translation. The definitions in 
 * the constructor stand for the default settings and can be changed 
 * during runtime using the GUI.
 * 
 * @author andrea
 *
 */


public class TranslationSettings {

	TargetSolver targetSolver;
	boolean useCommonSubExpressions;
	boolean useEqualSubExpressions;
	boolean applyStrictCopyPropagation;
	boolean useExplicitCommonSubExpressions;
	boolean applyDirectVariableReusage;
	boolean giveTranslationTimeInfo;
	boolean  giveTranslationInfo;
	String pathToMinion;
	// the variables whose solutions have been printed by the target solver
	String[] printedVariables;
	
	String settingsFileName;
	
	
	// DEFAULT settings for every translation
	public TranslationSettings() {
		this.settingsFileName = "settings";
		this.targetSolver = new Minion();
		this.useCommonSubExpressions = true;
		this.useEqualSubExpressions =true;
		this.useExplicitCommonSubExpressions = true;
		this.applyStrictCopyPropagation = false;
		this.giveTranslationTimeInfo = true;
		this.giveTranslationInfo = true;
		this.applyDirectVariableReusage = true;
		this.pathToMinion = readPathToMinion();
	}
	
	
	public String readPathToMinion() {
		
		 try {
			 File file = new File(this.settingsFileName);	 
			 String loadedString = "";
			 BufferedReader reader = null;
			 if(file.createNewFile()) {
				 String minionDirectory = System.getProperty("user.dir");
				 // currentDirectory - translator =>  minion directory
				 minionDirectory = minionDirectory.substring(0, minionDirectory.length()-10);
				 this.pathToMinion = minionDirectory+"bin/minion";;
			 }
			 else {
				 reader = new BufferedReader(new FileReader(file));
				 loadedString = reader.readLine();
				 if(loadedString == null || loadedString.equals("")) {
					 String minionDirectory = System.getProperty("user.dir");
					 // currentDirectory - translator =>  minion directory
					 minionDirectory = minionDirectory.substring(0, minionDirectory.length()-10);
					 this.pathToMinion = minionDirectory+"bin/minion";
				 }
				 else this.pathToMinion = loadedString;
			 }
		    	
		    	
		    
		    	if(reader != null)
		    		reader.close();
		    	return this.pathToMinion;
		    	
		    } catch(Exception e) {
		    	e.printStackTrace(System.out);
		    	System.out.println("Cannot open settings file '"+this.settingsFileName+"'.\n");
		    	System.exit(1);
		    	return "";
		    }
		
		
		
	}
	
	
	public void writeNewPath(String newPath) {
		
		try {
		File file = new File(this.settingsFileName);
		
		FileWriter writer = new FileWriter(file);
        
		if(!file.canRead())
			System.out.println("Cannot read file:\n "+file.toString()+"\n");
		else if(!file.canWrite())
			System.out.println("Cannot write file:\n "+file.toString()+"\n");
	
        writer.write(newPath+"\n");
        writer.close();
        
		} catch(Exception e) {
	    	e.printStackTrace(System.out);
	    	System.out.println("Cannot write new path into settings file '"+this.settingsFileName+"'.\n");
	    	System.exit(1);
	    }
        
	}
	
	public TargetSolver getTargetSolver() {
		return this.targetSolver;
	}
	
	public boolean useCommonSubExpressions() {
		return this.useCommonSubExpressions;
	}
	
	public boolean useExplicitCommonSubExpressions() {
		return this.useExplicitCommonSubExpressions;
	}
	
	public boolean useEqualCommonSubExpressions() {
		return this.useEqualSubExpressions;
	}
	
	public boolean giveTranslationTimeInfo() {
		return this.giveTranslationTimeInfo;
	}
	
	public boolean applyDirectVariableReusage() {
		return this.applyDirectVariableReusage;
	}
	
	public void setTargetSolver(TargetSolver solver) {
		this.targetSolver = solver;
	}
	
	public void setUseCommonSubExpressions(boolean turnOn) {
		this.useCommonSubExpressions = turnOn;
	}
	
	public void setUseExplicitCommonSubExpressions(boolean turnOn) {
		this.useExplicitCommonSubExpressions = turnOn;
	}
	
	public void setUseInferredCommonSubExpressions(boolean turnOn) {
		this.useEqualSubExpressions = turnOn;
	}
	
	public void setGiveTranslationTimeInfo(boolean turnOn) {
		this.giveTranslationTimeInfo = turnOn;
	}
	
	public void setApplyDirectVariableReusage(boolean turnOn) {
		this.applyDirectVariableReusage = turnOn;
	}
	
	public String getPathToMinion() {
		return this.pathToMinion;
	}
	
	public void setPathToMinion(String path) {
		this.pathToMinion = path;
		writeNewPath(path);
	}
	
	public boolean applyStrictCopyPropagation() {
		return this.applyStrictCopyPropagation;
	}
	
	public void setApplyStrictCopyPropagation(boolean turnOn) {
		this.applyStrictCopyPropagation = turnOn;
	}
	
	public void setPrintedVariables(String[] printedVariables) {
		this.printedVariables = printedVariables;
	}
	
	public String[] getPrintedVariables() {
		return this.printedVariables;
	}
}
