package translator.expression;

public class DomainDefinition implements Definition {

	private String name;
	private Domain domain;
	
	
	public DomainDefinition(String name,
							Domain domain) {
		this.name = name;
		this.domain = domain;
	}
	
	// ============== INHERITED METHODS =========
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return "letting "+name+" be domain "+domain;
	}
	
	// ============== ADDITIONAL METHODS ==========
	
	public Domain getDomain() {
		return this.domain;
	}

	
}
