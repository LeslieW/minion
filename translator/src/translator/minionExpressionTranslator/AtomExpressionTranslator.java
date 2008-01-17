package translator.minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import translator.minionModel.*;
import translator.preprocessor.Parameters;
import translator.preprocessor.PreprocessorException;
import translator.conjureEssenceSpecification.Domain;
import translator.conjureEssenceSpecification.Expression;
import translator.conjureEssenceSpecification.AtomicExpression;
import translator.conjureEssenceSpecification.FunctionExpression;
import translator.conjureEssenceSpecification.NonAtomicExpression;
import translator.conjureEssenceSpecification.EssenceGlobals;


/**
 * Translates simple expressions, which are AtomicExpressions (booleans, numbers, identifiers) or 
 * NonAtomicExpressions (vector- or matrixelements). All expressions have to be fully evaluated before
 * the translation process.
 * 
 * @author andrea
 * @see AtomicExpression
 * @see NonAtomicExpression
 */

public class AtomExpressionTranslator implements MinionTranslatorGlobals {

	HashMap<String, MinionIdentifier> minionVariables;
	HashMap<String, MinionIdentifier[]> minionVectors;
	HashMap<String, MinionIdentifier[][]> minionMatrices;
	HashMap<String, MinionIdentifier[][][]> minionCubes;
	
	HashMap<String, Domain> decisionVariables;
	ArrayList<String> decisionVariablesNames;
        MinionModel minionModel;
	MinionVariableCreator variableCreator;
	Parameters parameterArrays;
	
	//GlobalConstraintTranslator globalConstraintTranslator;
	int noTmpVars;
	boolean useWatchedLiterals;
	boolean useDiscreteVariables;
	
