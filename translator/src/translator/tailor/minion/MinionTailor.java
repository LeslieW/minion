package translator.tailor.minion;

import translator.expression.*;
import java.util.ArrayList;
import java.util.HashMap;
import translator.normaliser.NormalisedModel;
import translator.solver.Minion;

public class MinionTailor {

	
	
	//MinionModel minionModel;
	HashMap<String, int[]> offsetsFromZero;
	NormalisedModel normalisedModel;
	Minion solverSettings;
	
	
	// ======== CONSTRUCTOR ==================================
	
	public MinionTailor(NormalisedModel normalisedModel,
						Minion solverSettings) {
		
		this.offsetsFromZero = new HashMap<String,int[]>();
		this.normalisedModel = normalisedModel;
		this.solverSettings = solverSettings;
	}
	
	// ====== TRANSLATION TO MINION REPRESENTATION ===========
	
	
	public MinionModel tailorToMinion() 
		throws MinionException {
		
		// 1. tailor the variables and create a new empty model
		MinionModel minionModel = new MinionModel(new ArrayList<MinionConstraint>(),
				                           mapDecisionVariables(),
				                           this.normalisedModel.getDecisionVariablesNames(),
				                           this.normalisedModel.getAuxVariables(),
				                           this.solverSettings
				                           );
		
		// 2. tailor the constraints
		for(int i=this.normalisedModel.getConstraints().size()-1; i>=0; i--) 
			minionModel.addConstraint(toMinion(this.normalisedModel.getConstraints().remove(i)));
		
		
		return minionModel;
		
		
		
	}
	
	
	/**
	 * Tailors the normalised model, that was given in the constructor, to 
	 * a minion model.
	 * 
	 */
	public MinionModel tailorToMinion(NormalisedModel normalisedModel) 
		throws MinionException {
		
		this.normalisedModel = normalisedModel;
		
		// 1. tailor the variables and create a new empty model
		MinionModel minionModel = new MinionModel(new ArrayList<MinionConstraint>(),
				                           mapDecisionVariables(),
				                           this.normalisedModel.getDecisionVariablesNames(),
				                           this.normalisedModel.getAuxVariables(),
				                           this.solverSettings
				                           );
		
		// 2. tailor the constraints
		for(int i=this.normalisedModel.getConstraints().size()-1; i>=0; i--) 
			minionModel.addConstraint(toMinion(this.normalisedModel.getConstraints().remove(i)));
		
		
		return minionModel;
	}
	
	
	/**
	 * TODO!!!
	 * 
	 * @return
	 * @throws MinionException
	 */
	protected HashMap<String, ConstantDomain> mapDecisionVariables() 
		throws MinionException {
		
		HashMap<String,ConstantDomain> decisionVariables = new HashMap<String, ConstantDomain>();
		ArrayList<String> decisionVariablesNames = this.normalisedModel.getDecisionVariablesNames();
		
		for(int i=0; i<decisionVariablesNames.size(); i++) {
			Domain domain = this.normalisedModel.getDomainOfVariable(decisionVariablesNames.get(i));
			if(domain instanceof ConstantDomain)
				decisionVariables.put(decisionVariablesNames.get(i), (ConstantDomain) domain);
			else throw new MinionException("Found non-constant domain:"+domain+". Please define all parameters.");
			
		}
		
		return decisionVariables;
	}
	
	/**
	 * 
	 * @param constraint that should be flattened to Minion level.
	 * @return the String representation in Minion for the given 
	 * expression. 
	 */
	protected MinionConstraint toMinion(Expression constraint) 
		throws MinionException {
		
		if(constraint instanceof ArithmeticAtomExpression)
			return toMinion((ArithmeticAtomExpression) constraint);
		
		if(constraint instanceof CommutativeBinaryRelationalExpression)
			return toMinion((CommutativeBinaryRelationalExpression) constraint);
		
		return null;
	}
	
	
	
	
	
	
	/**
	 * Converts a commutative binary relational expression into the appropriate minion
	 * constraint. IMPORTANT NOTE: requires the arguments to be atoms. 
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(CommutativeBinaryRelationalExpression constraint) 
		throws MinionException {
		
		int operator = constraint.getOperator();
		Expression leftExpression = constraint.getLeftArgument();
		ArithmeticAtomExpression leftArgument = null;
		Expression rightExpression = constraint.getRightArgument();
		ArithmeticAtomExpression rightArgument = null;
		
		// get the left and right atoms 
		if(!(leftExpression instanceof ArithmeticAtomExpression)) {
			if(leftExpression instanceof RelationalAtomExpression) 
				leftArgument = ((RelationalAtomExpression)leftExpression).toArithmeticExpression();
			else throw new MinionException("Cannot translate constraint nested in another expression as in:"+constraint);	
				
		}
		else leftArgument = (ArithmeticAtomExpression) leftExpression;
		
		if(!(rightExpression instanceof ArithmeticAtomExpression)) {
			if(rightExpression instanceof RelationalAtomExpression) 
				rightArgument = ((RelationalAtomExpression)rightExpression).toArithmeticExpression();
			else throw new MinionException("Cannot translate constraint nested in another expression as in:"+constraint);	
				
		}
		else rightArgument = (ArithmeticAtomExpression) rightExpression;
		
		
		
		if(operator == Expression.EQ ||
				operator == Expression.IFF) {
			
			return new EqConstraint(toMinion(leftArgument), 
					                toMinion(rightArgument));
		}
		else if(operator == Expression.NEQ) {
			return new DiseqConstraint(toMinion(leftArgument), 
	                toMinion(rightArgument));
		}
			
		
		throw new MinionException("Unknown commutative binary relation:"+constraint);
	}
	
	
	/**
	 * Return the String representation of the Atomic arithmetic expression.
	 * 
	 * NOTE: there are ONLY CONSTANT indices allowed now. Every occurrence
	 * of another expression should have been flattened to an element constraint
	 * by now.
	 * 
	 * @param atom
	 * @return
	 * @throws MinionException
	 */
	private MinionAtom toMinion(ArithmeticAtomExpression atom) 
		throws MinionException {
		
		if(atom.getType() == Expression.INT)
			return new MinionConstant(atom.getConstant());
		
		else {
			Variable variable = atom.getVariable();
			
			if(variable instanceof ArrayVariable) {
				ArrayVariable arrayElement = (ArrayVariable) variable;
				
				if(this.offsetsFromZero.containsKey(arrayElement.getArrayNameOnly())) {
					int[] offsets = this.offsetsFromZero.get(((ArrayVariable) variable).getArrayNameOnly());
					
					int[] indices = arrayElement.getIntegerIndices();
					if(indices ==null) 
						throw new MinionException("Cannot translate array element with non-constant element index:"+atom);
					
					for(int i=0; i<indices.length;i++) {
						indices[i] = indices[i]-offsets[i];
					}
					return new MinionArrayElement(arrayElement.getArrayNameOnly(),indices);
				}
				else throw new MinionException("Cannot find offsets for array element:"+atom);
			}
			else return new MinionSingleVariable(variable.getVariableName());
		}

	}
	
}
