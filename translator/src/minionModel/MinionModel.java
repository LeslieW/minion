package minionModel;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.lang.ClassNotFoundException;

/**
 * MinionModel represents a constraint model for the solver Minion.
 * 
 *  Notes (ian):
 *  1. All variable references are stored relative to a particular
 *   type. Allows variables to be added at any time. 
 * 1. When Minion gets sparse discrete need to add.
 * 2. 2d, 3d matrices unimplemented.
 * 3. Fix print statement. Make convention 1st 2d mx is the one.
 * 4. NB Anywhere a var can appear, a const can.
 * 5. Table Cts: Frisch intends given 2d matrix, large disj of conj
 * 6. Lex omitted: not in E' yet.
 * 7. No reif yet. Eventually do with separate arraylists: reifMax reifyImpliesMax
 * 
 * @author andrea, ian
 */

public class MinionModel implements MinionGlobals {
    
    // Need these because of compression of similar vars.
    int noZeroOnes, noBoundVariables, noSparseVariables, noDiscreteVariables, noDiscreteSparseVariables ;
   
    /** offsets of the matrix-indices to zero of each matrix
     *  the offset is stored as follows: int[outmost offset, ... , innermost offset] 
     *  for example, a 2d-matrix has an offset for the vector range and the element range, so we get: int[vectorOffset, elementOffset] 
     *  the name is the original Name of the matrix */
    HashMap<String, int[]> matrixOffsets;
    /** HashMap containing the number of bound variables for each bound */
    //HashMap<int[], Integer> boundsMap;
    //HashMap<int[], Integer> discreteBoundsMap;
    
    /** contains all bounds of the bound variables. is needed to work with the HashMap boundsMap*/
    ArrayList<int[]> boundsList;
    ArrayList<int[]> discreteBoundsList;
    
    /** contains the original variables */
    ArrayList<MinionIdentifier> originalVariables;
    
    /** lists of the variables of all different domain types */
    ArrayList<MinionBoolVariable> booleanVariables;
    ArrayList<MinionBoundsVariable> boundsVariables ;                       
    ArrayList<MinionSBoundsVariable> sparseVariables ;                 
    ArrayList<MinionDiscreteVariable> discreteVariables ;                     
    ArrayList<MinionDiscreteSVariable> discreteSparseVariables ;              
    
    ArrayList<MinionVector> matrices1d;
    ArrayList<MinionMatrix>matrices2d; // matrices3d ;
    ArrayList<MinionCube> matrices3d;
    
    HashMap<String,MinionVector> minionVectors;
    HashMap<String,MinionMatrix> minionMatrices;
    HashMap<String,MinionCube> minionCubes;
    
    boolean maximising ;
    MinionIdentifier objectiveVar ;
    
    ArrayList<MinionAllDifferent> allDifferentConstraints ;                        // Vector ids
    ArrayList<MinionEqConstraint> equalityConstraints ;                     // MinionEqConstraint
    ArrayList<MinionDisEqConstraint> disequalityConstraints ;                  // MinionDisEqConstraint
    ArrayList<MinionInEqConstraint> inequalityConstraints ;                // MinionInEqConstraint
    ArrayList<MinionElementConstraint> elementConstraints ;                 // < vid, var, var >
    ArrayList<MinionMinConstraint> minConstraints ;                          // MinionMinConstraint
    ArrayList<MinionMaxConstraint> maxConstraints ;                            // MinionMaxConstraint
    //ArrayList occurrenceConstraints ;                  // <vid, c, var>
    ArrayList<MinionProductConstraint> productConstraints ;                   // MinionProductConstraint
    ArrayList sumLeqVectorConstraints ;                 // <vid, var>
    ArrayList<MinionSumLeqConstraint> sumLeqVariablesConstraints ;                 // MinionSumLeqConstraint
    // ArrayList sumGeqConstraints ;                         // <vid, var>
    ArrayList<MinionSumGeqConstraint> sumGeqVariablesConstraints ;                 // MinionSumGeqConstraint
    ArrayList<MinionWeightedSumLeqConstraint> weightedSumLeqVariablesConstraints;   // MinionWeightedSumLeqConstraint
    ArrayList<MinionWeightedSumGeqConstraint> weightedSumGeqVariablesConstraints;   // MinionWeightedSumGeqConstraint
    //ArrayList powerConstraints;                     // var, var, var
    ArrayList<MinionReifyConstraint> reificationConstraints;                // <Object constraint, const>
    
    /** list containing MinionConstraints that have been generated outside the minionModel */
    ArrayList<MinionConstraint> otherConstraints;
    
    
    /** contains all constraints in the  Minion Model. not relevant for the model itself */
    ArrayList<MinionConstraint> constraintList;
    ArrayList<MinionIdentifier> minionIdentifiers;
    
  /** 
   * Creates a blank minion Model holding no variables or constraints.
   * */
 public  MinionModel () {
	 /** amount of (single!) variables in the different categories (boolean, boundDomain, SparseDomains, etc) */
    noZeroOnes = 0 ;
    noBoundVariables = 0 ;
    noSparseVariables = 0 ;
    noDiscreteVariables = 0 ;
    noDiscreteSparseVariables = 0 ;

    matrixOffsets = new HashMap<String, int[]> ();
    //boundsMap = new HashMap<int[], Integer>();
    //discreteBoundsMap = new HashMap<int[], Integer>();
    discreteBoundsList = new ArrayList<int[]>();
    boundsList = new ArrayList<int[]>();
    
	originalVariables = new ArrayList<MinionIdentifier>();
    
    booleanVariables = new ArrayList<MinionBoolVariable>() ;
    boundsVariables = new ArrayList<MinionBoundsVariable>() ;
    sparseVariables = new ArrayList<MinionSBoundsVariable>() ;
    discreteVariables = new ArrayList<MinionDiscreteVariable>() ;
    discreteSparseVariables = new ArrayList<MinionDiscreteSVariable>() ;

    /** matrix lists */
    matrices1d = new ArrayList<MinionVector>() ;
    matrices2d = new ArrayList<MinionMatrix>() ;
    matrices3d = new ArrayList<MinionCube>() ;

    minionVectors = new HashMap<String, MinionVector>();
    minionMatrices = new HashMap<String, MinionMatrix>();
    minionCubes = new HashMap<String, MinionCube>();
    
    maximising = true ;
    objectiveVar = null ;

    allDifferentConstraints = new ArrayList<MinionAllDifferent>() ;
    equalityConstraints = new ArrayList<MinionEqConstraint>() ;
    disequalityConstraints = new ArrayList<MinionDisEqConstraint>() ;
    inequalityConstraints = new ArrayList<MinionInEqConstraint>() ;
    elementConstraints = new ArrayList<MinionElementConstraint>() ;
    minConstraints = new ArrayList<MinionMinConstraint>() ;
    maxConstraints = new ArrayList<MinionMaxConstraint>() ;
    //occurrenceConstraints = new ArrayList() ;
    productConstraints = new ArrayList<MinionProductConstraint>() ;
    sumLeqVectorConstraints = new ArrayList() ;
    sumLeqVariablesConstraints = new ArrayList<MinionSumLeqConstraint>() ;
    //sumGeqConstraints = new ArrayList() ;
    sumGeqVariablesConstraints = new ArrayList<MinionSumGeqConstraint>() ;
    weightedSumLeqVariablesConstraints = new ArrayList<MinionWeightedSumLeqConstraint>();
    weightedSumGeqVariablesConstraints = new ArrayList<MinionWeightedSumGeqConstraint>();
    //powerConstraints = new ArrayList();
    reificationConstraints = new ArrayList<MinionReifyConstraint>();
    otherConstraints = new ArrayList<MinionConstraint> ();
    
    
    
    constraintList = new ArrayList<MinionConstraint>();
    minionIdentifiers = new ArrayList<MinionIdentifier> ();
  }

 
    /**
     * Creates a new boolean Minion variable and returns its (absolute) index in
     * the model (each variable has a unique index and can only be identified
     * by that). Since the boolean variables come first, their relative index is
     * the same as their absolute index.
     * @param variable the MinionBoolVariable that is being added to the Minion model
     * @return the index the variable has in the Minion model
     */

    public int add01Variable(MinionBoolVariable variable) {
    	int index = noZeroOnes++;
    	variable.setIndices(index, index);
 
 		if(!(variable.getOriginalName().startsWith("freshVariable")))
 			originalVariables.add(variable);
 	   	
    	booleanVariables.add(variable);
    	minionIdentifiers.add(variable);
    	print_debug("Adding BOOLEAN VARIABLE to minionModel:"+variable.getOriginalName()+", with minionName: x"+variable.getAbsoluteIndex());
    	return index;
    	}
    
    
    /**
     * Creates a vector (size is specified by the parameter) of new boolean Minion variables 
     * and computes its relative index in the model (each variable has a relative index (index concerning
     * boolean variables only) and an absolute index (index concerning all variables)
     * and can only be identified by that).
     * The relative index is the index of the vector.
     * @param variableVector the MinionBoolVariable[] array that should be added to the minionModel.
     * @param offset TODO
     */
    
