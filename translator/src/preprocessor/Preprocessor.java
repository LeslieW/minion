package preprocessor;

import java.util.HashMap;
import java.util.ArrayList;

import minionExpressionTranslator.TranslationUnsupportedException;
import minionModel.MinionException;

import conjureEssenceSpecification.*;


/** 
 *  The Preprocessor preprocesses relevant (solver-independent) information from the 
 *  Essence' Syntax tree (produced by the parser) and collects it. 
 *  The result is the basic information about the model instance:
 *  a set of decision variables (with evaluated domains) and 
 *  a list of evaluated and simplified constraints.  
 *  <br>
 *  The preprocessor performs the following steps:
    <ol>
    <li> assign values to the parameters
    <li> collect decision variables
    <li> process expressions/constraints
      <ul>
       <li> insert parameter values in expressions
       <li> evaluate expressions
       <li> split independent constraints from another 
       <li> simplify expressions (still under construction)
       </ul>
   </ol>   
   All these operations are independent of the solver we are translating
   Essence' to and are highly recommended in order to simplify the 
   translation process.
      */

public class Preprocessor implements PreprocessorGlobals {

    ExpressionEvaluator evaluator;
    //ExpressionSimplificator simplificator;

    /** a HashMap containing the 
   parameters values and their names as keys */
    private HashMap<String,Constant>  parameters; // key : <E'id (String identifiername)>
                                // value: <Constant instance>   

    /** contains all one dimensional arrays of constants/parameters */
    private HashMap<String, int[]> parameterVectors;
    /** contains all 2 dimensional arrays of constants/parameters */
    private HashMap<String, int[][]> parameterMatrices;
    /** contains all 3 dimensional arrays of constants/parameters */
    private HashMap<String, int[][][]> parameterCubes;
    
    /** holds the lower (Expression[0]) and upper bound Expression[1] of the parameter with name String*/
    private HashMap<String, Domain[]> parameterArrayBounds;
    
    private HashMap<String, int[]> parameterArrayOffsets;
    
    /** a HashMap containing the decision variables'
        domains with their names as keys */
    private HashMap<String,Domain>  decisionVariables ;       // key: <E'id (String variableName)>
                                      // value: <Domain variableDomain>
    
    /** an ArrayList<String> with all parameters' names */
    private ArrayList<String> parameterNames;  // <String>
    private ArrayList<String> parameterArrayNames;
    
    /** an ArrayList<String> with all parameters' names */
    private ArrayList<String> decisionVariablesNames;  // <String>
    
/** contains expressions that have been stated by a 'where' statement */
    private ArrayList<Expression> whereExpressions;
    
    /** an ArrayList<Expression> of all constraints */
    private ArrayList<Expression> constraints;  // <Expression>

    /** contains objective, if one was specified */	
    private Objective objective;

    private Parameters parameterArrays;
    
    private boolean verbose_translation;
    
    
    /**
     * Preprocess the specification and generate a list of decision variables 
     * and constraints.
     * 
     * @param syntaxTreeProblem the EssenceSpecification generated from the problem specification
     * @param syntaxTreeParameters the EssenceSpecification generated from the parameter specification
     * @throws PreprocessorException
     */
    public Preprocessor (EssenceSpecification syntaxTreeProblem, EssenceSpecification syntaxTreeParameters)
		throws PreprocessorException {
	
 
    	parameters = new HashMap<String,Constant> ();
    	parameterVectors = new HashMap<String, int[]>();
    	parameterMatrices = new HashMap<String, int[][]>();
    	parameterCubes = new HashMap<String, int[][][]>();
    	decisionVariables = new HashMap<String,Domain> ();
    	parameterArrayBounds = new HashMap<String, Domain[]>();	
    	parameterArrayOffsets = new HashMap<String, int[]>();
    	
    	
    	parameterNames = new ArrayList<String>();
    	parameterArrayNames = new ArrayList<String>();
    	whereExpressions = new ArrayList<Expression>();
    	
    	decisionVariablesNames = new ArrayList<String>();
    	constraints = new ArrayList<Expression>();
    	verbose_translation = true;

       	print_verbose_message("Starting to process parameters...");
       	parameterArrays = new Parameters(this.parameterVectors,
                this.parameterMatrices,
                this.parameterCubes,
                this.parameterArrayOffsets);
       	
       	evaluator = new ExpressionEvaluator(parameters, parameterArrays);
       	
       	
    	computeParameters(syntaxTreeProblem.getDeclarations(), 
    					  syntaxTreeParameters.getDeclarations());
    	
    	//parameterArrays = new Parameters(this.parameterVectors,
        //        this.parameterMatrices,
         //       this.parameterCubes,
          //      this.parameterArrayOffsets);
    	
    	print_verbose_message("Finished processing parameters...");
    	
       	evaluator = new ExpressionEvaluator(parameters, parameterArrays);
       	print_debug("constructed expressionEvaluator.");
    	
       	//print_message("Starting processing decision variables...");
       	// insert parameters and store them in the HashMap
    	insertDecisionVariablesInHashMap(syntaxTreeProblem.getDeclarations());
    	//orderDomainsOfDecisionVariables();
    	//print_message("Finished processing decision variables...");
    	
    	print_debug("inserted decision Variables in HashMap.");
    	
       	//print_message("Starting processing constraints...");
    	// insert parameters, evaluate constraints and store them in a list
    	preprocessConstraints(syntaxTreeProblem.getExpressions());
       	//print_message("Finished processing constraints...");
    	
       	print_debug("Constraint expressions: ");
       	for(int i=0; i<constraints.size(); i++)
       		print_debug(i+": "+constraints.get(i));
    	
       	
       	print_verbose_message("Starting processing objective...");
    	this.objective = preprocessObjective(syntaxTreeProblem.getObjective());
    	print_verbose_message("Finished processing objective...");
    	
 
    	// is still under construction
    	//orderConstraints();
    		
    	
    
    }
  

