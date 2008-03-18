package translator.xcsp2ep.parser.components;

public class PElement extends PGlobalConstraint {
	private PVariable index;

	private Object[] table;

	private Object value;

	

	public PElement(String name, PVariable[] scope, PVariable indexVariable, Object[] table, Object value) {
		super(name, scope);
		this.index = indexVariable;
		this.table = table;
		this.value = value;
	}

	
	public PVariable getIndexVariable() {
		return this.index;
	}
	
	public Object[] getTable() {
		return this.table;
	}
	
	public Object getValue() {
		return this.value;
	}

	public String toString() {
		String s = super.toString() + " : element\n\t";
		s += "index=" + index + "  table=";
		//s += "index=" + scope[0].getName() + "  table="; // commented by andrea
		for (int i = 0; i < table.length; i++)
			s += (table[i] instanceof PVariable ? ((PVariable)table[i]).getName() : table[i]) + " ";
		s += "  value=" + (value instanceof PVariable ? ((PVariable)value).getName() :  value);
		return s;
	}
}
