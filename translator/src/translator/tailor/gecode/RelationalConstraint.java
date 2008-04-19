package translator.tailor.gecode;

public class RelationalConstraint implements GecodeConstraint {

	char consistencyLevel = GecodeConstraint.ICL_DEF;
	char[] supportedConsistencyLevels = new char[] {};
	boolean isReifiable = false;
	char propagationKind = GecodeConstraint.PK_DEF;
	
	
	//========== INHERITED METHODS =======================================
	
	public char getConsistencyLevel() {
		return this.consistencyLevel;
	}

	public char[] getSupportedConsistencyLevels() {
		return this.supportedConsistencyLevels;
	}

	public void setConsistencyLevel(char newConsistency) {
		
		for(int i=0; i< this.supportedConsistencyLevels.length; i++)
			if(newConsistency == this.supportedConsistencyLevels[i]) {
				this.consistencyLevel = newConsistency;
				return;
			}
		
	}
	
	public void setPropagationKind(char propagationKind) {
		if(propagationKind == GecodeConstraint.PK_DEF ||
			propagationKind == GecodeConstraint.PK_SPEED ||
			propagationKind == GecodeConstraint.PK_MEM)
		this.propagationKind = propagationKind;
	}
	
	public boolean isReifiable() {
		return this.isReifiable;
	}
	
	

	public String toCCString() {
		return "";
	}
	
	//=========== ADDITIONAL METHODS ======================================

	/**
	 * Return the String that represents the  
	 * operator OP in Gecode.
	 * 
	 */
	protected String operatorToString(char OP) {
		
		if(OP == GecodeConstraint.IRT_EQ)
			return "IRT_EQ";
		else if(OP == GecodeConstraint.IRT_GQ)
			return "IRT_GQ";
		else if(OP == GecodeConstraint.IRT_GR)
			return "IRT_GR";
		else if(OP == GecodeConstraint.IRT_LE)
			return "IRT_LE";
		else if(OP == GecodeConstraint.IRT_LQ)
			return "IRT_LQ";
		else if(OP == GecodeConstraint.IRT_NQ)
			return "IRT_NQ";
		else if(OP == GecodeConstraint.BOT_AND)
			return "BOT_AND";
		else if(OP == GecodeConstraint.BOT_OR)
			return "BOT_OR";
		else if(OP == GecodeConstraint.BOT_IMP)
			return "BOT_IMP";
		else if(OP == GecodeConstraint.BOT_EQV)
			return "BOT_EQV";
		else if(OP == GecodeConstraint.BOT_XOR)
			return "BOT_XOR";
		
		
		else return "unknown OP";
	}
	
	/**
	 * Returns the String that represents the consistency
	 * level CL in Gecode
	 * 
	 * @param CL
	 * @return
	 */
	protected String consistencyToString(char CL) {
		
		if(CL == GecodeConstraint.ICL_VAL)
			return "ICL_VAL";
		else if(CL == GecodeConstraint.ICL_DOM)
			return "ICL_DOM";
		else if(CL == GecodeConstraint.ICL_BND)
			return "ICL_BND";
		
		else return "ICL_DEF";
	}
	
	/**
	 * Returns the String that represents the propagation
	 * kind CL in Gecode
	 * 
	 * @param PR
	 * @return
	 */
	protected String propagationToString(char PR) {
		if(PR == GecodeConstraint.PK_DEF)
			return "PK_DEF";
		else if(PR == GecodeConstraint.PK_SPEED)
			return "PK_SPEED";
		else if(PR == GecodeConstraint.PK_MEM)
			return "PK_MEM";
		
		else return "PK_DEF";
	}
}
