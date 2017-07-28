package reinforcechess;

import java.util.ArrayList;

public class mxjava {

	public static void main(String[] args) {
		ArrayList<double[]> database = new ArrayList<>();
		double[] one = {0,4,6,7,6,5,1,5,4,5,1,6,4,5,2,67};
		database.add(one);
		database.add(one);
		double[] two = {0,3,4,5,8,9,0,2,5,1,2,3,4};
		database.add(two);
		
		double[] states = {0,3,4,5,8,9,0,2,5,1,2,3,4};
		
		System.out.println(stateInDatabase(database,states));
		System.out.println(whereInDatabase(database,states));
		
		System.out.println(1/0.0);
		
	}
	
	public static double[] UCT1Array(double[] w, double[] n, double t, double constant, double scale, boolean infinity) {
		double[] newArray = new double[w.length];
		for (int i = 0;i<newArray.length;i++) {
			newArray[i] = UCT1(w[i],n[i],t,constant,scale,infinity);
		}
		return newArray;
	}
	
	public static double UCT1(double w, double n, double t, double constant, double scale, boolean infinity) {
		if (n!=0) {
			double winRatio = w/n;
			double underSquareRoot = Math.log(t)/n;
			double secondPart = constant * (Math.sqrt(underSquareRoot));
			return scale*(winRatio+secondPart);
		}
		else {
			if (infinity) return 1;
			else return 0;
		}
		
	}
	
	
	public static boolean stateInDatabase(ArrayList<double[]> allStates, double[] states) {
		for (int i = 0; i<allStates.size();i++) {
			if (sameState(allStates.get(i),states)) return true;
		}
		return false;
	}
	
	public static int whereInDatabase(ArrayList<double[]> allStates, double[] states) {
		for (int i = 0; i<allStates.size();i++) {
			if (sameState(allStates.get(i),states)) return i;
		}
		return -1;
	}
	
	public static boolean sameState(double[] one, double[] two) {
		if (one.length!=two.length) return false;
		for (int i = 0;i<Math.min(one.length,two.length);i++) {
			if (one[i]!=two[i]) { 
				return false;
			}
		}
		return true;
	}
	
	//DUPLICATE MOVES BY SAME PIECE
	public static boolean specifyInitial(String[] moves, String[][] chessBoard, String place) {
		for (int i = 0;i<moves.length;i++) {
			for (int j = 0;j<moves.length;j++) {
				if (!moves[i].equals("O-O") && !moves[j].equals("O-O") && !moves[i].equals("O-O-O") && !moves[j].equals("O-O-O")){
					if (i!=j && moves[i].substring(2,4).equals(moves[j].substring(2,4)) && !moves[i].substring(0, 2).equals(moves[j].substring(0,2))) {
						String firstMove = Main.compReadableMove(moves[i]);
						String secondMove = Main.compReadableMove(moves[j]);

						String pieces = chessBoard[8-(Character.getNumericValue(firstMove.charAt(1)))][Character.getNumericValue(firstMove.charAt(0))-1].toLowerCase() + chessBoard[8-(Character.getNumericValue(secondMove.charAt(1)))][Character.getNumericValue(secondMove.charAt(0))-1].toLowerCase();

						if (pieces.indexOf("p") == -1 && pieces.indexOf("k") == -1  && pieces.indexOf("b") == -1) {
							if (pieces.equals("rr") || pieces.equals("nn")) return true;
						}
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
	
	public static double[] addVectors(double[] first, double[] second) {
		double[] result = new double[first.length];
		for (int i = 0;i<result.length;i++) {
			result[i]=first[i]+second[i];
		}
		return result;
	}
	
	public static double[] scale(double[] array, double scale) {
		double[] result = new double[array.length];
		for (int i=0;i<array.length;i++) {
			result[i]=array[i]*scale;
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