    public void add01VariableVector(MinionBoolVariable[] variableVector, int offset) {		
	  int relativeIndex = matrices1d.size() ;
	  
	  for (int i1 = 0; i1 < variableVector.length; i1++) {
	    variableVector[i1].setRelativeIndex(relativeIndex);
	    variableVector[i1].setAbsoluteIndex(noZeroOnes++);
		if(!(variableVector[i1].getOriginalName().startsWith("freshVariable")))
 			originalVariables.add(variableVector[i1]);
		
	    booleanVariables.add(variableVector[i1]);
		minionIdentifiers.add(variableVector[i1]);
		print_debug("Adding BOOLEAN VARIABLE to minionModel:"+variableVector[i1].getOriginalName()+", with minionName: x"+variableVector[i1].getAbsoluteIndex());
	  }
	  
	  matrixOffsets.put(variableVector[0].getOriginalName(), new int[] { offset });
	  MinionVector vector = new MinionVector(variableVector, variableVector[0].getOriginalName(), offset);
	  matrices1d.add(vector) ;
	  minionVectors.put(variableVector[0].getOriginalName(), vector);
    }
    
    
    /**
     * Add a variableVector that is empty and will (should!) be inserted with values later
     * @param variableVector TODO
     * @param originalName
     * @param offset
     */
    public void addEmpty01VariableVector(MinionIdentifier[] variableVector, String originalName, int offset) {		
  	  
  	  matrixOffsets.put(originalName, new int[] { offset });
  	  MinionVector vector = new MinionVector(variableVector, originalName, offset);
  	  matrices1d.add(vector) ;
  	  minionVectors.put(originalName, vector);
      }
      
    
    /**
     * Add a matrix to the minion Model. Each element of the matrix is stored as new element and 
     * must not have been added before! The offsets of each range (vector and element-range) are 
     * stored. 
     * 
     * @param matrix
     * @param vectorOffset
     * @param elementOffset
     */
    public void add01VariableMatrix(MinionBoolVariable[][] matrix, int vectorOffset, int elementOffset) {
	
    	int relativeIndex = matrices2d.size();
    	
		for(int i=0; i<matrix.length; i++) {	
			for(int j=0; j<matrix[i].length; j++) {
				matrix[i][j].setRelativeIndex(relativeIndex);
				matrix[i][j].setAbsoluteIndex(noZeroOnes++);
				if(!(matrix[i][j].getOriginalName().startsWith("freshVariable")))
					originalVariables.add(matrix[i][j]);
				booleanVariables.add(matrix[i][j]);
				minionIdentifiers.add(matrix[i][j]);
		    }	
		
		}
		matrixOffsets.put(matrix[0][0].getOriginalName(), new int[] { vectorOffset, elementOffset } );
		MinionMatrix newMatrix = new MinionMatrix(matrix, matrix[0][0].getOriginalName(), new int[] { vectorOffset, elementOffset } );
		matrices2d.add(newMatrix);
		print_debug("Added the MATRIX to the minionModel :"+matrix[0][0].getOriginalName());
		minionMatrices.put(matrix[0][0].getOriginalName(), newMatrix);
    }
    
    /**
     * Add a variableMatrix that is empty and will (should!) be inserted with values later
     * 
     * @param matrix
     * @param originalName
     * @param vectorOffset
     * @param elementOffset
     */
    public void addEmpty01VariableMatrix(MinionBoolVariable[][] matrix, String originalName, int vectorOffset, int elementOffset) {
    	
		matrixOffsets.put(originalName, new int[] { vectorOffset, elementOffset } );
		MinionMatrix newMatrix = new MinionMatrix(matrix, originalName, new int[] { vectorOffset, elementOffset } );
		matrices2d.add(newMatrix);
		print_debug("Added the MATRIX to the minionModel :"+originalName);
		minionMatrices.put(originalName, newMatrix);
    }
    
     
    /**
     * Add a cube of 0/1 variables to the Minion Model. Each element of the cube is stored as new element and 
     * must not have been added before! The offsets of each range (matrix, vector and element-range) are 
     * stored. 
     * 
     * @param cube
     * @param matrixOffset is the difference between 0 and the index of the first matrix, such that 
     *         firstIndex - offset = 0
     * @param vectorOffset
     * @param elementOffset
     */
   public void add01VariableCube(MinionBoolVariable[][][] cube, int matrixOffset, int vectorOffset, int elementOffset) {
    	
    	int relativeIndex = matrices3d.size();
    	
    	for(int i=0; i<cube.length; i++) {
    		for(int j=0; j<cube[i].length; j++) {
    			for(int k=0; k<cube[i][j].length; k++) {
    				cube[i][j][k].setRelativeIndex(relativeIndex);
    				cube[i][j][k].setAbsoluteIndex(noZeroOnes++);
    				if(!(cube[i][j][k].getOriginalName().startsWith("freshVariable")))
    					originalVariables.add(cube[i][j][k]);
    				booleanVariables.add(cube[i][j][k]);
    				minionIdentifiers.add(cube[i][j][k]);
    			}
    		}
    	}
    	
    	String cubeName = cube[0][0][0].getOriginalName();
    	matrixOffsets.put(cubeName, 
    				      new int[] { matrixOffset, vectorOffset, elementOffset} );
    	MinionCube newCube = new MinionCube(cube, cubeName,
    			                            new int[] { matrixOffset, vectorOffset, elementOffset});
    	matrices3d.add(newCube);
    	print_debug("Added the CUBE to the minionModel :"+cubeName);
    	minionCubes.put(cubeName, newCube);
    }
 
   
   
   /**
    * Add a variableCube that is empty. Variables will be inserted later (used for variable reuse)
    * 
    * @param cube
    * @param originalName
    * @param matrixOffset
    * @param vectorOffset
    * @param elementOffset
    */
   public void addEmpty01VariableCube(MinionBoolVariable[][][] cube, String originalName, int matrixOffset, int vectorOffset, int elementOffset) {
   	
	   matrixOffsets.put(originalName, 
   				      new int[] { matrixOffset, vectorOffset, elementOffset} );
   		MinionCube newCube = new MinionCube(cube, originalName,
   			                            new int[] { matrixOffset, vectorOffset, elementOffset});
   		matrices3d.add(newCube);
   		print_debug("Added the EMPTY CUBE to the minionModel :"+originalName);
   		minionCubes.put(originalName, newCube);
   }
   
  /** Computes the relative index of the variable and puts it into
   * the list of Bound variables. The absolute index can only be
   * computed after all variables have been collected.
   * @param boundsVariable the MinionBoundsVariable that will be inserted into the 
   * 		Minion model 	
     */
    public void addBoundsVariable(MinionBoundsVariable boundsVariable) {  
    	boundsVariable.setRelativeIndex(noBoundVariables++);
    	boundsVariables.add(boundsVariable) ;  
    	if(!(boundsVariable.getOriginalName().startsWith("freshVariable")))
 			originalVariables.add(boundsVariable);
    	
    	print_debug("added bounds variable: "+boundsVariable.getOriginalName()+" with lb:"+boundsVariable.getLowerBound()+", ub:"+boundsVariable.getUpperBound());
    	minionIdentifiers.add(boundsVariable);
    	int[] bounds = new int[] { boundsVariable.getLowerBound(), boundsVariable.getUpperBound(), 1 };
    	
    	boundsList.add(bounds);
    	
    	print_debug("Adding BOUNDSVARIABLE VARIABLE to minionModel:"+boundsVariable.getOriginalName()+", with minionName: x"+boundsVariable.getAbsoluteIndex());
    }
    
    
    
    /**
     * Creates a vector (size is specified by the parameter) of new Minion bound variables 
     * and computes its relative index in the model (each variable has a relative index (index concerning
     * bound variables only) and an absolute index (index concerning all variables)
     * and can only be identified by that).
     * @param variableVector the MinionBoundsVariable[] array that should be added to the minionModel.
     * @param offset TODO
     */

    public void addBoundsVariableVector(MinionBoundsVariable[] variableVector, int offset) {
    	int relativeIndex = matrices1d.size() ;
	
    	for (int i = 0; i < variableVector.length; i++) {
    		print_debug("starting loop with i="+i);
    		variableVector[i].setRelativeIndex(relativeIndex);
    		boundsVariables.add(variableVector[i]);
    		minionIdentifiers.add(variableVector[i]);
    		print_debug("Added bounds variable: "+variableVector[i].getOriginalName());
    		if(!(variableVector[i].getOriginalName().startsWith("freshVariable")))
     			originalVariables.add(variableVector[i]);
    		
    		noBoundVariables++;
    		print_debug("just increased the amount of boundVariables to "+noBoundVariables+", end of for loop with i ="+i);
    		print_debug("Adding BOOLEAN VARIABLE to minionModel:"+variableVector[i].getOriginalName()+", with minionName: x"+variableVector[i].getAbsoluteIndex());
    	}

    	int[] bounds = new int[] { variableVector[0].getLowerBound(), variableVector[0].getUpperBound(), variableVector.length };
    	boundsList.add(bounds);
    	
    	
    	matrixOffsets.put(variableVector[0].getOriginalName(), new int[] {offset});	
    	MinionVector vector = new MinionVector(variableVector, variableVector[0].getOriginalName(), offset);
    	matrices1d.add(vector) ;
    	minionVectors.put(variableVector[0].getOriginalName(), vector);
    }
    
