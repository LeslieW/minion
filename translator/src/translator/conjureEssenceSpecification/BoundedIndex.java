package translator.conjureEssenceSpecification;

/**
 * Any Index for an array element, such as (e1..e2),
 * (..e2) or (e1..)
 * 
 * @author andrea
 *
 */

public class BoundedIndex implements Index {

	Expression lowerIndex;
	Expression upperIndex;
	int type;
	
	// ================== CONSTRUCTORS ======================
	
	public BoundedIndex (Expression lowerIndex,
			           Expression upperIndex) {
		
		this.lowerIndex = lowerIndex;
		this.upperIndex = upperIndex;
		this.type = EssenceGlobals.UPPER_LOWER_BOUNDED_INDEX;
	}
	
	
	public BoundedIndex(Expression index,
					boolean isLowerIndex) {
		if(isLowerIndex)
			this.lowerIndex = index;
		
		else this.upperIndex = index;
		this.type = (isLowerIndex) ? 
				EssenceGlobals.LOWER_BOUNDED_INDEX :
					EssenceGlobals.UPPER_BOUNDED_INDEX;
		
	}
	
	public BoundedIndex() {
		this.type = EssenceGlobals.FULL_BOUNDED_INDEX;
	}
	
	// ============== INHERITED METHODS =====================
	
	public Index copy() {
		if(this.type == EssenceGlobals.LOWER_BOUNDED_INDEX)
			return new BoundedIndex(this.lowerIndex.copy(), true);
		
		else if (this.type == EssenceGlobals.UPPER_BOUNDED_INDEX)
			return new BoundedIndex(this.upperIndex.copy(), false);
		
		else if(this.type == EssenceGlobals.FULL_BOUNDED_INDEX)
			return new BoundedIndex();
		
		else return new BoundedIndex(this.lowerIndex.copy(),
								   this.upperIndex.copy());
	}


	public int getType() {
		return this.type;
	}
	
	
	public String toString() {
		
		String lindex = "";
		String uindex = "";
		if(this.lowerIndex != null)
			lindex= lowerIndex.toString();
		if(this.upperIndex != null)
			uindex = this.upperIndex.toString();
		
		return lindex+".."+uindex;
	}

	// ============== ADDITIONAL MEHTODS ==================================
	
	public Expression getLowerExpressionIndex() {
		return this.lowerIndex;
	}
	
	public Expression getUpperExpressionIndex() {
		return this.upperIndex;
	}
	
}