    public HashMap<String,Domain> getDecisionVariables() {
	return decisionVariables;
    }

    public HashMap<String,Constant> getParameters() {
	return parameters;
    }

    public ArrayList<String> getDecisionVariablesNames() {
	return decisionVariablesNames;
    }

    public ArrayList<Expression> getConstraints() {
	return constraints;
    }

    public Objective getObjective() {
    	return objective;
    }
    
    public Parameters getParameterArrays() {
    	return this.parameterArrays;
    }
    
    
    
    protected void orderConstraints() throws PreprocessorException {
    	
    	ConstraintOrdering constraintOrderer = new ConstraintOrdering();
    	
    	// order each constraint separately
    	for(int i=0; i<constraints.size(); i++ ) {
    		Expression orderedConstraint = constraintOrderer.orderExpression(constraints.remove(i));
    		constraints.add(i, orderedConstraint);
    	}
    	
    	// order the constraints
    	constraints = constraintOrderer.orderExpressionList(constraints);
    	
    }
    
    /**
     * Evaluate the expression that is minimised or maximised (if there even is an objective)
     * @param objective
     * @return
     * @throws PreprocessorException
     */
    public Objective preprocessObjective(Objective objective) 
         throws PreprocessorException {
    	
      	if(objective.getExpression()!=null) 
    		objective = new Objective(objective.isMinimising(), 
    				                  evaluator.evalExpression(insertValueForParameters(objective.getExpression())));
      	
      	return objective;
    }
    
    /** 
	* Compute the HashMap with parameters:
	* First collect all declarations: from the problem specification
	* and the parameter specification. Then collect all parameter names.
	* Assign the corresponding values (defined in the 'letting' block) to the 
	* parameters. If a parameter is left unassigned, throw an exception.
	* valuate the assigned values until they evaluate to integer 
	* values. If not, throw an exception.
	* @param problemDeclarations is the Declaration[] array containing the declarations made in
	*	the problem specification file
	* @param parametereclarations is the Declaration[] array containing the declarations made in
	*		the parameter specification file
	* @return HashMap containing parameters and their values
	* @throws PreprocessorException
	*	
     */

    private void computeParameters(Declaration[] problemDeclarations, Declaration[] parameterDeclarations) 
	throws PreprocessorException {

    	Declaration[] declarations = new Declaration[problemDeclarations.length + parameterDeclarations.length];

 
    	for(int i=0; i< parameterDeclarations.length; i++) 
    		declarations[i] = parameterDeclarations[i];
       	for(int i=0; i< problemDeclarations.length; i++) 
    		declarations[i+parameterDeclarations.length] = problemDeclarations[i];
	
	// 	collect parameters (given, where, letting)
    	collectParameterNames(declarations); 
    	print_debug("collected parameter names:"+parameterNames.toString());

	// 	collect their values (letting) and re-evaluate them 
    	collectParameters(declarations);
    	print_debug("collected parameters:"+parameters.toString());
  	
    	// update parameters such as simple variables or parameter domains
    	updateConstants(declarations);
    	// update parameter arrays
    	updateConstantArrays();
    	print_debug("updated parameters.");
	
	// 	TODO: restrictParameterValues (where)
	
    	if(!(everyParameterIsAssigned())) 
    		throw new PreprocessorException
    		("Not all parameters are defined. Please make sure that you have assigned a value to each parameter.");

    }

    /** 
	* collect Parameters: insert all declared parameters in the 
	* parameters list and set their value to null if there is none specified yet
	* 
	* @param declarations the Declaration[] array containing all declarations 
	* made in both the problem and parameter specification file
	* @throws PreprocessorException
     */
    private void collectParameterNames(Declaration[] declarations) 
	throws PreprocessorException {
	
    	for(int i=0; i< declarations.length; i++) {
	    
    		if(declarations[i].getRestrictionMode() == EssenceGlobals.GIVEN) {
    			Parameter[] parameterList = declarations[i].getParameter();

    			for(int j=0; j< parameterList.length; j++) {
    				DomainIdentifiers domainIdentifiers = parameterList[j].getDomainIdentifiers();
    				String[] parameterName = domainIdentifiers.getIdentifiers();
    				
    				// check if it's a matrix
    				Domain domain = domainIdentifiers.getDomain();
    				if(domain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN){
    					
    					Domain[] indexDomains = domain.getMatrixDomain().getIndexDomains();
    					// first collect the names of the arrays
       					for(int k=0; k < parameterName.length; k++) {
    						//this.parameterArrays.put(parameterName[k],null); 
    						this.parameterArrayBounds.put(parameterName[k],indexDomains);
    						
    						if(!this.parameterArrayNames.contains(parameterName[k]))
    							this.parameterArrayNames.add(parameterName[k]);
    					}	
       					
    				}// else the parameter is no matrix
    				else {
    					for(int k=0; k < parameterName.length; k++) {
    						this.parameters.put(parameterName[k],null); 
    						if(!this.parameterNames.contains(parameterName[k]))
    							this.parameterNames.add(parameterName[k]);
    					}	
    				}
    			}
    		}
    		else if(declarations[i].getRestrictionMode() == EssenceGlobals.LETTING) {
    			Constant[] constantList = declarations[i].getConstants();
    			
    			for(int j=0; j<constantList.length; j++) {
    				String constantName = "";
    				if(constantList[j].getRestrictionMode() == EssenceGlobals.CONSTANT_DOMAIN) {
    					constantName = constantList[j].getDomainConstant().getName();
 
    				} 
    				else constantName = constantList[j].getExpressionConstant().getName();
    				
   					parameters.put(constantName, constantList[j]);
   					if(!parameterNames.contains(constantName))
   						parameterNames.add(constantName);
    			}
    			
    		}
    		/*else if(declarations[i].getRestrictionMode() == EssenceGlobals.WHERE) {
    			Expression[] expressions = declarations[i].getExpressions();
    			for(int j=0; j<expressions.length; j++) 
    				whereExpressions.add(expressions[j]);

    		}*/
		}	
    }
    


