package conjureEssenceSpecification;

public class ClassFunction implements EssenceGlobals {

	/**
	 * restriction_mode 1 : injective<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : surjective<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : bijective<br/>&nbsp&nbsp&nbsp
	 */
	int restriction_mode;
	
	public int getRestrictionMode(){
		return restriction_mode;
	}
	public void setRestrictionMode(int rm){
		restriction_mode = rm;
	}
	
	/**
	 * @param i restriction_mode<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 1 : injective<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 2 : surjective<br/>&nbsp&nbsp&nbsp
	 * restriction_mode 3 : bijective<br/>&nbsp&nbsp&nbsp
	 */
	public ClassFunction(int i){
		restriction_mode = i;
	}
	
	public String toString(){
		
		switch(restriction_mode){
		case INJECTIVE : return "injective ";
		case SURJECTIVE : return "surjective ";
		case BIJECTIVE : return "bijective ";
		}
		
		return "";
	}
	
}