    /**
     * Add a variableVector that is empty. Variables will be inserted later (used for variable reuse)
     * @param variableVector
     * @param originalName
     * @param offset
     */
    public void addEmptyBoundsVariableVector(MinionBoundsVariable[] variableVector, String originalName, int offset) {
    	// removed boundsList!
    	
    	matrixOffsets.put(originalName, new int[] {offset});	
    	MinionVector vector = new MinionVector(variableVector, originalName, offset);
    	matrices1d.add(vector) ;
    	minionVectors.put(originalName, vector);
    }
    
    
    /**
     * Add a matrix of new minion bounds variables
     * 
     * @param matrix
     * @param vectorOffset
     * @param elementOffset
     */
    public void addBoundsVariableMatrix(MinionBoundsVariable[][] matrix, int vectorOffset, int elementOffset) {
    	
    	int relativeIndex = matrices2d.size();
    	
		for(int i=0; i<matrix.length; i++) {	
			for(int j=0; j<matrix[i].length; j++) {
				matrix[i][j].setRelativeIndex(relativeIndex);
				boundsVariables.add(matrix[i][j]);				
				if(!(matrix[i][j].getOriginalName().startsWith("freshVariable")))
					originalVariables.add(matrix[i][j]);
				minionIdentifiers.add(matrix[i][j]);
				noBoundVariables++;
		    }	
		
		}
    	int[] bounds = new int[] { matrix[0][0].getLowerBound(), matrix[0][0].getUpperBound(), matrix.length*matrix[0].length } ;
    	boundsList.add(bounds);	
    	    	 
		matrixOffsets.put(matrix[0][0].getOriginalName(), new int[] { vectorOffset, elementOffset } );
		MinionMatrix newMatrix = new MinionMatrix(matrix, matrix[0][0].getOriginalName(), new int[] { vectorOffset, elementOffset } );
		matrices2d.add(newMatrix);
		minionMatrices.put( matrix[0][0].getOriginalName(),newMatrix);
    }
    
    
    /**
     * Add a variableMatrix that is empty and will (should!) be inserted with values later
     * 
     * @param matrix
     * @param originalName
     * @param vectorOffset
     * @param elementOffset
     */
    public void addEmptyBoundsVariableMatrix(MinionBoundsVariable[][] matrix, String originalName, int vectorOffset, int elementOffset) {
    	
		matrixOffsets.put(originalName, new int[] { vectorOffset, elementOffset } );
		MinionMatrix newMatrix = new MinionMatrix(matrix, originalName, new int[] { vectorOffset, elementOffset } );
		matrices2d.add(newMatrix);
		print_debug("Added the MATRIX to the minionModel :"+originalName);
		minionMatrices.put(originalName, newMatrix);
    }
    
    
    /**
     * Add a cube of bounds variables to Minion Model. All cube elements are added to the Model as new elements,
     * so they must not have been added before!
     * 
     * @param cube
     * @param matrixOffset is the difference between 0 and the index of the first matrix, such that 
     *         firstIndex - offset = 0
     * @param vectorOffset
     * @param elemOffset
     */
    public void addBoundsVariableCube(MinionBoundsVariable[][][] cube, 
    		                          int matrixOffset, int vectorOffset, int elemOffset) {
    	
    	int relativeIndex = matrices3d.size();
    	
    	for(int i=0; i<cube.length; i++) {
    		for(int j=0; j<cube[i].length; j++) {
    			for(int k=0; k<cube[i][j].length; k++) {
    				cube[i][j][k].setRelativeIndex(relativeIndex);
    				boundsVariables.add(cube[i][j][k]);
    				if(!(cube[i][j][k].getOriginalName().startsWith("freshVariable")))
    					originalVariables.add(cube[i][j][k]);
    				minionIdentifiers.add(cube[i][j][k]);
    				noBoundVariables++;
    			}
    		}
    	}
    	
       	int[] bounds = new int[] { cube[0][0][0].getLowerBound(), cube[0][0][0].getUpperBound(), cube.length*cube[0].length } ;
    	boundsList.add(bounds);	
    	
    	String cubeName = cube[0][0][0].getOriginalName();
    	matrixOffsets.put(cubeName, new int[] {matrixOffset, vectorOffset, elemOffset}  );
    	MinionCube newCube = new MinionCube(cube, cubeName, new int[] {matrixOffset, vectorOffset, elemOffset} );
    	matrices3d.add(newCube);
    	minionCubes.put(cubeName, newCube);
    }
    
    
    /**
     * Add a variableCube that is empty. Variables will be inserted later (used for variable reuse)
     * 
     * @param cube
     * @param originalName
     * @param matrixOffset
     * @param vectorOffset
     * @param elementOffset
     */
    public void addEmptyBoundsVariableCube(MinionBoundsVariable[][][] cube, String originalName, int matrixOffset, int vectorOffset, int elementOffset) {
    	
 	   matrixOffsets.put(originalName, 
    				      new int[] { matrixOffset, vectorOffset, elementOffset} );
    		MinionCube newCube = new MinionCube(cube, originalName,
    			                            new int[] { matrixOffset, vectorOffset, elementOffset});
    		matrices3d.add(newCube);
    		print_debug("Added the EMPTY CUBE to the minionModel :"+originalName);
    		minionCubes.put(originalName, newCube);
    }
    
    /** Computes the relative index of the variable and puts it into
     * the list of discrete variables. The absolute index can only be
     * computed after all variables have been collected.
     * @param variable the MinionDiscreteVariable that will be inserted into the 
     * 		Minion model 	
       */
    public void addDiscreteVariable(MinionDiscreteVariable variable) {
    	variable.setRelativeIndex(noDiscreteVariables++);
    	discreteVariables.add(variable);
    	minionIdentifiers.add(variable);
    	if(!(variable.getOriginalName().startsWith("freshVariable")))
 			originalVariables.add(variable);

    	minionIdentifiers.add(variable);
    	int[] bounds = new int[] { variable.getLowerBound(), variable.getUpperBound(), 1 };
    	
    	discreteBoundsList.add(bounds);
 	
    }
    
    /**
     * Add a vector of minion discrete variables to the minion model. All variables are
     * assumed to have not be added to the model. 
     * 
     * @param variableVector
     * @param offset
     */
    public void addDiscreteVariableVector(MinionDiscreteVariable[] variableVector, int offset) {
    	int relativeIndex = matrices1d.size() ;
	
    	for (int i = 0; i < variableVector.length; i++) {
    		print_debug("starting loop with i="+i);
    		variableVector[i].setRelativeIndex(relativeIndex);
    		discreteVariables.add(variableVector[i]);
    		minionIdentifiers.add(variableVector[i]);
    		print_debug("Added discrete variable: "+variableVector[i].getOriginalName());
    		if(!(variableVector[i].getOriginalName().startsWith("freshVariable")))
     			originalVariables.add(variableVector[i]);
    		
    		noDiscreteVariables++;
    		print_debug("just increased the amount of boundVariables to "+noBoundVariables+", end of for loop with i ="+i);
    		print_debug("Adding BOOLEAN VARIABLE to minionModel:"+variableVector[i].getOriginalName()+", with minionName: x"+variableVector[i].getAbsoluteIndex());
    	}

    	int[] bounds = new int[] { variableVector[0].getLowerBound(), variableVector[0].getUpperBound(), variableVector.length };
    	discreteBoundsList.add(bounds);
    	
    	
    	matrixOffsets.put(variableVector[0].getOriginalName(), new int[] {offset});	
    	MinionVector vector = new MinionVector(variableVector, variableVector[0].getOriginalName(), offset);
    	matrices1d.add(vector) ;
    	minionVectors.put(variableVector[0].getOriginalName(), vector);
    }
    
    /**
     * Add a variableVector that is empty. Variables will be inserted later (used for variable reuse)
     * 
     * @param variableVector
     * @param originalName
     * @param offset
     */
    public void addEmptyDiscreteVariableVector(MinionDiscreteVariable[] variableVector, String originalName, int offset) {
    	// removed boundsList!
    	
    	matrixOffsets.put(originalName, new int[] {offset});	
    	MinionVector vector = new MinionVector(variableVector, originalName, offset);
    	matrices1d.add(vector) ;
    	minionVectors.put(originalName, vector);
    }
    
    
    /**
     * Add a matrix of new minion discrete variables
     * 
     * @param matrix
     * @param vectorOffset
     * @param elementOffset
     */
    public void addDiscreteVariableMatrix(MinionDiscreteVariable[][] matrix, int vectorOffset, int elementOffset) {
    	
    	int relativeIndex = matrices2d.size();
    	
		for(int i=0; i<matrix.length; i++) {	
			for(int j=0; j<matrix[i].length; j++) {
				matrix[i][j].setRelativeIndex(relativeIndex);
				discreteVariables.add(matrix[i][j]);				
				if(!(matrix[i][j].getOriginalName().startsWith("freshVariable")))
					originalVariables.add(matrix[i][j]);
				minionIdentifiers.add(matrix[i][j]);
				noDiscreteVariables++;
		    }	
		
		}
    	int[] bounds = new int[] { matrix[0][0].getLowerBound(), matrix[0][0].getUpperBound(), matrix.length*matrix[0].length } ;
    	discreteBoundsList.add(bounds);	
    	    	 
		matrixOffsets.put(matrix[0][0].getOriginalName(), new int[] { vectorOffset, elementOffset } );
		MinionMatrix newMatrix = new MinionMatrix(matrix, matrix[0][0].getOriginalName(), new int[] { vectorOffset, elementOffset } );
		matrices2d.add(newMatrix);
		minionMatrices.put( matrix[0][0].getOriginalName(),newMatrix);
    }
    
    
    /**
     * Add a variableMatrix that is empty and will (should!) be inserted with values later
     * 
     * @param matrix
     * @param originalName
     * @param vectorOffset
     * @param elementOffset
     */
    public void addEmptyDiscreteVariableMatrix(MinionDiscreteVariable[][] matrix, String originalName, int vectorOffset, int elementOffset) {
    	
		matrixOffsets.put(originalName, new int[] { vectorOffset, elementOffset } );
		MinionMatrix newMatrix = new MinionMatrix(matrix, originalName, new int[] { vectorOffset, elementOffset } );
		matrices2d.add(newMatrix);
		print_debug("Added the MATRIX to the minionModel :"+originalName);
		minionMatrices.put(originalName, newMatrix);
    }
    
    
    /**
     * Add a cube of bounds variables to Minion Model. All cube elements are added to the Model as new elements,
     * so they must not have been added before!
     * 
     * @param cube
     * @param matrixOffset is the difference between 0 and the index of the first matrix, such that 
     *         firstIndex - offset = 0
     * @param vectorOffset
     * @param elemOffset
     */
    public void addDiscreteVariableCube(MinionDiscreteVariable[][][] cube, 
            int matrixOffset, int vectorOffset, int elemOffset) {

    	int relativeIndex = matrices3d.size();

    	for(int i=0; i<cube.length; i++) {
    		for(int j=0; j<cube[i].length; j++) {
    			for(int k=0; k<cube[i][j].length; k++) {
    				cube[i][j][k].setRelativeIndex(relativeIndex);
    				discreteVariables.add(cube[i][j][k]);
    				if(!(cube[i][j][k].getOriginalName().startsWith("freshVariable")))
    					originalVariables.add(cube[i][j][k]);
    				minionIdentifiers.add(cube[i][j][k]);
    				noDiscreteVariables++;
    			}
    		}
    	}

    	int[] bounds = new int[] { cube[0][0][0].getLowerBound(), cube[0][0][0].getUpperBound(), cube.length*cube[0].length } ;
    	discreteBoundsList.add(bounds);	

    	String cubeName = cube[0][0][0].getOriginalName();
    	matrixOffsets.put(cubeName, new int[] {matrixOffset, vectorOffset, elemOffset}  );
    	MinionCube newCube = new MinionCube(cube, cubeName, new int[] {matrixOffset, vectorOffset, elemOffset} );
    	matrices3d.add(newCube);
    	minionCubes.put(cubeName, newCube);	
    }

    
    /**
     * Add a variableCube that is empty. Variables will be inserted later (used for variable reuse)
     * 
     * @param cube
     * @param originalName
     * @param matrixOffset
     * @param vectorOffset
     * @param elementOffset
     */
    public void addEmptyDiscreteVariableCube(MinionDiscreteVariable[][][] cube, String originalName, int matrixOffset, int vectorOffset, int elementOffset) {
    	
 	   		matrixOffsets.put(originalName, 
    				      new int[] { matrixOffset, vectorOffset, elementOffset} );
    		MinionCube newCube = new MinionCube(cube, originalName,
    			                            new int[] { matrixOffset, vectorOffset, elementOffset});
    		matrices3d.add(newCube);
    		print_debug("Added the EMPTY CUBE to the minionModel :"+originalName);
    		minionCubes.put(originalName, newCube);
    }
    
