package translator.tailor.gecode;

import java.util.ArrayList;
import java.util.HashMap;

import translator.expression.Domain;
import translator.TranslationSettings;
import translator.solver.Gecode;

/**
 * This class represents a Gecode Model
 * it inherits from Example
 * 
 * @author andrea
 *
 */

public class GecodeModel {

	/** list of 1-dim. variable arrays */
	private ArrayList<GecodeArrayVariable> variableList;
	/** arrays that buffer single vars */
	private ArrayList<GecodeArrayVariable> bufferArrays;
	/** buffered single int variables*/
	private ArrayList<GecodeIntVar> singleIntVarNames;
	/** buffered single bool variables*/
	private ArrayList<GecodeBoolVar> singleBoolVarNames;
	private HashMap<GecodeVariable, Domain> variableDomains = new HashMap<GecodeVariable, Domain>();
	private ArrayList<GecodeConstraint> constraints;
	private TranslationSettings settings;
	
	private String modelName;
	
	
	
	public GecodeModel(TranslationSettings settings,
					   ArrayList<GecodeArrayVariable> variables,
					   ArrayList<GecodeArrayVariable> bufferArrays,
					   ArrayList<GecodeConstraint> constraints, 
					   ArrayList<GecodeIntVar> singleIntVars, 
					   ArrayList<GecodeBoolVar> singleBoolVars) {
		
		this.settings = settings;
		this.variableList = variables;
		this.bufferArrays = bufferArrays;
		this.constraints = constraints;
		
		this.modelName = settings.getModelName();
		String firstLetter = modelName.substring(0, 1);
		modelName = modelName.substring(1,modelName.length());
		firstLetter = firstLetter.toUpperCase();
		modelName = firstLetter+modelName; // make the name uppercase
		
		this.singleIntVarNames = singleIntVars;
		this.singleBoolVarNames = singleBoolVars;
	}
	
	
	// ================ METHODS ==============================
	
