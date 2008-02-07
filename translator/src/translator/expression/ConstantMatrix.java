package translator.expression;

public class ConstantMatrix implements ConstantArray {

	private String arrayName;
	private int[][] elements;
	
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ========== CONSTRUCTOR ===================
	
	public ConstantMatrix(String arrayName,
			              int[][] elements) {
		this.arrayName = arrayName;
		this.elements = elements;
	}
	
	
	// =========== INHERITED METHODS ===========
	
	public String getArrayName() {
		return this.arrayName;
	}

	public Expression copy() {
		int [][] copiedElements = new int[elements.length][this.elements[0].length];
		for(int i=0; i<copiedElements.length; i++) {
			for(int j=0; j<copiedElements[0].length; j++)
				copiedElements[i][j] = elements[i][j];
			
		}
		return new ConstantMatrix(new String(this.arrayName),
				                  copiedElements);
	}

	public Expression evaluate() {
		return this;
	}

	public int[] getDomain() {
		return new int[] {LOWER_BOUND, UPPER_BOUND};
	}

	public int getType() {
		return Expression.CONSTANT_MATRIX;
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
		ConstantMatrix otherMatrix = (ConstantMatrix) e;
		
		if(this.elements.length == otherMatrix.elements.length) {
			
			if(this.elements[0].length == otherMatrix.elements[0].length) {
			
				for(int i=0; i<this.elements.length; i++) {
					for(int j=0; j<elements[0].length; j++) {
						if(this.elements[i][j] != otherMatrix.elements[i][j]) 
							return (this.elements[i][j] < otherMatrix.elements[i][j]) ?
								SMALLER : BIGGER;
					}
				}
				return EQUAL;
			}
			else return (this.elements[0].length < otherMatrix.elements[0].length) ?
					SMALLER : BIGGER;
		}
		else return (this.elements.length < otherMatrix.elements.length) ?
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
	
	public Expression insertDomainForVariable(Domain domain, String variable) {
		return this;
	}
	

	public String toString() {
		String s = arrayName+": [";
		
		for(int row=0; row<this.elements.length; row++) {
			if(row >0) 
				s = s.concat(",\n[");
			
			for(int col =0; col<this.elements[row].length; col++) {
				if(col==0) s =s.concat("[");
				if(col >0 ) s = s.concat(",");
				s = s.concat(elements[row][col]+"");
			}
			s = s.concat("]");
		}
		
		return s+"]";
	}

	
	// ========= ADDITIONAL METHODS =======================
	
	public int[][] getElements() {
		return this.elements;
	}
	
	public int getElementAt(int rowIndex, int colIndex) 
		throws Exception {
		
		//System.out.println("Getting element at row:"+rowIndex+" and col:"+colIndex+" from constant array:\n"+this.toString());
		
		if(rowIndex <this.elements.length && rowIndex >= 0) {
			if(colIndex <this.elements[0].length && colIndex >= 0) 
				return elements[rowIndex][colIndex];
			else throw new Exception("The column-Index '"+colIndex+"' for constant array '"+this+"' is out of bounds.\n"+
					"Feasible bounds are: rows: 1.."+(elements.length-1)+", cols: 1.."+(elements[0].length-1));
		}
		else throw new Exception("The row-Index '"+rowIndex+"' for constant array '"+this+"' is out of bounds.\n"+
				"Feasible bounds are: rows: 1.."+(elements.length-1)+", cols: 1.."+(elements[0].length-1));
	}
}
