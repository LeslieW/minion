package minionModel;

/**
   product Constraint
*/

public class MinionReifyConstraint extends MinionConstraint {

    MinionReifiableConstraint constraint;
    MinionIdentifier reifiedVariable;
    boolean resultPolarity;

    public MinionReifyConstraint(MinionReifiableConstraint c, MinionIdentifier id) {
	constraint = c;
	reifiedVariable = id;
	
	resultPolarity = true;
	if(id.getPolarity() ==0) {
		resultPolarity = false;
		id.setPolarity(1);
	}
    }

    public MinionIdentifier getReifiedVariable() {
	return reifiedVariable;
    }

    public String toString() {
    	
       	String result_neg = (resultPolarity) ? "" : "n";
    	
    	if(constraint.getClass().toString().endsWith("minionModel.MinionSumConstraint")) {
    		String result = "reify("+((MinionSumConstraint) constraint).toStringGreater()+", "+result_neg+reifiedVariable.toString()+")\n";
    		return result.concat("reify("+((MinionSumConstraint) constraint).toStringLess()+", "+result_neg+reifiedVariable.toString()+")\n");
    	}
    	else if(constraint.getClass().toString().endsWith("minionModel.MinionWeightedSumConstraint")) {
    		String result = "reify("+((MinionWeightedSumConstraint) constraint).toStringGreater()+", "+result_neg+reifiedVariable.toString()+")\n";
    		return result.concat("reify("+((MinionWeightedSumConstraint) constraint).toStringLess()+", "+result_neg+reifiedVariable.toString()+")\n");
    	}
    	else 
    		return "reify("+constraint.toString()+", "+result_neg+reifiedVariable.toString()+")\n";
    }


}
