package translator.tailor.gecode;

public class SimpleIntRelation extends RelationalConstraint {
	
	private char operator;
	private GecodeVariable leftArgument;
	private GecodeAtom rightArgument;

	
	/**
	 * First case: x0 ~r x1  where x0,x1 are both IntVars
	 * @param leftArgument IntVar x0
	 * @param operator relational integer operator r (see GecodeConstraint)
	 * @param rightArgument IntVar x1
	 */
	public SimpleIntRelation(GecodeIntVar leftArgument,
			              char operator,
			              GecodeIntVar rightArgument) {
		
		this.leftArgument = leftArgument;
		this.rightArgument = rightArgument;
		this.operator = operator;

		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM };
		this.isReifiable = true;
	}
	
	/**
	 * Second case: xi ~r y  forall 0 <= i <= |x|
	 * @param leftArgsArgument IntVarArgs x0
	 * @param operator relational integer operator r (see GecodeConstraint)
	 * @param rightArgument IntVar y
	 */
	public SimpleIntRelation(GecodeIntVarArgs leftArgsArgument,
						  char operator,						  
						  GecodeIntVar rightArgument) {
		this.leftArgument = leftArgsArgument;
		this.rightArgument = rightArgument;

		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	/**
	 * Third case: x0 ~r c
	 * @param leftArgument  x0
	 * @param operator relational integer operator r (see GecodeConstraint)
	 * @param constant constant c
	 */
	public SimpleIntRelation(GecodeIntVar leftArgument,
							 char operator,
							 GecodeConstant constant) {
		this.leftArgument = leftArgument;
		this.rightArgument = constant;
		this.operator = operator;

		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
		this.isReifiable = true;
	}
	
	
	/**
	 * Fourth case: Xi ~r const with 0<= i<=|X| where X is
	 * an argument array and const is an integer constant
	 * 
	 * @param leftArgsArgument
	 * @param operator
	 * @param constant
	 */
	public SimpleIntRelation(GecodeIntVarArgs leftArgsArgument,
						     char operator,						  
						     GecodeConstant constant) {
		this.leftArgument = leftArgsArgument;
		this.rightArgument = constant;

		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_BND,
													   GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	/**
	 * Post propagator for pairwise relation on x
	 * 
	 * @param argumentArray x
	 * @param operator
	 */
	public SimpleIntRelation(GecodeIntVarArgs argumentArray,
							char operator) {
		
		this.leftArgument = argumentArray;
		this.operator = operator;
		
		this.supportedConsistencyLevels = new char[] { GecodeConstraint.ICL_BND,
				   GecodeConstraint.ICL_DOM };
		this.isReifiable = false;
	}
	
	/**
	 * Sixth case:
	 * X op Y where X and Y are both argument arrays with same length. 
	 * If op is inequality it corresponds to the lex constraint
	 * 
	 * 
	 * @param leftArgsArgument
	 * @param operator
	 * @param rightArgsArgument
	 */
	public SimpleIntRelation(GecodeIntVarArgs leftArgsArgument,
							 char operator,
							 GecodeIntVarArgs rightArgsArgument) {
		
		this.leftArgument = leftArgsArgument;
		this.rightArgument = rightArgsArgument;
		this.operator = operator;

		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
	}
	
	//============= INHERITED METHODS =====================================
	
	
	
	public String toCCString() {
		
		if(this.rightArgument == null) {
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
	}

	public String toString() {
		
		if(this.rightArgument == null) {
			return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "
			+consistencyToString(this.consistencyLevel)+","
			+propagationToString(this.propagationKind)+")";
		}
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF) {
			return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "+rightArgument+")";
		}
		
		else return "rel(this,"+leftArgument+", "+operatorToString(this.operator)+", "+rightArgument+"," +
				""+consistencyToString(this.consistencyLevel)+","
				+propagationToString(this.propagationKind)+")";
	}
	
	//=========== ADDITIONAL METHODS ===============
	
	public char getOperator() {
		return this.operator;
	}

}
