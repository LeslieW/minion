package translator.expression;

public class ComposedArray implements Array {

	private Domain baseDomain;
	private SingleArray[] arrayList;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ============= CONSTRUCTOR ===================
	
	public ComposedArray(Domain baseDomain,
						 SingleArray[] arrayList) {
		
		this.baseDomain = baseDomain;
		this.arrayList = arrayList;
	}
	
	
	// ========= INHERITED METHODS ================

	public Domain getBaseDomain() {
		return this.baseDomain;
	}

	public Expression copy() {
		SingleArray[] copiedArrayList = new SingleArray[this.arrayList.length];
		for(int i=0; i<this.arrayList.length; i++)
			copiedArrayList[i] = (SingleArray) this.arrayList[i].copy();
		
		return new ComposedArray(this.baseDomain.copy(),
								 copiedArrayList);
	}

	public Expression evaluate() {
		for(int i=0; i<this.arrayList.length; i++)
			this.arrayList[i] = (SingleArray) this.arrayList[i].evaluate();
		
		return this;
	}

	public int[] getDomain() {
		if(this.baseDomain instanceof ConstantDomain) {
			return ((ConstantDomain) baseDomain).getRange();
		}
		else return new int[] { Expression.LOWER_BOUND, Expression.UPPER_BOUND };
	}

	public int getType() {
		return Expression.COMPOSED_ARRAY;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.arrayList.length; i++)
			this.arrayList[i] = (SingleArray) this.arrayList[i].insertValueForVariable(value, variableName);
		
		this.baseDomain = this.baseDomain.insertValueForVariable(value, variableName);
		
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		for(int i=0; i<this.arrayList.length; i++)
			this.arrayList[i] = (SingleArray) this.arrayList[i].insertValueForVariable(value, variableName);
		
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
		
		ComposedArray otherArray = (ComposedArray) e;
		
		if(this.arrayList.length == otherArray.arrayList.length) {
		
			for(int i=0; i<this.arrayList.length; i++) {
				if(arrayList[i].getType() == otherArray.arrayList[i].getType()) {
					char difference = this.arrayList[i].isSmallerThanSameType(otherArray.arrayList[i]);
					if(difference != EQUAL)
						return difference;
				}
				else return (arrayList[i].getType() < otherArray.arrayList[i].getType()) ?
						SMALLER : BIGGER;
			}
			return EQUAL;
			
			
		} else return (this.arrayList.length < otherArray.arrayList.length) ?
				SMALLER : BIGGER;
	
	}

	public void orderExpression() {
		int f;
		// we have to order the list!
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

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		
		return this;
	}
}
