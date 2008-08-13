package translator.tailor.gecode;


public class GecodePostConstraint extends RelationalConstraint {

	private String nonlinearExpression;
	
	
	public GecodePostConstraint(String sum) {
		this.nonlinearExpression = sum;
	}
	
	
	//========= INHERITED METHODS ====================
	
	public String toCCString() {
		StringBuffer s = new StringBuffer("post(this, "+nonlinearExpression);
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(")");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		return s.toString();
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer("post(this, "+nonlinearExpression);
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF) 
			s.append(")");
		else {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		return s.toString();
	}

}