    /** Computes the relative index of the variable and puts it into
     * the list of sparse bounds variables. The absolute index can only be
     * computed after all variables have been collected.
     * @param variable the MinionSBoundsVariable that will be inserted into the 
     * 		Minion model 	
       */
    public void addSparseVariable(MinionSBoundsVariable variable) {
    	variable.setRelativeIndex(noSparseVariables++);
    	sparseVariables.add(variable);
    	minionIdentifiers.add(variable);
    	if(!(variable.getOriginalName().startsWith("freshVariable")))
 			originalVariables.add(variable);
    }
    
    /** Computes the relative index of the variable and puts it into
     * the list of sparse bounds variables. The absolute index can only be
     * computed after all variables have been collected.
     * @param variable the MinionSBoundsVariable that will be inserted into the 
     * 		Minion model 	
       */
    public void addSparseDiscreteVariable(MinionDiscreteSVariable variable) {
    	variable.setRelativeIndex(noDiscreteSparseVariables++);
    	discreteSparseVariables.add(variable);
    	minionIdentifiers.add(variable);
    	if(!(variable.getOriginalName().startsWith("freshVariable")))
 			originalVariables.add(variable);
    }
    
    
    public void insertIdentifierInVectorAt(String vectorName, int vectorIndex, MinionIdentifier identifier) 
    	throws MinionException {
    	
    	MinionVector vector = minionVectors.get(vectorName);
    	if(vector == null)
    		throw new MinionException("Unknown vector (to MinionModel):"+vectorName);
    	
    	MinionIdentifier[] vectorElements = vector.getElements();
    	//int offsets[] = matrixOffsets.get(vectorName);
    	//vectorIndex = vectorIndex - offsets[0];
    	
    	if(vectorIndex <0 || vectorIndex >= vectorElements.length)
    		throw new MinionException("Array index "+vectorIndex+" is out of bounds in :"+vectorName);
    	
    	vectorElements[vectorIndex] = identifier;
    }
    
    
    
    public void insertIdentifierInMatrixAt(String matrixName, int vectorIndex, int elementIndex, MinionIdentifier identifier) 
		throws MinionException {
	
    	MinionMatrix matrix = minionMatrices.get(matrixName);
    	if(matrix == null)
    		throw new MinionException("Unknown vector (to MinionModel):"+matrixName);
	
    	MinionIdentifier[][] matrixElements = matrix.getMatrix();
    	//int offsets[] = matrixOffsets.get(matrixName);
    	//vectorIndex = vectorIndex - offsets[0];
    	//elementIndex = elementIndex - offsets[1];
    	
    	if(vectorIndex <0 || vectorIndex >= matrixElements.length)
    		throw new MinionException("Array index "+vectorIndex+" is out of bounds in :"+matrixName);
    	if(elementIndex <0 || elementIndex >= matrixElements[vectorIndex].length)
    		throw new MinionException("Array index "+elementIndex+" is out of bounds in :"+matrixName);	
    		
    	matrixElements[vectorIndex][elementIndex] = identifier;
    }
 
    public void insertIdentifierInCubeAt(String cubeName, int matrixIndex, int vectorIndex, int elementIndex, MinionIdentifier identifier) 
		throws MinionException {

    	MinionCube cube = minionCubes.get(cubeName);
    	if(cube == null)
    		throw new MinionException("Unknown vector (to MinionModel):"+cubeName);

    	MinionIdentifier[][][] cubeElements = cube.getCube();
    	//int offsets[] = matrixOffsets.get(cubeName);
    	//vectorIndex = vectorIndex - offsets[0];
    	//elementIndex = elementIndex - offsets[1];
	
    	if(matrixIndex <0 || matrixIndex >= cubeElements.length)
    		throw new MinionException("Array index "+matrixIndex+" is out of bounds in :"+cubeName);
    	if(vectorIndex <0 || vectorIndex >= cubeElements[matrixIndex].length)
    		throw new MinionException("Array index "+vectorIndex+" is out of bounds in :"+cubeName);
    	if(elementIndex <0 || elementIndex >= cubeElements[matrixIndex][vectorIndex].length)
    		throw new MinionException("Array index "+elementIndex+" is out of bounds in :"+cubeName);	
		
    	cubeElements[matrixIndex][vectorIndex][elementIndex] = identifier;
}
    
    /**
     * This method should be called when all variables have been added to the Minion model.
     * It computes the absolute index of every variable. Boolean variables are not considered,
     * since they come first in the variable list and therefore already have their absolute index.
     * 
     * TODO: later extend for sparse/discrete and so on bounds
     */
   public void computeAllAbsoluteIndices() {
    	
	    int i;	
    	// we start counting from the boolean variables
    	int absoluteIndex = noZeroOnes;
    
    	print_debug("setting absolute indices of bound domain variables starting from "+absoluteIndex);
    	
    	// then bound domain variables
    	for(i=0; i<boundsVariables.size(); i++) 
    		boundsVariables.get(i).setAbsoluteIndex(absoluteIndex++);
    		
    	print_debug("set absolute indices of bound domain variables ending with "+absoluteIndex);
  
    	// then sparse
    	for(i=0; i< sparseVariables.size(); i++)
    		sparseVariables.get(i).setAbsoluteIndex(absoluteIndex++);
    	
    	// then discrete
    	for(i=0; i< discreteVariables.size(); i++)
    		discreteVariables.get(i).setAbsoluteIndex(absoluteIndex++);
    		
    	// then sparse-discrete
    	for(i=0; i< discreteSparseVariables.size(); i++)
    		discreteSparseVariables.get(i).setAbsoluteIndex(absoluteIndex++);
    }
    
    
  /** 
   * @param lhs MinionIdentifier on the left hand side of the equality relation
   * @param rhs MinionIdentifier on the right hand side of the equality relation
   *  */
    public void addEqualityConstraint(MinionIdentifier lhs,
				      MinionIdentifier rhs) {
	    	
    	print_debug("adding new equality constraint");
    	MinionEqConstraint c = new MinionEqConstraint(lhs,rhs);
    	print_debug("added an equality constraint:"+c.toString());
    	equalityConstraints.add(c) ;
    	constraintList.add(c);
    }
    
    
    /** 
     * @param lhs MinionIdentifier on the left hand side of the disequality relation
     * @param rhs MinionIdentifier on the right hand side of the disequality relation
     *  */
    public void addDisequalityConstraint(MinionIdentifier lhs,
					 MinionIdentifier rhs) {
	
    	MinionDisEqConstraint c = new MinionDisEqConstraint(lhs,rhs);       
    	disequalityConstraints.add(c) ;
       	print_debug("added a disequality constraint:"+c.toString());
    	constraintList.add(c);
    }
    
    
    /** 
     * @param lhs MinionIdentifier on the left hand side of the inequality relation
     * @param rhs MinionIdentifier on the right hand side of the inequality relation
     * @param c MinionConstant that states if the relation is > or <
     *  */
    public void addInequalityConstraint(MinionIdentifier lhs,
				      MinionIdentifier rhs,
					MinionConstant c) {
	
    	MinionInEqConstraint ct = new MinionInEqConstraint(lhs,rhs,c);
     	print_debug("added an inequality constraint:"+ct.toString());
    	inequalityConstraints.add(ct) ;
    	constraintList.add(ct);
    }
    

