package conjureEssenceSpecification;

public class TupleList {
	
	/**
	 * restriction_mode 1 : no domain
	 * restriction_mode 2 : with domain
	 */
	int restriction_mode;
	Tuple[] tuples;
	Domain domain;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	
	public Tuple[] gettuples(){
		return tuples;
	}
	public void settuples(Tuple[] t){
		tuples=t;
	}
	public Domain getdomain(){
		return domain;
	}
	public void setdomain(Domain d){
		domain = d;
	}
	
	public TupleList(Tuple[] t){
		restriction_mode = 1;
		tuples = t;
	}
	
	public TupleList(Tuple[] t,Domain d){
		restriction_mode = 2;
		tuples = t;
		domain = d;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case 1 : return getTuples();
		case 2 : return getTuples() + " : " + domain.toString();
		}
		return "";
	}
	
	public String getTuples(){
		String output = "";
		for(int i =0;i<tuples.length;i++){
			output+= tuples[i].toString() + ", ";
		}
		return output;
	}

}
