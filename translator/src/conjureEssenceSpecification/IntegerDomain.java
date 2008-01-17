package conjureEssenceSpecification;

public class IntegerDomain implements EssenceGlobals {
	/**
	 * restriction_mode 1 : no range
	 * restriction_mode 2 : with range
	 * 
	 * 	 * 
	 * Essence' grammar: (terminals in capital letters)
	 *   int [ "(" { rangeAtom }' ")" ]
	 * 
	 */
	int restriction_mode;
	RangeAtom[] rangelist;
	
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

    public IntegerDomain copy()  {
	
	if(rangelist == null) 
	    return new IntegerDomain();

	else {
	    RangeAtom[] ranges = new RangeAtom[rangelist.length];
	    for(int i = 0; i < rangelist.length; i++)
		ranges[i] = rangelist[i].copy();
	    return new IntegerDomain(ranges);
	}
    }
	
	public IntegerDomain(){
		restriction_mode = INT_DOMAIN;
	}
	
	public IntegerDomain(RangeAtom[] r){
		restriction_mode = INT_DOMAIN_RANGE;
		rangelist = r;
	}
	
	public String toString(){
		
	    switch(restriction_mode){
	    case EssenceGlobals.INT_DOMAIN : return "int ";
	    case EssenceGlobals.INT_DOMAIN_RANGE : return "int ( " + getRanges() + " ) ";
	    }	
	    return "";
	}
    
	public String getRanges(){
		String output = "";
		output+=rangelist[0].toString();
		for(int i = 1;i<rangelist.length;i++){
			output += ", " + rangelist[i].toString();
		}
		
		return output;
	}

}
