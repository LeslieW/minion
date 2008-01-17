package conjureEssenceSpecification;

public class QuantificationExpression {
	
	Quantifier quantifier;
	BindingExpression bindingexp;
	Expression exp;
	
	
	public Quantifier getQuantifier(){
		return quantifier;
	}
	public void setQuantifier(Quantifier q){
		quantifier=q;
	}
	public BindingExpression getBindingExpression(){
		return bindingexp;
	}
	public void setBindingExpression(BindingExpression be){
		bindingexp=be;
	}
	public Expression getExpression(){
		return exp;
	}
	public void setExpression(Expression e){
		exp=e;
	}

    public QuantificationExpression copy() {
	return new QuantificationExpression(quantifier.copy(), bindingexp.copy(), exp.copy());
    }
	
	public QuantificationExpression(Quantifier q, BindingExpression b, Expression e){
		quantifier = q;
		bindingexp = b;
		exp = e;
	}
	
	public String toString(){	
		return quantifier.toString() + bindingexp.toString() + " . " + exp.toString();
	}

}
