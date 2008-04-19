package translator.expression;

import java.util.ArrayList;

/**
 * Represents all domains that are composed of multiple integer 
 * ranges. FOr instance, the domain ((1..10),(15,17,19),(20..29)) is composed 
 * by 3 integer ranges: 2 bounded ranges, (1..10) and (20..29) and 1 sparse
 * domain (15,17,19). 
 * 
 * @author andrea
 *
 */

public class MultipleIntRange implements ConstantDomain {

	/**
	 * the list of all the ranges that the domain is composed of.
	 */
	private ArrayList<IntRange> rangeList;
	
	
	//============== CONSTRUCTOR =============================
	
	public MultipleIntRange(ArrayList<IntRange> rangeList) {
		this.rangeList = rangeList;
	}
	
	
	// =========== INHERITED METHODS ==========================
	
	public int[] getFullDomain() {
		
		ArrayList<Integer> fullDomain = new ArrayList<Integer>();
		
		// collect all the full domains of the sub-ranges
		for(int i=0; i<this.rangeList.size(); i++) {
			int[] fullRangeDomain = rangeList.get(i).getFullDomain();
				
			for(int j=0; j<fullRangeDomain.length; j++) 
				fullDomain.add(fullRangeDomain[j]);
		}
		
		// convert the list to an array (I know, it's messy...)
		int[] fullDomainArray = new int[fullDomain.size()];
		for(int i=fullDomain.size()-1; i>=0;i--) {
			fullDomainArray[i] = fullDomain.remove(i);
		}
		return fullDomainArray;
	}

	public int[] getRange() {
		ArrayList<Integer> ranges = new ArrayList<Integer>();
		
		// collect all the ranges of the sub-ranges
		for(int i=0; i<this.rangeList.size(); i++) {
			int[] range = rangeList.get(i).getRange();
				
			for(int j=0; j<range.length; j++) 
				ranges.add(range[j]);
		}
		
		// convert the list to an array (I know, it's messy...)
		int[] rangesArray = new int[ranges.size()];
		for(int i=ranges.size()-1; i>=0;i--) {
			rangesArray[i] = ranges.remove(i);
		}
		return rangesArray;
	}

	public Domain copy() {
		ArrayList<IntRange> copiedRangeElements = new ArrayList<IntRange> ();
		
		for(int i=0; i<this.rangeList.size(); i++) {
			copiedRangeElements.add((IntRange) this.rangeList.get(i).copy());
		}
		return new MultipleIntRange(copiedRangeElements);
	}

	public Domain evaluate() {
		for(int i=0; i<this.rangeList.size(); i++) {
			this.rangeList.add(i,(IntRange) this.rangeList.remove(i).evaluate());
		}
		return this;
	}

	public int getType() {
		return INT_MIXED;
	}

	public String toString() {
		String s = "(";
		
		for(int i=0; i<this.rangeList.size(); i++)
			s = s.concat(", "+rangeList.get(i).toString());
		
		return s+")";
	}
	
	public boolean isConstantDomain() {
		return true;
	}
	
	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}
	
	public Domain insertValueForVariable(boolean value, String variableName) {
		return this;
	}
	
	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		
		MultipleIntRange otherRange = (MultipleIntRange) d;
		
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
