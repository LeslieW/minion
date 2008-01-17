package translator.conjureEssenceSpecification;

public class ExpressionConstant implements EssenceGlobals{
	
	/**
	 *Type 1 : without domain
	 *Type 2 : With domain 
	 **/
	int restriction_mode;
	String name; // constant name
	Domain domain;
	Expression expression;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public String getName(){
		return name;
	}
	public Domain getDomain(){
		return domain;
	}
	public Expression getExpression(){
		return expression;
	}
	
	public ExpressionConstant copy() {
		if(this.domain != null)
			return new ExpressionConstant(new String(name), domain.copy(), expression.copy());
		else return new ExpressionConstant(new String(name), expression.copy());
	}
	
	public ExpressionConstant(String i,Domain d, Expression e){
		restriction_mode = BE_DOMAIN;
		name =i;
		domain=d;
		expression=e;
	}
	
	public ExpressionConstant(String i, Expression e){
		restriction_mode = BE;
		name =i;
		expression=e;
	}
	
	public String toString(){
		String output = "";
		switch(restriction_mode){
		case BE : output+=toString1();break;
		case BE_DOMAIN : output+=toString2();break;
		}
		return output;
	}
	
	public String toString1(){
		return name + " be " + expression.toString();
	}
	
	public String toString2(){
		return name + domain.toString() + " be " + expression.toString();
	}
}
