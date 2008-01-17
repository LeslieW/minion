package translator.expression;

import java.util.ArrayList;

public class Conjunction extends NaryRelationalExpression {

	
	private ArrayList<Expression> conjointExpressions;
	
	
	//============== Constructors ==================
	
	public Conjunction(ArrayList<Expression> conjointExpressions) {
		this.conjointExpressions = conjointExpressions;
	}
	
//	============== Interfaced Methods ==================
	
	public ArrayList<Expression> getArguments() {
		return this.conjointExpressions;
	}
	
	public Expression copy() {
		ArrayList<Expression> copiedConjointExpressions = new ArrayList<Expression> (); 
		for(int i=0; i<this.conjointExpressions.size(); i++)
			copiedConjointExpressions.add(i, this.conjointExpressions.get(i).copy());
		
		return new Conjunction(copiedConjointExpressions);
	}

	public int getType() {
		return AND;
	}

	public void orderExpression() {
		this.conjointExpressions = this.orderExpressionList(this.conjointExpressions);

	}

	public int[] getDomain() {
		return new int[] {0,1};
	}
	
	public String toString() {
		String s = "and("+this.conjointExpressions.get(0);
		for(int i=1; i<this.conjointExpressions.size(); i++)
			s = s.concat(","+conjointExpressions.get(i));
		
		return s.concat(")");
	}
	
	// if this conjunction has less elements/arguments than the other conjunction,
	// then this conjunction is smaller
	public char isSmallerThanSameType(Expression e) {
		
		Conjunction otherConjunction = (Conjunction) e;
		
		// both conjunctions have the same amount of elements
		if(this.conjointExpressions.size() == otherConjunction.getArguments().size()) {
			
			// compare each argument, starting with the first
			for(int i=0; i<this.conjointExpressions.size(); i++) {
				Expression thisArgument = this.conjointExpressions.get(i);
				Expression otherArgument = otherConjunction.conjointExpressions.get(i);
				
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
			return (this.conjointExpressions.size() < otherConjunction.getArguments().size()) ?
				SMALLER : BIGGER;
	}

	
	public Expression evaluate() {
		
		ArrayList<Boolean> constants = new ArrayList<Boolean>();
		
		// first evaluate every argument
		for(int i=0; i<this.conjointExpressions.size(); i++) 
			conjointExpressions.add(i, conjointExpressions.remove(i).evaluate());
		
		
		// then look for constants and collect them
		//(start loop from the tail of the list, because we are removing elements)
		for(int i=this.conjointExpressions.size()-1; i>=0; i--) {
			
			if(this.conjointExpressions.get(i).getType() == BOOL)
				constants.add( ( (RelationalAtomExpression) conjointExpressions.remove(i) ).getBool());
		}
		
		// build the new constant from the identity
		boolean newConstant = true;
		
		// multiply all constants
		for(int i=0; i<constants.size(); i++) {
			newConstant = constants.get(i) && newConstant;
		}
		
		// if this piece is false, the whole Expression is false
		if(newConstant == false) {
			return new RelationalAtomExpression(false);
		}
		else {
			if(this.conjointExpressions.size() == 0)
				return new RelationalAtomExpression(newConstant);
			
			// the constant can only be true now, which means we can leave it out 
			//this.conjointExpressions.add(0,new RelationalAtomExpression(newConstant));
			return this;
		}
	}
	
	
	
	public Expression reduceExpressionTree() {
		
		for(int i=this.conjointExpressions.size()-1; i>=0; i--) {
			// merge the argument
			this.conjointExpressions.add(i, conjointExpressions.remove(i).reduceExpressionTree());
			
			// if the argument is a nested conjunction
			if(conjointExpressions.get(i).getType() == AND) {
				Conjunction nestedConjunction = (Conjunction) conjointExpressions.remove(i);
				
				// add the conjointExpressions of the nested conjunction
				for(int j=nestedConjunction.conjointExpressions.size()-1; j >=0; j--) {
					this.conjointExpressions.add(nestedConjunction.conjointExpressions.remove(j));
				}
			}
		}
		
		// if there is only one argument left, we have no disjunction but a single element
		if(this.conjointExpressions.size() == 1)
			return this.conjointExpressions.remove(0);
		
		return this;
	}
	
	
	protected void print_debug(String message) {
		
		if(DEBUG)
			System.out.println("[ DEBUG conjunction ] "+message);
		
	}
}

