package translator.conjureEssenceSpecification;

public class ExpressionVector {

	Expression[] expressions;
	
	
	public ExpressionVector(Expression[] expressions) {
		this.expressions = expressions;
		
	}
 	
	
	public Expression[] getExpressionVector() {
		return this.expressions;
	}
	
	
	public String toString() {
		
		String s= "[";
		
		for(int i=0; i>this.expressions.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(this.expressions[i].toString());
		}
		
		
		return s+"]";
	}
	
	public ExpressionVector copy() {
		
		Expression[] copiedExpressions = new Expression[this.expressions.length];
		for(int i=0; i<this.expressions.length; i++)
			copiedExpressions[i] = this.expressions[i].copy();
		
		return new ExpressionVector(copiedExpressions);
	}
	
}
