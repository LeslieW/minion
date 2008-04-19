package translator.expression;

import java.util.ArrayList;

public class ConstantRestriction implements Definition {

	private ArrayList<Expression> expressions;
	
	
	public ConstantRestriction(ArrayList<Expression> restriction) {
		this.expressions = restriction;
	}
	
	// ============ INHERITED METHODS ==============
	
	public String getName() {
		return expressions.toString();
	}

	public String toString() {
		StringBuffer s = new StringBuffer("where ");
		
		if(expressions.size() > 0) {
			for(int i=0; i<this.expressions.size()-1; i++)
				s.append(expressions.get(i)+",");
			s.append(expressions.get(expressions.size()-1));
		}
		return s.toString();
	}
	
	// ========== ADDITIONAL METHODS ================
	
	public ArrayList<Expression> getExpression() {
		return this.expressions;
	}
	
}
