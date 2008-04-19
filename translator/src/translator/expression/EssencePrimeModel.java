package translator.expression;

import java.util.ArrayList;

public class EssencePrimeModel {

	private ArrayList<GeneralDeclaration> declarations;
	private Objective objective;
	private ArrayList<Expression> expressions;
	
	
	public EssencePrimeModel(ArrayList<GeneralDeclaration> declarations,
							 Objective objective,
							 ArrayList<Expression> expressions) {
		this.declarations = declarations;
		this.objective = objective;
		this.expressions = expressions;
	}
	
	
	public EssencePrimeModel() {
		this.declarations = new ArrayList<GeneralDeclaration>();
		this.objective = new Objective();
		this.expressions = new ArrayList<Expression>();
		
	}
	
	// ==============================================
	
	public ArrayList<GeneralDeclaration> getDeclarations() {
		return this.declarations;
	}

	
	public Objective getObjective() {
		return this.objective;
	}
	
	public ArrayList<Expression> getConstraints() {
		return this.expressions;
	}
	
	public String toString() {
		
		StringBuffer s = new StringBuffer(translator.Translate.ESSENCE_PRIME_HEADER+"\n");
		
		for(int i=0; i<this.declarations.size(); i++) {
			s.append(declarations.get(i)+"\n");
		}
		
		
		s.append("\n");
		s.append(this.objective);
		s.append("\nsuch that\n");
		
		if(expressions.size() > 0) {
			for(int i = 0; i<this.expressions.size()-1; i++)
				s.append(expressions.get(i)+",\n");
			s.append(expressions.get(expressions.size()-1));
		}
		
		s.append("\n");	
		return s.toString();
	}
}
