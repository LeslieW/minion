package translator.expression;

public class DomainParameter implements Parameter {

	private Domain domain;
	private String parameterName;
	
	
	// ============ CONSTRUCTOR =========================
	
	public DomainParameter(Domain domain,
			               String parameterName) {
		this.domain = domain;
		this.parameterName = parameterName;
	}
	 
	// ============ INHERITED METHODS ====================
	
	public Parameter applyRestriction(Expression whereRestriction) {
		// do nothing - we cannot apply a restriction on a constant domain (yet)
		return this;
	}

	public Parameter evaluate() {
		this.domain = this.domain.evaluate();
		return this;
	}

	public int getType() {
		return DOMAIN;
	}
	
	public String getParameterName() {
		return this.parameterName;
	}

	public String toString() {
		return this.parameterName+": "+this.domain;	
	}
	
	// ================== OTHER METHODS ==========================
	
	public Domain getDomain() {
		return this.domain;
	}
}
