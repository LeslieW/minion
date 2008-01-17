package conjureEssenceSpecification;

public class Type implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : expression restriction_mode
	 * restriction_mode 2 : enumeration restriction_mode;
	 */
	int restriction_mode;
	Expression expression;
	Enumeration enumeration;
	
	public Type copy() {
		if(expression != null)
			return new Type(expression.copy());
		else return new Type(enumeration.copy());
	}
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	
	public Expression getExpression(){
		return expression;
	}
	
	public Enumeration getEnumeration(){
		return enumeration;
	}
	
	public Type(Expression e){
		restriction_mode = TYPE_EXPR;
		expression = e;
	}
	
	public Type(Enumeration e){
		restriction_mode = TYPE_ENUM;
		enumeration = e;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case TYPE_EXPR : return "of size " + expression.toString();
		case TYPE_ENUM : return "enum { " + enumeration.toString() + "} ";
		}
		return "";
	}

}
