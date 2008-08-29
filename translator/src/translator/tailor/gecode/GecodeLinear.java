package translator.tailor.gecode;

public class GecodeLinear extends RelationalConstraint {

	private int randomMaximum = 111;
	
	private ArgsArrayVariable variableArray;
	private GecodeAtom[] variables;
	private int[] weights;
	private String argVarName;
	private ConstantArgsArray constantArray;
	private char relation;
	private GecodeAtom result;
	
	/** 
	 * Case: sum( Xi )  ~r  c  (linear sum) 
	 * where X is a int/Boolean variable array, r is a relation and 
	 * c is an integer value
	 * 
	 * @param variableArray X
	 * @param relation r
	 * @param constValue c
	 */
	public GecodeLinear(ArgsArrayVariable variableArray,
					    char relation,
					    GecodeConstant constValue) {
		
		this.variableArray = variableArray;
		this.relation = relation;
		this.result = constValue;
		this.argVarName = variableArray.getVariableName();
		
		this.isReifiable = true;
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM };
		
	}
	
	public GecodeLinear(GecodeAtom[] variables,
		    char relation,
		    GecodeConstant constValue) {

		this.variables = variables;
		this.relation = relation;
		this.result = constValue;
		
		this.argVarName = this.computeArgsArrayName();
		
		this.isReifiable = true;
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
										  GecodeConstraint.ICL_DOM };

	}
	
	
	/**
	 * Case: sum( Xi )  ~r  y  (linear sum) 
	 * where X is a int/Boolean variable array, r is a relation and 
	 * y is an integer variable
	 * 
	 * @param variableArray
	 * @param relation
	 * @param variable
	 */
	public GecodeLinear(ArgsArrayVariable variableArray,
						char relation,
						GecodeAtom variable) {
		
		this.variableArray = variableArray;
		this.relation = relation;
		this.result = variable;
		this.argVarName = this.variableArray.getVariableName();
		
		this.isReifiable = true;
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
				  									  GecodeConstraint.ICL_DOM };
	}
	
	public GecodeLinear(GecodeAtom[] variables,
		    		    char relation,
		    		    GecodeAtom variable) {

		this.variables = variables;
		this.relation = relation;
		this.result = variable;
		this.argVarName = this.computeArgsArrayName();
		
		this.isReifiable = true;
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
										  GecodeConstraint.ICL_DOM };

	}
	
	/**
	 * Case: sum( Xi*Ai )  ~r  c  (linear weighted  sum) 
	 * where X is a int/Boolean variable array, A is a constant integer array, 
	 * r is a relation and 
	 * c is an integer value
	 * 
	 * 
	 * @param variableArray
	 * @param weights
	 * @param relation
	 * @param constValue
	 */
	public GecodeLinear(ArgsArrayVariable variableArray, 
						ConstantArgsArray weights,
						char relation,
						GecodeConstant constValue) {
		
		this.variableArray = variableArray;
		this.constantArray = weights;
		this.relation = relation;
		this.result = constValue;
	}
	
	public GecodeLinear(GecodeAtom[] variables, 
						int[] weights,
						char relation,
						GecodeConstant constValue) {

		this.variables = variables;
		this.weights= weights;
		this.relation = relation;
		this.result = constValue;
		this.argVarName = computeArgsArrayName();
	}
	
	
	/**
	 * Case: sum( Xi*Ai )  ~r  y  (linear weighted  sum) 
	 * where X is an integer/Boolean variable array, A is a constant integer array, 
	 * r is a relation and 
	 * y is an integer value
	 * 
	 * @param variableArray
	 * @param weights
	 * @param relation
	 * @param resultVariable
	 */
	public GecodeLinear(ArgsArrayVariable variableArray,
						ConstantArgsArray weights,
						char relation,
						GecodeIntVar resultVariable) {
		
		this.variableArray= variableArray;
		this.constantArray = weights;
		this.relation = relation;
		this.result = resultVariable;
		
		this.isReifiable = true;
	}
	
	public GecodeLinear(GecodeAtom[] variables, 
			int[] weights,
			char relation,
			GecodeAtom constValue) {

		this.variables = variables;
		this.weights= weights;
		this.relation = relation;
		this.result = constValue;
		this.argVarName = computeArgsArrayName();
	}
	
	// ============= INHERITED METHODS ======================
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("");
		String weightsArgsName = "";
		
		if(this.variables == null) {
		
			s.append("linear(this, ");
		
			if(this.constantArray != null) {
				s.append(this.constantArray+", ");
			}
		
			s.append(this.variableArray+", "+operatorToString(this.relation)+", "+this.result);
		}	
		
		
		// we need to do the IntVarArgs thing...
		else {
			
			// declare the args variable
			if(this.variables[0] instanceof IntegerVariable || 
					this.variables[0] instanceof GecodeConstant) 			
				s.append("IntVarArgs "+this.argVarName+"("+this.variables.length+");\n");
		
			// boolean sum
			else s.append("BoolVarArgs "+this.argVarName+"("+this.variables.length+");\n");
			
			
			// assign the variables to the args vector
			for(int i=0; i<this.variables.length; i++) 
				s.append("\t  "+argVarName+"["+i+"] = "+variables[i]+";\n");
			
			if(this.weights != null) {
				weightsArgsName = "int_"+this.argVarName;
				// declare weights 
				s.append("\tIntArgs "+weightsArgsName+"("+this.weights.length+");\n");
				for(int i=0; i<weights.length; i++) 
					s.append("\t  "+weightsArgsName+"["+i+"] = "+weights[i]+";\n");
				
			}
			s.append("\tlinear(this, "+((this.weights != null) ? weightsArgsName+", " : "") +this.argVarName+", "+
				operatorToString(this.relation)+", "+this.result);
			
			//s.append("\tlinear(this, "+this.argVarName+", "+operatorToString(this.relation)+", "+this.result);
		}
			
			
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(", opt.icl())");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		
		
		
		return s.toString();
		
	}
	
	
	
	public String toCCString() {
		
		
		StringBuffer s = new StringBuffer("");
		String weightsArgsName = "";
		
		if(this.variables == null) {
		
			s.append("linear(this, ");
		
			if(this.constantArray != null) {
				s.append(this.constantArray+", ");
			}
		
			s.append(this.variableArray+", "+operatorToString(this.relation)+", "+this.result);
		}	
		
		
		// we need to do the IntVarArgs thing...
		else {
			
			// declare the args variable
			if(this.variables[0] instanceof IntegerVariable || 
					this.variables[0] instanceof GecodeConstant) 			
				s.append("IntVarArgs "+this.argVarName+"("+this.variables.length+");\n");
		
			// boolean sum
			else s.append("BoolVarArgs "+this.argVarName+"("+this.variables.length+");\n");
			
			
			// assign the variables to the args vector
			for(int i=0; i<this.variables.length; i++) 
				s.append("\t  "+argVarName+"["+i+"] = "+variables[i]+";\n");
			
		
			if(this.weights != null) {
				weightsArgsName = "int_"+this.argVarName;
				// declare weights 
				s.append("\tIntArgs "+weightsArgsName+"("+this.weights.length+");\n");
				for(int i=0; i<weights.length; i++) 
					s.append("\t  "+weightsArgsName+"["+i+"] = "+weights[i]+";\n");
				
			}
			s.append("\tlinear(this, "+((this.weights != null) ? weightsArgsName+", " : "") +this.argVarName+", "+
					operatorToString(this.relation)+", "+this.result);
		}
			
			
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(", opt.icl())");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		
		
		
		return s.toString();
		
		
		/*StringBuffer s = new StringBuffer("linear(this, ");
		
		if(this.constantArray != null) {
			s.append(this.constantArray+", ");
		}
		
		s.append(this.variableArray+", "+operatorToString(this.relation)+", "+this.result);
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(")");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		return s.toString(); */
	}
	
	
	// ============== ADDITIONAL METHODS ==================
	
	
	private String computeArgsArrayName() {
		
		StringBuffer argVarName = new StringBuffer("_");
		for(int i=0; i<this.variables.length; i++) {				
			argVarName.append(variables[i]);
		}
		// remove [ and ] from arrays 
		for(int i=0; i<argVarName.length(); i++) {
			if(argVarName.charAt(i) == '[' ||
					argVarName.charAt(i) == ']') {
				argVarName.replace(i, i+1, "_");
			}
		}
		//add a random number to the end
		java.util.Random randomNumberGenerator = new java.util.Random();
		argVarName.append(randomNumberGenerator.nextInt(this.randomMaximum));
		
		if(argVarName.length() > GecodeConstraint.MAX_VARIABLE_LENGTH)
			return argVarName.toString().substring(argVarName.length()-GecodeConstraint.MAX_VARIABLE_LENGTH);
		
		return argVarName.toString();
	}
	
}
