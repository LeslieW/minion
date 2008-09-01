package translator.tailor.gecode;

public class GecodeIntVarArgs implements IntegerVariable, ArgsArrayVariable {

	//private GecodeAtom[] variables;
	private String name;
	private int length;
	private int ub;
	private int lb;
	
	// we need this to represent parts of an existing array (for instance the row of an array)
	private int[] indexDomains;
	private int[] referencedIndices; 
	private String originalName;
	
	public GecodeIntVarArgs(String name,
			                int length,
			                int lb, 
			                int ub) {
		this.length = length;
		this.name = name;
		this.originalName = name;
		this.lb = lb;
		this.ub = ub;
	}
	
	/** constructor for parts of arrays (translator.expression.IndexedArray)*/
	public GecodeIntVarArgs(String name,
							int[] indices,
							int[] indexRanges,
							int lb, 
							int ub) {
		this.indexDomains = indices;
		this.referencedIndices = indexRanges;
		this.length = indices.length;
		
		this.originalName = name;
		this.name = this.computePartialArrayName(name);
		
		this.lb = lb;
		this.ub = ub;
     }

	
	
	//========== INHERITED METHODS ==================
	
	public int[] getBounds() {
		return new int[] {lb, ub};
	}

	public int getLowerBound() {
		return this.lb;
	}
	
	public int getUpperBound() {
		return this.lb;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public boolean isArgsVariable() {
		return true;
	}

	public char getType() {
		return GecodeVariable.INT_ARG_VAR;
	}

	public String getVariableName() {
		return this.name;
	}

	public String toString() { 
		return this.name;
	}
	
	
	public String toDeclarationCCString() {
		return "IntVarArgs "+name+"("+this.length+")";
	}
	
	public boolean isIndexedArray() {
		return this.referencedIndices != null;
	}

	public int[] getIndexDomains() {
		return this.indexDomains;
	}
	
	public int[] getReferencedIndex() {
		return this.referencedIndices;
	}
	
	public String getArrayDefinition() { //throws GecodeException {
		
		StringBuffer s = new StringBuffer("");
		
		if(this.referencedIndices == null) 
			return s.toString(); //throw new GecodeException("Internal error. Cannot compute arra definition of non-partial array "+this);
		
		// the declaration of the args variable
		s.append("\tIntVarArgs "+this.name+"("+this.length+");\n");
	
		// assigning the values to the args array
		if(this.length == 1) {
			if(this.referencedIndices[0] == GecodeConstraint.WHOLE_RANGE_REFERENCED) {
				s.append("\tfor(int i=0; i<"+this.indexDomains[0]+"; i++)\n");
				s.append("\t   "+this.name+"[i] = "+this.originalName+"[i];\n");
			}
			else return ""; //throw new GecodeException("Internal error. No whole range specified in args array:"+this);
		}
		else if(this.length == 2) {
		
			if(this.referencedIndices[0] == GecodeConstraint.WHOLE_RANGE_REFERENCED) {
				s.append("\tfor(int i=0; i<"+this.indexDomains[0]+"; i++)\n");
				
				// M[..,..]
				if(this.referencedIndices[1] == GecodeConstraint.WHOLE_RANGE_REFERENCED) {
					s.append("\t   for(int j=0; j<"+this.indexDomains[1]+"; j++)\n");
					s.append("\t       "+this.name+"[i*"+this.indexDomains[0]+" +j)] = "+this.originalName+"[i][j];\n");
				}
				// M[..,c]
				else {
					s.append("\t   "+this.name+"[i] = "+this.originalName+"[i]["+this.referencedIndices[1]+"];\n");
				}
			}
			
			// M[c,..]
			else if(this.referencedIndices[1] == GecodeConstraint.WHOLE_RANGE_REFERENCED) {
				s.append("\tfor(int i=0; i<"+this.indexDomains[1]+"; i++)\n");
				s.append("\t   "+this.name+"[i] = "+this.originalName+"["+this.referencedIndices[1]+"][i];\n");
			}
			
			else return ""; //throw new GecodeException("Internal error. No whole range specified in args array:"+this);
		}
		else return ""; //throw new GecodeException("Cannot write array definition of 3 or multi-dimensional array "+this+" yet. sorry.");	
		
		return s.toString();
	}
	
	 // ============= ADDITIONAL METHODS =================
	
	
	public void addLowerBound(int newLb) {
		if(newLb < lb)
			this.lb = newLb;
	}

	
	public void addUpperBound(int newUb) {
		if(newUb > ub)
			this.ub = newUb;
	}
	
	public void increaseLength() {
		this.length++;
	}
	
	public void setLowerBound(int lb) {
		this.lb = lb;
	}
	
	public void setUpperBound(int ub) {
		this.ub = ub;
	}
	
	/**
	 * computes the name of a partial array.
	 * 
	 * @param origArrayName
	 * @return
	 */
	private String computePartialArrayName(String origArrayName) {
		
		String name = origArrayName+GecodeConstraint.ARRAY_PARTS_SUFFIX;
		
		/*StringBuffer s = new StringBuffer("_");
		if(this.referencedIndices != null) {
			for(int i=0; i<this.referencedIndices.length; i++ )
				if(referencedIndices[i] == GecodeConstraint.WHOLE_RANGE_REFERENCED)
					s.append("_");
				else s.append(referencedIndices[i]);
		}
		name +=s.toString(); */
		
		java.util.Random randomGenerator = new java.util.Random(GecodeConstraint.RANDOM_MAXIMUM);
		name += randomGenerator.nextInt();
		
		if(name.length() > GecodeConstraint.MAX_VARIABLE_LENGTH) {
			return name.toString().substring(name.length()-GecodeConstraint.MAX_VARIABLE_LENGTH);
		}
		
		return name;
	}
	

}
