package minionModel;

/**
 * Each variable has a relative index (index concerning the domain type of the 
 * variables only) and an absolute index (index concerning all variables)
 * and can only be identified by that. 
 * Notes (ian):  
 * 1. Variable ids are all given relative to the start of a variable
 *    type to allow insertion of new variables.
 * @author andrea, ian
*/
public abstract class MinionIdentifier {

 int relativeIndex;
 int absoluteIndex;
 
 int upperBound;
 int lowerBound;
	
 int polarity;
 
 String originalName;	
 
 /** 
  * @return the relative index of the Minion bounds variable.
  *  */
   public int getRelativeIndex() {
	   return relativeIndex;
   }

 /** 
   * @return the relative index of the Minion bounds variable.
   *  */ 
   public int getAbsoluteIndex() {
 	   return absoluteIndex;
   }
 
 
   public int getLowerBound() {
	   return lowerBound;
   }
   
   public int getUpperBound() {
	   return upperBound;
   }
   
   public String getOriginalName() {
	   return originalName;
   }
   
   public void setUpperBound(int bound) {
	   upperBound = bound;
   }
   
   public void setLowerBound(int bound) {
	   lowerBound = bound;
   }
   
	/** 	
     * @param index the relative (integer) index value of the Minion variable
     *  
     *  */ 
	protected void setRelativeIndex(int index) {
		relativeIndex = index;
	}
	
	/** 
	 *    
	 * @param index the absolute (integer) index value of the Minion variable
	 *  */ 
	protected void setAbsoluteIndex(int index) {
		absoluteIndex = index;
	}
	
	
	public int getPolarity() {
		return polarity;
	}
	
	public void setPolarity(int p) {
		this.polarity = p;
	}
}