  /**
   *  Find all parameter definitions and insert the values to the 
   *   corresponding parameter name in the HashMap 'parameters'.
   * @param declarations the Declaration[] array containing the declarations made in
   * 	the problem and parameter specification.
   * @throws PreprocessorException
   */
    private void collectParameters(Declaration[] declarations)
	throws PreprocessorException {
			
	for(int i=0; i< declarations.length; i++) {

	    if(declarations[i].getRestrictionMode() == EssenceGlobals.LETTING) {
	    	Constant[] constantList = declarations[i].getConstants();

		for(int j=0; j<constantList.length; j++) {

		  	print_debug("the "+j+"th element in the constantlist: "+constantList[j].toString());
		  	
		    switch(constantList[j].getRestrictionMode()) {
				    
			// we got a domain-constant
		    case EssenceGlobals.CONSTANT_DOMAIN:
			  	print_debug("the "+j+"th element in the constantlist is a domain constant :"+constantList[j].toString());
			  	
		    	DomainConstant beDomain = constantList[j].getDomainConstant();
			  	print_debug("the "+j+"th element in the constantlist: '"+beDomain.getDomain()+"' will be evaluated");
			  	
		    	Domain domain = evaluator.evalExpressionInDomain(beDomain.getDomain());
		    	constantList[j] = new Constant(new DomainConstant(beDomain.getName(), domain));
			  	print_debug("the "+j+"th element in the constantlist has been evaluated to :"+constantList[j].toString());

			  	if(parameters.containsKey(beDomain.getName())) 
			  		parameters.put(beDomain.getName(),constantList[j].copy());   

			  	else 	
			  		parameters.put(beDomain.getName(),constantList[j].copy());
			  	break;


			// we got a constant (can either be with or without domain (we neglect domain at moment))
		    case EssenceGlobals.CONSTANT:  
		    	ExpressionConstant beConst = constantList[j].getExpressionConstant();
			// 	evaluate the expression inside the constant
		    	Constant c = new Constant(new ExpressionConstant(beConst.getName(),
						         evaluator.evalExpression(beConst.getExpression())));
			
		    	if(parameters.containsKey(beConst.getName()))
		    		parameters.put(beConst.getName(),c);

		    	else 
		    		parameters.put(beConst.getName(),c);
		    	break;

		    default:
		    	throw new  PreprocessorException("Unknown Constant type :"+constantList[j].toString());		       
		    	}
		}// end for(j)
	    }
	    else if(declarations[i].getRestrictionMode() == EssenceGlobals.WHERE) {
	    	
	    	Expression[] expressions = declarations[i].getExpressions();
	    	
	    	for(int j=0; j<expressions.length; j++) {    		
	    		whereExpressions.add(evaluator.evalExpression(expressions[j]));
	    	}	
	    	
	    }
	    
	}// end for(i)	
    }



    /** 
     *
     * Should be evoked after collecting ALL parameters/constants.
     * Evaluates all constants in the minionParams-HashMap and 
     * assumes that all expressions can be reduced to atomic expressions.
     * 
     *	@param declarations array of Declaration[] containing corresponding letting 
     * 	 statements that carry a list of constants 
     * 
     */
    
   private void updateConstants(Declaration[] declarations)
       throws PreprocessorException {
       int i,j;       
       
       for(i=0; i< declarations.length; i++) {

	    if(declarations[i].getRestrictionMode() == EssenceGlobals.LETTING) {
		Constant[] constant_list = declarations[i].getConstants();
		
		for(j=0; j<constant_list.length; j++) {
		    
		    evaluateAndUpdate(constant_list[j]);
		    
		}
	    }
	}
   }
    


   /** 
     *	Evaluate and update the current parameter values.   
     *  
     * @param c Constant whose expressions are going to be evaluated
     * @throws PreprocessorException
     * 
    */
    private void evaluateAndUpdate(Constant c)
	throws PreprocessorException {
	
	switch (c.getRestrictionMode()) {	    
	    
	    // we got a domain-constant
	case EssenceGlobals.CONSTANT_DOMAIN:
	    DomainConstant beDomain = c.getDomainConstant();
	    Domain domain = evaluator.evalExpressionInDomain(beDomain.getDomain());
	    c = new Constant(new DomainConstant(beDomain.getName(), domain));
	    
	    print_debug("Putting new constant/parameter domain into parameterlist: "+beDomain.getName());
	    parameters.put(beDomain.getName(),c);   
	    break;

	    // we got a constant (can either be with or without domain (we neglect domain at moment))
	case EssenceGlobals.CONSTANT:  
	    ExpressionConstant beConst = c.getExpressionConstant();
	    // evaluate the expression inside the constant
	    c= new Constant(new ExpressionConstant(beConst.getName(),
				   evaluator.evalExpression(beConst.getExpression())));
	    
	    parameters.put(beConst.getName(),c);	
	    print_debug("Putting new constant/parameter expression into parameterlist: "+beConst.getName());
	    break;

	default:
	    throw new  PreprocessorException
		("Unknown Constant type :"+c.getRestrictionMode());		       
	}		
    }
    

