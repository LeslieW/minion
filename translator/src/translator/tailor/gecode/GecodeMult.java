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
		
		String s;
		
		if(this.argument1 instanceof BooleanVariable && 
		   this.argument2 instanceof BooleanVariable)
			s = "post(this, tt( eqv("+this.argument1+" && "+this.argument2+", "+this.result+") )";
		
		else  s =  "mult(this, "+this.argument1+", "+this.argument2+", "+this.result;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+", opt.icl())";
		
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
	}
	
	
	public String toString() {
		
		String s;
		
		//System.out.println("Arg1: "+this.argument1+" has type: "+this.argument1.getClass().getSimpleName());
		//System.out.println("Arg2: "+this.argument2+" has type: "+this.argument2.getClass().getSimpleName());
		
		if(this.argument1 instanceof BooleanVariable && 
		   this.argument2 instanceof BooleanVariable) {
			s = "post(this, tt( eqv("+this.argument1+" && "+this.argument2+", "+this.result+") )";
		}
			
		else  s =  "mult(this, "+this.argument1+", "+this.argument2+", "+this.result;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+", opt.icl())";
		
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
	}
	
}
