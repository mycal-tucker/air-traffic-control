import java.util.ArrayList;


public class AirplaneController extends Thread{
	private Simulator s; //S is shared with all other vehicle controllers, so it must
							//be protected.
	private Airplane plane; //is unique to the controller, so doesn't need locks
	private Airport startAirport;
	private Airport endAirport;
	private int departureTime;
	private ArrayList<Airplane> otherAirplanes;
	
	private boolean clearedToLand;
	private boolean destinationReached;
	
	final double requestLandThreshold = 5;
	final double commitLandThreshold = 5;
	
	public AirplaneController(Simulator s, Airplane p, Airport startAirport, Airport endAirport, int departureTime){
		if (p == null){
			throw new IllegalArgumentException("plane was null");
		}
		
		this.s = s;
		this.plane = p;
		this.startAirport = startAirport;
		this.endAirport = endAirport;
		this.departureTime = departureTime;
		
		this.otherAirplanes = new ArrayList<Airplane>(); //start as an empty list
		
		this.clearedToLand = false;
		this.destinationReached = false;
	}

	public void run(){
		double newTime = 0;
		double oldTime = 0;
		
		while (newTime < 100){
			
			/*
			 * Must lock on the simulator to make sure that simulator isn't accessed until
			 * this controller has seen a timestep and has updated appropriately.
			 * If it sees that s has updated all vehicles, notifies all threads waiting
			 * on s that it is now free.
			 */
			synchronized(this.s){
				newTime = this.s.getCurrentSec() + .001*this.s.getCurrentMSec();
				while (newTime == oldTime){
					try{
						//wait until time has changed
						s.wait();
						newTime = this.s.getCurrentSec() + .001*this.s.getCurrentMSec();
					}
					catch (InterruptedException ie){
						System.err.println("There was an interrupted exception");
						System.err.println(ie);
					}
				}
				Control newControl = this.getControl((int)(newTime*1000));
				this.plane.controlVehicle(newControl);

				oldTime = newTime;
				
				//since this vehicle is now updated, tell the simulator
				if (this.s.getNumNonUpdated() > 0){
					this.s.setNumNonUpdated(this.s.getNumNonUpdated()-1);
				}
				//if all vehicles are updated, tell all threads waiting on the simulator that
				//it's free now.
				if (this.s.getNumNonUpdated() == 0){
					this.s.notifyAll();
				}
			}
			
		}
	}

	public Control getControl(int time){

		
		//TODO: logic
		if (time < departureTime){ //get the plane pointed in the right direction
			//double[] oldPose = plane.getPosition();
			double[] newPose = new double[3];
			newPose[0] = startAirport.getX();
			newPose[1] = startAirport.getY();
			//change the angle to point from startAirport to endAirport 
			newPose[2] = Math.atan2(endAirport.getY() - startAirport.getY(), endAirport.getX() - startAirport.getX());
			//TODO unit tests for angle calculation
			this.plane.setPosition(newPose);
			if (this.plane.getFlying()){
				System.err.println("The plane is flying but it isn't the departure time yet. What?");
			}
			else{
				return new Control(0, 0);
				//Applying this control would be illegal, but since the plane isn't flying,
				//it won't try to use this control.
			}
		}
		if (time >= departureTime && !destinationReached){
			this.plane.setFlying(true);
			
			double targX = this.endAirport.getX();
			double targY = this.endAirport.getY();
			double[] currPosition = this.plane.getPosition();
			double currX = currPosition[0];
			double currY = currPosition[1];
			double currTheta = currPosition[2];
			
			//For now, ignore collision avoidance
			//TODO: collision avoidance
			
			double targTheta = Math.atan2(targY - currY, targX - currX);
			final double Krot = 1;
			double omega = Krot*(targTheta - currTheta);
			
			
			//if close to airport, request landing
			if (Math.hypot(targX - currX, targY - currY) < requestLandThreshold){
				if (this.clearedToLand && Math.hypot(targX - currX, targY - currY) < commitLandThreshold){
					this.endAirport.commitLand(this.plane);
					this.plane.setFlying(false); //it landed!
					this.destinationReached = true;
				}
				else if (!clearedToLand){
					this.clearedToLand = this.endAirport.requestLand(this.plane);
					if (!clearedToLand){
						return this.holdingPattern();
					}
				}
			}
			//TODO
			return new Control(10, omega);
			
		}
		if (destinationReached){
			//it got to the goal. make it disappear?
		}
		return new Control(10, 0);
	}
	
	private Control holdingPattern(){
		System.err.println("in a holding pattern");
		return new Control(5, Math.PI/4);
	}
	
	public Airport getStartAirport(){
		return this.startAirport;
	}
	
	public Airport getEndAirport(){
		return this.endAirport;
	}
	
	public int getDepartureTime(){
		return this.departureTime;
	}
	
	public void addOtherAirplane(Airplane a){
		this.otherAirplanes.add(a);
	}
}
