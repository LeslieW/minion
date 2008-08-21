package translator.expression;

public class AbsoluteValue implements UnaryArithmeticExpression {

	private Expression argument;
	boolean isNested;
	boolean willBeReified = false;
	
	//============== Constructors ==================
	public AbsoluteValue(Expression argument) {
		this.argument = argument;
		this.isNested = true;
	}
	
	
	//============== Interfaced Methods ==================
	public Expression getArgument() {
		return this.argument;
	}

	public Expression copy() {
		return new AbsoluteValue(this.argument.copy());
	}

	public int[] getDomain() {
		
		//System.out.println("Argument is instance of:"+argument.getClass().getSimpleName()+" with type:"+argument.getType());
		int[] bounds = this.argument.getDomain();
		
		//System.out.println("The bounds of the argument of "+this+" are: ("+bounds[0]+".."+bounds[1]+")");
		
		int lb = 0;
		int ub = 0;
		
		if(bounds[0] < 0) {
			lb = 0;
			if(-bounds[0] > bounds[1]) {
				ub = -bounds[0];
			}
			else ub = bounds[1];
			//System.out.println("Computed bounds of "+this+": ("+lb+".."+ub+")");
			return new int[] {lb, ub};
		}
		
		else lb = bounds[0];
		
		/* int lb = (bounds[0] < 0) ?
				    0 : // -bounds[0] :
		
				     bounds[0]; */
				     
		ub = bounds[1];
        /* ub = (bounds[bounds.length-1] < 0) ?
			         -bounds[bounds.length-1] :
			    	 bounds[bounds.length-1];
			     
        if(bounds[0] == -bounds[1]) 
        	lb = 0; */
			         
		//System.out.println("Computed bounds of "+this+": ("+lb+".."+ub+")");
	    //if(lb > ub) 
	    	//return new int[] {ub,lb};
	    //else 
	    return new int[] {lb, ub};
	}

	public int getType() {
		return ABS;
	}

	public void orderExpression() {
		this.argument.orderExpression();
	}
	
	public String toString() {
		return "|"+this.argument+"|";
	}
 
	public Expression evaluate() {
		
		this.argument = argument.evaluate();
		
		//System.out.println("Evaluating abs expression: "+this);
		
		if(argument.getType() == INT) {
			int constant = ((ArithmeticAtomExpression) argument).getConstant();
			return (constant < 0) ? 
			 new ArithmeticAtomExpression(-constant) :
				 new ArithmeticAtomExpression(constant);
			
		}
		else return this;
	}
	
	
	public char isSmallerThanSameType(Expression e) {
		
		AbsoluteValue otherAbs = (AbsoluteValue) e;
		
		if(this.argument.getType() == otherAbs.argument.getType())
			return this.argument.isSmallerThanSameType(otherAbs.argument);
		else return (this.argument.getType() < otherAbs.argument.getType()) ?
				SMALLER : BIGGER;
	}
	
	public Expression reduceExpressionTree() {
		this.argument = this.argument.reduceExpressionTree();
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		this.argument = this.argument.insertValueForVariable(value, variableName);
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
		this.argument = this.argument.restructure();
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) throws Exception {
		this.argument = this.argument.insertDomainForVariable(domain, variableName);
		return this;
	}
	
	public Expression replaceVariableWithExpression(String variableName, Expression expression) throws Exception {
		
		this.argument = this.argument.replaceVariableWithExpression(variableName, expression);
		return this;
	}
	
	public Expression replaceVariableWith(Variable oldVariable, Variable newVariable) {
		this.argument= this.argument.replaceVariableWith(oldVariable, newVariable);
		return this;
	}
	
	public boolean isLinearExpression() {
		return this.argument.isLinearExpression() ;
	}
	
	public String toSolverExpression(translator.solver.TargetSolver solver) 
	throws Exception {
	
		if(solver instanceof translator.solver.Gecode &&
				this.isLinearExpression()) {
			return "abs(this, "+this.argument.toSolverExpression(solver)+")";
		}
	
		throw new Exception("Internal error. Cannot give direct solver representation of expression '"+this
			+"' for solver "+solver.getSolverName());
}
}
