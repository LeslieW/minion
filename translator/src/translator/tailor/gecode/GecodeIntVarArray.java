package translator.tailor.gecode;

/**
 * Represents IntVarArrays. Also embodies multi-dimensional
 * arrays that are flattened by adding a function to the 
 * Gecode model
 * 
 * @author andrea
 *
 */

public class GecodeIntVarArray implements GecodeIntArray {

	private int lb;
	private int ub;
	private int[] sparseDomain;
	private int[] lengths;
	private String name;
	private int length;
	
	
	/**
	 * Constructor for bounds (or discrete) domain 
	 * variables
	 * 
	 * @param name
	 * @param length
	 * @param lb
	 * @param ub
	 */
	public GecodeIntVarArray(String name, 
							 int length,
							 int lb,
							 int ub) {
		this.name = name;
		this.length = length;
		this.lb = lb;
		this.ub = ub;
	}
	
	
	/**
	 * Constructor for sparse domain variables
	 * 
	 * @param name
	 * @param length
	 * @param sparseDomain
	 */
	public GecodeIntVarArray(String name, 
			 int length,
			 int[] sparseDomain) {
		this.name = name;
		this.length = length;
		this.sparseDomain = sparseDomain;
		this.lb = sparseDomain[0];
		this.ub = sparseDomain[sparseDomain.length-1];
	}
	
	
	/**
	 * Constructor for multi-dimensional arrays
	 * that are flattened internally
	 * 
	 * @param name
	 * @param lengths
	 * @param lb
	 * @param ub
	 */
	public GecodeIntVarArray(String name,
							 int[] lengths,
							 int lb,
							 int ub) {
		this.name = name;
		this.lengths = lengths;
		this.length = 1;
		for(int i=0; i<this.lengths.length; i++)
			length *= lengths[i];
		this.lb = lb;
		this.ub = ub;
	}
	
	
	public GecodeIntVarArray(String name,
			 				int[] lengths,
			 				int[] sparseDomain) {
		this.name = name;
		this.lengths = lengths;
		this.length = 1;
		for(int i=0; i<this.lengths.length; i++)
			length *= lengths[i];
		this.sparseDomain = sparseDomain;
		this.lb = sparseDomain[0];
		this.ub = sparseDomain[sparseDomain.length-1];
	}
	
	//============ INHERITED METHODS ===============
	
	public int getLength() {
		return this.length;
	}

	public int getLowerBound() {
		return this.lb;
	}

	public int getUpperBound() {
		return this.ub;
	}

	public int[] getBounds() {
		return new int[] {lb, ub};
	}

	public char getType() {
		return INT_ARRAY_VAR;
	}

	public String getVariableName() {
		if(this.lengths != null) 
			return GecodeConstraint.FLATTENED_ARRAY_PREFIX+name;
		return this.name;
	}

	public String toDeclarationCCString() {
		if(this.lengths != null) 
			return "IntVarArray "+GecodeConstraint.FLATTENED_ARRAY_PREFIX+name;
		return "IntVarArray "+this.name;
	}

	public boolean isArgsVariable() {
		return false;
	}
	
	public String toString() {
		if(this.lengths != null) 
			return GecodeConstraint.FLATTENED_ARRAY_PREFIX+name;
		else return name;
	}
	
	// =========== ADDITIONAL METHODS ========================
	
	// we need these methods to dynamically generate 
	// int array vars for single variables
	
	public void addLowerBound(int newLb) {
		if(newLb < lb)
			this.lb = newLb;
	}

	
	public void addUpperBound(int newUb) {
		if(newUb > ub)
			this.ub = newUb;
	}
	
	public void increaseLength() {
		this.length++;
	}
	
	public void setLowerBound(int lb) {
		this.lb = lb;
	}
	
	public void setUpperBound(int ub) {
		this.ub = ub;
	}
	
	public int[] getSparseDomain() {
		return this.sparseDomain;
	}
	
	public boolean isSparseDomain() {
		return (this.sparseDomain != null);
	}
	
	public boolean isMultiDimensional() {
		return this.lengths != null;
	}
	
	public int getDimension() {
		if(this.lengths == null)
			return 1;
		else return lengths.length;
	}
	
	public int[] getLengths() {
		return this.lengths;
	}
}
