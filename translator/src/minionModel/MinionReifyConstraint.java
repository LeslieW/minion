package minionModel;

/**
   product Constraint
*/

public class MinionReifyConstraint extends MinionConstraint {

    MinionReifiableConstraint constraint;
    MinionIdentifier reifiedVariable;
    MinionIdentifier reifiedVariable2;
    
    boolean resultPolarity;
    boolean resultPolarity2;

    public MinionReifyConstraint(MinionReifiableConstraint c, MinionIdentifier id) {
	  constraint = c;
	  reifiedVariable = id;
	
   	resultPolarity = true;
	
    if(id.getPolarity() ==0) {
		resultPolarity = false;
	 	id.setPolarity(1);
   	}
    
    }

    public MinionReifyConstraint(MinionReifiableConstraint c, MinionIdentifier var1, MinionIdentifier var2) {
    
    	this.constraint = c;
    	this.reifiedVariable = var1;
    	this.reifiedVariable2 = var2;
    	
    	resultPolarity = true;
    	if(var1.getPolarity() ==0) {
    		resultPolarity = false;
    		var1.setPolarity(1);
    	}
    	resultPolarity2 = true;
      	if(var2.getPolarity() ==0) {
    		resultPolarity2 = false;
    		var2.setPolarity(1);
    	}
    
    }
    
    
    public MinionIdentifier getReifiedVariable() {
	return reifiedVariable;
    }

    public MinionIdentifier getReifiedVariable2() {
    	return reifiedVariable2;
    }
    
    public String toString() {
    	
       	String result_neg = (resultPolarity) ? "" : "n";
    	
    	if(constraint.getClass().toString().endsWith("minionModel.MinionSumConstraint")) {
    		String result = "reify("+((MinionSumConstraint) constraint).toStringGreater()+", "+result_neg+reifiedVariable.toString()+")\n";
    		return result.concat("reify("+((MinionSumConstraint) constraint).toStringLess()+", "+result_neg+reifiedVariable2.toString()+")\n");
    	}
    	else if(constraint.getClass().toString().endsWith("minionModel.MinionWeightedSumConstraint")) {
    		String result = "reify("+((MinionWeightedSumConstraint) constraint).toStringGreater()+", "+result_neg+reifiedVariable.toString()+")\n";
    		return result.concat("reify("+((MinionWeightedSumConstraint) constraint).toStringLess()+", "+result_neg+reifiedVariable2.toString()+")\n");
    	}
    	else 
    		return "reify("+constraint.toString()+", "+result_neg+reifiedVariable.toString()+")";
    }


}
