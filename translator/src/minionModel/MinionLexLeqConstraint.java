package minionModel;

public class MinionLexLeqConstraint extends MinionReifiableConstraint {

	String matrixName1;
	String matrixName2;
	
	
	public MinionLexLeqConstraint(String matrxName1,
			                   String matrxName2) {
		
		this.matrixName1 = matrxName1;
		this.matrixName2 = matrxName2;
		
	}
	
	
	public String toString() {
	
		return "lexleq("+matrixName1+", "+matrixName2+")";
	}

}
