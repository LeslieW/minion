package translator.minionModel;

/** 
* Indices:
* The absolute and relative index is computed by the Minion model.
* They have to be set independently.
* @author andrea, ian
*/

public final class MinionDiscreteVariable extends MinionIdentifier {
 
  private int lowerBound;
  private int upperBound;
  

  /** 
   * @param lb the integer value of the lower domain-bound of the Minion variable
 * @param ub the integer value of the upper domain-bound of the Minion variable
 * @param name the String containing the variables' name in the original problem specification 
  */
  public MinionDiscreteVariable (int lb, int ub, String name) {
    lowerBound = lb ;
    upperBound = ub;
    originalName = name;
    polarity = -1;
  }

     /**
      * 
      * @return the integer value of the upper bound of the Minion variable
      */
     public int getUpperBound() {
    	 return upperBound;
     }
     
     /**
      * 
      * @return the integer value of the lower bound of the Minion variable
      */
     public int getLowerBound() {
    	 return lowerBound;
     }
     
    
  
     /**
      * @return the String representing the boolean Minion variable. Usually 
      * "x" followed by its index, for instance "x4" (with index 4). 
      * */
  public String toString() {
    return "x" + absoluteIndex ;
  }
}
