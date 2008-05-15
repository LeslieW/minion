package translator.expression;

import java.util.ArrayList;

public class Atmost implements GlobalConstraint {

	private Array array;
	private Expression arrayExpression;
	private int[] occurrenceOfVariables;
	private int[] values;
	private boolean isAtmost;
	
	private ArrayList<Expression> occurrenceList = new ArrayList<Expression> ();
	private ArrayList<Expression> valuesList = new ArrayList<Expression> ();
	
	private String occurrencesVectorName;
	private String valuesVectorName;
	
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
	
	public Atmost(Expression array,
		      int[] occurrenceOfVariables,
		      int[] values,
		      boolean isAtmost) {
		this.arrayExpression= array;
		this.occurrenceOfVariables = occurrenceOfVariables;
		this.values = values;
		this.isAtmost = isAtmost;
}
	
	public Atmost(Expression array,
				  String occurrences,
				  String values,
				  boolean isAtmost) {
		
		this.arrayExpression = array;
		this.occurrencesVectorName = occurrences;
		this.valuesVectorName = values;
		this.isAtmost = isAtmost;
		
		this.occurrenceOfVariables = new int[0];
		this.values = new int[0];
	}
	
	public Atmost(Expression array,
			  int[] occurrenceList,
			  String values,
			  boolean isAtmost) {
	
		this.arrayExpression = array;
		this.valuesVectorName = values;
		this.isAtmost = isAtmost;
	
		this.occurrenceOfVariables = occurrenceList;
		this.values = new int[0];
	}
	
	public Atmost(Expression array,
			  String occurrenceName,
			  int[] values,
			  boolean isAtmost) {
	
		this.arrayExpression = array;
		this.occurrencesVectorName = occurrenceName;
		this.isAtmost = isAtmost;
	
		this.occurrenceOfVariables = new int[0];
		this.values = values;
	}
	
	
	public Atmost(Expression array,
				  ArrayList<Expression> occurrenceList,
				  ArrayList<Expression> valuesList,
				  boolean isAtmost) {
		
		this.arrayExpression = array;
		this.occurrenceList = occurrenceList;
		this.valuesList =  valuesList;
		this.isAtmost = isAtmost;
		
		this.occurrenceOfVariables = new int[0];
		this.values = new int[0];
		
		//System.out.println("Created new object of atmost/atleast:"+this);
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
		
		if(array!=null) 
			return new Atmost((Array) this.array.copy(),
				           copiedOccurrences,
				           copiedValues,
				           this.isAtmost);
		
		else return new Atmost(this.arrayExpression.copy(),
		           copiedOccurrences,
		           copiedValues,
		           this.isAtmost);
	}

	public Expression evaluate() {
		
		if(array != null)
			this.array = (Array) this.array.evaluate();
		
		else {
			this.arrayExpression = arrayExpression.evaluate();
			if(arrayExpression instanceof Array)
				this.array = (Array) arrayExpression;
		}
		
		if(this.occurrenceList.size() > 0) {	
			boolean allAreInts = true;
			for(int i=this.occurrenceList.size()-1; i>=0;i--) {
				Expression e = occurrenceList.remove(i);
				e = e.evaluate();
				occurrenceList.add(i,e);
				if(e.getType() != INT)
					allAreInts = false;
			}
			
			if(allAreInts) {
				this.occurrenceOfVariables = new int[this.occurrenceList.size()];
				for(int i=this.occurrenceList.size()-1;i >=0; i--) {
					this.occurrenceOfVariables[i] = ((ArithmeticAtomExpression) occurrenceList.remove(i)).getConstant();
				}
			}
			
		}
		
		if(this.valuesList.size() > 0) {	
			boolean allAreInts = true;
			for(int i=this.valuesList.size()-1; i>=0;i--) {
				Expression e = valuesList.remove(i);
				e = e.evaluate();
				valuesList.add(i,e);
				if(e.getType() != INT)
					allAreInts = false;
			}

			if(allAreInts) {
				this.values = new int[this.valuesList.size()];
				for(int i=this.valuesList.size()-1;i >=0; i--) {
					this.values[i] = ((ArithmeticAtomExpression) valuesList.remove(i)).getConstant();
				}
			}
			
		}
		
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

	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		if(array != null)
			this.array = (Array) this.array.insertDomainForVariable(domain, variableName);
		
		else {
			this.arrayExpression = this.arrayExpression.insertDomainForVariable(domain, variableName);
			if(arrayExpression instanceof Array)
				this.array = (Array) array;
		}
		
		return this;
	}

