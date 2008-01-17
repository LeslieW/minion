package translator.expression;

import java.util.ArrayList;

public class Disjunction extends NaryRelationalExpression {

	private ArrayList<Expression> disjointExpressions;
	
	
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
		orderExpressionList(this.disjointExpressions);

	}
	
	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public String toString() {
		String s = "or("+this.disjointExpressions.get(0);
		for(int i=1; i<this.disjointExpressions.size(); i++)
			s = s.concat(","+disjointExpressions.get(i));
		
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
	
		if(newConstant == false) {
			return new RelationalAtomExpression(false);
		}
		else {
		// 	add the constant to the beginning of the list, since it is smallest for sure
			this.disjointExpressions.add(0,new RelationalAtomExpression(newConstant));
			return this;
		}
	}
	
	public Expression merge() {
		
		for(int i=this.disjointExpressions.size()-1; i>=0; i--) {
			// merge the argument
			this.disjointExpressions.add(i, disjointExpressions.remove(i).merge());
			
			// if the argument is a nested disjunction
			if(disjointExpressions.get(i).getType() == OR) {
				Disjunction nestedDisjunction = (Disjunction) disjointExpressions.remove(i);
				
				// add the disjointExpressions of the nested conjunction
				for(int j=nestedDisjunction.disjointExpressions.size()-1; j >=0; j--) {
					this.disjointExpressions.add(nestedDisjunction.disjointExpressions.remove(i));
				}
			}
		}
		
		return this;
	}
	
}
