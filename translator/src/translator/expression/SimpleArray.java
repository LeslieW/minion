package translator.expression;

public class SimpleArray implements SingleArray {

	private String arrayName;
	private Domain baseDomain;
	private BasicDomain[] indexDomains;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	/** this flag indicates if the array should be later (in the tailor for the solver) flattened  to a vector*/
	private boolean willBeFlattenedToVector = false;
	
	// =========== CONSTRUCOR ===============================
	
	public SimpleArray(String arrayName,
					   BasicDomain[] indices,
					   Domain baseDomain) {
		this.arrayName = arrayName;
		this.baseDomain = baseDomain;
		this.indexDomains = indices;
	}

	// =================== INHERITED METHODS =====================
	
	public String getArrayName() {
		return this.arrayName;
	}

	public Domain getBaseDomain() {
		return this.baseDomain;
	}

	public BasicDomain[] getIndexDomains() {
		return this.indexDomains;
	}
	
	public Expression copy() {
		
		BasicDomain[] copiedIndices = new BasicDomain[this.indexDomains.length];
		for(int i=0; i<copiedIndices.length; i++ )
			copiedIndices[i] = (BasicDomain) this.indexDomains[i].copy();
		
		return new SimpleArray(new String(this.arrayName),
								copiedIndices,
				                this.baseDomain.copy());
	}

	public Expression evaluate() {
		this.baseDomain = this.baseDomain.evaluate();
		
		for(int i=0; i<this.indexDomains.length; i++) {
			this.indexDomains[i] = (BasicDomain) indexDomains[i].evaluate();
		}
		
		return this;
	}

	public int[] getDomain() {
		if(this.baseDomain instanceof ConstantDomain)
			return ((ConstantDomain) baseDomain).getRange();
		else return new int[] {LOWER_BOUND, UPPER_BOUND};
	}

	public int getType() {
		return Expression.SIMPLE_ARRAY;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		//this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		SimpleArray otherArray = (SimpleArray) e;
		
		int difference = this.arrayName.compareTo(otherArray.arrayName);
		if(difference == 0) return EQUAL;
		else return (difference < 0) ?
				SMALLER : BIGGER;
	}

	public void orderExpression() {
		// do nothing

	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		
		if(this.baseDomain instanceof IdentifierDomain) {
			String domainName = ((IdentifierDomain) baseDomain).getDomainName();
			
			if(domainName.equals(variableName)) 
				this.baseDomain = domain;	
		}
		
		
		for(int i=0; i<this.indexDomains.length; i++) {
			if(this.indexDomains[i] instanceof IdentifierDomain) {
				String domainName = ((IdentifierDomain) indexDomains[i]).getDomainName();
				
				if(domainName.equals(variableName) && domain instanceof BasicDomain) 
					this.indexDomains[i]= (BasicDomain) domain;
			}
		}
		
		
		return this;
	}
	
	

	public Expression reduceExpressionTree() {
		return this;
	}

	public Expression restructure() {
		return this;
	}

	public void setIsNotNested() {
		this.isNested = false;

	}

	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeFlattenedToVariable = reified;

	}

	public String toString() {
		
		return this.arrayName;
		
	}
	
	// ================= ADDITIONAL METHODS =============================
	
	public boolean willBeFlattenedToVector() {
		return this.willBeFlattenedToVector;
	}
	
	public void setWillBeFlattenedToVector(boolean turnOn) {
		this.willBeFlattenedToVector = turnOn;
	}
	
	
}
