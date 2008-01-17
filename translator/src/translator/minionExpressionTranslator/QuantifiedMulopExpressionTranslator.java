package translator.minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

//import translator.minionModel.MinionBoolVariable;
import translator.minionModel.MinionBoolVariable;
import translator.minionModel.MinionConstant;
import translator.minionModel.MinionConstraint;
import translator.minionModel.MinionDisEqConstraint;
import translator.minionModel.MinionException;
import translator.minionModel.MinionIdentifier;
import translator.minionModel.MinionInEqConstraint;
import translator.minionModel.MinionModel;
import translator.minionModel.MinionReifiableConstraint;
import translator.minionModel.MinionSumConstraint;
import translator.minionModel.MinionSumGeqConstraint;
import translator.minionModel.MinionSumLeqConstraint;
import translator.preprocessor.ExpressionEvaluator;
import translator.preprocessor.Parameters;
import translator.preprocessor.PreprocessorException;
import translator.conjureEssenceSpecification.AtomicExpression;
import translator.conjureEssenceSpecification.Constant;
import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.ExpressionConstant;
import translator.conjureEssenceSpecification.Quantifier;
import translator.conjureEssenceSpecification.QuantificationExpression;
import translator.conjureEssenceSpecification.EssenceGlobals;
import translator.conjureEssenceSpecification.RangeAtom;
//import translator.conjureEssenceSpecification.BinaryExpression;