	public AtomExpressionTranslator(HashMap<String, MinionIdentifier> minionVars,
			HashMap<String, MinionIdentifier[]> minionVecs,
			HashMap<String, MinionIdentifier[][]> minionMatrixz, 
			HashMap<String, MinionIdentifier[][][]> minionCubes, 
			ArrayList<String> decisionVarsNames, 
			HashMap<String, Domain> decisionVars, 
			MinionModel mModel, 
			boolean useWatchedLiterals, 
			boolean useDiscreteVars, Parameters parameterArrays) {	
		
		this.minionVariables = minionVars;
		this.minionVectors = minionVecs;
		this.minionMatrices = minionMatrixz;
		this.minionCubes = minionCubes;
		this.decisionVariablesNames = decisionVarsNames;
		this.decisionVariables = decisionVars;
		this.minionModel = mModel;
		this.useWatchedLiterals = useWatchedLiterals;
		this.useDiscreteVariables = useDiscreteVars;
		this.parameterArrays = parameterArrays;
		this.variableCreator = new MinionVariableCreator(minionVariables,
								 minionVectors,
								 minionMatrices,
								 this.minionCubes,
								 decisionVariables,
								 decisionVariablesNames, 
								 minionModel, 
								 useDiscreteVariables);
		this.noTmpVars = 0;
		
	}
	
	
	/**
	 * Translates Expression e to a MinionIdentifier. The Expression HAS to be 
	 * either atomic (number, boolean, identifier) or a matrix-element. 
	 * @param e the atom Expression that will be translated
	 * @return the MinionIdentifier representing e
	 * @throws TranslationUnsupportedException
	 */
	protected MinionIdentifier translateAtomExpression(Expression e)
		throws TranslationUnsupportedException, MinionException, PreprocessorException {
		
		if(e.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR)
			return translateAtomicExpression(e.getAtomicExpression());
		
		else if (e.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
		  return translateNonAtomicExpression(e.getNonAtomicExpression());
		
		else 
			throw new TranslationUnsupportedException 
			("Internal error. Trying to translate a non atom expression '"+e.toString()+"' with the AtomExpressionTranslator.");
	}

 /** Translate an atomic expression to a Minion Identifier
  * @param atom the AtomicExpression that will be translated
  * @throws TranslationUnsupportedException
  * @return the MinionIdentifier associated with this expression
   */
   protected MinionIdentifier translateAtomicExpression(AtomicExpression atom)
		throws TranslationUnsupportedException, MinionException {

	   MinionIdentifier result = null ;
	   print_debug("translating atomic expression :"+atom.toString());
	   
	   switch (atom.getRestrictionMode()) {
	    
	   case EssenceGlobals.NUMBER:                                                 // int
		   result = new MinionConstant(atom.getNumber()) ;
		   break ;

	   case EssenceGlobals.BOOLEAN:
		   result = (atom.getBool()) ? new MinionConstant(1) : new MinionConstant(0) ;
		   break ;
	    
	   case EssenceGlobals.IDENTIFIER:
		   String variableName = atom.getString();
		   boolean isInVariablesList = false;
		   for(int i=0; i<decisionVariablesNames.size(); i++) {
			   print_debug("comparing decision variable '"+decisionVariablesNames.get(i)+"' with:"+variableName);
			   if(decisionVariablesNames.get(i).equals(variableName)) {
				   isInVariablesList = true;
				   if(minionVariables.containsKey(variableName)) 
					   return minionVariables.get(variableName);
				   else variableCreator.addNewVariable(atom.getString());   
			   }
		   }
		   if(!isInVariablesList)
			   throw new TranslationUnsupportedException
			    ("Unknown decision variable: "+atom.toString());
		   result = minionVariables.get(atom.getString());
		   break ;
	    
	   default:
		   throw new TranslationUnsupportedException(
						      "Cannot translate " + atom) ;
	   }	
	   print_debug("translated atomicExpression '"+atom.toString()+"' to resulting: "+result.getClass().toString());
	   return result ;
   	}	


   
   
   /** 
    * Translate a matrix element.
    * @param matrixElement the NonAtomicExpression that will be translated
    * @return MinionIdentifier associated with matrixElement
    * @throws TranslationUnsupportedException 
    *  */
  protected MinionIdentifier translateNonAtomicExpression(NonAtomicExpression matrixElement)
	throws TranslationUnsupportedException, MinionException, PreprocessorException {
	
	  print_debug("we are translating a matrixElement: "+matrixElement.toString());
	  
	  if(matrixElement.getRestrictionMode() !=  EssenceGlobals.NONATOMIC_EXPR_BRACKET) 
		  throw new 
				TranslationUnsupportedException
				 ("Unsupported type (relation) of identifier "+matrixElement) ;      
	    
	  
	  //   matrixName [ indexExpressions ] 
	  Expression[] indexExpressions = matrixElement.getExpressionList();
	  print_debug("we got its indexExpressions");
	  
	  if(matrixElement.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
		  throw new TranslationUnsupportedException
		  ("Please access matrix elements by MATRIXNAME[vectorIndex, elementIndex] and not by MATRIXNAME[vectorIndex][elementIndex] like:"+
				  matrixElement.toString());
	  
	  String matrixName = matrixElement.getExpression().getAtomicExpression().getString();
	  
	  if(parameterArrays.isParameter(matrixName)) {
		  print_debug("We found a parameter array element: "+matrixElement.toString());
		  return translateParameterMatrixElement(matrixElement);
	  }
	  
	  // make sure that the indices are legal
	  for(int i=0; i<indexExpressions.length; i++) {
		  if(indexExpressions[i].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
			  return translateElementConstraintIndex(matrixElement);
		  
		  if(indexExpressions[i].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
			  throw new TranslationUnsupportedException
		  		("Illegal (non-atomic) index '"+indexExpressions[i]+"' for vector/matrix element:"+matrixElement.toString());
		  
		  if(indexExpressions[i].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER)
			  return translateElementConstraintIndex(matrixElement);
		  
		  else if(indexExpressions[i].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
			  throw new TranslationUnsupportedException
		  	("	Illegal (non-integer) index '"+indexExpressions[i]+"' for vector/matrix element:"+matrixElement.toString());
	  }
	  
	  if(decisionVariables.containsKey(matrixName)) {
		   if(!minionVectors.containsKey(matrixName) && !minionMatrices.containsKey(matrixName) && !minionCubes.containsKey(matrixName)) 
			   variableCreator.addNewVariable(matrixName);
		   else 
			   print_debug("The matrix is already known:"+matrixName);
	  }
	  else throw new TranslationUnsupportedException("Unknown variable or matrix-element name :"+matrixElement.toString());
	  
	  switch(indexExpressions.length) {
	  
	  case 1: // 1-dimensional 
		  // the identifier is a simple element
		  if(minionVectors.containsKey(matrixName)) {
			  MinionIdentifier[] variableVector = minionVectors.get(matrixName); 
		  
			  int matrixIndex = indexExpressions[0].getAtomicExpression().getNumber();
			  int offset = minionModel.getVectorOffset(matrixName);//variableVector[0].getOriginalName());
			  matrixIndex = matrixIndex - offset;
		  
			  if(matrixIndex < 0 || matrixIndex >= variableVector.length)
				  throw new TranslationUnsupportedException		  
				  ("The index '"+matrixIndex+"' assigned to '"+matrixName+"' is out of bounds.");
		  
			  if(variableVector[matrixIndex]==null) {
					 variableCreator.addVectorElement(matrixName, matrixIndex);	
				  }
			  
			  return variableVector[matrixIndex];
		  }	
		  // the identifier is a vector 
		  else if(minionMatrices.containsKey(matrixName)) 
			throw new TranslationUnsupportedException
			("Cannot use vector '"+matrixElement.toString()+"' as an atomic expression.");
		  
		  else  
			  	throw new TranslationUnsupportedException
			  	("Unknown variable: "+matrixElement.toString());
		  
		  
	  case 2: //2-dimensional
		  print_debug("ok, we have a 2-dim matrix: "+matrixElement.toString());
		  MinionIdentifier[][] variableMatrix = minionMatrices.get(matrixName);
		  if(variableMatrix == null)
			  throw new TranslationUnsupportedException
			  	("Unknown variable matrix: "+matrixElement.toString());
		  
		  print_debug("and we know it!!! :) ");
		  int vectorIndex = indexExpressions[0].getAtomicExpression().getNumber();
		  int elementIndex = indexExpressions[1].getAtomicExpression().getNumber();
		  
		  print_debug("WITHOUT OFFSET: the vectorIndex is:"+vectorIndex+", and the elementIndex:"+elementIndex);
		  
		  int[] offsets = minionModel.getMatrixOffsets(matrixName);
		  
		  print_debug("offsets[0]:"+offsets[0]+", and offsets[1]:"+offsets[1]);
		  
		  vectorIndex = vectorIndex - offsets[0];
		  elementIndex = elementIndex - offsets[1];
		  
		  print_debug("WITH OFFSET: the vectorIndex is:"+vectorIndex+", and the elementIndex:"+elementIndex);
		  
		  if(vectorIndex <0 || vectorIndex >= variableMatrix.length)
			  throw new TranslationUnsupportedException		  
			  ("The vector-index '"+(vectorIndex+offsets[0])+"' assigned to '"+matrixName+"' is out of bounds.");
		
		  if(elementIndex <0 || elementIndex >= variableMatrix[vectorIndex].length)
			  throw new TranslationUnsupportedException		  
			  ("The element-index '"+(elementIndex+offsets[1])+"' assigned to '"+matrixName+"' is out of bounds.");
		  
		  if(variableMatrix[vectorIndex][elementIndex]==null) {
			 variableCreator.addMatrixElement(matrixName, vectorIndex, elementIndex);	
		  }
		  
		  return variableMatrix[vectorIndex][elementIndex];
	
	
	  case 3: // 3-dimensional
		  print_debug("Translating 3-dim matrix element: "+matrixElement.toString());
		  MinionIdentifier[][][] variableCube = minionCubes.get(matrixName);
		  if(variableCube == null)
			  throw new TranslationUnsupportedException
			  	("Unknown variable cube: "+matrixElement.toString());
			  
		  int matrixIndex = indexExpressions[0].getAtomicExpression().getNumber();
		  int vectIndex = indexExpressions[1].getAtomicExpression().getNumber();
		  int elemIndex = indexExpressions[2].getAtomicExpression().getNumber();
		  
		  int[] cOffsets = minionModel.getCubeOffsets(matrixName);
		  matrixIndex = matrixIndex - cOffsets[0];
		  vectIndex = vectIndex - cOffsets[1];
		  elemIndex = elemIndex - cOffsets[2];
		  
		  if(matrixIndex <0 || matrixIndex >= variableCube.length)
			  throw new TranslationUnsupportedException		  
			  ("The matrix-index '"+(matrixIndex+cOffsets[0])+"' assigned to '"+matrixName+"' is out of bounds in :"+matrixElement.toString()); 
			  
		  if(vectIndex <0 || vectIndex >= variableCube[matrixIndex].length)
			  throw new TranslationUnsupportedException		  
			  ("The vector-index '"+(vectIndex+cOffsets[1])+"' assigned to '"+matrixName+"' is out of bounds in:"+matrixElement.toString());
		
		  if(elemIndex <0 || elemIndex >= variableCube[matrixIndex][vectIndex].length)
			  throw new TranslationUnsupportedException		  
			  ("The element-index '"+(elemIndex+cOffsets[2])+"' assigned to '"+matrixName+"' is out of bounds in:"+matrixElement.toString());  
		  
		  // TODO: what if the element has not yet been initialised?
		  
		  if(variableCube[matrixIndex][vectIndex][elemIndex]==null) {
				 variableCreator.addCubeElement(matrixName, matrixIndex, vectIndex, elemIndex);	
			  }
		  
		  return variableCube[matrixIndex][vectIndex][elemIndex];
		  
	  default: // multi-dimensional
			  throw new TranslationUnsupportedException
			  ("Multi-dimensional matrices (over 2-dimensions) are not supported yet, sorry.");
	  
	  
	  }
  }
	  
  /**
   * Translate a matrixElement that is a parameter. Needs to be done here, since we cannot insert parameter
   * values for constant arrays that appear in quantifications and are indexed by binding variables.
   * 
   * @param matrixElement
   * @return the MinionConstant holding the value of the parameter
   * @throws TranslationUnsupportedException
   * @throws PreprocessorException
   */
  private MinionIdentifier translateParameterMatrixElement(NonAtomicExpression matrixElement) 
  	throws TranslationUnsupportedException, PreprocessorException {
	  
	  String arrayName = matrixElement.getExpression().getAtomicExpression().getString();
	  Expression[] indexExpressions = matrixElement.getExpressionList();
	  
	  // make sure that the indices are legal
	  for(int i=0; i<indexExpressions.length; i++) {
		  if(indexExpressions[i].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
			throw new TranslationUnsupportedException
				("Illegal (non-atomic) index '"+indexExpressions[i]+"' for parameter array element:"+matrixElement.toString());  
		  
		  if(indexExpressions[i].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
			  throw new TranslationUnsupportedException
				("Cannot use decision variable as index for parameter array element:"+matrixElement.toString());  
	  }
	  
	  switch(indexExpressions.length) {
	  
	  
	  case 1: // vector
		  int index = indexExpressions[0].getAtomicExpression().getNumber();
		  if(!parameterArrays.isParameterVector(arrayName))
			  throw new TranslationUnsupportedException
			  	("Cannot apply parameter array element '"+matrixElement.toString()+"'.'"+arrayName+"' is not a known parameter vector.");
	  
		  int vectorValue = parameterArrays.getVectorElementAt(arrayName, index);
		  return new MinionConstant(vectorValue);
	  
	  case 2: // matrix
		  int rowIndex = indexExpressions[0].getAtomicExpression().getNumber();
		  int colIndex = indexExpressions[1].getAtomicExpression().getNumber();
		  if(!parameterArrays.isParameterMatrix(arrayName))
			  throw new TranslationUnsupportedException
			  	("Cannot apply parameter array element '"+matrixElement.toString()+"'.'"+arrayName+"' is not a known parameter matrix."); 
	  
		  int matrixValue = parameterArrays.getMatrixElementAt(arrayName, rowIndex, colIndex);
		  return new MinionConstant(matrixValue);
	  
	  case 3: // cube
		  int plane = indexExpressions[0].getAtomicExpression().getNumber();
		  int row = indexExpressions[1].getAtomicExpression().getNumber();
		  int col = indexExpressions[2].getAtomicExpression().getNumber();
		  if(!parameterArrays.isParameterCube(arrayName))
			  throw new TranslationUnsupportedException
			  	("Cannot apply parameter array element '"+matrixElement.toString()+"'.'"+arrayName+"' is not a known parameter cube."); 
		  
		  int cubeValue = parameterArrays.getCubeElementAt(arrayName, plane, row, col);
		  return new MinionConstant(cubeValue);
		  
	  default:
		  throw new TranslationUnsupportedException
		  	("Parameter arrays with more than 3 dimensions are not supported yet:"+matrixElement.toString());
	  }
	 
  }
  
  /**
   * 
   * @param matrixElement
   * @return
   * @throws TranslationUnsupportedException
   * @throws MinionException
   */
  
  private MinionIdentifier translateElementConstraintIndex(NonAtomicExpression matrixElement) 
  	throws TranslationUnsupportedException, MinionException, PreprocessorException {
	  	  
	  print_debug("we are TRanslating a SPECIAL matrixElement: "+matrixElement.toString());
	      
	  //   matrixName [ indexExpressions ] 	  
	  Expression[] indexExpressions = matrixElement.getExpressionList();
	  print_debug("we got its indexExpressions");
	  
	  if(matrixElement.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
		  throw new TranslationUnsupportedException("Please access matrix elements by MATRIXNAME[vectorIndex, elementIndex] and not by MATRIXNAME[vectorIndex][elementIndex] like:"+
				  matrixElement.toString());
	  
	  String matrixName = matrixElement.getExpression().getAtomicExpression().getString();
	  
	  print_debug("we got its name");
	  if(matrixName!= null)
		  print_debug("the matrixname is:"+matrixName);
	  
	  
	  if(decisionVariables.containsKey(matrixName)) {
		  if(!minionVectors.containsKey(matrixName) && !minionMatrices.containsKey(matrixName)) 
			   variableCreator.addNewVariable(matrixName);	
	  }
	  else throw new TranslationUnsupportedException("Unknown variable or matrix-element name :"+matrixElement.toString());
	  
	  switch(indexExpressions.length) {
	  
	  case 1: // 1-dimensional 
		  // the identifier is a simple element
		  if(minionVectors.containsKey(matrixName)) {
	
			  if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR){
				  NonAtomicExpression indexVectorElement = indexExpressions[0].getNonAtomicExpression();
				  MinionIdentifier indexVariable = translateNonAtomicExpression(indexVectorElement);
	
					  MinionIdentifier[] originalVector = minionVectors.get(matrixName);
				  
					  MinionIdentifier freshVariable = null;
					  if(!useDiscreteVariables) {
						  freshVariable = new  MinionBoundsVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
								  "freshVariableElementConstraint"+(noTmpVars++));
						  minionModel.addBoundsVariable((MinionBoundsVariable) freshVariable);
					  } else {
						  freshVariable = new  MinionDiscreteVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
								  "freshVariableElementConstraint"+(noTmpVars++));
						  minionModel.addDiscreteVariable((MinionDiscreteVariable) freshVariable);
					  }
							  
					  
					  MinionElementConstraint elementConstraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(matrixName),
						  																  indexVariable,
						  																  freshVariable);
					  minionModel.addConstraint(elementConstraint);
					  return freshVariable;
				  	
			  }
			  else if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				  
				  if(indexExpressions[0].getRestrictionMode() != EssenceGlobals.IDENTIFIER)
					  throw new TranslationUnsupportedException("Internal error. Expected an identifier (or number) in the index of '"+matrixElement.toString()+
							  "', instead of: "+indexExpressions[0].toString());
				  
				  String identifier = indexExpressions[0].getAtomicExpression().getString();
				  MinionIdentifier[] originalVector = minionVectors.get(matrixName);
				  
				  MinionIdentifier freshVariable = null;
				  if(!useDiscreteVariables) {
					  freshVariable = new MinionBoundsVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
						  								"freshVariableElementConstraint"+(noTmpVars++));
				  	 minionModel.addBoundsVariable((MinionBoundsVariable) freshVariable);
				  }
				  else {
					  freshVariable = new MinionDiscreteVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
								"freshVariableElementConstraint"+(noTmpVars++));
					  minionModel.addDiscreteVariable((MinionDiscreteVariable) freshVariable);
					  
				  }
				  if(minionVariables.containsKey(identifier)) {
					  MinionElementConstraint elementConstraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(matrixName), 
							  																  minionVariables.get(identifier), 
							  																  freshVariable);
					  minionModel.addConstraint(elementConstraint);
					  return freshVariable;
				  }
				  else throw new TranslationUnsupportedException("Matrix-element '"+matrixElement.toString()+"' is indexed with an unknown identifier:"
						  +identifier);
			  }
			  else 	  throw new TranslationUnsupportedException("Illegal matrix index:"+indexExpressions[0].toString()+" in expression '"+
						  matrixElement.toString()+"'. Has to be a number or identifier.");
	
		  }	
		  // the identifier is a vector 
		  else if(minionMatrices.containsKey(matrixName)) 
			throw new TranslationUnsupportedException
			("Cannot use vector '"+matrixElement.toString()+"' as an atomic expression.");
		  
		  else  
			  	throw new TranslationUnsupportedException
			  	("Unknown vector or matrix: "+matrixElement.toString());
		  
		  
	  case 2: //2-dimensional
		  if(minionMatrices.containsKey(matrixName)) {
			  
			  MinionIdentifier[][] originalMatrix = minionMatrices.get(matrixName);
			  int matrixOffsets[] = minionModel.getMatrixOffsets(matrixName);
			  
			  if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				  // MATRIX [ num, NonAtomicExpr ]   
				  if(indexExpressions[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
					  print_debug("We have m[num, ?]");
					  int vectorIndex = indexExpressions[0].getAtomicExpression().getNumber();		  
					  vectorIndex = vectorIndex - matrixOffsets[0];
					  
					  MinionIdentifier elementIndexVariable = translateAtomExpression(indexExpressions[1]);	
					  //MinionIdentifier elementIndexVariable = translateNonAtomicExpression(indexExpressions[1].getNonAtomicExpression());					  
					  MinionIdentifier freshVariable = variableCreator.addFreshVariable(0,originalMatrix[0].length, 
							  									                    "freshVariable"+noTmpVars++,useDiscreteVariables);
						  
					  MinionElementConstraint constraint = new MinionElementConstraint(
							  											"row(m"+minionModel.getMatrixIndex(matrixName)+","+vectorIndex+")",
					  													elementIndexVariable,
					  													freshVariable);
					  minionModel.addConstraint(constraint);
					  return freshVariable;
					  
					  
				  } // MATRIX [ ID, ?]  
				  else if(indexExpressions[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
					    
					    switch(indexExpressions[1].getRestrictionMode()){
					    	
					    // matrix[ID, atom]
					    case EssenceGlobals.ATOMIC_EXPR:
					    	// matrix[ID, num]
					    	MinionIdentifier freshVariable = null;
					    	if(indexExpressions[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
					    		int colIndex = indexExpressions[1].getAtomicExpression().getNumber();
					    		colIndex = colIndex - matrixOffsets[1];
					    		freshVariable = variableCreator.addFreshVariable(0, originalMatrix.length, 
					    				                                           "freshVariable"+noTmpVars++, useDiscreteVariables);
					    		MinionIdentifier elementIndexVariable = translateAtomicExpression(indexExpressions[0].getAtomicExpression());
					    		MinionElementConstraint constraint = new MinionElementConstraint(
					    				              "col(m"+minionModel.getMatrixIndex(matrixName)+","+colIndex+")",
											           elementIndexVariable,
												       freshVariable);
					    		minionModel.addConstraint(constraint);
					    		return freshVariable;
					    	}
					    	
					    	// matrix[ID1, ID2] --> v' = [m00, m01, .... ,]   and index = ID1*(noCols-1) + ID2
					    	else if(indexExpressions[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
					    		
					    		int noCols = originalMatrix[0].length;
					    		int noRows = originalMatrix.length;
					    		
					    		MinionIdentifier[] flattenedMatrix = new MinionIdentifier[noRows*noCols];
					    		
					    		for(int row=0; row<noRows; row++) {	
					    			for(int col=0; col<noCols; col++) {
					    				flattenedMatrix[row*noCols + col] = originalMatrix[row][col]; 
					    			}	
					    		}
					    		// only do this, if we have not created the vector yet
					    		if(!minionModel.containsVector(FLATTENED_MATRIX_NAME+matrixName))
					    			minionModel.addKnownIdentifierVector(flattenedMatrix, 0, FLATTENED_MATRIX_NAME+matrixName);
					    		// f = Id1*noRows + Id2
					    		MinionIdentifier elementVar = variableCreator.addFreshVariable(0,noCols*noRows,
					    				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
					    		MinionWeightedSumConstraint indexConstraint = new MinionWeightedSumConstraint(
					    				                              new MinionIdentifier[] {translateAtomicExpression(indexExpressions[0].getAtomicExpression()),
					    				                            		                  translateAtomicExpression(indexExpressions[1].getAtomicExpression())},
					    				                              new MinionConstant[] {new MinionConstant(noRows-1), new MinionConstant(1) },
					    				                              elementVar);
					    		minionModel.addConstraint(indexConstraint);
					    		freshVariable = variableCreator.addFreshVariable(0,noCols*noRows,
	    				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
					    		MinionElementConstraint constraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(FLATTENED_MATRIX_NAME+matrixName),
					    				                                                         elementVar, freshVariable);
					    		minionModel.addConstraint(constraint);
					    		return freshVariable;
					    	}
					    	else throw new TranslationUnsupportedException
					    		("Expected number or identifier as matrix index in :"+matrixElement.toString());
					    	
					    	
					    case EssenceGlobals.NONATOMIC_EXPR: //matrix[atom  , nonAtom]
					    	
					    	
					    	
					    default: throw new TranslationUnsupportedException
							  ("Illegal index for matrix in :"+matrixElement.toString()+". Expected integer or variable index.");
					    }	
				  }
				  else throw new TranslationUnsupportedException
				  ("Illegal matrix index '"+indexExpressions[0].toString()+" in: "+matrixElement.toString());
			  }
				  // MATRIX [ NonAtomicExpr, E ]
			  else { //if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
			  
				  switch(indexExpressions[1].getRestrictionMode()) {
				  
				  // matrix[nonAtom, Atom] 
				  case EssenceGlobals.ATOMIC_EXPR:
					  // matrix[ nonAtomic, num ]
					  if(indexExpressions[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
						  int colIndex = indexExpressions[1].getAtomicExpression().getNumber();
						  colIndex = colIndex - matrixOffsets[1];
						  MinionIdentifier vectorIndexVar = translateNonAtomicExpression(indexExpressions[0].getNonAtomicExpression());
						  MinionIdentifier freshVariable = variableCreator.addFreshVariable(originalMatrix[0][0].getLowerBound(),
								  originalMatrix[0][0].getUpperBound(),   
 				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
						  MinionElementConstraint constraint = new MinionElementConstraint(
								                                               "col(m"+minionModel.getMatrixIndex(matrixName)+","+colIndex+")",
								                                                vectorIndexVar,
								                                                freshVariable);
						  minionModel.addConstraint(constraint);
						  return freshVariable;
						  
					  } // matrix[ nonAtomic, ID ] matrix[ID1, ID2] --> v' = [m00, m01, .... ,]   and index = ID1*(noCols-1) + ID2
					  else if(indexExpressions[1].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
						  MinionIdentifier vectorIndexVar = translateNonAtomicExpression(indexExpressions[0].getNonAtomicExpression());
						  MinionIdentifier colIndexVar = translateAtomicExpression(indexExpressions[0].getAtomicExpression());
						  
				    		int noCols = originalMatrix[0].length;
				    		int noRows = originalMatrix.length;
				    		
				    		MinionIdentifier[] flattenedMatrix = new MinionIdentifier[noRows*noCols];
				    		
				    		for(int row=0; row<noRows; row++) {	
				    			for(int col=0; col<noCols; col++) {
				    				flattenedMatrix[row*noCols + col] = originalMatrix[row][col]; 
				    			}	
				    		}
				    		if(!minionModel.containsVector(FLATTENED_MATRIX_NAME+matrixName))
				    			minionModel.addKnownIdentifierVector(flattenedMatrix, 0, FLATTENED_MATRIX_NAME+matrixName);
				    		// f = Id1*(noRows-1) + Id2
				    		MinionIdentifier elementVar = variableCreator.addFreshVariable(0,noCols*noRows,
				    				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
				    		MinionWeightedSumConstraint indexConstraint = new MinionWeightedSumConstraint(
				    				                              new MinionIdentifier[] {vectorIndexVar, colIndexVar},
				    				                              new MinionConstant[] {new MinionConstant(noRows-1), new MinionConstant(1) },
				    				                              elementVar);
				    		minionModel.addConstraint(indexConstraint);
				    		MinionIdentifier freshVariable = variableCreator.addFreshVariable(0,noCols*noRows,
   				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
				    		MinionElementConstraint constraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(FLATTENED_MATRIX_NAME+matrixName),
				    				                                                         elementVar, freshVariable);
				    		minionModel.addConstraint(constraint);
				    		return freshVariable;
						  
					  }
					  else 	throw new TranslationUnsupportedException
					  ("Illegal index for matrix in :"+matrixElement.toString()+". Expected integer or variable index.");
				  
					  
					  // matrix[ nonAtom, nonAtom], matrix[ID1, ID2] --> v' = [m00, m01, .... ,]   and index = ID1*(noCols-1) + ID2
				  case EssenceGlobals.NONATOMIC_EXPR:
					  
					  MinionIdentifier vectorIndexVar = translateNonAtomicExpression(indexExpressions[0].getNonAtomicExpression());
					  MinionIdentifier colIndexVar = translateNonAtomicExpression(indexExpressions[1].getNonAtomicExpression());
					  
			    		int noCols = originalMatrix[0].length;
			    		int noRows = originalMatrix.length;
			    		
			    		MinionIdentifier[] flattenedMatrix = new MinionIdentifier[noRows*noCols];
			    		
			    		for(int row=0; row<noRows; row++) {	
			    			for(int col=0; col<noCols; col++) {
			    				flattenedMatrix[row*noCols + col] = originalMatrix[row][col]; 
			    			}	
			    		}
			    		if(!minionModel.containsVector(FLATTENED_MATRIX_NAME+matrixName))
			    			minionModel.addKnownIdentifierVector(flattenedMatrix, 0, FLATTENED_MATRIX_NAME+matrixName);
			    		// f = Id1*(noRows-1) + Id2
			    		MinionIdentifier elementVar = variableCreator.addFreshVariable(0,noCols*noRows,
			    				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
			    		MinionWeightedSumConstraint indexConstraint = new MinionWeightedSumConstraint(
			    				                              new MinionIdentifier[] {vectorIndexVar, colIndexVar},
			    				                              new MinionConstant[] {new MinionConstant(noRows-1), new MinionConstant(1) },
			    				                              elementVar);
			    		minionModel.addConstraint(indexConstraint);
			    		MinionIdentifier freshVariable = variableCreator.addFreshVariable(0,noCols*noRows,
				                 "freshVariable"+noTmpVars++,useDiscreteVariables);
			    		MinionElementConstraint constraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(FLATTENED_MATRIX_NAME+matrixName),
			    				                                                         elementVar, freshVariable);
			    		minionModel.addConstraint(constraint);
			    		return freshVariable;
			    		
				  default:
					  throw new TranslationUnsupportedException("Illegal matrix index in :"+matrixElement.toString()+
							  ".Expected integer/parameter or decision variable.");
				  
				  }
				
				  
			  }
			  
		  }
		  else throw new TranslationUnsupportedException("Unknown matrix :"+matrixElement.toString());
		  
	
		  
	  default: // multi-dimensional
			  throw new TranslationUnsupportedException
			  ("Multi-dimensional matrices (over 2-dimensions) are not supported yet, sorry.");
	  
	  
	  }
	  
	  
	  //return null;
  }
  
  
  /**
   * 
   * @param matrixElement
   * @return
   * @throws TranslationUnsupportedException
   * @throws MinionException
   */
  
  protected MinionIdentifier translateElementConstraintIndex(NonAtomicExpression matrixElement, MinionIdentifier element) 
  	throws TranslationUnsupportedException, MinionException, PreprocessorException {
	  	  
	  print_debug("we are TRanslating a SPECIAL matrixElement: "+matrixElement.toString());
	      
	  //   matrixName [ indexExpressions ] 	  
	  Expression[] indexExpressions = matrixElement.getExpressionList();
	  print_debug("we got its indexExpressions");
	  
	  if(matrixElement.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
		  throw new TranslationUnsupportedException("Please access matrix elements by MATRIXNAME[vectorIndex, elementIndex] and not by MATRIXNAME[vectorIndex][elementIndex] like:"+
				  matrixElement.toString());
	  
	  String matrixName = matrixElement.getExpression().getAtomicExpression().getString();
	  
	  print_debug("we got its name");
	  if(matrixName!=null)
		  print_debug("matrixname is:"+matrixName);

	  print_debug("after printing matrixname");
	  
	  if(decisionVariables.containsKey(matrixName)) {
		  if(!minionVectors.containsKey(matrixName) && !minionMatrices.containsKey(matrixName)) 
			   variableCreator.addNewVariable(matrixName);	
	  }
	  else throw new TranslationUnsupportedException("Unknown variable or matrix-element name :"+matrixElement.toString());
	  
	  switch(indexExpressions.length) {
	  
	  case 1: // 1-dimensional 
		  // the identifier is a simple element
		  if(minionVectors.containsKey(matrixName)) {
	
			  if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR){
				  NonAtomicExpression indexVectorElement = indexExpressions[0].getNonAtomicExpression();
				  MinionIdentifier indexVariable = translateNonAtomicExpression(indexVectorElement);
	/*
				  if(minionVectors.containsKey(matrixName)) {*/
					  MinionIdentifier[] originalVector = minionVectors.get(matrixName);
					  
					  if(element == null) {
						  if(!useDiscreteVariables) {						  
							  element = new  MinionBoundsVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
									  "freshVariableElementConstraint"+(noTmpVars++));
					  		  minionModel.addBoundsVariable((MinionBoundsVariable) element);
						  } else {
							  element = new  MinionDiscreteVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
									  "freshVariableElementConstraint"+(noTmpVars++));
					  		  minionModel.addDiscreteVariable((MinionDiscreteVariable) element);							  
						  }
						  
					  }
					  MinionElementConstraint elementConstraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(matrixName),
						  																  indexVariable,
						  																  element);
					  minionModel.addConstraint(elementConstraint);
					  return element;
				/*  }
				  else if (minionMatrices.containsKey(matrixName)) 
					  throw new TranslationUnsupportedException("Expecting a matrix element and not a vector:"+matrixElement.toString());
					
				  else throw new TranslationUnsupportedException
				  	("Unknown vector or matrix element "+matrixElement.toString());*/
			  }
			  else if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				  
				  if(indexExpressions[0].getRestrictionMode() != EssenceGlobals.IDENTIFIER)
					  throw new TranslationUnsupportedException("Expected an identifier (or number) in the index of '"+matrixElement.toString()+
							  "', instead of: "+indexExpressions[0].toString());
				  
				  String identifier = indexExpressions[0].getAtomicExpression().getString();
				  MinionIdentifier[] originalVector = minionVectors.get(matrixName);
				  if(!useDiscreteVariables) {						  
					  element = new  MinionBoundsVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
							  "freshVariableElementConstraint"+(noTmpVars++));
			  		  minionModel.addBoundsVariable((MinionBoundsVariable) element);
				  } else {
					  element = new  MinionDiscreteVariable(originalVector[0].getLowerBound(),originalVector[0].getUpperBound(),
							  "freshVariableElementConstraint"+(noTmpVars++));
			  		  minionModel.addDiscreteVariable((MinionDiscreteVariable) element);							  
				  }
				  
				  if(minionVariables.containsKey(identifier)) {
					  MinionElementConstraint elementConstraint = new MinionElementConstraint("v"+minionModel.getVectorIndex(matrixName), 
							  																  minionVariables.get(identifier), 
							  																  element);
					  minionModel.addConstraint(elementConstraint);
					  return element;
				  }
				  else throw new TranslationUnsupportedException("Matrix-element '"+matrixElement.toString()+"' is indexed with an unknown identifier:"
						  +identifier);
			  }
			  else 	  throw new TranslationUnsupportedException("Illegal matrix index:"+indexExpressions[0].toString()+" in expression '"+
						  matrixElement.toString()+"'. Has to be a number or identifier.");
	
		  }	
		  // the identifier is a vector 
		  else if(minionMatrices.containsKey(matrixName)) 
			throw new TranslationUnsupportedException
			("Cannot use vector '"+matrixElement.toString()+"' as an atomic expression.");
		  
		  else  
			  	throw new TranslationUnsupportedException
			  	("Unknown vector or matrix: "+matrixElement.toString());
		  
		  
	  case 2: //2-dimensional
		  if(minionMatrices.containsKey(matrixName)) {
			  
			  if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				  // MATRIX [ num, NonAtomicExpr ]   
				  if(indexExpressions[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
					  int vectorIndex = indexExpressions[0].getAtomicExpression().getNumber();
					 
					  MinionIdentifier[][] originalMatrix = minionMatrices.get(matrixName);
					  int matrixOffsets[] = minionModel.getMatrixOffsets(matrixName);
					  vectorIndex = vectorIndex - matrixOffsets[0];
					  
					  
					  MinionIdentifier elementIndexVariable = translateNonAtomicExpression(indexExpressions[1].getNonAtomicExpression());
					  if(element== null) {
						  if(!useDiscreteVariables) {
							  element = new MinionBoundsVariable(0, originalMatrix[0].length, 
							  									                    "freshVariable"+noTmpVars++);
						  	  minionModel.addBoundsVariable((MinionBoundsVariable) element);
						  }
						  else {
							  element = new MinionDiscreteVariable(0, originalMatrix[0].length, 
					                    "freshVariable"+noTmpVars++);
							  minionModel.addDiscreteVariable((MinionDiscreteVariable) element);							  
						  }
					  }
					  MinionElementConstraint constraint = new MinionElementConstraint(
							  											"row(m"+minionModel.getMatrixIndex(matrixName)+","+vectorIndex+")",
					  													elementIndexVariable,
					  													element);
					  minionModel.addConstraint(constraint);
					  return element;
					  
					  
				  } // MATRIX [ ID, NonAtomicExpression]  ---> cannot translate to row(MATRIX, ID) because MINION only supportes row(MATRIX,int)
				  else if(indexExpressions[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
					 throw new TranslationUnsupportedException
					 ("Cannot translate matrix element with a variable as vectorIndex :"+indexExpressions[0].getAtomicExpression().getString()+
							 " in '"+matrixElement.toString()+"'. The MINION input language only supports constant vector indicies, sorry.");
				  }
				  else throw new TranslationUnsupportedException
				  ("Illegal matrix index '"+indexExpressions[0].toString()+" in: "+matrixElement.toString());
			  }
				  // MATRIX [ NonAtomicExpr, E ]
			  else //if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR)
			  
				  throw new TranslationUnsupportedException
					 ("Cannot translate matrix element with a variable vectorIndex :"+indexExpressions[0].toString()+
							 " in '"+matrixElement.toString()+"'. The MINION input language only supports constant vector indicies, sorry.");
				  
			  
			  
		  }
		  else throw new TranslationUnsupportedException("Unknown matrix :"+matrixElement.toString());
		  
	
		  
	  default: // multi-dimensional
			  throw new TranslationUnsupportedException
			  ("Multi-dimensional matrices (over 2-dimensions) are not supported yet, sorry.");
	  
	  
	  }
	  
	  
	  //return null;
  }
  
  /**
   * Translates an expression, that is an atom and a stand-alon constraint. This expression can either be
   * an identifier (variable or matrix-element), number or boolean value. If the expression is  false, a
   * warning is printed. If the expression is 'true', it is ignored. Variables and numbers are set to equal 
   * one, meaning that they are true.
   * @param e an atom Expression, either of subtype AtomicExpression or NonAtomicExpression
   * @throws TranslationUnsupportedException
   * @throws MinionException
   */
  
  protected void translateSingleAtomExpression(Expression e) 
  	throws TranslationUnsupportedException, MinionException, PreprocessorException {
   
	  if(e.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		  if(e.getAtomicExpression().getRestrictionMode() == EssenceGlobals.BOOLEAN) {
			  if(!e.getAtomicExpression().getBool())
				  print_message("One constraint was evaluated to 'false'. Your problem is therefore not solvable.");
		 
		  }	
	  }
	  else print_message("One of the constraints was evaluated the single atom expression: '"+e.toString()
			  	+"', and will be interpreted as being true.");
	  minionModel.addEqualityConstraint(translateAtomExpression(e), new MinionConstant(1));
  }
  
  
  
 
  
  
  /**
   * 
   * @param constraint
   * @return
   * @throws TranslationUnsupportedException
   * @throws MinionException
   */
  
	protected MinionReifiableConstraint translateGlobalConstraint(Expression constraint) 
	throws TranslationUnsupportedException, MinionException, PreprocessorException {
	
	if(constraint.getRestrictionMode() != EssenceGlobals.FUNCTIONOP_EXPR) 
		throw new TranslationUnsupportedException
			("Internal error. Trying to translate non-global constraint '"+constraint.toString()+
					"' with GlobalConstraintTranslator.");
	
	FunctionExpression globalConstraint = constraint.getFunctionExpression();
	
	switch(globalConstraint.getRestrictionMode()) {
	
	case EssenceGlobals.ALLDIFF:
		return translateAllDifferent(globalConstraint);
		
	case EssenceGlobals.ELEMENT:
		return translateElementConstraint(globalConstraint);
		
	default:
		throw new TranslationUnsupportedException
			("Translation of global constraint '"+constraint.toString()+"' not supported yet, sorry.");
		
	}
	
}


/**
* Translate and alldifferent-constraint
* 
* @param globalConstraint
* @throws TranslationUnsupportedException
*/

private MinionReifiableConstraint translateAllDifferent(FunctionExpression globalConstraint) 
	throws TranslationUnsupportedException, MinionException {
	
	print_debug("translating the alldifferent constraint: "+globalConstraint.toString());
	
	if(isKnownVector(globalConstraint.getExpression1())){
		// we impose allDiff on a simple vector
		if(globalConstraint.getExpression1().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			String parameterName = globalConstraint.getExpression1().getAtomicExpression().getString();
			return new MinionAllDifferent(minionVectors.get(parameterName),
					 "v"+minionModel.getVectorIndex(parameterName));
		}
		// we impose allDiff on a vector of a 2-dim matrix
		else { 
			Expression[] indexExpressions = globalConstraint.getExpression1().getNonAtomicExpression().getExpressionList();	
			String matrixName = globalConstraint.getExpression1().getNonAtomicExpression().getExpression().getAtomicExpression().getString();
			
			if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				if(indexExpressions[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
					int vectorIndex = indexExpressions[0].getAtomicExpression().getNumber();
					MinionIdentifier[][] matrix = minionMatrices.get(matrixName);
						// calculate Offset!!!
					int offsets[] = minionModel.getMatrixOffsets(matrixName);
					return new MinionAllDifferent(matrix[vectorIndex-offsets[0]], "row(m"+minionModel.getMatrixIndex(matrixName)+","
														+(vectorIndex-offsets[0])+")");
				}
			}
		}
	}
	throw new TranslationUnsupportedException
	("Infeasible allDifferent constraint:"+globalConstraint.toString());
}


/**
 * Translate and alldifferent-constraint
 * @return the MinionConstraint representing the global constraint
 * @param globalConstraint
 * @throws TranslationUnsupportedException
 */

/*	private MinionReifiableConstraint translateToAllDifferent(FunctionExpression globalConstraint) 
		throws TranslationUnsupportedException, MinionException {
		
		if(isKnownVector(globalConstraint.getExpression1())){
			// we impose allDiff on a simple vector
			if(globalConstraint.getExpression1().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
				String parameterName = globalConstraint.getExpression1().getAtomicExpression().getString();
				return new MinionAllDifferent(minionVectors.get(parameterName), "v"+minionModel.getVectorIndex(minionVectors.get(parameterName)));
			}
			// we impose allDiff on a vector of a 2-dim matrix
			else { 
				Expression[] indexExpressions = globalConstraint.getExpression1().getNonAtomicExpression().getExpressionList();	
				String matrixName = globalConstraint.getExpression1().getNonAtomicExpression().getExpression().getAtomicExpression().getString();
				
				if(indexExpressions[0].getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
					if(indexExpressions[0].getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
						int vectorIndex = indexExpressions[0].getAtomicExpression().getNumber();
						MinionIdentifier[][] matrix = minionMatrices.get(matrixName);
						int offsets[] = minionModel.getMatrixOffsets(matrixName);
						return new MinionAllDifferent(matrix[vectorIndex-offsets[0]],
								"row(m"+minionModel.getMatrixIndex(matrix)+","+(vectorIndex-offsets[0])+")");
					}
				}
			}
		}
		throw new TranslationUnsupportedException
		("Unknown identifier in global constraint "+globalConstraint.toString());
	}
*/
/**
 * Translate an element-constraint.
 * 
 * @param globalConstraint the FunctionExpression that contains an element constraint
 * @throws TranslationUnsupportedException is thrown if any of the parameters is unknown
 * @throws MinionException
 */

private MinionReifiableConstraint translateElementConstraint(FunctionExpression globalConstraint) 
	throws TranslationUnsupportedException, MinionException, PreprocessorException 	{
	
	String vectorName = "";
	if(isKnownVector(globalConstraint.getExpression1())) {
		if(globalConstraint.getExpression1().getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR)
			vectorName = globalConstraint.getExpression1().getAtomicExpression().getString();
		else 
			vectorName = globalConstraint.getExpression1().getNonAtomicExpression().getExpression().getAtomicExpression().getString();
	}
	
	Expression indexExpression = globalConstraint.getExpression2();
	MinionIdentifier indexVariable = translateKnownVariable(indexExpression);
	
	Expression elementExpression = globalConstraint.getExpression3();
	MinionIdentifier elementVariable = translateKnownVariable(elementExpression);
	
	if(globalConstraint.getExpression1().getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
		if(minionMatrices.containsKey(vectorName)) {
			int index = globalConstraint.getExpression1().getNonAtomicExpression().getExpressionList()[0].getAtomicExpression().getNumber();
			return new MinionElementConstraint("row(m"+minionModel.getMatrixIndex(vectorName)+","+index+")", 
					   indexVariable, 
					   elementVariable);
		}
		
		else return new MinionElementConstraint("v"+minionModel.getVectorIndex(vectorName), 
				   indexVariable, 
				   elementVariable);
	}
	return new MinionElementConstraint("v"+minionModel.getVectorIndex(vectorName), 
									   indexVariable, 
									   elementVariable);
	
}


/**
 * Translate an element-constraint.
 * 
 * @param globalConstraint the FunctionExpression that contains an element constraint
 * @throws TranslationUnsupportedException is thrown if any of the parameters is unknown
 * @throws MinionException
 */

/*private MinionReifiableConstraint translateToElementConstraint(FunctionExpression globalConstraint) 
	throws TranslationUnsupportedException, MinionException 	{
	
	String vectorName = "";
	if(isKnownVector(globalConstraint.getExpression1())) {
		vectorName = globalConstraint.getExpression1().getAtomicExpression().getString();
	}
	
	Expression indexExpression = globalConstraint.getExpression2();
	MinionIdentifier indexVariable = translateKnownVariable(indexExpression);
	
	Expression elementExpression = globalConstraint.getExpression3();
	MinionIdentifier elementVariable = translateKnownVariable(elementExpression);
	
	return new MinionElementConstraint(minionVectors.get(vectorName), 
										indexVariable, 
										elementVariable, 
										"v"+minionModel.getVectorIndex(minionVectors.get(vectorName)));
	
}*/

/**
 * @param parameter an Expression that stands for a vector parameter in a global constraint
 * @return true if the Expression parameter is an identifier that is a known vector of the minionModel.
 * @throws TranslationUnsupportedException is thrown when the expression is no identifier or the identifier is unknown.
 */
private boolean isKnownVector(Expression parameter) 
	  throws TranslationUnsupportedException, MinionException	{
	
	//		 we impose a global constraint on a simple vector
	if(parameter.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		
		if(parameter.getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER) 	
			throw new TranslationUnsupportedException
			("Illegal global constraint: expected an identifier for parameter :"+parameter.toString());
		
		String parameterName = parameter.getAtomicExpression().getString();
		  
		if(decisionVariables.containsKey(parameterName)) {
				
			  if(!minionVectors.containsKey(parameterName) && !minionMatrices.containsKey(parameterName)) {
				  print_debug("Have not added "+parameterName+"yet. Let's do it");
				   variableCreator.addNewVariable(parameterName);	
			  }
		  }
		
		if(!minionVectors.containsKey(parameterName) && !minionMatrices.containsKey(parameterName)) 
			throw new TranslationUnsupportedException
			("Illegal global constraint: unknown identifier name of parameter :"+parameter.toString());		
		
		return true; 
		
	}// we impose a global constraint on a vector of a matrix
	else if  (parameter.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
		
		Expression[] indexExpressions = parameter.getNonAtomicExpression().getExpressionList();	
		if(indexExpressions[0].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
			throw new TranslationUnsupportedException
			  ("Unsupported matrix index "+indexExpressions[0].toString()+". Expected integer value.");
		
		if(indexExpressions[0].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
			throw new TranslationUnsupportedException
			  ("Unsupported matrix index "+indexExpressions[0].toString()+". Expected integer value.");
		
		String matrixName = parameter.getNonAtomicExpression().getExpression().getAtomicExpression().getString();
		
		if(decisionVariables.containsKey(matrixName)) {
			
			  if(!minionVectors.containsKey(matrixName) && !minionMatrices.containsKey(matrixName)) {
				  print_debug("Have not added "+matrixName+"yet. Let's do it");
				   variableCreator.addNewVariable(matrixName);	
			  }
		  }
		
		
		if(minionMatrices.containsKey(matrixName) && indexExpressions.length == 1)
			return true;
		
		else throw new TranslationUnsupportedException
		  ("Illegal or unknown matrix-element "+parameter.toString());
	}
	
	else throw new TranslationUnsupportedException
		("Illegal global constraint: expected an atomic expression for parameter :"+parameter.toString());

	
}


/**
 * 
 * @param expression
 * @return the MinionIdentifier representing the Expression expression
 * @throws TranslationUnsupportedException
 * @throws MinionException
 */

private MinionIdentifier translateKnownVariable(Expression expression) 
	throws TranslationUnsupportedException, MinionException, PreprocessorException {
	
	MinionIdentifier variable = null;	
	
	
	if(expression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
		if(expression.getAtomicExpression().getRestrictionMode() == EssenceGlobals.NUMBER) {
			variable = new MinionConstant(expression.getAtomicExpression().getNumber());
		}
		else if(expression.getAtomicExpression().getRestrictionMode() == EssenceGlobals.IDENTIFIER) {
			String variableName = expression.getAtomicExpression().getString();
			if(minionVariables.containsKey(variableName)) 
				variable = minionVariables.get(variableName);
			else 
				throw new TranslationUnsupportedException
				("Illegal global constraint. Unknown identifier '"+variableName+"' as parameter.");
		}
			
	}
	else if(expression.getRestrictionMode() == EssenceGlobals.NONATOMIC_EXPR) {
		String variableName = expression.getNonAtomicExpression().getExpression().getAtomicExpression().getString();
		if(minionVariables.containsKey(variableName)) 
			variable = translateAtomExpression(expression);
		else 
			throw new TranslationUnsupportedException
			("Illegal global constraint. Unknown matrix element '"+expression.toString()+"' as parameter.");
	}
		
	else throw new TranslationUnsupportedException
			("Illegal global constraint. Expected atom expression for parameter :"+expression.toString());
	
	return variable;
	
}



	protected MinionConstraint translateLexConstraint (Expression e) 
	   throws TranslationUnsupportedException, MinionException {
		
		if(e.getRestrictionMode() != EssenceGlobals.LEX_EXPR)
			throw new TranslationUnsupportedException("Internal error: expected LexExpression instead of:"+e.toString());
		
		print_debug("Translating lex constraint :"+e.toString());
		
		Expression leftExpression = e.getLexExpression().getLeftExpression();
		Expression rightExpression = e.getLexExpression().getRightExpression();
		
		// nested lex-expressions
		if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			print_debug("IIIIIIITTTTTTTTSSSSS in the left part of the tree");
		}
		
		String leftMatrixName = getMatrixName(leftExpression);
		String rightMatrixName = getMatrixName(rightExpression);
		
		
		switch(e.getLexExpression().getLexOperator().getRestrictionMode()) {
		
		case EssenceGlobals.LEX_LEQ:
			return new MinionLexLeqConstraint(leftMatrixName, rightMatrixName);
	
		case EssenceGlobals.LEX_LESS:
			return new MinionLexLessConstraint(leftMatrixName, rightMatrixName);
			
		case EssenceGlobals.LEX_GEQ:
			return new MinionLexLeqConstraint(rightMatrixName, leftMatrixName);
			
		case EssenceGlobals.LEX_GREATER:
			return new MinionLexLessConstraint(rightMatrixName, leftMatrixName);
			
		default:
				throw new TranslationUnsupportedException("Unknown lex-operator in: "+e.toString());
		}
		
	
		
	
	
	}

	
	private String getMatrixName(Expression leftExpression) 
		throws TranslationUnsupportedException, MinionException {
		
		//MinionIdentifier[] leftVector = null;
		//MinionIdentifier[][] leftMatrix = null;
	
		
		if(leftExpression.getRestrictionMode() == EssenceGlobals.BINARYOP_EXPR) {
			print_debug("IIIIIIITTTTTTTTSSSSS in the left part of the tree");
		}
		
		
		if(leftExpression.getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR &&
				leftExpression.getRestrictionMode() != EssenceGlobals.NONATOMIC_EXPR) 
			throw new TranslationUnsupportedException
			  ("Illegal (non atom) argument for lex-operation:"+leftExpression.toString()+". Vector or a matrix required.");

		
		if(leftExpression.getRestrictionMode() == EssenceGlobals.ATOMIC_EXPR) {
			if(leftExpression.getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER) 
				throw new TranslationUnsupportedException
				  ("Illegal argument for lex-operation:"+leftExpression.toString()+". Expected vector or a matrix identifier.");
				
			String leftVectorName = leftExpression.getAtomicExpression().getString();
			if(decisionVariablesNames.contains(leftVectorName)) {
				if(!minionVectors.containsKey(leftVectorName) || !minionMatrices.containsKey(leftVectorName)) {
					variableCreator.addNewVariable(leftVectorName);
				}	
				if(minionVectors.containsKey(leftVectorName))
					return "v"+minionModel.getVectorIndex(leftVectorName);
				else if (minionMatrices.containsKey(leftVectorName))
					return "m"+minionModel.getMatrixIndex(leftVectorName);
				else throw new TranslationUnsupportedException("Unknown variable vector/matrix:"+leftVectorName);
			}
		
			else throw new TranslationUnsupportedException("Unknown variable vector/matrix:"+leftVectorName);
		}
		else {// leftExpression is a non-atomic expression
			NonAtomicExpression leftMatrixExpression = leftExpression.getNonAtomicExpression();
			
			if(leftMatrixExpression.getExpression().getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR) 
				throw new TranslationUnsupportedException
				("Expected vector/matrix name instead of '"+leftMatrixExpression.getExpression()+", please access matrix elements by m[i,j] instead of m[i][j].");
			
			if(leftMatrixExpression.getExpression().getAtomicExpression().getRestrictionMode() != EssenceGlobals.IDENTIFIER)
				throw new TranslationUnsupportedException
				("Expected vector/matrix name instead of '"+leftMatrixExpression.getExpression()+", please access matrix elements by m[i,j] instead of m[i][j].");
			
			String leftMatrixName = leftMatrixExpression.getExpression().getAtomicExpression().getString();
			
			if(decisionVariablesNames.contains(leftMatrixName)){ 
				if(!minionMatrices.containsKey(leftMatrixName)) {
					variableCreator.addNewVariable(leftMatrixName);
				}
			}
			
			
			if(minionVectors.containsKey(leftMatrixName)) 	
					throw new TranslationUnsupportedException
					("Illegal argument for lex-operator: "+leftExpression.toString()+". Expected vector or matrix.");
			
			else if (minionMatrices.containsKey(leftMatrixName)) {
				// we have a row of a matrix
					if(leftMatrixExpression.getExpressionList().length > 1)
						throw new TranslationUnsupportedException
						 ("Illegal argument for lex-operator: "+leftExpression.toString()+". Expected vector or matrix.");
					
					
					if(leftMatrixExpression.getExpressionList()[0].getRestrictionMode() != EssenceGlobals.ATOMIC_EXPR)
						throw new TranslationUnsupportedException
						 ("Illegal index for lex-operator argument:"+leftExpression.toString()+". Expected integer index.");
					if(leftMatrixExpression.getExpressionList()[0].getAtomicExpression().getRestrictionMode() != EssenceGlobals.NUMBER)
						throw new TranslationUnsupportedException
						 ("Illegal index for lex-operator argument:"+leftExpression.toString()+". Expected integer index.");
					
					int index = leftMatrixExpression.getExpressionList()[0].getAtomicExpression().getNumber();
					int offsets[] = minionModel.getMatrixOffsets(leftMatrixName);
					
					return "row(m"+minionModel.getMatrixIndex(leftMatrixName)+", "+(index-offsets[0])+")";	
			}
			else throw new TranslationUnsupportedException("Unknown variable vector/matrix:"+leftMatrixName);
			
		}
		
	}

  
   /** 
    * If the DEBUG-flag in the Globals-interface is set to true, then
    * print the debug-messages. These messages are rather interesting 
    * for the developper than for the user.
    * @param s : the String to be printed on the output
    */

    protected static void print_debug(String s) {
    	if(DEBUG)
    		System.out.println("[ DEBUG atomExpressionTranslator ] "+s);
    }  


    protected static void print_message(String s) {
    	if(PRINT_MESSAGE)
    		System.out.println("[ MESSAGE ] "+s);
    }  




	
	
	
	
	
}
