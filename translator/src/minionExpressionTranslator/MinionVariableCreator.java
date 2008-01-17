package minionExpressionTranslator;

import java.util.ArrayList;
import java.util.HashMap;

import minionModel.MinionBoolVariable;
import minionModel.MinionBoundsVariable;
import minionModel.MinionDiscreteVariable;
import minionModel.MinionException;
import minionModel.MinionIdentifier;
import minionModel.MinionModel;

import conjureEssenceSpecification.Domain;
import conjureEssenceSpecification.EssenceGlobals;
import conjureEssenceSpecification.RangeAtom;

public class MinionVariableCreator implements MinionTranslatorGlobals {

   	HashMap<String, Domain> decisionVariables;
    ArrayList<String> decisionVariablesNames;
	MinionModel myMinionModel;
    
	HashMap<String, MinionIdentifier> minionVariables;
	HashMap<String, MinionIdentifier[]> minionVectors;
	HashMap<String, MinionIdentifier[][]> minionMatrices;
	HashMap<String, MinionIdentifier[][][]> minionCubes;
	
	boolean useDiscreteVariables;
	
    public MinionVariableCreator(HashMap<String, MinionIdentifier> minionVars,
								 HashMap<String, MinionIdentifier[]> minionVecs,
							     HashMap<String, MinionIdentifier[][]> minionMatrixz, 
								 HashMap<String, MinionIdentifier[][][]> minionCubes, 
							     HashMap<String, Domain> decisionVars, 
							     ArrayList<String> varNames, 
							     MinionModel minionModel, boolean useDiscreteVars) {
    	
    	this.decisionVariables = decisionVars;
    	this.decisionVariablesNames = varNames;
    	this.myMinionModel = minionModel;
    	this.minionVariables = minionVars;
    	this.minionVectors = minionVecs;
    	this.minionMatrices = minionMatrixz;
    	this.minionCubes = minionCubes;
    	this.useDiscreteVariables = useDiscreteVars;
    }
	
    
    protected MinionIdentifier addFreshVariable(int lowerBound, int upperBound, String name, boolean discrete) {
    	
    	MinionIdentifier variable = null;
    	
    	if(lowerBound == 0 && upperBound == 1) {
       		variable = new MinionBoolVariable(1, name);
    		myMinionModel.add01Variable((MinionBoolVariable) variable);    	
    	}
    	else if(discrete) {  		
    		variable = new MinionDiscreteVariable(lowerBound, upperBound, name);
    		myMinionModel.addDiscreteVariable((MinionDiscreteVariable) variable); 
    	}
    	// bounds variable
    	else {
       		variable = new MinionBoundsVariable(lowerBound, upperBound, name);
    		myMinionModel.addBoundsVariable((MinionBoundsVariable) variable); 			
    	}
    	
    	return variable;
    }
    
    /**
     * matrixIndex, vectorIndex and elementIndex have to be adjusted with the offsets, this means that 
     * they have to range from 0..vectorlength/elemenlength. 
     * 
     * @param cubeName
     * @param matrixIndex
     * @param vectorIndex
     * @param elementIndex
     * @throws MinionException
     */
    