public class QuantifiedMulopExpressionTranslator extends
		SpecialExpressionTranslator {


	ArrayList<Quantifier> quantifiers;
	/** The binding variables in the ith position of the arrayList belong to the ith quantifier */
	ArrayList<String[]> quantifierVariables;
	/** containts the bounds of each binding variable*/
	HashMap<String, int[]> bindingVariablesBounds;
	/** contains every bindingVariableName with its actual value */
	HashMap<String, Constant> bindingParameters;
	ArrayList<String> bindingVariablesNames;
	int[] bindingVariablesValues;
	/** evaluates expressions */ 
	ExpressionEvaluator evaluator;
	
	
	/** indicates if the currently translated expression is the objective */
	//boolean isObjective;
	boolean isRelationalSumTranslation;
	/** If we translate an objective, we just have a variable */
	MinionIdentifier objectiveVariable;
	
	/** the quantified expression that is translated 
	 *  is composed of "Quantifiers sumexpression RELOP resultExpression" */
	Expression quantifiedExpression;
	Expression sumResult;
	Expression sumExpression;
	int relationalSumOperator;
	
	ArrayList<MinionIdentifier> identifiers;
	
	/*public QuantifiedMulopExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
	HashMap<String, MinionIdentifier[]> minionVecs,
	HashMap<String, MinionIdentifier[][]> minionMatrixz,
	HashMap<String, MinionIdentifier[][][]> minionCubes, 
	ArrayList<String> decisionVarsNames, 
	HashMap<String, Domain> decisionVars, 
	MinionModel mm, boolean useWatchedLiterals, boolean useDiscreteVariables, 
	Parameters parameterArrays) {
		this(minionVars, minionVecs, minionMatrixz, minionCubes, decisionVarsNames, decisionVars, mm, useWatchedLiterals, useDiscreteVariables, parameterArrays, subExpressionCollection);
	}
*/

	/*public QuantifiedMulopExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
	HashMap<String, MinionIdentifier[]> minionVecs,
	HashMap<String, MinionIdentifier[][]> minionMatrixz,
	HashMap<String, MinionIdentifier[][][]> minionCubes, 
	ArrayList<String> decisionVarsNames, 
	HashMap<String, Domain> decisionVars, 
	MinionModel mm, boolean useWatchedLiterals, boolean useDiscreteVariables, 
	Parameters parameterArrays, SubexpressionCollection subExpressionCollection) {
		this(minionVars, minionVecs, minionMatrixz, minionCubes, decisionVarsNames, decisionVars, mm, subExpressionCollection, parameterArrays, useWatchedLiterals, useDiscreteVariables);
	}
*/

	public QuantifiedMulopExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
			HashMap<String, MinionIdentifier[]> minionVecs,
			HashMap<String, MinionIdentifier[][]> minionMatrixz,
			HashMap<String, MinionIdentifier[][][]> minionCubes, 
			ArrayList<String> decisionVarsNames, 
			HashMap<String, Domain> decisionVars, 
			MinionModel mm,
			Parameters parameterArrays, 
			SubexpressionCollection subExpressionCollection, 
			boolean useWatchedLiterals, 
			boolean useDiscreteVariables) {	
		
		super(minionVars, minionVecs, minionMatrixz, minionCubes, decisionVarsNames, decisionVars, 
				mm, parameterArrays, subExpressionCollection, useWatchedLiterals, useDiscreteVariables);
		
		
		this.quantifiers = new ArrayList<Quantifier>();
		this.quantifierVariables = new ArrayList<String[]>();
		this.bindingVariablesBounds = new HashMap<String, int[]> ();
		this.bindingParameters = new HashMap<String,Constant>();
		this.bindingVariablesNames = new ArrayList<String>();
		
		this.evaluator = new ExpressionEvaluator(bindingParameters, parameterArrays);
		
		//this.isObjective = false;
		this.isRelationalSumTranslation = false;
		this.quantifiedExpression = null;
		identifiers = new ArrayList<MinionIdentifier>();
		this.sumResult = null;
	}
	

	/**
	 * Translate a quantified expression that corresponds to a non-relational expression,
	 * i.e. a sum quantification. The translation process will return a variable
	 * representing the (nested) sum. 
	 * 
	 * @param expression
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws PreprocessorException
	 * @throws MinionException
	 */
	public MinionIdentifier translateMulopQuantification(QuantificationExpression expression) 
		throws TranslationUnsupportedException, PreprocessorException, MinionException, PreprocessorException, ClassNotFoundException   {
		
		if(expression.getQuantifier().getRestrictionMode() != EssenceGlobals.SUM) 
			throw new TranslationUnsupportedException
			("Cannot translate a quantification that is not a sum to a non-relational expression:"+expression);
		
		
		// 1. collect all info we need prior translation
		collectQuantificationInfo(expression);
		
		// 2. remove unused binding variables 
		removeUnusedBindingVariables();
		
		// 3. creating binding variables
		this.bindingVariablesValues = new int[this.bindingVariablesNames.size()];
		for(int i=0; i<bindingVariablesValues.length; i++) 
			bindingVariablesValues[i] = this.bindingVariablesBounds.get(bindingVariablesNames.get(i))[0];
		
		// 4. 
		MinionIdentifier identifier = translateMulopQuantification(0);
		if(identifier == null)
			// throw exception? Or can this actually happen????
			throw new TranslationUnsupportedException
			("Internal error: Expected expression '"+this.quantifiedExpression+
					"' to be translated to an identifier representing the expression, but process returned null.");
		
		// 5. reset everything
		clearAll();
		
		return identifier;
	}
	
	/**
	 * Translate a sum of structure sum ( relational expression)
	 * 
	 * @param sumExpression
	 * @param willBeReified TODO
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 */
	public MinionConstraint translateQuantifiedSum(QuantificationExpression sumExpression, boolean willBeReified) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
		
		if(sumExpression.getQuantifier().getRestrictionMode() != EssenceGlobals.SUM)
			throw new TranslationUnsupportedException("Internal error. Expected sum-quantification instead of:"+sumExpression); 
		
		this.isRelationalSumTranslation = true;
		
		// 1. collect all info we need prior translation
		collectQuantificationInfo(sumExpression);
		
		// 2. remove unused binding variables 
		removeUnusedBindingVariables();
		
		// 3. creating binding variables
		this.bindingVariablesValues = new int[this.bindingVariablesNames.size()];
		for(int i=0; i<bindingVariablesValues.length; i++) 
			bindingVariablesValues[i] = this.bindingVariablesBounds.get(bindingVariablesNames.get(i))[0];
		
		Expression expression = sumExpression.getExpression();
		while(expression.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR) {
			expression = expression.getQuantification().getExpression();
		}
		if(expression.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR)
			throw new TranslationUnsupportedException("Illegal sum quantification:"+this.quantifiedExpression+". Expected relation with sum.");
		
		this.sumResult = expression.getBinaryExpression().getRightExpression();
		this.sumExpression = expression.getBinaryExpression().getLeftExpression();
		this.relationalSumOperator = expression.getBinaryExpression().getOperator().getRestrictionMode();
		
		// 4. translate the all the stuff
		MinionConstraint constraint = translateSumQuantification(0, willBeReified);
		if(constraint == null)
			// throw exception? Or can this actually happen????
			throw new TranslationUnsupportedException
			("Internal error: Expected expression '"+this.quantifiedExpression+
					"' to be translated to an identifier representing the expression, but process returned null.");
		
		// 5. reset everything
		clearAll();
		
		
		return constraint;
	}
	
	/**
	 * Translate the Expression quantifierExpression (class member) according to the 
	 * quantifier at depth quantifierPosition. The outmost quantifier has position 0.
	 * 
	 * @param quantifierPosition
	 * @return
	 */
	private MinionIdentifier translateMulopQuantification(int quantifierPosition) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException  {
		
        //we are going to translate all expressions concerning quantifier q for var at bindingVarIndex
		if(quantifiers.get(quantifierPosition).getRestrictionMode() != EssenceGlobals.SUM) 
			throw new TranslationUnsupportedException("Cannot translate a universal or existential quantifier inside a sum, sorry.");
		
		String[] bindingVariables = quantifierVariables.get(quantifierPosition); 
		
		//ArrayList<MinionIdentifier> identifiers = new ArrayList<MinionIdentifier>();
		
		int range = 1;
       // 1. translate each expression according to the range(s) of its binding variable(s)
		for(int j=0; j<bindingVariables.length; j++) {
			int[] bindingBounds = bindingVariablesBounds.get(bindingVariables[j]);
			range = range*(bindingBounds[1] - bindingBounds[0] + 1);
		}
		
		
		// 2. translate the (nested) quantification according to the ranges of the binding variables
		for(int i=0; i<range; i++) {
			
			// 	if this is the inner most quantifier
			if(quantifierPosition == quantifiers.size()-1) {
				MinionIdentifier identifier = translateAtomQuantification(quantifierPosition);
				print_debug("translated Atom quantification... now adding id to list if it's not null");
				if(identifier != null) {
					identifiers.add(identifier);
					print_debug("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW We just added another minionIdentifier to the reifiedVars list !!!!!!!!!");
				}
			}
			else { // we have nested quantification
				MinionIdentifier identifier = translateMulopQuantification(quantifierPosition+1);
				if(identifier==null) 
					print_debug("Translation of MULOP-expr returned zero....."); // the last whole scope might have not been feasible
									
				else {		
					for(int s=quantifierPosition+1; s<quantifiers.size(); s++){
						if(quantifiers.get(s).getRestrictionMode() != EssenceGlobals.SUM)
							throw new TranslationUnsupportedException
							("The sum quantification may not nest universal or existential quantifiers.");
						
					}
					identifiers.add(identifier);
				}
			}
			
		} // end for(range)
		
		//	we have translated the outmost (least nested) quantifier
		if(quantifierPosition == 0) {
		// 	2. we finished translating all the nested stuff, so lets get our MinionIdentifiers
			print_debug("Finished the whole translation process. Let's start constructing the sum...");
			Object[] identis = identifiers.toArray();
			MinionIdentifier[] freshVars = new MinionIdentifier[identis.length];
			print_debug("FreshVars for SUM::::::::::::::::::::::::");
			print_debug("Length of the identis:"+identis.length);
			for(int ii=0; ii<identis.length; ii++) {
				freshVars[ii] = (MinionIdentifier) identis[ii];
				print_debug(ii+": "+freshVars[ii]);
			}
		
		
		// 3. create the sum out of all the MinionIdentifiers
		//TODO: create a better variable with better bounds
		MinionIdentifier sumVariable = variableCreator.addFreshVariable
   		  (MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND, 
				   MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND, 
				   "freshVariable"+this.noTmpVars++,
				   this.useDiscreteVariables);
		
		this.minionModel.addSumConstraint(freshVars,
				    sumVariable,
					this.useWatchedLiterals);
		return sumVariable;
		}
		else return null;
	}
	
	/**
	 * 
	 * @param quantifierPosition
	 * @param willBeReified TODO
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 * @throws ClassNotFoundException
	 */
	private MinionConstraint translateSumQuantification(int quantifierPosition, boolean willBeReified) 
	throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException  {
	
    //we are going to translate all expressions concerning quantifier q for var at bindingVarIndex
	if(quantifiers.get(quantifierPosition).getRestrictionMode() != EssenceGlobals.SUM) 
		throw new TranslationUnsupportedException("Cannot translate a universal or existential quantifier inside a sum, sorry.");
	
	String[] bindingVariables = quantifierVariables.get(quantifierPosition); 
	
	int range = 1;
   // 1. translate each expression according to the range(s) of its binding variable(s)
	for(int j=0; j<bindingVariables.length; j++) {
		int[] bindingBounds = bindingVariablesBounds.get(bindingVariables[j]);
		range = range*(bindingBounds[1] - bindingBounds[0] + 1);
	}
	
	
	// 2. translate the (nested) quantification according to the ranges of the binding variables
	for(int i=0; i<range; i++) {
		
		// 	if this is the inner most quantifier
		if(quantifierPosition == quantifiers.size()-1) {
			MinionIdentifier identifier = translateAtomQuantification(quantifierPosition);
			print_debug("translated Atom quantification... now adding id to list if it's not null");
			if(identifier != null) {
				identifiers.add(identifier);
				print_debug("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW We just added another minionIdentifier to the reifiedVars list !!!!!!!!!");
			}
		}
		else { // we have nested quantification
			MinionIdentifier identifier = translateMulopQuantification(quantifierPosition+1);
			if(identifier==null) 
				print_debug("translation of mulop-expr returned null."); // the last whole scope might have not been feasible
								
			else {		
				for(int s=quantifierPosition+1; s<quantifiers.size(); s++){
					if(quantifiers.get(s).getRestrictionMode() != EssenceGlobals.SUM)
						throw new TranslationUnsupportedException
						("The sum quantification may not nest universal or existential quantifiers.");
					
				}
				identifiers.add(identifier);
			}
		}
	} // end for(range)
	
	// if we finished the whole translation process
	if(quantifierPosition == 0) {
	// 2. we finished translating all the nested stuff, so lets get our MinionIdentifiers
	Object[] identis = identifiers.toArray();
	MinionIdentifier[] freshVars = new MinionIdentifier[identis.length];
	print_debug("FreshVars for SUM::::::::::::::::::::::::");
	for(int ii=0; ii<identis.length; ii++) {
		freshVars[ii] = (MinionIdentifier) identis[ii];
		print_debug(ii+": "+freshVars[ii]);
	}
	
	
	// 3. create the sum out of all the MinionIdentifiers
	//Expression sumExpression = this.quantifiedExpression.getExpression()
	if(this.sumResult == null)	
		throw new TranslationUnsupportedException("Infeasible sum expression:"+this.quantifiedExpression+".");
	
		switch(this.relationalSumOperator) {
	
		case EssenceGlobals.LEQ:
			return new MinionSumLeqConstraint(freshVars, 
                      translateMulopExpression(this.sumResult), useWatchedLiterals && !willBeReified);
		
		case EssenceGlobals.GEQ:
			return new MinionSumGeqConstraint(freshVars, 
					  translateMulopExpression(this.sumResult), useWatchedLiterals && !willBeReified);
	
		case EssenceGlobals.EQ:
			return new MinionSumConstraint(freshVars,translateMulopExpression(this.sumResult), 
					useWatchedLiterals && !willBeReified);
			
		case EssenceGlobals.GREATER:			
			MinionReifiableConstraint c1g = new MinionSumLeqConstraint(freshVars,
			translateMulopExpression(this.sumResult), false);
			MinionBoolVariable freshVar1g = this.reifyConstraint(c1g);
			
			
			MinionReifiableConstraint c2g = new MinionSumGeqConstraint(freshVars,
			translateMulopExpression(this.sumResult), false);
			MinionBoolVariable freshVar2g = this.reifyConstraint(c2g);
			
			return new MinionInEqConstraint(freshVar2g,freshVar1g, new MinionConstant(-1));
			
		case EssenceGlobals.LESS:
			MinionReifiableConstraint c1l = new MinionSumLeqConstraint(freshVars,
			translateMulopExpression(this.sumResult), false);
			MinionBoolVariable freshVar1l = this.reifyConstraint(c1l);
			
			MinionReifiableConstraint c2l = new MinionSumGeqConstraint(freshVars,
			translateMulopExpression(this.sumResult), false);			
			MinionBoolVariable freshVar2l = this.reifyConstraint(c2l);
			
			return new MinionInEqConstraint(freshVar1l,freshVar2l, new MinionConstant(-1));
			
		case EssenceGlobals.NEQ:
			MinionReifiableConstraint c1n = new MinionSumLeqConstraint(freshVars,
			translateMulopExpression(this.sumResult), false);
			MinionBoolVariable freshVar1n = this.reifyConstraint(c1n);
			
			MinionReifiableConstraint c2n = new MinionSumGeqConstraint(freshVars,
			translateMulopExpression(this.sumResult), false);
			MinionBoolVariable freshVar2n = this.reifyConstraint(c2n);
			
			print_debug("Adding disequality betweens sums and freshVar1:"+freshVar1n.getOriginalName()+" and freshVar2: "+freshVar2n.getOriginalName());
			return new MinionDisEqConstraint(freshVar1n,freshVar2n);
		
		default:
			throw new TranslationUnsupportedException
			("Infeasible relation in sum-quantified expression "+this.quantifiedExpression.toString());
		}
	
	
	} // else if we are not at quantificationPosition 0
	else return null;
	
}
	
	/** 
	 * Translate a singly "generated" constraint from a quantification. The constraint has been constructed by
	 * inserting values for all binding variables. 
	 * 
	 * @param quantifierPosition
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 * @throws ClassNotFoundException
	 */
	private MinionIdentifier translateAtomQuantification(int quantifierPosition) 
	  throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
		
		Expression constraint = readNextConstraint();
		if(constraint == null)
			return null;
		else if(this.isRelationalSumTranslation ) {
			if(constraint.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR)
				throw new MinionException("Internal error: expected relational sum expression instead of:"+constraint);
			
			return translateMulopExpression(constraint.getBinaryExpression().getLeftExpression());
		}
		else 
			return translateMulopExpression(constraint);
	}
	
	
	/**
	 * Read the next Constraint using info from the class members. Returns null if there
	 * is no more Constraint to be read.
	 * 
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 */
	private Expression readNextConstraint() 
	throws TranslationUnsupportedException, MinionException, PreprocessorException {
	
		if(bindingVariablesBounds.get(bindingVariablesNames.get(0))[1] < bindingVariablesValues[0]) {
				print_debug("we have reached the upper Bound of the outest binding variable...let's return NULL");
				return null;
		}
	
	Expression generatedExpression = quantifiedExpression.copy();
	
	for(int i=0; i<bindingVariablesValues.length; i++) {
		print_debug("Gonna insert "+bindingVariablesValues[i]+" for "+bindingVariablesNames.get(i));
		generatedExpression = insertValueForIdentifierInExpression(bindingVariablesValues[i], bindingVariablesNames.get(i), generatedExpression);
	}
	
	print_debug("ORIGINAL values of binding variables, before increasing values:");
	for(int i=0; i<bindingVariablesValues.length; i++)
		print_debug("binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesValues[i]);
	
	// increase the binding variables' value
	bindingVariablesValues[bindingVariablesValues.length-1]++;
	
	print_debug("Increased the last bindingVariable again, NOW VARIABLE VALUES are :");
	for(int i=0; i<bindingVariablesValues.length; i++)
		print_debug("binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesValues[i]);
	print_debug("and the upper bounds are:");
	for(int i=0; i<bindingVariablesValues.length; i++) {
		print_debug("upper bound binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesBounds.get(bindingVariablesNames.get(i))[1]);
		print_debug("lower bound binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesBounds.get(bindingVariablesNames.get(i))[0]);
	}
	
	boolean carry = false;
	for(int i=bindingVariablesValues.length-1; i>=0; i--) {
		// if one of the binding Variables has reached its upper bound
		if(bindingVariablesBounds.get(bindingVariablesNames.get(i))[1] < bindingVariablesValues[i]) {
		/*	if(i==0) { // we have read all possible Constraints
				print_debug("we have reached the upper Bound of the outest binding variable...let's return NULL");
				return null;
			}*/
			print_debug("we have reached the upper bound of binding variable "+bindingVariablesNames.get(i));
			bindingVariablesValues[i] = bindingVariablesBounds.get(bindingVariablesNames.get(i))[0];
			carry = true;
		}
		else {
			if(carry) {
				bindingVariablesValues[i]++;
				if(bindingVariablesBounds.get(bindingVariablesNames.get(i))[1] < bindingVariablesValues[i]) {
					print_debug("we have reached the upper bound of binding variable "+bindingVariablesNames.get(i));
					bindingVariablesValues[i] = bindingVariablesBounds.get(bindingVariablesNames.get(i))[0];
					carry = true;
				}
				else 
					carry = false;
			}
		}
	}
	
	print_debug("ADJUSTED the values of the binding variables, now getting :");
	for(int i=0; i<bindingVariablesValues.length; i++)
		print_debug("binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesValues[i]);
	
	
	print_debug("AAAAAAAAAAAAAAYYYYYYYYYY next generated Expression is:"+this.evaluator.evalExpression(generatedExpression).toString());
	return generatedExpression;
	}
	
	
	  /** 
     * do that using a trick: add id to the list of parameters with 
     * value "value". Then evaluate the expression, which will insert
     * "value" for every occurence of "id". Then remove "id" from the
     * parameter list again.
     * 
     * 
     * @param value the int value that should be inserted in e for identifier id
     * @param id the name of the variable represented by a String that should be replaced 
     *        by value
     * @param e the Expression in which the identifier value will be replaced by int value.
     * @return the Expression where every occurence of the identifier id is replaced by value
     * @throws TranslationUnsupportedException
     * @throws MinionException
     */

    public Expression insertValueForIdentifierInExpression(int value, String id, Expression e) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException {

    		Expression buffer_expr = e.copy();
    		print_debug("copied expression:"+buffer_expr.toString());
    		Constant c = new Constant(new ExpressionConstant(id, new Expression(new AtomicExpression(value)) ));
    		print_debug("gonna insert the value "+value+" for this binding variable "+id);
    		bindingParameters.put(id,  c);
    		print_debug("inswerting the value "+value+" for this binding variable "+id);
    		Expression new_expression = evaluator.evalExpression(buffer_expr);
    		print_debug("got the new eexpression:"+new_expression.toString());
    		bindingParameters.remove(id);
    		print_debug("returning the new expression:"+new_expression.toString());
    		return new_expression;
    }

	
	/**
	 * Collect all information necessary prior to generate expressions from
	 * a quantified expression. It is assumed that all expressions and 
	 * domains occurring in the whole expression are fully evaluated and that
	 * every parameter has been substitued by its value. <br>
	 * The following steps are performed:
	 * <ol>
	 *   <li>store the quantifiers in the quantifier stack (the first element of the 
	 *       quantifier list is the most nested quantifier)
	 *   </li>
	 *   <li>store the set of binding variables corresponding to each quantifier
	 *   </li>
	 *   <li>store the (integer!) range of each binding variables (throws an exception if the 
	 *       range is not an integer range)
	 *   </li>
	 *   <li>continue collecting info in case there are nested quantifications 
	 *   </li>
	 * </ol>
	 * 
	 * @param constraint the expression (not necessarily quantified) that is filtered 
	 * 	for information on the quantification
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 */
	
	
	private void collectQuantificationInfo(QuantificationExpression e) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException {
		
	
			// 1. store quantifier in the quantifier queue (FIFO: most nested quantifier is last element) 
			print_debug("Will insert quantifier in the quantifiersList at position:"+quantifiers.size());
			this.quantifiers.add(quantifiers.size(),e.getQuantifier());
			
			// 2. store the set of binding variables corresponding to the quantifier
			String[] bindingVarNames = e.getBindingExpression().getDomainIdentifiers().getIdentifiers();
			print_debug("Will insert quantifierVariables in the quantifiersList at position:"+quantifierVariables.size());
			this.quantifierVariables.add(quantifierVariables.size(), bindingVarNames);
			
			// 3. store binding variables' ranges
			print_debug("About to store binding domains");
			Domain bindingDomain = e.getBindingExpression().getDomainIdentifiers().getDomain();		
			print_debug("binding domain: "+bindingDomain.toString());
			if(bindingDomain.getRestrictionMode() != EssenceGlobals.INTEGER_RANGE) 
				throw new TranslationUnsupportedException
					("Binding variable domain is not an integer domain: "+bindingDomain.toString());			
		
			
			RangeAtom[] bindingBounds = bindingDomain.getIntegerDomain().getRangeList();
			if(bindingBounds[0].getLowerBound().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR &&
			   bindingBounds[0].getUpperBound().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				if(bindingBounds[0].getLowerBound().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER &&
						bindingBounds[0].getUpperBound().getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			
					print_debug("binding bounds[0] : "+bindingBounds[0].toString()+", binding bounds[1] : "+bindingBounds[0].toString());
					int[] bounds = new int[] { bindingBounds[0].getLowerBound().getAtomicExpression().getNumber(),
					                   bindingBounds[0].getUpperBound().getAtomicExpression().getNumber() };	
			
					print_debug("binding bounds[0] : "+bounds[0]+", bounds[1] : "+bounds[1]);
					for(int i=0; i<bindingVarNames.length; i++) {
						print_debug("In loop "+i+", gonna start now...");
						bindingVariablesBounds.put(bindingVarNames[i],bounds);
						print_debug("In loop "+i+", added bounds for bindingVarNames[i]:"+bindingVarNames[i]);
						bindingVariablesNames.add(bindingVarNames[i]);
					}	
				} // if the bounds are numbers
				else throw new TranslationUnsupportedException
				("Cannot translate non-integer bounds '"+bindingBounds[0].toString()+"' yet, sorry.");
			}
			else throw new TranslationUnsupportedException
				("Cannot translate non-integer bounds '"+bindingBounds[0].toString()+"' yet, sorry.");
			print_debug("Inserted bounds in HashMap with bindingVariables as key. Starting to collect further info..");
			// 4. continue recursively if we have nested quantification
			if(e.getExpression().getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR)
				collectQuantificationInfo(e.getExpression().getQuantification());
			else this.quantifiedExpression = e.getExpression();

			print_debug("Binding variables names: "+bindingVariablesNames.toString());
			
				
	}
	
	
	/**
	 * 
	 * @throws PreprocessorException
	 */
	
	private void removeUnusedBindingVariables() 
	    throws PreprocessorException 	{
	
	for(int i=0; i < bindingVariablesNames.size(); i++) {
			print_debug("These are the bindingVarioablesNBames: "+bindingVariablesNames.toString());
			print_debug("CHeking if "+bindingVariablesNames.get(i)+" appears in "+quantifiedExpression.toString());
		// if the binding variable does not appear in the quantified expression
		if(!evaluator.appearsInExpression(bindingVariablesNames.get(i), quantifiedExpression)) {
			String unUsedBindingVariable = bindingVariablesNames.get(i);
			print_message("Binding variable '"+unUsedBindingVariable+"' does not appear in the expression :"+quantifiedExpression.toString());
			
			bindingVariablesNames.remove(i);
			bindingVariablesBounds.remove(unUsedBindingVariable);
			// for every quantifier
			for(int j=0; j<quantifierVariables.size(); j++) {
				// for every binding variable of the quantifier 
				for(int s=0; s<quantifierVariables.get(j).length; s++) {
					
					if(quantifierVariables.get(j)[s].equals(unUsedBindingVariable)) {
						int length = quantifierVariables.get(j).length;
						// if this was not the only binding variable for this quantifier
						if(length >1) {
							String[] newBindingVariables = new String[length-1];
							for(int k=0; k<length-1; k++) {
								if(k<s)
									newBindingVariables[k] = quantifierVariables.get(j)[k];
								else if(k>=s)
									newBindingVariables[k] = quantifierVariables.get(j)[k+1];
							}
							quantifierVariables.remove(j);
							quantifierVariables.add(j,newBindingVariables);
						} // if this was the last binding variable of the quantifier, we can remove it
						else {
							 quantifierVariables.remove(j);
							 quantifiers.remove(j);
						}	
						--i;
						break;
					}
				}
			} // end for each quantifier
			
		} // end if appears In Expression
		
	} // end for i
	
}


	/** 
	 * Reset all info-storage to initial state for the next quantification
	 * to be translated ...
	 */
	 private void clearAll() {  	
	    	bindingVariablesNames.clear();
			bindingParameters.clear();
			quantifierVariables.clear();
			quantifiers.clear();
			bindingVariablesBounds.clear();
			bindingVariablesValues = null;
			quantifiedExpression = null;
			identifiers.clear();
			this.isRelationalSumTranslation = false;
	    } 
	 
	 
	 protected static void print_debug(String s) {
	    	if(DEBUG)
	    		System.out.println("[ DEBUG quantifiedMulopExpressionTranslator ] "+s);
	    }  

	
}
