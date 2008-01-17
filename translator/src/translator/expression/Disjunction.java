package translator.expression;

import java.util.ArrayList;

public class Disjunction extends NaryRelationalExpression {

	private ArrayList<Expression> disjointExpressions;
	private boolean isNested = true;
	
	//============== Constructors ==================
	
	public Disjunction(ArrayList<Expression> disjointExpressions) {
		this.disjointExpressions = disjointExpressions;
	}
	
//	============== Interfaced Methods ==================
	
	public ArrayList<Expression> getArguments() {
		return this.disjointExpressions;
	}
	
	public Expression copy() {
		ArrayList<Expression> copiedDisjointExpressions = new ArrayList<Expression> (); 
		for(int i=0; i<this.disjointExpressions.size(); i++)
			copiedDisjointExpressions.add(i, this.disjointExpressions.get(i).copy());
		
		return new Disjunction(copiedDisjointExpressions);
	}

	public int getType() {
		return OR;
	}

	public void orderExpression() {
		this.disjointExpressions = orderExpressionList(this.disjointExpressions);

	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public String toString() {
		String s = "or("+this.disjointExpressions.get(0);
		for(int i=1; i<this.disjointExpressions.size(); i++)
			s = s.concat(",\n\t"+disjointExpressions.get(i));
		
		return s.concat(")");
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		Disjunction otherDisjunction = (Disjunction) e;
		
		// both disjunctions have the same amount of elements
		if(this.disjointExpressions.size() == otherDisjunction.getArguments().size()) {
			
			// compare each argument, starting with the first
			for(int i=0; i<this.disjointExpressions.size(); i++) {
				Expression thisArgument = this.disjointExpressions.get(i);
				Expression otherArgument = otherDisjunction.disjointExpressions.get(i);
				
				// the arguments have the sametype
				if(thisArgument.getType() == otherArgument.getType()) {
					char argumentRelation = thisArgument.isSmallerThanSameType(otherArgument);
					if(argumentRelation != EQUAL)
						return argumentRelation;
				}
				else return (thisArgument.getType() < otherArgument.getType()) ?
						SMALLER : BIGGER;
			}
			// if we reached the end of the for-loop without returning something, both conjunctions are equal
			return EQUAL;
		}
			
		else 
			return (this.disjointExpressions.size() < otherDisjunction.getArguments().size()) ?
				SMALLER : BIGGER;
	}

	
	public Expression evaluate() {
		ArrayList<Boolean> constants = new ArrayList<Boolean>();
	
		// first evaluate every argument
		for(int i=0; i<this.disjointExpressions.size(); i++) 
			disjointExpressions.add(i, disjointExpressions.remove(i).evaluate());
		
	
		// then look for constants and collect them
		// (start loop from the tail of the list, because we are removing elements)
		for(int i=this.disjointExpressions.size()-1; i>=0; i--) {
	
			if(this.disjointExpressions.get(i).getType() == BOOL)
				constants.add( ( (RelationalAtomExpression) disjointExpressions.remove(i) ).getBool());
		}
	
		// build the new constant from the identity
		boolean newConstant = false;
	
		// multiply all constants
		for(int i=0; i<constants.size(); i++) {
			newConstant = constants.get(i) || newConstant;
		}
	
		//print_debug("evaluated atoms to constant:"+newConstant);
		
		if(newConstant == true) {
			return new RelationalAtomExpression(true);
		}
		else {
			if(this.disjointExpressions.size() == 0)
				return new RelationalAtomExpression(newConstant);
			
		    // 	the constant can only be false now, which we can neglect
			//this.disjointExpressions.add(0,new RelationalAtomExpression(newConstant));
			return this;
		}
	}
	
	public Expression reduceExpressionTree() {
		
		for(int i=this.disjointExpressions.size()-1; i>=0; i--) {
			// merge the argument
			this.disjointExpressions.add(i, disjointExpressions.remove(i).reduceExpressionTree());
			
			// if the argument is a nested disjunction
			if(disjointExpressions.get(i).getType() == OR) {
				Disjunction nestedDisjunction = (Disjunction) disjointExpressions.remove(i);
				
				// add the disjointExpressions of the nested conjunction
				for(int j=nestedDisjunction.disjointExpressions.size()-1; j >=0; j--) {
					this.disjointExpressions.add(nestedDisjunction.disjointExpressions.remove(j));
				}
			}
		}
		
		// if there is only one argument left, we have no disjunction but a single element
		if(this.disjointExpressions.size() == 1)
			return this.disjointExpressions.remove(0);
		
		return this;
	}
	
	
	protected void print_debug(String message) {
		
		if(DEBUG)
			System.out.println("[ DEBUG disjunction ] "+message);
		
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.disjointExpressions.size(); i++) {
			this.disjointExpressions.add(i, this.disjointExpressions.remove(i).insertValueForVariable(value, variableName));
		}
		return this;
	}
	
	public boolean isNested() {
		return isNested;
	}
	
	public void setIsNotNested() {
		this.isNested = false;
	}
	
	public Expression restructure() {
		for(int i=0; i<this.disjointExpressions.size(); i++) {
			this.disjointExpressions.add(i, this.disjointExpressions.remove(i).restructure());
		}
		return this;
	}
	
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		for(int i=0; i<this.disjointExpressions.size(); i++)
			this.disjointExpressions.add(i,this.disjointExpressions.remove(i).insertDomainForVariable(domain, variableName));
		return this;
	}
	
}
