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
	boolean applyStrictCopyPropagation;
	String pathToMinion;
	// the variables whose solutions have been printed by the target solver
	String[] printedVariables;
	
	String settingsFileName;
	
	public TranslationSettings() {
		this.settingsFileName = "settings";
		this.targetSolver = new Minion();
		this.useCommonSubExpressions = true;
		this.applyStrictCopyPropagation = false;
		this.pathToMinion = readPathToMinion();
	}
	
	
	public String readPathToMinion() {
		
		File file = new File(this.settingsFileName);
		
		 try {
		    	BufferedReader reader = new BufferedReader(new FileReader(file));
		    	String loadedString = reader.readLine();
		    	if(loadedString == null || loadedString.equals(""))
		    		this.pathToMinion = System.getProperty("user.home")+"/minion/bin/minion";;
		    	this.pathToMinion = loadedString;
		    	
		    	reader.close();
		    	return loadedString;
		    	
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
	
	public void setTargetSolver(TargetSolver solver) {
		this.targetSolver = solver;
	}
	
	public void setUseCommonSubExpressions(boolean turnOn) {
		this.useCommonSubExpressions = turnOn;
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
