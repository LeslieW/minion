package 	translator.normaliser;

import java.util.HashMap;

public class Parameters {

    private HashMap<String, int[]> parameterVectors;
    /** contains all 2 dimensional arrays of constants/parameters */
    private HashMap<String, int[][]> parameterMatrices;
    /** contains all 3 dimensional arrays of constants/parameters */
    private HashMap<String, int[][][]> parameterCubes;
	
    private HashMap<String, int[]> parameterArrayOffsets;
    
    
    public Parameters(HashMap<String,int[]> parameterVectors,
    		HashMap<String,int[][]> parameterMatrices,
    		HashMap<String,int[][][]> parameterCubes,
    		HashMap<String,int[]> parameterOffsets) {
    	
    	
    	this.parameterVectors = parameterVectors;
    	this.parameterMatrices = parameterMatrices;
    	this.parameterCubes = parameterCubes;
    	this.parameterArrayOffsets = parameterOffsets;
    	
    }
    
    
    public boolean isParameter(String arrayName) {     		
    	return this.parameterVectors.containsKey(arrayName) || 
    	       this.parameterMatrices.containsKey(arrayName) || 
    	       this.parameterCubes.containsKey(arrayName);
    }

    // all of these methods might return null!!
    
 /*   public int[] getParameterVector(String vectorName) 
   	throws NormaliserException {
    	if(parameterVectors ==null)
    		return null;

    	else return parameterVectors.get(vectorName);    	
    }*/
 	
    public boolean isParameterVector(String vectorName) {
    	if(parameterVectors!=null)
    		return parameterVectors.containsKey(vectorName);
    	else return false;
    }
    
    /*public int[][] getParameterMatrix(String matrixName) 
	throws NormaliserException {
    	if(parameterVectors ==null)
    		return null;
	else return parameterMatrices.get(matrixName);    	
    }*/
	
    public boolean isParameterMatrix(String matrixName) {
    	if(parameterVectors!=null)
    		return parameterMatrices.containsKey(matrixName);
    	else return false;
    }

 /*   public int[][][] getParameterCube(String cubeName) 
	throws NormaliserException {
    	if(parameterVectors ==null)
    		return null;
	else return parameterCubes.get(cubeName);    	
    }
	*/
    public boolean isParameterCube(String cubeName) {
    	if(parameterVectors!=null)
    		return parameterCubes.containsKey(cubeName);
    	else return false;
    }

    public int[] getArrayOffset(String arrayName) {
    	return this.parameterArrayOffsets.get(arrayName);
    	
    }
    
    public int getVectorElementAt(String vectorName, int index) 
    	throws NormaliserException {
    	
    	if(!this.parameterVectors.containsKey(vectorName)) 
    		throw new NormaliserException("Trying to access element of unknown vector:"+vectorName);
    	
    	int[] parameterVector = this.parameterVectors.get(vectorName);
    	int[] offsets = this.parameterArrayOffsets.get(vectorName);
    	index = index - offsets[0];
    	
    	if(index < 0 || index >= parameterVector.length)
    		throw new NormaliserException("Index out of bounds of parameter array:"+vectorName+"["+(index+offsets[0])+"].");
    	
    	return parameterVector[index];	
    }
    
    public int getMatrixElementAt(String matrixName, int rowIndex, int colIndex) 
		throws NormaliserException {
	
    	if(!this.parameterMatrices.containsKey(matrixName)) 
    		throw new NormaliserException("Trying to access element of unknown matrix:"+matrixName);
	
    	int[][] parameterMatrix = this.parameterMatrices.get(matrixName);
    	int[] offsets = this.parameterArrayOffsets.get(matrixName);
    	rowIndex = rowIndex - offsets[0];
    	colIndex = colIndex - offsets[1];
    	
    	if(rowIndex < 0 || rowIndex >= parameterMatrix.length || 
    			colIndex < 0 || colIndex >= parameterMatrix[0].length)
    		throw new NormaliserException("Index out of bounds of parameter array:"+matrixName+"["
    				+(rowIndex+offsets[0])+","+(colIndex+offsets[1])+ "].");
	
    	return parameterMatrix[rowIndex][colIndex];	
    }
    
    public int getCubeElementAt(String cubeName, int planeIndex, int rowIndex, int colIndex) 
		throws NormaliserException {
    	
    	if(!this.parameterCubes.containsKey(cubeName)) 
    		throw new NormaliserException("Trying to access element of unknown cube:"+cubeName);

    	int[][][] parameterCube = this.parameterCubes.get(cubeName);
    	int[] offsets = this.parameterArrayOffsets.get(cubeName);
    	colIndex = colIndex - offsets[0];
    	rowIndex = rowIndex - offsets[1];
    	colIndex = colIndex - offsets[2];
	
    	if(planeIndex < 0 || planeIndex >= parameterCube.length ||
    			rowIndex < 0 || rowIndex >= parameterCube[0].length || 
    				colIndex < 0 || colIndex >= parameterCube[0][0].length)
    		throw new NormaliserException("Index out of bounds of parameter array:"+cubeName+"["
    				+(planeIndex+offsets[0])+","+(rowIndex+offsets[1])+","+(colIndex+offsets[2])+ "].");

    	return parameterCube[planeIndex][rowIndex][colIndex];	
    }

}
