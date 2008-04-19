package translator.expression;

import java.util.ArrayList;

public interface Declaration extends GeneralDeclaration {

	
	public ArrayList<String> getNames();
	
	public Domain getDomain();
	
	public String toString();
	
}
