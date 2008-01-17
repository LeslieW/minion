package conjureEssenceSpecification;

public class KindFunction implements EssenceGlobals{
	
	/**
	 * restriction_mode 1 : partial<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : total<br/>&nbsp&nbsp&nbsp
	 */
	int restriction_mode;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	
	/**
	 * 
	 * @param i restriction mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : partial<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : total<br/>&nbsp&nbsp&nbsp
	 */
	public KindFunction(int i){
		restriction_mode = i;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case KIND_FUNCTION_PARTIAL : return "partial ";
		case KIND_FUNCTION_TOTAL : return "total ";
		}		
		return "";
	}

}
