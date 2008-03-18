package translator.xcsp2ep.parser.components;


public abstract class PGlobalConstraint extends PConstraint {
	public PGlobalConstraint(String name, PVariable[] scope) {
		super(name, scope);
	}
}