    /** 
     *  Add a new product constraint to the minion model of the 
     *  form left * right = result
     *  @param left MinionIdentifier on the left hand side of the product 
     *  @param right MinionIdentifier on the right hand side of the product 
     *  @param result MinionIdentifier that results from mutliplying left and right
     *  
     */ 
    public void addProductConstraint(MinionIdentifier left,
				     MinionIdentifier right,
				     MinionIdentifier result) {

    	MinionProductConstraint c = new MinionProductConstraint(left, right, result);
     	print_debug("added a product constraint:"+c.toString());
    	productConstraints.add(c);
    	constraintList.add(c);
    }


    /** 
     * The less-or-equal sum has the following meaning:
     * sumleq([v1, v2, v3, v4], v5) corresponds to   v1 + v2 + v3 + v4 <= v5 
     * @param expr MinionIdentifier[] array that contain all identifiers that form
     * 	the left hand side of the sum.
     * @param result MinionIdentifier resulting from adding up the array expr
     * @param useWatchedLiterals TODO
     */ 
    public void addSumLeqVariablesConstraint(MinionIdentifier[] expr,
					     MinionIdentifier result, boolean useWatchedLiterals) {

    	MinionSumLeqConstraint c = new MinionSumLeqConstraint(expr,result, useWatchedLiterals);
     	print_debug("added a sumleq constraint:"+c.toString());
    	sumLeqVariablesConstraints.add(c);
    	constraintList.add(c);
    }


    /** 
     * The greater-or-equal sum has the following meaning:
     * sumleq([v1, v2, v3, v4], v5) corresponds to   v1 + v2 + v3 + v4 >= v5 
     * @param expr MinionIdentifier[] array that containts all identifiers that form
     * 	the left hand side of the sum.
     * @param result MinionIdentifier resulting from adding up the array expr
     * @param useWatchedLiterals TODO
     */ 
    public void addSumGeqVariablesConstraint(MinionIdentifier[] expr,
					     MinionIdentifier result, boolean useWatchedLiterals) {

    	
    	
    	MinionSumGeqConstraint c = new MinionSumGeqConstraint(expr,result, useWatchedLiterals);
    	print_debug("added a sumgeq constraint:"+c.toString()+" and we used watchedLiterals: "+useWatchedLiterals);
    	sumGeqVariablesConstraints.add(c);
    	constraintList.add(c);
    }

    
    
    public void addSumConstraint(MinionIdentifier[] expr, MinionIdentifier result, boolean useWatchedLiterals) {
    	
    	MinionSumConstraint c = new MinionSumConstraint(expr,result,useWatchedLiterals);
    	
    	print_debug("added a sum constraint:"+c.toString());
    	this.otherConstraints.add(c);
    	constraintList.add(c);
    	
    }

    /** 
     *  Add a new weighted sumleq constraint to the minion model of the 
     *  form var-vector (MinionBoundsVar[]), const-vector (MinionConst[]), id (MinionIdentifier)
     *  where weightedsumleq(var-vector, const-vector, id), meaning
     *
     *  var1*const1 + var2*const2 + ... + var_n*const_n <= id
     *    
     *  @param vars MinionIdentifier[] holding the variables on the left hand side of the inequation
     *  @param constants MinionConstant[] holding the variables on the left hand side of the inequation
     *  @param id MinionIdentifier representing the result of the scalar product
     *  @throws MinionException
     */ 
    public void addWeightedSumLeqConstraint(MinionIdentifier[] vars, MinionConstant[] constants, MinionIdentifier id) 
    	throws MinionException {

    	if(vars.length != constants.length)
    		throw new MinionException
    		("Variable vector '"+vars.toString()+
    				"' does not have the same length as constant vector '"+constants.toString()+"'.");


    	MinionWeightedSumLeqConstraint c = new MinionWeightedSumLeqConstraint(vars, constants, id);
    	print_debug("added a weighted sumleq constraint:"+c.toString());
    	weightedSumLeqVariablesConstraints.add(c);
    	constraintList.add(c);
    }

    
    /**
     *  Add a new weighted sumgeq constraint to the minion model of the 
     *  form var-vector (MinionBoundsVar[]), const-vector (MinionConst[]), id (MinionIdentifier)
     *  where weightedsumleq(var-vector, const-vector, id), meaning
     *
     *  var1*const1 + var2*const2 + ... + var_n*const_n >= id
     *  
     *  @param vars MinionIdentifier[] holding the variables on the left hand side of the inequation
     *  @param constants MinionConstant[] holding the variables on the left hand side of the inequation
     *  @param id MinionIdentifier representing the result of the scalar product  
     *  @throws MinionException  
     */
    public void addWeightedSumGeqConstraint(MinionIdentifier[] vars, MinionConstant[] constants, MinionIdentifier id) 
		throws MinionException {

    	if(vars.length != constants.length)
    		throw new MinionException
    		("Variable vector '"+vars.toString()+
    				"' does not have the same length as constant vector '"+constants.toString()+"'.");
	
    	MinionWeightedSumGeqConstraint c = new MinionWeightedSumGeqConstraint(vars, constants, id);
    	print_debug("added a weighted sumgeq constraint:"+c.toString());
    	weightedSumGeqVariablesConstraints.add(c);
    	constraintList.add(c);

    }

    
  
    
    
    /** 
     *  Add a reified constraint and assign it to the MinionIdentifier bool.
     *
     *  @param reifiableConstraint is the MinionReifiableConstraint to be reified
     *  @param reifiedVar is the boolean MinionIdentifier holding the outcome of reification
     * @throws MinionException
     */
    public void addReificationConstraint(MinionReifiableConstraint reifiableConstraint, MinionIdentifier reifiedVar) 
		throws MinionException  {
		
    	MinionReifyConstraint constraint = new MinionReifyConstraint(reifiableConstraint, reifiedVar);
    	print_debug("added a reification constraint:"+constraint.toString());
    	reificationConstraints.add(constraint);
    	constraintList.add(constraint);
    }


    /** 
     * @param ids MinionIdentifier[] array holding the variables whose maximum should be determined
     * @param result MinionIdentifier holds the maxium of ids
     * @throws MinionException
     * 
    */
    public void addMaxConstraint(MinionIdentifier[] ids, MinionIdentifier result) 
		throws MinionException {
	
    	MinionMaxConstraint constraint = new MinionMaxConstraint(ids, result);
    	print_debug("added a max constraint:"+constraint.toString());
    	maxConstraints.add(constraint);
    	constraintList.add(constraint);
    }


    /** 
     * @param ids MinionIdentifier[] array holding the variables whose minimum should be determined
     * @param result MinionIdentifier holds the minimum of ids
     * @throws MinionException
     * 
    */
    public void addMinConstraint(MinionIdentifier[] ids, MinionIdentifier result) 
		throws MinionException {

    	MinionMinConstraint constraint = new MinionMinConstraint(ids, result);
       	print_debug("added a min constraint:"+constraint.toString());
    	minConstraints.add(constraint);
    	constraintList.add(constraint);
    }
    
    
    /**
     * Impose an allDifferent constraint on a known variable vector.
     * 
     * @param vector 
     * @throws MinionException is thrown when the vector is unknown
     */
    
    public void addAllDifferentConstraint(MinionIdentifier[] vector) 
    	throws MinionException {
    	
    	MinionVector variableVector = minionVectors.get(vector[0].getOriginalName());
    	
    	if(matrices1d.contains(variableVector)) {
    		MinionAllDifferent allDifferentConstraint = new MinionAllDifferent(vector, "v"+matrices1d.indexOf(vector));
    		allDifferentConstraints.add(allDifferentConstraint);
    	}
    	else throw new MinionException("Imposing allDifferent on unknown vector:"+vector[0].getOriginalName());
    }
    
    /**
     * Add an allDifferent constraint that is imposed on a vector of a defined matrix
     * 
     * @param matrix the MinionIdentifier[][] where on the vector at index vectorIndexInMatrix an alldifferent constraint is imposed
     * @param vectorIndexInMatrix the int index of the matrix stating on which vector the alldifferent constraint is imposed 
     * @throws MinionException is thrown when the matrix is unknown or the index is out pf bounds
     */
    public void addAllDifferentConstraint(MinionIdentifier[][] matrix, int vectorIndexInMatrix) 
    	throws MinionException {
    	
    	MinionMatrix varMatrix = minionMatrices.get(matrix[0][0].getOriginalName());
    	
    	if(matrices2d.contains(varMatrix)) {
    		if(vectorIndexInMatrix >= 0 && vectorIndexInMatrix < matrix.length) {
    			MinionAllDifferent allDifferentConstraint = new MinionAllDifferent(matrix[vectorIndexInMatrix], 
    																			"row(m"+matrices2d.indexOf(varMatrix)+","+vectorIndexInMatrix+")");
    			allDifferentConstraints.add(allDifferentConstraint);
    		}
    		else throw new MinionException("Index "+vectorIndexInMatrix+" for matrix '"+matrix[0][0].getOriginalName()+"' out of bounds.");
    	}
    	else throw new MinionException("Imposing allDifferent on unknown vector:"+matrix[0][0].getOriginalName());
    }
    
    /**
     * Add an element constraint to the model.
     * 
     * @param vector the MinionIdentifier array containing the elements
     * @param index the MinionIdentifier stating the index of the vector where element should be
     * @param element the MinionIdentifier that should be in vector at index of index
     * @throws MinionException is thrown when the vector is unknown
     */
    public void addElementConstraint(MinionIdentifier[] vector, MinionIdentifier index, MinionIdentifier element) 
    	throws MinionException {
    	
    	MinionVector variableVector = minionVectors.get(vector[0].getOriginalName());
    	
    	if(matrices1d.contains(variableVector)) {
    		MinionElementConstraint constraint = new MinionElementConstraint("v"+matrices1d.indexOf(vector), index, element);
    		elementConstraints.add(constraint);
    	}
    	else throw new MinionException("Imposing element-constraint on unknown element-vector:"+vector[0].getOriginalName());
    }
    
    
    
