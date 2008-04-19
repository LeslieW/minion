package translator.expression;

import java.util.ArrayList;

/**
 *  Represents all domains that are composed of multiple ranges. 
 *  For instance, the domain ((1..a),(15,17,b),(20..c)) is composed 
 *  by 3 ranges: 2 bounded ranges, (1..a) and (20..c) and 1 sparse
 * domain (15,17,b). The ranges in the domain have to be ordered,
 * so identifiers in the domain have to follow the ordering. In the 
 * example above, this means that 1<=a<=15, 17<=b<=20 and c>=20.   
 * We might not be able to check for a correct ordering since the 
 * parameters might stay unknown for the whole translation process.
 * 
 * @author andrea
 *
 */

public class MultipleExpressionRange implements ExpressionDomain {

	ArrayList<ExpressionRange> rangeList;
	
	// =========== CONSTRUCTOR ==============================
	
	public MultipleExpressionRange(ArrayList<ExpressionRange> rangeList) {
		this.rangeList = rangeList;
	}
	
	//========= INHERITED METHODS ===========================
	
	public Domain copy() {
		ArrayList<ExpressionRange> copiedRangeElements = new ArrayList<ExpressionRange> ();
		
		for(int i=0; i<this.rangeList.size(); i++) {
			copiedRangeElements.add((ExpressionRange) this.rangeList.get(i).copy());
		}
		return new MultipleExpressionRange(copiedRangeElements);
	}

	public Domain evaluate() {
		boolean allRangesAreConstant = true;
		
		for(int i=0; i<this.rangeList.size(); i++) {
			Domain range = (Domain) this.rangeList.get(i).evaluate();
			if(!(range.getType() == INT_BOUNDS) || !(range.getType() == INT_SPARSE))
				allRangesAreConstant = false;
		}
		
		if(allRangesAreConstant) {
			ArrayList<IntRange> intRangeList = new ArrayList<IntRange>();
			for(int i=0; i<this.rangeList.size(); i++) {
				intRangeList.add((IntRange) this.rangeList.get(i));
			}
			return new MultipleIntRange(intRangeList);
		}
		
			
		return this;
	}

	public int getType() {
		return EXPR_MIXED;
	}

	
	public String toString() 			{
		String s = "(";
		
		for(int i=0; i<this.rangeList.size(); i++)
			s = s.concat(", "+rangeList.get(i).toString());
		
		return s+")";
	}
	
	public boolean isConstantDomain() {
		return false;
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
		boolean allExpressionsAreInteger = true;
		
		for(int i=0; i < this.rangeList.size(); i++) {
			ExpressionRange range = (ExpressionRange) rangeList.remove(i).insertValueForVariable(value, variableName);
			this.rangeList.add(i, range);
			
			if(!range.isConstantDomain())
				allExpressionsAreInteger = false;
		}
		
		if(allExpressionsAreInteger) {
			ArrayList<IntRange> intRangeList = new ArrayList<IntRange>();
			for(int i=this.rangeList.size()-1; i>=0; i++) 
				intRangeList.add(0, (IntRange) this.rangeList.remove(i));
			return new MultipleIntRange(intRangeList);
		}
		
		return this;
	}
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		boolean allExpressionsAreInteger = true;
		
		for(int i=0; i < this.rangeList.size(); i++) {
			ExpressionRange range = (ExpressionRange) rangeList.remove(i).insertValueForVariable(value, variableName);
			this.rangeList.add(i, range);
			
			if(!range.isConstantDomain())
				allExpressionsAreInteger = false;
		}
		
		if(allExpressionsAreInteger) {
			ArrayList<IntRange> intRangeList = new ArrayList<IntRange>();
			for(int i=this.rangeList.size()-1; i>=0; i++) 
				intRangeList.add(0, (IntRange) this.rangeList.remove(i));
			return new MultipleIntRange(intRangeList);
		}
		
		return this;
	}
	
	
	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		
		boolean allRangesAreConstant = true;
		
		for(int i=this.rangeList.size()-1; i>= 0; i--) {
			ExpressionRange range = rangeList.remove(i);
			range = (ExpressionRange) range.replaceVariableWithDomain(variableName, newDomain);
			range = (ExpressionRange) range.evaluate();
			allRangesAreConstant = allRangesAreConstant && (range instanceof ConstantDomain);
			rangeList.add(i, range);
		}
		
		if(allRangesAreConstant) {
			ArrayList<IntRange> constantRange = new ArrayList<IntRange> ();
			for(int i=0; i< rangeList.size(); i++) {
				constantRange.add(i,(IntRange) this.rangeList.get(i));
			}
			return new MultipleIntRange(constantRange);
		}
		
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		
		MultipleExpressionRange otherRange = (MultipleExpressionRange) d;
		
		if(this.rangeList.size() == otherRange.rangeList.size()) {
			
			for(int i=0; i<this.rangeList.size(); i++) {
				
				if(this.rangeList.get(i).getType() == otherRange.rangeList.get(i).getType()) {
					
					char  difference = this.rangeList.get(i).isSmallerThanSameType(otherRange.rangeList.get(i));
					if(difference != Expression.EQUAL) return difference;
					
				}
				else return (this.rangeList.get(i).getType() <otherRange.rangeList.get(i).getType()) ?
						Expression.SMALLER : Expression.BIGGER;
			}
			
			return Expression.EQUAL;
		}
		else return (this.rangeList.size() < otherRange.rangeList.size()) ?
				Expression.SMALLER : Expression.BIGGER;
		
		
	}
	
}
