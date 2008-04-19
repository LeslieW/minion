package translator.expression;

public class ConstantArrayDomain  implements ConstantDomain,MatrixDomain {

	ConstantDomain baseDomain;
	ConstantDomain[] indexDomains;
	
	
	// ============ CONSTRUCTOR ===================
	
	public ConstantArrayDomain(ConstantDomain[] indexDomains,
			              ConstantDomain baseDomain) {
		this.indexDomains = indexDomains;
		this.baseDomain = baseDomain;
	}
	
	
	// ============ INHERITED METHODS ================================
	
	public int[] getFullDomain() {
		return this.baseDomain.getFullDomain();
	}

	public int[] getRange() {
		return this.baseDomain.getRange();
	}

	public Domain copy() {
		ConstantDomain[] copiedIndexDomain = new ConstantDomain[this.indexDomains.length];
		for(int i=0; i<this.indexDomains.length; i++)
			copiedIndexDomain[i] = (ConstantDomain) this.indexDomains[i].copy();
	
		return new ConstantArrayDomain(copiedIndexDomain,
				                       (ConstantDomain) this.baseDomain.copy());
	}

	public Domain evaluate() {
		return this;
	}

	public int getType() {
		return Domain.CONSTANT_ARRAY;
	}

	public Domain insertValueForVariable(int value, String variableName) {
		return this;
	}

	public Domain insertValueForVariable(boolean value, String variableName) {
		return this;
	}
	
	public boolean isConstantDomain() {
		return true;
	}

	public Domain replaceVariableWithDomain(String variableName, Domain newDomain) {
		return this;
	}
	
	public char isSmallerThanSameType(BasicDomain d) {
		int f;
		return Expression.EQUAL;
	}
	
	// ============= OTHER MEHTODS ============================
	
	public ConstantDomain getBaseDomain() {
		return this.baseDomain;
	}
	
	public ConstantDomain[] getIndexDomains() {
		return this.indexDomains;
	}
	
	public String toString() {
		
		String s = "matrix indexed by [";
		
		for(int i=0; i<this.indexDomains.length; i++) {
			if(i >0) s = s.concat(",");
			s = s.concat(indexDomains[i].toString());
		}
			
		s = s+"] of "+this.baseDomain.toString();
		
		return s;
	}
	
	public int getOffsetFromZeroAt(int index) 
		throws Exception {
		
		if(index >= this.indexDomains.length || index < 0)
			throw new Exception("Index '"+index+"' out of bounds:"+this);

		return this.indexDomains[index].getRange()[0];
	}
}
