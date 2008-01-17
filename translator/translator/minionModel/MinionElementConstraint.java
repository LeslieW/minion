package translator.minionModel;

public class MinionElementConstraint extends MinionReifiableConstraint {

	String vectorName;
	MinionIdentifier indexVariable;
	MinionIdentifier elementVariable;
	
	public MinionElementConstraint(String vectorsName, 
			                       MinionIdentifier indexVar,
			                       MinionIdentifier element) {
		
		indexVariable = indexVar;
		elementVariable = element;			
		vectorName = vectorsName;
	}
	
	
	public String toString() {
	  return "element("+vectorName+", "+indexVariable.toString()+", "+elementVariable.toString()+")";
	}

}