    /** 
	* @returns true, if every parameter in the HashMap parameters
	* has a value assigned to it.
	*	
     */
    private boolean everyParameterIsAssigned() {
	
	for(int i=0; i < parameterNames.size(); i++) {
		Constant constant = parameters.get(parameterNames.get(i));
	    if(constant == null) {
	    	print_debug("the following parameter has no value assigned to it:"+parameterNames.get(i));
	    	return false;	    
	    }
	   /* else {
	    	// TODO: check if every element of constant arrays are assign values to.
	    	if(constant.getRestrictionMode() == EssenceGlobals.CONSTANT) {
	    		Domain constantsDomain = constant.getExpressionConstant().getDomain();
	    		// if the constant is a matrix, we have to make sure that every element is assigned
	    		if(constantsDomain.getRestrictionMode() == EssenceGlobals.MATRIX_DOMAIN) {
	    			MatrixDomain matrixDomain = constantsDomain.getMatrixDomain();
	    			
	    			Domain[] indexDomain = matrixDomain.getIndexDomains();
	    			//Domain rangeDomain = matrixDomain.getRangeDomain();
	    			
	    			for(int j=0; i<indexDomain.length; i++) {
	    				if(indexDomain[j].getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
	    				   IntegerDomain intRange = indexDomain[j].getIntegerDomain();
	    				   RangeAtom[] ranges = intRange.getRangeList();
	    				   for(int k =0; k<ranges.length; k++) {
	    				       Expression lb = ranges[k].getLowerBound();
	    				       Expression ub = ranges[k].getUpperBound();
	    				       
	    				       if(lb.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR && 
	    				    		  ub.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
	    				    	   if(lb.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER && 
	    				    			   ub.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
	    				    		   int lowerBound = lb.getAtomicExpression().getNumber();
	    				    		   int upperBound = ub.getAtomicExpression().getNumber();
	    				    	   }
	    				       }
	    				   }
	    				}
	    				
	    			}
	    		}
	    	}
	    	
	    }*/
	}	
	return true;
    }


    /** 
	* collect all the decision variables and evaluate their domains.
	* @params the parameter is the declaration-array of the 
	*        problem model.
    *
     */

    private void insertDecisionVariablesInHashMap(Declaration[] declarations) 
	throws PreprocessorException{
	
	for (int i = 0; i < declarations.length; i++) {	   
	    
	    if (declarations[i].getRestrictionMode() == EssenceGlobals.FIND) {
		
	    	DomainIdentifiers[] decisionVars = declarations[i].getVariables() ;
		
	    	for (int j = 0; j < decisionVars.length; j++) {
		    
	    		String[] variableNames = decisionVars[j].getIdentifiers();

	    		for(int k=0; k < variableNames.length; k++)  {
	    			String varName = variableNames[k];
	    			Domain domain = evaluator.evalExpressionInDomain(decisionVars[j].getDomain() ) ;
	    			decisionVariables.put(varName, domain);
	    			decisionVariablesNames.add(varName);
	    		}
	    	} // end of declared vars loop
	    	}
		} // end of declarations loop

    }


  /**
   * Collect all constraints: first insert parameter values in the
   * expressions and then evaluate them. Then split independent
   * Constraints and finally sum them up in the constraints ArrayList.
   * 
   * @param expressions the Expression[] array containing the expression to be 
   * 	evaluated and collected in the constraints ArrayList
   * @throws PreprocessorException
   */
    private void preprocessConstraints(Expression[] expressions)
	throws PreprocessorException {
	
	// insert parameter values
	for(int i=0; i<expressions.length; i++) {
	    expressions[i] = evaluator.evalExpression(insertValueForParameters(expressions[i]));

	// split independent constraints
	    ArrayList<Expression> constraintList = splitExpressionToConstraints(expressions[i]);

	    // TODO: simplify constraints (before or after splitting?)

	    // collect constraints in the ArrayList 'constraints'	
	    for(int j=0; j<constraintList.size(); j++)
	    	constraints.add(constraintList.get(j));
	    
	}

    }

