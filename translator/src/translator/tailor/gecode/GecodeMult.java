package translator.tailor.gecode;

public class GecodeMult extends RelationalConstraint {

	private GecodeAtom argument1;
	private GecodeAtom argument2;
	private GecodeAtom result;
	
	
	/** 
	 * Binary multiplication 
	 * x0 * x1 = result
	 * 
	 * @param arg1
	 * @param arg2
	 * @param result
	 */
	public GecodeMult(GecodeAtom arg1,
				      GecodeAtom arg2,
				      GecodeAtom result) {
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
