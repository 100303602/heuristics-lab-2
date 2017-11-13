import java.io.File;
import java.io.IOException;
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

/** 
 * @author �lvaro C�ceres Mu�oz
 * @author Andr�s Gonz�lez L�pez
 * */

/**
 * @param program_name
 * @param maze_map.lab
 * @param number_of_ghosts
 * */

public class SATPacman {
		
	public static void main(String args[]) throws IOException {
        // Measure execution time
		long startTime = System.currentTimeMillis();

        // Arguments provided by the user
		int ghosts = Integer.parseInt(args[1]);
		final String filename =args[0] + ".lab";

        // 0. Load the file and convert it into a list of lists
		File file = new File(filename);
		Scanner scanner = new Scanner(file);
		List<String> mapList= new ArrayList<String>();
		while(scanner.hasNextLine()) {
			mapList.add(scanner.nextLine());
		}

		Character[][] mapArray = new Character[mapList.size()][mapList.get(0).length()];
		for(int i = 0; i < mapList.size(); i++) {
			for (int j = 0; j < mapList.get(0).length(); j++) {
				mapArray[i][j] = mapList.get(i).charAt(j);
			}
		}

		int rows = mapArray.length;
		int cols = mapArray[0].length;

        // 1. Create the store and the sat wrapper to encode the problem
		Store store = new Store();
		SatWrapper satWrapper = new SatWrapper();
		store.impose(satWrapper);

        // 2. Create the binary variables that represent the presence
        // of pacman & ghosts in map
		BooleanVar pacmanVars[][] = new BooleanVar[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				pacmanVars[i][j] = new BooleanVar(store, "P(" + i + "," + j + ")");
			}
		}

		BooleanVar ghostVars[][][] = new BooleanVar[rows][cols][ghosts];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < ghosts; k++) {
					ghostVars[i][j][k] = new BooleanVar(store, "G" + k + "(" + i + "," + j + ")");
				}
			}
		}

        // 3. Collect all the variables for the SimpleSelect
		BooleanVar[] allVariables = new BooleanVar[(rows*cols)*(1+ghosts)];
		int aux = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				allVariables[aux] = pacmanVars[i][j];
				aux++;
			}
		}
		for (int k = 0; k < ghosts; k++) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					allVariables[aux] = ghostVars[i][j][k];
					aux++;
				}
			}
		}

        // 4. Register all the variables in the sat wrapper
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				satWrapper.register(pacmanVars[i][j]);
			}
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < ghosts; k++) {
					satWrapper.register(ghostVars[i][j][k]);
				}
			}
		}

        // 5. Obtain the non-negated literals out of the binary variables
		int pacmanLiterals [][] = new int[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				pacmanLiterals[i][j] = satWrapper.cpVarToBoolVar(pacmanVars[i][j], 1, true);
			}
		}

		int ghostLiterals [][][] = new int[rows][cols][ghosts];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < ghosts; k++) {
					ghostLiterals[i][j][k] = satWrapper.cpVarToBoolVar(ghostVars[i][j][k], 1, true);
				}
			}
		}

		/* The problem will be defined in CNF form, thus, every clause
		 * of the problem will be added one by one.
		 * Each clause will consist in the disjunction of a set of the
		 * previously defined literals.
		 * We express a negated literal as follows: -xLiteral
		 * */

        // 6. Add all clauses

        // 6.1.a) Pacman and ghosts can only be placed in empty cells
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (mapArray[i][j].equals('%') || mapArray[i][j].equals('O')) {
					addClause(satWrapper, -pacmanLiterals[i][j]);
					for (int k = 0; k < ghosts; k++) {
						addClause(satWrapper, -ghostLiterals[i][j][k]);
					}
				}
			}
		}

        // 6.1.b) One ghost per row at most
		for (int g1 = 0; g1 < ghosts; g1++) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					for (int g2 = 0; g2 < ghosts; g2++) {
						for (int k = 0; k < cols; k++) {
							if(g1!=g2){
								addClause(satWrapper, -ghostLiterals[i][j][g1], -ghostLiterals[i][k][g2]);
							}
						}
					}
				}
			}
		}

        // 6.1.c) No ghosts around Pacman
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < ghosts; k++) {
                    if(j+1<cols){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i][j+1][k]);
                    }
                    if(j-1>=0){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i][j-1][k]);
                    }
                    if(i+1<rows){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i+1][j][k]);
                    }
                    if(i+1<rows && j+1<cols){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i+1][j+1][k]);
                    }
                    if(i+1<rows && j-1>=0){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i+1][j-1][k]);
                    }
                    if(i-1>=0){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i-1][j][k]);
                    }
                    if(i-1>=0 && j+1<cols){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i-1][j+1][k]);
                    }
                    if(i-1>=0 && j-1>=0){
                        addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i-1][j-1][k]);
                    }

				}
			}
		}

        // 6.2.a) There must be at least one Pacman
		IntVec auxClause = new IntVec(satWrapper.pool);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				auxClause.add(pacmanLiterals[i][j]);
			}
		}
		satWrapper.addModelClause(auxClause.toArray());

        // 6.2.b) There cannot be more than one Pacman
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int l = 0; l < rows; l++) {
					for (int m = 0; m < cols; m++) {
						if(!(i==l && j==m)) {
                            // A -> ~B => ~A v ~B
							addClause(satWrapper, -pacmanLiterals[i][j], -pacmanLiterals[l][m]);
						}
					}
				}
			}
		}

        // 6.2.c) There must be exactly the specified number of ghosts
		IntVec[] auxClause2 = new IntVec[ghosts];
		for (int k = 0; k < ghosts; k++) {
			auxClause2[k] = new IntVec (satWrapper.pool);
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					auxClause2[k].add(ghostLiterals[i][j][k]);
				}
			}
			satWrapper.addModelClause(auxClause2[k].toArray());
		}

        // 6.2.d) Pacman cannot be placed on the same cell of any ghost
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < ghosts; k++) {
					addClause(satWrapper, -pacmanLiterals[i][j], -ghostLiterals[i][j][k]);
				}
			}
		}

        // 6.2.e) Ghosts cannot be placed on the same cell of any other ghost
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < ghosts; k++) {
					for (int l = 0; l < ghosts; l++) {
						if(k!=l) {
							addClause(satWrapper, -ghostLiterals[i][j][k], -ghostLiterals[i][j][l]);
						}
					}
				}
			}
		}

        // 7. Solve the problem
	    Search<BooleanVar> search = new DepthFirstSearch<BooleanVar>();
		SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(allVariables, new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());
		Boolean result = search.labeling(store, select);

		Character[][] results = new Character[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				results[i][j] = mapArray[i][j];
				if(pacmanVars[i][j].dom().value() == 1) {
					results[i][j] = 'P';
				}
				for (int k = 0; k < ghosts; k++) {
					if(ghostVars[i][j][k].dom().value() == 1) {
						results[i][j] = Integer.toString(k).charAt(0);
					}
				}
			}
		}
		if (result) {
			System.out.println("Solution: ");
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					System.out.print(results[i][j]);
                    // file.write(results[i][j]);
				}
				System.out.println();
                // file.write('\n');
			}
		} else{
			System.out.println("*** No");
		}
		System.out.println();
        // file.close()... hay que cerrar el archivo?
		
        // Print results
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

	public static void printArray(int x, int y){
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {

			}
		}
	}
}
