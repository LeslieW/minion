package translator.expression;

/**
 * Represents all ranges that are defined by an lower and upperbound.
 * Both bounds do not have to be integers, but may be arbitrary expressions.
 * 
 * @author andrea
 *
 */

public class BoundedExpressionRange implements ExpressionRange {

	private Expression lowerBound;
	private Expression upperBound;
	
	
	// ========= CONSTRUCTOR ==========================
	
	public BoundedExpressionRange(Expression lowerBound,
			                      Expression upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	
	// ======= INHERITED METHODS ==========================
	
	public Domain copy() {
		return new BoundedExpressionRange(this.lowerBound.copy(),
				                          this.upperBound.copy());
	}

	public Expression[] getLowerAndUpperBound() {
		return new Expression[] {this.lowerBound, this.upperBound };
	}
	
	public Domain evaluate() {
		
		this.lowerBound = this.lowerBound.reduceExpressionTree().evaluate();
		this.upperBound = this.upperBound.reduceExpressionTree().evaluate();
		
		if(this.lowerBound.getType() == Expression.INT && this.upperBound.getType() == Expression.INT) {
			return new BoundedIntRange(( (ArithmeticAtomExpression) this.lowerBound).getConstant(),
					                   ( (ArithmeticAtomExpression) this.upperBound).getConstant());
		}
		else if(this.lowerBound.getType() == Expression.BOOL && this.upperBound.getType() == Expression.BOOL) {
			boolean lb = ((RelationalAtomExpression) this.lowerBound).getBool();
			boolean ub = ((RelationalAtomExpression) this.lowerBound).getBool();
			// (lb,ub) == (0,1)  meaning lb < ub
			if(!lb && ub) return new BoolDomain();
			else if(lb == ub) return (lb) ? 
					                new SparseIntRange(new int[] {1}) :
					                	 new SparseIntRange(new int[] {0});
			//TODO:  else we have a violation, because lb > ub
			// should we deal with this here or elsewhere (when we actually use the domain)?
		}
		
		return this;	
	}

	public int getType() {
		return EXPR_BOUNDS;
	}

	public String toString() {
		return "("+this.lowerBound.toString()+".."+this.upperBound.toString()+")";
	}
	
	public boolean isConstantDomain() {
		return false;
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
	
		//System.out.println("Inserting value for variable");
		
		this.lowerBound = this.lowerBound.insertValueForVariable(value, variableName);
		this.upperBound = this.upperBound.insertValueForVariable(value, variableName);
		
		if(this.lowerBound.getType() == Expression.INT && 
				this.upperBound.getType() == Expression.INT) {
			return new BoundedIntRange(( (ArithmeticAtomExpression) this.lowerBound).getConstant(),
				  	                   ( (ArithmeticAtomExpression) this.upperBound).getConstant() 
					                   );
		}
		
		return this;
	}
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		
		//System.out.println("Inserting value for variable");
		
		this.lowerBound = this.lowerBound.insertValueForVariable(value, variableName);
		this.upperBound = this.upperBound.insertValueForVariable(value, variableName);
		
		if(this.lowerBound.getType() == Expression.INT && 
				this.upperBound.getType() == Expression.INT) {
			return new BoundedIntRange(( (ArithmeticAtomExpression) this.lowerBound).getConstant(),
				  	                   ( (ArithmeticAtomExpression) this.upperBound).getConstant() 
					                   );
		}
		
		return this;
	}
	
	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		
		BoundedExpressionRange otherBounded = (BoundedExpressionRange) d;
	
		if(this.lowerBound.getType() == this.upperBound.getType()) {
			char difference = this.lowerBound.isSmallerThanSameType(otherBounded.lowerBound);
			
			if(difference == Expression.EQUAL) {
				
				if(this.upperBound.getType() == otherBounded.upperBound.getType()) {
					
					return this.upperBound.isSmallerThanSameType(otherBounded.upperBound);
				}
				else return (this.upperBound.getType() < otherBounded.upperBound.getType()) ?
						Expression.SMALLER : Expression.BIGGER;
			}
			else return difference;
		}
		else return (this.lowerBound.getType() < this.upperBound.getType()) ?
				Expression.SMALLER : Expression.BIGGER;
	}
	

	// ============ ADDITIONAL METHODS =======================================================
	
	
	public Expression getLowerBound() {
		return this.lowerBound;
	}
	
	
	public Expression getUpperBound() {
		return this.upperBound;
	}
	
}