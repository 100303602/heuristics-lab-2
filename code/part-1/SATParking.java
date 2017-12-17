import java.io.*;
import java.util.HashMap;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jacop.core.BooleanVar;
import org.jacop.core.Store;
import org.jacop.jasat.utils.structures.IntVec;
import org.jacop.satwrapper.SatWrapper;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;


public class SATParking {
	public static class Car {
		private char category;
		private int arrivalTime;
		public static HashMap<Character,Float> waitingTimes;

		public Car (char category, int arrivalTime) {
			this.category    = category;
			this.arrivalTime = arrivalTime;
			this.waitingTimes = new HashMap<Character, Float>();
			this.waitingTimes.put('A', 0.5f);
			this.waitingTimes.put('B', 2f);
			this.waitingTimes.put('C', 3f);
			this.waitingTimes.put('_', -1f);
		}

		public char getCategory () { return this.category; }

		public float getWaitingTime () { return this.waitingTimes.get(this.category); }

		public int getArrivalTime () { return this.arrivalTime; }
	}

	public static void main(String args[]) {
		String inputFileName = args[0];
		int M = 0;
		int N = 0;
		Car [][] board = null;

		final int left  = 0;
		final int right = 1;

		try {
			FileReader fileReader = new FileReader(inputFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String header = bufferedReader.readLine();
			M = Integer.parseInt(header.split(" ")[0]);
			N = Integer.parseInt(header.split(" ")[1]);
			System.out.println("M:" + M + ", N:" + N + "\n");
			board = new Car[M][N];

			for (int i=0; i<M; i++) {
				String[] line = bufferedReader.readLine().split(" ");
				for (int j=0; j<line.length; j++)
					board[i][j] = new Car(line[j].charAt(0), Character.getNumericValue(line[j].charAt(1)));
			}
		}
		catch (FileNotFoundException ex) { System.out.println("File not found: '"     + inputFileName + "'"); }
		catch (IOException ex)           { System.out.println("Error reading file: '" + inputFileName + "'"); }

		// Create the store and the satWrapper to encode the problem
		Store store = new Store();
		SatWrapper satWrapper = new SatWrapper();
		store.impose(satWrapper);

		// Create the binary variables that represent the presence of cars in map
		// adjacent car entered before current car
		BooleanVar[][][] before        = new BooleanVar[M][N][2];
		// adjacent car has the same category as the current car
		BooleanVar[][][] sameCategory  = new BooleanVar[M][N][2];
		// adjcent car's category has lower waiting time than the current car's category
		BooleanVar[][][] lowerWaitingTime = new BooleanVar[M][N][2];

		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) for (int k=left; k<=right; k++) {
			String variableDescription = "car" + (k==left ? "Left" : "Right") + "To(" + i + "," + j +")";
			before[i][j][k]            = new BooleanVar(store, variableDescription + "EnteredTheLaneBefore");
			sameCategory[i][j][k]      = new BooleanVar(store, variableDescription + "HasTheSameCategory");
			lowerWaitingTime[i][j][k]  = new BooleanVar(store, variableDescription + "HasLowerWaitingTime");
		}

		// Collect all the variables for the SimpleSelect
		BooleanVar[] allVariables = new BooleanVar[(M*(N-2)*6)];
		int m = 0;
		for (int k=left; k<=right; k++) for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = before[i][j][k];
		for (int k=left; k<=right; k++) for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = sameCategory[i][j][k];
		for (int k=left; k<=right; k++) for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = lowerWaitingTime[i][j][k];

		// Register all the variables in the SatWrapper
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) for (int k=left; k<=right; k++) {
			satWrapper.register(before[i][j][k]);
			satWrapper.register(sameCategory[i][j][k]);
			satWrapper.register(lowerWaitingTime[i][j][k]);
		}

