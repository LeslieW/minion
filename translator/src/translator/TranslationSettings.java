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

	
	public final String OUTPUTFILE_HEADER = "This file has been automatically generated by TAILOR v"+Translate.VERSION;
	public final String OUTPUTFILE_HEADER_BUGS = "bug-reports: andrea@cs.st-and.ac.uk";
	public final String DEFAULT_MODEL_NAME = "ProblemModel";
	public final int DISCRETE_UPPER_BOUND = 200;
	
	// solving issues
	public final int FIND_ALL_SOLUTIONS = 0;
	public final int DEFAULT_NO_OF_SOLUTIONS = 1;
	public final boolean BRANCH_OVER_AUX_VARIABLES = true;
	
	int discreteUpperBound;
	TargetSolver targetSolver;
	String variableBranching = TargetSolver.DEFAULT_VAR_BRANCHING;
	String valueBranching = TargetSolver.DEFAULT_VAL_BRANCHING;
	boolean branchOverAuxVariables;
	
	// translation issues
	boolean useCommonSubExpressions;
	boolean useEqualSubExpressions;
	boolean applyStrictCopyPropagation;
	boolean useExplicitCommonSubExpressions;
	boolean applyDirectVariableReusage;
	boolean giveTranslationTimeInfo;
	boolean  giveTranslationInfo;
	boolean debugMode; // print stack trace when exceptions are thrown etc
	boolean writeTimeInfoIntoFile;
	boolean writeEssencePIntoFile; // for xcsp conversion
	boolean propagateSingleIntRanges;
	
	// if set to true, then parse errors, such as m[5][4] are accepted and recovered from
	boolean allowParseErrorRecovery;
	int noOfSolutions;
	
	String pathToMinion;
	// the variables whose solutions have been printed by the target solver
	String[] printedVariables;
	
	String settingsFileName;
	String essenceP_outputFileName;
	String solverInputFileName;
	String modelName;
	

	
	// DEFAULT settings for every translation
	public TranslationSettings() {
		this.targetSolver = new Minion();
		initialiseSettings();
	}
	
	public TranslationSettings(TargetSolver solver) {	
		this.targetSolver = solver;
		initialiseSettings();
	}

	
	private void initialiseSettings() {
		this.settingsFileName = "settings";
		this.targetSolver = new Minion();
		this.useCommonSubExpressions = true;
		this.useEqualSubExpressions =true;
		this.useExplicitCommonSubExpressions = true;
		this.applyStrictCopyPropagation = false;
		this.giveTranslationTimeInfo = false;
		this.giveTranslationInfo = true;
		this.applyDirectVariableReusage = false;
		this.pathToMinion = readPathToMinion();
		this.essenceP_outputFileName = "out.eprime";
		this.modelName = DEFAULT_MODEL_NAME;
		this.debugMode = false;
		this.writeTimeInfoIntoFile = true;
		this.writeEssencePIntoFile = false;
		this.propagateSingleIntRanges = true;
		this.discreteUpperBound = this.DISCRETE_UPPER_BOUND;
		this.allowParseErrorRecovery = false;
		this.noOfSolutions = this.DEFAULT_NO_OF_SOLUTIONS;
		this.solverInputFileName = "out."+targetSolver.getSolverInputExtension();
		this.branchOverAuxVariables = this.BRANCH_OVER_AUX_VARIABLES;
	}
	
	
	public String readPathToMinion() {
		
		 try {
			 File file = new File(this.settingsFileName);	 
			 String loadedString = "";
			 BufferedReader reader = null;
			 if(file.createNewFile()) {
				 String minionDirectory = System.getProperty("user.dir");
				 // currentDirectory - translator =>  minion directory
				 if(minionDirectory.length() > 10)
					 minionDirectory = minionDirectory.substring(0, minionDirectory.length()-10);
				 this.pathToMinion = minionDirectory+"bin/minion";;
			 }
			 else {
				 reader = new BufferedReader(new FileReader(file));
				 loadedString = reader.readLine();
				 if(loadedString == null || loadedString.equals("")) {
					 String minionDirectory = System.getProperty("user.dir");
					 // currentDirectory - translator =>  minion directory
					 if(minionDirectory.length() > 10)
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
	
	public String getEssencePrimeOutputFileName() {
		return this.essenceP_outputFileName;
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
	
	public boolean giveTranslationInfo() {
		return this.giveTranslationInfo;
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
	
	public String getModelName() {
		return this.modelName;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public boolean getPrintTranslationTimeIntoFile() {
		return this.writeTimeInfoIntoFile;
	}
	
	public void setPrintTranslationTimeIntoFile(boolean turnOn) {
		this.writeTimeInfoIntoFile = turnOn;
	}
	
	public boolean getWriteEssencePrimeModelIntoFile() {
		return this.writeEssencePIntoFile;
	}
	
	public void setWriteEssencePrimeModelIntoFile(boolean turnOn) {
		this.writeEssencePIntoFile = turnOn;
	}
	
	public boolean getPropagateSingleIntRanges() {
		return this.propagateSingleIntRanges;
	}
	
	public void setPropagateSingleIntRanges(boolean turnOn) {
		this.propagateSingleIntRanges = turnOn;
	}
	
	public int getDiscreteUpperBound() {
		return this.discreteUpperBound;
	}
	
	public void setDiscreteUpperBound(int newUb) {
		if(newUb < 0)
			this.discreteUpperBound = -newUb;
		this.discreteUpperBound = newUb;
	}
	
	public void setSolverInputFileName(String filename) {
		this.solverInputFileName = filename;
	}
	
	public String getSolverInputFileName() {
		return this.solverInputFileName;
	}
	
	public void setAllowParseErrorRecovery(boolean turnOn) {
		this.allowParseErrorRecovery = turnOn;
	}
	
	public boolean getAllowParseErrorRecovery() {
		return this.allowParseErrorRecovery;
	}
	
	public void setNoOfSolutions(int noOfSols) {
		this.noOfSolutions = noOfSols;
	}
	
	public int getNoOfSolutions() {
		return this.noOfSolutions;
	}
	
	public int getFindAllSolutionsAlias() {
		return this.FIND_ALL_SOLUTIONS;
	}
	
	public String getVarBranching() {
		return this.variableBranching;
	}
	
	public String getValBranching() {
		return this.valueBranching;
	}
	
	public void setVarBranching(String varBranching) {
		if(this.targetSolver.supportsVarBranchingStrategy(varBranching))
			this.variableBranching = varBranching;
	}
	
	public void setValBranching(String valBranching) {
		if(this.targetSolver.supportsValBranchingStrategy(valBranching))
			this.valueBranching = valBranching;
	}
	
	public boolean getBranchOverAuxVariables() {
		return this.branchOverAuxVariables;
	}
	
	public void setBranchOverAuxVariables(boolean turnOn) {
		this.branchOverAuxVariables = turnOn;
	}
}
