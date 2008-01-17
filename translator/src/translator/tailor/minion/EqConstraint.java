package translator.tailor.minion;


public class EqConstraint implements MinionConstraint {

	
	MinionAtom leftExpression;
	MinionAtom rightExpression;
	
	
	public EqConstraint(MinionAtom leftArgument,
			            MinionAtom rightArgument) {
		
		this.leftExpression = leftArgument;
		this.rightExpression = rightArgument;
	}
	
	
	public String toString() {
		return "eq("+this.leftExpression+", "+this.rightExpression+")";
	}
	
}
