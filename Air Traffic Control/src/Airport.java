import java.util.ArrayList;


public class Airport {
	private double x;
	private double y;
	
	private int capacity;
	
	private ArrayList<Airplane> landingAirplanes;
	private ArrayList<Airplane> groundedAirplanes;
	
	public Airport(double x, double y, int capacity){
		this.x = x;
		this.y = y;
		this.capacity = capacity;
		
		landingAirplanes = new ArrayList<Airplane>();
		groundedAirplanes = new ArrayList<Airplane>();
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
	
	/**
	 * Two phase commit thing. Check capacity. For now, just return true. FIXME
	 * @param a
	 * @return
	 */
	public boolean requestLand(Airplane a){
		return true;
	}
	
	public void commitLand(Airplane a){
		if (!landingAirplanes.contains(a)){
			System.err.println("you can't commit a landing before requesting it.");
			return;
		}
		landingAirplanes.remove(a);
		groundedAirplanes.add(a);
	}
}
