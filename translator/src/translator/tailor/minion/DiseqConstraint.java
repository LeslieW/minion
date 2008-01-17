package translator.tailor.minion;


public class DiseqConstraint implements MinionConstraint {
		
	MinionAtom leftExpression;
	MinionAtom rightExpression;
		
		
	public DiseqConstraint(MinionAtom leftArgument,
				           MinionAtom rightArgument) {
			
		this.leftExpression = leftArgument;
		this.rightExpression = rightArgument;
	}
		
		
	public String toString() {
		return "diseq("+this.leftExpression+", "+this.rightExpression+")";
	}
		

	
}
