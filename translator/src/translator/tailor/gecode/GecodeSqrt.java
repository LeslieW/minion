package translator.tailor.gecode;

public class GecodeSqrt extends RelationalConstraint {

	private GecodeIntVar argument;
	private GecodeIntVar result;
	
	/**
	 * Gecode's square-root constraint: sqrt(argument) = result
	 * 
	 * @param argument
	 * @param result
	 */
	public GecodeSqrt(GecodeIntVar argument,
					 GecodeIntVar result) {
		this.argument = argument;
		this.result = result;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND};
	}
	
	//============== INHERITED METHODS ========================
	
	
	public String toCCString() {
		
		String s =  "sqrt(this, "+this.argument+", "+this.result;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
		
	}
	
	public String toString() {
		
		String s =  "sqrt(this, "+this.argument+", "+this.result;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
		
	}
	
	
}