    public int getVectorIndex(String originalName) throws MinionException {	
    	MinionVector vector = minionVectors.get(originalName);
    	if(vector == null) 
    		throw new MinionException
    		("Internal error: trying to get vector index of unknown vector: "+originalName);
    	
    	if(matrices1d.contains(vector)) 
    		return 	matrices1d.indexOf(vector);
    	
    	else return -1;
    	
    	
    }
    
    /**
     * 
     * @param originalName
     * @return the index the matrix has in the minion model, e.g. 2 if it is the 3rd matrix m2
     * @throws MinionException
     */
    public int getMatrixIndex(String originalName) throws MinionException {	
    	MinionMatrix matrix = minionMatrices.get(originalName);
    	
    	if(matrix == null)
    		throw new MinionException
    		("Internal error: trying to get matrix index of unknown matrix: "+originalName);
    	
    	if(matrices2d.contains(matrix)) 
    		return 	matrices2d.indexOf(matrix);
    	
    	else return -1;
    	
    	
    }
    
    /**
     * Get the index of the cube with name "originalName" in the minionModel (index 2 would mean
     * that the cube is the third cube in the Model)
     * 
     * @param originalName
     * @return
     * @throws MinionException
     */
    public int getCubeIndex(String originalName) throws MinionException {
    	MinionCube cube = minionCubes.get(originalName);
    	
    	if(cube == null)
    		throw new MinionException
    		("Internal error: trying to get cube index of unknown cube: "+originalName);
    	
    	if(matrices3d.contains(cube))
    		return matrices3d.indexOf(cube);
    	
    	else return -1;
    }
    
    
    
    public int getVectorOffset(String vectorName) 
    	throws MinionException 	{
    	
    	if(matrixOffsets.containsKey(vectorName)) {
    		int[] offsets = matrixOffsets.get(vectorName);
    		int elemOffset = offsets[0];
    		// in case the elements of the vector belong to a matrix and are just reused in the vector
    		if(offsets.length > 1)
    			elemOffset = offsets[1];
    		return elemOffset;
    	}
    	else throw new MinionException
    	("Internal error: searching for offset of unknown vector-variable: "+vectorName);
    }
    

    public int[] getMatrixOffsets(String matrixName) 
    	throws MinionException {
    	
    	int[] offsets = matrixOffsets.get(matrixName);
    	if(offsets == null) 
    		throw new MinionException
    		("Internal error: searching for offset of unknown matrix-variable: "+matrixName);
    			
    	else return offsets;
    }
    
    
    public int[] getCubeOffsets(String cubeName) 
    	throws MinionException {
    	
    	int[] offsets = matrixOffsets.get(cubeName);
       	if(offsets == null) 
    		throw new MinionException
    		("Internal error: searching for offset of unknown cube-variable: "+cubeName);
    			
    	else return offsets;
    }
    
    /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
       getMxElem
       %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */
   /* public MinionIdentifier getMxElem (int mxIdx, int elemIdx) {
    	MinionIdentifier[] mx = (MinionIdentifier[])matrices1d.get(mxIdx) ;
    	return mx[elemIdx] ;
    }*/
    
    
    /**
     * TODO: split this ugly thing up.
     * @return the String representing the Minion model
     *  
 	*/
    
   public String toString() {

	int i,j; // for the for-loops	
	int n = noZeroOnes + noBoundVariables + noSparseVariables + noDiscreteVariables +
           noDiscreteSparseVariables ;
	
	StringBuffer result = new StringBuffer() ;

	
	/** Preamble */
	result.append(MINION_VERSION) ;
	result.append(MinionGlobals.MINION_HEADER+"\n#");
	
	result.append("\n#\n# The variables used in this model correspond to the following\n# decision variables of the original problem:\n#   (minion\toriginal)");
	result.append(printOriginalNames());
	
	/** amount of different variables. first: boolean */
	result.append("\n"+noZeroOnes+"\n") ;
	print_debug("After writing down zeroOne variables") ;

	/** BoundsVars*/
	result.append(boundsVariables.size()+"\n") ;
/*	for (i = 0; i < boundsVariables.size(); i++) {
		MinionBoundsVariable variable = boundsVariables.get(i);
	    result.append(variable.getLowerBound() + " " + variable.getUpperBound() + " 1\n") ;
	}*/
	print_debug("written down size og boundsVariables..:"+boundsVariables.size());
	
	for(i=0; i<boundsList.size(); i++) {

		int[] bounds = boundsList.get(i);
	    print_debug("bounds variable def"+bounds[0]+" to "+bounds[1]+" amount "+bounds[2]);
		result.append(bounds[0]+" "+bounds[1]+" "+bounds[2]+"\n");
	}
	
	print_debug("After writing down bound variables") ;
	result.append("\n") ;
	
	/**  SparseBoundsVars */ 
	result.append(noSparseVariables+"\n") ;
	int[] sparseDomain ;
	for (i = 0; i < sparseVariables.size(); i++) {
	    sparseDomain = sparseVariables.get(i).getSparseDomain();
	    result.append("{") ;
	    for (j = 0; j < sparseDomain.length; j++) {
	    	result.append(" "+sparseDomain[j]) ;
		if (j < (sparseDomain.length - 1))
		    result.append(",") ;
	    }
	    result.append("} 1\n") ;
	}
	result.append("\n") ;
	
	/**  DiscreteVars */  
	result.append(noDiscreteVariables+"\n") ;
  
	for(i=0; i<discreteBoundsList.size(); i++) {
		int[] discreteBounds = discreteBoundsList.get(i);
		result.append(discreteBounds[0]+" "+discreteBounds[1]+" "+discreteBounds[2]+"\n");
	}
	
	result.append("\n") ;
	
	/**  DiscreteSparseVars
	 * Currently unsupported in dominion.
	 * */
	result.append("0\n") ;
   	


	/** Variable Order */	
	if(ENABLE_VARIABLE_ORDERING) {
	    result.append("[") ;
	    for (i = 0; i < n; i++) {
	    	result.append("x"+i) ;
		if (i < (n-1))
		    result.append(", ") ;
	    }
	    result.append("]\n") ;   
	    
	}
	else result.append("[ ] ");


	/** Value Order */
	if(ENABLE_VALUE_ORDERING) {
	    result.append("[") ;
	    for (i = 0; i < n; i++) {
		result.append("a") ;
		if (i < (n-1))
		    result.append(", ") ;
	    }
	    result.append("]\n") ;    
	}
	else result.append("[ ] \n");

	
	/** 1d Matrices */
	if(originalVariables.size() > 0) {
		MinionIdentifier[] originalVariableVector = new MinionIdentifier[originalVariables.size()];
		for(i=0; i<originalVariables.size(); i++)
			originalVariableVector[i] = originalVariables.get(i);
		MinionVector vector = new MinionVector(originalVariableVector, originalVariableVector[0].getOriginalName(), 0);
		matrices1d.add(vector);
	}
	
	result.append(matrices1d.size()+"\n") ;
	for (i = 0; i < matrices1d.size(); i++) {
	    MinionIdentifier[] matrix1d = matrices1d.get(i).getElements() ;
	    result.append("[") ;
	    for (j = 0; j < matrix1d.length; j++) {
	    	result.append(matrix1d[j]) ;
	    	if (j < (matrix1d.length-1)) 
	    		result.append(", ") ;
	    	}
	    result.append("]\n") ;    
	}
	
	// 2d matrices
	result.append(matrices2d.size()+"\n");
	for(i=0; i<matrices2d.size(); i++) {
		MinionIdentifier[][] matrix = matrices2d.get(i).getMatrix();
		result.append("[");
		for(int vectorIndex=0; vectorIndex<matrix.length; vectorIndex++) {
			result.append("[");
			for(int elemIndex=0; elemIndex<matrix[vectorIndex].length; elemIndex++) {
				result.append(matrix[vectorIndex][elemIndex].toString());
				if(elemIndex < matrix[vectorIndex].length-1)
					result.append(",");
			}
			if(vectorIndex < matrix.length -1)
				result.append("],\n");
			else result.append("]\n");
		}
		result.append("]\n");
	}
	
	
	 
	// 3d Matrices 
	result.append(matrices3d.size()+"\n");
	for(i=0; i<matrices3d.size(); i++) {
		MinionIdentifier[][][] cube = matrices3d.get(i).getCube();
		result.append("[");
		for(int matrixIndex =0; matrixIndex<cube.length; matrixIndex++) {
			result.append("[");
			for(int vectorIndex=0; vectorIndex<cube[matrixIndex].length; vectorIndex++) {
				result.append("[");
				for(int elemIndex=0; elemIndex<cube[matrixIndex][vectorIndex].length; elemIndex++) {
					result.append(cube[matrixIndex][vectorIndex][elemIndex].toString());
					if(elemIndex < cube[matrixIndex][vectorIndex].length-1)
						result.append(",");
				}
				if(vectorIndex < cube[matrixIndex].length -1)
					result.append("],\n");
				else result.append("]\n");
			}
			if(matrixIndex <cube.length-1)
				result.append("], \n");
			else result.append("]\n");
		}	
		result.append("]\n");
	}
	
	
	/** Objective */
	if (objectiveVar == null) result.append("objective none\n") ;
	else
	    result.append("objective "+
			  ( maximising ? "maximising " : "minimising " )+
			  objectiveVar+"\n") ;
	
	
	/** printing
	 * TODO: improve printing */
	
	/*  in this case the last vector (1-dim matrix) is the vector of 
	 *   decision variables we want to print */
	if(this.matrices2d.size() > 0)
		result.append("print m"+(matrices2d.size()-1)+"\n");
	else if(originalVariables.size() > 0) 
		result.append("print v"+(matrices1d.size()-1)+"\n");
	else 
		result.append("print none\n") ;
	
	/** constraints */
    for (i = 0; i < allDifferentConstraints.size(); i++)
	    result.append(allDifferentConstraints.get(i).toString()+"\n") ;
	
	
	print_debug("BEFORE writing down eq-constraints") ;

	/** equality constraints */
	for (i = 0; i < equalityConstraints.size(); i++) {
	    result.append( ( (MinionEqConstraint) equalityConstraints.get(i)).toString() );
	    result.append("\n");
	}
	
	print_debug("AFTER writing down eq-constraints") ;
	
	/** disequality constraints */
	for (i = 0; i < disequalityConstraints.size(); i++) {
		print_debug("printing the "+i+"th disequality constraint: "+(disequalityConstraints.get(i)).toString());
	    result.append( ( (MinionDisEqConstraint) disequalityConstraints.get(i)).toString() );
	    result.append("\n");
	}
	
	/** inequality constraints */
	for (i = 0; i < inequalityConstraints.size(); i++) {
	    result.append( ( (MinionInEqConstraint) inequalityConstraints.get(i)).toString() );
	    result.append("\n");
	}
	
	/** element constraints 
	for (i = 0; i < elementConstraints.size(); i++) {
	    int[] args = (int[])elementConstraints.get(i) ;
	    result.append("element(v"+args[0]+", x"+args[1]+", x"+args[2]+")\n") ;
	}
	*/
	
	/** minimum constraints */
	for (i = 0; i < minConstraints.size(); i++) {
	    result.append( ( (MinionMinConstraint) minConstraints.get(i) ).toString());
	    result.append("\n");
	}
	
	/** maximum constraints */
	for (i = 0; i < maxConstraints.size(); i++) {
	    result.append( ( (MinionMaxConstraint) maxConstraints.get(i) ).toString());
	    result.append("\n");
	}
	
	/** occurence constraints 
	for (i = 0; i < occurrenceConstraints.size(); i++) {
	    int[] args = (int[])occurrenceConstraints.get(i) ;
	    result.append("occurrence(v"+args[0]+", x"+args[1]+", "+
			  args[2]+")\n") ;
	}
	*/
	
	print_debug("BEFORE writing down product constraints") ;
	
	/** product constraints */
	for (i = 0; i < productConstraints.size(); i++) {
	    result.append( ((MinionProductConstraint) productConstraints.get(i)).toString()) ;
	    result.append("\n");
	}
	
	print_debug("AFTER writing down product constraints") ;
	
	/** sumLeqVector constraints */
	for (i = 0; i < sumLeqVectorConstraints.size(); i++) {
	    int[] args = (int[])sumLeqVectorConstraints.get(i) ;
	    result.append("sumleq(v"+args[0]+", x"+args[1]+", x"+")\n") ;
	}
	
	print_debug("BEFORE writing down sumleq constraints") ;

	/** sumLeqVariables constraints */
	for (i = 0; i < sumLeqVariablesConstraints.size(); i++) {
	    result.append( ((MinionSumLeqConstraint) sumLeqVariablesConstraints.get(i)).toString()+"\n");
	}

	print_debug("BEFORE writing down sumleq constraints") ;
	
	/** sumGeq constraints */
	/*for (i = 0; i < sumGeqConstraints.size(); i++) {
	    int[] args = (int[])sumGeqConstraints.get(i) ;
	    result.append("sumgeq(v"+args[0]+", x"+args[1]+", x"+")\n") ;
	}*/
	

		
	/** sumGeqVariables constraints */
	for (i = 0; i < sumGeqVariablesConstraints.size(); i++) {	    
	    result.append( ((MinionSumGeqConstraint) sumGeqVariablesConstraints.get(i)).toString()+"\n");
	}
	
	
	/** weightedSumLeqVariables constraints */
	for(i = 0; i < weightedSumLeqVariablesConstraints.size();i++) {	    
	    result.append( ((MinionWeightedSumLeqConstraint) weightedSumLeqVariablesConstraints.get(i)).toString());
	}
	
	print_debug("Printing sumgeq constraints NOW");
	/** weightedSumLeqVariables constraints */
	for(i = 0; i < weightedSumGeqVariablesConstraints.size();i++) {
	    result.append( ((MinionWeightedSumGeqConstraint) weightedSumGeqVariablesConstraints.get(i)).toString());
	}
	
	print_debug("before writing reification constraints ");
	/** reification constraints */
	for(i = 0; i < reificationConstraints.size(); i++)
	    result.append( ((MinionReifyConstraint) reificationConstraints.get(i)).toString()+"\n");

	print_debug("before other, after writing reification constraints ");
	
	for(i=0; i<otherConstraints.size(); i++) {
	    result.append( (otherConstraints.get(i)).toString()+"\n");
	}
	
	print_debug("after writing other constraints ");
	return result.toString() ;
   }
   
