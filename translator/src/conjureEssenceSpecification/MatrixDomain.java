package conjureEssenceSpecification;

public class MatrixDomain implements EssenceGlobals {
	
	/**
	 * The domain(s) that are indices of the matrix are called
	 * "indexDomains" and the domain over which the matrix-elements 
	 * may range is called "rangeDomain".	 
	 * 
	 * Essence' grammar: (terminals in capital letters)
	 *   MATRIX INDEXED BY [ { domain }' ] OF domain
	 *   
	 */
	
	int restriction_mode;
	Domain[] indexDomains;
	Domain rangeDomain;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public Domain[] getIndexDomains(){
		return indexDomains;
	}
	public void setIndexDomains(Domain[] d){
		indexDomains=d;
	}
	public Domain getRangeDomain(){
		return rangeDomain;
	}
	public void setRangeDomain(Domain d){
		rangeDomain=d;
	}

    public MatrixDomain copy() {
	Domain[] mxDomain = new Domain[indexDomains.length];
	for(int i=0; i < mxDomain.length; i++) 
	    mxDomain[i] = indexDomains[i].copy();

	Domain d = rangeDomain.copy();
	return new MatrixDomain(mxDomain, d);
    }
	
	public MatrixDomain(Domain[] ds,Domain d){
		restriction_mode = MATRIXD_DOMAIN;
		indexDomains = ds;
		rangeDomain = d;
	}
	
	public String toString(){
		return "matrix indexed by [ " + getDomains() + "] of "+ rangeDomain.toString();
	}
	
	public String getDomains(){
		String output = "";
		for(int i =0;i<indexDomains.length;i++){
			output+= indexDomains[i].toString() + ", ";
		}
		return output;
	}

}
