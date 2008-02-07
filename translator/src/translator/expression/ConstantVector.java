package translator.expression;

public class ConstantVector implements ConstantArray {

	private String arrayName;
	private int[] elements;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ========= CONSTRUCTOR =====================
	
	
	public ConstantVector(String arrayName,
			              int[] elements) {
		
		this.arrayName = arrayName;
		this.elements = elements;
	}
	
	
	// =========== INHERITED METHIODS =================
	
	public String getArrayName() {
		return this.arrayName;
	}

	public Expression copy() {
		int[] copiedElements = new int[this.elements.length];
		for(int i=0; i<copiedElements.length; i++)
			copiedElements[i] = elements[i];
		
 		
		return new ConstantVector(new String(this.arrayName),
				                  copiedElements);
	}

	public Expression evaluate() {
		return this;
	}

	public int[] getDomain() {
		return new int[] {LOWER_BOUND, UPPER_BOUND};
	}

	public int getType() {
		return Expression.CONSTANT_VECTOR;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		return this;
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		
		ConstantVector otherVector = (ConstantVector) e;
		
		if(this.elements.length == otherVector.elements.length) {
			
			for(int i=0; i<this.elements.length; i++) {
				
				if(this.elements[i] != otherVector.elements[i]) 
					return (this.elements[i] == otherVector.elements[i]) ?
						SMALLER : BIGGER;
			}
			return EQUAL;
		}
		else return (this.elements.length < otherVector.elements.length) ?
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
		String s = arrayName+" be [";
		
		for(int i=0; i<this.elements.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(elements[i]+"");
		}
		
		return s+"]";
	}
	
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		return this;
	}
	
	// ========= ADDITIONAL METHODS =======================
	
	public int[] getElements() {
		return this.elements;
	}
	
	public int getElementAt(int index) 
		throws Exception {
		if(index <this.elements.length && index >= 0)
			return elements[index];
		else throw new Exception("Index '"+index+"' for constant array '"+this+"' is out of bounds.");
	}
	
	
}
