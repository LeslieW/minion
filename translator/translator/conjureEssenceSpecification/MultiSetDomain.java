package translator.conjureEssenceSpecification;

public class MultiSetDomain implements EssenceGlobals {

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
	
	public MultiSetDomain(Domain d){
		restriction_mode = MULTISETD;
		domain = d;
	}
	public MultiSetDomain(SizeSet s,Domain d){
		restriction_mode = MULTISETD_SIZESET;
		domain = d;
		sizeset =s;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case MULTISETD : return "mset of "+domain.toString();
		case MULTISETD_SIZESET : return "mset " + sizeset.toString() + "of " + domain.toString();
		}		
		return "";
	}
	
}
