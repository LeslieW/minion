package translator.conjureEssenceSpecification;

public class DomainRel {
	
	int restriction_mode;
	Multiplicity multileft;
	Domain domain;
	Multiplicity multiright;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	
	public Multiplicity getmultileft(){
		return multileft;
	}
	public Domain getdomain(){
		return domain;
	}
	public Multiplicity getmultiright(){
		return multiright;
	}
	public void setmultileft(Multiplicity m){
		multileft=m;
	}
	public void setdomain(Domain d){
		domain=d;
	}
	public void setmultiright(Multiplicity m){
		multiright=m;
	}
	
	public DomainRel(Multiplicity ml,Domain d, Multiplicity mr){
		restriction_mode = 1;
		multileft = ml;
		domain = d;
		multiright = mr;
	}
	
	public DomainRel(Multiplicity ml,Domain d){
		restriction_mode = 2;
		multileft = ml;
		domain = d;
	}
	
	public String toStirng(){
		switch(restriction_mode){
		case 1 : return multileft.toString() + domain.toString() + multiright.toString();
		case 2 : return multileft.toString() + domain.toString();
		}
		return "";
	}

}
