public class Car {
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

