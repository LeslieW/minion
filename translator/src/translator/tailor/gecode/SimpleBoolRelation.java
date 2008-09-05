package translator.tailor.gecode;

public class SimpleBoolRelation extends RelationalConstraint {

	private GecodeVariable leftArgument;
	private char operator;
	private GecodeAtom rightArgument;
	private GecodeAtom reifiedVariable; 
	
	/**
	 * First case:  x0 intOp x1  where x1 and x2 are BoolVars
	 * Please note that intOp is an integer relational operator!
	 * 
	 * @param leftArgument x0
	 * @param intOp integer relational operator
	 * @param rightArgument x1
	 */
	public SimpleBoolRelation(GecodeBoolAtomVariable leftArgument,
							  char intOp,
							  GecodeBoolAtomVariable rightArgument) {
		
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.operator = intOp;
		
		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_DOM };
		this.isReifiable = true;
	}
	
	
	/** 
	 * Second case: xi  intOp y  where intOp is a relational integer
	 * operator  
	 * 
	 * @param leftArgsArgument array of variables xi
	 * @param intOp integer relational operator
	 * @param rightArgument boolVar
	 */
	public SimpleBoolRelation(GecodeBoolVarArgs leftArgsArgument,
			                  char intOp,
			                  GecodeBoolAtomVariable rightArgument) {
		
		this.leftArgument = leftArgsArgument;
		this.rightArgument = rightArgument;
		this.operator = intOp;
		
		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	/**
	 * Third case: x intOp const  where const is an integer value (either 0 or 1). 
	 * 
	 * @param leftVariable
	 * @param intOp
	 * @param constant
	 */
	public SimpleBoolRelation(GecodeBoolAtomVariable leftVariable,
							  char intOp,
							  GecodeConstant constant) {
		this.leftArgument = leftVariable;
		this.rightArgument = constant;
		this.operator = intOp;
		
		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	/**
	 * Fourth case: xI intOp const where xi is an element of the arguments 
	 * array X and const is an integer value (either 0 or 1)
	 * 
	 * @param leftArgsVariable X
	 * @param intOp integer relational operator
	 * @param constant integer value
	 */
	public SimpleBoolRelation(GecodeBoolVarArgs leftArgsVariable,
			  				  char intOp,
			  				  GecodeConstant constant) {
		this.leftArgument = leftArgsVariable;
		this.rightArgument = constant;
		this.operator = intOp;

		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	
	/** 
	 * Fifth case: X intOp Y where X and Y are argument arrays. 
	 * If intOp is an inequality relation, then it corresponds 
	 * to lex ordering constraint. 
	 * 
	 * 
	 * @param leftArgsArgument
	 * @param intOp
	 * @param rightArgsArgument
	 */
	public SimpleBoolRelation(GecodeBoolVarArgs leftArgsArgument,
							  char intOp,
							  GecodeBoolVarArgs rightArgsArgument) {
		this.leftArgument = leftArgsArgument;
		this.operator = intOp;
		this.rightArgument = rightArgsArgument;
		
		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	/** 
	 * Sixth case: intop(X): pairwise relation on X where X is an
	 * argument array and intOp is a relational integer operator. 
	 * 
	 * @param leftArgsArgument
	 * @param intOp
	 */
	public SimpleBoolRelation(GecodeBoolVarArgs leftArgsArgument,
							  char intOp) {
		this.leftArgument = leftArgsArgument;
		this.operator = intOp;
		this.rightArgument = null;
		
		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	
	/**
	 * Reification version of rel
	 * @param leftArg
	 * @param op
	 * @param rightArg
	 * @param reifiedVar
	 */
	public SimpleBoolRelation(GecodeVariable leftArg,
							char op,
							GecodeAtom rightArg,
							GecodeAtom reifiedVar) {

		this.leftArgument = leftArg;
		this.rightArgument = rightArg;
		this.operator = op;
		this.reifiedVariable = reifiedVar;
	}
	
	// =========== INHERITED METHODS ===================================================================
	
	public String toCCString() {
		
		/** if(this.rightArgument == null) {
			return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "
			+consistencyToString(this.consistencyLevel)+","
			+propagationToString(this.propagationKind)+")";
		}
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF) {
			return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "+rightArgument+")";
		}
		
		else return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "+rightArgument+","
			+consistencyToString(this.consistencyLevel)+","
			+propagationToString(this.propagationKind)+")";
			*/
		return this.toString();
	}

	public String toString() {
		
		if(this.rightArgument == null) {
			return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "
			+consistencyToString(this.consistencyLevel)+","
			+propagationToString(this.propagationKind)+")";
		}

		StringBuffer s = new StringBuffer("");
		
		// if this is just an args variable, we need to declare and define it first
		if(this.rightArgument instanceof ArgsArrayVariable) {
			ArgsArrayVariable argsArray = (ArgsArrayVariable) rightArgument;
			if(argsArray.isIndexedArray()) {
				s.append(argsArray.getArrayDefinition());
			}
		}
		
		if(s.length() != 0)
			s.append("\t");
		
		if(this.leftArgument instanceof ArgsArrayVariable) {
			ArgsArrayVariable argsArray = (ArgsArrayVariable) leftArgument;
			if(argsArray.isIndexedArray()) {
				s.append(argsArray.getArrayDefinition());
			}
		}
		
		if(s.length() != 0)
			s.append("\t");
			
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF) {
			return s.append("rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "+rightArgument+
					((this.reifiedVariable ==null) ? "" : ", "+this.reifiedVariable)+", opt.icl())").toString();
		}
		
		else return s.append("rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "+rightArgument+"," +
				""+consistencyToString(this.consistencyLevel)+","
				+propagationToString(this.propagationKind)+")").toString();
	}
	
	 
	
}
