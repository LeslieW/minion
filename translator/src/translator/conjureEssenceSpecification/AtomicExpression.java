package translator.conjureEssenceSpecification;

public class AtomicExpression implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : number<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : boolean<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : identifier<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 4 : { } : domain<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 5 : # # : domain<br/>&nbsp&nbsp&nbsp
	 */
	private int restriction_mode;
	private int number;
	private boolean bool;
	private String identifier;
	private Domain domain;
	
	private AuxiliaryVariable auxVariable;
	
	
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public int getNumber(){
		return number;
	}
	public boolean getBool(){
		return bool;
	}
	public String getString(){
		return identifier;
	}
	public Domain getDomain(){
		return domain;
	}
	
	public AuxiliaryVariable getAuxiliaryVariable() {
		return this.auxVariable;
	}

	
	/*public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public void setNumber(int num){
		number=num;
	}
	public void setBool(boolean b){
		bool = b;
	}
	public void setString(String i){
		identifier = i;
	}
	public void setDomain(Domain d){
		domain = d;
	}
*/
    public AtomicExpression copy() {
	if(restriction_mode == EssenceGlobals.NUMBER) {
	    int i = number;
	    return new AtomicExpression(i);
	}
	else if (restriction_mode == EssenceGlobals.BOOLEAN) {
	    boolean b = bool;
	    return new AtomicExpression(b);
	}
	else { // we have an identifier
	    return new AtomicExpression(identifier);
	} 
	    
    }
	
    public AtomicExpression (int i){
    	restriction_mode = NUMBER;
    	number = i;
    }
    
    public AtomicExpression (boolean b){
    	restriction_mode = BOOLEAN;
    	bool = b;
    }
	
    public AtomicExpression (String i){
    	restriction_mode = IDENTIFIER;
    	identifier = i;
    }
    
    public AtomicExpression(AuxiliaryVariable auxiliaryVariable) {
    	this.auxVariable = auxiliaryVariable;
    	this.restriction_mode = AUX_VARIABLE;
    }
    
    /**
     * 
     * @param i - restriction_mode<br/>&nbsp&nbsp&nbsp
     * restriction_mode 4 : { } : domain<br/>&nbsp&nbsp&nbsp
     * restriction_mode 5 : # # : domain<br/>&nbsp&nbsp&nbsp
     * @param d
     */
   /* public AtomicExpression (int i, Domain d){
	restriction_mode = i;
	domain = d;
    }
    */
    public String toString(){
	
    	String output = "";
	
    	switch(restriction_mode){
		
    	/*case NUMBER: output += toStringNumber();break;
    	case BOOLEAN: {output += toStringBoolean();break;} */
    	case BOOLEAN: return this.bool+"";
    	case NUMBER: return this.number+"";
    	case IDENTIFIER: output += identifier.toString();break;
    	case AUX_VARIABLE: return auxVariable.toString();
    	case ATOMIC_SET_DOMAIN: output += toStringBracket();break;
    	case ATOMIC_MULTISET_DOMAIN: output += toStringHash();break;
	    
    	}		
    	return output;
    }
    
    /*private String toStringNumber(){		
	return number + " ";
    }
    private String toStringBoolean(){		
	return bool + " ";
    }*/
    private String toStringBracket(){		
	return "{ } : " + domain.toString() + " ";
    }
    private String toStringHash(){		
	return "# # : " + domain.toString() + " ";
    }
    
}
