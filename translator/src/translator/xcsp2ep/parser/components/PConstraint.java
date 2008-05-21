package translator.xcsp2ep.parser.components;


public abstract class PConstraint {
	protected String name;

	protected PVariable[] scope;


	public String getName() {
		return name;
	}

	public PVariable[] getScope() {
		return scope;
	}

	public int getArity() {
		return scope.length;
	}

	public PConstraint(String name, PVariable[] scope) {
		this.name = name;
		this.scope = scope;
	}

	
	public int getMaximalCost() {
		return 1;
	}

	public String toString() {
		String s = "  constraint " + name + " with arity = " + scope.length + ", scope = ";
		s += scope[0].getName();
		for (int i = 1; i < scope.length; i++) {
			if(scope[i] != null)
				s += " " + scope[i].getName();
		}
		return s;
	}
}
