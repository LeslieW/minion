package conjureEssenceSpecification;

public class Multiplicity {
	
	Expression expression;
	
	public Expression getexpression(){
		return expression;
	}
	public void setexpression(Expression e){
		expression=e;
	}
	
	public Multiplicity(Expression e){
		expression = e;
	}

	public String toString(){
		return "( "+expression.toString()+") ";
	}
}
