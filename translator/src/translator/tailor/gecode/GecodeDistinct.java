package translator.tailor.gecode;

/**
 * Gecode's distinct/alldifferent constraint
 * 
 * @author andrea
 *
 */

public class GecodeDistinct extends RelationalConstraint {

	private GecodeIntVarArgs array;
	
	
	public GecodeDistinct(GecodeIntVarArgs variables) {
		this.array = variables;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM,
													  GecodeConstraint.ICL_VAL};		                                           
	}
	

	// =============== INHERITED METHODS ========================
	
	public String toCCString() { 
		
		String s = "distinct(this, "+array;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
		
	}
	
	public String toString() { 
		
		String s = "distinct(this, "+array;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
		
	}
	
}
