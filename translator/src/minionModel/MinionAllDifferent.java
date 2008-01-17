package minionModel;

public class MinionAllDifferent extends MinionReifiableConstraint {

	MinionIdentifier[] vector;
	/** the name of the matrix/vector in the Minion model */
	String matrixName;
	
	public MinionAllDifferent(MinionIdentifier[] parameters, String minionName) {
	
		vector = parameters;
		matrixName = minionName;
	}
	
	public String toString() {
		return "alldiff("+matrixName+")";
	}

}
