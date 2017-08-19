import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;


public class homework {
	
	static class Game {
		public enum Mode { MINIMAX, ALPHABETA, COMPETITION }
		
		public int size;
		public Mode mode;
		public char you;
		public int maxDepth;
		public int[][] cellValues;
		public char[][] initialState;		
	}
	
	static class Action {
		public enum TYPE { STAKE, RAID }
		
		public int row;
		public int col;
		public TYPE type;
		
		public Action(int row, int col, TYPE type) {
			this.row = row;
			this.col = col;
			this.type = type;
		}
		
		public Action() { }
	}
	
	/*-----------------------------------initGame()-----------------------------------*/
	
	private static void initGame(Game game) throws FileNotFoundException {
		File inputFile = new File("TestCasesHW2/Test10/input.txt");
		Scanner in = new Scanner(inputFile);
		try {
			game.size = in.nextInt();
			in.nextLine();
			String line = in.nextLine();
			game.mode = line.equals("MINIMAX") ? Game.Mode.MINIMAX : (line.equals("ALPHABETA") ? Game.Mode.ALPHABETA : Game.Mode.COMPETITION);
			game.you = in.nextLine().charAt(0) == 'O' ? 'O' : 'X';
			game.maxDepth = in.nextInt();
			in.nextLine();
			game.cellValues = new int[game.size][game.size];
			for (int i = 0; i < game.size; i++) {
				for (int j = 0; j < game.size; j++) {
					game.cellValues[i][j] = in.nextInt();
				}
				in.nextLine();
			}
			game.initialState = new char[game.size][game.size];
			for (int i = 0; i < game.size; i++) {
				line = in.nextLine();
				for (int j = 0; j < game.size; j++) {
					game.initialState[i][j] = line.charAt(j);
				}
			}			
		} finally {
			in.close();
		}		
	}
	
	/*-----------------------------------minimaxDecision()-----------------------------------*/
	
