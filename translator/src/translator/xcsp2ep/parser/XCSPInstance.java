package translator.xcsp2ep.parser;

import java.util.Map;

import org.w3c.dom.Document;

import translator.xcsp2ep.parser.components.PConstraint;
import translator.xcsp2ep.parser.components.PDomain;
import translator.xcsp2ep.parser.components.PPredicate;
import translator.xcsp2ep.parser.components.PRelation;
import translator.xcsp2ep.parser.components.PVariable;


/**
 * Represents an XCSP instance of a CSP model (WCSP and QCSP not included).
 * 
 * @author andrea
 *
 */

public class XCSPInstance {

	//public static final String VERSION = "version 0.0.1 (March 7, 2008)";

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
	
	public XCSPInstance(Document document,
			            String type,
			            String format,
			            int maxConstraintArity,
			            Map<String, PDomain> mapOfDomains,
			            Map<String, PVariable> mapOfVariables,
			            Map<String, PRelation> mapOfRelations,
			            Map<String, PPredicate> mapOfPredicates,
			            Map<String, PConstraint> mapOfConstraints,
			            PVariable[] variables,
			            int nbExtensionConstraints,
			            int nbIntensionConstraints,
			            int nbGlobalConstraints,
			            String satisfiable) {

		this.document = document;
		this.type = type;
		this.maxConstraintArity = maxConstraintArity;
		this.mapOfDomains = mapOfDomains;
		this.mapOfVariables = mapOfVariables;
		this.mapOfRelations = mapOfRelations;
		this.mapOfPredicates = mapOfPredicates;
		this.mapOfConstraints = mapOfConstraints;
		this.variables = variables;
		this.nbExtensionConstraints = nbExtensionConstraints;
		this.nbIntensionConstraints = nbIntensionConstraints;
		this.nbGlobalConstraints = nbGlobalConstraints;
		this.satisfiable = satisfiable;
		
	}
	

	public PVariable[] getVariables() {
		return variables;
	}

	public int getMaxConstraintArity() {
		return maxConstraintArity;
	}

	public Map<String, PConstraint> getMapOfConstraints() {
		return mapOfConstraints;
	}

	public Map<String, PDomain> getMapOfDomains() {
		return mapOfDomains;
	}
	
	public Map<String, PVariable> getMapOfVariables() {
		return mapOfVariables;
	}
	
	public Map<String, PRelation> getMapOfRelations() {
		return mapOfRelations;
	}
	
	public Map<String, PPredicate> getMapOfPredicates() {
		return mapOfPredicates;
	}
	
	public int getNbExtensionConstraints() {
		return nbExtensionConstraints;
	}

	public int getNbIntensionConstraints() {
		return nbIntensionConstraints;
	}

	public int getNbGlobalConstraints() {
		return nbGlobalConstraints;
	}
	
	public String getConstraintsCategory() {
		return (nbExtensionConstraints > 0 ? "E" : "") + (nbIntensionConstraints > 0 ? "I" : "") + (nbGlobalConstraints > 0 ? "G" : "");
	}

	public String getSatisfiable() {
		return satisfiable;
	}

	public String getMinViolatedConstraints() {
		return minViolatedConstraints;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getFormat() {
		return this.format;
	}
	
	public Document getDocument() {
		return this.document;
	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("XCSP format:"+this.format+" with type "+this.type);
		s.append("Variables:\n"+this.mapOfVariables+"\n");
		s.append("Domains:\n"+this.mapOfDomains+"\n");
		s.append("Predicates:\n"+this.mapOfPredicates+"\n");
		s.append("Constraints:\n"+this.mapOfVariables+"\n");
		s.append("Relations:\n"+this.mapOfVariables+"\n");
		
		
		return s.toString();
		
	}
}
