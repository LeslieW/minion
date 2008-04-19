package translator.expression;

/**
 * This domain stands for ArrayDomains and ConstantArrayDomains,
 * hence any domain that represents and array/matrix
 * 
 * @author andrea
 *
 */

public interface MatrixDomain extends Domain {

	public Domain[] getIndexDomains();
	
	public Domain getBaseDomain();
	
}
