package translator.expression;

import java.util.ArrayList;

public class Conjunction extends NaryRelationalExpression {

	
	private ArrayList<Expression> conjointExpressions;
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	//============== Constructors ==================
	
	public Conjunction(ArrayList<Expression> conjointExpressions) {
		this.conjointExpressions = conjointExpressions;
	}
	
	public Conjunction(Expression[] arguments) {
		
		this.conjointExpressions = new ArrayList<Expression>();
		
		for(int i=0; i<arguments.length; i++) {
			conjointExpressions.add(arguments[i]);
		}
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
		
		if(this.conjointExpressions.size() ==0)
			return "true";
		
		else if(this.conjointExpressions.size() == 1)
			return this.conjointExpressions.get(0).toString();
		
		else {
			StringBuffer s = new StringBuffer("( ("+this.conjointExpressions.get(0).toString()+")");
			
			for(int i=1; i<this.conjointExpressions.size(); i++) {
				s.append(" /\\ ("+this.conjointExpressions.get(i).toString()+")");
			}
			s.append(" )");
			return s.toString();
		}
		
		/* if(this.conjointExpressions.size() ==0)
			return "and()";
		
		String s = "and("+this.conjointExpressions.get(0);
		for(int i=1; i<this.conjointExpressions.size(); i++)
			s = s.concat(",\n\t"+conjointExpressions.get(i));
		
		return s.concat(")"); */
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
				boolean willBeFlattened= nestedConjunction.isGonnaBeFlattenedToVariable();
				
				// add the conjointExpressions of the nested conjunction
				for(int j=nestedConjunction.conjointExpressions.size()-1; j >=0; j--) {
					Expression e = nestedConjunction.conjointExpressions.remove(j);
					e.willBeFlattenedToVariable(willBeFlattened);
					this.conjointExpressions.add(e);
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
	
	
	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.conjointExpressions.size(); i++) {
			this.conjointExpressions.add(i, this.conjointExpressions.remove(i).insertValueForVariable(value, variableName));
		}
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		for(int i=0; i<this.conjointExpressions.size(); i++) {
			this.conjointExpressions.add(i, this.conjointExpressions.remove(i).insertValueForVariable(value, variableName));
		}
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		for(int i=0; i<this.conjointExpressions.size(); i++)
			this.conjointExpressions.add(i, this.conjointExpressions.remove(i).replaceVariableWithExpression(variableName, expression));
		return this;
	}
	
	public boolean isNested() {
		return isNested;
	}
	
	public void setIsNotNested() {
		this.isNested = false;
	}
	
	public boolean isGonnaBeFlattenedToVariable() {
		return this.willBeReified;
	}
	
	public void willBeFlattenedToVariable(boolean reified) {
		this.willBeReified = reified;
	}
	
	public Expression restructure() {
		for(int i=0; i<this.conjointExpressions.size(); i++)
			this.conjointExpressions.add(i, this.conjointExpressions.remove(i).restructure());
		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		for(int i=0; i<this.conjointExpressions.size(); i++)
			this.conjointExpressions.add(i,this.conjointExpressions.remove(i).insertDomainForVariable(domain, variableName));
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		for(int i=0; i<this.conjointExpressions.size(); i++)
			this.conjointExpressions.add(i,this.conjointExpressions.remove(i).replaceVariableWith(oldVariable, newVariable));
		
		return this;
		
	}
	
	public boolean isLinearExpression() {
		//for(int i=0; i<this.conjointExpressions.size(); i++) {
		//	if(!this.conjointExpressions.get(i).isLinearExpression())
		//		return false;
		//}
		return false;
	}
	
	public String toSolverExpression(translator.solver.TargetSolver solver) 
	throws Exception {
		
		
		if(solver instanceof translator.solver.Gecode) {
			StringBuffer s = new StringBuffer("");
			
			for(int i=0; i<this.conjointExpressions.size(); i++) {
				
				Expression argument = this.conjointExpressions.get(i);
				if(argument instanceof AtomExpression) 
					s.append(argument.toSolverExpression(solver));
				else s.append("("+argument.toSolverExpression(solver)+")");
				
				if(i<this.conjointExpressions.size()-1) 
					s.append(" && ");
			}
			return s.toString();
		}
		
		throw new Exception("Internal error. Cannot give direct solver representation of expression '"+this
			+"' for solver "+solver.getSolverName());
	}
}

