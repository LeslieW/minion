package translator.xcsp2ep.parser.components;


public class PAllDifferent extends PGlobalConstraint {
	
	public PAllDifferent(String name, PVariable[] scope) {
		super(name, scope);
	}


	public String toString() {
		return super.toString() + " : allDifferent";
	}
}
