package translator.expression;

public interface ConstantArray extends Expression {

	public String getArrayName();
	
	public int getDimension();
	
	public Domain getArrayDomain();
	
	public void setArrayDomain(ArrayDomain domain);
	
	public int[] getIndexOffsets();
	
}
