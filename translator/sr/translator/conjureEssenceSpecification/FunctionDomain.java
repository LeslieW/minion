package translator.conjureEssenceSpecification;

public class FunctionDomain implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : without function descriptors
	 * restriction_mode 2 : with kind
	 * restriction_mode 3 : with class
	 * restriction_mode 4 : with both
	 */
	int restriction_mode;
	Domain domainLeft;
	Domain domainRight;
	KindFunction kf;
	ClassFunction cf;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	
	public Domain getdomainLeft(){
		return domainLeft;
	}
	public Domain getdomainRight(){
		return domainRight;
	}
	public void setdomainLeft(Domain d){
		domainLeft=d;
	}
	public void setdomainRight(Domain d){
		domainRight=d;
	}
	
	public KindFunction getkindfunction(){
		return kf;
	}
	public ClassFunction getclassfunction(){
		return cf;
	}
	
	public void setkindfunction(KindFunction kf){
		this.kf=kf;
	}
	public void setclassfunction(ClassFunction cf){
		this.cf=cf;
	}
	
	public FunctionDomain(Domain d1,Domain d2){
		restriction_mode = FUNCTIOND;
		domainLeft = d1;
		domainRight = d2;
	}
	public FunctionDomain(Domain d1,KindFunction kf,Domain d2){
		restriction_mode = FUNCTIOND_KIND;
		domainLeft = d1;
		domainRight = d2;
		this.kf=kf;
	}
	
	public FunctionDomain(Domain d1,KindFunction kf,ClassFunction cf,Domain d2){
		restriction_mode = FUNCTIOND_KIND_CLASS;
		domainLeft = d1;
		domainRight = d2;
		this.kf=kf;
		this.cf=cf;
	}
	
	public FunctionDomain(Domain d1,ClassFunction cf,Domain d2){
		restriction_mode = FUNCTIOND_CLASS;
		domainLeft = d1;
		domainRight = d2;
		this.cf=cf;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case FUNCTIOND : return domainLeft.toString() + "-> " + domainRight.toString();
		case FUNCTIOND_KIND : return domainLeft.toString() + "-> " + kf.toString() + domainRight.toString();
		case FUNCTIOND_CLASS : return domainLeft.toString() + "-> " + cf.toString() + domainRight.toString();
		case FUNCTIOND_KIND_CLASS : return domainLeft.toString() + "-> " + kf.toString() + cf.toString() + domainRight.toString();
		}		
		return "";
	}
}
