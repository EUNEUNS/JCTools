package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.MatrixUtil;

import org.junit.Test;

public class MatrixUtilsTest {

	@Test
	public void testMatrixConcatLR() {
		double[][] left = {{1,2},
				{3,4}};
		double[][] right = {{5,6},
				{7,8}};

		double[][] newMatrix = MatrixUtil.matrixConcatLR(left, right);

		assertEquals("LR concat 0,0 is not correct",newMatrix[0][0],1,0);
		assertEquals("LR concat 0,1 is not correct",newMatrix[0][1],2,0);
		assertEquals("LR concat 0,2 is not correct",newMatrix[0][2],5,0);
		assertEquals("LR concat 0,3 is not correct",newMatrix[0][3],6,0);
		assertEquals("LR concat 1,0 is not correct",newMatrix[1][0],3,0);
		assertEquals("LR concat 1,1 is not correct",newMatrix[1][1],4,0);
		assertEquals("LR concat 1,2 is not correct",newMatrix[1][2],7,0);
		assertEquals("LR concat 1,3 is not correct",newMatrix[1][3],8,0);
	}

	@Test
	public void testMatrixConcatUL() {
		double[][] upper = {{1,2},
				{3,4}};
		double[][] lower = {{5,6},
				{7,8}};

		double[][] newMatrix = MatrixUtil.matrixConcatUL(upper, lower);
		
		assertEquals("LR concat 0,0 is not correct",newMatrix[0][0],1,0);
		assertEquals("LR concat 0,1 is not correct",newMatrix[0][1],2,0);
		assertEquals("LR concat 1,0 is not correct",newMatrix[1][0],3,0);
		assertEquals("LR concat 1,1 is not correct",newMatrix[1][1],4,0);
		assertEquals("LR concat 2,0 is not correct",newMatrix[2][0],5,0);
		assertEquals("LR concat 2,1 is not correct",newMatrix[2][1],6,0);
		assertEquals("LR concat 3,0 is not correct",newMatrix[3][0],7,0);
		assertEquals("LR concat 3,1 is not correct",newMatrix[3][1],8,0);
	}
}
