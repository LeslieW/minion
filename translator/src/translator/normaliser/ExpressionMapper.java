package translator.normaliser;

import translator.conjureEssenceSpecification.*;
import translator.expression.*;

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
	HashMap<String, Domain> decisionVariables;
	/** A hashmap containing all parameters with their corresponding domains */
	HashMap<String, Domain> parameters;
	
	
	
	public ExpressionMapper(HashMap<String,Domain> decisionVariables,
			                HashMap<String, Domain> parameters) {
		
		this.decisionVariables = decisionVariables;
		this.parameters = parameters;
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
			
		case EssenceGlobals.UNITOP_EXPR:
			return mapUnaryExpression(oldExpression.getUnaryExpression());
				
		case EssenceGlobals.BINARYOP_EXPR:
			return mapBinaryExpression(oldExpression.getBinaryExpression());
			
		default: 
			throw new NormaliserException("Cannot map expression yet or unknown expression type:"+oldExpression);
				
		}
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
				Domain domain = this.decisionVariables.get(oldAtom.getString());
				translator.expression.Variable decisionVar = createVariableFromDomain(oldAtom.getString(),
						                                        domain);
				if(decisionVar.getType() == translator.expression.Expression.BOOL_VARIABLE)
					return new RelationalAtomExpression(decisionVar);
				else return new ArithmeticAtomExpression(decisionVar);	
			}
			// the identifier is a parameter
			else if(this.parameters.containsKey(oldAtom.getString())) {
				Domain domain = this. parameters.get(oldAtom.getString());
				translator.expression.Variable parameter = createVariableFromDomain(oldAtom.getString(),
						                                        domain);
				if(parameter.getType() == translator.expression.Expression.BOOL_VARIABLE)
					return new RelationalAtomExpression(parameter);
				else return new ArithmeticAtomExpression(parameter, true);					
				
			}
			else throw new NormaliserException("Unknown identifier: "+oldAtom.getString());
		
			
		default: throw new NormaliserException("Unknown atomic type:"+oldAtom.toString()); 	
		}
	}
	
	
	
	/**
	 * Creates a variable according to the type of the domain.
	 * 
	 * @param domain
	 * @param isParameter
	 * @return
	 * @throws NormaliserException
	 */
	public translator.expression.Variable createVariableFromDomain(String variableName,
			                                                       Domain domain) 
		throws NormaliserException {
		
		
		switch(domain.getRestrictionMode()) {
		
		case EssenceGlobals.BOOLEAN_DOMAIN:
			return new translator.expression.Variable(variableName,0,1);
		
		case EssenceGlobals.INTEGER_RANGE:
			IntegerDomain intDomain = domain.getIntegerDomain();
			
			// we have a bounds domain	
			if(intDomain.getRangeList().length == 1) {
				translator.expression.Expression lb = mapExpression(intDomain.getRangeList()[0].getLowerBound());
				translator.expression.Expression ub = mapExpression(intDomain.getRangeList()[0].getUpperBound());
				
				if(!(lb instanceof translator.expression.ArithmeticExpression))
					throw new NormaliserException("Unfeasible lower bound for variable '"
							+variableName+"':"+lb+". Expected a relational expression.");
				
				if(!(ub instanceof translator.expression.ArithmeticExpression))
					throw new NormaliserException("Unfeasible upper bound for variable '"
							+variableName+"':"+ub+". Expected a relational expression.");
				
				return new translator.expression.Variable(variableName, 
						                                  (ArithmeticExpression) lb,
						                                  (ArithmeticExpression) ub);
			}
			//  we have a sparse domain
			else {
				RangeAtom[] rangeAtom = intDomain.getRangeList();
				ArrayList<Integer> sparseDomain = new ArrayList<Integer>();
				
				for(int i=0; i<rangeAtom.length; i++) {
					translator.expression.Expression lb = mapExpression(rangeAtom[i].getLowerBound());
					
					// if the sparse element is not an atom, raise an exception
					if(!(lb instanceof translator.expression.ArithmeticAtomExpression))
						throw new NormaliserException("Unfeasible element in sparse domain for variable '"
								+variableName+"':"+lb+". Expected an integer value.");
					
					ArithmeticAtomExpression sparseElement = (ArithmeticAtomExpression) lb;
					
					//if the sparse element is not an integer, raise and exception (we cannot order it otherwise)
					if(sparseElement.getType() == translator.expression.Expression.INT)
						sparseDomain.add(sparseElement.getConstant());
					else throw new NormaliserException("Unfeasible element in sparse domain for variable '"
							+variableName+"':"+lb+". Expected an integer value.");
				}
				
				return new translator.expression.Variable(variableName,
						                                  sparseDomain);
			}
			
		case EssenceGlobals.BRACKETED_DOMAIN:
			return createVariableFromDomain(variableName, domain.getDomain());
			
		case EssenceGlobals.IDENTIFIER_RANGE:
			throw new NormaliserException("Unknown identifier in domain "+domain.toString()+" of variable '"+variableName+
					"' or internal error (normalising before insertion of all identifier domains).");
			
		
		default: throw new NormaliserException("Unknown identifier in domain "+domain.toString()+" of variable '"+variableName+
		"' or internal error (normalising before insertion of all identifier domains).");
			
		}
		
	}
	
}
