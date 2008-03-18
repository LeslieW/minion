package translator.xcsp2ep.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import translator.xcsp2ep.parser.PredicateTokens.RelationalOperator;
import translator.xcsp2ep.parser.components.PAllDifferent;
import translator.xcsp2ep.parser.components.PConstraint;
import translator.xcsp2ep.parser.components.PCumulative;
import translator.xcsp2ep.parser.components.PDomain;
import translator.xcsp2ep.parser.components.PElement;
import translator.xcsp2ep.parser.components.PExtensionConstraint;
import translator.xcsp2ep.parser.components.PIntensionConstraint;
import translator.xcsp2ep.parser.components.PPredicate;
import translator.xcsp2ep.parser.components.PRelation;
import translator.xcsp2ep.parser.components.PVariable;
import translator.xcsp2ep.parser.components.PWeightedSum;


/**
 * This class corresponds to a Java translator.xcsp2ep.parser.that uses DOM (Document Object Model) to parse CSP and WCSP instances in format "XCSP 2.1". <br>
 * Here, we assume that the instance is well-formed (valid). This class is given for illustration purpose. Feel free to adapt it !
 * 
 * @author christophe lecoutre
 * @version 2.1.1
 */
public class InstanceParser {
	public static final String VERSION = "version 2.1.1 (January 18, 2008)";

	private Document document;

	private String type;

	private String format;

	private int maxConstraintArity;

	private Map<String, PDomain> mapOfDomains;

	private Map<String, PVariable> mapOfVariables;

	private Map<String, PRelation> mapOfRelations;

	private Map<String, PPredicate> mapOfPredicates;

	private Map<String, PConstraint> mapOfConstraints;

	private PVariable[] variables;

	private int nbExtensionConstraints;

	private int nbIntensionConstraints;

	private int nbGlobalConstraints;

	private String satisfiable;

	private String minViolatedConstraints;

	public String getType() {
		return type;
	}

	

	/**
	 * Used to determine if elements of the instance must be displayed when parsing.
	 */
	private boolean displayInstance = true;

	/**
	 * Build a DOM object that corresponds to the file whose name is given. <br>
	 * The file must represent a CSP instance according to format XCSP 2.1
	 * 
	 * @param fileName the name of a file representing a CSP instance.
	 */
	public void loadInstance(String fileName) {
		document = XMLManager.load(fileName);
	}

	private void parsePresentation(Element presentationElement) {
		String s = presentationElement.getAttribute(InstanceTokens.MAX_CONSTRAINT_ARITY.trim());
		maxConstraintArity = s.length() == 0 || s.equals("?") ? -1 : Integer.parseInt(s);
		type = presentationElement.getAttribute(InstanceTokens.TYPE.trim());
		type = type.length() == 0 || type.equals("?") ? InstanceTokens.CSP : type;
		format = presentationElement.getAttribute(InstanceTokens.FORMAT.trim());
		if (displayInstance)
			System.out.println("Instance with maxConstraintArity=" + maxConstraintArity + " type=" + type + " format=" + format);
		s = presentationElement.getAttribute(InstanceTokens.NB_SOLUTIONS).trim();
		satisfiable = s.length() == 0 || s.equals("?") ? "unknown" : s.equals("0") ? "false" : "true";
		s = presentationElement.getAttribute(InstanceTokens.MIN_VIOLATED_CONSTRAINTS).trim();
		minViolatedConstraints = satisfiable.equals("true") ? "0" : s.length() == 0 || s.equals("?") ? "unknown" : s;
	}

