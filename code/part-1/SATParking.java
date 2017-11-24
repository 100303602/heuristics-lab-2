import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

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
	class Car {
		private char category;
		private int order;

		public Car(char category, int order){
			this.category = category;
			this.order = order;
		}

		public void setCategory(char category){
			this.category = category;
		}

		public void setOrder(int order){
			this.order = order;
		}

		public char getCategory(){
			return this.category;
		}

		public int getOrder(){
			return this.order;
		}
	}

	public static void main(String args[]){
		String fileName = args[0];
		final int M, N;
		SATParking.Car [][] board;

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String header = bufferedReader.readLine();
			M = Integer.parseInt(header.split(" ")[0]);
			N = Integer.parseInt(header.split(" ")[1]);
			board = new SATParking.Car[M][N];
			
			System.out.println(M + " " + N);
			for(int i=0; i<M; i++) {
				String line = bufferedReader.readLine();
				for(int j=0; j<N; j++) {
					board[i][j] = new SATParking.Car(line.split(" ")[j].charAt(0), Character.getNumericValue(line.split(" ")[j].charAt(1)));
				}
			}
			for(int i=0; i<M; i++) {
				for(int j=0; j<N; j++) {
					System.out.print(board[i][j]);
				}
			}
		} catch(FileNotFoundException ex) {
			System.out.println("File not found: '" + fileName + "'");
		} catch(IOException ex) {
			System.out.println("Error reading file: '" + fileName + "'");
		}

		// Create the store and the satWrapper to encode the problem
		Store store = new Store();
		SatWrapper satWrapper = new SatWrapper();
		store.impose(satWrapper);

		// Create the binary variables that represent the presence of cars in map
		BooleanVar[][] parkingLot = new BooleanVar[M][N];
		BooleanVar[][] catA = new BooleanVar[M][N];
		BooleanVar[][] catB = new BooleanVar[M][N];
		BooleanVar[][] catC = new BooleanVar[M][N];
		BooleanVar[][][] adjCars = new BooleanVar[M][N][2];

		for(int i=0; i<M; i++) {
			for(int j=0; j<N; j++) {
				parkingLot[i][j] = new BooleanVar(store, "Car(" + i + "," + j + ")");
				catA[i][j] = new BooleanVar(store, "A(" + i + "," + j + ")");
				catB[i][j] = new BooleanVar(store, "B(" + i + "," + j + ")");
				catC[i][j] = new BooleanVar(store, "C(" + i + "," + j + ")");
				for (int k=0; k<2; k++) {
					adjCars[i][j][k] = new BooleanVar(store, "Car(" + i + "," + j + ")" + k);
				}
			}
		}

		// Collect all the variables for the SimpleSelect
		BooleanVar[] allVariables = new BooleanVar[(M*N*4)];
		for (int i=0; i<(M*N*4); i++) {
			for(int j=0; j<M; j++) {
				for(int k=0; k<N; k++) {
					allVariables[i] = parkingLot[j][k];
				}
			}
			for(int j=0; j<M; j++) {
				for(int k=0; k<N; k++) {
					allVariables[i] = catA[j][k];
				}
			}
			for(int j=0; j<M; j++) {
				for(int k=0; k<N; k++) {
					allVariables[i] = catB[j][k];
				}
			}
			for(int j=0; j<M; j++) {
				for(int k=0; k<N; k++) {
					allVariables[i] = catC[j][k];
				}
			}
			for(int j=0; j<M; j++) {
				for(int k=0; k<N; k++) {
					for(int l=0; l<2; l++) {
						allVariables[i] = adjCars[j][k][l];
					}
				}
			}
		}

		// Register all the variables in the SatWrapper
		for(int i=0; i<M; i++) {
			for(int j=0; j<N; j++) {
				satWrapper.register(parkingLot[i][j]);
				satWrapper.register(catA[i][j]);
				satWrapper.register(catB[i][j]);
				satWrapper.register(catC[i][j]);
				for(int k=0; k<2; k++) {
					satWrapper.register(adjCars[i][j][k]);
				}
			}
		}

		// Obtain non-negated literals out of the binary variables
		int[][] carLiterals  = new int[M][N];
		int[][] catALiterals = new int[M][N];
		int[][] catBLiterals = new int[M][N];
		int[][] catCLiterals = new int[M][N];
		int[][][] adjCarsLiterals = new int[M][N][2];

		for (int i=0; i<M; i++) {
			for (int j=0; j<N; j++) {
				if (board[i][j] != null) {

					if(board[i][j].getOrder() < board[i][j+1].getOrder()) {
						adjCarsLiterals[i][j][1] = satWrapper.cpVarToBoolVar(adjCars[i][j][1], 1, true);
					} else {
						adjCarsLiterals[i][j][1] = satWrapper.cpVarToBoolVar(adjCars[i][j][1], 1, false);
					}

					if(board[i][j].getOrder() < board[i][j-1].getOrder()) {
						adjCarsLiterals[i][j][0] = satWrapper.cpVarToBoolVar(adjCars[i][j][0], 1, true);
					} else {
						adjCarsLiterals[i][j][0] = satWrapper.cpVarToBoolVar(adjCars[i][j][0], 1, false);
					}

					carLiterals[i][j] = satWrapper.cpVarToBoolVar(parkingLot[i][j], 1, true);
					switch (board[i][j].getCategory()) {
						case 'A':
							catALiterals[i][j] = satWrapper.cpVarToBoolVar(catA[i][j], 1, true);
							catBLiterals[i][j] = satWrapper.cpVarToBoolVar(catB[i][j], 1, false);
							catCLiterals[i][j] = satWrapper.cpVarToBoolVar(catC[i][j], 1, false);
							break;
						case 'B':
							catBLiterals[i][j] = satWrapper.cpVarToBoolVar(catB[i][j], 1, true);
							catALiterals[i][j] = satWrapper.cpVarToBoolVar(catB[i][j], 1, false);
							catCLiterals[i][j] = satWrapper.cpVarToBoolVar(catC[i][j], 1, false);
							break;
						case 'C':
							catCLiterals[i][j] = satWrapper.cpVarToBoolVar(catC[i][j], 1, true);
							catALiterals[i][j] = satWrapper.cpVarToBoolVar(catB[i][j], 1, false);
							catBLiterals[i][j] = satWrapper.cpVarToBoolVar(catC[i][j], 1, false);
							break;
						default:
							break;
					}
				} else {
					carLiterals[i][j] = satWrapper.cpVarToBoolVar(parkingLot[i][j], 1, false);
					catALiterals[i][j] = satWrapper.cpVarToBoolVar(catA[i][j], 1, false);
					catBLiterals[i][j] = satWrapper.cpVarToBoolVar(catB[i][j], 1, false);
					catCLiterals[i][j] = satWrapper.cpVarToBoolVar(catC[i][j], 1, false);
				}
			}
		}



		/* The problem will be defined in CNF form, thus, every clause
		* of the problem will be added one by one.
		* Each clause will consist in the disjunction of a set of the
		* previously defined literals.
		* We express a negated literal as follows: -xLiteral
		* */

		// Add all clauses

		//1.
		//2.
		//3.
		//4.
		//5.
		//6.
		//7.

		// Solve the problem
		Search<BooleanVar> search = new DepthFirstSearch<BooleanVar>();
		SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(allVariables, new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());
		Boolean result = search.labeling(store, select);

		Character[][] results = new Character[M][N];
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				if(parkingLot[i][j].dom().value() == 1) {
					results[i][j] = '>';
				}
			}
		}

		if(result) {
			System.out.println("Solution: ");
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < N; j++) {
					System.out.println(results[i][j]);
				}
				System.out.println();
			}
		} else {
			System.out.println("*** No");
		}
		System.out.println();

		System.out.println("---------------------");
		System.out.println("Extra information");
		long endTime = System.currentTimeMillis();
		//long totalTime = endTime - startTime;
		System.out.println("Map: " + args[0]);
		System.out.println("#Ghosts: " + args[1]);
		//System.out.println("Execution time: " + (totalTime/1000.0));
	}



	// Adds a clause of 1 literal
	public static void addClause(SatWrapper satWrapper, int literal1, int literal2){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		clause.add(literal2);
		satWrapper.addModelClause(clause.toArray());
	}

	// Adds a clause of 2 literals
	public static void addClause(SatWrapper satWrapper, int literal1){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		satWrapper.addModelClause(clause.toArray());
	}

	// Adds a clause of 3 literals
	public static void addClause(SatWrapper satWrapper, int literal1, int literal2, int literal3){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		clause.add(literal2);
		clause.add(literal3);
		satWrapper.addModelClause(clause.toArray());
	}


}
