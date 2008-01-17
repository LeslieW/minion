package minionModel;

public class MinionSumConstraint extends MinionReifiableConstraint {

	
	  	MinionIdentifier[] variables;
	    MinionIdentifier result; // to obtain < use 1
	    boolean useWatchedLiterals;
	    boolean[] argPolarities;   
	    boolean resultPolarity;
	    
	    
	    public MinionSumConstraint(MinionIdentifier[] vars, MinionIdentifier r, boolean useWatchedLiterals) {
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
	    	String constraintNameGeq = "sumgeq";
	    	String constraintNameLeq = "sumleq";
	    	if(useWatchedLiterals) {
	    		constraintNameGeq = "watchsumgeq";
	    		constraintNameLeq = "watchsumleq";
	    	}
	    	
	    	String p = (argPolarities[0]) ? "" : "n";
	    	String r = constraintNameGeq+"(["+p+variables[0];
	    	for(int i = 1; i < variables.length; i++) {
	    		String polarity = (argPolarities[i]) ? "" : "n";
	    	    r = r.concat(", "+polarity+variables[i]);		
	    	}
	    	String resultP = (resultPolarity) ? "" : "n";
	    	r= r.concat("], "+resultP+result+")\n");	
	    	
	    	r = r.concat(constraintNameLeq+"(["+p+variables[0]);

	    	for(int j = 1; j < variables.length; j++) {
	    		String polarity = (argPolarities[j]) ? "" : "n";
	    	    r=r.concat(", "+polarity+variables[j]);		
	    	}
	    	return r.concat("], "+resultP+result+")");	
	    }
	        
	    public String toStringGreater() {

	    	String constraintName = "sumgeq";
	    	if(useWatchedLiterals) 
	    		constraintName = "watchsumgeq";
	    		
	    	String p = (argPolarities[0]) ? "" : "n";
	    	String r = constraintName+"(["+p+variables[0];
	    	
	    	for(int i = 1; i < variables.length; i++) {
	    		String polarity = (argPolarities[i]) ? "" : "n";
	    		r = r.concat(", "+polarity+variables[i]);
	    	}
	    	String resultP = (resultPolarity) ? "" : "n";
	    	return r.concat("], "+resultP+result+")");
	    	
	    }

	    public String toStringLess() {
	    	
	    	String constraintName = "sumleq";
	    	if(useWatchedLiterals) 
	    		constraintName = "watchsumleq";
	    	
	    	String p = (argPolarities[0]) ? "" : "n";
	        String s = constraintName+"(["+p+variables[0];

	        for(int j = 1; j < variables.length; j++) {
	        	String polarity = (argPolarities[j]) ? "" : "n";
	        	s=s.concat(", "+polarity+variables[j]);
	        }
	        String resultP = (resultPolarity) ? "" : "n";
	        return s.concat("], "+resultP+result+")");	
	    }
	    
	    
}
