package translator.minionModel;

/** 
 * IMPORTANT NOTE: discrete sparse domain variables are not yet supported by (Do)Minion
 * 
 * Indices:
 * The absolute and relative index is computed by the Minion model.
 * They have to be set independently.
 * @author andrea, ian
 */

public final class MinionDiscreteSVariable extends MinionIdentifier {

	private int[] sparseDiscreteDomain;
	
	/**
	 * @param sparseDiscrDomain is the int[] array of the values in the sparse domain
	 * of the variable
	 * @param name the String containing the variables' name in the original problem specification 
	 * */
  public MinionDiscreteSVariable (int[] sparseDiscrDomain, String name) {
    sparseDiscreteDomain = new int[sparseDiscrDomain.length] ;
    
    for(int i=0; i<sparseDiscreteDomain.length; i++)
    	sparseDiscreteDomain[i] = sparseDiscrDomain[i];
   
    originalName = name;
    polarity = -1;
  }
  
  /**
   * @return the int[] array containing the sparse discrete domain of the variable 
   */
  public int[] getSparseDiscreteDomain() {
	  return sparseDiscreteDomain;
  }
  
  /**
   * @return the String representing the boolean Minion variable. Usually 
   * "x" followed by its index, for instance "x4" (with index 4). 
   * */
  public String toString() {
    return "x" + absoluteIndex ;
  }
}
