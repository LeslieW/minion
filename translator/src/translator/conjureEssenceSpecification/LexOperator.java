package translator.conjureEssenceSpecification;

public class LexOperator {

    //public final int LEX_LESS = 128;
    //public final int LEX_LEQ = 129;
    //public final int LEX_GREATER = 130;
    //public final int LEX_GEQ = 131;
	
	int restrictionMode;
	
	public LexOperator(int r) {
		
		this.restrictionMode = r;
		
	}
	
	public int getRestrictionMode() {
		return this.restrictionMode;
	}
	
	public LexOperator copy() {
		return new LexOperator(restrictionMode);
	}
	
	public String toString() {
		switch(restrictionMode) {
			
		case EssenceGlobals.LEX_GEQ: return ">=lex";
		case EssenceGlobals.LEX_LEQ: return "<=lex";
		case EssenceGlobals.LEX_GREATER: return ">lex";
		case EssenceGlobals.LEX_LESS: return "<lex";
		default :
			return "UNKNOWN lex";
		}
	}
	
}
