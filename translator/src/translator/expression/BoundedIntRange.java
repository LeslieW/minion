package translator.expression;

public class BoundedIntRange implements IntRange {

	private int lowerBound;
	private int upperBound;
	
	//========== CONSTRUCTOR ==========================
	
	public BoundedIntRange(int lowerBound,
			               int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	
	//=========== INHERITED METHODS ====================
	
	public int[] getFullDomain() {
		
		if(this.upperBound < this.lowerBound)
			return new int[0];
		
		int[] fullDomain = new int[(this.upperBound-this.lowerBound)+1];
		for(int i=0; i<fullDomain.length; i++)
			fullDomain[i] = this.lowerBound+i;
		return fullDomain;
	}

	public int[] getRange() {
		return new int[] {this.lowerBound, this.upperBound};
	}

	public Domain copy() {
		return new BoundedIntRange(this.lowerBound, 
				                   this.upperBound);
	}
	
	public Expression[] getLowerAndUpperBound() {
		return new Expression[] {new ArithmeticAtomExpression(this.lowerBound),
								 new ArithmeticAtomExpression(this.upperBound)} ;
		
	}

	public Domain evaluate() {
		if(lowerBound == upperBound) {
			return new SingleIntRange(lowerBound );
		}
		else return this;
	}

	public int getType() {
		return INT_BOUNDS;
	}
	
	public String toString() {
		return "int("+this.lowerBound+".."+this.upperBound+")";
	}

	public boolean isConstantDomain() {
		return true;
	}
	
	public ExpressionRange toExpressionRange() {	
		return new BoundedExpressionRange(new ArithmeticAtomExpression(lowerBound),
				                          new ArithmeticAtomExpression(upperBound));
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		return this;
	}	
	
	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		
		BoundedIntRange otherBounded = (BoundedIntRange) d;
	
		if(this.lowerBound== otherBounded.lowerBound) {	
			if(this.upperBound == otherBounded.upperBound) {
					return Expression.EQUAL;
			}
			else return (this.upperBound < otherBounded.upperBound) ?
					Expression.SMALLER : Expression.BIGGER;
			
		}
		else return (this.lowerBound < otherBounded.lowerBound) ?
				Expression.SMALLER : Expression.BIGGER;
	}
	
}
