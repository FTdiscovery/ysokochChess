package reinforcechess;

public class mxjava {

	//DUPLICATE MOVES BY SAME PIECE
	public static boolean specifyInitial(String[] moves, String[][] chessBoard, String place) {
		for (int i = 0;i<moves.length;i++) {
			for (int j = 0;j<moves.length;j++) {
				if (i!=j && moves[i].substring(2,4).equals(moves[j].substring(2,4))) {
					String firstMove = Main.compReadableMove(moves[i]);
					String secondMove = Main.compReadableMove(moves[j]);
					
					String pieces = chessBoard[8-(Character.getNumericValue(firstMove.charAt(1)))][Character.getNumericValue(firstMove.charAt(0))-1].toLowerCase() + chessBoard[8-(Character.getNumericValue(secondMove.charAt(1)))][Character.getNumericValue(secondMove.charAt(0))-1].toLowerCase();
					
					if (pieces.indexOf("p") == -1 && pieces.indexOf("k") == -1  && pieces.indexOf("b") == -1) {
						if (pieces.equals("rr") || pieces.equals("nn")) return true;
					}
				}
			}
		}
		return false;
	}

	//FOR MAKING MOVES
	public static int numberDirectory(double[] values, String[] moves) {
		int max = 0;
		for (int i = 1;i<moves.length;i++) {
			if (values[i]>values[max]) { max = i; }
		}
		return max;
	}

	public static String computerMove(double[] values, String[] moves) {
		return moves[numberDirectory(values,moves)];
	}


	public static double[] computerActionArray(double[] values, String[] moves) {
		numberDirectory(values,moves);
		double[] actionArray = new double[values.length];
		actionArray[numberDirectory(values,moves)] =1;
		return actionArray;
	}



	//FOR NEURAL NETWORK MATHEMATICS
	public static double sigmoidPackage(double x, boolean deriv) {
		if (deriv) {
			return x*(1-x);
		}
		return 1/(1+Math.pow(Math.E,-x));
	}

	public static double[][] synapseLayer(int inputs, int outputs) {
		double[][] LAYER = new double[inputs][outputs];
		for (int i = 0;i<inputs;i++) {
			for (int j = 0;j<outputs;j++) {
				LAYER[i][j] = (Math.random()*2)-1;
			}
		}
		return LAYER;
	}

	public static double[][] matrixMult(double[][] firstMatrix, double[][] secondMatrix) {
		double[][] newMatrix = new double[firstMatrix.length][secondMatrix[0].length];
		for (int i = 0; i < newMatrix.length; i++) { 
			for (int j = 0; j < newMatrix[0].length; j++) { 
				for (int k = 0; k < firstMatrix[0].length; k++) { 
					newMatrix[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
				}
			}
		}
		return newMatrix;
	}
	public static double[][] scalarMult(double[][]firstMatrix,double[][]secondMatrix) {
		double[][] newMatrix = new double[firstMatrix.length][secondMatrix[0].length];
		for (int i = 0; i < newMatrix.length; i++) { 
			for (int j = 0; j < newMatrix[0].length; j++) { 
				newMatrix[i][j] = firstMatrix[i][j]*secondMatrix[i][j];
			}
		}
		return newMatrix;
	}
	public static double[][] subtract(double[][] firstMatrix, double[][] secondMatrix) {
		double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
		for (int i = 0; i < firstMatrix.length; i++) {
			for (int j = 0; j < firstMatrix[0].length; j++) {
				result[i][j] = firstMatrix[i][j] - secondMatrix[i][j];
			}
		}
		return result;
	}
	public static double[][] add(double[][] firstMatrix, double[][] secondMatrix) {
		double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
		for (int i = 0; i < firstMatrix.length; i++) {
			for (int j = 0; j < firstMatrix[0].length; j++) {
				result[i][j] = firstMatrix[i][j] + secondMatrix[i][j];
			}
		}
		return result;
	}

	public static double[][] transpose(double [][] m){
		double[][] temp = new double[m[0].length][m.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				temp[j][i] = m[i][j];
		return temp;
	}

	public static void print(String x, double[][]m) {
		System.out.println(x);
		for (int i = 0;i<m.length;i++) {
			for (int j = 0;j<m[0].length;j++) {
				System.out.println(m[i][j]);
			}
			System.out.println("-----row done-----");
		}		
		System.out.println("\n");
	}

}
