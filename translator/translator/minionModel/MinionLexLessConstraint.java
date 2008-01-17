package translator.minionModel;

public class MinionLexLessConstraint extends MinionReifiableConstraint {

	String matrixName1;
	String matrixName2;
	
	
	public MinionLexLessConstraint(String matrxName1,
			                   String matrxName2) {
		
		this.matrixName1 = matrxName1;
		this.matrixName2 = matrxName2;
		
	}
	
	
	public String toString() {
	
		return "lexless("+matrixName1+", "+matrixName2+")";
	}

}
