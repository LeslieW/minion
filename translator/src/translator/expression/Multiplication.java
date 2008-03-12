package translator.expression;

import java.util.ArrayList;

public class Multiplication extends NaryArithmeticExpression {

	
	private ArrayList<Expression> arguments;
	private boolean isNested = true;
	private boolean willBeReified = false;
	private boolean convertToProductConstraint = false;
	
	// ==================== CONSTRUCTOR ====================
	public Multiplication(ArrayList<Expression> arguments) {
		this.arguments = arguments;
	}
	
	
	public Multiplication(Expression[] arguments) {
		
		this.arguments = new ArrayList<Expression>();
		for(int i=0; i<arguments.length; i++)
			this.arguments.add(arguments[i]);
	}
	
	// =============== INTERFACED METHODS ==================
	public ArrayList<Expression> getArguments() {
		return this.arguments;
	}
	
	public Expression copy() {
		ArrayList<Expression> copiedArguments = new ArrayList<Expression>();
		for(int i=0; i<this.arguments.size(); i++)
			copiedArguments.add(this.arguments.get(i).copy());
		
		return new Multiplication(copiedArguments);
	}

	public int[] getDomain() {
		
		int lowerBound = 1;
		int upperBound = 1;
		
		boolean negativeBound = false;
		boolean zeroLowerBound = false;
		boolean zeroUpperBound = false;
		
		for(int i=0; i<this.arguments.size(); i++) {
			int lb_i = this.arguments.get(i).getDomain()[0];
			int ub_i = this.arguments.get(i).getDomain()[1];
			
			//System.out.println("lb"+i+":"+lb_i+" and ub"+i+":"+ub_i+", of argument:"+arguments.get(i)+" with type:"+this.arguments.get(i).getType());
			
			if(lb_i == 0) 
				zeroLowerBound = true;
				// keep lower bound as it is
			else {
				negativeBound = (lb_i < 0) || negativeBound;
				lowerBound = lowerBound*lb_i;
			}
			
			if(ub_i == 0) zeroUpperBound = true;
			else {
				negativeBound = (ub_i < 0) || negativeBound;
				upperBound = upperBound*ub_i;
			}
		}
		
		//System.out.println("lb:"+lowerBound+" and ub:"+upperBound+", before taking care of -,0 etc");
		
		if(negativeBound) { // don't care about zero here, since the bounds will range over it anyway
			if(upperBound < 0) {
				// absolute value
				lowerBound = upperBound;
				upperBound = upperBound - 2*upperBound;
			}
			else {
				lowerBound = upperBound - 2*upperBound;
			}
		}
		else {
			if(zeroLowerBound) {
				if(lowerBound > 0)
					lowerBound = 0;
			}
			if(zeroUpperBound) {
				if(lowerBound > 0) lowerBound = 0;
			}
		}
		
		//System.out.println("FINISHED ADAPTION> lb:"+lowerBound+" and ub:"+upperBound+", after taking care of -,0 etc");
		return new int[] {lowerBound, upperBound};
	}

	public int getType() {
		return MULT;
	}

	public void orderExpression() {
		this.arguments = this.orderExpressionList(this.arguments);

	}

	public String toString() {
		
		if(this.arguments.size() == 0)
			return "empty*";
		
		String s = this.arguments.get(0).toString();
		
		for(int i=1; i<this.arguments.size(); i++)
			s = s.concat("*"+this.arguments.get(i));
		
		return s;
	}
	
	public char isSmallerThanSameType(Expression e) {
		
		Multiplication otherMultiplication = (Multiplication) e;
		
		// both disjunctions have the same amount of elements
		if(this.arguments.size() == otherMultiplication.getArguments().size()) {
			
			// compare each argument, starting with the first
			for(int i=0; i<this.arguments.size(); i++) {
				Expression thisArgument = this.arguments.get(i);
				Expression otherArgument = otherMultiplication.arguments.get(i);
				
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
			return (this.arguments.size() < otherMultiplication.getArguments().size()) ?
			SMALLER : BIGGER;
		
	}
	
	public Expression evaluate() {
		
		ArrayList<Integer> constants = new ArrayList<Integer>();
		
		// first evaluate every argument
		for(int i=0; i<this.arguments.size(); i++) 
			arguments.add(i, arguments.remove(i).evaluate());
		
		
		// then look for constants and collect them
		//(start loop from the tail of the list, because we are removing elements)
		for(int i=this.arguments.size()-1; i>=0; i--) {
			Expression argument = this.arguments.get(i);
			
			if(argument.getType() == INT) 
				constants.add(((ArithmeticAtomExpression) arguments.remove(i)).getConstant());
			
			else if(argument.getType() == BOOL)
				constants.add( ( (RelationalAtomExpression) arguments.remove(i) 
						).toArithmeticExpression().getConstant());
		}
		
		// build the new constant from the identity of multiplication
		int newConstant = 1;
		
		// multiply all constants
		for(int i=0; i<constants.size(); i++) {
			newConstant = constants.get(i)*newConstant;
		}
		
		if(newConstant == 0)
			return new ArithmeticAtomExpression(0);
		else if(newConstant == 1) {
			if(this.arguments.size() == 0)
				return new ArithmeticAtomExpression(1);
			return this; // don't add it to the list since it is the identity
		}
		else {
			if(this.arguments.size() == 0)
				return new ArithmeticAtomExpression(newConstant);
			
		    // add the constant to the beginning of the list, since it is smallest for sure
			this.arguments.add(0,new ArithmeticAtomExpression(newConstant));
			return this;
		}
	}
	
	
	public Expression reduceExpressionTree() {
		
		for(int i=this.arguments.size()-1; i>=0; i--) {
			// merge the argument
			this.arguments.add(i, arguments.remove(i).reduceExpressionTree());
			
			// if the argument is a nested mutliplication
			if(arguments.get(i).getType() == MULT) {
				Multiplication nestedMultiplication = (Multiplication) arguments.remove(i);
				
				// add the arguments of the nested multiplication 
				for(int j=nestedMultiplication.arguments.size()-1; j >=0; j--) {
					this.arguments.add(nestedMultiplication.arguments.remove(j));
				}
			}
		}
		
		// if the multiplication only consists of one element, then return just the one element
		if(this.arguments.size() == 1) 
			return this.arguments.remove(0);
		
		return this;
	}
	
	public Expression insertValueForVariable(int value, String variableName) {
		for(int i=0; i<this.arguments.size(); i++) {
			this.arguments.add(i, this.arguments.remove(i).insertValueForVariable(value, variableName));
		}
		return this;
	}
	
	public Expression insertValueForVariable(boolean value, String variableName) {
		for(int i=0; i<this.arguments.size(); i++) {
			this.arguments.add(i, this.arguments.remove(i).insertValueForVariable(value, variableName));
		}
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
		for(int i=0; i<this.arguments.size(); i++)
			this.arguments.add(i, this.arguments.remove(i).restructure());
		
		return this;
	}
	
	public Expression insertDomainForVariable(Domain domain, String variableName) {
		for(int i=0; i<this.arguments.size(); i++)
			this.arguments.add(i,this.arguments.remove(i).insertDomainForVariable(domain, variableName));
		return this;
	}
	
	
	// ================== ADDITIONAL METHODS ==============================
	
	public boolean willBeConvertedToProductConstraint() {
		return this.convertToProductConstraint;
	}
	
	
	public void setWillBeConverteredToProductConstraint(boolean turnOn) {
		this.convertToProductConstraint = turnOn;
	}
}
