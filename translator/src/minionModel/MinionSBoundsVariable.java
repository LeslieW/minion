package minionModel;

/** 
* Indices:
* The absolute and relative index is computed by the Minion model.
* They have to be set independently.
* @author andrea, ian
*/

public final class MinionSBoundsVariable extends MinionIdentifier {
	  
	  private int[] sparseDomain;
	 
  /**
   * @param sDomain is the int[] array of the values in the sparse domain
   * of the variable
 * @param name TODO
   * */
  public MinionSBoundsVariable (int[] sDomain, String name) {	  
    sparseDomain = new int[sDomain.length];
    
    for(int i=1; i<sDomain.length; i++)
    	sparseDomain[i] = sDomain[i];
    
    originalName = name;
    polarity = -1;
    
  }
  
  /**
   * @return the int[] array containing the sparse domain of the variable 
   */

  public int[] getSparseDomain() {
	  return sparseDomain;
  }
  
  /**
   * @return the String representing the boolean Minion variable. Usually 
   * "x" followed by its index, for instance "x4" (with index 4). 
   * */
  public String toString() {
    return "x" + absoluteIndex ;
  }
}
