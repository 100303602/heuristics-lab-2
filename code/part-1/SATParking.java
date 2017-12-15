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
	public static class Car {
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
		int M = 0;
		int N = 0;
		Car [][] board = null;

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String header = bufferedReader.readLine();
			M = Integer.parseInt(header.split(" ")[0]);
			N = Integer.parseInt(header.split(" ")[1]);
			board = new Car[M][N];
			
			System.out.println(M + " " + N);
			for(int i=0; i<M; i++) {
				String line = bufferedReader.readLine();
				for(int j=0; j<N; j++) {
					board[i][j] = new Car(line.split(" ")[j].charAt(0), Character.getNumericValue(line.split(" ")[j].charAt(1)));
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
		BooleanVar[][][] after = new BooleanVar[M][N][2];
		BooleanVar[][][] sameCategory = new BooleanVar[M][N][2];
		BooleanVar[][][] greaterCategory = new BooleanVar[M][N][2];

		for(int i=0; i<M; i++) {
			for(int j=0; j<N; j++) {
				for (int k=0; k<2; k++) {
					after[i][j][k] = new BooleanVar(store, "after(" + i + "," + j + "," + (k==0 ? "left" : "right") + ")");
					sameCategory[i][j][k] = new BooleanVar(store, "sameCategory(" + i + "," + j + "," + (k==0 ? "left" : "right") + ")");
					greaterCategory[i][j][k] = new BooleanVar(store, "greaterCategory(" + i + "," + j + "," + (k==0 ? "left" : "right") + ")");
				}
			}
		}

		// Collect all the variables for the SimpleSelect
		BooleanVar[] allVariables = new BooleanVar[(M*N*6)];
		int aux = 0;
		for(int j=0; j<M; j++) for(int k=0; k<N; k++) for(int l=0; l<2; l++, aux++) allVariables[aux] = after[j][k][l];
		for(int j=0; j<M; j++) for(int k=0; k<N; k++) for(int l=0; l<2; l++, aux++) allVariables[aux] = sameCategory[j][k][l];
		for(int j=0; j<M; j++) for(int k=0; k<N; k++) for(int l=0; l<2; l++, aux++) allVariables[aux] = greaterCategory[j][k][l];

		// Register all the variables in the SatWrapper
		for(int i=0; i<M; i++) for(int j=0; j<N; j++) for(int k=0; k<2; k++) {satWrapper.register(after[i][j][k]); satWrapper.register(sameCategory[i][j][k]); satWrapper.register(greaterCategory[i][j][k]);}

		// Obtain non-negated literals out of the binary variables
		int[][][] greaterCategoryLiterals = new int [M][N][2];
		int[][][] sameCategoryLiterals = new int[M][N][2];
		int[][][] afterLiterals = new int[M][N][2];

		for (int i=0; i<M; i++) {
			for (int j=0; j<N; j++) {
				if (board[i][j] != null) {
					if (j < N-1) {
						if(board[i][j].getOrder() < board[i][j+1].getOrder()) {
							afterLiterals[i][j][1] = satWrapper.cpVarToBoolVar(after[i][j][1], 1, true);
				            addClause(satWrapper, -afterLiterals[i][j][1]);
						} else {
							afterLiterals[i][j][1] = satWrapper.cpVarToBoolVar(after[i][j][1], 1, true);
							//afterLiterals[i][j][1] = satWrapper.cpVarToBoolVar(after[i][j][1], 0, true);
				            addClause(satWrapper, afterLiterals[i][j][1]);
						}
					}
					if (j == N-1) {
						afterLiterals[i][j][1] = satWrapper.cpVarToBoolVar(after[i][j][1], 1, true);
						//afterLiterals[i][j][1] = satWrapper.cpVarToBoolVar(after[i][j][1], 0, true);
                        addClause(satWrapper, -afterLiterals[i][j][1]);

						greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);		
                        addClause(satWrapper, greaterCategoryLiterals[i][j][1]);

						sameCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(sameCategory[i][j][1], 1, true);
                        addClause(satWrapper, sameCategoryLiterals[i][j][1]);
					}
					if (j > 0) {
						if(board[i][j].getOrder() < board[i][j-1].getOrder()) {
							afterLiterals[i][j][0] = satWrapper.cpVarToBoolVar(after[i][j][0], 1, true);
                            addClause(satWrapper, -afterLiterals[i][j][0]);
						} else {
							afterLiterals[i][j][0] = satWrapper.cpVarToBoolVar(after[i][j][0], 1, true);
							//afterLiterals[i][j][0] = satWrapper.cpVarToBoolVar(after[i][j][0], 0, true);
                            addClause(satWrapper, afterLiterals[i][j][0]);
						}
					}
					if (j == 0) {
						afterLiterals[i][j][0] = satWrapper.cpVarToBoolVar(after[i][j][0], 1, true);
						//afterLiterals[i][j][0] = satWrapper.cpVarToBoolVar(after[i][j][0], 0, true);
                        addClause(satWrapper, afterLiterals[i][j][0]);

						greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);	
                        addClause(satWrapper, greaterCategoryLiterals[i][j][1]);

						sameCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(sameCategory[i][j][0], 1, true);
                        addClause(satWrapper, sameCategoryLiterals[i][j][1]);
					}
					if (j < N-1) {
						switch(board[i][j].getCategory()) {
							case 'A':
								switch(board[i][j+1].getCategory()) {
									case 'A':
                                        sameCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(sameCategory[i][j][1], 1, true);
                                        addClause(satWrapper, sameCategoryLiterals[i][j][1]);
									case 'B':
                                        greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);	
                                        addClause(satWrapper, greaterCategoryLiterals[i][j][1]);
									case 'C':
                                        greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);
                                        addClause(satWrapper, greaterCategoryLiterals[i][j][1]);
									default:
								}
						
							case 'B':
								switch(board[i][j+1].getCategory()) {	
									case 'A':
                                        greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);	
                                        addClause(satWrapper, -greaterCategoryLiterals[i][j][1]);
                                        //case 'A': greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 0, true);	
									case 'B':
                                        sameCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(sameCategory[i][j][1], 1, true);
                                        addClause(satWrapper, sameCategoryLiterals[i][j][1]);
									case 'C':
                                        greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);
                                        addClause(satWrapper, greaterCategoryLiterals[i][j][1]);
									default:
								}

							case 'C':
								switch(board[i][j+1].getCategory()) {
									case 'A':
                                        greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);		
                                        addClause(satWrapper, -greaterCategoryLiterals[i][j][1]);
                                        //case 'A': greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 0, true);		
									case 'B':
                                        greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 1, true);	
                                        addClause(satWrapper, -greaterCategoryLiterals[i][j][1]);
                                        //case 'B': greaterCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][1], 0, true);	
									case 'C':
                                        sameCategoryLiterals[i][j][1] = satWrapper.cpVarToBoolVar(sameCategory[i][j][1], 1, true);
                                        addClause(satWrapper, sameCategoryLiterals[i][j][1]);
									default:
								}
						}
					}
					if (j > 0) {
						switch(board[i][j].getCategory()) {
							case 'A':
								switch(board[i][j-1].getCategory()) {
									case 'A':
                                        sameCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(sameCategory[i][j][0], 1, true);
                                        addClause(satWrapper, sameCategoryLiterals[i][j][0]);
									case 'B':
                                        greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);	
                                        addClause(satWrapper, greaterCategoryLiterals[i][j][0]);
									case 'C':
                                        greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);
                                        addClause(satWrapper, greaterCategoryLiterals[i][j][0]);
									default:
								}
						
							case 'B':
								switch(board[i][j-1].getCategory()) {	
									case 'A':
                                        greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);	
                                        addClause(satWrapper, -greaterCategoryLiterals[i][j][0]);
                                        //case 'A': greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 0, true);	
									case 'B':
                                        sameCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(sameCategory[i][j][0], 1, true);
                                        addClause(satWrapper, sameCategoryLiterals[i][j][0]);
									case 'C':
                                        greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);
                                        addClause(satWrapper, greaterCategoryLiterals[i][j][0]);
									default:
								}

							case 'C':
								switch(board[i][j-1].getCategory()) {
									case 'A':
                                        greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);		
                                        addClause(satWrapper, -greaterCategoryLiterals[i][j][0]);
                                        //case 'A': greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 0, true);		
									case 'B':
                                        greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 1, true);	
                                        addClause(satWrapper, -greaterCategoryLiterals[i][j][0]);
                                        //case 'B': greaterCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(greaterCategory[i][j][0], 0, true);	
									case 'C':
                                        sameCategoryLiterals[i][j][0] = satWrapper.cpVarToBoolVar(sameCategory[i][j][0], 1, true);
                                        addClause(satWrapper, sameCategoryLiterals[i][j][0]);
									default:
								}
						}
					}
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

