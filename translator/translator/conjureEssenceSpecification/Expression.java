package translator.conjureEssenceSpecification;

public class Expression implements EssenceGlobals {
	
    /**
     * restriction_mode 1 : bracketed expression
     * restriction_mode 2 : atomic expression
     * restriction_mode 3 : non-atomic exp
     * restriction_mode 4 : group op exp
     * restriction_mode 5 : unit op exp
     * restriction_mode 6 : binary op exp
     * restriction_mode 7 : function op exp
     * restriction_mode 8 : quantifier op exp 
     */
    int restriction_mode;
    Expression exp;
    AtomicExpression atomic;
    NonAtomicExpression nonatomic;
    GroupExpression groupop;
    UnaryExpression unitop;
    BinaryExpression binaryop;
    FunctionExpression functionop;
    QuantificationExpression quantifierop;
    LexExpression lexExpression;
    
    public int getRestrictionMode(){
	return restriction_mode;
    }
   
    public Expression getExpression(){
	return exp;
    }
    public AtomicExpression getAtomicExpression(){
	return atomic;
    }
    public NonAtomicExpression getNonAtomicExpression(){
	return nonatomic;
    }
    public GroupExpression getGroupExpression(){
	return groupop;
    }
    public UnaryExpression getUnaryExpression(){
	return unitop;
    }
    public BinaryExpression getBinaryExpression(){
	return binaryop;
    }
    public FunctionExpression getFunctionExpression(){
	return functionop;
    }
    public QuantificationExpression getQuantification(){
	return quantifierop;
    }
    
    public LexExpression getLexExpression() {
    	return this.lexExpression;
    }
    
    public Expression copy() {
	switch(this.getRestrictionMode()) {
	    
	case EssenceGlobals.ATOMIC_EXPR:
	    return new Expression(this.getAtomicExpression().copy());

	case EssenceGlobals.BINARYOP_EXPR:
	    return new Expression(this.getBinaryExpression().copy());

	case EssenceGlobals.NONATOMIC_EXPR:
	    return new Expression(this.getNonAtomicExpression().copy());

	case EssenceGlobals.FUNCTIONOP_EXPR:
	    return new Expression(this.getFunctionExpression().copy());

	case EssenceGlobals.UNITOP_EXPR:
	    return new Expression(this.getUnaryExpression().copy());
	    
	case EssenceGlobals.QUANTIFIER_EXPR:
		return new Expression(this.quantifierop.copy());
	    
	case EssenceGlobals.LEX_EXPR:
		return new Expression(this.lexExpression.copy());
		
	case EssenceGlobals.BRACKET_EXPR:
		return new Expression(this.exp.copy());
		
	default:
	    return null;
	}
    }
    
  /*  public void setExpression(Expression e){
	exp=e;
    }
    public void setAtomicExpression(AtomicExpression ae){
	atomic=ae;
    }
    public void setNonAtomicExpression(NonAtomicExpression nae){
	nonatomic=nae;
    }
    public void setGroupExpression(GroupExpression goe){
	groupop=goe;
    }
    public void setUnaryExpression(UnaryExpression uop){
	unitop=uop;
    }
    public void setBinaryExpression(BinaryExpression bop){
	binaryop=bop;
    }
    public void setFunctionExpression(FunctionExpression fop){
	functionop=fop;
    }
    public void setQuantification(QuantificationExpression qop){
	quantifierop=qop;
    }*/
    
    public Expression(Expression e){
		restriction_mode = BRACKET_EXPR;
		exp = e;
	}
	
	public Expression(AtomicExpression e){
		restriction_mode = ATOMIC_EXPR;
		atomic = e;
	}
	
	public Expression(NonAtomicExpression e){
		restriction_mode = NONATOMIC_EXPR;
		nonatomic = e;
	}
	
	public Expression(GroupExpression e){
		restriction_mode = GROUPOP_EXPR;
		groupop = e;
	}
	
	public Expression(UnaryExpression e){
		restriction_mode = UNITOP_EXPR;
		unitop = e;
	}
	
	public Expression(BinaryExpression e){
		restriction_mode = BINARYOP_EXPR;
		binaryop = e;
	}
	
	public Expression(FunctionExpression e){
		restriction_mode = FUNCTIONOP_EXPR;
		functionop = e;
	}
	
	public Expression(QuantificationExpression e){
		restriction_mode = QUANTIFIER_EXPR;
		quantifierop = e;
	}
	
	public Expression(LexExpression e) {
		restriction_mode = LEX_EXPR;
		lexExpression = e;
	}
	
	public String toString() {
		
		switch(restriction_mode){
		case BRACKET_EXPR : return "( " + exp.toString() + " ) ";
		case ATOMIC_EXPR : return atomic.toString();
		case NONATOMIC_EXPR : return nonatomic.toString();
		case GROUPOP_EXPR : return groupop.toString();
		case UNITOP_EXPR : return unitop.toString();
		case BINARYOP_EXPR : return binaryop.toString();
		case FUNCTIONOP_EXPR : return functionop.toString();
		case QUANTIFIER_EXPR : return quantifierop.toString();
		case LEX_EXPR: return lexExpression.toString();
		}		
		return "";
	}


}
