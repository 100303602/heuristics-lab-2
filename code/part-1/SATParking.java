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

	// Class representing a car object. It is used to set up the problem for JaCoP.
	public static class Car {
		// attributes
		private char category;
		private int arrivalTime;
		public static HashMap<Character,Float> waitingTimes;

		// constructor
		public Car (char category, int arrivalTime) {
			this.category    = category;
			this.arrivalTime = arrivalTime;

			// this hashmap allows to easily add new categories and / or changing waiting times for a given category.
			this.waitingTimes = new HashMap<Character, Float>();
			this.waitingTimes.put('A', 0.5f);
			this.waitingTimes.put('B', 2f);
			this.waitingTimes.put('C', 3f);
			this.waitingTimes.put('_', -1f);
		}

		// getters and setters
		public char getCategory () { return this.category; }
		public float getWaitingTime () { return this.waitingTimes.get(this.category); }
		public int getArrivalTime () { return this.arrivalTime; }
	}

	public static void main(String args[]) {
		// name of the .input file to load
		String inputFileName = args[0];
		// number of parking lanes
		int M = 0;
		// number of parking positions within a given parking lane
		int N = 0;
		Car [][] cars = null;

		// convenience variables for representing left and right directions
		final int left  = 0;
		final int right = 1;

		// load parking lot data from the .input file
		try {
			FileReader fileReader = new FileReader(inputFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String header = bufferedReader.readLine();
			M = Integer.parseInt(header.split(" ")[0]);
			N = Integer.parseInt(header.split(" ")[1]);
			cars = new Car[M][N];

			for (int i=0; i<M; i++) {
				String[] line = bufferedReader.readLine().split(" ");
				for (int j=0; j<line.length; j++)
					cars[i][j] = new Car(line[j].charAt(0), Character.getNumericValue(line[j].charAt(1)));
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
		// current parking position contains a car (that is, it's not empty)
		BooleanVar[][] isCar = new BooleanVar[M][N];

		// create description strings for the boolean variables
		String variableDescription;
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) {
			variableDescription = "(" + i + "," + j + ")";
			isCar[i][j]                = new BooleanVar(store, "c" + variableDescription);
			for (int k=left; k<=right; k++) {
				variableDescription += (k==left ? "L" : "R");
				before[i][j][k]            = new BooleanVar(store, "b" + variableDescription);
				sameCategory[i][j][k]      = new BooleanVar(store, "s" + variableDescription);
				lowerWaitingTime[i][j][k]  = new BooleanVar(store, "l" + variableDescription);
			}
		}

		// Collect all the variables for the SimpleSelect
		BooleanVar[] allVariables = new BooleanVar[(M*(N-2)*7)];
		int m = 0;
		for (int k=left; k<=right; k++) for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = before[i][j][k];
		for (int k=left; k<=right; k++) for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = sameCategory[i][j][k];
		for (int k=left; k<=right; k++) for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = lowerWaitingTime[i][j][k];
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++, m++) allVariables[m] = isCar[i][j];

		// Register all the variables in the SatWrapper
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) {
			satWrapper.register(isCar[i][j]);
			for (int k=left; k<=right; k++) {
				satWrapper.register(before[i][j][k]);
				satWrapper.register(sameCategory[i][j][k]);
				satWrapper.register(lowerWaitingTime[i][j][k]);
			}
		}

		// Obtain non-negated literals out of the binary variables
		int[][][] lowerWaitingTimeLiterals = new int[M][N][2];
		int[][][] sameCategoryLiterals     = new int[M][N][2];
		int[][][] beforeLiterals           = new int[M][N][2];
		int[][]   isCarLiterals            = new int[M][N];

		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) if(cars[i][j] != null) {
			isCarLiterals[i][j] = satWrapper.cpVarToBoolVar(isCar[i][j], 1, true);
			for (int k=left; k<=right; k++) {
				beforeLiterals[i][j][k]           = satWrapper.cpVarToBoolVar(before[i][j][k], 1, true);
				sameCategoryLiterals[i][j][k]     = satWrapper.cpVarToBoolVar(sameCategory[i][j][k], 1, true);
				lowerWaitingTimeLiterals[i][j][k] = satWrapper.cpVarToBoolVar(lowerWaitingTime[i][j][k], 1, true);
			}
		}

		// add unitary clauses representing parking lot information
		int currentCategory, currentArrivalTime, adjacentCategory, adjacentArrivalTime;
		float currentWaitingTime, adjacentWaitingTime;
		int side = 0;
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) {
			currentCategory     = cars[i][j].getCategory();
			currentWaitingTime  = cars[i][j].getWaitingTime();
			currentArrivalTime  = cars[i][j].getArrivalTime();
			if (currentCategory != '_')
				addClause(satWrapper, isCarLiterals[i][j]);
			else
				addClause(satWrapper, -isCarLiterals[i][j]);
			for (int k=left; k<=right; k++) {
				if (k == left)
					side = -1;
				else
					side = 1;
				adjacentCategory    = cars[i][j+side].getCategory();
				adjacentWaitingTime = cars[i][j+side].getWaitingTime();
				adjacentArrivalTime = cars[i][j+side].getArrivalTime();
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
		}

		// Add clauses that check for blocked cars
		for (int i=0; i<M; i++) for (int j=1; j<N-1; j++) for (int k=left; k<=right; k++) {
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], sameCategoryLiterals[i][j][0], sameCategoryLiterals[i][j][1]);
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], sameCategoryLiterals[i][j][0], beforeLiterals[i][j][1]);
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], sameCategoryLiterals[i][j][1], beforeLiterals[i][j][0]);
			addClause(satWrapper, lowerWaitingTimeLiterals[i][j][0], lowerWaitingTimeLiterals[i][j][1], beforeLiterals[i][j][0], beforeLiterals[i][j][1]);
		}

		// Solve the problem and measure execution time
		long startTime = System.currentTimeMillis();
		Search<BooleanVar> search            = new DepthFirstSearch<BooleanVar>();
		SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(allVariables, new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());
		long endTime = System.currentTimeMillis();
		Boolean result                       = search.labeling(store, select);

		System.out.println(result ? "No cars are blocked." : "One or more cars are bloqued.");

		// generate output file with cars exit directions
		if (result) {
			BufferedWriter out = null;
			try {
				FileWriter fstream = new FileWriter(args[0].substring(0, args[0].length() - ".input".length()) + ".output");
				out = new BufferedWriter(fstream);
				for (int i=0; i<M; i++) {
					for (int j=0; j<N; j++) {
						if (cars[i][j].getCategory() != '_') {
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

		// extra information
		System.out.println("M:" + M + ", N:" + N + ", size:" + M*N);
		long totalTime = endTime - startTime;
		System.out.println("Execution time (s): " + (totalTime/1000.0));
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
