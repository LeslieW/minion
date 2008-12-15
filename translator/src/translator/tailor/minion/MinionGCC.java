package translator.tailor.minion;

public class MinionGCC implements MinionConstraint {

	MinionArray variableArray;
	MinionAtom[] variableList;
	
	MinionAtom[] valuesList;
	int[] valuesValues;
	
	MinionAtom[] capacityList;
	int[] capacityValues;
	MinionArray capacities;
	
	public MinionGCC(MinionArray array,
			         int[] values,
			         int[] capacities) {
		this.variableArray = array;
		this.valuesValues = values;
		this.capacityValues = capacities;
	}
	
	public MinionGCC(MinionAtom[] array,
	         int[] values,
	         int[] capacities) {
		this.variableList = array;
		this.valuesValues = values;
		this.capacityValues = capacities;
	}
	
	public MinionGCC(MinionArray array,
			         int[] values,
			         MinionArray capacities) {
		this.variableArray = array;
		this.valuesValues = values;
		this.capacities = capacities;
	}
	
	
	public String toString() {
		
		StringBuffer s = new StringBuffer("gcc(");
	
		if(this.variableArray != null)
			s.append(this.variableArray.toString());
		else {
			s.append("["+this.variableList[0]);
			for(int i=1; i<this.variableList.length; i++)
				s.append(","+variableList[i]);
			s.append("]");
		}
		s.append(", ");
		
		s.append("["+valuesValues[0]);
		for(int i=1; i<this.valuesValues.length; i++) 
			s.append(","+valuesValues[i]);
		s.append("], ");
		
		if(this.capacities == null) {
			s.append("["+capacityValues[0]);
			for(int i=1; i<this.capacityValues.length; i++) 
				s.append(","+capacityValues[i]);
			s.append("])");
		}
		else {
			s.append(this.capacities+")");
		}
		
		return s.toString();
	}
}
