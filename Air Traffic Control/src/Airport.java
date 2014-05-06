
public class Airport {
	private double x;
	private double y;
	
	private int capacity;
	
	public Airport(double x, double y, int capacity){
		this.x = x;
		this.y = y;
		this.capacity = capacity;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public double getCapacity(){
		return this.capacity;
	}
}
