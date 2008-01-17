package translator.conjureEssenceSpecification;

/**
 * Essence' grammar: (terminals in capital letters)
 * expression binaryOperator expression
 *
 */

public class BinaryExpression {
	
	BinaryOperator biop;
	Expression expleft;
	Expression expright;
	
	public BinaryOperator getOperator(){
		return biop;
	}
	public Expression getLeftExpression(){
		return expleft;
	}
	public Expression getRightExpression(){
		return expright;
	}
	
	public void setOperator(BinaryOperator biop){
		this.biop =biop;
	}
	public void setLeftExpression(Expression expleft){
		this.expleft=expleft;
	}
	public void setRightExpression(Expression expright){
		this.expright=expright;
	}

    public BinaryExpression copy() {
        return  new BinaryExpression(expleft.copy(), biop.copy(), expright.copy());

    }
	
	public BinaryExpression(Expression el,BinaryOperator b,Expression er){
		expleft = el;
		biop = b;
		expright = er;
	}
	
	public String toString(){
		return " "+expleft.toString()+" " + biop.toString() + " "+expright.toString()+" ";
		//return biop.toString() +"("+expleft.toString() +", "+ expright.toString()+")";
	}

}
