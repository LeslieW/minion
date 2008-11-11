package translator.expression;

public class ConstantMatrix implements ConstantArray {

	private String arrayName;
	private int[][] elements;
	private Domain domain;
	
	private boolean willBeFlattenedToVariable = false;
	private boolean isNested = true;
	
	// ========== CONSTRUCTOR ===================
	
	public ConstantMatrix(String arrayName,
			              int[][] elements) {
		this.arrayName = arrayName;
		this.elements = elements;
	}
	
	public ConstantMatrix(String arrayName,
						  Integer[][] values) {
		this.arrayName = arrayName;
		this.elements = new int[values.length][values[0].length];
		
		for(int i=0; i<values.length; i++) {
			for(int j=0; j<values[0].length; j++) {
				this.elements[i][j] = (int) values[i][j];
			}
		}
		
	}
	
	
	public ConstantMatrix(String arrayName,
					      Integer[][] values,
					      Domain domain) {
		
		this.arrayName = arrayName;
		this.elements = new int[values.length][values[0].length];
		
		for(int i=0; i<values.length; i++) {
			for(int j=0; j<values[0].length; j++) {
				this.elements[i][j] = (int) values[i][j];
			}
		}
		this.domain = domain;
		//System.out.println("Created constant matrix with domain:"+domain);	
	}
	
	// =========== INHERITED METHODS ===========
	
	public String getArrayName() {
		return this.arrayName;
	}

	public int getDimension() {
		return 2;
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
	
	public Expression insertDomainForVariable(Domain domain, String variable) throws Exception {
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
	
	public int[][] getElements() {
		return this.elements;
	}
	
	public int getElementAt(int rowIndex, int colIndex) 
		throws Exception {
		
		//System.out.println("Getting element at row:"+rowIndex+" and col:"+colIndex+" from constant array:\n"+this.toString());
		
		if(rowIndex <this.elements.length && rowIndex >= 0) {
			if(colIndex <this.elements[0].length && colIndex >= 0) 
				return elements[rowIndex][colIndex];
			else {
				int[] offsets = this.getIndexOffsets();
				if(offsets.length == 0)
					throw new Exception("The column-Index '"+colIndex+"' for constant array '"+this.arrayName+"' is out of bounds.\n"+	
							"Feasible bounds are: rows: 0.."+(elements.length-1)+", cols: 0.."+(elements[0].length-1));
				else {
					throw new Exception("The column-Index '"+(colIndex+offsets[1])+"' for constant array '"+this.arrayName+"' is out of bounds.\n"+	
							"Feasible bounds are: rows: "+offsets[0]+".."+(elements.length-1+offsets[0])+
							", cols: "+(offsets[1])+".."+(elements[0].length-1+offsets[1]));
				}
			}
		}
		else {
			int[] offsets = this.getIndexOffsets();
			if(offsets.length == 0)
				throw new Exception("The row-Index '"+rowIndex+"' for constant array '"+this.arrayName+"' is out of bounds.\n"+
				"Feasible bounds are: rows: 0.."+(elements.length-1)+", cols: 0.."+(elements[0].length-1));
			else 
				throw new Exception("The row-Index '"+(rowIndex+offsets[0])+"' for constant array '"+this.arrayName+"' is out of bounds.\n"+
						"Feasible bounds are: rows: "+offsets[0]+".."+(elements.length-1+offsets[0])+
						", cols: "+(offsets[1])+".."+(elements[0].length-1+offsets[1]));
		}
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
	
	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}
}
