package translator.expression;

public class Atmost implements GlobalConstraint {

	private Array array;
	private int[] occurrenceOfVariables;
	private int[] values;
	private boolean isAtmost;
	
	boolean willBeFlattenedToVariable = false;
	boolean isNested = true;
	
	// ========== CONSTRUCTOR ======================
	
	public Atmost(Array array,
			      int[] occurrenceOfVariables,
			      int[] values,
			      boolean isAtmost) {
		this.array = array;
		this.occurrenceOfVariables = occurrenceOfVariables;
		this.values = values;
		this.isAtmost = isAtmost;
	}
 	
	// ========= INHERITED METHODS =====================
	
	public Expression[] getArguments() {
		Expression[] arguments = new Expression[1+this.occurrenceOfVariables.length+values.length]; 
		
		arguments[0] = this.array;
		for(int i=0; i<this.occurrenceOfVariables.length; i++)
			arguments[i+1] = new ArithmeticAtomExpression(this.occurrenceOfVariables[i]);
		
		for(int i=0; i<this.values.length; i++)
			arguments[1+this.occurrenceOfVariables.length+i] = new ArithmeticAtomExpression(this.values[i]);
		
		return arguments;
	}

	public Expression copy() {
		
		int[] copiedOccurrences = new int[this.occurrenceOfVariables.length];
		for(int i=0; i<this.occurrenceOfVariables.length; i++)
			copiedOccurrences[i] = this.occurrenceOfVariables[i];
		
		int[] copiedValues = new int[this.values.length];
		for(int i=0; i<this.values.length; i++)
			copiedValues[i] = this.values[i];
		
		return new Atmost((Array) this.array.copy(),
				           copiedOccurrences,
				           copiedValues,
				           this.isAtmost);
	}

	public Expression evaluate() {
		this.array = (Array) this.array.evaluate();
		
		return this;
	}

	public int[] getDomain() {
		return new int[] {0,1};
	}

	public int getType() {
		return (this.isAtmost) ?
				Expression.ATMOST_CONSTRAINT :
					Expression.ATLEAST_CONSTRAINT;
	}

	public Expression insertDomainForVariable(Domain domain, String variableName) {
		this.array = (Array) this.array.insertDomainForVariable(domain, variableName);
		
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		this.array = (Array) this.array.insertValueForVariable(value, variableName);
		
		return this;		
	}

	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeFlattenedToVariable;
	}

	public boolean isNested() {
		return this.isNested;
	}

	public char isSmallerThanSameType(Expression e) {
		Atmost otherAtmost = (Atmost) e;
		
		if(this.getType() == otherAtmost.getType()) {
			
			if(this.array.getType() == otherAtmost.array.getType()) {
				
				char diff = this.array.isSmallerThanSameType(otherAtmost.array);
				if(diff == EQUAL) {
					
					if(this.occurrenceOfVariables.length == otherAtmost.occurrenceOfVariables.length) {
						
						for(int i=0; i<this.occurrenceOfVariables.length; i++) {
							if(this.occurrenceOfVariables[i] != otherAtmost.occurrenceOfVariables[i])								
							return (this.occurrenceOfVariables[i] < otherAtmost.occurrenceOfVariables[i]) ?
									SMALLER : BIGGER;
						}
						
						for(int i=0; i<this.values.length; i++) {
							if(this.values[i] != otherAtmost.values[i])								
							return (this.values[i] < otherAtmost.values[i]) ?
									SMALLER : BIGGER;
						}
						return EQUAL;
					}
					else return (this.occurrenceOfVariables.length < otherAtmost.occurrenceOfVariables.length)  ?
							SMALLER : BIGGER;
				}
				else return diff;
			}
			else return (this.array.getType() < otherAtmost.array.getType()) ?
					SMALLER : BIGGER;
		}
		else return (this.getType() < otherAtmost.getType()) ?
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
		
		String s = (this.isAtmost) ? 
				       "atmost(" : "atleast(";
		
		s = s.concat(this.array.toString()+",[");
		
		for(int i=0; i<this.occurrenceOfVariables.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(this.occurrenceOfVariables[i]+"");
		}
		
		s= s+"], [";
		
		for(int i=0; i<this.values.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(this.values[i]+"");
		}
		
		
		
		return s+"])";
	}
	
	
	// ============== ADDITIONAL VALUES ======================
	
	public boolean isAtmost() {
		return this.isAtmost;
	}

	public int[] getOccurrences() {
		return this.occurrenceOfVariables;
	}
	
	public int[] getValues() {
		return this.values;
	}
	
	public Array getArray() {
		return this.array;
	}
	
}
