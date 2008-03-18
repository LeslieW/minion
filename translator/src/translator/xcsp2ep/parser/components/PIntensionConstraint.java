package translator.xcsp2ep.parser.components;


public class PIntensionConstraint extends PConstraint {
	private PPredicate predicate;

	private String parameters;
	
	public PPredicate getPredicate() {
		return predicate;
	}

	public String getParameters() {
		return this.parameters;
	}

	public PIntensionConstraint(String name, 
				                PVariable[] scope,
				                PPredicate predicate, 
				                String effectiveParametersExpression) {
		super(name, scope);
		this.predicate = predicate;

		String[] variableNames = new String[scope.length];
		for (int i = 0; i < variableNames.length; i++)
			variableNames[i] = scope[i].getName();

		this.parameters = effectiveParametersExpression;
		// System.out.println(universalPredicateExpression);
	}


	public String toString() {
		return super.toString() + ", and associated predicate " + predicate.getName();
	}
}
