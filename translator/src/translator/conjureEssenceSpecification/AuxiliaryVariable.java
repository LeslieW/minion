package translator.conjureEssenceSpecification;

public class AuxiliaryVariable {

	
	private String name;
	private int lowerBound;
	private int upperBound;
	
	
	public AuxiliaryVariable(String name, int lb, int ub) {
		this.name = name;
		this.lowerBound = lb;
		this.upperBound = ub;
	}
	  
	  
	public int getLowerBound() {
		return this.lowerBound;
	}
	
	
	public int getUpperBound() {
		return this.upperBound;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.name;
	}
	  
}