	private int[] parseDomainValues(int nbValues, String stringOfValues) {
		int cnt = 0;
		int[] values = new int[nbValues];
		StringTokenizer st = new StringTokenizer(stringOfValues);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int position = token.indexOf(InstanceTokens.DISCRETE_INTERVAL_SEPARATOR);
			if (position == -1)
				values[cnt++] = Integer.parseInt(token);
			else {
				int min = Integer.parseInt(token.substring(0, position));
				int max = Integer.parseInt(token.substring(position + InstanceTokens.DISCRETE_INTERVAL_SEPARATOR.length()));
				for (int j = min; j <= max; j++)
					values[cnt++] = j;
			}
		}
		return values;
	}

	private PDomain parseDomain(Element domainElement) {
		String name = domainElement.getAttribute(InstanceTokens.NAME);
		int nbValues = Integer.parseInt(domainElement.getAttribute(InstanceTokens.NB_VALUES));
		int[] values = parseDomainValues(nbValues, domainElement.getTextContent());
		if (nbValues != values.length)
			throw new RuntimeException();
		return new PDomain(name, values);
	}

	private void parseDomains(Element domainsElement) {
		mapOfDomains = new HashMap<String, PDomain>();
		int nbDomains = Integer.parseInt(domainsElement.getAttribute(InstanceTokens.NB_DOMAINS));
		if (displayInstance)
			System.out.println("=> " + nbDomains + " domains");

		NodeList nodeList = domainsElement.getElementsByTagName(InstanceTokens.DOMAIN);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PDomain domain = parseDomain((Element) nodeList.item(i));
			mapOfDomains.put(domain.getName(), domain);
			if (displayInstance)
				System.out.println(domain);
		}
	}

	private PVariable parseVariable(Element variableElement) {
		String name = variableElement.getAttribute(InstanceTokens.NAME);
		String domainName = variableElement.getAttribute(InstanceTokens.DOMAIN);
		return new PVariable(name, mapOfDomains.get(domainName));
	}

	private void parseVariables(Element variablesElement) {
		mapOfVariables = new HashMap<String, PVariable>();
		int nbVariables = Integer.parseInt(variablesElement.getAttribute(InstanceTokens.NB_VARIABLES));
		if (displayInstance)
			System.out.println("=> " + nbVariables + " variables");

		variables = new PVariable[nbVariables];
		NodeList nodeList = variablesElement.getElementsByTagName(InstanceTokens.VARIABLE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PVariable variable = parseVariable((Element) nodeList.item(i));
			mapOfVariables.put(variable.getName(), variable);
			variables[i] = variable;
			if (displayInstance)
				System.out.println(variable);
		}
	}

	private int[][] parseRelationTuples(int nbTuples, int arity, String s) {
		int[][] tuples = new int[nbTuples][arity];
		StringTokenizer st = new StringTokenizer(s, InstanceTokens.WHITE_SPACE + InstanceTokens.TUPLES_SEPARATOR);
		for (int i = 0; i < tuples.length; i++)
			for (int j = 0; j < arity; j++)
				tuples[i][j] = Integer.parseInt(st.nextToken());
		return tuples;
	}

	private int[] weights;

	private int[][] parseSoftRelationTuples(int nbTuples, int arity, String s) {
		int[][] tuples = new int[nbTuples][arity];
		weights = new int[nbTuples];
		StringTokenizer st = new StringTokenizer(s, InstanceTokens.WHITE_SPACE + InstanceTokens.TUPLES_SEPARATOR);
		int currentCost = -2;
		for (int i = 0; i < nbTuples; i++) {
			String token = st.nextToken();
			int costFlagPosition = token.lastIndexOf(InstanceTokens.COST_SEPARATOR);
			if (costFlagPosition != -1) {
				currentCost = Integer.parseInt(token.substring(0, costFlagPosition));
				token = token.substring(costFlagPosition + 1);
			}
			weights[i] = currentCost;
			tuples[i][0] = Integer.parseInt(token);
			for (int j = 1; j < arity; j++)
				tuples[i][j] = Integer.parseInt(st.nextToken());
		}
		return tuples;
	}

	private PRelation parseRelation(Element relationElement) {
		String name = relationElement.getAttribute(InstanceTokens.NAME);
		int arity = Integer.parseInt(relationElement.getAttribute(InstanceTokens.ARITY));
		int nbTuples = Integer.parseInt(relationElement.getAttribute(InstanceTokens.NB_TUPLES));
		String semantics = relationElement.getAttribute(InstanceTokens.SEMANTICS);
		if (semantics.equals(InstanceTokens.SOFT)) {
			int[][] tuples = parseSoftRelationTuples(nbTuples, arity, relationElement.getTextContent());
			String s = relationElement.getAttribute(InstanceTokens.DEFAULT_COST);
			int defaultCost = s.equals(InstanceTokens.INFINITY) ? Integer.MAX_VALUE : Integer.parseInt(s);
			return new PRelation(name, arity, nbTuples, semantics, tuples, weights, defaultCost);
		} else {
			int[][] tuples = parseRelationTuples(nbTuples, arity, relationElement.getTextContent());
			return new PRelation(name, arity, nbTuples, semantics, tuples);
		}
	}

	private void parseRelations(Element relationsElement) {
		mapOfRelations = new HashMap<String, PRelation>();
		if (relationsElement == null)
			return;
		int nbRelations = Integer.parseInt(relationsElement.getAttribute(InstanceTokens.NB_RELATIONS));
		if (displayInstance)
			System.out.println("=> " + nbRelations + " relations");

		NodeList nodeList = relationsElement.getElementsByTagName(InstanceTokens.RELATION);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PRelation relation = parseRelation((Element) nodeList.item(i));
			mapOfRelations.put(relation.getName(), relation);
			if (displayInstance)
				System.out.println(relation);
		}
	}

	private PPredicate parsePredicate(Element predicateElement) {
		String name = predicateElement.getAttribute(InstanceTokens.NAME);
		Element parameters = (Element) predicateElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
		Element expression = (Element) predicateElement.getElementsByTagName(InstanceTokens.EXPRESSION).item(0);
		Element functional = (Element) expression.getElementsByTagName(InstanceTokens.FUNCTIONAL).item(0);
		return new PPredicate(name, parameters.getTextContent(), functional.getTextContent());
	}

	private void parsePredicates(Element predicatesElement) {
		mapOfPredicates = new HashMap<String, PPredicate>();
		if (predicatesElement == null)
			return;
		int nbPredicates = Integer.parseInt(predicatesElement.getAttribute(InstanceTokens.NB_PREDICATES));
		if (displayInstance)
			System.out.println("=> " + nbPredicates + " predicates");

		NodeList nodeList = predicatesElement.getElementsByTagName(InstanceTokens.PREDICATE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PPredicate predicate = parsePredicate((Element) nodeList.item(i));
			mapOfPredicates.put(predicate.getName(), predicate);
			if (displayInstance)
				System.out.println(predicate);
		}
	}

	private PVariable[] parseScope(String scope) {
		StringTokenizer st = new StringTokenizer(scope, " ");
		PVariable[] involvedVariables = new PVariable[st.countTokens()];
		for (int i = 0; i < involvedVariables.length; i++)
			involvedVariables[i] = mapOfVariables.get(st.nextToken());
		return involvedVariables;
	}

	private int searchIn(String s, PVariable[] t) {
		for (int i = 0; i < t.length; i++)
			if (t[i].getName().equals(s))
				return i;
		return -1;
	}


	private PConstraint parseElementConstraint(String name, Element parameters) {
		String text = Toolkit.insertWhitespaceAround(parameters.getTextContent(), InstanceTokens.BRACKETS);
		StringTokenizer st = new StringTokenizer(text, InstanceTokens.WHITE_SPACE);
		List<PVariable> list = new ArrayList<PVariable>();
		PVariable index = mapOfVariables.get(st.nextToken()); // index is necessarily a variable
		list.add(index);
		st.nextToken(); // token [
		List<Object> table = new ArrayList<Object>();
		String token = st.nextToken();
		while (!token.equals("]")) {
			Object object = mapOfVariables.get(token);
			if (object == null)
				object = Integer.parseInt(token);
			else
				list.add((PVariable) object);
			table.add(object);
			token = st.nextToken();
		}
		token = st.nextToken();
		Object value = mapOfVariables.get(token);
		if (value == null)
			value = Integer.parseInt(token);
		else
			list.add((PVariable) value);
		// NB : the scope is reordered
		return new PElement(name, list.toArray(new PVariable[list.size()]), index, table.toArray(new Object[table.size()]), value);
	}

	private PConstraint parseWeightedSumConstraint(String name, PVariable[] involvedVariables, Element parameters) {
		NodeList nodeList = parameters.getChildNodes();
		StringTokenizer st = new StringTokenizer(nodeList.item(0).getTextContent(), InstanceTokens.WHITE_SPACE + "[{}]");
		int[] coeffs = new int[involvedVariables.length];
		while (st.hasMoreTokens()) {
			int coeff = Integer.parseInt(st.nextToken());
			int position = searchIn(st.nextToken(), involvedVariables);
			coeffs[position] += coeff;
		}
		RelationalOperator operator = RelationalOperator.getRelationalOperatorFor(nodeList.item(1).getNodeName());
		int limit = Integer.parseInt(nodeList.item(2).getTextContent().trim());
		return new PWeightedSum(name, involvedVariables, coeffs, operator, limit);
	}

	private PConstraint parseCumulativeConstraint(String name, PVariable[] involvedVariables, Element parameters) {
		// Be careful : only the case where no integer constant is involved in task description is considered for the moment

		NodeList nodeList = parameters.getChildNodes();
		int nilPosition = (nodeList.getLength() == 1 ? -1 : new StringTokenizer(nodeList.item(0).getTextContent(), InstanceTokens.WHITE_SPACE + "[]{}").countTokens());

		StringTokenizer st = new StringTokenizer(parameters.getTextContent(), InstanceTokens.WHITE_SPACE + "[]{}");
		for (int i = 0; i < involvedVariables.length; i++)
			involvedVariables[i] = mapOfVariables.get(st.nextToken());
		int limit = Integer.parseInt(st.nextToken());
		return new PCumulative(name, involvedVariables, nilPosition, limit);
	}

	private PConstraint parseConstraint(Element constraintElement) {
		String name = constraintElement.getAttribute(InstanceTokens.NAME);
		int arity = Integer.parseInt(constraintElement.getAttribute(InstanceTokens.ARITY));
		if (arity > maxConstraintArity)
			maxConstraintArity = arity;
		PVariable[] involvedVariables = parseScope(constraintElement.getAttribute(InstanceTokens.SCOPE));

		String reference = constraintElement.getAttribute(InstanceTokens.REFERENCE);
		if (mapOfRelations.containsKey(reference)) {
			nbExtensionConstraints++;
			return new PExtensionConstraint(name, involvedVariables, mapOfRelations.get(reference));
		}

		if (mapOfPredicates.containsKey(reference)) {
			Element parameters = (Element) constraintElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
			nbIntensionConstraints++;
			return new PIntensionConstraint(name, involvedVariables, mapOfPredicates.get(reference), parameters.getTextContent());
		}

		nbGlobalConstraints++;
		String lreference = reference.toLowerCase();
		Element parameters = (Element) constraintElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
		
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.ALL_DIFFERENT)))
			return new PAllDifferent(name, involvedVariables);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.ELEMENT)))
			return parseElementConstraint(name, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.WEIGHTED_SUM)))
			return parseWeightedSumConstraint(name, involvedVariables, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.CUMULATIVE)))
			return parseCumulativeConstraint(name, involvedVariables, parameters);

		System.out.println("Problem with the reference " + reference);
		return null;
	}

	private void parseConstraints(Element constraintsElement) {
		mapOfConstraints = new HashMap<String, PConstraint>();
		int nbConstraints = Integer.parseInt(constraintsElement.getAttribute(InstanceTokens.NB_CONSTRAINTS));
		if (displayInstance) {
			System.out.print("=> " + nbConstraints + " constraints");
			if (type.equals(InstanceTokens.WCSP)) {
				int maximalCost = Integer.parseInt(constraintsElement.getAttribute(InstanceTokens.MAXIMAL_COST));
				String s = constraintsElement.getAttribute(InstanceTokens.INITIAL_COST);
				int initialCost = s.equals("") ? 0 : Integer.parseInt(s);
				System.out.print(" maximalCost=" + maximalCost + " initialCost=" + initialCost);
			}
			System.out.println();
		}

		NodeList nodeList = constraintsElement.getElementsByTagName(InstanceTokens.CONSTRAINT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PConstraint constraint = parseConstraint((Element) nodeList.item(i));
			mapOfConstraints.put(constraint.getName(), constraint);
			if (displayInstance)
				System.out.println(constraint);
		}
	}

	/**
	 * Parse the DOM object that has been loaded.
	 * 
	 * @param displayInstance if <code> true </code>, elements of the instance will be displayed.
	 */
	public void parseOld(boolean displayInstance) {
		this.displayInstance = displayInstance;
		parsePresentation((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.PRESENTATION).item(0));
		parseDomains((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.DOMAINS).item(0));
		parseVariables((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.VARIABLES).item(0));
		parseRelations((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.RELATIONS).item(0));
		parsePredicates((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.PREDICATES).item(0));
		parseConstraints((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.CONSTRAINTS).item(0));
		
	}
	
	/**
	 * Parse the DOM object that has been loaded.
	 * 
	 * @param displayInstance if <code> true </code>, elements of the instance will be displayed.
	 */
	public XCSPInstance parse(boolean displayInstance) {
		this.displayInstance = displayInstance;
		parsePresentation((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.PRESENTATION).item(0));
		parseDomains((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.DOMAINS).item(0));
		parseVariables((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.VARIABLES).item(0));
		parseRelations((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.RELATIONS).item(0));
		parsePredicates((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.PREDICATES).item(0));
		parseConstraints((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.CONSTRAINTS).item(0));
		
		
		return new XCSPInstance(this.document,
				                                     this.type,
				                                     this.format,
				                                     this.maxConstraintArity,
				                                     this.mapOfDomains,
				                                     this.mapOfVariables,
				                                     this.mapOfRelations,
				                                     this.mapOfPredicates,
				                                     this.mapOfConstraints,
				                                     this.variables,
				                                     this.nbExtensionConstraints,
				                                     this.nbIntensionConstraints,
				                                     this.nbGlobalConstraints,
				                                     this.satisfiable);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("InstanceParser " +VERSION);
			System.out.println("Usage : java ... InstanceParser <instanceName>");
			System.exit(1);
		}

		InstanceParser parser = new InstanceParser();
		parser.loadInstance(args[0]);
		parser.parse(true);
	}
}