   /** 
   * If the DEBUG-flag in the Globals-interface is set to true, then
   * print the debug-messages. These messages are rather interesting 
   * for the developper than for the user.
   * @param s  the String to be printed on the output
   */

   private static void print_debug(String s) {
   	if(DEBUG)
   		System.out.println("[ DEBUG minionModel ] "+s);
   }

  /** 
  *	If the PRINT_MESSAGE-flag in the Globals-interface is set to true, then
  * print some general messages on the output-stream. These messages are 
  * relevant for the user and won't give enough information for the
  * developper.
  * @param String s : the String to be printed on the output
  */

  /**private static void print_message(String s) {
	if(PRINT_MESSAGE)
	    System.out.println(s);
  	}
   */
    
   /**
    * 
    * @return a Minion comment String containing information
    * about the original variable names and their names in the Minion
    * model.
    */
   
   private String printOriginalNames() {
	   
	   print_debug("Gonna print the original names now");
	   
	   int i;
	   StringBuffer result = new StringBuffer();
	   MinionIdentifier variable = null;
	   MinionIdentifier[] vector = null;
	   MinionIdentifier[][] matrix = null;
	   
	   if(booleanVariables.size() >0)
		   result.append("\n#\n# Boolean domain variables:\n#");
	   for(i=0; i<booleanVariables.size(); i++) {
		   variable = booleanVariables.get(i);
		   result.append("\n#\t"+variable.toString()+"\t"+variable.getOriginalName());
	   }
	   
	   if(boundsVariables.size() >0)
		   result.append("\n#\n# Integer bound domain variables:\n#");
	   for(i=0; i<boundsVariables.size(); i++) {
		   variable = boundsVariables.get(i);
		   result.append("\n#\t"+variable.toString()+"\t"+variable.getOriginalName());
	   }
	   
	   if(sparseVariables.size() >0)
		   result.append("\n#\n# Sparse bound domain variables:\n#");
	   for(i=0; i<sparseVariables.size(); i++) {
		   variable = sparseVariables.get(i);
		   result.append("\n#\t"+variable.toString()+"\t"+variable.getOriginalName());
	   }
	   
	   if(discreteVariables.size() >0)
		   result.append("\n#\n# Discrete bound domain variables:\n#");
	   for(i=0; i<discreteVariables.size(); i++) {
		   variable = discreteVariables.get(i);
		   result.append("\n#\t"+variable.toString()+"\t"+variable.getOriginalName());
	   }
	   
	   if(discreteSparseVariables.size() >0)
		   result.append("\n#\n# Discrete sparse bound domain variables:\n#");
	   for(i=0; i<discreteSparseVariables.size(); i++) {
		   variable = discreteSparseVariables.get(i);
		   result.append("\n#\t"+variable.toString()+"\t"+variable.getOriginalName());
	   }
	
	   if(matrices1d.size() > 0)
		   result.append("\n#\n# 1-dimensional matrices (vectors):");
	   
	   for(i=0; i<matrices1d.size(); i++) {
		   vector = matrices1d.get(i).getElements();
		   String originalName = matrices1d.get(i).getOriginalName();
		   int offset[] = matrixOffsets.get(originalName);
		   int elemOffset = offset[0];
		   if(offset.length  > 1)
			   elemOffset = offset[1];
		   print_debug("just got the offsets of the "+i+". vector:"+originalName+" with length: "+vector.length);
		   for(int j=0; j<vector.length; j++) {
			   print_debug(j+". iteration, appending :"+vector[j].toString()+"\t"+originalName+"["+(elemOffset+j)+"]");
			   result.append("\n#\t"+vector[j].toString()+"\t"+originalName+"["+(elemOffset+j)+"]");
		   }
	   }

	   if(matrices2d.size() > 0)
		   result.append("\n#\n# 2-dimensional matrices:");
	   
	   for(i=0; i<matrices2d.size(); i++) {
		   matrix = matrices2d.get(i).getMatrix();
		   String originalName = matrices2d.get(i).getOriginalName();
		   int offset[] = matrixOffsets.get(originalName);
		   for(int j=0; j<matrix.length; j++) {
			   for(int elemIndex=0; elemIndex < matrix[j].length; elemIndex++)   
				   result.append("\n#\t"+matrix[j][elemIndex].toString()+"\t"+originalName+
						   "["+(offset[0]+j)+","+(offset[1]+elemIndex)+"]");
		   }
	   }
	   
	   result.append("\n");
	   
	   return result.toString();
   }
   
   public ArrayList<MinionConstraint> getConstraintList() { 
   
	   return constraintList;
	   
   }
   
   /**
    * This method does not remove any constraints!! The list only contains none anymore.
    *
    */
   public void clearConstraintList() {
	   constraintList.clear();
   }
   
   
   public ArrayList<MinionIdentifier> getMinionIdentifiers() {
	   return minionIdentifiers;
   }
   
   /**
    * This method does not remove any identifiers!! The list only contains none anymore.
    *
    */
   
