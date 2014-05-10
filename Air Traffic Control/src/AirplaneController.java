import java.util.ArrayList;


public class AirplaneController extends Thread{
	final double requestLandThreshold = 5;
	final double commitLandThreshold = 5;
	
	private Simulator s; //S is shared with all other vehicle controllers, so it must
							//be protected.
	private Airplane plane; //is unique to the controller, so doesn't need locks
	private Airport startAirport;
	private Airport endAirport;
	private int departureTime;
	
	private boolean inHoldingPattern;
	private int holdingStartTime;
	
	private ArrayList<Airplane> otherAirplanes;
	
	private boolean clearedToLand;
	private boolean destinationReached;
	
	private final double COLLISION_AVOID_THRESHOLD = 5;
	private final double Krot = 1;
	private final double linSpeed = 5;
	
	
	
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
		if (time < departureTime){ //get the plane pointed in the right direction
			return this.beforeTakeoff();
		}
		if (time >= departureTime && !destinationReached){
			this.plane.setFlying(true);
			
			Control avoidOtherPlanes = this.collisionAvoidance();
			if (avoidOtherPlanes != null){
				//System.out.println("just dodged a collision!");
				//for now, ignore collision avoidance so I can focus on holding patterns
				//FIXME
				//return avoidOtherPlanes;
			}
			//if avoidOtherPlanes is null then we don't need to worry about collisions yet.
			double targX = this.endAirport.getX();
			double targY = this.endAirport.getY();
			double[] currPosition = this.plane.getPosition();
			double currX = currPosition[0];
			double currY = currPosition[1];
			double currTheta = currPosition[2];
			double targTheta = Math.atan2(targY - currY, targX - currX);
			double omega = omegaGain(targTheta, currTheta);
			//only for debugging
//			if (this.plane.getPlaneName().equals("plane3")){
//				System.out.println("curr: " + currTheta);
//				System.out.println("targ: " + targTheta);
//				System.out.println("omega: " + omega);
//				System.out.println();
//			}
			
			
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
						this.holdingStartTime = this.s.getCurrentSec()*1000 + this.s.getCurrentMSec();
						return this.holdingPattern();
					}
				}
			}
			return new Control(this.linSpeed, omega);
			
		}
		if (destinationReached){
			//TODO it got to the goal. make it disappear?
		}
		return new Control(this.linSpeed, 0);
	}
	
	
	private Control beforeTakeoff(){
		//double[] oldPose = plane.getPosition();
		double[] newPose = new double[3];
		newPose[0] = startAirport.getX();
		newPose[1] = startAirport.getY();
		//change the angle to point from startAirport to endAirport 
		newPose[2] = Math.atan2(endAirport.getY() - startAirport.getY(), endAirport.getX() - startAirport.getX());
		this.plane.setPosition(newPose);
		if (this.plane.getFlying()){
			System.err.println("The plane is flying but it isn't the departure time yet. What?");
		}
		return new Control(0, 0);
		//Applying this control would be illegal, but since the plane isn't flying,
		//it won't try to use this control.
	}
	
	private Control collisionAvoidance(){
		double[] currPosition = this.plane.getPosition();
		double currX = currPosition[0];
		double currY = currPosition[1];
		double currTheta = currPosition[2];
		
		Airplane closest = null;
		double minDistance = Integer.MIN_VALUE;
		for (Airplane temp: this.otherAirplanes){
			double[] tempPosition = temp.getPosition();
			double tempDistance = Math.hypot(currY - tempPosition[1], currX - tempPosition[0]);
			if (closest == null || minDistance > tempDistance){
				closest = temp;
				minDistance = tempDistance;
			}
		}
		if (closest == null || minDistance > this.COLLISION_AVOID_THRESHOLD){
			return null;
		}
		
		else{ //just turn right. Can make many improvements
//			double[] otherPosition = closest.getPosition();
//			double otherX = otherPosition[0];
//			double otherY = otherPosition[1];
//			double otherTheta = otherPosition[2];
//			double targTheta = Math.atan2(otherY - currY, otherX - currX);
//			double omega = -omegaGain(targTheta, currTheta) + .1;
//			//return new Control(this.linSpeed, omega);
			return new Control(this.linSpeed, -.5);
		}
	}

	@SuppressWarnings("unused")
	private Control closeToEdge(){
		double[] pose = this.plane.getPosition();
		double x = pose[0];
		double y = pose[1];
		double theta = pose[2];
		double desiredTheta = 0; //overwritten?
		boolean closeToEdge = false;

		if (x< 10){
			if (y < 10){
				desiredTheta = Math.PI/4;
			}
			else if (y > 90){
				desiredTheta = 7*Math.PI/4;
			}
			else{
				desiredTheta = 0;
			}
			closeToEdge = true;
		}
		else if (x > 90){
			if (y < 10){
				desiredTheta = 3*Math.PI/4;
			}
			else if(y > 90){
				desiredTheta = 5*Math.PI/4;
			}
			else{
				desiredTheta = Math.PI;
			}
			closeToEdge = true;
		}
		else if (y < 10){
			desiredTheta = Math.PI/2;
			closeToEdge = true;
		}
		else if (y > 90){
			desiredTheta = 3*Math.PI/2;
			closeToEdge = true;
		}

		if (closeToEdge){
			double linSpeed = 5*Math.random() + 5;
			double angGain = .3;
			double angError = (desiredTheta - theta)%(2*Math.PI);
			double rotVel = angGain*angError;
			return new Control(linSpeed, rotVel);
		}
		return null;
	}
	
	private Control holdingPattern(){
		//TODO logic
		//System.out.println("in a holding pattern");
		return new Control(this.linSpeed, Math.PI/4);
	}
	
	public double omegaGain(double desired, double actual){
		if (desired < 0){
			desired += 2*Math.PI;
		}
		if (actual < 0){
			actual += 2*Math.PI;
		}
		double diff = (desired-actual)%(2*Math.PI);
		if (diff > Math.PI){
			diff = -1*(diff - Math.PI);
		}
		else if (diff < -Math.PI){
			diff = -1*(diff + Math.PI);
		}
		return this.Krot*diff;
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
