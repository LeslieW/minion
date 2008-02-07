package translator.tailor.minion;


public class DiseqConstraint implements MinionConstraint {
		
	MinionAtom leftExpression;
	MinionAtom rightExpression;
		
		
	public DiseqConstraint(MinionAtom leftArgument,
				           MinionAtom rightArgument) {
			
		this.leftExpression = leftArgument;
		this.rightExpression = rightArgument;
	}
		
		
	public StringBuffer toStringFormat() {
		return new StringBuffer("diseq(").append(this.leftExpression).append(",").append(this.rightExpression).append(")");
	}
	
	public String toString() {
		return "diseq("+this.leftExpression+", "+this.rightExpression+")";
	}
		

	
}
