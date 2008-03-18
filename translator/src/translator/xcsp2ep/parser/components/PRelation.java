package translator.xcsp2ep.parser.components;


import translator.xcsp2ep.parser.InstanceTokens;


public class PRelation {

	private String name;

	private int arity;

	private int nbTuples;

	private String semantics;

	private int[][] tuples;

	/**
	 * weights = null if semantics is different from InstanceTokens.SOFT
	 */
	private int[] weights;

	/**
	 * defaultCost = Integer.MAX_VALUE if defaultCost is infinity
	 */
	private int defaultCost;
	
	/**
	 * The max of all weights values and deafultCost. It is 1 for an ordinary relation.
	 */
	private int maximalCost;
	
	

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	public String getSemantics() {
		return semantics;
	}

	public int[][] getTuples() {
		return tuples;
	}

	public int[] getWeights() {
		return weights;
	}

	public int getDefaultCost() {
		return defaultCost;
	}

	public int getMaximalCost() {
		return maximalCost;
	}
	
	public PRelation(String name, int arity, int nbTuples, String semantics, int[][] tuples, int[] weights, int defaultCost) {
		this.name = name;
		this.arity = arity;
		this.nbTuples = nbTuples;
		this.semantics = semantics;
		this.tuples = tuples;
		this.weights = weights;
		this.defaultCost = defaultCost;
		if (weights == null)
			maximalCost=1;
		else {
			maximalCost=defaultCost;
			for (int w : weights)
				if (w > maximalCost)
					maximalCost=w;
		}
	}

	public PRelation(String name, int arity, int nbTuples, String semantics, int[][] tuples) {
		this(name, arity, nbTuples, semantics, tuples, null, semantics.equals(InstanceTokens.SUPPORTS) ? 1 : 0);
	}


	public String toString() {
		int displayLimit = 5;
		String s = "  relation " + name + " with arity=" + arity + ", semantics=" + semantics + ", nbTuples=" + nbTuples + ", defaultCost=" + defaultCost + " : ";
		for (int i = 0; i < Math.min(nbTuples, displayLimit); i++) {
			s += "(";
			for (int j = 0; j < arity; j++)
				s += (tuples[i][j] + (j < arity - 1 ? "," : ""));
			s += ") ";
			if (weights != null)
				s += " with cost=" + weights[i] + ", ";
		}
		return s + (nbTuples > displayLimit ? "..." : "");
	}

	public boolean isSimilarTo(int arity, int nbTuples, String semantics, int[][] tuples) {
		if (semantics.equals(InstanceTokens.SOFT))
			throw new IllegalArgumentException();
		if (this.arity != arity || this.nbTuples != nbTuples)
			return false;
		if (!this.semantics.equals(semantics))
			return false;
		for (int i = 0; i < tuples.length; i++)
			for (int j = 0; j < tuples[i].length; j++)
				if (this.tuples[i][j] != tuples[i][j])
					return false;
		return true;
	}

	public String getStringListOfTuples() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < tuples.length; i++) {
			for (int j = 0; j < tuples[i].length; j++) {
				s.append(tuples[i][j]);
				if (j != tuples[i].length - 1)
					s.append(' ');
			}
			if (i != tuples.length - 1)
				s.append('|');
		}
		return s.toString();
	}

}
