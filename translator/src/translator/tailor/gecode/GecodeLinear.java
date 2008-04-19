package translator.tailor.gecode;

public class GecodeLinear extends RelationalConstraint {

	private ArgsVariable boolArray;
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
	public GecodeLinear(ArgsVariable variableArray,
					    char relation,
					    GecodeConstant constValue) {
		
		this.boolArray = variableArray;
		this.relation = relation;
		this.result = constValue;
		
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
	public GecodeLinear(ArgsVariable variableArray,
						char relation,
						GecodeIntVar variable) {
		
		this.boolArray = variableArray;
		this.relation = relation;
		this.result = variable;
		
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
	public GecodeLinear(ArgsVariable variableArray, 
						ConstantArgsArray weights,
						char relation,
						GecodeConstant constValue) {
		
		this.boolArray = variableArray;
		this.constantArray = weights;
		this.relation = relation;
		this.result = constValue;
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
	public GecodeLinear(ArgsVariable variableArray,
						ConstantArgsArray weights,
						char relation,
						GecodeIntVar resultVariable) {
		
		this.boolArray= variableArray;
		this.constantArray = weights;
		this.relation = relation;
		this.result = resultVariable;
		
		this.isReifiable = true;
	}
	
	// ============= INHERITED METHODS ======================
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("linear(this, ");
		
		if(this.constantArray != null) {
			s.append(this.constantArray+", ");
		}
		
		s.append(this.boolArray+", "+operatorToString(this.relation)+", "+this.result);
	
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(")");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		
		return s.toString();
		
	}
	
	public String toCCString() {
		StringBuffer s = new StringBuffer("linear(this, ");
		
		if(this.constantArray != null) {
			s.append(this.constantArray+", ");
		}
		
		s.append(this.boolArray+", "+operatorToString(this.relation)+", "+this.result);
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(")");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		return s.toString();
	}
	
	
}
