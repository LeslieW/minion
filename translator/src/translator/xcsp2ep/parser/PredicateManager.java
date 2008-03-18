package translator.xcsp2ep.parser;

import java.util.StringTokenizer;


public class PredicateManager {

	public static String[] extractFormalParameters(String formalParametersExpression, boolean controlRedundancy) {
		StringTokenizer st = new StringTokenizer(formalParametersExpression);
		int cpt = 0;
		String[] formalParameters = new String[st.countTokens() / 2];
		while (st.hasMoreTokens()) {
			st.nextToken(); // curently, only int is authorized as type, so we can discard this information
			String token = st.nextToken();
			if (controlRedundancy)
				for (int j = 0; j < cpt; j++)
					if (formalParameters[j].equals(token))
						return null;
			formalParameters[cpt++] = token;
		}
		return formalParameters;
	}

	public static String[] extractFormalParameters(String formalParametersExpression) {
		return extractFormalParameters(formalParametersExpression, false);
	}


	public static String[] extractUniversalEffectiveParameters(String effectiveParametersExpression, String[] variableNames) {
		StringTokenizer st = new StringTokenizer(effectiveParametersExpression);
		String[] effectiveParameters = new String[st.countTokens()];
		for (int i = 0; i < effectiveParameters.length; i++) {
			String token = st.nextToken();
			if (!Toolkit.isInteger(token)) {
				int position = Toolkit.searchIn(token, variableNames);
				if (position == -1)
					throw new IllegalArgumentException();
				token = InstanceTokens.getParameterNameFor(position);
			}
			effectiveParameters[i] = token;
		}
		return effectiveParameters;
	}
}