	public Expression insertValueForVariable(int value, String variableName) {
		
		if(array != null)
			this.array = (Array) this.array.insertValueForVariable(value, variableName);
		else {
			this.arrayExpression = this.arrayExpression.insertValueForVariable(value, variableName);
		}
		
		if(this.occurrenceList.size() > 0) {		
			for(int i=this.occurrenceList.size()-1; i>=0;i--) {
				Expression e = occurrenceList.get(i);
				e = e.insertValueForVariable(value, variableName);
			}
			
		}
		
		if(this.valuesList.size() > 0) {	
			for(int i=this.valuesList.size()-1; i>=0;i--) {
				Expression e = valuesList.get(i);
				e = e.insertValueForVariable(value, variableName);
			}
			
		}
		
		return this;		
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		if(array != null)
			this.array = (Array) this.array.insertValueForVariable(value, variableName);
		else 
			this.arrayExpression = this.arrayExpression.insertValueForVariable(value, variableName);
		
		if(this.occurrenceList.size() > 0) {		
			for(int i=this.occurrenceList.size()-1; i>=0;i--) {
				Expression e = occurrenceList.get(i);
				e = e.insertValueForVariable(value, variableName);
			}
			
		}
		
		if(this.valuesList.size() > 0) {	
			for(int i=this.valuesList.size()-1; i>=0;i--) {
				Expression e = valuesList.get(i);
				e = e.insertValueForVariable(value, variableName);
			}
			
		}
		
		return this;	
	}

	public Expression replaceVariableWithExpression(String variableName, Expression expression) {
		
		//System.out.println("Replacing variable "+variableName+" with expression "+expression+" in :"+this);
		
		if(this.array != null) {
			Expression e = this.array.replaceVariableWithExpression(variableName, expression);
			try {
			if(!(e instanceof Array)) 
				throw new Exception("Replacing variable '"+variableName+"' with infeasible expression '"+expression+
						"' that modifies atmost-array into:"+e+". Expected array type.");
			} catch (Exception exc) {
				exc.printStackTrace(System.out);
				System.exit(1);
			}
			this.array = (Array) e;
		}
		else {
			
			try {
			
			if(this.arrayExpression != null)	
				this.arrayExpression = this.arrayExpression.replaceVariableWithExpression(variableName, expression);
			
			if(this.occurrencesVectorName != null) {
				if(this.occurrencesVectorName.equals(variableName)) {
					if(expression instanceof ConstantVector)
						this.occurrenceOfVariables = ((ConstantVector) expression).getElements();
				}
			}
			
			if(this.occurrencesVectorName != null) {
				if(this.occurrencesVectorName.equals(variableName)) {
					if(expression instanceof ConstantVector)
						this.occurrenceOfVariables = ((ConstantVector) expression).getElements();
					else throw new Exception("Cannot insert '"+expression+"' for '"+this.occurrencesVectorName+
							" in atleast/atmost constraint because of type mismatch. Expected a constant vector instead of "+expression.getClass());
				}
			}
			
			if(this.valuesVectorName != null) {
				if(this.valuesVectorName.equals(variableName)) {
					if(expression instanceof ConstantVector)
						this.values = ((ConstantVector) expression).getElements();
					else throw new Exception("Cannot insert '"+expression+"' for '"+this.valuesVectorName+
							" in atleast/atmost constraint because of type mismatch. Expected a constant vector instead of "+expression.getClass());
				}
			}
			
			
			/* EXPRESSION LIST */
			if(this.occurrenceList.size() > 0) {		
				for(int i=this.occurrenceList.size()-1; i>=0;i--) {
					Expression e = occurrenceList.get(i);
					e = e.replaceVariableWithExpression(variableName, expression);
				}
				
			}
			
			if(this.valuesList.size() > 0) {	
				for(int i=this.valuesList.size()-1; i>=0;i--) {
					Expression e = valuesList.get(i);
					e = e.replaceVariableWithExpression(variableName, expression);
				}
				
			}
			
			
			} catch (Exception e) {
				e.printStackTrace(System.out);
				System.exit(1);
			}
		}
		//System.out.println("ReplacED variable "+variableName+" with expression "+expression+" in :"+this);
		
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
		
		
		StringBuffer s = (this.isAtmost) ? 
				       new StringBuffer("atmost(") : 
				    	   new StringBuffer("atleast(");
		
		if(array != null)
			s.append(this.array.toString()+",[");
		else s.append(this.arrayExpression+", [");
		
		
		for(int i=0; i<this.occurrenceOfVariables.length; i++) {
			if(i >0) s.append(",");
			s.append(this.occurrenceOfVariables[i]+"");
		}
		if(this.occurrenceOfVariables.length ==0) {
			
			if(this.occurrenceList.size() > 0) {
				for(int i=0; i<this.occurrenceList.size()-1; i++) {
					s.append(this.occurrenceList.get(i)+",");
				}
				s.append(this.occurrenceList.get(this.occurrenceList.size()-1));
				
			}
			
			if(this.occurrencesVectorName!=null)
				s.append(this.occurrencesVectorName);
			
		}
		
		s.append("], [");
		
		
		for(int i=0; i<this.values.length; i++) {
			if(i >0) s.append(",");
			s.append(this.values[i]+"");
		}
		if(this.values.length ==0) {
			
			if(this.valuesVectorName != null)
					s.append(this.valuesVectorName);
			else {
				if(this.valuesList.size() > 0) {
					for(int i=0; i<this.valuesList.size()-1; i++) {
						s.append(this.valuesList.get(i));
					}
					s.append(this.valuesList.get(this.valuesList.size()-1));
				}
			}
		}
		
		//System.out.println("Constructed ATMOST/ATLEAST constraint:"+s);
		return s+"])";
	}
	
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		return this;
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

	public ArrayList<Expression> getOccurrenceExpressions() {
		return this.occurrenceList;
	}
	
	public ArrayList<Expression> getValueExpressions() {
		return this.valuesList;
	}
}
