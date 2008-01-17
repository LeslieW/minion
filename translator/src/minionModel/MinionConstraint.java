package minionModel;

public abstract class MinionConstraint {


    // returns the Minion-representation of the constraint
    public abstract String toString();

    
    
    protected boolean[] adjustVariablePolarity(MinionIdentifier[] arguments) {

    	boolean[] polarities = new boolean[arguments.length];
    	
    	for(int i=0; i<arguments.length; i++) {
    		MinionIdentifier identifier = arguments[i];
    		polarities[i] = true;
    		if(identifier.getPolarity() == 0) {
    			polarities[i] = false;
    			identifier.setPolarity(1);
    		}
    	}
    	
    	return polarities;
    }
    
}
