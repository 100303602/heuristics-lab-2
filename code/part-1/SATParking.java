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
			for(int i=0; i<m; i++){
				String line = bufferedReader.readLine();
				for(int j=0; j<n; j++){
					board[i][j] = new SATParking.Car(line.split(" ")[j].charAt(0), Character.getNumericValue(line.split(" ")[j].charAt(1)));
				}
			}
			for(int i=0; i<board.length; i++){
				for(int j=0; j<board[i].length; j++){
					System.out.print(board[i][j]);
				}
			}
		} catch(FileNotFoundException ex) {
			System.out.println("File not found: '" + fileName + "'");
		} catch(IOException ex) {
			System.out.println("Error reading file: '" + fileName + "'");
		}

		/*
		Store store = new Store();
		SatWrapper satWrapper = new SatWrapper();
		store.impose(satWrapper);

		BooleanVar[][] parkingLot = new BooleanVar[m][n];
		
		for(int i=0; i<parkingLot.length; i++) {
			for(int j=0; j<parkingLot[i].length; j++) {
				satWrapper.register(parkingLot[i][j]);	
			}
		}*/


	}
	
}