  public void clearIdentifierList() {
	   minionIdentifiers.clear();
   }
  
  
  public void addIdentifier(MinionIdentifier identifier)
  	throws ClassNotFoundException, MinionException {
	  
	  print_debug("the object is an instance of "+identifier.getClass().toString()+" and called "+identifier.getOriginalName());
	  
	  if ( Class.forName ("minionModel.MinionBoolVariable").isInstance (identifier)) {
			booleanVariables.add((MinionBoolVariable) identifier);
			int index = noZeroOnes++;
			((MinionBoolVariable) identifier).setIndices(index, index);
		}
	  else if ( Class.forName ("minionModel.MinionBoundsVariable").isInstance (identifier)) {
		  
		  if(identifier.getLowerBound() == 0 && identifier.getUpperBound() == 1) {
			MinionBoolVariable boolVar = new MinionBoolVariable(1, identifier.getOriginalName());
			booleanVariables.add(boolVar);	
			int index = noBoundVariables++;
			(boolVar).setIndices(index, index); 
		  }
		  else {
			boundsVariables.add((MinionBoundsVariable) identifier);
		  	noBoundVariables++;
			int[] bounds = new int[] { identifier.getLowerBound(), identifier.getUpperBound(), 1 };
	
	    	boundsList.add(bounds);
	    	
		  }
		}
	  else if ( Class.forName ("minionModel.MinionSBoundsVariable").isInstance (identifier)) {
			sparseVariables.add((MinionSBoundsVariable) identifier);
			noSparseVariables++;
		}
	  else if ( Class.forName ("minionModel.MinionDiscreteVariable").isInstance (identifier)) {
			discreteVariables.add((MinionDiscreteVariable) identifier);
			noDiscreteVariables++;
			int[] bounds = new int[] { identifier.getLowerBound(), identifier.getUpperBound(), 1 };
			
	    	discreteBoundsList.add(bounds);
		}
	  
	  else if( Class.forName ("minionModel.MinionDiscreteSVariable").isInstance (identifier)) {
			discreteSparseVariables.add((MinionDiscreteSVariable) identifier);
			noDiscreteSparseVariables++;
		}
	  
	  else if( Class.forName ("minionModel.MinionConstant").isInstance (identifier)) {
			return;
		}
	  else 
		  throw new MinionException
		  ("Cannot add MinionIdentifier '"+identifier.toString()+
				  "'. Unknown class:"+identifier.getClass().toString());
	  	minionIdentifiers.add(identifier);
	  	
	  	print_debug("Adding VARIABLE to minionModel:"+identifier.getOriginalName()+", with minionName: x"+identifier.getAbsoluteIndex());
  }
  
  /**
   * Add a variable vector that consists only of known elements, that already have been
   * added to the minionModel.
   * 
   * @param variableVector
 * @param offset
 * @param originalName TODO
   * @throws ClassNotFoundException
   * @throws MinionException
   */
  public void addKnownIdentifierVector(MinionIdentifier[] variableVector, int offset, String originalName) {

	  MinionVector vector = new MinionVector(variableVector, originalName, offset);
	  matrices1d.add(vector);
	  matrixOffsets.put(originalName, new int[] { offset});
	  minionVectors.put(originalName,vector);
	  
  }
  
  public void addKnownIdentifierMatrix(MinionIdentifier[][] variableMatrix, int offset[], String originalName) {

	  MinionMatrix matrix = new MinionMatrix(variableMatrix, originalName, offset);
	  matrices2d.add(matrix);
	  matrixOffsets.put(originalName, offset);
	  minionMatrices.put(originalName, matrix);
}
  
  /**
   * Add a vector consisting of the identifiers in variableVector and the corresponding offset to
   * the minion Model.
   * 
   * @param variableVector
   * @param offset
   * @throws ClassNotFoundException
   * @throws MinionException
   */
  public void addIdentifierVector(MinionIdentifier[] variableVector, int offset)
	throws ClassNotFoundException, MinionException {
	  
	  
	  for(int i=0; i<variableVector.length; i++)
		  print_debug("Adding VARIABLE to minionModel:"+variableVector[i].getOriginalName()+
				  ", with minionName: x"+variableVector[i].getAbsoluteIndex());
	  
	  int relativeIndex = matrices1d.size() ;
	 	  
	  print_debug("the object is an instance of "+variableVector.getClass().toString());
	  
	  if ( Class.forName ("minionModel.MinionBoolVariable").isInstance (variableVector[0])) {
		
		  for (int i1 = 0; i1 < variableVector.length; i1++) {
			  variableVector[i1].setRelativeIndex(relativeIndex);
			  variableVector[i1].setAbsoluteIndex(noZeroOnes++);
			  if(!(variableVector[i1].getOriginalName().startsWith("freshVariable")))
				  originalVariables.add(variableVector[i1]);
		
			  booleanVariables.add((MinionBoolVariable) variableVector[i1]);
			  minionIdentifiers.add(variableVector[i1]);
		  }
	  
		  matrixOffsets.put(variableVector[0].getOriginalName(), new int[] { offset });
		  MinionVector vector = new MinionVector(variableVector, variableVector[0].getOriginalName(), offset);
		  matrices1d.add(vector) ;
		  minionVectors.put(variableVector[0].getOriginalName(),vector);
	  	}
	  
	  else if ( Class.forName ("minionModel.MinionBoundsVariable").isInstance (variableVector[0])) {

	    	for (int i = 0; i < variableVector.length; i++) {
	    		variableVector[i].setRelativeIndex(relativeIndex);
	    		boundsVariables.add((MinionBoundsVariable) variableVector[i]);
	    		minionIdentifiers.add(variableVector[i]);
	    		print_debug("Added bounds variable: "+variableVector[i].getOriginalName());
	    		if(!(variableVector[i].getOriginalName().startsWith("freshVariable")))
	     			originalVariables.add(variableVector[i]);
	    		
	    		noBoundVariables++;
	    	}
	    	int[] bounds = new int[] { variableVector[0].getLowerBound(), variableVector[0].getUpperBound(), variableVector.length };
	    	boundsList.add(bounds);
	    	
	    	matrixOffsets.put(variableVector[0].getOriginalName(), new int[] {offset});	
	    	MinionVector vector = new MinionVector(variableVector, variableVector[0].getOriginalName(), offset);
	    	matrices1d.add(vector) ;
	    	minionVectors.put(variableVector[0].getOriginalName(),vector);
	  		}	
	  
	  
	  else if ( Class.forName ("minionModel.MinionDiscreteVariable").isInstance (variableVector[0])) {

	    	for (int i = 0; i < variableVector.length; i++) {
	    		variableVector[i].setRelativeIndex(relativeIndex);
	    		discreteVariables.add((MinionDiscreteVariable) variableVector[i]);
	    		minionIdentifiers.add(variableVector[i]);
	    		print_debug("Added bounds variable: "+variableVector[i].getOriginalName());
	    		if(!(variableVector[i].getOriginalName().startsWith("freshVariable")))
	     			originalVariables.add(variableVector[i]);
	    		
	    		noDiscreteVariables++;
	    	}
	    	int[] bounds = new int[] { variableVector[0].getLowerBound(), variableVector[0].getUpperBound(), variableVector.length };
	    	discreteBoundsList.add(bounds);
	    	
	    	matrixOffsets.put(variableVector[0].getOriginalName(), new int[] {offset});	
	    	MinionVector vector = new MinionVector(variableVector, variableVector[0].getOriginalName(), offset);
	    	matrices1d.add(vector) ;
	    	minionVectors.put(variableVector[0].getOriginalName(),vector);
	  		}	
	  
	 
	
	  else 
		  throw new MinionException
		  ("Cannot add MinionIdentifier '"+variableVector[0].toString()+
				  "'. Unknown class:"+variableVector[0].getClass().toString());
}
  
  
  
  public void addIdentifierMatrix(MinionIdentifier[][] variableMatrix, int[] offset)
	throws ClassNotFoundException, MinionException {
	  
	 	  
	  print_debug("the object is an instance of "+variableMatrix[0][0].getClass().toString());
	  
	  if ( Class.forName ("minionModel.MinionBoolVariable").isInstance (variableMatrix[0][0])) 
		add01VariableMatrix((MinionBoolVariable[][]) variableMatrix, offset[0], offset[1]);
	  	
	  
	  else if ( Class.forName ("minionModel.MinionBoundsVariable").isInstance (variableMatrix[0][0])) 
		addBoundsVariableMatrix(( MinionBoundsVariable[][]) variableMatrix, offset[0], offset[1]);
	  
	  else if ( Class.forName ("minionModel.MinionDiscreteVariable").isInstance (variableMatrix[0][0])) 
			addDiscreteVariableMatrix(( MinionDiscreteVariable[][]) variableMatrix, offset[0], offset[1]);
	  
	  else 
		  throw new MinionException
		  ("Cannot add MinionIdentifier '"+variableMatrix[0].toString()+
				  "'. Unknown class:"+variableMatrix[0].getClass().toString());
}

  /**
   * Add the objective: in Minion the objective may only be a variable 
   * 
   * @param variable
   * @param minimising if true, the var is minimised if false maximised
   */
  public void addObjective(MinionIdentifier variable, boolean minimising) {
	  
	  this.maximising = !minimising;
	  this.objectiveVar = variable;
	  
  }
  
  
  public void addConstraint(MinionConstraint constraint) {
	otherConstraints.add(constraint);	
	print_debug("added other constraint "+constraint.toString()+" to other cosntraints list LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
	  
  }
  
  public boolean containsVector(String originalVectorName) {
	  
	  if(minionVectors.containsKey(originalVectorName))
		  return true;
	  else return false;
	  
  }
  
  
}
