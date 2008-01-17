package translator.minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import translator.minionModel.*;

import translator.preprocessor.ExpressionEvaluator;
import translator.preprocessor.Parameters;
import translator.preprocessor.PreprocessorException;

import translator.conjureEssenceSpecification.AtomicExpression;
import translator.conjureEssenceSpecification.NonAtomicExpression;
import translator.conjureEssenceSpecification.BinaryExpression;
//import translator.conjureEssenceSpecification.BinaryOperator;
import translator.conjureEssenceSpecification.Constant;
import translator.conjureEssenceSpecification.ExpressionConstant;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.RangeAtom;
import translator.conjureEssenceSpecification.QuantificationExpression;
import translator.conjureEssenceSpecification.Quantifier;
import translator.conjureEssenceSpecification.EssenceGlobals;

public class QuantifierTranslator implements MinionTranslatorGlobals {

	
	ArrayList<Quantifier> quantifiers;
	/** The binding variables in the ith position of the arrayList belong to the ith quantifier */
	ArrayList<String[]> quantifierVariables;
	/** containts the bounds of each binding variable*/
	HashMap<String, int[]> bindingVariablesBounds;
	/** contains every bindingVariableName with its actual value */
	HashMap<String, Constant> bindingParameters;
	ArrayList<String> bindingVariablesNames;
	
	// info about Minion model
	HashMap<String, MinionIdentifier> minionVariables;
	HashMap<String, MinionIdentifier[]> minionVectors;
	HashMap<String, MinionIdentifier[][]> minionMatrices;
	HashMap<String, MinionIdentifier[][][]> minionCubes;
	
	HashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
	Parameters parameterArrays;
	SubexpressionCollection subExpressionCollection;
	
	ExpressionEvaluator evaluator;
	QuantifiedMulopExpressionTranslator translator;
	
	MinionVariableCreator variableCreator;	
	
	// the original model
	MinionModel translatorMinionModel;
	
	// stuff for generating constraints
	int[] bindingVariablesValues;
	int noOfReifiedVariables;
	
	/** the quantified expression that is translated */
	Expression quantifiedExpression;
	
	/** the right part of the actually processed sum quantification */
	Expression sumExpression;
	
	/** indicates if all quantifiers on top-level should be reified */
	boolean reify;
	boolean useWatchedLiterals;
	boolean useDiscreteVariables;
	
	/** indicates if the currently translated expression is the objective */
	boolean isObjective;
	
	/** If we translate an objective, we just have a variable */
	MinionIdentifier objectiveVariable;
	
	/*public QuantifierTranslator(HashMap<String, MinionIdentifier> minionVars,
		HashMap<String, MinionIdentifier[]> minionVecs,
		HashMap<String, MinionIdentifier[][]> minionMatrixz, 
		HashMap<String, MinionIdentifier[][][]> minionCubes, 
		ArrayList<String> decisionVarsNames, 
		MinionModel mm, 
		HashMap<String, Domain> decisionVars, 
		Parameters parameterArrays, boolean useWatchedLiterals, boolean useDiscreteVariables) 
	throws MinionException, ClassNotFoundException {
		this(minionVars, minionVecs, minionMatrixz, minionCubes, decisionVarsNames, mm, decisionVars, parameterArrays, subExpressionCollection, useWatchedLiterals, useDiscreteVariables);
	}

*/


	public QuantifierTranslator(HashMap<String, MinionIdentifier> minionVars,
				HashMap<String, MinionIdentifier[]> minionVecs,
				HashMap<String, MinionIdentifier[][]> minionMatrixz, 
				HashMap<String, MinionIdentifier[][][]> minionCubes, 
				ArrayList<String> decisionVarsNames, 
				MinionModel mm, 
				HashMap<String, Domain> decisionVars, 
				Parameters parameterArrays, SubexpressionCollection subExpressionCollection, boolean useWatchedLiterals, boolean useDiscreteVariables) 
			throws MinionException, ClassNotFoundException {
		
		this.quantifiers = new ArrayList<Quantifier>();
		this.quantifierVariables = new ArrayList<String[]>();
		this.bindingVariablesBounds = new HashMap<String, int[]> ();
		this.bindingParameters = new HashMap<String,Constant>();
		this.bindingVariablesNames = new ArrayList<String>();
		
		this.minionVariables = minionVars;
		this.minionVectors = minionVecs;
		this.minionMatrices = minionMatrixz;
		this.minionCubes = minionCubes;
		this.decisionVariables = decisionVars;
		this.decisionVariablesNames = decisionVarsNames;
		this.translatorMinionModel = mm;
		this.parameterArrays = parameterArrays;
		this.subExpressionCollection = subExpressionCollection;
		
		this.evaluator = new ExpressionEvaluator(bindingParameters, parameterArrays);
		this.noOfReifiedVariables =0;
		
		this.translator = new QuantifiedMulopExpressionTranslator(minionVars, //new HashMap<String, MinionIdentifier>(),
				minionVecs, //new HashMap<String, MinionIdentifier[]>(),
				minionMatrixz, //new ArrayList<String>(),
				minionCubes, 
				 decisionVarsNames,
				decisionVars, 
				translatorMinionModel, 
				parameterArrays, 
				subExpressionCollection, 
				useWatchedLiterals, 
				useDiscreteVariables);
		
		this.variableCreator = new MinionVariableCreator(minionVariables,
				 minionVectors,
				 minionMatrices,
				 minionCubes,
				 decisionVariables,
				 decisionVariablesNames, translatorMinionModel, useDiscreteVariables);
		
		this.reify = false;
		this.sumExpression = null;
		this.useWatchedLiterals = useWatchedLiterals;
		this.useDiscreteVariables = useDiscreteVariables;
		this.isObjective = false;
	}
	
	

	
	public void translate(QuantificationExpression e) 
	     throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
		
		if(e.getQuantifier().getRestrictionMode() == EssenceGlobals.SUM) {
			print_debug("SUMMMMM constraint go to the translator:"+e);
			this.translatorMinionModel.addConstraint(this.translator.translateQuantifiedSum(e, false));
			return;
		}
		
		// 1. collect info
		collectQuantificationInfo(e);
		
		// 2. remove unused binding variables 
		removeUnusedBindingVariables();
		
		print_debug("removed unused binding variables.");
		// 3. set the values of every binding value to its lower bound
		bindingVariablesValues = new int[bindingVariablesNames.size()];
		for(int i=0; i<bindingVariablesValues.length; i++) 
			bindingVariablesValues[i] = bindingVariablesBounds.get(bindingVariablesNames.get(i))[0];
		
