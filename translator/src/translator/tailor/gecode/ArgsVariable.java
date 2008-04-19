package translator.tailor.gecode;

public interface ArgsVariable extends GecodeVariable, ArgsAtom {

	public GecodeAtom[] getVariables();
	
	public GecodeAtom getVariableAt(int index) throws GecodeException;
	
}
