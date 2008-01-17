package translator.expression;

public class IdentifierDomain implements Domain {

	private String domainName;
	
	// ============ CONSTRUCTOR =============
	
	public IdentifierDomain(String domainName) {
		this.domainName = domainName;
	}
	
	
	//========= INHERITED METHODS ============
	
	public Domain copy() {
		String copiedName = new String(this.domainName); 
		return new IdentifierDomain(copiedName);
	}

	public Domain evaluate() {
		return this;
	}

	public int getType() {
		return IDENTIFIER;
	}

	public boolean isConstantDomain() {
		return false;
	}
	
	public String toString() {
		return this.domainName;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}
	
}
