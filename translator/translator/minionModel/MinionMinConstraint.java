package translator.minionModel;

/**
   Min- Constraint
*/

public class MinionMinConstraint extends MinionReifiableConstraint {

    MinionIdentifier[] arguments;
    MinionIdentifier result;
    boolean[] argPolarities;   
    boolean resultPolarity;
    
    public MinionMinConstraint(MinionIdentifier[] args, MinionIdentifier r) {
	
	arguments = new MinionIdentifier[args.length];
	for(int i=0; i < args.length; i++)
	    arguments[i] = args[i]; 

	result = r;
	argPolarities = adjustVariablePolarity(arguments);
	resultPolarity = true;
	if(result.getPolarity() ==0) {
		resultPolarity = false;
		result.setPolarity(1);
	}
    }

    public String toString() {
        String pol = (argPolarities[0]) ? "" : "n";
    	String s =  "min(["+pol+arguments[0];
    	for(int i=1; i< arguments.length; i++) {
    		String polarity = (argPolarities[i]) ? "" : "n";
    	    s = s.concat(", "+polarity+arguments[i].toString());
    	}
    	String polarity = (resultPolarity) ? "" : "n";
    	return s.concat("], "+polarity+result.toString()+")");
    }

}
