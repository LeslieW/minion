package translator.expression;

public class ConstantVector implements ConstantArray {

	private String arrayName;
	private int[] elements;
	private Domain domain;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ========= CONSTRUCTOR =====================
	
	
	public ConstantVector(String arrayName,
			              int[] elements) {
		
		this.arrayName = arrayName;
		this.elements = elements;
	}
	
	public ConstantVector(String arrayName,
						  Integer[] values) {
		this.arrayName = arrayName;
		this.elements = new int[values.length];
		for(int i=0; i<this.elements.length; i++)
			elements[i] = (int) values[i];
	}
	
	public ConstantVector(String arrayName,
						  Integer[] values,
						  Domain domain) {
		this.arrayName = arrayName;
		this.elements = new int[values.length];
		for(int i=0; i<this.elements.length; i++)
			elements[i] = (int) values[i];
		this.domain = domain;
	}
	
	// =========== INHERITED METHIODS =================
	
	public String getArrayName() {
		return this.arrayName;
	}

	public int getDimension() {
		return 1;
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

	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
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
	
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		if(this.domain != null)
			this.domain = this.domain.replaceVariableWithDomain(variableName, domain);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		
		return this;
	}
	
	
	public boolean isLinearExpression() {
		return false;
	}
	
	public String toSolverExpression(translator.solver.TargetSolver solver) 
	throws Exception {
		
		throw new Exception("Internal error. Cannot give direct solver representation of expression '"+this
			+"' for solver "+solver.getSolverName());
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
	
	public Domain getArrayDomain() {
		return this.domain;
	}
	
	public void setArrayDomain(ArrayDomain domain) {
		this.domain = domain;
	}
	
	public int[] getIndexOffsets() {
		
		if(this.domain != null) {
			domain = domain.evaluate();
			if(domain instanceof ConstantArrayDomain) { 
				ConstantArrayDomain constDomain = (ConstantArrayDomain) domain;
				int[] offsets = new int[constDomain.getIndexDomains().length];
				for(int i=0; i<offsets.length; i++) {
					offsets[i] = constDomain.getIndexDomains()[i].getRange()[0];
				}
				return offsets;
			}
		}
		return new int[0];
	}
}
