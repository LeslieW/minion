package translator.expression;

public class ArrayDomain implements Domain {

	/** the domain each array element is ranging over */
	private Domain baseDomain;
	/** the domain of the dimensions of the array. the first 
	 * indexDomain is the row, then column, etc*/
	private Domain[] indexDomains;
	
	
	
	// =============== CONSTRUCTOR ==============================
	
	public ArrayDomain(Domain baseDomain,
			           Domain[] dimensionDomains) {
		this.baseDomain = baseDomain;
		this.indexDomains = dimensionDomains;
	
	}
			           
	// =========== INHERITED METHODS ============================
	
	public Domain copy() {
		Domain[] copiedIndexDomains = new Domain[this.indexDomains.length];
		for(int i=0; i<this.indexDomains.length; i++)
			copiedIndexDomains[i] = this.indexDomains[i].copy();
	
		return new ArrayDomain(this.baseDomain.copy(),
				                copiedIndexDomains);
	}

	public Domain evaluate() {
		this.baseDomain = this.baseDomain.evaluate();
		for(int i=0; i<this.indexDomains.length; i++)
			this.indexDomains[i] = this.indexDomains[i].evaluate();
		return this;
	}

	public int getType() {
		return ARRAY;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		
		for(int i=0; i<this.indexDomains.length; i++)
			this.indexDomains[i] = this.indexDomains[i].insertValueForVariable(value, variableName);
		
		return this;
	}

	public boolean isConstantDomain() {
		return this.baseDomain.isConstantDomain();
	}

	public String toString() {
		String s = "matrix indexed by : [ "+this.indexDomains[0];
		
		for(int i=1; i<this.indexDomains.length; i++)
			s = s.concat(","+indexDomains[i].toString());
		
		
		return s+" ] of "+this.baseDomain.toString();
	}
	
	
	// ================= OTHER METHODS ======================================
	
	public Domain getBaseDomain() {
		return this.baseDomain;
	}
	
	
	public Domain[] getIndexDomains() {
		return this.indexDomains;
	}
}