    protected void addCubeElement(String cubeName, int matrixIndex,  int vectorIndex, int elementIndex) 
	throws MinionException {
	
	print_debug("creating cube element for: "+cubeName+"["+matrixIndex+","+vectorIndex+","+elementIndex+"].");
	
	Domain domain = decisionVariables.get(cubeName);
	if(domain.getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN) {
		throw new MinionException("Internal error: expected matrix range as domain for variable '"+cubeName+"'.");
	}
	
	Domain rangeDomain = domain.getMatrixDomain().getRangeDomain();
	
	MinionIdentifier[][][] cube = minionCubes.get(cubeName);
	if(cube == null) 
		throw new MinionException("Unknown matrix '"+cubeName+"'.");
	
    switch(rangeDomain.getRestrictionMode()) {

     
	case EssenceGlobals.BOOLEAN_DOMAIN:
		MinionBoolVariable boolVariable = new MinionBoolVariable(1, cubeName);
		myMinionModel.add01Variable(boolVariable);
		cube[matrixIndex][vectorIndex][elementIndex] = (MinionIdentifier) boolVariable;
		break;			        
		
	case EssenceGlobals.INTEGER_RANGE: /** TODO: we assume that there are no sparse bounds here yet*/
		// first get the upper and lower bound 
		print_debug("looking at the INTEGER domain of variable with name :"+cubeName+" with domain: "+rangeDomain.toString());
		RangeAtom[] rangeList = rangeDomain.getIntegerDomain().getRangeList();
		int lowerBound = rangeList[0].getLowerBound().getAtomicExpression().getNumber();
		int upperBound = rangeList[0].getUpperBound().getAtomicExpression().getNumber();
		
		if(lowerBound == 0 && upperBound == 1) {
			MinionBoolVariable booolVariable = new MinionBoolVariable(1, cubeName);
			myMinionModel.add01Variable(booolVariable);
			cube[matrixIndex][vectorIndex][elementIndex] = (MinionIdentifier) booolVariable;
		}
		else {
		// then create a Minion Identifier and insert it into the minionModel
			
			if(!useDiscreteVariables) {
				MinionBoundsVariable intDomainVariable = new MinionBoundsVariable(lowerBound,upperBound,cubeName);
				myMinionModel.addBoundsVariable(intDomainVariable);
				cube[matrixIndex][vectorIndex][elementIndex] = (MinionIdentifier) intDomainVariable;
			}
			else {
				MinionDiscreteVariable intDomainVariable = new MinionDiscreteVariable(lowerBound,upperBound,cubeName);
				myMinionModel.addDiscreteVariable(intDomainVariable);
				cube[matrixIndex][vectorIndex][elementIndex] = (MinionIdentifier) intDomainVariable;
			}
		}
		break;
     
    
    
	default:
		throw new MinionException
		("Cannot translate cube element of '"+cubeName+"' with domain type: "+domain+". Integer or boolean domain required.");
    }
}
    
    
    /**
     * Both vectorIndex and elementIndex have to be adjusted with the offsets, this means that 
     * they have to range from 0..vectorlength/elemenlength. 
     * 
     * @param matrixName
     * @param vectorIndex
     * @param elementIndex
     * @throws MinionException
     */
    protected void addMatrixElement(String matrixName, int vectorIndex, int elementIndex) 
    	throws MinionException {
    	
    	print_debug("creating matrix element for: "+matrixName+"["+vectorIndex+","+elementIndex+"].");
		
		Domain domain = decisionVariables.get(matrixName);
		if(domain.getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN) {
			throw new MinionException("Internal error: expected matrix range as domain for variable '"+matrixName+"'.");
		}
		
    	Domain rangeDomain = domain.getMatrixDomain().getRangeDomain();
    	
    	MinionIdentifier[][] matrix = minionMatrices.get(matrixName);
    	if(matrix == null) 
    		throw new MinionException("Unknown matrix '"+matrixName+"'.");
    	
        switch(rangeDomain.getRestrictionMode()) {
	
         
		case EssenceGlobals.BOOLEAN_DOMAIN:
			MinionBoolVariable boolVariable = new MinionBoolVariable(1, matrixName);
			myMinionModel.add01Variable(boolVariable);
			matrix[vectorIndex][elementIndex] = (MinionIdentifier) boolVariable;
			break;			        
			
		case EssenceGlobals.INTEGER_RANGE: /** TODO: we assume that there are no sparse bounds here yet*/
			// first get the upper and lower bound 
			print_debug("looking at the INTEGER domain of variable with name :"+matrixName+" with domain: "+rangeDomain.toString());
			RangeAtom[] rangeList = rangeDomain.getIntegerDomain().getRangeList();
			int lowerBound = rangeList[0].getLowerBound().getAtomicExpression().getNumber();
			int upperBound = rangeList[0].getUpperBound().getAtomicExpression().getNumber();
			
			if(lowerBound == 0 && upperBound == 1) {
				MinionBoolVariable booolVariable = new MinionBoolVariable(1, matrixName);
				myMinionModel.add01Variable(booolVariable);
				matrix[vectorIndex][elementIndex] = (MinionIdentifier) booolVariable;
			}
			else {
			// then create a Minion Identifier and insert it into the minionModel
				
				if(!useDiscreteVariables) {
					MinionBoundsVariable intDomainVariable = new MinionBoundsVariable(lowerBound,upperBound,matrixName);
					myMinionModel.addBoundsVariable(intDomainVariable);
					matrix[vectorIndex][elementIndex] = (MinionIdentifier) intDomainVariable;
				}
				else {
					MinionDiscreteVariable intDomainVariable = new MinionDiscreteVariable(lowerBound,upperBound,matrixName);
					myMinionModel.addDiscreteVariable(intDomainVariable);
					matrix[vectorIndex][elementIndex] = (MinionIdentifier) intDomainVariable;
				}
			}
			break;
         
        
        
    	default:
			throw new MinionException
			("Cannot translate matrix '"+matrixName+"' with domain type: "+domain+". Integer or boolean domain required.");
        }
    }
    
