import java.io.*;

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
								int m, n;
								SATParking.Car [][] board;

								try {
																FileReader fileReader = new FileReader(fileName);
																BufferedReader bufferedReader = new BufferedReader(fileReader);

																String header = bufferedReader.readLine();
																m = Integer.parseInt(header.split(" ")[0]);
																n = Integer.parseInt(header.split(" ")[1]);
																board = new SATParking.Car[m][n];

																System.out.println(m + " " + n);
																for(int i=0; i<m; i++) {
																								String line = bufferedReader.readLine();
																								for(int j=0; j<n; j++) {
																																board[i][j] = new SATParking.Car(line.split(" ")[j].charAt(0), Character.getNumericValue(line.split(" ")[j].charAt(1)));
																								}
																}
																for(int i=0; i<board.length; i++) {
																								for(int j=0; j<board[i].length; j++) {
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
								BooleanVar[][] parkingLot = new BooleanVar[m][n];
								for(int i=0; i<parkingLot.length; i++) {
																for(int j=0; j<parkingLot[i].length; j++) {
																								parkingLot[i][j] = new BooleanVar(store, "C(" + i + "," + j + ")");
																}
								}

								// Collect all the variables for the SimpleSelect
								BooleanVar[] allVariables = new BooleanVar[(m*n)];
								for (int i=0; i<(m*n); i++) {
																for(int j=0; j<parkingLot.length; j++) {
																								for(int k=0; k<parkingLot[i].length; k++) {
																																allVariables[i] = parkingLot[j][k];
																								}
																}
								}

								// Register all the variables in the SatWrapper
								for(int i=0; i<parkingLot.length; i++) {
																for(int j=0; j<parkingLot[i].length; j++) {
																								satWrapper.register(parkingLot[i][j]);
																}
								}

								// Obtain non-negated literals out of the binary variables
								int[][] carLiterals = new int[m][n];
								for (int i=0; i<carLiterals.length; i++) {
																for (int j=0; j<carLiterals[0].length; j++) {
																								carLiterals[i][j] = satWrapper.cpVarToBoolVar(parkingLot[i][j], 1, true);
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

								Character[][] results = new Character[m][n];
								for (int i = 0; i < m; i++) {
																for (int j = 0; j < m; j++) {
																								results[i][j] = board[i][j];
																								if(parkingLot[i][j].dom().value() == 1) {
																																results[i][j] = 'C';
																								}
																}
								}

								if(result) {
																System.out.println("Solution: ");
																for (int i = 0; i < m; i++) {
																								for (int j = 0; j < m; j++) {
																																System.out.println(results[i][j]);
																								}
																								System.out.println();
																}
								} else {
																System.out.println("*** No")
								}
								System.out.println();

								System.out.println("---------------------");
								System.out.println("Extra information");
								long endTime = System.currentTimeMillis();
								long totalTime = endTime - startTime;
								System.out.println("Map: " + args[0]);
								System.out.println("#Ghosts: " + args[1]);
								System.out.println("Execution time: " + (totalTime/1000.0));
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
