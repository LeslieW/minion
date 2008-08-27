package translator.tailor.gecode;

/**
 * The post constraint can post either linear constraints on int 
 * variables or boolean constraints
 * 
 * @author andrea
 *
 */

public class GecodePostConstraint extends RelationalConstraint {

	private String linearExpression;
	private boolean isBooleanRelation;
	private boolean satisfy;
	
	public GecodePostConstraint(String linearRelation) {
		this.linearExpression = linearRelation;
		this.isBooleanRelation = false;
	}
	
	public GecodePostConstraint(String booleanRelation,
								boolean satisfy) {
		this.linearExpression = booleanRelation;
		this.isBooleanRelation = true;
		this.satisfy = satisfy;
	}
	
	//========= INHERITED METHODS ====================
	
	public String toCCString() {
		StringBuffer s = new StringBuffer("post(this,");
		
		if(this.isBooleanRelation) 
			s.append((this.satisfy ? "tt" : "ff")+"("+linearExpression+")");
		else s.append(linearExpression); 
			
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(", opt.icl())");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		return s.toString();
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer("post(this,");
		
		if(this.isBooleanRelation) 
			s.append((this.satisfy ? "tt" : "ff")+"("+linearExpression+")");
		else s.append(linearExpression); 
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(", opt.icl())");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		return s.toString();
	}

}
