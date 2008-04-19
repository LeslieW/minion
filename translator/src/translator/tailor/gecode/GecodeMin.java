package translator.tailor.gecode;

/**
 * Class for both minimum and maximum constraint
 * 
 * @author andrea
 *
 */

public class GecodeMin extends RelationalConstraint {

	private GecodeIntVar firstArgument;
	private GecodeIntVar secondArgument;
	private GecodeIntVarArgs intArguments;
	
	private GecodeIntVar minimum;
	private boolean isMinimum = true;
	
	
	/** 
	 * Case: min(x0,x1) = y 
	 * 
	 * @param first x0
	 * @param second x1
	 * @param minimum y
	 */
	public GecodeMin(GecodeIntVar first,
					 GecodeIntVar second,
					 GecodeIntVar minimum) {
		this.firstArgument = first;
		this.secondArgument = second;
		this.minimum = minimum;
		this.isMinimum = true;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND};
	}
	
	/** 
	 * Case: min(x0,x1) = y   if isMinimum==true
	 *       maz(x0,x1) = y   otherwise
	 * 
	 * @param first x0
	 * @param second x1
	 * @param minimum y
	 */
	public GecodeMin(GecodeIntVar first,
					 GecodeIntVar second,
					 GecodeIntVar minimum,
					 boolean isMinimum) {
		this.firstArgument = first;
		this.secondArgument = second;
		this.minimum = minimum;
		this.isMinimum = isMinimum;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND};
	}
	
	/**
	 * Case:  min{X} = y where X is an array of integers
	 * and y an integer variable
	 * 
	 * @param argumentArray
	 * @param minimum
	 */
	public GecodeMin(GecodeIntVarArgs argumentArray,
					 GecodeIntVar minimum) {
		this.intArguments = argumentArray;
		this.minimum = minimum;
		this.isMinimum = true;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND};
	}
	
	/**
	 * Case:  min{X} = y where X is an array of integers
	 * and y an integer variable
	 *  if isMinimum == true; otherwise:
	 *   max{X} = y
	 * 
	 * @param argumentArray
	 * @param minimum
	 * @param isMinimum
	 */
	public GecodeMin(GecodeIntVarArgs argumentArray,
			         GecodeIntVar minimum,
			         boolean isMinimum) {
		this.intArguments = argumentArray;
		this.minimum = minimum;
		this.isMinimum = isMinimum;
		
		this.supportedConsistencyLevels = new char[] {GecodeConstraint.ICL_BND};
	}	
	
	//================= INHERITED METHODS =================================
	
	public String toCCString() {
		
		StringBuffer s = (this.isMinimum) ? 
				new StringBuffer("min(this, ") : 
					new StringBuffer("max(this, ") ;
		
		if(this.firstArgument != null) {
			s.append(this.firstArgument+", "+this.secondArgument);
		}
		else {
			s.append(this.intArguments);
		}
		
		s.append(", "+this.minimum);
	
		if(!(this.consistencyLevel == GecodeConstraint.ICL_DEF && 
				this.propagationKind == GecodeConstraint.PK_DEF)) {
			s.append(", "+consistencyToString(this.consistencyLevel)+", "+propagationToString(this.propagationKind));
		}
		
		s.append(")");	
		
		return s.toString();
	}
	
	
}
