package reinforcechess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	static ArrayList<double[]> WHITE_STATES = new ArrayList<>(); 
	static ArrayList<Double> WHITE_STATES_APPEARANCES = new ArrayList<>(); //t
	static ArrayList<double[]> WHITE_CHOSEN_ACTION_COUNT = new ArrayList<>(); //n
	static ArrayList<double[]> WHITE_WINS_FOR_ACTION = new ArrayList<>(); //w

	static ArrayList<double[]> BLACK_STATES = new ArrayList<>();
	static ArrayList<Double> BLACK_STATES_APPEARANCES = new ArrayList<>(); //t
	static ArrayList<double[]> BLACK_CHOSEN_ACTION_COUNT = new ArrayList<>(); //n
	static ArrayList<double[]> BLACK_WINS_FOR_ACTION = new ArrayList<>(); //w

	static ArrayList<double[]> WHITE_UCT1 = new ArrayList<>();
	static ArrayList<double[]> BLACK_UCT1 = new ArrayList<>();

	static ArrayList<String[]> library = new ArrayList<>();

	static int totalLegalMovesPerPosition = 80;

	static String PGN_GAME_LOG = "[White: Random Chess AI]\n[Black: Random Chess AI]\n\n";

	static String[] colNames = "abcdefgh".split("");
	static String[][] chessBoard = {
			//A   B   C   D   E   F   G   H
			{"r","n","b","q","k","b","n","r"},  //8
			{"p","p","p","p","p","p","p","p"},  //7
			{" "," "," "," "," "," "," "," "},  //6
			{" "," "," "," "," "," "," "," "},  //5
			{" "," "," "," "," "," "," "," "},  //4
			{" "," "," "," "," "," "," "," "},  //3
			{"P","P","P","P","P","P","P","P"},  //2
			{"R","N","B","Q","K","B","N","R"}};	//1


	//First 8 are for white pawns, first 8 are for black pawns
	static boolean[] pawnDoubleMove = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
	static int[] timePawnMoved = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int totalMoves = 0;
	static int whiteKingMoved = 0;
	static int blackKingMoved = 0;
	static int kwRookMoved = 0;
	static int qwRookMoved = 0;
	static int kbRookMoved = 0;
	static int qbRookMoved = 0;

	static String pureLog = "";
	static boolean debugOn = false;

	/* NOW THAT THE COMPUTER CAN PLAY GAMES WITH ITSELF, IT NEEDS TO FIND CERTAIN BOOKS TO PRETRAIN
	 * 
	 * We take advantage of the files that exist on the computer, and then train it before the computer tries reinforcement learning.
	 * :)
	 */
	static String[] books = {"polgar"};
	static String documents = System.getProperty ("user.home") + "/Documents/Procrastination Box/Chess Books/";


	public static void main(String[] args) throws IOException {

		double[][] normalState = new double[1][384]; //fed input. blank at the moment in order for initialization of the neural network
		double[][] moveOutput = new double[1][totalLegalMovesPerPosition]; // fed output. blank of the moment in order for initialization of the neural network
		int neuronsPerHiddenLayer = 450;
		double learningRate = 0.01;
		
		TwoLayerNeuralNetwork wBrain = new TwoLayerNeuralNetwork(normalState,moveOutput,neuronsPerHiddenLayer,learningRate);
		TwoLayerNeuralNetwork bBrain = new TwoLayerNeuralNetwork(normalState,moveOutput,neuronsPerHiddenLayer,learningRate);

		
		//PRE REINFORCEMENT TRAINING
		for (int i = 0;i<books.length;i++) {
			FileReader book = new FileReader (documents + books[i]+".txt");
			BufferedReader buffer = new BufferedReader(book);
			//Read the files...
			String word;
			ArrayList<String> game = new ArrayList<>();
			while ((word = buffer.readLine()) != null) {
				game.add(word);
			}
			String [] addToLibrary = game.toArray(new String[game.size()]);
			library.add(addToLibrary);
		}
		if (debugOn) {
			for (int i = 0;i<library.size();i++) {
				System.out.println(Arrays.toString(library.get(i)));
			}
		}

		//play the games
		for (int i = 0;i<library.size();i++) {
			ArrayList<Integer> whiteMoves = new ArrayList<>();
			ArrayList<Integer> blackMoves = new ArrayList<>();
			ArrayList<double[]> whiteUpdateIfWin = new ArrayList<>();
			ArrayList<double[]> blackUpdateIfWin = new ArrayList<>();
			double[] blank = new double[totalLegalMovesPerPosition];


			for(int j = 0;j<library.get(i).length;j++) {
				String move = library.get(i)[j];
				//Save everything into the moves.
				if (totalMoves%2==0) {
					if (mxjava.stateInDatabase(WHITE_STATES, convertToState(chessBoard))) {
						double[] action;
						if (move.length()>=4) {
							action = mxjava.action(legalWMoves(), move, totalLegalMovesPerPosition);
						}
						else {
							action = mxjava.castleAction(legalWMoves(), move, totalLegalMovesPerPosition);
						}
						int directory = mxjava.whereInDatabase(WHITE_STATES,convertToState(chessBoard));
						WHITE_STATES_APPEARANCES.set(directory, WHITE_STATES_APPEARANCES.get(directory)+1);
						WHITE_CHOSEN_ACTION_COUNT.set(directory, mxjava.addVectors(WHITE_CHOSEN_ACTION_COUNT.get(directory), action));
						whiteMoves.add(directory);	
						if (!debugOn) System.out.println(Arrays.toString(action));
						whiteUpdateIfWin.add(action);
					}
					else {
						double[] action;
						if (move.length()>=4) {
							action = mxjava.action(legalWMoves(), move, totalLegalMovesPerPosition);
						}
						else {
							action = mxjava.castleAction(legalWMoves(), move, totalLegalMovesPerPosition);
						}
						whiteMoves.add(WHITE_STATES.size());
						WHITE_STATES.add(convertToState(chessBoard));
						WHITE_STATES_APPEARANCES.add((double) 1);
						WHITE_CHOSEN_ACTION_COUNT.add(action);
						WHITE_WINS_FOR_ACTION.add(blank);
						if (!debugOn) System.out.println(Arrays.toString(action));
						whiteUpdateIfWin.add(action);
					}

				}
				else {
					if (mxjava.stateInDatabase(BLACK_STATES, convertToState(chessBoard))) {
						double[] action;

						if (move.length()>=4) {
							action = mxjava.action(legalBMoves(), move, totalLegalMovesPerPosition);
						}
						else {
							action = mxjava.castleAction(legalBMoves(), move, totalLegalMovesPerPosition);
						}
						int directory = mxjava.whereInDatabase(BLACK_STATES,convertToState(chessBoard));
						BLACK_STATES_APPEARANCES.set(directory, BLACK_STATES_APPEARANCES.get(directory)+1);
						BLACK_CHOSEN_ACTION_COUNT.set(directory, mxjava.addVectors(BLACK_CHOSEN_ACTION_COUNT.get(directory), action));
						blackMoves.add(directory);
						if (!debugOn) System.out.println(Arrays.toString(action));
						blackUpdateIfWin.add(action);
					}
					else {
						double[] action;
						if (move.length()>=4) {
							action = mxjava.action(legalBMoves(), move, totalLegalMovesPerPosition);
						}
						else {
							action = mxjava.castleAction(legalBMoves(), move, totalLegalMovesPerPosition);
						}
						blackMoves.add(BLACK_STATES.size());
						BLACK_STATES.add(convertToState(chessBoard));
						BLACK_STATES_APPEARANCES.add((double)1);
						BLACK_CHOSEN_ACTION_COUNT.add(action);
						BLACK_WINS_FOR_ACTION.add(blank);
						if (!debugOn) System.out.println(Arrays.toString(action));
						blackUpdateIfWin.add(action);
					}
				}
				//Make the Move
				if (debugOn) { System.out.println(Arrays.toString(legalWMoves())); System.out.println(Arrays.toString(legalBMoves())); }
				makeMove(move);
				printBoard(chessBoard);
			}
			
			//Game is finished.
			while (WHITE_STATES.size()>WHITE_CHOSEN_ACTION_COUNT.size()) {
				WHITE_STATES.remove(WHITE_STATES.size()-1);
			}
			while (BLACK_STATES.size()>BLACK_CHOSEN_ACTION_COUNT.size()) {
				BLACK_STATES.remove(BLACK_STATES.size()-1);
			}
			
			//UPDATE THE WINS
			for (int k = 0;k<whiteMoves.size();k++) {
				if(gameStatus()==1) {
					int directory = whiteMoves.get(k);
					WHITE_WINS_FOR_ACTION.set(whiteMoves.get(k), mxjava.addVectors(WHITE_WINS_FOR_ACTION.get(directory),whiteUpdateIfWin.get(k)));
				}
				else if (gameStatus()==0 || gameStatus() ==5) {
					int directory = whiteMoves.get(k);
					WHITE_WINS_FOR_ACTION.set(directory,mxjava.addVectors(WHITE_WINS_FOR_ACTION.get(directory),mxjava.scale(whiteUpdateIfWin.get(k), 0.5)));
				}
			}
			for (int k = 0;k<blackMoves.size()-1;k++) {
				if(gameStatus()==-1) {
					int directory = blackMoves.get(k);
					BLACK_WINS_FOR_ACTION.set(blackMoves.get(k), mxjava.addVectors(BLACK_WINS_FOR_ACTION.get(directory),blackUpdateIfWin.get(k)));
				}
				else if (gameStatus()==0 || gameStatus() ==5) {
					int directory = blackMoves.get(k);
					BLACK_WINS_FOR_ACTION.set(blackMoves.get(k), mxjava.addVectors(BLACK_WINS_FOR_ACTION.get(directory),mxjava.scale(blackUpdateIfWin.get(k),0.5)));

				}
			}
			whiteMoves.clear();
			blackMoves.clear();
			whiteUpdateIfWin.clear();
			blackUpdateIfWin.clear();

			//TRAIN THE DATA ONCE IT'S DONE.
			if (!debugOn) {
				System.out.println("\nAPPEARANCES\n---------");
				print2DArrayList(WHITE_CHOSEN_ACTION_COUNT,5);
				System.out.println("\nWINS PER ACTION\n---------");
				print2DArrayList(WHITE_WINS_FOR_ACTION,5);
			}

			WHITE_UCT1.clear();
			BLACK_UCT1.clear();
			//CREATE UCT1 FOR EACH STATE.

			for (int k = 0;k<WHITE_STATES.size();k++) {
				WHITE_UCT1.add(mxjava.UCT1Array(WHITE_WINS_FOR_ACTION.get(k), WHITE_CHOSEN_ACTION_COUNT.get(k), WHITE_STATES_APPEARANCES.get(k), Math.sqrt(2),0.8,false));
			}
			for (int k = 0;k<BLACK_STATES.size();k++) {
				BLACK_UCT1.add(mxjava.UCT1Array(BLACK_WINS_FOR_ACTION.get(k), BLACK_CHOSEN_ACTION_COUNT.get(k), BLACK_STATES_APPEARANCES.get(k), Math.sqrt(2),0.8,false));
			}


			//CHANGE THE ARRAYLIST TO AN ARRAY, AND TRAIN THE NEURAL NETWORK.
			double[][] whiteTrainingInput = new double[WHITE_STATES.size()][WHITE_STATES.get(0).length];
			double[][] whiteTrainingOutput = new double[WHITE_UCT1.size()][WHITE_UCT1.get(0).length];
			double[][] blackTrainingInput = new double[BLACK_STATES.size()][BLACK_STATES.get(0).length];
			double[][] blackTrainingOutput = new double[BLACK_UCT1.size()][BLACK_UCT1.get(0).length];

			for (int k = 0;k<WHITE_STATES.size();k++) {
				whiteTrainingInput[k] = WHITE_STATES.get(k);
			}
			for (int k = 0;k<WHITE_UCT1.size();k++) {
				whiteTrainingOutput[k] = WHITE_UCT1.get(k);
			}
			for (int k = 0;k<BLACK_STATES.size();k++) {
				blackTrainingInput[k] = BLACK_STATES.get(k);
			}
			for (int k = 0;k<BLACK_UCT1.size();k++) {
				blackTrainingOutput[k] = BLACK_UCT1.get(k);
			}
			wBrain.INPUT_VALUES = whiteTrainingInput;
			wBrain.OUTPUT_VALUES = whiteTrainingOutput;
			bBrain.INPUT_VALUES = blackTrainingInput;
			bBrain.OUTPUT_VALUES = blackTrainingOutput;

			wBrain.trainNetwork(1500);
			bBrain.trainNetwork(1500);
			
			print2DArrayList(WHITE_UCT1,5);
			System.out.println("\n hmm.");
			print2DArray(wBrain.OUTPUT_VALUES,5);
			resetBoard();
			System.out.println("\n"+Arrays.toString(wBrain.predict(convertToState(chessBoard))));
			System.out.println(mxjava.max(wBrain.predict(convertToState(chessBoard))));
		}

		
		//Start Reinforcement Learning
		ArrayList<Integer> whiteMoves = new ArrayList<>();
		ArrayList<Integer> blackMoves = new ArrayList<>();
		ArrayList<double[]> whiteUpdateIfWin = new ArrayList<>();
		ArrayList<double[]> blackUpdateIfWin = new ArrayList<>();
		double[] blank = new double[totalLegalMovesPerPosition];

		int simuls = 1;
		for (int x = 1;x<=simuls;x++) {

			int gamesPerSimul = (x==simuls)? 1 : 2;
			//GENERATE RANDOM GAME.
			for (int i = 0;i<gamesPerSimul ;i++) {
				int movesExchanged = 50;
				totalMoves = 0;
				resetBoard();
				PGN_GAME_LOG = "[White: Random Chess AI]\n[Black: Random Chess AI]\n\n";
				while(totalMoves<movesExchanged*2 && gameStatus() == 5) {
					if (totalMoves%2==0) {
						if (x!=simuls) { wBrain.newSynapseWeights(); }
						String move = mxjava.computerMove(wBrain.predict(convertToState(chessBoard)), legalWMoves());
						double[] action = mxjava.computerActionArray(wBrain.predict(convertToState(chessBoard)), legalWMoves());

						//ADD THE STATES AND ACTION INTO THE GAME LOG
						if (mxjava.stateInDatabase(WHITE_STATES, convertToState(chessBoard))) {
							int directory = mxjava.whereInDatabase(WHITE_STATES,convertToState(chessBoard));
							WHITE_STATES_APPEARANCES.set(directory, WHITE_STATES_APPEARANCES.get(directory)+1);
							WHITE_CHOSEN_ACTION_COUNT.set(directory, mxjava.addVectors(WHITE_CHOSEN_ACTION_COUNT.get(directory), action));
							whiteMoves.add(directory);		
						}
						else {
							whiteMoves.add(WHITE_STATES.size());
							WHITE_STATES.add(convertToState(chessBoard));
							WHITE_STATES_APPEARANCES.add((double) 1);
							WHITE_CHOSEN_ACTION_COUNT.add(action);
							WHITE_WINS_FOR_ACTION.add(blank);
						}
						whiteUpdateIfWin.add(action);

						//MAKE THE MOVE
						makeMove(move);
						pureLog += (totalMoves/2 + 1) + ".";
						pureLog += move+ " ";
						if (debugOn) {
							System.out.println(move);
							printBoard(chessBoard);
						}
					}
					else if (totalMoves%2==1) {
						if (x!=simuls) { bBrain.newSynapseWeights(); }
						String move = mxjava.computerMove(bBrain.predict(convertToState(chessBoard)), legalBMoves());
						double[] action = mxjava.computerActionArray(bBrain.predict(convertToState(chessBoard)), legalBMoves());

						//ADD THE STATES AND ACTION INTO THE GAME LOG
						if (mxjava.stateInDatabase(BLACK_STATES, convertToState(chessBoard))) {
							int directory = mxjava.whereInDatabase(BLACK_STATES,convertToState(chessBoard));
							BLACK_STATES_APPEARANCES.set(directory, BLACK_STATES_APPEARANCES.get(directory)+1);
							BLACK_CHOSEN_ACTION_COUNT.set(directory, mxjava.addVectors(BLACK_CHOSEN_ACTION_COUNT.get(directory), action));
							blackMoves.add(directory);
						}
						else {
							blackMoves.add(BLACK_STATES.size());
							BLACK_STATES.add(convertToState(chessBoard));
							BLACK_STATES_APPEARANCES.add((double)1);
							BLACK_CHOSEN_ACTION_COUNT.add(action);
							BLACK_WINS_FOR_ACTION.add(blank);
						}
						blackUpdateIfWin.add(action);
						//MAKE THE MOVE
						makeMove(move);
						pureLog += move+ " ";
						if (debugOn) {
							System.out.println(move);
							printBoard(chessBoard);
						}
					}

				}
				//*/

				if (gameStatus() == 0) PGN_GAME_LOG+= "0.5-0.5";
				else if (gameStatus() == 1) PGN_GAME_LOG += "1-0";
				else if (gameStatus() == -1) PGN_GAME_LOG += "0-1";

				printBoard(chessBoard);
				System.out.println(PGN_GAME_LOG);
				if (debugOn) System.out.println(pureLog);

				/* MACHINE LEARNING STARTS HERE.
				 * [FUTURE MUSTS: We need to write it so that we only a) adds a new state to the database if it had not been seen before,
				 * and change the database of actions taken accordingly. In the future, we will have certain 'libraries' to train the 
				 * computer on, and certain 'rating' methods for each student to use...based on real games, book openings...etc. 
				 * Perhaps will use alpha beta pruning for end game.]
				 * 
				 * Firstly, we need to make sure that one state only corresponds to one action in each game. Thus, we make sure the size of the
				 * State ArrayList is the same as the Action ArrayList.
				 * 
				 * We count the appearances of each state in the database, but since the ArrayList only shows the game log, this would not be
				 * necessary in our calculations.
				 * 
				 * Then, we back propagate the moves, with the likelihood of the moves with the formula w/n + c([sqrt(ln t/n)], where
				 * w = total wins for the chosen action
				 * n = total times the action was chosen
				 * t = sum of all n, the amount of times the state of the chessboard has been seen.
				 * c = exploration parameter, which can be edited...but is currently sqrt(2).
				 * [This formula is not finalized, we can also use RAVE, but this is the current idea.]
				 * 
				 * THOUGHTS TO CONSIDER: Should there be a separate neural network that could make evaluations of each position, so that
				 * the computer knows when to resign? Or should the computer rate every single possible move and choose to play the move that
				 * would yield a most favourable increase in rating? Possible, but training the rating would mean it follows the play style of
				 * a certain chess engine...for now, a MCTS based engine system will be tested.
				 */

				while (WHITE_STATES.size()>WHITE_CHOSEN_ACTION_COUNT.size()) {
					WHITE_STATES.remove(WHITE_STATES.size()-1);
				}
				while (BLACK_STATES.size()>BLACK_CHOSEN_ACTION_COUNT.size()) {
					BLACK_STATES.remove(BLACK_STATES.size()-1);
				}


				//UPDATE THE WINS
				for (int k = 0;k<whiteMoves.size();k++) {
					if(gameStatus()==1) {
						int directory = whiteMoves.get(k);
						WHITE_WINS_FOR_ACTION.set(whiteMoves.get(k), mxjava.addVectors(WHITE_WINS_FOR_ACTION.get(directory),whiteUpdateIfWin.get(k)));
					}
					else if (gameStatus()==0 || gameStatus() ==5) {
						int directory = whiteMoves.get(k);
						WHITE_WINS_FOR_ACTION.set(directory,mxjava.addVectors(WHITE_WINS_FOR_ACTION.get(directory),mxjava.scale(whiteUpdateIfWin.get(k), 0.5)));
					}
				}
				for (int k = 0;k<blackMoves.size()-1;k++) {
					if(gameStatus()==-1) {
						int directory = blackMoves.get(k);
						BLACK_WINS_FOR_ACTION.set(blackMoves.get(k), mxjava.addVectors(BLACK_WINS_FOR_ACTION.get(directory),blackUpdateIfWin.get(k)));
					}
					else if (gameStatus()==0 || gameStatus() ==5) {
						int directory = blackMoves.get(k);
						BLACK_WINS_FOR_ACTION.set(blackMoves.get(k), mxjava.addVectors(BLACK_WINS_FOR_ACTION.get(directory),mxjava.scale(blackUpdateIfWin.get(k),0.5)));

					}
				}

				System.out.println(gameStatus());

				if (debugOn) {
					System.out.println(WHITE_STATES.size() + " "+WHITE_CHOSEN_ACTION_COUNT.size()+" "+BLACK_STATES.size() + " "+BLACK_CHOSEN_ACTION_COUNT.size());
					System.out.println(Arrays.toString(WHITE_CHOSEN_ACTION_COUNT.get(0)));
					System.out.println(Arrays.toString(WHITE_WINS_FOR_ACTION.get(0)));
					System.out.println(Arrays.toString(whiteMoves.toArray()));
					System.out.println(Arrays.toString(blackMoves.toArray()));
				}

				whiteMoves.clear();
				blackMoves.clear();
				whiteUpdateIfWin.clear();
				blackUpdateIfWin.clear();
			}

			//TRAIN THE DATA ONCE IT'S DONE.
			if (debugOn) {
				System.out.println("\nAPPEARANCES\n---------");
				print2DArrayList(WHITE_CHOSEN_ACTION_COUNT,5);
				System.out.println("\nWINS PER ACTION\n---------");
				print2DArrayList(WHITE_WINS_FOR_ACTION,5);
			}

			WHITE_UCT1.clear();
			BLACK_UCT1.clear();
			//CREATE UCT1 FOR EACH STATE.
			if ((x-1)<simuls) {
				for (int i = 0;i<WHITE_STATES.size();i++) {
					WHITE_UCT1.add(mxjava.UCT1Array(WHITE_WINS_FOR_ACTION.get(i), WHITE_CHOSEN_ACTION_COUNT.get(i), WHITE_STATES_APPEARANCES.get(i), Math.sqrt(2),0.5,true));
				}
				for (int i = 0;i<BLACK_STATES.size();i++) {
					BLACK_UCT1.add(mxjava.UCT1Array(BLACK_WINS_FOR_ACTION.get(i), BLACK_CHOSEN_ACTION_COUNT.get(i), BLACK_STATES_APPEARANCES.get(i), Math.sqrt(2),0.5,true));
				}
			}
			else {
				for (int i = 0;i<WHITE_STATES.size();i++) {
					WHITE_UCT1.add(mxjava.UCT1Array(WHITE_WINS_FOR_ACTION.get(i), WHITE_CHOSEN_ACTION_COUNT.get(i), WHITE_STATES_APPEARANCES.get(i), Math.sqrt(2),0.5,false));
				}
				for (int i = 0;i<BLACK_STATES.size();i++) {
					BLACK_UCT1.add(mxjava.UCT1Array(BLACK_WINS_FOR_ACTION.get(i), BLACK_CHOSEN_ACTION_COUNT.get(i), BLACK_STATES_APPEARANCES.get(i), Math.sqrt(2),0.5,false));
				}
			}

			//CHANGE THE ARRAYLIST TO AN ARRAY, AND TRAIN THE NEURAL NETWORK.
			double[][] whiteTrainingInput = new double[WHITE_STATES.size()][WHITE_STATES.get(0).length];
			double[][] whiteTrainingOutput = new double[WHITE_UCT1.size()][WHITE_UCT1.get(0).length];
			double[][] blackTrainingInput = new double[BLACK_STATES.size()][BLACK_STATES.get(0).length];
			double[][] blackTrainingOutput = new double[BLACK_UCT1.size()][BLACK_UCT1.get(0).length];

			for (int i = 0;i<WHITE_STATES.size();i++) {
				whiteTrainingInput[i] = WHITE_STATES.get(i);
			}
			for (int i = 0;i<WHITE_UCT1.size();i++) {
				whiteTrainingOutput[i] = WHITE_UCT1.get(i);
			}
			for (int i = 0;i<BLACK_STATES.size();i++) {
				blackTrainingInput[i] = BLACK_STATES.get(i);
			}
			for (int i = 0;i<BLACK_UCT1.size();i++) {
				blackTrainingOutput[i] = BLACK_UCT1.get(i);
			}
			wBrain.INPUT_VALUES = whiteTrainingInput;
			wBrain.OUTPUT_VALUES = whiteTrainingOutput;
			bBrain.INPUT_VALUES = blackTrainingInput;
			bBrain.OUTPUT_VALUES = blackTrainingOutput;

			if (x<simuls) {
				wBrain.trainNetwork(250);
				bBrain.trainNetwork(250);
			}

			System.out.println("SIMULATION " + x + " completed.");

			print2DArrayList(WHITE_UCT1,5);

			if (debugOn) {
				System.out.println(WHITE_STATES.size() + " " + WHITE_UCT1.size());
				System.out.println(WHITE_STATES.get(0).length + " " + WHITE_UCT1.get(0).length);
				System.out.println("\nUCT1\n---------");
				print2DArrayList(WHITE_UCT1,15);
				System.out.println("\nUCT2\n---------");
				print2DArrayList(BLACK_UCT1,15);
			}
		}
	}

	public static void print2DArrayList(ArrayList<double[]> ab, int c) {
		for (int i = 0;i<Math.min(c, ab.size());i++) {
			double[] newArray = ab.get(i);
			System.out.println(Arrays.toString(newArray));
		}
	}
	
	public static void print2DArray(double[][] ab, int c) {
		for (int i = 0;i<Math.min(c, ab.length);i++) {
			double[] newArray = ab[i];
			System.out.println(Arrays.toString(newArray));
		}
	}

	public static void resetBoard() {
		boolean[] a = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
		int[] b= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		pawnDoubleMove = a;
		timePawnMoved = b;
		String[][] reset = {
				//A   B   C   D   E   F   G   H
				{"r","n","b","q","k","b","n","r"},  //8
				{"p","p","p","p","p","p","p","p"},  //7
				{" "," "," "," "," "," "," "," "},  //6
				{" "," "," "," "," "," "," "," "},  //5
				{" "," "," "," "," "," "," "," "},  //4
				{" "," "," "," "," "," "," "," "},  //3
				{"P","P","P","P","P","P","P","P"},  //2
				{"R","N","B","Q","K","B","N","R"}};	//1
		chessBoard = reset;
	}


	public static void showAllPossibleWhiteMoves() {
		String[] legalWMoves = legalWMoves();
		if (legalWMoves[0].equals("")) System.out.println("No possible moves.");
		else {
			for (int i = 0;i<legalWMoves().length;i++) {
				makeMove(legalWMoves[i]);
				System.out.println(legalWMoves[i]);
				moveStatusBoardPrint();
				undoMove(legalWMoves[i]);
			}
		}
	}

	public static void showAllPossibleBlackMoves() {
		String[] legalBMoves = legalBMoves();
		if (legalBMoves[0].equals("")) System.out.println("No possible moves.");
		else {
			for (int i = 0;i<legalWMoves().length;i++) {
				makeMove(legalBMoves[i]);
				System.out.println(legalBMoves[i]);
				moveStatusBoardPrint();
				undoMove(legalBMoves[i]);
			}
		}
	}

	public static int gameStatus() {
		//1 is a white win, 0 is a tie, -1 is a black win, and 5 is ongoing.
		if(legalWMoves()[0].equals("")) { 
			if (!whiteKingSafe()) { return -1; }
			else { return 0;} 
		}
		if(legalBMoves()[0].equals("")) { 
			if (!blackKingSafe()) { return 1; }
			else { return 0;} 
		}
		return 5;
	}

	public static void moveStatusBoardPrint() {
		System.out.println("Move #" + ((totalMoves-1)/2+1));
		String nextToMove = (totalMoves%2 == 0) ? "White" : "Black";
		System.out.println("Next to move: " + nextToMove + "\n");
		printBoard(chessBoard);
	}

	public static String compReadableMove(String a) {
		String move = a;
		String[] files = "abcdefgh".split("");
		if (a.length()>3) {
			if (!a.substring(4).equals("=")) {
				for (int i = 0;i<files.length;i++) {
					move = move.replace(files[i], Integer.toString(i+1));
				}
			}
			else {
				for (int i = 0;i<files.length;i++) {
					move = move.substring(0,1).replace(files[i], Integer.toString(i+1))+move.substring(1,2).replace(files[i], Integer.toString(i+1))+move.substring(2);
				}
			}
		}
		return move.toString();
	}

	public static void undoMove(String a) {
		PGN_GAME_LOG = PGN_GAME_LOG.replace(moveUpdatePGN(a), "");
		a = compReadableMove(a);
		if (a.equals("O-O")) {
			if (totalMoves%2!=0) {
				chessBoard[7][4] = "K";
				chessBoard[7][7] = "R";
				chessBoard[7][6] = " ";
				chessBoard[7][5] = " ";
				whiteKingMoved--;
				kwRookMoved--;
			}
			if (totalMoves%2!=1) {
				chessBoard[0][4] = "k";
				chessBoard[0][7] = "r";
				chessBoard[0][6] = " ";
				chessBoard[0][5] = " ";
				blackKingMoved--;
				kbRookMoved--;
			}
		}
		if (a.length()>3) {
			if (a.substring(4).equals("=")) {
				if (totalMoves%2!=0) {
					chessBoard[1][Character.getNumericValue(a.charAt(0))-1] = "P";
					chessBoard[0][Character.getNumericValue(a.charAt(1))-1] = (a.substring(2, 3).equals("/")) ? " " : a.substring(2, 3);
				}
				else if (totalMoves%2!=1) {
					chessBoard[6][Character.getNumericValue(a.charAt(0))-1] = "p";
					chessBoard[7][Character.getNumericValue(a.charAt(1))-1] = (a.substring(2, 3).equals("/")) ? " " : a.substring(2, 3);
				}
			}
			else if (a.substring(4,5).equals("s")) {
				chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1] = " ";
				chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1] = (totalMoves%2==0)? "p":"P";
				chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(2))-1] = (totalMoves%2==0)? "P":"p";

			}
			else if (a.equals("O-O-O")) {
				if (totalMoves%2!=0) {
					chessBoard[7][4] = "K";
					chessBoard[7][0] = "R";
					chessBoard[7][2] = " ";
					chessBoard[7][3] = " ";
					whiteKingMoved--;
					qwRookMoved--;
				}
				if (totalMoves%2!=1) {
					chessBoard[0][4] = "k";
					chessBoard[0][0] = "r";
					chessBoard[0][2] = " ";
					chessBoard[0][3] = " ";
					blackKingMoved--;
					qbRookMoved--;
				}
			}
			else {
				String temp = chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1];
				chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1] = (totalMoves%2 == 0) ? a.substring(4) : a.substring(4).toLowerCase();
				chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1] = temp;
				if (chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1].equals("")) chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1]=" ";
				//CHECKING EN PASSANT
				if (temp.equals("P") && a.substring(1, 2).equals("2")) {pawnDoubleMove[Integer.parseInt(a.substring(0,1))-1] = false; timePawnMoved[Integer.parseInt(a.substring(0,1))-1] = 0;}
				if (temp.equals("p") && a.substring(1, 2).equals("7")) {pawnDoubleMove[Integer.parseInt(a.substring(0,1))+7] = false; timePawnMoved[Integer.parseInt(a.substring(0,1))+7] = 0;}
				//CHECKING CASTLE LEGALITY
				if (temp.equals("K")) whiteKingMoved--;
				if (temp.equals("k")) blackKingMoved--;
				if (temp.equals("R") && a.substring(0,2).equals("a1")) qwRookMoved--;
				if (temp.equals("R") && a.substring(0,2).equals("h1")) kwRookMoved--;
				if (temp.equals("r") && a.substring(0,2).equals("a8")) qbRookMoved--;
				if (temp.equals("r") && a.substring(0,2).equals("h8")) kbRookMoved--;

			}
		}
		totalMoves--;
	}

	public static String moveUpdatePGN(String raw) {
		String a = compReadableMove(raw);
		String moveMarker = (totalMoves%2==0) ? (totalMoves/2 + 1) + "." : "";
		if (a.equals("O-O") || a.equals("O-O-O")) {
			return moveMarker+a;
		}
		else if (a.length()>4) {
			if (!a.substring(4).equals("=")) {
				if (chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase().equals("P")) {
					return moveMarker+raw.substring(0,1) + "x" + raw.substring(2,4);
				}

				else {
					if (totalMoves%2==0) {
						if (mxjava.specifyInitial(legalWMoves(), chessBoard, a)) return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + raw.substring(0,1)+"x"+raw.substring(2,4)).replace("P", "");
						else return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + "x"+raw.substring(2,4)).replace("P", "");
					}
					else if (totalMoves%2==1) {
						if (mxjava.specifyInitial(legalBMoves(), chessBoard, a)) return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + raw.substring(0,1)+"x"+raw.substring(2,4)).replace("P", "");
						else return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + "x"+raw.substring(2,4)).replace("P", "");
					}}
			}
			else {
				if (totalMoves%2==0) { 
					if (raw.substring(0,1).equals(raw.substring(1,2))) {
						return moveMarker+raw.substring(1,2) +"8="+raw.substring(3,4);
					}
					else {
						return moveMarker+raw.substring(0,1) + "x" + raw.substring(1,2) +"8="+raw.substring(3,4);
					}
				}
				else if (totalMoves%2==1) { 
					if (raw.substring(2,3) == "/") {
						return moveMarker+raw.substring(1,2) +"1="+raw.substring(3,4).toUpperCase();
					}
					else {
						return moveMarker+raw.substring(0,1) + "x" + raw.substring(1,2) +"1="+raw.substring(3,4).toUpperCase();
					}
				}
			}
		}
		else if (raw.length()==4) {	
			if (totalMoves%2==0) {
				if (mxjava.specifyInitial(legalWMoves(), chessBoard, a)) return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + raw.substring(0,1)+raw.substring(2,4)).replace("P", "");
				else return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + raw.substring(2,4)).replace("P", "");
			}
			else if (totalMoves%2==1) {
				if (mxjava.specifyInitial(legalBMoves(), chessBoard, a)) return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + raw.substring(0,1)+raw.substring(2,4)).replace("P", "");
				else return moveMarker+(chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1].toUpperCase() + raw.substring(2,4)).replace("P", "");
			}
		}
		return "";
	}

	public static void makeMove(String a) {
		String raw = a;
		PGN_GAME_LOG += moveUpdatePGN(raw);
		a = compReadableMove(a);

		if (a.length()>3) {
			if (a.substring(4).equals("=")) {
				if (totalMoves%2==0) {
					chessBoard[1][Character.getNumericValue(a.charAt(0))-1] = " ";
					chessBoard[0][Character.getNumericValue(a.charAt(1))-1] = a.substring(3,4);
				}
				else if (totalMoves%2==1) {
					chessBoard[6][Character.getNumericValue(a.charAt(0))-1] = " ";
					chessBoard[7][Character.getNumericValue(a.charAt(1))-1] = a.substring(3,4);
				}

			}
			else if (a.substring(4).equals("s")) {
				String temp = chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1];
				chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1] = temp;
				chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1] = " ";
				chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(2))-1] = " ";
			}
			else if (a.equals("O-O-O")) {
				if (totalMoves%2==0) {
					chessBoard[7][4] = " ";
					chessBoard[7][0] = " ";
					chessBoard[7][2] = "K";
					chessBoard[7][3] = "R";
					whiteKingMoved++;
					qwRookMoved++;
				}
				else if (totalMoves%2==1) {
					chessBoard[0][4] = " ";
					chessBoard[0][0] = " ";
					chessBoard[0][2] = "k";
					chessBoard[0][3] = "r";
					blackKingMoved++;
					qbRookMoved++;
				}
			}
			else {
				String temp = chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1];
				chessBoard[8-(Character.getNumericValue(a.charAt(3)))][Character.getNumericValue(a.charAt(2))-1] = temp;
				chessBoard[8-(Character.getNumericValue(a.charAt(1)))][Character.getNumericValue(a.charAt(0))-1] = " ";
				//CHECKING EN PASSANT LEGALITY
				if (temp.equals("P") && a.substring(1, 2).equals("2") && a.substring(3, 4).equals("4")) {pawnDoubleMove[Integer.parseInt(a.substring(0,1))-1] = true; timePawnMoved[Integer.parseInt(a.substring(0,1))-1] = totalMoves+1;}
				if (temp.equals("p") && a.substring(1, 2).equals("7") && a.substring(3, 4).equals("5")) {pawnDoubleMove[Integer.parseInt(a.substring(0,1))+7] = true; timePawnMoved[Integer.parseInt(a.substring(0,1))+7] = totalMoves+1;}
				if (temp.equals("K")) whiteKingMoved++;
				if (temp.equals("k")) blackKingMoved++;
				if (temp.equals("R") && a.substring(0,2).equals("a1")) { qwRookMoved++; chessBoard[0][0]=" ";}
				if (temp.equals("R") && a.substring(0,2).equals("h1")) { kwRookMoved++; chessBoard[7][0]=" ";}
				if (temp.equals("r") && a.substring(0,2).equals("a8")) {qbRookMoved++; chessBoard[0][7]=" ";}
				if (temp.equals("r") && a.substring(0,2).equals("h8")) {kbRookMoved++; chessBoard[7][7]=" ";}
			}
		}
		else {
			if (a.equals("O-O")) {
				if (totalMoves%2==0) {
					chessBoard[7][4] = " ";
					chessBoard[7][7] = " ";
					chessBoard[7][6] = "K";
					chessBoard[7][5] = "R";
					whiteKingMoved++;
					kwRookMoved++;
				}
				else if (totalMoves%2==1) {
					chessBoard[0][4] = " ";
					chessBoard[0][7] = " ";
					chessBoard[0][6] = "k";
					chessBoard[0][5] = "r";
					blackKingMoved++;
					kbRookMoved++;
				}
			}
		}
		totalMoves++;
		if ((!whiteKingSafe() || !blackKingSafe()) && gameStatus()==5) PGN_GAME_LOG+= "+";
		if ((!whiteKingSafe() || !blackKingSafe()) && (gameStatus()==1 || gameStatus()==-1)) PGN_GAME_LOG+= "# ";
		else {
			PGN_GAME_LOG+=" ";
		}

	}

	public static String[][] stateVisual(double[] state) {
		String[][] a = {
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "},
				{" "," "," "," "," "," "," "," "}};

		for (int i = 0;i<(64*6);i++) {
			if (state[i]==1) {
				if (i%6==0) a[(i/6)/8][(i/6)%8] = "P";
				if (i%6==1) a[(i/6)/8][(i/6)%8] = "N";
				if (i%6==2) a[(i/6)/8][(i/6)%8] = "B";
				if (i%6==3) a[(i/6)/8][(i/6)%8] = "R";
				if (i%6==4) a[(i/6)/8][(i/6)%8] = "Q";
				if (i%6==5) a[(i/6)/8][(i/6)%8] = "K";
			} 
			else if (state[i]==-1) {
				if (i%6==0) a[(i/6)/8][(i/6)%8] = "p";
				if (i%6==1) a[(i/6)/8][(i/6)%8] = "n";
				if (i%6==2) a[(i/6)/8][(i/6)%8] = "b";
				if (i%6==3) a[(i/6)/8][(i/6)%8] = "r";
				if (i%6==4) a[(i/6)/8][(i/6)%8] = "q";
				if (i%6==5) a[(i/6)/8][(i/6)%8] = "k";
			} 
		}
		return a;
	}

	public static void printBoard(String[][] a) {
		System.out.println(" A  B  C  D  E  F  G  H");
		System.out.println("------------------------");
		for (int i = 0;i<8;i++) {
			System.out.println(Arrays.toString(a[i]) + " |" + Integer.valueOf(8-(i)) + "| ");
		}
		System.out.println("------------------------");
	}

	public static double[] convertToState(String[][] chessBoard) {
		double[] a = new double[384];
		for (int i = 0;i<64;i++) {
			switch (chessBoard[i/8][i%8]) {
			case "P": a[i*6] = 4;
			break;
			case "N": a[i*6+1] = 4;
			break;
			case "B": a[i*6+2] = 4;
			break;
			case "R": a[i*6+3] = 4;
			break;
			case "Q": a[i*6+4] = 4;
			break;
			case "K": a[i*6+5] = 4;
			break;
			case "p": a[i*6] = -4;
			break;
			case "n": a[i*6+1] = -4;
			break;
			case "b": a[i*6+2] = -4;
			break;
			case "r": a[i*6+3] = -4;
			break;
			case "q": a[i*6+4] = -4;
			break;
			case "k": a[i*6+5] = -4;
			break;
			}
		}
		return a;
	} 

	public static int findWhiteKing() {
		for (int i = 0;i<64;i++) {
			if (chessBoard[i/8][i%8].equals("K")) return i;
		}
		return 0;
	}

	public static String[] legalWMoves() {
		String moves="";
		for (int i=0; i<64; i++) {
			switch (chessBoard[i/8][i%8]) {
			case "P": moves+=legalWP(i);
			break;
			case "R": moves+=legalWR(i);
			break;
			case "N": moves+=legalWN(i);
			break;
			case "B": moves+=legalWB(i);
			break;
			case "Q": moves+=legalWQ(i);
			break;
			case "K": moves+=legalWK(i);
			break;
			}
		}
		return moves.split(" ");
	}

	public static String legalWhiteEnPassant(int i) {
		int r=i/8, c=i%8;
		int displayR = 8-r;
		for (int j=-1; j<=1; j+=2) { 
			try {//en passant
				if(chessBoard[r][c+j].equals("p") && chessBoard[r-1][c+j].equals(" ") && displayR == 5 && (timePawnMoved[8+c+j]-totalMoves==0) && pawnDoubleMove[8+c+j]) {

					chessBoard[r][c] = " ";
					chessBoard[r][c+j] = " ";
					chessBoard[r-1][c+j] = "P";
					if (whiteKingSafe()) {
						chessBoard[r][c] = "P";
						chessBoard[r][c+j] = "p";
						chessBoard[r-1][c+j] = " ";
						return colNames[c]+displayR+colNames[c+j]+(displayR+1)+"s ";
					}
					else {
						chessBoard[r][c] = "P";
						chessBoard[r][c+j] = "p";
						chessBoard[r-1][c+j] = " ";
					}
				}
				else {

				}
			} catch (Exception e) {}
		}
		return "";
	}

	public static String legalBlackEnPassant(int i) {
		String moves="";
		int r=i/8, c=i%8;
		int displayR = 8-r;
		for (int j=-1; j<=1; j+=2) { 
			try {//en passant
				if(chessBoard[r][c+j].equals("P") && chessBoard[r+1][c+j].equals(" ") && displayR == 4 && (timePawnMoved[c+j]-totalMoves==0) && pawnDoubleMove[c+j]) {
					chessBoard[r][c] = " ";
					chessBoard[r][c+j] = " ";
					chessBoard[r+1][c+j] = "p";
					if (whiteKingSafe()) {
						chessBoard[r][c] = "p";
						chessBoard[r][c+j] = "P";
						chessBoard[r+1][c+j] = " ";
						return colNames[c]+displayR+colNames[c+j]+(displayR-1)+"s ";
					}
					else {
						chessBoard[r][c] = "p";
						chessBoard[r][c+j] = "P";
						chessBoard[r+1][c+j] = " ";
					}
				}
				else {

				}
			} catch (Exception e) {}
		}
		return "";
	}

	public static String legalWP(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		for (int j=-1; j<=1; j+=2) {
			try {//capture
				if (Character.isLowerCase(chessBoard[r-1][c+j].charAt(0)) && i>=16) {
					oldPiece=chessBoard[r-1][c+j];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r-1][c+j]="P";
					if (whiteKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[(c+j)]+(displayR+1)+oldPiece+" ";
					}
					chessBoard[r][c]="P";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r-1][c+j]=oldPiece;
				}
			} catch (Exception e) {}
			/*
			try {//en passant
				if (chessBoard[r][c+j].equals("p") && r==3) {
					chessBoard[r][c]=" ";
					chessBoard[r][c+j]=" ";
					chessBoard[r-1][c+j]="P";
					if (whiteKingSafe() && pawnDoubleMove[8+c+j] && ((totalMoves-timePawnMoved[8+c+j])==0)) {
						moves=moves+colNames[c]+displayR+colNames[(c+j)]+(displayR+1)+"x ";
					}
					chessBoard[r][c]="P";
					chessBoard[r][c+j]="p";
					chessBoard[r-1][c+j]=" ";
				}
			} catch (Exception e) {}
			 */
			try {//promotion && capture
				if (Character.isLowerCase(chessBoard[r-1][c+j].charAt(0)) && i<16) {
					String[] temp={"Q","R","B","N"};
					for (int k=0; k<4; k++) {
						oldPiece=chessBoard[r-1][c+j];
						chessBoard[r][c]=" ";
						chessBoard[r-1][c+j]=temp[k];
						if (whiteKingSafe()) {
							//column1,column2,captured-piece,new-piece,=
							moves=moves+colNames[c]+colNames[(c+j)]+oldPiece+temp[k]+"="+" ";
						}
						chessBoard[r][c]="P";
						chessBoard[r-1][c+j]=oldPiece;
					}
				}
			} catch (Exception e) {}
		}
		try {//move one up
			if (" ".equals(chessBoard[r-1][c]) && i>=16) {
				oldPiece=chessBoard[r-1][c];
				chessBoard[r][c]=" ";
				chessBoard[r-1][c]="P";
				if (whiteKingSafe()) {
					moves=moves+colNames[c]+displayR+colNames[c]+(displayR+1)+oldPiece;
				}
				chessBoard[r][c]="P";
				if(oldPiece.equals("")) oldPiece = " ";
				chessBoard[r-1][c]=oldPiece;
			}
		} catch (Exception e) {}
		try {//promotion && no capture
			if (" ".equals(chessBoard[r-1][c]) && i<16) {
				String[] temp={"Q","R","B","N"};
				for (int k=0; k<4; k++) {
					oldPiece=chessBoard[r-1][c];
					chessBoard[r][c]=" ";
					chessBoard[r-1][c]=temp[k];
					if (whiteKingSafe()) {
						//column1,column2,captured-piece,new-piece,P
						moves=moves+colNames[c]+colNames[c]+"/"+temp[k]+"="+" ";
					}
					chessBoard[r][c]="P";
					chessBoard[r-1][c]=oldPiece;
				}
			}
		} catch (Exception e) {}
		try {//move two up
			if (" ".equals(chessBoard[r-1][c]) && " ".equals(chessBoard[r-2][c]) && i>=48 && r==6) {
				oldPiece=chessBoard[r-2][c];
				chessBoard[r][c]=" ";
				chessBoard[r-2][c]="P";
				if (whiteKingSafe()) {
					moves=moves+colNames[c]+displayR+colNames[c]+(displayR+2)+oldPiece;
				}
				chessBoard[r][c]="P";
				if(oldPiece.equals("")) oldPiece = " ";
				chessBoard[r-2][c]=oldPiece;
			}
		} catch (Exception e) {}
		return moves+legalWhiteEnPassant(i);
	}

	public static String legalWR(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		int temp=1;
		for (int j=-1; j<=1; j+=2) {
			try {
				while(" ".equals(chessBoard[r][c+temp*j]))
				{
					oldPiece=chessBoard[r][c+temp*j];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r][c+temp*j]="R";
					if (whiteKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[(c+temp*j)]+displayR+oldPiece+" ";
					}
					chessBoard[r][c]="R";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r][c+temp*j]=oldPiece;
					temp++;
				}
				if (Character.isLowerCase(chessBoard[r][c+temp*j].charAt(0))) {
					oldPiece=chessBoard[r][c+temp*j];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r][c+temp*j]="R";
					if (whiteKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[(c+temp*j)]+displayR+oldPiece+" ";
					}
					chessBoard[r][c]="R";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r][c+temp*j]=oldPiece;
				}
			} catch (Exception e) {}
			temp=1;
			try {
				while(" ".equals(chessBoard[r+temp*j][c]))
				{
					oldPiece=chessBoard[r+temp*j][c];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r+temp*j][c]="R";
					if (whiteKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[c]+(displayR-temp*j)+oldPiece+" ";
					}
					chessBoard[r][c]="R";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r+temp*j][c]=oldPiece;
					temp++;
				}
				if (Character.isLowerCase(chessBoard[r+temp*j][c].charAt(0))) {
					oldPiece=chessBoard[r+temp*j][c];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r+temp*j][c]="R";
					if (whiteKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[c]+(displayR-temp*j)+oldPiece+" ";
					}
					chessBoard[r][c]="R";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r+temp*j][c]=oldPiece;
				}
			} catch (Exception e) {}
			temp=1;
		}
		return moves;
	}
	public static String legalWN(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		for (int j=-1; j<=1; j+=2) {
			for (int k=-1; k<=1; k+=2) {
				try {
					if (Character.isLowerCase(chessBoard[r+j][c+k*2].charAt(0)) || " ".equals(chessBoard[r+j][c+k*2])) {
						oldPiece=chessBoard[r+j][c+k*2];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+j][c+k*2] = "N";
						if (whiteKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+k*2)]+(displayR-j)+oldPiece+" ";
						}
						chessBoard[r][c]="N";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+j][c+k*2]=oldPiece;
					}
				} catch (Exception e) {}
				try {
					if (Character.isLowerCase(chessBoard[r+j*2][c+k].charAt(0)) || " ".equals(chessBoard[r+j*2][c+k])) {
						oldPiece=chessBoard[r+j*2][c+k];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+j*2][c+k]="N";
						if (whiteKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+k)]+(displayR-j*2)+oldPiece+" ";
						}
						chessBoard[r][c]="N";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+j*2][c+k]=oldPiece;
					}
				} catch (Exception e) {}
			}
		}
		return moves;
	}

	public static String legalWB(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		int temp=1;
		for (int j=-1; j<=1; j+=2) {
			for (int k=-1; k<=1; k+=2) {
				try {
					while(" ".equals(chessBoard[r+temp*j][c+temp*k]))
					{
						oldPiece=chessBoard[r+temp*j][c+temp*k];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+temp*j][c+temp*k]="B";
						if (whiteKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
						}
						chessBoard[r][c]="B";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+temp*j][c+temp*k]=oldPiece;
						temp++;
					}
					if (Character.isLowerCase(chessBoard[r+temp*j][c+temp*k].charAt(0))) {
						oldPiece=chessBoard[r+temp*j][c+temp*k];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+temp*j][c+temp*k]="B";
						if (whiteKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
						}
						chessBoard[r][c]="B";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+temp*j][c+temp*k]=oldPiece;
					}
				} catch (Exception e) {}
				temp=1;
			}
		}
		return moves;
	}
	public static String legalWQ(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		int temp=1;
		for (int j=-1; j<=1; j++) {
			for (int k=-1; k<=1; k++) {
				if (j!=0 || k!=0) {
					try {
						while(" ".equals(chessBoard[r+temp*j][c+temp*k]))
						{
							oldPiece=chessBoard[r+temp*j][c+temp*k];
							if (oldPiece.equals(" ")) oldPiece="";
							chessBoard[r][c]=" ";
							chessBoard[r+temp*j][c+temp*k]="Q";
							if (whiteKingSafe()) {
								moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
							}
							chessBoard[r][c]="Q";
							if (oldPiece.equals("")) oldPiece=" ";
							chessBoard[r+temp*j][c+temp*k]=oldPiece;
							temp++;
						}
						if (Character.isLowerCase(chessBoard[r+temp*j][c+temp*k].charAt(0))) {
							oldPiece=chessBoard[r+temp*j][c+temp*k];
							if (oldPiece.equals(" ")) oldPiece="";
							chessBoard[r][c]=" ";
							chessBoard[r+temp*j][c+temp*k]="Q";
							if (whiteKingSafe()) {
								moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
							}
							chessBoard[r][c]="Q";
							if (oldPiece.equals("")) oldPiece=" ";
							chessBoard[r+temp*j][c+temp*k]=oldPiece;
						}
					} catch (Exception e) {}
					temp=1;
				}
			}
		}
		return moves;
	}
	public static String legalWK(int i) {
		int whiteKingPos = findWhiteKing();
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		if (chessBoard[7][4].equals("K") && chessBoard[7][7].equals("R") && chessBoard[7][5].equals(" ") && chessBoard[7][6].equals(" ")) {
			if (whiteKingMoved==0 && kwRookMoved==0) {
				chessBoard[7][4] = " ";
				chessBoard[7][7] = " ";
				chessBoard[7][5] = "R";
				chessBoard[7][6] = "K";
				if (whiteKingSafe()) {
					moves=moves+"O-O ";
				}
				chessBoard[7][5] = " ";
				chessBoard[7][6] = " ";
				chessBoard[7][7] = "R";
				chessBoard[7][4] = "K";
			}
		}
		if (chessBoard[7][4].equals("K") && chessBoard[7][0].equals("R") && chessBoard[7][1].equals(" ") && chessBoard[7][2].equals(" ") && chessBoard[7][3].equals(" ")) {
			if (whiteKingMoved==0 && qwRookMoved==0) {
				chessBoard[7][4] = " ";
				chessBoard[7][0] = " ";
				chessBoard[7][3] = "R";
				chessBoard[7][2] = "K";
				if (whiteKingSafe()) {
					moves=moves+"O-O-O ";
				}
				chessBoard[7][4] = "K";
				chessBoard[7][0] = "R";
				chessBoard[7][3] = " ";
				chessBoard[7][2] = " ";
			}
		}
		for (int j=0; j<9; j++) {
			if (j!=4) {
				try {
					if (Character.isLowerCase(chessBoard[r-1+j/3][c-1+j%3].charAt(0)) || " ".equals(chessBoard[r-1+j/3][c-1+j%3])) {
						oldPiece=chessBoard[r-1+j/3][c-1+j%3];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r-1+j/3][c-1+j%3]="K";
						int kingTemp=whiteKingPos;
						whiteKingPos=i+(j/3)*8+j%3-9;
						if (whiteKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c-1+j%3)]+(displayR+1-j/3)+oldPiece+" ";
						}
						chessBoard[r][c]="K";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r-1+j/3][c-1+j%3]=oldPiece;
						whiteKingPos=kingTemp;
					}
				} catch (Exception e) {}
			}
		}
		//need to add casting later
		return moves;
	}
	public static boolean whiteKingSafe() {
		int whiteKingPos = findWhiteKing();
		//bishop/queen
		int temp=1;
		for (int i=-1; i<=1; i+=2) {
			for (int j=-1; j<=1; j+=2) {
				try {
					while(" ".equals(chessBoard[whiteKingPos/8+temp*i][whiteKingPos%8+temp*j])) {temp++;}
					if ("b".equals(chessBoard[whiteKingPos/8+temp*i][whiteKingPos%8+temp*j]) ||
							"q".equals(chessBoard[whiteKingPos/8+temp*i][whiteKingPos%8+temp*j])) {
						return false;
					}
				} catch (Exception e) {}
				temp=1;
			}
		}
		//rook/queen
		for (int i=-1; i<=1; i+=2) {
			try {
				while(" ".equals(chessBoard[whiteKingPos/8][whiteKingPos%8+temp*i])) {temp++;}
				if ("r".equals(chessBoard[whiteKingPos/8][whiteKingPos%8+temp*i]) ||
						"q".equals(chessBoard[whiteKingPos/8][whiteKingPos%8+temp*i])) {
					return false;
				}
			} catch (Exception e) {}
			temp=1;
			try {
				while(" ".equals(chessBoard[whiteKingPos/8+temp*i][whiteKingPos%8])) {temp++;}
				if ("r".equals(chessBoard[whiteKingPos/8+temp*i][whiteKingPos%8]) ||
						"q".equals(chessBoard[whiteKingPos/8+temp*i][whiteKingPos%8])) {
					return false;
				}
			} catch (Exception e) {}
			temp=1;
		}
		//knight
		for (int i=-1; i<=1; i+=2) {
			for (int j=-1; j<=1; j+=2) {
				try {
					if ("n".equals(chessBoard[whiteKingPos/8+i][whiteKingPos%8+j*2])) {
						return false;
					}
				} catch (Exception e) {}
				try {
					if ("n".equals(chessBoard[whiteKingPos/8+i*2][whiteKingPos%8+j])) {
						return false;
					}
				} catch (Exception e) {}
			}
		}
		//pawn
		if (whiteKingPos>=16) {
			try {
				if ("p".equals(chessBoard[whiteKingPos/8-1][whiteKingPos%8-1])) {
					return false;
				}
			} catch (Exception e) {}
			try {
				if ("p".equals(chessBoard[whiteKingPos/8-1][whiteKingPos%8+1])) {
					return false;
				}
			} catch (Exception e) {}
			//king
			for (int i=-1; i<=1; i++) {
				for (int j=-1; j<=1; j++) {
					if (i!=0 || j!=0) {
						try {
							if ("k".equals(chessBoard[whiteKingPos/8+i][whiteKingPos%8+j])) {
								return false;
							}
						} catch (Exception e) {}
					}
				}
			}
		}
		return true;
	}

	public static int findBlackKing() {
		for (int i = 0;i<64;i++) {
			if (chessBoard[i/8][i%8].equals("k")) return i;
		}
		return 0;
	}

	public static String[] legalBMoves() {
		String moves="";
		for (int i=0; i<64; i++) {
			switch (chessBoard[i/8][i%8]) {
			case "p": moves+=legalBP(i);
			break;
			case "r": moves+=legalBR(i);
			break;
			case "n": moves+=legalBN(i);
			break;
			case "b": moves+=legalBB(i);
			break;
			case "q": moves+=legalBQ(i);
			break;
			case "k": moves+=legalBK(i);
			break;
			}
		}
		return moves.split(" ");
	}


	public static String legalBP(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		for (int j=-1; j<=1; j+=2) {
			try {//capture
				if (Character.isUpperCase(chessBoard[r+1][c+j].charAt(0)) && i<48) {
					oldPiece=chessBoard[r+1][c+j];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r+1][c+j]="p";
					if (blackKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[(c+j)]+(displayR-1)+oldPiece+" ";
					}
					chessBoard[r][c]="p";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r+1][c+j]=oldPiece;
				}
			} catch (Exception e) {}
			try {//promotion && capture
				if (Character.isUpperCase(chessBoard[r+1][c+j].charAt(0)) && i>=48) {
					String[] temp={"q","r","b","n"};
					for (int k=0; k<4; k++) {
						oldPiece=chessBoard[r+1][c+j];
						chessBoard[r][c]=" ";
						chessBoard[r+1][c+j]=temp[k];
						if (blackKingSafe()) {
							//column1,column2,captured-piece,new-piece,=
							moves=moves+colNames[c]+colNames[(c+j)]+oldPiece+temp[k]+"="+" ";
						}
						chessBoard[r][c]="p";
						chessBoard[r+1][c+j]=oldPiece;
					}
				}
			} catch (Exception e) {}
		}
		try {//move one up
			if (" ".equals(chessBoard[r+1][c]) && i<48) {
				oldPiece=chessBoard[r+1][c];
				chessBoard[r][c]=" ";
				chessBoard[r+1][c]="p";
				if (blackKingSafe()) {
					moves=moves+colNames[c]+displayR+colNames[c]+(displayR-1)+oldPiece;
				}
				chessBoard[r][c]="p";
				if(oldPiece.equals("")) oldPiece = " ";
				chessBoard[r+1][c]=oldPiece;
			}
		} catch (Exception e) {}
		try {//promotion && no capture
			if (" ".equals(chessBoard[r+1][c]) && i>=48) {
				String[] temp={"q","r","b","n"};
				for (int k=0; k<4; k++) {
					oldPiece=chessBoard[r+1][c];
					chessBoard[r][c]=" ";
					chessBoard[r+1][c]=temp[k];
					if (blackKingSafe()) {
						//column1,column2,captured-piece,new-piece,P
						moves=moves+colNames[c]+colNames[c]+"/"+temp[k]+"="+" ";
					}
					chessBoard[r][c]="p";
					chessBoard[r+1][c]=oldPiece;
				}
			}
		} catch (Exception e) {}
		try {//move two up
			if (" ".equals(chessBoard[r+1][c]) && " ".equals(chessBoard[r+2][c]) && i<=16 && r==1) {
				oldPiece=chessBoard[r+2][c];
				chessBoard[r][c]=" ";
				chessBoard[r+2][c]="p";
				if (blackKingSafe()) {
					moves=moves+colNames[c]+displayR+colNames[c]+(displayR-2)+oldPiece;
				}
				chessBoard[r][c]="p";
				if(oldPiece.equals("")) oldPiece = " ";
				chessBoard[r+2][c]=oldPiece;
			}
		} catch (Exception e) {}
		return moves+legalBlackEnPassant(i);
	}


	public static String legalBR(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		int temp=1;
		for (int j=-1; j<=1; j+=2) {
			try {
				while(" ".equals(chessBoard[r][c+temp*j]))
				{
					oldPiece=chessBoard[r][c+temp*j];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r][c+temp*j]="r";
					if (blackKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[(c+temp*j)]+displayR+oldPiece+" ";
					}
					chessBoard[r][c]="r";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r][c+temp*j]=oldPiece;
					temp++;
				}
				if (Character.isUpperCase(chessBoard[r][c+temp*j].charAt(0))) {
					oldPiece=chessBoard[r][c+temp*j];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r][c+temp*j]="r";
					if (blackKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[(c+temp*j)]+displayR+oldPiece+" ";
					}
					chessBoard[r][c]="r";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r][c+temp*j]=oldPiece;
				}
			} catch (Exception e) {}
			temp=1;
			try {
				while(" ".equals(chessBoard[r+temp*j][c]))
				{
					oldPiece=chessBoard[r+temp*j][c];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r+temp*j][c]="r";
					if (blackKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[c]+(displayR-temp*j)+oldPiece+" ";
					}
					chessBoard[r][c]="r";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r+temp*j][c]=oldPiece;
					temp++;
				}
				if (Character.isUpperCase(chessBoard[r+temp*j][c].charAt(0))) {
					oldPiece=chessBoard[r+temp*j][c];
					if(oldPiece.equals(" ")) oldPiece = "";
					chessBoard[r][c]=" ";
					chessBoard[r+temp*j][c]="r";
					if (blackKingSafe()) {
						moves=moves+colNames[c]+displayR+colNames[c]+(displayR-temp*j)+oldPiece+" ";
					}
					chessBoard[r][c]="r";
					if(oldPiece.equals("")) oldPiece = " ";
					chessBoard[r+temp*j][c]=oldPiece;
				}
			} catch (Exception e) {}
			temp=1;
		}
		return moves;
	}


	public static String legalBN(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		for (int j=-1; j<=1; j+=2) {
			for (int k=-1; k<=1; k+=2) {
				try {
					if (Character.isUpperCase(chessBoard[r+j][c+k*2].charAt(0)) || " ".equals(chessBoard[r+j][c+k*2])) {
						oldPiece=chessBoard[r+j][c+k*2];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+j][c+k*2]="n";
						if (blackKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+k*2)]+(displayR-j)+oldPiece+" ";
						}
						chessBoard[r][c]="n";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+j][c+k*2]=oldPiece;
					}
				} catch (Exception e) {}
				try {
					if (Character.isUpperCase(chessBoard[r+j*2][c+k].charAt(0)) || " ".equals(chessBoard[r+j*2][c+k])) {
						oldPiece=chessBoard[r+j*2][c+k];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+j*2][c+k]="n";
						if (blackKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+k)]+(displayR-j*2)+oldPiece+" ";
						}
						chessBoard[r][c]="n";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+j*2][c+k]=oldPiece;
					}
				} catch (Exception e) {}
			}
		}
		return moves;
	}


	public static String legalBB(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		int temp=1;
		for (int j=-1; j<=1; j+=2) {
			for (int k=-1; k<=1; k+=2) {
				try {
					while(" ".equals(chessBoard[r+temp*j][c+temp*k]))
					{
						oldPiece=chessBoard[r+temp*j][c+temp*k];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+temp*j][c+temp*k]="b";
						if (blackKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
						}
						chessBoard[r][c]="b";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+temp*j][c+temp*k]=oldPiece;
						temp++;
					}
					if (Character.isUpperCase(chessBoard[r+temp*j][c+temp*k].charAt(0))) {
						oldPiece=chessBoard[r+temp*j][c+temp*k];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r+temp*j][c+temp*k]="b";
						if (blackKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
						}
						chessBoard[r][c]="b";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r+temp*j][c+temp*k]=oldPiece;
					}
				} catch (Exception e) {}
				temp=1;
			}
		}
		return moves;
	}


	public static String legalBQ(int i) {
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		int temp=1;
		for (int j=-1; j<=1; j++) {
			for (int k=-1; k<=1; k++) {
				if (j!=0 || k!=0) {
					try {
						while(" ".equals(chessBoard[r+temp*j][c+temp*k]))
						{
							oldPiece=chessBoard[r+temp*j][c+temp*k];
							if (oldPiece.equals(" ")) oldPiece="";
							chessBoard[r][c]=" ";
							chessBoard[r+temp*j][c+temp*k]="q";
							if (blackKingSafe()) {
								moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
							}
							chessBoard[r][c]="q";
							if (oldPiece.equals("")) oldPiece=" ";
							chessBoard[r+temp*j][c+temp*k]=oldPiece;
							temp++;
						}
						if (Character.isUpperCase(chessBoard[r+temp*j][c+temp*k].charAt(0))) {
							oldPiece=chessBoard[r+temp*j][c+temp*k];
							if (oldPiece.equals(" ")) oldPiece="";
							chessBoard[r][c]=" ";
							chessBoard[r+temp*j][c+temp*k]="q";
							if (blackKingSafe()) {
								moves=moves+colNames[c]+displayR+colNames[(c+temp*k)]+(displayR-temp*j)+oldPiece+" ";
							}
							chessBoard[r][c]="q";
							if (oldPiece.equals("")) oldPiece=" ";
							chessBoard[r+temp*j][c+temp*k]=oldPiece;
						}
					} catch (Exception e) {}
					temp=1;
				}
			}
		}
		return moves;
	}
	public static String legalBK(int i) {
		int blackKingPos = findBlackKing();
		String moves="", oldPiece;
		int r=i/8, c=i%8;
		int displayR = 8-r;
		if (chessBoard[0][4].equals("k") && chessBoard[0][7].equals("r") && chessBoard[0][5].equals(" ") && chessBoard[0][6].equals(" ")) {
			if (blackKingMoved==0 && kbRookMoved==0) {
				chessBoard[0][4] = " ";
				chessBoard[0][7] = " ";
				chessBoard[0][5] = "r";
				chessBoard[0][6] = "k";
				if (blackKingSafe()) {
					moves=moves+"O-O ";
				}
				chessBoard[0][5] = " ";
				chessBoard[0][6] = " ";
				chessBoard[0][7] = "r";
				chessBoard[0][4] = "k";
			}
		}
		if (chessBoard[0][4].equals("K") && chessBoard[0][0].equals("R") && chessBoard[0][1].equals(" ") && chessBoard[0][2].equals(" ") && chessBoard[0][3].equals(" ")) {
			if (whiteKingMoved==0 && qwRookMoved==0) {
				chessBoard[0][4] = " ";
				chessBoard[0][0] = " ";
				chessBoard[0][3] = "r";
				chessBoard[0][2] = "k";
				if (whiteKingSafe()) {
					moves=moves+"O-O-O ";
				}
				chessBoard[0][4] = "k";
				chessBoard[0][0] = "r";
				chessBoard[0][3] = " ";
				chessBoard[0][2] = " ";
			}
		}
		for (int j=0; j<9; j++) {
			if (j!=4) {
				try {
					if (Character.isUpperCase(chessBoard[r-1+j/3][c-1+j%3].charAt(0)) || " ".equals(chessBoard[r-1+j/3][c-1+j%3])) {
						oldPiece=chessBoard[r-1+j/3][c-1+j%3];
						if (oldPiece.equals(" ")) oldPiece="";
						chessBoard[r][c]=" ";
						chessBoard[r-1+j/3][c-1+j%3]="k";
						int kingTemp=blackKingPos;
						blackKingPos=i+(j/3)*8+j%3-9;
						if (blackKingSafe()) {
							moves=moves+colNames[c]+displayR+colNames[(c-1+j%3)]+(displayR+1-j/3)+oldPiece+" ";
						}
						chessBoard[r][c]="k";
						if (oldPiece.equals("")) oldPiece=" ";
						chessBoard[r-1+j/3][c-1+j%3]=oldPiece;
						blackKingPos=kingTemp;
					}
				} catch (Exception e) {}
			}
		}
		//need to add casting later
		return moves;
	}
	public static boolean blackKingSafe() {
		int blackKingPos = findBlackKing();
		//bishop/queen
		int temp=1;
		for (int i=-1; i<=1; i+=2) {
			for (int j=-1; j<=1; j+=2) {
				try {
					while(" ".equals(chessBoard[blackKingPos/8+temp*i][blackKingPos%8+temp*j])) {temp++;}
					if ("B".equals(chessBoard[blackKingPos/8+temp*i][blackKingPos%8+temp*j]) ||
							"Q".equals(chessBoard[blackKingPos/8+temp*i][blackKingPos%8+temp*j])) {
						return false;
					}
				} catch (Exception e) {}
				temp=1;
			}
		}
		//rook/queen
		for (int i=-1; i<=1; i+=2) {
			try {
				while(" ".equals(chessBoard[blackKingPos/8][blackKingPos%8+temp*i])) {temp++;}
				if ("R".equals(chessBoard[blackKingPos/8][blackKingPos%8+temp*i]) ||
						"Q".equals(chessBoard[blackKingPos/8][blackKingPos%8+temp*i])) {
					return false;
				}
			} catch (Exception e) {}
			temp=1;
			try {
				while(" ".equals(chessBoard[blackKingPos/8+temp*i][blackKingPos%8])) {temp++;}
				if ("R".equals(chessBoard[blackKingPos/8+temp*i][blackKingPos%8]) ||
						"Q".equals(chessBoard[blackKingPos/8+temp*i][blackKingPos%8])) {
					return false;
				}
			} catch (Exception e) {}
			temp=1;
		}
		//knight
		for (int i=-1; i<=1; i+=2) {
			for (int j=-1; j<=1; j+=2) {
				try {
					if ("N".equals(chessBoard[blackKingPos/8+i][blackKingPos%8+j*2])) {
						return false;
					}
				} catch (Exception e) {}
				try {
					if ("N".equals(chessBoard[blackKingPos/8+i*2][blackKingPos%8+j])) {
						return false;
					}
				} catch (Exception e) {}
			}
		}
		//pawn
		if (blackKingPos<=48) {
			try {
				if ("P".equals(chessBoard[blackKingPos/8+1][blackKingPos%8-1])) { 
					return false;
				}
			} catch (Exception e) {}
			try {
				if ("P".equals(chessBoard[blackKingPos/8+1][blackKingPos%8+1])) {
					return false;
				}
			} catch (Exception e) {}
			//king
			for (int i=-1; i<=1; i++) {
				for (int j=-1; j<=1; j++) {
					if (i!=0 || j!=0) {
						try {
							if ("K".equals(chessBoard[blackKingPos/8+i][blackKingPos%8+j])) {
								return false;
							}
						} catch (Exception e) {}
					}
				}
			}
		}
		return true;
	}
}
