package translator.tailor.gecode;

/**
 * Gecode's distinct/alldifferent constraint
 * 
 * @author andrea
 *
 */

public class GecodeDistinct extends RelationalConstraint {

	private GecodeArray array;
	
	
	public GecodeDistinct(GecodeArray variables) {
		this.array = variables;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM,
													  GecodeConstraint.ICL_VAL};		                                           
	}
	

	// =============== INHERITED METHODS ========================
	
	public String toCCString() { 
		
		/*String s = "distinct(this, "+array;
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			s = s+")";
		else s = s+", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")";
		
		return s;
		*/
		return this.toString();
	}
	
	public String toString() { 
		
		StringBuffer str = new StringBuffer("");
		
		if(array instanceof ArgsArrayVariable) {
			ArgsArrayVariable argsVar = (ArgsArrayVariable) array;
			if(argsVar.isIndexedArray()) {
				str.append(argsVar.getArrayDefinition());
			}
		}
		
		if(str.length() != 0)
			str.append("\t");
		
		 str.append("distinct(this, "+array);
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF &&
				this.propagationKind == GecodeConstraint.PK_DEF)
			str.append(")");
		else str.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		
		return str.toString();
		
	}
	
}
