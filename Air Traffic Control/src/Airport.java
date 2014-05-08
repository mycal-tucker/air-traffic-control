import java.util.ArrayList;


public class Airport {
	private double x;
	private double y;
	
	private int maxCapacity;
	
	private ArrayList<Airplane> landingAirplanes;
	private ArrayList<Airplane> groundedAirplanes;
	
	private final double REQUEST_LAND_THRESHOLD = 10;
	
	public Airport(double x, double y, int capacity){
		this.x = x;
		this.y = y;
		this.maxCapacity = capacity;
		
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
		return this.maxCapacity - this.landingAirplanes.size() - this.groundedAirplanes.size();
	}
	
	/**
	 * Two phase commit thing. Check capacity. For now, just return true. FIXME
	 * must be synchronized so that two planes can't request landing at the same time.
	 * @param a
	 * @return
	 */
	public synchronized boolean requestLand(Airplane a){
		double[] planePos = a.getPosition();
		double distance = Math.hypot(planePos[0] - this.x, planePos[1] - this.y);
		if (distance > REQUEST_LAND_THRESHOLD){
			return false;
		}
		if (this.getCapacity() == 0){
			return false;
		}
		this.landingAirplanes.add(a);
		return true;
	}
	
	public void commitLand(Airplane a){
		if (!this.landingAirplanes.contains(a)){
			System.err.println("you can't commit a landing before requesting it.");
			return;
		}
		this.landingAirplanes.remove(a);
		this.groundedAirplanes.add(a);
		
	}
}
