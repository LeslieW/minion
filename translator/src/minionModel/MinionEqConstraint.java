package minionModel;
/**
   Equality Constraint
*/

public class MinionEqConstraint extends MinionReifiableConstraint {

    MinionIdentifier lhs;
    MinionIdentifier rhs;
    boolean left_polarity;
    boolean right_polarity;
    
    public MinionEqConstraint(MinionIdentifier left, MinionIdentifier right) {
	lhs = left;
	rhs = right;
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
	return "eq("+left_negation+lhs+", "+right_negation+rhs+")" ;	
}

}
