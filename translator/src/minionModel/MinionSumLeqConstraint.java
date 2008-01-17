package minionModel;

/**
   MinionLeqSumConstraint 
*/

public class MinionSumLeqConstraint extends MinionReifiableConstraint {

    MinionIdentifier[] variables;
    MinionIdentifier result; // to obtain < use 1 
    boolean useWatchedLiterals;
    boolean[] argPolarities;   
    boolean resultPolarity;
    
    public MinionSumLeqConstraint(MinionIdentifier[] vars, MinionIdentifier r, boolean useWatchedLiterals) {
	variables = vars;
	result = r;
	this.useWatchedLiterals = useWatchedLiterals;
	
	argPolarities = adjustVariablePolarity(vars);
	resultPolarity = true;
	if(result.getPolarity() ==0) {
		resultPolarity = false;
		result.setPolarity(1);
	}
    }

    public String toString() {
    	
    	String constraintName = "sumleq";
    	if(useWatchedLiterals)
    		constraintName = "watchsumleq";
    	
       	String p = (argPolarities[0]) ? "" : "n";
		String r = constraintName+"(["+p+variables[0];
		for(int i = 1; i < variables.length; i++) {
			String polarity = (argPolarities[i]) ? "" : "n";
	    	r = r.concat(", "+polarity+variables[i]);		
		}
    	String resultP = (resultPolarity) ? "" : "n";
    	return r.concat("], "+resultP+result+")");	
    }

}
