package translator.conjureEssenceSpecification;

public class BindUnit implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : identifer list
	 * restriction_mode 2 : tuple list
	 * 
	 * Essence' grammar: (terminals in capital letters)
	 *   identifierList 
	 * | tupleList
	 * 
	 */
	int restriction_mode;
	String[] identifierlist;
	TupleList tuplelist;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public String[] getIdentifierList(){
		return identifierlist;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	public void setIdentifierList(String[] il){
		identifierlist=il;
	}
	
	public BindUnit(String[] il){
		restriction_mode = BIND_IDENTIFIER_LIST;
		identifierlist = il;
	}
	public BindUnit(TupleList tl){
		restriction_mode = BIND_TUPLE_LIST;
		tuplelist = tl;
	}

	public String toString(){
		
		switch(restriction_mode){
		case BIND_IDENTIFIER_LIST : return identifierlist.toString();
		case BIND_TUPLE_LIST : return tuplelist.toString();
		}
		return "";
	}
	
}