//        for(int i = 0; i<M; i++) {
//		  	for(int j = 0; j<N; j++) {
//				addClause(satWrapper, sameCategoryLiterals[i][j][0]);
//				addClause(satWrapper, sameCategoryLiterals[i][j][1]);
//				addClause(satWrapper, -afterLiterals[i][j][0]);
//				addClause(satWrapper, -afterLiterals[i][j][1]);
//
//				addClause(satWrapper, greaterCategoryLiterals[i][j][0]);
//				addClause(satWrapper, greaterCategoryLiterals[i][j][1]);
//             }
//        }
		//1.
		  
//        for(int i = 0; i<M; i++) {
//		  	for(int j = 0; j<N; j++) {
//				addClause(satWrapper, greaterCategoryLiterals[i][j][0], greaterCategoryLiterals[i][j][1], sameCategoryLiterals[i][j][0], sameCategoryLiterals[i][j][1]);
//				addClause(satWrapper, greaterCategoryLiterals[i][j][0], greaterCategoryLiterals[i][j][1], sameCategoryLiterals[i][j][0], -afterLiterals[i][j][1]);
//				addClause(satWrapper, greaterCategoryLiterals[i][j][0], greaterCategoryLiterals[i][j][1], sameCategoryLiterals[i][j][1], -afterLiterals[i][j][0]);
//				addClause(satWrapper, greaterCategoryLiterals[i][j][0], greaterCategoryLiterals[i][j][1], -afterLiterals[i][j][0], -afterLiterals[i][j][1]);
//			}
//		  }

		// Solve the problem
		Search<BooleanVar> search = new DepthFirstSearch<BooleanVar>();
		SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(allVariables, new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());
		Boolean result = search.labeling(store, select);

//		boolean result = store.consistency();
		//System.out.println("Is the formula satisfiable?" +  result);

		//Character[][] results = new Character[M][N];
		//for (int i = 0; i < M; i++) {
			//for (int j = 0; j < N; j++) {
		//		if(parkingLot[i][j].dom().value() == 1) {
		//			results[i][j] = '>';
		//		}
			//}
		//}

		if(result) {
			System.out.println("Solution: ");
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < N; j++) {
		//			System.out.println(results[i][j]);
				}
		//		System.out.println();
			}
		} else {
			System.out.println("*** No");
		}
		System.out.println();

//		System.out.println("---------------------");
//		System.out.println("Extra information");
//		long endTime = System.currentTimeMillis();
		//long totalTime = endTime - startTime;
//		System.out.println("Map: " + args[0]);
//		System.out.println("#Ghosts: " + args[1]);
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
	
	// Adds a clause of 4 literals
	public static void addClause(SatWrapper satWrapper, int literal1, int literal2, int literal3, int literal4){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		clause.add(literal2);
		clause.add(literal3);
		clause.add(literal4);
		satWrapper.addModelClause(clause.toArray());
	}
}
