package translator.conjureEssenceSpecification;

import translator.conjureEssenceSpecification.AtomicExpression;
import translator.conjureEssenceSpecification.NonAtomicExpression;
import translator.conjureEssenceSpecification.Expression;

public class AtomExpression {

	AtomicExpression atomicVariable;
	NonAtomicExpression arrayElement;
	
	
	public AtomExpression(AtomicExpression variable) {
		this.atomicVariable = variable;
	}
	
	public AtomExpression(NonAtomicExpression variable) {
		this.arrayElement = variable;
	}
	
	public AtomExpression copy() {
		if(this.atomicVariable !=null)
			return new AtomExpression(this.atomicVariable.copy());
		else return new AtomExpression(this.arrayElement.copy());
					
	}
	
	public AtomicExpression getAtomicVariable() {
		return this.atomicVariable;
	}
	
	public NonAtomicExpression getNonAtomicVariable() {
		return this.arrayElement;
	}
	
	public boolean isArrayElement() {
		return (arrayElement != null) ?
				true : false;
	}
	
	public Expression getCorrespondingExpression() {
		if(this.atomicVariable != null)
			return new Expression(atomicVariable);
		else return new Expression(this.arrayElement);
	}
	
	public String toString() {
		
		return (this.arrayElement != null) ?
				this.arrayElement.toString() : 
					this.atomicVariable.toString();
	}
	
}
