package translator.conjureEssenceSpecification;

public class Constant implements EssenceGlobals {
	
	/**
	 * 	 
	 * Essence' grammar: (terminals in capital letters)
	 *   constant :==  identifier BE DOMAIN domain
	 *               | identifier [ : domain ] BE expression
	 *               | identifier BE NEW TYPE type
	 *               | identifier BE array
	 */
	
	int restriction_mode;
	DomainConstant domainConstant;
	ExpressionConstant expressionConstant;
	NewTypeConstant newTypeConstant;
	ConstantArray constantArray;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	
	public Constant copy() {
		
		if(this.domainConstant != null)
			return new Constant(domainConstant.copy());
		
		else if(this.expressionConstant != null)
			return new Constant(expressionConstant.copy());
		
		else return new Constant(newTypeConstant.copy()); 
	}
	
	public DomainConstant getDomainConstant(){
		return domainConstant;
	}
	public void setDomainConstant(DomainConstant bd){
		domainConstant=bd;
	}
	public ExpressionConstant getExpressionConstant(){
		return expressionConstant;
	}
	
	public ConstantArray getConstantArray() {
		return this.constantArray;
	}
	
	
	public void setExpressionConstant(ExpressionConstant be){
		this.expressionConstant = be;
	}
	public NewTypeConstant getNewTypeConstant(){
		return newTypeConstant;
	}
	public void setNewTypeConstant(NewTypeConstant bnt){
		newTypeConstant = bnt;
	}
	
	public Constant(DomainConstant b){
		restriction_mode = CONSTANT_DOMAIN;
		domainConstant = b;
	}
	
	public Constant(ExpressionConstant b){
		restriction_mode = CONSTANT;
		expressionConstant = b;
	}
	
	public Constant(NewTypeConstant b){
		restriction_mode = CONSTANT_NEW_TYPE;
		newTypeConstant = b;
	}

	
	public Constant(ConstantArray array) {
		this.constantArray = array;
		this.restriction_mode = EssenceGlobals.CONSTANT_ARRAY;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case CONSTANT_DOMAIN : return domainConstant.toString();
		case CONSTANT : return expressionConstant.toString();
		case CONSTANT_NEW_TYPE : return newTypeConstant.toString();
		case CONSTANT_ARRAY: return this.constantArray.toString();
		}
		
		return "";
	}
}