    /**
     * elementIndex has to be adjusted with the offsets, this means that 
     * it has to range from 0..elemenlength. 
     * 
     * @param vectorName
     * @param elementIndex
     * @throws MinionException
     */
    
    protected void addVectorElement(String vectorName,  int elementIndex) 
	throws MinionException {
	
	print_debug("creating matrix element for: "+vectorName+"["+elementIndex+"].");
	
	Domain domain = decisionVariables.get(vectorName);
	if(domain.getRestrictionMode() != EssenceGlobals.MATRIX_DOMAIN) {
		throw new MinionException("Internal error: expected vector range as domain for variable '"+vectorName+"'.");
	}
	
	Domain rangeDomain = domain.getMatrixDomain().getRangeDomain();
	
	MinionIdentifier[] vector = minionVectors.get(vectorName);
	if(vector == null) 
		throw new MinionException("Unknown vector '"+vectorName+"'.");
	
    switch(rangeDomain.getRestrictionMode()) {

     
	case EssenceGlobals.BOOLEAN_DOMAIN:
		MinionBoolVariable boolVariable = new MinionBoolVariable(1, vectorName);
		myMinionModel.add01Variable(boolVariable);
		vector[elementIndex] = (MinionIdentifier) boolVariable;
		break;			        
		
	case EssenceGlobals.INTEGER_RANGE: /** TODO: we assume that there are no sparse bounds here yet*/
		// first get the upper and lower bound 
		print_debug("looking at the INTEGER domain of variable with name :"+vectorName+" with domain: "+rangeDomain.toString());
		RangeAtom[] rangeList = rangeDomain.getIntegerDomain().getRangeList();
		int lowerBound = rangeList[0].getLowerBound().getAtomicExpression().getNumber();
		int upperBound = rangeList[0].getUpperBound().getAtomicExpression().getNumber();
		
		if(lowerBound == 0 && upperBound == 1) {
			MinionBoolVariable booolVariable = new MinionBoolVariable(1, vectorName);
			myMinionModel.add01Variable(booolVariable);
			vector[elementIndex] = (MinionIdentifier) booolVariable;
		}
		else {
		// then create a Minion Identifier and insert it into the minionModel
			
			if(!useDiscreteVariables) {
				MinionBoundsVariable intDomainVariable = new MinionBoundsVariable(lowerBound,upperBound,vectorName);
				myMinionModel.addBoundsVariable(intDomainVariable);
				vector[elementIndex] = (MinionIdentifier) intDomainVariable;
			}
			else {
				MinionDiscreteVariable intDomainVariable = new MinionDiscreteVariable(lowerBound,upperBound,vectorName);
				myMinionModel.addDiscreteVariable(intDomainVariable);
				vector[elementIndex] = (MinionIdentifier) intDomainVariable;
			}
		}
		break;
     
    
    
	default:
		throw new MinionException
		("Cannot translate vector element of '"+vectorName+"' with domain type: "+domain+". Integer or boolean domain required.");
    }
}

    
    /**
     * Add a new decision variable with name variableName to the MinionModel. This may be a single variable,
     * matrix etc depending on its domain.
     * 
     * @param variableName
     * @throws MinionException
     */
	protected void addNewVariable(String variableName) 
		throws MinionException 	{

		
		print_debug("creating variable with name :"+variableName);
		
		Domain domain = decisionVariables.get(variableName);
		
		// TODO: forgot IdentifierRange!!! -> add to ExpressionEvaluator to evaluate all identifier-domains
		
		switch(domain.getRestrictionMode()) {
		
		case EssenceGlobals.BOOLEAN_DOMAIN:
			MinionBoolVariable boolVariable = new MinionBoolVariable(1, variableName);
			myMinionModel.add01Variable(boolVariable);
			minionVariables.put(variableName, boolVariable);
			break;			        
			
		case EssenceGlobals.INTEGER_RANGE: /** TODO: we assume that there are no sparse bounds here yet*/
			// first get the upper and lower bound 
			print_debug("looking at the INTEGER domain of variable with name :"+variableName+" with domain: "+domain.toString());
			RangeAtom[] rangeList = domain.getIntegerDomain().getRangeList();
			int lowerBound = rangeList[0].getLowerBound().getAtomicExpression().getNumber();
			int upperBound = rangeList[0].getUpperBound().getAtomicExpression().getNumber();
			
			if(lowerBound == 0 && upperBound == 1) {
				MinionBoolVariable booolVariable = new MinionBoolVariable(1, variableName);
				myMinionModel.add01Variable(booolVariable);
				minionVariables.put(variableName, booolVariable);
			}
			else {
			// then create a Minion Identifier and insert it into the minionModel
				
				if(!useDiscreteVariables) {
					MinionBoundsVariable intDomainVariable = new MinionBoundsVariable(lowerBound,upperBound,variableName);
					myMinionModel.addBoundsVariable(intDomainVariable);
					minionVariables.put(variableName, intDomainVariable); 
				}
				else {
					MinionDiscreteVariable intDomainVariable = new MinionDiscreteVariable(lowerBound,upperBound,variableName);
					myMinionModel.addDiscreteVariable(intDomainVariable);
					minionVariables.put(variableName, intDomainVariable);
				}
			}
			break;
		
		case EssenceGlobals.MATRIX_DOMAIN:
			/** TODO: at the moment we only support 1-dim matrices */
			Domain[] indexDomain = domain.getMatrixDomain().getIndexDomains();
			Domain rangeDomain = domain.getMatrixDomain().getRangeDomain();
			
			switch(indexDomain.length) {
			
			/** 1-dimensional */
			case 1: 
					if(rangeDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
						int indexLength = 0;
						int offset = 0;
						
						if(indexDomain[0].getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
							/** MATRIX id INDEXED BY bool OF bool */
							indexLength = 2;
						}	
						else if(indexDomain[0].getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
							/** MATRIX id INDEXED BY int OF bool */
							RangeAtom[] bounds = indexDomain[0].getIntegerDomain().getRangeList();
							offset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
							indexLength = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
								          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;								
						}
						else 
							throw new MinionException("The domain indexing the matrix '"+variableName+
														"' is not supported yet:"+indexDomain[0].toString());
								
							MinionBoolVariable[] booleanVector = new MinionBoolVariable[indexLength];
							for(int j=0; j< indexLength; j++) 
								booleanVector[j] = new MinionBoolVariable(1, variableName);
							
							myMinionModel.add01VariableVector(booleanVector, offset);
							minionVectors.put(variableName,(MinionIdentifier[]) booleanVector);
							
							break;
							
					}
					
					else if(rangeDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
						int indexLength = 0;
						int offset = 0;
						
						RangeAtom[] rangeBounds = rangeDomain.getIntegerDomain().getRangeList();
						int ub = rangeBounds[0].getUpperBound().getAtomicExpression().getNumber();
						int lb = rangeBounds[0].getLowerBound().getAtomicExpression().getNumber();
						
						print_debug("now creating minionVariables for the vector with int range of rangeDomain.");
						
						if(indexDomain[0].getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
							/** MATRIX id INDEXED BY bool OF int */
							indexLength = 2;
						}								
						else if(indexDomain[0].getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
							/** MATRIX id INDEXED BY int OF int */
							RangeAtom[] bounds = indexDomain[0].getIntegerDomain().getRangeList();
							offset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
							indexLength = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
								          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
						}
						else throw new MinionException
						("Cannot translate domain type '"+indexDomain[0].toString()+"', only integer and boolean domains.");
						
						if(ub == 1 && lb == 0) {
							MinionBoolVariable[] boolDomainVector = new MinionBoolVariable[indexLength];
							for(int j=0; j< indexLength; j++) {
								boolDomainVector[j] = new MinionBoolVariable(1, variableName);
							}
							myMinionModel.add01VariableVector(boolDomainVector, offset);
							minionVectors.put(variableName, (MinionIdentifier[]) boolDomainVector);
						}
						else {
							
							if(!useDiscreteVariables) {						
								print_debug("will add the minion int domain variables now, and indexLength is "+indexLength);
								MinionBoundsVariable[] intDomainVector = new MinionBoundsVariable[indexLength];
								for(int j=0; j< indexLength; j++) {
									intDomainVector[j] = new MinionBoundsVariable(lb,ub, variableName);
								}
								myMinionModel.addBoundsVariableVector(intDomainVector, offset);
								minionVectors.put(variableName, (MinionIdentifier[]) intDomainVector);
							}
							else {
								print_debug("will add the minion int domain variables now, and indexLength is "+indexLength);
								MinionDiscreteVariable[] intDomainVector = new MinionDiscreteVariable[indexLength];
								for(int j=0; j< indexLength; j++) {
									intDomainVector[j] = new MinionDiscreteVariable(lb,ub, variableName);
								}
								myMinionModel.addDiscreteVariableVector(intDomainVector, offset);
								minionVectors.put(variableName, (MinionIdentifier[]) intDomainVector);
								
							}
						}
						
						break;
					}
					else	
						throw new MinionException
						("Domain '"+domain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
			case 2: // 2-dimensional
				Domain vectorDomain = indexDomain[0];
				Domain elementDomain = indexDomain[1];
				
				int noVectors = 0;
				int noElements = 0;
				int vectorOffset = 0;
				int elementOffset = 0;
				
				//1.  first do the vector-domain part
				if(vectorDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noVectors = 2;
				
				else if(vectorDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = vectorDomain.getIntegerDomain().getRangeList();
					vectorOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noVectors = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+vectorDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				// 2. then the element-domain part
				if(elementDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ vectorDomain, bool ] OF domain */
					noElements = 2;
				
				else if(vectorDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ vectorDomain, int ] OF domain */
					RangeAtom[] bounds = elementDomain.getIntegerDomain().getRangeList();
					elementOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noElements = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+elementDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
								
				
				// 3. Insert identifiers into matrix and add them to the MinionModel
				if(rangeDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
					MinionBoolVariable[][] matrix = new MinionBoolVariable[noVectors][noElements];
					for(int vector_i=0; vector_i<noVectors; vector_i++) {
						for(int j=0; j<noElements; j++) {
							matrix[vector_i][j] = new MinionBoolVariable(1,variableName);
						}
					}
					myMinionModel.add01VariableMatrix(matrix, vectorOffset, elementOffset);
					minionMatrices.put(variableName, matrix);
				}
				else if(rangeDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					
					
					RangeAtom[] rangeBounds = rangeDomain.getIntegerDomain().getRangeList();
					int ub = rangeBounds[0].getUpperBound().getAtomicExpression().getNumber();
					int lb = rangeBounds[0].getLowerBound().getAtomicExpression().getNumber();
					
					if(!useDiscreteVariables) {		
						MinionBoundsVariable[][] matrix = new MinionBoundsVariable[noVectors][noElements];
						for(int vector_i=0; vector_i<noVectors; vector_i++) {
							for(int j=0; j<noElements; j++) {
								matrix[vector_i][j] = new MinionBoundsVariable(lb,ub,variableName);
							}
						}
						myMinionModel.addBoundsVariableMatrix(matrix, vectorOffset, elementOffset);
						minionMatrices.put(variableName, matrix);
					}
					else {
						MinionDiscreteVariable[][] matrix = new MinionDiscreteVariable[noVectors][noElements];
						for(int vector_i=0; vector_i<noVectors; vector_i++) {
							for(int j=0; j<noElements; j++) {
								matrix[vector_i][j] = new MinionDiscreteVariable(lb,ub,variableName);
							}
						}
						myMinionModel.addDiscreteVariableMatrix(matrix, vectorOffset, elementOffset);
						minionMatrices.put(variableName, matrix);									
					}
				}
				else throw new MinionException
				("Domain '"+rangeDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
			
				
				break;
					
				
			case 3: // 3-dimensional
				Domain matrixDomain = indexDomain[0];
				Domain vectDomain = indexDomain[1];
				Domain elemDomain = indexDomain[2];
				
				int noMatrices = 0;
				int noVects = 0;
				int noElems = 0;
				int matrixOffset = 0;
				int vectOffset = 0;
				int elemOffset = 0;
				
				
				// start with the domain of the matrix level (first index)
				if(matrixDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noMatrices = 2;
				
				else if(matrixDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = matrixDomain.getIntegerDomain().getRangeList();
					matrixOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noMatrices = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+matrixDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
				// continue with vector level
				if(vectDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noVects = 2;
				
				else if(vectDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = vectDomain.getIntegerDomain().getRangeList();
					vectOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noVects = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+vectDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
				// at last take care of the element level (3rd index)
				if(elemDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noElems = 2;
				
				else if(elemDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = elemDomain.getIntegerDomain().getRangeList();
					elemOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noElems = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+elemDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
				
				// Insert identifiers into matrix and add them to the MinionModel
				if(rangeDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
					MinionBoolVariable[][][] cube = new MinionBoolVariable[noMatrices][noVects][noElems];
					for(int matrix_i=0; matrix_i<noMatrices; matrix_i++) {
						for(int vector_i=0; vector_i<noVects; vector_i++) {
							for(int j=0; j<noElems; j++) {
								cube[matrix_i][vector_i][j] = new MinionBoolVariable(1,variableName);
							}
						}
					}
					myMinionModel.add01VariableCube(cube, matrixOffset, vectOffset, elemOffset);
					minionCubes.put(variableName, cube);
				}
				else if(rangeDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					
					
					RangeAtom[] rangeBounds = rangeDomain.getIntegerDomain().getRangeList();
					int ub = rangeBounds[0].getUpperBound().getAtomicExpression().getNumber();
					int lb = rangeBounds[0].getLowerBound().getAtomicExpression().getNumber();
					
					if(!useDiscreteVariables) {		
						MinionBoundsVariable[][][] cube = new MinionBoundsVariable[noMatrices][noVects][noElems];
						for(int matrix_i=0; matrix_i<noMatrices; matrix_i++) {
							for(int vector_i=0; vector_i<noVects; vector_i++) {
								for(int j=0; j<noElems; j++) {
									cube[matrix_i][vector_i][j] = new MinionBoundsVariable(lb,ub,variableName);	
								}
							}
						}
						myMinionModel.addBoundsVariableCube(cube, matrixOffset, vectOffset, elemOffset);
						minionCubes.put(variableName, cube);
					}
					else {
						MinionDiscreteVariable[][][] cube = new MinionDiscreteVariable[noMatrices][noVects][noElems];
						for(int matrix_i=0; matrix_i<noMatrices; matrix_i++) {
							for(int vector_i=0; vector_i<noVects; vector_i++) {
								for(int j=0; j<noElems; j++) {
									cube[matrix_i][vector_i][j] = new MinionDiscreteVariable(lb,ub,variableName);
								}
							}
						}
						myMinionModel.addDiscreteVariableCube(cube, matrixOffset, vectOffset, elemOffset);
						minionCubes.put(variableName, cube);									
					}
				}
				else throw new MinionException
				("Domain '"+rangeDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
			
				break;
				
			default:
				throw new MinionException
					("Multi-dimensional matrices (larger than 2) are not supported yet, sorry.");		
				
			}
			
		}
		
		
	}
	
	
	/**
	 * 
	 * @param variableName
	 * @throws TranslationUnsupportedException
	 * @throws MinionException
	 */
	protected void addEmptyMatrix(String variableName) 
		throws TranslationUnsupportedException, MinionException  {
		
		Domain domain = decisionVariables.get(variableName);
		
		switch(domain.getRestrictionMode()) {
		
		case EssenceGlobals.MATRIX_DOMAIN:
			Domain[] indexDomain = domain.getMatrixDomain().getIndexDomains();
			Domain rangeDomain = domain.getMatrixDomain().getRangeDomain();
			
			switch(indexDomain.length) {
			
			/** 1-dimensional */
			case 1: 
					if(rangeDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
						int indexLength = 0;
						int offset = 0;
						
						if(indexDomain[0].getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
							/** MATRIX id INDEXED BY bool OF bool */
							indexLength = 2;
						}	
						else if(indexDomain[0].getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
							/** MATRIX id INDEXED BY int OF bool */
							RangeAtom[] bounds = indexDomain[0].getIntegerDomain().getRangeList();
							offset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
							indexLength = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
								          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;								
						}
						else 
							throw new MinionException("The domain indexing the matrix '"+variableName+
														"' is not supported yet:"+indexDomain[0].toString());
								
						MinionBoolVariable[] booleanVector = new MinionBoolVariable[indexLength];
						// keep them null-pointers
						//for(int j=0; j< indexLength; j++) 
						//	booleanVector[j] = new MinionBoolVariable(true, variableName);
							
						myMinionModel.addEmpty01VariableVector(booleanVector, variableName, offset);
						minionVectors.put(variableName,(MinionIdentifier[]) booleanVector);
							
						break;
							
					}
					
					else if(rangeDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
						int indexLength = 0;
						int offset = 0;
						
						RangeAtom[] rangeBounds = rangeDomain.getIntegerDomain().getRangeList();
						int ub = rangeBounds[0].getUpperBound().getAtomicExpression().getNumber();
						int lb = rangeBounds[0].getLowerBound().getAtomicExpression().getNumber();
						
						print_debug("now creating minionVariables for the vector with int range of rangeDomain.");
						
						if(indexDomain[0].getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
							/** MATRIX id INDEXED BY bool OF int */
							indexLength = 2;
						}								
						else if(indexDomain[0].getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
							/** MATRIX id INDEXED BY int OF int */
							RangeAtom[] bounds = indexDomain[0].getIntegerDomain().getRangeList();
							offset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
							indexLength = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
								          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
						}
						else throw new MinionException
						("Cannot translate domain type '"+indexDomain[0].toString()+"', only integer and boolean domains.");
						
						if(ub == 1 && lb == 0) {
							MinionBoolVariable[] boolDomainVector = new MinionBoolVariable[indexLength];
							
							myMinionModel.addEmpty01VariableVector(boolDomainVector, variableName, offset);
							minionVectors.put(variableName, (MinionIdentifier[]) boolDomainVector);
						}
						else {
							
							if(!useDiscreteVariables) {						
								print_debug("will add the minion int domain variables now, and indexLength is "+indexLength);
								MinionBoundsVariable[] intDomainVector = new MinionBoundsVariable[indexLength];
								
								myMinionModel.addEmptyBoundsVariableVector(intDomainVector, variableName, offset);
								minionVectors.put(variableName, (MinionIdentifier[]) intDomainVector);
							}
							else {
								print_debug("will add the minion int domain variables now, and indexLength is "+indexLength);
								MinionDiscreteVariable[] intDomainVector = new MinionDiscreteVariable[indexLength];
								
								myMinionModel.addEmptyDiscreteVariableVector(intDomainVector, variableName, offset);
								minionVectors.put(variableName, (MinionIdentifier[]) intDomainVector);
								
							}
						}
						
						break;
					}
					else	
						throw new MinionException
						("Domain '"+domain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
			case 2: // 2-dimensional
				Domain vectorDomain = indexDomain[0];
				Domain elementDomain = indexDomain[1];
				
				int noVectors = 0;
				int noElements = 0;
				int vectorOffset = 0;
				int elementOffset = 0;
				
				//1.  first do the vector-domain part
				if(vectorDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noVectors = 2;
				
				else if(vectorDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = vectorDomain.getIntegerDomain().getRangeList();
					vectorOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noVectors = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+vectorDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				// 2. then the element-domain part
				if(elementDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ vectorDomain, bool ] OF domain */
					noElements = 2;
				
				else if(vectorDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ vectorDomain, int ] OF domain */
					RangeAtom[] bounds = elementDomain.getIntegerDomain().getRangeList();
					elementOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noElements = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+elementDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
								
				
				// 3. Insert identifiers into matrix and add them to the MinionModel
				if(rangeDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
					MinionBoolVariable[][] matrix = new MinionBoolVariable[noVectors][noElements];
					myMinionModel.addEmpty01VariableMatrix(matrix, variableName, vectorOffset, elementOffset);
					minionMatrices.put(variableName, matrix);
				}
				else if(rangeDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
		
					if(!useDiscreteVariables) {		
						MinionBoundsVariable[][] matrix = new MinionBoundsVariable[noVectors][noElements];
						myMinionModel.addEmptyBoundsVariableMatrix(matrix, variableName, vectorOffset, elementOffset);
						minionMatrices.put(variableName, matrix);
					}
					else {
						MinionDiscreteVariable[][] matrix = new MinionDiscreteVariable[noVectors][noElements];
						myMinionModel.addEmptyDiscreteVariableMatrix(matrix, variableName, vectorOffset, elementOffset);
						minionMatrices.put(variableName, matrix);									
					}
				}
				else throw new MinionException
				("Domain '"+rangeDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
			
				
				break;
					
				
			case 3: // 3-dimensional
				Domain matrixDomain = indexDomain[0];
				Domain vectDomain = indexDomain[1];
				Domain elemDomain = indexDomain[2];
				
				int noMatrices = 0;
				int noVects = 0;
				int noElems = 0;
				int matrixOffset = 0;
				int vectOffset = 0;
				int elemOffset = 0;
				
				
				// start with the domain of the matrix level (first index)
				if(matrixDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noMatrices = 2;
				
				else if(matrixDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = matrixDomain.getIntegerDomain().getRangeList();
					matrixOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noMatrices = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+matrixDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
				// continue with vector level
				if(vectDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noVects = 2;
				
				else if(vectDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = vectDomain.getIntegerDomain().getRangeList();
					vectOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noVects = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+vectDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
				// at last take care of the element level (3rd index)
				if(elemDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN)
					/** MATRIX id INDEXED BY [ bool, elemDomain ] OF domain */
					noElems = 2;
				
				else if(elemDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
					/** MATRIX id INDEXED BY [ int, elemDomain ] OF domain */
					RangeAtom[] bounds = elemDomain.getIntegerDomain().getRangeList();
					elemOffset = bounds[0].getLowerBound().getAtomicExpression().getNumber();
					noElems = (bounds[0].getUpperBound().getAtomicExpression().getNumber() - 
						          bounds[0].getLowerBound().getAtomicExpression().getNumber()) + 1;	
					
				}
				else throw new MinionException
				("Domain '"+elemDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
				
				
				
				// Insert identifiers into matrix and add them to the MinionModel
				if(rangeDomain.getRestrictionMode() == EssenceGlobals.BOOLEAN_DOMAIN) {
					MinionBoolVariable[][][] cube = new MinionBoolVariable[noMatrices][noVects][noElems];
					myMinionModel.addEmpty01VariableCube(cube, variableName, matrixOffset, vectOffset, elemOffset);
					minionCubes.put(variableName, cube);
				}
				else if(rangeDomain.getRestrictionMode() == EssenceGlobals.INTEGER_RANGE) {
				
					if(!useDiscreteVariables) {		
						MinionBoundsVariable[][][] cube = new MinionBoundsVariable[noMatrices][noVects][noElems];
						myMinionModel.addEmptyBoundsVariableCube(cube, variableName, matrixOffset, vectOffset, elemOffset);
						minionCubes.put(variableName, cube);
					}
					else {
						MinionDiscreteVariable[][][] cube = new MinionDiscreteVariable[noMatrices][noVects][noElems];
						myMinionModel.addEmptyDiscreteVariableCube(cube, variableName, matrixOffset, vectOffset, elemOffset);
						minionCubes.put(variableName, cube);									
					}
				}
				else throw new MinionException
				("Domain '"+rangeDomain.toString()+"' of variable '"+variableName+"' is not supported yet, sorry.");
			
				break;
				
			default:
				throw new MinionException
					("Multi-dimensional matrices (larger than 2) are not supported yet, sorry.");		
				
			}
			
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
	    		System.out.println("[ DEBUG minionVariableGenerator ] "+s);
	    }  


	    protected static void print_message(String s) {
	    	if(PRINT_MESSAGE)
	    		System.out.println("[ MESSAGE ] "+s);
	    }  



	
	
    
}
