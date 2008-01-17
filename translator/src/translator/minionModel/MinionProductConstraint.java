package translator.minionModel;

/**
   product Constraint
*/

public class MinionProductConstraint extends MinionReifiableConstraint {

    MinionIdentifier lhs;
    MinionIdentifier rhs;
    MinionIdentifier result; // to obtain < use 1 
    boolean left_polarity;
    boolean right_polarity;
    boolean resultPolarity;
    
    public MinionProductConstraint(MinionIdentifier left, MinionIdentifier right, MinionIdentifier r) {
	lhs = left;
	rhs = right;
	result = r;
	
	left_polarity = true;
	right_polarity = true;
	
   	// check if either of them has been negated 
	if(lhs.getPolarity() ==0) {
		left_polarity = false;
		lhs.setPolarity(1);
	}
	
	if(rhs.getPolarity() ==0) {
		right_polarity = false;
		rhs.setPolarity(1);
	}   	
	
	resultPolarity = true;
	if(result.getPolarity() ==0) {
		resultPolarity = false;
		result.setPolarity(1);
	}
	
    }

    public String toString() {
    	String left_neg = (left_polarity) ? "" : "n";
    	String right_neg = (right_polarity) ? "" : "n";
    	String result_neg = (resultPolarity) ? "" : "n";
	return "product("+left_neg+lhs+", "+right_neg+rhs+", "+result_neg+result+")";
	
    }

}
