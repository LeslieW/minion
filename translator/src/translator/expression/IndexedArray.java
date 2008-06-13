package translator.expression;

/**
 * This class represents arrays that are indexed, i.e. only parts of a
 * defined array. Say x is a 2-dim. matrix over int(1..10), then 
 * x[..,2]  stands for all columns in the second row and is a one-dimensional
 * array. The difference to the class ArrayVariable is that instances of
 * this class always correspond to a 1 or more dimensional array whereas 
 * ArrayVariables are always single variables. 
 * 
 * 
 * @author andrea
 *
 */

public class IndexedArray implements SingleArray {

	private String arrayName;
	private BasicDomain[] indexRanges;
	private Domain baseDomain;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ========== CONSTRUCTOR =============
	
	public IndexedArray(String arrayName,
			     BasicDomain[] indexRanges,
			     Domain baseDomain) {
		
		this.arrayName = arrayName;
		this.indexRanges = indexRanges;
		this.baseDomain = baseDomain;
	}
	
	
	// ======== INHERITED MEHTODS ==========
	
	public Expression copy() {
		BasicDomain[] copiedIndexRanges = new BasicDomain[this.indexRanges.length];
		for(int i=0; i<this.indexRanges.length; i++)
			copiedIndexRanges[i] = (BasicDomain) this.indexRanges[i].copy();
		return new IndexedArray(new String(this.arrayName),
				         copiedIndexRanges,
				         this.baseDomain.copy());
	}

	public Expression evaluate() {
		for(int i=0; i<this.indexRanges.length; i++)
			this.indexRanges[i] = (BasicDomain) this.indexRanges[i].evaluate();
	
		return this;
	}

	public Domain getBaseDomain() {
		return this.baseDomain;
	}
	
	public String getArrayName() {
		return this.arrayName;
	}
	
	public int[] getDomain() {
		if(this.baseDomain instanceof ConstantDomain) {
			return ((ConstantDomain) baseDomain).getRange();
		}
		else return new int[] { Expression.LOWER_BOUND, Expression.UPPER_BOUND };
	}

	public int getType() {
		return Expression.INDEXED_ARRAY;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.indexRanges.length; i++)
			this.indexRanges[i] = (BasicDomain) this.indexRanges[i].insertValueForVariable(value, variableName);
		
		this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		//for(int i=0; i<this.indexRanges.length; i++)
		//	this.indexRanges[i] = (BasicDomain) this.indexRanges[i].insertValueForVariable(value, variableName);
		
		//this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		// TODO!
		//this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		
		int f; // do the same for domains!
		
		return this;	
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		IndexedArray otherArray = (IndexedArray) e;
		if(this.arrayName.compareTo(otherArray.arrayName) == 0) {
			
			if(this.indexRanges.length == otherArray.indexRanges.length) {
				
				for(int i=0; i<this.indexRanges.length; i++) {
					
					if(indexRanges[i].getType() == otherArray.indexRanges[i].getType()) {
						
						char difference = indexRanges[i].isSmallerThanSameType(otherArray.indexRanges[i]);
						if(difference != EQUAL) return difference;
						
					}
					else return (indexRanges[i].getType() < otherArray.indexRanges[i].getType()) ?
							SMALLER : BIGGER;
				}
				return EQUAL;
			
			} else return (this.indexRanges.length < otherArray.indexRanges.length) ?
					SMALLER : BIGGER;
		}
		else return (this.arrayName.compareTo(otherArray.arrayName) < 0) ?
				SMALLER : BIGGER;
		
	}

	public void orderExpression() {
		// do nothing
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
		
		String s = this.arrayName+"[";
		for(int i=0; i<this.indexRanges.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(indexRanges[i].toString()); 
		}
		return s+"]";
	}
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		
		if(this.baseDomain instanceof IdentifierDomain) {
			String domainName = ((IdentifierDomain) baseDomain).getDomainName();
			
			if(domainName.equals(variableName)) 
				this.baseDomain = domain;	
		}
		
		
		for(int i=0; i<this.indexRanges.length; i++) {
			if(this.indexRanges[i] instanceof IdentifierDomain) {
				String domainName = ((IdentifierDomain) indexRanges[i]).getDomainName();
				
				if(domainName.equals(variableName) && domain instanceof BasicDomain) 
					this.indexRanges[i]= (BasicDomain) domain;
			}
		}
		
		
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		if(this.arrayName.equals(oldVariable.getVariableName()))
			return newVariable;
		return this;
	}
	
	 // =================== ADDITIONAL METHODS ===============================================
	
	
	public boolean hasConstantIndices() {
		
		boolean indicesAreConstant = true;
		
		for(int i=0; i<this.indexRanges.length; i++) {
			indicesAreConstant = indicesAreConstant && (indexRanges[i] instanceof ConstantDomain);
		}
		
		return indicesAreConstant;
	}
	
	public BasicDomain[] getIndexRanges() {
		return this.indexRanges;
	}
	
}
