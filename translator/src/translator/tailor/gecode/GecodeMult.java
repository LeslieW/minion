package translator.tailor.gecode;

public class GecodeMult extends RelationalConstraint {

	private GecodeIntVar argument1;
	private GecodeIntVar argument2;
	private GecodeIntVar result;
	
	
	/** 
	 * Binary multiplication 
	 * x0 * x1 = result
	 * 
	 * @param arg1
	 * @param arg2
	 * @param result
	 */
	public GecodeMult(GecodeIntVar arg1,
				      GecodeIntVar arg2,
				      GecodeIntVar result) {
		this.argument1 = arg1;
		this.argument2 =arg2;
		this.result = result;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND};
	}
	
	
	// ============== INHERITED METHODS ==================
	
	
	public String toCCString() {
		
		String s =  "mult(this, "+this.argument1+", "+this.argument2+", "+this.result;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
	}
	
	
	public String toString() {
		
		String s =  "mult(this, "+this.argument1+", "+this.argument2+", "+this.result;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
	}
	
}
