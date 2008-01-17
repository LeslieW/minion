package translator.normaliser;

import translator.conjureEssenceSpecification.*;
import translator.expression.*;
import translator.expression.Expression;

import java.util.HashMap;
import java.util.ArrayList;



/**
 * Maps expression of the syntax tree to a different expression
 * representation that encaptures several features and which is 
 * used through out the reformulation system.
 * <br>
 * Before mapping expressions to advanced expressions the following 
 * steps MUST have been taken:
 * 1. Insert domain ranges for their corresponding identifiers. This means
 * 	  that all domains assigned to a variable in the decision variable 
 *    HashMap HAVE TO be either boolean domains, integer domains or 
 *    matrix domains composed of booleans or/and integers. NO IDENTIFIER
 *    domains are allowed, so please insert all ranges for the corresponding
 *    domain-identifiers.
 *    
 * 
 * @author andrea
 *
 */

public class ExpressionMapper {

	/** A hashmap containing all decision variables with their corresponding domains  */
	HashMap<String, translator.conjureEssenceSpecification.Domain> decisionVariables;
	/** A hashmap containing all parameters with their corresponding domains */
	HashMap<String, translator.conjureEssenceSpecification.Domain> parameterDomains;
	
	/** contains all the parameter arrays with their values */
	HashMap<String, translator.conjureEssenceSpecification.ConstantArray> constantArrays;
	
    String debug = "";
	
	public ExpressionMapper(HashMap<String,translator.conjureEssenceSpecification.Domain> decisionVariables,
			                HashMap<String, translator.conjureEssenceSpecification.Domain> parameterDomains, 
			                HashMap<String, translator.conjureEssenceSpecification.ConstantArray> constantArrays) {
		
		this.decisionVariables = decisionVariables;
		this.parameterDomains = parameterDomains;
		this.constantArrays = constantArrays;
	}
	
	
	
	// ======================== METHODS =========================================================
	
	/**
	 * Map the old expressions to the new format
	 */
	public  ArrayList<Expression> mapExpressions(translator.conjureEssenceSpecification.Expression[] oldConstraints) 
	throws NormaliserException {
	
		ArrayList<translator.expression.Expression> newConstraints = new ArrayList<translator.expression.Expression> ();
	
		for(int i=0; i<oldConstraints.length; i++) {
			translator.expression.Expression newExpression = mapExpression(oldConstraints[i]);
			newConstraints.add(newExpression);
		}
		
		
		return newConstraints;
	}
	
	
	/**
	 * Maps Expression oldExpression to an Expression of the form used
	 * throughout the reformulation process .
	 * 
	 * @param oldExpression
	 * @return
	 */
	public translator.expression.Expression mapExpression(translator.conjureEssenceSpecification.Expression oldExpression) 
		throws NormaliserException {
		
		switch(oldExpression.getRestrictionMode()) {
		
		case EssenceGlobals.BRACKET_EXPR:
			return mapExpression(oldExpression.getExpression());
		
		case EssenceGlobals.ATOMIC_EXPR:	
			return mapAtomicExpression(oldExpression.getAtomicExpression()); 
			
		case EssenceGlobals.NONATOMIC_EXPR:
			return mapNonAtomicExpression(oldExpression.getNonAtomicExpression());
			
		case EssenceGlobals.UNITOP_EXPR:
			return mapUnaryExpression(oldExpression.getUnaryExpression());
				
		case EssenceGlobals.BINARYOP_EXPR:
			return mapBinaryExpression(oldExpression.getBinaryExpression());
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			return mapQuantification(oldExpression.getQuantification());
			
		case EssenceGlobals.FUNCTIONOP_EXPR:
			return mapGlobalConstraint(oldExpression.getFunctionExpression());
			
		case EssenceGlobals.LEX_EXPR:
			return mapLexConstraint(oldExpression.getLexExpression());
			
		case EssenceGlobals.TABLE_CONSTRAINT:
			return mapTableConstraint(oldExpression.getTableConstraint());
			
		default: 
			throw new NormaliserException("Cannot map expression yet or unknown expression type:\n"+oldExpression);
				
		}
	}
	
	
	
