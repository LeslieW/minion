package translator.tailor.minion;

import translator.expression.*;
import java.util.ArrayList;
import java.util.HashMap;
import translator.normaliser.NormalisedModel;
import translator.solver.Minion;

public class MinionTailor {

	public final String MINION_AUXVAR_NAME = "_aux";
	int noMinionAuxVars;
	int usedCommonSubExpressions;
	
	MinionModel minionModel;
	HashMap<String, int[]> offsetsFromZero;
	NormalisedModel normalisedModel;
	Minion solverSettings;
	
	
	// ======== CONSTRUCTOR ==================================
	
	public MinionTailor(NormalisedModel normalisedModel,
						Minion solverSettings) {
			
		this.offsetsFromZero = new HashMap<String,int[]>();
		this.normalisedModel = normalisedModel;
		this.solverSettings = solverSettings;
		this.noMinionAuxVars = this.normalisedModel.getAuxVariables().size();
		this.usedCommonSubExpressions = this.normalisedModel.getAmountOfCommonSubExpressionsUsed();
	}
	
	// ====== TRANSLATION TO MINION REPRESENTATION ===========
	
	
	public MinionModel tailorToMinion() 
		throws MinionException {
		
		// 1. tailor the variables and create a new empty model
		this.minionModel = new MinionModel(new ArrayList<MinionConstraint>(),
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
	 * 
	 * @return
	 * @throws MinionException
	 */
	protected HashMap<String, ConstantDomain> mapDecisionVariables() 
		throws MinionException {
		
		HashMap<String,ConstantDomain> decisionVariables = new HashMap<String, ConstantDomain>();
		ArrayList<String> decisionVariablesNames = this.normalisedModel.getDecisionVariablesNames();
		
		for(int i=0; i<decisionVariablesNames.size(); i++) {
			String varName = decisionVariablesNames.get(i);
			Domain domain = this.normalisedModel.getDomainOfVariable(varName);
			domain = domain.evaluate();
			if(domain instanceof ConstantDomain) {
				ConstantDomain constantDomain = (ConstantDomain) domain;
				decisionVariables.put(varName, constantDomain);
				
				if(constantDomain instanceof ConstantArrayDomain) {
					ConstantDomain[] indices = ((ConstantArrayDomain) constantDomain).getIndexDomains();
					int[] offsets = new int[indices.length];
					
					for(int j=0; j<offsets.length; j++) {
						
						if(indices[j] instanceof BoolDomain)
							offsets[j] = 0;
						else if(indices[j] instanceof BoundedIntRange)
							offsets[j] = ((BoundedIntRange) indices[j]).getRange()[0]; // lowerBound
						
						else throw new MinionException("Cannot translate sparse domains as array-indices yet.");
					}
					this.offsetsFromZero.put(varName, offsets);
				}
					
				
			}
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
		
		if(constraint instanceof NonCommutativeRelationalBinaryExpression)
			return toMinion((NonCommutativeRelationalBinaryExpression) constraint);
		
		if(constraint instanceof SumConstraint)
			return toMinion((SumConstraint) constraint);
		
		if(constraint instanceof Reification) 
			return toMinion((Reification) constraint);
		
		if(constraint instanceof Disjunction)
			return toMinion((Disjunction) constraint);
	
		if(constraint instanceof Conjunction)
			return toMinion((Conjunction) constraint);
		
		if(constraint instanceof translator.expression.ProductConstraint)
			return toMinion((translator.expression.ProductConstraint) constraint);
		
		if(constraint instanceof Multiplication)
			return toMinion((Multiplication) constraint);
		
		if(constraint instanceof RelationalAtomExpression)
			return toMinion((RelationalAtomExpression) constraint);
		
		
		if(constraint instanceof AllDifferent) 
			return toMinion((translator.expression.AllDifferent) constraint);
		
		throw new MinionException("Cannot tailor expression to Minion yet:"+constraint);
	}
	
	
	
	protected MinionConstraint toMinion(translator.expression.AllDifferent allDiff) 
		throws MinionException {
		
		MinionArray array = toMinionArray((Array) allDiff.getArgument());
		return new AllDifferent(array);
	}
	
	/**
	 * Tailor simple array
	 * 
	 * @param array
	 * @return
	 * @throws MinionException
	 */
	protected MinionArray toMinionArray(Array array) 
		throws MinionException {
		
		if(array instanceof SimpleArray) {
			return new MinionSimpleArray(((SimpleArray) array).getArrayName());			
		}
		else throw new MinionException("Sorry, cannot translate array type yet:"+array);

	}
	
	
	/**
	 * This method always returns a minion atom, since there is NO way a multiplication
	 * can occur in Minion without being flattened.
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	protected MinionAtom toMinion(Multiplication constraint) 
		throws MinionException {
		
		ArrayList<Expression> arguments = constraint.getArguments();
		if(arguments.size() != 2)
			throw new MinionException("Minion's product constraint only takes two arguments. Therefore cannot translate:"+constraint);
		
		arguments.get(0).willBeFlattenedToVariable(true);
		arguments.get(1).willBeFlattenedToVariable(true);
		
		MinionAtom leftArg = (MinionAtom) toMinion(arguments.get(0));
		MinionAtom rightArg = (MinionAtom) toMinion(arguments.get(1));
		
		MinionAtom auxVariable = createMinionAuxiliaryVariable(constraint.getDomain()[0], constraint.getDomain()[1]);
		ProductConstraint product = new ProductConstraint(leftArg,
				 										  rightArg,
				 										  auxVariable);
		this.minionModel.addConstraint(product);
		return auxVariable;
	}
	
	
	/**
	 * 
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(translator.expression.ProductConstraint constraint) 
		throws MinionException {
		
		Expression[] arguments = constraint.getArguments();
		if(arguments.length != 2)
			throw new MinionException("Minion's product constraint only takes two arguments. Therefore cannot translate:"+constraint);
		
		arguments[0].willBeFlattenedToVariable(true);
		arguments[1].willBeFlattenedToVariable(true);
		
		MinionAtom[] productArgs = new MinionAtom[2];
		productArgs[0] = (MinionAtom) toMinion(arguments[0]);
		productArgs[1] = (MinionAtom) toMinion(arguments[1]);
		
		
		Expression resultExpression = constraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		ProductConstraint minionProduct = new ProductConstraint(productArgs[0],
				                                                productArgs[1],
				                                                result);
		
		if(constraint.isGonnaBeFlattenedToVariable()) 
			return reifyMinionConstraint(minionProduct);
		else return minionProduct;
	
	}
	
	
	/**
	 * Translate a conjunction by a sum constraint
	 * 
	 * @param conjunction
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(Conjunction conjunction) 
		throws MinionException {
		
		ArrayList<Expression> conjointArgs = conjunction.getArguments();
		MinionAtom[] arguments = new MinionAtom[conjointArgs.size()];
		
		for(int i=0; i<arguments.length; i++) {
			Expression conjointArg = conjointArgs.get(i);
			conjointArg.willBeFlattenedToVariable(true);
			arguments[i] = (MinionAtom) toMinion(conjointArg);
		}
		
		if(conjunction.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(new SumGeqConstraint(arguments, new MinionConstant(arguments.length)));
		}
		else return new SumGeqConstraint(arguments, new MinionConstant(arguments.length));
		
		
	}
	
	
	/**
	 * Translate a disjunction by a sum constraint
	 * 
	 * @param disjunction
	 * @return
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(Disjunction disjunction)
		throws MinionException {
		
		ArrayList<Expression> disjointArgs = disjunction.getArguments();
		MinionAtom[] arguments = new MinionAtom[disjointArgs.size()];
		
		for(int i=0; i<arguments.length; i++) {
			Expression disjointArg = disjointArgs.get(i);
			disjointArg.willBeFlattenedToVariable(true);
			arguments[i] = (MinionAtom) toMinion(disjointArg);
		}
		
		if(disjunction.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(new SumGeqConstraint(arguments, new MinionConstant(1)));
		}
		else return new SumGeqConstraint(arguments, new MinionConstant(1));
	}
	
	
	/**
	 * Map a reification constraint to Minion. This method does not
	 * return a variable!!
	 * 
	 * @param reification
	 * @return a Reify Constraint
	 * @throws MinionException
	 */
	protected MinionConstraint toMinion(Reification reification) 
		throws MinionException {
		
		MinionConstraint reifiedConstraint = toMinion(reification.getReifiedConstraint());
		MinionAtom reifiedVariable = (MinionAtom) toMinion(reification.getReifiedVariable());
		
		if(reification.isGonnaBeFlattenedToVariable())
			return reifyMinionConstraint(new Reify(reifiedConstraint, reifiedVariable));
		
		return new Reify(reifiedConstraint, reifiedVariable);
	}
	
	/**
	 * Translate a sum constraint to Minion. A sum constraint is a collection
	 * of positive and negative arguments that are added up and in a relation
	 * (<,>,=,!=,<=,>=) to a certain result expression.
	 * 
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(SumConstraint sumConstraint) 
		throws MinionException {
		
		int operator = sumConstraint.getRelationalOperator();
		
		if(operator == Expression.LESS ||
				operator == Expression.GREATER ||
				operator == Expression.NEQ)
			return toMinionStrongIneqSumConstraint(sumConstraint);
		
		else 
			return toMinionWeakIneqSumConstraint(sumConstraint);
		
	}
	
	
	/**
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinionStrongIneqSumConstraint(SumConstraint sumConstraint)
		throws MinionException {
		
		Expression[] positiveArguments = sumConstraint.getPositiveArguments();
		Expression[] negativeArguments = sumConstraint.getNegativeArguments();
		Expression resultExpression = sumConstraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		int operator = sumConstraint.getRelationalOperator();
		
		ArithmeticAtomExpression auxVariable = new ArithmeticAtomExpression(createAuxVariable());
		
		SumConstraint firstSum = new SumConstraint(positiveArguments,
				                                   negativeArguments,
				                                   Expression.EQ,
				                                   auxVariable,
				                                   true);
		//System.out.println("intermediate sum consteaint:"+firstSum+" while translating: "+sumConstraint);
		MinionConstraint firstConstraint = toMinionWeakIneqSumConstraint(firstSum);
		this.minionModel.addConstraint(firstConstraint);
		
		if(sumConstraint.isGonnaBeFlattenedToVariable()) {
			if(operator == Expression.LESS) {
				IneqConstraint constraint =  (sumConstraint.isResultOnLeftSide()) ? 
						new IneqConstraint(result, (MinionAtom) toMinion(auxVariable), -1) : 
							new IneqConstraint((MinionAtom) toMinion(auxVariable), result, -1) ;
						
				return reifyMinionConstraint(constraint);		
			}
			else if(operator == Expression.GREATER) {
				IneqConstraint constraint =  (sumConstraint.isResultOnLeftSide()) ? 
						new IneqConstraint((MinionAtom) toMinion(auxVariable), result, -1) : 
							new IneqConstraint(result, (MinionAtom) toMinion(auxVariable), -1) ;
						
				return reifyMinionConstraint(constraint);	
			}
			else if(operator == Expression.NEQ) {
				DiseqConstraint constraint =  new DiseqConstraint((MinionAtom) toMinion(auxVariable), result);		
				return reifyMinionConstraint(constraint);					
				
			}
			else throw new MinionException("Internal error. Tried to tailor non-strong relational sum by strong sum method:"+sumConstraint);
		}
		else {
			if(operator == Expression.LESS) {
				return  (sumConstraint.isResultOnLeftSide()) ? 
						new IneqConstraint(result, (MinionAtom) toMinion(auxVariable), -1) : 
							new IneqConstraint((MinionAtom) toMinion(auxVariable), result, -1) ;		
			}
			else if(operator == Expression.GREATER) {
				return  (sumConstraint.isResultOnLeftSide()) ? 
						new IneqConstraint((MinionAtom) toMinion(auxVariable), result, -1) : 
							new IneqConstraint(result, (MinionAtom) toMinion(auxVariable), -1) ;
						
			}
			else if(operator == Expression.NEQ) {
				return  new DiseqConstraint((MinionAtom) toMinion(auxVariable), result);		
				
			}
			else throw new MinionException("Internal error. Tried to tailor non-strong relational sum by strong sum method:"+sumConstraint);
			
			
		}

	}
	
	
	/**
	 * Translate SumConstraints that have the following operators:
	 * <=. >=. =
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinionWeakIneqSumConstraint(SumConstraint sumConstraint)
		throws MinionException {
	
		Expression[] positiveArgs = sumConstraint.getPositiveArguments();
		Expression[] negativeArgs = sumConstraint.getNegativeArguments();
		
		
		// ---------------- we have to reify the sum!! --------------------------------------
		// // don't use watched or weighted sum then!
		if(sumConstraint.isGonnaBeFlattenedToVariable()) {
	
			MinionAtom[] arguments = new MinionAtom[positiveArgs.length+negativeArgs.length];
			
			for(int i=0; i<positiveArgs.length; i++) {
				positiveArgs[i].willBeFlattenedToVariable(true);
				arguments[i] = (MinionAtom) toMinion(positiveArgs[i]);
			}
			Expression resultExpression = sumConstraint.getResult();
			resultExpression.willBeFlattenedToVariable(true);
			MinionAtom result = (MinionAtom) toMinion(resultExpression);
			
			int operator = sumConstraint.getRelationalOperator();
			
			if(operator == Expression.LEQ) 
				return reifyMinionConstraint(new SumLeqConstraint(arguments, result));
			
			else if(operator == Expression.GEQ)
				return reifyMinionConstraint(new SumGeqConstraint(arguments,result));
			
			else if(operator == Expression.EQ) {
				SumLeqConstraint sum1 = new SumLeqConstraint(arguments, result);
				SumGeqConstraint sum2 = new SumGeqConstraint(arguments, result);
				
				MinionAtom auxVariable1 = reifyMinionConstraint(sum1);
				MinionAtom auxVariable2 = reifyMinionConstraint(sum2);
				
				MinionAtom reifiedVariable = createMinionAuxiliaryVariable();
				
				ProductConstraint conjunction = new ProductConstraint(auxVariable1,auxVariable2,reifiedVariable);
				this.minionModel.addConstraint(conjunction);
				
				return reifiedVariable;
				
			}
			else throw new MinionException("Interal error: expected only weak operator instead of operator '"
					+operator+"' in constraint:"+sumConstraint);
		}
		
		//--------------- else: we don't have to reify the constraint -----------------------------------------
		else {	
			// check what kind of sum we have here and look for the right kind to match up with
			boolean hasNegativeArguments = (negativeArgs.length > 0) ? true : false;
			boolean hasMultiplication = false;
			
			for(int i=0; i<positiveArgs.length; i++) {
				if(positiveArgs[i] instanceof UnaryMinus)
					hasNegativeArguments = true;
				
				if(positiveArgs[i] instanceof Multiplication) {
					hasMultiplication = true;
				}
			}
			
			if(hasNegativeArguments || hasMultiplication) {
				return toMinionWeakWeightedSumConstraint(sumConstraint);
				
			}
			// positive, non linear
			else return toSimpleUnReifiedWeakSumConstraint(sumConstraint);
		
		}
	}
	
	
	/**
	 * Return a weightedsumleq/weightedsumgeq (or both for =) constraint. 
	 * It will NOT reify a constraint!!
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinionWeakWeightedSumConstraint(SumConstraint sumConstraint) 
		throws MinionException {
		
		Expression[] positiveArguments = sumConstraint.getPositiveArguments();
		Expression[] negativeArguments = sumConstraint.getNegativeArguments();
		
		int operator = sumConstraint.getRelationalOperator();
		Expression resultExpression = sumConstraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		// check for the case when E1 - E2 RELOP 0   ===>   E1 RELOP E2
		if(positiveArguments.length == 1 && 
				negativeArguments.length == 1 &&
				resultExpression.getType() == Expression.INT) {
			
			if(((ArithmeticAtomExpression) resultExpression).getConstant() == 0) {
				Expression posArg = positiveArguments[0];
				posArg.willBeFlattenedToVariable(true);
				Expression negArg = negativeArguments[0];
				negArg.willBeFlattenedToVariable(true);
				if(operator == Expression.LEQ)
					return new IneqConstraint((MinionAtom) toMinion(posArg), (MinionAtom) toMinion(negArg));
				
				else if(operator == Expression.GEQ)
					return new IneqConstraint((MinionAtom) toMinion(negArg), (MinionAtom) toMinion(posArg));
				
				else if(operator == Expression.EQ) 
					return new EqConstraint((MinionAtom) toMinion(negArg), (MinionAtom) toMinion(posArg));	
			}	
		}
		
		
		int[] weights = new int[positiveArguments.length + negativeArguments.length];
		MinionAtom[] arguments = new MinionAtom[positiveArguments.length + negativeArguments.length];
			
			for(int i=0; i<positiveArguments.length; i++) {
				Expression argument = positiveArguments[i];
				
				if(argument instanceof Multiplication) {
					argument.orderExpression();
					argument.evaluate();
					Multiplication product = (Multiplication) argument;
					
					if(product.getArguments().size() > 2) 
						throw new MinionException("Cannot translate n-ary product constraint:"+argument);
					
					else if(product.getArguments().size() == 1) {
						weights[i] = 1;
						Expression onlyArg = product.getArguments().remove(0);
						onlyArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(onlyArg);
					}
					
					// we have exactly 2 arguments
					if(product.getArguments().get(0).getType() == Expression.INT) {
						weights[i] = ((ArithmeticAtomExpression) product.getArguments().remove(0)).getConstant();
						Expression otherArg = product.getArguments().remove(0);
						otherArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(otherArg);
					}
					else {
						product.willBeFlattenedToVariable(true);
						weights[i] = 1;
						arguments[i] = (MinionAtom) toMinion(product);
					}
				} // end if: multiplication
				
				else { // any other type
					argument.willBeFlattenedToVariable(true);
					weights[i] = 1;
					arguments[i] = (MinionAtom) toMinion(argument);	
				}
			} // end for: all positive arguments
			
			
			
			// all negative arguments
			for(int i=positiveArguments.length; i<positiveArguments.length+negativeArguments.length; i++) {
				Expression argument = negativeArguments[i-positiveArguments.length];
				
				if(argument instanceof Multiplication) {
					argument.orderExpression();
					argument.evaluate();
					Multiplication product = (Multiplication) argument;
					
					if(product.getArguments().size() > 2) 
						throw new MinionException("Cannot translate n-ary product constraint:"+argument);
					
					else if(product.getArguments().size() == 1) {
						weights[i] = -1;
						Expression onlyArg = product.getArguments().remove(0);
						onlyArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(onlyArg);
					}
					
					// we have exactly 2 arguments
					if(product.getArguments().get(0).getType() == Expression.INT) {
						weights[i] = -((ArithmeticAtomExpression) product.getArguments().remove(0)).getConstant();
						Expression otherArg = product.getArguments().remove(0);
						otherArg.willBeFlattenedToVariable(true);
						arguments[i] = (MinionAtom) toMinion(otherArg);
					}
					else {
						product.willBeFlattenedToVariable(true);
						weights[i] = -1;
						arguments[i] = (MinionAtom) toMinion(product);
					}
				} // end if: multiplication
				
				else { // any other type
					argument.willBeFlattenedToVariable(true);
					weights[i] = -1;
					arguments[i] = (MinionAtom) toMinion(argument);	
				}
			} // end for: all negative arguments
			
		
			boolean allWeightsAreOnes = true;
			
			for(int i=0; i<weights.length; i++)
				allWeightsAreOnes = allWeightsAreOnes && (weights[i] == 1);
			
			
			if(operator == Expression.LEQ) {
				return (allWeightsAreOnes) ?
						new SumLeqConstraint(arguments, result) :
						new WeightedSumLeqConstraint(arguments,weights, result);
			}
			else if(operator == Expression.GEQ) {
				return  (allWeightsAreOnes)  ? 
						new SumGeqConstraint(arguments, result) :
						new WeightedSumGeqConstraint(arguments,weights, result);
			}
			else if(operator == Expression.EQ) {
				MinionConstraint weightedConstraint1 =  (allWeightsAreOnes) ?
						new SumGeqConstraint(arguments, result) :
						new WeightedSumGeqConstraint(arguments, weights, result);
						
				this.minionModel.addConstraint(weightedConstraint1);
				return (allWeightsAreOnes) ? 
						new SumLeqConstraint(arguments, result) :
						new WeightedSumLeqConstraint(arguments,weights, result);
				
				
			}
			else throw new MinionException("Internal error: tried to translate non-weak sumConstraint with weak-sumConstraint method:"+sumConstraint);
	
	}
	
	/**
	 * Return a simple sumleq/sumgeq (or both for =) constraint. The sum MAY NOT contain
	 * any negative arguments!! This method will return 
	 * a watched literal sum constriant if the arguments are all booleans.
	 * It will NOT reify a constraint!!
	 * 
	 * @param sumConstraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toSimpleUnReifiedWeakSumConstraint(SumConstraint sumConstraint) 
		throws MinionException {
		
		int operator = sumConstraint.getRelationalOperator();
		Expression[] positiveArgs = sumConstraint.getPositiveArguments();
		boolean areBooleanArguments = true;
		
		MinionAtom[] arguments = new MinionAtom[positiveArgs.length];
		for(int i=0; i<positiveArgs.length; i++) {
			positiveArgs[i].willBeFlattenedToVariable(true);
			arguments[i] = (MinionAtom) toMinion(positiveArgs[i]);
			if(areBooleanArguments) {
				if(!this.minionModel.variableHasBooleanDomain(arguments[i].getVariableName())) {
					areBooleanArguments = false && areBooleanArguments;
				}
			}
		}
		Expression resultExpression = sumConstraint.getResult();
		resultExpression.willBeFlattenedToVariable(true);
		MinionAtom result = (MinionAtom) toMinion(resultExpression);
		
		
		if(operator == Expression.LEQ) {
			if(this.minionModel.solverSettings.useWatchedSum()) {
				if(areBooleanArguments) {
					return new SumLeqConstraint(arguments, result, true);
				}
				else return new SumLeqConstraint(arguments, result, false);
			}
			else return new SumLeqConstraint(arguments, result);
		}
		
		else if(operator == Expression.GEQ) {
			if(this.minionModel.solverSettings.useWatchedSum()) {
				if(areBooleanArguments) {
					return new SumGeqConstraint(arguments, result, true);
				}
				else return new SumGeqConstraint(arguments, result, false);
			}
			else return new SumGeqConstraint(arguments, result);	
		}
		
		else if(operator == Expression.EQ) {
			if(this.minionModel.solverSettings.useWatchedSum()) {
				if(areBooleanArguments) {
					this.minionModel.addConstraint(new SumLeqConstraint(arguments, result, true));
					return new SumGeqConstraint(arguments, result, true);
				}
				else {
					this.minionModel.addConstraint(new SumLeqConstraint(arguments, result, false));
					return new SumGeqConstraint(arguments, result, false);
				}
			}
			else {
				this.minionModel.addConstraint(new SumLeqConstraint(arguments, result));
				return new SumGeqConstraint(arguments, result);	
			}
		}
		else throw new MinionException("Internal error: expected only sumconstraint with operators =,<=,>= instead of:"+sumConstraint);
		
		
	}
	
	/**
	 * Converts non commutative binary relations to Minion representation
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionConstraint toMinion(NonCommutativeRelationalBinaryExpression constraint) 
		throws MinionException {
		
		// lex-constraints!!!! -> move them to another expression type!!
		
		
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
		
		MinionConstraint minionConstraint = null;
		
		if(operator == Expression.LEQ)
			minionConstraint =new IneqConstraint(toMinion(leftArgument), toMinion(rightArgument),0);
		
		else if(operator == Expression.GEQ)
			minionConstraint = new IneqConstraint(toMinion(rightArgument), toMinion(leftArgument), 0);
		
		else if(operator == Expression.LESS)
			minionConstraint = new IneqConstraint(toMinion(leftArgument), toMinion(rightArgument),-1);
		
		else if(operator == Expression.GREATER)
			minionConstraint = new IneqConstraint(toMinion(rightArgument), toMinion(leftArgument), -1);
		
		else if(operator == Expression.IF)
			minionConstraint = new IneqConstraint(toMinion(leftArgument), toMinion(rightArgument),0);
		
		else throw new MinionException("Unknown non-commutative binary relation:"+constraint);
		
		
		if(constraint.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(minionConstraint);
		}
		else return minionConstraint;
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
		
		MinionConstraint minionConstraint = null;
		
		if(operator == Expression.EQ ||
				operator == Expression.IFF) {
			
			minionConstraint =  new EqConstraint(toMinion(leftArgument), 
					                toMinion(rightArgument));
		}
		else if(operator == Expression.NEQ) {
			minionConstraint =  new DiseqConstraint(toMinion(leftArgument), 
	                toMinion(rightArgument));
		}
		else throw new MinionException("Unknown commutative binary relation:"+constraint);	
		
		if(constraint.isGonnaBeFlattenedToVariable()) {
			return reifyMinionConstraint(minionConstraint);
		}
		else return minionConstraint;
		
		
	}
	
	
	/**
	 * 
	 * @param constraint
	 * @return
	 * @throws MinionException
	 */
	private MinionAtom reifyMinionConstraint(MinionConstraint constraint)
		throws MinionException {
		

		MinionAtom auxVariable = createMinionAuxiliaryVariable();
		
		MinionConstraint reifiedConstraint = new Reify(constraint, auxVariable);
		this.minionModel.addConstraint(reifiedConstraint);
		
		return auxVariable;
	}
		
	
	private MinionAtom createMinionAuxiliaryVariable() {
		
		SingleVariable auxVar = new SingleVariable(MINION_AUXVAR_NAME+this.noMinionAuxVars++,
                new translator.expression.BoolDomain());
		MinionSingleVariable auxVariable = new MinionSingleVariable(MINION_AUXVAR_NAME+(this.noMinionAuxVars-1));
		this.minionModel.addAuxiliaryVariable(auxVar);
		
		return auxVariable;
	}
	
	private MinionAtom createMinionAuxiliaryVariable(int lb, int ub) {
		
		SingleVariable auxVar = new SingleVariable(MINION_AUXVAR_NAME+this.noMinionAuxVars++,
                									new translator.expression.BoundedIntRange(lb,ub));
		MinionSingleVariable auxVariable = new MinionSingleVariable(MINION_AUXVAR_NAME+(this.noMinionAuxVars-1));
		this.minionModel.addAuxiliaryVariable(auxVar);
		
		return auxVariable;
	}
	
	
	/**
	 * Create an auxliary variable represented as an Expression (not the minion representation)
	 * Is added to the list of aux variables in the minion model.
	 * 
	 * @return
	 */
	private Variable createAuxVariable() {
		
		SingleVariable variable = new SingleVariable(this.MINION_AUXVAR_NAME+this.noMinionAuxVars++, new BoolDomain());
		this.minionModel.addAuxiliaryVariable(variable);
		return variable;
		
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
	
	
	protected MinionConstraint toMinion(RelationalAtomExpression atom) 
	throws MinionException {
	
	if(atom.getType() == Expression.BOOL)
		return (((RelationalAtomExpression) atom).getBool()) ? 
				new MinionConstant(1) :
					new MinionConstant(0);
	
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
