package translator.expression;

public class ConstantArrayDomain implements ConstantDomain {

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

	public boolean isConstantDomain() {
		return true;
	}

	
	// ============= OTHER MEHTODS ============================
	
	public ConstantDomain getBaseDomain() {
		return this.baseDomain;
	}
	
	public ConstantDomain[] getIndexDomains() {
		return this.indexDomains;
	}
	
}
