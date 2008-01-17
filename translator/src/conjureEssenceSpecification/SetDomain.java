package conjureEssenceSpecification;

public class SetDomain implements EssenceGlobals {
	
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
	
	public SetDomain(Domain d){
		restriction_mode = SETD;
		domain = d;
	}
	public SetDomain(SizeSet s,Domain d){
		restriction_mode = SETD_SIZESET;
		domain = d;
		sizeset =s;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case SETD : return "set of "+domain.toString();
		case SETD_SIZESET : return "set " + sizeset.toString() + "of " + domain.toString();
		}		
		return "";
	}

}