	public ArrayList<GecodeArrayVariable> getDecisionVariables() {
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
	public String toSimpleString() {
		
		StringBuffer s = new StringBuffer("#include \"examples/support.hh\"\n#include \"gecode/minimodel.hh\"\n\n");
	
		s.append("\n\nConstraints: "+this.constraints);
		s.append("\n\n1-dim. array-Variables: "+this.variableList);
		s.append("\n\nBuffered variables: "+this.bufferArrays);
		
		// TODO:!!
		return s.toString();
	}
	
	/**
	 * Print the Gecode C++ Model
	 * 
	 */
	public String toString() {
		
		StringBuffer s = new StringBuffer("/** \n *  "+this.settings.OUTPUTFILE_HEADER+
				                         "\n *  "+this.settings.OUTPUTFILE_HEADER_BUGS+"\n */\n\n");
		// s.append(printStatistics()); TODO!!
		
		s.append("#include \"examples/support.hh\"\n#include \"gecode/minimodel.hh\"\n\n");
		
		// file header
		//s.append("/** \n *  "+this.settings.OUTPUTFILE_HEADER+"\n *  "+this.settings.OUTPUTFILE_HEADER_BUGS+"\n */\n\n");
		
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
		
		s.append(printingMethodToString()+"\n\n");
		
		s.append(copyMethodToString());
		
		s.append("\n\n");
		s.append(cloningConstructorToString());
		
		s.append("\n};\n");
		return s.toString();
	}
	
	
	private String actualProblemConstructorToString() {
		
		StringBuffer s = new StringBuffer("   // actual problem\n   "+this.modelName+"(const Options& opt) : "+
				variableInitialisationToString());
		
		s.append(" {\n\n");
		s.append(bufferArraysDeclaration()+"\n");
		s.append(constraintsToString()+"\n");
		s.append(branchingToString()+"\n");
		
		s.append("   }\n\n");
		return s.toString();
	}
	
	
	//------------------------------------------------------------------
	
	/**
	 * Writes down the variable declaration of the variables
	 * @return
	 */
	private String variableDeclarationToString() {
		
		StringBuffer s = new StringBuffer("");
		for(int i=0; i<this.variableList.size(); i++)
			s.append("   "+variableList.get(i).toDeclarationCCString()+";\n");
		
		for(int i=0; i<this.bufferArrays.size(); i++)
			s.append("   "+bufferArrays.get(i).toDeclarationCCString()+";\n");
		
		return s.toString();
	}
	
	
	private String copyMethodToString() {
		
		StringBuffer s = new StringBuffer("   // copy during cloning\n   virtual Space*\n   copy(bool share) {\n");
		s.append("\treturn new "+modelName+"(share, *this);\n");
		s.append("   }\n");
		
		return s.toString();
		
	}
	
	/**
	 * Returns the cloning constructor of the problem model where 
	 * every variable array is updated.
	 * @return
	 */
	private String cloningConstructorToString() {
		StringBuffer s = new StringBuffer("   // constructor for cloning\n   "
				+modelName+"(bool share, "+modelName+"& s) : Example(share,s) {\n");
		
		for(int i=0; i<this.bufferArrays.size(); i++) {
			s.append("\t"+bufferArrays.get(i)+".update(this, share, s."+bufferArrays.get(i)+");\n");
		}
		
		s.append("\n   }\n");
		
		return s.toString();
		
	}
	
	/**
	 * Method that prints the results/solutions of the variables
	 * @return
	 */
	private String printingMethodToString() {
		StringBuffer s = new StringBuffer("   // method for printing solutions\n");
		s.append("   virtual void print(void) {\n\tstd::cout << \"\\t\";\n");
		
		for(int i=0; i<this.variableList.size(); i++) {
			GecodeArrayVariable var = variableList.get(i);
			s.append("\tstd::cout  << \" "+var.getVariableName()+":\" << std::endl;\n");
			s.append("\tfor(int i=0; i<"+var.getLength()+", i++) \n");
			s.append("\t   std::cout  << "+var.getVariableName()+"[i] << \"  \";");
			s.append("\tstd::cout << std::endl;\n\n");
		}
		
		s.append("\n");
		
		for(int i=0; i<this.bufferArrays.size(); i++) {
			GecodeArrayVariable var = bufferArrays.get(i);
			boolean isInteger = (var instanceof GecodeIntVarArray);
			
			for(int j=0; j<var.getLength(); j++) {
				if(isInteger) {
					s.append("\tstd::cout  << \" "+this.singleIntVarNames.get(j).getVariableName()+":\"");
				}
				else s.append("\tstd::cout  << \" "+this.singleBoolVarNames.get(j).getVariableName()+":\"");
				
				s.append(" << "+var.getVariableName()+"["+j+"] << std::endl;\n");
			}
		}
		
		s.append("   }\n");
		
		return s.toString();
	}

	private String bufferArraysDeclaration() {
		
		StringBuffer s = new StringBuffer("");
		
		if(bufferArrays.size() > 0) {
			s.append("      // mapping single variables to the array\n");
		}
		
		for(int i=0; i<this.bufferArrays.size(); i++) {
			GecodeArrayVariable array = bufferArrays.get(i);
			
			if(array instanceof GecodeIntVarArray) {
				String arrayName = array.getVariableName();
				s.append("\tIntVar");
				for(int j=0; j<array.getLength(); j++) {
					if(j>0) s.append(", ");
					if(j%5==0) s.append("\n\t ");
					s.append(this.singleIntVarNames.get(j).getVariableName()+"("+arrayName+"["+j+"])");
				}				
			}
			else if(array instanceof GecodeBoolVarArray) {
				String arrayName = array.getVariableName();
				s.append("\tBoolVar");
				for(int j=0; j<array.getLength(); j++) {
					if(j>0) s.append(", ");
					if(j%5==0) s.append("\n\t ");
					s.append(this.singleBoolVarNames.get(j).getVariableName()+"("+arrayName+"["+j+"])");
				}	
			}
			s.append(";\n\n");
		}
		
		return s.toString();
		
	}
	
	/**
	 * Produce the branching strategy for the variables
	 * @return
	 */
	private String branchingToString() {
		
		StringBuffer s = new StringBuffer("");
	
		String variableBranching = ((Gecode) this.settings.getTargetSolver()).toGecodeVariableBranching(settings.getVarBranching());
		String valueBranching = ((Gecode) this.settings.getTargetSolver()).toGecodeValueBranching(settings.getValBranching());
		
		for(int i=0; i<this.bufferArrays.size(); i++) {
			s.append("\tbranch(this, "+bufferArrays.get(i)+", "+variableBranching+", "+valueBranching+");\n");
		}
		return s.toString();
	}
	
	/**
	 * Initialising the variable in the constructor
	 * @return
	 */
	private String variableInitialisationToString() {
		StringBuffer s= new StringBuffer("");
		for(int i=0; i<this.variableList.size(); i++) {
			if(i > 0) s.append(",\n");
			GecodeArrayVariable var = variableList.get(i);
			s.append("\t"+var+"(this, "+var.getLength()+", "+var.getLowerBound()+", "+var.getUpperBound()+")");
		}
		for(int i=0; i<this.bufferArrays.size(); i++) {
			if(i > 0) s.append(",\n");
			GecodeArrayVariable var = bufferArrays.get(i);
			s.append("\t"+var+"(this, "+var.getLength()+", "+var.getLowerBound()+", "+var.getUpperBound()+")");
		}
		return s.toString();
	}
	
	/**
	 * Printing the constraints
	 * @return
	 */
	private String constraintsToString() {
		StringBuffer s = new StringBuffer("");
		for(int i=0; i<this.constraints.size(); i++) {
			s.append("\t"+constraints.get(i)+";\n");
		}
		return s.toString();
	}
	

}