	/**
	 * Converts the objective representation of the old syntax tree into the new syntax tree.
	 * 
	 * @param oldObjective
	 * @return the advanced representation of the objective
	 * @throws NormaliserException
	 */
	public Objective mapObjective(translator.conjureEssenceSpecification.Objective oldObjective)
		throws NormaliserException {
		
		if(oldObjective.getExpression() != null) {
			translator.expression.Expression objectiveExpression = mapExpression(oldObjective.getExpression());
			return new Objective(objectiveExpression,
					             !oldObjective.isMinimising());
		}
		
		else return new Objective();
			
	}
	
	
	/**
	 * Maps table constraints to the other representation used throughout the translation process.
	 * @param oldConstraint
	 * @return
	 * @throws NormaliserException
	 */
	protected translator.expression.TableConstraint mapTableConstraint(translator.conjureEssenceSpecification.TableConstraint oldConstraint)
	   throws NormaliserException {
		
		AtomExpression[] oldVariables = oldConstraint.getVariables();
		translator.expression.Variable[] newVariables = new translator.expression.Variable[oldVariables.length];
		for(int i=0; i<oldVariables.length; i++) 
			newVariables[i] = 
			                (oldVariables[i].getNonAtomicVariable() != null) ? 
			                		((translator.expression.ArithmeticAtomExpression) mapExpression(
			                				    new translator.conjureEssenceSpecification.Expression(oldVariables[i].getNonAtomicVariable()))).getVariable() :
			                		((translator.expression.ArithmeticAtomExpression) mapExpression(
			                					new translator.conjureEssenceSpecification.Expression(oldVariables[i].getAtomicVariable()))).getVariable();	
		
		
		translator.expression.ConstantTuple[] newConstantTuples = new translator.expression.ConstantTuple[oldConstraint.getConstantTuples().length] ;
		for(int i=0; i<newConstantTuples.length; i++)
			newConstantTuples[i] = new translator.expression.ConstantTuple(oldConstraint.getConstantTuples()[i].getTupleElements());
			                				    
		return new translator.expression.TableConstraint(newVariables,
				                                         newConstantTuples);
	}
	
	
	/**
	 * Maps a lex constraint
	 * 
	 * @param oldLexConstraint
	 * @return the advanced representation of a lex constraint
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapLexConstraint(LexExpression oldLexConstraint) 
		throws NormaliserException {
		
		return new NonCommutativeRelationalBinaryExpression(mapExpression(oldLexConstraint.getLeftExpression()),
				                                      mapOperator(oldLexConstraint.getLexOperator().getRestrictionMode()),
				                                      mapExpression(oldLexConstraint.getRightExpression()));
		
		
	}
	
	/**
	 * Maps global constraints to their corresponding new representation
	 * 
	 * @param oldGlobalConstraint
	 * @return the corresponding translator.expression.Expression for the global constraint
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapGlobalConstraint(FunctionExpression oldGlobalConstraint) 
		throws NormaliserException {
		
		switch(oldGlobalConstraint.getRestrictionMode()) {
		
		case EssenceGlobals.ALLDIFF:
			translator.expression.Expression argument = mapExpression(oldGlobalConstraint.getExpression1());
			if(argument instanceof Array)
				return new AllDifferent((Array) argument);
			else throw new NormaliserException("Illegal argument for alldifferent: '"+argument+
					"'. Expected a decision variable of array/matrix type.");
			
		case EssenceGlobals.ELEMENT:
			return new ElementConstraint(mapExpression(oldGlobalConstraint.getExpression1()),
					                     mapExpression(oldGlobalConstraint.getExpression2()),
					                     mapExpression(oldGlobalConstraint.getExpression3()));
			
		default:
			throw new NormaliserException("Cannot translate Global Constraint yet:"+oldGlobalConstraint);
		
		
		}
	}
	
	/**
	 * Maps a quantification of the old syntax tree representation to the new representation. Universal and 
	 * existential quantification are translated to the QuantifiedExpression data structure, sums are 
	 * directly translated to an n-ary sum.
	 * 
	 * @param oldQExpression
	 * @return
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapQuantification(QuantificationExpression oldQExpression) 
		throws NormaliserException {
		
		translator.expression.Domain quantifiedDomain = mapDomain(oldQExpression.getBindingExpression().getDomainIdentifiers().getDomain());
		String[] quantifiedVariables = oldQExpression.getBindingExpression().getDomainIdentifiers().getIdentifiers();
		translator.expression.Expression quantifiedExpression = mapExpression(oldQExpression.getExpression());
		
		
		if(oldQExpression.getQuantifier().getRestrictionMode() == EssenceGlobals.SUM) {
			
			return new QuantifiedSum(quantifiedVariables,
	                                 quantifiedDomain,
	                                 quantifiedExpression);
		}
			
		return (oldQExpression.getQuantifier().getRestrictionMode() == EssenceGlobals.FORALL) ? 
				new QuantifiedExpression(true, // is universal quantification == true
						                 quantifiedVariables,
						                 quantifiedDomain,
						                 quantifiedExpression)
		:
			new QuantifiedExpression(false,// is universal quantification == false
	                 quantifiedVariables,
	                 quantifiedDomain,
	                 quantifiedExpression)
		;
	}
	
	/**
	 * Maps array elements to the advanced expression representation.
	 * 
	 * 
	 * @param oldArrayElement
	 * @return either an relational or arithmetic atom expression, depending on the type of the domain.
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapNonAtomicExpression(NonAtomicExpression oldArrayElement) 
		throws NormaliserException {
		
		//--------------------- matrix name ----------------------------------------------
		String arrayName = oldArrayElement.getArrayName();
		

		
		// ------------- matrix domain ----------------------------------------------------
		// get the domain of the matrixElement
		translator.conjureEssenceSpecification.Domain domain = null;
		if(this.decisionVariables.containsKey(arrayName))
			domain = this.decisionVariables.get(arrayName);
		
		if(domain == null) { // in this case the array element is a parameter
			if(this.constantArrays.containsKey(arrayName) ) {
				
				BoundedIntRange paramDomain = new BoundedIntRange(translator.expression.Expression.LOWER_BOUND, 
 		                                                          translator.expression.Expression.UPPER_BOUND);
				
				// map the expression indices
				Index[] oldIndices = oldArrayElement.getIndexList();
				translator.expression.Expression[] newIndices = new translator.expression.Expression[oldIndices.length];
				for(int i=0; i<oldIndices.length; i++) {
					if(oldIndices[i] instanceof ExpressionIndex) {
						translator.conjureEssenceSpecification.Expression oldIndex = ((ExpressionIndex) oldIndices[i]).getIndexExpression();
						newIndices[i] = mapExpression(oldIndex);
					}
					else throw new NormaliserException("Cannot range-index parameter arrays yet, sorry: "+oldArrayElement);
					
				}
				translator.expression.ArrayVariable parameter = new ArrayVariable(arrayName,
						                                                          newIndices,
						                                                          paramDomain);
				
				return new ArithmeticAtomExpression(parameter, true);					
			}
			else throw new NormaliserException("Unknown array element:"+oldArrayElement);
			
		}
		
		
		// should we just map the base domain, or also the dimensions??
		if(domain.getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN) 
			throw new NormaliserException("Identifier '"+arrayName+"' does not correspond to an array element, but to domain: "+domain);
		
		// we only care about the base domain, not about the dimensions of the array 
		translator.conjureEssenceSpecification.Domain baseDomain = domain.getMatrixDomain().getRangeDomain();
		translator.expression.Domain mappedDomain = mapDomain(baseDomain);
		
		// dimensions check!
		Index[] indices = oldArrayElement.getIndexList();
		if(domain.getMatrixDomain().getIndexDomains().length != indices.length) 
			throw new NormaliserException("Array/Matrix dimensions do not fit in '"+arrayName+
					"': has "+indices.length+" indices for "+domain.getMatrixDomain().getIndexDomains().length+" dimensions."
					+"\nPlease give an index for _every_ dimension of the array element.");
		
		
		// check if we have ranges (the dereferenced expression is 1- or more-dimensional) 
		// or if this is just a single variable
		boolean isSingleVariable = true;
		for(int i=0; i<indices.length; i++) {
			//System.out.println("This is the index:"+indices[i]+" of variable "+arrayName);
			isSingleVariable = isSingleVariable && (indices[i] instanceof ExpressionIndex); 
			//System.out.println("This is the index:"+indices[i]+" of variable "+arrayName);
			
		}
		
		//System.out.println("indices length is:"+indices.length);
		
		// ----------- if this is not a single variable, then treat it in the variuable array method ---------------
		if(!isSingleVariable) {
			translator.expression.Domain[] indexDomains = new translator.expression.Domain[domain.getMatrixDomain().getIndexDomains().length];
			for(int i= 0; i<indexDomains.length; i++) {
				indexDomains[i] = mapDomain(domain.getMatrixDomain().getIndexDomains()[i]);
				//System.out.println("mapped old domain :"+domain.getMatrixDomain().getIndexDomains()[i]);
				//System.out.println("To new domain :"+indexDomains[i]);
			}
			
			return mapVariableArray(arrayName, indices, mappedDomain, indexDomains);
		}
		
		
		
		// ---------- Matrix indices -------------------------------------------------------
		
		boolean allIndicesAreInteger = true;
		
		for(int i=0; i<indices.length; i++) {
			translator.conjureEssenceSpecification.Expression index = ((ExpressionIndex) indices[i]).getIndexExpression();
			if(index.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				if(index.getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
					allIndicesAreInteger = false;
			}
			else allIndicesAreInteger = false;
		}
		
		if(allIndicesAreInteger) {
			int[] intIndices = new int[indices.length];
			for(int i=0; i<indices.length; i++) {
				intIndices[i] = ((ExpressionIndex) indices[i]).getIndexExpression().getAtomicExpression().getNumber();
			}
			return (mappedDomain.getType() == translator.expression.Domain.BOOL) ?
					  new RelationalAtomExpression(new ArrayVariable(arrayName,
                                                                     intIndices,
                                                                     mappedDomain))
				:
				  new ArithmeticAtomExpression(new ArrayVariable(arrayName,
					                                intIndices,
					                                mappedDomain));
			
		}
		// end if: allIndices are integer
		else { 
			translator.expression.Expression[] expressionIndices = new translator.expression.Expression[indices.length];
			for(int i=0; i<indices.length; i++) {
				expressionIndices[i] = mapExpression(((ExpressionIndex) indices[i]).getIndexExpression());
			} 
			return (mappedDomain.getType() == translator.expression.Domain.BOOL) ?
					  new RelationalAtomExpression(new ArrayVariable(arrayName,
                                                                   expressionIndices,
                                                                   mappedDomain))
				:			
			new ArithmeticAtomExpression(new ArrayVariable(arrayName,
					                                              expressionIndices,
					                                              mappedDomain));
		}
	}
	
	

	
	
	/**
	 * 
	 * 
	 * @param arrayName
	 * @param indices
	 * @param baseDomain
	 * @param indexDomains
	 * @return
	 * @throws NormaliserException
	 */
	private translator.expression.Expression mapVariableArray(String arrayName, 
			                                                  Index[] indices,
			                                                  translator.expression.Domain baseDomain,
			                                                  translator.expression.Domain[] indexDomains) 
	throws NormaliserException {
		
		
		if(indices.length != indexDomains.length) throw new NormaliserException("Array/Matrix dimensions do not fit in '"+arrayName+
					"': has "+indices.length+" indices for "+indexDomains.length+" dimensions."
					+"Please give an index for _every_ dimension of the array element.");
		
		
		BasicDomain[] basicIndexDomains = new BasicDomain[indices.length];
		
		
		
		for(int i=0; i<indices.length; i++) {
			
			// (..)
			if(indices[i].getType() == EssenceGlobals.FULL_BOUNDED_INDEX) {
				if(indexDomains[i] instanceof BasicDomain)
					basicIndexDomains[i] = (BasicDomain) indexDomains[i];
				else throw new NormaliserException("Illegal domain for index of array variable '"+arrayName+
						"'. Cannot set array domain as index domain:"+indexDomains[i]);
			}
			
			// e1..e2
			else if(indices[i].getType() == EssenceGlobals.UPPER_LOWER_BOUNDED_INDEX) {
				
				translator.expression.Expression lb = mapExpression(((BoundedIndex) indices[i]).getLowerExpressionIndex());
				translator.expression.Expression ub = mapExpression(((BoundedIndex) indices[i]).getUpperExpressionIndex());
				
				if(lb.getType() == translator.expression.Expression.INT && 
						lb.getType() == translator.expression.Expression.INT) {
					basicIndexDomains[i] = new BoundedIntRange(((ArithmeticAtomExpression) lb).getConstant(),
							                                   ((ArithmeticAtomExpression) ub).getConstant());
				}
				else basicIndexDomains[i] = new BoundedExpressionRange(lb,ub);
			}
			
			
			// (e1,e2, .., en)
			else if(indices[i].getType() == EssenceGlobals.SPARSE_INDEX) {
				
				SparseIndex sparseIndex = (SparseIndex) indices[i];
				boolean sparseElementsAreInteger = true;
				
				translator.expression.Expression[] sparseElements = new translator.expression.Expression[sparseIndex.getSparseElements().length];
				for(int j=0; j<sparseElements.length; j++) {
					sparseElements[j] = mapExpression(sparseIndex.getSparseElements()[j]);
					sparseElementsAreInteger = sparseElementsAreInteger && (sparseElements[j].getType() == translator.expression.Expression.INT);
				}
				
				if(sparseElementsAreInteger) {
					int[] sparseInts = new int[sparseElements.length];
					for(int j=0; j<sparseInts.length; j++) 
						sparseInts[j] = ((ArithmeticAtomExpression) sparseElements[j]).getConstant();
					basicIndexDomains[i] = new SparseIntRange(sparseInts);
				}
				else basicIndexDomains[i] = new SparseExpressionRange(sparseElements); 
			}
			
			
			// e1..
			else if(indices[i].getType() == EssenceGlobals.LOWER_BOUNDED_INDEX) {
				
				translator.expression.Expression lb = mapExpression(((BoundedIndex) indices[i]).getLowerExpressionIndex());
				
				if(indexDomains[i] instanceof BoundedIntRange) {
					int upperBound = ((BoundedIntRange) indexDomains[i]).getRange()[1];
					if(lb.getType() == translator.expression.Expression.INT) {
						basicIndexDomains[i] = new BoundedIntRange( ((ArithmeticAtomExpression) lb).getConstant(),
																	upperBound);
					}
					else basicIndexDomains[i] = new BoundedExpressionRange(lb, new ArithmeticAtomExpression(upperBound));
				}
				else if(indexDomains[i] instanceof BoundedExpressionRange) {
					translator.expression.Expression upperBound = ((BoundedExpressionRange) indexDomains[i]).getUpperBound();
					basicIndexDomains[i] = new BoundedExpressionRange(lb, upperBound);
				}
				else throw new NormaliserException("Can only use bounds-range for bound-domain-indexed array variables yet, sorry:");
			}
			
			
			// ..e2
			else if(indices[i].getType() == EssenceGlobals.UPPER_BOUNDED_INDEX) {
				
				translator.expression.Expression ub = mapExpression(((BoundedIndex) indices[i]).getUpperExpressionIndex());
				
				if(indexDomains[i] instanceof BoundedIntRange) {
					int lowerBound = ((BoundedIntRange) indexDomains[i]).getRange()[0];
					if(ub.getType() == translator.expression.Expression.INT) {
						basicIndexDomains[i] = new BoundedIntRange( lowerBound,
																	((ArithmeticAtomExpression) ub).getConstant());
					}
					else basicIndexDomains[i] = new BoundedExpressionRange(new ArithmeticAtomExpression(lowerBound), ub);
				}
				else if(indexDomains[i] instanceof BoundedExpressionRange) {
					translator.expression.Expression lowerBound = ((BoundedExpressionRange) indexDomains[i]).getLowerBound();
					basicIndexDomains[i] = new BoundedExpressionRange(lowerBound, ub);
				}
				else throw new NormaliserException("Can only use bounds-range for bound-domain-indexed array variables yet, sorry:");
				
				
			
			}
			// identifier
			else if(indices[i].getType() == EssenceGlobals.EXPRESSION_INDEX) {
				
				translator.expression.Expression indexExpression = mapExpression(((ExpressionIndex) indices[i]).getIndexExpression());
				indexExpression = indexExpression.evaluate();
				
				if(indexExpression.getType() == translator.expression.Expression.INT) {
					basicIndexDomains[i] = new SingleIntRange(((ArithmeticAtomExpression) indexExpression).getConstant());
				}
				else {
					basicIndexDomains[i] = new SingleExpressionRange(indexExpression);
				}
				
				
			}
		
			else throw new NormaliserException("Sorry, cannot translate variable index '"+indices[i]+
					" with which you dereferenced the variable '"+arrayName+"' yet.");	
			
		}
		
		
		return new IndexedArray(arrayName,
				                basicIndexDomains,
				                baseDomain);
	}
	
	
	/**
	 * Map a binary expression to the advanced expression representation. The expressions are mapped
	 * very simply and stupidly, meaning that we do not try to detect linear sums, but just map every
	 * possible n-ary operation, such as sums or conjunction to a binary relation. To assure that 
	 * linear sums or an n-ary conjunction/disjunction/multiplication is detected, run the 
	 * merge() method that optimises the expressions tree by merging adjacent common n-ary nodes into one 
	 * single n-ary node.
	 * 
	 * @param oldBinExpression
	 * @return
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapBinaryExpression(BinaryExpression oldBinExpression) 
		throws NormaliserException {
		
		int operator = oldBinExpression.getOperator().getRestrictionMode();
		translator.expression.Expression leftArgument = mapExpression(oldBinExpression.getLeftExpression());
		translator.expression.Expression rightArgument = mapExpression(oldBinExpression.getRightExpression());
		
		//print_debug("Left mapped expression:"+leftArgument+", right mapped expression:"+rightArgument);
		
		// non-commutative relational operator
		if(operator == EssenceGlobals.LEQ ||
				operator == EssenceGlobals.GEQ ||
				operator == EssenceGlobals.LESS ||
				operator == EssenceGlobals.GREATER ||
				operator == EssenceGlobals.IF) {
			return new NonCommutativeRelationalBinaryExpression(leftArgument, mapOperator(operator), rightArgument);
		}
		
		// commutative relational operator
		else if(operator == EssenceGlobals.EQ ||
				operator == EssenceGlobals.NEQ ||
		        operator == EssenceGlobals.IFF) {
			return new CommutativeBinaryRelationalExpression(leftArgument, mapOperator(operator), rightArgument);
		}
		
		// non-commutative arithmetic operator
		else if(operator == EssenceGlobals.POWER ||
				operator == EssenceGlobals.DIVIDE) {
			return new NonCommutativeArithmeticBinaryExpression(leftArgument, mapOperator(operator), rightArgument);
		}
		
		
		// possibly n-ary (commutative) operators 
		else if(operator == EssenceGlobals.MULT) {
			ArrayList<translator.expression.Expression> list = new ArrayList<translator.expression.Expression>();
			list.add(leftArgument);
			list.add(rightArgument);
			return new Multiplication(list);
		}
		else if(operator == EssenceGlobals.PLUS) {
			ArrayList<translator.expression.Expression> positiveList = new ArrayList<translator.expression.Expression>();
			positiveList.add(leftArgument);
			positiveList.add(rightArgument);
			ArrayList<translator.expression.Expression> negativeList = new ArrayList<translator.expression.Expression>();
			return new Sum(positiveList, negativeList);
		}		
		else if(operator == EssenceGlobals.MINUS) {
			ArrayList<translator.expression.Expression> positiveList = new ArrayList<translator.expression.Expression>();
			positiveList.add(leftArgument);
			ArrayList<translator.expression.Expression> negativeList = new ArrayList<translator.expression.Expression>();
			negativeList.add(rightArgument);
			return new Sum(positiveList, negativeList);
		}
		else if(operator == EssenceGlobals.AND) {
			ArrayList<translator.expression.Expression> conjointExpressions = new ArrayList<translator.expression.Expression>();
			conjointExpressions.add(leftArgument);
			conjointExpressions.add(rightArgument);
			return new Conjunction(conjointExpressions);
		}
		else if(operator == EssenceGlobals.OR) {
			ArrayList<translator.expression.Expression> disjointExpressions = new ArrayList<translator.expression.Expression>();
			disjointExpressions.add(leftArgument);
			disjointExpressions.add(rightArgument);
			return new Disjunction(disjointExpressions);
		}
		
		else throw new NormaliserException("Unknow operator '"+new BinaryOperator(operator)+"' in binary expression :"+oldBinExpression);
	}
	

	/**
	 * Maps the operator types from the old system to the operator types 
	 * of the new system. Operator types are constant numbers that 
	 * stand for an operation. In the new system, the operator constants
	 * are also ordered according to the precedence (and the ordering of 
	 * expressions as well)
	 * 
	 * @param oldOperator
	 * @return the corresponding constant value for the operator in the 'new'
	 * advanced expression representation
	 */
	protected int mapOperator(int oldOperator) {
		
		switch(oldOperator) {
		
		case EssenceGlobals.EQ:
			return translator.expression.Expression.EQ;
		case EssenceGlobals.LEQ:
			return translator.expression.Expression.LEQ;
		case EssenceGlobals.GEQ:
			return translator.expression.Expression.GEQ;
		case EssenceGlobals.NEQ:
			return translator.expression.Expression.NEQ;
		case EssenceGlobals.LESS:
			return translator.expression.Expression.LESS;
		case EssenceGlobals.GREATER:
			return translator.expression.Expression.GREATER;
		case EssenceGlobals.PLUS:
			return translator.expression.Expression.PLUS;
		case EssenceGlobals.MINUS:
			return translator.expression.Expression.MINUS;
		case EssenceGlobals.MULT:
			return translator.expression.Expression.MULT;
		case EssenceGlobals.POWER:
			return translator.expression.Expression.POWER;
		case EssenceGlobals.DIVIDE:
			return translator.expression.Expression.DIV;
		case EssenceGlobals.IF:
			return translator.expression.Expression.IF;
		case EssenceGlobals.IFF:
			return translator.expression.Expression.IFF;
		case EssenceGlobals.AND:
			return translator.expression.Expression.AND;
		case EssenceGlobals.OR:
			return translator.expression.Expression.OR;
		case EssenceGlobals.ABS:
			return translator.expression.Expression.ABS;
		case EssenceGlobals.NEGATION:
			return translator.expression.Expression.U_MINUS;
		case EssenceGlobals.NOT:
			return translator.expression.Expression.NEGATION;
		case EssenceGlobals.LEX_GEQ:
			return translator.expression.Expression.LEX_GEQ;
		case EssenceGlobals.LEX_LEQ:
			return translator.expression.Expression.LEX_LEQ;
		case EssenceGlobals.LEX_GREATER:
			return translator.expression.Expression.LEX_GREATER;
		case EssenceGlobals.LEX_LESS:
			return translator.expression.Expression.LEX_LESS;			
			
		default: return oldOperator;
		
		}
		
	}
	
	
	/**
	 * Map an unary expression to the advanced representation.
	 * 
	 * @param oldUnaryExpression
	 * @return
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapUnaryExpression(UnaryExpression oldUnaryExpression) 
	  throws NormaliserException {
		
		switch(oldUnaryExpression.getRestrictionMode()) {
		
		case EssenceGlobals.NEGATION: // unary minus
			return new UnaryMinus(mapExpression(oldUnaryExpression.getExpression()));
		
		case EssenceGlobals.NOT:
			return new Negation(mapExpression(oldUnaryExpression.getExpression()));
			
		case EssenceGlobals.ABS:
			return new AbsoluteValue(mapExpression(oldUnaryExpression.getExpression()));
		
		default : throw new NormaliserException("Unknown unary expression type:"+oldUnaryExpression);	
		}
	 }
	
	
	
	/**
	 * Maps an AtomicExpression to its equivalent advanced expression. Domains of 
	 * decision variables are needed in this context in order to determine if the
	 * atom is a relational or arithmetic entity. 
	 * 
	 * @param oldAtom
	 * @return
	 * @throws NormaliserException
	 */
	protected translator.expression.Expression mapAtomicExpression(AtomicExpression oldAtom) 
		throws NormaliserException {
		
		
		switch(oldAtom.getRestrictionMode()) {
		
		case EssenceGlobals.NUMBER:
			return new ArithmeticAtomExpression(oldAtom.getNumber());
		
		case EssenceGlobals.BOOLEAN:
			return new RelationalAtomExpression(oldAtom.getBool());
			
		case EssenceGlobals.IDENTIFIER: 
			// the identifier is a decision variable
			if(this.decisionVariables.containsKey(oldAtom.getString())) {
				translator.conjureEssenceSpecification.Domain domain = this.decisionVariables.get(oldAtom.getString());
				
				// check if the identifier is an array
				if(domain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN) {
					return mapArray(oldAtom.getString(), domain.getMatrixDomain());
				}
				
				translator.expression.SingleVariable decisionVar = createVariableFromDomain(oldAtom.getString(),
						                                        domain);
				if(decisionVar.getType() == translator.expression.Expression.BOOL_VARIABLE)
					return new RelationalAtomExpression(decisionVar);
				else return new ArithmeticAtomExpression(decisionVar);	
			}
			// the identifier is a parameter
			else if(this.parameterDomains.containsKey(oldAtom.getString())) {
				translator.conjureEssenceSpecification.Domain domain = this. parameterDomains.get(oldAtom.getString());
				translator.expression.SingleVariable parameter = createVariableFromDomain(oldAtom.getString(),
						                                        domain);
				if(parameter.getType() == translator.expression.Expression.BOOL_VARIABLE)
					return new RelationalAtomExpression(parameter);
				else return new ArithmeticAtomExpression(parameter, true);					
				
			} // this might be the binding variable of a quantification
			  // we might want to watch this identifier
			else {
				return new ArithmeticAtomExpression(new SingleVariable(oldAtom.getString(),
						                                               new BoundedIntRange(translator.expression.Expression.LOWER_BOUND, 
						                                            		               translator.expression.Expression.UPPER_BOUND)));
			}
		
			
		default: throw new NormaliserException("Unknown atomic type:"+oldAtom.toString()); 	
		}
	}
	
	
	/**
	 * The identifier with name 'arrayName' has a matrix domain associated to itself,
	 * so it is a matrix of a certain dimension. Hence map it to a SimpleArray.
	 * 
	 * @param arrayName
	 * @param matrixDomain
	 * @return
	 * @throws NormaliserException
	 */
	public SimpleArray mapArray(String arrayName, MatrixDomain matrixDomain) 
		throws NormaliserException {
		
		translator.conjureEssenceSpecification.Domain[] oldIndexDomains = matrixDomain.getIndexDomains();
		BasicDomain[] indexDomains = new BasicDomain[oldIndexDomains.length];
		for(int i=0; i<indexDomains.length; i++) {
			if(oldIndexDomains[i].getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN) 
				throw new NormaliserException("Illegal domain type for array dimension of '"+arrayName+"':"+oldIndexDomains[i]);
			indexDomains[i] = (BasicDomain) mapDomain(oldIndexDomains[i]);
		}
		
		return new SimpleArray(arrayName, indexDomains, mapDomain(matrixDomain.getRangeDomain()));
	}
	
	
	/**
	 * Creates a variable according to the type of the domain. This method is only intended
	 * for single variables, i.e. NO ARRAY variables.
	 * 
	 * @param domain
	 * @param isParameter
	 * @return
	 * @throws NormaliserException
	 */
	public translator.expression.SingleVariable createVariableFromDomain(String variableName,
			                                                       translator.conjureEssenceSpecification.Domain domain) 
		throws NormaliserException {
		
		
		translator.expression.Domain mappedDomain = mapDomain(domain);
		return new SingleVariable(variableName, mappedDomain);
		
	}
	
	
    /**
     * Transforms the old decision variable hashMap into a decision variable hashmap using the
     * advanced domain representation.
     * 
     * @return a decision variable hashmap using the
     * advanced domain representation.
     */
    public HashMap<String,translator.expression.Domain> getNewDecisionVariables(ArrayList<String> decisionVariablesNames) 
    	throws NormaliserException {
    
    	HashMap<String, translator.expression.Domain> newDecisionVariables = new HashMap<String, translator.expression.Domain>();
    	
    	for(int i=0; i<decisionVariablesNames.size(); i++) {
    		String variable = decisionVariablesNames.get(i);
    		if(this.decisionVariables.get(variable) != null)
    			newDecisionVariables.put(variable, mapDomain(this.decisionVariables.get(variable)));
    	}
    	
    	return newDecisionVariables;
    }
	
	
	/**
	 * Maps the old domain representation to the new domain representation (that is used throughout 
	 * the translation process).  
	 * 
	 * @param domain
	 * @return the corresponding domain in the new domain representation
	 * @throws NormaliserException
	 */
	protected translator.expression.Domain mapDomain(translator.conjureEssenceSpecification.Domain domain) 
	throws NormaliserException {
		
		
		switch(domain.getRestrictionMode()) {
		
		case EssenceGlobals.BOOLEAN_DOMAIN:
			return new BoolDomain();
		
		case EssenceGlobals.INTEGER_RANGE:
			return mapIntegerDomain(domain.getIntegerDomain());
			
		case EssenceGlobals.BRACKETED_DOMAIN:
			return mapDomain(domain.getDomain());
			
		case EssenceGlobals.IDENTIFIER_RANGE:
			return new translator.expression.IdentifierDomain(domain.getIdentifierDomain().getIdentifier());
			
		case EssenceGlobals.MATRIX_DOMAIN:
			return mapMatrixDomain(domain.getMatrixDomain());
			
			
		default: throw new NormaliserException("Unknown domain type '"+domain.toString()+ 
		"' or internal error (normalising before insertion of all identifier domains).");
			
		}
		
		
	
	}
	
	
	/**
	 * Maps matrix domains to their representaiton in the advanced domain representation
	 * @param oldMatrixDomain
	 * @return
	 * @throws NormaliserException
	 */
	protected ArrayDomain mapMatrixDomain(MatrixDomain oldMatrixDomain) 
		throws NormaliserException {
		
		translator.expression.Domain baseDomain = mapDomain(oldMatrixDomain.getRangeDomain());
		translator.expression.Domain indexDomains[] = new translator.expression.Domain[oldMatrixDomain.getIndexDomains().length];
		for(int i=0;i< indexDomains.length; i++) {
			indexDomains[i] = mapDomain(oldMatrixDomain.getIndexDomains()[i]);
		}
		
		return new ArrayDomain(baseDomain, indexDomains);
	}
	
