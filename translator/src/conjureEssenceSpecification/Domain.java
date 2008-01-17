package conjureEssenceSpecification;

public class Domain implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : bracketed domain;
	 * restriction_mode 2 : boolean
	 * restriction_mode 3 : integer range;
	 * restriction_mode 4 : identifier range
	 * restriction_mode 5 : set
	 * restriction_mode 6 : multi set
	 * restriction_mode 7 : matrix
	 * restriction_mode 8 : implication
	 * restriction_mode 9 : rel
	 * restriction_mode 10 : partition
	 * restriction_mode 11 : rpartition
	 */
	int restriction_mode;
	Domain domain;
	IntegerDomain intdomain;
	IdentifierDomain identifier;
	SetDomain set;
	MultiSetDomain mset;
	MatrixDomain matrix;
	FunctionDomain function;
	RelDomain rel;
	PartitionDomain partition;
	RPartitionDomain rpartition;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	
	public Domain getDomain(){
		return domain;
	}
	public IntegerDomain getIntegerDomain(){
		return intdomain;
	}
	public IdentifierDomain getIdentifierDomain(){
		return identifier;
	}
	public SetDomain getSetDomain(){
		return set;
	}
	public MultiSetDomain getMultiSetDomain(){
		return mset;
	}
	public MatrixDomain getMatrixDomain(){
		return matrix;
	}
	public FunctionDomain getFunctionDomain(){
		return function;
	}
	public RelDomain getRelationDomain(){
		return rel;
	}
	public PartitionDomain getPartitionDomain(){
		return partition;
	}
	public RPartitionDomain getRPartitionDomain(){
		return rpartition;
	}
	
	public void setDomain(Domain d){
		domain=d;
	}
	public void setIntegerDomain(IntegerDomain id){
		intdomain = id;
	}
	public void setIdentifierDomain(IdentifierDomain id){
		identifier=id;
	}
	public void setSetDomain(SetDomain s){
		set=s;
	}
	public void setMultiSetDomain(MultiSetDomain msd){
		mset=msd;
	}
	public void setMatrixDomain(MatrixDomain md){
		matrix=md;
	}
	public void setFunctionDomain(FunctionDomain fd){
		function=fd;
	}
	public void setRelationDomain(RelDomain rd){
		rel=rd;
	}
	public void setPartitionDomain(PartitionDomain pd){
		partition=pd;
	}
	public void setRPartitionDomain(RPartitionDomain rpd){
		rpartition = rpd;
	}
	

    public Domain copy() {

	
	switch(restriction_mode) {
	    
	case EssenceGlobals.BRACKETED_DOMAIN:
	    return new Domain(this.getDomain().copy());

	case EssenceGlobals.BOOLEAN_DOMAIN:
	    return new Domain(new Domain());

	case EssenceGlobals.INTEGER_RANGE:
	    return new Domain(this.getIntegerDomain().copy());
	    
	case EssenceGlobals.IDENTIFIER_RANGE:
	    return new Domain(this.getIdentifierDomain().copy());

	case EssenceGlobals.MATRIX_DOMAIN:
	    return new Domain(this.getMatrixDomain().copy());

	default: // i know, very ugly, but I don't care right now
	    return null;

	}
	
    }
	
	public Domain(Domain d){		
		restriction_mode = BRACKETED_DOMAIN;
		domain = d;
	}
	
	public Domain(){		
		restriction_mode = BOOLEAN_DOMAIN;
	}
	
	public Domain(IntegerDomain d){		
		restriction_mode = INTEGER_RANGE;
		intdomain = d;
	}
	public Domain(IdentifierDomain d){		
		restriction_mode = IDENTIFIER_RANGE;
		identifier = d;
	}
	
	public Domain(SetDomain d){		
		restriction_mode = SET_DOMAIN;
		set = d;
	}
	
	public Domain(MultiSetDomain d){		
		restriction_mode = MULTISET_DOMAIN;
		mset = d;
	}
	
	public Domain(MatrixDomain d){		
		restriction_mode = MATRIX_DOMAIN;
		matrix = d;
	}
	
	public Domain(FunctionDomain d){		
		restriction_mode = FUNCTION_DOMAIN;
		function = d;
	}
	
	public Domain(RelDomain d){		
		restriction_mode = REL_DOMAIN;
		rel = d;
	}
	
	public Domain(PartitionDomain d){		
		restriction_mode = PARTITION_DOMAIN;
		partition = d;
	}
	
	public Domain(RPartitionDomain d){		
		restriction_mode = RPARTITION_DOMAIN;
		rpartition = d;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case BRACKETED_DOMAIN : return "( " + domain.toString() + " ) ";
		case BOOLEAN_DOMAIN : return "bool ";
		case INTEGER_RANGE : return intdomain.toString();
		case IDENTIFIER_RANGE : return identifier.toString();
		case SET_DOMAIN : return set.toString();
		case MULTISET_DOMAIN : return mset.toString();
		case MATRIX_DOMAIN : return matrix.toString();
		case FUNCTION_DOMAIN : return function.toString();
		case REL_DOMAIN : return rel.toString();
		case PARTITION_DOMAIN : return partition.toString();
		case RPARTITION_DOMAIN : return rpartition.toString();		
		}		
		return "";
	}
	

}
