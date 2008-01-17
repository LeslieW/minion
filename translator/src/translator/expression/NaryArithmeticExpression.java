	package translator.expression;

import java.util.ArrayList;


public abstract class NaryArithmeticExpression implements ArithmeticExpression {

	//public ArrayList<Expression> getArguments();
	private boolean isNested = true;
	private boolean willBeReified = false;
	
	
	public int[] getDomain() {
		// TODO Auto-generated method stub
		return new int[] {Expression.LOWER_BOUND, Expression.UPPER_BOUND};
	}
	
	
// ==================== Additional Methods =====================
	
	/**
	 * Order the list of Expressions according to the Essence' 
	 * expression ordering. Returns an ordered List.
	 * 
	 * @param list
	 * @return 
	 */
	
	protected ArrayList<Expression> orderExpressionList(ArrayList<Expression> list) {
		
		ArrayList<Expression> orderedList = new ArrayList<Expression>();
		
		for(int i=0; i<list.size(); i++) {
			orderedList = insertIntoOrderedList(list.get(i), orderedList);
		}

		return orderedList;
	}

	
	/**
	 * Inserts the Expression expression in to the given list at its position
	 * according to the ordering of Essence' expressions.
	 * 
	 * @param expression
	 * @param list
	 * @return
	 */
	private ArrayList<Expression> insertIntoOrderedList(Expression expression, 
				                                        ArrayList<Expression> list) {
		// first order the expression
		expression.orderExpression();
		
		// if this is the first element (list is empty) just insert it
		if(list.size() == 0) {
			list.add(expression);
			return list;
		}
		
		Expression otherElement = null;
		
		for(int i=0; i<list.size(); i++) {
			otherElement = list.get(i);
		
			//print_debug("element '"+expression+"' to put into ordered list: "+list);
			
			// if the element is smaller the element we are comparing to
			if(otherElement.getType() > expression.getType()) {
				list.add(i,expression);
				return list;
			}
			
			else if(otherElement.getType() == expression.getType()) {
				char expressionRelation = expression.isSmallerThanSameType(otherElement);
				if(expressionRelation == EQUAL || expressionRelation == SMALLER) {
				    //print_debug(" element '"+expression+"' to put into ordered list: "+list+" at position:"+i);
					list.add(i,expression);
					return list;
				}		
			}
			
			// if the element is the most expensive one, add it to the end of the list 
			if(i == list.size()-1) {
				list.add(expression);
				return list;
			}
		}
		
		
		return list;
	}
	
	protected void print_debug(String message) {
		
	if(DEBUG)
		System.out.println("[ DEBUG n-ary arithm expression ] "+message);
	}

	public boolean isNested() {
		return isNested;
	}
	
	public void setIsNotNested() {
		this.isNested = false;
	}
	
	public boolean isGonnaBeReified() {
		return this.willBeReified;
	}
	
	public void willBeReified(boolean reified) {
		this.willBeReified = reified;
	}
	
	
}