	/**
	 * the integer domain must not purely consist of integer values.
	 * 
	 * @param intDomain
	 * @return the corresponding expression.Domain for the conjureEssenceSpecification.Domain
	 * @throws NormaliserException
	 */
	protected translator.expression.Domain mapIntegerDomain(IntegerDomain intDomain) 
		throws NormaliserException {
		
		
		// we have a bounds domain or a single element domain
		if(intDomain.getRangeList().length == 1) {
			translator.expression.Expression lb = mapExpression(intDomain.getRangeList()[0].getLowerBound());
			translator.expression.Expression ub = mapExpression(intDomain.getRangeList()[0].getUpperBound());
			
			if(!(lb instanceof translator.expression.ArithmeticExpression))
				throw new NormaliserException("Unfeasible lower bound '"
						+lb+". Expected an arithmetic expression.");
			
			if(!(ub instanceof translator.expression.ArithmeticExpression))
				throw new NormaliserException("Unfeasible upper bound '"+ub+
						". Expected an arithmetic expression.");
			
			ArithmeticExpression lowerBound = (ArithmeticExpression) lb;
			ArithmeticExpression upperBound = (ArithmeticExpression) ub;
			
			// we have an integer bound
			if(lowerBound.getType() == translator.expression.Expression.INT && 
			   upperBound.getType() == translator.expression.Expression.INT)  {
				
				int lower = ((ArithmeticAtomExpression) lb).getConstant();
				int upper = ((ArithmeticAtomExpression) ub).getConstant();
				
				if(lower > upper) throw new NormaliserException
				 ("Infeasible bound domain: lower bound '"+lower+
						 "' is higher than upper bound '"+upper+"'.");						
				
				else if(lower == upper) 
					return new SparseIntRange(new int[] {lower});
				
				else return new BoundedIntRange(((ArithmeticAtomExpression) lb).getConstant(),
					                        ((ArithmeticAtomExpression) ub).getConstant());
			}
			// we have an expression bound
			else return new BoundedExpressionRange(lowerBound,
     		                                       upperBound);
		}
		
		
		//  we have a sparse domain or mixed domain
		else {
			RangeAtom[] rangeAtom = intDomain.getRangeList();
			ArrayList<Integer> integerRange = new ArrayList<Integer>();
			ArrayList<translator.expression.Expression> expressionRange = new ArrayList<translator.expression.Expression>();
			
			ArrayList<IntRange> intRangeList = new ArrayList<IntRange>();
			ArrayList<ExpressionRange> exprRangeList = new ArrayList<ExpressionRange>();
			
			int i = 0;
			while(i<rangeAtom.length) {
				
				// ------------ either the range atom is sparse -----------------------------------------
				// this is piece of a sparse domain -> collect them all
				while(i<rangeAtom.length && rangeAtom[i].getRestrictionMode() == EssenceGlobals.RANGE_EXPR) {
					translator.expression.Expression sparseElement = mapExpression(rangeAtom[i].getLowerBound());
					
					if(!(sparseElement instanceof translator.expression.ArithmeticExpression))
						throw new NormaliserException("Unfeasible element in sparse domain '"
								+sparseElement+". Expected an integer value.");
					
					// as long as there are no composed range expressions we can separate 
					// pure integer expressions
					if(sparseElement.getType() == translator.expression.Expression.INT &&
							expressionRange.size() == 0 &&
							exprRangeList.size() ==0) 
						integerRange.add( ((ArithmeticAtomExpression) sparseElement).getConstant());
					
					else expressionRange.add(sparseElement);
					
					i++;	
				} // after detecting a set of sparse elements, add them all together into a Int/ExpressionRange
				if(integerRange.size() > 0){
					int[] sparseIntRange = new int[integerRange.size()];
					for(int j=sparseIntRange.length-1; j >=0; j--)
						sparseIntRange[j] = integerRange.remove(j); 
					intRangeList.add(new SparseIntRange(sparseIntRange));
				}
				integerRange.clear();
				
				if(expressionRange.size() > 0) {
					translator.expression.Expression[] sparseIntRange = new translator.expression.Expression[integerRange.size()];
					for(int j=sparseIntRange.length-1; j >=0; j--)
						sparseIntRange[j] = expressionRange.remove(j); 
					exprRangeList.add(new SparseExpressionRange(sparseIntRange));
				}
				expressionRange.clear();
				
				
				if(i== rangeAtom.length)
					break;
				
				// ------------------------ or the range atom consists of a lb and ub ---------------------------
				// now we have to consider the i-th element that is a bounded domain 	
				
				translator.expression.Expression lb = mapExpression(rangeAtom[i].getLowerBound());
				translator.expression.Expression ub = mapExpression(rangeAtom[i].getUpperBound());
				
				if(!(lb instanceof translator.expression.ArithmeticExpression))
					throw new NormaliserException("Unfeasible lower bound '"
							+lb+". Expected a relational expression.");
				
				if(!(ub instanceof translator.expression.ArithmeticExpression))
					throw new NormaliserException("Unfeasible upper bound '"+ub+
							". Expected a relational expression.");
				
				ArithmeticExpression lowerBound = (ArithmeticExpression) lb;
				ArithmeticExpression upperBound = (ArithmeticExpression) ub;
				
				// we have an integer bound
				if(lowerBound.getType() == translator.expression.Expression.INT && 
				   upperBound.getType() == translator.expression.Expression.INT)  {
					
					int lower = ((ArithmeticAtomExpression) lb).getConstant();
					int upper = ((ArithmeticAtomExpression) ub).getConstant();
					
					if(lower > upper) throw new NormaliserException
					 ("Infeasible bound domain: lower bound '"+lower+
							 "' is higher than upper bound '"+upper+"'.");						
					
					else if(lower == upper) 
						intRangeList.add(new SparseIntRange(new int[] {lower}));
					
					else {
					    intRangeList.add(new BoundedIntRange(((ArithmeticAtomExpression) lb).getConstant(),
						                        ((ArithmeticAtomExpression) ub).getConstant()));
					}
				}
				// we have an expression bound
				else exprRangeList.add(new BoundedExpressionRange(lowerBound,
         		                                       upperBound));
				
				i++;
			} // end while i<rangeAtom.length
			
			// there are only integer range atoms
			if(exprRangeList.size() == 0) {
				if(intRangeList.size() == 1) {
					return intRangeList.get(0);
				}
				else return new MultipleIntRange(intRangeList);
			}
			// there are expression and int range atoms
			else {
				// the order is OK here: we only collect integer expressions if
				// there are no composed expression atoms. So if there are some,
				// they need to be put into the front of the range
				for(int j=intRangeList.size()-1; j>=0; j--) {
					exprRangeList.add(0,intRangeList.get(j).toExpressionRange());
				}
				return new MultipleExpressionRange(exprRangeList);
			}
			
		}
	}
	
	
	protected void print_debug(String message) {
		
		this.debug = this.debug.concat(" [ DEBUG expressionMapper ] "+message);
		
		
	}
	
}