    /** 
	* iterate through the expression, and whenever finding an identifier,
	* check if it is a parameter in which case the value for it is 
	* inserted and again evaluated.
	*	
	* @param the expression in which all parameters (that are defined
	* in the parameters HashMap) are exchanged by their value
	* @returns the expression that contains no parameters but their
	* corresponding values instead.
	*	
     */
    private Expression insertValueForParameters(Expression expression)
	throws PreprocessorException {

	switch(expression.getRestrictionMode()) {

	case EssenceGlobals.ATOMIC_EXPR:
	    AtomicExpression atomicExpression = expression.getAtomicExpression();

	    // if we found an identifier, check if it is a parameter
	    if(atomicExpression.getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
		if(parameters.containsKey(atomicExpression.getString())) {
		    Constant c = (Constant) parameters.get(atomicExpression.getString());
		    if(c == null) /* we did not find e in the parameter-hashmap*/ 
		    	return expression;
		    	
		    if(c.getRestrictionMode() == EssenceGlobals.CONSTANT_DOMAIN) 
		    	throw new PreprocessorException("Domain identifier '"+atomicExpression.toString()+"' used in constraint expression.");
		    
		    Expression new_e = c.getExpressionConstant().getExpression();
		    return evaluator.evalExpression(new_e);
		}

	    }
	    return expression;	

	case EssenceGlobals.BRACKET_EXPR:// (E)
	    return new Expression(insertValueForParameters(expression.getExpression()) );

	case EssenceGlobals.BINARYOP_EXPR: 
	    return new Expression(new BinaryExpression(insertValueForParameters(expression.getBinaryExpression().getLeftExpression()),
							 expression.getBinaryExpression().getOperator(), 
							 insertValueForParameters(expression.getBinaryExpression().getRightExpression()) ));

	case EssenceGlobals.UNITOP_EXPR:
	    return new Expression(new UnaryExpression(expression.getUnaryExpression().getRestrictionMode(),
						       insertValueForParameters(expression.getUnaryExpression().getExpression())));

	case EssenceGlobals.QUANTIFIER_EXPR:
		return new Expression(new QuantificationExpression(expression.getQuantification().getQuantifier(),
				                                           expression.getQuantification().getBindingExpression(),
				                                           insertValueForParameters(expression.getQuantification().getExpression())));
	    
	case EssenceGlobals.NONATOMIC_EXPR:
		if(expression.getNonAtomicExpression().getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
			throw new PreprocessorException("Please access array elements by m[i,j] instead of m[i][j], thank you.");
		
		String matrixName = expression.getNonAtomicExpression().getExpression().getAtomicExpression().getString();
		if(!this.parameterArrayNames.contains(matrixName))
			return expression; // then this might be a decision variable
		
		// get the indices
		Expression[] indexExpressions = expression.getNonAtomicExpression().getExpressionList();
		int[] indices = new int[indexExpressions.length];
		for(int j=0;j<indexExpressions.length; j++) {
			if(indexExpressions[j].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
				throw new PreprocessorException("Cannot insert parameter array with index that is not an integer or identifier:"+expression.toString());
			if(indexExpressions[j].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
				return expression;
			indices[j] = indexExpressions[j].getAtomicExpression().getNumber();
		}
		
		
		switch(indices.length) {
		
		case 1: // 1-dimensional
			if(!this.parameterVectors.containsKey(matrixName))
				throw new PreprocessorException("Wrong dimensions: "+matrixName+" is not a vector.");
			int[] parameterVector = this.parameterVectors.get(matrixName);
			int index = indices[0] - this.parameterArrayOffsets.get(matrixName)[0];
			
			if(index >= parameterVector.length || index < 0 )
				throw new PreprocessorException("Index for parameter array is out of bounds:"+expression.toString());
			if(parameterVector[index] == UNDEFINED_PARAMETER_ARRAY_ELEMENT)
				print_message("Parameter '"+matrixName+"["+indices[0]+"]' appears to not have been initialised.");
			
			return new Expression(new AtomicExpression(parameterVector[index]));
			
			
		case 2: // 2-dimensional
			if(!this.parameterMatrices.containsKey(matrixName))
				throw new PreprocessorException("Wrong dimensions: "+matrixName+" is not a matrix.");
			int[][] parameterMatrix = this.parameterMatrices.get(matrixName);
			int[] offsets = this.parameterArrayOffsets.get(matrixName);
			int rowIndex = indices[0] - offsets[0];
			int colIndex = indices[1] - offsets[1];
			
			if(rowIndex >= parameterMatrix.length || rowIndex < 0 ||
					colIndex >= parameterMatrix[0].length || colIndex < 0)
				throw new PreprocessorException("Index for parameter array is out of bounds:"+expression.toString());
			if(parameterMatrix[rowIndex][colIndex] == UNDEFINED_PARAMETER_ARRAY_ELEMENT)
				print_message("Parameter '"+matrixName+"["+indices[0]+","+indices[1]+"]' appears to not have been initialised.");
		
			return new Expression(new AtomicExpression(parameterMatrix[rowIndex][colIndex]));
			
		case 3: // 3-dimensional
			if(!this.parameterCubes.containsKey(matrixName)) 
				throw new PreprocessorException("Wrong dimensions: "+matrixName+" is not a cube.");
			int[][][] parameterCube = this.parameterCubes.get(matrixName);
			
			int[] cOffsets = this.parameterArrayOffsets.get(matrixName);
			int plane = indices[0] - cOffsets[0];
			int row = indices[1] - cOffsets[1];
			int col = indices[2] - cOffsets[2];		
			
			if(plane <0 || plane >= parameterCube.length ||
				 row >= parameterCube[0].length || row < 0 || 
				     col >= parameterCube[0][0].length || col < 0)
				throw new PreprocessorException("Index for parameter array is out of bounds:"+expression.toString());
			if(parameterCube[plane][row][col] == UNDEFINED_PARAMETER_ARRAY_ELEMENT)
				print_message("Parameter '"+matrixName+"["+indices[0]+","+indices[1]+","+indices[2]+"]' appears to not have been initialised.");
			
			return new Expression(new AtomicExpression(parameterCube[plane][row][col]));
			
			
		default:
			throw new PreprocessorException("Sorry, parameter arrays over 3-dimensions are not supported yet:"+expression.toString());
		}
	   
	default:
	    // !!! do the rest later
	    return expression;
	    

	}
	
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
	* @throws PreprocessorException
    */
    
    private ArrayList<Expression> splitExpressionToConstraints(Expression constraint) 
	throws PreprocessorException {
	
	ArrayList<Expression> constraintList = new ArrayList<Expression>();

	// we can only split constraints, if we have and AND on top of the syntax tree
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
	
	// we cannot split the constraint since there is no AND at top-level of the constraint
	constraintList.add(constraint) ;
	

	return constraintList;
    }

  
    
    
    private void updateConstantArrays() 
    	throws PreprocessorException {
    	

    	// 1. Create a int-array for every (multi-dim.) parameter array
    	//    and store it in the corresponding HashMap (parameterVector/Matrix/Cube)
    	
    	// for every parameter array
    	for(int i=0; i< this.parameterArrayNames.size(); i++) {
    		
    		String parameterName = parameterArrayNames.get(i);
    		print_debug("Updating parameter:"+parameterName);
    		Domain[] indexDomain = this.parameterArrayBounds.get(parameterName);
    		// evaluate the domains of the parameter array
    		int[][] bounds = new int[indexDomain.length][2];
    		
    		for(int j=0; j<indexDomain.length; j++) {
    			indexDomain[j] = evaluator.evalExpressionInDomain(indexDomain[j]);
    			
    			switch(indexDomain[j].getRestrictionMode()) {
    				
    			case EssenceGlobals.INTEGER_RANGE:
    				RangeAtom[] range = indexDomain[j].getIntegerDomain().getRangeList();
    				for(int k=0; k<range.length; k++) {
    					if(range[k].getLowerBound().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
    						throw new PreprocessorException("Could not evaluate lower bound of parameter '"+parameterName+
    								"' to an integer value.");
    					if(range[k].getLowerBound().getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
    						throw new PreprocessorException("Could not evaluate lower bound of parameter '"+parameterName+
							"' to an integer value.");
    					bounds[j][0] = range[k].getLowerBound().getAtomicExpression().getNumber();
    					
       					if(range[k].getUpperBound().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
    						throw new PreprocessorException("Could not evaluate upper bound of parameter '"+parameterName+
    								"' to an integer value.");
    					if(range[k].getUpperBound().getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
    						throw new PreprocessorException("Could not evaluate upper bound of parameter '"+parameterName+
							"' to an integer value.");
    					bounds[j][1] = range[k].getUpperBound().getAtomicExpression().getNumber();
    				}
    				break;
    			case EssenceGlobals.BOOLEAN_DOMAIN:
    				bounds[j][0] = 0;
    				bounds[j][1] = 1;
    				break;
    				
    			default:
    				throw new PreprocessorException("Cannot translate domain of parameter '"+parameterName+
    						". Expected an integer range or boolean domain instead of:"+indexDomain[j].toString());
    				
    			}
    		}// end for all indices
    		
    		int noRows,noCols,noPlanes;
    		
    		switch(indexDomain.length) {
    		
    		case 1: // we have a parameter vector
    			noCols = bounds[0][1] - bounds[0][0] + 1; // ub - lb
    			int[] parameterVector = new int[noCols];
    			for(int col=0; col<parameterVector.length; col++)
    				parameterVector[col] = UNDEFINED_PARAMETER_ARRAY_ELEMENT;
    			
    			this.parameterVectors.put(parameterName, parameterVector);
    			this.parameterArrayOffsets.put(parameterName, new int[] {bounds[0][0]});
    			break;
    			
    		case 2: // we have a parameter matrix
    			noRows = bounds[0][1] - bounds[0][0] + 1;
    			noCols = bounds[1][1] - bounds[1][0] + 1;
    			int[][] parameterMatrix = new int[noRows][noCols];
    			for(int row=0; row<parameterMatrix.length; row++)
    				for(int col=0; col<parameterMatrix[row].length; col++)
    					parameterMatrix[row][col] = UNDEFINED_PARAMETER_ARRAY_ELEMENT;
    			
    			
    			this.parameterMatrices.put(parameterName, parameterMatrix);
    			this.parameterArrayOffsets.put(parameterName, new int[] {bounds[0][0],bounds[1][0]});
    			break;
    			
    		case 3:
    			noPlanes = bounds[0][1] - bounds[0][0] + 1;
       			noRows = bounds[1][1] - bounds[1][0] + 1;
    			noCols = bounds[2][1] - bounds[2][0] + 1;
    			int[][][] parameterCube = new int[noPlanes][noRows][noCols];
    			
    			for(int plane=0; plane<parameterCube.length; plane++)
    				for(int row=0; row<parameterCube[plane].length; row++)
    					for(int col=0; col<parameterCube[plane][row].length; col++)
    						parameterCube[plane][row][col] = UNDEFINED_PARAMETER_ARRAY_ELEMENT;
    			
    			this.parameterCubes.put(parameterName, parameterCube);
    			this.parameterArrayOffsets.put(parameterName, new int[] {bounds[0][0], bounds[1][0],bounds[2][0]} );
    			break;
    			
    		default:
    			throw new PreprocessorException("Sorry, matrices/arrays with more than 3 dimensions, such as '"+parameterName+
    					"' are not supported as parameters.");
    				
    		}
    		
    		print_debug("Finished parameter update of "+parameterName);
    	}
    	
    	print_debug("Finished first part of parameter array update, gonna insert values now. ");
    	// 2. Now insert values for each parameter 
    	insertValuesForParameterArrays();
    	print_debug("Finished parameter array update, inserted values. ");
    	/*for(int k=0; k<this.parameterMatrices.size(); k++) {
    		int[][] matrix = parameterMatrices.get(this.parameterArrayNames.get(k));
    		for(int l=0; l<matrix.length; l++) {
    			print_debug("\n");
    			for(int j=0; j<matrix[l].length; j++)
    				print_debug(this.parameterArrayNames.get(k)+"["+l+","+j+"] = "+matrix[l][j]+"\t");
    		}	
    	}*/
    	
    }
    
    
    /**
     * We assume that the parameter int arrays have been stored in the 
     * corresponding HashMap. We will iterate through the where-expressions
     * (that have been evaluated) and try to extract info from theses expressions
     * and apply it to the parameter array.
     *
     */
    private void insertValuesForParameterArrays() 
    	throws PreprocessorException {
    	
    	// evaluate where ... expressions
    	while(whereExpressions.size() > 0) {
    		//print_debug("Applying WHERE expression "+whereExpression.get(0)+" of expressions "+whereExpressions.toString());
    		applyExpressionToArrays(whereExpressions.remove(0));
    	}
    	
    	// here we could check if all parameter arrays have been assigned a value
    	
    	// insert parameter array values in the constraints -> or do this in the evaliator?
    }
    
    /**
     * Takes an expression, that has been specified in a WHERE statement and applies its information
     * to the parameters that were specified. At the moment we can only apply equalities and universally
     * quantified equalities.
     * 
     * @param expression
     * @throws PreprocessorException
     */
    private void applyExpressionToArrays(Expression expression) 
    	throws PreprocessorException {
    	
    	print_debug("Applying expression '"+expression+"' to arrays with restriction Mode:"+expression.getRestrictionMode());
   		switch(expression.getRestrictionMode()) {
		
		case EssenceGlobals.BINARYOP_EXPR:
			// m[..] = E   (the only case that could be interesting for us? )
			if(expression.getBinaryExpression().getOperator().getRestrictionMode() == EssenceGlobals.EQ) {
				if(expression.getBinaryExpression().getLeftExpression().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR){
					
					print_debug("Applying binary expression:"+expression.toString());
					
					NonAtomicExpression matrixElement = expression.getBinaryExpression().getLeftExpression().getNonAtomicExpression();
					if(matrixElement.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
						throw new PreprocessorException("Please access array elements by m[i,j] instead of m[i][j], as in :"+matrixElement.toString());
					if(matrixElement.getExpression().getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER)
						throw new PreprocessorException("Please access array elements by m[i,j] instead of m[i][j], as in :"+matrixElement.toString());
					
					String matrixElementName = matrixElement.getExpression().getAtomicExpression().getString();
					print_debug("Matrix element Name is "+matrixElementName);
					
					
					// get the value we want to assign it to
					Expression rightExpression = evaluator.evalExpression(expression.getBinaryExpression().getRightExpression());
					if(rightExpression.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
						throw new PreprocessorException("Sorry, can only assign integer values to a parameter array element and not :"
								+expression.toString());
					
					// get the indices of the matrix element
					Expression[] indexExpressions = matrixElement.getExpressionList();
					int[] indices = new int[indexExpressions.length];
					for(int i=0; i<indices.length; i++) {
						print_debug("Working on the "+i+"th indexexpression. and indices has length:"+indices.length);
						print_debug(i+"th indexexpression:"+indexExpressions[i].toString());
						if(indexExpressions[i].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
							throw new PreprocessorException("Cannot assign parameter value to index '"+indexExpressions[i]+
									 "' in "+expression.toString()+". Expected an integer value.");
						if(indexExpressions[i].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
							throw new PreprocessorException("Cannot assign parameter value to index '"+indexExpressions[i]+
									 "' in "+expression.toString()+". Expected an integer value.");
						
						indices[i] = indexExpressions[i].getAtomicExpression().getNumber();
					}
					print_debug("We survived the forloop1");
					
					//print_debug("Gonna insert value :"+rightExpression.getAtomicExpression().getNumber()+" into array: "
					//		+matrixElementName+" at index: "+indices[0]+", "+indices[1]);
					
					insertParameterValueIntoArray(rightExpression.getAtomicExpression().getNumber(), 
							                      matrixElementName, 
							                      indices);
					print_debug("We survived the forloop2");
				} // end if left part is an array
			} // end if op==EQ
			
			else print_message("Ignored WHERE statement:"+expression.toString());
			break;
	
			
		case EssenceGlobals.QUANTIFIER_EXPR:
			// forall i,j,.. in Domain : m[...] = E   (might be nested!)
			print_debug("Working on a quantified expression:"+expression.toString());
			
			if(expression.getQuantification().getQuantifier().getRestrictionMode() == EssenceGlobals.FORALL) {
				
				//Expression quantifiedExpression = expression.getQuantification().getExpression();
				// get the binding variables
				String[] bindingVariables = expression.getQuantification().getBindingExpression().getDomainIdentifiers().getIdentifiers();
				ArrayList<String> bindingVars = new ArrayList<String>();
				for(int j=0; j<bindingVariables.length; j++)
					bindingVars.add(bindingVariables[j]);
					
				// get the bounds/range
				Domain range = evaluator.evalExpressionInDomain(expression.getQuantification().getBindingExpression().getDomainIdentifiers().getDomain());
				if(range.getRestrictionMode() != EssenceGlobals.INTEGER_RANGE)
					throw new PreprocessorException
					("Cannot translate quantified expression, expected an integer range for the binding variables instead of :"+range.toString());
				
				if(range.getIntegerDomain().getRangeList()[0].getLowerBound().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
					throw new PreprocessorException
					("Cannot translate quantified expression, expected an integer range for the binding variables instead of :"+range.toString());
					
				if(range.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER) 
					throw new PreprocessorException
					("Cannot translate quantified expression, expected an integer range for the binding variables instead of :"+range.toString());
				int lowerBound = range.getIntegerDomain().getRangeList()[0].getLowerBound().getAtomicExpression().getNumber();
				
				
				if(range.getIntegerDomain().getRangeList()[0].getUpperBound().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
					throw new PreprocessorException
					("Cannot translate quantified expression, expected an integer range for the binding variables instead of :"+range.toString());
					
				if(range.getIntegerDomain().getRangeList()[0].getUpperBound().getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER) 
					throw new PreprocessorException
					("Cannot translate quantified expression, expected an integer range for the binding variables instead of :"+range.toString());
				int upperBound = range.getIntegerDomain().getRangeList()[0].getUpperBound().getAtomicExpression().getNumber();
				
				print_debug("Starting the insertion thing now. Got binding variables:"+bindingVars.toString()+
						" and lb:"+lowerBound+", and ub:"+upperBound+" and expression:"+expression.getQuantification().getExpression());
				
				insertBindingValueInExpression(expression.getQuantification().getExpression(), bindingVars, lowerBound, upperBound);
				break;				
			} // end if quantifier == forall
			else {
				print_message("Ignored WHERE statement: "+expression.toString()+
						". Don't know how to treat an existential quantification on a set of parameters.");
				break;
			}
			
		default:
			print_message("Ignored WHERE statement: "+expression.toString()+". Don't know how to treat it yet.");
		}	
    }
    
    /**
     * Insert all values in between lowerBound and upperBound (inclusive!) for the first binding variable in 
     * the ArrayList bindingVariables in Expression e. Recursively continue the insertion until no binding
     * variables are left, and there are no further nested quantifications. In that case, apply the expression
     * to the arrays. 
     * @param e
     * @param bindingVariables
     * @param lowerBound
     * @param upperBound
     * @throws PreprocessorException
     */
    private void insertBindingValueInExpression(Expression e, ArrayList<String> bindingVariables, int lowerBound, int upperBound) 
    	throws PreprocessorException {
    	
    	print_debug("IIIIIIIIIIIIInserting values in expression:"+e.toString()+" with bindingVars:"+bindingVariables.toString()+
    			"and lb:"+lowerBound+" and ub:"+upperBound);
    	
    	if(bindingVariables.isEmpty()) {
    		// the expression is further nested 
    		if(e.getRestrictionMode() == EssenceGlobals.QUANTIFIER_EXPR) {
    			applyExpressionToArrays(e);
    		} 
    		else { // now we can apply the expression to the parameter arrays
    			applyExpressionToArrays(e);
    		}	
    		return;
    	}
    	if(lowerBound < upperBound) {
    		for(int i=lowerBound; i<=upperBound; i++) {
    			// think about if copy is right here!
    			Expression new_e = insertValueForIdentifierInExpression(i,bindingVariables.get(0),e.copy());
    			ArrayList<String> subList = new ArrayList<String>();
    			for(int j=1;j<bindingVariables.size(); j++)
    				subList.add(bindingVariables.get(j));
    			
    			print_debug("Inserted values for expression:"+new_e.toString()+", will continue with binding vars:"+subList.toString()); 
    			insertBindingValueInExpression(new_e, subList,
    					                      lowerBound, upperBound);
    		}
    	}
    	
    	else{ 
    		for(int i = upperBound; i >= lowerBound; i--) {
    			Expression new_e = insertValueForIdentifierInExpression(i,bindingVariables.remove(0),e.copy());
    			
       			ArrayList<String> subList = new ArrayList<String>();
    			for(int j=1;j<bindingVariables.size(); j++)
    				subList.add(bindingVariables.get(j));
    			
    			print_debug("Inserted values for expression:"+new_e.toString()+", will continue with binding vars:"+subList.toString()); 
    			insertBindingValueInExpression(new_e, subList,
	                      lowerBound, upperBound);
    		}
    	}
    
    }
    
    
    /**
     * Insert value at position indices into the parameter array with name
     * parameterArrayName
     * @param value
     * @param parameterArrayName
     * @param indices
     * @throws PreprocessorException
     */
    private void insertParameterValueIntoArray(int value, String parameterArrayName, int[] indices) 
    throws PreprocessorException  {
    	
		if(!this.parameterArrayNames.contains(parameterArrayName)) 
			throw new PreprocessorException("Undefined parameter array '"+parameterArrayName
					+"'. Please define parameter arrays in the 'given' block.");
    
		int[] offsets = this.parameterArrayOffsets.get(parameterArrayName);
		
		switch(indices.length) {
		
		case 1: // vector
			print_debug("Got a parameter-vector");
			int[] parameterVector = this.parameterVectors.get(parameterArrayName);
			if(parameterVector == null)
				throw new PreprocessorException("Wrong dimensions: Cannot assign "+value+" to '"+parameterArrayName+"' - it is not a vector.");
			
			//parameterVector[indices[0]] = value;
			parameterVector[indices[0]-offsets[0]] = value;
			break;
			
			
		case 2: //2-dimensional (matrix)
			print_debug("inserting value into an array.");
			int[][] parameterMatrix = this.parameterMatrices.get(parameterArrayName);
			if(parameterMatrix == null)
				throw new PreprocessorException("Wrong dimensions: Cannot assign "+value+" to '"+parameterArrayName+"' - it is not a matrix.");
			
			int row = indices[0] - offsets[0];
			int col = indices[1] - offsets[0];
			print_debug("inserting value into an array. indices:("+row+","+col+")");
			parameterMatrix[row][col] = value;
			print_debug("INSERTED value into an array. indices:("+row+","+col+")");
			break;
			
		case 3:
			int [][][] parameterCube = this.parameterCubes.get(parameterArrayName);
			if(parameterCube == null)
				throw new PreprocessorException("Wrong dimensions: Cannot assign "+value+" to '"+parameterArrayName+"' - it is not a cube.");
			
			parameterCube[indices[0]][indices[1]][indices[2]] = value;
			break;
			
			
		default:
			throw new PreprocessorException
			("Sorry, matrices/arrays with more than 3 dimensions, such as '"+parameterArrayName+
			"' are not supported as parameters.");
		}
    	
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
		throws  PreprocessorException {

    		Expression buffer_expr = e.copy();
    		print_debug("copied expression:"+buffer_expr.toString());
    		Constant c = new Constant(new ExpressionConstant(id, new Expression(new AtomicExpression(value)) ));
    		print_debug("gonna insert the value "+value+" for this binding variable "+id);
    		this.parameters.put(id, c);
    		print_debug("inswerting the value "+value+" for this binding variable "+id);
    		Expression new_expression = evaluator.evalExpression(buffer_expr);
    		print_debug("got the new eexpression:"+new_expression.toString());
    		this.parameters.remove(id);
    		print_debug("returning the new expression:"+new_expression.toString());
    		return new_expression;
    }
    
    
    
    protected void orderDomainsOfDecisionVariables() throws PreprocessorException  { 	
    	for(int i=0; i<this.decisionVariablesNames.size(); i++) {
    		
    		Domain domain = this.decisionVariables.get(this.decisionVariablesNames.get(i));
    		if(domain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
    			this.decisionVariables.put(decisionVariablesNames.get(i), 
    				new Domain(evaluator.orderIntegerDomain(domain.getIntegerDomain())));  	
    		}
    	} 	
    }
    
    /** 
     *  Print a debug message if the DEBUG flag is set.
     *  @param s the String to be printed
     *  */

    private void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG preprocessor ] "+s);
    }

    /** %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
     print_message()
     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */

    private void print_message(String s) {
    	if(PRINT_MESSAGE)
	    System.out.println("[ Preprocessor] "+s);
    }
    
    
    private void print_verbose_message(String s) {
    	if(this.verbose_translation)
	    System.out.println("[ Preprocessor] "+s);
    }
    

}
