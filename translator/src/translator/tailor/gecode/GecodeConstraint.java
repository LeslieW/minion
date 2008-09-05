package translator.tailor.gecode;

public interface GecodeConstraint {

	public final String BUFFERARRAY_NAME = "_buffer";
	/** suffix that parts of arrays have */
	public final String ARRAY_PARTS_SUFFIX = "_part";
	/** the prefix that flattened arrays have */
	public final String FLATTENED_ARRAY_PREFIX = "_";
	
	public final int RANDOM_MAXIMUM = 11111;
	/** it should be less than 256 which is standard in C++ */
	public final int MAX_VARIABLE_LENGTH = 24; 
	public final int WHOLE_RANGE_REFERENCED = -100;
	
	/** Consistency levels for each constraint */
	/** value consistency level (naive) */
	public final char ICL_VAL = 0;
	/**  bounds consistency level */
	public final char ICL_BND = 1;
	/**  domain consistency level */
	public final char ICL_DOM = 2;
	/** default consistency level */
	public final char ICL_DEF = 3;
	
	
	/** Propagation kind:
	 * Signals that a particular kind is used in propagation for the implementation 
	 * of a particular constraint. */
	/** default */
	public final char PK_DEF = 30;
	/** Prefer speed over memory consumption */
	public final char PK_SPEED = 31;
	/** Prefer little memory over speed.*/
	public final char PK_MEM = 32;
	
	
	/** Relational Integer Operators */
	public final char IRT_EQ = 10; // =
	public final char IRT_NQ = 11; // !=
	public final char IRT_LQ = 12; // <=
	public final char IRT_LE = 13; // <
	public final char IRT_GQ = 14; // >=
	public final char IRT_GR = 15; // >
	
	
	/** Relational Boolean Operators */
	public final char BOT_AND = 20;  // /\
	public final char BOT_OR = 21;   // \/
	public final char BOT_IMP = 22;  // =>
	public final char BOT_EQV = 23;  // <=>
	public final char BOT_XOR = 24;  // xor
	

	
	/**
	 * Returns the default consistency level that the 
	 * constraint is propagated on
	 * 
	 * @return
	 */
	public char getConsistencyLevel();
	
	/**
	 * Returns the supported consistency levels the
	 * constraint can be propagated on.
	 * 
	 * @return
	 */
	public char[] getSupportedConsistencyLevels();
	
	
	/** 
	 * Set a consistency level for the constraint. If the consistency
	 * level is not supported, the default level will be used instead.
	 * 
	 * @param consistencyLevel
	 */
	public void setConsistencyLevel(char consistencyLevel);
	
	
	/**
	 * Set a propagation kinf for the constraint. If the propagation
	 * kind is not supported, the default kind will be used instead.
	 * 
	 * @param propagationKind 
	 */
	public void setPropagationKind(char propagationKind);
	
	/**
	 * Returns true, if Gecode provides a reifiable version of the constraint
	 * @return
	 */
	public boolean isReifiable();
	
	/**
	 * Returns the constraint in C++ code.
	 * 
	 * @return
	 */
	public String toCCString();
	
	/**
	 * Returns the general representation of the Gecode constraint.
	 * @return
	 */
	public String toString();

}
