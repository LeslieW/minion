package translator.tailor.gecode;

/**
 * Represents Gecode's variants of the element constraint (v1.5.1)
 * 
 * @author andrea
 *
 */

public class GecodeElement extends RelationalConstraint {

	// array[index] = value
	private ArgsAtom array;
	private GecodeVariable index;
	private GecodeAtom value;
	
	/**
	 * First case: N [x] = y  where N is a constant array and x and y
	 * are integer variables
	 * 
	 * @param constantArray N
	 * @param index x
	 * @param value y
	 */
	public GecodeElement(ConstantArgsArray constantArray,
						 GecodeIntVar index,
						 GecodeIntVar value) {
		this.array = constantArray;
		this.index = index;
		this.value = value;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
	}
	
	/**
	 * Second case: N [x] = y  where N is a constant array and x is an
	 * integer variable and y is a boolean variable
	 * 
	 * @param constantArray
	 * @param index
	 * @param value
	 */
	public GecodeElement(ConstantArgsArray constantArray,
						 GecodeIntVar index,
						 GecodeBoolVar value) {
		this.array = constantArray;
		this.index = index;
		this.value = value;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
	}
	
	/**
	 * Third case: N [x] = const  where N is a constant array and x is an
	 * integer variable and const is an integer value
	 * 
	 * @param constantArray
	 * @param index
	 * @param constValue
	 */
	public GecodeElement(ConstantArgsArray constantArray,
						 GecodeIntVar index,
						 GecodeConstant constValue) {
		this.array = constantArray;
		this.index = index;
		this.value = constValue;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
	}
	
	
	/** 
	 * Fourth case: X[index] = value where X is an 
	 * integer variable Array and 'index' an Intvar and 'value' 
	 * an integer variable 
	 * 
	 * @param variableArray X
	 * @param index 
	 * @param value
	 */
	public GecodeElement(GecodeIntVarArgs variableArray,
					     GecodeIntVar index,
					     GecodeIntVar value) {
		this.array = variableArray;
		this.index = index;
		this.value = value;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM };
	}
	
	/**
	 * Fifth case: X[index] = value where X is an 
	 * integer variable Array and 'index' an Intvar and 'value' 
	 * an integer value
	 * 
	 * @param variableArray
	 * @param index
	 * @param constValue
	 */
	public GecodeElement(GecodeIntVarArgs variableArray,
						 GecodeIntVar index, 
						 GecodeConstant constValue) {
		this.array = variableArray;
		this.index = index;
		this.value = constValue;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND,
													  GecodeConstraint.ICL_DOM };
	}
	
	
	/** 
	 * Sixth case: X[index] = value where 'X' is a 
	 * Boolean variable Array and 'index' an Intvar and 'value' 
	 * a Boolean variable
	 * 
	 * @param variableArray
	 * @param index
	 * @param value
	 */
	public GecodeElement(GecodeBoolVarArgs variableArray,
						 GecodeIntVar index,
						 GecodeBoolVar value) {
		this.array = variableArray;
		this.index = index;
		this.value = value;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
	}
	
	/** 
	 * Sixth case: X[index] = value where 'X' is a 
	 * Boolean variable Array and 'index' an Intvar and 'value' 
	 * an integer value in {0,1}
	 * 
	 * @param variableArray
	 * @param index
	 * @param constValue
	 */
	public GecodeElement(GecodeBoolVarArgs variableArray,
						 GecodeIntVar index,
						 GecodeConstant constValue) {
		this.array = variableArray;
		this.index = index;
		this.value = constValue;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_DOM };
	}
	
	// ===================== INHERITED METHODS =================
	

	public String toCCString() {
		
		StringBuffer s = new StringBuffer("element(this, ");
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.ICL_DEF) {
			s.append(this.array+", "+this.index+", "+this.value+")");
		}
		else {
			s.append(this.array+", "+this.index+", "+this.value+","
					+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		
		return s.toString();
	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("element(this, ");
		
		if(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.ICL_DEF) {
			s.append(this.array+", "+this.index+", "+this.value+")");
		}
		else {
			s.append(this.array+", "+this.index+", "+this.value+","
					+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind)+")");
		}
		
		return s.toString();
	}

}
