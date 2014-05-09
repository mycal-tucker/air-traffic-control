import java.util.ArrayList;


public class Airport {
	private double x;
	private double y;
	
	private int maxCapacity;
	
	private ArrayList<Airplane> landingAirplanes;
	private ArrayList<Airplane> groundedAirplanes;
	
	private final double REQUEST_LAND_THRESHOLD = 10;
	
	private String name;
	
	private long lastLandingTime;
	private final long landingTimeWindow = 100; //milliseconds
	
	public Airport(double x, double y, int capacity){
		this.x = x;
		this.y = y;
		this.maxCapacity = capacity;
		this.lastLandingTime = 0; //will be overwritten once something lands.
		
		landingAirplanes = new ArrayList<Airplane>();
		groundedAirplanes = new ArrayList<Airplane>();
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public void setName(String s){
		this.name = s.toUpperCase();
	}
	
	public double getCapacity(){
		return this.maxCapacity - this.landingAirplanes.size() - this.groundedAirplanes.size();
	}
	
	/**
	 * Spawn airplane a at airport if there's room.
	 * @param a
	 * @return
	 */
	public synchronized boolean spawnAirplane(Airplane a){
		if (this.getCapacity() == 0){
			return false;
		}
		this.groundedAirplanes.add(a);
		double[] startPosition = new double[3];
		startPosition[0] = this.x;
		startPosition[1] = this.y;
		startPosition[2] = 0; //arbitrary value since will face destination eventually
		a.setPosition(startPosition);
		return true;
	}
	
	/**
	 * Two phase commit thing. Check capacity and all that
	 * must be synchronized so that two planes can't request landing at the same time.
	 * @param a
	 * @return
	 */
	public synchronized boolean requestLand(Airplane a){
		if (this.landingAirplanes.contains(a)){ //if you were cleared to land, you are still
			//cleared to land
			return true;
		}

		double[] planePos = a.getPosition();
		double distance = Math.hypot(planePos[0] - this.x, planePos[1] - this.y);
		if (distance > REQUEST_LAND_THRESHOLD){//if you are too far away, you aren't cleared to
			//land
			return false;
		}
		if (this.getCapacity() == 0){ //if we don't have spots, you're not cleared to land
			return false;
		}
		long currTime = System.currentTimeMillis();
		if (currTime <= this.lastLandingTime + this.landingTimeWindow){
			return false; //have to wait until safe.
		}
		this.landingAirplanes.add(a);
		return true;
	}
	
	/**
	 * It's the two phase commit thing. If a plane has requested and been granted access to
	 * land, this method shall cause the airplane to land.
	 * @param a
	 */
	public void commitLand(Airplane a){
		if (!this.landingAirplanes.contains(a)){
			System.err.println("you can't commit a landing before requesting it.");
			return;
		}
		this.landingAirplanes.remove(a);
		this.groundedAirplanes.add(a);
		a.setFlying(false);
		this.lastLandingTime = System.currentTimeMillis();
	}
	
	public void unrequestLand(Airplane a){
		if (this.landingAirplanes.contains(a)){
			this.landingAirplanes.remove(a);
		}
		else if (this.groundedAirplanes.contains(a)){
			System.err.println("you already landed. why are you de-requesting to land?");
		}
	}
	
	public void takeoff(Airplane a){
		if (this.groundedAirplanes.contains(a)){
			this.groundedAirplanes.remove(a);
			a.setFlying(true);
		}
		else{
			System.err.println("That airplane wasn't here to begin with so can't take off");
		}
	}
}
