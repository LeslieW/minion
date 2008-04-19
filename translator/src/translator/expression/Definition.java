package translator.expression;

/**
 * Represents definitions of constants and parameters  
 * 
 * @author andrea
 *
 */

public interface Definition extends GeneralDeclaration {

	/** 
	 * get the name of the defined variable/constant
	 * @return
	 */
	public String getName();
	
	public String toString();
	
}
