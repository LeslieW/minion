package translator.tailor.gecode;

public class GecodeIntVarArray implements GecodeIntArray {

	private int lb;
	private int ub;
	private int[] sparseDomain;
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
		return this.name;
	}

	public String toDeclarationCCString() {
		return "IntVarArray "+this.name;
	}

	public boolean isArgsVariable() {
		return false;
	}
	
	public String toString() {
		return name;
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
}
