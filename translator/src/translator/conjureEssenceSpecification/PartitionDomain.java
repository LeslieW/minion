package translator.conjureEssenceSpecification;

public class PartitionDomain implements EssenceGlobals {

	
	/**
	 * restriction_mode 1 : without sizeSet
	 * restriction_mode 2 : with sizeSet 
	 */
	int restriction_mode;
	Domain domain;
	SizeSet sizeset;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	public Domain getdomain(){
		return domain;
	}
	public void setdomain(Domain d){
		domain=d;
	}
	public SizeSet getsizeset(){
		return sizeset;
	}
	public void setsizeset(SizeSet ss){
		sizeset=ss;
	}
	
	public PartitionDomain(Domain d){
		restriction_mode = PARTITIOND;
		domain = d;
	}
	public PartitionDomain(SizeSet s,Domain d){
		restriction_mode = PARTITIOND_SIZESET;
		domain = d;
		sizeset =s;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case PARTITIOND : return "partition " + domain.toString();
		case PARTITIOND_SIZESET : return "partition " + sizeset.toString() + domain.toString();
		}
		return "";
	}
}
