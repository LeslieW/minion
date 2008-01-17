package conjureEssenceSpecification;

/**
 * Essence' grammar: (terminals in capital letters)
 * identifier BE NEW TYPE domain
 *
 */

public class NewTypeConstant implements EssenceGlobals{
	
	String identifier;
	Type type;
	
	public String getName(){
		return identifier;
	}

	public Type getType(){
		return type;
	}
	
	public NewTypeConstant copy() {
		return new NewTypeConstant(new String(identifier), type.copy());
	}
	
	public NewTypeConstant(String i,Type t){
		identifier = i;
		type = t;
	}

	public String toString(){
		return identifier + "be new type " + type.toString();
		
	}
}
