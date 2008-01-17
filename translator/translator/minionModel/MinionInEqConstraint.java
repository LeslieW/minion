package translator.minionModel;

/**
   Inequality Constraint: less or equal. Can be turned to the stricter "less" by setting 
   the Constant value below zero.
*/

public class MinionInEqConstraint extends MinionReifiableConstraint {

    MinionIdentifier lhs;
    MinionIdentifier rhs;
    MinionConstant constant; // to obtain < use 1
    boolean left_polarity;
    boolean right_polarity;
    
    public MinionInEqConstraint(MinionIdentifier left, MinionIdentifier right, MinionConstant c) {
	lhs = left;
	rhs = right;
	constant = c;
	
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
    }

    public String toString() {
    	String left_negation = (left_polarity) ? "" : "n";
    	String right_negation = (right_polarity) ? "" : "n";
    	return "ineq("+left_negation+lhs+", "+right_negation+rhs+","+constant+")" ;	
    }

}
