package translator.conjureEssenceSpecification;

public class IdentifierDomain implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : no range
	 * restriction_mode 2 : with range list
	 * 
	 * 	 
	 * Essence' grammar: (terminals in capital letters or included in "")
	 *   identifier [ "(" { rangeAtom }' ")" ]
	 * 
	 */
	int restriction_mode;
	private String identifier;
	private RangeAtom[] rangelist;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public RangeAtom[] getRangeList(){
		return rangelist;
	}
	public void setRangeList(RangeAtom[] rl){
		rangelist = rl;
	}
	public String getIdentifier(){
		return identifier;
	}
	public void setIdentifier(String i){
		identifier=i;
	}

    public IdentifierDomain copy() {
	String ident = new String(identifier);
	if(rangelist == null)
	    return new IdentifierDomain(ident);
	else {
	    RangeAtom[] ranges = new RangeAtom[rangelist.length];
	    for(int i = 0; i < rangelist.length; i++)
		ranges[i] = rangelist[i].copy();
	    return new IdentifierDomain(ident, ranges);
	}
    }
	
	public IdentifierDomain(String i){
		restriction_mode = IDENTIFIER_DOMAIN;
		identifier = i;
	}
	
	public IdentifierDomain(String i,RangeAtom[] r){
		restriction_mode = IDENTIFIER_DOMAIN_RANGE;
		identifier = i;
		rangelist = r;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case IDENTIFIER_DOMAIN : return identifier;
		case IDENTIFIER_DOMAIN_RANGE : return identifier + "( " + getRange() + ") ";
		}
		
		return "";
	}
	
	public String getRange(){
		String output = "";
		for(int i =0; i< rangelist.length;i++){
			output+=rangelist[i].toString()+", ";
		}
		return output;
	}

}
