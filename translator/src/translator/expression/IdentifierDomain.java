package translator.expression;

public class IdentifierDomain implements BasicDomain {

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
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		return this;
	}
	
	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		
		if(variableName.equals(this.domainName)) {
			return newDomain;
		}
		
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		
		IdentifierDomain otherDomain = (IdentifierDomain) d;
		
		int difference = this.domainName.compareTo(otherDomain.domainName);
		
		if(difference == 0) return Expression.EQUAL;
		else return (difference < 0) ?
				Expression.SMALLER : Expression.BIGGER;
		
	}
	
	
	// =========== ADDITIONAL METHODS =================
	
	public String getDomainName() {
		return this.domainName;
	}
}
