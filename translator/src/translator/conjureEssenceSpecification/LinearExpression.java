package translator.conjureEssenceSpecification;

import java.util.ArrayList;

public class LinearExpression {

	
	private ArrayList<AtomExpression> positiveList;
	private ArrayList<AtomExpression> negativeList;
	private AtomExpression result;
	private boolean resultIsOnRightSide;
	private int operator;
	
	/**
	 * Represents a list of elements that are added up (or substracted) - depending
	 * on if parameter isPositive is set to true(+) or not(-).
	 * 
	 * @param elementList
	 * @param isPositive
	 */
	public LinearExpression(ArrayList<AtomExpression> elementList, 
			                boolean isPositive) {
		if(isPositive) 
			this.positiveList = elementList;
		
		else this.negativeList = elementList;
	}
	
	
	/**
	 * E1 + ... + EN - EN+1 - ... - EN+M
	 * 
	 * with N.. length of positiveList and M length of negative list.
	 * 
	 * @param positiveList
	 * @param negativeList
	 */
	public LinearExpression(ArrayList<AtomExpression> positiveList,
			                ArrayList<AtomExpression> negativeList) {
		
		this.negativeList = negativeList;
		this.positiveList = positiveList;
	}
	
	
	/**
	 * Structure of linear expression:<br>
	 * <br>
	 *  elementList op result<br>
	 * <br>
	 * if resultIsOnRightSide is set to true. If set to
	 * false it is <br>
	 * <br>
	 *  result op elementlist<br>
	 *  <br>
	 *  Please note that operator has to be a relational operator.
	 *  
	 * @param elementList
	 * @param result
	 * @param isPositive
	 * @param resultIsOnRightSide
	 */
	public LinearExpression(ArrayList<AtomExpression> elementList,
				          int operator,
			              AtomExpression result,
			              boolean isPositive, 
			              boolean resultIsOnRightSide) {
		if(isPositive) 
			this.positiveList = elementList;
		else this.negativeList = elementList;
		
		this.resultIsOnRightSide = resultIsOnRightSide;
		this.result = result;
	}
	
	
	/**
	 * Linear expression has the structure: <br>
	 * <br>
	 * E1 + ... + EN - EN+1 - ... - EN+M   operator result <br>
	 * <br>
	 * if resultIsOnRightSide is set to true. operator has to 
	 * be a relational operator 
	 * ( N.. length of positiveList and M length of negative list)
	 * 
	 * @param positiveList
	 * @param negativeList
	 * @param operator
	 * @param result
	 * @param resultIsOnRightSide
	 */
	public LinearExpression(ArrayList<AtomExpression> positiveList,
			                ArrayList<AtomExpression> negativeList,
			                int operator,
			                AtomExpression result,
			                boolean resultIsOnRightSide) {
		
		this.positiveList = positiveList;
		this.negativeList = negativeList;
		this.operator = operator;
		this.result = result;
		this.resultIsOnRightSide = resultIsOnRightSide;
		
	}
	
	
	
	/**
	 * deep copy of an linear expression
	 * @return
	 */
	public LinearExpression copy() {
		
		ArrayList<AtomExpression> positives = new ArrayList<AtomExpression>();
		for(int i=0; i< this.positiveList.size(); i++)
			positives.add(i, positiveList.get(i)); 
		
		ArrayList<AtomExpression> negatives = new ArrayList<AtomExpression>();
		for(int i=0; i< this.negativeList.size(); i++)
			negatives.add(i, negativeList.get(i)); 
		
		boolean isPositive = true;
		if(positives.size() <= 0)
			isPositive = false;
		
		if(this.result == null) {
			
			if(!isPositive)
				return new LinearExpression(negatives, false); //not positive
			else if(negatives.size() <= 0)
				return new LinearExpression(positives, true); // positive
			else 
				return new LinearExpression(positives, negatives); // both pos and neg
		
		}
		
		else {
			if(!isPositive)
				return new LinearExpression(negatives, 
						                    this.operator,
						                    this.result.copy(),
						                    false,
						                    this.resultIsOnRightSide);
			else if(negatives.size() <= 0) 
				return new LinearExpression(positives, 
											this.operator,
											this.result.copy(),
											true,
											this.resultIsOnRightSide);
			else 
				return new LinearExpression(positives,
						                    negatives,
						                    this.operator,
						                    this.result.copy(),
				                            this.resultIsOnRightSide);
		}
	}
	
	
	public ArrayList<AtomExpression> getPositiveElements() {
		return this.positiveList;
	}
	
	
	public ArrayList<AtomExpression> getNegativeElements() {
		return this.negativeList;
	}
	
	/**
	 * Returns true, if the linear expression is relational, i.e. of the form<br>
	 * E1 +- E2 +- ... +- En Relop E
	 * 
	 * @return
	 */
	public boolean isRelationalLinearExpression() {
		return (this.result != null) ?
				true : false;
		
	}
	
	/**
	 * Returns true if the linear expression has elements that
	 * are composed by addition
	 * 
	 * @return
	 */
	public boolean hasPositiveElements() {
		return (this.positiveList.size() >0) ?
				true : false;
	}
	
	
	/**
	 * Returns true if the linear expression has elements that
	 * are composed by substraction
	 * @return
	 */
	public boolean hasNegativeElements() {
		return (this.negativeList.size() >0) ?
				true : false;
	}
	
	public String toString() {
		
		String listString = "";
		for(int i=0; i<this.positiveList.size()-1; i++) {
			listString = listString.concat(positiveList.get(i).toString()+" + ");
		}
		if(positiveList.size() > 0)
			listString.concat(positiveList.get(positiveList.size()-1).toString());
		
		for(int i=0; i<this.negativeList.size(); i++) 
			listString = listString.concat(" - "+negativeList.get(i).toString());
		
		
		
		if(result!=null) {
			String resultString = this.result.toString();
			String operatorString = new BinaryOperator(this.operator).toString();
			
			return (this.resultIsOnRightSide) ?
					resultString+operatorString+listString : 
						listString+operatorString+resultString;
		}
		else {
			return listString;
		}
		
	}
	
	
}
