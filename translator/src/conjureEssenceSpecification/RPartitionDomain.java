package conjureEssenceSpecification;

public class RPartitionDomain {

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
	}	public Domain getdomain(){
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
	
	public RPartitionDomain(Domain d){
		restriction_mode = 1;
		domain = d;
	}
	public RPartitionDomain(SizeSet s,Domain d){
		restriction_mode = 2;
		domain = d;
		sizeset =s;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case 1 : return "rpartition " + domain.toString();
		case 2 : return "rpartition " + sizeset.toString() + domain.toString();
		}
		return "";
	}
}
