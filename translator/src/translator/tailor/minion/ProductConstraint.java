package translator.tailor.minion;


/**
 * Represents the Minion Product constraint
 * 
 * 
 * @author andrea
 *
 */

public class ProductConstraint implements MinionConstraint {

	
	MinionAtom leftExpression;
	MinionAtom rightExpression;
	MinionAtom result;
	
	
	public ProductConstraint(MinionAtom leftArgument,
			                 MinionAtom rightArgument,
			                 MinionAtom result) {
		
		this.leftExpression = leftArgument;
		this.rightExpression = rightArgument;
		this.result = result;
	}

	
	public String toString() {
		
		return "product("+leftExpression.toString()+","+rightExpression.toString()+", "+result.toString()+")";
	
	}
}