		// Obtain non-negated literals out of the binary variables
		int[][][] lowerWaitingTimeLiterals = new int[M][N][2];
		int[][][] sameCategoryLiterals     = new int[M][N][2];
		int[][][] beforeLiterals           = new int[M][N][2];

		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) if(board[i][j] != null) for (int k=left; k<=right; k++) {
			beforeLiterals[i][j][k]           = satWrapper.cpVarToBoolVar(before[i][j][k], 1, true);
			sameCategoryLiterals[i][j][k]     = satWrapper.cpVarToBoolVar(sameCategory[i][j][k], 1, true);
			lowerWaitingTimeLiterals[i][j][k] = satWrapper.cpVarToBoolVar(lowerWaitingTime[i][j][k], 1, true);
		}

		int currentCategory, currentArrivalTime, adjacentCategory, adjacentArrivalTime;
		float currentWaitingTime, adjacentWaitingTime;
		int side = 0;
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) for (int k=left; k<=right; k++) {
			if (k == left)
				side = -1;
			else
				side = 1;
			currentCategory     = board[i][j].getCategory();
			currentWaitingTime  = board[i][j].getWaitingTime();
			currentArrivalTime  = board[i][j].getArrivalTime();
			adjacentCategory    = board[i][j+side].getCategory();
			adjacentWaitingTime = board[i][j+side].getWaitingTime();
			adjacentArrivalTime = board[i][j+side].getArrivalTime();
			if (adjacentCategory != '_') {
				if (adjacentCategory == currentCategory) {
					addClause(satWrapper, sameCategoryLiterals[i][j][k]);
					addClause(satWrapper, -lowerWaitingTimeLiterals[i][j][k]);
				}
				else {
					addClause(satWrapper, -sameCategoryLiterals[i][j][k]);
					if (adjacentWaitingTime < currentWaitingTime)
						addClause(satWrapper, lowerWaitingTimeLiterals[i][j][k]);
					else
						addClause(satWrapper, -lowerWaitingTimeLiterals[i][j][k]);
				}
				if (adjacentArrivalTime < currentArrivalTime)
					addClause(satWrapper, beforeLiterals[i][j][k]);
				else
					addClause(satWrapper, -beforeLiterals[i][j][k]);
			}
			else {
				addClause(satWrapper, lowerWaitingTimeLiterals[i][j][k]);
				addClause(satWrapper, sameCategoryLiterals[i][j][k]);
				addClause(satWrapper, beforeLiterals[i][j][k]);
			}
		}

		// Add all clauses
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) for (int k=left; k<=right; k++) {
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], sameCategoryLiterals[i][j][0], sameCategoryLiterals[i][j][1]);
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], sameCategoryLiterals[i][j][0], beforeLiterals[i][j][1]);
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], sameCategoryLiterals[i][j][1], beforeLiterals[i][j][0]);
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], beforeLiterals[i][j][0], beforeLiterals[i][j][1]);
		}

		// Solve the problem
		Search<BooleanVar> search            = new DepthFirstSearch<BooleanVar>();
		SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(allVariables, new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());
		Boolean result                       = search.labeling(store, select);

		System.out.println(result ? "No cars are blocked." : "One or more cars are bloqued.");

		if (result) {
			BufferedWriter out = null;
			try {
				FileWriter fstream = new FileWriter(args[0].substring(0, args[0].length() - ".input".length()) + "_parte1.output");
				out = new BufferedWriter(fstream);
				for (int i=0; i<M; i++) {
					for (int j=0; j<N; j++) {
						if (board[i][j].getCategory() != '_') {
							if (j == 0)
								out.write('<');
							else if (j == N-1)
								out.write('>');
							else {
								if ((lowerWaitingTime[i][j][left].dom().value() == 1) || (sameCategory[i][j][left].dom().value() == 1 && before[i][j][left].value() == 1))
									out.write('<');
								else if ((lowerWaitingTime[i][j][right].dom().value() == 1) || (sameCategory[i][j][right].dom().value() == 1 && before[i][j][right].value() == 1))
									out.write('>');
							}
						}
						else out.write('_');
					}
					out.write('\n');
				}
				out.close();
				System.out.println("A .output file with escape directions for cars has been generated.");
			}
			catch (IOException e) {System.err.println("Error: " + e.getMessage());}
		}
		else System.out.println("No .output file has been generated as there was one or more cars blocked.");
		System.out.println();
	}

	// Adds a clause of 2 literals
	public static void addClause(SatWrapper satWrapper, int literal1){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		satWrapper.addModelClause(clause.toArray());
	}

	// Adds a clause of 4 literals
	public static void addClause (SatWrapper satWrapper, int literal1, int literal2, int literal3, int literal4) {
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		clause.add(literal2);
		clause.add(literal3);
		clause.add(literal4);
		satWrapper.addModelClause(clause.toArray());
	}
}
