package translator.conjureEssenceSpecification;

public class RelDomain implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : without sizeSet
	 * restriction_mode 2 : with sizeset
	 */
	int restriction_mode;
	SizeSet sizeset;
	DomainRel[] domainrel;

	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
	
		restriction_mode = rm;
	}
	public SizeSet getsizeset(){
		return sizeset;
	}
	public void setsizeset(SizeSet ss){
		sizeset=ss;
	}
	public DomainRel[] getdomainrel(){
		return domainrel;
	}
	public void setdomainrel(DomainRel[] dr){
		domainrel=dr;
	}
	
	public RelDomain(DomainRel[] dr){
		restriction_mode = RELD;
		domainrel = dr;
	}
	
	public RelDomain(SizeSet ss,DomainRel[] dr){
		restriction_mode =RELD_SIZESET;
		sizeset = ss;
		domainrel = dr;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case RELD : return "rel " + getDomainRel();
		case RELD_SIZESET : return "rel " + sizeset.toString() + getDomainRel();
		}
		return "";
	}
	
	public String getDomainRel(){		
		String output ="";
		for(int i =0; i<domainrel.length;i++){
			output+= domainrel[i].toString()+"* ";
		}
		return output;		
	}

}
