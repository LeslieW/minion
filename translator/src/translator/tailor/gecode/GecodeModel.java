package translator.tailor.gecode;

import java.util.ArrayList;
import java.util.HashMap;

import translator.expression.Domain;
import translator.TranslationSettings;

/**
 * This class represents a Gecode Model
 * it inherits from Example
 * 
 * @author andrea
 *
 */

public class GecodeModel {

	private ArrayList<GecodeVariable> variableList;
	private ArrayList<ArgsVariable> bufferArrays;
	private HashMap<GecodeVariable, Domain> variableDomains = new HashMap<GecodeVariable, Domain>();
	private ArrayList<GecodeConstraint> constraints;
	private TranslationSettings settings;
	
	private String modelName;
	
	
	
	public GecodeModel(TranslationSettings settings,
					   ArrayList<GecodeVariable> variables,
					   ArrayList<ArgsVariable> bufferArrays,
					   ArrayList<GecodeConstraint> constraints) {
		
		this.settings = settings;
		this.variableList = variables;
		this.bufferArrays = bufferArrays;
		this.constraints = constraints;
		this.modelName = settings.getModelName();
	}
	
	
	// ================ METHODS ==============================
	
	public ArrayList<GecodeVariable> getDecisionVariables() {
		return this.variableList;
	}
	
	public Domain getDomainOfVariable(GecodeVariable variable) 
		throws GecodeException {
	
		Domain domain =  this.variableDomains.get(variable);
		if(domain == null) 
				throw new GecodeException("Internal error. Trying to get domain of unknown variable:"+variable);
		return domain;
	}
	
	
	// ============== PRINT METHODS ==========================
	
	/**
	 * Print the Gecode Model
	 * 
	 */
	public String toString() {
		
		StringBuffer s = new StringBuffer("#include \"examples/support.hh\"\n#include \"gecode/minimodel.hh\"\n\n");
	
		s.append("\n\nConstraints: "+this.constraints);
		s.append("\n\nVariables: "+this.variableList);
		
		// TODO:!!
		return s.toString();
	}
	
	/**
	 * Print the Gecode C++ Model
	 * 
	 */
	public String toCCString() {
		
		StringBuffer s = new StringBuffer("#include \"examples/support.hh\"\n#include \"gecode/minimodel.hh\"\n\n");
		
		// file header
		s.append("/** \n *  "+this.settings.OUTPUTFILE_HEADER+"\n *  "+this.settings.OUTPUTFILE_HEADER_BUGS+"\n */\n\n");
		
		s.append(modelClassToString()+"\n\n");
		s.append(mainFunctionToString());
		
		return s.toString();
	}
	
	private String mainFunctionToString() {
		
		StringBuffer s = new StringBuffer("int\nmain(int argc, char* argv[]) {\n");
		s.append("   Options opt(\""+modelName+"\");\n");
		s.append("   opt.solutions(0);\n");
		s.append("   opt.iterations(2000);\n");
		s.append("   opt.parse(argc, argv);\n");
		s.append("   Example::run<"+modelName+", DFS, Options>(opt);\n");
		s.append("   return 0;\n}\n\n");
		
		return s.toString();
	}
	
	
	private String modelClassToString() {
		
		StringBuffer s = new StringBuffer("class "+this.modelName+" : public Example {\n\n");
		s.append("protected:   // variables:\n");
		s.append(variableDeclarationToString()+"\n");
		
		s.append("public: \n\n");
		s.append(actualProblemConstructorToString()+"\n");
		
		// TODO: print, clone and copy
		s.append(copyMethodToString());
		
		s.append("\n};\n");
		return s.toString();
	}
	
	
	private String variableDeclarationToString() {
		
		StringBuffer s = new StringBuffer("");
		for(int i=0; i<this.variableList.size(); i++)
			s.append("   "+variableList.get(i).toCCString()+";\n");
		
		return s.toString();
	}
	
	
	private String copyMethodToString() {
		
		StringBuffer s = new StringBuffer("   // copy during cloning\n   vitual Space*\n   copy(bool share) {\n");
		s.append("      return new "+modelName+"(share, *this);\n");
		s.append("   }\n");
		
		return s.toString();
		
	}
	
	private String actualProblemConstructorToString() {
		
		StringBuffer s = new StringBuffer("   // actual problem\n   "+this.modelName+"(const Options& opt) : "+
				variableInitialisationToString());
		
		s.append(" {\n");
		s.append(bufferArraysDeclaration());
		s.append(constraintsToString()+"\n");
		s.append(branchingToString()+"\n");
		
		s.append("   }\n\n");
		return s.toString();
	}
	
	private String bufferArraysDeclaration() {
		
		StringBuffer s = new StringBuffer("");
		for(int i=0; i<this.bufferArrays.size(); i++) {
			// Under constructioN!!
			s.append("      "+bufferArrays.get(i).toCCString()+";\n");
		}
		
		return s.toString();
		
	}
	
	private String branchingToString() {
		// TODO:
		return "      branching...";
	}
	
	
	private String variableInitialisationToString() {
		// TODO:
		return "variable-Initialisation";
	}
	
	private String constraintsToString() {
		StringBuffer s = new StringBuffer("");
		for(int i=0; i<this.constraints.size(); i++) {
			s.append("      "+constraints.get(i)+";\n");
		}
		return s.toString();
	}
	

}
