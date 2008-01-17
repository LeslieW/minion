package minionModel;

/** 
 * Notes on indices:
 * Single boolean variables have the same relative index as absolute index (since boolean
 * variables are the first ones on Minion models). But boolean variables that are collected 
 * in vectors or matrices have a different relative and absolute index.
 * 
 * @author andrea, ian
 *
 */

public final class MinionBoolVariable extends MinionIdentifier {
  
  //private boolean polarity ;
  

  /** 
    * Creates a boolean Minion variable. 
 * @param p the boolean polarity of the Minion variable 
 * @param name c
   */
  public MinionBoolVariable (int p, String name) {
    this.polarity = p ;
    upperBound = 1;
    lowerBound = 0;
    originalName = name;
  }

  /** 
   * Sets the Minion model indicies. This method should only be called in the 
   * MinionModel.
   * @param relIndex the relative (integer) index value of the boolean Minion variable
   * @param absIndex the absolute (integer) index value of the boolean Minion variable
  */
 protected void setIndices (int relIndex, int absIndex) {
   absoluteIndex = absIndex;
   relativeIndex = relIndex;
 }

 /**
  * The polarity is only relevant for cases where negation has been applied.
  * We can only consider this case DIRECTLY in the MinionConstraints. So if a
  * variable is negated, then its polarity is set to false. The variable is checked
  * for a negative polarity in the Minion Constraint, and reset to true, if it was 
  * false.
  * 
  * @param p
  */
 public void setPolarity(int p) {
	 this.polarity = p;
 }
 
 public int getPolarity() {
	 return this.polarity;
 }
 
  /**
   * @return the String representing the boolean Minion variable. Usually 
   * "x" followed by its index, for instance "x4" (with index 4). 
   * */
  public String toString() {
    return "x" + absoluteIndex ;
  }
  
}
