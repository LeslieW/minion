package translator.conjureEssenceSpecification;

public interface Index {

	public final int INT_INDEX = 20;
	public final int VAR_INDEX = 21;
	// (..)
	public final int FULL_RANGE_INDEX = 22;
	
	// (e..)
	public final int LOWER_RANGE_INT_INDEX = 23;
	// (..e)
	public final int UPPER_RANGE_INT_INDEX = 24;
	// (e1..e2)
	public final int LOWER_UPPER_RANGE_INT_INDEX = 25;

	// (e..)
	public final int LOWER_RANGE_EXPR_INDEX = 23;
	// (..e)
	public final int UPPER_RANGE_EXPR_INDEX = 24;
	// (e1..e2)
	public final int LOWER_UPPER_RANGE_EXPR_INDEX = 25;
	
	
	public Index copy();
	
	
	public int getType();
	
	
	public String toString();
	
}
