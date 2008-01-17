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
		boolean allAreConstantDomains = true;
		
		this.baseDomain = this.baseDomain.evaluate();
		allAreConstantDomains = allAreConstantDomains && (baseDomain instanceof ConstantDomain);
		
		for(int i=0; i<this.indexDomains.length; i++) {
			this.indexDomains[i] = this.indexDomains[i].evaluate();
			allAreConstantDomains = allAreConstantDomains && (indexDomains[i] instanceof ConstantDomain);
		}
			
		if(allAreConstantDomains) {
			ConstantDomain[] indices = new ConstantDomain[this.indexDomains.length];
			for(int i=0; i<this.indexDomains.length; i++) {
				indices[i] = (ConstantDomain) this.indexDomains[i];
			}
			return new ConstantArrayDomain(indices, 
										   (ConstantDomain) this.baseDomain);
		}
		
		
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
	
	public int[] getIndexOffsetsFromZero() {
		// we need the offset for every dimension
		int[] offsetsFromZero = new int[this.indexDomains.length];
		
		for(int i=0; i<this.indexDomains.length; i++) {
			Domain indexDomain = this.indexDomains[i];
			if(indexDomain.isConstantDomain()) {
				if(indexDomain instanceof BoolDomain)
					offsetsFromZero[i] = 0;
				else if(indexDomain instanceof IntRange)
					offsetsFromZero[i] = ((IntRange) indexDomain).getRange()[0];
				else if(indexDomain instanceof MultipleIntRange)
					offsetsFromZero[i] = ((MultipleIntRange) indexDomain).getRange()[0];
			
			}
			else return null; // we cannot throw an exception here
			                  // we just don't know the offsets
		}
		
		return offsetsFromZero;
	}
	
	
	public void setBaseDomain(Domain domain) 
		throws Exception {
		
		if(domain instanceof ArrayDomain)
			throw new Exception("Cannot have matrix domain as a base domain for matrix domain :"+this);
		
		else this.baseDomain = domain;
		
	}
	
	
	public void setIndexDomainAt(Domain domain, int index) 
		throws Exception {
		
		if(index < this.indexDomains.length && index >= 0) {
			if(domain instanceof ArrayDomain)
				throw new Exception("Cannot have matrix domain as a base domain for matrix domain :"+this);
			
			this.indexDomains[index] = domain;
		}
		else throw new Exception("Index '"+index+"' for index-domain of array domain '"+this+"' is out of bounds.");
	}
	
}
