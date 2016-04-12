package net.lifove.research.utils;

public class MatrixUtil {

	public static double[][] matrixConcatLR(double[][] LMatrix,double[][] RMatrix){
		if(LMatrix.length!=RMatrix.length){
			System.out.println("matrixConcatLR error: the number of rows in two matrix should be same");
			System.exit(0);
		}
		
		int numRows = LMatrix.length;
		int numColsForLMatrix = LMatrix[0].length;
		int numColsForRMatrix = RMatrix[0].length;
			
		double[][] newMatrix = new double[numRows][numColsForLMatrix+numColsForRMatrix];
		
		for(int row = 0; row <numRows;row++){
			for(int col = 0; col<numColsForLMatrix;col++){
				newMatrix[row][col]=LMatrix[row][col];
			}
			for(int col = numColsForLMatrix; col<numColsForLMatrix+numColsForRMatrix;col++){
				newMatrix[row][col]=RMatrix[row][col-numColsForLMatrix];
			}
		}
		
		return newMatrix;
	}
	
	public static double[][] matrixConcatUL(double[][] UMatrix, double[][] LMatrix){
		if(LMatrix[0].length!=UMatrix[0].length){
			System.out.println("matrixConcatUL error: the number of columns in two matrix should be same");
			System.exit(0);
		}
		
		int numURows = UMatrix.length;
		int numLRows = LMatrix.length;
		int numCols = UMatrix[0].length;
		
		double[][] newMatrix = new double[numURows+numLRows][numCols];
		
		System.arraycopy(UMatrix, 0, newMatrix, 0, numURows);
		System.arraycopy(LMatrix, 0, newMatrix, numURows, numLRows);
		
		return newMatrix;
	}

}
