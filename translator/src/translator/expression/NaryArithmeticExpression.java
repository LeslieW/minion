package translator.expression;

import java.util.ArrayList;


public abstract class NaryArithmeticExpression implements ArithmeticExpression {

	//public ArrayList<Expression> getArguments();
	
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
		
		for(int i=0; i<list.size(); i++)
			orderedList = insertIntoOrderedList(list.get(i), orderedList);
		
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
		// if this is the first element (list is empty) just insert it
		if(list.size() == 0) {
			list.add(expression);
			return list;
		}
		
		Expression otherElement = null;
		
		for(int i=0; i<list.size(); i++) {
			otherElement = list.get(i);
		
			// if the element is smaller the element we are comparing to
			if(otherElement.getType() > expression.getType())
				list.add(i,expression);
			
			// if the element is the most expensive one, add it to the end of the list 
			else if(i == list.size())
				list.add(expression);
		}
		
		return list;
	}
	
}