		print_debug("Checking if it is a quantified assignment:"+e.toString());
		// 4a. we have something like v[i] = m[0,i] and create a vector referencing to the elements of m
		if(isQuantifiedAssignment(e)) {
			print_debug("is a quantified assignment: "+e.toString());
			//translateQuantifiedAssignment(e);
			
			// 1. generate an empty matrix for the left expression (according to its domain)
			addEmptyAssignedMatrix(e); 
		}
		//else {
		// 	4b. translate quantification, starting with quantifier_0
			MinionConstraint constraint = translateQuantification(0);
			if(constraint != null){
				translatorMinionModel.addConstraint(constraint);
			}
			print_debug("Have translated the quantified expression.");
		//}	
		clearAll();
	}
	
	
	
	public MinionConstraint translate(Expression e, boolean expressionWillBeReified) 
	  throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
		
		this.reify = expressionWillBeReified;
		
		switch(e.getRestrictionMode()) {
		
		case EssenceGlobals.QUANTIFIER_EXPR:
			
			if(e.getQuantification().getQuantifier().getRestrictionMode() == EssenceGlobals.SUM) {
				return this.translator.translateQuantifiedSum(e.getQuantification(), expressionWillBeReified);	
			}
			
			//	1. collect info
			collectQuantificationInfo(e.getQuantification());
			
			// 2. check if all binding variables occur in expressions (and quantifiers may become redundant)
			removeUnusedBindingVariables();
			
			// 3. set the values of every binding value to its lower bound
			bindingVariablesValues = new int[bindingVariablesNames.size()];
			for(int i=0; i<bindingVariablesValues.length; i++) 
				bindingVariablesValues[i] = bindingVariablesBounds.get(bindingVariablesNames.get(i))[0];
	
			// translate quantification, starting with quantifier_0
			MinionReifiableConstraint constraint = translateQuantification(0);
			clearAll();
			return constraint;
		
		case EssenceGlobals.BINARYOP_EXPR:
			return translateBinaryQuantifiedExpression(e.getBinaryExpression(), expressionWillBeReified);
			
		default:
			
			return translator.translateSpecialExpression(e, expressionWillBeReified);
		
		}	
		
	}

	
	/**
	 * 
	 * @param expression
	 * @param expressionWillBeReified
	 * @return
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 */
	private MinionConstraint translateBinaryQuantifiedExpression (BinaryExpression expression, boolean expressionWillBeReified)
	    throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException {
		
		print_debug("Translating binary quantified expression:"+expression);
		
		int operator = expression.getOperator().getRestrictionMode();
		Expression rightExpression = expression.getRightExpression();
		Expression leftExpression = expression.getLeftExpression();
		MinionIdentifier rightPart = null;
		MinionIdentifier leftPart = null;
		
		while(rightExpression.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR)
			rightExpression = rightExpression.getExpression();
		
		while(leftExpression.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR)
			leftExpression = leftExpression.getExpression();
		
		print_debug("starting the translation of a binary quantified expression:*********"+expression);
		print_debug("rightExpression:"+rightExpression);
		print_debug("leftExpression: "+leftExpression);
				
		
		//		 translate left part of the binary expression
	     if(!translator.containsQuantification(leftExpression)){
	    	 print_debug("The left expression doesnot contain a quantification");
	    	 
			if(this.translator.isRelationalExpression(leftExpression)) {
				leftPart = (MinionIdentifier) translator.reifyConstraint((MinionReifiableConstraint) 
						                                   translator.translateSpecialExpression(leftExpression,true));
			} else
				leftPart = translator.translateMulopExpression(leftExpression);
			
        // the expression contains quantifications
		} else { 
			print_debug("The left expression contains a quantification");
			if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				print_debug("translating binary right expression:"+leftExpression);
				leftPart = (MinionIdentifier) translator.reifyConstraint((MinionReifiableConstraint) 
						                                   translateBinaryQuantifiedExpression(leftExpression.getBinaryExpression(),true));
			}
			else if(leftExpression.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR) {
				print_debug("translating quantified right expression:"+leftExpression);
				
				if(this.translator.isRelationalExpression(leftExpression)) {
					MinionConstraint constraint = this.translate(leftExpression, true); // true: will be reified
					if(constraint == null)
						throw new MinionException("Reified translation returned null instead of constraint for expression:"+leftExpression);
					leftPart = translator.reifyConstraint((MinionReifiableConstraint) constraint);
				} 
				else 
					leftPart = translator.translateMulopQuantification(leftExpression.getQuantification());
			}// unary expression?
			else if(leftExpression.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) 
				leftPart = translator.translateUnaryExpression(leftExpression.getUnaryExpression());
			// there is no other possibility (at the moment)
		}
		
	     
		
		// translate right part of the binary expression
	     if(!translator.containsQuantification(rightExpression)){
	    	 print_debug("The right expression does not contain a quantification:"+rightExpression);
			if(this.translator.isRelationalExpression(rightExpression)) {
				rightPart = (MinionIdentifier) translator.reifyConstraint((MinionReifiableConstraint) 
						                                   translator.translateSpecialExpression(rightExpression,true));
			} else
				rightPart = translator.translateMulopExpression(rightExpression);
			
         // the expression contains quantifications
		} else {
			print_debug("The right expression contains a quantification:"+rightExpression);
			
			
			
			if(rightExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				print_debug("translating binary right expression:"+rightExpression);
				rightPart = (MinionIdentifier) translator.reifyConstraint((MinionReifiableConstraint) 
						                                   translateBinaryQuantifiedExpression(rightExpression.getBinaryExpression(),true));
			}
			else if(rightExpression.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR) {
				print_debug("translating quantified right expression:"+rightExpression);
				
				if(this.translator.isRelationalExpression(rightExpression)) {
					print_debug("RELATIONAL EXPRESSION, part quantified:"+rightExpression);
					MinionConstraint constraint = this.translate(rightExpression, true); // true: will be reified
					if(constraint == null)
						throw new MinionException("Reified translation returned null instead of constraint for expression:"+rightExpression);
					rightPart = translator.reifyConstraint((MinionReifiableConstraint) constraint);
				} 
				else {
					print_debug("NON_RELATIONAL expression:"+rightExpression);
					rightPart = translator.translateMulopQuantification(rightExpression.getQuantification());
				}
			}// unary expression?
			else if(rightExpression.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR) 
				rightPart = translator.translateUnaryExpression(rightExpression.getUnaryExpression());
			// there is no other possibility (at the moment)
		}
	
		print_debug("translated right expression:"+rightExpression);	
		
		
		if(leftPart == null)
			throw new TranslationUnsupportedException("Cannot translate nested expression :"+leftExpression);
		if(rightPart == null)
			throw new TranslationUnsupportedException("Cannot translate nested expression :"+rightExpression);
		
		
		print_debug("NNNNNNNNNNNNOOOOOWWW starting translating the rest according to the opertars");
		
		switch(operator) {
		
		case EssenceGlobals.EQ:
			return new MinionEqConstraint(leftPart,rightPart);
		
		case EssenceGlobals.NEQ:
			if(!expressionWillBeReified)
				return new MinionDisEqConstraint(leftPart,rightPart);
			else { // cannot reify diseq yet
				MinionBoolVariable reifiedVar = translator.reifyConstraint((MinionReifiableConstraint) new MinionEqConstraint(leftPart,rightPart));
				return new MinionEqConstraint(reifiedVar, new MinionConstant(0));
			}
				
		case EssenceGlobals.LEQ:
			if(!expressionWillBeReified)
				return new MinionInEqConstraint(leftPart,rightPart,new MinionConstant(0));
			else  // watched literals: we will reify it and watched lit sums are not reifiable yet 
				return new MinionSumLeqConstraint(new MinionIdentifier[] {leftPart}, rightPart, this.useWatchedLiterals && false);
			
		case EssenceGlobals.GEQ:
			if(!expressionWillBeReified)
				return new MinionInEqConstraint(rightPart,leftPart,new MinionConstant(0));
			else  // watched literals: we will reify it and watched lit sums are not reifiable yet 
				return new MinionSumGeqConstraint(new MinionIdentifier[] {leftPart}, rightPart, this.useWatchedLiterals && false);
			
		case EssenceGlobals.LESS:
			if(!expressionWillBeReified)
				return new MinionInEqConstraint(leftPart,rightPart,new MinionConstant(-1));
			else  // watched literals: we will reify it and watched lit sums are not reifiable yet
				  // r < l     =    r+1 <= l
				return new MinionSumLeqConstraint(new MinionIdentifier[] {leftPart, new MinionConstant(1)}, rightPart, 
						                                                         this.useWatchedLiterals && false);
		case EssenceGlobals.GREATER:
			if(!expressionWillBeReified)
				return new MinionInEqConstraint(rightPart,leftPart, new MinionConstant(-1));
			else { // r > l    =   r+1 <= l
				return new MinionSumGeqConstraint(new MinionIdentifier[] {leftPart, new MinionConstant(1)}, rightPart, 
                        this.useWatchedLiterals && false);
			}
			
		case EssenceGlobals.AND:
			if(!expressionWillBeReified) {
				return new MinionProductConstraint(leftPart,rightPart, new MinionConstant(1));
			}
			else { // r /\ l   =   r + l = 2   (both should be booleans)
				return new MinionSumConstraint(new MinionIdentifier[] {leftPart, rightPart}, new MinionConstant(2),
						          this.useWatchedLiterals && false);
			}
				
		case EssenceGlobals.OR:
			return new MinionSumGeqConstraint(new MinionIdentifier[] {leftPart, rightPart}, 
											  new MinionConstant(1), 
											  this.useWatchedLiterals && !expressionWillBeReified);	
			
		case EssenceGlobals.IF: // r => l     =     r <= l
			if(!expressionWillBeReified)
				return new MinionInEqConstraint(leftPart, rightPart, new MinionConstant(0));
			else return new MinionSumLeqConstraint(new MinionIdentifier[] {leftPart}, rightPart, false);
			
		case EssenceGlobals.IFF:
			return new MinionEqConstraint(leftPart,rightPart);
		
		
		default:
			throw new TranslationUnsupportedException("Cannot translate constraint '"+expression+"' because of operator :"+expression.getOperator());
		}
	}
	
	
	
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
			quantifiers.add(quantifiers.size(),e.getQuantifier());
			
			// 2. store the set of binding variables corresponding to the quantifier
			String[] bindingVarNames = e.getBindingExpression().getDomainIdentifiers().getIdentifiers();
			print_debug("Will insert quantifierVariables in the quantifiersList at position:"+quantifierVariables.size());
			quantifierVariables.add(quantifierVariables.size(), bindingVarNames);
			
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
			else quantifiedExpression = e.getExpression();

			print_debug("Binding variables names: "+bindingVariablesNames.toString());
			
				
	}
	
	
	/**
	 * Translate the quantified expression according to quantifier at position quantifierposition in
	 * the quantifier list. The quantifier at the beginning of the list, is the outest one and the last quantifier
	 * the most nested one.
	 * 
	 * @param quantifierPosition
	 * @return the MinionReifiableConstraint that results from applying the quantifier. In case the constraint is null, there is no need for 
	 * imposing reification. If yes, then some kind of reification has to be done. If the constraint is null, it has already been inserted
	 * into the minionModel
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 * @throws PreprocessorException
	 * @throws ClassNotFoundException
	 */
	
	
	private MinionReifiableConstraint translateQuantification(int quantifierPosition) 
		throws TranslationUnsupportedException, MinionException, PreprocessorException, ClassNotFoundException{
		
		// we are going to translate all expressions concerning quantifier q for var at bindingVarIndex
		Quantifier q = quantifiers.get(quantifierPosition);
		String[] bindingVariables = quantifierVariables.get(quantifierPosition); 
		
		ArrayList<MinionIdentifier> identifiers = new ArrayList<MinionIdentifier>();
		
		int range = 1;
		
		// translate each expression according to the range(s) of its binding variable(s)
		for(int j=0; j<bindingVariables.length; j++) {
			int[] bindingBounds = bindingVariablesBounds.get(bindingVariables[j]);
			
			range = range*(bindingBounds[1] - bindingBounds[0] + 1);
			
			//int range = bindingBounds[1] - bindingBounds[0] + 1;
			//print_debug("range of quantifier at position "+quantifierPosition+" is "+range);
			//print_debug("PROCESSING bindingVariable "+bindingVariables[j]);
		}
		for(int i=0; i<range; i++) {
			// 	if this is the inner most quantifier
			//	print_debug("PROCESSING bindingVariable "+bindingVariables[j]+"with current value of range:"+i);
			if(quantifierPosition == quantifiers.size()-1) {
				MinionIdentifier id = translateAtomQuantification(quantifierPosition);
				print_debug("translated Atom quantification... now adding id to list if it's not null");
				if(id != null) { // we need reification 
					print_debug("Gonna add the identifier to the list of identifiers.");
					identifiers.add(id);
					print_debug("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW We just added another minionIdentifier to the reifiedVars list !!!!!!!!!");
				}
			}
			else { // we have nested quantification
				MinionReifiableConstraint constraint = translateQuantification(quantifierPosition+1);
				if(constraint==null) print_debug("THE WHOLE SCOPE WAS INFEASIBLE!!!!!!!! of the "+q.toString()+"-quantifier."); // the last whole scope might have not been feasible
									
				else {		
					MinionBoolVariable reifiedVar = new MinionBoolVariable(1,"freshVariable"+this.translator.noTmpVars++);
								//translatorMinionModel.add01Variable(reifiedVar);
					switch(q.getRestrictionMode()) {
								
					case EssenceGlobals.EXISTS:
						print_debug("TRanslating an EXXXXXXXXXXXISTENTIAL quantifier");
						translatorMinionModel.addReificationConstraint(constraint, reifiedVar);
						identifiers.add(reifiedVar);
						break;
								
					case EssenceGlobals.FORALL:
						print_debug("TRanslating a UUUUUUUUUUUUUUUUUNIVERSAL quantifier");
						if(needsReification(quantifierPosition)) {
							translatorMinionModel.addReificationConstraint(constraint, reifiedVar);
							identifiers.add(reifiedVar);
						}
						else translatorMinionModel.addConstraint(constraint);
						break;
						
					case EssenceGlobals.SUM:
						for(int s=quantifierPosition+1; s<quantifiers.size(); s++){
							if(quantifiers.get(s).getRestrictionMode() != EssenceGlobals.SUM)
								throw new TranslationUnsupportedException
								("The sum quantification may not nest universal or existential quantifiers.");
						}
								
						//	throw new TranslationUnsupportedException
						//("Double nested Sum quantifications are not supported yet, sorry.");
					}
				}
			}
			//}
		}
		
		print_debug("FIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIINISHED with one variable scope, hehe.");
		
		switch(q.getRestrictionMode()) {
		
		case EssenceGlobals.EXISTS:
			// return a maximum constraint
			print_debug("translatin an EXISTS quantifier and adding aq max-constraint from identifier-array:"+identifiers.toString());
			if(identifiers.size() == 0)
				return null;
			Object[] idents = identifiers.toArray();
			MinionIdentifier[] freshVariables = new MinionIdentifier[idents.length];
			for(int ii=0; ii<idents.length; ii++) {  //the identifiers have been added to at model during reification
				freshVariables[ii] = (MinionIdentifier) idents[ii];
				//translatorMinionModel.addIdentifier(freshVariables[ii]);
			}
			return new //MinionMaxConstraint(freshVariables,new MinionConstant(1));
			       MinionSumGeqConstraint(freshVariables, new MinionConstant(1), false);
			
			
		case EssenceGlobals.FORALL:
			//return a minimum constraint if the identifiers are not empty
			//return nothing otherwise
			if(identifiers.size() ==0) {
				print_debug("We DOOOOOOOOOOOON't need reification, yipiiieee!");
				return null;
			}
			else {		
				print_debug("IIIIIIIIIIIIIIIIIIIIIIIidentifiers (to reifiy) size is :"+identifiers.size());
				Object[] identis = identifiers.toArray();
				MinionIdentifier[] freshVars = new MinionIdentifier[identis.length];
				for(int ii=0; ii<identis.length; ii++) {
					freshVars[ii] = (MinionIdentifier) identis[ii];
					//translatorMinionModel.addIdentifier(freshVars[ii]);
				}
				return new //MinionMinConstraint(freshVars,new MinionConstant(1));
				   MinionSumGeqConstraint(freshVars, new MinionConstant(freshVars.length), false);
			}
		case EssenceGlobals.SUM:
			print_debug("Translating a sum quantification.........................................");
			if(identifiers.size() ==0) return null; // should not actually happen!
			else {		
				Object[] identis = identifiers.toArray();
				MinionIdentifier[] freshVars = new MinionIdentifier[identis.length];
				print_debug("FreshVars for SUM::::::::::::::::::::::::");
				for(int ii=0; ii<identis.length; ii++) {
					freshVars[ii] = (MinionIdentifier) identis[ii];
					print_debug(ii+": "+freshVars[ii]);
				}
				if(sumExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
				
					switch(sumExpression.getBinaryExpression().getOperator().getRestrictionMode()) {
				
					case EssenceGlobals.LEQ:
						return new MinionSumLeqConstraint(freshVars, 
		                          translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), useWatchedLiterals && !reify);
					
					case EssenceGlobals.GEQ:
						return new MinionSumGeqConstraint(freshVars, 
								  translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), useWatchedLiterals && !reify);
				
					case EssenceGlobals.EQ:
						return new MinionSumConstraint(freshVars,translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), useWatchedLiterals && !reify);
						
					case EssenceGlobals.GREATER:
						//MinionBoolVariable freshVar1g = new MinionBoolVariable(1, "freshVariable"+noOfReifiedVariables++);
						//translatorMinionModel.add01Variable(freshVar1g);
						MinionReifiableConstraint c1g = new MinionSumLeqConstraint(freshVars,
								translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), false);
						//translatorMinionModel.addReificationConstraint(c1g,freshVar1g);
						MinionBoolVariable freshVar1g = this.translator.reifyConstraint(c1g);
						
						//MinionBoolVariable freshVar2g = new MinionBoolVariable(1, "freshVariable"+noOfReifiedVariables++);
						//translatorMinionModel.add01Variable(freshVar2g);
						MinionReifiableConstraint c2g = new MinionSumGeqConstraint(freshVars,
								translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), false);
						//translatorMinionModel.addReificationConstraint(c2g,freshVar2g);
						MinionBoolVariable freshVar2g = this.translator.reifyConstraint(c2g);
						return new MinionInEqConstraint(freshVar2g,freshVar1g, new MinionConstant(-1));
						
					case EssenceGlobals.LESS:
						//MinionBoolVariable freshVar1l = new MinionBoolVariable(1, "freshVariable"+noOfReifiedVariables++);
						//translatorMinionModel.add01Variable(freshVar1l);
						MinionReifiableConstraint c1l = new MinionSumLeqConstraint(freshVars,
								translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), false);
						//translatorMinionModel.addReificationConstraint(c1l,freshVar1l);
						MinionBoolVariable freshVar1l = this.translator.reifyConstraint(c1l);
						
						//MinionBoolVariable freshVar2l = new MinionBoolVariable(1, "freshVariable"+noOfReifiedVariables++);
						//translatorMinionModel.add01Variable(freshVar2l);
						MinionReifiableConstraint c2l = new MinionSumGeqConstraint(freshVars,
								translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), false);
						//translatorMinionModel.addReificationConstraint(c2l,freshVar2l);
						MinionBoolVariable freshVar2l = this.translator.reifyConstraint(c2l);
						return new MinionInEqConstraint(freshVar1l,freshVar2l, new MinionConstant(-1));
						
					case EssenceGlobals.NEQ:
						//MinionBoolVariable freshVar1n = new MinionBoolVariable(1, "freshVariable"+noOfReifiedVariables++);
						//translatorMinionModel.add01Variable(freshVar1n);
						MinionReifiableConstraint c1n = new MinionSumLeqConstraint(freshVars,
								translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), false);
						//translatorMinionModel.addReificationConstraint(c1n,freshVar1n);
						MinionBoolVariable freshVar1n = this.translator.reifyConstraint(c1n);
						
						//MinionBoolVariable freshVar2n = new MinionBoolVariable(1, "freshVariable"+noOfReifiedVariables++);
						//translatorMinionModel.add01Variable(freshVar2n);
						MinionReifiableConstraint c2n = new MinionSumGeqConstraint(freshVars,
								translator.translateMulopExpression(sumExpression.getBinaryExpression().getRightExpression()), false);
						//translatorMinionModel.addReificationConstraint(c2n,freshVar2n);
						MinionBoolVariable freshVar2n = this.translator.reifyConstraint(c2n);
						print_debug("Adding disequality betweens sums and freshVar1:"+freshVar1n.getOriginalName()+" and freshVar2: "+freshVar2n.getOriginalName());
						return new MinionDisEqConstraint(freshVar1n,freshVar2n);
					
					default:
						throw new TranslationUnsupportedException
						("Infeasible relation in sum-quantified expression "+sumExpression.toString());
					}
				}
				// else : sumExpression != binary expression
				else if(this.isObjective) {
					// TODO: create a better variable with better bounds
					MinionIdentifier objectiveVar = variableCreator.addFreshVariable
			   		  (MinionTranslatorGlobals.INTEGER_DOMAIN_LOWER_BOUND, 
							   MinionTranslatorGlobals.INTEGER_DOMAIN_UPPER_BOUND, 
							   "freshVariable"+this.translator.noTmpVars++,
							   this.useDiscreteVariables);
					
					this.translatorMinionModel.addSumConstraint(freshVars,
							    objectiveVar,
								this.useWatchedLiterals);
					this.objectiveVariable = objectiveVar;
					return null;
				}
				else throw new TranslationUnsupportedException("Infeasible quantified sum expression: "+sumExpression);
			}
			

		
		default:
				throw new TranslationUnsupportedException
				("Unknown quantification type of "+q.toString());
			
		}
		
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
		
		
		if(!isFeasibleExpression(constraint)) {
			print_debug("The expression is NOOOOOOT feasible: "+constraint.toString());
			return null;
		}
		else if(equalsBooleanTrue(constraint)) {
			return null;
		}
		
		constraint = translator.removeAtomicSubExpressions(constraint);
		boolean reifiable = true;
		
		print_debug("The expression is feasible: "+constraint.toString());
		
		switch(quantifiers.get(quantifierPosition).getRestrictionMode()) {
		
		case EssenceGlobals.FORALL:
			if(needsReification(quantifierPosition)) {
				reifiable = true;
				print_debug("The expression quantified by forall has to be REIFIED!!!!!!!!");
				//MinionBoolVariable reifiedVar = new MinionBoolVariable(1, "freshVariable"+this.translator.noTmpVars++);
				MinionReifiableConstraint reifiableConstraint = null;
				if(hasOtherQuantifications(constraint)) {
					print_debug("THere are OOOOOOOOOOTHER quantified constraints, but nested... in"+constraint.toString()); 
					QuantifierTranslator quantifierTranslator = new QuantifierTranslator(minionVariables, minionVectors,
										minionMatrices, minionCubes,decisionVariablesNames, translatorMinionModel, decisionVariables, parameterArrays, subExpressionCollection, useWatchedLiterals, useDiscreteVariables);
					reifiableConstraint = (MinionReifiableConstraint) quantifierTranslator.translate(constraint, reifiable);
				}
				else 
				 reifiableConstraint = (MinionReifiableConstraint) translator.translateSpecialExpression(constraint,reifiable);
						 															//this.evaluator.evalExpression(constraint), reifiable);
				MinionBoolVariable reifiedVar = this.translator.reifyConstraint(reifiableConstraint);
				//translatorMinionModel.addReificationConstraint(reifiableConstraint, reifiedVar);
				return reifiedVar;
			} // we don't need reification
			else { 	
				reifiable = false;
				print_debug("The expression quantified by forall DOES NOT have to be REIFIED!!!!!!!!");
				MinionConstraint minionConstraint = null;
			  if(hasOtherQuantifications(constraint)) {
				print_debug("THere are OOOOOOOOOOTHER quantified constraints, but nested... in"+constraint.toString()); 
				QuantifierTranslator quantifierTranslator = new QuantifierTranslator(minionVariables, minionVectors,
									minionMatrices, minionCubes,decisionVariablesNames, translatorMinionModel, decisionVariables, parameterArrays, subExpressionCollection, useWatchedLiterals, useDiscreteVariables);
				minionConstraint = quantifierTranslator.translate(constraint, reifiable);
						//this.evaluator.evalExpression(constraint), reifiable);
				if(minionConstraint != null)
					translatorMinionModel.addConstraint(minionConstraint);
			  }
			  else {
				  //translatorMinionModel.addConstraint(minionConstraint);
				  ArrayList<Expression> exprs = splitExpressionToConstraints(constraint);
				  print_debug("split constraint :"+constraint.toString()+" to expressions:"+exprs.toString());
				  for(int index=0; index<exprs.size(); index++) {
					  minionConstraint = translator.translateSpecialExpression((exprs.get(index)), reifiable);
					  //this.evaluator.evalExpression(exprs.get(index)), reifiable);
					  if(minionConstraint != null)
						  translatorMinionModel.addConstraint(minionConstraint);
				  }
			  }
			  
			  print_debug("GONNNNNNNa return null now, be cause we need not reify.");
			  return null;
		    }
		
		
		case EssenceGlobals.EXISTS:
			reifiable = true;
			//MinionBoolVariable reifiedVar = new MinionBoolVariable(1, "freshVariable"+this.translator.noTmpVars++);
			MinionReifiableConstraint reifiableConstraint = null;
			if(hasOtherQuantifications(constraint)) {
				print_debug("THere are OOOOOOOOOOTHER quantified constraints, but nested... in"+constraint.toString()); 
				QuantifierTranslator quantifierTranslator = new QuantifierTranslator(minionVariables, minionVectors,
									minionMatrices, minionCubes,decisionVariablesNames, translatorMinionModel, decisionVariables, parameterArrays, subExpressionCollection, useWatchedLiterals, useDiscreteVariables);
				reifiableConstraint = (MinionReifiableConstraint) quantifierTranslator.translate(constraint, reifiable);
			}
			else 
			  reifiableConstraint = (MinionReifiableConstraint) translator.translateSpecialExpression(constraint, reifiable);
			
			MinionBoolVariable reifiedVar = this.translator.reifyConstraint(reifiableConstraint);
				print_debug("translated reified part of exists constraint");
				print_debug("The constraint is:"+reifiableConstraint);
			//translatorMinionModel.addReificationConstraint(reifiableConstraint, reifiedVar);
			print_debug("Refied the constraint to a variable that we return now.");
			//if(reifiedVar == null)
				//print_debug("And the reified variable is null");
			return reifiedVar;
			
			
			
		case EssenceGlobals.SUM:
			if(constraint.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			
				print_debug("translating sum constraint atom:"+constraint);
				
				if(constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.EQ ||
						constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.NEQ ||
						constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.LEQ ||
						constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.GEQ ||
						constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.LESS ||
						constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.GREATER)  {
					
					this.sumExpression = constraint;
					MinionIdentifier identifier = translator.translateMulopExpression(constraint.getBinaryExpression().getLeftExpression());
					
					// this means that the identifier is not known to the minionModel
					//if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR &&
					 //  constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) {
				//			translatorMinionModel.addIdentifier(identifier);
				//	}
					return identifier;
				}
			
				else throw new TranslationUnsupportedException
				("Unfeasible sum expression: "+constraint.toString()+". Needs to contain a relational operator (=,>=,<=,<,>,!=).");
	
			}
			else if(this.isObjective) {
				this.sumExpression = constraint;
				MinionIdentifier identifier = translator.translateMulopExpression(constraint);
				return identifier;
				
			}
			else throw new TranslationUnsupportedException
			("Unfeasible sum expression: "+constraint.toString()+". Needs to be a binary expression.");
			
		default:
			throw new TranslationUnsupportedException
			("Unknown quantification type '"+quantifiers.get(quantifierPosition).toString()+"' in "  +constraint.toString());
			
		}
		
		
	}
	
	
	private boolean needsReification(int quantifierPosition) {
		
		if(this.reify)
			return true;
		
		boolean needsReification = false;
		
		if(quantifiers.get(quantifierPosition).getRestrictionMode() == EssenceGlobals.EXISTS)
			needsReification = true;
		
		if(quantifierPosition >0) {
			for(int i=quantifierPosition; i>=0; i--) {
				print_debug("Iterating through quantifiers to find exists-qs.looking at quantifier in pos "+i+", which is"+quantifiers.get(i));
				if(quantifiers.get(i).getRestrictionMode() == EssenceGlobals.EXISTS)
					needsReification = true;
			}
		}
		return needsReification;
		
	}
	
	
	/**
	 * Returns true, if the Expression constraint corresponds to the Boolean "true".
	 * 
	 * @param constraint
	 * @return
	 */
	public boolean equalsBooleanTrue(Expression constraint) {
		
		if(constraint.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			if(constraint.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
				boolean booleanValue = constraint.getAtomicExpression().getBool();
				return booleanValue;
			}
		}
		return false;
	}
	
	
	private boolean hasOtherQuantifications(Expression e) {
		
		if(e.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			return hasOtherQuantifications(e.getBinaryExpression().getLeftExpression()) ||
			       hasOtherQuantifications(e.getBinaryExpression().getRightExpression());
		}
		else if(e.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR)
			return true;
		
		else if(e.getRestrictionMode() == EssenceGlobals.UNITOP_EXPR)
			return hasOtherQuantifications(e.getUnaryExpression().getExpression());
		
		else if(e.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR)
			return hasOtherQuantifications(e.getExpression());
		
		return false;
	}
	
	/**
	 * 
	 * @return the next Expression generated from the quantified expression
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
		/*print_debug("and the upper bounds are:");
		for(int i=0; i<bindingVariablesValues.length; i++) {
			print_debug("upper bound binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesBounds.get(bindingVariablesNames.get(i))[1]);
			print_debug("lower bound binding Variable "+bindingVariablesNames.get(i)+"= "+bindingVariablesBounds.get(bindingVariablesNames.get(i))[0]);
		}*/
		
		Expression e = this.evaluator.evalExpression(generatedExpression);
		print_debug("AAAAAAAAAAAAAAYYYYYYYYYY next generated Expression is:"+e);
		return e;//generatedExpression;
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
     * 
     * Determine if the expression (or a relational subexpression) is false
     * when it is completely evaluated. <br>
     * E.g.  3 < 2  /\  x[2] = 4 <br>
     * is evaluated to false, because 3 < 2 is false. But x[2] = 4 would be
     * evaluated to true, since it can not be proven wrong. But since it is 
     * disjuncted with 3 < 2, the whole expression is evaluated to false.
     * 
     * @param e the Expression that is checked for being true or false if completly evaluated
     * @return true if the Expression e cannot be proven to be false if completly evaluated
     * @throws PreprocessorException
     */
    protected boolean isFeasibleExpression(Expression e) 
		throws PreprocessorException {

	   if(e.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {

		   Expression e_left = evaluator.evalExpression(e.getBinaryExpression().getLeftExpression());
		   Expression e_right = evaluator.evalExpression(e.getBinaryExpression().getRightExpression());

		   if(e_left.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) 
			   e_left = e_left.getExpression();
		   if(e_right.getRestrictionMode() == EssenceGlobals.BRACKET_EXPR) 
			   e_right = e_right.getExpression();

		   if((e_left.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) && 
				   (e_right.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR)) {

			   if(e_left.getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER ||
					   e_right.getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
				   return true;
			   
			   AtomicExpression expr_left = e_left.getAtomicExpression();
			   AtomicExpression expr_right = e_right.getAtomicExpression();

			   switch(e.getBinaryExpression().getOperator().getRestrictionMode()) {
		    
			   case EssenceGlobals.EQ :		
				   return (expr_left.getNumber() == expr_right.getNumber());	
		    
			   case EssenceGlobals.NEQ :
				   return (expr_left.getNumber() != expr_right.getNumber());
		    
			   case EssenceGlobals.GEQ :
				   return (expr_left.getNumber() >= expr_right.getNumber());
		    
			   case EssenceGlobals.LEQ :
				   return (expr_left.getNumber() <= expr_right.getNumber());
		    
			   case EssenceGlobals.GREATER :
				   return (expr_left.getNumber() > expr_right.getNumber());
		    
			   case EssenceGlobals.LESS :
				   return (expr_left.getNumber() < expr_right.getNumber());
		    
			   default:
				   return (isFeasibleExpression(new Expression(expr_left)) && isFeasibleExpression(new Expression(expr_right)));
		    
			   }
		   }
		   return (isFeasibleExpression(e_left) && isFeasibleExpression(e_right));
	   }

	   else if(e.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		   // in case we have boolean atoms
		   if(e.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN)  
			   return 	e.getAtomicExpression().getBool();
	    
		   return true;
	   }	
	
	   else return true;
   	}


    /** 
	* Try to split constraint into sub-constraints, like 
	* (x != y ) /\ (y + 3 >= 0) /\ (x - 4 > 0) into the 3 constraints
	* (x != y ) (y + 3 >= 0) (x - 4 > 0)  
	* in order  to reduce the amount of reification and fresh variables
	*	
 	* @param Expression constraint the constraint will be split up into 
	* subconstraints (if possible)
	* @return ArrayList[] the list of Expressions, that constraint could 
	* be split into 
	*
    */
    
    private ArrayList<Expression> splitExpressionToConstraints(Expression constraint) 
	throws TranslationUnsupportedException {
	
    	ArrayList<Expression> constraintList = new ArrayList<Expression>();

	// 	we can only split constraints, if we have and AND on top of the syntax tree
    	if(constraint.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
    		if(constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.AND) {
		
    			Expression left = constraint.getBinaryExpression().getLeftExpression();	     

    			if(left.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
    				constraintList = splitExpressionToConstraints(left);
    				constraintList.add(constraint.getBinaryExpression().getRightExpression());
    				return constraintList;
		}	    
    			else {	
    				constraintList.add(constraint.getBinaryExpression().getLeftExpression());
    				constraintList.add(constraint.getBinaryExpression().getRightExpression());
    				return constraintList;
    			}
    			
    		}	
    	}
	
	// 	we cannot split the constraint since there is no AND at top-level of the constraint
    	constraintList.add(constraint) ;
	

    	return constraintList;
    }
    
    /**
     * Returns true, if the expression that is quantified is of the form v[i] = m[0,i] that corresponds to an assignment
     * of a vector/matrix to another vector/matrix. In this case we can then (later) add a corresponding vector, that is
     * linked to the elements of the other matrix instead of imposing equality constraints. 
     * @param e 
     * @return
     * @throws TranslationUnsupportedException
     */
    
    private boolean isQuantifiedAssignment(QuantificationExpression e) 
    	throws TranslationUnsupportedException {
    	// iterate to innermost expression
    	while(e.getExpression().getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR)
    		e = e.getExpression().getQuantification();
    	
    	Expression constraint = e.getExpression();
    	if(constraint.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
    		if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR &&
    				constraint.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR &&
    				constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.EQ) {
    			NonAtomicExpression leftMatrix = constraint.getBinaryExpression().getLeftExpression().getNonAtomicExpression();
    			NonAtomicExpression rightMatrix = constraint.getBinaryExpression().getLeftExpression().getNonAtomicExpression();
    			
    			print_debug("non-atomic op nonatomic:"+e.toString());
    			
    			if(leftMatrix.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
    				throw new TranslationUnsupportedException("Please access matrix elements by m[i,j] instead of m[i][j], as in "+leftMatrix.toString());
    			if(rightMatrix.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
    				throw new TranslationUnsupportedException("Please access matrix elements by m[i,j] instead of m[i][j], as in "+rightMatrix.toString());
    			if(leftMatrix.getExpression().getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER)
    				throw new TranslationUnsupportedException("Invalid matrix element:"+leftMatrix.toString());
      			if(rightMatrix.getExpression().getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER)
    				throw new TranslationUnsupportedException("Invalid matrix element:"+rightMatrix.toString());
    			
    			String leftMatrixName = leftMatrix.getExpression().getAtomicExpression().getString();
    			if(!decisionVariablesNames.contains(leftMatrixName))
    				return false; 
    			if(minionVectors.containsKey(leftMatrixName))
    				return false;
    			else if(minionMatrices.containsKey(leftMatrixName))
    				return false;
    			else return true;
    		}
    		else if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR &&
    				(constraint.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR ||
    			     constraint.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) &&
    				constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.EQ) {
    			
    			AtomicExpression leftMatrix = constraint.getBinaryExpression().getLeftExpression().getAtomicExpression();
    			AtomicExpression rightExpression = constraint.getBinaryExpression().getRightExpression().getAtomicExpression();
    			
    			if(leftMatrix.getRestrictionMode() != EssenceGlobals.IDENTIFIER)
    				return false;
    			
     			if(rightExpression.getRestrictionMode() != EssenceGlobals.IDENTIFIER)
    				return false;
       			
    			if(!decisionVariablesNames.contains(rightExpression.getString()))
    				return false;
    			
    			if(decisionVariables.get(rightExpression.getString()).getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN ||
    					decisionVariables.get(leftMatrix.getString()).getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN) 
    				return false;
     			
    			
    			String leftMatrixName = leftMatrix.getString();
    			
    			if(!decisionVariablesNames.contains(leftMatrixName)) 
    				return false;
    			
    			if(minionVariables.containsKey(leftMatrixName))
    				return false;
    			
    			if(!minionVectors.containsKey(leftMatrixName) && !minionMatrices.containsKey(leftMatrixName))
    				return true;
    		}
    		else if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR &&
        				constraint.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR &&
        				constraint.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.EQ) {
        			NonAtomicExpression leftMatrix = constraint.getBinaryExpression().getLeftExpression().getNonAtomicExpression();
        			AtomicExpression rightExpression = constraint.getBinaryExpression().getRightExpression().getAtomicExpression();
        			
        			if(rightExpression.getRestrictionMode() != EssenceGlobals.IDENTIFIER)
        				return false;
        			
        			if(leftMatrix.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
        				throw new TranslationUnsupportedException("Please access matrix elements by m[i,j] instead of m[i][j], as in "+leftMatrix.toString());
        
        			if(leftMatrix.getExpression().getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER)
        				throw new TranslationUnsupportedException("Invalid matrix element:"+leftMatrix.toString());
           			
        			String leftMatrixName = leftMatrix.getExpression().getAtomicExpression().getString();
        			
        			if(!decisionVariablesNames.contains(rightExpression.getString()))
        				return false;
        			
        			if(decisionVariables.get(rightExpression.getString()).getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN) 
        				return false;
        			
        			if(!decisionVariablesNames.contains(leftMatrixName))
        				return false; 
        			if(minionVectors.containsKey(leftMatrixName))
        				return false;
        			else if(minionMatrices.containsKey(leftMatrixName))
        				return false;
        			else return true;
        		}
    		
    	}
    	
    	return false;
    }
    
    
    /**
     * Add an empty Matrix: is part of the assignment (variable reuse) translation. We have an expression
     * quantification (atom EQ atom) that corresponds to an assignment of one decision variable to another.
     * If the left expression (the one that something is assigned to), then we can create a new empty
     * matrix and during the quantification translation just place the assigned identifier in the empty matrix.
     * (empty spaces are filled afterwards)     
     *  
     * @param e
     * @throws TranslationUnsupportedException
     */
    private void addEmptyAssignedMatrix(QuantificationExpression e) 
    	throws TranslationUnsupportedException , MinionException//, ClassNotFoundException, PreprocessorException {
    	{
    	while(e.getExpression().getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR)
    		e = e.getExpression().getQuantification();
    	
    	Expression constraint = e.getExpression();
    	
    	if(constraint.getRestrictionMode() != EssenceGlobals.BINARYOP_EXPR) 
    		throw new TranslationUnsupportedException
    			("Internal error. Trying to create empty matrix from expression that is not binary:"+constraint.toString());
    	
    	if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
    		// if this expression is a decision variable and does not exist in the MinionModel, then add 
    		// if over the VariableCreator!
    		AtomicExpression leftAtom = constraint.getBinaryExpression().getLeftExpression().getAtomicExpression();
    		if(leftAtom.getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
    			String variableName = leftAtom.getString();
    			if(decisionVariables.containsKey(variableName))
    				variableCreator.addEmptyMatrix(variableName);
    			else throw new TranslationUnsupportedException
    				("Unknown variable :"+variableName);
    			
    		}
    		else throw new TranslationUnsupportedException
    			("Cannot assign a value/decision variable to a constant '"+leftAtom+"' in:"+e.toString());
    	}
    	else if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
    		NonAtomicExpression leftMatrixElement = constraint.getBinaryExpression().getLeftExpression().getNonAtomicExpression();
    		
    		if(leftMatrixElement.getExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
    			if(leftMatrixElement.getExpression().getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
    	   			String variableName = leftMatrixElement.getExpression().getAtomicExpression().getString();
        			if(decisionVariables.containsKey(variableName))
        				variableCreator.addEmptyMatrix(variableName);
        			else throw new TranslationUnsupportedException
        				("Unknown variable :"+variableName);
    			}
    			else throw new TranslationUnsupportedException
    			("Please access matrix elements by m[i,j,k] instead of m[i][j][k], as in:"+leftMatrixElement.toString());	
    		}
    		else throw new TranslationUnsupportedException
    			("Please access matrix elements by m[i,j,k] instead of m[i][j][k], as in:"+leftMatrixElement.toString());
    	}
    	else // we made a mistake!! should we rather just skip it or through an exception?
    	throw new TranslationUnsupportedException
			("Internal error. Trying to create empty matrix from expression that is not an atom:"+constraint.toString());
    }
    
    
    /**
     * 
     * @param e
     * @throws TranslationUnsupportedException
     * @throws MinionException
     */
    /*private void translateQuantifiedAssignment(QuantificationExpression e) 
    	throws TranslationUnsupportedException, MinionException, ClassNotFoundException, PreprocessorException {
    	
       	while(e.getExpression().getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR)
    		e = e.getExpression().getQuantification();
    	
       	Expression constraint = e.getExpression();
       	boolean leftIsVector = false;
       	boolean leftIsMatrix = false;
       	String leftExpressionName = null;
       	String rightExpressionName = null;
       	MinionIdentifier[] originalVector = null;
       	MinionIdentifier[][] originalMatrix = null;
       	int vectorOffset = 0;
       	int elemOffset = 0;
       	int leftVectorIndex = -1; // or better -1
       	int leftElementIndex = -1;
       	String leftVectorIndexName = null;
       	String leftElementIndexName = null;

       	
       	// ------------------ 1. first get the name of the new (left) matrix/vector -------------------------------------------         
       	if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
       		AtomicExpression leftExpression	= constraint.getBinaryExpression().getLeftExpression().getAtomicExpression();
	    
       		if(leftExpression.getRestrictionMode() != EssenceGlobals.IDENTIFIER) 
       			throw new TranslationUnsupportedException("Internal error: trying to translate an assignment expression "+constraint.toString()
							  +", where one argument is not an identifier:"+leftExpression.toString());
	    
       		leftExpressionName = leftExpression.getString();

       		Domain leftExpressionDomain = decisionVariables.get(leftExpressionName);
       		if(leftExpressionDomain.getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN)
       			throw new TranslationUnsupportedException
       			("Cannot assign vector '"+constraint.getBinaryExpression().getRightExpression()+"' to: "
       					+constraint.getBinaryExpression().getLeftExpression());
	    
       		print_debug("The left exp[ression is a MMMMMMMMMMmaaaatrix domain");
       		if(leftExpressionDomain.getMatrixDomain().getIndexDomains().length == 1) {
       			leftIsVector = true;
       			Domain elementDomain = leftExpressionDomain.getMatrixDomain().getIndexDomains()[0];
       			print_debug("THHHHHHHHHHHHHHHHis is the element Domain:"+elementDomain.toString());
       			if(elementDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
       				elemOffset = elementDomain.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
       				print_debug("WWWWWWWWWWWWWWWWWW element offset is :"+elemOffset);
	    	}
	    }
	    else if(leftExpressionDomain.getMatrixDomain().getIndexDomains().length == 2) {
	    	leftIsMatrix = true;
	    	
	    	Domain elementDomain = leftExpressionDomain.getMatrixDomain().getIndexDomains()[1];
	    	if(elementDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) 
	    		elemOffset = elementDomain.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
	    	Domain vectorDomain = leftExpressionDomain.getMatrixDomain().getIndexDomains()[0];
	    	if(vectorDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) 
	    		vectorOffset = elementDomain.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
	    }
	    else throw new TranslationUnsupportedException
		     ("Multi-dimensional matrices (more than 2 dimensions), as "+leftExpressionName+" are not supported yet, sorry.");
       	}
       	
       	
       	
       	else if(constraint.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
	    NonAtomicExpression leftExpression = constraint.getBinaryExpression().getLeftExpression().getNonAtomicExpression();
	    
	    leftExpressionName = leftExpression.getExpression().getAtomicExpression().getString(); 
	    
	    Domain leftExpressionDomain = decisionVariables.get(leftExpressionName);
	    print_debug("looking at domain of "+leftExpressionName+" which is:"+leftExpressionDomain.toString());
	    
	    if(leftExpressionDomain.getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN)
		throw new TranslationUnsupportedException
		    ("Cannot assign vector/matrix '"+constraint.getBinaryExpression().getRightExpression()+"' to (simple variable) "
		     +constraint.getBinaryExpression().getLeftExpression());
	    
	    // the domain of the left variable is a vector
	    if(leftExpressionDomain.getMatrixDomain().getIndexDomains().length == 1) {

	     	print_debug("THis ia a vectoooooooooooooooooooooor:"+leftExpression.toString());
	    	leftIsVector = true;
	    	Domain elementDomain = leftExpressionDomain.getMatrixDomain().getIndexDomains()[0];
	    	print_debug("THHHHHHHHHHHHHHHHis is the element Domain:"+elementDomain.toString());
	    	if(elementDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
	    		elemOffset = elementDomain.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
	    		print_debug("WWWWWWWWWWWWWWWWWW element offset is :"+elemOffset);
	    	}
	    	
		// we assign elements element-wise to the vector v[ ??] = RIGHT
		if(leftExpression.getExpressionList().length ==1) {

		    if(leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {

			if(leftExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) 
			    leftElementIndex = leftExpression.getExpressionList()[0].getAtomicExpression().getNumber();

			else if(leftExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER)
			    leftElementIndexName = leftExpression.getExpressionList()[0].getAtomicExpression().getString();

		    }
		    else if(leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			throw new TranslationUnsupportedException
			    ("Sorry, cannot translate assignment constraints with bionary expressions in their indices:"+constraint.toString());	
		    }
		    else throw new TranslationUnsupportedException("Please access matrix elements by m[i,j] instead of m[i][j], as in "+leftExpression.toString());

		    leftIsVector = true;
		}
		else throw new TranslationUnsupportedException
			 ("Infeasible index of vector in "+leftExpression);
	    }
	    // the domain of the letf variable is 2-dimensional
	    else if(leftExpressionDomain.getMatrixDomain().getIndexDomains().length == 2) {
	    	
	    	print_debug("We have a matrix here: "+leftExpressionDomain.toString());
	    	leftIsMatrix = true;
	    	Domain elementDomain = leftExpressionDomain.getMatrixDomain().getIndexDomains()[1];
	    	print_debug("got element domain:"+elementDomain.toString());
	    	
	    	if(elementDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
	    		print_debug("We have an integer range:"+elementDomain.getIntegerDomain().toString());
	    		elemOffset = elementDomain.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
	    	}
	    	print_debug("Element offset is:"+elemOffset);
	    	
	    	Domain vectorDomain = leftExpressionDomain.getMatrixDomain().getIndexDomains()[0];
	    	print_debug("got vector domain:"+vectorDomain.toString());
	    	if(vectorDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) 
	    		vectorOffset = elementDomain.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
	    	
	    	print_debug("the left expression is a matrix:"+leftExpression.toString());

		print_debug("The length of the left expression list: "+leftExpression.getExpressionList().length);
		
		// matrix[ ??? ] = RIGHT
		if(leftExpression.getExpressionList().length ==1) {
		    if(leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			if(leftExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			    leftIsVector = true;
			    leftElementIndex = leftExpression.getExpressionList()[0].getAtomicExpression().getNumber();
			}
			else if (leftExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
			    print_debug("We have an matrix[identifier] :"+leftExpression.toString());
			    leftIsMatrix = true;
			    leftVectorIndexName = leftExpression.getExpressionList()[0].getAtomicExpression().getString();
			}
			else if(leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			    // TODO!!!!!!!
			    throw new TranslationUnsupportedException
				("Sorry, cannot translate assignment constraints with bionary expressions in their indices:"+constraint.toString());
			}
			else throw new TranslationUnsupportedException
				 ("Internal error, expected atomic or binary expression in index of left expression in assignment constraint:"+constraint.toString());
			
			
		    }else if(leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			// TODO!!!!!!!
			throw new TranslationUnsupportedException
			    ("Sorry, cannot translate assignment constraints with binary expressions in their indices:"+constraint.toString());
		    }			
		    
		    
		    else throw new TranslationUnsupportedException
			       ("Internal error, expected atomic or binary expression in index of left expression in assignment constraint:"+constraint.toString());
		    
		    
		} // end if leftExpression.getExpressionList().length ==1
		else if(leftExpression.getExpressionList().length == 2) {

		    print_debug("matrix[a,b]:"+leftExpression.toString());

		    if(leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {

			// matrix[int, ???]
			if(leftExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			    if(leftExpression.getExpressionList()[1].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				// matrix[int, int] = RIGHT
				if(leftExpression.getExpressionList()[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) 
				    throw new TranslationUnsupportedException("Assigning vector or matrix to a single element of matrix: "+e.toString());
				    
				// matrix[int, identifier] = LEFT
				else if (leftExpression.getExpressionList()[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
				    leftVectorIndex = leftExpression.getExpressionList()[0].getAtomicExpression().getNumber();
				    leftElementIndexName = leftExpression.getExpressionList()[1].getAtomicExpression().getString();
				    leftIsVector = true;
				}
				else throw new TranslationUnsupportedException
					 ("Cannot translate index that is neither an identifier nor a number in index of left expression in:"+e.toString());

			    }
			    else if (leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR)
				throw new TranslationUnsupportedException
				    ("Sorry, cannot translate assignment constraints with binary expressions in their indices:"+constraint.toString());
			    else throw new TranslationUnsupportedException
				     ("Internal error, expected atomic or binary expression in index of left expression in assignment constraint:"+constraint.toString());  

			} // matrix[identifier, ???] = RIGHT
			else if(leftExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
			    leftVectorIndexName = leftExpression.getExpressionList()[0].toString();
			    
			    print_debug("left expression is a matrix of the form m[identifier, ??] :"+leftExpression.toString());

			    if(leftExpression.getExpressionList()[1].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				// matrix[identifier, int] = RIGHT
				if(leftExpression.getExpressionList()[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
				    leftElementIndex = leftExpression.getExpressionList()[1].getAtomicExpression().getNumber();
				    leftIsVector = true;
				}				    
				// matrix[identifier, identifier] = LEFT
				else if (leftExpression.getExpressionList()[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
				    print_debug("left expression is a matrix of the form m[identifier, ident] :"+leftExpression.toString());
				    leftElementIndexName = leftExpression.getExpressionList()[1].getAtomicExpression().getString();
				    leftIsMatrix = true;
				}
				else throw new TranslationUnsupportedException
					 ("Cannot translate index that is neither an identifier nor a number in index of left expression in:"+e.toString());

			    }
			    else if (leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR)
				throw new TranslationUnsupportedException
				    ("Sorry, cannot translate assignment constraints with binary expressions in their indices:"+constraint.toString());
			    else throw new TranslationUnsupportedException
				     ("Internal error, expected atomic or binary expression in index of left expression in assignment constraint:"+constraint.toString());  

			}

		    }
		    else if (leftExpression.getExpressionList()[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			throw new TranslationUnsupportedException
			    ("Sorry, cannot translate assignment constraints with binary expressions in their indices:"+constraint.toString());
		    }
		    else throw new TranslationUnsupportedException
			     ("Internal error, expected atomic or binary expression in index of left expression in assignment constraint:"+constraint.toString());
		}
		else throw new TranslationUnsupportedException
			 ("Multi-dimensional matrices (more than 2 dimensions), as "+leftExpressionName+" are not supported yet, sorry.");
		
	    }
	    else throw new TranslationUnsupportedException
		     ("Multi-dimensional matrices (more than 2 dimensions), as "+leftExpressionName+" are not supported yet, sorry.");
	    
	} else {
	    MinionConstraint c = translateQuantification(0);
	    if(c != null){
		translatorMinionModel.addConstraint(c);
	    }
	    return;	    
	}


       	print_debug("Now we know all about the left expression: "+constraint.getBinaryExpression().toString());	
       	// ------------------------  2. get the minionIdentifier vector or matrix ---------------------------------------------
       	
       	// LEFT = ATOMIC
       	if(constraint.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
	    rightExpressionName = constraint.getBinaryExpression().getRightExpression().getAtomicExpression().getString();
	    if(decisionVariablesNames.contains(rightExpressionName)) {
		if(!minionVectors.containsKey(rightExpressionName) && !minionMatrices.containsKey(rightExpressionName))
		    variableCreator.addNewVariable(rightExpressionName);
	    }
	    else throw new TranslationUnsupportedException("Internal error. Expected (known) decision variable instead of :"+rightExpressionName.toString()+
							   ", in assignment constraint:"+constraint.toString());
	    
	    if(minionVectors.containsKey(rightExpressionName)) {
	    	originalVector = minionVectors.get(rightExpressionName);
	    	//elemOffset = translatorMinionModel.getVectorOffset(rightExpressionName);
	    }
	    else if(minionMatrices.containsKey(rightExpressionName)) {
		originalMatrix = minionMatrices.get(rightExpressionName);
		//vectorOffset = translatorMinionModel.getMatrixOffsets(rightExpressionName)[0];
		//elemOffset = translatorMinionModel.getMatrixOffsets(rightExpressionName)[1];
	    }
	    else throw new TranslationUnsupportedException("Internal error. Expected matrix or vector instead of :"+rightExpressionName.toString()+
							   ", in assignment constraint:"+constraint.toString());
       	}
       	
       	// LEFT = NONATOMIC[ ..]
       	else if(constraint.getBinaryExpression().getRightExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
	    NonAtomicExpression rightExpression	= constraint.getBinaryExpression().getRightExpression().getNonAtomicExpression();
	    
	    if(rightExpression.getExpression().getRestrictionMode()!= EssenceGlobals.ATOMIC_EXPR) 
		throw new TranslationUnsupportedException("Please access matrix elements by m[i,j] instead of m[i][j], as in "+rightExpression.toString());
	    
	    print_debug("Decision variables are:"+decisionVariablesNames.toString());

	    rightExpressionName = rightExpression.getExpression().getAtomicExpression().getString();

	    if(decisionVariablesNames.contains(rightExpressionName)) {
		print_debug("Right Expression has not been added yet, so let's add it!");
		if(!minionVectors.containsKey(rightExpressionName) && !minionMatrices.containsKey(rightExpressionName))
		    variableCreator.addNewVariable(rightExpressionName);
	    }
	   

	    Expression[] indexList = rightExpression.getExpressionList();
	    
	    // the original is either a vector or a matrix
	    // LEFT = a [ index ]
	    if(indexList.length == 1) {
		// the index must not be a number, except when it's a matrix
		
		// the original is a matrix
		if(minionMatrices.containsKey(rightExpressionName)) {
		    if(indexList[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			// LEFT = matrix[int]
			if(indexList[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER){
			    int vectorIndex = indexList[1].getAtomicExpression().getNumber();   
			    int[] offset = translatorMinionModel.getMatrixOffsets(rightExpressionName);
			    originalVector = minionMatrices.get(rightExpressionName)[vectorIndex - offset[0]];	
			    //elemOffset = translatorMinionModel.getMatrixOffsets(rightExpressionName)[1];	
			}
			// LEFT = matrix[identifier]
			else if(indexList[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER){
			    // if the identifier is a binding variable - hurray!
			    String vectorIndex = indexList[0].getAtomicExpression().getString();
			    if(bindingVariablesBounds.containsKey(vectorIndex)) {
				int[] bounds = bindingVariablesBounds.get(vectorIndex);
				MinionIdentifier[][] matrix = minionMatrices.get(rightExpressionName);
				int[] offset = translatorMinionModel.getMatrixOffsets(rightExpressionName);
				bounds[0] = bounds[0] - offset[0];
				bounds[1] = bounds[1] - offset[0];
				int length = bounds[1] - bounds[0];
				//vectorOffset = offset[0];
				//elemOffset = offset[1];
				if(leftIsMatrix) {
				    if(leftVectorIndexName != null){
					if(bindingVariablesBounds.containsKey(leftVectorIndexName)) {
					    // matrix1[id] = matrix2[id]
					    if(leftVectorIndexName.equals(vectorIndex)) {

						if(bounds[0]<bounds[1]) {
						    if(matrix.length >= bounds[1]) {
							originalMatrix = new MinionIdentifier[length][matrix[0].length];
							for(int i=0; i<length; i++) {	
							    originalMatrix[i] = matrix[i+bounds[0]];
							}
						    }
						    else throw new TranslationUnsupportedException
							     ("Index out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
						}
						else throw new TranslationUnsupportedException
							 ("Upper bound must be greater than lower bound of binding variable "+vectorIndex);
						
					    } else { // matrix1[id1] = matrix2[id2]

						throw new TranslationUnsupportedException
						    ("Cannot translate expression "+e.toString()+" yet, because the variables have different bounds.");
					    }
					    
					} else throw new TranslationUnsupportedException("Unknown variable index in "+constraint.toString()+" in :"+e.toString());
				    }
				    else throw new TranslationUnsupportedException
					 ("Wrong dimensions: Cannot assign matrix to non-matrix expression with fixed vector index:"
					  +constraint.toString()+" in :"+e.toString());
				}
				else throw new TranslationUnsupportedException
					 ("Wrong dimensions: Cannot assign matrix to non-matrix expression:"+constraint.toString()+" in :"+e.toString());

			    }
			    else throw new TranslationUnsupportedException
				     ("Expected binding variable or integer index instead of:"+vectorIndex+", during translation of assignment expression:"
				      +e.toString());
			}
			else throw new TranslationUnsupportedException("Internal error, expected atomic or binary expression in assignment constraint:"+constraint.toString());
			
		    }// end if AtomicExpression
		    // LEFT = matrix[e1 op e2]
		    else if(indexList[0].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			//this is going to be ugly!!!
			// TODO!!!	
			throw new TranslationUnsupportedException
			    ("Sorry, cannot translate assignment constraints with bionary expressions in their indices:"+constraint.toString());
			
		    }
		    else throw new TranslationUnsupportedException
			     ("Infeasible matrix index: "+indexList[0].toString()+" in expression:"+constraint.toString());
		} // end if(the original is a matrix)
		
		
		// the original is a  vector
		else if(minionVectors.containsKey(rightExpressionName)) {
		    if(indexList[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			// LEFT = vector[identifier]
			if(indexList[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER){
			    // if the identifier is a binding variable - hurray!
			    String elemIndex = indexList[0].getAtomicExpression().getString();
			    
			    if(bindingVariablesBounds.containsKey(elemIndex)) {
				int[] bounds = bindingVariablesBounds.get(elemIndex);
				MinionIdentifier[] vector = minionVectors.get(rightExpressionName);
				int[] offset = translatorMinionModel.getMatrixOffsets(rightExpressionName);
				bounds[0] = bounds[0] - offset[0];
				bounds[1] = bounds[1] - offset[0];
				int length = bounds[1] - bounds[0] +1;
				//elemOffset = offset[0];
				print_debug("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEeelement offset is:"+elemOffset);
				
				if(leftIsVector) {
				    // v[id] = RIGHT  or m[int,id] = RIGHT
				    if(leftElementIndexName != null) {
					
					// m[int,id1] = v[id2]
					if(leftVectorIndex >= 0) {
					    print_message("Can only reuse elements of vector '"+rightExpressionName+"' if the whole matrix '"+leftExpressionName+
							  "' is assigned to elements at once, not only a row as in: "+e.toString()+
							  ". Will translate the constraint without reusing variables of "+rightExpressionName+".");
					    
					    MinionConstraint c = translateQuantification(0);
					    if(constraint != null){
						translatorMinionModel.addConstraint(c);
					    }
					    return;

					}
					// v1[id1] = v2[id2]
					else {
							// v1[id] = v2[id] 
					    if(elemIndex.equals(leftElementIndexName)) {
						if(bounds[0]<bounds[1]) {
						    if(vector.length >= bounds[1]) {
							originalVector = new MinionIdentifier[length];
							
								for(int i=0; i<length; i++) {	
									originalVector[i] = vector[i+bounds[0]];
								}
						    }
						    else throw new TranslationUnsupportedException
							     ("Index out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
						}
						else throw new TranslationUnsupportedException("Upper bound must be greater than lower bound of binding variable "+elemIndex);
					    }
					    // v1[id1] = v2[id2], id1 != id2
					    else {
						if(bindingVariablesBounds.containsKey(leftElementIndexName)) {
						    int[] leftBounds = bindingVariablesBounds.get(leftElementIndexName);
						    int leftOffset = leftBounds[0];
						    leftBounds[1] = leftBounds[1] - leftOffset;
						    int l = leftBounds[1] - leftBounds[0];
						    
						    originalVector = new MinionIdentifier[l];
						    if(bindingVariablesNames.indexOf(leftElementIndexName) > bindingVariablesNames.indexOf(elemIndex)) {
						    	for(int j=0; j<vector.length; j++)
						    		for(int i=0; i<l; i++) 
						    			originalVector[i] = vector[j];
						    			
						    }		
						    else {
						    	for(int i=0; i<l; i++)
						    		for(int j=0; j<vector.length; j++) 
						    			originalVector[i] = vector[j];					    	
						    }

						}
						else throw new TranslationUnsupportedException("Unknown index variable in left expression in :"+e.toString());

					    }

					}
				    } 
				    // v[? ,int]			      
				    else throw new TranslationUnsupportedException
					     ("Wrong dimensions: Infeasible matrix index: "+indexList[0].toString()+" in expression:"+constraint.toString());
				    
				    
				} else if(leftIsMatrix) 
				     throw new TranslationUnsupportedException("Wrong dimensions assigned to each other in constraint: "+e.toString());
			    }
			    else throw new TranslationUnsupportedException
				     ("Expected binding variable or integer index instead of:"+elemIndex+", during translation of assignment expression:"
				      +e.toString());
			}
			else throw new TranslationUnsupportedException
				 ("Infeasible matrix index: "+indexList[0].toString()+" in expression:"+constraint.toString());
			
			// the index has to be the binding variable
			
		    }
		    else throw new TranslationUnsupportedException("Internal error. Expected matrix or vector instead of :"+rightExpressionName.toString()+
								   ", in assignment constraint:"+constraint.toString());
		    
       		} // if minionVector contains rightExpression
       		
	    } // end if(length == 1)
		
       		// LEFT = a [vectorIndex , elemIndex ]	
	    else if(indexList.length == 2) {
		//       		 the original is a matrix
		if(!minionMatrices.containsKey(rightExpressionName))
		    throw new TranslationUnsupportedException("The matrix '"+rightExpressionName+"' in expression '"+
							      constraint.toString()+"' is not a known matrix.");
		
		MinionIdentifier[][] matrix = minionMatrices.get(rightExpressionName);
		//MinionIdentifier[] vector = null; // in case we only use a row of the matrix
		Expression vectorIndexExpression = indexList[0];
		
		if(vectorIndexExpression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		    // LEFT = matrix[int, ?? ]
		    if(vectorIndexExpression.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			int vectorIndex = vectorIndexExpression.getAtomicExpression().getNumber();
			
			// LEFT = matrix[int, ATOM]
			if(indexList[1].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			    
			    // LEFT = matrix[int, identifier]
			    if(indexList[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
				String elemIndex = indexList[1].getAtomicExpression().getString();
				
				if(bindingVariablesBounds.containsKey(elemIndex)) {
				    int[] elemIndexBounds = bindingVariablesBounds.get(elemIndex);	
				    int[] offset = translatorMinionModel.getMatrixOffsets(rightExpressionName);
				    elemIndexBounds[0] = elemIndexBounds[0] - offset[1];
				    elemIndexBounds[1] = elemIndexBounds[1] - offset[1];
				    int noElements = elemIndexBounds[1] - elemIndexBounds[0];
				    
				    if(leftIsVector) {
					if(leftElementIndexName !=null) {					    					    
					    originalVector = new MinionIdentifier[noElements];
					    //elemOffset = offset[1]; 
					    if(vectorIndex > 0 && vectorIndex < matrix.length) {
						if(matrix[vectorIndex].length >= noElements) {	
						    
						    for(int eIndex=0; eIndex < noElements; eIndex++) {
							originalVector[eIndex] = matrix[vectorIndex][eIndex+elemIndexBounds[0]]; 
						    }
						    
						} else throw new TranslationUnsupportedException
							   ("Index '"+noElements+"' out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
						
					    } else throw new TranslationUnsupportedException
						       ("Index '"+vectorIndex+"' out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
					}
					else throw new TranslationUnsupportedException
						 ("Cannot assign vector to a single element, as in "+constraint.toString());
				    }
				    else { // if left is matrix

				    }

				}
				else throw new TranslationUnsupportedException
					 ("Expected binding variable or integer index instead of:"+vectorIndex+", during translation of assignment expression:"
					  +e.toString());						
			    }
			    else throw new TranslationUnsupportedException
				     ("Expected identifier or binary expression as index instead of '"
				      +indexList[1]+"' in "+rightExpressionName+", in expression:"+constraint.toString());
			    
			    
			} // LEFT = matrix[int, e1 op e2]
			else if(indexList[1].getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			    // TODO!!!!!!!! 						
			    throw new TranslationUnsupportedException
				("Cannot translate assignments with binary expressions in the indices as in '"+constraint.toString()+"', sorry.");
			    
			}
			else throw new TranslationUnsupportedException
				 ("Expected identifier or binary expression as index instead of '"+indexList[1]+"' in "+rightExpressionName+", in expression:"+constraint.toString());
		    }
		    // LEFT = a[identifier, ??]
		    else if(vectorIndexExpression.getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
			String vectorIndex = indexList[0].getAtomicExpression().getString();
			
			if(bindingVariablesBounds.containsKey(vectorIndex)) {
			    int[] vectorIndexBounds = bindingVariablesBounds.get(vectorIndex);				
			    int[] offset = translatorMinionModel.getMatrixOffsets(rightExpressionName);
			    int vectorIndexLowerBound = vectorIndexBounds[0] - offset[0];
			    int vectorIndexUpperBound = vectorIndexBounds[1] - offset[0];
			    int noVectors = vectorIndexBounds[1] - vectorIndexBounds[0]+1;
			    print_debug("Vector Binding variable bounds:"+vectorIndexLowerBound+", and "+vectorIndexUpperBound+" of bindingVar "+vectorIndex+
			    		"with offset:"+
					offset[0]+", and range:"+noVectors);
			    
			    if(indexList[1].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				
				// LEFT = matrix[identifier, int]  (accessing a column)
				if(indexList[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {	
				    int elemIndex = vectorIndexExpression.getAtomicExpression().getNumber();

				    if(leftVectorIndexName!=null) {
					
					if(leftElementIndex > 0) {
					    
					    if(vectorIndexLowerBound<vectorIndexUpperBound) {
						if(matrix.length >= vectorIndexUpperBound) {
						    originalMatrix = new MinionIdentifier[noVectors][leftElementIndex];
						    //elemOffset = offset[0]; // yes, because now the vector offset becomes element offset!
						    for(int i=0; i<noVectors; i++) {	
							if(matrix[i+vectorIndexLowerBound].length > elemIndex)
							    originalMatrix[i][leftElementIndex] = matrix[i+vectorIndexLowerBound][elemIndex];
							else throw new TranslationUnsupportedException
								 ("Index '"+elemIndex+"' out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
						    }
						}
						else throw new TranslationUnsupportedException
							 ("Index out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());

					    }
					    else throw new TranslationUnsupportedException("Upper bound must be greater than lower bound of binding variable "+vectorIndex);
					    
					} else {
					    if(vectorIndexLowerBound<vectorIndexUpperBound) {
						if(matrix.length >= vectorIndexUpperBound) {
						    originalVector = new MinionIdentifier[noVectors];
						    //elemOffset = offset[0]; // yes, because now the vector offset becomes element offset!
						    for(int i=0; i<noVectors; i++) {	
							if(matrix[i+vectorIndexLowerBound].length > elemIndex)
							    originalVector[i] = matrix[i+vectorIndexLowerBound][elemIndex];
							else throw new TranslationUnsupportedException
								 ("Index '"+elemIndex+"' out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
						    }
						}
						else throw new TranslationUnsupportedException
							 ("Index out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());

					    }
					    else throw new TranslationUnsupportedException("Upper bound must be greater than lower bound of binding variable "+vectorIndex);
					}
				    } 
				    else throw new TranslationUnsupportedException
					     ("Wrong dimension: cannot assign vector to non-vector element in :"+constraint.toString());
				    
				    
				    
				} // LEFT = matrix[identifier, identifier]
				else if(indexList[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
				    String elemIndex = indexList[1].getAtomicExpression().getString();
				    
				    if(bindingVariablesBounds.containsKey(elemIndex)) {
					int[] elemIndexBounds = bindingVariablesBounds.get(elemIndex);
					print_debug("Element Binding variable bounds:"+elemIndexBounds[0]+", and "+elemIndexBounds[1]+" without offset, of var:"+elemIndex);			
					elemIndexBounds[0] = elemIndexBounds[0] - offset[1];
					elemIndexBounds[1] = elemIndexBounds[1] - offset[1];
					int noElements = elemIndexBounds[1] - elemIndexBounds[0]+1;

					print_debug("Element Binding variable bounds:"+elemIndexBounds[0]+", and "+elemIndexBounds[1]+" with offset:"+
					offset[1]+",  and range:"+noElements);					


					//elemOffset = offset[1]; 
					//vectorOffset = offset[0]; 	
					
					if(leftVectorIndexName!=null && leftElementIndexName !=null) {
					    
					    print_debug("both left indices are strings: vector: "+leftVectorIndexName+" and elem:"+leftElementIndexName);
					    // m1[i,j] = m2[i,j]
					    if(leftVectorIndexName.equals(vectorIndex) && leftElementIndexName.equals(elemIndex)) {
						originalMatrix = new MinionIdentifier[noVectors][noElements];
						if(matrix.length >= noVectors) {
						    for(int vIndex=0; vIndex < noVectors; vIndex++) {
							
							if(matrix[vIndex].length >= noElements) {	
							    for(int eIndex=0; eIndex < noElements; eIndex++) {
								originalMatrix[vIndex][eIndex] = matrix[vIndex +vectorIndexLowerBound][eIndex+elemIndexBounds[0]]; 
							    }
							} else throw new TranslationUnsupportedException
								   ("Index '"+noElements+"' out of bounds for matrix "
								    +rightExpressionName+", in expression:"+constraint.toString());
							
						    }	
						}else throw new TranslationUnsupportedException
						      ("Index '"+noVectors+"' out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());
					    
					    } // m1[i,j] = m2[j,i]
					    else if (leftVectorIndexName.equals(elemIndex) && leftElementIndexName.equals(vectorIndex)) {
						print_debug("indices are switched: : "+leftVectorIndexName+" and elem:"+leftElementIndexName);
						
						originalMatrix = new MinionIdentifier[noElements][noVectors];
						if(matrix.length >= noVectors) {
						    for(int vIndex=0; vIndex < noVectors; vIndex++) {
							
							if(matrix[vIndex].length >= noElements) {	
							    for(int eIndex=0; eIndex < noElements; eIndex++) {
								originalMatrix[eIndex][vIndex] = matrix[vIndex +vectorIndexLowerBound][eIndex+elemIndexBounds[0]]; 
							    }
							} else throw new TranslationUnsupportedException
								   ("Index '"+noElements+"' out of bounds for matrix "
								    +rightExpressionName+", in expression:"+constraint.toString());
							
						    }	
						}else throw new TranslationUnsupportedException
						      ("Index '"+noVectors+"' out of bounds for matrix "+rightExpressionName+", in expression:"+constraint.toString());


					    } else {
						// we cannot translate this yet
						MinionConstraint c = translateQuantification(0);
						if(constraint != null){
						    translatorMinionModel.addConstraint(c);
						}
						return;
					    }
					} 
					else throw new TranslationUnsupportedException
						 ("Wrong dimensions: canoot assign a vector to the left expression in constraint:"+constraint.toString());



				    }
				    else throw new TranslationUnsupportedException
					     ("Expected binding variable or integer index instead of:"+vectorIndex+", during translation of assignment expression:"
					      +e.toString());
				}
				else throw new TranslationUnsupportedException
					 ("Expected integer, identifier or binary expression instead of: "+vectorIndex+" in expression:"+constraint.toString());
			    }
			    else throw new TranslationUnsupportedException
				     ("Expected binding variable or integer index instead of:"+vectorIndex+", during translation of assignment expression:"
				      +e.toString());
			    
			} else throw new TranslationUnsupportedException
				   ("Internal error: expected binding variable in index instead of "+vectorIndex+", in constraint:"+constraint.toString());
		    }
		    else throw new TranslationUnsupportedException
			     ("Internal error, expected an integer value or identifier in index instead of: "
			      +vectorIndexExpression.toString()+", in assignment constraint:"+constraint.toString());
		}
		else if (vectorIndexExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
		    
		    // TODO!!!!
		    
		    throw new TranslationUnsupportedException
			("Cannot translate assignments with binary expressions in the indices as in '"+constraint.toString()+"', sorry.");
		    
		}
		else throw new TranslationUnsupportedException("Internal error, expected atomic or binary expression in assignment constraint:"+constraint.toString());
		    
	    } // end: if(length ==2)	
	    else throw new TranslationUnsupportedException
		     ("Multi-dimensional matrices (more than 2 dimensions), as "+rightExpression.toString()+" are not supported yet, sorry.");
	    
	    
       	}	// end LEFT = NONATOMIC[...]
	else throw new TranslationUnsupportedException("Internal error, expected atom expression in assignment constraint:"+constraint.toString());
	
	
       	
       	// ------------------------------------------------------------------------------
       	
       	if(originalVector != null) {
	    // add the new vector if the left expression fits to the right expression
	    if(leftIsVector) {
		translatorMinionModel.addKnownIdentifierVector(originalVector, elemOffset, leftExpressionName);
		minionVectors.put(leftExpressionName, originalVector);
		return;
	    }
	    else throw new TranslationUnsupportedException
		     ("Wrong dimensions: cannot assign vector '"+constraint.getBinaryExpression().getRightExpression().toString()+"' to :"
		      +constraint.getBinaryExpression().getLeftExpression().toString());
       	}
       	else if(originalMatrix != null) {
	    // add the new matrix if the left expression fits to the right expression
       		if(leftIsMatrix) {
       			translatorMinionModel.addKnownIdentifierMatrix(originalMatrix, new int[] {vectorOffset, elemOffset}, leftExpressionName);
       			minionMatrices.put(leftExpressionName, originalMatrix);
       			return;
       		}
       		else throw new TranslationUnsupportedException
       		("Wrong dimensions: cannot assign matrix '"+constraint.getBinaryExpression().getRightExpression().toString()+"' to :"
       				+constraint.getBinaryExpression().getLeftExpression().toString());	
       		
       	}
       	else throw new TranslationUnsupportedException
       	 ("Internal error, could not translate assignment constraint: "+constraint.toString());
    }
    
    
    */
    
    protected void resetObjectiveVariable() {
    	this.objectiveVariable = null;
    }
    
	protected void translatingObjective(boolean translatingObjective) {
		this.isObjective = translatingObjective;
	}
    
    
	protected MinionIdentifier getObjectiveVariable() {
		return this.objectiveVariable;
	}
	
	
    /**
     * clears all the variables used for storage during the translation process of 
     * a quantification. 
     */
    
    private void clearAll() {  	
    	bindingVariablesNames.clear();
		bindingParameters.clear();
		quantifierVariables.clear();
		quantifiers.clear();
		bindingVariablesBounds.clear();
		bindingVariablesValues = null;
		quantifiedExpression = null;    	
    } 
	
	  /** 
     * If the DEBUG-flag in the Globals-interface is set to true, then
     * print the debug-messages. These messages are rather interesting 
     * for the developper than for the user.
     * @param s : the String to be printed on the output
     */

     protected static void print_debug(String s) {
     	if(DEBUG)
     		System.out.println("[ DEBUG quantifierTranslator ] "+s);
     }  

     protected static void print_message(String s) {
      	if(PRINT_MESSAGE)
      		System.out.println("[ WARNING ] "+s);
      }  



 	
}
