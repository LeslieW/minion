package translator.xcsp2ep.parser.components;


public class PCumulative extends PGlobalConstraint {
	private int nbTasks;

	private Absent absent;

	private int limit;

	enum Absent {
		NO_ONE, ORIGIN, DURATION, END;

		public static Absent getAbsentFor(int position) {
			if (position == -1)
				return NO_ONE;
			if (position == 0)
				return ORIGIN;
			if (position == 1)
				return DURATION;
			return END;
		}

		public static String getStringFor(Absent absent) {
			if (absent == NO_ONE)
				return "no";
			if (absent == ORIGIN)
				return "origin";
			if (absent == DURATION)
				return "duration";
			return "end";
		}
	}

	public PCumulative(String name, PVariable[] scope, Absent absent, int limit) {
		super(name, scope);
		this.nbTasks = scope.length / (absent == Absent.NO_ONE ? 4 : 3);
		this.absent = absent;
		this.limit = limit;
	}

	public PCumulative(String name, PVariable[] scope, int absentId, int limit) {
		this(name, scope, Absent.getAbsentFor(absentId), limit);
	}

	public String toString() {
		String s = super.toString() + " : cumulative\n\t";
		s += "nbTasks=" + nbTasks + "  absent=" + Absent.getStringFor(absent) + " limit=" + limit;
		return s;
	}
}
