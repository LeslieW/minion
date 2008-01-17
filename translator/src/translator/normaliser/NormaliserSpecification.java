package translator.normaliser;

import translator.conjureEssenceSpecification.*;
import java.util.ArrayList;

public interface NormaliserSpecification {

	
	// different types of normalisation
	public static final char NORMALISE_BASIC = 0;
	public static final char NORMALISE_EVAL = 1;
	public static final char NORMALISE_ORDER = 2;
	public static final char NORMALISE_CANCELLATION =3;
	public static final char NORMALISE_FULL = 4;
	
	public final int UNDEFINED_PARAMETER_ARRAY_ELEMENT = -99999;
	
	/**
	 * Normalises the problem- and parameter-specification and returns a 
	 * normalised model. This approach does FULL normalisation.
	 *  
	 * @return a normalised model. 
	 * @throws NormaliserException
	 */
	public NormalisedModel normalise() throws NormaliserException;
	
	/**
	 * Normalises the problem- and parameter-specification and returns a 
	 * normalised model. Normalises corresponding to the normalisation type
	 * (evaluation only, ordering only, etc)
	 * 
	 * @param normaliseType
	 * @return a normalised model. 
	 * @throws NormaliserException
	 */
	public NormalisedModel normalise(char normaliseType) throws NormaliserException;
	
	/**
	 * Provides FULL normalisation: basics (parameter insertion), 
	 * ordering, reducing of expression structure and evaluation.
	 * 
	 * @return the list of ordered, evaluated and reduces constraints
	 * @throws NormaliserException
	 */
	public ArrayList<translator.expression.Expression> normaliseConstraints() throws NormaliserException;
	
	
	/**
	 * Provides advanced normalisation: basics (parameter insertion) and  
	 * expression evaluation. Please note, that evaluation is not optimised
	 * because some expressions are better evaluated when they have been 
	 * previously ordered.
	 * 
	 * @return
	 * @throws NormaliserException
	 */
	//protected ArrayList<translator.expression.Expression> normaliseEvaluate() throws NormaliserException;
	
	/**
	 * Basic normalisation inserts parameter values into expressions, some evaluation of 
	 * parameter values.
	 *  
	 * @return
	 * @throws NormaliserException
	 */
	//protected ArrayList<translator.expression.Expression> normaliseBasic() throws NormaliserException;
	
	/**
	 * Insert the parameters, specified in the parameterSpecification. Is it important insert parameters
	 * BEFORE mapping Expressions to the other representation!!
	 * 
	 * @param problemSpecification
	 * @param parameterSpecification
	 * @return
	 * @throws NormaliserException
	 */
	public ArrayList<Expression> insertParameters(EssenceSpecification problemSpecification,
			                                      EssenceSpecification parameterSpecification)
			                                      throws NormaliserException;
	
	/** maps expressions from the old expression tree (that we get from the parser) to the new expression 
	 *  representation. 
	 *  
	 *  Why do we have 2 different representations? 
	 *  The first representation is a datastructure based on the quite straightforward Essence' grammar 
	 *  but it is a big pain to perform reformulations and enhancing steps on them: the nested structure
	 *  (not using any inheritances) makes code quite unreadable and it does not 
	 *  encapsulate any features of expressions or operations. 
	 *  
	 *  The second representation is very easy to reformulate, enhance 
	 *  and reduce and encaptures a lot of features in its structure that the other represenation does not.
	 *   
	 *  Why not use this representation from the start? Because it would require to re-write the grammar  
	 *  (and make it unreadable to anyone else), because it would require to re-write the whole parameter
	 *  insertion part and because it contains some little type-information (what 
	 *  type does an identifier have - int or bool?) which cannot be determined during parse time. 
	 *  */
	public ArrayList<translator.expression.Expression> mapExpressionList
	                                                     (ArrayList<translator.conjureEssenceSpecification.Expression> oldExpressionList)
	     throws NormaliserException;
	
	
	/**
	 * Returns the evaluated list of Expressions
	 */
	//public ArrayList<translator.expression.Expression> evaluateConstraints(ArrayList<translator.expression.Expression> constraints) 
	//	throws NormaliserException;
	
	
	//public ArrayList<translator.expression.Expression> orderConstraints(ArrayList<translator.expression.Expression> constraints) 
	//	throws NormaliserException;

	
	//public ArrayList<translator.expression.Expression> reduceExpressions(ArrayList<translator.expression.Expression> constraints) 
	//	throws NormaliserException;
	
	
}