	private static boolean isTerminal(char[][] state, Game game) {
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (state[i][j] == '.') {
					return false;
				}
			}
		}
		return true;
	}	
	
	private static boolean isCutOff(char[][] state, Game game, int currDepth) {
		if (currDepth >= game.maxDepth || isTerminal(state, game)) {
			return true;
		}
		return false;
	}
	
	private static int calculateScore(char[][] state, Game game) {
		int yourScore = 0, otherScore = 0;
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				if (state[i][j] != '.') {
					if (state[i][j] == game.you) {
						yourScore += game.cellValues[i][j];
					} else {
						otherScore += game.cellValues[i][j];
					}
				}
			}
		}
		return yourScore - otherScore;
	}
	
	private static boolean isValidStake(char[][] state, int row, int col) {
		return state[row][col] == '.';
	}

	private static char[][] stake(char[][] state, int row, int col, char currPlayer) {
		char[][] newState = new char[state.length][state[0].length];
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				newState[i][j] = state[i][j];
			}
		}
		newState[row][col] = currPlayer;		
		return newState;
	}	
	
	private static boolean isValidRaid(char[][] state, int row, int col, char currPlayer) {
		if (state[row][col] == '.') {
			if ((row > 0 && state[row - 1][col] == currPlayer) || 
				(row < state.length - 1 && state[row + 1][col] == currPlayer) || 
				(col > 0 && state[row][col - 1] == currPlayer) || 
				(col < state[0].length - 1 && state[row][col + 1] == currPlayer)) { 
				return true;
			}
		}
		return false;
	}
	
	private static char[][] raid(char[][] state, int row, int col, char currPlayer) {
		char[][] newState = new char[state.length][state[0].length];
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				newState[i][j] = state[i][j];
			}
		}
		newState[row][col] = currPlayer;
		char other = currPlayer == 'O' ? 'X' : 'O';
		if (row > 0 && state[row - 1][col] == other) {
			newState[row - 1][col] = currPlayer;
		}
		if (row < state.length - 1 && state[row + 1][col] == other) {
			newState[row + 1][col] = currPlayer;
		}
		if (col > 0 && state[row][col - 1] == other) {
			newState[row][col - 1] = currPlayer;
		}
		if (col < state[0].length - 1 && state[row][col + 1] == other) {
			newState[row][col + 1] = currPlayer;
		}
		return newState;
	}
	
	
	private static int maxValue(char[][] state, Game game, int alpha, int beta, int currDepth, char currPlayer) {
		if (isCutOff(state, game, currDepth)) {
			return calculateScore(state, game);
		}
		int value = Integer.MIN_VALUE;
		char other = currPlayer == 'O' ? 'X' : 'O';
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (isValidStake(state, i, j)) {
					value = Math.max(value, minValue(stake(state, i, j, currPlayer), game, alpha, beta, currDepth + 1, other));
				}
				if (game.mode == Game.Mode.ALPHABETA) {
					if (value >= beta) {
						return value;
					}
					alpha = Math.max(alpha, value);
				}				
			}
		}
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (isValidRaid(state, i, j, currPlayer)) {
					value = Math.max(value, minValue(raid(state, i, j, currPlayer), game, alpha, beta, currDepth + 1, other));
				}
				if (game.mode == Game.Mode.ALPHABETA) {
					if (value >= beta) {
						return value;
					}
					alpha = Math.max(alpha, value);
				}				
			}
		}		
		return value;
	}
	
	private static int minValue(char[][] state, Game game, int alpha, int beta, int currDepth, char currPlayer) {
		if (isCutOff(state, game, currDepth)) {
			return calculateScore(state, game);
		}
		int value = Integer.MAX_VALUE;
		char other = currPlayer == 'O' ? 'X' : 'O';
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (isValidStake(state, i, j)) {
					value = Math.min(value, maxValue(stake(state, i, j, currPlayer), game, alpha, beta, currDepth + 1, other));
				}
				if (game.mode == Game.Mode.ALPHABETA) {
					if (value <= alpha) {
						return value;
					}
					beta = Math.min(beta, value);
				}				
			}
		}
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (isValidRaid(state, i, j, currPlayer)) {
					value = Math.min(value, maxValue(raid(state, i, j, currPlayer), game, alpha, beta, currDepth + 1, other));
				}
				if (game.mode == Game.Mode.ALPHABETA) {
					if (value <= alpha) {
						return value;
					}
					beta = Math.min(beta, value);
				}				
			}
		}		
		return value;
	}
	
	private static Action minimaxDecision(Game game) {
		int max = Integer.MIN_VALUE;
		Action bestMove = null;
		char other = game.you == 'O' ? 'X' : 'O';
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (isValidStake(game.initialState, i, j)) {
					int value = minValue(stake(game.initialState, i, j, game.you), game, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, other);
					if (value > max) {
						max = value;
						bestMove = new Action(i, j, Action.TYPE.STAKE);
					}
				}
			}
		}
		for (int i = 0; i < game.size; i++) {
			for (int j = 0; j < game.size; j++) {
				if (isValidRaid(game.initialState, i, j, game.you)) {
					int value = minValue(raid(game.initialState, i, j, game.you), game, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, other);
					if (value > max) {
						max = value;
						bestMove = new Action(i, j, Action.TYPE.RAID);
					}
				}
			}
		}
		return bestMove;		
	}
	
	/*-----------------------------------outputResult()-----------------------------------*/
	
	private static void printMatrix(PrintWriter out, char[][] state) {
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				out.print(state[i][j]);
			}
			if (i < state.length - 1) {
				out.println();
			}
		}
	}

	private static void outputResult(Game game, Action move) throws FileNotFoundException {
		PrintWriter out = new PrintWriter("TestCasesHW2/Test10/myOutput.txt");
		try {
			out.print((char)('A' + move.col));
			out.print((move.row + 1) + " ");
			String moveType = move.type == Action.TYPE.STAKE ? "Stake" : "Raid";
			out.println(moveType);
			if (move.type == Action.TYPE.STAKE) {
				printMatrix(out, stake(game.initialState, move.row, move.col, game.you));
			} else {
				printMatrix(out, raid(game.initialState, move.row, move.col, game.you));
			}
		} finally {
			out.close();
		}
		
	}
	
	/*-----------------------------------main()-----------------------------------*/
	
	public static void main(String[] args) {
		try {
			Game game = new Game();
			initGame(game);
			Action bestMove = minimaxDecision(game);
			outputResult(game, bestMove);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Input file not found or Can't open output file.");
		}
	}
}