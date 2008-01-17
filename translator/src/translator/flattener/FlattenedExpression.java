package translator.flattener;

import translator.conjureEssenceSpecification.*;

import java.util.ArrayList;

public class FlattenedExpression {

	private Expression unflattenedExpression;
	
	/**
	 * Represents the subexpression unflattenedExpression
	 * and should be a 
	 * variable (most likely an auxiliary variable)
	 */
	private AtomExpression variable;
	
	/**
	 * Represents the subexpression unflattenedExpression 
	 * and should be a 
	 * variable (most likely an auxiliary variable)
	 */
	Expression finalExpression;
	
	/** 
	 * Contains all expressions that are added up together 
	 * in the subexpression
	 */	
	private ArrayList<AtomExpression> plusList;
	
	/**
	 * Contains all the expressions that are subtracted in the
	 * subexpression
	 */
	private ArrayList<AtomExpression> minusList;
	
	
	public FlattenedExpression(Expression unflattenedExpression, Expression representingExpression) {
		this.unflattenedExpression = unflattenedExpression;
		this.finalExpression = representingExpression;
		this.plusList = new ArrayList<AtomExpression>();
		this.minusList = new ArrayList<AtomExpression>();
	}
	
	public FlattenedExpression(Expression unflattenedExpression, AtomExpression expression, boolean isPlus) {
		this.unflattenedExpression = unflattenedExpression;
		this.plusList = new ArrayList<AtomExpression>();
		this.minusList = new ArrayList<AtomExpression>();
		if(isPlus)
			plusList.add(expression);
		else minusList.add(expression); 
	}

	public FlattenedExpression(Expression subExpression, AtomExpression representingVar) {
		this.unflattenedExpression = subExpression;
		this.variable = representingVar;
		this.plusList = new ArrayList<AtomExpression>();
		this.minusList = new ArrayList<AtomExpression>();
	}
	
	public FlattenedExpression(Expression subExpression, AtomicExpression representingVar) {
		this.unflattenedExpression = subExpression;
		this.variable = new AtomExpression(representingVar);
		this.plusList = new ArrayList<AtomExpression>();
		this.minusList = new ArrayList<AtomExpression>();
	}
	
	public FlattenedExpression(Expression subExpression, NonAtomicExpression representingVar) {
		this.unflattenedExpression = subExpression;
		this.variable = new AtomExpression(representingVar);
		this.plusList = new ArrayList<AtomExpression>();
		this.minusList = new ArrayList<AtomExpression>();
	}
	
	
	
	
	/**
	 * Copy a subexpression-representation. 
	 * 
	 * @return
	 */
	public FlattenedExpression copy() {
		
		if(this.variable != null) 
			return new FlattenedExpression(this.unflattenedExpression, this.variable.copy());
		
		else if(finalExpression != null)
				return new FlattenedExpression(this.unflattenedExpression, this.finalExpression);
		
		
		else {
			FlattenedExpression newRepresentation = null;
		if(plusList.size() > 0){
			newRepresentation = new FlattenedExpression(this.unflattenedExpression, 
					                                            this.plusList.get(0));
			for(int i=0; i<this.plusList.size(); i++)
				newRepresentation.addPlusExpression(this.plusList.get(i).copy());
		
			if(minusList.size() >0)
				for(int i=0; i<this.plusList.size(); i++)
					newRepresentation.addMinusExpression(this.minusList.get(i).copy());
			
			return newRepresentation;
		}
		else {
			newRepresentation = new FlattenedExpression(this.unflattenedExpression, 
                    																		this.minusList.get(0));
			for(int i=0; i<this.minusList.size(); i++)
				newRepresentation.addMinusExpression(this.plusList.get(i).copy());

			return newRepresentation;
		}
		}
	}
	
	
	/**
	 * Set the expression that the flattened expressions represent
	 * 
	 * @param expression
	 */
	//public void setUnflattenedExpression(Expression expression) {
	//	this.unflattenedExpression = expression;
	//}

	public void replaceWithVariable(AtomicExpression variable, Expression newExpression) {
		this.variable = new AtomExpression(variable);
		this.plusList.clear();
		this.minusList.clear();
		this.unflattenedExpression = newExpression;
		
	}
	
	public void replaceWithVariable(NonAtomicExpression variable, Expression newExpression) {
		this.variable = new AtomExpression(variable);
		this.plusList.clear();
		this.minusList.clear();
		this.unflattenedExpression = newExpression;
	}
	
	public void replaceWithVariable(AtomExpression variable, Expression newExpression) {
		this.variable = variable;
		this.plusList.clear();
		this.minusList.clear();
		this.unflattenedExpression = newExpression;
	} 
	
	
	public void replaceFinalExpressionWith(Expression e) {
		this.finalExpression = e;
		this.variable = null;
		this.minusList.clear();
		this.plusList.clear();
	}
	
	
	public void addPlusExpression(AtomExpression e) {
		this.plusList.add(e);
	}
	
	public void addMinusExpression(AtomExpression e) {
		this.minusList.add(e);
	}
	
	public boolean isAtomExpression() {
		return (this.variable != null) ?
					true : false;
	}
	
	public boolean isFinalExpression() {
		return (this.finalExpression != null) ?
				true : false;
	}
	
	public boolean isLinearExpression() {
		return (this.minusList.size() > 0 ||
				this.plusList.size() > 0) ?
						true : false;
	}
	
	/**
	 * Returns the variable representing the currently
	 * processed subexpression. Might return null!
	 * @return
	 */
	public AtomicExpression getAtomicVariable() {
		if(this.variable != null)
			return (this.variable.getAtomicVariable());
		else return null;
	}
	
	public NonAtomicExpression getNonAtomicVariable() {
		if(this.variable != null)
			return this.variable.getNonAtomicVariable();
		else return null;
	}
	
	public AtomExpression getVariable() {
		return this.variable;
	}
	
	
	
	public LinearExpression getLinearExpression() {
		
		ArrayList<AtomExpression> positives = new ArrayList<AtomExpression>();
		for(int i=0; i< this.plusList.size(); i++)
			positives.add(i, plusList.get(i)); 
		
		ArrayList<AtomExpression> negatives = new ArrayList<AtomExpression>();
		for(int i=0; i< this.minusList.size(); i++)
			negatives.add(i, minusList.get(i)); 
		
		boolean isPositive = true;
		if(positives.size() <= 0)
			isPositive = false;
					
			if(!isPositive)
				return new LinearExpression(negatives, false); //not positive
			else if(negatives.size() <= 0)
				return new LinearExpression(positives, true); // positive
			else 
				return new LinearExpression(positives, negatives); // both pos and neg
		
	
	}
	

	
	
	public Expression getOriginalExpression() {
		return this.unflattenedExpression;
	}
	
	public Expression getFinalExpression() {
		return this.finalExpression;
	}
	
	
	public AtomExpression getAtomExpression() {
		return this.variable;
	}
	
	/**
	 * Returns the variable as type Expression (if it is not
	 * null). This means that this variable has either an
	 * atomic or non-atomic type.
	 * @return
	 */
	public Expression getAtomAsExpression() {
	
		if(this.variable != null)
			return variable.getCorrespondingExpression();
		else return null;
	}
	
	
}
