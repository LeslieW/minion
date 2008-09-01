package translator.tailor.gecode;

public class GecodeBoolVarArgs implements ArgsArrayVariable, BooleanVariable {

	private String name;
	private int length;
	
//	 we need this to represent parts of an existing array (for instance the row of an array)
	private int[] indexDomains;
	private int[][] referencedIndices; 
	private String originalName;
	
	public GecodeBoolVarArgs(String name,
							 int length) {
		this.length = length;
		this.name = name;
		this.originalName = name;
	}
	
	/** constructor for parts of arrays (translator.expression.IndexedArray)*/
	public GecodeBoolVarArgs(String name,
							int[] indices,
							int[][] indexRanges) {
		this.indexDomains = indices;
		this.referencedIndices = indexRanges;
		this.length = 1;
		for(int i=0; i<indices.length; i++) {
			if(this.referencedIndices[i][0] != this.referencedIndices[i][1]) 
				length *= this.referencedIndices[i][1] - this.referencedIndices[i][0] + 1;
		}
		this.originalName = name;
		this.name = this.computePartialArrayName(name);
     }
	
	// ================ INHERITED METHODS =========================
	
	
	public int[] getBounds() {
		return new int[] {0,1};
	}

	public char getType() {
		return GecodeVariable.BOOL_ARG_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public boolean isArgsVariable() {
		return true;
	}

	public String toString() {
		return this.name;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public int getLowerBound() {
		return 0;
	}
	
	public int getUpperBound() {
		return 1;
	}
	
	public String toDeclarationCCString() {
		return "BoolVarArgs "+name+"("+this.length+")";
	}
	
	public boolean isIndexedArray() {
		return this.referencedIndices != null;
	}

	public int[] getIndexDomains() {
		return this.indexDomains;
	}
	
	public int[][] getReferencedIndex() {
		return this.referencedIndices;
	}
	
    public String getArrayDefinition()  { // sthrows GecodeException {
	
		if(this.referencedIndices == null) 
			return ""; //throw new GecodeException("Internal error. Cannot compute arra definition of non-partial array "+this);
		
		StringBuffer s = new StringBuffer("");
		
		// the declaration of the args variable
		s.append("BoolVarArgs "+this.name+"("+this.length+");\n");
	
		// assigning the values to the args array
		if(this.indexDomains.length == 1) {
				s.append("\tfor(int i="+this.referencedIndices[0][0]+"; i<="+this.referencedIndices[0][1]+"; i++)\n");
				s.append("\t   "+this.name+"[i] = "+this.originalName+"[i];\n");
		}
		else if(this.indexDomains.length == 2) {
		
			if(this.referencedIndices[0][0] != this.referencedIndices[0][1]) {
				s.append("\tfor(int i=0; i<="+this.indexDomains[0]+"; i++)\n");
				
				// M[..,..]
				if(this.referencedIndices[1][0] != this.referencedIndices[1][1]) {
					s.append("\t   for(int j="+this.referencedIndices[0][0]+"; j<="+this.referencedIndices[0][1]+"; j++)\n");
					s.append("\t       "+this.name+"[i*"+this.indexDomains[0]+" +j)] = "+this.originalName+"(i,j);\n");
				}
				// M[..,c]
				else {
					s.append("\t   "+this.name+"[i] = "+this.originalName+"(i,"+this.referencedIndices[1][0]+");\n");
				}
			}
			
			// M[c,..]
			else  {
				s.append("\tfor(int i="+this.referencedIndices[1][0]+"; i<="+this.referencedIndices[1][1]+"; i++)\n");
				s.append("\t   "+this.name+"[i] = "+this.originalName+"("+this.referencedIndices[0][1]+",i);\n");
			}
			
		}
		else return "oioioi"; //throw new GecodeException("Cannot write array definition of 3 or multi-dimensional array "+this+" yet. sorry.");	
		
		return s.toString();
	}
	
	
	// ============= ADDITIONAL METHODS ===============
	
	public void increaseLength() {
		this.length++;
	}
	
	/**
	 * computes the name of a partial array. It has the same name
	 * whenever it occurs twice (so it should not be declared twice)
	 * 
	 * @param origArrayName
	 * @return
	 */
	private String computePartialArrayName(String origArrayName) {
		
		String name = origArrayName+GecodeConstraint.ARRAY_PARTS_SUFFIX;
		
		StringBuffer s = new StringBuffer("_");
		if(this.referencedIndices != null) {
			for(int i=0; i<this.referencedIndices.length; i++ )
				if(referencedIndices[i][0] != referencedIndices[i][1])
					s.append(referencedIndices[i][0]+this.referencedIndices[i][1]);
				else s.append(referencedIndices[i][0]);
		}
		name +=s.toString()+"_";
		
		java.util.Random randomGenerator = new java.util.Random();		
		name += randomGenerator.nextInt(GecodeConstraint.RANDOM_MAXIMUM);
		
		if(name.length() > GecodeConstraint.MAX_VARIABLE_LENGTH-1) {
			return "_"+name.toString().substring(name.length()-GecodeConstraint.MAX_VARIABLE_LENGTH-1);
		}
		
		return name;
	}
}
